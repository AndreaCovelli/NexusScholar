import sqlite3
import json
import csv
import os

# --- CONFIGURATION ---
DB_PATH = 'enrichment_cache.db'
INPUT_JSONL = 'dblp_ai_ml_2015_2025.jsonl'
OUTPUT_DIR = 'import_files'
MONGO_DIR = os.path.join(OUTPUT_DIR, 'mongodb')
NEO4J_DIR = os.path.join(OUTPUT_DIR, 'neo4j')

# Helper to ensure directories exist
os.makedirs(MONGO_DIR, exist_ok=True)
os.makedirs(NEO4J_DIR, exist_ok=True)

# Global ID Maps and Entity Storage
# We use (Name, S2_AuthorId) as the key for better disambiguation
author_map_by_s2id = {}  # S2_ID -> Internal_ID (Primary)
author_map_by_name = {}  # Name -> Internal_ID (Fallback for DBLP-only)
venue_map = {}
topic_map = {}
paper_s2_to_internal_map = {} # S2_PaperId -> Internal_Paper_ID

papers = {}
authors = {}
venues = {}
topics = {}

# Relationships (Neo4j)
relationships = {
    'AUTHORED': [],
    'PUBLISHED_IN': [],
    'HAS_TOPIC': [],
    'CITES': []
}

# ID Counters
paper_id_counter = 0
author_id_counter = 0
venue_id_counter = 0
topic_id_counter = 0

# --- HELPER FUNCTIONS ---

def clean_text(text):
    """
    Cleans text: ensures string type, removes null bytes and specific 
    Unicode line terminators (U+2028, U+2029).
    """
    if not text:
        return None
    # Ensure input is string before replacing
    return str(text).replace('\0', '').replace('\u2028', ' ').replace('\u2029', ' ').strip()

def get_venue_id(venue_name):
    global venue_id_counter
    cleaned_name = clean_text(venue_name)
    if not cleaned_name: return None
    
    if cleaned_name not in venue_map:
        venue_id_counter += 1
        vid = f"V{venue_id_counter:05d}"
        venue_map[cleaned_name] = vid
        venues[vid] = {'_id': vid, 'name': cleaned_name}
    return venue_map[cleaned_name]

def get_topic_id(topic_name):
    global topic_id_counter
    cleaned_name = clean_text(topic_name)
    if not cleaned_name: return None
        
    if cleaned_name not in topic_map:
        topic_id_counter += 1
        tid = f"T{topic_id_counter:05d}"
        topic_map[cleaned_name] = tid
        topics[tid] = {'_id': tid, 'name': cleaned_name}
    return topic_map[cleaned_name]

def get_author_id(name, s2_author_id=None):
    global author_id_counter
    cleaned_name = clean_text(name)
    if not cleaned_name: return None

    # --- WATERFALL LOGIC IMPLEMENTATION ---
    
    # 1. Priority: Semantic Scholar ID (The definitive identifier)
    if s2_author_id:
        if s2_author_id in author_map_by_s2id:
            aid = author_map_by_s2id[s2_author_id]
            # Optional: Update the stored name if the current one is "better" (e.g., longer)
            if len(cleaned_name) > len(authors[aid]['name']):
                authors[aid]['name'] = cleaned_name
            return aid
        
    # 2. Fallback: DBLP Name (Only executed if S2ID is NOT present)
    elif cleaned_name in author_map_by_name:
        return author_map_by_name[cleaned_name]

    # 3. New Author Found
    author_id_counter += 1
    aid = f"A{author_id_counter:05d}"
    authors[aid] = {
        '_id': aid, 
        'name': cleaned_name, 
        's2_author_id': s2_author_id,
        'total_publications': 0,
        'total_citations': 0
    }
    
    # Register in the appropriate map for future lookups
    if s2_author_id:
        author_map_by_s2id[s2_author_id] = aid
    else:
        # Only register in the name map if S2ID is absent
        author_map_by_name[cleaned_name] = aid
        
    return aid

# --- PHASE 3.1: PROCESSING AND INTEGRATION ---

def process_records():
    global paper_id_counter
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()

    print("Loading enrichment cache into memory...")
    # Load the cache into memory for fast lookup
    cursor.execute("SELECT dblp_key, status, s2_data FROM enrichment")
    cache = {row[0]: (row[1], row[2]) for row in cursor.fetchall()}
    conn.close()

    print("Starting record processing, integration, and ID assignment...")
    # Iterate through the original DBLP file as the primary source
    # This ensures we process all papers in our scope, even those missed by S2
    with open(INPUT_JSONL, 'r', encoding='utf-8') as f:
        for line in f:
            dblp_record = json.loads(line)
            dblp_key = dblp_record['dblp_key']
            
            # 1. Assign Paper ID
            paper_id_counter += 1
            pid = f"P{paper_id_counter:06d}"
            
            # 2. Retrieve Cache Data
            status, s2_data_raw = cache.get(dblp_key, ('MISSING', None))
            s2_data = json.loads(s2_data_raw) if s2_data_raw else None

            # 3. Build the Paper Object (Merging DBLP and S2)
            paper = {
                '_id': pid,
                'title': clean_text(dblp_record['title']),
                'year': dblp_record['year'],
                'dblp_key': dblp_key,
                'doi': clean_text(dblp_record.get('doi')),
                'abstract': None,
                'fields_of_study': [], # Store names for MongoDB analytics
                'citation_count': 0,
                'author_ids': [],
                'venue_id': None,
                '_s2_citations_raw': [] # Temporary storage for Phase 3.2
            }

            # 4. Process Venue (From DBLP)
            venue_id = get_venue_id(dblp_record['venue'])
            paper['venue_id'] = venue_id
            if venue_id:
                relationships['PUBLISHED_IN'].append((pid, venue_id))

            # 5. Integrate Semantic Scholar Data (If successful)
            if status == 'SUCCESS' and s2_data:
                paper['abstract'] = clean_text(s2_data.get('abstract'))
                
                # Robustly handle citation count
                try:
                    paper['citation_count'] = int(s2_data.get('citationCount') or 0)
                except (ValueError, TypeError):
                    paper['citation_count'] = 0

                s2_paper_id = s2_data.get('paperId')

                # Register S2 ID for graph resolution later
                if s2_paper_id:
                    paper_s2_to_internal_map[s2_paper_id] = pid

                # Process Topics (Fields of Study)
                for topic in s2_data.get('fieldsOfStudy') or []:
                    tid = get_topic_id(topic)
                    if tid:
                        # Store the name in the paper doc for MongoDB
                        paper['fields_of_study'].append(topics[tid]['name'])
                        relationships['HAS_TOPIC'].append((pid, tid))

                # Store raw citation IDs for later resolution
                paper['_s2_citations_raw'] = [c.get('paperId') for c in s2_data.get('citations') or [] if c.get('paperId')]
                
                # Process Authors (Prefer S2 data as it has IDs)
                s2_authors = s2_data.get('authors') or []
                if s2_authors:
                    for author in s2_authors:
                        aid = get_author_id(author.get('name'), author.get('authorId'))
                        if aid:
                            paper['author_ids'].append(aid)
                            relationships['AUTHORED'].append((aid, pid))
                
                # Fallback if S2 author list is empty but DBLP has authors
                elif dblp_record.get('authors'):
                     for author_name in dblp_record['authors']:
                        # Use None for S2 ID during fallback
                        aid = get_author_id(author_name, None)
                        if aid:
                            paper['author_ids'].append(aid)
                            relationships['AUTHORED'].append((aid, pid))

            # 6. Fallback for Non-Enriched Papers (Use DBLP Authors)
            else:
                for author_name in dblp_record.get('authors', []):
                    aid = get_author_id(author_name, None)
                    if aid:
                        paper['author_ids'].append(aid)
                        relationships['AUTHORED'].append((aid, pid))

            papers[pid] = paper

    print(f"Processed {len(papers)} papers, {len(authors)} authors, {len(venues)} venues, {len(topics)} topics.")

# --- PHASE 3.2: CITATION RESOLUTION AND METRIC CALCULATION ---

def resolve_citations_and_metrics():
    print("Resolving internal citation graph and calculating author metrics...")
    count = 0
    # Second pass: Now that all internal IDs are assigned, resolve links and calculate metrics.
    for pid, paper in papers.items():
        # 1. Resolve Citations
        for cited_s2_id in paper.get('_s2_citations_raw', []):
            # Check if the cited paper exists within OUR dataset
            if cited_s2_id in paper_s2_to_internal_map:
                cited_internal_id = paper_s2_to_internal_map[cited_s2_id]
                # Ensure we don't cite ourselves
                if pid != cited_internal_id:
                    relationships['CITES'].append((pid, cited_internal_id))
                    count += 1
        
        # Clean up temporary data
        if '_s2_citations_raw' in paper:
            del paper['_s2_citations_raw']
            
        # 2. Calculate Author Metrics (for MongoDB)
        citation_count = paper.get('citation_count', 0)
        for author_id in paper.get('author_ids', []):
            if author_id in authors:
                authors[author_id]['total_publications'] += 1
                authors[author_id]['total_citations'] += citation_count

    print(f"Resolved {count} internal citations and updated author metrics.")

# --- PHASE 3.3: OUTPUT GENERATION ---

def write_mongodb_files():
    print("Writing MongoDB JSONL files...")
    
    # Papers
    with open(os.path.join(MONGO_DIR, 'papers.jsonl'), 'w', encoding='utf-8') as f:
        for paper in papers.values():
            f.write(json.dumps(paper, ensure_ascii=False) + '\n')
            
    # Authors
    with open(os.path.join(MONGO_DIR, 'authors.jsonl'), 'w', encoding='utf-8') as f:
        for author in authors.values():
            f.write(json.dumps(author, ensure_ascii=False) + '\n')

    # Venues
    with open(os.path.join(MONGO_DIR, 'venues.jsonl'), 'w', encoding='utf-8') as f:
        for venue in venues.values():
            f.write(json.dumps(venue, ensure_ascii=False) + '\n')

def write_neo4j_files():
    print("Writing Neo4j CSV files (Optimized for neo4j-admin import)...")

    # Helper for CSV writing
    def write_csv(filename, header, data_generator):
        filepath = os.path.join(NEO4J_DIR, filename)
        try:
            with open(filepath, 'w', encoding='utf-8', newline='') as f:
                # Use QUOTE_MINIMAL for efficiency
                writer = csv.writer(f, quoting=csv.QUOTE_MINIMAL)
                writer.writerow(header)
                for row in data_generator:
                    writer.writerow(row)
        except IOError as e:
            print(f"Error writing file {filepath}: {e}")

    # --- NODES ---
    # Headers define the ID Space and Node Label (e.g., :ID(Paper))
    
    # Papers
    # We include citationCount in the graph for potential weighted PageRank analysis
    write_csv('nodes_papers.csv', 
              ['paperId:ID(Paper)', 'title', 'year:int', 'citationCount:int'],
              ([p['_id'], p['title'], p['year'], p['citation_count']] for p in papers.values()))

    # Authors
    write_csv('nodes_authors.csv',
              ['authorId:ID(Author)', 'name'],
              ([a['_id'], a['name']] for a in authors.values()))

    # Venues
    write_csv('nodes_venues.csv',
              ['venueId:ID(Venue)', 'name'],
              ([v['_id'], v['name']] for v in venues.values()))

    # Topics
    write_csv('nodes_topics.csv',
              ['topicId:ID(Topic)', 'name'],
              ([t['_id'], t['name']] for t in topics.values()))

    # --- RELATIONSHIPS ---
    # Headers define the Start Node ID Space and End Node ID Space
    
    # AUTHORED
    write_csv('rels_authored.csv',
              [':START_ID(Author)', ':END_ID(Paper)'],
              relationships['AUTHORED'])

    # CITES
    write_csv('rels_cites.csv',
              [':START_ID(Paper)', ':END_ID(Paper)'],
              relationships['CITES'])

    # PUBLISHED_IN
    write_csv('rels_published_in.csv',
              [':START_ID(Paper)', ':END_ID(Venue)'],
              relationships['PUBLISHED_IN'])

    # HAS_TOPIC
    write_csv('rels_has_topic.csv',
              [':START_ID(Paper)', ':END_ID(Topic)'],
              relationships['HAS_TOPIC'])

if __name__ == "__main__":
    print("--- Phase 3: Integration and Formatting ---")
    process_records()
    resolve_citations_and_metrics()
    write_mongodb_files()
    write_neo4j_files()
    print("Phase 3 Complete. Import files are ready in the 'import_files' directory.")
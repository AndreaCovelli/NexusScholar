import sqlite3
import json
import csv
import os

# --- CONFIGURATION ---
DB_PATH = 'enrichment_cache.db'
INPUT_JSONL = 'dblp_ai_ml_2015_2025.jsonl'
OUTPUT_DIR = 'import_files'
MONGO_SUBDIR = 'mongodb'
NEO4J_SUBDIR = 'neo4j'


def clean_text(text):
    """
    Cleans text: ensures string type, removes null bytes and specific
    Unicode line terminators (U+2028, U+2029).
    """
    if not text:
        return None
    # Ensure input is string before replacing
    return str(text).replace('\0', '').replace('\u2028', ' ').replace('\u2029', ' ').strip()


class IntegrationPipeline:
    """
    Encapsulates all state and logic for:
    - Loading enrichment cache
    - Processing records and assigning IDs
    - Resolving citations and author metrics
    - Writing MongoDB and Neo4j export files

    Replaces former global variables with instance attributes.
    """

    def __init__(
            self,
            db_path: str = DB_PATH,
            input_jsonl: str = INPUT_JSONL,
            output_dir: str = OUTPUT_DIR,
            mongo_subdir: str = MONGO_SUBDIR,
            neo4j_subdir: str = NEO4J_SUBDIR,
    ) -> None:
        # Paths / configuration
        self.db_path = db_path
        self.input_jsonl = input_jsonl
        self.output_dir = output_dir
        self.mongo_dir = os.path.join(output_dir, mongo_subdir)
        self.neo4j_dir = os.path.join(output_dir, neo4j_subdir)

        # Helper to ensure directories exist
        os.makedirs(self.mongo_dir, exist_ok=True)
        os.makedirs(self.neo4j_dir, exist_ok=True)

        # Instance ID Maps and Entity Storage
        # We use (Name, S2_AuthorId) as the key for better disambiguation
        self.author_map_by_s2id = {}  # S2_ID -> Internal_ID (Primary)
        self.author_map_by_name = {}  # Name -> Internal_ID (Fallback for DBLP-only)
        self.venue_map = {}
        self.topic_map = {}
        self.paper_s2_to_internal_map = {}  # S2_PaperId -> Internal_Paper_ID

        self.papers = {}
        self.authors = {}
        self.venues = {}
        self.topics = {}

        # Relationships (Neo4j)
        self.relationships = {
            'AUTHORED': [],
            'PUBLISHED_IN': [],
            'HAS_TOPIC': [],
            'CITES': []
        }

        # ID Counters
        self.paper_id_counter = 0
        self.author_id_counter = 0
        self.venue_id_counter = 0
        self.topic_id_counter = 0

    # --- HELPER METHODS ---

    def get_venue_id(self, venue_name):
        cleaned_name = clean_text(venue_name)
        if not cleaned_name:
            return None

        if cleaned_name not in self.venue_map:
            self.venue_id_counter += 1
            vid = f"V{self.venue_id_counter:05d}"
            self.venue_map[cleaned_name] = vid
            self.venues[vid] = {'_id': vid, 'name': cleaned_name}
        return self.venue_map[cleaned_name]

    def get_topic_id(self, topic_name):
        cleaned_name = clean_text(topic_name)
        if not cleaned_name:
            return None

        if cleaned_name not in self.topic_map:
            self.topic_id_counter += 1
            tid = f"T{self.topic_id_counter:05d}"
            self.topic_map[cleaned_name] = tid
            self.topics[tid] = {'_id': tid, 'name': cleaned_name}
        return self.topic_map[cleaned_name]

    def get_author_id(self, name, s2_author_id=None):
        cleaned_name = clean_text(name)
        if not cleaned_name:
            return None

        # --- WATERFALL LOGIC IMPLEMENTATION ---

        # 1. Priority: Semantic Scholar ID (The definitive identifier)
        if s2_author_id:
            if s2_author_id in self.author_map_by_s2id:
                aid = self.author_map_by_s2id[s2_author_id]
                # Optional: Update the stored name if the current one is "better" (e.g., longer)
                if len(cleaned_name) > len(self.authors[aid]['name']):
                    self.authors[aid]['name'] = cleaned_name
                return aid

        # 2. Fallback: DBLP Name (Only executed if S2ID is NOT present)
        elif cleaned_name in self.author_map_by_name:
            return self.author_map_by_name[cleaned_name]

        # 3. New Author Found
        self.author_id_counter += 1
        aid = f"A{self.author_id_counter:05d}"
        self.authors[aid] = {
            '_id': aid,
            'name': cleaned_name,
            's2_author_id': s2_author_id,
            'total_publications': 0,
            'total_citations': 0
        }

        # Register in the appropriate map for future lookups
        if s2_author_id:
            self.author_map_by_s2id[s2_author_id] = aid
        else:
            # Only register in the name map if S2ID is absent
            self.author_map_by_name[cleaned_name] = aid

        return aid

    # --- PHASE 3.1: PROCESSING AND INTEGRATION ---

    def process_records(self):
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        print("Loading enrichment cache into memory...")
        # Load the cache into memory for fast lookup
        cursor.execute("SELECT dblp_key, status, s2_data FROM enrichment")
        cache = {row[0]: (row[1], row[2]) for row in cursor.fetchall()}
        conn.close()

        print("Starting record processing, integration, and ID assignment...")
        # Iterate through the original DBLP file as the primary source
        # This ensures we process all papers in our scope, even those missed by S2
        with open(self.input_jsonl, 'r', encoding='utf-8') as f:
            for line in f:
                dblp_record = json.loads(line)
                dblp_key = dblp_record['dblp_key']

                # 1. Assign Paper ID
                self.paper_id_counter += 1
                pid = f"P{self.paper_id_counter:06d}"

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
                    'fields_of_study': [],  # Store names for MongoDB analytics
                    'citation_count': 0,
                    'author_ids': [],
                    'venue': None,
                    '_s2_citations_raw': []  # Temporary storage for Phase 3.2
                }

                # 4. Process Venue (From DBLP)
                venue_name = clean_text(dblp_record['venue'])
                venue_id = self.get_venue_id(venue_name)
                paper['venue'] = venue_name
                if venue_id:
                    self.relationships['PUBLISHED_IN'].append((pid, venue_id))

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
                        self.paper_s2_to_internal_map[s2_paper_id] = pid

                    # Process Topics (Fields of Study)
                    for topic in s2_data.get('fieldsOfStudy') or []:
                        tid = self.get_topic_id(topic)
                        if tid:
                            # Store the name in the paper doc for MongoDB
                            paper['fields_of_study'].append(self.topics[tid]['name'])
                            self.relationships['HAS_TOPIC'].append((pid, tid))

                    # Store raw citation IDs for later resolution
                    paper['_s2_citations_raw'] = [
                        c.get('paperId')
                        for c in (s2_data.get('citations') or [])
                        if c.get('paperId')
                    ]

                    # Process Authors (Prefer S2 data as it has IDs)
                    s2_authors = s2_data.get('authors') or []
                    if s2_authors:
                        for author in s2_authors:
                            aid = self.get_author_id(author.get('name'), author.get('authorId'))
                            if aid:
                                paper['author_ids'].append(aid)
                                self.relationships['AUTHORED'].append((aid, pid))

                    # Fallback if S2 author list is empty but DBLP has authors
                    elif dblp_record.get('authors'):
                        for author_name in dblp_record['authors']:
                            # Use None for S2 ID during fallback
                            aid = self.get_author_id(author_name, None)
                            if aid:
                                paper['author_ids'].append(aid)
                                self.relationships['AUTHORED'].append((aid, pid))

                # 6. Fallback for Non-Enriched Papers (Use DBLP Authors)
                else:
                    for author_name in dblp_record.get('authors', []):
                        aid = self.get_author_id(author_name, None)
                        if aid:
                            paper['author_ids'].append(aid)
                            self.relationships['AUTHORED'].append((aid, pid))

                self.papers[pid] = paper

        print(
            f"Processed {len(self.papers)} papers, {len(self.authors)} authors, "
            f"{len(self.venues)} venues, {len(self.topics)} topics."
        )

    # --- PHASE 3.2: CITATION RESOLUTION AND METRIC CALCULATION ---

    def resolve_citations_and_metrics(self):
        print("Resolving internal citation graph and calculating author metrics...")
        count = 0
        # Second pass: Now that all internal IDs are assigned, resolve links and calculate metrics.
        for pid, paper in self.papers.items():
            # 1. Resolve Citations
            for cited_s2_id in paper.get('_s2_citations_raw', []):
                # Check if the cited paper exists within OUR dataset
                if cited_s2_id in self.paper_s2_to_internal_map:
                    cited_internal_id = self.paper_s2_to_internal_map[cited_s2_id]
                    # Ensure we don't cite ourselves
                    if pid != cited_internal_id:
                        self.relationships['CITES'].append((pid, cited_internal_id))
                        count += 1

            # Clean up temporary data
            if '_s2_citations_raw' in paper:
                del paper['_s2_citations_raw']

            # 2. Calculate Author Metrics (for MongoDB)
            citation_count = paper.get('citation_count', 0)
            for author_id in paper.get('author_ids', []):
                if author_id in self.authors:
                    self.authors[author_id]['total_publications'] += 1
                    self.authors[author_id]['total_citations'] += citation_count

        print(f"Resolved {count} internal citations and updated author metrics.")

    # --- PHASE 3.3: OUTPUT GENERATION ---

    def write_mongodb_files(self):
        print("Writing MongoDB JSONL files...")

        # Papers
        with open(os.path.join(self.mongo_dir, 'papers.jsonl'), 'w', encoding='utf-8') as f:
            for paper in self.papers.values():
                f.write(json.dumps(paper, ensure_ascii=False) + '\n')

        # Authors
        with open(os.path.join(self.mongo_dir, 'authors.jsonl'), 'w', encoding='utf-8') as f:
            for author in self.authors.values():
                f.write(json.dumps(author, ensure_ascii=False) + '\n')

    def write_neo4j_files(self):
        print("Writing Neo4j CSV files (Optimized for neo4j-admin import)...")

        # Helper for CSV writing
        def write_csv(filename, header, data_generator):
            filepath = os.path.join(self.neo4j_dir, filename)
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
        write_csv(
            'nodes_papers.csv',
            ['paperId:ID(Paper)', 'title', 'year:int', 'citationCount:int'],
            ([p['_id'], p['title'], p['year'], p['citation_count']] for p in self.papers.values())
        )

        # Authors
        write_csv(
            'nodes_authors.csv',
            ['authorId:ID(Author)', 'name'],
            ([a['_id'], a['name']] for a in self.authors.values())
        )

        # Venues
        write_csv(
            'nodes_venues.csv',
            ['venueId:ID(Venue)', 'name'],
            ([v['_id'], v['name']] for v in self.venues.values())
        )

        # Topics
        write_csv(
            'nodes_topics.csv',
            ['topicId:ID(Topic)', 'name'],
            ([t['_id'], t['name']] for t in self.topics.values())
        )

        # --- RELATIONSHIPS ---
        # Headers define the Start Node ID Space and End Node ID Space

        # AUTHORED
        write_csv(
            'rels_authored.csv',
            [':START_ID(Author)', ':END_ID(Paper)'],
            self.relationships['AUTHORED']
        )

        # CITES
        write_csv(
            'rels_cites.csv',
            [':START_ID(Paper)', ':END_ID(Paper)'],
            self.relationships['CITES']
        )

        # PUBLISHED_IN
        write_csv(
            'rels_published_in.csv',
            [':START_ID(Paper)', ':END_ID(Venue)'],
            self.relationships['PUBLISHED_IN']
        )

        # HAS_TOPIC
        write_csv(
            'rels_has_topic.csv',
            [':START_ID(Paper)', ':END_ID(Topic)'],
            self.relationships['HAS_TOPIC']
        )


def main():
    """Runs the main integration pipeline."""
    print("--- Phase 3: Integration and Formatting ---")
    pipeline = IntegrationPipeline()
    pipeline.process_records()
    pipeline.resolve_citations_and_metrics()
    pipeline.write_mongodb_files()
    pipeline.write_neo4j_files()
    print("Phase 3 Complete. Import files are ready in the 'import_files' directory.")

if __name__ == "__main__":
    main()
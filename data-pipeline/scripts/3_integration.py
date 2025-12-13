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
    - Processing records
    - Writing MongoDB and Neo4j export files
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

        # Storage
        self.author_map_by_s2id = {}   # S2_ID -> Internal_ID
        self.author_map_by_name = {}   # Name -> Internal_ID
        self.topic_map = {}
        self.paper_s2_to_internal_map = {}

        self.papers = {}
        self.authors = {}
        self.topics = {}

        self.relationships = {
            'AUTHORED': [],
            'HAS_TOPIC': [],
            'CITES': []
        }

        self.paper_id_counter = 0
        self.author_id_counter = 0
        self.topic_id_counter = 0

    # --- HELPER METHODS ---

    def get_topic_id(self, topic_name):
        cleaned_name = clean_text(topic_name)
        if not cleaned_name:
            return None

        # Logic: Case-insensitive lookup, but preserve original casing for display
        lookup_key = cleaned_name.lower()

        if lookup_key not in self.topic_map:
            self.topic_id_counter += 1
            tid = f"T{self.topic_id_counter:05d}"

            self.topic_map[lookup_key] = tid

            # Store the object:
            # Use the 'cleaned_name' (original casing) for display in 'name'
            self.topics[tid] = {'_id': tid, 'name': cleaned_name}

        return self.topic_map[lookup_key]

    def get_author_id(self, name, s2_author_id=None):
        cleaned_name = clean_text(name)
        if not cleaned_name:
            return None

        # --- WATERFALL LOGIC IMPLEMENTATION ---

        # 1. Priority: Semantic Scholar ID (The definitive identifier)
        if s2_author_id:
            if s2_author_id in self.author_map_by_s2id:
                aid = self.author_map_by_s2id[s2_author_id]
                # Update name if current one is longer/better
                if len(cleaned_name) > len(self.authors[aid]['name']):
                    self.authors[aid]['name'] = cleaned_name
                return aid

        # 2. Fallback: DBLP Name (Only executed if S2ID is NOT present)
        elif cleaned_name in self.author_map_by_name:
            return self.author_map_by_name[cleaned_name]

        # 3. Create New Author
        self.author_id_counter += 1
        aid = f"A{self.author_id_counter:05d}"
        self.authors[aid] = {
            '_id': aid,
            'name': cleaned_name,
            's2_author_id': s2_author_id,
            'total_publications': 0,
            'publications_summary': []
        }

        if s2_author_id:
            self.author_map_by_s2id[s2_author_id] = aid
        else:
            self.author_map_by_name[cleaned_name] = aid

        return aid

    def _add_author_to_paper(self, pid, paper, name, s2_author_id=None):
        """Helper to link an author to a paper (Neo4j & MongoDB)."""
        aid = self.get_author_id(name, s2_author_id)
        if aid:
            # Neo4j Relationship
            self.relationships['AUTHORED'].append((aid, pid))
            # Mongo Embedded Object
            paper['authors'].append({
                "id": aid,
                "name": self.authors[aid]['name']
            })

    # --- PROCESSING ---

    def process_records(self):
        conn = sqlite3.connect(self.db_path)
        cursor = conn.cursor()

        print("Loading enrichment cache into memory...")
        # Load the cache into memory for fast lookup
        cursor.execute("SELECT dblp_key, status, s2_data FROM enrichment")
        cache = {row[0]: (row[1], row[2]) for row in cursor.fetchall()}
        conn.close()

        print("Starting processing...")
        with open(self.input_jsonl, 'r', encoding='utf-8') as f:
            for line in f:
                dblp_record = json.loads(line)
                dblp_key = dblp_record['dblp_key']

                # 1. Assign Paper ID
                self.paper_id_counter += 1
                pid = f"P{self.paper_id_counter:06d}"

                # 2. Get Cache
                status, s2_data_raw = cache.get(dblp_key, ('MISSING', None))
                s2_data = json.loads(s2_data_raw) if s2_data_raw else None

                # 3. Build Paper Object
                paper = {
                    '_id': pid,
                    'title': clean_text(dblp_record['title']),
                    'year': dblp_record['year'],
                    'dblp_key': dblp_key,
                    'doi': clean_text(dblp_record.get('doi')),
                    'abstract': None,
                    'fields_of_study': [],
                    'authors': [],
                    'venue': [],
                    '_s2_citations_raw': [] # Temporary storage for S2 citations
                }

                # 4. Process Venue (Array of strings in Mongo, no Node in Neo4j)
                venue_name = clean_text(dblp_record.get('venue'))
                if venue_name:
                    paper['venue'] = [venue_name]

                # 5. Integrate S2 Data
                if status == 'SUCCESS' and s2_data:
                    paper['abstract'] = clean_text(s2_data.get('abstract'))

                    s2_paper_id = s2_data.get('paperId')
                    if s2_paper_id:
                        self.paper_s2_to_internal_map[s2_paper_id] = pid

                    # Topics
                    for topic in s2_data.get('fieldsOfStudy') or []:
                        tid = self.get_topic_id(topic)
                        if tid:
                            # Store the name in the paper doc for MongoDB
                            paper['fields_of_study'].append(self.topics[tid]['name'])
                            self.relationships['HAS_TOPIC'].append((pid, tid))

                    # Raw Citations
                    paper['_s2_citations_raw'] = [
                        c.get('paperId') for c in (s2_data.get('citations') or [])
                        if c.get('paperId')
                    ]

                    # Authors (Prefer S2)
                    s2_authors = s2_data.get('authors') or []
                    if s2_authors:
                        for author in s2_authors:
                            self._add_author_to_paper(pid, paper, author.get('name'), author.get('authorId'))

                    # Fallback Authors
                    elif dblp_record.get('authors'):
                        for author_name in dblp_record['authors']:
                            self._add_author_to_paper(pid, paper, author_name)

                # 6. No Enrichment Fallback
                else:
                    for author_name in dblp_record.get('authors', []):
                        self._add_author_to_paper(pid, paper, author_name)


                self.papers[pid] = paper

        print(f"Processed {len(self.papers)} papers, {len(self.authors)} authors.")

    def resolve_citations_and_metrics(self):
        print("Resolving citations and building author summaries...")

        for pid, paper in self.papers.items():
            # 1. Resolve Citations (Neo4j)
            for cited_s2_id in paper.get('_s2_citations_raw', []):
                # Check if the cited paper exists within OUR dataset
                if cited_s2_id in self.paper_s2_to_internal_map:
                    cited_internal_id = self.paper_s2_to_internal_map[cited_s2_id]
                    # Ensure we don't cite ourselves
                    if pid != cited_internal_id:
                        self.relationships['CITES'].append((pid, cited_internal_id))

            # Remove temp data
            if '_s2_citations_raw' in paper:
                del paper['_s2_citations_raw']

            # 2. Update Author Metrics (MongoDB)
            # Iterate over the author objects stored in the paper

            # Handle None titles gracefully
            safe_title = paper['title'] if paper['title'] is not None else "Untitled"

            summary_entry = {
                "paper_id": pid,
                "year": paper['year'],
                "title": safe_title
            }

            for author_obj in paper.get('authors', []):
                aid = author_obj['id']
                if aid in self.authors:
                    self.authors[aid]['total_publications'] += 1
                    # Append a COPY of the summary object to avoid shared reference aliasing
                    self.authors[aid]['publications_summary'].append(summary_entry.copy())

    # --- OUTPUT ---

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
                    writer = csv.writer(f, quoting=csv.QUOTE_MINIMAL) # type: ignore
                    writer.writerow(header)
                    for row in data_generator:
                        writer.writerow(row)
            except IOError as e:
                print(f"Error writing {filepath}: {e}")

        # Nodes: Paper
        write_csv(
            'nodes_papers.csv',
            ['paperId:ID(Paper)', 'title'],
            ([p['_id'], p['title']] for p in self.papers.values())
        )

        # Nodes: Author
        write_csv(
            'nodes_authors.csv',
            ['authorId:ID(Author)', 'name'],
            ([a['_id'], a['name']] for a in self.authors.values())
        )

        # Nodes: Topic
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
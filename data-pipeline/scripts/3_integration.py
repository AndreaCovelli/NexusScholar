import sqlite3
import json
import csv
import os
import random
import string
import bcrypt
import secrets
from datetime import datetime, timedelta, timezone

# --- CONFIGURATION ---
DB_PATH = 'enrichment_cache.db'
INPUT_JSONL = 'dblp_ai_ml_2015_2025.jsonl'
OUTPUT_DIR = 'import_files'
MONGO_SUBDIR = 'mongodb'
NEO4J_SUBDIR = 'neo4j'
INPUT_DIR = 'input_files'
FILE_USERS_RAW = os.path.join(INPUT_DIR, 'rawUsers.json')
FILE_ADMIN_RAW = os.path.join(INPUT_DIR, 'rawAdmin.json')
FILE_OUTPUT_USERS = 'registeredUsers.jsonl'
FILE_OUTPUT_ADMIN = 'admins.jsonl'

# User Generation Constants
MIN_BOOKMARKS = 2
MAX_BOOKMARKS = 7

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

    # Admin Permissions Definition
    # Defined as a class attribute to centralize permission logic
    PERMISSIONS = [
        "DELETE_PAPER",
        "BAN_USER",
        "TRIGGER_ETL_SYNC"
    ]

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

        # Identity tracking (for deduplication)
        self.seen_emails = set()
        self.seen_usernames = set()

    # --- HELPER METHODS ---

    def _resolve_unique_identity(self, base_username, email):
        """
        Ensures email uniqueness and generates a unique username.
        Returns the final unique username, or None if email is duplicated.
        """
        if email in self.seen_emails:
            return None  # Skip duplicate email

        candidate = base_username
        counter = 1
        # Auto-increment username if it exists (e.g. user, user1, user2)
        while candidate in self.seen_usernames:
            candidate = f"{base_username}{counter}"
            counter += 1

        self.seen_emails.add(email)
        self.seen_usernames.add(candidate)
        return candidate

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

        print("Loading enrichment cache into memory...")
        # Load the cache into memory for fast lookup
        try:
            cursor = conn.cursor()
            cursor.execute("SELECT dblp_key, status, s2_data FROM enrichment")
            cache = {row[0]: (row[1], row[2]) for row in cursor.fetchall()}
        finally:
            conn.close()

        print("Starting processing...")

        if not os.path.exists(self.input_jsonl):
            print(f"Error: {self.input_jsonl} not found.")
            return

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

    # function used to generate a date in last year
    @staticmethod
    def get_random_date_obj():
        """
        Returns a timezone-aware datetime object in UTC within the last year.
        """
        # Use timezone.utc to ensure the 'Z' suffix is actually true
        end = datetime.now(timezone.utc)
        start = end - timedelta(days=365)

        # Calculate random time
        random_date = start + (end - start) * random.random()
        return random_date

    # function that generates password considering alphabet, digits and some spec. char.
    @staticmethod
    def generate_password(length=10):
        chars = string.ascii_letters + string.digits + string.punctuation
        return ''.join(secrets.choice(chars) for _ in range(length))

    def generate_users(self):
        print("Starting User Generation...")

        if self.papers:
            # Create list of tuples: (id, title)
            paper_keys = [
                (p['_id'], p['title'])
                for p in self.papers.values()
                if p.get('_id') and p.get('title')
            ]
        else:
            paper_keys = []

        # Load raw user data
        if not os.path.exists(FILE_USERS_RAW):
            print(f"Error: {FILE_USERS_RAW} not found. Skipping user generation.")
            return

        try:
            with open(FILE_USERS_RAW, 'r', encoding='utf-8') as f:
                users_raw = json.load(f)
        except json.JSONDecodeError as e:
            print(f"Error: Failed to parse {FILE_USERS_RAW}: {e}")
            return

        user_output_path = os.path.join(self.mongo_dir, FILE_OUTPUT_USERS)
        print(f"Generating {len(users_raw)} users (JSONL)...")

        try:
            with open(user_output_path, 'w', encoding='utf-8') as fw:
                for i, user_raw in enumerate(users_raw):
                    # Validate required fields
                    full_name = user_raw.get('name')
                    email = user_raw.get('email')

                    if not full_name or not email:
                        continue

                    user_id = f"U{i+1:06d}"

                    # Uniqueness Check: Generate safe username or skip if email invalid
                    base_username = clean_text(full_name).lower().replace(' ', '_')
                    username = self._resolve_unique_identity(base_username, email)

                    if not username:
                        print(f"Skipping duplicate email: {email}")
                        continue

                    # Security: Generate Hash
                    raw_password = self.generate_password()
                    # low bcrypt rounds just for user mocks
                    hashed_pass = bcrypt.hashpw(raw_password.encode('utf-8'), bcrypt.gensalt(rounds=4))
                    hash_str = hashed_pass.decode('utf-8')

                    # Time: Creation
                    creation_date_obj = self.get_random_date_obj()
                    creation_date_str = creation_date_obj.strftime("%Y-%m-%dT%H:%M:%SZ")

                    # Logic: Bookmarks
                    bookmarks = []
                    if paper_keys:
                        # Use global constants
                        available_count = len(paper_keys)

                        # Guard against cases where fewer papers exist than the minimum requested
                        if available_count < MIN_BOOKMARKS:
                            num_bookmarks = available_count
                        else:
                            # Clamp the upper bound to the available count
                            upper_bound = min(MAX_BOOKMARKS, available_count)
                            num_bookmarks = random.randint(MIN_BOOKMARKS, upper_bound)

                        # Sample from lightweight list
                        selected_papers = random.sample(paper_keys, num_bookmarks)

                        now_utc = datetime.now(timezone.utc)
                        time_gap = (now_utc - creation_date_obj).total_seconds()
                        max_seconds = int(time_gap) if time_gap > 0 else 0

                        for p_id, p_title in selected_papers:
                            # Randomize bookmark time strictly between User Creation and Now
                            seconds_offset = random.randint(0, max_seconds)
                            saved_at_obj = creation_date_obj + timedelta(seconds=seconds_offset)

                            bookmarks.append({
                                "paper_id": p_id,
                                "title": p_title,
                                "saved_at": saved_at_obj.strftime("%Y-%m-%dT%H:%M:%SZ")
                            })

                    user_doc = {
                        "_id": user_id,
                        "username": username,
                        "email": email,
                        "password_hash": hash_str,
                        "created_at": creation_date_str,
                        "full_name": full_name,
                        "bookmarked_papers": bookmarks
                    }

                    # Write JSONL Line
                    fw.write(json.dumps(user_doc, ensure_ascii=False) + "\n")

            print(f"Successfully wrote users to {user_output_path}")

        except IOError as e:
            print(f"Disk I/O Error writing users: {e}")

    def generate_admin(self):
        print("Starting Admin Generation...")

        if not os.path.exists(FILE_ADMIN_RAW):
            print(f"Error: {FILE_ADMIN_RAW} not found. Skipping admin generation.")
            return

        try:
            with open(FILE_ADMIN_RAW, 'r', encoding='utf-8') as f:
                raw_admin = json.load(f)
        except json.JSONDecodeError as e:
            print(f"Error: Failed to parse {FILE_ADMIN_RAW}: {e}")
            return

        admin_output_path = os.path.join(self.mongo_dir, FILE_OUTPUT_ADMIN)
        print(f"Generating {len(raw_admin)} admins (JSONL)...")

        try:
            with open(admin_output_path, 'w', encoding='utf-8') as fw:
                for i, ra in enumerate(raw_admin):
                    admin_name = ra.get("name")
                    email = ra.get("email")

                    if not admin_name or not email:
                        continue

                    a_id = f"AD{i+1:02d}"

                    # Uniqueness Check: Ensure safe username generation
                    safe_name = clean_text(admin_name).lower().replace(' ', '_')
                    base_username = f"{safe_name}_admin"

                    a_username = self._resolve_unique_identity(base_username, email)

                    if not a_username:
                        print(f"Skipping duplicate email (Admin): {email}")
                        continue

                    # Security: Hash
                    raw_pw = self.generate_password()
                    hashed_pass = bcrypt.hashpw(raw_pw.encode('utf-8'), bcrypt.gensalt(rounds=4))
                    a_pw_hash = hashed_pass.decode('utf-8')

                    # Time
                    a_date_obj = self.get_random_date_obj()
                    a_date_str = a_date_obj.strftime("%Y-%m-%dT%H:%M:%SZ")

                    # Permissions: Use class constant
                    total_perms = len(self.PERMISSIONS)
                    # Select at least 1 permission, up to the total number available
                    num_perms_to_assign = random.randint(1, total_perms)
                    a_permissions = random.sample(self.PERMISSIONS, num_perms_to_assign)

                    admin_doc = {
                        "_id": a_id,
                        "username": a_username,
                        "email": email,
                        "password_hash": a_pw_hash,
                        "created_at": a_date_str,
                        "permissions": a_permissions
                    }

                    # Write JSONL Line
                    fw.write(json.dumps(admin_doc, ensure_ascii=False) + "\n")

            print(f"Successfully wrote admins to {admin_output_path}")

        except IOError as e:
            print(f"Disk I/O Error writing admins: {e}")


def main():
    """Runs the main integration pipeline."""
    print("--- Phase 3: Integration and Formatting ---")
    pipeline = IntegrationPipeline()
    pipeline.process_records()
    pipeline.resolve_citations_and_metrics()
    pipeline.write_mongodb_files()
    pipeline.generate_users()
    pipeline.generate_admin()
    pipeline.write_neo4j_files()
    print("Phase 3 Complete. Import files are ready in the 'import_files' directory.")

if __name__ == "__main__":
    main()
import requests
import sqlite3
import json
import time
from tenacity import (
    retry, 
    stop_after_attempt, 
    wait_exponential, 
    retry_if_exception_type, 
    before_sleep_log,
    RetryError
)
import logging
import sys
import os
from dotenv import load_dotenv

# --- CONFIGURATION ---
BATCH_SIZE = 100  # Batch size for API
DB_PATH = 'enrichment_cache.db'
API_URL = "https://api.semanticscholar.org/graph/v1/paper/batch"

# Fields to retrieve (Includes 'citations.paperId' for Graph Construction)
FIELDS = 'paperId,title,abstract,citationCount,referenceCount,fieldsOfStudy,citations.paperId,authors.authorId,authors.name'

# Setup basic logging for tenacity to print retries to console
logging.basicConfig(stream=sys.stdout, level=logging.INFO)
logger = logging.getLogger(__name__)

# Custom Exception for explicit handling of 429s within Tenacity
class RateLimitException(Exception):
    pass

# --- DATABASE SETUP ---
def setup_database():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS enrichment (
            dblp_key TEXT PRIMARY KEY,
            doi TEXT,
            status TEXT,
            s2_data TEXT
        )
    ''')
    cursor.execute('CREATE INDEX IF NOT EXISTS idx_status ON enrichment(status)')
    conn.commit()
    return conn

# --- DATA LOADING ---
def load_initial_data(conn, input_jsonl):
    """Loads DBLP data into SQLite if not already present."""
    cursor = conn.cursor()
    cursor.execute("SELECT COUNT(*) FROM enrichment")
    if cursor.fetchone()[0] > 0:
        print("Database not empty. Skipping initial load...")
        return

    print("Loading DBLP data into database...")
    batch = []
    with open(input_jsonl, 'r', encoding='utf-8') as f:
        for line in f:
            record = json.loads(line)
            raw_doi = record.get('doi', '')
            if raw_doi:
                raw_doi = raw_doi.replace('https://doi.org/', '').replace('http://dx.doi.org/', '')
            
            batch.append((record['dblp_key'], raw_doi))
            
            if len(batch) >= 10000:
                cursor.executemany("INSERT OR IGNORE INTO enrichment (dblp_key, doi, status) VALUES (?, ?, 'PENDING')", batch)
                conn.commit()
                batch = []
    
    if batch:
        cursor.executemany("INSERT OR IGNORE INTO enrichment (dblp_key, doi, status) VALUES (?, ?, 'PENDING')", batch)
        conn.commit()
    print("Initial load complete.")

# --- TENACITY API REQUEST ---

# Retry Logic:
# 1. Stop after 5 attempts.
# 2. Wait exponentially: 2s, 4s, 8s... up to 60s.
# 3. Retry ONLY on Network Errors or our custom RateLimitException.
@retry(
    stop=stop_after_attempt(5),
    wait=wait_exponential(multiplier=1, min=2, max=60),
    retry=retry_if_exception_type((requests.exceptions.RequestException, RateLimitException)),
    before_sleep=before_sleep_log(logger, logging.WARNING) # Logs when a retry happens
)
def fetch_batch_robust(ids_to_fetch):
    """
    Fetches a batch of papers. Raises exceptions on 429/5xx to trigger retries.
    Returns None immediately on 400/404 errors (no retry).
    """
    load_dotenv()
    api_key = os.getenv("MY_API_KEY")
    
    headers = {"x-api-key": api_key}
    
    payload = {"ids": ids_to_fetch}
    params = {"fields": FIELDS, "limit": BATCH_SIZE}
    
    # Timeout is crucial to prevent hanging requests
    response = requests.post(API_URL, params=params, json=payload, headers=headers, timeout=30)
    
    if response.status_code == 200:
        return response.json()
    
    elif response.status_code == 429:
        # Raise custom exception to trigger the @retry decorator
        raise RateLimitException(f"Rate limit hit (429).")
        
    elif response.status_code >= 500:
        # Server errors should also be retried
        response.raise_for_status()
        
    else:
        # Client errors (400, 403, 404) - Do NOT retry, just return None
        print(f"  Non-retriable error {response.status_code}: {response.text}")
        return None

# --- MAIN PROCESSING LOOP ---
def process_enrichment():
    conn = setup_database()
    cursor = conn.cursor()

    # --- STATISTICS COUNTERS ---
    stats = {
        'processed': 0,
        'success': 0,
        'not_found': 0,
        'errors': 0
    }

    print("Starting enrichment process...")
    
    while True:
        # 1. Get a batch of PENDING papers
        cursor.execute("SELECT dblp_key, doi FROM enrichment WHERE status='PENDING' AND doi != '' LIMIT ?", (BATCH_SIZE,))
        records = cursor.fetchall()
        
        if not records:
            break
            
        print(f"Processing batch of {len(records)} papers...")
        
        dblp_keys = [r[0] for r in records]
        api_ids = [f"DOI:{r[1]}" for r in records]
        
        try:
            # 2. Call API (Protected by Tenacity)
            results = fetch_batch_robust(api_ids)
            
            if results is None:
                # Batch failed (Non-retriable error)
                stats['errors'] += len(dblp_keys)

                # Should only happen on 400/404 errors
                print("  Batch returned error (likely invalid IDs). Marking as ERROR.")
                placeholders = ','.join(['?'] * len(dblp_keys))
                cursor.execute(f"UPDATE enrichment SET status='ERROR' WHERE dblp_key IN ({placeholders})", dblp_keys)
                conn.commit()
                continue

            # 3. Update Database
            updates = []
            for dblp_key, result in zip(dblp_keys, results):
                if result:
                    updates.append(('SUCCESS', json.dumps(result), dblp_key))
                    stats['success'] += 1
                else:
                    updates.append(('NOT_FOUND', None, dblp_key))
                    stats['not_found'] += 1
            
            cursor.executemany("UPDATE enrichment SET status=?, s2_data=? WHERE dblp_key=?", updates)
            conn.commit()

            stats['processed'] += len(dblp_keys)

            # --- CALCULATE AND PRINT RATIO ---
            total_attempts = stats['success'] + stats['not_found']
            hit_ratio = (stats['success'] / total_attempts * 100) if total_attempts > 0 else 0.0
            print(f"Batch Done. Total: {stats['processed']} | "
                  f"Hit Ratio: {hit_ratio:.2f}% "
                  f"(Found: {stats['success']}, Missing: {stats['not_found']}, Errors: {stats['errors']})")

        except RetryError:
            # Tenacity gave up after max attempts (e.g., persistent 429s or network down)
            print("  CRITICAL: Max retries exceeded. Marking batch as ERROR and skipping.")
            stats['errors'] += len(dblp_keys)

            placeholders = ','.join(['?'] * len(dblp_keys))
            cursor.execute(f"UPDATE enrichment SET status='ERROR' WHERE dblp_key IN ({placeholders})", dblp_keys)
            conn.commit()
        
        except Exception as e:
            print(f"  Unexpected error: {e}")
            break
        
        # 4. Rate Limit Pacing (Inter-batch sleep)
        # Tenacity handles retries, but we still need a baseline sleep between SUCCESSFUL batches
        # to be a "good citizen" and avoid hitting the limit in the first place.
        time.sleep(1)

    # Handle papers with NO DOI
    print("Marking papers without DOIs...")
    cursor.execute("UPDATE enrichment SET status='NO_DOI' WHERE status='PENDING' AND (doi IS NULL OR doi = '')")
    conn.commit()
    
    conn.close()
    print("Enrichment Complete.")

if __name__ == "__main__":
    print("--- Phase 2: Enrichment SemanticScholar ---")

    conn = setup_database()
    # Uncomment the next line only for the first run:
    load_initial_data(conn, 'dblp_ai_ml_2015_2025.jsonl')
    
    process_enrichment()
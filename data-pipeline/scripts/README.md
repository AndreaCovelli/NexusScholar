# NexusScholar Data Pipeline

This directory contains the ETL (Extract, Transform, Load) pipeline for NexusScholar. The pipeline transforms raw XML data from DBLP, enriches it with citation data from Semantic Scholar, and loads it into the Polyglot Persistence layer (MongoDB + Neo4j).

## Pipeline Overview

The pipeline is divided into **4 sequential phases**:

1.  **Parsing:** Extracts AI/ML conference papers from the massive DBLP XML dump.
2.  **Enrichment:** Queries the Semantic Scholar API to fetch abstracts, citations, and topics.
3.  **Integration:** Merges data, resolves internal IDs, and generates import files.
4.  **Loading:** Bulk loads data into MongoDB (Replica Set) and Neo4j.

---

## Prerequisites & Setup

### 1. Environment Variables
Ensure the `.env` file in the project root is configured. The pipeline requires:
*   `MY_API_KEY`: Your Semantic Scholar API Key (for Phase 2).
*   `MONGO_...`: MongoDB credentials.
*   `NEO4J_...`: Neo4j credentials.

The MongoDB replica set requires a keyfile for authentication. Generate it by running one of the following commands from the `deployment/` directory:

- **Linux/macOS:**
  ```bash
  openssl rand -base64 756 > mongo-keyfile
  ```
- **Windows (PowerShell):**
  ```powershell
  ./generate_mongo-keyfile.ps1
  ```

### 2. Python Dependencies
Navigate to the project root directory and create the virtual environment:
```bash
python -m venv .venv
```

After creating the virtual environment, activate it.

**On Windows (PowerShell):**
```powershell
.venv\Scripts\Activate.ps1
```

**On Linux/macOS:**
```bash
source .venv/bin/activate
```

Once activated, install the required libraries:
```bash
pip install lxml requests bcrypt tenacity python-dotenv pandas pymongo neo4j
```

### 3. Infrastructure
Before loading data (Phase 4), the Docker infrastructure must be running.

**Start the infrastructure:**
```bash
# From the project root
docker compose --env-file .env -f deployment/docker-compose.local.yml up -d
```

**Initialize MongoDB Replica Set:**
(Only required the first time you bring the containers up)

From `data-pipeline/scripts` run:
```bash
bash ../../deployment/scripts/init-mongo-replica.sh
```

---

## Running the Pipeline

### Phase 1: Parse DBLP Data
*   **Input:** Raw DBLP XML dump.
*   **Action:** Uses `dblp.xml.gz` and filters for venues like NeurIPS, ICML, CVPR, etc.

1.  Download `dblp.xml.gz` and `dblp.dtd` from [dblp.org](https://dblp.org/xml/) and place them in `data-pipeline/scripts/`.
2.  Change directory to `data-pipeline/scripts`.
3.  Run the parser:
    ```bash
    python 1_parse_and_filter_dblp.py
    ```
    *Output:* `dblp_ai_ml_2015_2025.jsonl`

### Phase 2: Enrichment (Semantic Scholar)
*   **Input:** Filtered JSONL from Phase 1.
*   **Action:** Batches requests to Semantic Scholar API. Caches results in SQLite to handle interruptions.

1.  Run the enrichment script:
    ```bash
    python 2_enrichment_semanticscholar.py
    ```
    *Output:* Updates `enrichment_cache.db`

    > **Note:** If the script stops (e.g., API limit reached), simply run it again. It resumes from where it left off using the SQLite cache.

### Phase 3: Integration & Formatting
*   **Input:** `dblp_ai_ml_2015_2025.jsonl` + `enrichment_cache.db`.
*   **Action:** Assigns internal IDs (P0001, A0001), resolves citation graphs, and formats data for bulk import.

1.  Run the integration script:
    ```bash
    python 3_integration.py
    ```
    *Output:* Creates `import_files/mongodb/*.jsonl` and `import_files/neo4j/*.csv`.

### Phase 4: Data Loading
*   **Input:** `import_files/` directory.
*   **Action:** Uses Docker commands to perform bulk imports.

1.  **Ensure Docker containers are running** (see Prerequisites).
2.  Run the loader script:
    ```bash
    bash 4_load_data.sh
    ```
    **What this script does:**
    *   Imports JSONL files into **MongoDB** (`authors`, `venues`, `papers`).
    *   Temporarily **stops** the Neo4j container.
    *   Runs `neo4j-admin database import` for high-speed CSV loading.
    *   **Restarts** the Neo4j container.
### Phase 5: Index creation
*   **Action:** Uses Docker commands create indexes.

1.  **Ensure Docker containers are running** (see Prerequisites).
2.  Run the loader script:
    ```bash
    bash 5_create_index.sh
    ```
    **What this script does:**
    *   Create Indexes into **MongoDB**.


---

## Maintenance & Reset

If you need to wipe the databases and start fresh (e.g., if Phase 4 failed partway or you want to reload data):

1.  **Tear down volumes:**
    ```bash
    # From project root
    docker compose -f deployment/docker-compose.local.yml down -v
    ```
    *Warning: This deletes all data in MongoDB and Neo4j.*

2.  **Restart Infrastructure:**
    ```bash
    docker compose --env-file .env -f deployment/docker-compose.local.yml up -d
    ```

3.  **Re-initialize Replica Set:**
    ```bash
    bash deployment/scripts/init-mongo-replica.sh
    ```

4.  **Re-run Phase 4:**
    ```bash
    cd data-pipeline/scripts
    bash 4_load_data.sh
    ```
5.  **Re-run Phase 5:**
    ```bash
    bash 5_create_index.sh
    ```
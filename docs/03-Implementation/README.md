# Phase 3: Implementation Details

## 1. Technology Stack
*   **Language:** Java 25 (LTS)
*   **Framework:** Spring Boot 3.5.7
*   **Build Tool:** Maven
*   **Security:** Spring Security + Java JWT (Auth0)
*   **Testing:** JUnit 5, Testcontainers, Mockito
*   **Quality Control:** Spotless (Google Java Format), JaCoCo, SonarCloud

## 2. Key Modules

### 2.1 Data Access Layer (DAOs)
The application uses the Repository pattern but separates logic based on the data source.
*   **Mongo Repositories:** Extend `MongoRepository` for standard CRUD and custom aggregation pipelines (e.g., `PaperAnalysisDAOImpl`).
*   **Graph DAO:** A custom implementation (`GraphDAO.java`) using the Neo4j Java Driver. It handles low-level Cypher queries and manages GDS projections explicitly.

### 2.2 Service Layer & Algorithms
The business logic encapsulates the complexity of the underlying algorithms.

#### MongoDB Aggregations
*   **Trend Analysis:** Uses `$unwind` on fields of study, `$group` by year, and `$sort` to determine trending topics.
*   **User Leaderboard:** Aggregates user bookmarks using `$match` (on date range) and `$count` to find the most popular papers.

#### Neo4j Graph Algorithms (GDS)
*   **PageRank:**
    *   Checks if graph `paperCitations` exists.
    *   Projects `(Paper)-[:CITES]->(Paper)`.
    *   Runs `gds.pageRank.stream` to score papers.
*   **Leiden Algorithm:**
    *   Projects `coAuthors` graph (undirected `AUTHORED` relationships).
    *   Runs `gds.leiden.stream` to find modularity-based communities.
*   **Shortest Path:** Uses standard Cypher `shortestPath` to find the collaboration distance.
*   **Betweenness Centrality:** Calculates nodes acting as bridges in the network.

### 2.3 Security Implementation
*   **JWT Filter:** A custom `OncePerRequestFilter` intercepts requests, validates the `Bearer` token using `JwtTokenProvider`, and sets the Spring `SecurityContext`.
*   **RBAC:** Endpoints are protected using `@PreAuthorize("hasRole('ADMIN')")` or `hasRole('USER')`.

## 3. The ETL Pipeline
The data ingestion process is automated via Python scripts located in `data-pipeline/scripts/`.

1.  **Parsing (`1_parse_and_filter_dblp.py`):** Streams the massive DBLP XML file (`lxml.etree`) to keep memory usage low. Filters for specific AI/ML venues.
2.  **Enrichment (`2_enrichment_semanticscholar.py`):** Uses `tenacity` for robust API retries. Caches results in `enrichment_cache.db` (SQLite) to allow pausing/resuming the process.
3.  **Integration (`3_integration.py`):**
    *   Resolves entity identities (merging authors).
    *   Generates synthetic User/Admin accounts for testing.
    *   Outputs `JSONL` for MongoDB and `CSV` headers/rows for Neo4j Import Tool.
4.  **Bulk Loading (`4_load_data.sh`):**
    *   Uses `mongoimport` for document insertion.
    *   Uses `neo4j-admin database import` for massive graph ingestion (offline mode).
5.  **Indexing (`5_create_index.sh`):** Applies uniqueness constraints and text indexes after the load.

## 4. CAP Theorem Analysis
*   **MongoDB:** Configured as **CP** (Consistency/Partition Tolerance) during network partitions. The Replica Set ensures strong consistency for the Primary node.
*   **Neo4j:** Operated as a standalone instance (**CA** - Consistency/Availability) in this architecture.
*   **System:** NexusScholar prioritizes **Consistency** and **Partition Tolerance**. Data integrity is critical for the analytics pipeline.
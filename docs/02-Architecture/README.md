# Phase 2: System Architecture

## 1. High-Level Architecture
NexusScholar employs a **Polyglot Persistence** architecture, leveraging the strengths of two distinct database technologies to optimize for different query patterns.

![High Level Architecture](./high-level-architecture.png)

### Components
1.  **Backend API (Spring Boot):** A stateless REST API that orchestrates business logic, handles security, and routes queries to the appropriate database.
2.  **Document Store (MongoDB):** Handles structured metadata, user profiles, and heavy aggregation tasks (e.g., grouping papers by year).
3.  **Graph Database (Neo4j):** Handles highly connected data (citations, co-authorship) and executes complex graph algorithms.
4.  **ETL Pipeline (Python):** An offline processing pipeline that ingests raw XML, enriches it via APIs, and creates bulk import files.

## 2. Database Design & Polyglot Strategy

### 2.1 MongoDB (The "System of Record")
MongoDB is used to store the "rich" content. It allows for flexible schemas (e.g., varying fields in papers) and high-speed retrieval of profile data.

*   **Collections:** `papers`, `authors`, `registeredUsers`, `admins`.
*   **Replica Set (`rs0`):** Configured with 1 Primary and 2 Secondary nodes (`mongo1`, `mongo2`, `mongo3`) to ensure high availability and read scaling.
*   **Optimization:**
    *   **Text Indexes:** On `title` and `abstract` for search.
    *   **Embedding:** `PublicationSummary` is embedded within `Author` documents to avoid joins. `BookmarkedPaper` is embedded within `User`.

### 2.2 Neo4j (The "Relationship Engine")
Neo4j is used specifically for structural analysis. It contains a "skeleton" of the data optimized for traversal.

*   **Nodes:** `Paper`, `Author`, `Topic`.
*   **Relationships:**
    *   `(:Author)-[:AUTHORED]->(:Paper)`
    *   `(:Paper)-[:CITES]->(:Paper)`
    *   `(:Paper)-[:HAS_TOPIC]->(:Topic)`
*   **Optimization:**
    *   **Graph Data Science (GDS) Library:** Projections are created in-memory (e.g., `paperCitations`, `coAuthors`) to run algorithms like PageRank without affecting transactional performance.

## 3. Data Consistency Strategy
Since data spans two databases, consistency is managed via **Application-Side Dual Writes**:

1.  **Write to MongoDB:** The entity is saved to the document store first (Primary).
2.  **Write to Neo4j:** If the MongoDB write succeeds, the application attempts to update the graph nodes/relationships via `GraphDAO`.
3.  **Failure Handling:** If the Neo4j write fails, a `DAOException` is thrown, alerting the client to the inconsistency.

## 4. Deployment Infrastructure
The system is containerized using **Docker Compose**.

*   **Backend:** Custom image based on Eclipse Temurin OpenJDK 25.
*   **Databases:** Official images for Mongo (`mongo:8.2.3`) and Neo4j Enterprise (`neo4j:2025.11.2`).
*   **Network:** All services communicate over a private Docker bridge network (`nexusscholar-net`).
*   **Security:** MongoDB uses keyfile authentication for inter-node communication.
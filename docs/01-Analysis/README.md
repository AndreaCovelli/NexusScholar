# Phase 1: Analysis and Requirements

## 1. Overview
**NexusScholar** is a scientific literature analytics platform designed to aggregate, enrich, and analyze research papers from the Computer Science domain (specifically AI and ML venues like NeurIPS, ICML, CVPR). The system addresses the challenge of "information overload" by not just storing papers, but by surfacing meaningful connections, trends, and influential entities within the citation network.

## 2. Functional Requirements

### 2.1 User Management & Authentication
*   **Roles:** The system supports two distinct roles: `USER` (Researchers/Students) and `ADMIN` (System Maintainers).
*   **Registration:** Users can register with a username, email, and password.
*   **Authentication:** Stateless authentication using JWT (JSON Web Tokens).
*   **Security:** Passwords must be hashed (BCrypt) before storage.

### 2.2 Core Data Management
*   **Papers:** View detailed information including titles, abstracts, publication year, DOIs, and venues.
*   **Authors:** View author profiles, their Semantic Scholar IDs, and publication history.
*   **Bookmarks:** Registered users can bookmark specific papers for later reference.

### 2.3 Search Capabilities
*   **Full-Text Search:** Search for papers by title or abstract keywords (e.g., "Neural Networks").
*   **Filtering:** Filter papers by year or authors by publication count.
*   **Autocomplete:** Prefix search for Author names and Usernames.

### 2.4 Analytical Dashboards (MongoDB Aggregations)
*   **Trend Analysis:** Visualize the growth of specific fields of study over time.
*   **Venue Analysis:** Rank conferences/journals by publication volume per year.
*   **Collaboration Evolution:** Track the average number of authors per paper over the last decade.
*   **Leaderboards:** Identify the most bookmarked papers within specific timeframes.

### 2.5 Advanced Graph Analysis (Neo4j GDS)
*   **Influence Scoring:** Calculate **PageRank** scores to identify the most influential papers in the citation network.
*   **Community Detection:** Use the **Leiden** algorithm to discover hidden communities of collaborating authors.
*   **Pathfinding:** Find the **Shortest Path** of collaboration between two researchers (Degrees of Separation).
*   **Centrality:** Calculate **Betweenness Centrality** to find "bridge" papers that connect disparate fields.

## 3. Non-Functional Requirements
*   **Scalability:** The document store supports horizontal scaling (Replica Sets) to handle read-heavy workloads.
*   **Performance:** Graph algorithms run efficiently on large datasets using in-memory graph projections.
*   **Availability:** The system remains operational even if a database node fails (High Availability via Replication).

## 4. Domain Model

The domain is modeled around three primary entities:

1.  **Paper:** The central entity. Contains metadata (DOI, Year, Abstract) and links to Authors and Venues.
2.  **Author:** Individuals who write papers. Contains aggregated metrics (Total Publications) and a summary of their work.
3.  **User (Abstract):**
    *   **RegisteredUser:** Can search and bookmark papers.
    *   **Admin:** Can manage users and trigger system maintenance tasks.

### Relationships
*   **Authored By:** Many-to-Many relationship between Authors and Papers.
*   **Cites:** Directed relationship between Papers (Paper A cites Paper B).
*   **Has Topic:** Papers are tagged with Fields of Study (Topics).
*   **Bookmarked:** Users have a list of saved Papers.

## 5. Data Sources
*   **DBLP (XML):** Primary source for bibliographic metadata and verified publication records.
*   **Semantic Scholar (API):** Enrichment source for Abstracts, Citations, and Fields of Study.
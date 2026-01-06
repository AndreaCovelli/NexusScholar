# Backend Development & Testing Guide

This document outlines the recommended workflow for developing, debugging, and testing the NexusScholar backend.

## 1. The Development Architecture (Hybrid Workflow)

To avoid the slowness of rebuilding Docker images for every code change, we use a **Hybrid Workflow**:
*   **Databases (MongoDB, Neo4j):** Run inside Docker containers.
*   **Backend (Java/Spring):** Runs natively on your local machine (IDE or Terminal).

### Why?
*   **Speed:** Instant hot-reloading of code changes.
*   **Debugging:** Native IDE debugging support (breakpoints, variable inspection).
*   **Stability:** Avoids "404 Not Found" errors caused by running stale Docker images.

---

## 2. Initial Configuration

Since the databases run in Docker but the app runs locally, the app needs to connect to `localhost` ports instead of Docker container names.

### Step A: Create Local Configuration
Create a new file: `src/main/resources/application-local.yaml`.

```yaml
spring:
  data:
    mongodb:
      # Connects to localhost:27017 (exposed by Docker)
      uri: mongodb://admin:password@localhost:27017/nexusscholar?authSource=admin
  neo4j:
    # Connects to localhost:7687 (exposed by Docker)
    uri: bolt://localhost:7687
    authentication:
      username: neo4j
      password: password
```

*> **Note:** Update the usernames/passwords to match your `.env` file if you changed the defaults.*

### Step B: Git Ignore
Ensure `application-local.yaml` is not committed to version control. Add this to your `.gitignore`:

```text
application-local.yaml
```

---

## 3. How to Run the Application

### 1. Start the Infrastructure
Start the databases, but **exclude** the backend container (to avoid port conflicts).

```bash
# From the project root
docker compose --env-file .env -f deployment/docker-compose.local.yml up -d mongo1 mongo2 mongo3 neo4j
```

### 2. Run the Backend (with 'local' profile)
You must tell Spring Boot to load the `application-local.yaml` configuration.

**Option A: IntelliJ IDEA / Eclipse**
1.  Open the Run Configuration for `NexusScholarBackendApplication`.
2.  In **VM Options** or **Active Profiles**, set: `local`.
3.  Click **Run** or **Debug**.

**Option B: Command Line**
```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

---

## 4. Testing the Graph Controller

Once the application is running locally on port 8080, you can test the endpoints directly in your browser or via Swagger.

### Option A: Swagger UI (Recommended)
Navigate to: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
1.  Open the **`graph-controller`** section.
2.  Click **Try it out** → **Execute**.

### Option B: Browser URLs
You can access these `GET` endpoints directly:

*   **PageRank Scores:**
    [http://localhost:8080/api/graph/analysis/pagerank](http://localhost:8080/api/graph/analysis/pagerank)
*   **Leiden Communities:**
    [http://localhost:8080/api/graph/analysis/leidenCommunities](http://localhost:8080/api/graph/analysis/leidenCommunities)
*   **Betweenness Centrality:**
    [http://localhost:8080/api/graph/analysis/betweenness](http://localhost:8080/api/graph/analysis/betweenness)
*   **Shortest Path (Example):**
    [http://localhost:8080/api/graph/analysis/shortestPath/Jennifer%20Dean&B.%20Rappazzo](http://localhost:8080/api/graph/analysis/shortestPath/Jennifer%20Dean&B.%20Rappazzo)

---

## 5. Final Verification (Before Pushing)

Once you have finished development and testing locally, you should verify that your code works inside the Docker container. This ensures the `Dockerfile` is correct and the application runs in a Linux environment.

**Use the following command only when you are done and want to verify that the final container builds correctly before pushing to GitHub:**

```bash
# 1. Stop your local Java application (to free up port 8080)
# 2. Run:
docker compose --env-file .env -f deployment/docker-compose.local.yml up -d --build backend
```

---

## 6. Troubleshooting

**"Whitelabel Error Page / 404 Not Found"**
*   **Cause:** You are likely running the **Dockerized** backend (which contains an old version of the code) instead of your local version.
*   **Fix:** Stop the backend container: `docker compose stop backend` and run the Java app locally as described in Section 3.

**"Connection Refused" to Databases**
*   **Cause:** The Docker containers are not running, or the `local` profile is not active (so the app is trying to find `mongo1` instead of `localhost`).
*   **Fix:** Ensure containers are up (`docker ps`) and you passed the `local` profile to the runner.

**"Empty List []" returned from endpoints**
*   **Cause:** The algorithms ran successfully, but the Neo4j database is empty.
*   **Fix:** Ensure you have run Phase 4 of the data pipeline (`4_load_data.sh`).
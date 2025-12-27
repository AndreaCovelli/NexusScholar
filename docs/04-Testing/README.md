# Testing and Validation

This document outlines the testing strategy, tools, and commands used to ensure the quality and reliability of the NexusScholar Backend.

The project uses:

* **JUnit 5** for test definition and assertions.
* **Testcontainers** for integration testing against real, ephemeral database instances (MongoDB and Neo4j).
* **Spotless** for enforcing consistent code formatting (Google Java Format).
* **JaCoCo** for measuring code coverage.

## 1. Prerequisites

Before running the tests locally, you must ensure the following are installed and running:

1. **Java JDK 25**: The project is configured for the latest Java LTS features.
2. **Docker Desktop (or Engine)**: **CRITICAL.** The integration tests use Testcontainers to automatically spin up MongoDB and Neo4j containers. **If Docker is not running, the tests will fail.**
3. **Maven**: You can use the wrapper script (`./mvnw`) included in the repository or a local Maven installation.

---

## 2. Code Formatting (Spotless)

We use the **Spotless Maven Plugin** to enforce the **Google Java Format**. The Continuous Integration (CI) pipeline will fail if the code is not formatted correctly.

**Note:** All commands must be run from the `backend/` directory.

### Apply Formatting (Auto-Fix)

To automatically format your code (imports, spacing, indentation):

```bash
cd backend
mvn spotless:apply
```

> **Tip:** Run this command before pushing your code to avoid CI failures.

### Check Formatting

To verify that your code adheres to the formatting rules without modifying any files:

```bash
cd backend
mvn spotless:check
```

---

## 3. Running Tests

The test suite consists of both Unit Tests (fast, mocked dependencies) and Integration Tests (slower, requiring Docker).

### Run Full Test Suite

To clean the build artifacts, compile the code, run all tests, and generate quality reports:

```bash
cd backend
mvn clean verify
```

**What happens during `mvn clean verify`:**

1. **Clean:** Removes the `target/` directory.
2. **Compile:** Compiles the source code.
3. **Spotless Check:** Verifies code formatting.
4. **Test Execution:** Runs all tests.
* *Unit Tests:* (e.g., `UtilsTest`, `Neo4jModelTest`) run quickly.
* *Integration Tests:* (e.g., `GraphServiceTest`) will pull and start Docker images for `mongo` and `neo4j`.


5. **Quality Reports:** Generates the JaCoCo code coverage report.

---

## 4. Test Architecture

### Unit Tests

Located in `src/test/java`, these tests verify the logic of individual classes in isolation.

* **Examples:** `Neo4jModelTest`, `UtilsTest`, `HealthServiceTest`.
* **Focus:** Model integrity, utility functions, and controller responses (mocked).

### Integration Tests

These tests load the full Spring Application Context and interact with real databases.

* **Examples:** `GraphServiceTest`, `NexusScholarBackendApplicationTests`.
* **Infrastructure:** Defined in `TestcontainersConfiguration.java`.
* **Behavior:** Validates Cypher queries, GDS algorithms (like PageRank), and data persistence.

---

## 5. Code Coverage (JaCoCo)

The project uses the **JaCoCo Maven Plugin** to analyze code coverage.

After running `mvn clean verify`, you can view the detailed coverage report locally:

* **File Path:** `backend/target/site/jacoco/index.html`

Open this file in your web browser to see line-by-line coverage metrics.

---

## 6. Continuous Integration

The testing pipeline is automated via GitHub Actions (`.github/workflows/backend-ci.yml`).
On every push and pull request, the system:

1. Sets up JDK 25.
2. Runs `mvn spotless:check`.
3. Runs `mvn clean verify`.
4. Uploads the coverage report to **Codecov**.
5. Performs static code analysis via **SonarCloud**.
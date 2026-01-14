# NexusScholar Deployment Guide

This directory contains the automation scripts required to deploy the NexusScholar architecture onto a distributed cluster of Virtual Machines.

## Architecture

The deployment targets three specific nodes defined in your configuration:

| Node Role | IP Address (Example) | Components |
|:--- |:--- |:--- |
| **Primary** | `10.1.1.80` | Backend API, Neo4j, MongoDB (Primary) |
| **Worker 1** | `10.1.1.78` | MongoDB (Secondary) |
| **Worker 2** | `10.1.1.77` | MongoDB (Secondary) |

---

## Prerequisites

1.  **Local Environment:** You must have a Unix-like terminal (Linux, macOS, or WSL on Windows) with `ssh`, `scp`, and `openssl`.
2.  **Data Generation:** You must have successfully run **Phase 1, 2, and 3** of the Data Pipeline locally.
    *   Ensure `data-pipeline/scripts/import_files/` exists and contains data.
3.  **Root Directory Rule:**
    > **IMPORTANT:** All commands below must be executed from the **Project Root Directory** (`NexusScholar/`), NOT from inside the `deployment/` folder.

---

## Configuration

Ensure you have a `.env` file in the **Project Root** with the following keys:

```bash
# Application
APP_PORT=8080

# Database Credentials
MONGO_USER=admin
MONGO_PASSWORD=secretpassword
MONGO_DB_NAME=nexusscholar
MONGO_URI=mongodb://admin:secretpassword@localhost:27017/nexusscholar?authSource=admin

NEO4J_URI=bolt://neo4j:7687
NEO4J_USER=neo4j
NEO4J_PASSWORD=secretpassword

# Security
JWT_SECRET_KEY=...
JWT_VALIDITY_IN_MILLISECONDS=3600000

# Deployment Targets
PRIMARY_NODE=10.1.1.80
WORKER_1=10.1.1.78
WORKER_2=10.1.1.77
SSH_USER=root
```

---

## Deployment Steps

Execute these scripts in the exact order listed below.

### Step 1: Configure SSH Access
Generates a dedicated SSH key (`~/.ssh/nexusscholar_key`) and distributes it to all three VMs to allow passwordless automation.

```bash
chmod +x deployment/setup_keys.sh
./deployment/setup_keys.sh
```

### Step 2: Install Docker
Installs Docker Engine and the Docker Compose Plugin on all nodes.
*Note: This script uses a manual installation method to avoid compatibility issues with Ubuntu Focal.*

```bash
chmod +x deployment/install_docker.sh
./deployment/install_docker.sh
```

### Step 3: Deploy Infrastructure & Backend
1.  Generates dynamic `docker-compose.yml` files for each node.
2.  Uploads configurations and secrets.
3.  Starts the containers.
4.  **Automatically initializes the MongoDB Replica Set.**

```bash
chmod +x deployment/deploy_orchestrator.sh
./deployment/deploy_orchestrator.sh
```

### Step 4: Run Remote ETL (Data Load)
Uploads the local `import_files` (JSONL/CSV) to the Primary Node and triggers the import scripts inside the remote Docker containers.

```bash
chmod +x deployment/etl_orchestrator.sh
./deployment/etl_orchestrator.sh
```

---

## Verification

Once the deployment is complete, access the services via your browser:

*   **Swagger UI (Backend):**
    `http://10.1.1.80:8080/swagger-ui.html`

*   **Neo4j Browser:**
    `http://10.1.1.80:7474`
    *(Login with credentials from .env)*

---

## Troubleshooting

**1. "File not found" errors:**
*   Ensure you are running commands from `~/NexusScholar/`, **not** `~/NexusScholar/deployment/`.

**2. Docker installation fails:**
*   Ensure the VMs have internet access.
*   If `apt-get` locks, reboot the VM and try Step 2 again.

**3. MongoDB Replica Set fails to initialize:**
*   Check the logs on the primary node:
    ```bash
    ssh -i ~/.ssh/nexusscholar_key root@10.1.1.80 "docker logs mongo1"
    ```

**4. Updating the Backend Image:**
*   If you pushed a new image to GitHub Registry, simply re-run Step 3:
    ```bash
    ./deployment/deploy_orchestrator.sh
    ```
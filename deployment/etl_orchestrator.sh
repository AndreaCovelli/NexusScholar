#!/bin/bash
set -e

# ================= CONFIGURATION =================
# Infrastructure
PRIMARY_NODE="10.1.1.80"
SSH_USER="root"
SSH_OPTS="-i ~/.ssh/nexusscholar_key -o StrictHostKeyChecking=no -o ConnectTimeout=10"

# Local Paths (Relative to the Project Root)
LOCAL_PIPELINE_DIR="data-pipeline/scripts"
LOCAL_IMPORT_DIR="${LOCAL_PIPELINE_DIR}/import_files"
LOCAL_ENV_FILE=".env"

# Remote Paths (On the server)
REMOTE_BASE="/opt/nexusscholar"
REMOTE_SCRIPTS_DIR="${REMOTE_BASE}/scripts"
REMOTE_MONGO_IMPORT_DIR="${REMOTE_BASE}/mongo_import"
REMOTE_NEO4J_IMPORT_DIR="${REMOTE_BASE}/neo4j_import"

# ================= PRE-FLIGHT CHECKS =================
echo "--- Starting Remote ETL Orchestration ---"

# 1. Check for .env in the current directory (Root)
if [ ! -f "$LOCAL_ENV_FILE" ]; then
    echo "Error: .env file not found at $(pwd)/$LOCAL_ENV_FILE"
    echo "Please run this script from the Project Root."
    exit 1
fi

# 2. Check for Import Files
if [ ! -d "$LOCAL_IMPORT_DIR" ]; then
    echo "Error: Import files not found at $LOCAL_IMPORT_DIR"
    echo "Please run Phase 3 (Python integration) locally first."
    exit 1
fi

# ================= STEP 1: UPLOAD =================
echo "Step 1: Syncing Scripts and Data to Primary Node..."

# 1. Create directory structure on remote
echo "   Creating remote directories..."
ssh $SSH_OPTS ${SSH_USER}@${PRIMARY_NODE} \
    "mkdir -p ${REMOTE_SCRIPTS_DIR} ${REMOTE_MONGO_IMPORT_DIR} ${REMOTE_NEO4J_IMPORT_DIR}"

# 2. Sync Files using Rsync
# Replaced SCP with Rsync to prevent duplicate loading of unchanged files (Delta Transfer)
echo "   Syncing scripts and data via rsync..."

# Prepare rsync SSH command
RSYNC_CMD="rsync -azv -e \"ssh $SSH_OPTS\""

# Sync Scripts
eval "$RSYNC_CMD" \
    "${LOCAL_PIPELINE_DIR}/4_load_data.sh" \
    "${LOCAL_PIPELINE_DIR}/5_create_index.sh" \
    "${SSH_USER}@${PRIMARY_NODE}:${REMOTE_SCRIPTS_DIR}/"

# Sync MongoDB Data (JSONL)
# Trailing slash on source ensures contents are copied into the target directory
echo "   Syncing MongoDB data..."
eval "$RSYNC_CMD" \
    "${LOCAL_IMPORT_DIR}/mongodb/" \
    "${SSH_USER}@${PRIMARY_NODE}:${REMOTE_MONGO_IMPORT_DIR}/"

# Sync Neo4j Data (CSV)
echo "   Syncing Neo4j data..."
eval "$RSYNC_CMD" \
    "${LOCAL_IMPORT_DIR}/neo4j/" \
    "${SSH_USER}@${PRIMARY_NODE}:${REMOTE_NEO4J_IMPORT_DIR}/"

# ================= STEP 2: EXECUTE =================
echo "Step 2: Executing scripts on Primary Node..."

# We execute commands remotely via SSH.
# We explicitly set the environment variables to point to the *Remote* paths
# so the scripts (4_load_data.sh / 5_create_index.sh) look in the right place on the server.

ssh $SSH_OPTS ${SSH_USER}@${PRIMARY_NODE} /bin/bash <<EOF
set -e
cd ${REMOTE_BASE}

echo "   [Remote] Setting execution permissions..."
chmod +x scripts/*.sh

echo "   [Remote] Running Phase 4: Data Loading..."

# --- OVERRIDE CONFIGURATION FOR REMOTE CONTEXT ---
# These variables override the defaults inside the scripts
export COMPOSE_FILE="./docker-compose.yml"
export ENV_FILE="./.env"
export MONGO_IMPORT_DIR="${REMOTE_MONGO_IMPORT_DIR}"

# Note: NEO4J_IMPORT_DIR inside 4_load_data.sh defaults to /var/lib/neo4j/import
# which is the Internal Container path. Since our docker-compose.yml (deployed by deploy_orchestrator)
# mounts ./neo4j_import to /var/lib/neo4j/import, we don't need to change this variable.

./scripts/4_load_data.sh

echo "   [Remote] Running Phase 5: Index Creation..."
# ENV_FILE is already exported above
./scripts/5_create_index.sh

echo "[Remote] ETL Pipeline Execution Complete."
EOF

echo "=================================================="
echo " ETL Orchestration Finished Successfully."
echo "=================================================="

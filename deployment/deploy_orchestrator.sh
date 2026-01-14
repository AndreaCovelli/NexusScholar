#!/bin/bash
set -e

# ================= CONFIGURATION =================
# Infrastructure IPs
PRIMARY_NODE="10.1.1.80"   # Backend + Neo4j + Mongo Primary
WORKER_1="10.1.1.78"       # Mongo Secondary
WORKER_2="10.1.1.77"       # Mongo Secondary

# SSH & Rsync Configuration
SSH_USER="root"
SSH_KEY="~/.ssh/nexusscholar_key"
# We separate options for flexibility in rsync flags
SSH_OPTS="-o StrictHostKeyChecking=no -o ConnectTimeout=10"
RSYNC_SSH_CMD="ssh -i $SSH_KEY $SSH_OPTS"

# Application Config
PROJECT_NAME="nexusscholar"
REMOTE_BASE="/opt/${PROJECT_NAME}"
ENV_FILE=".env"
KEYFILE_PATH="deployment/mongo-keyfile"

# Docker Image
BACKEND_IMAGE="ghcr.io/andreacovelli/nexusscholar:latest"

# ================= PRE-FLIGHT CHECKS =================
echo "--- Starting Deployment Orchestration (RSYNC Mode) ---"

if [ ! -f "$ENV_FILE" ]; then
    echo "Error: .env file not found in $(pwd)"
    exit 1
fi

# Ensure MongoDB keyfile exists for cluster security
if [ ! -f "$KEYFILE_PATH" ]; then
    echo "Mongo Keyfile not found. Generating a new one..."
    mkdir -p deployment
    openssl rand -base64 756 > "$KEYFILE_PATH"
    chmod 600 "$KEYFILE_PATH"
    echo "Keyfile generated at $KEYFILE_PATH"
fi

# Load env vars for substitution in compose files
set -a
source "$ENV_FILE"
set +a

# ================= HELPER FUNCTIONS =================

generate_worker_compose() {
    local NODE_NAME=$1
    cat <<EOF
services:
  ${NODE_NAME}:
    image: mongo:8.2.3-noble
    container_name: ${NODE_NAME}
    restart: always
    ports:
      - "27017:27017"
    env_file: .env
    environment:
      MONGO_INITDB_ROOT_USERNAME: \${MONGO_USER}
      MONGO_INITDB_ROOT_PASSWORD: \${MONGO_PASSWORD}
    volumes:
      - ${NODE_NAME}_data:/data/db
      - ./mongo-keyfile:/opt/keyfile/mongo-keyfile
    command: >
      bash -c "cp /opt/keyfile/mongo-keyfile /etc/mongo-keyfile &&
      chmod 400 /etc/mongo-keyfile &&
      chown 999:999 /etc/mongo-keyfile &&
      docker-entrypoint.sh mongod --replSet rs0 --keyFile /etc/mongo-keyfile --bind_ip_all"

volumes:
  ${NODE_NAME}_data:
EOF
}

generate_primary_compose() {
    cat <<EOF
services:
  mongo1:
    image: mongo:8.2.3-noble
    container_name: mongo1
    restart: always
    ports:
      - "27017:27017"
    env_file: .env
    environment:
      MONGO_INITDB_ROOT_USERNAME: \${MONGO_USER}
      MONGO_INITDB_ROOT_PASSWORD: \${MONGO_PASSWORD}
    volumes:
      - mongo1_data:/data/db
      - ./mongo-keyfile:/opt/keyfile/mongo-keyfile
    command: >
      bash -c "cp /opt/keyfile/mongo-keyfile /etc/mongo-keyfile &&
      chmod 400 /etc/mongo-keyfile &&
      chown 999:999 /etc/mongo-keyfile &&
      docker-entrypoint.sh mongod --replSet rs0 --keyFile /etc/mongo-keyfile --bind_ip_all"

  neo4j:
    image: neo4j:2025.11.2-enterprise-bullseye
    container_name: nexusscholar-neo4j
    restart: always
    ports:
      - "7474:7474"
      - "7687:7687"
    environment:
      NEO4J_ACCEPT_LICENSE_AGREEMENT: "yes"
      NEO4J_AUTH: \${NEO4J_USER}/\${NEO4J_PASSWORD}
      NEO4J_PLUGINS: '["graph-data-science", "apoc"]'
      NEO4J_dbms_security_procedures_unrestricted: "gds.*,apoc.*"
      NEO4J_server_default__listen__address: "0.0.0.0"
    volumes:
      - neo4j_data:/data
      - ./neo4j_import:/var/lib/neo4j/import

  backend:
    image: ${BACKEND_IMAGE}
    container_name: nexusscholar-backend
    restart: always
    ports:
      - "8080:8080"
    env_file: .env
    depends_on:
      - mongo1
      - neo4j
    environment:
      SPRING_NEO4J_URI: bolt://neo4j:7687
      
      SPRING_NEO4J_AUTHENTICATION_USERNAME: \${NEO4J_USER}
      SPRING_NEO4J_AUTHENTICATION_PASSWORD: \${NEO4J_PASSWORD}
      SPRING_DATA_MONGODB_URI: mongodb://\${MONGO_USER}:\${MONGO_PASSWORD}@${PRIMARY_NODE}:27017,${WORKER_1}:27017,${WORKER_2}:27017/\${MONGO_DB_NAME}?replicaSet=rs0&authSource=admin

volumes:
  mongo1_data:
  neo4j_data:
EOF
}

deploy_node() {
    local TARGET_IP=$1
    local NODE_TYPE=$2 # "primary" or "worker"
    local WORKER_NAME=$3 

    echo ">> Deploying to ${NODE_TYPE} node: ${TARGET_IP}..."

    # 1. Prepare Directory
    ssh -i $SSH_KEY $SSH_OPTS ${SSH_USER}@${TARGET_IP} "mkdir -p ${REMOTE_BASE}/neo4j_import"

    # 2. Generate Docker Compose Locally
    local COMPOSE_FILE="docker-compose-temp-${TARGET_IP}.yml"
    if [ "$NODE_TYPE" == "primary" ]; then
        generate_primary_compose > "$COMPOSE_FILE"
    else
        generate_worker_compose "$WORKER_NAME" > "$COMPOSE_FILE"
    fi

    # 3. Sync Configuration Files (Rsync)
    # -a: archive mode (preserves permissions/times)
    # -z: compression
    # -v: verbose
    echo "   Syncing configurations..."
    rsync -azv -e "$RSYNC_SSH_CMD" \
        "$ENV_FILE" \
        "$KEYFILE_PATH" \
        ${SSH_USER}@${TARGET_IP}:${REMOTE_BASE}/

    # Rename keyfile at destination to match compose mount expectation if needed, 
    # but strictly speaking rsync keeps filenames. 
    # Note: docker-compose mounts ./mongo-keyfile, so we ensure it's at root of REMOTE_BASE.

    # 4. Sync Docker Compose
    # We rename it during rsync by specifying the destination filename
    rsync -azv -e "$RSYNC_SSH_CMD" \
        "$COMPOSE_FILE" \
        ${SSH_USER}@${TARGET_IP}:${REMOTE_BASE}/docker-compose.yml

    rm "$COMPOSE_FILE"

    # 5. Apply Changes
    echo "   Applying Docker Compose..."
    ssh -i $SSH_KEY $SSH_OPTS ${SSH_USER}@${TARGET_IP} \
        "cd ${REMOTE_BASE} && docker compose pull --quiet && docker compose up -d --remove-orphans"
    
    echo "   Done."
}

# ================= EXECUTION =================

# 1. Deploy Workers
deploy_node $WORKER_1 "worker" "mongo2"
deploy_node $WORKER_2 "worker" "mongo3"

# 2. Deploy Primary
deploy_node $PRIMARY_NODE "primary"

# 3. Wait for MongoDB
echo ">> Waiting for MongoDB Primary to be ready..."
RETRIES=0
MAX_RETRIES=12

while true; do
    if ssh -i $SSH_KEY $SSH_OPTS ${SSH_USER}@${PRIMARY_NODE} \
        "docker exec mongo1 mongosh --quiet -u ${MONGO_USER} -p ${MONGO_PASSWORD} --authenticationDatabase admin --eval \"db.adminCommand('ping')\"" >/dev/null 2>&1; then
        echo "   MongoDB is online."
        break
    fi

    RETRIES=$((RETRIES+1))
    if [ $RETRIES -ge $MAX_RETRIES ]; then
        echo "Error: MongoDB timed out."
        exit 1
    fi
    sleep 5
done

# 4. Initialize Replica Set
echo ">> Configuring Replica Set..."
RS_INIT_SCRIPT="
try {
    var status = rs.status();
    if (status.ok === 1 && status.set === 'rs0') {
        print('Replica Set already initialized.');
        quit(0);
    }
} catch (e) {}

var config = {
    _id: 'rs0',
    members: [
        { _id: 0, host: '${PRIMARY_NODE}:27017', priority: 2 },
        { _id: 1, host: '${WORKER_1}:27017', priority: 1 },
        { _id: 2, host: '${WORKER_2}:27017', priority: 1 }
    ]
};

try {
    rs.initiate(config);
    print('Replica Set initiated.');
} catch (e) {
    if (e.codeName === 'AlreadyInitialized') {
        print('Already initialized.');
    } else {
        throw e;
    }
}
"

ssh -i $SSH_KEY $SSH_OPTS ${SSH_USER}@${PRIMARY_NODE} \
    "docker exec mongo1 mongosh -u ${MONGO_USER} -p ${MONGO_PASSWORD} --authenticationDatabase admin --eval \"${RS_INIT_SCRIPT}\""

echo "=================================================="
echo " Deployment Complete!"
echo "   Backend: http://${PRIMARY_NODE}:8080/swagger-ui.html"
echo "   Neo4j:   http://${PRIMARY_NODE}:7474"
echo "=================================================="

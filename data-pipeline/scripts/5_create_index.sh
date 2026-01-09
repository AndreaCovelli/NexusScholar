#!/bin/bash
# data-pipeline/scripts/5_create_indexes.sh

# Prevent MinGW/Git Bash path conversion issues on Windows
export MSYS_NO_PATHCONV=1

# This script creates indexes for MongoDB collections.
# It drops existing indexes first to ensure a clean state.

set -e # Exit immediately if a command exits with a non-zero status.

# --- Configuration ---
COMPOSE_FILE="../../deployment/docker-compose.local.yml"

# Load environment variables (for credentials) from the project root
ENV_FILE="../../.env"
if [ -f "${ENV_FILE}" ]; then
  # Export variables from .env file safely
  set -a
  # shellcheck disable=SC1090
  source "${ENV_FILE}"
  set +a
else
  echo "Error: .env file not found at ${ENV_FILE}."
  exit 1
fi

# Use the DB name defined in the .env file
DB_NAME=${MONGO_DB_NAME}

echo "--- Starting Phase 5: Index Creation ---"

# Define the connection string for the container internal network
# We connect to 'mongo1' inside the docker network.
MONGO_CONN="mongodb://${MONGO_USER}:${MONGO_PASSWORD}@mongo1:27017/${DB_NAME}?replicaSet=rs0&authSource=admin"

echo "1. executing MongoDB index operations..."

# We use 'docker compose run' to execute the mongosh CLI inside the network.
# The --eval flag allows us to pass JavaScript commands directly.

docker compose -f $COMPOSE_FILE run --rm mongo1 mongosh "$MONGO_CONN" --eval "
    // --- 1. Collection: papers ---
    print('Processing collection: papers');
    // Remove all existing indexes (except the default _id_ index)
    db.papers.dropIndexes();

    // Create text index for title and abstract with specific weights
    db.papers.createIndex(
        { title: 'text', abstract: 'text' },
        { weights: { title: 10, abstract: 3 }, name: 'textPaperIndex' }
    );
    print(' -> Index textPaperIndex created.');

    // --- 2. Collection: authors ---
    print('Processing collection: authors');
    db.authors.dropIndexes();

    // Create ascending index on name
    db.authors.createIndex(
        { name: 1 },
        { name: 'authorsIndex' }
    );
    print(' -> Index authorsIndex created.');

    // --- 3. Collection: registeredUser ---
    print('Processing collection: registeredUser');
    db.registeredUser.dropIndexes();

    // Create ascending index on full_name
    db.registeredUser.createIndex(
        { full_name: 1 },
        { name: 'usersIndex' }
    );
    print(' -> Index usersIndex created.');

    // --- 4. Collection: admin ---
    print('Processing collection: admin');
    db.admin.dropIndexes();

    // Create ascending index on username
    db.admin.createIndex(
        { username: 1 },
        { name: 'adminIndex' }
    );
    print(' -> Index adminIndex created.');
"

echo "Phase 5: Index Creation Complete."
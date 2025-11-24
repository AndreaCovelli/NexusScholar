#!/bin/bash
# deployment/scripts/init-mongo-replica.sh

# This script initializes the MongoDB replica set (rs0).

# Determine the directory of the script and the path to the .env file in the project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd)"
ENV_FILE="${SCRIPT_DIR}/../../.env"

# Load environment variables (for credentials)
if [ -f "${ENV_FILE}" ]; then
  # Automatically export all variables defined in the sourced file
  set -a
  # shellcheck source=../.env
  source "${ENV_FILE}"
  set +a
else
  echo "Error: .env file not found at ${ENV_FILE}."
  exit 1
fi

echo "Waiting for MongoDB nodes to be ready (approx 15s)..."
# Give the containers time to start up and initialize authentication
sleep 5

echo "Initializing MongoDB Replica Set (rs0)..."

# Connect to mongo1 using authentication and initialize the replica set
# We use a try-catch block in mongosh to gracefully handle the case where it's already initialized.
docker exec mongo1 mongosh -u ${MONGO_USER} -p ${MONGO_PASSWORD} --authenticationDatabase admin --eval '
try {
    rs.initiate({
        _id: "rs0",
        members: [
            { _id: 0, host: "mongo1:27017" },
            { _id: 1, host: "mongo2:27017" },
            { _id: 2, host: "mongo3:27017" }
        ]
    });
    print("Replica set initiated successfully.");
} catch (e) {
    if (e.codeName === "AlreadyInitialized") {
        print("Replica set already initialized.");
    } else {
        print("Error initializing replica set:");
        printjson(e);
        throw e;
    }
}
'

if [ $? -eq 0 ]; then
  echo "Initialization command successful. Waiting for Primary election (approx 10s)..."
  sleep 10
  echo "Cluster is ready."
else
  echo "Failed to initialize Replica Set. Check the logs of the mongo1 container."
  exit 1
fi
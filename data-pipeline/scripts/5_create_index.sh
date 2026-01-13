export MSYS_NO_PATHCONV=1

set -e

# Load environment variables
ENV_FILE="../../.env"
if [ -f "${ENV_FILE}" ]; then
  set -a
  source "${ENV_FILE}"
  set +a
else
  echo "Error: .env file not found at ${ENV_FILE}."
  exit 1
fi

DB_NAME=${MONGO_DB_NAME}

echo "--- Starting Phase 5: Index Creation ---"

# We list mongo1, mongo2, and mongo3.
# mongosh will connect to one, discover the Replica Set topology,
# and automatically route the 'createIndex' commands to the current Primary (Leader).
MONGO_CONN="mongodb://${MONGO_USER}:${MONGO_PASSWORD}@mongo1:27017,mongo2:27017,mongo3:27017/${DB_NAME}?replicaSet=rs0&authSource=admin"

echo "1. executing MongoDB index operations..."

# EXECUTION UPDATE:
# Uses 'docker exec' to run inside the existing container (no variable warnings, faster).
docker exec -i mongo1 mongosh "$MONGO_CONN" --eval "
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

    // --- 3. Collection: registeredUsers ---
    print('Processing collection: registeredUsers');
    db.registeredUsers.dropIndexes();

    // Create ascending index on full_name
    db.registeredUsers.createIndex(
        { full_name: 1 },
        { name: 'usersIndex' }
    );
    print(' -> Index usersIndex created.');

    // --- 4. Collection: admins ---
    print('Processing collection: admin');
    db.admins.dropIndexes();

    // Create ascending index on username
    db.admins.createIndex(
        { username: 1 },
        { name: 'adminIndex' }
    );
    print(' -> Index adminIndex created.');
"

echo "Phase 5: Index Creation Complete."
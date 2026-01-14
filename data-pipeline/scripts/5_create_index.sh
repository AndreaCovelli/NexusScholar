export MSYS_NO_PATHCONV=1

set -e

# Allow overriding config via ENV variables for remote execution
: "${ENV_FILE:=../../.env}"
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
    // Ensure DOI and DBLP Key are unique
    db.papers.createIndex({ doi: 1 }, { name: 'doiIndex', unique: true, sparse: true });
    db.papers.createIndex({ dblp_key: 1 }, { name: 'dblpKeyIndex', unique: true });

    print(' -> Indexes for papers created.');

    // --- 2. Collection: authors ---
    print('Processing collection: authors');
    db.authors.dropIndexes();

    // Create ascending index on name
    db.authors.createIndex(
        { name: 1 },
        { name: 'authorsIndex' }
    );
    // Ensure Semantic Scholar ID is unique
    db.authors.createIndex({ s2_author_id: 1 }, { name: 's2IdIndex', unique: true, sparse: true });

    print(' -> Indexes for authors created.');

    // --- 3. Collection: registeredUsers ---
    print('Processing collection: registeredUsers');
    db.registeredUsers.dropIndexes();

    // Create ascending index on full_name
    db.registeredUsers.createIndex(
        { full_name: 1 },
        { name: 'usersIndex' }
    );

    db.registeredUsers.createIndex({ username: 1 }, { name: 'usersUsernameIndex', unique: true });
    db.registeredUsers.createIndex({ email: 1 }, { name: 'usersEmailIndex', unique: true });
    print(' -> Indexes for registeredUsers created.');

    // --- 4. Collection: admins ---
    print('Processing collection: admins');
    db.admins.dropIndexes();

    // Create ascending index on username
    db.admins.createIndex(
        { username: 1 },
        { name: 'adminIndex', unique: true }
    );
    db.admins.createIndex(
        { email: 1 },
        { name: 'adminEmailIndex', unique: true }
    );
    print(' -> Indexes for admins created.');
"

echo "2. executing Neo4j constraint operations..."

# Retry loop to ensure Neo4j is ready before applying constraints
MAX_RETRIES=30
count=0
echo "Waiting for Neo4j to be ready..."
until docker exec -i nexusscholar-neo4j cypher-shell -u "${NEO4J_USER}" -p "${NEO4J_PASSWORD}" "RETURN 1" > /dev/null 2>&1; do
    sleep 1
    count=$((count+1))
    if [ $count -ge $MAX_RETRIES ]; then
        echo "Neo4j timed out."
        exit 1
    fi
done

# Execute Cypher commands to create constraints and indexes
docker exec -i nexusscholar-neo4j cypher-shell -u "${NEO4J_USER}" -p "${NEO4J_PASSWORD}" <<EOF
    // 1. Ensure fast lookups and data integrity for Papers (O(1) MERGE)
    CREATE CONSTRAINT paper_id_unique IF NOT EXISTS FOR (p:Paper) REQUIRE p.paperID IS UNIQUE;

    // 2. Ensure fast lookups for Authors (O(1) MERGE)
    CREATE CONSTRAINT author_id_unique IF NOT EXISTS FOR (a:Author) REQUIRE a.authorId IS UNIQUE;

    // 3. Ensure fast lookups for Shortest Path (queries by Name)
    CREATE INDEX author_name_index IF NOT EXISTS FOR (a:Author) ON (a.name);
EOF

echo " -> Neo4j Constraints and Indexes created."

echo "Phase 5: Index Creation Complete."
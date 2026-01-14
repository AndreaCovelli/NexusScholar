#!/bin/bash

# ================= CONFIGURATION =================
ENV_FILE=".env"
KEY_NAME="nexusscholar_key"
KEY_PATH="$HOME/.ssh/$KEY_NAME"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}=== SSH Key Setup Utility ===${NC}"

# 1. Load Environment Variables
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${RED}Error: $ENV_FILE not found.${NC}"
    echo "Please create it and define PRIMARY_NODE, WORKER_1, WORKER_2, and SSH_USER."
    exit 1
fi

# Export variables from .env to current shell
set -a
source "$ENV_FILE"
set +a

# Validate required variables
if [[ -z "$PRIMARY_NODE" || -z "$WORKER_1" || -z "$WORKER_2" || -z "$SSH_USER" ]]; then
    echo -e "${RED}Error: Missing deployment variables in .env${NC}"
    echo "Ensure PRIMARY_NODE, WORKER_1, WORKER_2, and SSH_USER are set."
    exit 1
fi

# Build Array of Nodes
NODES=("$PRIMARY_NODE" "$WORKER_1" "$WORKER_2")

# 2. Generate Key if it doesn't exist
if [ -f "$KEY_PATH" ]; then
    echo -e "Found existing key at: ${GREEN}$KEY_PATH${NC}"
else
    echo -e "${YELLOW}Key not found. Generating new ed25519 key...${NC}"
    ssh-keygen -t ed25519 -f "$KEY_PATH" -C "nexus-deploy" -N "" -q
    echo -e "Key generated at: ${GREEN}$KEY_PATH${NC}"
fi

# 3. Iterate through nodes
echo -e "\n${YELLOW}Starting key distribution to user '${SSH_USER}'...${NC}"

for IP in "${NODES[@]}"; do
    echo -e "\n--------------------------------------------------"
    echo -e "Target: ${YELLOW}$IP${NC}"

    # Check connectivity and existing access using BatchMode
    if ssh -i "$KEY_PATH" -o BatchMode=yes -o StrictHostKeyChecking=no -o ConnectTimeout=3 "$SSH_USER@$IP" "exit" 2>/dev/null; then
        echo -e "${GREEN}Success:${NC} Passwordless access is already configured."
        continue
    fi

    # If check failed, attempt to copy ID
    echo -e "Keys not set up. Attempting to copy..."
    
    # ssh-copy-id handles the appending to authorized_keys and chmod permissions securely
    ssh-copy-id -i "$KEY_PATH.pub" -o StrictHostKeyChecking=no "$SSH_USER@$IP"

    # Verify immediately after copy
    if ssh -i "$KEY_PATH" -o BatchMode=yes -o StrictHostKeyChecking=no -o ConnectTimeout=3 "$SSH_USER@$IP" "exit" 2>/dev/null; then
        echo -e "${GREEN}Verified:${NC} Key uploaded successfully."
    else
        echo -e "${RED}Error:${NC} Failed to verify connection to $IP."
    fi
done

echo -e "\n--------------------------------------------------"
echo -e "Summary:"
echo -e "1. Key file: ${YELLOW}$KEY_PATH${NC}"
echo -e "2. Update your 'deploy_orchestrator.sh' SSH_OPTS variable to:"
echo -e "   ${GREEN}SSH_OPTS=\"-i $KEY_PATH -o StrictHostKeyChecking=no ...\"${NC}"

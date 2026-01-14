#!/bin/bash
set -e

# ================= CONFIGURATION =================
ENV_FILE=".env"
KEY_PATH="$HOME/.ssh/nexusscholar_key"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# ================= PRE-FLIGHT CHECKS =================
echo -e "${YELLOW}=== NexusScholar Docker Installer (Manual Method) ===${NC}"

if [ ! -f "$ENV_FILE" ]; then
    echo -e "${RED}Error: $ENV_FILE not found.${NC}"
    exit 1
fi

# Load Environment Variables
set -a
source "$ENV_FILE"
set +a

# Validate Variables
if [[ -z "$PRIMARY_NODE" || -z "$WORKER_1" || -z "$WORKER_2" || -z "$SSH_USER" ]]; then
    echo -e "${RED}Error: Missing node variables in .env${NC}"
    exit 1
fi

# Define Nodes
NODES=("$PRIMARY_NODE" "$WORKER_1" "$WORKER_2")

# SSH Options
SSH_OPTS="-i $KEY_PATH -o StrictHostKeyChecking=no -o ConnectTimeout=10"

# ================= INSTALLATION FUNCTION =================
install_on_node() {
    local IP=$1
    echo -e "\n--------------------------------------------------"
    echo -e "Target: ${YELLOW}$IP${NC}"

    # 1. Check if Docker is already properly installed
    if ssh $SSH_OPTS "$SSH_USER@$IP" "command -v docker &> /dev/null && docker compose version &> /dev/null"; then
        echo -e "${GREEN}Docker is already installed on $IP.${NC}"
        return
    fi

    echo -e "Installing Docker via Official Repositories..."

    # 2. Run Explicit Installation Commands
    # We explicitly install docker-ce, cli, containerd, and compose-plugin.
    # We DO NOT install docker-model-plugin which caused your error.
    ssh $SSH_OPTS "$SSH_USER@$IP" "bash -s" <<EOF
        set -e
        
        # 1. Clean up potential broken installs
        apt-get remove -y docker docker-engine docker.io containerd runc || true
        
        # 2. Update and Install Prereqs
        apt-get update -y
        apt-get install -y ca-certificates curl gnupg

        # 3. Add Docker GPG Key
        install -m 0755 -d /etc/apt/keyrings
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | tee /etc/apt/keyrings/docker.asc > /dev/null
        chmod a+r /etc/apt/keyrings/docker.asc

        # 4. Add Docker Repository
        echo \
          "deb [arch=\$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
          \$(. /etc/os-release && echo "\$VERSION_CODENAME") stable" | \
          tee /etc/apt/sources.list.d/docker.list > /dev/null

        # 5. Install Docker Engine and Compose (Excluding the broken plugin)
        apt-get update -y
        apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

        # 6. Enable Service
        systemctl enable docker
        systemctl start docker
EOF

    # 3. Verify Installation
    echo -e "Verifying installation on $IP..."
    if ssh $SSH_OPTS "$SSH_USER@$IP" "docker --version && docker compose version"; then
         echo -e "${GREEN}Success: Docker installed on $IP${NC}"
    else
         echo -e "${RED}Error: Verification failed on $IP${NC}"
         exit 1
    fi
}

# ================= EXECUTION FLOW =================

echo -e "${YELLOW}Starting Installation...${NC}"

for NODE in "${NODES[@]}"; do
    install_on_node "$NODE"
done

echo -e "\n${GREEN}=== Installation Complete on All Nodes ===${NC}"

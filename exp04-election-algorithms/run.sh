#!/bin/bash
set -e

send_msg() {
  # Simple python script to send tcp message
  docker exec $1 python3 -c "import socket; s = socket.socket(socket.AF_INET, socket.SOCK_STREAM); s.connect(('127.0.0.1', $2)); s.send(b'$3\n'); s.close()" 2>/dev/null || true
}

echo "=========================================="
echo " Starting BULLY Algorithm Demonstration"
echo "=========================================="
docker compose -f docker-compose-bully.yml up -d --build
sleep 3

echo "--> Failing Node 3 (The Leader)"
send_msg "bully-node3" "8003" "FAIL"
sleep 1

echo "--> Asking Node 1 to start election"
send_msg "bully-node1" "8001" "START_ELECTION"
sleep 3

docker compose -f docker-compose-bully.yml logs
docker compose -f docker-compose-bully.yml down


echo ""
echo "=========================================="
echo " Starting RING Algorithm Demonstration"
echo "=========================================="
docker compose -f docker-compose-ring.yml up -d --build
sleep 3

echo "--> Failing Node 3 (The Leader)"
send_msg "ring-node3" "8003" "FAIL"
sleep 1

echo "--> Asking Node 1 to start election"
send_msg "ring-node1" "8001" "START_ELECTION"
sleep 3

docker compose -f docker-compose-ring.yml logs
docker compose -f docker-compose-ring.yml down

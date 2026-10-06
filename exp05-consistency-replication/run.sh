#!/bin/bash
set -e

echo "Building and starting Primary and Replica containers..."
docker compose up -d --build
sleep 3

echo "Running Client Demo..."
docker run --rm --network tradex-repl -v $(pwd)/../out:/app/out tradex-exp05 java -cp /app/out tradex.replication.ClientDemo || docker exec primary java tradex.replication.ClientDemo

echo "Cleaning up..."
docker compose down

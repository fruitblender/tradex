#!/bin/bash
set -e

echo "Building and starting Primary and Backup containers..."
docker compose up -d --build
sleep 3

echo "Starting ClientMonitor process in background to trace events..."
docker run --rm --name client-monitor --network tradex-ft -v $(pwd)/../out:/app/out tradex-exp08 java -cp /app/out tradex.primarybackup.ClientMonitor &
CLIENT_PID=$!

echo "Waiting for 6 seconds to let client ping the primary..."
sleep 6

echo "KILLING PRIMARY CONTAINER to trigger failover..."
docker stop primary

echo "Waiting for ClientMonitor to finish recovery sequence..."
wait $CLIENT_PID

echo "Cleaning up..."
docker compose down

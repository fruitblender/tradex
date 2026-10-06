#!/bin/bash
set -e

echo "Building and starting Load Balancer and 3 Workers..."
docker compose up -d --build
sleep 3

echo "Running Load Balancing Demo..."
docker run --rm --network tradex-lb -v $(pwd)/../out:/app/out tradex-exp06 java -cp /app/out tradex.loadbalancer.ClientDemo || docker exec loadbalancer java tradex.loadbalancer.ClientDemo

echo "Cleaning up..."
docker compose down

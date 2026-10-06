#!/bin/bash
set -e

echo "Building Docker image..."
# Move to root of TradeX to have access to common/
cd ..
docker build -t tradex-exp01 -f exp01-rmi/Dockerfile .

echo "Creating Docker network..."
docker network create tradex-net || true

echo "Starting RMI Server container..."
# Using RMI_HOSTNAME so the RMI registry registers the correct IP/hostname
docker run -d --name rmi-server --network tradex-net -e RMI_HOSTNAME=rmi-server tradex-exp01 java tradex.rmi.RmiServer

echo "Waiting for server to start..."
sleep 3

echo "Starting RMI Client container..."
docker run --rm --network tradex-net -e RMI_SERVER_HOST=rmi-server tradex-exp01 java tradex.rmi.RmiClient

echo "Cleaning up..."
docker stop rmi-server
docker rm rmi-server
docker network rm tradex-net

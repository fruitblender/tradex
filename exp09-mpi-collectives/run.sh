#!/bin/bash
set -e

echo "Building Docker image containing MPJ Express for Experiment 9..."
cd ..
docker build -t tradex-exp09 -f exp09-mpi-collectives/Dockerfile .

echo "Executing Java MPI Collectives using 4 processes (Multicore device)..."
# We run the mpjrun.sh script installed inside the docker container
# -dev multicore allows it to spawn threads/processes locally without needing ssh daemons
docker run --rm -w /app tradex-exp09 mpjrun.sh -np 4 -dev multicore -cp out tradex.mpi.MpiCollectivesDemo

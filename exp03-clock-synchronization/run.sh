#!/bin/bash
set -e

echo "Building Docker image for Clock Synchronization..."
cd ..
docker build -t tradex-exp03 -f exp03-clock-synchronization/Dockerfile .

echo "Running Experiment 3..."
docker run --rm tradex-exp03

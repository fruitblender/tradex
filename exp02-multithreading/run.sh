#!/bin/bash
set -e

echo "Building and running Multithreading Experiment via Docker Compose..."
docker compose up --build

echo "Done. (Use Ctrl+C to exit if it stays attached)"

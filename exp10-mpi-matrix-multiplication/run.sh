#!/bin/bash
set -e

echo "Building Docker image containing MPJ Express for Experiment 10..."
cd ..
docker build -t tradex-exp10 -f exp10-mpi-matrix-multiplication/Dockerfile .

echo "Executing Java MPI Matrix Multiplication using 4 processes (Multicore device)..."
# Passing 400 as the matrix size argument
docker run --rm -w /app tradex-exp10 mpjrun.sh -np 4 -dev multicore -cp out tradex.mpi.MatrixMultiplicationDemo 400

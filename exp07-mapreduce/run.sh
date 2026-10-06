#!/bin/bash
set -e

echo "Building Docker image for MapReduce..."
cd ..
docker build -t tradex-exp07 -f exp07-mapreduce/Dockerfile .

echo "Running Experiment 7..."
# We run from the root directory context in Docker so that "data/trades.csv" maps to "exp07-mapreduce/data/trades.csv" 
# Oh wait, the working directory in Dockerfile is /app which contains the whole project.
# I should just run it. But let's pass the correct working directory to make sure path resolves correctly.
docker run --rm -w /app/exp07-mapreduce tradex-exp07 java -cp /app/out tradex.mapreduce.MapReduceDemo

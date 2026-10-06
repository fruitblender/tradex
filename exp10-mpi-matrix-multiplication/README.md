# Experiment 10 — Parallel Matrix Multiplication Using MPI

## Objective
Perform matrix multiplication in parallel using Java and a genuine MPI implementation (MPJ Express). Calculate the speedup compared to sequential execution.

## Relevant Distributed Systems Theory
Matrix multiplication ($C = A \times B$) is a computationally intensive task that is highly parallelizable. By distributing the rows of matrix A across multiple nodes, each node can independently compute a subset of the rows of the resulting matrix C. This is a classic demonstration of breaking a large problem into smaller parallel tasks using message passing.

## Architecture and Components
- **MPJ Express**: The Java MPI implementation providing the `mpjrun` execution framework.
- **MatrixMultiplicationDemo**: The MPI program implementing the algorithm.
  - **Broadcast**: Rank 0 broadcasts the entire `B` matrix to all processes.
  - **Scatter**: Rank 0 scatters the rows of matrix `A` such that each process gets $N / size$ rows.
  - **Local Compute**: Each process multiplies its subset of `A` against the full matrix `B`.
  - **Gather**: Rank 0 gathers the locally computed subsets of `C` to form the final result matrix.

## Compilation and Execution

### Docker execution
A convenience script `run.sh` is provided. Run it from inside the `exp10-mpi-matrix-multiplication` directory:
```bash
chmod +x run.sh
./run.sh
```

### Constraints
- The matrix size `N` must be evenly divisible by the number of processes (default 4 processes). By default, `N=400`.
- This ensures all MPI scatter/gather operations operate on equally sized buffers without complex offset logic, maintaining the code's readability for an academic submission.

## Demonstration
The script output demonstrates:
1. Matrix initialization.
2. The parallel computation execution and time taken.
3. The sequential computation execution and time taken.
4. **Verification**: A strict equality check confirming that the parallel output array exactly matches the sequential output array.
5. **Speedup**: $Sequential Time / Parallel Time$. 

*Note: For relatively small matrices (e.g. 400x400) executed in Java within a single machine (Multicore MPJ device), the speedup might sometimes be less than 1x. This is standard behavior caused by JVM JIT compilation overhead, threading overhead, and the constant overhead of MPI message passing compared to the extremely short pure math computation time.*

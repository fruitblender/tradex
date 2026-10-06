# Experiment 9 — MPI Collective Communication

## Objective
Demonstrate MPI (Message Passing Interface) collective communication using a genuine Java MPI runtime (MPJ Express). We will implement `Broadcast`, `Scatter`, and `Gather`.

## Relevant Distributed Systems Theory
MPI is a standardized and portable message-passing standard designed to function on a wide variety of parallel computing architectures. 
- **Broadcast**: The root process sends data to all other processes in the communicator.
- **Scatter**: The root process divides a large dataset into equal chunks and distributes one chunk to each participating process.
- **Gather**: The reverse of Scatter. Each process sends a piece of data to the root, which combines them back into a single array.

## TradeX Use Case
To analyze trade volumes in parallel:
1. **Broadcast**: The Root (Rank 0) decides the stock symbol (e.g., `TCS`) and broadcasts it to the cluster.
2. **Scatter**: The Root has a large array of historical trade quantities and scatters subsets of it to all processing nodes.
3. **Computation**: Each node sums its local chunk.
4. **Gather**: The Root gathers the partial sums from all nodes and computes the final aggregate trade volume.

## Architecture and Components
- **MPJ Express**: The pure-Java implementation of the MPI specification used.
- **MpiCollectivesDemo**: The single SPMD (Single Program, Multiple Data) Java class executed by all ranks. It uses `MPI.COMM_WORLD.Rank()` to branch logic based on its identity.

## Compilation and Execution

### Docker execution
Because setting up MPI requires environment variables and library paths (`mpj.jar`, `mpjrun.sh`), we use Docker to fully automate the MPJ Express download and configuration without relying on Maven.

A convenience script `run.sh` is provided. Run it from inside the `exp09-mpi-collectives` directory:
```bash
chmod +x run.sh
./run.sh
```

### Inner workings of execution
- The `Dockerfile` downloads MPJ Express `v0.44` and adds it to the system path.
- We compile the source using `javac -cp "$MPJ_HOME/lib/mpj.jar"`.
- We execute the program using `mpjrun.sh -np 4 -dev multicore`, which instructs the MPJ runtime to spawn 4 processes simulating 4 MPI nodes.

## Demonstration
The script output demonstrates:
1. Rank 0 broadcasting "TCS", and Ranks 0,1,2,3 printing receipt of it.
2. Rank 0 scattering an array of size 12 (`[10, 20, 30, ... 120]`).
3. Each rank receiving a sub-array of 3 elements and computing the sum.
4. Rank 0 gathering the sums and outputting the final consolidated trade volume.

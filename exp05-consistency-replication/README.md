# Experiment 5 — Data Consistency and Replication Models

## Objective
Demonstrate how replicated TradeX data behaves under different consistency models (Strong vs Eventual consistency).

## Relevant Distributed Systems Theory
Data replication prevents data loss and increases read availability. However, it introduces consistency challenges:
- **Synchronous Replication (Strong Consistency)**: The primary node writes data locally and forces all replicas to acknowledge the write before responding to the client. This guarantees a subsequent read from any replica will yield the latest value (Read-After-Write), but it suffers from high write latency.
- **Asynchronous Replication (Eventual Consistency)**: The primary writes locally, replies `ACK` to the client immediately, and replicates in the background. Write latency is low, but a read immediately after a write from a replica might return a "stale" (old) value until the data propagates.

## TradeX Use Case
In a stock trading system, the central `PrimaryNode` accepts stock price updates. 
- In SYNC mode, updates to TCS are slow but reliable across all `ReplicaNodes`.
- In ASYNC mode, updates are lightning-fast for the client, but a trader reading from a replica might temporarily see a stale price.

## Architecture and Components
- **PrimaryNode**: Handles writes and replicates data to replicas based on the active mode (`SYNC` or `ASYNC`).
- **ReplicaNode**: Accepts writes from the primary (with an artificial 1-second simulated network/disk delay) and serves read requests.
- **ClientDemo**: A short-lived process that connects to the nodes, updates prices, and immediately queries replicas to demonstrate the consistency models in action.

## Source Files
- `src/tradex/replication/PrimaryNode.java`
- `src/tradex/replication/ReplicaNode.java`
- `src/tradex/replication/ClientDemo.java`

## Compilation and Execution
We use Docker Compose to spawn the primary and 2 replica nodes on a single network.

### Docker execution
A convenience script `run.sh` is provided. It starts the cluster, executes the `ClientDemo`, and then stops the cluster. Run it from inside the `exp05-consistency-replication` directory:
```bash
chmod +x run.sh
./run.sh
```

## Demonstration
The ClientDemo output demonstrates:
1. **Synchronous Mode**: The write takes > 2000ms (due to the 1000ms delay simulated in each of the two replicas). Immediate reads from replicas show the newly updated price.
2. **Asynchronous Mode**: The write returns in < 10ms. Immediate reads from replicas show the *stale* (old) price. After waiting 3 seconds, the reads return the *converged* (new) price.

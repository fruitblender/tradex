# Experiment 4 — Bully and Ring Election Algorithms

## Objective
Demonstrate leader election among TradeX servers using both Bully and Ring algorithms across real network processes.

## Relevant Distributed Systems Theory
In a distributed system, a coordinator or leader is often required to manage centralized tasks (like finalizing an order sequence). 
- **Bully Algorithm**: When a node detects the leader is dead, it broadcasts an `ELECTION` to all nodes with a higher ID. If none respond, it bullies them by declaring itself the new `COORDINATOR`.
- **Ring Algorithm**: Nodes are logically arranged in a ring. A node detecting failure passes an `ELECTION` token to its neighbor. Each active node appends its ID. When the token wraps around to the initiator, the highest ID in the list is selected, and a `COORDINATOR` token is passed around the ring.

## TradeX Use Case
Three simulated trading servers (Nodes 1, 2, 3) where Node 3 is initially the leader. If Node 3 fails, the remaining nodes must elect Node 2 as the new leader to process high-priority trades.

## Architecture and Components
- **BullyNode**: Evaluates incoming sockets for `ELECTION` and responds with `OK` if it has a higher ID.
- **RingNode**: Evaluates incoming sockets and passes the list of candidates sequentially through the active ring peers.
- **Message Protocol**: A simple string-based TCP protocol (`ELECTION`, `COORDINATOR`, `FAIL`, `START_ELECTION`).

## Source Files
- `src/tradex/election/bully/BullyNode.java`
- `src/tradex/election/ring/RingNode.java`

## Compilation and Execution

### Docker execution
A convenience script `run.sh` orchestrates the failure and election events using Docker Compose. It first runs the Bully algorithm, simulates failure and election, prints logs, then repeats for the Ring algorithm.

```bash
chmod +x run.sh
./run.sh
```

## Demonstration
The script output displays:
1. **Bully Logs**: Node 1 starts an election, Node 2 replies `OK` (halting Node 1). Node 2 starts an election, Node 3 (failed) does not reply. Node 2 announces itself as Coordinator.
2. **Ring Logs**: Node 1 starts an election token `ELECTION:1,`. Node 2 appends itself `ELECTION:1,2,`. Node 3 is skipped because it's failed. Node 1 receives the wrapped token, finds the max (2), and sends `COORDINATOR:2` around the ring.

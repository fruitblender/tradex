# Experiment 8 — Fault Tolerance Using Primary-Backup Replication

## Objective
Demonstrate recovery when a primary TradeX server fails, utilizing a passive backup node.

## Relevant Distributed Systems Theory
Fault tolerance ensures a system remains operational even if some of its components fail. Primary-Backup replication (Passive Replication) works by assigning all client interactions to a Primary node. The Primary forwards state changes to the Backup. If the Primary fails (detected via heartbeats/timeouts), the Backup is promoted to take over its responsibilities.

*Note: In a true production system, timeout-based promotion without distributed consensus risks "split-brain" if a network partition occurs. This experiment demonstrates the basic timeout and promotion mechanism without full consensus overhead.*

## TradeX Use Case
The `ClientMonitor` submits a trade order to the `Primary`. The `Primary` replicates this order to the `Backup`. The `ClientMonitor` constantly pings the `Primary`. When the `Primary` container is abruptly stopped, the `ClientMonitor` detects timeouts, officially declares the primary dead, and sends a `PROMOTE` command to the `Backup`. The client then successfully queries the surviving new primary for the old order data.

## Architecture and Components
- **ServerNode**: Runs as either `PRIMARY` or `BACKUP`. 
  - `PRIMARY` accepts writes and replicates them.
  - `BACKUP` accepts replicated data and waits for a `PROMOTE` signal.
- **ClientMonitor**: Acts as both a client placing orders and an external cluster monitor tracking health and orchestrating failover.

## Source Files
- `src/tradex/primarybackup/ServerNode.java`
- `src/tradex/primarybackup/ClientMonitor.java`

## Compilation and Execution
We use Docker Compose to spawn the primary and backup nodes. The `run.sh` script automates the failure scenario.

### Docker execution
A convenience script `run.sh` is provided. Run it from inside the `exp08-primary-backup` directory:
```bash
chmod +x run.sh
./run.sh
```

## Demonstration
The script output demonstrates:
1. Submitting the order to the primary.
2. The client confirming replication by reading the backup.
3. The client logging successful `PING`s to the primary.
4. The bash script deliberately stopping the `primary` container.
5. The client experiencing socket timeouts. After 3 consecutive timeouts, it declares the primary dead.
6. The client promoting the backup.
7. The client successfully retrieving the original order from the newly promoted primary, proving no data was lost during the failure.

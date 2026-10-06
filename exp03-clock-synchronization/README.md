# Experiment 3 — Clock Synchronization

## Objective
Demonstrate logical clock ordering and physical clock synchronization in TradeX.

## Relevant Distributed Systems Theory
1. **Lamport Logical Clocks:** Because there is no perfectly synchronized global clock in a distributed system, causality (the "happens-before" relationship) is maintained using logical counters. Each node increments its counter for local events and updates its counter to `max(local_clock, message_timestamp) + 1` upon receiving a message.
2. **Berkeley Algorithm:** An active physical clock synchronization algorithm where a coordinator node polls the time from all participants, calculates an average offset, and sends specific adjustments back to each participant to bring all clocks into agreement.

## TradeX Use Case
- **Lamport:** A client placing a `BUY` order and the server processing it. The events across the network are causally ordered via Lamport timestamps.
- **Berkeley:** Multiple TradeX nodes drift apart in time. A central coordinator fetches their simulated tick times and synchronizes them to a unified average.

## Architecture and Components
- **LamportNode**: Maintains a `logicalClock`. Exposes methods for `localEvent`, `sendEvent`, and `receiveEvent`.
- **BerkeleyNode**: Maintains a `simulatedTime`. Allows querying time and applying adjustments.
- **ClockSyncDemo**: The main runner that creates instances of these nodes to simulate network messages and time-fetching within a single container.

*Note: Per the academic requirements, we represent independent clock participants in code and simulate the clock drifts, avoiding the complexities of NTP daemon manipulation inside Docker containers.*

## Source Files
- `src/tradex/clock/LamportNode.java`
- `src/tradex/clock/BerkeleyNode.java`
- `src/tradex/clock/ClockSyncDemo.java`

## Compilation and Execution

### Docker execution
A convenience script `run.sh` is provided. Run it from inside the `exp03-clock-synchronization` directory:
```bash
chmod +x run.sh
./run.sh
```

### Manual execution
Without Docker (from the root directory):
1. Compile: `javac -d out exp03-clock-synchronization/src/tradex/clock/*.java`
2. Run: `java -cp out tradex.clock.ClockSyncDemo`

## Demonstration
The output clearly shows two parts:
1. **Lamport:** The clock monotonically increases. When the server receives the client's message, it forces its clock to jump ahead of the message's timestamp.
2. **Berkeley:** Three nodes (Coordinator, Node-1, Node-2) start with differing times. The coordinator computes the average offset and issues adjustments. The final state shows all three clocks synchronized perfectly.

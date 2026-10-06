# Experiment 2 — Multithreading in a Distributed System

## Objective
Demonstrate concurrent processing of multiple TradeX requests using a Java server and thread pool.

## Relevant Distributed Systems Theory
In a distributed system, a single server node is frequently accessed by multiple clients concurrently. To avoid blocking all clients while one is being processed, the server must support multithreading. A thread pool (e.g., `ExecutorService`) limits resource exhaustion while maintaining concurrency. 
When multiple threads access shared memory (like an inventory or order database), synchronization is required to prevent race conditions and maintain data consistency.

## TradeX Use Case
The `TradingServer` maintains a central inventory of stocks. Multiple `TradingClient` connections attempt to buy stocks concurrently. The server utilizes a `FixedThreadPool` to assign a `ClientHandler` thread to each connection. The `processOrder` method is marked `synchronized` to ensure that checks against inventory and subsequent deductions happen atomically.

## Architecture and Components
- **TradingServer**: Listens on port 9090, accepting connections and submitting them to a thread pool. It holds the shared `stockInventory` and `totalOrdersProcessed` state.
- **ClientHandler**: The `Runnable` task that reads requests from a connected client socket and calls the server to process orders.
- **TradingClient**: Starts multiple local threads representing independent clients. It connects to the server and blasts `BUY` orders concurrently.

## Source Files
- `src/tradex/multithread/TradingServer.java`
- `src/tradex/multithread/ClientHandler.java`
- `src/tradex/multithread/TradingClient.java`

## Compilation and Execution
We use Docker Compose to run the server and client in separate containers.

### Docker execution
A convenience script `run.sh` is provided. Run it from inside the `exp02-multithreading` directory:
```bash
chmod +x run.sh
./run.sh
```

Alternatively:
```bash
docker compose up --build
```

To clean up after execution:
```bash
docker compose down
```

### Manual execution
Without Docker (from the root directory):
1. Compile: `javac -d out exp02-multithreading/src/tradex/multithread/*.java`
2. Run Server: `java -cp out tradex.multithread.TradingServer`
3. Run Client: `java -cp out tradex.multithread.TradingClient`

## Demonstration
When running, the server log will show:
- Connection acceptances.
- Worker threads (e.g., `pool-1-thread-X`) processing orders.
- The shared `totalOrdersProcessed` incrementing safely.
- The `stockInventory` of TCS decrementing exactly by the requested quantities without race conditions (overselling).

## Known Limitations
- Relying on the `synchronized` keyword synchronizes at the object level, meaning only one trade can happen at a time across *all* stocks. In a production system, finer-grained locking (e.g., row-level locks or `ConcurrentHashMap`) would be used to allow concurrent trades on different symbols.

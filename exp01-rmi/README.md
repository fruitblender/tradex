# Experiment 1 — Client-Server Communication Using Java RMI

## Objective
Demonstrate remote method invocation between a TradeX client and a remote trading server. This is part of the TradeX Distributed Systems laboratory project.

## Relevant Distributed Systems Theory
Remote Method Invocation (RMI) enables an object running in one Java Virtual Machine to invoke methods on an object running in another Java Virtual Machine. This provides a mechanism for client-server communication where network interactions are abstracted away behind familiar method calls.

## TradeX Use Case
In this experiment, the client acts as a trader terminal, making remote procedure calls to the `TradingService` on the central server to get stock prices, place orders, and check order statuses.

## Architecture and Components
- **TradingService**: The remote interface defining the operations.
- **TradingServiceImpl**: The implementation of the trading service, maintaining in-memory state of stocks and orders.
- **RmiServer**: Starts the RMI registry and binds the `TradingService` object.
- **RmiClient**: Connects to the RMI registry, looks up the remote object, and invokes operations.

## Source Files
- `src/tradex/rmi/TradingService.java`
- `src/tradex/rmi/TradingServiceImpl.java`
- `src/tradex/rmi/RmiServer.java`
- `src/tradex/rmi/RmiClient.java`
- Also depends on `common/src/tradex/model/*`

## Compilation and Execution
We use Docker to run the client and server in separate containers on the same Docker network.

### Docker execution
A convenience script `run.sh` is provided. Run it from inside the `exp01-rmi` directory:

```bash
chmod +x run.sh
./run.sh
```

### Manual execution
If you prefer running without Docker (ensure `javac` and `java` are available in your path):

1. Compile the code from the root project directory:
   ```bash
   mkdir -p out
   javac -d out common/src/tradex/model/*.java exp01-rmi/src/tradex/rmi/*.java
   ```
2. Start the server:
   ```bash
   java -cp out tradex.rmi.RmiServer
   ```
3. Open a new terminal and start the client:
   ```bash
   java -cp out tradex.rmi.RmiClient
   ```

## Demonstration
During execution, you should see the client output:
1. Querying the stock price of TCS.
2. Placing a BUY order for TCS.
3. Checking the order status to see it was EXECUTED.

## Known Limitations
- The RMI registry uses a fixed port (1099) and relies on explicit IP/hostname resolution (`RMI_HOSTNAME`), which is passed as an environment variable in Docker to prevent NAT/networking issues.
- The state is purely in-memory. Terminating the server clears all orders.

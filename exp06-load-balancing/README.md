# Experiment 6 — Load Balancing

## Objective
Implement and compare Round Robin and Least Connections load-balancing algorithms within TradeX.

## Relevant Distributed Systems Theory
A load balancer distributes incoming client requests across multiple backend servers to ensure no single server is overwhelmed.
- **Round Robin**: Distributes requests sequentially. If there are 3 servers, it sends requests to 1, 2, 3, 1, 2, 3... regardless of server capacity.
- **Least Connections**: Tracks the number of active (in-flight) requests on each server. It routes new requests to the server with the fewest active requests.

## TradeX Use Case
The Load Balancer fronts three trade processing `WorkerNode`s. These workers have different artificial processing speeds:
- Worker 1 (`FAST_WORKER`): 50ms per request.
- Worker 2 (`MED_WORKER`): 200ms per request.
- Worker 3 (`SLOW_WORKER`): 500ms per request.

## Architecture and Components
- **LoadBalancer**: Listens for client requests. Based on its active mode (`RR` or `LC`), it picks a worker, tracks active connections, forwards the request, and returns the response.
- **WorkerNode**: A backend node that sleeps for its configured `PROCESSING_DELAY` before responding.
- **ClientDemo**: Blasts 30 concurrent requests to the Load Balancer using a thread pool. It runs once in `RR` mode and once in `LC` mode, retrieving final statistics after each run.

## Source Files
- `src/tradex/loadbalancer/WorkerNode.java`
- `src/tradex/loadbalancer/LoadBalancer.java`
- `src/tradex/loadbalancer/ClientDemo.java`

## Compilation and Execution
We use Docker Compose to start the Load Balancer and the 3 workers. 

### Docker execution
A convenience script `run.sh` is provided. Run it from inside the `exp06-load-balancing` directory:
```bash
chmod +x run.sh
./run.sh
```

## Demonstration
The script output demonstrates:
1. **Round Robin**: The 30 requests are distributed exactly evenly (10 to each worker). Because Worker 3 is extremely slow, the entire batch takes a relatively long time, and Worker 3's queue piles up.
2. **Least Connections**: The load balancer intelligently routes traffic. The `FAST_WORKER` gets the bulk of the requests (since it clears its active queue quickly), `MED_WORKER` gets a few, and `SLOW_WORKER` gets very few (often just 1 or 2). The entire batch finishes much faster than Round Robin.

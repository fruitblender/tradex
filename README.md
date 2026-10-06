# TradeX - Distributed Systems Laboratory Project

TradeX is a minimal, simulated distributed stock trading system implemented in Java. It serves as the baseline project for demonstrating 10 fundamental Distributed Systems experiments. This is an academic demonstration project, prioritizing clarity, simplicity, and fundamental concepts over production-grade features.

## Technologies Used
* **Language:** Java (JDK 21 compatible)
* **Containerization:** Docker and Docker Compose
* **Build System:** Standard `javac` and `java` commands (No Maven or Gradle)
* **Architecture:** Minimal client-server and peer-to-peer implementations using sockets, RMI, and MPI.

## Project Structure
The project is divided into a `common` module for shared domain entities and individual, standalone folders for each of the 10 experiments. Each experiment is completely self-contained with its own execution instructions and Docker configuration.

```text
TradeX/
├── README.md
├── .gitignore
├── common/                               # Minimal shared domain model (Stock, Trader, Order)
├── exp01-rmi/                            # Client-Server Communication Using Java RMI
├── exp02-multithreading/                 # Multithreading in a Distributed System
├── exp03-clock-synchronization/          # Lamport Logical Clocks & Berkeley Algorithm
├── exp04-election-algorithms/            # Bully and Ring Election Algorithms
├── exp05-consistency-replication/        # Data Consistency and Replication Models
├── exp06-load-balancing/                 # Round Robin and Least Connections Load Balancing
├── exp07-mapreduce/                      # Basic MapReduce Using Java
├── exp08-primary-backup/                 # Fault Tolerance Using Primary-Backup Replication
├── exp09-mpi-collectives/                # MPI Collective Communication (Broadcast, Scatter, Gather)
└── exp10-mpi-matrix-multiplication/      # Parallel Matrix Multiplication Using MPI
```

## How to Build and Run
Each experiment contains its own `README.md` and a `run.sh` script (or `docker-compose.yml`) which provides exact instructions for compilation and execution using Docker. Generally, you will navigate into an experiment's directory and execute its runner script.

No external dependencies are required to be manually downloaded; Docker images will download and compile the necessary dependencies (such as MPJ Express for MPI experiments).

# Experiment 7 — Basic MapReduce Using Java

## Objective
Implement a basic MapReduce job to analyze historical TradeX records without relying on external frameworks like Hadoop.

## Relevant Distributed Systems Theory
MapReduce is a programming model for processing large data sets with a parallel, distributed algorithm. It consists of:
1. **Map Phase**: Filtering and sorting data (e.g., mapping a raw CSV line to a `(StockSymbol, Quantity)` Key-Value pair).
2. **Shuffle/Grouping Phase**: Redistributing data based on output keys, such that all data belonging to one key is located on the same worker node (e.g., grouping all `TCS` quantities together).
3. **Reduce Phase**: A summary operation (e.g., summing all grouped quantities for a given stock symbol).

## TradeX Use Case
We want to calculate the total historical quantity traded for each stock symbol based on a CSV export of trade data.

## Architecture and Components
- **MapReduceDemo**: A standalone Java application that sequentially performs:
  1. `readInput()`: Reads the CSV.
  2. `mapPhase()`: Converts lines to `KeyValue<String, Integer>`.
  3. `shufflePhase()`: Groups by symbol into a `Map<String, List<Integer>>`.
  4. `reducePhase()`: Sums the lists into final totals.
- **data/trades.csv**: The sample input data.

*Note: As requested for this academic project, this is a localized Java-only simulation of the MapReduce algorithm, designed to demonstrate the conceptual pipeline rather than distributed Hadoop execution.*

## Source Files
- `src/tradex/mapreduce/MapReduceDemo.java`
- `data/trades.csv`

## Compilation and Execution

### Docker execution
A convenience script `run.sh` is provided. Run it from inside the `exp07-mapreduce` directory:
```bash
chmod +x run.sh
./run.sh
```

### Manual execution
Without Docker (from the root directory):
1. Compile: `javac -d out exp07-mapreduce/src/tradex/mapreduce/*.java`
2. Run (from the `exp07-mapreduce` directory so the relative data path resolves): 
   ```bash
   cd exp07-mapreduce
   java -cp ../out tradex.mapreduce.MapReduceDemo
   ```

## Demonstration
The output clearly logs each phase of the MapReduce pipeline:
1. **Input Data**: The raw CSV lines.
2. **Map Phase**: Individual `(Symbol, Quantity)` pairs.
3. **Shuffle Phase**: Keys mapped to lists of quantities (e.g., `TCS -> [10, 15]`).
4. **Final Reduced Output**: The summed quantities for INFY (30), RELIANCE (5), and TCS (25).

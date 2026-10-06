package tradex.mapreduce;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MapReduceDemo {

    // Simple Key-Value Pair class
    static class KeyValue<K, V> {
        K key;
        V value;
        KeyValue(K key, V value) { this.key = key; this.value = value; }
        @Override
        public String toString() { return "(" + key + ", " + value + ")"; }
    }

    public static void main(String[] args) {
        String csvFile = "data/trades.csv";
        
        System.out.println("=========================================");
        System.out.println(" TradeX Basic MapReduce Simulation");
        System.out.println("=========================================");

        // 1. Input Reading
        List<String> rawLines = readInput(csvFile);
        System.out.println("\n--- 1. Input Data ---");
        rawLines.forEach(System.out::println);

        // 2. Map Phase
        // Input: String (line) -> Output: List of KeyValue (Symbol, Quantity)
        List<KeyValue<String, Integer>> mappedData = mapPhase(rawLines);
        System.out.println("\n--- 2. Map Phase Output ---");
        mappedData.forEach(System.out::println);

        // 3. Shuffle / Grouping Phase
        // Input: List of KeyValue -> Output: Map of Symbol to List of Quantities
        Map<String, List<Integer>> groupedData = shufflePhase(mappedData);
        System.out.println("\n--- 3. Shuffle / Grouping Output ---");
        groupedData.forEach((k, v) -> System.out.println(k + " -> " + v));

        // 4. Reduce Phase
        // Input: Map of Symbol to List of Quantities -> Output: Map of Symbol to Total Quantity
        Map<String, Integer> reducedData = reducePhase(groupedData);
        
        // 5. Output Generation
        System.out.println("\n--- 4 & 5. Final Reduced Output ---");
        // Using TreeMap to sort by keys alphabetically, matching the prompt's expected output
        new TreeMap<>(reducedData).forEach((k, v) -> System.out.printf("%-10s %d%n", k, v));
    }

    private static List<String> readInput(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; } // Skip header
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Could not read input file: " + e.getMessage());
        }
        return lines;
    }

    private static List<KeyValue<String, Integer>> mapPhase(List<String> lines) {
        List<KeyValue<String, Integer>> mapped = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length >= 3) {
                String symbol = parts[1];
                int quantity = Integer.parseInt(parts[2]);
                mapped.add(new KeyValue<>(symbol, quantity));
            }
        }
        return mapped;
    }

    private static Map<String, List<Integer>> shufflePhase(List<KeyValue<String, Integer>> mappedData) {
        Map<String, List<Integer>> grouped = new HashMap<>();
        for (KeyValue<String, Integer> kv : mappedData) {
            grouped.putIfAbsent(kv.key, new ArrayList<>());
            grouped.get(kv.key).add(kv.value);
        }
        return grouped;
    }

    private static Map<String, Integer> reducePhase(Map<String, List<Integer>> groupedData) {
        Map<String, Integer> reduced = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : groupedData.entrySet()) {
            int sum = 0;
            for (int qty : entry.getValue()) {
                sum += qty;
            }
            reduced.put(entry.getKey(), sum);
        }
        return reduced;
    }
}

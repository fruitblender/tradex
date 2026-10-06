package tradex.multithread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TradingServer {
    private static final int PORT = 9090;
    
    // Shared state
    private final Map<String, Integer> stockInventory;
    private int totalOrdersProcessed = 0;

    public TradingServer() {
        stockInventory = new HashMap<>();
        stockInventory.put("TCS", 1000);
        stockInventory.put("INFY", 500);
        stockInventory.put("RELIANCE", 2000);
    }

    // Synchronized method to protect shared state from race conditions
    public synchronized String processOrder(String symbol, int quantity) {
        String threadName = Thread.currentThread().getName();
        Integer available = stockInventory.get(symbol);
        
        if (available == null) {
            return "FAILED: Unknown stock " + symbol;
        }
        
        if (available >= quantity) {
            // Simulate processing time to increase chance of race conditions if not synchronized
            try { Thread.sleep(50); } catch (InterruptedException e) {}
            
            stockInventory.put(symbol, available - quantity);
            totalOrdersProcessed++;
            System.out.println("[" + threadName + "] Processed order: " + quantity + " of " + symbol + 
                               ". Remaining: " + stockInventory.get(symbol) + 
                               ". Total orders processed: " + totalOrdersProcessed);
            return "SUCCESS: Bought " + quantity + " " + symbol;
        } else {
            return "FAILED: Insufficient inventory for " + symbol;
        }
    }

    public void start() {
        ExecutorService threadPool = Executors.newFixedThreadPool(5);
        System.out.println("TradeX Multithreaded Server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                // Assign to worker thread
                threadPool.submit(new ClientHandler(clientSocket, this));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new TradingServer().start();
    }
}

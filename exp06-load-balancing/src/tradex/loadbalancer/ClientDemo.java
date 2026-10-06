package tradex.loadbalancer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientDemo {

    public static void main(String[] args) throws Exception {
        String lbHost = "loadbalancer";
        int port = 8000;

        System.out.println("==================================================");
        System.out.println(" EXPERIMENT 6: TRADEX LOAD BALANCING DEMO");
        System.out.println(" Problem Scenario: 30 Trade Orders sent concurrently to TradeX Load Balancer.");
        System.out.println(" Backend Servers available:");
        System.out.println("   - Worker 1 (Fast Execution Server): 50ms delay");
        System.out.println("   - Worker 2 (Medium Execution Server): 200ms delay");
        System.out.println("   - Worker 3 (Slow Execution Server): 500ms delay");
        System.out.println("==================================================\n");

        // PART A: Round Robin
        System.out.println("--- DEMO 1: Round Robin Algorithm ---");
        System.out.println("Strategy: Send orders strictly in order (1 -> 2 -> 3 -> 1 -> 2 -> 3...)");
        sendCommand(lbHost, port, "SET_MODE:RR");
        sendCommand(lbHost, port, "RESET_STATS");
        
        long startRR = System.currentTimeMillis();
        runLoadTest(lbHost, port, 30);
        long endRR = System.currentTimeMillis();
        
        String statsRR = sendCommand(lbHost, port, "STATS");
        System.out.println("Order Distribution across servers:");
        printParsedStats(statsRR);
        System.out.println("Total Batch Processing Time (Round Robin): " + (endRR - startRR) + " ms\n");

        
        // PART B: Least Connections
        System.out.println("--- DEMO 2: Least Connections Algorithm ---");
        System.out.println("Strategy: Dynamically send orders to whichever server has fewest active running trades");
        sendCommand(lbHost, port, "SET_MODE:LC");
        sendCommand(lbHost, port, "RESET_STATS");
        
        long startLC = System.currentTimeMillis();
        runLoadTest(lbHost, port, 30);
        long endLC = System.currentTimeMillis();
        
        String statsLC = sendCommand(lbHost, port, "STATS");
        System.out.println("Order Distribution across servers:");
        printParsedStats(statsLC);
        System.out.println("Total Batch Processing Time (Least Connections): " + (endLC - startLC) + " ms\n");

        System.out.println("==================================================");
        System.out.println(" CONCLUSION:");
        System.out.println(" Round Robin assigned exactly 10 orders to each server regardless of speed.");
        System.out.println(" Least Connections routed majority orders to the Fast Server (Worker 1), finishing the entire batch much faster!");
        System.out.println("==================================================");
    }

    private static void printParsedStats(String rawStats) {
        if (rawStats != null && rawStats.startsWith("STATS|")) {
            String[] pairs = rawStats.substring(6).split(",");
            for (String pair : pairs) {
                if (!pair.trim().isEmpty()) {
                    String[] parts = pair.split("=");
                    System.out.println("  * " + parts[0] + ": " + parts[1] + " trade orders processed");
                }
            }
        }
    }

    private static void runLoadTest(String host, int port, int totalRequests) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(totalRequests);

        for (int i = 0; i < totalRequests; i++) {
            final int orderId = i + 101;
            pool.submit(() -> {
                try {
                    sendCommand(host, port, "PROCESS_TRADE:ORDER_" + orderId);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        pool.shutdown();
    }

    private static String sendCommand(String host, int port, String cmd) throws Exception {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(cmd);
            return in.readLine();
        }
    }
}

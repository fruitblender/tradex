package tradex.replication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PrimaryNode {
    private final int port;
    private final String[] replicas;
    private final Map<String, Double> stockPrices = new HashMap<>();
    private String mode = "SYNC"; // Default to synchronous
    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(2);

    public PrimaryNode(int port, String replicasConfig) {
        this.port = port;
        this.replicas = replicasConfig.split(",");
        stockPrices.put("TCS", 3000.0);
    }

    public void start() {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("[Primary] Started on port " + port);
            while (true) {
                Socket socket = server.accept();
                new Thread(() -> handleRequest(socket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            String req = in.readLine();
            if (req == null) return;

            if (req.startsWith("SET_MODE:")) {
                mode = req.split(":")[1];
                System.out.println("[Primary] Mode set to " + mode);
                out.println("ACK");
            } else if (req.startsWith("WRITE:")) {
                String[] parts = req.split(":");
                String symbol = parts[1];
                double price = Double.parseDouble(parts[2]);
                
                // Write locally
                stockPrices.put(symbol, price);
                System.out.println("[Primary] Local write: " + symbol + " -> " + price);

                if ("SYNC".equals(mode)) {
                    // Synchronous: Wait for replicas
                    for (String replica : replicas) {
                        sendToReplica(replica, req);
                    }
                    System.out.println("[Primary] SYNC replication complete.");
                    out.println("ACK");
                } else {
                    // Asynchronous: Return ACK immediately, replicate in background
                    out.println("ACK");
                    asyncExecutor.submit(() -> {
                        for (String replica : replicas) {
                            sendToReplica(replica, req);
                        }
                        System.out.println("[Primary] ASYNC replication eventually complete.");
                    });
                }
            } else if (req.startsWith("READ:")) {
                String symbol = req.split(":")[1];
                out.println(stockPrices.getOrDefault(symbol, -1.0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToReplica(String replica, String msg) {
        String[] parts = replica.split(":");
        try (Socket socket = new Socket(parts[0], Integer.parseInt(parts[1]));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(msg);
            in.readLine(); // wait for ACK
        } catch (Exception e) {
            System.err.println("[Primary] Failed to replicate to " + replica);
        }
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv("NODE_PORT"));
        String replicas = System.getenv("REPLICAS");
        new PrimaryNode(port, replicas).start();
    }
}

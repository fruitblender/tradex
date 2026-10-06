package tradex.replication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ReplicaNode {
    private final int port;
    private final Map<String, Double> stockPrices = new HashMap<>();

    public ReplicaNode(int port) {
        this.port = port;
        stockPrices.put("TCS", 3000.0);
    }

    public void start() {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("[Replica] Started on port " + port);
            while (true) {
                Socket socket = server.accept();
                handleRequest(socket);
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

            if (req.startsWith("WRITE:")) {
                String[] parts = req.split(":");
                String symbol = parts[1];
                double price = Double.parseDouble(parts[2]);
                
                // Simulate some network/disk delay
                Thread.sleep(1000); 
                stockPrices.put(symbol, price);
                
                System.out.println("[Replica] Updated " + symbol + " to " + price);
                out.println("ACK");
            } else if (req.startsWith("READ:")) {
                String symbol = req.split(":")[1];
                double price = stockPrices.getOrDefault(symbol, -1.0);
                out.println(price);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv("NODE_PORT"));
        new ReplicaNode(port).start();
    }
}

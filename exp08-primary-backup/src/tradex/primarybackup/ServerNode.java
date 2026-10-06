package tradex.primarybackup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ServerNode {
    private final int port;
    private String mode;
    private final String backupAddress;
    private final Map<String, String> orderStore = new HashMap<>();

    public ServerNode(int port, String initialMode, String backupAddress) {
        this.port = port;
        this.mode = initialMode;
        this.backupAddress = backupAddress;
    }

    public void start() {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("[Server] Started on port " + port + " as " + mode);
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

            if (req.equals("PING")) {
                out.println("PONG");
            } else if (req.equals("PROMOTE")) {
                this.mode = "PRIMARY";
                System.out.println("[Server] PROMOTED TO PRIMARY!");
                out.println("ACK");
            } else if (req.startsWith("WRITE:")) {
                if (!"PRIMARY".equals(mode)) {
                    out.println("ERROR: Not Primary");
                    return;
                }
                String[] parts = req.split(":");
                String orderId = parts[1];
                String details = parts[2];
                
                orderStore.put(orderId, details);
                System.out.println("[Server-PRIMARY] Order written: " + orderId);
                
                // Replicate to backup
                if (backupAddress != null && !backupAddress.isEmpty()) {
                    boolean success = replicateToBackup("REPLICATE:" + orderId + ":" + details);
                    if (!success) {
                        System.out.println("[Server-PRIMARY] WARNING: Failed to replicate to backup.");
                    }
                }
                out.println("ACK");
            } else if (req.startsWith("REPLICATE:")) {
                String[] parts = req.split(":");
                String orderId = parts[1];
                String details = parts[2];
                orderStore.put(orderId, details);
                System.out.println("[Server-BACKUP] Order replicated: " + orderId);
                out.println("ACK");
            } else if (req.startsWith("READ:")) {
                String orderId = req.split(":")[1];
                out.println(orderStore.getOrDefault(orderId, "NOT_FOUND"));
            }
        } catch (Exception e) {
            // Ignore stream closed errors
        }
    }

    private boolean replicateToBackup(String msg) {
        String[] parts = backupAddress.split(":");
        try (Socket socket = new Socket(parts[0], Integer.parseInt(parts[1]));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(msg);
            return "ACK".equals(in.readLine());
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv("NODE_PORT"));
        String mode = System.getenv("NODE_MODE");
        String backup = System.getenv("BACKUP_ADDR");
        new ServerNode(port, mode, backup).start();
    }
}

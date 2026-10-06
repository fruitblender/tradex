package tradex.loadbalancer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class WorkerNode {
    private final int port;
    private final String workerId;
    private final int delayMs;
    private final AtomicInteger totalProcessed = new AtomicInteger(0);

    public WorkerNode(String workerId, int port, int delayMs) {
        this.workerId = workerId;
        this.port = port;
        this.delayMs = delayMs;
    }

    public void start() {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("[" + workerId + "] Started on port " + port + " with processing delay " + delayMs + "ms");
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
            if (req != null) {
                // Simulate processing delay
                Thread.sleep(delayMs);
                
                int count = totalProcessed.incrementAndGet();
                out.println(workerId + "_ACK_TOTAL:" + count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String id = System.getenv("WORKER_ID");
        int port = Integer.parseInt(System.getenv("WORKER_PORT"));
        int delay = Integer.parseInt(System.getenv("PROCESSING_DELAY"));
        new WorkerNode(id, port, delay).start();
    }
}

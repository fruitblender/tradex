package tradex.loadbalancer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancer {
    private final int port;
    private final List<String> workers = new ArrayList<>();
    private final Map<String, AtomicInteger> activeRequests = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> totalRequestsAssigned = new ConcurrentHashMap<>();
    
    private String mode = "RR"; // Default Round Robin
    private int rrIndex = 0;

    public LoadBalancer(int port, String workersConfig) {
        this.port = port;
        for (String w : workersConfig.split(",")) {
            workers.add(w);
            activeRequests.put(w, new AtomicInteger(0));
            totalRequestsAssigned.put(w, new AtomicInteger(0));
        }
    }

    public void start() {
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("[LoadBalancer] Started on port " + port);
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
                this.mode = req.split(":")[1];
                System.out.println("[LoadBalancer] Mode set to " + mode);
                out.println("ACK");
                return;
            } else if (req.equals("STATS")) {
                StringBuilder sb = new StringBuilder("STATS|");
                for (String w : workers) {
                    sb.append(w).append("=").append(totalRequestsAssigned.get(w).get()).append(",");
                }
                out.println(sb.toString());
                return;
            } else if (req.equals("RESET_STATS")) {
                for (String w : workers) {
                    totalRequestsAssigned.get(w).set(0);
                }
                out.println("ACK");
                return;
            }

            // Route request
            String selectedWorker = selectWorker();
            totalRequestsAssigned.get(selectedWorker).incrementAndGet();
            activeRequests.get(selectedWorker).incrementAndGet();
            
            try {
                String resp = forwardToWorker(selectedWorker, req);
                out.println(resp);
            } finally {
                activeRequests.get(selectedWorker).decrementAndGet();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized String selectWorker() {
        if ("RR".equals(mode)) {
            String w = workers.get(rrIndex);
            rrIndex = (rrIndex + 1) % workers.size();
            return w;
        } else if ("LC".equals(mode)) {
            String bestWorker = workers.get(0);
            int minActive = activeRequests.get(bestWorker).get();
            
            for (int i = 1; i < workers.size(); i++) {
                String w = workers.get(i);
                int active = activeRequests.get(w).get();
                if (active < minActive) {
                    minActive = active;
                    bestWorker = w;
                }
            }
            return bestWorker;
        }
        return workers.get(0);
    }

    private String forwardToWorker(String workerConfig, String req) {
        String[] parts = workerConfig.split(":");
        try (Socket socket = new Socket(parts[0], Integer.parseInt(parts[1]));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(req);
            return in.readLine();
        } catch (Exception e) {
            return "ERROR";
        }
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv("LB_PORT"));
        String workers = System.getenv("WORKERS");
        new LoadBalancer(port, workers).start();
    }
}

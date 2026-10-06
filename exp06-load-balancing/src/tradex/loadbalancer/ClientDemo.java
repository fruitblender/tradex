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
        System.out.println(" PART A: Round Robin Load Balancing");
        System.out.println("==================================================");
        sendCommand(lbHost, port, "SET_MODE:RR");
        sendCommand(lbHost, port, "RESET_STATS");
        
        runLoadTest(lbHost, port, 30);
        
        System.out.println("\n--- Round Robin Final Distribution ---");
        System.out.println(sendCommand(lbHost, port, "STATS"));

        
        System.out.println("\n==================================================");
        System.out.println(" PART B: Least Connections Load Balancing");
        System.out.println("==================================================");
        sendCommand(lbHost, port, "SET_MODE:LC");
        sendCommand(lbHost, port, "RESET_STATS");
        
        runLoadTest(lbHost, port, 30);
        
        System.out.println("\n--- Least Connections Final Distribution ---");
        System.out.println(sendCommand(lbHost, port, "STATS"));
    }

    private static void runLoadTest(String host, int port, int totalRequests) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(totalRequests);
        long start = System.currentTimeMillis();

        for (int i = 0; i < totalRequests; i++) {
            final int reqId = i;
            pool.submit(() -> {
                try {
                    String resp = sendCommand(host, port, "PROCESS_TRADE:" + reqId);
                    // Commenting out detailed response to avoid clutter
                    // System.out.println("Req " + reqId + " -> " + resp);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long end = System.currentTimeMillis();
        System.out.println("Processed " + totalRequests + " requests in " + (end - start) + "ms");
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

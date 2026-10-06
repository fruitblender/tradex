package tradex.multithread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TradingClient implements Runnable {
    private final String serverHost;
    private final int port;
    private final String clientId;

    public TradingClient(String serverHost, int port, String clientId) {
        this.serverHost = serverHost;
        this.port = port;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(serverHost, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            for (int i = 1; i <= 5; i++) {
                String command = "BUY,TCS,10";
                System.out.println("[" + clientId + "] Sending: " + command);
                out.println(command);
                
                String response = in.readLine();
                System.out.println("[" + clientId + "] Response: " + response);
                
                Thread.sleep(100); // Wait a bit before next request
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("[" + clientId + "] Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String host = System.getenv("SERVER_HOST");
        if (host == null) {
            host = "127.0.0.1";
        }
        
        System.out.println("Starting multiple simulated clients targeting " + host + ":9090...");
        
        // Start 3 independent client threads to simulate concurrent requests
        Thread c1 = new Thread(new TradingClient(host, 9090, "Client-A"));
        Thread c2 = new Thread(new TradingClient(host, 9090, "Client-B"));
        Thread c3 = new Thread(new TradingClient(host, 9090, "Client-C"));
        
        c1.start();
        c2.start();
        c3.start();
        
        try {
            c1.join();
            c2.join();
            c3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println("All simulated clients finished.");
    }
}

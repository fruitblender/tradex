package tradex.multithread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final TradingServer server;

    public ClientHandler(Socket clientSocket, TradingServer server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println("[" + threadName + "] Handling client request...");
        
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // Expected format: BUY,SYMBOL,QUANTITY (e.g., BUY,TCS,10)
                String[] parts = inputLine.split(",");
                if (parts.length == 3 && parts[0].equals("BUY")) {
                    String symbol = parts[1];
                    int quantity = Integer.parseInt(parts[2]);
                    
                    String result = server.processOrder(symbol, quantity);
                    out.println(result);
                } else {
                    out.println("ERROR: Invalid command format. Use BUY,SYMBOL,QUANTITY");
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("[" + threadName + "] Client disconnected or error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

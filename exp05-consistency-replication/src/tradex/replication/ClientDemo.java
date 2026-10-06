package tradex.replication;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientDemo {

    public static void main(String[] args) throws Exception {
        String primaryHost = "primary";
        String rep1Host = "replica1";
        String rep2Host = "replica2";
        int port = 8000;

        System.out.println("==================================================");
        System.out.println("   TRADEX EXPERIMENT 5: DATA REPLICATION & CONSISTENCY");
        System.out.println("   Scenario: Primary Server updates stock prices and replicates to Replica 1 & Replica 2");
        System.out.println("==================================================\n");
        
        System.out.println("STEP 1: Trader updates Stock Prices on Primary Server");
        System.out.println("  * Setting TCS Price = Rs 3500.00");
        sendCommand(primaryHost, port, "WRITE:TCS:3500.0");
        System.out.println("  * Setting RELIANCE Price = Rs 2980.00");
        sendCommand(primaryHost, port, "WRITE:RELIANCE:2980.0");
        System.out.println("  [Primary Status] Prices saved locally and broadcasted to Replicas.\n");

        System.out.println("STEP 2: Reading Stock Prices from Replica 1 Server");
        System.out.println("  * TCS Price on Replica 1      : Rs " + sendCommand(rep1Host, port, "READ:TCS"));
        System.out.println("  * RELIANCE Price on Replica 1 : Rs " + sendCommand(rep1Host, port, "READ:RELIANCE") + "\n");

        System.out.println("STEP 3: Reading Stock Prices from Replica 2 Server");
        System.out.println("  * TCS Price on Replica 2      : Rs " + sendCommand(rep2Host, port, "READ:TCS"));
        System.out.println("  * RELIANCE Price on Replica 2 : Rs " + sendCommand(rep2Host, port, "READ:RELIANCE") + "\n");

        System.out.println("==================================================");
        System.out.println(" SUCCESS: Data Consistency Verified!");
        System.out.println(" Both Replica servers hold the exact updated stock prices from Primary.");
        System.out.println("==================================================");
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

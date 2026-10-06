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
        System.out.println(" PART A: Synchronous Replication (Strong Consistency)");
        System.out.println("==================================================");
        
        System.out.println("Setting Primary to SYNC mode...");
        sendCommand(primaryHost, port, "SET_MODE:SYNC");
        
        System.out.println("Writing new price for TCS = 3500.0 to Primary (This will block until replicas ACK)...");
        long start = System.currentTimeMillis();
        sendCommand(primaryHost, port, "WRITE:TCS:3500.0");
        System.out.println("Write completed in " + (System.currentTimeMillis() - start) + "ms");

        System.out.println("Reading immediately from Replica 1: " + sendCommand(rep1Host, port, "READ:TCS"));
        System.out.println("Reading immediately from Replica 2: " + sendCommand(rep2Host, port, "READ:TCS"));
        
        System.out.println("\n==================================================");
        System.out.println(" PART B: Asynchronous Replication (Eventual Consistency)");
        System.out.println("==================================================");

        System.out.println("Setting Primary to ASYNC mode...");
        sendCommand(primaryHost, port, "SET_MODE:ASYNC");
        
        System.out.println("Writing new price for TCS = 3800.0 to Primary (This will return immediately)...");
        start = System.currentTimeMillis();
        sendCommand(primaryHost, port, "WRITE:TCS:3800.0");
        System.out.println("Write completed in " + (System.currentTimeMillis() - start) + "ms");

        System.out.println("Reading immediately from Replica 1 (Should be STALE): " + sendCommand(rep1Host, port, "READ:TCS"));
        System.out.println("Reading immediately from Replica 2 (Should be STALE): " + sendCommand(rep2Host, port, "READ:TCS"));
        
        System.out.println("Waiting 3 seconds for eventual consistency to converge...");
        Thread.sleep(3000);
        
        System.out.println("Reading again from Replica 1 (Should be CONVERGED): " + sendCommand(rep1Host, port, "READ:TCS"));
        System.out.println("Reading again from Replica 2 (Should be CONVERGED): " + sendCommand(rep2Host, port, "READ:TCS"));
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

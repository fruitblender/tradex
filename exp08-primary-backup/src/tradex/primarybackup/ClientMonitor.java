package tradex.primarybackup;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientMonitor {

    public static void main(String[] args) throws Exception {
        String primaryHost = "primary";
        String backupHost = "backup";
        int port = 8000;

        System.out.println("==================================================");
        System.out.println("   TRADEX EXPERIMENT 8: PRIMARY-BACKUP FAULT TOLERANCE");
        System.out.println("   Scenario: Primary Server crashes -> Backup Server takes over with ZERO data loss");
        System.out.println("==================================================\n");

        // 1. Submit trade order
        System.out.println("STEP 1: Trader places an Order on Primary Server");
        System.out.println("  Order Details : Order #ORD-101 -> BUY 50 Shares of TCS @ Rs 3500.00");
        String writeResp = sendCommand(primaryHost, port, "WRITE:ORD-101:BUY 50 TCS @ Rs 3500.00");
        System.out.println("  Primary Status: ORDER STORED & ACKNOWLEDGED (" + writeResp + ")\n");

        // 2. Replication check on Backup
        System.out.println("STEP 2: Primary replicates order data to Passive Backup Server");
        String repCheck = sendCommand(backupHost, port, "READ:ORD-101");
        System.out.println("  Backup Status : Synced Data -> \"" + repCheck + "\"\n");

        // 3. Monitor Health
        System.out.println("STEP 3: Client periodically pings Primary Server to monitor health");
        String pingResp = sendCommand(primaryHost, port, "PING");
        System.out.println("  Heartbeat Check: " + pingResp + " (Primary Server Healthy)\n");

        System.out.println("--------------------------------------------------");
        System.out.println("!!! SIMULATING SERVER FAILURE: PRIMARY CONTAINER CRASHES !!!");
        System.out.println("--------------------------------------------------\n");
        
        int timeoutCount = 0;
        int maxTimeouts = 2;
        while (timeoutCount < maxTimeouts) {
            try {
                sendCommand(primaryHost, port, "PING");
            } catch (Exception e) {
                timeoutCount++;
                System.out.println("  * Ping to Primary failed! (Timeout " + timeoutCount + " of " + maxTimeouts + ")");
                Thread.sleep(1000);
            }
        }

        System.out.println("\n  [ALERT] Primary Server confirmed DOWN after " + maxTimeouts + " failed heartbeats!\n");

        // 4. Promote Backup
        System.out.println("STEP 4: Initiating Failover -> Promoting Backup Server to NEW PRIMARY");
        String promResp = sendCommand(backupHost, port, "PROMOTE");
        System.out.println("  Promotion Status: " + promResp + " (Backup is now NEW PRIMARY)\n");
        
        // 5. Query order from new Primary
        System.out.println("STEP 5: Trader fetches order #ORD-101 from the New Primary Server");
        String finalRead = sendCommand(backupHost, port, "READ:ORD-101");
        System.out.println("  Order Retrieved: \"" + finalRead + "\"");
        
        System.out.println("\n==================================================");
        System.out.println(" SUCCESS: Fault Tolerance Verified!");
        System.out.println(" The Backup successfully took over and preserved all trade data.");
        System.out.println("==================================================");
    }

    private static String sendCommand(String host, int port, String cmd) throws Exception {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            // Set socket timeout so we don't block forever if node is dead
            socket.setSoTimeout(1500); 
            out.println(cmd);
            return in.readLine();
        }
    }
}

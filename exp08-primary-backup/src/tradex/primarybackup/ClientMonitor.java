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
        System.out.println(" Client/Monitor connecting to Cluster...");
        System.out.println("==================================================");

        // 1. Submit an order to primary
        System.out.println("\n--- 1. Submitting Order to Primary ---");
        String writeResp = sendCommand(primaryHost, port, "WRITE:ORD-100:BUY-TCS-50-SHARES");
        System.out.println("Response from Primary: " + writeResp);

        // 2. Query backup directly to confirm replication (Backdoor read for demo)
        System.out.println("\n--- 2. Confirming Replication on Backup ---");
        String repCheck = sendCommand(backupHost, port, "READ:ORD-100");
        System.out.println("Order fetched from Backup: " + repCheck);

        // 3. Monitor Health
        System.out.println("\n--- 3 & 4. Monitoring Primary Health (Waiting for Failure) ---");
        int timeoutCount = 0;
        int maxTimeouts = 3;
        
        while (timeoutCount < maxTimeouts) {
            try {
                String pingResp = sendCommand(primaryHost, port, "PING");
                System.out.println("Primary Health: OK (" + pingResp + ")");
                Thread.sleep(2000);
            } catch (Exception e) {
                timeoutCount++;
                System.out.println("Primary Health: FAILED (Timeout " + timeoutCount + "/" + maxTimeouts + ")");
                Thread.sleep(1000);
            }
        }

        System.out.println("\nPRIMARY DECLARED DEAD.");

        // 5. Promote the Backup
        System.out.println("\n--- 5. Promoting the Backup to Primary ---");
        String promResp = sendCommand(backupHost, port, "PROMOTE");
        System.out.println("Promotion Response: " + promResp);
        
        // 6. Query the surviving service
        System.out.println("\n--- 6. Querying the new Primary for the existing order ---");
        String finalRead = sendCommand(backupHost, port, "READ:ORD-100");
        System.out.println("Order successfully recovered from new Primary: " + finalRead);
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

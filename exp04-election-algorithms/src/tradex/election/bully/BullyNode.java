package tradex.election.bully;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class BullyNode {
    private final int myId;
    private final int myPort;
    private final List<Peer> allPeers;
    private int coordinatorId = -1;
    private final AtomicBoolean isActive = new AtomicBoolean(true);

    public BullyNode(int id, int port, String peersConfig) {
        this.myId = id;
        this.myPort = port;
        this.allPeers = new ArrayList<>();
        
        if (peersConfig != null && !peersConfig.isEmpty()) {
            for (String p : peersConfig.split(",")) {
                String[] parts = p.split(":");
                allPeers.add(new Peer(Integer.parseInt(parts[0]), parts[1], Integer.parseInt(parts[2])));
            }
        }
    }

    public void start() {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(myPort)) {
                System.out.println("[Node " + myId + "] Bully Node started on port " + myPort);
                while (true) {
                    Socket socket = server.accept();
                    new Thread(() -> handleConnection(socket)).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        
        // Initial leader assumption (highest ID)
        coordinatorId = 3; 
    }

    private void handleConnection(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            String msg = in.readLine();
            if (msg == null || !isActive.get()) {
                if (msg != null && msg.equals("PING")) out.println("DOWN");
                return;
            }

            System.out.println("[Node " + myId + "] Received: " + msg);
            
            if (msg.startsWith("ELECTION")) {
                int senderId = Integer.parseInt(msg.split(":")[1]);
                out.println("OK");
                if (myId > senderId) {
                    // Start own election in a new thread
                    new Thread(this::startElection).start();
                }
            } else if (msg.startsWith("COORDINATOR")) {
                coordinatorId = Integer.parseInt(msg.split(":")[1]);
                System.out.println("[Node " + myId + "] New Coordinator is Node " + coordinatorId);
                out.println("ACK");
            } else if (msg.equals("FAIL")) {
                isActive.set(false);
                System.out.println("[Node " + myId + "] SIMULATING FAILURE.");
                out.println("FAILED");
            } else if (msg.equals("START_ELECTION")) {
                out.println("ACK");
                new Thread(this::startElection).start();
            } else if (msg.equals("PING")) {
                out.println("PONG");
            }
        } catch (Exception e) {
            // Ignore socket errors on closing
        }
    }

    private void startElection() {
        if (!isActive.get()) return;
        System.out.println("[Node " + myId + "] Starting ELECTION.");
        boolean higherNodeResponded = false;

        for (Peer peer : allPeers) {
            if (peer.id > myId) {
                String response = sendMessage(peer.host, peer.port, "ELECTION:" + myId);
                if ("OK".equals(response)) {
                    higherNodeResponded = true;
                }
            }
        }

        if (!higherNodeResponded) {
            System.out.println("[Node " + myId + "] No higher nodes responded. I AM THE COORDINATOR.");
            coordinatorId = myId;
            for (Peer peer : allPeers) {
                if (peer.id < myId) {
                    sendMessage(peer.host, peer.port, "COORDINATOR:" + myId);
                }
            }
        } else {
            System.out.println("[Node " + myId + "] Higher node responded. Waiting for COORDINATOR message.");
        }
    }

    private String sendMessage(String host, int port, String msg) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out.println(msg);
            return in.readLine();
        } catch (Exception e) {
            return "ERROR";
        }
    }

    public static void main(String[] args) {
        int id = Integer.parseInt(System.getenv("NODE_ID"));
        int port = Integer.parseInt(System.getenv("NODE_PORT"));
        String peers = System.getenv("PEERS");
        
        BullyNode node = new BullyNode(id, port, peers);
        node.start();
    }

    static class Peer {
        int id; String host; int port;
        Peer(int id, String host, int port) { this.id = id; this.host = host; this.port = port; }
    }
}

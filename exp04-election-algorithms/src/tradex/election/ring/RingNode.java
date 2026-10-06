package tradex.election.ring;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RingNode {
    private final int myId;
    private final int myPort;
    private final List<Peer> allPeers;
    private int coordinatorId = -1;
    private final AtomicBoolean isActive = new AtomicBoolean(true);

    public RingNode(int id, int port, String peersConfig) {
        this.myId = id;
        this.myPort = port;
        this.allPeers = new ArrayList<>();
        
        if (peersConfig != null && !peersConfig.isEmpty()) {
            for (String p : peersConfig.split(",")) {
                String[] parts = p.split(":");
                allPeers.add(new Peer(Integer.parseInt(parts[0]), parts[1], Integer.parseInt(parts[2])));
            }
        }
        this.allPeers.sort(Comparator.comparingInt(p -> p.id));
    }

    public void start() {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(myPort)) {
                System.out.println("[Ring Node " + myId + "] Started on port " + myPort);
                while (true) {
                    Socket socket = server.accept();
                    new Thread(() -> handleConnection(socket)).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        coordinatorId = 3;
    }

    private void handleConnection(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            String msg = in.readLine();
            if (msg == null || !isActive.get()) return;
            System.out.println("[Node " + myId + "] Received: " + msg);

            if (msg.startsWith("ELECTION")) {
                String listStr = msg.split(":")[1];
                if (listStr.contains("," + myId + ",") || listStr.startsWith(myId + ",")) {
                    // Token wrapped around
                    int max = 0;
                    for (String s : listStr.split(",")) {
                        if (!s.isEmpty()) max = Math.max(max, Integer.parseInt(s));
                    }
                    System.out.println("[Node " + myId + "] Election concluded. Highest ID is " + max + ". Sending COORDINATOR.");
                    passMessage("COORDINATOR:" + max);
                } else {
                    // Append self and pass on
                    String newList = listStr + myId + ",";
                    passMessage("ELECTION:" + newList);
                }
                out.println("ACK");
            } else if (msg.startsWith("COORDINATOR")) {
                int leaderId = Integer.parseInt(msg.split(":")[1]);
                if (leaderId != this.coordinatorId) {
                    this.coordinatorId = leaderId;
                    System.out.println("[Node " + myId + "] New Coordinator is Node " + leaderId);
                    passMessage(msg); // Keep passing until it wraps
                } else {
                    System.out.println("[Node " + myId + "] Coordinator message wrapped around. Stop.");
                }
                out.println("ACK");
            } else if (msg.equals("FAIL")) {
                isActive.set(false);
                System.out.println("[Node " + myId + "] SIMULATING FAILURE.");
                out.println("FAILED");
            } else if (msg.equals("START_ELECTION")) {
                out.println("ACK");
                passMessage("ELECTION:" + myId + ",");
            }
        } catch (Exception e) {}
    }

    private void passMessage(String msg) {
        if (!isActive.get()) return;
        
        int myIndex = -1;
        for (int i = 0; i < allPeers.size(); i++) {
            if (allPeers.get(i).id == myId) myIndex = i;
        }

        // Try passing to next active node in ring
        for (int offset = 1; offset < allPeers.size(); offset++) {
            int nextIndex = (myIndex + offset) % allPeers.size();
            Peer nextPeer = allPeers.get(nextIndex);
            
            String resp = sendMessage(nextPeer.host, nextPeer.port, msg);
            if (!"ERROR".equals(resp)) {
                System.out.println("[Node " + myId + "] Passed message to Node " + nextPeer.id);
                return; // successfully passed
            }
        }
        System.out.println("[Node " + myId + "] Could not pass message; all other nodes seem dead.");
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
        new RingNode(id, port, peers).start();
    }

    static class Peer {
        int id; String host; int port;
        Peer(int id, String host, int port) { this.id = id; this.host = host; this.port = port; }
    }
}

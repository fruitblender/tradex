package tradex.clock;

import java.util.Arrays;
import java.util.List;

public class ClockSyncDemo {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println(" PART A: LAMPORT LOGICAL CLOCKS");
        System.out.println("=================================================");
        demonstrateLamport();

        System.out.println("\n=================================================");
        System.out.println(" PART B: BERKELEY CLOCK SYNCHRONIZATION");
        System.out.println("=================================================");
        demonstrateBerkeley();
    }

    private static void demonstrateLamport() {
        LamportNode client = new LamportNode("Client");
        LamportNode server = new LamportNode("Server");

        // Client performs local action
        client.localEvent("User logs in");

        // Client prepares and sends order
        int msgTime = client.sendEvent("Submitting BUY TCS order");

        // Server does some local prep
        server.localEvent("Initializing order processor");

        // Server receives client order
        server.receiveEvent("Received BUY TCS order", msgTime);

        // Server executes order locally
        server.localEvent("Order Executed");

        // Server sends response
        int respTime = server.sendEvent("Order SUCCESS");

        // Client receives response
        client.receiveEvent("Received Order SUCCESS", respTime);
    }

    private static void demonstrateBerkeley() {
        // Simulated clocks in arbitrary ticks
        BerkeleyNode coordinator = new BerkeleyNode("Coordinator", 1000);
        BerkeleyNode participant1 = new BerkeleyNode("Node-1", 950);   // 50 ticks behind
        BerkeleyNode participant2 = new BerkeleyNode("Node-2", 1060);  // 60 ticks ahead

        List<BerkeleyNode> allNodes = Arrays.asList(coordinator, participant1, participant2);

        System.out.println("--- Initial Clocks ---");
        for (BerkeleyNode node : allNodes) {
            System.out.printf("[%s] Initial Time: %d%n", node.getNodeId(), node.getTime());
        }

        System.out.println("\n--- Coordinator fetching times and calculating ---");
        long coordinatorTime = coordinator.getTime();
        long sumOffsets = 0;

        for (BerkeleyNode node : allNodes) {
            long offset = node.getTime() - coordinatorTime;
            System.out.printf("Coordinator notes %s offset is %d%n", node.getNodeId(), offset);
            sumOffsets += offset;
        }

        long averageOffset = sumOffsets / allNodes.size();
        System.out.printf("Calculated average offset: %d%n", averageOffset);

        System.out.println("\n--- Sending adjustments ---");
        for (BerkeleyNode node : allNodes) {
            // How far the node is from the desired average
            long currentOffset = node.getTime() - coordinatorTime;
            long adjustment = averageOffset - currentOffset;
            node.adjustTime(adjustment);
        }

        System.out.println("\n--- Final Synchronized Clocks ---");
        for (BerkeleyNode node : allNodes) {
            System.out.printf("[%s] Final Time: %d%n", node.getNodeId(), node.getTime());
        }
    }
}

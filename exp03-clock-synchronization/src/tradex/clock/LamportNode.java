package tradex.clock;

public class LamportNode {
    private final String nodeId;
    private int logicalClock;

    public LamportNode(String nodeId) {
        this.nodeId = nodeId;
        this.logicalClock = 0;
    }

    public void localEvent(String eventDescription) {
        logicalClock++;
        System.out.printf("[%s] Local Event: %s | Clock: %d%n", nodeId, eventDescription, logicalClock);
    }

    public int sendEvent(String eventDescription) {
        logicalClock++;
        System.out.printf("[%s] Sending Event: %s | Clock: %d%n", nodeId, eventDescription, logicalClock);
        return logicalClock;
    }

    public void receiveEvent(String eventDescription, int receivedTimestamp) {
        logicalClock = Math.max(logicalClock, receivedTimestamp) + 1;
        System.out.printf("[%s] Received Event: %s (Msg Timestamp: %d) | New Clock: %d%n", 
                          nodeId, eventDescription, receivedTimestamp, logicalClock);
    }
}

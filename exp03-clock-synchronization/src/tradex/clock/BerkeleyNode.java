package tradex.clock;

public class BerkeleyNode {
    private final String nodeId;
    private long simulatedTime; // Representing time in milliseconds or ticks

    public BerkeleyNode(String nodeId, long initialSimulatedTime) {
        this.nodeId = nodeId;
        this.simulatedTime = initialSimulatedTime;
    }

    public String getNodeId() {
        return nodeId;
    }

    public long getTime() {
        return simulatedTime;
    }

    public void adjustTime(long offset) {
        this.simulatedTime += offset;
        System.out.printf("[%s] Adjusted time by %d. New Time: %d%n", nodeId, offset, simulatedTime);
    }
}

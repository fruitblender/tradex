package tradex.mpi;

import mpi.MPI;

public class MpiCollectivesDemo {

    public static void main(String[] args) {
        // Initialize MPI
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        if (size < 2) {
            if (rank == 0) {
                System.out.println("This demonstration requires at least 2 MPI processes.");
            }
            MPI.Finalize();
            return;
        }

        if (rank == 0) {
            System.out.println("==========================================");
            System.out.println(" MPI Collectives: Broadcast, Scatter, Gather");
            System.out.println(" Total Processes (Nodes): " + size);
            System.out.println("==========================================");
        }
        
        MPI.COMM_WORLD.Barrier();

        // ------------------------------------------------
        // 1. BROADCAST (Bcast)
        // ------------------------------------------------
        // Rank 0 decides the stock symbol for analysis and broadcasts it
        String[] symbolBuf = new String[1];
        if (rank == 0) {
            symbolBuf[0] = "TCS";
            System.out.println("\n[BROADCAST] Root (Rank 0) is broadcasting target symbol: " + symbolBuf[0]);
        }
        
        MPI.COMM_WORLD.Bcast(symbolBuf, 0, 1, MPI.OBJECT, 0);
        
        // Let nodes print in order (rudimentary barrier logic for console clarity)
        for (int i = 0; i < size; i++) {
            if (rank == i) {
                System.out.println("  -> Rank " + rank + " received broadcast symbol: " + symbolBuf[0]);
            }
            MPI.COMM_WORLD.Barrier();
        }

        // ------------------------------------------------
        // 2. SCATTER
        // ------------------------------------------------
        // We have historical trade quantities. We scatter chunks to each node.
        int elementsPerNode = 3;
        int totalElements = size * elementsPerNode;
        int[] globalData = null;

        if (rank == 0) {
            globalData = new int[totalElements];
            System.out.print("\n[SCATTER] Root (Rank 0) is scattering trade chunks: [ ");
            for (int i = 0; i < totalElements; i++) {
                globalData[i] = 10 * (i + 1); // 10, 20, 30, ...
                System.out.print(globalData[i] + " ");
            }
            System.out.println("]");
        }

        int[] localData = new int[elementsPerNode];
        
        MPI.COMM_WORLD.Scatter(globalData, 0, elementsPerNode, MPI.INT,
                               localData, 0, elementsPerNode, MPI.INT, 0);

        // ------------------------------------------------
        // LOCAL COMPUTATION
        // ------------------------------------------------
        int localSum = 0;
        for (int val : localData) {
            localSum += val;
        }
        
        for (int i = 0; i < size; i++) {
            if (rank == i) {
                System.out.print("  -> Rank " + rank + " received local chunks: [ ");
                for (int v : localData) System.out.print(v + " ");
                System.out.println("]. Partial Sum = " + localSum);
            }
            MPI.COMM_WORLD.Barrier();
        }

        // ------------------------------------------------
        // 3. GATHER
        // ------------------------------------------------
        // Gather all the partial sums back to Rank 0
        int[] gatheredSums = null;
        if (rank == 0) {
            gatheredSums = new int[size];
            System.out.println("\n[GATHER] Root (Rank 0) is gathering partial sums...");
        }

        MPI.COMM_WORLD.Gather(new int[]{localSum}, 0, 1, MPI.INT,
                              gatheredSums, 0, 1, MPI.INT, 0);

        if (rank == 0) {
            int finalTotalVolume = 0;
            System.out.print("  -> Root (Rank 0) collected partial sums: [ ");
            for (int i = 0; i < size; i++) {
                System.out.print(gatheredSums[i] + " ");
                finalTotalVolume += gatheredSums[i];
            }
            System.out.println("]");
            
            System.out.println("\n*** FINAL RESULT ***");
            System.out.println("Total Trade Volume computed for " + symbolBuf[0] + ": " + finalTotalVolume);
        }

        // Clean up
        MPI.Finalize();
    }
}

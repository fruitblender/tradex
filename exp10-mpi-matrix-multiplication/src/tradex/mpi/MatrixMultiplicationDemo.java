package tradex.mpi;

import mpi.MPI;

public class MatrixMultiplicationDemo {

    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        // Matrix size N x N. Must be divisible by size for simplicity.
        int N = 400; 
        if (args.length > 3) {
            try {
                // mpjrun passes some of its own args, the user args are typically at the end
                N = Integer.parseInt(args[args.length - 1]);
            } catch (NumberFormatException e) {
                // Ignore, keep N = 400
            }
        }

        if (N % size != 0) {
            if (rank == 0) {
                System.out.println("Error: Matrix size N (" + N + ") must be evenly divisible by the number of MPI processes (" + size + ").");
            }
            MPI.Finalize();
            return;
        }

        int[] A = null;
        int[] B = new int[N * N];
        int[] C_parallel = null;
        int[] C_sequential = null;

        long parallelStartTime = 0;
        
        if (rank == 0) {
            System.out.println("==========================================");
            System.out.println(" MPI Parallel Matrix Multiplication");
            System.out.println(" Matrix Size: " + N + " x " + N);
            System.out.println(" Processes: " + size);
            System.out.println("==========================================");

            A = new int[N * N];
            C_parallel = new int[N * N];
            C_sequential = new int[N * N];

            // Initialize matrices
            for (int i = 0; i < N * N; i++) {
                A[i] = (i % 10) + 1;
                B[i] = (i % 5) + 1;
            }
            
            System.out.println("Matrices A and B initialized. Starting parallel computation...");
            parallelStartTime = System.currentTimeMillis();
        }

        // 1. Broadcast matrix B to all processes
        MPI.COMM_WORLD.Bcast(B, 0, N * N, MPI.INT, 0);

        // 2. Scatter rows of matrix A
        int elementsPerNode = (N * N) / size;
        int rowsPerNode = N / size;
        int[] localA = new int[elementsPerNode];

        MPI.COMM_WORLD.Scatter(A, 0, elementsPerNode, MPI.INT,
                               localA, 0, elementsPerNode, MPI.INT, 0);

        // 3. Local Computation: localC = localA x B
        int[] localC = new int[elementsPerNode];
        
        for (int i = 0; i < rowsPerNode; i++) {
            for (int j = 0; j < N; j++) {
                int sum = 0;
                for (int k = 0; k < N; k++) {
                    // localA row i, col k -> localA[i * N + k]
                    // B row k, col j -> B[k * N + j]
                    sum += localA[i * N + k] * B[k * N + j];
                }
                localC[i * N + j] = sum;
            }
        }

        // 4. Gather computed rows of C back to Root
        MPI.COMM_WORLD.Gather(localC, 0, elementsPerNode, MPI.INT,
                              C_parallel, 0, elementsPerNode, MPI.INT, 0);

        if (rank == 0) {
            long parallelEndTime = System.currentTimeMillis();
            long parallelDuration = parallelEndTime - parallelStartTime;
            System.out.println("Parallel computation complete. Time taken: " + parallelDuration + " ms");

            System.out.println("Starting sequential computation for verification...");
            long seqStartTime = System.currentTimeMillis();
            
            // Sequential matrix multiplication
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    int sum = 0;
                    for (int k = 0; k < N; k++) {
                        sum += A[i * N + k] * B[k * N + j];
                    }
                    C_sequential[i * N + j] = sum;
                }
            }
            
            long seqEndTime = System.currentTimeMillis();
            long seqDuration = seqEndTime - seqStartTime;
            System.out.println("Sequential computation complete. Time taken: " + seqDuration + " ms");

            // Verify correctness
            boolean correct = true;
            for (int i = 0; i < N * N; i++) {
                if (C_parallel[i] != C_sequential[i]) {
                    correct = false;
                    break;
                }
            }
            
            if (correct) {
                System.out.println("\nSUCCESS: Parallel result perfectly matches sequential result.");
            } else {
                System.out.println("\nERROR: Parallel result differs from sequential result!");
            }

            // Calculate Speedup
            if (parallelDuration > 0) {
                double speedup = (double) seqDuration / parallelDuration;
                System.out.printf("Speedup (Sequential / Parallel) = %.2fx%n", speedup);
                if (speedup < 1.0) {
                    System.out.println("(Note: Speedup < 1x in Java for short runs is often due to JVM JIT compilation overhead, threading overhead, and MPJ Express initialization delays vs the actual math computation time.)");
                }
            }
        }

        MPI.Finalize();
    }
}

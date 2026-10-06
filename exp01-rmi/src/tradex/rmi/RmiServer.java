package tradex.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RmiServer {
    public static void main(String[] args) {
        try {
            // Need to set java.rmi.server.hostname for Docker environments
            String hostname = System.getenv("RMI_HOSTNAME");
            if (hostname == null) {
                hostname = "127.0.0.1";
            }
            System.setProperty("java.rmi.server.hostname", hostname);

            TradingService tradingService = new TradingServiceImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("TradingService", tradingService);
            System.out.println("TradeX RMI Server is running on " + hostname + ":1099...");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package tradex.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import tradex.model.Order;

public class RmiClient {
    public static void main(String[] args) {
        try {
            String serverHost = System.getenv("RMI_SERVER_HOST");
            if (serverHost == null) {
                serverHost = "127.0.0.1";
            }
            
            System.out.println("Connecting to TradeX RMI Server at " + serverHost + ":1099...");
            Registry registry = LocateRegistry.getRegistry(serverHost, 1099);
            TradingService service = (TradingService) registry.lookup("TradingService");

            // Test 1: Get Stock Price
            System.out.println("\n--- Test 1: Querying Stock Price ---");
            double price = service.getStockPrice("TCS");
            System.out.println("Current TCS Price: " + price);

            // Test 2: Place an Order
            System.out.println("\n--- Test 2: Placing an Order ---");
            Order myOrder = service.placeOrder("TRADER_01", "TCS", Order.OrderType.BUY, 10, 3405.0);
            System.out.println("Order Placed successfully: " + myOrder);

            // Test 3: Check Order Status
            System.out.println("\n--- Test 3: Checking Order Status ---");
            Order fetchedOrder = service.getOrderStatus(myOrder.getOrderId());
            System.out.println("Fetched Order Status: " + fetchedOrder.getStatus());
            
        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

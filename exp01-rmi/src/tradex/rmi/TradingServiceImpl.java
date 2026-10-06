package tradex.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import tradex.model.Order;
import tradex.model.Stock;

public class TradingServiceImpl extends UnicastRemoteObject implements TradingService {
    
    private Map<String, Stock> market;
    private Map<String, Order> orderStore;

    protected TradingServiceImpl() throws RemoteException {
        super();
        market = new HashMap<>();
        orderStore = new HashMap<>();
        
        // Populate some sample stocks
        market.put("TCS", new Stock("TCS", "Tata Consultancy Services", 3400.0));
        market.put("INFY", new Stock("INFY", "Infosys", 1500.0));
        market.put("RELIANCE", new Stock("RELIANCE", "Reliance Industries", 2900.0));
    }

    @Override
    public double getStockPrice(String symbol) throws RemoteException {
        Stock stock = market.get(symbol);
        if (stock != null) {
            System.out.println("[SERVER] Price requested for " + symbol + ": " + stock.getCurrentPrice());
            return stock.getCurrentPrice();
        }
        System.out.println("[SERVER] Price requested for unknown stock " + symbol);
        throw new RemoteException("Stock symbol not found: " + symbol);
    }

    @Override
    public Order placeOrder(String traderId, String symbol, Order.OrderType type, int quantity, double price) throws RemoteException {
        String orderId = UUID.randomUUID().toString().substring(0, 8);
        Order order = new Order(orderId, traderId, symbol, type, quantity, price);
        
        System.out.println("[SERVER] Received order: " + order);
        
        // Simulate some validation and basic execution
        if (!market.containsKey(symbol)) {
            order.setStatus(Order.OrderStatus.FAILED);
        } else {
            order.setStatus(Order.OrderStatus.EXECUTED);
        }
        
        orderStore.put(orderId, order);
        System.out.println("[SERVER] Order executed/saved: " + orderId);
        
        return order;
    }

    @Override
    public Order getOrderStatus(String orderId) throws RemoteException {
        System.out.println("[SERVER] Status requested for order: " + orderId);
        return orderStore.get(orderId);
    }
}

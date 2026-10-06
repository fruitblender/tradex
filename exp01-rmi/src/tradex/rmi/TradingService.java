package tradex.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import tradex.model.Order;

public interface TradingService extends Remote {
    double getStockPrice(String symbol) throws RemoteException;
    Order placeOrder(String traderId, String symbol, Order.OrderType type, int quantity, double price) throws RemoteException;
    Order getOrderStatus(String orderId) throws RemoteException;
}

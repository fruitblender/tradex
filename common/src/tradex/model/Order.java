package tradex.model;

import java.io.Serializable;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum OrderType { BUY, SELL }
    public enum OrderStatus { PENDING, EXECUTED, FAILED }

    private String orderId;
    private String traderId;
    private String symbol;
    private OrderType type;
    private int quantity;
    private double price;
    private OrderStatus status;

    public Order(String orderId, String traderId, String symbol, OrderType type, int quantity, double price) {
        this.orderId = orderId;
        this.traderId = traderId;
        this.symbol = symbol;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.status = OrderStatus.PENDING;
    }

    public String getOrderId() { return orderId; }
    public String getTraderId() { return traderId; }
    public String getSymbol() { return symbol; }
    public OrderType getType() { return type; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public OrderStatus getStatus() { return status; }
    
    public void setStatus(OrderStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Order{" + "orderId='" + orderId + '\'' + ", type=" + type + ", symbol='" + symbol + '\'' + ", quantity=" + quantity + ", price=" + price + ", status=" + status + '}';
    }
}

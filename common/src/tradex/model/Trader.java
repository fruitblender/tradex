package tradex.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Trader implements Serializable {
    private static final long serialVersionUID = 1L;

    private String traderId;
    private double balance;
    private Map<String, Integer> holdings; // Symbol -> Quantity

    public Trader(String traderId, double initialBalance) {
        this.traderId = traderId;
        this.balance = initialBalance;
        this.holdings = new HashMap<>();
    }

    public String getTraderId() { return traderId; }
    
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public Map<String, Integer> getHoldings() { return holdings; }
    
    public void addHolding(String symbol, int quantity) {
        holdings.put(symbol, holdings.getOrDefault(symbol, 0) + quantity);
    }
    
    public void removeHolding(String symbol, int quantity) {
        int current = holdings.getOrDefault(symbol, 0);
        if (current >= quantity) {
            holdings.put(symbol, current - quantity);
        }
    }

    @Override
    public String toString() {
        return "Trader{" + "traderId='" + traderId + '\'' + ", balance=" + balance + ", holdings=" + holdings + '}';
    }
}

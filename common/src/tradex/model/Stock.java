package tradex.model;

import java.io.Serializable;

public class Stock implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String symbol;
    private String name;
    private double currentPrice;

    public Stock(String symbol, String name, double currentPrice) {
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = currentPrice;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    @Override
    public String toString() {
        return "Stock{" + "symbol='" + symbol + '\'' + ", name='" + name + '\'' + ", currentPrice=" + currentPrice + '}';
    }
}

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TradingPlatform {
    private final Map<String, Stock> stocks; 
    private final Map<String, User> users;

    public TradingPlatform() {
        this.stocks = new HashMap<>();
        this.users = new HashMap<>();
    }

    // --- Platform Management ---

    public void addStock(Stock stock) {
        this.stocks.put(stock.getSymbol(), stock);
    }

    public void addUser(User user) {
        this.users.put(user.getUserId(), user);
    }

    // --- Market Data Display ---

    public void displayMarketData() {
        System.out.println("\n--- Current Market Data ---");
        for (Stock stock : stocks.values()) {
            System.out.printf("  %s (%s): $%.2f\n", stock.getSymbol(), stock.getName(), stock.getCurrentPrice());
        }
        System.out.println("---------------------------\n");
    }

    // --- Buy/Sell Operations Handler ---

    public void executeTrade(String userId, String symbol, String type, int quantity) {
        User user = users.get(userId);
        Stock stock = stocks.get(symbol);

        if (user == null || stock == null) {
            System.out.println("Error: User or Stock not found.");
            return;
        }

        if ("BUY".equalsIgnoreCase(type)) {
            user.buyStock(stock, quantity);
        } else if ("SELL".equalsIgnoreCase(type)) {
            user.sellStock(stock, quantity);
        } else {
            System.out.println("Error: Invalid trade type. Use 'BUY' or 'SELL'.");
        }
    }

    // =========================================================================
    // NESTED CLASS: Stock
    // =========================================================================
    // Represents a tradable asset.
    static class Stock {
        private final String symbol;
        private final String name;
        private double currentPrice;
        private final List<Double> priceHistory; 

        public Stock(String symbol, String name, double initialPrice) {
            this.symbol = symbol;
            this.name = name;
            this.currentPrice = initialPrice;
            this.priceHistory = new ArrayList<>();
            this.priceHistory.add(initialPrice);
        }

        public void updatePrice(double newPrice) {
            this.currentPrice = newPrice;
            this.priceHistory.add(newPrice);
            System.out.println("Market Update: " + this.symbol + " is now $" + newPrice);
        }

        public String getSymbol() { return symbol; }
        public String getName() { return name; }
        public double getCurrentPrice() { return currentPrice; }
    }

    // =========================================================================
    // NESTED CLASS: Transaction
    // =========================================================================
    // Records a single buy or sell action.
    static class Transaction {
        private final String userId;
        private final String stockSymbol;
        private final String type; 
        private final int quantity;
        private final double price;
        private final LocalDateTime timestamp;

        public Transaction(String userId, String stockSymbol, String type, int quantity, double price) {
            this.userId = userId;
            this.stockSymbol = stockSymbol;
            this.type = type;
            this.quantity = quantity;
            this.price = price;
            this.timestamp = LocalDateTime.now();
        }

        public double getTransactionValue() {
            return quantity * price;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s %d shares of %s at $%.2f. Total: $%.2f", 
                                 timestamp.toLocalDate(), type, quantity, stockSymbol, price, getTransactionValue());
        }
    }

    // =========================================================================
    // NESTED CLASS: User
    // =========================================================================
    // Manages user cash, holdings (portfolio), and trading logic.
    static class User {
        private final String userId;
        private double cashBalance;
        // Map: Stock Symbol -> Quantity Held
        private final Map<String, Integer> portfolio; 
        private final List<Transaction> transactionHistory;

        public User(String userId, double initialDeposit) {
            this.userId = userId;
            this.cashBalance = initialDeposit;
            this.portfolio = new HashMap<>();
            this.transactionHistory = new ArrayList<>();
        }

        // --- Buy Operation ---
        public boolean buyStock(Stock stock, int quantity) {
            double cost = stock.getCurrentPrice() * quantity;
            if (cashBalance < cost) {
                System.out.println("❌ " + userId + ": Insufficient cash to buy " + quantity + " shares of " + stock.getSymbol());
                return false;
            }

            cashBalance -= cost;
            portfolio.put(stock.getSymbol(), portfolio.getOrDefault(stock.getSymbol(), 0) + quantity);
            Transaction t = new Transaction(userId, stock.getSymbol(), "BUY", quantity, stock.getCurrentPrice());
            transactionHistory.add(t);
            System.out.printf("✅ %s bought %d shares of %s for $%.2f. Cash left: $%.2f\n", userId, quantity, stock.getSymbol(), cost, cashBalance);
            return true;
        }

        // --- Sell Operation ---
        public boolean sellStock(Stock stock, int quantity) {
            String symbol = stock.getSymbol();
            int sharesHeld = portfolio.getOrDefault(symbol, 0);

            if (sharesHeld < quantity) {
                System.out.println("❌ " + userId + ": Insufficient shares of " + symbol + " to sell " + quantity);
                return false;
            }

            double revenue = stock.getCurrentPrice() * quantity;
            
            cashBalance += revenue;
            int newQuantity = sharesHeld - quantity;
            if (newQuantity == 0) {
                portfolio.remove(symbol);
            } else {
                portfolio.put(symbol, newQuantity);
            }
            Transaction t = new Transaction(userId, symbol, "SELL", quantity, stock.getCurrentPrice());
            transactionHistory.add(t);
            System.out.printf("✅ %s sold %d shares of %s for $%.2f. New cash: $%.2f\n", userId, quantity, symbol, revenue, cashBalance);
            return true;
        }

        // --- Portfolio Performance Tracking ---

        // Note: For this method to work inside the User class, it needs to be passed the map of all market stocks.
        public double getPortfolioValue(Map<String, Stock> marketStocks) {
            double stockValue = 0.0;
            for (Map.Entry<String, Integer> entry : portfolio.entrySet()) {
                String symbol = entry.getKey();
                int quantity = entry.getValue();
                Stock stock = marketStocks.get(symbol);
                if (stock != null) {
                    stockValue += quantity * stock.getCurrentPrice();
                }
            }
            return stockValue + cashBalance;
        }

        public void displayPerformance(Map<String, Stock> marketStocks) {
            System.out.println("\n--- Portfolio Performance for " + userId + " ---");
            System.out.printf("Cash Balance: $%.2f\n", cashBalance);

            if (portfolio.isEmpty()) {
                System.out.println("Portfolio is empty.");
                return;
            }

            System.out.println("Holdings:");
            double totalStockValue = 0.0;
            for (Map.Entry<String, Integer> entry : portfolio.entrySet()) {
                String symbol = entry.getKey();
                int quantity = entry.getValue();
                Stock stock = marketStocks.get(symbol);
                if (stock != null) {
                    double currentValue = quantity * stock.getCurrentPrice();
                    totalStockValue += currentValue;
                    System.out.printf("  %s: %d shares (Current Price: $%.2f, Value: $%.2f)\n", 
                                      symbol, quantity, stock.getCurrentPrice(), currentValue);
                }
            }
            System.out.printf("Total Stock Market Value: $%.2f\n", totalStockValue);
            System.out.printf("Total Portfolio Value (Cash + Stock): $%.2f\n", getPortfolioValue(marketStocks));
            System.out.println("-------------------------------------------");
        }
        
        public void displayTransactionHistory() {
            System.out.println("\n--- Transaction History for " + userId + " ---");
            if (transactionHistory.isEmpty()) {
                System.out.println("No transactions recorded.");
                return;
            }
            for (Transaction t : transactionHistory) {
                System.out.println(t);
            }
            System.out.println("-------------------------------------------");
        }

        public String getUserId() { return userId; }
        public double getCashBalance() { return cashBalance; }
    }

    // =========================================================================
    // MAIN METHOD (Execution)
    // =========================================================================
    public static void main(String[] args) {
        TradingPlatform platform = new TradingPlatform();

        // 1. Setup Stocks (Market Data)
        Stock apple = new Stock("AAPL", "Apple Inc.", 150.00);
        Stock google = new Stock("GOOGL", "Alphabet Inc.", 2500.00);
        platform.addStock(apple);
        platform.addStock(google);

        // 2. Setup Users
        User alice = new User("Alice", 10000.00);
        platform.addUser(alice);
        platform.addUser(new User("Bob", 5000.00));

        // Display initial market data
        platform.displayMarketData();

        // 3. Alice Trades (Buy/Sell Operations)
        platform.executeTrade("Alice", "AAPL", "BUY", 10);
        platform.executeTrade("Alice", "GOOGL", "BUY", 2);

        // 4. Simulate Market Change
        System.out.println("\n--- Market Volatility Simulation ---");
        apple.updatePrice(155.50); // Apple price goes up
        google.updatePrice(2450.00); // Google price goes down
        platform.displayMarketData();

        // 5. Alice Sells (Buy/Sell Operations)
        platform.executeTrade("Alice", "AAPL", "SELL", 5);

        // 6. Track Portfolio Performance
        alice.displayPerformance(platform.stocks);

        // 7. Track Transactions
        alice.displayTransactionHistory();
    }
}
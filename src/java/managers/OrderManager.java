package managers;

import db.DBUtil;
import models.CartItem;
import models.Order;
import models.OrderDetail;
import models.Product;
import core.IdGenerator;
import exceptions.NoQuantityLeftException; // Ensure this custom exception exists

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderManager {

    private ProductManager productManager;

    public OrderManager() throws SQLException {
        // Initialize ProductManager for stock updates and fetching product details
        try {
            this.productManager = new ProductManager();
        } catch (SQLException e) {
            System.err.println("OrderManager: Failed to initialize ProductManager dependency: " + e.getMessage());
            throw e; // Re-throw to indicate critical failure during construction
        }
    }

    /**
     * Places an order for a user with items from their cart.
     * This method handles the transaction.
     *
     * @param userId          The ID of the user placing the order.
     * @param cartItems       The list of CartItem objects to be ordered.
     * @param totalAmount     The total amount for the order.
     * @param shippingAddress The shipping address for the order.
     * @return The ID of the newly created order.
     * @throws SQLException             if a database error occurs.
     * @throws NoQuantityLeftException if there is insufficient stock for any item.
     */
    public String placeOrder(String userId, List<CartItem> cartItems, double totalAmount, String shippingAddress)
            throws SQLException, NoQuantityLeftException { // Added NoQuantityLeftException

        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty for placing an order.");
        }
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart items cannot be empty for placing an order.");
        }
        if (totalAmount < 0) {
            throw new IllegalArgumentException("Total amount cannot be negative for an order.");
        }
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            // Depending on requirements, you might allow this or make it mandatory
            System.err.println("OrderManager: Warning - Shipping address is empty for order by UserID: " + userId);
            // For now, we'll proceed, but in a real app, this might be an error or require a default.
        }


        Connection conn = null;
        String orderId = null; // Will be generated

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction

            System.out.println("OrderManager: Placing order for UserID: " + userId +
                               " | Items: " + cartItems.size() +
                               " | Total: " + String.format("%.2f", totalAmount));

            // 1. Validate stock availability BEFORE creating order header or any records
            System.out.println("OrderManager: Validating stock availability...");
            for (CartItem item : cartItems) {
                Product productInDb = productManager.getProductById(item.getProductId());
                if (productInDb == null) {
                    // This should ideally not happen if cart items are based on existing products
                    throw new SQLException("Critical error: Product with ID " + item.getProductId() +
                                           " (from cart) not found in database during order placement.");
                }
                if (productInDb.getStock() < item.getQuantity()) {
                    throw new NoQuantityLeftException("Insufficient stock for product: " + productInDb.getName() +
                                                      ". Requested: " + item.getQuantity() +
                                                      ", Available: " + productInDb.getStock());
                }
            }
            System.out.println("OrderManager: Stock validation successful.");


            // 2. Create Order Header
            orderId = IdGenerator.generateOrderId();
            String insertOrderSql = "INSERT INTO Orders (OrderID, UserID, OrderDate, TotalAmount, Status, ShippingAddress) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmtOrder = conn.prepareStatement(insertOrderSql)) {
                pstmtOrder.setString(1, orderId);
                pstmtOrder.setString(2, userId);
                pstmtOrder.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pstmtOrder.setDouble(4, totalAmount);
                pstmtOrder.setString(5, "PENDING"); // Default order status
                pstmtOrder.setString(6, shippingAddress);
                pstmtOrder.executeUpdate();
                System.out.println("OrderManager: Order header created (ID: " + orderId + ")");
            }

            // 3. Create Order Details and Update Product Stock (Batch operation)
            String insertOrderDetailSql = "INSERT INTO OrderDetails (OrderDetailID, OrderID, ProductID, ProductNameSnapshot, Quantity, PriceAtPurchase) VALUES (?, ?, ?, ?, ?, ?)";
            String updateStockSql = "UPDATE Products SET Stock = Stock - ? WHERE ProductID = ? AND Stock >= ?"; // Ensure stock doesn't go negative

            System.out.println("OrderManager: Preparing batch for order details and stock updates...");
            try (PreparedStatement pstmtDetail = conn.prepareStatement(insertOrderDetailSql);
                 PreparedStatement pstmtStock = conn.prepareStatement(updateStockSql)) {

                for (CartItem item : cartItems) {
                    // Add to OrderDetails batch
                    pstmtDetail.setString(1, IdGenerator.generateOrderDetailId());
                    pstmtDetail.setString(2, orderId);
                    pstmtDetail.setString(3, item.getProductId());
                    pstmtDetail.setString(4, item.getProductName()); // Name from CartItem (snapshot at time of cart)
                    pstmtDetail.setInt(5, item.getQuantity());
                    pstmtDetail.setDouble(6, item.getProduct().getPrice()); // Price from Product object within CartItem
                    pstmtDetail.addBatch();

                    // Add to Stock Update batch
                    pstmtStock.setInt(1, item.getQuantity());
                    pstmtStock.setString(2, item.getProductId());
                    pstmtStock.setInt(3, item.getQuantity()); // Condition for atomic update (Stock >= requested_quantity)
                    pstmtStock.addBatch();
                }
                System.out.println("OrderManager: Executing order details batch...");
                pstmtDetail.executeBatch(); // Execute all order detail inserts

                System.out.println("OrderManager: Executing stock update batch...");
                int[] stockUpdateCounts = pstmtStock.executeBatch(); // Execute all stock updates

                // Verify stock updates (important for consistency)
                for (int i = 0; i < stockUpdateCounts.length; i++) {
                    if (stockUpdateCounts[i] == 0 || stockUpdateCounts[i] == Statement.SUCCESS_NO_INFO) {
                        // Check if it's SUCCESS_NO_INFO which can mean success for some drivers when count is not available.
                        // More robustly, re-fetch stock or rely on the WHERE Stock >= ? clause.
                        // If count is truly 0, it means the update for that product failed (e.g., stock became insufficient).
                        Product failedProduct = cartItems.get(i).getProduct();
                        System.err.println("OrderManager: Stock update failed for product " + failedProduct.getName() +
                                           " (ID: " + failedProduct.getProductId() + "). Possible race condition or unexpected issue.");
                        throw new SQLException("Failed to update stock for product: " + failedProduct.getName() +
                                               ". Order has been rolled back.");
                    }
                }
            }

            conn.commit(); // Commit transaction if all operations are successful
            System.out.println("OrderManager: Order placed successfully (ID: " + orderId + "). Transaction committed.");

        } catch (SQLException | NoQuantityLeftException e) { // Catch SQL or our custom NoQuantityLeftException
            System.err.println("OrderManager: Error occurred during order placement, attempting to rollback. OrderID (if generated): " + orderId);
            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("OrderManager: Transaction rolled back successfully.");
                } catch (SQLException exRollback) {
                    System.err.println("OrderManager: CRITICAL Error during transaction rollback: " + exRollback.getMessage());
                    exRollback.printStackTrace(); // Log rollback error
                }
            }
            // Log the original error before re-throwing
            System.err.println("OrderManager: Root cause of order placement failure: " + e.getMessage());
            if (!(e instanceof NoQuantityLeftException)) { // Avoid printStackTrace if it's our expected business exception
                 e.printStackTrace();
            }
            throw e; // Re-throw the original exception to be caught and handled by the servlet
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restore default auto-commit behavior
                    conn.close();
                } catch (SQLException exClose) {
                    System.err.println("OrderManager: Error closing connection: " + exClose.getMessage());
                    // This is less critical than the main operation failure but should be logged.
                }
            }
        }
        return orderId; // Return the generated order ID
    }


    public List<Order> getOrdersByUserId(String userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.FullName as CustomerName " +
                     "FROM Orders o " +
                     "LEFT JOIN Users u ON o.UserID = u.UserID " +
                     "WHERE o.UserID = ? ORDER BY o.OrderDate DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order(
                            rs.getString("OrderID"),
                            rs.getString("UserID"),
                            rs.getString("CustomerName"),
                            rs.getTimestamp("OrderDate").toLocalDateTime(),
                            rs.getDouble("TotalAmount")
                    );
                    // Lazy load details, or load them here if always needed
                    // order.setOrderDetails(getOrderDetailsForOrder(order.getOrderId()));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    public Order getOrderById(String orderId) throws SQLException {
        Order order = null;
        String sql = "SELECT o.*, u.FullName as CustomerName " +
                     "FROM Orders o " +
                     "LEFT JOIN Users u ON o.UserID = u.UserID " +
                     "WHERE o.OrderID = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    order = new Order(
                            rs.getString("OrderID"),
                            rs.getString("UserID"),
                            rs.getString("CustomerName"),
                            rs.getTimestamp("OrderDate").toLocalDateTime(),
                            rs.getDouble("TotalAmount")
                    );
                    order.setOrderDetails(getOrderDetailsForOrder(orderId)); // Eagerly load details
                }
            }
        }
        return order;
    }

    public List<OrderDetail> getOrderDetailsForOrder(String orderId) throws SQLException {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT * FROM OrderDetails WHERE OrderID = ?";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    details.add(new OrderDetail(
                            rs.getString("OrderDetailID"),
                            rs.getString("OrderID"),
                            rs.getString("ProductID"),
                            rs.getString("ProductNameSnapshot"),
                            rs.getInt("Quantity"),
                            rs.getDouble("PriceAtPurchase")
                    ));
                }
            }
        }
        return details;
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> allOrders = new ArrayList<>();
        String sql = "SELECT o.*, u.FullName as CustomerName " +
                     "FROM Orders o " +
                     "LEFT JOIN Users u ON o.UserID = u.UserID " +
                     "ORDER BY o.OrderDate DESC";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Order order = new Order(
                        rs.getString("OrderID"),
                        rs.getString("UserID"),
                        rs.getString("CustomerName"),
                        rs.getTimestamp("OrderDate").toLocalDateTime(),
                        rs.getDouble("TotalAmount")
                );
                // For admin view, consider if details should be loaded by default or on request
                // order.setOrderDetails(getOrderDetailsForOrder(order.getOrderId()));
                allOrders.add(order);
            }
        }
        return allOrders;
    }

    // Potential method to update order status (for admin use)
    public boolean updateOrderStatus(String orderId, String newStatus) throws SQLException {
        if (orderId == null || orderId.trim().isEmpty() || newStatus == null || newStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID and new status cannot be null or empty.");
        }
        // Optional: Validate newStatus against a list of allowed statuses
        // e.g., PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED

        String sql = "UPDATE Orders SET Status = ? WHERE OrderID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, orderId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("OrderManager: Status for OrderID " + orderId + " updated to " + newStatus);
            }
            return rowsAffected > 0;
        }
    }
}
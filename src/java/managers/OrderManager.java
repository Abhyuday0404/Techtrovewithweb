package managers;

import models.Order;
import models.OrderDetail;
import models.CartItem;
import models.Product;
import models.User;
import db.DBUtil;
import core.IdGenerator;
import exceptions.NoQuantityLeftException;
import exceptions.InvalidProductIdException; // Though less likely here if cart items are valid

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderManager {

    // No connection field here; get connection per method or pass it in for transactions.
    private CartManager cartManager;
    private ProductManager productManager;

    public OrderManager() throws SQLException {
        // Initialize dependent managers
        try {
            this.cartManager = new CartManager(); // CartManager itself might throw SQLException
            this.productManager = new ProductManager();
        } catch (SQLException e) {
            System.err.println("OrderManager FATAL: Failed to initialize dependency managers: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Places an order for the given user with the items from their cart.
     * This method handles its own transaction.
     *
     * @param userId      The ID of the user placing the order.
     * @param cartItems   The list of CartItem objects to be ordered.
     * @param totalAmount The pre-calculated total amount for the order.
     * @return The generated OrderID if successful.
     * @throws SQLException             If a database error occurs.
     * @throws NoQuantityLeftException  If stock is insufficient for any item.
     * @throws IllegalArgumentException If input parameters are invalid.
     */
    public String placeOrder(String userId, List<CartItem> cartItems, double totalAmount)
            throws SQLException, NoQuantityLeftException, IllegalArgumentException {

        if (userId == null || userId.trim().isEmpty()) throw new IllegalArgumentException("User ID is required.");
        if (cartItems == null || cartItems.isEmpty()) throw new IllegalArgumentException("Cart cannot be empty to place an order.");
        if (totalAmount < 0) throw new IllegalArgumentException("Total amount cannot be negative.");

        System.out.printf("OrderManager: Placing order for UserID: %s | Items: %d | Total: %.2f%n", userId, cartItems.size(), totalAmount);

        Connection conn = null;
        String orderId = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // START TRANSACTION

            // 1. Validate stock availability (critical check within transaction)
            System.out.println("  OrderManager: Validating stock availability...");
            validateStockAvailabilityWithinTransaction(conn, cartItems);
            System.out.println("  OrderManager: Stock validation successful.");

            // 2. Create Order Header
            orderId = IdGenerator.generateOrderId();
            String insertOrderSQL = "INSERT INTO Orders (OrderID, UserID, OrderDate, TotalAmount) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmtOrder = conn.prepareStatement(insertOrderSQL)) {
                pstmtOrder.setString(1, orderId);
                pstmtOrder.setString(2, userId);
                pstmtOrder.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                pstmtOrder.setDouble(4, totalAmount);
                pstmtOrder.executeUpdate();
                System.out.println("  OrderManager: Order header created (ID: " + orderId + ")");
            }

            // 3. Create Order Details and Update Product Stock (Batch Operations)
            String insertDetailSQL = "INSERT INTO OrderDetails (OrderDetailID, OrderID, ProductID, Quantity, Price) VALUES (?, ?, ?, ?, ?)";
            String updateStockSQL = "UPDATE Products SET Stock = Stock - ? WHERE ProductID = ? AND Stock >= ?"; // Crucial: AND Stock >= ?

            try (PreparedStatement pstmtDetail = conn.prepareStatement(insertDetailSQL);
                 PreparedStatement pstmtUpdateStock = conn.prepareStatement(updateStockSQL)) {

                System.out.println("  OrderManager: Preparing batch for order details and stock updates...");
                for (CartItem item : cartItems) {
                    Product product = item.getProduct();
                    if (product == null) {
                        throw new SQLException("Invalid cart item encountered: product is null for CartID " + item.getCartId());
                    }
                    String productId = product.getProductId();
                    int quantityOrdered = item.getQuantity();
                    double priceAtOrder = product.getPrice(); // Price at the time of order

                    // Add to OrderDetails batch
                    pstmtDetail.setString(1, IdGenerator.generateOrderDetailId());
                    pstmtDetail.setString(2, orderId);
                    pstmtDetail.setString(3, productId);
                    pstmtDetail.setInt(4, quantityOrdered);
                    pstmtDetail.setDouble(5, priceAtOrder);
                    pstmtDetail.addBatch();

                    // Add to Stock Update batch
                    pstmtUpdateStock.setInt(1, quantityOrdered);
                    pstmtUpdateStock.setString(2, productId);
                    pstmtUpdateStock.setInt(3, quantityOrdered); // Check if stock is still sufficient
                    pstmtUpdateStock.addBatch();
                }

                System.out.println("  OrderManager: Executing order details batch...");
                pstmtDetail.executeBatch();
                System.out.println("  OrderManager: Executing stock updates batch...");
                int[] stockUpdateResults = pstmtUpdateStock.executeBatch();

                // Verify all stock updates were successful
                for (int i = 0; i < stockUpdateResults.length; i++) {
                    if (stockUpdateResults[i] == 0 || stockUpdateResults[i] == Statement.EXECUTE_FAILED) {
                        // This means stock ran out for this item between validation and update, or was already too low
                        CartItem failedItem = cartItems.get(i);
                        String errorMsg = String.format(
                            "Stock level changed critically for '%s' (ID: %s) during order processing. Order cancelled.",
                            failedItem.getProductName(), failedItem.getProductId()
                        );
                        System.err.println("  OrderManager: " + errorMsg);
                        conn.rollback(); // Rollback immediately
                        throw new NoQuantityLeftException(errorMsg + " Please review your cart and try again.");
                    }
                }
                System.out.println("  OrderManager: Stock updates batch verified successfully.");
            }

            // 4. Clear the user's cart (using the CartManager method that expects an external transaction)
            //    This version of clearCart in CartManager should NOT manage its own transaction or close the connection.
            System.out.println("  OrderManager: Clearing cart for user " + userId);
            cartManager.clearCart(userId); // Assumes this method is refactored to not commit/rollback/close
            System.out.println("  OrderManager: Cart cleared.");

            // If everything is successful, commit the transaction
            conn.commit();
            System.out.println("OrderManager: Order " + orderId + " placed and committed successfully.");
            return orderId;

        } catch (SQLException | NoQuantityLeftException | IllegalArgumentException e) {
            if (conn != null) {
                try {
                    System.err.println("OrderManager: Error occurred, attempting to rollback transaction for OrderID (if generated): " + orderId);
                    conn.rollback();
                } catch (SQLException exRb) {
                    System.err.println("OrderManager FATAL: Rollback failed: " + exRb.getMessage());
                    // Log or throw a more critical exception if rollback itself fails
                }
            }
            System.err.println("OrderManager: Error placing order: " + e.getMessage());
            throw e; // Re-throw the original exception
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restore default auto-commit behavior
                    conn.close();             // Close the connection
                } catch (SQLException exClose) {
                    System.err.println("OrderManager: Error closing connection: " + exClose.getMessage());
                }
            }
        }
    }

    private void validateStockAvailabilityWithinTransaction(Connection conn, List<CartItem> cartItems)
            throws SQLException, NoQuantityLeftException {
        // This check is crucial and must use the provided transactional connection
        String checkStockSQL = "SELECT Name, Stock FROM Products WHERE ProductID = ? FOR UPDATE"; // FOR UPDATE locks rows
        try (PreparedStatement pstmtCheck = conn.prepareStatement(checkStockSQL)) {
            for (CartItem item : cartItems) {
                Product product = item.getProduct();
                 if (product == null) throw new SQLException("Invalid cart item during stock validation: product is null.");
                String productId = product.getProductId();
                int requestedQuantity = item.getQuantity();

                pstmtCheck.setString(1, productId);
                try (ResultSet rs = pstmtCheck.executeQuery()) {
                    if (rs.next()) {
                        int availableStock = rs.getInt("Stock");
                        String productName = rs.getString("Name");
                        if (availableStock < requestedQuantity) {
                            System.err.println(String.format("  Stock FAIL (within transaction) for %s (ID: %s). Requested: %d, Available: %d",
                                productName, productId, requestedQuantity, availableStock));
                            throw new NoQuantityLeftException(String.format(
                                "Insufficient stock for '%s'. Only %d available, you requested %d.",
                                productName, availableStock, requestedQuantity
                            ));
                        }
                    } else {
                        // Product disappeared from DB after being added to cart - very unlikely if FKs are good
                        throw new SQLException("Product with ID '" + productId + "' (named '" + item.getProductName() +
                                               "') no longer available. Please remove it from your cart.");
                    }
                }
            }
        }
    }


    public Order getOrderById(String orderId) throws SQLException {
        Order order = null;
        // Join with Users table to get customer name
        String sql = "SELECT o.*, u.FullName " +
                     "FROM Orders o LEFT JOIN Users u ON o.UserID = u.UserID " +
                     "WHERE o.OrderID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String customerName = rs.getString("FullName");
                    // If user was deleted and UserID in Orders is null, or if join fails
                    if (rs.getString("UserID") == null && customerName == null) customerName = "(User Deleted/Unknown)";
                    else if (customerName == null) customerName = "(Name N/A)";


                    order = new Order(
                        rs.getString("OrderID"),
                        rs.getString("UserID"),
                        customerName,
                        rs.getTimestamp("OrderDate").toLocalDateTime(),
                        rs.getDouble("TotalAmount")
                    );
                    // Fetch and set order details
                    order.setOrderDetails(getOrderDetailsForOrder(conn, orderId)); // Pass connection for transaction
                }
            }
        }
        return order;
    }

    // Fetches order details for a given order, using an existing connection (part of a larger op)
    private List<OrderDetail> getOrderDetailsForOrder(Connection conn, String orderId) throws SQLException {
        List<OrderDetail> details = new ArrayList<>();
        // Join with Products to get product name at the time of fetching details
        String sql = "SELECT od.*, p.Name AS ProductName " +
                     "FROM OrderDetails od LEFT JOIN Products p ON od.ProductID = p.ProductID " +
                     "WHERE od.OrderID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String productName = rs.getString("ProductName");
                    if (rs.getString("ProductID") == null && productName == null) productName = "(Product Deleted/Unknown)";
                    else if (productName == null) productName = "(Product Name N/A)";

                    details.add(new OrderDetail(
                        rs.getString("OrderDetailID"),
                        rs.getString("OrderID"),
                        rs.getString("ProductID"),
                        productName,
                        rs.getInt("Quantity"),
                        rs.getDouble("Price") // Price stored at time of order
                    ));
                }
            }
        }
        return details;
    }
    
    public List<Order> getOrdersByUserId(String userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.FullName " +
                     "FROM Orders o LEFT JOIN Users u ON o.UserID = u.UserID " +
                     "WHERE o.UserID = ? ORDER BY o.OrderDate DESC";
        try (Connection conn = DBUtil.getConnection(); // New connection for this self-contained operation
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                     String customerName = rs.getString("FullName");
                     if (customerName == null) customerName = "(Name N/A)";
                    Order order = new Order(
                        rs.getString("OrderID"),
                        rs.getString("UserID"),
                        customerName,
                        rs.getTimestamp("OrderDate").toLocalDateTime(),
                        rs.getDouble("TotalAmount")
                    );
                    // Fetch details using the same connection for consistency within this method call
                    order.setOrderDetails(getOrderDetailsForOrder(conn, order.getOrderId()));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

     public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.FullName FROM Orders o LEFT JOIN Users u ON o.UserID = u.UserID ORDER BY o.OrderDate DESC";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String customerName = rs.getString("FullName");
                if (customerName == null && rs.getString("UserID") != null) customerName = "(Name N/A)";
                else if (customerName == null) customerName = "(User Deleted/Unknown)";

                Order order = new Order(
                    rs.getString("OrderID"),
                    rs.getString("UserID"),
                    customerName,
                    rs.getTimestamp("OrderDate").toLocalDateTime(),
                    rs.getDouble("TotalAmount")
                );
                order.setOrderDetails(getOrderDetailsForOrder(conn, order.getOrderId()));
                orders.add(order);
            }
        }
        return orders;
    }
}
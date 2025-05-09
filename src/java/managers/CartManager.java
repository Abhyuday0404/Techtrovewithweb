package managers;

import models.CartItem;
import models.Product;
import models.User; // Needed for User context
import db.DBUtil;
import core.IdGenerator; // Assuming you have this from Part 1
import exceptions.InvalidProductIdException;
import exceptions.NoQuantityLeftException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    // No currentUserId field needed if we pass it to methods or get from session within servlet

    private ProductManager productManager;

    public CartManager() throws SQLException {
        // Initialize ProductManager, it's a dependency
        try {
            this.productManager = new ProductManager();
        } catch (SQLException e) {
            System.err.println("CartManager FATAL: Failed to initialize ProductManager dependency.");
            throw e; // Rethrow to signal critical failure
        }
    }

    public List<CartItem> getCartItems(String userId) throws SQLException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty when fetching cart items.");
        }
        List<CartItem> cartItems = new ArrayList<>();
        String sql = "SELECT c.CartID, c.Quantity, p.* " + // Select all product fields
                     "FROM Cart c JOIN Products p ON c.ProductID = p.ProductID " +
                     "WHERE c.UserID = ? ORDER BY p.Name"; // Order by product name for consistent display

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Use the ProductManager's mapping logic if available, or map here
                    java.sql.Date sqlMfgDate = rs.getDate("ManufactureDate");
                    Product product = new Product(
                        rs.getString("ProductID"), rs.getString("Name"), rs.getString("Brand"),
                        rs.getString("Model"), rs.getString("Description"), rs.getDouble("Price"),
                        rs.getInt("Stock"), (sqlMfgDate != null) ? sqlMfgDate.toLocalDate() : null,
                        rs.getString("CategoryID"), rs.getString("ImageURL"));

                    CartItem item = new CartItem(
                        rs.getString("CartID"),
                        product, // The fully mapped Product object
                        rs.getInt("Quantity")
                    );
                    cartItems.add(item);
                }
            }
        }
        return cartItems;
    }

    public void addToCart(String userId, String productId, int quantity)
            throws SQLException, NoQuantityLeftException, InvalidProductIdException, IllegalArgumentException {

        if (userId == null || userId.trim().isEmpty()) throw new IllegalArgumentException("User ID required.");
        if (productId == null || productId.trim().isEmpty()) throw new InvalidProductIdException("Product ID required.");
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive.");

        Connection conn = null; // Declare outside try for rollback
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction

            Product product = productManager.getProductById(productId); // Fetch product details
            if (product == null) {
                conn.rollback(); // Rollback before throwing
                throw new InvalidProductIdException("Product with ID '" + productId + "' not found.");
            }
            int availableStock = product.getStock();

            // Check if item already in cart for this user
            String checkCartSql = "SELECT CartID, Quantity FROM Cart WHERE UserID = ? AND ProductID = ?";
            int existingQuantityInCart = 0;
            String existingCartId = null;

            try (PreparedStatement pstCheck = conn.prepareStatement(checkCartSql)) {
                pstCheck.setString(1, userId);
                pstCheck.setString(2, productId);
                try (ResultSet rsCart = pstCheck.executeQuery()) {
                    if (rsCart.next()) {
                        existingCartId = rsCart.getString("CartID");
                        existingQuantityInCart = rsCart.getInt("Quantity");
                    }
                }
            }

            int newTotalQuantityInCart = existingQuantityInCart + quantity;

            if (newTotalQuantityInCart > availableStock) {
                 conn.rollback();
                 throw new NoQuantityLeftException(String.format(
                    "Cannot add %d of '%s'. Requested total %d exceeds available stock of %d. (Already in cart: %d)",
                    quantity, product.getName(), newTotalQuantityInCart, availableStock, existingQuantityInCart));
            }

            if (existingCartId != null) { // Update existing cart item
                String updateSql = "UPDATE Cart SET Quantity = ? WHERE CartID = ?";
                try (PreparedStatement pstUpdate = conn.prepareStatement(updateSql)) {
                    pstUpdate.setInt(1, newTotalQuantityInCart);
                    pstUpdate.setString(2, existingCartId);
                    pstUpdate.executeUpdate();
                    System.out.println("Updated CartID " + existingCartId + " for user " + userId + " to quantity " + newTotalQuantityInCart);
                }
            } else { // Insert new cart item
                String insertSql = "INSERT INTO Cart (CartID, UserID, ProductID, Quantity) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstInsert = conn.prepareStatement(insertSql)) {
                    String newCartId = IdGenerator.generateCartId(); // Assuming IdGenerator is available
                    pstInsert.setString(1, newCartId);
                    pstInsert.setString(2, userId);
                    pstInsert.setString(3, productId);
                    pstInsert.setInt(4, quantity); // Initial quantity to add
                    pstInsert.executeUpdate();
                    System.out.println("Added new CartID " + newCartId + " for user " + userId + " product " + productId + " with quantity " + quantity);
                }
            }
            conn.commit();
        } catch (SQLException | NoQuantityLeftException | InvalidProductIdException | IllegalArgumentException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException exRb) { System.err.println("Rollback failed in addToCart: " + exRb.getMessage()); }
            }
            System.err.println("Error in addToCart for user " + userId + ", product " + productId + ": " + e.getMessage());
            throw e; // Re-throw the exception to be handled by the servlet
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean updateQuantity(String userId, String cartId, int newQuantity)
            throws SQLException, NoQuantityLeftException, IllegalArgumentException {
        if (userId == null || userId.trim().isEmpty()) throw new IllegalArgumentException("User ID required.");
        if (cartId == null || cartId.trim().isEmpty()) throw new IllegalArgumentException("Cart ID required.");

        if (newQuantity <= 0) { // If new quantity is 0 or less, remove the item
            return removeFromCart(userId, cartId);
        }

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // Get product ID and current stock for validation
            String getProductInfoSql = "SELECT c.ProductID, p.Stock, p.Name FROM Cart c JOIN Products p ON c.ProductID = p.ProductID WHERE c.CartID = ? AND c.UserID = ?";
            String productId = null;
            int availableStock = 0;
            String productName = null;

            try(PreparedStatement pstmtInfo = conn.prepareStatement(getProductInfoSql)) {
                pstmtInfo.setString(1, cartId);
                pstmtInfo.setString(2, userId);
                try(ResultSet rsInfo = pstmtInfo.executeQuery()){
                    if(rsInfo.next()){
                        productId = rsInfo.getString("ProductID");
                        availableStock = rsInfo.getInt("Stock");
                        productName = rsInfo.getString("Name");
                    } else {
                        conn.rollback();
                        throw new IllegalArgumentException("Cart item with ID '" + cartId + "' not found for user '" + userId + "'.");
                    }
                }
            }

            if (newQuantity > availableStock) {
                conn.rollback();
                throw new NoQuantityLeftException(String.format(
                    "Cannot update quantity of '%s' to %d. Only %d available in stock.",
                    productName, newQuantity, availableStock));
            }

            String updateSql = "UPDATE Cart SET Quantity = ? WHERE CartID = ? AND UserID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setInt(1, newQuantity);
                pstmt.setString(2, cartId);
                pstmt.setString(3, userId);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    conn.commit();
                    System.out.println("Updated quantity for CartID " + cartId + " to " + newQuantity + " for user " + userId);
                    return true;
                } else {
                    conn.rollback(); // Should not happen if product info was found
                    return false;
                }
            }
        } catch (SQLException | NoQuantityLeftException | IllegalArgumentException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException exRb) { System.err.println("Rollback failed: " + exRb.getMessage()); }
            }
            System.err.println("Error updating cart quantity for CartID " + cartId + ", UserID " + userId + ": " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean removeFromCart(String userId, String cartId) throws SQLException {
        if (userId == null || userId.trim().isEmpty()) throw new IllegalArgumentException("User ID required.");
        if (cartId == null || cartId.trim().isEmpty()) throw new IllegalArgumentException("Cart ID required.");

        String sql = "DELETE FROM Cart WHERE CartID = ? AND UserID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cartId);
            pstmt.setString(2, userId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                 System.out.println("Removed CartID " + cartId + " for user " + userId);
            } else {
                 System.out.println("No cart item found with CartID " + cartId + " for user " + userId + " to remove.");
            }
            return rowsAffected > 0;
        }
    }

    public void clearCart(String userId) throws SQLException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty for clearing cart.");
        }
        String sql = "DELETE FROM Cart WHERE UserID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Cleared cart for UserID " + userId + ". Items removed: " + rowsAffected);
        }
    }
}
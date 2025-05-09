// src/java/managers/CartManager.java
package managers;

import db.DBUtil;
import models.Cart;
import models.CartItem;
import models.Product;
import core.IdGenerator;
import exceptions.InvalidQuantityException; 
import exceptions.NoQuantityLeftException; 

import java.sql.*;
import java.time.LocalDate; // <<< ADDED THIS IMPORT
import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private ProductManager productManager; 

    public CartManager() throws SQLException {
        try {
            this.productManager = new ProductManager();
        } catch (SQLException e) {
            System.err.println("CartManager: Failed to initialize ProductManager: " + e.getMessage());
            throw e;
        }
    }

    public List<CartItem> getCartItems(String userId) throws SQLException {
        List<CartItem> cartItems = new ArrayList<>();
        String sql = "SELECT c.CartID, c.ProductID, c.Quantity, p.Name, p.Price, p.Stock, p.Brand, " +
                     "p.Model, p.Description, p.ManufactureDate, p.CategoryID " + 
                     "FROM Cart c JOIN Products p ON c.ProductID = p.ProductID " +
                     "WHERE c.UserID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date sqlMfgDate = rs.getDate("ManufactureDate");
                    LocalDate mfgDate = (sqlMfgDate != null) ? sqlMfgDate.toLocalDate() : null; // Now LocalDate is recognized
                    
                    Product product = new Product(
                            rs.getString("ProductID"),
                            rs.getString("Name"),
                            rs.getString("Brand"),
                            rs.getString("Model"),        
                            rs.getString("Description"),  
                            rs.getDouble("Price"),
                            rs.getInt("Stock"), 
                            mfgDate,                      
                            rs.getString("CategoryID")    
                    );
                    CartItem item = new CartItem(
                            rs.getString("CartID"),
                            product,
                            rs.getInt("Quantity")
                    );
                    cartItems.add(item);
                }
            }
        }
        return cartItems;
    }

    // ... (The rest of your CartManager.java methods should remain the same as the version you confirmed was working,
    //      ensuring they are consistent with the Product model not having imageUrl)

    public void addItemToCart(String userId, String productId, int quantity)
            throws SQLException, InvalidQuantityException, NoQuantityLeftException {
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity to add to cart must be positive.");
        }

        Product product = productManager.getProductById(productId); 
        if (product == null) {
            throw new SQLException("Product with ID " + productId + " not found.");
        }
        
        Cart existingCartEntry = findCartEntry(userId, productId);

        if (existingCartEntry != null) {
            int newQuantity = existingCartEntry.getQuantity() + quantity;
            if (product.getStock() < newQuantity) { 
                 throw new NoQuantityLeftException("Not enough stock for " + product.getName() +
                                              " to increase quantity. Requested total: " + newQuantity + ", Available: " + product.getStock());
            }
            updateCartItemQuantity(existingCartEntry.getCartId(), newQuantity);
            System.out.println("Updated CartID " + existingCartEntry.getCartId() + " to quantity " + newQuantity);
        } else {
            if (product.getStock() < quantity) { 
                 throw new NoQuantityLeftException("Not enough stock for " + product.getName() +
                                                  ". Requested: " + quantity + ", Available: " + product.getStock());
            }
            String cartId = IdGenerator.generateCartId();
            String sql = "INSERT INTO Cart (CartID, UserID, ProductID, Quantity, AddedDate) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, cartId);
                pstmt.setString(2, userId);
                pstmt.setString(3, productId);
                pstmt.setInt(4, quantity);
                pstmt.setTimestamp(5, Timestamp.valueOf(java.time.LocalDateTime.now()));
                pstmt.executeUpdate();
                System.out.println("Added new CartID " + cartId + " for user " + userId + " product " + productId + " with quantity " + quantity);
            }
        }
    }

    private Cart findCartEntry(String userId, String productId) throws SQLException {
        String sql = "SELECT * FROM Cart WHERE UserID = ? AND ProductID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Cart(
                            rs.getString("CartID"),
                            rs.getString("UserID"),
                            rs.getString("ProductID"),
                            rs.getInt("Quantity")
                    );
                }
            }
        }
        return null;
    }

    public void updateCartItemQuantity(String cartId, int newQuantity)
            throws SQLException, InvalidQuantityException, NoQuantityLeftException {
        if (newQuantity <= 0) {
            throw new InvalidQuantityException("New quantity must be positive. To remove, use removeItemFromCart.");
        }

        String getProductSql = "SELECT ProductID FROM Cart WHERE CartID = ?";
        String productId = null;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmtGetProd = conn.prepareStatement(getProductSql)) {
            pstmtGetProd.setString(1, cartId);
            try (ResultSet rs = pstmtGetProd.executeQuery()) {
                if (rs.next()) {
                    productId = rs.getString("ProductID");
                } else {
                    throw new SQLException("Cart item with ID " + cartId + " not found to update quantity.");
                }
            }
        }

        Product product = productManager.getProductById(productId); 
        if (product == null) {
            throw new SQLException("Product associated with cart item " + cartId + " not found.");
        }
        if (product.getStock() < newQuantity) {
            throw new NoQuantityLeftException("Not enough stock for " + product.getName() +
                                              ". Requested: " + newQuantity + ", Available: " + product.getStock());
        }

        String sql = "UPDATE Cart SET Quantity = ? WHERE CartID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setString(2, cartId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                System.err.println("Warning: No cart item found with CartID " + cartId + " to update quantity.");
            } else {
                System.out.println("Updated quantity for CartID " + cartId + " to " + newQuantity);
            }
        }
    }

    public void removeItemFromCart(String cartId) throws SQLException {
        String sql = "DELETE FROM Cart WHERE CartID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cartId);
            int affectedRows = pstmt.executeUpdate();
             if (affectedRows == 0) {
                System.err.println("Warning: No cart item found with CartID " + cartId + " to remove.");
            } else {
                System.out.println("Removed CartID " + cartId);
            }
        }
    }

    public void clearCart(String userId) throws SQLException {
        String sql = "DELETE FROM Cart WHERE UserID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
            System.out.println("Cart cleared for user: " + userId);
        }
    }

    public double calculateTotal(List<CartItem> cartItems) {
        double total = 0.0;
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                total += item.getSubtotal(); 
            }
        }
        return total;
    }
}
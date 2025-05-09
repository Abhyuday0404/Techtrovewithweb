// src/java/managers/CartManager.java
package managers;

import db.DBUtil;
import models.Cart;
import models.CartItem;
import models.Product;
import core.IdGenerator;
import exceptions.InvalidQuantityException; // Assuming you have this
import exceptions.NoQuantityLeftException; // Assuming you have this

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private ProductManager productManager; // To get product details

    public CartManager() throws SQLException {
        try {
            this.productManager = new ProductManager();
        } catch (SQLException e) {
            System.err.println("CartManager: Failed to initialize ProductManager: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves all cart items for a given user, enriched with product details.
     * @param userId The ID of the user.
     * @return A list of CartItem objects.
     * @throws SQLException if a database error occurs.
     */
    public List<CartItem> getCartItems(String userId) throws SQLException { // <--- METHOD NAME MATCHES SERVLET
        List<CartItem> cartItems = new ArrayList<>();
        // SQL to join Cart with Products to get product details
        String sql = "SELECT c.CartID, c.ProductID, c.Quantity, p.Name, p.Price, p.Stock, p.ImageURL, p.Brand " +
                     "FROM Cart c JOIN Products p ON c.ProductID = p.ProductID " +
                     "WHERE c.UserID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Create a Product object for the CartItem
                    Product product = new Product(
                            rs.getString("ProductID"),
                            rs.getString("Name"),
                            rs.getString("Brand"),
                            null, // Model - not fetched here, can be added if needed
                            null, // Description - not fetched here
                            rs.getDouble("Price"),
                            rs.getInt("Stock"), // Current stock, good for display/validation
                            null, // ManufactureDate - not fetched
                            null, // CategoryId - not fetched
                            rs.getString("ImageURL")
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

    /**
     * Adds a product to the user's cart or updates its quantity if it already exists.
     * @param userId The ID of the user.
     * @param productId The ID of the product to add.
     * @param quantity The quantity to add.
     * @throws SQLException if a database error occurs.
     * @throws InvalidQuantityException if quantity is not positive.
     * @throws NoQuantityLeftException if requested quantity exceeds stock.
     */
    public void addItemToCart(String userId, String productId, int quantity) // <--- METHOD NAME MATCHES SERVLET
            throws SQLException, InvalidQuantityException, NoQuantityLeftException {
        if (quantity <= 0) {
            throw new InvalidQuantityException("Quantity to add to cart must be positive.");
        }

        Product product = productManager.getProductById(productId);
        if (product == null) {
            throw new SQLException("Product with ID " + productId + " not found.");
        }
        // Stock check logic here will be slightly different from my previous suggestion if using this version
        // This version checks stock *before* checking if item is in cart
        if (product.getStock() < quantity && findCartEntry(userId, productId) == null) { // Only for new items
            throw new NoQuantityLeftException("Not enough stock for " + product.getName() +
                                              ". Requested: " + quantity + ", Available: " + product.getStock());
        }


        Cart existingCartEntry = findCartEntry(userId, productId);

        if (existingCartEntry != null) {
            // Product already in cart, update quantity
            int newQuantity = existingCartEntry.getQuantity() + quantity;
            if (product.getStock() < newQuantity) { // Re-check stock for combined quantity
                 throw new NoQuantityLeftException("Not enough stock for " + product.getName() +
                                              " to increase quantity. Requested total: " + newQuantity + ", Available: " + product.getStock());
            }
            updateCartItemQuantity(existingCartEntry.getCartId(), newQuantity); // This calls the other update method
            System.out.println("Updated CartID " + existingCartEntry.getCartId() + " to quantity " + newQuantity);
        } else {
            // New product for the cart
            String cartId = IdGenerator.generateCartId();
            String sql = "INSERT INTO Cart (CartID, UserID, ProductID, Quantity) VALUES (?, ?, ?, ?)";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, cartId);
                pstmt.setString(2, userId);
                pstmt.setString(3, productId);
                pstmt.setInt(4, quantity);
                pstmt.executeUpdate();
                System.out.println("Added new CartID " + cartId + " for user " + userId + " product " + productId + " with quantity " + quantity);
            }
        }
    }

    /**
     * Finds an existing cart entry for a user and product.
     * @param userId The user's ID.
     * @param productId The product's ID.
     * @return The Cart object if found, null otherwise.
     * @throws SQLException if a database error occurs.
     */
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

    /**
     * Updates the quantity of an existing item in the cart.
     * @param cartId The ID of the cart entry to update.
     * @param newQuantity The new quantity (must be positive).
     * @throws SQLException if a database error occurs.
     * @throws InvalidQuantityException if newQuantity is not positive.
     * @throws NoQuantityLeftException if newQuantity exceeds available stock (requires fetching product stock).
     */
    public void updateCartItemQuantity(String cartId, int newQuantity) // <--- THIS METHOD IS USED INTERNALLY AND BY SERVLET
            throws SQLException, InvalidQuantityException, NoQuantityLeftException {
        if (newQuantity <= 0) {
            // If new quantity is 0 or less, it's better to call removeItemFromCart explicitly from servlet
            // For now, this version of CartManager expects positive quantities here.
            throw new InvalidQuantityException("New quantity must be positive. To remove, use removeItemFromCart from the servlet if quantity is zero.");
        }

        // Before updating, check stock for the product associated with this cartId
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

    /**
     * Removes an item completely from the cart.
     * @param cartId The ID of the cart entry to remove.
     * @throws SQLException if a database error occurs.
     */
    public void removeItemFromCart(String cartId) throws SQLException { // <--- METHOD NAME MATCHES SERVLET
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

    /**
     * Clears all items from a user's cart (e.g., after checkout).
     * @param userId The ID of the user whose cart is to be cleared.
     * @throws SQLException if a database error occurs.
     */
    public void clearCart(String userId) throws SQLException {
        String sql = "DELETE FROM Cart WHERE UserID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.executeUpdate();
            System.out.println("Cart cleared for user: " + userId);
        }
    }

    /**
     * Calculates the total price of all items in the cart.
     * @param cartItems List of CartItem objects.
     * @return The total price.
     */
    public double calculateTotal(List<CartItem> cartItems) {
        double total = 0.0;
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                total += item.getSubtotal(); // CartItem.getSubtotal() = product.getPrice() * quantity
            }
        }
        return total;
    }
}
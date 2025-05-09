package models;

/**
 * Represents a single row (an entry) in the user's shopping cart database table (Cart).
 * This model primarily holds the foreign keys and quantity.
 * Use the CartItem model for richer objects including Product details.
 */
public class Cart {
    private String cartId;    // Primary key for the Cart table row (e.g., CRT_XYZ)
    private String userId;    // Foreign key to the Users table
    private String productId; // Foreign key to the Products table
    private int quantity;     // Quantity of the specific product in the cart

    /**
     * Constructs a Cart object representing a database row.
     *
     * @param cartId    The unique ID for this cart entry.
     * @param userId    The ID of the user owning this cart item.
     * @param productId The ID of the product in this cart entry.
     * @param quantity  The quantity of the product (should be positive).
     */
    public Cart(String cartId, String userId, String productId, int quantity) {
        // Basic validation
        if (cartId == null || cartId.trim().isEmpty() ||
            userId == null || userId.trim().isEmpty() ||
            productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("CartID, UserID, and ProductID cannot be null or empty.");
        }
        if (quantity <= 0) {
            // Cart entries should generally have positive quantity.
            // Consider if 0 is allowed temporarily during updates before removal.
             System.err.println("Warning: Cart object created with non-positive quantity: " + quantity);
            // throw new IllegalArgumentException("Cart quantity must be positive.");
        }

        this.cartId = cartId;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }

    // --- Getters ---
    public String getCartId() {
        return cartId;
    }

    public String getUserId() {
        return userId;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    // --- Setters ---
    // Setting IDs might be discouraged after creation, depends on use case.
    public void setCartId(String cartId) {
        if (cartId == null || cartId.trim().isEmpty()) {
            throw new IllegalArgumentException("Cart ID cannot be set to null or empty.");
        }
        this.cartId = cartId;
    }

    public void setUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be set to null or empty.");
        }
        this.userId = userId;
    }

    public void setProductId(String productId) {
         if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be set to null or empty.");
        }
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        if (quantity < 0) { // Allow setting to 0 (e.g., before removal), but not negative.
            throw new IllegalArgumentException("Quantity cannot be negative.");
        }
        this.quantity = quantity;
    }

    // --- Overrides ---
    @Override
    public String toString() {
        return "Cart{" +
                "cartId='" + cartId + '\'' +
                ", userId='" + userId + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                '}';
    }

     // --- equals() and hashCode() ---
     // Based on cartId as the primary identifier for a cart row.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return java.util.Objects.equals(cartId, cart.cartId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(cartId);
    }
}
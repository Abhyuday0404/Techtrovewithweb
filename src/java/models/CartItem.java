package models;

/**
 * Represents a user-facing view of an item in the shopping cart.
 * This object combines the quantity from the Cart table entry with the
 * full details of the associated Product. It is typically constructed
 * after joining Cart and Products data.
 */
public class CartItem {
    private final String cartId;    // The unique ID of the corresponding Cart table row
    private final Product product;  // The full Product object associated with this cart item
    private int quantity;     // Quantity of this product in the cart (can be updated)

    /**
     * Constructs a CartItem.
     *
     * @param cartId    The unique ID of the entry in the Cart table.
     * @param product   The associated Product object (must not be null).
     * @param quantity  The quantity of the product in the cart (should be positive).
     */
    public CartItem(String cartId, Product product, int quantity) {
        if (cartId == null || cartId.trim().isEmpty()) {
            throw new IllegalArgumentException("Cart ID cannot be null or empty for CartItem.");
        }
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null for CartItem.");
        }
        if (quantity <= 0) {
             // Allow creation, but operations might fail if quantity is not positive.
             System.err.println("Warning: CartItem created with non-positive quantity: " + quantity);
        }
        this.cartId = cartId;
        this.product = product;
        this.quantity = quantity;
    }

    // --- Getters ---
    public String getCartId() {
        return cartId;
    }

    /** Gets the full Product object associated with this cart item. */
    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

     /** Sets the quantity for this cart item (e.g., if updated locally before DB sync). */
     public void setQuantity(int quantity) {
         if (quantity < 0) { // Allow 0 temporarily? Or enforce > 0?
             throw new IllegalArgumentException("Quantity cannot be negative.");
         }
         this.quantity = quantity;
     }

    // --- Convenience Getters (delegate to embedded Product) ---
    public String getProductId() {
        return product.getProductId();
    }

    public String getProductName() {
        return product.getName();
    }

    public double getPrice() {
        return product.getPrice(); // Get the current price from the Product
    }

    public String getBrand() {
         return product.getBrand();
    }


    // --- Calculation ---
    /**
     * Calculates the subtotal for this line item (price * quantity).
     * Uses the current price stored in the associated Product object.
     * @return The calculated subtotal.
     */
    public double getSubtotal() {
        return quantity * product.getPrice();
    }

    // --- Overrides ---
    @Override
    public String toString() {
        // Provides a more detailed string representation than the basic Cart model
        return String.format("CartItem[CartID: %s, Product: %s (%s), Qty: %d, Price: %.2f, Subtotal: %.2f]",
                             cartId,
                             product.getName(), product.getProductId(),
                             quantity,
                             product.getPrice(),
                             getSubtotal());
    }

     // --- equals() and hashCode() ---
     // Primarily based on cartId, assuming it's the unique identifier for this specific cart entry.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return java.util.Objects.equals(cartId, cartItem.cartId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(cartId);
    }
}
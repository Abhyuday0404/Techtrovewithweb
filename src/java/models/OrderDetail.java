package models;

import java.util.Objects;

/**
 * Represents a single line item within a customer Order.
 * Contains information about a specific product purchased, its quantity,
 * and the price at the time of the order.
 * Corresponds to a row in the OrderDetails database table.
 */
public class OrderDetail {
    private final String orderDetailId; // Primary key for this line item
    private final String orderId;       // Foreign key to the parent Order
    private final String productId;     // Foreign key to the Product purchased (can be NULL if product deleted)
    private final String productName;   // Name of the product (denormalized/joined for convenience)
    private final int quantity;         // Quantity of this product purchased
    private final double price;         // Price per unit *at the time the order was placed*

    /**
     * Constructs an OrderDetail object.
     *
     * @param orderDetailId The unique ID for this order line item.
     * @param orderId       The ID of the order this item belongs to.
     * @param productId     The ID of the product purchased (can be null if product was deleted).
     * @param productName   The name of the product (retrieved via JOIN, can indicate if deleted).
     * @param quantity      The quantity purchased (must be positive).
     * @param price         The price per unit at the time of order (must be non-negative).
     */
    public OrderDetail(String orderDetailId, String orderId, String productId,
                       String productName, int quantity, double price) {
        // Validate required IDs and values
        if (orderDetailId == null || orderDetailId.trim().isEmpty())
            throw new IllegalArgumentException("OrderDetail ID cannot be null or empty.");
        if (orderId == null || orderId.trim().isEmpty())
            throw new IllegalArgumentException("Order ID cannot be null or empty.");
        // ProductID can be null if the referenced product was deleted (due to ON DELETE SET NULL)
        // if (productId == null || productId.trim().isEmpty())
        //    throw new IllegalArgumentException("Product ID cannot be null or empty.");
        if (quantity <= 0)
            throw new IllegalArgumentException("Quantity must be positive for an order detail.");
        if (price < 0)
            throw new IllegalArgumentException("Price cannot be negative for an order detail.");

        this.orderDetailId = orderDetailId;
        this.orderId = orderId;
        this.productId = productId;
        // Use provided name, which might indicate deletion if ProductID is null
        this.productName = productName != null ? productName : "(Product Data Unavailable)";
        this.quantity = quantity;
        this.price = price;
    }

    // --- Getters ---
    public String getOrderDetailId() { return orderDetailId; }
    public String getOrderId() { return orderId; }
    /** Gets the ID of the product. May be null if the product was deleted after the order was placed. */
    public String getProductId() { return productId; }
    /** Gets the name of the product, retrieved at the time the details were fetched. May indicate if product was deleted. */
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    /** Gets the price per unit paid for this item *at the time of the order*. */
    public double getPrice() { return price; }

    // --- Calculation ---
    /**
     * Calculates the subtotal for this line item (quantity * price_at_time_of_order).
     * @return The calculated subtotal.
     */
    public double getSubtotal() {
        return quantity * price;
    }

    // --- Overrides ---
    @Override
    public String toString() {
        return String.format("OrderDetail[ID:%s, OrdID:%s, ProdID:%s, Name:'%s', Qty:%d, Price:%.2f, Sub:%.2f]",
                orderDetailId, orderId,
                (productId != null ? productId : "N/A"),
                productName, // Already handled potential nulls
                quantity, price, getSubtotal());
    }

    /**
     * Checks for equality based on the orderDetailId.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDetail that = (OrderDetail) o;
        return Objects.equals(orderDetailId, that.orderDetailId);
    }

    /**
     * Generates a hash code based on the orderDetailId.
     */
    @Override
    public int hashCode() {
        return Objects.hash(orderDetailId);
    }
}
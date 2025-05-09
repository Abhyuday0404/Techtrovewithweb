package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // Used in detailed toString if needed

/**
 * Represents a customer order placed in the system.
 * Contains header information (IDs, date, total) and a list of OrderDetail items.
 * Corresponds to a row in the Orders database table.
 */
public class Order {
    private final String orderId;        // Primary key for the Orders table
    private final String userId;         // Foreign key to the Users table (who placed the order)
    private final String customerName;   // Customer's name (denormalized/joined from Users)
    private final LocalDateTime orderDate; // When the order was placed
    private final double totalAmount;    // The total amount calculated *at the time of order*
    private List<OrderDetail> orderDetails; // List of individual items in the order

    // Formatter for consistent date/time output
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructs an Order object.
     *
     * @param orderId      The unique ID for this order.
     * @param userId       The ID of the user who placed the order.
     * @param customerName The name of the customer (can be retrieved via JOIN).
     * @param orderDate    The date and time the order was placed.
     * @param totalAmount  The total cost of the order at the time it was placed.
     */
    public Order(String orderId, String userId, String customerName,
                 LocalDateTime orderDate, double totalAmount) {
        // Validate essential fields
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty.");
        }
        // UserID might be null if the user was deleted and FK is SET NULL
        // if (userId == null || userId.trim().isEmpty()) {
        //    throw new IllegalArgumentException("User ID cannot be null or empty.");
        // }
        if (totalAmount < 0) {
             throw new IllegalArgumentException("Total amount cannot be negative.");
        }

        this.orderId = orderId;
        this.userId = userId;
        this.customerName = customerName; // Can be null if user was deleted or not joined
        this.orderDate = orderDate != null ? orderDate : LocalDateTime.now(); // Default to now if null
        this.totalAmount = totalAmount;
        this.orderDetails = new ArrayList<>(); // Initialize details list (can be populated later)
    }

    // --- Getters ---
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getCustomerName() { return customerName; }
    public LocalDateTime getOrderDate() { return orderDate; }
    /** Gets the total amount as calculated when the order was placed. */
    public double getTotalAmount() { return totalAmount; }
    /** Gets the list of items (OrderDetails) associated with this order. */
    public List<OrderDetail> getOrderDetails() { return orderDetails; }

    // --- Setter for Order Details ---
    /**
     * Sets the list of order details for this order.
     * Typically called after fetching details from the database.
     * @param orderDetails The list of OrderDetail items.
     */
    public void setOrderDetails(List<OrderDetail> orderDetails) {
        // Use a new list if null is passed, for safety
        this.orderDetails = (orderDetails != null) ? orderDetails : new ArrayList<>();
    }

    // --- Overrides ---
    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", userId='" + (userId != null ? userId : "N/A") + '\'' +
                ", customerName='" + (customerName != null ? customerName : "(N/A)") + '\'' +
                ", orderDate=" + (orderDate != null ? orderDate.format(DATE_TIME_FORMATTER) : "N/A") +
                ", totalAmount=₹" + String.format("%.2f", totalAmount) + // Format currency
                ", itemCount=" + (orderDetails != null ? orderDetails.size() : 0) +
                '}';
    }

     /**
      * Checks for equality based on the orderId.
      */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return java.util.Objects.equals(orderId, order.orderId);
    }

    /**
     * Generates a hash code based on the orderId.
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(orderId);
    }
}
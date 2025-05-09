// src/java/models/OrderDetail.java
package models;

import java.util.Objects;

public class OrderDetail {
    private String orderDetailId;
    private String orderId;       // <--- REMOVED 'final' KEYWORD HERE
    private final String productId;
    private final String productName;
    private final int quantity;
    private final double price;

    public OrderDetail(String orderDetailId, String orderId, String productId,
                       String productName, int quantity, double price) {
        // Constructor remains the same (with the orderDetailId check already removed)

        this.orderDetailId = orderDetailId;
        this.orderId = orderId; // Now this assignment in the constructor is fine for a non-final field
        this.productId = productId;
        this.productName = productName != null ? productName : "(Product Data Unavailable)";
        this.quantity = quantity;
        this.price = price;
    }

    // --- Getters ---
    public String getOrderDetailId() { return orderDetailId; }
    public String getOrderId() { return orderId; }
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }

    // --- Setters ---
    public void setOrderDetailId(String orderDetailId) {
        if (orderDetailId == null || orderDetailId.trim().isEmpty()) {
            throw new IllegalArgumentException("OrderDetail ID cannot be set to null or empty by manager.");
        }
        this.orderDetailId = orderDetailId;
    }

    public void setOrderId(String orderId) { // This setter can now work
         if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be set to null or empty by manager.");
        }
        this.orderId = orderId;
    }
    
    // ... rest of the OrderDetail class (getSubtotal, toString, equals, hashCode)
    public double getSubtotal() {
        return quantity * price;
    }

    @Override
    public String toString() {
        return String.format("OrderDetail[ID:%s, OrdID:%s, ProdID:%s, Name:'%s', Qty:%d, Price:%.2f, Sub:%.2f]",
                (orderDetailId != null ? orderDetailId : "NEW"), 
                (this.orderId != null ? this.orderId : "NEW"), // Use this.orderId to be explicit
                (productId != null ? productId : "N/A"),
                productName,
                quantity, price, getSubtotal());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDetail that = (OrderDetail) o;
        if (orderDetailId == null || that.orderDetailId == null) {
            return false;
        }
        return Objects.equals(orderDetailId, that.orderDetailId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderDetailId != null ? orderDetailId : System.identityHashCode(this));
    }
}
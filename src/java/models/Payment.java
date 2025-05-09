package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Payment {
    private String paymentId;
    private String orderId;
    private String paymentMethod; // e.g., "COD_DEMO", "Credit Card Placeholder"
    private String transactionId; // Optional external ID
    private LocalDateTime paymentDate;
    private PaymentStatus status;

    public enum PaymentStatus {
        PENDING,    // Default for demo
        COMPLETED,
        FAILED,
        REFUNDED
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Payment(String paymentId, String orderId, String paymentMethod,
                   String transactionId, LocalDateTime paymentDate, PaymentStatus status) {
        if (paymentId == null || paymentId.trim().isEmpty())
            throw new IllegalArgumentException("Payment ID cannot be null or empty.");
        if (orderId == null || orderId.trim().isEmpty())
            throw new IllegalArgumentException("Order ID cannot be null or empty.");
        if (paymentMethod == null || paymentMethod.trim().isEmpty())
            throw new IllegalArgumentException("Payment Method cannot be null or empty.");

        this.paymentId = paymentId;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId; // Can be null
        this.paymentDate = (paymentDate != null) ? paymentDate : LocalDateTime.now();
        this.status = (status != null) ? status : PaymentStatus.PENDING;
    }

    // Getters
    public String getPaymentId() { return paymentId; }
    public String getOrderId() { return orderId; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getTransactionId() { return transactionId; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public PaymentStatus getStatus() { return status; }

    // Setters
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Payment{" + "paymentId='" + paymentId + '\'' + ", orderId='" + orderId + '\'' +
               ", method='" + paymentMethod + '\'' + ", status=" + status +
               ", date=" + (paymentDate != null ? paymentDate.format(DATE_TIME_FORMATTER) : "N/A") + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(paymentId, payment.paymentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }
}
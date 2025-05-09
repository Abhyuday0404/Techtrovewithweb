package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class PurchaseHistory {
    private String historyId;
    private String userId;
    private String orderId;
    private LocalDateTime purchaseDate; // Often same as order date

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public PurchaseHistory(String historyId, String userId, String orderId, LocalDateTime purchaseDate) {
        if (historyId == null || historyId.trim().isEmpty())
            throw new IllegalArgumentException("History ID cannot be null or empty.");
        // userId can be null if the user was deleted and FK is ON DELETE SET NULL
        if (orderId == null || orderId.trim().isEmpty())
            throw new IllegalArgumentException("Order ID cannot be null or empty for Purchase History.");

        this.historyId = historyId;
        this.userId = userId;
        this.orderId = orderId;
        this.purchaseDate = (purchaseDate != null) ? purchaseDate : LocalDateTime.now();
    }

    // Getters
    public String getHistoryId() { return historyId; }
    public String getUserId() { return userId; }
    public String getOrderId() { return orderId; }
    public LocalDateTime getPurchaseDate() { return purchaseDate; }

    // Setters (less common for history records)
    public void setHistoryId(String historyId) { this.historyId = historyId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

    @Override
    public String toString() {
        return "PurchaseHistory{" + "historyId='" + historyId + '\'' +
               ", userId='" + (userId != null ? userId : "N/A") + '\'' +
               ", orderId='" + orderId + '\'' +
               ", purchaseDate=" + (purchaseDate != null ? purchaseDate.format(DATE_TIME_FORMATTER) : "N/A") + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchaseHistory that = (PurchaseHistory) o;
        return Objects.equals(historyId, that.historyId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(historyId);
    }
}
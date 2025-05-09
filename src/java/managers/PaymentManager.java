package managers;

import db.DBUtil;
import models.Payment;
import core.IdGenerator;

import java.sql.*;
import java.time.LocalDateTime;

public class PaymentManager {

    public PaymentManager() throws SQLException {
        // Constructor
    }

    /**
     * Records a payment for an order.
     * In a real app, this would involve more steps like payment gateway interaction.
     * For this demo, it just creates a record in the Payments table.
     * This method assumes it's called within an existing transaction if part of an order process,
     * or handles its own if called standalone. For simplicity now, handles its own.
     */
    public String recordPayment(String orderId, double amount, String paymentMethodDetails) throws SQLException {
        String paymentId = IdGenerator.generatePaymentId();
        // For demo purposes, we'll assume payment is completed successfully (or COD)
        Payment.PaymentStatus status = Payment.PaymentStatus.COMPLETED;
        String transactionIdPlaceholder = "DEMO_TXN_" + System.currentTimeMillis();

        String sql = "INSERT INTO Payments (PaymentID, OrderID, PaymentMethod, TransactionID, PaymentDate, Status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection(); // Manages its own connection for this standalone recording
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, paymentId);
            pstmt.setString(2, orderId);
            pstmt.setString(3, paymentMethodDetails); // e.g., "Cash on Delivery (Demo)"
            pstmt.setString(4, transactionIdPlaceholder);
            pstmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(6, status.name()); // Store enum name as string

            pstmt.executeUpdate();
            System.out.println("Payment recorded: ID " + paymentId + " for Order ID " + orderId + " with status " + status);
            return paymentId;
        } catch (SQLException e) {
            System.err.println("Error recording payment for Order ID " + orderId + ": " + e.getMessage());
            throw e;
        }
    }

    public Payment getPaymentByOrderId(String orderId) throws SQLException {
        Payment payment = null;
        String sql = "SELECT * FROM Payments WHERE OrderID = ? ORDER BY PaymentDate DESC LIMIT 1"; // Get latest for an order
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    payment = new Payment(
                            rs.getString("PaymentID"),
                            rs.getString("OrderID"),
                            rs.getString("PaymentMethod"),
                            rs.getString("TransactionID"),
                            rs.getTimestamp("PaymentDate") != null ? rs.getTimestamp("PaymentDate").toLocalDateTime() : null,
                            Payment.PaymentStatus.valueOf(rs.getString("Status"))
                    );
                }
            }
        }
        return payment;
    }

    // Other methods like getPaymentById, updatePaymentStatus, etc., could be added.
}
package managers;

import models.Feedback;
import models.Product; // For fetching product name
import models.User;    // For fetching user name
import db.DBUtil;
import core.IdGenerator;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FeedbackManager {

    // No connection field here; get connection per method.
    // ProductManager might be needed if we want to validate product existence before adding feedback,
    // or enrich Feedback objects with product names without joining in every query.
    // private ProductManager productManager;


    public FeedbackManager() throws SQLException {
        // try {
        //     this.productManager = new ProductManager();
        // } catch (SQLException e) {
        //     System.err.println("FeedbackManager: Failed to initialize ProductManager dependency.");
        //     // Depending on how critical it is, you might throw e or just log
        // }
    }

    /**
     * Adds feedback submitted by a user for a specific product.
     *
     * @param userId    The ID of the user submitting the feedback.
     * @param productId The ID of the product receiving feedback.
     * @param rating    The rating given (1-5).
     * @param comment   The user's textual comment (can be empty or null).
     * @throws SQLException If a database error occurs.
     * @throws IllegalArgumentException If parameters are invalid.
     */
    public void addFeedback(String userId, String productId, int rating, String comment)
            throws SQLException, IllegalArgumentException {

        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty when adding feedback.");
        }
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty when adding feedback.");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be an integer between 1 and 5.");
        }

        String sql = "INSERT INTO Feedback (FeedbackID, UserID, ProductID, Message, Rating, Timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, IdGenerator.generateFeedbackId());
            pstmt.setString(2, userId);
            pstmt.setString(3, productId);
            if (comment != null && !comment.trim().isEmpty()) {
                pstmt.setString(4, comment.trim());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }
            pstmt.setInt(5, rating);
            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            pstmt.executeUpdate();
            System.out.println("Feedback added successfully for ProductID: " + productId + " by UserID: " + userId);

        } catch (SQLException e) {
            System.err.println("Error adding feedback for product " + productId + " by user " + userId + ": " + e.getMessage());
            // Check for specific FK violations (e.g., ProductID doesn't exist)
             if (e.getMessage().toLowerCase().contains("foreign key constraint fails")) {
                 if (e.getMessage().toLowerCase().contains("productid")) {
                    throw new SQLException("Cannot add feedback: Product with ID '" + productId + "' may not exist or there's a data issue.", e);
                 } else if (e.getMessage().toLowerCase().contains("userid")) {
                     throw new SQLException("Cannot add feedback: User with ID '" + userId + "' may not exist.", e);
                 }
            }
            throw e; // Re-throw other SQL exceptions
        }
    }

    /**
     * Retrieves all feedback entries for a specific product, including user's full name.
     */
    public List<Feedback> getProductFeedbackWithUserNames(String productId) throws SQLException {
        if (productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be empty.");
        }
        List<Feedback> feedbackList = new ArrayList<>();
        String sql = "SELECT f.*, u.FullName " +
                     "FROM Feedback f " +
                     "JOIN Users u ON f.UserID = u.UserID " +
                     "WHERE f.ProductID = ? ORDER BY f.Timestamp DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Feedback feedback = mapResultSetToFeedback(rs);
                    feedback.setUserName(rs.getString("FullName"));
                    // feedback.setProductName(productName); // Product name already known if fetching for a specific product
                    feedbackList.add(feedback);
                }
            }
        }
        return feedbackList;
    }

    /**
     * Retrieves all feedback entries, including user names and product names. (For Admin View)
     */
    public List<Feedback> getAllFeedbackWithDetails() throws SQLException {
        List<Feedback> feedbackList = new ArrayList<>();
        String sql = "SELECT f.*, u.FullName, p.Name AS ProductName " +
                     "FROM Feedback f " +
                     "LEFT JOIN Users u ON f.UserID = u.UserID " + // LEFT JOIN in case user deleted
                     "LEFT JOIN Products p ON f.ProductID = p.ProductID " + // LEFT JOIN in case product deleted
                     "ORDER BY f.Timestamp DESC";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Feedback feedback = mapResultSetToFeedback(rs);
                feedback.setUserName(rs.getString("FullName")); // Might be null if user deleted
                feedback.setProductName(rs.getString("ProductName")); // Might be null if product deleted
                feedbackList.add(feedback);
            }
        }
        return feedbackList;
    }


    private Feedback mapResultSetToFeedback(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("Timestamp");
        LocalDateTime timestamp = (ts != null) ? ts.toLocalDateTime() : null;

        return new Feedback(
            rs.getString("FeedbackID"),
            rs.getString("UserID"),
            rs.getString("ProductID"),
            rs.getString("Message"),
            rs.getInt("Rating"),
            timestamp
        );
        // Joined fields (UserName, ProductName) are set by the calling method.
    }
}
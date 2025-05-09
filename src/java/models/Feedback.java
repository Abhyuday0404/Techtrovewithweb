package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a customer feedback entry, typically associated with a product and a user.
 * Corresponds to a row in the Feedback database table.
 */
public class Feedback {
    private String feedbackId;     // Primary key for the Feedback table
    private String userId;         // Foreign key to the Users table
    private String productId;      // Foreign key to the Products table
    private String message;        // The textual feedback content
    private int rating;            // Numerical rating (e.g., 1-5)
    private LocalDateTime timestamp; // When the feedback was submitted

    // Optional fields populated by JOINs when retrieving feedback for display
    private String userName;       // Full name of the user who gave feedback
    private String productName;    // Name of the product the feedback is about

    // Static formatter for consistent date/time output in toString/display methods
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Constructs a Feedback object.
     *
     * @param feedbackId The unique ID for this feedback entry.
     * @param userId     The ID of the user submitting the feedback.
     * @param productId  The ID of the product being reviewed.
     * @param message    The feedback comment (can be null or empty).
     * @param rating     The rating (must be 1-5).
     * @param timestamp  The date and time the feedback was submitted (defaults to now if null).
     */
    public Feedback(String feedbackId, String userId, String productId, String message, int rating, LocalDateTime timestamp) {
        // Basic validation for essential IDs
        if (feedbackId == null || feedbackId.trim().isEmpty() ||
            userId == null || userId.trim().isEmpty() ||
            productId == null || productId.trim().isEmpty()) {
            throw new IllegalArgumentException("Feedback, User, and Product IDs cannot be null or empty.");
        }
        // Use setter for rating validation
        setRating(rating);

        this.feedbackId = feedbackId;
        this.userId = userId;
        this.productId = productId;
        this.message = message; // Allow null/empty message
        this.timestamp = (timestamp != null) ? timestamp : LocalDateTime.now(); // Default timestamp if needed
    }

    // --- Getters ---
    public String getFeedbackId() { return feedbackId; }
    public String getUserId() { return userId; }
    public String getProductId() { return productId; }
    public String getMessage() { return message; }
    public int getRating() { return rating; }
    public LocalDateTime getTimestamp() { return timestamp; }
    /** Gets the user's name (populated by a JOIN). */
    public String getUserName() { return userName; }
    /** Gets the product's name (populated by a JOIN). */
    public String getProductName() { return productName; }

    // --- Setters ---
    // Setters for primary fields might be restricted depending on use case.
    public void setFeedbackId(String feedbackId) { this.feedbackId = feedbackId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setProductId(String productId) { this.productId = productId; }
    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    /** Sets the user's name (usually after fetching via JOIN). */
    public void setUserName(String userName) { this.userName = userName; }
    /** Sets the product's name (usually after fetching via JOIN). */
    public void setProductName(String productName) { this.productName = productName; }

    /**
     * Sets the rating, ensuring it's within the valid range (1-5).
     * @param rating The rating value.
     * @throws IllegalArgumentException If the rating is outside the valid range.
     */
    public void setRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        this.rating = rating;
    }

    // --- Overrides ---
    @Override
    public String toString() {
        return "Feedback{" +
                "id='" + feedbackId + '\'' +
                ", userId='" + userId + '\'' + (userName != null ? " (" + userName + ")" : "") +
                ", productId='" + productId + '\'' + (productName != null ? " [" + productName + "]" : "") +
                ", rating=" + rating + "/5" +
                ", message='" + (message != null && message.length() > 30 ? message.substring(0, 27) + "..." : message) + '\'' +
                ", time=" + (timestamp != null ? timestamp.format(DATE_TIME_FORMATTER) : "N/A") +
                '}';
    }

     /**
      * Checks for equality based on the feedbackId.
      */
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         Feedback feedback = (Feedback) o;
         return java.util.Objects.equals(feedbackId, feedback.feedbackId);
     }

     /**
      * Generates a hash code based on the feedbackId.
      */
     @Override
     public int hashCode() {
         return java.util.Objects.hash(feedbackId);
     }
}
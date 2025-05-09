package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a user of the TechTrove system, who can be a customer or an admin.
 * Corresponds to a row in the Users database table.
 */
public class User {
    private String userId;          // Primary key (e.g., USR_XYZ)
    private String fullName;        // User's full name (required)
    private String email;           // User's email address (unique, required, used for login)
    private String password;        // User's password (required, **store hashed in real app**)
    private String phoneNumber;     // Optional phone number
    private String address;         // Optional shipping/billing address
    private UserRole role;          // User's role (USER or ADMIN)
    private LocalDateTime registrationDate; // When the user registered
    private String profilePicture;  // Optional path/URL to profile picture

    /**
     * Defines the possible roles a user can have within the system.
     */
    public enum UserRole {
        USER,  // Regular customer
        ADMIN  // Administrator with elevated privileges
    }

    // Formatter for consistent date/time output
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Constructs a User object.
     *
     * @param userId           The unique ID for the user.
     * @param fullName         The user's full name (required).
     * @param email            The user's email address (required, unique).
     * @param password         The user's password (required, should be hashed).
     * @param phoneNumber      The user's phone number (optional).
     * @param address          The user's address (optional).
     * @param role             The user's role (USER or ADMIN, defaults to USER if null).
     * @param registrationDate The date and time of registration (defaults to now if null).
     * @param profilePicture   The path or URL to the user's profile picture (optional).
     */
    public User(String userId, String fullName, String email, String password, String phoneNumber,
                String address, UserRole role, LocalDateTime registrationDate, String profilePicture) {

        // --- Validation ---
        if (userId == null || userId.trim().isEmpty())
            throw new IllegalArgumentException("User ID cannot be null or empty.");
        if (fullName == null || fullName.trim().isEmpty())
            throw new IllegalArgumentException("Full Name cannot be null or empty.");
        if (email == null || email.trim().isEmpty() || !email.contains("@")) // Basic email format check
            throw new IllegalArgumentException("A valid Email address is required.");
        if (password == null || password.isEmpty()) // Allow spaces in password, but not empty
            throw new IllegalArgumentException("Password cannot be null or empty.");

        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.password = password; // Store plain text for demo; HASH IN PRODUCTION!
        this.phoneNumber = phoneNumber; // Allow null/empty
        this.address = address;       // Allow null/empty
        this.role = (role != null) ? role : UserRole.USER; // Default to USER if null
        this.registrationDate = (registrationDate != null) ? registrationDate : LocalDateTime.now(); // Default to now
        this.profilePicture = profilePicture; // Allow null/empty
    }

    // --- Getters ---
    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    /** WARNING: Retrieving plain text password. Only for demonstration/internal use where necessary. */
    public String getPassword() { return password; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public UserRole getRole() { return role; }
    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public String getProfilePicture() { return profilePicture; }

    // --- Setters ---
    // Setting ID usually restricted. Email uniqueness needs DB check if changed.
    public void setUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) throw new IllegalArgumentException("User ID cannot be empty.");
        this.userId = userId;
    }
    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) throw new IllegalArgumentException("Full Name cannot be empty.");
        this.fullName = fullName;
    }
    public void setEmail(String email) {
         if (email == null || email.trim().isEmpty() || !email.contains("@")) throw new IllegalArgumentException("Valid Email required.");
        this.email = email;
    }
    /** WARNING: Sets plain text password. Hashing should occur before calling this in a real app. */
    public void setPassword(String password) {
        if (password == null || password.isEmpty()) throw new IllegalArgumentException("Password cannot be empty.");
        this.password = password;
    }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setAddress(String address) { this.address = address; }
    public void setRole(UserRole role) { this.role = (role != null) ? role : UserRole.USER; } // Default if null
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    // --- Overrides ---
    @Override
    public String toString() {
        return "User{" +
               "userId='" + userId + '\'' +
               ", fullName='" + fullName + '\'' +
               ", email='" + email + '\'' +
               ", role=" + role +
               ", registered=" + (registrationDate != null ? registrationDate.format(DATE_TIME_FORMATTER) : "N/A") +
               '}';
    }

    /**
     * Checks for equality based on the userId, which should be unique.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    /**
     * Generates a hash code based on the userId.
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
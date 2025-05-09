// src/java/core/PasswordUtil.java
package core;

// import java.security.MessageDigest;
// import java.security.NoSuchAlgorithmException;
// import java.util.Base64;

/**
 * Placeholder for Password Utility.
 * In a real application, use robust hashing algorithms like bcrypt or Argon2.
 * DO NOT USE THIS IN PRODUCTION.
 */
public class PasswordUtil {

    /**
     * Hashes a password.
     * THIS IS A PLACEHOLDER AND NOT SECURE.
     * @param password the plain text password.
     * @return the "hashed" password (currently just returns the plain password).
     */
    public static String hashPassword(String password) {
        // In a real app:
        // try {
        //     MessageDigest md = MessageDigest.getInstance("SHA-256");
        //     byte[] hashedBytes = md.digest(password.getBytes());
        //     return Base64.getEncoder().encodeToString(hashedBytes);
        // } catch (NoSuchAlgorithmException e) {
        //     throw new RuntimeException("Failed to hash password", e);
        // }
        System.err.println("WARNING: Using insecure plain text password storage (PasswordUtil.hashPassword)");
        return password; // NOT SECURE - FOR DEMO ONLY
    }

    /**
     * Checks a plain text password against a stored "hashed" password.
     * THIS IS A PLACEHOLDER AND NOT SECURE.
     * @param plainPassword the plain text password to check.
     * @param hashedPassword the stored "hashed" password.
     * @return true if passwords match, false otherwise.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        // In a real app, you'd re-hash plainPassword and compare with hashedPassword
        // Or use a library-provided comparison function for bcrypt etc.
        System.err.println("WARNING: Using insecure plain text password comparison (PasswordUtil.checkPassword)");
        return hashPassword(plainPassword).equals(hashedPassword); // NOT SECURE - FOR DEMO ONLY
    }
}
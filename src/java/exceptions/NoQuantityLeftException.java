package exceptions;

/**
 * Custom exception thrown when an operation cannot be completed because
 * there is insufficient stock for a requested product quantity.
 */
public class NoQuantityLeftException extends Exception {

    /**
     * Constructs a NoQuantityLeftException with the specified detail message.
     * @param message the detail message (e.g., indicating the product and available stock).
     */
    public NoQuantityLeftException(String message) {
        super(message);
    }

    /**
     * Constructs a NoQuantityLeftException with the specified detail message and cause.
     * @param message the detail message.
     * @param cause the cause (which is saved for later retrieval by the getCause() method).
     */
    public NoQuantityLeftException(String message, Throwable cause) {
        super(message, cause);
    }
}
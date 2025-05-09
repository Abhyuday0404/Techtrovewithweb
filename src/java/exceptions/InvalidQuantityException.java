package exceptions;

/**
 * Custom exception thrown when an invalid quantity (e.g., negative)
 * is provided for an operation, such as setting stock or adding to cart.
 */
public class InvalidQuantityException extends Exception {

    /**
     * Constructs an InvalidQuantityException with the specified detail message.
     * @param message the detail message.
     */
    public InvalidQuantityException(String message) {
        super(message);
    }

    /**
     * Constructs an InvalidQuantityException with the specified detail message and cause.
     * @param message the detail message.
     * @param cause the cause (which is saved for later retrieval by the getCause() method).
     */
    public InvalidQuantityException(String message, Throwable cause) {
        super(message, cause);
    }
}
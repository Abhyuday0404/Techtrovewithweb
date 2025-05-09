package exceptions;

/**
 * Custom exception thrown when an invalid price (e.g., negative)
 * is provided for a product operation.
 */
public class InvalidPriceException extends Exception {

    /**
     * Constructs an InvalidPriceException with the specified detail message.
     * @param message the detail message.
     */
    public InvalidPriceException(String message) {
        super(message);
    }

     /**
     * Constructs an InvalidPriceException with the specified detail message and cause.
     * @param message the detail message.
     * @param cause the cause (which is saved for later retrieval by the getCause() method).
     */
    public InvalidPriceException(String message, Throwable cause) {
        super(message, cause);
    }
}
package exceptions;

/**
 * Custom exception thrown when an operation references a Product ID
 * that does not exist or is invalid in the current context.
 * Renamed from InvalidMedicineIdException.
 */
public class InvalidProductIdException extends Exception {

    /**
     * Constructs an InvalidProductIdException with the specified detail message.
     * @param message the detail message.
     */
    public InvalidProductIdException(String message) {
        super(message);
    }

    /**
     * Constructs an InvalidProductIdException with the specified detail message and cause.
     * @param message the detail message.
     * @param cause the cause (which is saved for later retrieval by the getCause() method).
     */
    public InvalidProductIdException(String message, Throwable cause) {
        super(message, cause);
    }
}
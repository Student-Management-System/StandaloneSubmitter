package de.uni_hildesheim.sse.submitter.svn.hookErrors;

/**
 * An exception signaling that the result messages sent by the hook are invalid, e.g. due to formatting issues.
 * 
 * @author Adam
 */
public class InvalidErrorMessagesException extends Exception {

    private static final long serialVersionUID = 231978897211019333L;

    /**
     * Creates an instance.
     * 
     * @param message A message describing this exception.
     * @param cause Another exception that caused this exception.
     */
    public InvalidErrorMessagesException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates an instance.
     * 
     * @param message A message describing this exception.
     */
    public InvalidErrorMessagesException(String message) {
        super(message);
    }
    
}

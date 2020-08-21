package de.uni_hildesheim.sse.submitter.svn;

/**
 * Handles all kinds of exceptions, which may occur while trying to submit the project.
 * 
 * @author El-Sharkawy
 * 
 */
public class SubmitException extends Exception {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 8455802426802392618L;

    private ErrorType errorCode;
    private String location;

    /**
     * Sole constructor for this class.
     * @param errorCode The kind of error, which occurred.
     * @param location The location, where the error occurred, maybe <code>null</code>
     */
    public SubmitException(ErrorType errorCode, String location) {
        this.errorCode = errorCode;
        this.location = location;
    }

    /**
     * Getter for the {@link ErrorType}.
     * @return The kind of the occurred error.
     */
    public ErrorType getErrorCode() {
        return errorCode;
    }

    /**
     * Getter for the error location.
     * @return The location of the error, for creating sufficient user messages.
     * Maybe <code>null</code> for certain {@link ErrorType}s.
     */
    public String getLocation() {
        return location;
    }
}

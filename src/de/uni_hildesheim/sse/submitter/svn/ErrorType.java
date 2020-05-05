package de.uni_hildesheim.sse.submitter.svn;

/**
 * ErrorTypes for the {@link SubmitException}.
 * @author El-Sharkawy
 *
 */
public enum ErrorType {
    /**
     * Server is not reachable.
     */
    NO_REPOSITORY_FOUND,
    
    /**
     * Could not create a temporary folder for checking out
     * files from the server.
     */
    COULD_NOT_CREATE_TEMP_DIR,
    
    /**
     * Server is reachable but the specified exercise is not found on the server.
     */
    NO_EXERCISE_FOUND,
    
    /**
     * {@link Submitter} is not able to change the SVN statuses inside the temporary folder.
     */
    DO_STATUS_NOT_POSSIBLE,
    
    /**
     * Exercise was found on the server, but the user has no right to submit files into the specified folder.
     */
    CANNOT_COMMIT;

}

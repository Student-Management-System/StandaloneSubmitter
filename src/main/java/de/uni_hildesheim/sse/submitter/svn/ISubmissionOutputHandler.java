package de.uni_hildesheim.sse.submitter.svn;

import de.uni_hildesheim.sse.submitter.svn.hookErrors.ErrorDescription;

/**
 * Caller of the {@link Submitter}.
 * @author El-Sharkawy
 *
 */
public interface ISubmissionOutputHandler {

    /**
     * Shows an info message (e.g. no files has been changed, or submission was successful).
     * @param message The message to display.
     */
    public void showInfoMessage(String message);
    
    /**
     * Shows an error message (for example server is not reachable).
     * @param message The message to display.
     */
    public void showErrorMessage(String message);

    /**
     * Shows an info message (for example SVN HOOK has detected errors and will not accept the submission).
     * @param message The message to display.
     * @param descriptions Errors detected by the SVN HOOK script.
     */
    public void showInfoMessage(String message, ErrorDescription[] descriptions);
    
}

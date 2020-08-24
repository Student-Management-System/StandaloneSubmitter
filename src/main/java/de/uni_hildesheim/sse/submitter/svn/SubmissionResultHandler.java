package de.uni_hildesheim.sse.submitter.svn;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;

import de.uni_hildesheim.sse.submitter.Starter;
import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.settings.ToolSettings;
import de.uni_hildesheim.sse.submitter.svn.hookErrors.ErrorParser;
import de.uni_hildesheim.sse.submitter.svn.hookErrors.InvalidErrorMessagesException;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;

/**
 * Utility class to translate results and exceptions into messages, which are understandable for students / course
 * attendees. ;-)
 * 
 * @author El-Sharkawy
 * 
 */
public class SubmissionResultHandler {
    
    private static final String BLOCKED_BY_PRE_COMMIT_PREFIX
            = "Commit blocked by pre-commit hook (exit code 1) with output:\n";

    private ISubmissionOutputHandler handler;

    /**
     * Sole constructor for this class.
     * 
     * @param handler
     *            A handler for displaying messages to the user.
     */
    public SubmissionResultHandler(ISubmissionOutputHandler handler) {
        this.handler = handler;
    }
    
    /**
     * Getter for the {@link ISubmissionOutputHandler} that this
     * {@link SubmissionResultHandler} uses.
     * @return The {@link ISubmissionOutputHandler}.
     */
    public ISubmissionOutputHandler getHandler() {
        return handler;
    }

    /**
     * Handles exceptions occurred during submitting the homework.
     * 
     * @param commitExc The Exception which was caught.
     * @param exercise The exercise that the submission that created this exception was for. Must be
     *      non-<code>null</code> for error type {@link ErrorType#NO_EXERCISE_FOUND}.
     * @param submissionFolder The folder name on the server where the failing submission should be committed to.
     *      Usually this is the group name for group assignments or the user-name for single assignments.
     *      May be <code>null</code> when not known.
     */
    public void handleCommitException(SubmitException commitExc, Assignment exercise, String submissionFolder) {
        String errorMsg = null;
        if (null != commitExc.getErrorCode()) {
            switch (commitExc.getErrorCode()) {
            case NO_REPOSITORY_FOUND:
                errorMsg = I18nProvider.getText("submission.error.repository_not_found", commitExc.getLocation(),
                        ToolSettings.getConfig().getCourse().getTeamName(), 
                        ToolSettings.getConfig().getCourse().getTeamMail());
                break;
            case COULD_NOT_CREATE_TEMP_DIR:
                if (commitExc.getLocation() != null) {
                    errorMsg = I18nProvider.getText("submission.error.could_not_create_temp_dir", 
                            commitExc.getLocation());
                } else {
                    errorMsg = I18nProvider.getText("submission.error.could_not_create_temp_dir_no_location_available");
                }
                break;
            case NO_EXERCISE_FOUND:
                String userType = exercise.isGroupWork() ? I18nProvider.getText("submission.group")
                    : I18nProvider.getText("submission.user");
                if (submissionFolder == null) {
                    submissionFolder = I18nProvider.getText("errors.stdmanagemt.unreachable");
                }
                // 4 Parameters: Exercise name, user/group, user/group name, upload location 
                errorMsg = I18nProvider.getText("submission.error.exercise_not_found", exercise.getName(), userType,
                    submissionFolder, commitExc.getLocation());
                break;
            case CANNOT_COMMIT:
                errorMsg = I18nProvider.getText("submission.error.cannot_commit", commitExc.getLocation());
                break;
            case DO_STATUS_NOT_POSSIBLE:
                errorMsg = I18nProvider.getText("submission.error.do_status_not_possible", 
                        ToolSettings.getConfig().getCourse().getTeamName(), 
                        ToolSettings.getConfig().getCourse().getTeamMail());
                break;
            default:
                errorMsg = I18nProvider.getText("submission.error.basis", 
                        ToolSettings.getConfig().getCourse().getTeamName(),
                        ToolSettings.getConfig().getCourse().getTeamMail());
                break;
            }
        } else {
            errorMsg = I18nProvider.getText("submission.error.basis", 
                    ToolSettings.getConfig().getCourse().getTeamName(),
                    ToolSettings.getConfig().getCourse().getTeamMail());
        }

        handler.showErrorMessage(errorMsg);
    }

    /**
     * Translates a {@link SVNCommitInfo} into a message, readable by the user.
     * @param info The result of a commit (maybe describing an error).
     */
    public void handleCommitResult(SVNCommitInfo info) {
        SVNErrorMessage errorMsg = info.getErrorMessage();
        if (null == errorMsg) {
            if (-1 == info.getNewRevision()) {
                handler.showInfoMessage(I18nProvider.getText("submission.result.no_changes"));
            } else {
                handler.showInfoMessage(I18nProvider.getText("submission.result.success"));
            }
        } else {
            try {
                ErrorParser parser = new ErrorParser();
                parser.parse(errorsToString(errorMsg));
                String message;
                if (errorMsg.getErrorCode().equals(SVNErrorCode.REPOS_POST_COMMIT_HOOK_FAILED)) {
                    message = I18nProvider.getText("submission.error.errors_found");
                } else {
                    message = I18nProvider.getText("submission.error.project_not_accepted");
                }
                handler.showInfoMessage(message, parser.getErrors());
                
            } catch (InvalidErrorMessagesException e) {
                if (Starter.DEBUG) {
                    e.printStackTrace();
                }
                handler.showErrorMessage(I18nProvider.getText("gui.error.unexpected_error"));
            }
        }
    }

    /**
     * Translates the given {@link SVNErrorMessage} into a XML text.
     * @param errorMsg The {@link SVNErrorMessage} created by receiving the result from the SVN HOOK script.
     * @return The extracted XML text.
     */
    private String errorsToString(SVNErrorMessage errorMsg) {
        String result = "";
        if (errorMsg.getErrorCode().equals(SVNErrorCode.REPOS_POST_COMMIT_HOOK_FAILED)) {
            String messageString = errorMsg.getMessageTemplate();
            int pos = messageString.indexOf('\n');
            if (pos > 0) {
                messageString = messageString.substring(pos);
            }
            result = messageString;
            
        } else {
            do {
                String messageString = errorMsg.getMessageTemplate();
                if (messageString != null && messageString.startsWith(BLOCKED_BY_PRE_COMMIT_PREFIX)) {
                    messageString = messageString.substring(BLOCKED_BY_PRE_COMMIT_PREFIX.length());
                    result = messageString;
                    break;
                }
                errorMsg = errorMsg.getChildErrorMessage();
            } while (errorMsg != null);
        }
        return result;
    }

}

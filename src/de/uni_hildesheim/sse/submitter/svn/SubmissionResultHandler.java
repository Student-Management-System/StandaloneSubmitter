package de.uni_hildesheim.sse.submitter.svn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;

import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.settings.SubmissionConfiguration;
import de.uni_hildesheim.sse.submitter.settings.ToolSettings;
import de.uni_hildesheim.sse.submitter.svn.hookErrors.ErrorParser;
import net.ssehub.exercisesubmitter.protocol.backend.NetworkException;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;

/**
 * Utility class to translate results and exceptions into messages, which are understandable for students / course
 * attendees. ;-)
 * 
 * @author El-Sharkawy
 * 
 */
public class SubmissionResultHandler {
    private static final Logger LOGGER = LogManager.getLogger(SubmissionResultHandler.class);

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
     * @param commitExc
     *            The Exception which was caught.
     */
    public void handleCommitException(SubmitException commitExc) {
        SubmissionConfiguration config = handler.getConfiguration();
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
                Assignment exercise = config.getExercise();
                String userType = exercise.isGroupWork() ? I18nProvider.getText("submission.group")
                    : I18nProvider.getText("submission.user");
                String location;
                try {
                    location = handler.getNetworkProtocol().getPathToSubmission(exercise)[1];
                } catch (NetworkException e) {
                    location = I18nProvider.getText("errors.stdmanagemt.unreachable");
                    LOGGER.warn("Could not query REST server", e);
                }
                // 4 Parameters: Exercise name, user/group, user/group name, upload location 
                errorMsg = I18nProvider.getText("submission.error.exercise_not_found", exercise.getName(), userType,
                    location, commitExc.getLocation());
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
            ErrorParser parser = new ErrorParser(errorsToString(errorMsg));
            String message;
            if (errorMsg.getErrorCode().equals(SVNErrorCode.REPOS_POST_COMMIT_HOOK_FAILED)) {
                message = I18nProvider.getText("submission.error.errors_found");
            } else {
                message = I18nProvider.getText("submission.error.project_not_accepted");
            }
            handler.showInfoMessage(message, parser.getErrors());
        }
    }

    /**
     * Translates the given {@link SVNErrorMessage} into a XML text.
     * @param errorMsg The {@link SVNErrorMessage} created by receiving the result from the SVN HOOK script.
     * @return The extracted XML text.
     */
    private String errorsToString(SVNErrorMessage errorMsg) {
        StringBuffer result = new StringBuffer();
        if (errorMsg.getErrorCode().equals(SVNErrorCode.REPOS_POST_COMMIT_HOOK_FAILED)) {
            String errorMessage = errorMsg.getFullMessage();
            int pos = errorMessage.indexOf('\n');
            if (pos > 0) {
                errorMessage.substring(pos);
            }
            result.append(errorMessage);
        } else {
            while (errorMsg.hasChildErrorMessage()) {
                String error = errorMsg.getMessageTemplate();
                if (!ToolSettings.getConfig().getCommitMessages().getFailed().equals(error)) {
                    error = error.substring(ToolSettings.getConfig().getCommitMessages().getBlocked().length());
                    result.append(error);
                    result.append("\n");
                }
                errorMsg = errorMsg.getChildErrorMessage();
            }
        }
        return result.toString();
    }

}

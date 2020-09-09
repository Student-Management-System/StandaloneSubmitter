package de.uni_hildesheim.sse.submitter.svn;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;

import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.settings.ToolSettings;
import de.uni_hildesheim.sse.submitter.svn.hookErrors.ErrorDescription;
import de.uni_hildesheim.sse.submitter.svn.hookErrors.Severity;
import de.uni_hildesheim.sse.submitter.svn.hookErrors.Tool;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment.State;

public class SubmissionResultHandlerTest implements ISubmissionOutputHandler {

    private List<Message> createdMessages = new LinkedList<>();
    
    @Test
    @DisplayName("success message for commit without errors")
    public void noErrors() {
        SubmissionResultHandler handler = new SubmissionResultHandler(this);
        
        SVNCommitInfo commitInfo = new SVNCommitInfo(123, "me", new Date());
        
        handler.handleCommitResult(commitInfo);
        
        assertEquals(Arrays.asList(
                    new Message("info", I18nProvider.getText("submission.result.success")
                )), createdMessages, "a single success message should have been created");
    }
    
    @Test
    @DisplayName("displays a message if no changes have been commited")
    public void noChanges() {
        SubmissionResultHandler handler = new SubmissionResultHandler(this);
        
        SVNCommitInfo commitInfo = SVNCommitInfo.NULL;
        
        handler.handleCommitResult(commitInfo);
        
        assertEquals(Arrays.asList(
                new Message("info", I18nProvider.getText("submission.result.no_changes"))
            ), createdMessages, "a single message should have been created");
    }
    
    @Test
    @DisplayName("displays an internal error message if a unsuccessful commit has invalid result message")
    public void errorWithInvalidXmlFormat() {
        SubmissionResultHandler handler = new SubmissionResultHandler(this);
        
        SVNErrorMessage error = SVNErrorMessage.create(SVNErrorCode.REPOS_POST_COMMIT_HOOK_FAILED, "This is not expected");
        SVNCommitInfo commitInfo = new SVNCommitInfo(123, "me", new Date(), error);
        
        handler.handleCommitResult(commitInfo);
        
        assertEquals(Arrays.asList(
                new Message("error", I18nProvider.getText("gui.error.unexpected_error"))
            ), createdMessages, "a single message should have been created");
    }
    
    @Test
    @DisplayName("displays an error message for a post-commit hook failure")
    public void postCommitError() {
        SubmissionResultHandler handler = new SubmissionResultHandler(this);
        
        SVNErrorMessage error = SVNErrorMessage.create(SVNErrorCode.REPOS_POST_COMMIT_HOOK_FAILED,
                "Warning: post-commit hook failed (exit code 1) with output:\n"
                + "<submitResults>\n"
                + "    <message tool=\"javac\" type=\"warning\" message=\"something\" />\n"
                + "</submitResults>\n"
        );
        SVNCommitInfo commitInfo = new SVNCommitInfo(123, "me", new Date(), error);
        
        handler.handleCommitResult(commitInfo);
        
        ErrorDescription expectedErrorDescription = new ErrorDescription();
        expectedErrorDescription.setTool(Tool.JAVAC);
        expectedErrorDescription.setSeverity(Severity.WARNING);
        expectedErrorDescription.setMessage("something");
        
        assertEquals(Arrays.asList(
                new Message("info", I18nProvider.getText("submission.error.errors_found"), expectedErrorDescription)
            ), createdMessages, "a single message should have been created");
    }
    
    @Test
    @DisplayName("displays an error message for a pre-commit hook failure")
    public void preCommitError() {
        SubmissionResultHandler handler = new SubmissionResultHandler(this);
        
        SVNErrorMessage error = SVNErrorMessage.create(SVNErrorCode.REPOS_HOOK_FAILURE, "Commit failed (details follow):");
        
        SVNErrorMessage nested1 = SVNErrorMessage.create(SVNErrorCode.REPOS_HOOK_FAILURE,
                "Commit blocked by pre-commit hook (exit code 1) with output:\n"
                + "<submitResults>\n"
                + "    <message tool=\"encoding\" type=\"error\" message=\"invalid encoding\"/>\n"
                + "</submitResults>\n"
        );
        error.setChildErrorMessage(nested1);
        
        SVNErrorMessage nested2 = SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED,
                "{0} of ''{1}'': 500 Internal Server Error ({2})", "MERGE", "/some/path/", "http://some.example/");
        nested1.setChildErrorMessage(nested2);
        
        SVNCommitInfo commitInfo = new SVNCommitInfo(-1, null, null, error);
        
        handler.handleCommitResult(commitInfo);
        
        ErrorDescription expectedErrorDescription = new ErrorDescription();
        expectedErrorDescription.setTool(Tool.ENCODING);
        expectedErrorDescription.setSeverity(Severity.ERROR);
        expectedErrorDescription.setMessage("invalid encoding");
        
        assertEquals(Arrays.asList(
                new Message("info", I18nProvider.getText("submission.error.project_not_accepted"), expectedErrorDescription)
            ), createdMessages, "a single message should have been created");
    }
    
    @Test
    @DisplayName("handles SubmitException with no error code")
    public void exceptionNoErrorCode() {
        SubmissionResultHandler handler = new SubmissionResultHandler(this);
        
        SubmitException exception = new SubmitException(null, null);
        
        handler.handleCommitException(exception, null, null);
        
        assertEquals(Arrays.asList(
                new Message("error", I18nProvider.getText("submission.error.basis", 
                        ToolSettings.getConfig().getCourse().getTeamName(),
                        ToolSettings.getConfig().getCourse().getTeamMail()))
            ), createdMessages, "a single error message should have been created");
    }
    
    @Test
    @DisplayName("handles SubmitException with an NO_REPOSITORY_FOUND error code")
    public void exceptionNoRepositoryFound() {
        SubmissionResultHandler handler = new SubmissionResultHandler(this);
        
        SubmitException exception = new SubmitException(ErrorType.NO_REPOSITORY_FOUND, "some location");
        
        handler.handleCommitException(exception, null, null);
        
        assertEquals(Arrays.asList(
                new Message("error", I18nProvider.getText("submission.error.repository_not_found", "some location",
                        ToolSettings.getConfig().getCourse().getTeamName(), 
                        ToolSettings.getConfig().getCourse().getTeamMail()))
            ), createdMessages, "a single error message should have been created");
    }
    
    @Test
    @DisplayName("handles SubmitException with an COULD_NOT_CREATE_TEMP_DIR error code without a location")
    public void exceptionNoTempdirNoLocation() {
        SubmissionResultHandler handler = new SubmissionResultHandler(this);
        
        SubmitException exception = new SubmitException(ErrorType.COULD_NOT_CREATE_TEMP_DIR, null);
        
        handler.handleCommitException(exception, null, null);
        
        assertEquals(Arrays.asList(
                new Message("error", I18nProvider.getText("submission.error.could_not_create_temp_dir_no_location_available"))
            ), createdMessages, "a single error message should have been created");
    }
    
    @Test
    @DisplayName("handles SubmitException with an COULD_NOT_CREATE_TEMP_DIR error code with a location")
    public void exceptionNoTempdirWithLocation() {
        SubmissionResultHandler handler = new SubmissionResultHandler(this);
        
        SubmitException exception = new SubmitException(ErrorType.COULD_NOT_CREATE_TEMP_DIR, "some/directory");
        
        handler.handleCommitException(exception, null, null);
        
        assertEquals(Arrays.asList(
                new Message("error", I18nProvider.getText("submission.error.could_not_create_temp_dir", "some/directory"))
            ), createdMessages, "a single error message should have been created");
    }
    
    @Test
    @DisplayName("handles SubmitException with an CANNOT_COMMIT error code")
    public void exceptionCannotCommit() {
        SubmissionResultHandler handler = new SubmissionResultHandler(this);
        
        SubmitException exception = new SubmitException(ErrorType.CANNOT_COMMIT, "some/dir2");
        
        handler.handleCommitException(exception, null, null);
        
        assertEquals(Arrays.asList(
                new Message("error", I18nProvider.getText("submission.error.cannot_commit", "some/dir2"))
            ), createdMessages, "a single error message should have been created");
    }
    
    @Test
    @DisplayName("handles SubmitException with an DO_STATUS_NOT_POSSIBLE error code")
    public void exceptionDoStatusNotPossible() {
        SubmissionResultHandler handler = new SubmissionResultHandler(this);
        
        SubmitException exception = new SubmitException(ErrorType.DO_STATUS_NOT_POSSIBLE, null);
        
        handler.handleCommitException(exception, null, null);
        
        assertEquals(Arrays.asList(
                new Message("error", I18nProvider.getText("submission.error.do_status_not_possible", 
                        ToolSettings.getConfig().getCourse().getTeamName(), 
                        ToolSettings.getConfig().getCourse().getTeamMail()))
            ), createdMessages, "a single error message should have been created");
    }
    
    @Test
    @DisplayName("handles SubmitException with an COULD_NOT_QUERY_MANAGEMENT_SYSTEM error code")
    public void exceptionCouldNotQueryManagementSystem() {
        SubmissionResultHandler handler = new SubmissionResultHandler(this);
        
        SubmitException exception = new SubmitException(ErrorType.COULD_NOT_QUERY_MANAGEMENT_SYSTEM, null);
        
        handler.handleCommitException(exception, null, null);
        
        assertEquals(Arrays.asList(
                new Message("error", I18nProvider.getText("submission.error.basis", 
                        ToolSettings.getConfig().getCourse().getTeamName(), 
                        ToolSettings.getConfig().getCourse().getTeamMail()))
                ), createdMessages, "a single error message should have been created");
    }
    
    @Test
    @DisplayName("handles SubmitException with an NO_EXERCISE_FOUND error code for a group assignment")
    public void exceptionNoExerciseFoundForGroup() {
        SubmissionResultHandler handler = new SubmissionResultHandler(this);
        
        SubmitException exception = new SubmitException(ErrorType.NO_EXERCISE_FOUND, "some/local/directory");
        
        handler.handleCommitException(exception, new Assignment("Assignment_name", "Some ID", State.SUBMISSION, true, 0), "some submission");
        
        assertEquals(Arrays.asList(
                new Message("error", I18nProvider.getText("submission.error.exercise_not_found",
                        "Assignment_name", I18nProvider.getText("submission.group"), "some submission", "some/local/directory"))
                ), createdMessages, "a single error message should have been created");
    }
    
    @Test
    @DisplayName("handles SubmitException with an NO_EXERCISE_FOUND error code for a non-group assignment")
    public void exceptionNoExerciseFoundForNonGroup() {
        SubmissionResultHandler handler = new SubmissionResultHandler(this);
        
        SubmitException exception = new SubmitException(ErrorType.NO_EXERCISE_FOUND, "some/local/directory");
        
        handler.handleCommitException(exception, new Assignment("Assignment_name", "Some ID", State.SUBMISSION, false, 0), "some submission");
        
        assertEquals(Arrays.asList(
                new Message("error", I18nProvider.getText("submission.error.exercise_not_found",
                        "Assignment_name", I18nProvider.getText("submission.user"), "some submission", "some/local/directory"))
                ), createdMessages, "a single error message should have been created");
    }
    
    @Test
    @DisplayName("handles SubmitException with an NO_EXERCISE_FOUND error code and no submission folder")
    public void exceptionNoExerciseFoundNoSubmissionFolder() {
        SubmissionResultHandler handler = new SubmissionResultHandler(this);
        
        SubmitException exception = new SubmitException(ErrorType.NO_EXERCISE_FOUND, "some/local/directory");
        
        handler.handleCommitException(exception, new Assignment("Assignment_name", "Some ID", State.SUBMISSION, false, 0), null);
        
        assertEquals(Arrays.asList(
                new Message("error", I18nProvider.getText("submission.error.exercise_not_found",
                        "Assignment_name", I18nProvider.getText("submission.user"), 
                        I18nProvider.getText("errors.stdmanagemt.unreachable"), "some/local/directory"))
                ), createdMessages, "a single error message should have been created");
    }

    @Override
    public void showInfoMessage(String message) {
        createdMessages.add(new Message("info", message));
    }

    @Override
    public void showErrorMessage(String message) {
        createdMessages.add(new Message("error", message));
    }

    @Override
    public void showInfoMessage(String message, ErrorDescription[] descriptions) {
        createdMessages.add(new Message("info", message, descriptions));
    }

    private static class Message {
        
        private String type;
        
        private String message;
        
        private ErrorDescription[] errorDescriptions;

        public Message(String type, String message, ErrorDescription... errorDescriptions) {
            this.type = type;
            this.message = message;
            this.errorDescriptions = errorDescriptions;
        }

        public Message(String type, String message) {
            this.type = type;
            this.message = message;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(errorDescriptions);
            result = prime * result + Objects.hash(message, type);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Message)) {
                return false;
            }
            Message other = (Message) obj;
            return Arrays.equals(errorDescriptions, other.errorDescriptions) && Objects.equals(message, other.message)
                    && Objects.equals(type, other.type);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Message [type=");
            builder.append(type);
            builder.append(", message=");
            builder.append(message);
            builder.append(", errorDescriptions=");
            builder.append(Arrays.toString(errorDescriptions));
            builder.append("]");
            return builder.toString();
        }
        
    }
    
    @BeforeAll
    public static void initToolSettings() {
        assertDoesNotThrow(() -> ToolSettings.INSTANCE.init());
    }
    
}

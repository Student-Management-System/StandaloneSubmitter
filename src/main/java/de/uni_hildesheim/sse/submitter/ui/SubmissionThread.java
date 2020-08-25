package de.uni_hildesheim.sse.submitter.ui;

import java.io.File;
import java.util.Date;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;

import de.uni_hildesheim.sse.submitter.Starter;
import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.settings.SubmissionConfiguration;
import de.uni_hildesheim.sse.submitter.svn.SubmissionResultHandler;
import de.uni_hildesheim.sse.submitter.svn.SubmitException;
import de.uni_hildesheim.sse.submitter.svn.SubmitResult;
import de.uni_hildesheim.sse.submitter.svn.Submitter;
import net.ssehub.exercisesubmitter.protocol.backend.NetworkException;

/**
 * A thread to submit a project.
 * 
 * @author Adam Krafczyk
 */
class SubmissionThread extends Thread {

    private Window parent;
    private SubmissionConfiguration config;
    private File projectFolder;
    
    /**
     * Creates a new SubmissionThread.
     * 
     * @param parent the window that created this thread.
     * @param config the configuration object (for e.g. name and password).
     * @param projectFolder the folder of the project to be submitted.
     */
    SubmissionThread(Window parent, SubmissionConfiguration config, File projectFolder) {
        
        this.parent = parent;
        this.config = config;
        this.projectFolder = projectFolder;
    }
    
    @Override
    public void run() {
        if (Starter.DEBUG_NO_SUBMISSION) {
            createMockErrors();
            
        } else {
            SubmissionResultHandler resultHandler = new SubmissionResultHandler(parent);
            
            try {
                Submitter submitter = Submitter.create(config, parent.getNetworkProtocol());
                SubmitResult result = submitter.submitFolder(projectFolder);
                resultHandler.handleCommitResult(result.getCommitInfo());
                if (result.getNumJavFiles() <= 0) {
                    parent.showErrorMessage(I18nProvider.getText("submission.error.no_java_files"));
                }
            } catch (SubmitException e) {
                String submissionPath = null;
                try {
                    submissionPath = parent.getNetworkProtocol()
                            .getPathToSubmission(config.getExercise()).getSubmissionPath();
                } catch (NetworkException e2) {
                    // ignore
                }
                
                resultHandler.handleCommitException(e, config.getExercise(), submissionPath);
            } finally {
                parent.toggleButtons(true);
            }
        }
    }
    
    /**
     * For testing purposes, creates mock errors and sends them to the {@link #translator}.
     */
    private void createMockErrors() {
        // 
        SVNErrorMessage error = SVNErrorMessage.create(SVNErrorCode.REPOS_POST_COMMIT_HOOK_FAILED,
            "Commit blocked by pre-commit hook (exit code 1) with output:\n"
            + "<submitResults>\n"
            + "    <message tool=\"hook\" type=\"error\" message=\"An internal error occurred\"/>\n"
            + "    <message tool=\"file-size\" type=\"error\" message=\"Submission size is too large\"/>\n"
            + "    <message tool=\"encoding\" type=\"error\" message=\"File has invalid encoding; expected UTF-8\" "
                        + "file=\"SomeFile.java\"/>\n"
            + "    <message tool=\"eclipse-configuration\" type=\"warning\" message=\"Submission does not have "
                        + "Checkstyle enabled\" file=\".project\"/>\n"
            + "    <message tool=\"javac\" type=\"error\" message=\"';' expected\" file=\"SomeSource.java\" "
                        + "line=\"17\">\n"
            + "        <example position=\"67\"/>\n"
            + "    </message>\n"
            + "    <message tool=\"checkstyle\" type=\"error\" message=\"';' preceeded by whitespace\" "
                        + "file=\"SomeSource.java\" line=\"7\">\n"
            + "        <example position=\"54\"/>\n"
            + "    </message>\n"
            + "</submitResults>\n"
        );
        SVNCommitInfo info = new SVNCommitInfo(4, "student1", new Date(), error);
        new SubmissionResultHandler(parent).handleCommitResult(info);
    }
    
}

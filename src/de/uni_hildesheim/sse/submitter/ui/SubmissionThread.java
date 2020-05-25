package de.uni_hildesheim.sse.submitter.ui;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;

import de.uni_hildesheim.sse.submitter.settings.SubmissionConfiguration;
import de.uni_hildesheim.sse.submitter.svn.ISubmissionOutputHandler;
import de.uni_hildesheim.sse.submitter.svn.SubmissionResultHandler;
import de.uni_hildesheim.sse.submitter.svn.SubmitException;
import de.uni_hildesheim.sse.submitter.svn.SubmitResult;
import de.uni_hildesheim.sse.submitter.svn.Submitter;

/**
 * A thread to submit a project.
 * 
 * @author Adam Krafczyk
 */
class SubmissionThread extends Thread {

    private Window parent;
    private SubmissionConfiguration config;
    private SubmissionResultHandler translator;
    private File projectFolder;
    
    /**
     * Creates a new SubmissionThread.
     * 
     * @param parent the window that created this thread.
     * @param config the configuration object (for e.g. name and password).
     * @param translator the translator for results.
     * @param projectFolder the folder of the project to be submitted.
     */
    SubmissionThread(Window parent, SubmissionConfiguration config, SubmissionResultHandler translator,
        File projectFolder) {
        
        this.parent = parent;
        this.config = config;
        this.translator = translator;
        this.projectFolder = projectFolder;
    }
    
    @Override
    public void run() {
        try (Submitter submitter = new Submitter(config, parent.getNetworkProtocol())) {
            SubmitResult result = submitter.submitFolder(projectFolder);
            translator.handleCommitResult(result.getCommitInfo());
            if (result.getNumJavFiles() <= 0) {
                ISubmissionOutputHandler handler = translator.getHandler();
                handler.showErrorMessage("No java files submitted.");
            }
        } catch (IOException e) {
            LogManager.getLogger(SubmissionThread.class).warn("Could not clean up temp folder.", e);
        } catch (SubmitException e) {
            translator.handleCommitException(e);
        } finally {
            parent.toggleButtons(true);
        }
    }
    
}

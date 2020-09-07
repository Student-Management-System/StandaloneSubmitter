package de.uni_hildesheim.sse.submitter.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tmatesoft.svn.core.SVNException;

import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.settings.SubmissionConfiguration;
import de.uni_hildesheim.sse.submitter.settings.ToolSettings;
import de.uni_hildesheim.sse.submitter.svn.ErrorType;
import de.uni_hildesheim.sse.submitter.svn.ISubmissionOutputHandler;
import de.uni_hildesheim.sse.submitter.svn.RemoteRepository;
import de.uni_hildesheim.sse.submitter.svn.Revision;
import de.uni_hildesheim.sse.submitter.svn.ServerNotFoundException;
import de.uni_hildesheim.sse.submitter.svn.SubmissionResultHandler;
import de.uni_hildesheim.sse.submitter.svn.SubmitException;
import de.uni_hildesheim.sse.submitter.svn.SubmitResult;
import de.uni_hildesheim.sse.submitter.svn.Submitter;
import net.ssehub.exercisesubmitter.protocol.backend.NetworkException;
import net.ssehub.exercisesubmitter.protocol.backend.UnknownCredentialsException;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmissionTarget;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmitterProtocol;

/**
 * The main class of the standalone submitter. Stores all information that is specified by the user. The information
 * here is displayed in the {@link StandaloneSubmitterWindow}.
 * 
 * @author Adam
 */
public class StandaloneSubmitter {

    private static final Logger LOGGER = LogManager.getLogger();
    
    private SubmissionConfiguration submissionConfiguration;
    
    private SubmitterProtocol managementSystem;
    
    private RemoteRepository svnRepository;
    
    private boolean loggedIn;
    
    private ISubmissionOutputHandler outputHandler;

    /**
     * Creates a {@link StandaloneSubmitter}.
     * 
     * @param submissionConfiguration The submission configuration with initial values set.
     * @param managementSystem The network connection to the student management system to use.
     */
    public StandaloneSubmitter(SubmissionConfiguration submissionConfiguration,
            SubmitterProtocol managementSystem) {
        this.submissionConfiguration = submissionConfiguration;
        this.managementSystem = managementSystem;
    }
    
    /**
     * Sets an output handler which callbacks are used in other methods of this class. Must be set to something
     * non-<code>null</code> before an output handler is used by any other methods.
     * 
     * @param outputHandler The output handler to use.
     */
    public void setOutputHandler(ISubmissionOutputHandler outputHandler) {
        this.outputHandler = outputHandler;
    }
    
    /**
     * Returns the username for logging into the student management system and the SVN server.
     * 
     * @return The username.
     */
    public String getUser() {
        return this.submissionConfiguration.getUser();
    }
    
    /**
     * Sets the username for logging into the student management system and the SVN server.
     * 
     * @param  user The username.
     */
    public void setUser(String user) {
        this.submissionConfiguration.setUser(user);
    }
    
    /**
     * Sets the password to use for logging into the student management system and the SVN server.
     * 
     * @param password The password to use.
     */
    public void setPassword(char[] password) {
        this.submissionConfiguration.setPW(password);
    }
    
    /**
     * Sets the {@link RemoteRepository} to use in future operations. Must be called with non-<code>null</code>
     * parameter before any method is called that requires a {@link RemoteRepository}.
     * 
     * @param svnRepository The {@link RemoteRepository} to use.
     */
    public void setSvnRepository(RemoteRepository svnRepository) {
        this.svnRepository = svnRepository;
    }
    
    /**
     * Returns the {@link RemoteRepository} that was previously set by either
     * {@link #setSvnRepository(RemoteRepository)} or {@link #createSvnRepository(String)}.
     * <p>
     * Used by test cases.
     * 
     * @return The {@link RemoteRepository}, or <code>null</code> if not yet set.
     */
    RemoteRepository getSvnRepository() {
        return this.svnRepository;
    }
    
    /**
     * Creates a {@link RemoteRepository}, tests the connection, and uses it for future operations. Alternative to
     * {@link #setSvnRepository(RemoteRepository)}.
     * 
     * @param url The URL of the SVN repository.
     * 
     * @return Whether the {@link RemoteRepository} was successfully created and the connection is valid.
     * 
     * @throws ServerNotFoundException When the URL points to an invalid location.
     * 
     * @see #setUser(String)
     * @see #setPassword(char[])
     */
    public boolean createSvnRepository(String url) throws ServerNotFoundException {
        RemoteRepository repository = new RemoteRepository(url,
                this.submissionConfiguration.getUser(), this.submissionConfiguration.getPW());
        boolean success = repository.checkConnection();
        if (success) {
            this.svnRepository = repository;
        }
        return success;
    }
    
    /**
     * Logs into the user management system.
     * 
     * @return Whether the login was successful.
     * 
     * @throws UnknownCredentialsException If the user-name and password are wrong or the user is unknown to the system.
     * @throws net.ssehub.exercisesubmitter.protocol.backend.ServerNotFoundException If the student management system
     *      server is not found.
     * 
     * @see #setUser(String)
     * @see #setPassword(char[])
     */
    public boolean logIntoStudentManagementSystem() throws UnknownCredentialsException,
            net.ssehub.exercisesubmitter.protocol.backend.ServerNotFoundException {
        this.loggedIn = false;
        this.loggedIn = managementSystem.login(this.submissionConfiguration.getUser(),
                new String(this.submissionConfiguration.getPW()));
        return this.loggedIn;
    }
    
    /**
     * Returns whether logged into the student management system.
     * 
     * @return Whether the last login succeeded.
     * 
     * @see #logIntoStudentManagementSystem()
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }
    
    /**
     * Returns the path for the remote SVN repository of the currently selected exercise.
     * 
     * @return The remote path of the current exercise.
     * 
     * @throws NetworkException If the group name could not be queried from the student management system.
     */
    private String getRemotePathOfCurrentExercise() throws NetworkException {
        Assignment currentExercise = submissionConfiguration.getExercise();
        return managementSystem.getPathToSubmission(currentExercise).getAbsolutePathInRepository();
    }
    
    /**
     * Returns a list of {@link Assignment}s that are currently in the reviewed state.
     * 
     * @return List of {@link Assignment}s that were reviewed, or <code>null</code> in case of an error.
     */
    public List<Assignment> getAssignmentsInReviewedState() {
        List<Assignment> result;
        try {
            result = managementSystem.getReviewedAssignments();
        } catch (NetworkException e) {
            LOGGER.error("Could not get assignments in reviewed state", e);
            outputHandler.showErrorMessage(I18nProvider.getText("gui.error.repos_not_found"));
            result = null;
        }
        return result;
    }
    
    /**
     * Returns a list of {@link Assignment}s that are currently in the submission state.
     * 
     * @return List of {@link Assignment}s that are open, or <code>null</code> in case of an error.
     */
    public List<Assignment> getOpenAssignments() {
        List<Assignment> result = null;
        try {
            result = this.managementSystem.getOpenAssignments();
        } catch (NetworkException e) {
            LOGGER.error("Could not get open assignments", e);
            outputHandler.showErrorMessage(I18nProvider.getText("gui.error.repos_not_found"));
            result = null;
        }
        return result;
    }
    
    /**
     * Selects an exercise. This will be used in future operations, e.g. {@link #submit()}.
     * 
     * @param exercise The exercise that is selected by the user.
     */
    public void setSelectedExercise(Assignment exercise) {
        this.submissionConfiguration.setExercise(exercise);
    }
    
    /**
     * Returns the exercise that is selected by the user, i.e. the last value passed to
     * {@link #setSelectedExercise(Assignment)}.
     * 
     * @return The currently selected exercise.
     */
    public Assignment getSelectedExercise() {
        return this.submissionConfiguration.getExercise();
    }
    
    /**
     * Sets the directory that will be submitted or where a replay will be stored.
     * 
     * @param directory The submission directory, typically specified by the user.
     */
    public void setDirectoryToSubmit(File directory) {
        this.submissionConfiguration.setProjectFolder(directory);
    }
    
    /**
     * Returns the directory that will be submitted or where a replay will be stored, i.e. the last value passed to
     * {@link #setDirectoryToSubmit(File)}. May be initialized by a configuration file.
     * 
     * @return The currently selected directory, may be <code>null</code> if no directory has been selected yet.
     */
    public File getDirectoryToSubmit() {
        return this.submissionConfiguration.getProjectFolder();
    }
    
    /**
     * Submits the currently selected directory as the currently selected exercise to the current SVN repository.
     * Notifies the {@link ISubmissionOutputHandler} accordingly.
     * 
     * @see #setSvnRepository(RemoteRepository)
     * @see #setDirectoryToSubmit(File)
     * @see #setSelectedExercise(Assignment)
     * @see #setOutputHandler(ISubmissionOutputHandler)
     */
    public void submit() {
        if (submissionConfiguration.getProjectFolder() == null) {
            outputHandler.showErrorMessage(I18nProvider.getText("gui.error.no_path_given"));
            
        } else if (!submissionConfiguration.getProjectFolder().isDirectory()) {
            outputHandler.showErrorMessage(I18nProvider.getText("errors.messages.not_a_direcotry",
                    submissionConfiguration.getProjectFolder().getAbsolutePath()));
            
        } else {
            SubmissionResultHandler resultHandler = new SubmissionResultHandler(outputHandler);
            
            Assignment exerciseToSubmit = submissionConfiguration.getExercise();
            SubmissionTarget submissionTarget = null;
            
            try {
                submissionTarget = managementSystem.getPathToSubmission(exerciseToSubmit);
                Submitter submitter = new Submitter(submissionTarget.getSubmissionURL(), exerciseToSubmit.getName(),
                        submissionConfiguration.getUser(), submissionConfiguration.getPW());
                SubmitResult result = submitter.submitFolder(submissionConfiguration.getProjectFolder());
                resultHandler.handleCommitResult(result.getCommitInfo());
                if (result.getNumJavFiles() <= 0) {
                    outputHandler.showErrorMessage(I18nProvider.getText("submission.error.no_java_files"));
                }
                
            } catch (NetworkException e) {
                LOGGER.error("Couldn't get submission path", e);
                resultHandler.handleCommitException(
                        new SubmitException(ErrorType.COULD_NOT_QUERY_MANAGEMENT_SYSTEM, exerciseToSubmit.getName()),
                        null, null);
                
            } catch (SubmitException e) {
                String submissionPath = null;
                if (submissionTarget != null) {
                    submissionPath = submissionTarget.getSubmissionPath();
                }
                resultHandler.handleCommitException(e, exerciseToSubmit, submissionPath);
            }
        }
    }
    
    /**
     * Retrieves the revision history of the currently selected exercise. Notifies the {@link ISubmissionOutputHandler}
     * in case of an error.
     * 
     * @return The revision history, or <code>null</code> in case of an error.
     * 
     * @see #setOutputHandler(ISubmissionOutputHandler)
     * @see #setSelectedExercise(Assignment)
     */
    public List<Revision> getHistoryOfCurrentExercise() {
        List<Revision> result;
        try {
            result = svnRepository.getHistory(getRemotePathOfCurrentExercise());
            
        } catch (SVNException | NetworkException e) {
            LOGGER.error("Could not get history", e);
            outputHandler.showErrorMessage(I18nProvider.getText("gui.error.unknown_error"));
            result = null;
        }
        return result;
    }
    
    /**
     * Replays the given revision of the currently selected exercise into the currently selected directory.
     * Notifies the {@link ISubmissionOutputHandler} accordingly.
     * 
     * @param revision The revision number to replay.
     * 
     * @see #setSvnRepository(RemoteRepository)
     * @see #setDirectoryToSubmit(File)
     * @see #setSelectedExercise(Assignment)
     * @see #setOutputHandler(ISubmissionOutputHandler)
     */
    public void replaySubmission(long revision) {
        try {
            svnRepository.replay(revision, getDirectoryToSubmit(), getRemotePathOfCurrentExercise());
            outputHandler.showInfoMessage(I18nProvider.getText("gui.log.replaying_successful"));
        } catch (SVNException | IOException | NetworkException e) {
            LOGGER.error("Could not replay submission from server", e);
            outputHandler.showErrorMessage(I18nProvider.getText("gui.error.replay_error"));
        }
    }
    
    /**
     * Replays the correction (i.e. last revision) of the currently selected exercise into the currently selected
     * directory. Notifies the {@link ISubmissionOutputHandler} accordingly.
     * 
     * @see #setSvnRepository(RemoteRepository)
     * @see #setDirectoryToSubmit(File)
     * @see #setSelectedExercise(Assignment)
     * @see #setOutputHandler(ISubmissionOutputHandler)
     */
    public void replayCorrection() {
        try {
            svnRepository.replay(getDirectoryToSubmit(), getRemotePathOfCurrentExercise());
            
            outputHandler.showInfoMessage(I18nProvider.getText("gui.log.replaying_successful"));
            
        } catch (SVNException e) {
            if (e.getMessage().contains("404 Not Found")) {
                try {
                    SubmissionTarget dest = managementSystem.getPathToSubmission(submissionConfiguration.getExercise());
                    outputHandler.showErrorMessage(I18nProvider.getText("gui.error.replay.no_submission_error",
                            dest.getAssignmentName(), dest.getSubmissionPath(),
                            ToolSettings.getConfig().getCourse().getTeamMail()));
                } catch (NetworkException e1) {
                    LOGGER.error("Could not replay submission from server", e);
                    outputHandler.showErrorMessage(I18nProvider.getText("gui.error.replay_error"));
                }
            } else {
                LOGGER.error("Could not replay submission from server", e);
                outputHandler.showErrorMessage(I18nProvider.getText("gui.error.replay_error"));
            }
        } catch (IOException | NetworkException e) {
            LOGGER.error("Could not replay submission from server", e);
            outputHandler.showErrorMessage(I18nProvider.getText("gui.error.replay_error"));
        }
    }
    
}

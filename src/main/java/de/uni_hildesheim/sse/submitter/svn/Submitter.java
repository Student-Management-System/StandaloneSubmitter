package de.uni_hildesheim.sse.submitter.svn;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.wc.DefaultSVNCommitParameters;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.io.FolderInitializer;
import de.uni_hildesheim.sse.submitter.settings.SubmissionConfiguration;
import net.ssehub.exercisesubmitter.protocol.backend.NetworkException;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmitterProtocol;

/**
 * This class is responsible for submitting the whole project.
 * 
 * @author El-Sharkawy
 * 
 */
public class Submitter {

    private SVNCommitClient client;
    private SVNUpdateClient updateClient;
    private SVNURL url;
    private final SVNClientManager clientManager;
    private String user;
    private String exerciseName;

    /**
     * Creates a submitter with the given parameters.
     * 
     * @param url The URL to submit to. This must already contain the exercise and group elements where to submit.
     * @param exerciseName The name of the exercise that is submitted.
     * @param user The username to use for authentication to the SVN server.
     * @param pw The password to use for authentication to the SVN server.
     * 
     * @throws SubmitException If the given URl is invalid.
     */
    public Submitter(String url, String exerciseName, String user, char[] pw) throws SubmitException {
        this.user = user;
        this.exerciseName = exerciseName;
        
        try {
            this.url = SVNURL.parseURIEncoded(url);
        } catch (SVNException e) {
            throw new SubmitException(ErrorType.NO_REPOSITORY_FOUND, url);
        }
        
        clientManager = SVNClientManager.newInstance(null,
                BasicAuthenticationManager.newInstance(user, pw));
        client = clientManager.getCommitClient();
        client.setCommitParameters(new DefaultSVNCommitParameters() {
            @Override
            public Action onMissingFile(File file) {
                return DELETE;
            }

            @Override
            public Action onMissingDirectory(File file) {
                return DELETE;
            }
        });
        updateClient = clientManager.getUpdateClient();
    }
    
    /**
     * Creates a submitter from the given configuration and student management system connection.
     * <p>
     * TODO: refactor this away.
     * 
     * @param config The configuration to get the username, password, and current {@link Assignment} from.
     * @param protocol The protocol to query the student managemetn system for the group name.
     * 
     * @return A {@link Submitter} for the given configuration.
     * 
     * @throws SubmitException If the student managemen system could not be queried or creating the submitter fails.
     */
    public static Submitter create(SubmissionConfiguration config, SubmitterProtocol protocol) throws SubmitException {
        try {
            String url = protocol.getPathToSubmission(config.getExercise()).getSubmissionURL();
            
            return new Submitter(url, config.getExercise().getName(), config.getUser(), config.getPW());
            
        } catch (NetworkException e1) {
            throw new SubmitException(ErrorType.COULD_NOT_QUERY_MANAGEMENT_SYSTEM, config.getExercise().getName());
        }
    }
    
    /**
     * Submits a user project to the submission server.
     * @param folder A top level folder of a java project, which shall be submitted.
     * @return A Info message about the submission.
     * @throws SubmitException If an error occurred before the the server could run the hook script.
     */
    public SubmitResult submitFolder(File folder) throws SubmitException {
        SVNCommitInfo info = null;
        int numJavaFiles = 0;
        
        File checkoutFolder = null;
        try {
            // Checkout Exercise
            checkoutFolder = checkOut();

            // Prepare Commit
            numJavaFiles = prepareCommit(folder, checkoutFolder);
            try {
                clientManager.getWCClient().doAdd(checkoutFolder, true, false, false, SVNDepth.INFINITY, false, false);
            } catch (SVNException e) {
                throw new SubmitException(ErrorType.DO_STATUS_NOT_POSSIBLE, null);
            }

            // Commit exercise
            String commitMsg = I18nProvider.getText("submission.commit.exercise", user);
            try {
                info = client.doCommit(new File[] {checkoutFolder}, false, commitMsg, null, null, false, false,
                        SVNDepth.INFINITY);
            } catch (SVNAuthenticationException e) {
                throw new SubmitException(ErrorType.CANNOT_COMMIT, url.toString());
            } catch (SVNException e) {
                SVNErrorMessage errorMsg = e.getErrorMessage();
                if (errorMsg.hasChildWithErrorCode(SVNErrorCode.REPOS_HOOK_FAILURE)) {
                    info = new SVNCommitInfo(-1, user, new Date(), errorMsg);
                } else {
                    throw new SubmitException(ErrorType.CANNOT_COMMIT, url.toString());
                }
            }

        // Cleanup
        } finally {
            if (checkoutFolder != null) {
                FileUtils.deleteQuietly(checkoutFolder);
            }
        }

        return new SubmitResult(numJavaFiles, info);
    }

    /**
     * Checkouts the exercise folder from the server.
     * Part of the {@link #submitFolder(File)} method. This is needed by SVN.
     * 
     * @return The location where the SVN working copy was checked out.
     * 
     * @throws SubmitException If an error occurred before the the server could run the hook script.
     */
    private File checkOut() throws SubmitException {
        File checkoutLocation;
        try {
            checkoutLocation = Files.createTempDirectory(null, new FileAttribute<?>[] {}).toFile();
            checkoutLocation.deleteOnExit();
        } catch (IOException e) {
            throw new SubmitException(ErrorType.COULD_NOT_CREATE_TEMP_DIR, System.getProperty("java.io.tmpdir"));
        }
        try {
            updateClient.doCheckout(url, checkoutLocation, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);
        } catch (SVNException e) {
            throw new SubmitException(ErrorType.NO_EXERCISE_FOUND, url.toString());
        }
        
        return checkoutLocation;
    }

    /**
     * Configures the {@link #tempFolder} for submission (copy files and so on).
     * 
     * @param sourceFolder A top level folder of a java project, which shall be submitted.
     * @param targetFolder The target folder where the files should be placed.
     * 
     * @return The number of java files in the sourceFolder.
     * @throws SubmitException If an error occurred before the the server could run the hook script.
     */
    private int prepareCommit(File sourceFolder, File targetFolder) throws SubmitException {
        FolderInitializer initilizer = new FolderInitializer(sourceFolder, targetFolder);
        try {
            initilizer.init(exerciseName);
            return FileUtils.listFiles(sourceFolder, new String[] {"java"}, true).size();
        } catch (IOException e) {
            throw new SubmitException(ErrorType.COULD_NOT_CREATE_TEMP_DIR, System.getProperty("java.io.tmpdir"));
        }
    }

}

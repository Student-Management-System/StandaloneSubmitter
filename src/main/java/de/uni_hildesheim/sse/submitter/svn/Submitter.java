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
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.DefaultSVNCommitParameters;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;

import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.io.FolderInitializer;
import de.uni_hildesheim.sse.submitter.settings.SubmissionConfiguration;
import net.ssehub.exercisesubmitter.protocol.backend.NetworkException;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmitterProtocol;

/**
 * This class is responsible for submitting the whole project.
 * 
 * @author El-Sharkawy
 * 
 */
public class Submitter implements AutoCloseable {

    private SubmissionConfiguration config;
    private File tempFolder;
    private SVNCommitClient client;
    private SVNUpdateClient updateClient;
    private SVNURL url;
    private final SVNClientManager clientManager;
    private SubmitterProtocol protocol;

    /**
     * Sole constructor.
     * @param config The local settings for submitting projects (e.g. user name and password).
     * @param protocol The network protocol for querying the REST server
     * @throws SubmitException If an error occurred before the the server could run the hook script.
     */
    public Submitter(SubmissionConfiguration config, SubmitterProtocol protocol) throws SubmitException {
        this.config = config;
        this.protocol = protocol;
        url = composeTarget(config);
        clientManager = SVNClientManager.newInstance(null, config.getUser(), config.getPW());
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();

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
     * Composes the target URL. 
     * @param config The local settings for submitting projects (e.g. user name and password).
     * @return the target URL as String
     * @throws SubmitException If an error occurred.
     */
    private SVNURL composeTarget(SubmissionConfiguration config) throws SubmitException {
        SVNURL url = null;
        String target = null;
        try {
            target = protocol.getPathToSubmission(config.getExercise()).getSubmissionURL();
            url = SVNURL.parseURIEncoded(target);
        } catch (NetworkException e1) {
            throw new SubmitException(ErrorType.COULD_NOT_QUERY_MANAGEMENT_SYSTEM, config.getExercise().getName());
        } catch (SVNException e) {
            throw new SubmitException(ErrorType.NO_REPOSITORY_FOUND, target);
        }
        
        return url;
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
        try {
            // Checkout Exercise
            checkOut();

            // Prepare Commit
            numJavaFiles = prepareCommit(folder);
            try {
                updateState(tempFolder);
            } catch (SVNException e) {
                throw new SubmitException(ErrorType.DO_STATUS_NOT_POSSIBLE, null);
            }

            // Commit exercise
            String commitMsg = I18nProvider.getText("submission.commit.exercise") + config.getUser();
            try {
                info = client.doCommit(new File[] {tempFolder}, false, commitMsg, null, null, false, false,
                        SVNDepth.INFINITY);
            } catch (SVNAuthenticationException e) {
                throw new SubmitException(ErrorType.CANNOT_COMMIT, url.toString());
            } catch (SVNException e) {
                SVNErrorMessage errorMsg = e.getErrorMessage();
                if (errorMsg.hasChildWithErrorCode(SVNErrorCode.REPOS_HOOK_FAILURE)) {
                    info = new SVNCommitInfo(-1, config.getUser(), new Date(), errorMsg);
                } else {
                    throw new SubmitException(ErrorType.CANNOT_COMMIT, url.toString());
                }
            }

        // Cleanup
        } finally {
            try {
                FileUtils.deleteDirectory(tempFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new SubmitResult(numJavaFiles, info);
    }

    /**
     * Recursive update method to update also sub packages/folders of a submission.
     * Checks for new, deleted, and modified files/folders which must be submitted. 
     * @param folder A folder to check, also all sub folders will be checked. Should be stated with {@link #tempFolder}.
     * @throws SVNException In case of an error (the used library has no description for that).
     */
    private void updateState(File folder) throws SVNException {
        clientManager.getStatusClient().doStatus(folder, SVNRevision.HEAD, SVNDepth.INFINITY, false, false,
                false, false, new SVNStatusHandler(clientManager.getWCClient()), null);
        
        File[] nestedFiles = folder.listFiles();
        if (null != nestedFiles) {
            for (int i = 0; i < nestedFiles.length; i++) {
                File nestedFile = nestedFiles[i];
                if (nestedFile.isDirectory() && !".svn".equalsIgnoreCase(nestedFile.getName())) {
                    updateState(nestedFile);
                }
            }
        }
    }

    /**
     * Checkouts the exercise folder from the server.
     * Part of the {@link #submitFolder(File)} method. This is needed by SVN.
     * @throws SubmitException If an error occurred before the the server could run the hook script.
     */
    private void checkOut() throws SubmitException {
        try {
            tempFolder = Files.createTempDirectory(null, new FileAttribute<?>[] {}).toFile();
            tempFolder.deleteOnExit();
        } catch (IOException e) {
            throw new SubmitException(ErrorType.COULD_NOT_CREATE_TEMP_DIR, System.getProperty("java.io.tmpdir"));
        }
        try {
            updateClient.doCheckout(url, tempFolder, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);
        } catch (SVNException e) {
            throw new SubmitException(ErrorType.NO_EXERCISE_FOUND, url.toString());
        }
    }

    /**
     * Configures the {@link #tempFolder} for submission (copy files and so on).
     * @param sourceFolder A top level folder of a java project, which shall be submitted.
     * @return The number of java files in the sourceFolder.
     * @throws SubmitException If an error occurred before the the server could run the hook script.
     */
    private int prepareCommit(File sourceFolder) throws SubmitException {
        FolderInitializer initilizer = new FolderInitializer(sourceFolder, tempFolder);
        try {
            initilizer.init(config.getExercise().getName());
            return FileUtils.listFiles(sourceFolder, new String[] {"java"}, true).size();
        } catch (IOException e) {
            throw new SubmitException(ErrorType.COULD_NOT_CREATE_TEMP_DIR, System.getProperty("java.io.tmpdir"));
        }
    }

    @Override
    public void close() throws IOException {
        FileUtils.deleteDirectory(tempFolder);
    }
}

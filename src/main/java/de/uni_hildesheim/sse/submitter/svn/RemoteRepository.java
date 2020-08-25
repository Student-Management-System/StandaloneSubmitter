package de.uni_hildesheim.sse.submitter.svn;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

import de.uni_hildesheim.sse.submitter.io.FolderInitializer;
import de.uni_hildesheim.sse.submitter.settings.SubmissionConfiguration;
import de.uni_hildesheim.sse.submitter.settings.ToolSettings;
import net.ssehub.exercisesubmitter.protocol.backend.NetworkException;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmitterProtocol;

/**
 * Contains methods to fetch information from the remote repository.
 * 
 * @author Adam Krafczyk
 * @author El-Sharkawy
 */
public class RemoteRepository implements Closeable {

    public static final String MODE_SUBMISSION = "SUBMISSION";
    public static final String MODE_REPLAY = "REPLAY";
    
    private static final Logger LOGGER = LogManager.getLogger(RemoteRepository.class);
    
    private SubmissionConfiguration config;
    private String target;
    private SVNRepository repository;
    private SVNClientManager clientManager;
    private SubmitterProtocol protocol;
    
    /**
     * Creates a repository object for the server given in configuration.
     * 
     * @param config contains information about server, user, password, etc.
     * @param protocol The network protocol for querying the REST server
     * 
     * @throws ServerNotFoundException when unable to connect to server
     */
    public RemoteRepository(SubmissionConfiguration config, SubmitterProtocol protocol) throws ServerNotFoundException {
        this.config = config;
        this.protocol = protocol;
        
        target = ToolSettings.getConfig().getRepositoryURL();
        try {
            SVNURL url = SVNURL.parseURIEncoded(target);
            repository = SVNRepositoryFactory.create(url);
        } catch (SVNException e) {
            throw new ServerNotFoundException(target);
        }
        repository.setAuthenticationManager(BasicAuthenticationManager.newInstance(config.getUser(), config.getPW()));
        
        clientManager = SVNClientManager.newInstance(null,
                BasicAuthenticationManager.newInstance(config.getUser(), config.getPW()));
    }
    
    /**
     * Returns the history of the repository.
     * @return a list of Strings, where each String represents a single version
     * @throws SVNException if fetching the history information fails
     * @throws IOException if writing the files fails.
     */
    public List<Revision> getHistory() throws SVNException, IOException {
        List<Revision> result = new ArrayList<Revision>();
        
        String[] targetPaths = null;
        try {
            String remotePath = protocol.getPathToSubmission(config.getExercise()).getAbsolutePathInRepository();
            targetPaths = new String[]{remotePath};
        } catch (NetworkException e) {
            throw new IOException(e);
        }
        Collection<?> revisions = repository.log(targetPaths, null, 0, repository.getLatestRevision(), false, false);
        
        for (Object o : revisions) {
            SVNLogEntry logEntry = (SVNLogEntry) o;
            result.add(new Revision(logEntry));
        }
        
        return result;
    }
    
    /**
     * Tests if a connection can be established to the specified repository server, which is used as submission server.
     * Tests that the provided credentials are accepted by the server.
     * @return <code>true</code> if the submission server can be access with the provided credentials,
     *      <code>false</code> otherwise.
     */
    public boolean checkConnection() {
        boolean connected = false;
        
        try {
            repository.testConnection();
            connected = true;
        } catch (SVNException e) {
            LOGGER.warn("Could not connect to sumbission server \"" + ToolSettings.getConfig().getRepositoryURL()
                + "\"", e);
        }
        
        return connected;
    }
    
    /**
     * Get a previous version and save it in the given directory.
     * @param path the path to the directory. Contents will be deleted.
     * @param revision the revision to replay
     * @throws SVNException if unable to get revision.
     * @throws IOException if writing the files fails.
     */
    public void replay(String path, long revision) throws SVNException, IOException {
        File target = new File(path);
        if (!target.exists()) {
            target.mkdirs();
        } else if (target.isDirectory()) {
            rmdir(target, false);
        } else {
            target.delete();
            target.mkdir();
        }
        
        File tmpDir = Files.createTempDirectory("abgabe").toFile();
        try {
            clientManager.getUpdateClient().doCheckout(repository.getLocation(), tmpDir,
                    SVNRevision.create(revision), SVNRevision.create(revision), SVNDepth.INFINITY, true);
            tmpDir = new File(tmpDir, config.getExercise().getName());
            String subFolderName = tmpDir.listFiles()[0].getName();
            tmpDir = new File(tmpDir, subFolderName);
            if (!tmpDir.isDirectory()) {
                throw new IOException("Temp-directory has wrong structure"); // TODO
            }
            recursiveCopy(tmpDir, target);
        } finally {
            rmdir(tmpDir, true);
        }
    }
    
    /**
     * Get the current revision of the exercise selected in the {@link SubmissionConfiguration}
     * and save it in the given directory.
     * @param path The path to the directory. Contents will be deleted.
     * @throws SVNException if unable to get the current revision.
     * @throws IOException if writing the files fails.
     */
    public void replay(String path) throws SVNException, IOException {
        long latestRevision = 0;
        
        String[] targetPaths = null;
        try {
            String remotePath = protocol.getPathToSubmission(config.getExercise()).getAbsolutePathInRepository();
            targetPaths = new String[]{remotePath};
        } catch (NetworkException e) {
            throw new IOException(e);
        }
        
        Collection<?> revisions = repository.log(targetPaths, null, 0, repository.getLatestRevision(), false, false);
        
        for (Object o : revisions) {
            SVNLogEntry logEntry = (SVNLogEntry) o;
            if (logEntry.getRevision() > latestRevision) {
                latestRevision = logEntry.getRevision();
            }
        }
        
        replay(path, latestRevision);
    }
    
    /**
     * Deletes a given directory and all contained files and directories.
     * 
     * @param dir the directory to be deleted
     * @param self <code>true</code> if also <code>dir</code> should 
     *        be deleted, or only the contents (<code>false</code>)
     *
     * @since 1.00
     */
    private static void rmdir(File dir, boolean self) {
        File[] filelist = dir.listFiles();
        if (null != filelist) {
            for (File file : filelist) {
                if (file.isFile()) {
                    file.delete();
                }
                if (file.isDirectory()) {
                    rmdir(file.getAbsoluteFile(), true);
                }
            }
        }
        if (self) {
            dir.delete();
        }
    }
    
    /**
     * Recursively copy contents of the source folder to the destination.
     * Ignores CLASSPATH_FILE_NAME and PROJECT_FILE_NAME from {@link FolderInitializer} and .settings.
     * @param src the source directory
     * @param dst the destination directory
     * @throws IOException when copying fails
     */
    private static void recursiveCopy(File src, File dst) throws IOException {
        for (File file : src.listFiles()) {
            if (file.getName().equals(FolderInitializer.CLASSPATH_FILE_NAME)
                    || file.getName().equals(FolderInitializer.PROJECT_FILE_NAME)
                    || file.getName().equals(".settings")) {
                continue;
            }
            
            Files.copy(file.toPath(), dst.toPath().resolve(file.getName()));
            if (file.isDirectory()) {
                recursiveCopy(file, new File(dst, file.getName()));
            }
        }
    }
    
    @Override
    public void close() {
        repository.closeSession();
    }
    
}

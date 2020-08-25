package de.uni_hildesheim.sse.submitter.svn;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

import de.uni_hildesheim.sse.submitter.io.FolderInitializer;

/**
 * Contains methods to fetch information from the remote repository.
 * 
 * @author Adam Krafczyk
 * @author El-Sharkawy
 */
public class RemoteRepository {

    private static final Logger LOGGER = LogManager.getLogger();
    
    private SVNURL svnUrl;
    private SVNClientManager clientManager;
    
    /**
     * Creates a {@link RemoteRepository} for the given server with the given credentials.
     * 
     * @param url The URL of the SVN server.
     * @param user The username to use.
     * @param password The password to use.
     * 
     * @throws ServerNotFoundException If the URL is malformed.
     */
    public RemoteRepository(String url, String user, char[] password) throws ServerNotFoundException {
        try {
            svnUrl = SVNURL.parseURIEncoded(url);
        } catch (SVNException e) {
            LOGGER.error("Couldn't parse URL: " + url, e);
            throw new ServerNotFoundException(url);
        }
        
        clientManager = SVNClientManager.newInstance(null,
                BasicAuthenticationManager.newInstance(user, password));
    }
    
    /**
     * Returns the history of the repository.
     * 
     * @param remotePath The path on the server to get the history for.
     * 
     * @return a list of Strings, where each String represents a single version
     * 
     * @throws SVNException if fetching the history information fails
     */
    public List<Revision> getHistory(String remotePath) throws SVNException {
        List<Revision> result = new ArrayList<Revision>();
        
        clientManager.getLogClient().doLog(svnUrl, new String[] {remotePath},
                SVNRevision.HEAD, SVNRevision.create(0), SVNRevision.HEAD, false, false, Integer.MAX_VALUE,
            (logEntry) -> {
                result.add(new Revision(logEntry));
            });
        
        return result;
    }
    
    /**
     * Tests if a connection can be established to the specified repository server, which is used as submission server.
     * Tests that the provided credentials are accepted by the server.
     * 
     * @return <code>true</code> if the submission server can be access with the provided credentials,
     *      <code>false</code> otherwise.
     */
    public boolean checkConnection() {
        boolean connected = false;
        
        SVNRepository repository = null;
        try {
            repository = clientManager.createRepository(svnUrl, false);
            repository.testConnection();
            connected = true;
        } catch (SVNException e) {
            LOGGER.error("Could not connect to sumbission server: " + svnUrl, e);
        } finally {
            if (repository != null) {
                repository.closeSession();
            }
        }
        
        return connected;
    }
    
    /**
     * Get the given revision of the given exercise directory and save it in the given directory.
     * 
     * @param revision The revision to check out.
     * @param targetDirectory the path to the directory where to create the checkout. Contents will be deleted.
     * @param remotePath The path of the submission to replay.
     * 
     * @throws SVNException if unable to get the current revision.
     * @throws IOException if writing the files fails.
     */
    public void replay(long revision, File targetDirectory, String remotePath) throws SVNException, IOException {
        if (!targetDirectory.exists()) {
            if (!targetDirectory.mkdirs()) {
                throw new IOException("Cannot create " + targetDirectory);
            }
        } else if (targetDirectory.isDirectory()) {
            FileUtils.cleanDirectory(targetDirectory);
        } else {
            throw new IOException(targetDirectory + " is a file");
        }
        
        File tmpDir = Files.createTempDirectory("abgabe").toFile();
        try {
            SVNURL url = SVNURL.parseURIEncoded(svnUrl.toString() + "/" + remotePath);
            clientManager.getUpdateClient().doCheckout(url, tmpDir,
                    SVNRevision.create(revision), SVNRevision.create(revision), SVNDepth.INFINITY, true);
            
            FileUtils.copyDirectory(tmpDir, targetDirectory, (file) ->
                    !file.getName().equals(FolderInitializer.CLASSPATH_FILE_NAME)
                    && !file.getName().equals(FolderInitializer.PROJECT_FILE_NAME)
                    && !file.getName().equals(".settings")
                    && !file.getName().equals(".svn")
            );
            
        } finally {
            FileUtils.deleteQuietly(tmpDir);
        }
    }
    
    /**
     * Get the current revision of the given exercise directory and save it in the given directory.
     * 
     * @param targetDirectory the path to the directory where to create the checkout. Contents will be deleted.
     * @param remotePath The path of the submission to replay.
     * 
     * @throws SVNException if unable to get the current revision.
     * @throws IOException if writing the files fails.
     */
    public void replay(File targetDirectory, String remotePath) throws SVNException, IOException {
        AtomicLong latestRevision = new AtomicLong();
        
        clientManager.getLogClient().doLog(svnUrl, new String[] {remotePath},
                SVNRevision.HEAD, SVNRevision.HEAD, SVNRevision.HEAD, false, false, Integer.MAX_VALUE,
            (logEntry) -> {
                latestRevision.set(logEntry.getRevision());
            });
        
        replay(latestRevision.get(), targetDirectory, remotePath);
    }
    
}

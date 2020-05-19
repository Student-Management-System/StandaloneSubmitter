package de.uni_hildesheim.sse.submitter.svn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

import de.uni_hildesheim.sse.submitter.conf.Configuration;
import de.uni_hildesheim.sse.submitter.io.FolderInitilizer;
import de.uni_hildesheim.sse.submitter.settings.ToolSettings;

/**
 * Contains methods to fetch information from the remote repository.
 * 
 * @author Adam Krafczyk
 */
public class RemoteRepository {

    public static final String MODE_SUBMISSION = "SUBMISSION";
    public static final String MODE_REPLAY = "REPLAY";
    
    private Configuration config;
    private String target;
    private SVNRepository repository;
    
    /**
     * Creates a repository object for the server given in configuration.
     * 
     * @param config contains information about server, user, password, etc.
     * 
     * @throws ServerNotFoundException when unable to connect to server
     */
    public RemoteRepository(Configuration config) throws ServerNotFoundException {
        this.config = config;
        
        target = ToolSettings.getConfig().getRepositoryURL();
        try {
            SVNURL url = SVNURL.parseURIEncoded(target);
            repository = SVNRepositoryFactory.create(url);
        } catch (SVNException e) {
            throw new ServerNotFoundException(target);
        }
        repository.setAuthenticationManager(
            new DefaultSVNAuthenticationManager(null, true, config.getUser(), config.getPW())
        );
    }
    
    /**
     * Gets all repositories that the user can commit to.
     * 
     * @param wantedMode The mode of the repo.
     * @return list of repository names
     * 
     * @throws SVNAuthenticationException when the login details are wrong
     * @throws ServerNotFoundException when unable to connect to the server
     */
    public List<String> getRepositories(String wantedMode)
        throws SVNAuthenticationException, ServerNotFoundException {
        
        List<String> result = new ArrayList<String>();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            repository.getFile("permissions", -1, null, out);
            LineNumberReader in = new LineNumberReader(
                new InputStreamReader(new ByteArrayInputStream(
                    out.toByteArray())));

            String line;
            do {
                line = in.readLine();
                if (null != line) {
                    StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                    if (2 == tokenizer.countTokens()) {
                        String path = tokenizer.nextToken();
                        String mode = tokenizer.nextToken();
                        if (path.length() > 0 && mode.length() > 0) {
                            if (path.startsWith("/")) {
                                path = path.substring(1);
                            }
                            if (mode.equalsIgnoreCase(wantedMode)) {
                                result.add(path);
                            }
                        }
                    }
                }
            } while (null != line && in.ready());
            in.close();
            out.close();
        } catch (SVNAuthenticationException e) {
            throw e;
        } catch (SVNException e) {
            if (e.getCause() instanceof UnknownHostException) {
                throw new ServerNotFoundException(target);
            }
            JOptionPane.showMessageDialog(null, e.getMessage());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return result;
    }
    
    /**
     * Returns the history of the repository.
     * @return a list of Strings, where each String represents a single version
     * @throws SVNException if fetching the history information fails
     */
    public List<Revision> getHistory() throws SVNException {
        List<Revision> result = new ArrayList<Revision>();
        
        String[] targetPaths = {
                config.getExercise() + "/" + config.getGroup()
        };
        Collection<?> revisions = repository.log(targetPaths, null, 0,
                    repository.getLatestRevision(), false, false);
        
        for (Object o : revisions) {
            SVNLogEntry logEntry = (SVNLogEntry) o;
            result.add(new Revision(logEntry));
        }
        
        return result;
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
            repository.checkout(revision, null, true, new ExportEditor(tmpDir));
            tmpDir = new File(tmpDir, config.getExercise());
            tmpDir = new File(tmpDir, config.getGroup());
            if (!tmpDir.isDirectory()) {
                throw new IOException("Temp-directory has wrong structure"); // TODO
            }
            recursiveCopy(tmpDir, target);
        } finally {
            rmdir(tmpDir, true);
        }
    }
    
    /**
     * Get the current revision of the exercise selected in the {@link Configuration}
     * and save it in the given directory.
     * @param path The path to the directory. Contents will be deleted.
     * @throws SVNException if unable to get the current revision.
     * @throws IOException if writing the files fails.
     */
    public void replay(String path) throws SVNException, IOException {
        long latestRevision = 0;
        
        String[] targetPaths = {
                config.getExercise() + "/" + config.getGroup()
        };
        Collection<?> revisions = repository.log(targetPaths, null, 0,
                    repository.getLatestRevision(), false, false);
        
        for (Object o : revisions) {
            SVNLogEntry logEntry = (SVNLogEntry) o;
            if (logEntry.getRevision() > latestRevision) {
                latestRevision = logEntry.getRevision();
            }
        }
        
        replay(path, latestRevision);
    }
    
    @Override
    public void finalize() throws Throwable {
        repository.closeSession();
        super.finalize();
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
     * Ignores CLASSPATH_FILE_NAME and PROJECT_FILE_NAME from {@link FolderInitilizer} and .settings.
     * @param src the source directory
     * @param dst the destination directory
     * @throws IOException when copying fails
     */
    private static void recursiveCopy(File src, File dst) throws IOException {
        for (File file : src.listFiles()) {
            if (file.getName().equals(FolderInitilizer.CLASSPATH_FILE_NAME)
                    || file.getName().equals(FolderInitilizer.PROJECT_FILE_NAME)
                    || file.getName().equals(".settings")) {
                continue;
            }
            
            Files.copy(file.toPath(), dst.toPath().resolve(file.getName()));
            if (file.isDirectory()) {
                recursiveCopy(file, new File(dst, file.getName()));
            }
        }
    }
    
}

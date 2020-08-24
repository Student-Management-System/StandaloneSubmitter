package de.uni_hildesheim.sse.submitter.svn;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import de.uni_hildesheim.sse.submitter.settings.SubmissionConfiguration;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment.State;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmitterProtocol;

/**
 * Integration tests for {@link Submitter}. Tests committing to temporary local SVN repositories.
 * 
 * @author Adam
 */
public class SubmitterIT {
    
    private Set<File> temporaryDirectories = new HashSet<>();

    @Test
    @DisplayName("throws if SVN repository doesnt exist")
    public void repoDoesntExist() throws SubmitException, IOException {
        File svnFolder = createTemporaryDirectory();
        
        SubmissionConfiguration config = new SubmissionConfiguration("someStudent", "123456".toCharArray(),
                new Assignment("Homework04", "Homework04", State.SUBMISSION, true));
        
        SubmitterProtocol protocol = new TestSubmitterProtocol(null, null, null, svnFolder.toURI().toString());
        
        assertThrows(SubmitException.class,
                () -> new Submitter(config, protocol)
        );
    }
    
    @Test
    @DisplayName("commit without problems")
    public void commitNoProblems() throws SubmitException, IOException, SVNException {
        File svnFolder = createTemporaryDirectory();
        setupSvnRepoForSubmission(svnFolder, "Homework04", "JP001");
        
        SubmissionConfiguration config = new SubmissionConfiguration("someStudent", "123456".toCharArray(),
                new Assignment("Homework04", "Homework04", State.SUBMISSION, true));
        
        SubmitterProtocol protocol = new TestSubmitterProtocol(null, null, null, "file:///" + svnFolder.getAbsolutePath());
        
        String fileContent = "public class Main {\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"Hello World!\");\n"
                + "    }\n"
                + "}\n";
        
        File submissionFolder = createTemporaryDirectory();
        File javaFile = new File(submissionFolder, "Main.java");
        FileUtils.write(javaFile, fileContent);
        
        assertFileNotInRepository(svnFolder, "Homework04/JP001/Main.java");
        
        Submitter submitter = new Submitter(config, protocol);
        SubmitResult result = submitter.submitFolder(submissionFolder);
        submitter.close();
        
        assertAll(
                () -> assertEquals(1, result.getNumJavFiles(), "number of submitted Java files should be correct"),
                () -> assertEquals(2, result.getCommitInfo().getNewRevision(), "revision should be correct"),
                () -> assertNull(result.getCommitInfo().getErrorMessage(), "no error messages expected"),
                () -> assertFileInRepository(svnFolder, "Homework04/JP001/Main.java", fileContent),
                () -> assertFileInRepository(svnFolder, "Homework04/JP001/.classpath", null),
                () -> assertFileInRepository(svnFolder, "Homework04/JP001/.project", null)
        );
    }
    
    private void setupSvnRepoForSubmission(File svnFolder, String exercise, String group) throws SVNException {
        SVNURL svnUrl = SVNRepositoryFactory.createLocalRepository(svnFolder, true, false);
        SVNClientManager clientManager = SVNClientManager.newInstance();
        
        File checkout = createTemporaryDirectory();
        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        updateClient.doCheckout(svnUrl, checkout, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, false);
        
        File submissionFolder = new File(checkout, exercise + "/" + group);
        submissionFolder.mkdirs();
        
        SVNWCClient wcClient = clientManager.getWCClient();
        wcClient.doAdd(submissionFolder, false, false, true, SVNDepth.INFINITY, false, true);
        
        SVNCommitClient commitClient = clientManager.getCommitClient();
        
        SVNCommitInfo result = commitClient.doCommit(new File[] {submissionFolder.getParentFile(), submissionFolder}, false,
                "Initialize submission folder", null, null, false, false, SVNDepth.INFINITY);
        assertNull(result.getErrorMessage(), "Precondition: commit should have succeeded");
        assertEquals(1, result.getNewRevision(), "Precondition: repository should be at revision 1");
    }
    
    private void assertFileInRepository(File svnFolder, String fileInRepo, String expectedContent) throws SVNException, IOException {
        SVNURL svnUrl = SVNURL.fromFile(svnFolder);
        SVNClientManager clientManager = SVNClientManager.newInstance();
        
        File checkout = createTemporaryDirectory();
        
        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        updateClient.doCheckout(svnUrl, checkout, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, false);
        
        File targetFile = new File(checkout, fileInRepo);
        
        assertTrue(targetFile.isFile(), "file " + fileInRepo + " should exist in checkout");
        if (expectedContent != null) {
            assertEquals(expectedContent, FileUtils.readFileToString(targetFile), "file in repo should have correct content");
        }
    }
    
    private void assertFileNotInRepository(File svnFolder, String fileInRepo) throws SVNException, IOException {
        SVNURL svnUrl = SVNURL.fromFile(svnFolder);
        SVNClientManager clientManager = SVNClientManager.newInstance();
        
        File checkout = createTemporaryDirectory();
        
        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        updateClient.doCheckout(svnUrl, checkout, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, false);
        
        File targetFile = new File(checkout, fileInRepo);
        
        assertFalse(targetFile.exists(), "file " + fileInRepo + " should not exist in checkout");
    }
    
    private File createTemporaryDirectory() {
        return assertDoesNotThrow(() -> {
            
            File tempfile = File.createTempFile("SubmitterIT", null);
            tempfile.delete();
            
            File tempdir = tempfile;
            tempdir.mkdir();
            
            assertTrue(tempdir.isDirectory(), "Precondition: temporary directory is created");
            
            temporaryDirectories.add(tempdir);
            
            return tempdir;
        });
    }
    
    @AfterEach
    public void cleanupTemporaryDirectories() {
        for (File directory : temporaryDirectories) {
            try {
                FileUtils.deleteDirectory(directory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}

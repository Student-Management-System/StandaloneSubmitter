package de.uni_hildesheim.sse.submitter.svn;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tmatesoft.svn.core.SVNException;

public class RemoteRepositoryTest {

    private static final File TESTDATA = new File("src/test/resources");
    
    private static final File TEST_SVN_REPO = new File(TESTDATA, "svnRepo");
    
    private static final String SVN_USER = "student1";
    
    private static final char[] SVN_PW = "123456".toCharArray();
    
    private Set<File> temporaryDirectories = new HashSet<>();
    
    @Test
    @DisplayName("constructor throws for invalid URL")
    public void invalidUrl() {
        assertThrows(ServerNotFoundException.class,
            () -> new RemoteRepository("this is:broken", SVN_USER, SVN_PW));
    }
    
    @Test
    @DisplayName("check connection returns true for a valid SVN repository")
    public void checkConnectionSuccessful() {
        RemoteRepository repository = assertDoesNotThrow(() ->
            new RemoteRepository("file:///" + TEST_SVN_REPO.getAbsolutePath(), SVN_USER, SVN_PW));
        
        assertTrue(repository.checkConnection(), "checkConnection() should succeed");
    }
    
    @Test
    @DisplayName("check connection returns false for an invalid SVN repository")
    public void checkConnectionNotSuccessful() {
        RemoteRepository repository = assertDoesNotThrow(() ->
            new RemoteRepository("file:///" + new File(TESTDATA, "doesnt_exist").getAbsolutePath(), SVN_USER, SVN_PW));
        
        assertFalse(repository.checkConnection(), "checkConnection() should not succeed");
    }
    
    @Test
    @DisplayName("getHistory() returns correct history")
    public void getHistory() {
        RemoteRepository repository = assertDoesNotThrow(() ->
            new RemoteRepository("file:///" + TEST_SVN_REPO.getAbsolutePath(), SVN_USER, SVN_PW));
        
        List<Revision> history = assertDoesNotThrow(() -> repository.getHistory("Homework01/Group02"));
        
        assertEquals(Arrays.asList(
                new Revision(1, "2020-08-25 10:08 (1): Init structure (by www-data)"),
                new Revision(2, "2020-08-25 10:08 (2): Submit my homework (by student1)"),
                new Revision(3, "2020-08-25 11:08 (3): Small fixes (by student1)")
            ), history);
    }
    
    @Test
    @DisplayName("getHistory() throws for path that doesn't exist")
    public void getHistoryInexistantPath() {
        RemoteRepository repository = assertDoesNotThrow(() ->
            new RemoteRepository("file:///" + TEST_SVN_REPO.getAbsolutePath(), SVN_USER, SVN_PW));
        
        assertThrows(SVNException.class, () -> repository.getHistory("Homework01/doesnt_exist"));
    }
    
    @Test
    @DisplayName("replay latest submission")
    public void replayLatest() {
        RemoteRepository repository = assertDoesNotThrow(() ->
            new RemoteRepository("file:///" + TEST_SVN_REPO.getAbsolutePath(), SVN_USER, SVN_PW));
        
        File replayLocation = createTemporaryDirectory();
        
        assertDoesNotThrow(() -> repository.replay(replayLocation, "Homework01/Group02"));
        
        File file1 = new File(replayLocation, "submitted_file.txt");
        File file2 = new File(replayLocation, "second_submitted_file.txt");
        
        assertAll(
            () -> assertEquals(2, replayLocation.listFiles().length, "target should have only 2 files"),
            () -> assertTrue(file1.isFile(), "target should have submitted_file.txt"),
            () -> assertTrue(file2.isFile(), "target should have second_submitted_file.txt"),
            
            () -> assertEquals("file changed.\n", FileUtils.readFileToString(file1), "submitted_file.txt contains correct content"),
            () -> assertEquals("a second file.\n", FileUtils.readFileToString(file2), "second_submitted_file.txt contains correct content")
        );
    }
    
    @Test
    @DisplayName("replay latest submission in non-existant target directory")
    public void replayTargetDirectoryDoesntExist() {
        RemoteRepository repository = assertDoesNotThrow(() ->
            new RemoteRepository("file:///" + TEST_SVN_REPO.getAbsolutePath(), SVN_USER, SVN_PW));
        
        File replayLocation = createTemporaryDirectory();
        replayLocation.delete();
        assertFalse(replayLocation.exists(), "Precondition: replay location does not exist");
        
        assertDoesNotThrow(() -> repository.replay(replayLocation, "Homework01/Group02"));
        
        File file1 = new File(replayLocation, "submitted_file.txt");
        File file2 = new File(replayLocation, "second_submitted_file.txt");
        
        assertAll(
            () -> assertEquals(2, replayLocation.listFiles().length, "target should have only 2 files"),
            () -> assertTrue(file1.isFile(), "target should have submitted_file.txt"),
            () -> assertTrue(file2.isFile(), "target should have second_submitted_file.txt"),
            
            () -> assertEquals("file changed.\n", FileUtils.readFileToString(file1), "submitted_file.txt contains correct content"),
            () -> assertEquals("a second file.\n", FileUtils.readFileToString(file2), "second_submitted_file.txt contains correct content")
        );
    }
    
    @Test
    @DisplayName("replay latest submission in non-empty target directory")
    public void replayTargetDirectoryNotEmpty() throws IOException {
        RemoteRepository repository = assertDoesNotThrow(() ->
            new RemoteRepository("file:///" + TEST_SVN_REPO.getAbsolutePath(), SVN_USER, SVN_PW));
        
        File replayLocation = createTemporaryDirectory();
        File preExisting = new File(replayLocation, "before.txt");
        preExisting.createNewFile();
        assertTrue(preExisting.isFile(), "Precondition: pre-existing file should exist");
        
        assertDoesNotThrow(() -> repository.replay(replayLocation, "Homework01/Group02"));
        
        File file1 = new File(replayLocation, "submitted_file.txt");
        File file2 = new File(replayLocation, "second_submitted_file.txt");
        
        assertAll(
            () -> assertEquals(2, replayLocation.listFiles().length, "target should have only 2 files"),
            () -> assertTrue(file1.isFile(), "target should have submitted_file.txt"),
            () -> assertTrue(file2.isFile(), "target should have second_submitted_file.txt"),
            
            () -> assertFalse(preExisting.exists(), "pre-existing file should not exist anymore"),
            
            () -> assertEquals("file changed.\n", FileUtils.readFileToString(file1), "submitted_file.txt contains correct content"),
            () -> assertEquals("a second file.\n", FileUtils.readFileToString(file2), "second_submitted_file.txt contains correct content")
        );
    }
    
    @Test
    @DisplayName("replay throws if target directory is a file")
    public void replayTargetDirectoryIsFile() throws IOException {
        RemoteRepository repository = assertDoesNotThrow(() ->
            new RemoteRepository("file:///" + TEST_SVN_REPO.getAbsolutePath(), SVN_USER, SVN_PW));
        
        File replayLocation = File.createTempFile("RemoteRepositoryTest", null);
        replayLocation.deleteOnExit();
        assertTrue(replayLocation.isFile(), "Precondition: target location is a file");
        
        assertThrows(IOException.class, () -> repository.replay(replayLocation, "Homework01/Group02"));
        
        replayLocation.delete();
    }
    
    private File createTemporaryDirectory() {
        return assertDoesNotThrow(() -> {
            
            File tempfile = File.createTempFile("RemoteRepositoryTest", null);
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

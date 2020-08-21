package de.uni_hildesheim.sse.submitter.io;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class FolderInitializerTest {

    private Set<File> temporaryDirectories = new HashSet<>();
    
    @Test
    @DisplayName(".classpath and .project are added for empty source directory")
    public void emptySource() throws IOException {
        File source = createTemporaryDirectory();
        File target = createTemporaryDirectory();
        
        assertAll(
            () -> assertEquals(0, source.listFiles().length, "Precondition: source should be empty"),
            () -> assertEquals(0, target.listFiles().length, "Precondition: target should be empty")
        );
        
        FolderInitializer initializer = new FolderInitializer(source, target);
        assertDoesNotThrow(() -> initializer.init("Something"));
        
        File projectFile = new File(target, ".project");
        File classpathFile = new File(target, ".classpath");
        
        assertAll(
            () -> assertEquals(0, source.listFiles().length, "source directory should remain empty"),
            () -> assertEquals(2, target.listFiles().length, "target should contain two files"),
            () -> assertTrue(classpathFile.isFile(), "target should contain .classpath file"),
            () -> assertTrue(projectFile.isFile(), "target should contain .project file")
        );
        
        String[] classpathContent = FileUtils.readFileToString(classpathFile).split("\n");
        String[] projectContent = FileUtils.readFileToString(projectFile).split("\n");
        
        assertAll(
            () -> assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", classpathContent[0], ".classpath file should have correct content"),
            () -> assertEquals("<classpath>", classpathContent[1], ".classpath file should have correct content"),
            () -> assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", projectContent[0], ".project file should have correct content"),
            () -> assertEquals("<projectDescription>", projectContent[1], ".project file should have correct content")
        );
    }
    
    @Test
    @DisplayName(".project contains correct project name")
    public void projectName() throws IOException {
        File source = createTemporaryDirectory();
        File target = createTemporaryDirectory();
        
        FolderInitializer initializer = new FolderInitializer(source, target);
        assertDoesNotThrow(() -> initializer.init("SomeTestName"));
        
        File projectFile = new File(target, ".project");
        assertTrue(projectFile.isFile(), "target should contain .project file");
        
        String[] projectContent = FileUtils.readFileToString(projectFile).split("\n");
        
        assertAll(
            () -> assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", projectContent[0], ".project file should have correct copied content"),
            () -> assertEquals("<projectDescription>", projectContent[1], ".project file should have correct copied content"),
            () -> assertEquals("    <name>SomeTestName</name>", projectContent[2], ".project file should have correct project name"),
            () -> assertEquals("    <comment></comment>", projectContent[3], ".project file should have correct copied content")
        );
    }
    
    @Test
    @DisplayName("source files are copied")
    public void sourceFilesCopied() throws IOException {
        File source = createTemporaryDirectory();
        File target = createTemporaryDirectory();
        
        File sourceFile1 = new File(source, "File1.java");
        File sourceFile2 = new File(source, "Util.java");
        
        FileUtils.write(sourceFile1, "Testcontent1\n");
        FileUtils.write(sourceFile2, "Other\n\ttest\ncontent.\n");
        
        assertAll(
            () -> assertEquals(2, source.listFiles().length, "Precondition: source should contain 2 files"),
            () -> assertEquals(0, target.listFiles().length, "Precondition: target should be empty")
        );
        
        FolderInitializer initializer = new FolderInitializer(source, target);
        assertDoesNotThrow(() -> initializer.init("Something"));
        
        File projectFile = new File(target, ".project");
        File classpathFile = new File(target, ".classpath");
        File targetFile1 = new File(target, "File1.java");
        File targetFile2 = new File(target, "Util.java");
        
        assertAll(
            () -> assertEquals(2, source.listFiles().length, "source directory should remain untouched"),
            () -> assertEquals(4, target.listFiles().length, "target should contain 4 files"),
            () -> assertTrue(targetFile1.isFile(), "target should contain File1.java file"),
            () -> assertTrue(targetFile2.isFile(), "target should contain Util.java file"),
            () -> assertTrue(classpathFile.isFile(), "target should contain .classpath file"),
            () -> assertTrue(projectFile.isFile(), "target should contain .project file"),
            
            () -> assertEquals("Testcontent1\n", FileUtils.readFileToString(targetFile1), "File1.java in target should have copied content"),
            () -> assertEquals("Other\n\ttest\ncontent.\n", FileUtils.readFileToString(targetFile2), "Util.java in target should have copied content")
        );
    }
    
    @Test
    @DisplayName("clears target directory")
    public void clearsTarget() throws IOException {
        File source = createTemporaryDirectory();
        File target = createTemporaryDirectory();
        
        File targetFile = new File(target, "test/some_file.txt");
        targetFile.getParentFile().mkdir();
        FileUtils.write(targetFile, "some data\n");
        
        assertAll(
            () -> assertEquals(0, source.listFiles().length, "Precondition: source should be empty"),
            () -> assertEquals(1, target.listFiles().length, "Precondition: target should not be empty")
        );
        
        FolderInitializer initializer = new FolderInitializer(source, target);
        assertDoesNotThrow(() -> initializer.init("Something"));
        
        File projectFile = new File(target, ".project");
        File classpathFile = new File(target, ".classpath");
        
        assertAll(
            () -> assertEquals(2, target.listFiles().length, "target should contain two files"),
            () -> assertTrue(classpathFile.isFile(), "target should contain .classpath file"),
            () -> assertTrue(projectFile.isFile(), "target should contain .project file"),
            () -> assertFalse(targetFile.exists(), "target should not contain pre-existing files")
        );
    }
    
    @Test
    @DisplayName(".svn folder in target is not cleaned")
    public void targetSvnNotCleared() throws IOException {
        File source = createTemporaryDirectory();
        File target = createTemporaryDirectory();
        
        File targetFile1 = new File(target, "some_file.txt");
        FileUtils.write(targetFile1, "some data\n");
        
        File targetFile2 = new File(target, ".svn/some_file.txt");
        targetFile2.getParentFile().mkdir();
        FileUtils.write(targetFile2, "some svn data\n");
        
        assertAll(
            () -> assertEquals(0, source.listFiles().length, "Precondition: source should be empty"),
            () -> assertEquals(2, target.listFiles().length, "Precondition: target should not be empty")
        );
        
        FolderInitializer initializer = new FolderInitializer(source, target);
        assertDoesNotThrow(() -> initializer.init("Something"));
        
        File projectFile = new File(target, ".project");
        File classpathFile = new File(target, ".classpath");
        
        assertAll(
            () -> assertEquals(3, target.listFiles().length, "target should contain 3 files"),
            () -> assertTrue(classpathFile.isFile(), "target should contain .classpath file"),
            () -> assertTrue(projectFile.isFile(), "target should contain .project file"),
            () -> assertTrue(targetFile2.isFile(), "target should contain files in .svn folder"),
            () -> assertFalse(targetFile1.exists(), "target should not contain other files outside .svn folder")
        );
    }
    
    @Test
    @DisplayName("pre-existing .project and .classpath in source are used")
    public void existingProjectAndClasspath() throws IOException {
        File source = createTemporaryDirectory();
        File target = createTemporaryDirectory();
        
        File sourceProject = new File(source, ".project");
        FileUtils.write(sourceProject, "some project data\n");
        
        File sourceClasspath = new File(source, ".classpath");
        FileUtils.write(sourceClasspath, "classpath content\n");
        
        assertAll(
            () -> assertEquals(2, source.listFiles().length, "Precondition: source should contain 2 files"),
            () -> assertEquals(0, target.listFiles().length, "Precondition: target should be empty")
        );
        
        FolderInitializer initializer = new FolderInitializer(source, target);
        assertDoesNotThrow(() -> initializer.init("Something"));
        
        File targetProject = new File(target, ".project");
        File targetClasspath = new File(target, ".classpath");
        
        assertAll(
            () -> assertEquals(2, target.listFiles().length, "target should contain 2 files"),
            () -> assertTrue(targetClasspath.isFile(), "target should contain .classpath file"),
            () -> assertTrue(targetProject.isFile(), "target should contain .project file"),
            
            () -> assertEquals("classpath content\n", FileUtils.readFileToString(targetClasspath), "target .classpath should contain source content"),
            () -> assertEquals("some project data\n", FileUtils.readFileToString(targetProject), "target .project should contain source content")
        );
    }
    
    private File createTemporaryDirectory() {
        return assertDoesNotThrow(() -> {
            
            File tempfile = File.createTempFile("FolderInitializerTest", null);
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

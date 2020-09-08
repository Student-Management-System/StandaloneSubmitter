package de.uni_hildesheim.sse.submitter.io;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;

/**
 * A class for checking a submission folder, e.g. size and number of files (recursively, including all sub-folders).
 * 
 * @author Adam
 */
public class FolderCheck {

    private int numFiles;
    
    private int numJavaFiles;
    
    private long totalSize;
    
    /**
     * Creates a {@link FolderCheck} for the given directory.
     * 
     * @param directory The folder to check.
     * @throws IOException If reading the directory information fails.
     */
    public FolderCheck(File directory) throws IOException {
        init(directory);
    }
    
    /**
     * Initializes the data for the given folder. Results are stored in attributes of this object.
     * 
     * @param directory The folder to check.
     * 
     * @throws IOException If reading the directory information fails.
     */
    private void init(File directory) throws IOException {
        Files.walk(directory.toPath())
            .filter((path) -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
            .forEach((path) -> {
                File file = path.toFile();
                numFiles++;
                if (file.getName().endsWith(".java")) {
                    numJavaFiles++;
                }
                totalSize += file.length();
            });
    }
    
    /**
     * Returns the number of files in the directory.
     * 
     * @return The number of files in the directory.
     */
    public int getNumFiles() {
        return numFiles;
    }
    
    /**
     * Returns the number of Java source files (ending with <code>.java</code>) in the directory.
     * 
     * @return The number of Java source files.
     */
    public int getNumJavaFiles() {
        return numJavaFiles;
    }
    
    /**
     * Returns the sum of all file sizes in the directory.
     * 
     * @return The total size of the directory, in bytes.
     */
    public long getTotalSize() {
        return totalSize;
    }
    
}

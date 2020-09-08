package de.uni_hildesheim.sse.submitter.settings;

/**
 * Holds settings for checks to perform on the directory to submit.
 * 
 * @author Adam
 */
public class FolderCheckSettings {

    private int minJavaFiles;
    
    private int minFiles;
    
    private int maxFiles;
    
    private long maxSize;

    /**
     * The minimum number of Java files expected in a submission.
     * 
     * @return the minJavaFiles
     */
    public int getMinJavaFiles() {
        return minJavaFiles;
    }

    /**
     * The minimum number of Java files expected in a submission.
     * 
     * @param minJavaFiles the minJavaFiles to set
     */
    public void setMinJavaFiles(int minJavaFiles) {
        this.minJavaFiles = minJavaFiles;
    }

    /**
     * The minimum number of files expected in a submission.
     * 
     * @return the minFiles
     */
    public int getMinFiles() {
        return minFiles;
    }

    /**
     * The minimum number of files expected in a submission.
     * 
     * @param minFiles the minFiles to set
     */
    public void setMinFiles(int minFiles) {
        this.minFiles = minFiles;
    }

    /**
     * The maximum number of files expected in a submission.
     * 
     * @return the maxFiles
     */
    public int getMaxFiles() {
        return maxFiles;
    }

    /**
     * The maximum number of files expected in a submission.
     * 
     * @param maxFiles the maxFiles to set
     */
    public void setMaxFiles(int maxFiles) {
        this.maxFiles = maxFiles;
    }

    /**
     * The maximum size expected for a complete submission directory, in bytes.
     * 
     * @return the maxSize
     */
    public long getMaxSize() {
        return maxSize;
    }

    /**
     * The maximum size expected for a complete submission directory, in bytes.
     * 
     * @param maxSize the maxSize to set
     */
    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }
    
}

package de.uni_hildesheim.sse.submitter.svn;

import org.tmatesoft.svn.core.SVNCommitInfo;

/**
 * Holds the results of a submission.
 * 
 * @author Adam Krafczyk
 */
public class SubmitResult {

    private int numJavaFiles;
    
    private SVNCommitInfo commitInfo;
    
    /**
     * Creates a new submission result.
     * @param numJavaFiles The number of java files submitted.
     * @param commitInfo The {@link SVNCommitInfo} of the commit.
     */
    public SubmitResult(int numJavaFiles, SVNCommitInfo commitInfo) {
        this.numJavaFiles = numJavaFiles;
        this.commitInfo = commitInfo;
    }
    
    /**
     * Getter for the number of java files submitted.
     * @return The number of java files submitted.
     */
    public int getNumJavFiles() {
        return numJavaFiles;
    }
    
    /**
     * Getter for the {@link SVNCommitInfo}.
     * @return The {@link SVNCommitInfo} for the commit.
     */
    public SVNCommitInfo getCommitInfo() {
        return commitInfo;
    }
    
}

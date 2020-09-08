package de.uni_hildesheim.sse.submitter.ui;

import java.io.File;

/**
 * Interface for listeners of events created by the {@link StandaloneSubmitter}.
 * 
 * @author Adam
 */
public interface IStandaloneSubmitterListener {

    /**
     * Called after the submission directory has been changed.
     * 
     * @param newFolder The new directory that will be submitted.
     * 
     * @see StandaloneSubmitter#setDirectoryToSubmit(File)
     */
    public void onSubmissionDirectoryChanged(File newFolder);
    
}

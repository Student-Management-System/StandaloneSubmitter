package de.uni_hildesheim.sse.submitter.svn;

import org.tmatesoft.svn.core.SVNLogEntry;

/**
 * Represents a single revision.
 * 
 * @author Adam Krafczyk
 */
public class Revision {

    private String description;
    private long revision;
    
    /**
     * Creates a {@link Revision} for the given {@link SVNLogEntry}.
     * @param logEntry the log entry that holds information about the revision.
     */
    public Revision(SVNLogEntry logEntry) {
        StringBuffer buf = new StringBuffer();
        buf.append(logEntry.getDate());
        buf.append(" (");
        buf.append(logEntry.getRevision());
        buf.append("): ");
        buf.append(logEntry.getMessage());
        buf.append(" (by ");
        buf.append(logEntry.getAuthor());
        buf.append(")");
        description = buf.toString();
        revision = logEntry.getRevision();
    }
    
    @Override
    public String toString() {
        return description;
    }
    
    /**
     * Returns the revision number.
     * @return the revision number
     */
    public long getRevision() {
        return revision;
    }
    
}

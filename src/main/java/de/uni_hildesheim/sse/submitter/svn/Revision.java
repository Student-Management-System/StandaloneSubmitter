package de.uni_hildesheim.sse.submitter.svn;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

import org.tmatesoft.svn.core.SVNLogEntry;

/**
 * Represents a single revision.
 * 
 * @author Adam Krafczyk
 */
public class Revision {

    private static final DateTimeFormatter DATE_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:MM", Locale.ROOT).withZone(ZoneId.systemDefault());
    
    private long revision;
    
    private String description;
    
    /**
     * Creates a {@link Revision} for the given {@link SVNLogEntry}.
     * @param logEntry the log entry that holds information about the revision.
     */
    public Revision(SVNLogEntry logEntry) {
        StringBuffer buf = new StringBuffer();
        buf.append(DATE_FORMATTER.format(logEntry.getDate().toInstant()));
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
    
    /**
     * Creates a {@link Revision} with the given parameters.
     * 
     * @param revision The revision number.
     * @param description The description of this revision.
     */
    public Revision(long revision, String description) {
        this.revision = revision;
        this.description = description;
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

    @Override
    public int hashCode() {
        return Objects.hash(description, revision);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Revision)) {
            return false;
        }
        Revision other = (Revision) obj;
        return Objects.equals(description, other.description) && revision == other.revision;
    }
    
}

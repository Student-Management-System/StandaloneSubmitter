package de.uni_hildesheim.sse.submitter.svn.hookErrors;

/**
 * Severity type specifying whether a problem is an error or only a warning.
 * 
 * @author El-Sharkawy
 */
public enum Severity {
    
    WARNING, ERROR, UNKNOWN;

    /**
     * Returns the specified literal, with the given name.
     * 
     * @param name The name of the literal (case insensitive).
     * @return The specified literal or {@link #UNKNOWN} for invalid names.
     */
    public static Severity getByName(String name) {
        name = name.toUpperCase();
        Severity result;
        try {
            result = valueOf(name);
        } catch (IllegalArgumentException e) {
            result = Severity.UNKNOWN;
        }
        return result;
    }
    
}

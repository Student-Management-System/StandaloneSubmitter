package de.uni_hildesheim.sse.submitter.svn.hookErrors;

/**
 * Severity type specifing whether a problem is an error or only a warning.
 * @author El-Sharkawy
 *
 */
public enum SeverityType {
    WARNING, ERROR, UNKNOWN;

    /**
     * Returns the specified literal, with the given name.
     * @param name The name of the literal (case insensitive).
     * @return The specified literal or <code>null</code> if no literal matches the given name.
     */
    public static SeverityType getByName(String name) {
        name = name.toUpperCase();
        SeverityType result;
        try {
            result = valueOf(name);
        } catch (IllegalArgumentException e) {
            result = SeverityType.UNKNOWN;
        }
        return result;
    }
}

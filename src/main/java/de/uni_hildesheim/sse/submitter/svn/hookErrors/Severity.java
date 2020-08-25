package de.uni_hildesheim.sse.submitter.svn.hookErrors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Severity type specifying whether a problem is an error or only a warning.
 * 
 * @author El-Sharkawy
 */
public enum Severity {
    
    WARNING, ERROR, UNKNOWN;

    private static final Logger LOGGER = LogManager.getLogger();
    
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
            LOGGER.warn("Unknown severity: {}", name);
            result = Severity.UNKNOWN;
        }
        return result;
    }
    
}

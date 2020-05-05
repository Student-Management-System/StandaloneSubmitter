package de.uni_hildesheim.sse.submitter.svn.hookErrors;

/**
 * The different tools/checker of the hook script, which may return errors.
 * @author El-Sharkawy
 *
 */
public enum ErrorType {
    JAVAC("javac"), CHECKSTYLE("checkstyle"), JUNIT("junit"), COMMIT("commit-handler"), HOOK("hook");

    private String toolName;

    /**
     * Internal constructor.
     * @param toolName The name of the tool, needed for the {@link #getByToolName(String)} method.
     */
    private ErrorType(String toolName) {
        this.toolName = toolName;
    }

    /**
     * Returns the {@link ErrorType} specified by the tool name.
     * @param toolName The name as it is returned by the hook script.
     * @return The {@link ErrorType} or <tt>null</tt> if no literal was found with the specified name.
     */
    public static ErrorType getByToolName(String toolName) {
        ErrorType result = null;
        for (ErrorType type : values()) {
            if (type.toolName.equalsIgnoreCase(toolName)) {
                result = type;
                break;
            }
        }

        return result;
    }
}

package de.uni_hildesheim.sse.submitter.svn.hookErrors;

/**
 * The different tools/checker of the hook script, which may return errors.
 * 
 * @author El-Sharkawy
 * @author Adam
 */
public enum ErrorType {
    FILE_SIZE("file-size"),
    ENCODING("encoding"),
    ECLIPSE_CONFIG("eclipse-configuration"),
    JAVAC("javac"),
    CHECKSTYLE("checkstyle"),
    
    /**
     * Signifies an error by the hook itself, i.e. internal errors.
     */
    HOOK("hook"),
    
    // from old jSvnSubmitHook
    JUNIT("junit"),
    COMMIT("commit-handler");

    private String toolName;

    /**
     * Internal constructor.
     * @param toolName The name of the tool, needed for the {@link #getByToolName(String)} method.
     */
    private ErrorType(String toolName) {
        this.toolName = toolName;
    }
    
    /**
     * Returns the tool-name.
     * 
     * @return The tool-name.
     */
    public String getToolName() {
        return toolName;
    }

    /**
     * Returns the {@link ErrorType} specified by the tool name.
     * @param toolName The name as it is returned by the hook script.
     * @return The {@link ErrorType} or <code>null</code> if no literal was found with the specified name.
     */
    public static ErrorType getByToolName(String toolName) {
        // the old jSvnSubmitHook had a different name for the file-size check
        if ("file-size-check".equals(toolName)) {
            toolName = "file-size";
        }
        
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

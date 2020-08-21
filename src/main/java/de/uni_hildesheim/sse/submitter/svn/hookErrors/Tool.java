package de.uni_hildesheim.sse.submitter.svn.hookErrors;

/**
 * The different checks run by the hook, which may return errors.
 * 
 * @author El-Sharkawy
 * @author Adam
 */
public enum Tool {
    FILE_SIZE("file-size"),
    ENCODING("encoding"),
    ECLIPSE_CONFIG("eclipse-configuration"),
    JAVAC("javac"),
    CHECKSTYLE("checkstyle"),
    
    /**
     * Signifies an error by the hook itself, i.e. internal errors.
     */
    HOOK("hook"),
    
    /**
     * An error from a tool that is not known to this tool.
     */
    UNKNOWN("unknown"),
    
    // from old jSvnSubmitHook
    JUNIT("junit"),
    COMMIT("commit-handler");

    private String toolName;

    /**
     * Internal constructor.
     * @param toolName The name of the tool, needed for the {@link #getByToolName(String)} method.
     */
    private Tool(String toolName) {
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
     * Returns the {@link Tool} specified by the tool name.
     * 
     * @param toolName The name as it is returned by the hook script. May be <code>null</code>.
     * 
     * @return The {@link Tool} or <code>null</code> if no literal was found with the specified name.
     */
    public static Tool getByToolName(String toolName) {
        // the old jSvnSubmitHook had a different name for the file-size check
        if ("file-size-check".equals(toolName)) {
            toolName = "file-size";
        }
        
        Tool result = null;
        for (Tool type : values()) {
            if (type.toolName.equalsIgnoreCase(toolName)) {
                result = type;
                break;
            }
        }

        return result;
    }
}

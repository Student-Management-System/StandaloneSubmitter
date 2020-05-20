package de.uni_hildesheim.sse.submitter.settings;

/**
 * Holds the configuration for tool (does not contain the configuration of the user).
 * @author El-Sharkawy
 *
 */
public class ToolConfiguration {
    
    private String programName;
    private String programVersion;
    
    private String repositoryURL;
    private boolean repositorySendsXmlAnswers;
    private String repositoryConnectionError;
    private String mgmtURL;
    private String authURL;
    
    private CourseSettings course;
    private HookMessageSettings commitMessages;
    private UiColorSettings colorSettings;
    
    /**
     * Returns the name of the application.
     * @return the application.
     */
    public String getProgramName() {
        return programName;
    }
    
    /**
     * Returns the version of the application.
     * @return the application.
     */
    public String getProgramVersion() {
        return programVersion;
    }
    
    /**
     * Returns the location for submitting the homework.
     * @return the location for submitting the homework.
     */
    public String getRepositoryURL() {
        return repositoryURL;
    }
    
    /**
     * <tt>true</tt> The commit hook send answers in XML format, <tt>false</tt> the hook sends messages in plain text
     * format.
     * @return Whether the commit hook sends XML answers.
     */
    public boolean isRepositorySendsXmlAnswers() {
        return repositorySendsXmlAnswers;
    }
    
    /**
     * The message if the server is not reachable.
     * @return the repositoryConnectionError
     */
    public String getRepositoryConnectionError() {
        return repositoryConnectionError;
    }
    
    /**
     * Returns the location of the <b>student management system</b>.
     * @return the location of the <b>student management system</b>.
     */
    public String getMgmtURL() {
        return mgmtURL;
    }
    
    /**
     * Returns the location of the <b>authentication system</b>.
     * @return the location of the <b>authentication system</b>.
     */
    public String getAuthURL() {
        return authURL;
    }
    
    /**
     * Configuration to parse answers of the commit hook.
     * @return the Configuration to parse answers of the commit hook.
     */
    public HookMessageSettings getCommitMessages() {
        return commitMessages;
    }
    
    /**
     * Configuration of colors used by this tool.
     * @return Configuration of colors used by this tool.
     */
    public UiColorSettings getColorSettings() {
        return colorSettings;
    }
    
    /**
     * Sets the name of the application.
     * @param programName the name of the application.
     */
    public void setProgramName(String programName) {
        this.programName = programName;
    }
    
    /**
     * Sets the version of the application.
     * @param programVersion the version of the application.
     */
    public void setProgramVersion(String programVersion) {
        this.programVersion = programVersion;
    }
    
    /**
     * Sets the location for submitting the homework.
     * @param repositoryURL the location for submitting the homework.
     */
    public void setRepositoryURL(String repositoryURL) {
        this.repositoryURL = repositoryURL;
    }
    
    /**
     * <tt>true</tt> The commit hook send answers in XML format, <tt>false</tt> the hook sends messages in plain text
     * format.
     * @param repositorySendsXmlAnswers Specifies if the hook sends messages in XML format.
     */
    public void setRepositorySendsXmlAnswers(boolean repositorySendsXmlAnswers) {
        this.repositorySendsXmlAnswers = repositorySendsXmlAnswers;
    }
    
    /**
     * The message if the server is not reachable.
     * @param repositoryConnectionError The message if the server is not reachable.
     */
    public void setRepositoryConnectionError(String repositoryConnectionError) {
        this.repositoryConnectionError = repositoryConnectionError;
    }
    
    /**
     * Sets the location of the <b>student management system</b>.
     * @param mgmtURL the location of the <b>student management system</b>.
     */
    public void setMgmtURL(String mgmtURL) {
        this.mgmtURL = mgmtURL;
    }
    
    /**
     * Gets the location of the <b>authentication system</b>.
     * @param authURL the location of the <b>authentication system</b>.
     */
    public void setAuthURL(String authURL) {
        this.authURL = authURL;
    }
    
    /**
     * Configuration to parse answers of the commit hook.
     * @param commitMessages Configuration to parse answers of the commit hook.
     */
    public void setCommitMessages(HookMessageSettings commitMessages) {
        this.commitMessages = commitMessages;
    }
    
    /**
     * Configuration of colors used by this tool.
     * @param colorSettings Configuration of colors used by this tool.
     */
    public void setColorSettings(UiColorSettings colorSettings) {
        this.colorSettings = colorSettings;
    }

    /**
     * Stores information of the course handled by the tool.
     * @return The handled course.
     */
    public CourseSettings getCourse() {
        return course;
    }

    /**
     * Stores information of the course handled by the tool.
     * @param course The handled course.
     */
    public void setCourse(CourseSettings course) {
        this.course = course;
    }
}

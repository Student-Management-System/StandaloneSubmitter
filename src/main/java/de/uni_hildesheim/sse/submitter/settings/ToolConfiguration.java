package de.uni_hildesheim.sse.submitter.settings;

import java.util.Map;

/**
 * Holds the configuration for tool (does not contain the configuration of the user).
 * @author El-Sharkawy
 *
 */
public class ToolConfiguration {
    
    private String programName;
    private String programVersion;
    
    private String repositoryURL;
    private String mgmtURL;
    private String authURL;
    
    private CourseSettings course;
    
    private Map<String, String> messageTranslations;
    
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
     * Returns a map of hook messages as keys and i18n keys as values.
     * @return A map of hook message translations.
     */
    public Map<String, String> getMessageTranslations() {
        return messageTranslations;
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
     * Sets a map of hook messages as keys and i18n keys as values.
     * @param messageTranslations A map of hook message translations.
     */
    public void setMessageTranslations(Map<String, String> messageTranslations) {
        this.messageTranslations = messageTranslations;
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

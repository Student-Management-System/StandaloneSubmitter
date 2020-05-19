package de.uni_hildesheim.sse.submitter.svn.hookErrors;

import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.settings.HookMessageSettings;
import de.uni_hildesheim.sse.submitter.settings.ToolSettings;

/**
 * Data class, describing a returned error.
 * 
 * @author El-Sharkawy
 * 
 */
public class ErrorDescription {

    private ErrorType type;
    private SeverityType severity;
    private String file;
    private String lines;
    private String code;
    private String solution;
    
    /**
     * Getter for the tool information, which produced the error message.
     * @return the type
     */
    public ErrorType getType() {
        return type;
    }

    /**
     * Setter for the tool information, which produced the error message.
     * @param type the type to set
     */
    public void setType(ErrorType type) {
        this.type = type;
    }

    /**
     * Getter for the severity of the returned problem message.
     * @return the severity
     */
    public SeverityType getSeverity() {
        return severity;
    }

    /**
     * Setter for the severity of the returned problem message.
     * @param severity the severity to set
     */
    public void setSeverity(SeverityType severity) {
        this.severity = severity;
    }

    /**
     * Getter for the file, where the problem is located.
     * @return the file
     */
    public String getFile() {
        return file;
    }

    /**
     * Setter for the file, where the problem is located.
     * @param file the file to set
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * Getter for the lines of the problem inside the file.
     * @return the lines
     */
    public String getLines() {
        return lines;
    }

    /**
     * Setter for the lines of the problem inside the file.
     * @param lines the lines to set
     */
    public void setLines(String lines) {
        this.lines = lines;
    }

    /**
     * Getter for the code snippet, which produced the error.
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * Setter for the code snippet, which produced the error.
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }
    
    /**
     * Short hand to retrieve the settings for the submission hook.
     * @return The settings for the submission hook.
     */
    private HookMessageSettings getConfig() {
        return ToolSettings.getConfig().getCommitMessages();
    }


    /**
     * Setter for the Solution message.
     * @param solution The "message" attribute of the hook error message.
     */
    public void setSolution(String solution) {
        if (getConfig().getMissingJava().equals(solution)) {
            this.solution = I18nProvider.getText("errors.messages.no_java_submitted");
        } else if (getConfig().getContainsTabs().equals(solution)) {
            this.solution = I18nProvider.getText("errors.messages.tabs_included");
        } else if (getConfig().getMissingJavaDoc().equals(solution)) {
            this.solution = I18nProvider.getText("errors.messages.missingJavaDoc");
        } else if (getConfig().getCheckError().equals(solution)) {
            this.solution = I18nProvider.getText("error.unknownError");
        } else {
            this.solution = solution;
        }
    }

    /**
     * Getter for the solution of the HOOK error message.
     * @return The solution of the HOOK error message.
     */
    public String getSolution() {
        return solution;
    }

}

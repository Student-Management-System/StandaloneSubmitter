package de.uni_hildesheim.sse.submitter.svn.hookErrors;

import java.util.Objects;

import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.settings.ToolSettings;

/**
 * Data class, describing a returned error.
 * 
 * @author El-Sharkawy
 * 
 */
public class ErrorDescription {

    private Tool tool;
    private Severity severity;
    private String message;
    
    private String file;
    private int line = -1;
    
    /**
     * Getter for the tool information, which produced the error message.
     * @return the type
     */
    public Tool getTool() {
        return tool;
    }

    /**
     * Setter for the tool information, which produced the error message.
     * @param tool the type to set
     */
    public void setTool(Tool tool) {
        this.tool = tool;
    }

    /**
     * Getter for the severity of the returned problem message.
     * @return the severity
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Setter for the severity of the returned problem message.
     * @param severity the severity to set
     */
    public void setSeverity(Severity severity) {
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
     * Getter for the line of the problem inside the file. Anything lower or equal to 0 means there is no specific
     * line that this error belongs to.
     * 
     * @return the line
     */
    public int getLine() {
        return line;
    }

    /**
     * Setter for the line of the problem inside the file.
     * @param line the line to set
     */
    public void setLine(int line) {
        this.line = line;
    }


    /**
     * Setter for the message describing this error.
     * 
     * @param message The "message" attribute of the hook error message.
     */
    public void setMessage(String message) {
        String translationKey = ToolSettings.getConfig().getMessageTranslations().get(message);
        if (translationKey != null) {
            this.message = I18nProvider.getText(translationKey);
        } else {
            this.message = message;
        }
    }

    /**
     * Getter for the description of this error. May be localized.
     * @return The error message.
     */
    public String getMessage() {
        return message;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, line, severity, message, tool);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ErrorDescription)) {
            return false;
        }
        ErrorDescription other = (ErrorDescription) obj;
        return Objects.equals(file, other.file)
                && Objects.equals(line, other.line) && severity == other.severity
                && Objects.equals(message, other.message) && tool == other.tool;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ErrorDescription [tool=");
        builder.append(tool);
        builder.append(", severity=");
        builder.append(severity);
        builder.append(", file=");
        builder.append(file);
        builder.append(", line=");
        builder.append(line);
        builder.append(", message=");
        builder.append(message);
        builder.append("]");
        return builder.toString();
    }

}

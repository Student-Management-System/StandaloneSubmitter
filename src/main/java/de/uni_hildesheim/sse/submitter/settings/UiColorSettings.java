package de.uni_hildesheim.sse.submitter.settings;

import de.uni_hildesheim.sse.submitter.svn.hookErrors.ErrorType;

/**
 * Configures the UI (i.e., color).
 * @author El-Sharkawy
 */
public class UiColorSettings {
    private String defaultColor;
    private String javacColor;
    private String checkstyleColor;
    private String junitColor;
    private String commitColor;
    private String errorColor;
    private String warningColor;
    private String hookColor;
    
    /**
     * Returns the color of log messages.
     * @return The color to use as color hex code. 
     */
    public String getDefaultColor() {
        return defaultColor;
    }
    
    /**
     * The color for highlighting compilation errors.
     * @return The color to use as color hex code. 
     */
    public String getJavacColor() {
        return javacColor;
    }
    
    /**
     * The color for highlighting checkstyle errors.
     * @return The color to use as color hex code. 
     */
    public String getCheckstyleColor() {
        return checkstyleColor;
    }
   
    /**
     * The color for highlighting JUnit errors.
     * @return The color to use as color hex code. 
     */
    public String getJunitColor() {
        return junitColor;
    }
    
    /**
     * The color for highlighting commit related errors.
     * @return The color to use as color hex code. 
     */
    public String getCommitColor() {
        return commitColor;
    }
    /**
     * The color for highlighting problems of severity <b>error</b>.
     * @return The color to use as color hex code. 
     */
    public String getErrorColor() {
        return errorColor;
    }
    /**
     * The color for highlighting problems of severity <b>warning</b>.
     * @return The color to use as color hex code. 
     */
    public String getWarningColor() {
        return warningColor;
    }
    
    /**
     * The color for highlighting Commit Hook errors.
     * @return The color to use as color hex code. 
     */
    public String getHookColor() {
        return hookColor;
    }
    
    /**
     * Gets a color string for a given {@link ErrorType}.
     *  
     * @param type The type of error to get an appropriate color for.
     * 
     * @return The color string.
     */
    public String getColor(ErrorType type) {
        String color;
        switch (type) {
        case JAVAC:
            color = getJavacColor();
            break;
            
        case CHECKSTYLE:
            color = getCheckstyleColor();
            break;
            
        case HOOK:
            color = getHookColor();
            break;
            
        // legacy jSvnSubmitHook
        case JUNIT:
            color = getJunitColor();
            break;
        case COMMIT:
            color = getCommitColor();
            break;
        
        default:
            color = getDefaultColor();
            break;
        }
        return color;
    }
    
    /**
     * Sets the color of log messages.
     * @param defaultColor The color to use as color hex code. 
     */
    public void setDefaultColor(String defaultColor) {
        this.defaultColor = defaultColor;
    }
    
    /**
     * Sets the color for highlighting compilation errors.
     * @param javacColor The color to use as color hex code. 
     */
    public void setJavacColor(String javacColor) {
        this.javacColor = javacColor;
    }
    
    /**
     * Sets the color for highlighting checkstyle errors.
     * @param checkstyleColor The color to use as color hex code. 
     */
    public void setCheckstyleColor(String checkstyleColor) {
        this.checkstyleColor = checkstyleColor;
    }
    
    /**
     * Sets the color for highlighting JUnit errors.
     * @param junitColor The color to use as color hex code. 
     */
    public void setJunitColor(String junitColor) {
        this.junitColor = junitColor;
    }
    
    /**
     * Sets the color for highlighting commit related errors.
     * @param commitColor The color to use as color hex code. 
     */
    public void setCommitColor(String commitColor) {
        this.commitColor = commitColor;
    }
    
    /**
     * Sets the color for highlighting problems of severity <b>error</b>.
     * @param errorColor The color to use as color hex code. 
     */
    public void setErrorColor(String errorColor) {
        this.errorColor = errorColor;
    }
    
    /**
     * Sets the color for highlighting problems of severity <b>warning</b>.
     * @param warningColor The color to use as color hex code. 
     */
    public void setWarningColor(String warningColor) {
        this.warningColor = warningColor;
    }
    
    /**
     * Sets the color for highlighting Commit Hook errors.
     * @param hookColor The color to use as color hex code. 
     */
    public void setHookColor(String hookColor) {
        this.hookColor = hookColor;
    }
}

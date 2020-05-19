package de.uni_hildesheim.sse.submitter.settings;

/**
 * Settings required to parse the answer messages of the commit hook.
 * @author El-Sharkawy
 */
public class HookMessageSettings {
    
    private String failed;
    private String blocked;
    private String missingJava;
    private String containsTabs;
    private String missingJavaDoc;
    private String checkError;
   
    /**
     * Returns the message that the commit was failed.
     * @return The message as emitted by the commit hook.
     */
    public String getFailed() {
        return failed;
    }
    
    /**
     * Returns the message that the commit was blocked to due checks.
     * @return The message as emitted by the commit hook.
     */
    public String getBlocked() {
        return blocked;
    }
    
    /**
     * Returns the message that the commit was blocked to due missing Java code files.
     * @return The message as emitted by the commit hook.
     */
    public String getMissingJava() {
        return missingJava;
    }
    
    /**
     * Returns the message that the commit was blocked to used tabs in code.
     * @return The message as emitted by the commit hook.
     */
    public String getContainsTabs() {
        return containsTabs;
    }
    
    /**
     * Returns the message that the commit was blocked to due missing JavaDoc.
     * @return The message as emitted by the commit hook.
     */
    public String getMissingJavaDoc() {
        return missingJavaDoc;
    }
    
    /**
     * Returns the begin text for showing the failed checks.
     * @return The message as emitted by the commit hook.
     */
    public String getCheckError() {
        return checkError;
    }
    
    /**
     * Sets the message that the commit was failed.
     * @param failed The message as emitted by the commit hook.
     */
    public void setFailed(String failed) {
        this.failed = failed;
    }
    
    /**
     * Sets the message that the commit was blocked to due checks.
     * @param blocked The message as emitted by the commit hook.
     */
    public void setBlocked(String blocked) {
        this.blocked = blocked;
    }
    
    /**
     * Sets the message that the commit was blocked to due missing Java code files.
     * @param missingJava The message as emitted by the commit hook.
     */
    public void setMissingJava(String missingJava) {
        this.missingJava = missingJava;
    }
    
    /**
     * Sets the begin text for showing the failed checks.
     * @param containsTabs The message as emitted by the commit hook.
     */
    public void setContainsTabs(String containsTabs) {
        this.containsTabs = containsTabs;
    }
    
    /**
     * Sets the message that the commit was blocked to due missing JavaDoc.
     * @param missingJavaDoc The message as emitted by the commit hook.
     */
    public void setMissingJavaDoc(String missingJavaDoc) {
        this.missingJavaDoc = missingJavaDoc;
    }
    
    /**
     * Sets the begin text for showing the failed checks.
     * @param checkError The message as emitted by the commit hook.
     */
    public void setCheckError(String checkError) {
        this.checkError = checkError;
    }
}

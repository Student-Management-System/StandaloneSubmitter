package de.uni_hildesheim.sse.submitter.svn;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;

import de.uni_hildesheim.sse.submitter.conf.Configuration;
import de.uni_hildesheim.sse.submitter.settings.ToolSettings;
import de.uni_hildesheim.sse.submitter.svn.hookErrors.ErrorParser;

/**
 * Utility class to translate results and exceptions into messages, which are understandable for students / course
 * attendees. ;-)
 * 
 * @author El-Sharkawy
 * 
 */
public class SubmissionResultHandler {

    private ISubmissionOutputHandler handler;

    /**
     * Sole constructor for this class.
     * 
     * @param handler
     *            A handler for displaying messages to the user.
     */
    public SubmissionResultHandler(ISubmissionOutputHandler handler) {
        this.handler = handler;
    }
    
    /**
     * Getter for the {@link ISubmissionOutputHandler} that this
     * {@link SubmissionResultHandler} uses.
     * @return The {@link ISubmissionOutputHandler}.
     */
    public ISubmissionOutputHandler getHandler() {
        return handler;
    }

    /**
     * Handles exceptions occurred during submitting the homework.
     * 
     * @param commitExc
     *            The Exception which was caught.
     */
    public void handleCommitException(SubmitException commitExc) {
        Configuration config = handler.getConfiguration();
        String errorMsg = null;
        if (null != commitExc.getErrorCode()) {
            switch (commitExc.getErrorCode()) {
            case NO_REPOSITORY_FOUND:
                errorMsg = "Server konnte nicht erreicht werden. Bitte prüfen Sie Ihre Internetverbindung.\n"
                        + "Versuchen Sie " + commitExc.getLocation() + " im Browser aufzurufen.\n"
                        + "Sollte dieses auch nicht erfolgreich sein, so kontaktieren Sie bitte das "
                        + ToolSettings.getConfig().getCourse().getTeamName()
                        + " (" + ToolSettings.getConfig().getCourse().getTeamMail() + ").";
                break;
            case COULD_NOT_CREATE_TEMP_DIR:
                errorMsg = "Es konnte kein Temporärerordner auf der Festplatte angelegt werden.\n";
                if (commitExc.getLocation() != null) {
                    errorMsg += "Bitte prüfen Sie ob Sie auf " + commitExc.getLocation()
                            + "  Lese-/Schreibzugriff haben.";
                } else {
                    errorMsg += "Bitte prüfen Sie ob Sie auf der Festplatte Lese-/Schreibzugriff haben.";
                }
                break;
            case NO_EXERCISE_FOUND:
                errorMsg = "Es konnte für die Gruppe "
                        + config.getGroup()
                        + " keine Hausaufgabe mit dem Namen "
                        + config.getExercise().getName()
                        + " gefunden werden.\n"
                        + "Bitte vergewissern Sie sich, dass\n"
                        + "  1.) Das der Gruppenname korrekt ist "
                        + "(es wird zwischen Groß-/Kleinschreibung unterschieden): "
                        + config.getGroup()
                        + "\n"
                        + "  2.) Der Name der Hausaufgabe korrekt ist "
                        + "(es wird zwischen Groß-/Kleinschreibung unterschieden): "
                        + config.getExercise().getName() + "\n"
                        + "  3.) Ihre Logindaten korrekt sind (es wird der Login vom Rechenzentrum erwartet).\n"
                        + "Versuchen Sie " + commitExc.getLocation() + " im Browser aufzurufen.";
                break;
            case CANNOT_COMMIT:
                errorMsg = "Es konnten keine Hausaufgaben nach " + commitExc.getLocation() + " hochgeladen werden.\n"
                        + "Prüfen Sie bitte ob eine Hausaufgabenabgabe für die angegebene "
                        + "Hausaufgabe überhaupt vorgesehen ist.";
                break;
            case DO_STATUS_NOT_POSSIBLE:
                errorMsg = "Es konnten keine Informationen über die zu submittenden Dateien gesammelt werden.\n"
                        + "Die genaue Ursache hierfür ist unklar." + "Kontaktieren Sie bitte das "
                        + ToolSettings.getConfig().getCourse().getTeamName()
                        + " (" + ToolSettings.getConfig().getCourse().getTeamMail() + ").";
                break;
            default:
                errorMsg = "Ein unerwarter Fehler ist aufgetreten.\n" + "Kontaktieren Sie bitte das "
                        + ToolSettings.getConfig().getCourse().getTeamName()
                        + " (" + ToolSettings.getConfig().getCourse().getTeamMail() + ").";
                break;
            }
        } else {
            errorMsg = "Ein unerwarter Fehler ist aufgetreten.\n" + "Kontaktieren Sie bitte das "
                    + ToolSettings.getConfig().getCourse().getTeamName()
                    + " (" + ToolSettings.getConfig().getCourse().getTeamMail() + ").";
        }

        handler.showErrorMessage(errorMsg);
    }

    /**
     * Translates a {@link SVNCommitInfo} into a message, readable by the user.
     * @param info The result of a commit (maybe describing an error).
     */
    public void handleCommitResult(SVNCommitInfo info) {
        SVNErrorMessage errorMsg = info.getErrorMessage();
        if (null == errorMsg) {
            if (-1 == info.getNewRevision()) {
                handler.showInfoMessage("Keine Dateiänderungen festgestellt,\n"
                        + "dementsprechend wurden auch keine Änderungen\n" + "hochgeladen.");
            } else {
                handler.showInfoMessage("Dateien wurden erfolgreich hochgeladen.");
            }
        } else {
            ErrorParser parser = new ErrorParser(errorsToString(errorMsg));
            String message;
            if (errorMsg.getErrorCode().equals(SVNErrorCode.REPOS_POST_COMMIT_HOOK_FAILED)) {
                message = "Das Projekt wurde abgegeben, es wurden jedoch Fehler bei der Abgabe festgestellt:";
            } else {
                message = "Das eingereichte Projekt enthält Fehler und wurde daher nicht angenommen.\n"
                        + "Bitte korrigieren Sie folgende Fehler und versuchen es dann erneut:";
            }
            handler.showInfoMessage(message, parser.getErrors());
        }
    }

    /**
     * Translates the given {@link SVNErrorMessage} into a XML text.
     * @param errorMsg The {@link SVNErrorMessage} created by receiving the result from the SVN HOOK script.
     * @return The extracted XML text.
     */
    private String errorsToString(SVNErrorMessage errorMsg) {
        StringBuffer result = new StringBuffer();
        if (errorMsg.getErrorCode().equals(SVNErrorCode.REPOS_POST_COMMIT_HOOK_FAILED)) {
            String errorMessage = errorMsg.getFullMessage();
            int pos = errorMessage.indexOf('\n');
            if (pos > 0) {
                errorMessage.substring(pos);
            }
            result.append(errorMessage);
        } else {
            while (errorMsg.hasChildErrorMessage()) {
                String error = errorMsg.getMessageTemplate();
                if (!ToolSettings.getConfig().getCommitMessages().getFailed().equals(error)) {
                    error = error.substring(ToolSettings.getConfig().getCommitMessages().getBlocked().length());
                    result.append(error);
                    result.append("\n");
                }
                errorMsg = errorMsg.getChildErrorMessage();
            }
        }
        return result.toString();
    }

}

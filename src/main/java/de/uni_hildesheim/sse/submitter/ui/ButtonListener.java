package de.uni_hildesheim.sse.submitter.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.io.FolderCheck;
import de.uni_hildesheim.sse.submitter.settings.FolderCheckSettings;
import de.uni_hildesheim.sse.submitter.settings.ToolSettings;
import de.uni_hildesheim.sse.submitter.svn.Revision;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;

/**
 * Listener for all buttons in the GUI.
 * 
 * @author Adam Krafczyk
 * @author El-Sharkawy
 *
 */
class ButtonListener implements ActionListener {

    static final String ACTION_SUBMIT        = "Submit";
    static final String ACTION_REPLAY        = "Replay";
    static final String ACTION_HISTORY       = "History";
    static final String ACTION_BROWSE_FOLDER = "Browse";
    static final String ACTION_REVIEW        = "Review";
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    private StandaloneSubmitterWindow parent;
    
    private StandaloneSubmitter model;
    
    /**
     * Creates this listener.
     * 
     * @param parent the window which contains the buttons to be listened.
     * @param model The standalone submitter model.
     */
    ButtonListener(StandaloneSubmitterWindow parent, StandaloneSubmitter model) {
        this.parent = parent;
        this.model = model;
    }
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        switch (evt.getActionCommand()) {
        case ACTION_BROWSE_FOLDER:
            createFolderChooser();
            break;
            
        case ACTION_SUBMIT:
            submit(evt);
            
            break;
        case ACTION_REPLAY:
            openReplayDialog(evt);
            break;
            
        case ACTION_HISTORY:
            parent.clearLog();
            parent.toggleButtons(false);
            parent.addProgressAnimator((JButton) evt.getSource());
            
            new Thread(() -> {
                List<Revision> history = model.getHistoryOfCurrentExercise();
                if (history != null) {
                    SwingUtilities.invokeLater(() -> parent.showHistory(history));
                }
                SwingUtilities.invokeLater(() -> parent.toggleButtons(true));
            }).start();
            break;
            
        case ACTION_REVIEW:
            parent.clearLog();
            if (model.getDirectoryToSubmit() == null) { // TODO
                parent.showErrorMessage(I18nProvider.getText("gui.error.no_path_given"));
            } else {
                List<Assignment> assignments = model.getAssignmentsInReviewedState();
                if (assignments == null || assignments.size() == 0) {
                    parent.showInfoMessage(I18nProvider.getText("gui.error.no_review_repos"));
                } else {
                    new ReviewDialog(parent, model, assignments);
                }
                    
            }
            break;
            
        default:
            LOGGER.error("Unknown action command: {}", evt.getActionCommand());
            break;
        }
    }
    
    /**
     * Handles the submit button being pressed.
     * 
     * @param evt The event of the button-press.
     */
    private void submit(ActionEvent evt) {
        parent.toggleButtons(false);
        parent.addProgressAnimator((JButton) evt.getSource());
        parent.clearLog();
        
        boolean submit = true;
        
        try {
            FolderCheck checker = new FolderCheck(model.getDirectoryToSubmit());
            
            String warningMessage = null;
            
            FolderCheckSettings settings = ToolSettings.getConfig().getFolderCheckSettings();
            if (checker.getTotalSize() > settings.getMaxSize()) {
                warningMessage = I18nProvider.getText("warnings.folder_too_large",
                        FileUtils.byteCountToDisplaySize(checker.getTotalSize()));
                
            } else if (checker.getNumJavaFiles() < settings.getMinJavaFiles()) {
                warningMessage = I18nProvider.getText("warnings.too_few_java_files", checker.getNumJavaFiles());
                
            } else if (checker.getNumFiles() < settings.getMinFiles()
                    || checker.getNumFiles() > settings.getMaxFiles()) {
                warningMessage = I18nProvider.getText("warnings.file_count", checker.getNumFiles());
            }
            
            if (warningMessage != null) {
                warningMessage += "\n" + I18nProvider.getText("warnings.submit.are_you_sure");
                submit = JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(parent, warningMessage,
                        I18nProvider.getText("warnings.title"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (IOException e) {
            LOGGER.warn("Exception while checking submission folder", e);
        }
        
        if (submit) {
            parent.showInfoMessage(I18nProvider.getText("gui.log.submitting"));
            
            new Thread(() -> {
                model.submit();
                SwingUtilities.invokeLater(() -> parent.toggleButtons(true));
            }).start();
        } else {
            parent.toggleButtons(true);
        }
    }

    /**
     * Creates and handles the replay previous version dialog.
     * 
     * @param evt The event that caused this dialog to open.
     */
    private void openReplayDialog(ActionEvent evt) {
        parent.clearLog();
        if (model.getDirectoryToSubmit() == null) { // TODO
            parent.showErrorMessage(I18nProvider.getText("gui.error.no_path_given"));
        } else {
            parent.toggleButtons(false);
            parent.addProgressAnimator((JButton) evt.getSource());
            int result = JOptionPane.showConfirmDialog(parent, I18nProvider.getText("gui.warning.delete_dir_on_replay"),
                    I18nProvider.getText("gui.elements.replay"), JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                new ReplayDialog(parent, model);
                parent.toggleButtons(true);
            } else {
                parent.toggleButtons(true);
            }
        }
    }

    /**
     * Creates and handles a FileChooser to select a local folder for submission / replaying.
     */
    private void createFolderChooser() {
        JFileChooser fileChooser = new JFileChooser(model.getDirectoryToSubmit());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(parent);
        switch (result) {
        case JFileChooser.APPROVE_OPTION:
            File projectFolder = fileChooser.getSelectedFile().getAbsoluteFile();
            model.setDirectoryToSubmit(projectFolder);
            break;
        case JFileChooser.CANCEL_OPTION:
            break;
        case JFileChooser.ERROR_OPTION:
            break;
        default:
            LOGGER.error("Unexpected result from JFileChooser: {}", result);
            break;
        }
    }

}

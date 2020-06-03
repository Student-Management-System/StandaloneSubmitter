package de.uni_hildesheim.sse.submitter.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tmatesoft.svn.core.SVNException;

import de.uni_hildesheim.sse.submitter.Starter;
import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;

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
    
    private static final Logger LOGGER = LogManager.getLogger(ButtonListener.class);
    
    private Window parent;
    
    /**
     * Creates this listener.
     * 
     * @param parent the window which contains the buttons to be listened.
     */
    ButtonListener(Window parent) {
        this.parent = parent;
    }
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        /*
         * SE: According to https://docs.oracle.com/javase/8/docs/technotes/guides/language/strings-switch.html
         * switch on Strings should be more efficient than if-else-blocks and I thing it's better readable
         */
        switch (evt.getActionCommand()) {
        case ACTION_BROWSE_FOLDER:
            createFolderChooser();
            break;
        case ACTION_SUBMIT:
            parent.toggleButtons(false);
            parent.submit();
            break;
        case ACTION_REPLAY:
            openReplayDialog();
            break;
        case ACTION_HISTORY:
            parent.clearLog();
            parent.toggleButtons(false);
            try {   
                parent.showHistory(parent.getRemoteRepository().getHistory());
            } catch (SVNException | IOException e) {
                // Shouldn't happen
                parent.showErrorMessage(I18nProvider.getText("gui.error.login_wrong"));
                LOGGER.warn("Could not show history.", e);
            }
            parent.toggleButtons(true);
            break;
        case ACTION_REVIEW:
            parent.clearLog();
            if (parent.getSelectedPath().trim().isEmpty()) {
                parent.showErrorMessage(I18nProvider.getText("gui.error.no_path_given"));
            } else {
                new ReviewDialog(parent);
            }
            break;
        default:
            LOGGER.debug("Unknown action command: {}", evt.getActionCommand());
            break;
        }
    }

    /**
     * Creates and handles the replay previous version dialog.
     */
    private void openReplayDialog() {
        parent.clearLog();
        if (parent.getSelectedPath().trim().isEmpty()) {
            parent.showErrorMessage(I18nProvider.getText("gui.error.no_path_given"));
        } else {
            parent.toggleButtons(false);
            int result = JOptionPane.showConfirmDialog(parent, I18nProvider.getText("gui.warning.delete_dir_on_replay"),
                    I18nProvider.getText("gui.elements.replay"), JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    new ReplayDialog(parent);
                } catch (SVNException | IOException e) {
                    // Shouldn't happen
                    parent.showErrorMessage(I18nProvider.getText("gui.error.login_wrong"));
                } finally {
                    parent.toggleButtons(true);
                }
            } else {
                parent.toggleButtons(true);
            }
        }
    }

    /**
     * Creates and handles a FileChooser to select a local folder for submission / replaying.
     */
    private void createFolderChooser() {
        JFileChooser fileChooser = new JFileChooser(parent.getSelectedPath());
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(parent);
        switch (result) {
        case JFileChooser.APPROVE_OPTION:
            File projectFolder = fileChooser.getSelectedFile().getAbsoluteFile();
            parent.getConfiguration().setProjectFolder(projectFolder);
            parent.setSelectedPath(projectFolder.getPath());
            break;
        case JFileChooser.CANCEL_OPTION:
            break;
        case JFileChooser.ERROR_OPTION:
            break;
        default:
            if (Starter.DEBUG) {
                System.err.println("Unexpected result from JFileChooser: " + result);
            }
            break;
        }
    }

}

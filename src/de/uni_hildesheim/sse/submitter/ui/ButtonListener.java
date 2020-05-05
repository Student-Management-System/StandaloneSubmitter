package de.uni_hildesheim.sse.submitter.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.tmatesoft.svn.core.SVNException;

import de.uni_hildesheim.sse.submitter.Starter;
import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;

/**
 * Listener for all buttons in the GUI.
 * 
 * @author Adam Krafczyk
 *
 */
class ButtonListener implements ActionListener {

    static final String ACTION_SUBMIT        = "Submit";
    static final String ACTION_REPLAY        = "Replay";
    static final String ACTION_HISTORY       = "History";
    static final String ACTION_BROWSE_FOLDER = "Browse";
    static final String ACTION_REVIEW        = "Review";
    
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
        String command = evt.getActionCommand();
        if (command.equals(ACTION_BROWSE_FOLDER)) {
            JFileChooser fileChooser = new JFileChooser(parent.getSelectedPath());
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(parent);
            switch (result) {
            case JFileChooser.APPROVE_OPTION:
                parent.setSelectedPath(fileChooser.getSelectedFile().getAbsolutePath());
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
        } else if (command.equals(ACTION_SUBMIT)) {
            parent.toggleButtons(false);
            parent.submit();
        } else if (command.equals(ACTION_REPLAY)) {
            parent.clearLog();
            if (parent.getSelectedPath().trim().isEmpty()) {
                parent.showErrorMessage(I18nProvider.getText("gui.error.no_path_given"));
                return;
            }
            
            parent.toggleButtons(false);
            int result = JOptionPane.showConfirmDialog(parent, I18nProvider.getText("gui.warning.delete_dir_on_replay"),
                    I18nProvider.getText("gui.elements.replay"), JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    new ReplayDialog(parent);
                } catch (SVNException e) {
                    // Shouldn't happen
                    parent.showErrorMessage(I18nProvider.getText("gui.error.login_wrong"));
                } finally {
                    parent.toggleButtons(true);
                }
            } else {
                parent.toggleButtons(true);
            }
        } else if (command.equals(ACTION_HISTORY)) {
            parent.clearLog();
            parent.toggleButtons(false);
            try {
                parent.showHistory(parent.getRemoteRepository().getHistory());
            } catch (SVNException e) {
                // Shouldn't happen
                parent.showErrorMessage(I18nProvider.getText("gui.error.login_wrong"));
            }
            parent.toggleButtons(true);
        } else if (command.equals(ACTION_REVIEW)) {
            parent.clearLog();
            if (parent.getSelectedPath().trim().isEmpty()) {
                parent.showErrorMessage(I18nProvider.getText("gui.error.no_path_given"));
                return;
            }
            new ReviewDialog(parent);
        } else {
            if (Starter.DEBUG) {
                System.err.println("Unknown action command: " + command);
            }
        }
    }

}

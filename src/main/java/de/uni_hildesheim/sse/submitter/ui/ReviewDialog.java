package de.uni_hildesheim.sse.submitter.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import net.ssehub.exercisesubmitter.protocol.backend.NetworkException;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmitterProtocol;

/**
 * A dialog to review corrected exercises.
 * 
 * @author Adam Krafczyk
 * @author El-Sharkawy
 */
class ReviewDialog extends JDialog implements ActionListener {
    private static final Logger LOGGER = LogManager.getLogger(ReviewDialog.class);

    private static final long serialVersionUID = -2707409724385716679L;
    
    private static final String ACTION_OK = "Ok";
    private static final String ACTION_CANCEL = "Cancel";
    
    private JComboBox<Assignment> assessmentsBox;
    
    private Window parent;
    
    /**
     * Creates a {@link ReviewDialog} for the given parent window.
     * @param parent The parent window.
     */
    ReviewDialog(Window parent) {
        this.parent = parent;
        SubmitterProtocol protocol = parent.getNetworkProtocol();
        
        assessmentsBox = new JComboBox<>();
        assessmentsBox.setRenderer(new AssignmentComboxRenderer());
        try {
            protocol.getReviewedAssignments().stream()
                .forEach(a -> assessmentsBox.addItem(a));
        } catch (NetworkException e) {
            LOGGER.error("Could not contact student management server after successful login.", e);
            // This shouldn't happen here... (since it worked in LoginDialog)
            parent.showErrorMessage(I18nProvider.getText("gui.error.repos_not_found"));
        }
        if (assessmentsBox.getItemCount() == 0) {
            // close dialog
            return;
        }
        
        JPanel pane = new JPanel();
        setContentPane(pane);
        pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        pane.setLayout(new GridLayout(2, 2, 2, 2));
        
        pane.add(new JLabel(I18nProvider.getText("gui.elements.exercise")));
        pane.add(assessmentsBox);
        
        JButton okButton = new JButton(I18nProvider.getText("gui.elements.ok"));
        okButton.setActionCommand(ACTION_OK);
        okButton.addActionListener(this);
        pane.add(okButton);
        
        JButton cancelButton = new JButton(I18nProvider.getText("gui.elements.cancel"));
        cancelButton.setActionCommand(ACTION_CANCEL);
        cancelButton.addActionListener(this);
        pane.add(cancelButton);
        
        setTitle(I18nProvider.getText("gui.elements.select_correction_replay"));
        pack();
        setLocationRelativeTo(parent);
        setModal(true);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getActionCommand().equals(ACTION_OK)) {
            int result = JOptionPane.showConfirmDialog(this, I18nProvider.getText("gui.warning.delete_dir_on_replay"),
                    I18nProvider.getText("gui.elements.replay"), JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                parent.replayCorrection((Assignment) assessmentsBox.getSelectedItem());
            }
        }
        setVisible(false);
    }
    
}

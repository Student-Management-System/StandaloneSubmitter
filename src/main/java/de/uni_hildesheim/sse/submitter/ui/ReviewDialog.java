package de.uni_hildesheim.sse.submitter.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;

/**
 * A dialog to review corrected exercises.
 * 
 * @author Adam Krafczyk
 * @author El-Sharkawy
 */
class ReviewDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = -2707409724385716679L;
    
    private static final String ACTION_OK = "Ok";
    private static final String ACTION_CANCEL = "Cancel";
    
    private JComboBox<Assignment> assessmentsBox;
    
    private Window parent;
    
    /**
     * Creates a {@link ReviewDialog} for the given parent window.
     * 
     * @param parent The parent window.
     * @param reviewedAssignments The list of assignments that are reviewed and may be selected in this dialog.
     */
    ReviewDialog(Window parent, List<Assignment> reviewedAssignments) {
        this.parent = parent;
        
        assessmentsBox = new JComboBox<>();
        assessmentsBox.setRenderer(new AssignmentComboxRenderer());
        reviewedAssignments.stream().forEach(assessmentsBox::addItem);
        
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
        dispose();
        if (evt.getActionCommand().equals(ACTION_OK)) {
            int result = JOptionPane.showConfirmDialog(parent, I18nProvider.getText("gui.warning.delete_dir_on_replay"),
                    I18nProvider.getText("gui.elements.replay"), JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                parent.replayCorrection((Assignment) assessmentsBox.getSelectedItem());
            }
        }
    }
    
}

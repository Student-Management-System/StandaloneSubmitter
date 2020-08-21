package de.uni_hildesheim.sse.submitter.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.tmatesoft.svn.core.SVNException;

import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.svn.Revision;

/**
 * A dialog for selecting the version to replay.
 * 
 * @author Adam Krafczyk
 */
class ReplayDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = -5045304735293095673L;

    private static final String OK_COMMAND = "ok";
    
    private static final String CANCEL_COMMAND = "cancel";
    
    private Window parent;
    
    /*
     * GUI-Elements
     */
    private JList<String> list;
    private List<Revision> revisions;
    
    /**
     * Creates a {@link ReplayDialog} with the given parent.
     * @param parent the parent window
     * @throws SVNException if unable to get repositories
     * @throws IOException if writing the files fails.
     */
    ReplayDialog(Window parent) throws SVNException, IOException {
        this.parent = parent;
        
        JButton okButton = new JButton("Ok");
        okButton.setActionCommand(OK_COMMAND);
        okButton.addActionListener(this);
        
        JButton cancelButton = new JButton(I18nProvider.getText("gui.elements.cancel"));
        cancelButton.setActionCommand(CANCEL_COMMAND);
        cancelButton.addActionListener(this);
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(okButton);
        bottomPanel.add(cancelButton);
        
        revisions = parent.getRemoteRepository().getHistory();
        DefaultListModel<String> listModel = new DefaultListModel<String>();
        for (int i = 0; i < revisions.size(); i++) {
            listModel.add(i, revisions.get(i).toString());
        }
        list = new JList<String>(listModel);
        
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(bottomPanel, BorderLayout.SOUTH);
        contentPane.add(new JScrollPane(list), BorderLayout.CENTER);
        
        pack();
        setLocationRelativeTo(parent);
        setTitle(I18nProvider.getText("gui.elements.select_replay"));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setModal(true);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (evt.getActionCommand().equals(CANCEL_COMMAND)) {
            dispose();
        } else if (evt.getActionCommand().equals(OK_COMMAND) && !list.isSelectionEmpty()) {
            long revision = revisions.get(list.getSelectedIndex()).getRevision();
            parent.replayRevision(revision);
            dispose();
        }
    }
    
}

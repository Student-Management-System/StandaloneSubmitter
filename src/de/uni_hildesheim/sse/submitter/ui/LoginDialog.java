package de.uni_hildesheim.sse.submitter.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.tmatesoft.svn.core.SVNAuthenticationException;

import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.settings.SubmissionConfiguration;
import de.uni_hildesheim.sse.submitter.svn.RemoteRepository;
import de.uni_hildesheim.sse.submitter.svn.ServerNotFoundException;
import net.ssehub.exercisesubmitter.protocol.backend.UnknownCredentialsException;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmitterProtocol;

/**
 * A dialog where group name, name and password can be specified.
 * 
 * @author Adam Krafczyk
 * @author El-Sharkawy
 */
class LoginDialog extends JDialog implements ActionListener {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 365531812487797101L;
    
    private SubmissionConfiguration config;
    
    private RemoteRepository repository;
    private SubmitterProtocol protocol;
    
    /*
     * GUI components
     */
    private JTextField nameField;
    private JPasswordField passwordField;
    private JLabel errorMessageLabel;
    
    /**
     * Creates this dialog for the given parent.
     * 
     * @param parent the parent window.
     */
    LoginDialog(Window parent) {
        this.config = parent.getConfiguration();
        this.protocol = parent.getNetworkProtocol();
        
        // Initialize components
        nameField = new JTextField(config.getUser());
        nameField.addActionListener(this);
        passwordField = new JPasswordField(config.getPW());
        passwordField.addActionListener(this);
        errorMessageLabel = new JLabel();
        errorMessageLabel.setForeground(Color.RED);
        JButton button = new JButton(I18nProvider.getText("gui.elements.login"));
        button.addActionListener(this);
        
        JPanel topPanel = new JPanel(new GridLayout(0, 2));
        topPanel.add(new JLabel(I18nProvider.getText("gui.elements.name")));
        topPanel.add(nameField);
        topPanel.add(new JLabel(I18nProvider.getText("gui.elements.password")));
        topPanel.add(passwordField);
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(button);
        
        JPanel contentPane = new JPanel();
        setContentPane(contentPane);
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout());
        contentPane.add(errorMessageLabel, BorderLayout.NORTH);
        contentPane.add(topPanel, BorderLayout.CENTER);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);
        
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent arg0) {}
            @Override
            public void windowIconified(WindowEvent arg0) {}
            @Override
            public void windowDeiconified(WindowEvent arg0) {}
            @Override
            public void windowDeactivated(WindowEvent arg0) {}
            @Override
            public void windowClosing(WindowEvent arg0) {
                System.exit(0);
            }
            @Override
            public void windowClosed(WindowEvent arg0) {}
            @Override
            public void windowActivated(WindowEvent arg0) {}
        });
        
        setTitle("Login");
        pack();
        setResizable(false);
        setModal(true);
        setLocationRelativeTo(parent);
        setVisible(true);
    }
    
    /**
     * Returns the {@link RemoteRepository}.
     * @return the {@link RemoteRepository}
     */
    RemoteRepository getRepository() {
        return repository;
    }
    
    @Override
    public void actionPerformed(ActionEvent evt) {
        String user = nameField.getText();
        String pw = new String(passwordField.getPassword());
        config.setUser(user);
        config.setPW(pw);

        
        String errorMessage = null;
        try {
            boolean success = protocol.login(user, pw);
            if (success) {
                try {
                    repository = new RemoteRepository(config, protocol);
                    // get repository list here to test if login information are correct
                    repository.getRepositories(RemoteRepository.MODE_SUBMISSION);
                } catch (ServerNotFoundException e) {
                    errorMessage = I18nProvider.getText("gui.error.server_not_found")
                            + " " + e.getAddress();
                } catch (SVNAuthenticationException e) {
                    errorMessage = I18nProvider.getText("gui.error.login_wrong");
                }
            } else {
                // TODO SE: Use I18n after revision here
                errorMessage = "Unknown error occured. Could no log into the Student Management System.";
            }
        } catch (UnknownCredentialsException e) {
            // TODO SE: Use I18n after revision here
            errorMessage = "Credentials are unknown by the Student Management System.";
        } catch (net.ssehub.exercisesubmitter.protocol.backend.ServerNotFoundException e) {
            // TODO SE: Use I18n after revision here
            errorMessage = "Could not reach the Student Management System.";
        }
       
        
        if (errorMessage == null) {
            dispose();
        } else {
            errorMessageLabel.setText(errorMessage);
            pack();
        }
    }
    
}

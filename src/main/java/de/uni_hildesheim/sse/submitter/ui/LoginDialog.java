package de.uni_hildesheim.sse.submitter.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.UIManager;

import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.settings.SubmissionConfiguration;
import de.uni_hildesheim.sse.submitter.settings.ToolSettings;
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
    private Window parent;
    
    /*
     * GUI components
     */
    private JTextField nameField;
    private JPasswordField passwordField;
    private JTextArea errorMessageLabel;
    private Popup capsLockWarning;
    
    /**
     * Creates this dialog for the given parent.
     * 
     * @param parent the parent window.
     */
    LoginDialog(Window parent) {
        this.config = parent.getConfiguration();
        this.protocol = parent.getNetworkProtocol();
        this.parent = parent;
        
        // Initialize components
        initUserComponents();
        createLayout();
        
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
     * Creates the layout of this dialog. Creates the content pane and adds the UI components.
     */
    private void createLayout() {
        JButton button = new JButton(I18nProvider.getText("gui.elements.login"));
        button.addActionListener(this);
        
        JPanel contentPane = new JPanel(new GridBagLayout());
        GridBagConstraints constr = new GridBagConstraints();
        constr.insets = new Insets(2, 2, 2, 2);
        
        constr.gridx = 0;
        constr.gridy = 0;
        constr.anchor = GridBagConstraints.WEST;
        constr.fill = GridBagConstraints.NONE;
        JLabel nameLbl = new JLabel(I18nProvider.getText("gui.elements.name"));
        nameLbl.setLabelFor(nameField);
        contentPane.add(nameLbl, constr);
        
        constr.gridx = 1;
        constr.gridy = 0;
        constr.anchor = GridBagConstraints.CENTER;
        constr.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(nameField, constr);
        
        constr.gridx = 0;
        constr.gridy = 1;
        constr.anchor = GridBagConstraints.WEST;
        constr.fill = GridBagConstraints.NONE;
        JLabel pwLbl = new JLabel(I18nProvider.getText("gui.elements.password"));
        pwLbl.setLabelFor(passwordField);
        contentPane.add(pwLbl, constr);
        
        constr.gridx = 1;
        constr.gridy = 1;
        constr.anchor = GridBagConstraints.CENTER;
        constr.fill = GridBagConstraints.HORIZONTAL;
        contentPane.add(passwordField, constr);
        
        constr.gridx = 0;
        constr.gridy = 2;
        constr.anchor = GridBagConstraints.CENTER;
        constr.fill = GridBagConstraints.HORIZONTAL;
        constr.gridwidth = 2;
        contentPane.add(errorMessageLabel, constr);
        constr.gridwidth = 1;
        
        constr.gridx = 1;
        constr.gridy = 3;
        constr.anchor = GridBagConstraints.EAST;
        constr.fill = GridBagConstraints.NONE;
        contentPane.add(button, constr);
        
        setContentPane(contentPane);
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    /**
     * Creates the items, which are also used outside of the constructor.
     */
    private void initUserComponents() {
        nameField = new JTextField(config.getUser(), 10);
        nameField.addActionListener(this);
        passwordField = new JPasswordField(config.getPW(), 10);
        passwordField.addActionListener(this);
        passwordField.addKeyListener(new KeyAdapter() {
            
            @Override
            public void keyPressed(KeyEvent evt) {
                capsLockWarn();
            }
        });
        passwordField.addFocusListener(new FocusListener() {
            
            @Override
            public void focusLost(FocusEvent evt) {
                hideCapsLockWarning();
            }
            
            @Override
            public void focusGained(FocusEvent evt) {
                capsLockWarn();
            }
        });
        errorMessageLabel = new JTextArea();
        errorMessageLabel.setWrapStyleWord(true);
        errorMessageLabel.setLineWrap(true);
        errorMessageLabel.setEditable(false);
        errorMessageLabel.setFocusable(false);
        errorMessageLabel.setBackground(UIManager.getColor("Label.background"));
        errorMessageLabel.setFont(UIManager.getFont("Label.font"));
        errorMessageLabel.setBorder(UIManager.getBorder("Label.border"));
        errorMessageLabel.setForeground(Color.RED);
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
            // First check: Check that credentials are supported by REST servers
            boolean success = protocol.login(user, pw);
            if (success) {
                try {
                    repository = new RemoteRepository(config, protocol);
                    // Double check: Check that credentials are also accepted by the submission server
                    success = repository.checkConnection();
                    if (!success) {
                        errorMessage = I18nProvider.getText("gui.error.login_wrong_repository",
                            ToolSettings.getConfig().getCourse().getTeamName(), 
                            ToolSettings.getConfig().getCourse().getTeamMail());                        
                    }
                } catch (ServerNotFoundException e) {
                    errorMessage = I18nProvider.getText("gui.error.server_not_found") + " " + e.getAddress();
                }
            } else {
                errorMessage = I18nProvider.getText("gui.error.unknown_error");
            }
        } catch (UnknownCredentialsException e) {
            errorMessage = I18nProvider.getText("gui.error.unknown_credentials");
        } catch (net.ssehub.exercisesubmitter.protocol.backend.ServerNotFoundException e) {
            errorMessage = I18nProvider.getText("gui.error.system_unreachable");
        }
       
        
        if (errorMessage == null) {
            dispose();
        } else {
            errorMessageLabel.setText(errorMessage);
            pack();
            setLocationRelativeTo(parent);
        }
    }
    
    /**
     * Warns the user if caps lock is pressed.
     */
    private void capsLockWarn() {
        boolean capsLockPressed = Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
        if (capsLockPressed) {
            if (null == capsLockWarning) {
                Point location = passwordField.getLocationOnScreen();
                JToolTip toolTip = new JToolTip();
                toolTip.setTipText(I18nProvider.getText("gui.warning.caps_lock"));
                int x = location.x;
                int y = location.y - (toolTip.getPreferredSize().height - 5);
                
                capsLockWarning = PopupFactory.getSharedInstance().getPopup(LoginDialog.this, toolTip, x, y);
                capsLockWarning.show();
            }
        } else {
            hideCapsLockWarning();
        }
    }
    
    /**
     * Hides the caps lock warning tooltip if it is displayed.
     */
    private void hideCapsLockWarning() {
        if (capsLockWarning != null) {
            capsLockWarning.hide();
            capsLockWarning = null;
        }
    }
}

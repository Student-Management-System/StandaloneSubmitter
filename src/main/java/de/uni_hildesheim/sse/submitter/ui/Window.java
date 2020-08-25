package de.uni_hildesheim.sse.submitter.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tmatesoft.svn.core.SVNException;

import de.uni_hildesheim.sse.submitter.Starter;
import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.settings.SubmissionConfiguration;
import de.uni_hildesheim.sse.submitter.settings.ToolConfiguration;
import de.uni_hildesheim.sse.submitter.settings.ToolSettings;
import de.uni_hildesheim.sse.submitter.settings.UiColorSettings;
import de.uni_hildesheim.sse.submitter.svn.ISubmissionOutputHandler;
import de.uni_hildesheim.sse.submitter.svn.RemoteRepository;
import de.uni_hildesheim.sse.submitter.svn.Revision;
import de.uni_hildesheim.sse.submitter.svn.TestSubmitterProtocol;
import de.uni_hildesheim.sse.submitter.svn.hookErrors.ErrorDescription;
import net.ssehub.exercisesubmitter.protocol.backend.NetworkException;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmissionTarget;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmitterProtocol;

/**
 * Main window for the submitter.
 * @author Adam Krafczyk
 *
 */
public class Window extends JFrame implements ISubmissionOutputHandler {
    
    private static final Logger LOGGER = LogManager.getLogger(Window.class);

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 3655318432262797101L;

    /*
     * GUI elements
     */
    private JTextField sourceDirectoryField;
    private LogArea logArea;
    private JComboBox<Assignment> assignmentBox;
    private JButton submitBtn;
    private JButton historyBtn;
    private JButton replayBtn;
    private JButton reviewBtn;
    private JButton browseBtn;
    
    private ButtonProgressAnimator progressAnimator;
    
    private SubmissionConfiguration config;
    private RemoteRepository repository;
    private SubmitterProtocol protocol;

    /**
     * Sole constructor for this class.
     */
    public Window() {
        // Set window properties
        String title = ToolSettings.getConfig().getProgramName();
        String version = ToolSettings.getConfig().getProgramVersion();
        if (null != version) {
            title += " - v" + version;
        }
        setTitle(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 500);
        ToolConfiguration tConf = ToolSettings.getConfig();
        if (Starter.DEBUG_NO_MGMT_SYTEM) {
            protocol = new TestSubmitterProtocol(tConf.getAuthURL(), tConf.getMgmtURL(), tConf.getCourse().getCourse(),
                    tConf.getRepositoryURL());
        } else {
            protocol = new SubmitterProtocol(tConf.getAuthURL(), tConf.getMgmtURL(), tConf.getCourse().getCourse(),
                    tConf.getRepositoryURL());
        }
        String semester = tConf.getCourse().getSemester();
        if (null != semester) {
            protocol.setSemester(semester);
        }
        
        // Initialize components
        initComponents();
        createLayout();
        
        setVisible(true);
        setLocationRelativeTo(null);
        
        config = SubmissionConfiguration.load();
        LoginDialog dialog = new LoginDialog(this, config, protocol);
        dialog.setVisible(true);
        repository = dialog.getRepository();
        try {
            setAssignmentMenu(protocol.getOpenAssignments());
        } catch (NetworkException e) {
            // This shouldn't happen here... (since it worked in LoginDialog)
            showErrorMessage(I18nProvider.getText("gui.error.repos_not_found"));
        }
        if (config.getProjectFolder() != null) {
            setSelectedPath(config.getProjectFolder());
        }
        
        if (Starter.DEBUG) {
            sourceDirectoryField.setText(new File("example").getAbsolutePath());
        }
    }
    
    /**
     * Returns the path for the remote SVN repository of the currently selected exercise.
     * 
     * @return The remote path of the current exercise.
     * 
     * @throws NetworkException If the group name could not be queried from the student management system.
     */
    public String getRemotePathOfCurrentExercise() throws NetworkException {
        Assignment currentExercise = config.getExercise();
        return protocol.getPathToSubmission(currentExercise).getAbsolutePathInRepository();
    }
    
    /**
     * Returns a list of {@link Assignment}s that are currently in the reviewed state.
     * 
     * @return List of {@link Assignment}s that were reviewed.
     * 
     * @throws NetworkException If fetching the list fails.
     */
    public List<Assignment> getAssignmentsInReviewedState() throws NetworkException {
        return protocol.getReviewedAssignments();
    }
    
    /**
     * Returns the network protocol to communicate with the <b>student management system</b> via it's REST interface.
     * @return The network protocol for the REST server.
     */
    public SubmitterProtocol getNetworkProtocol() {
        return protocol;
    }
    
    /**
     * Creates a button.
     * 
     * @param text the text on the button.
     * @param actionCommand the action command.
     * @param listener the listener for the button.
     * @return the button.
     */
    private JButton createButton(String text, String actionCommand, ButtonListener listener) {
        JButton button = new JButton(text);
        button.setActionCommand(actionCommand);
        button.addActionListener(listener);
        return button;
    }
    
    /**
     * Sets the assignments that the user can choose.
     * 
     * @param assignments List of defined assignments (homework, exams, exercises, ...)
     */
    private void setAssignmentMenu(List<Assignment> assignments) {
        assignmentBox.removeAllItems();
        assignments.stream()
            .forEach(a -> assignmentBox.addItem(a));
        config.setExercise((Assignment) assignmentBox.getSelectedItem());
    }
    
    /**
     * Creates components that are stored as class attributes.
     */
    private void initComponents() {
        ButtonListener listener = new ButtonListener(this);
        
        sourceDirectoryField = new JTextField();
        sourceDirectoryField.getDocument().addDocumentListener(new DocumentListener() {
            
            @Override
            public void removeUpdate(DocumentEvent evt) {
                config.setProjectFolder(new File(sourceDirectoryField.getText()));
            }
            
            @Override
            public void insertUpdate(DocumentEvent evt) {
                config.setProjectFolder(new File(sourceDirectoryField.getText()));
            }
            
            @Override
            public void changedUpdate(DocumentEvent evt) {
                config.setProjectFolder(new File(sourceDirectoryField.getText()));
            }
            
        });
        
        logArea = new LogArea();
        assignmentBox = new JComboBox<>();
        assignmentBox.setRenderer(new AssignmentComboxRenderer());
        
        assignmentBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                config.setExercise((Assignment) assignmentBox.getSelectedItem());
            }
        });
        
        browseBtn = createButton(I18nProvider.getText("gui.elements.browse"),
                ButtonListener.ACTION_BROWSE_FOLDER, listener);
        replayBtn = createButton(I18nProvider.getText("gui.elements.replay"),
                ButtonListener.ACTION_REPLAY, listener);
        submitBtn = createButton(I18nProvider.getText("gui.elements.submit"),
                ButtonListener.ACTION_SUBMIT, listener);
        historyBtn = createButton(I18nProvider.getText("gui.elements.history"),
                ButtonListener.ACTION_HISTORY, listener);
        reviewBtn = createButton(I18nProvider.getText("gui.elements.review"),
                ButtonListener.ACTION_REVIEW, listener);
    }
    
    /**
     * Creates the layout and adds them to this window.
     */
    private void createLayout() {
        // Create repository chooser panel
        JPanel repoChooserPanel = new JPanel();
        repoChooserPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        repoChooserPanel.add(new JLabel(I18nProvider.getText("gui.elements.exercise")));
        repoChooserPanel.add(assignmentBox);
        
        // Create source-folder choose area
        JPanel topTopPanel = new JPanel(new BorderLayout(5, 5));
        topTopPanel.add(new JLabel(I18nProvider.getText("gui.elements.srcfolder")), BorderLayout.WEST);
        topTopPanel.add(sourceDirectoryField, BorderLayout.CENTER);
        topTopPanel.add(browseBtn, BorderLayout.EAST);
        topTopPanel.add(new JLabel(""), BorderLayout.NORTH);
        topTopPanel.add(repoChooserPanel, BorderLayout.SOUTH);
        
        // Create action button area
        JPanel topBottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBottomPanel.add(reviewBtn);
        topBottomPanel.add(replayBtn);
        topBottomPanel.add(historyBtn);
        topBottomPanel.add(submitBtn);
        
        // Join source-folder choose area and action button area
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        topPanel.add(topTopPanel);
        topPanel.add(topBottomPanel);
        
        // Join everything in content pane
        JPanel contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout());
        contentPane.add(topPanel, BorderLayout.NORTH);
 //       contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(logArea, BorderLayout.CENTER);
        setContentPane(contentPane);
    }

    /**
     * Tries to submit the specified folder to the submission server.
     * Handles returned error messages.
     * @param folder A top level folder for a java project.
     */
    private void submitFolder(File folder) {
        new SubmissionThread(this, config, folder).start();
    }
    
    /**
     * Toggles whether the buttons are click-able or not.
     * @param enabled <code>true</code> when the buttons should become enabled
     */
    void toggleButtons(boolean enabled) {
        if (enabled && this.progressAnimator != null) {
            this.progressAnimator.stop();
            this.progressAnimator = null;
        }
        
        submitBtn.setEnabled(enabled);
        historyBtn.setEnabled(enabled);
        replayBtn.setEnabled(enabled);
        reviewBtn.setEnabled(enabled);
    }
    
    /**
     * Adds a {@link ButtonProgressAnimator} to the given button. This class takes care that the progress animator
     * is stopped when appropriate.
     * 
     * @param button The button to add the progress animator to.
     */
    void addProgressAnimator(JButton button) {
        if (this.progressAnimator != null) {
            this.progressAnimator.stop();
        }
        this.progressAnimator = new ButtonProgressAnimator(button);
        this.progressAnimator.start();
    }
    
    /**
     * Returns the currently selected source-path. May not exist.
     * 
     * @return the path as String.
     */
    String getSelectedPath() {
        return sourceDirectoryField.getText();
    }
    
    /**
     * Submits the selected project.
     */
    void submit() {
        clearLog();
        showInfoMessage(I18nProvider.getText("gui.log.submitting"));
        File projectFolder = new File(sourceDirectoryField.getText());
        if (projectFolder.isDirectory()) {
            submitFolder(projectFolder);
        } else {
            showErrorMessage(sourceDirectoryField.getText() + " "
                    + I18nProvider.getText("errors.messages.not_a_direcotry"));
            toggleButtons(true);
        }
    }
    
    /**
     * Shows the history to the user.
     * @param history list with lines of history; each line should be one entry
     */
    void showHistory(List<Revision> history) {
        clearLog();
        showInfoMessage(I18nProvider.getText("gui.log.history"));
        for (Revision line : history) {
            showInfoMessage(line.toString());
        }
    }
    
    /**
     * Clears the log.
     */
    void clearLog() {
        logArea.clear();
    }
    
    /**
     * Sets the currently selected source-path.
     * 
     * @param path the new path.
     */
    void setSelectedPath(File path) {
        sourceDirectoryField.setText(path.getAbsolutePath());
        config.setProjectFolder(path);
    }
    
    /**
     * Getter for the {@link RemoteRepository}.
     * @return the {@link RemoteRepository}
     */
    RemoteRepository getRemoteRepository() {
        return repository;
    }
    
    /**
     * Replay to a selected revision.
     * @param revision the revision to replay
     */
    void replayRevision(long revision) {
        clearLog();
        showInfoMessage(I18nProvider.getText("gui.log.replaying"));
        try {
            getRemoteRepository().replay(revision, new File(getSelectedPath()), getRemotePathOfCurrentExercise());
            showInfoMessage(I18nProvider.getText("gui.log.replaying_successful"));
        } catch (SVNException | IOException | NetworkException e) {
            LOGGER.error("Could not replay submission from server", e);
            showErrorMessage(I18nProvider.getText("gui.error.replay_error"));
        }
    }
    
    /**
     * Replays a corrected exercise.
     * @param assignment The corrected assignment (exercise / exam) to replay.
     */
    void replayCorrection(Assignment assignment) {
        toggleButtons(false);
        addProgressAnimator(reviewBtn);
        clearLog();
        
        config.setExercise(assignment);
        
        showInfoMessage(I18nProvider.getText("gui.log.replaying"));
        
        new Thread(() -> {
            try {
                getRemoteRepository().replay(new File(getSelectedPath()), getRemotePathOfCurrentExercise());
                config.setExercise((Assignment) assignmentBox.getSelectedItem());
                
                SwingUtilities.invokeLater(() -> {
                    showInfoMessage(I18nProvider.getText("gui.log.replaying_successful"));
                    toggleButtons(true);
                });
                
            } catch (SVNException e) {
                if (e.getMessage().contains("404 Not Found")) {
                    try {
                        SubmissionTarget dest = protocol.getPathToSubmission(assignment);
                        SwingUtilities.invokeLater(() ->
                            showErrorMessage(I18nProvider.getText("gui.error.replay.no_submission_error",
                                dest.getAssignmentName(), dest.getSubmissionPath(),
                                ToolSettings.getConfig().getCourse().getTeamMail())));
                    } catch (NetworkException e1) {
                        LOGGER.error("Could not replay submission from server", e);
                        SwingUtilities.invokeLater(() ->
                            showErrorMessage(I18nProvider.getText("gui.error.replay_error")));
                    }
                } else {
                    LOGGER.error("Could not replay submission from server", e);
                    SwingUtilities.invokeLater(() -> showErrorMessage(I18nProvider.getText("gui.error.replay_error")));
                }
            } catch (IOException | NetworkException e) {
                LOGGER.error("Could not replay submission from server", e);
                
                SwingUtilities.invokeLater(() -> showErrorMessage(I18nProvider.getText("gui.error.replay_error")));
            }
        }).start();
    }

    /**
     * Short hand method to retrieve the color settings for the application.
     * @return The color settings of the tool.
     */
    private UiColorSettings colors() {
        return ToolSettings.getConfig().getColorSettings();
    }
    
    @Override
    public void showErrorMessage(String message) {
        logArea.append(I18nProvider.getText("errors.messages.error") + ": " + message,
            colors().getErrorColor(), true);
    }

    @Override
    public void showInfoMessage(String message) {
        logArea.append(message);
        logArea.append("\n");
    }

    @Override
    public void showInfoMessage(String message, ErrorDescription[] descriptions) {
        logArea.append(message);
        logArea.append("\n");
        for (ErrorDescription description : descriptions) {
            if (null != description.getTool()) {
                
                logArea.append(I18nProvider.getText("gui.tool." + description.getTool().getToolName()) + " ",
                        colors().getColor(description.getTool()), true);
                
            }
            if (null != description.getSeverity()) {
                switch (description.getSeverity()) {
                case ERROR:
                    logArea.append(I18nProvider.getText("gui.log.error_in") + " ",
                        colors().getErrorColor(), false);
                    break;
                case WARNING:
                    logArea.append(I18nProvider.getText("gui.log.warning_in") + " ",
                        colors().getWarningColor(), false);
                    break;
                default:
                    logArea.append(I18nProvider.getText("gui.log.prolem_in") + " ");
                    break;
                }
            }
            if (null != description.getFile()) {
                logArea.append(description.getFile() + " ");
            } else {
                logArea.append(I18nProvider.getText("gui.log.unspecified_files") + " ");
            }
            if (description.getLine() > 0) {
                logArea.append(I18nProvider.getText("gui.log.line", description.getLine()) + ":");
            }
            logArea.append("\n");
            if (null != description.getMessage()) {
                logArea.append(" -> " + I18nProvider.getText("gui.log.cause") + ": " + description.getMessage());
                logArea.append("\n");
            }
        }
    }

}

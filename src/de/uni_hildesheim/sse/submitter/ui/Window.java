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

import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;

import de.uni_hildesheim.sse.submitter.Starter;
import de.uni_hildesheim.sse.submitter.conf.ConfigReader;
import de.uni_hildesheim.sse.submitter.conf.Configuration;
import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.settings.Settings;
import de.uni_hildesheim.sse.submitter.svn.ISubmissionOutputHandler;
import de.uni_hildesheim.sse.submitter.svn.RemoteRepository;
import de.uni_hildesheim.sse.submitter.svn.Revision;
import de.uni_hildesheim.sse.submitter.svn.ServerNotFoundException;
import de.uni_hildesheim.sse.submitter.svn.SubmissionResultHandler;
import de.uni_hildesheim.sse.submitter.svn.hookErrors.ErrorDescription;

/**
 * Main window for the submitter.
 * @author Adam Krafczyk
 *
 */
public class Window extends JFrame implements ISubmissionOutputHandler {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 3655318432262797101L;

    /*
     * GUI elements
     */
    private JTextField sourceDirectoryField;
    private LogArea logArea;
    private JComboBox<String> reposBox;
    private JButton submitBtn;
    private JButton historyBtn;
    private JButton replayBtn;
    private JButton reviewBtn;
    
    private SubmissionResultHandler translator;
    private Configuration config;
    private RemoteRepository repository;

    /**
     * Sole constructor for this class.
     */
    public Window() {
        // Set window properties
        String title = Settings.getSettings("prog.name", "ExerciseSubmitter");
        String version = Settings.getSettings("prog.version");
        if (null != version) {
            title += " - v" + version;
        }
        setTitle(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 500);
        
        // Initialize components
        initComponents();
        
        setVisible(true);
        setLocationRelativeTo(null);
        
        config = ConfigReader.read();
        translator = new SubmissionResultHandler(this);
        LoginDialog dialog = new LoginDialog(this);
        repository = dialog.getRepository();
        try {
            setRepositoryList(repository.getRepositories(RemoteRepository.MODE_SUBMISSION));
        } catch (ServerNotFoundException | SVNAuthenticationException e) {
            // This shouldn't happen here... (since it worked in LoginDialog)
            showErrorMessage(I18nProvider.getText("gui.error.repos_not_found"));
        }
        
        if (Starter.DEBUG) {
            toggleButtons(false);
            sourceDirectoryField.setText(new File("example").getAbsolutePath());
            submit();
        }
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
     * Sets the repositories that the user can choose.
     * 
     * @param repos list with all repositories names
     */
    private void setRepositoryList(List<String> repos) {
        reposBox.removeAllItems();
        for (String repo : repos) {
            reposBox.addItem(repo);
        }
        config.setExercise((String) reposBox.getSelectedItem());
    }
    
    /**
     * Creates and adds all components.
     */
    private void initComponents() {
        ButtonListener listener = new ButtonListener(this);
        
        sourceDirectoryField = new JTextField();
        logArea = new LogArea();
        reposBox = new JComboBox<String>();
        
        reposBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                config.setExercise((String) reposBox.getSelectedItem());
            }
        });
        
        JButton browseBtn = createButton(I18nProvider.getText("gui.elements.browse"),
                ButtonListener.ACTION_BROWSE_FOLDER, listener);
        replayBtn = createButton(I18nProvider.getText("gui.elements.replay"),
                ButtonListener.ACTION_REPLAY, listener);
        submitBtn = createButton(I18nProvider.getText("gui.elements.submit"),
                ButtonListener.ACTION_SUBMIT, listener);
        historyBtn = createButton(I18nProvider.getText("gui.elements.history"),
                ButtonListener.ACTION_HISTORY, listener);
        reviewBtn = createButton(I18nProvider.getText("gui.elements.review"),
                ButtonListener.ACTION_REVIEW, listener);
        
        // Create repository chooser panel
        JPanel repoChooserPanel = new JPanel();
        repoChooserPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        repoChooserPanel.add(new JLabel(I18nProvider.getText("gui.elements.exercise")));
        repoChooserPanel.add(reposBox);
        
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
        new SubmissionThread(this, config, translator, folder).start();
    }
    
    /**
     * Toggles whether the buttons are click-able or not.
     * @param enabled <code>true</code> when the buttons should become enabled
     */
    void toggleButtons(boolean enabled) {
        submitBtn.setEnabled(enabled);
        historyBtn.setEnabled(enabled);
        replayBtn.setEnabled(enabled);
        reviewBtn.setEnabled(enabled);
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
        // logArea.setText("");
        logArea.clear();
    }
    
    /**
     * Sets the currently selected source-path.
     * 
     * @param path the new path.
     */
    void setSelectedPath(String path) {
        sourceDirectoryField.setText(path);
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
            getRemoteRepository().replay(getSelectedPath(), revision);
            showInfoMessage(I18nProvider.getText("gui.log.replaying_successful"));
        } catch (SVNException | IOException e) {
            e.printStackTrace();
            showErrorMessage(I18nProvider.getText("gui.error.replay_error"));
        }
    }
    
    /**
     * Replays a corrected exercise.
     * @param repoName The name of the exercise.
     */
    void replayCorrection(String repoName) {
        toggleButtons(false);
        clearLog();
        
        config.setExercise(repoName);
        
        showInfoMessage(I18nProvider.getText("gui.log.replaying"));
        try {
            getRemoteRepository().replay(getSelectedPath());
            showInfoMessage(I18nProvider.getText("gui.log.replaying_successful"));
        } catch (SVNException | IOException e) {
            e.printStackTrace();
            showErrorMessage(I18nProvider.getText("gui.error.replay_error"));
        }
        
        config.setExercise((String) reposBox.getSelectedItem());
        
        toggleButtons(true);
    }

    @Override
    public void showErrorMessage(String message) {
        logArea.append(I18nProvider.getText("errors.messages.error") + ": " + message,
                Settings.getSettings("ui.log.error.color"), true);
    }

    @Override
    public Configuration getConfiguration() {
        return config;
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
            if (null != description.getType()) {
                switch (description.getType()) {
                case JAVAC:
                    logArea.append(I18nProvider.getText("gui.log.compile") + " "
                            , Settings.getSettings("ui.log.javac.color"), true);
                    break;
                case CHECKSTYLE:
                    logArea.append(I18nProvider.getText("gui.log.checkstyle") + " "
                            , Settings.getSettings("ui.log.checkstyle.color"), true);
                    break;
                case JUNIT:
                    logArea.append(I18nProvider.getText("gui.log.logic") + " "
                            , Settings.getSettings("ui.log.junit.color"), true);
                    break;
                case COMMIT:
                    logArea.append(I18nProvider.getText("gui.log.commit") + " "
                            , Settings.getSettings("ui.log.commit.color"), true);
                    break;
                case HOOK:
                    logArea.append(I18nProvider.getText("gui.log.hook") + " "
                            , Settings.getSettings("ui.log.hook.color"), true);
                    break;
                default:
                    break;
                }
            }
            if (null != description.getSeverity()) {
                switch (description.getSeverity()) {
                case ERROR:
                    logArea.append(I18nProvider.getText("gui.log.error_in") + " "
                            , Settings.getSettings("ui.log.error.color"), false);
                    break;
                case WARNING:
                    logArea.append(I18nProvider.getText("gui.log.warning_in") + " "
                            , Settings.getSettings("ui.log.warning.color"), false);
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
            if (null != description.getLines() && !description.getLines().isEmpty()) {
                logArea.append(I18nProvider.getText("gui.log.line") + " " + description.getLines() + ":");
            }
            logArea.append("\n");
            if (null != description.getCode()) {
                logArea.append(" -> " + I18nProvider.getText("gui.log.error") + ": " + description.getCode());
                logArea.append("\n");
            }
            if (null != description.getSolution()) {
                logArea.append(" -> " + I18nProvider.getText("gui.log.cause") + ": " + description.getSolution());
                logArea.append("\n");
            }
        }
    }

}

package de.uni_hildesheim.sse.submitter.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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

import de.uni_hildesheim.sse.submitter.i18n.I18nProvider;
import de.uni_hildesheim.sse.submitter.settings.ToolSettings;
import de.uni_hildesheim.sse.submitter.settings.UiColorSettings;
import de.uni_hildesheim.sse.submitter.svn.ISubmissionOutputHandler;
import de.uni_hildesheim.sse.submitter.svn.Revision;
import de.uni_hildesheim.sse.submitter.svn.hookErrors.ErrorDescription;
import net.ssehub.exercisesubmitter.protocol.frontend.Assignment;

/**
 * Main window for the submitter.
 * 
 * @author Adam Krafczyk
 */
public class StandaloneSubmitterWindow extends JFrame implements ISubmissionOutputHandler {
    
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
    
    private StandaloneSubmitter model;
    
    /**
     * Sole constructor for this class.
     * 
     * @param model The {@link StandaloneSubmitter} to use.
     */
    public StandaloneSubmitterWindow(StandaloneSubmitter model) {
        this.model = model;
        this.model.setOutputHandler(this);
        
        // Initialize components
        initComponents();
        createLayout();
        
        // Set window properties
        String title = ToolSettings.getConfig().getProgramName();
        String version = ToolSettings.getConfig().getProgramVersion();
        if (null != version) {
            title += ' ' + version;
        }
        setTitle(title);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 500);
        
        setVisible(true);
        setLocationRelativeTo(null);
    }
    
    /**
     * Call this after the {@link StandaloneSubmitter} has been initialized with login data.
     */
    public void afterLogin() {
        if (this.model.isLoggedIn()) {
            List<Assignment> openAssignments = this.model.getOpenAssignments();
            if (openAssignments != null) {
                setAssignmentMenu(openAssignments);
            }
            
            if (this.model.getDirectoryToSubmit() != null) {
                this.sourceDirectoryField.setText(model.getDirectoryToSubmit().getAbsolutePath());
            }
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
     * Sets the assignments that the user can choose.
     * 
     * @param assignments List of defined assignments (homework, exams, exercises, ...)
     */
    private void setAssignmentMenu(List<Assignment> assignments) {
        assignmentBox.removeAllItems();
        assignments.stream()
            .forEach(a -> assignmentBox.addItem(a));
        model.setSelectedExercise((Assignment) assignmentBox.getSelectedItem());
    }
    
    /**
     * Creates components that are stored as class attributes.
     */
    private void initComponents() {
        ButtonListener listener = new ButtonListener(this, this.model);
        
        sourceDirectoryField = new JTextField();
        sourceDirectoryField.getDocument().addDocumentListener(new DocumentListener() {
            
            /**
             * Called for all changes on the textfield.
             */
            private void onChange() {
                model.setDirectoryToSubmit(new File(sourceDirectoryField.getText()));
            }
            
            @Override
            public void removeUpdate(DocumentEvent evt) {
                onChange();
            }
            
            @Override
            public void insertUpdate(DocumentEvent evt) {
                onChange();
            }
            
            @Override
            public void changedUpdate(DocumentEvent evt) {
                onChange();
            }
            
        });
        
        logArea = new LogArea();
        assignmentBox = new JComboBox<>();
        assignmentBox.setRenderer(new AssignmentComboxRenderer());
        
        assignmentBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                model.setSelectedExercise((Assignment) assignmentBox.getSelectedItem());
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
     * Calls {@link #addProgressAnimator(JButton)} with the review button.
     */
    void addProgressAnimatorToReviewButton() {
        addProgressAnimator(reviewBtn);
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
     * Short hand method to retrieve the color settings for the application.
     * @return The color settings of the tool.
     */
    private UiColorSettings colors() {
        return ToolSettings.getConfig().getColorSettings();
    }
    
    @Override
    public void showErrorMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(I18nProvider.getText("errors.messages.error") + ": " + message,
                    colors().getErrorColor(), true);
        });
    }

    @Override
    public void showInfoMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message);
            logArea.append("\n");
        });
    }

    @Override
    public void showInfoMessage(String message, ErrorDescription[] descriptions) {
        SwingUtilities.invokeLater(() -> {
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
        });
    }
    
}

package de.uni_hildesheim.sse.submitter.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.Timer;

/**
 * A timer that regularly updates the button text to indicate a running task.
 * <ol>
 *  <li>Create an instance</li>
 *  <li>Call {@link #start()}</li>
 *  <li>(do the task asynchronously)</li>
 *  <li>Call {@link #stop()}</li>
 * </ol>
 * 
 * @author Adam
 */
class ButtonProgressAnimator extends Timer implements ActionListener {

    private static final long serialVersionUID = 5163487331638112464L;

    private static final char[] ANIMATION_STEPS = {'-', '\\', '/'}; 
    
    private static final int ANIMATION_INTERVAL_MS = 200;
    
    private int currentStep;
    
    private String originalText;
    
    private JButton button;
    
    /**
     * Creates an updater for the given button.
     * 
     * @param button The button to update in regular intervals.
     */
    public ButtonProgressAnimator(JButton button) {
        super(ANIMATION_INTERVAL_MS, null);
        addActionListener(this);
        
        this.button = button;
        this.originalText = button.getText();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        currentStep++;
        currentStep %= ANIMATION_STEPS.length;
        
        button.setText(originalText + ' ' + ANIMATION_STEPS[currentStep]);
    }
    
    @Override
    public void stop() {
        super.stop();
        button.setText(originalText);
    }
    
}

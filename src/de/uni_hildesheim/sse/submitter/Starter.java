package de.uni_hildesheim.sse.submitter;

import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.logging.log4j.LogManager;

import de.uni_hildesheim.sse.submitter.settings.ToolSettings;
import de.uni_hildesheim.sse.submitter.svn.TestSubmitterProtocol;
import de.uni_hildesheim.sse.submitter.ui.Window;

/**
 * Entry point for the whole program.
 * @author El-Sharkawy
 *
 */
public class Starter {
    
    /**
     * General debug mode.
     */
    public static final boolean DEBUG = false;
    
    /**
     * Disable management submission. Will emulate a submission for Testblatt01Aufgabe01/JP001.
     * See {@link TestSubmitterProtocol}.
     */
    public static final boolean DEBUG_NO_MGMT_SYTEM = true;
    
    /**
     * Disable SVN repository submission. Will emulate a hook response with a POST-commit failure with a variety of
     * different errors and warnings. See SubmissionThread.createMockErrors().
     */
    public static final boolean DEBUG_NO_SUBMISSION = false;
    
    /**
     * Starts the program.
     * @param args Will be ignored.
     */
    public static void main(String[] args) {
        try {
            ToolSettings.INSTANCE.init();
        } catch (IOException e) {
            LogManager.getLogger(Starter.class).fatal("Could not load configuration", e);
        }
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
            LogManager.getLogger(Starter.class).info("Could not switch UI to native look and feel", e);
        }
        
        if (DEBUG) {
            Locale.setDefault(Locale.GERMANY);
        }
        new Window();
    }

}

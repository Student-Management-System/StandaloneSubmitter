package de.uni_hildesheim.sse.submitter;

import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.uni_hildesheim.sse.submitter.settings.SubmissionConfiguration;
import de.uni_hildesheim.sse.submitter.settings.ToolConfiguration;
import de.uni_hildesheim.sse.submitter.settings.ToolSettings;
import de.uni_hildesheim.sse.submitter.svn.TestSubmitterProtocol;
import de.uni_hildesheim.sse.submitter.ui.LoginDialog;
import de.uni_hildesheim.sse.submitter.ui.StandaloneSubmitter;
import de.uni_hildesheim.sse.submitter.ui.StandaloneSubmitterWindow;
import net.ssehub.exercisesubmitter.protocol.frontend.SubmitterProtocol;

/**
 * Entry point for the whole program.
 * @author El-Sharkawy
 *
 */
public class Starter {
    
    /**
     * Disable management submission. Will emulate a submission for Testblatt01Aufgabe01/JP001.
     * See {@link TestSubmitterProtocol}.
     */
    public static final boolean DEBUG_NO_MGMT_SYTEM = Boolean.getBoolean("submitter.debug.no_mgmt");
    
    /**
     * Disable SVN repository submission. Will emulate a hook response with a POST-commit failure with a variety of
     * different errors and warnings. See SubmissionThread.createMockErrors().
     */
    public static final boolean DEBUG_NO_SUBMISSION = Boolean.getBoolean("submitter.debug.no_submission");

    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * Starts the program.
     * @param args Will be ignored.
     */
    public static void main(String[] args) {
        try {
            ToolSettings.INSTANCE.init();
        } catch (IOException e) {
            LOGGER.fatal("Could not load configuration", e);
        }
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
            LOGGER.warn("Could not switch UI to native look and feel", e);
        }
        
        ToolConfiguration tConf = ToolSettings.getConfig();
        SubmitterProtocol protocol;
        if (DEBUG_NO_MGMT_SYTEM) {
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
        SubmissionConfiguration config = SubmissionConfiguration.load();
        StandaloneSubmitter model = new StandaloneSubmitter(config, protocol);
        
        SwingUtilities.invokeLater(() -> {
            StandaloneSubmitterWindow window = new StandaloneSubmitterWindow(model);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
            
            LoginDialog dialog = new LoginDialog(window, model);
            dialog.setVisible(true);
            
            window.afterLogin();
        });
    }

}

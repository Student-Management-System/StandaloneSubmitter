package de.uni_hildesheim.sse.submitter;

import java.io.IOException;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;

import de.uni_hildesheim.sse.submitter.settings.ToolSettings;
import de.uni_hildesheim.sse.submitter.ui.Window;

/**
 * Entry point for the whole program.
 * @author El-Sharkawy
 *
 */
public class Starter {
    public static final boolean DEBUG = false;
    
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
        
        if (DEBUG) {
            Locale.setDefault(Locale.GERMANY);
        }
        new Window();
    }

}

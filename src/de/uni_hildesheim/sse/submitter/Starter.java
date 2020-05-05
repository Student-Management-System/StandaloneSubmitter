package de.uni_hildesheim.sse.submitter;

import java.util.Locale;

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
        if (DEBUG) {
            Locale.setDefault(Locale.GERMANY);
        }
        new Window();
    }

}

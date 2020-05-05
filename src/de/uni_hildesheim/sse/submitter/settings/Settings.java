package de.uni_hildesheim.sse.submitter.settings;

import java.util.Properties;

import de.uni_hildesheim.sse.submitter.AbstractPropertiesReader;

/**
 * Loads the settings from the integrated properties file.
 * @author El-Sharkawy
 *
 */
public class Settings extends AbstractPropertiesReader {

    private static final Settings INSTANCE = new Settings();

    private Properties settings;

    /**
     * Private singleton constructor.
     */
    private Settings() {
        settings = loadProperties("settings.properties");
    }

    /**
     * Searches for the property with the specified key in the settings file.
     * The method returns the specified <tt>defaultValue</tt> if the property is not found.
     * This method should be used for crucial settings, which must not be null.
     *
     * @param   key   The settings key, which should be load.
     * @param   defaultValue   The fall back value.
     * @return  the value in this property list with the specified key value.
     * @see     #getSettings(String)
     */
    public static String getSettings(String key, String defaultValue) {
        return INSTANCE.settings.getProperty(key, defaultValue);
    }

    /**
     * Searches for the property with the specified key in the settings file.
     * The method returns <code>null</code> if the property is not found.
     *
     * @param   key   The settings key, which should be load.
     * @return  the value in this property list with the specified key value, maybe <tt>null</tt>
     * @see     #getSettings(String, String)
     */
    public static String getSettings(String key) {
        return INSTANCE.settings.getProperty(key);
    }
}

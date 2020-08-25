package de.uni_hildesheim.sse.submitter.i18n;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is responsible for creating user texts in different languages. The instance of this class will
 * automatically detect the default language of the user and tries to create output texts in this language.
 * 
 * @author El-Sharkawy
 * @author Adam
 */
public class I18nProvider {

    /**
     * Singleton instance of this class.
     */
    public static final I18nProvider INSTANCE = new I18nProvider();
    
    private static final Logger LOGGER = LogManager.getLogger();

    private ResourceBundle messages;
    

    /**
     * Sole constructor for this class, will load user texts in the correct language.
     */
    private I18nProvider() {
        init();
    }

    /**
     * Initializes the translation messages. This may be called explicitly after the {@link Locale} has been changed.
     */
    public void init() {
        messages = ResourceBundle.getBundle("de.uni_hildesheim.sse.submitter.i18n.messages");
    }
    
    /**
     * Returns a user text for the given key.
     * 
     * @param key The name of the user text inside the properties file.
     * @return The user text or an empty string if the key wasn't found.
     */
    public static String getText(String key) {
        String result;
        try {
            result = INSTANCE.messages.getString(key);
        } catch (MissingResourceException e) {
            LOGGER.error("Missing translation key", e);
            result = null;
        }
        
        return result;
    }
    
    /**
     * Returns a user text for the given key and uses {@link String#format(String, Object...)} to insert parameters.
     * @param key The name of the user text inside the properties file.
     * @param args Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.
     * @return The user text or an empty string if the key wasn't found.
     */
    public static String getText(String key, Object... args) {
        String formatString = getText(key);
        
        String result = formatString;
        if (null != formatString) {
            result = String.format(formatString, args);
        }
        
        return result;
    }
}

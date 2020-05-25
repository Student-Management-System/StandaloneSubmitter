package de.uni_hildesheim.sse.submitter.i18n;

import java.util.Locale;
import java.util.Properties;

import de.uni_hildesheim.sse.submitter.AbstractPropertiesReader;

/**
 * This class is responsible for creating user texts in different languages. The instance of this class will
 * automatically detect the default language of the user and tries to create output texts in this language.
 * 
 * @author El-Sharkawy
 * 
 */
public class I18nProvider extends AbstractPropertiesReader {

    /**
     * Singleton instance of this class.
     */
    public static final I18nProvider INSTANCE = new I18nProvider();

    private Properties main;
    private Properties backup;

    /**
     * Sole constructor for this class, will load user texts in the correct language.
     */
    private I18nProvider() {
        String country = Locale.getDefault().getCountry();
        if (Locale.GERMANY.getCountry().equals(country)) {
            main = loadProperties("i18n_de.properties");
            backup = loadProperties("i18n_en.properties");
        } else {
            main = loadProperties("i18n_en.properties");
            backup = loadProperties("i18n_de.properties");
        }
    }

    /**
     * Returns a user text for the given key.
     * 
     * @param key The name of the user text inside the properties file.
     * @return The user text or an empty string if the key wasn't found.
     */
    public static String getText(String key) {
        String result = INSTANCE.main.getProperty(key);
        if (null == result) {
            result = INSTANCE.backup.getProperty(key, "");
        }

        return result;
    }
    
    /**
     * Returns a user text for the given key and uses {@link String#format(String, Object...)} to insert parameters.
     * @param key The name of the user text inside the properties file.
     * @param args Arguments referenced by the format specifiers in the format
     *         string.  If there are more arguments than format specifiers, the
     *         extra arguments are ignored.  The number of arguments is
     *         variable and may be zero or <tt>null</tt>.
     * @return The user text or an empty string if the key wasn't found.
     */
    public static String getText(String key, Object... args) {
        String text = getText(key);
        
        if (null != args && args.length > 0 && null != text && !text.isBlank()) {
            text = String.format(text, args);
        }
        
        return text;
    }
}

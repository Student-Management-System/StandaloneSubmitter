package de.uni_hildesheim.sse.submitter.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link I18nProvider} class.
 * 
 * @author kunold
 * @author Adam
 */
public class I18nProviderTest {
    
    /**
     * Tests the {@link I18nProvider#getText(String)} method in English.
     * Swapping the language is not supported by the {@link I18nProvider} class as it is a singleton which loads the
     * language at start up.
     */
    @Test
    @DisplayName("english text")
    public void english() {
        Locale.setDefault(Locale.US);
        I18nProvider.INSTANCE.init();
        
        String actualValue = I18nProvider.getText("Menu.submissionMenu");
        assertEquals("Exercise Submission", actualValue, "Wrong value returned for Menu.submissionMenu");
    }
    
    @Test
    @DisplayName("german text")
    public void german() {
        Locale.setDefault(Locale.GERMANY);
        I18nProvider.INSTANCE.init();
        
        String actualValue = I18nProvider.getText("Menu.submissionMenu");
        assertEquals("Hausaufgabenabgabe", actualValue, "Wrong value returned for Menu.submissionMenu");
    }
    
    @Test
    @DisplayName("missing key")
    public void missingKey() {
        String actualValue = I18nProvider.getText("doesnt.exist");
        assertNull(actualValue, "Value for missing key should be null");
    }
    
    @Test
    @DisplayName("formatted text")
    public void formattedText() {
        Locale.setDefault(Locale.US);
        I18nProvider.INSTANCE.init();
        
        String actualValue = I18nProvider.getText("submission.error.basis", "VALUE1", "some other value");
        assertEquals("An unexpected error has occurred.\nPlease contact the VALUE1 (some other value).", actualValue,
                "Formatted value should contain correct replacements");
    }
    
    @Test
    @DisplayName("formatted text for missing key")
    public void formattedTextMissingKey() {
        String actualValue = I18nProvider.getText("doesnt.exist", 13);
        assertNull(actualValue, "Value for missing key should be null");
    }

}

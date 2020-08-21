package de.uni_hildesheim.sse.submitter.i18n;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * Tests the {@link I18nProvider} class.
 * @author kunold
 *
 */
public class I18nProviderTest {
    
    /**
     * Tests the {@link I18nProvider#getText(String)} method in English.
     * Swapping the language is not supported by the {@link I18nProvider} class as it is a singleton which loads the
     * language at start up.
     */
    @Test
    public void testGetTextInEnglish() {
        // Test of the English language
        Locale.setDefault(Locale.US); 
        String actualValue = I18nProvider.getText("Menu.submissionMenu");
        assertEquals("Exercise Submission", actualValue, "Wrong value returned for Menu.submissionMenu");
    }

}
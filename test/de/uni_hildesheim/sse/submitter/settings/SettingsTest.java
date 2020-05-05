package de.uni_hildesheim.sse.submitter.settings;


import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link Settings} class.
 * @author kunold
 * @author El-Sharkawy
 *
 */
public class SettingsTest {

    /**
     * Tests {@link Settings#getSettings(String)} method <b>without</b> a default value.
     */
    @Test
    public void testGetSettingsWithoutDefault() {
        String actualValue = Settings.getSettings("prog.name");
        Assert.assertNotNull("Value for \"prog.name\" does not exist, but was expected.", actualValue);
        Assert.assertEquals("Wrong value returned for setting: prog.name", "ExerciseSubmitter", actualValue);
    }

    /**
     * Tests {@link Settings#getSettings(String, String)} method with a default value.
     * The default value should be used if the value wasn't specified in configuration file.
     */
    @Test
    public void testGetSettingsWithDefault() {
        // Value exist and is different than default value -> use specified value
        String actualValue = Settings.getSettings("prog.name", "defaultValue");
        Assert.assertNotNull("Value for \"prog.name\" does not exist, but was expected.", actualValue);
        Assert.assertEquals("Wrong value returned for setting: prog.name", "ExerciseSubmitter", actualValue);
        
        // Value does not exist  -> use default value
        actualValue = Settings.getSettings("blubb", "defaultValue");
        Assert.assertNotNull("No default value created for unexisting setting.", actualValue);
        Assert.assertEquals("Wrong default value returned", "defaultValue", actualValue);
    }
    
}

package de.uni_hildesheim.sse.submitter.settings;

import net.ssehub.exercisesubmitter.protocol.utils.AbstractSettings;

/**
 * Stores the internally used settings of the tool (not of the user).
 * @author El-Sharkawy
 *
 */
public class ToolSettings extends AbstractSettings<ToolConfiguration> {
    
    public static final ToolSettings INSTANCE = new ToolSettings();
    
    /**
     * Singleton constructor.
     */
    private ToolSettings() {}

    @Override
    protected String getSettingsFileName() {
        return "submitter-settings.json";
    }
    
    /**
     * Returns the tool configuration.
     * @return The configuration of the tool.
     */
    public static ToolConfiguration getConfig() {
        return INSTANCE.getConfiguration();
    }

    @Override
    protected Class<ToolConfiguration> getConfigClass() {
        return ToolConfiguration.class;
    }

}

package de.uni_hildesheim.sse.submitter.conf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import de.uni_hildesheim.sse.submitter.Starter;

/**
 * Static utility class for reading submission settings from the local file system.
 * @author El-Sharkawy
 *
 */
public class ConfigReader {

    static final String GROUP = "Gruppe";
    static final String USER = "Benutzername";
    static final String PW = "Passwort";
//    static final String EXERCISE = "Aufgabe";
    
    private static final String CONFIG_FILE_NAME = "submission.conf";

    /**
     * Loads the Configuration from the file system, assuming the file is named with the default
     * file name ({@value #CONFIG_FILE_NAME}.
     * @return The configuration for submitting the project to the submission server.
     */
    public static Configuration read() {
        File configFile = findConfigFile();
        Configuration result = null;
        if (configFile != null) {
            result = read(configFile);
        } else {
            result = new Configuration("", "", "", null, null);
        }
        return result;
    }

    /**
     * Loads the Configuration from the file system.
     * @param configDestination The location of the settings file (absolute path).
     * @return The configuration for submitting the project to the submission server.
     */
    public static Configuration read(File configDestination) {
        Configuration config = null;
        Properties prop = new Properties();
        InputStream input = null;

        try {

            if (!configDestination.exists()) {
                try {
                    configDestination.getParentFile().mkdirs();
                    configDestination.createNewFile();
                } catch (IOException e) {
                    configDestination = null;
                }
            }
            
            if (configDestination != null && configDestination.exists()) {
                input = new FileInputStream(configDestination);
    
                // load a properties file
                prop.load(input);
    
                String group = readProperties(prop, GROUP);
                String user = readProperties(prop, USER);
                String pw = readProperties(prop, PW);
//                String exercise = readProperties(prop, EXERCISE);
    
                config = new Configuration(group, user, pw, null, configDestination);
            } else {
                config = new Configuration("", "", "", null, null);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return config;
    }

    /**
     * Reads one setting out of the settings file.
     * @param prop The loaded settings file (part of the {@link #read(File, IUserInput)} method).
     * @param key The setting to read (the identifier of the setting).
     * @return The loaded value for the sepcified setting.
     */
    private static String readProperties(Properties prop, String key) {
        String result = prop.getProperty(key);
        if (null == result) {
            result = "";
        }

        return result;
    }

    /**
     * Creates a {@link File} object pointing to the default settings file ({@value #CONFIG_FILE_NAME}).
     * This method assumes that the file is stored relative to the program (JAR file).
     * @return A {@link File} object pointing to the default settings file ({@value #CONFIG_FILE_NAME}),
     * this file may not exist. <code>null</code> if unable to find a path.
     */
    private static File findConfigFile() {
        String path = System.getProperty("user.home");
        File result = null;
        if (path != null) {
            result = new File(path + File.separator + CONFIG_FILE_NAME);
        } else {
            URL resource = Starter.class.getClassLoader().getResource(CONFIG_FILE_NAME);
            if (resource != null) {
                path = resource.toExternalForm();
                path = path.replaceAll("file:/", "");
                result = new File(path);
            }
        }
        return result;
    }
}

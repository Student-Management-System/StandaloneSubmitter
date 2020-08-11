package de.uni_hildesheim.sse.submitter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Super class for classes reading properties out of a properties file.
 * @author El-Sharkawy
 *
 */
public class AbstractPropertiesReader {

    /**
     * Loads the language files. This should be part of the constructor.
     * 
     * @param fileName
     *            The name of the file, relative to this (sub-)class.
     * @return {@link Properties} with (key, userText) pairs, empty if the file could not be found/read.
     */
    protected Properties loadProperties(String fileName) {
        Properties prop = new Properties();
        InputStream in = getClass().getResourceAsStream(fileName);
        try {
            prop.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            in.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return prop;
    }
}

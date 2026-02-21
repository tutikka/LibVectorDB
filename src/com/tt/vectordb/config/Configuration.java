package com.tt.vectordb.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

/**
 * Class for loading configuration properties and reading values.
 *
 * @author Tuomas Tikka
 */
public class Configuration {

    private static final Logger L = LoggerFactory.getLogger(Configuration.class);

    private static final String FILENAME = "vectordb.properties";

    private static Configuration configuration;

    private Properties properties;

    /**
     * Constructor, where properties are loaded from files on disk.
     */
    private Configuration() {
        File file = new File(System.getProperty("configuration", FILENAME));
        try {
            properties = new Properties();
            properties.load(new FileReader(file));
            L.info(String.format("loaded %d properties from file '%s'", properties.size(), FILENAME));
        } catch (Exception e) {
            L.error(String.format("error loading properties from file '%s': %s", FILENAME, e.getMessage()));
            e.printStackTrace(System.err);
        }
    }

    /**
     * Constructor, where properties are loaded from input parameters.
     *
     * @param properties The properties
     */
    private Configuration(Properties properties) {
        this.properties = properties;
    }

    /**
     * Return the usable instance to read configuration properties.
     *
     * @return The instance
     */
    public static Configuration getConfiguration() {
        if (configuration == null) {
            configuration = new Configuration();
        }
        return (configuration);
    }

    /**
     * Return the usable instance to read configuration properties.
     *
     * @param properties The properties
     * @return The instance
     */
    public static Configuration getConfiguration(Properties properties) {
        if (configuration == null) {
            configuration = new Configuration(properties);
        }
        return (configuration);
    }

    /**
     * Return a string value for a property.
     *
     * @param key The property key
     * @param defaultValue A default value if property with 'key' is not found
     * @return The string value for the property
     */
    public String get(String key, String defaultValue) {
        return (properties.getProperty(key, defaultValue));
    }

    /**
     * Return an integer value for a property.
     *
     * @param key The property key
     * @param defaultValue A default value if property with 'key' is not found
     * @return The integer value for the property
     */
    public int get(String key, int defaultValue) {
        try {
            return (Integer.parseInt(properties.getProperty(key)));
        } catch (Exception e) {
            L.warn(String.format("error reading property with key '%s' as integer (%s) - using default value %d", key, e.getMessage(), defaultValue));
            return (defaultValue);
        }
    }

}

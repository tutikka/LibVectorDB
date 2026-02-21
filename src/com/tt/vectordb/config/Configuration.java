package com.tt.vectordb.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class Configuration {

    private static final Logger L = LoggerFactory.getLogger(Configuration.class);

    private static final String FILENAME = "vectordb.properties";

    private static Configuration configuration;

    private Properties properties;

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

    private Configuration(Properties properties) {
        this.properties = properties;
    }

    public static Configuration getConfiguration() {
        if (configuration == null) {
            configuration = new Configuration();
        }
        return (configuration);
    }

    public static Configuration getConfiguration(Properties properties) {
        if (configuration == null) {
            configuration = new Configuration(properties);
        }
        return (configuration);
    }

    public String get(String key, String defaultValue) {
        return (properties.getProperty(key, defaultValue));
    }

    public int get(String key, int defaultValue) {
        try {
            return (Integer.parseInt(properties.getProperty(key)));
        } catch (Exception e) {
            return (defaultValue);
        }
    }

}

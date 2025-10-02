package com.govtech.chinesescramble.exception;

/**
 * Exception thrown when a configuration file is not found or invalid
 *
 * @author Elite Backend Lead Developer
 * @version 1.0.0
 */
public class ConfigurationNotFoundException extends RuntimeException {

    private final String configKey;

    public ConfigurationNotFoundException(String configKey) {
        super("Configuration not found: " + configKey);
        this.configKey = configKey;
    }

    public ConfigurationNotFoundException(String message, String configKey) {
        super(message);
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }
}

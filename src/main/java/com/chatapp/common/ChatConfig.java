package com.chatapp.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager for the chat application.
 * Loads settings from application.properties or uses defaults.
 */
public final class ChatConfig {
    private static final String CONFIG_FILE = "application.properties";
    private static final Properties properties = new Properties();
    
    // Default values
    private static final int DEFAULT_MAX_CLIENTS = 50;
    private static final int DEFAULT_PORT = 8080;
    private static final boolean DEFAULT_VERBOSE = false;
    
    static {
        try (InputStream input = ChatConfig.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load configuration file. Using defaults.");
        }
    }
    
    private ChatConfig() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Gets the maximum number of concurrent clients allowed.
     */
    public static int getMaxClients() {
        return Integer.parseInt(properties.getProperty("chat.max.clients", 
            String.valueOf(DEFAULT_MAX_CLIENTS)));
    }
    
    /**
     * Gets the default server port.
     */
    public static int getDefaultPort() {
        return Integer.parseInt(properties.getProperty("chat.default.port", 
            String.valueOf(DEFAULT_PORT)));
    }
    
    /**
     * Gets whether verbose logging is enabled by default.
     */
    public static boolean isVerboseDefault() {
        return Boolean.parseBoolean(properties.getProperty("chat.verbose", 
            String.valueOf(DEFAULT_VERBOSE)));
    }
    
    /**
     * Gets the application name.
     */
    public static String getAppName() {
        return properties.getProperty("chat.app.name", "Java Chat Application");
    }
    
    /**
     * Gets the application version.
     */
    public static String getVersion() {
        return properties.getProperty("chat.version", "2.0.0");
    }
}

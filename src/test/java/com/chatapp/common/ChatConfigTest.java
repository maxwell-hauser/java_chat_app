package com.chatapp.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ChatConfig class.
 */
class ChatConfigTest {

    @Test
    void testGetMaxClients() {
        int maxClients = ChatConfig.getMaxClients();
        assertTrue(maxClients > 0, "Max clients should be positive");
    }

    @Test
    void testGetDefaultPort() {
        int port = ChatConfig.getDefaultPort();
        assertTrue(port > 0 && port <= 65535, "Port should be valid");
    }

    @Test
    void testGetAppName() {
        String appName = ChatConfig.getAppName();
        assertNotNull(appName);
        assertFalse(appName.isEmpty());
    }

    @Test
    void testGetVersion() {
        String version = ChatConfig.getVersion();
        assertNotNull(version);
        assertFalse(version.isEmpty());
    }

    @Test
    void testIsVerboseDefault() {
        // Should not throw exception
        boolean verbose = ChatConfig.isVerboseDefault();
        // Value can be true or false, just verify it works
        assertTrue(verbose == true || verbose == false);
    }

    @Test
    void testConfigurationValues() {
        // Test that configuration loads without errors
        assertDoesNotThrow(() -> {
            ChatConfig.getMaxClients();
            ChatConfig.getDefaultPort();
            ChatConfig.getAppName();
            ChatConfig.getVersion();
            ChatConfig.isVerboseDefault();
        });
    }
}

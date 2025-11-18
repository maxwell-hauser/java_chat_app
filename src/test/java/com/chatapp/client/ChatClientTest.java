package com.chatapp.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ChatClient class.
 */
class ChatClientTest {
    
    private ChatClient client;

    @BeforeEach
    void setUp() {
        client = new ChatClient("localhost", 8080);
    }

    @AfterEach
    void tearDown() {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
    }

    @Test
    void testClientCreation() {
        assertNotNull(client);
        assertFalse(client.isConnected());
        assertEquals("localhost", client.getHost());
        assertEquals(8080, client.getPort());
    }

    @Test
    void testClientWithPortOnly() {
        try (ChatClient portOnlyClient = new ChatClient(9000)) {
            assertEquals("localhost", portOnlyClient.getHost());
            assertEquals(9000, portOnlyClient.getPort());
        }
    }

    @Test
    void testCannotSendWhenDisconnected() {
        boolean result = client.sendMessage("Test");
        assertFalse(result);
    }

    @Test
    void testDisconnectWhenNotConnected() {
        // Should not throw exception
        assertDoesNotThrow(() -> client.disconnect());
    }

    @Test
    void testAutoCloseable() {
        assertDoesNotThrow(() -> {
            try (ChatClient autoCloseClient = new ChatClient(8080)) {
                // Client will be auto-closed
            }
        });
    }

    @Test
    void testMessageHandler() {
        StringBuilder received = new StringBuilder();
        client.setMessageHandler(received::append);
        
        // Message handler should be set
        assertDoesNotThrow(() -> client.setMessageHandler(msg -> {}));
    }
}

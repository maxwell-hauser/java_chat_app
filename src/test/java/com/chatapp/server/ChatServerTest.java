package com.chatapp.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

/**
 * Unit tests for the ChatServer class.
 */
class ChatServerTest {
    
    private ChatServer server;

    @BeforeEach
    void setUp() {
        server = new ChatServer();
    }

    @AfterEach
    void tearDown() {
        if (server != null && server.isRunning()) {
            server.stop();
        }
    }

    @Test
    void testServerCreation() {
        assertNotNull(server);
        assertFalse(server.isRunning());
        assertEquals(0, server.getClientCount());
    }

    @Test
    void testServerStart() throws IOException {
        server.start();
        
        assertTrue(server.isRunning());
        assertTrue(server.getPort() > 0);
        assertEquals(0, server.getClientCount());
    }

    @Test
    void testServerStop() throws IOException {
        server.start();
        assertTrue(server.isRunning());
        
        server.stop();
        assertFalse(server.isRunning());
    }

    @Test
    void testCannotStartTwice() throws IOException {
        server.start();
        
        assertThrows(IllegalStateException.class, () -> {
            server.start();
        });
    }

    @Test
    void testServerWithCustomConfiguration() throws IOException {
        try (ChatServer customServer = new ChatServer(9999, 10, true)) {
            customServer.start();
            assertTrue(customServer.isRunning());
            assertEquals(9999, customServer.getPort());
        }
    }

    @Test
    void testAutoCloseable() throws Exception {
        try (ChatServer autoCloseServer = new ChatServer()) {
            autoCloseServer.start();
            assertTrue(autoCloseServer.isRunning());
        }
        // Server should be stopped after try-with-resources
        assertFalse(server.isRunning());
    }
}

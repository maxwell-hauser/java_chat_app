package com.chatapp.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * Unit tests for the Message record.
 */
class MessageTest {

    @Test
    void testUserMessageCreation() {
        Message msg = Message.user("Alice", "Hello, World!");
        
        assertEquals(MessageType.USER, msg.type());
        assertEquals("Alice", msg.sender());
        assertEquals("Hello, World!", msg.content());
        assertNotNull(msg.timestamp());
    }

    @Test
    void testSystemMessageCreation() {
        Message msg = Message.system("Server started");
        
        assertEquals(MessageType.SYSTEM, msg.type());
        assertNull(msg.sender());
        assertEquals("Server started", msg.content());
        assertNotNull(msg.timestamp());
    }

    @Test
    void testErrorMessageCreation() {
        Message msg = Message.error("Connection failed");
        
        assertEquals(MessageType.ERROR, msg.type());
        assertNull(msg.sender());
        assertEquals("Connection failed", msg.content());
        assertNotNull(msg.timestamp());
    }

    @Test
    void testUserMessageFormat() {
        Message msg = Message.user("Bob", "Test message");
        String formatted = msg.format();
        
        assertEquals("[Bob] Test message", formatted);
    }

    @Test
    void testSystemMessageFormat() {
        Message msg = Message.system("User joined");
        String formatted = msg.format();
        
        assertEquals("[SYSTEM] User joined", formatted);
    }

    @Test
    void testErrorMessageFormat() {
        Message msg = Message.error("Network error");
        String formatted = msg.format();
        
        assertEquals("[ERROR] Network error", formatted);
    }

    @Test
    void testMessageImmutability() {
        LocalDateTime now = LocalDateTime.now();
        Message msg = new Message(MessageType.USER, "Charlie", "Content", now);
        
        assertEquals(MessageType.USER, msg.type());
        assertEquals("Charlie", msg.sender());
        assertEquals("Content", msg.content());
        assertEquals(now, msg.timestamp());
    }
}

package com.chatapp.common;

import java.time.LocalDateTime;

/**
 * Represents a chat message in the system.
 * Uses Java 21 record for immutable data.
 *
 * @param type The type of message (USER, SYSTEM, ERROR)
 * @param sender The username of the sender (null for system messages)
 * @param content The message content
 * @param timestamp When the message was created
 */
public record Message(
    MessageType type,
    String sender,
    String content,
    LocalDateTime timestamp
) {
    /**
     * Creates a user message with the current timestamp.
     */
    public static Message user(String sender, String content) {
        return new Message(MessageType.USER, sender, content, LocalDateTime.now());
    }

    /**
     * Creates a system message with the current timestamp.
     */
    public static Message system(String content) {
        return new Message(MessageType.SYSTEM, null, content, LocalDateTime.now());
    }

    /**
     * Creates an error message with the current timestamp.
     */
    public static Message error(String content) {
        return new Message(MessageType.ERROR, null, content, LocalDateTime.now());
    }

    /**
     * Formats the message for display.
     */
    public String format() {
        return switch (type) {
            case USER -> String.format("[%s] %s", sender, content);
            case SYSTEM -> String.format("[SYSTEM] %s", content);
            case ERROR -> String.format("[ERROR] %s", content);
        };
    }
}

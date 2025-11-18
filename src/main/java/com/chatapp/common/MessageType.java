package com.chatapp.common;

/**
 * Enumeration of message types in the chat system.
 */
public enum MessageType {
    /** A message sent by a user */
    USER,
    
    /** A system-generated message (join/leave notifications, etc.) */
    SYSTEM,
    
    /** An error message */
    ERROR
}

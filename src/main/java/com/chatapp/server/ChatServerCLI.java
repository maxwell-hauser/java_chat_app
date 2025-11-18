package com.chatapp.server;

import java.io.IOException;

/**
 * Command-line interface for the chat server.
 * Accepts an optional "verbose" argument for detailed logging.
 * 
 * Usage:
 *   java com.chatapp.server.ChatServerCLI [verbose]
 */
public class ChatServerCLI {
    
    public static void main(String[] args) {
        // Verbose mode is configured in ChatConfig or via logging level
        
        try (ChatServer server = new ChatServer()) {
            System.out.println("Starting chat server...");
            server.start();
            
            System.out.println("Server is running on port: " + server.getPort());
            System.out.println("Press Ctrl+C to stop the server.");
            
            // Add shutdown hook for graceful termination
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down server...");
                server.stop();
            }));
            
            // Keep main thread alive
            Thread.currentThread().join();
            
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Server interrupted");
            System.exit(1);
        }
    }
}

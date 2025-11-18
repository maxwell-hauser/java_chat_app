package com.chatapp.client;

import java.io.IOException;
import java.util.Scanner;

/**
 * Command-line interface for the chat client.
 * 
 * Usage:
 *   java com.chatapp.client.ChatClientCLI [host] [port]
 *   
 * If host is not specified, defaults to "localhost".
 * Port must be provided as the second argument if host is specified.
 */
public class ChatClientCLI {
    
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        
        if (args.length < 2) {
            System.err.println("Usage: java com.chatapp.client.ChatClientCLI <host> <port>");
            System.err.println("Example: java com.chatapp.client.ChatClientCLI localhost 8080");
            System.exit(1);
        }
        
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + args[1]);
            System.exit(1);
            return;
        }
        
        try (ChatClient client = new ChatClient(host, port);
             Scanner scanner = new Scanner(System.in)) {
            
            System.out.println("Connecting to server at " + host + ":" + port + "...");
            client.connect();
            System.out.println("Connected!");
            
            // Set up message handler to print received messages
            client.setMessageHandler(System.out::println);
            
            // Username registration
            System.out.print("Enter your username: ");
            String username = scanner.nextLine().trim();
            client.registerUsername(username);
            
            // Give server time to respond
            Thread.sleep(100);
            
            System.out.println("\nYou can now start chatting. Type /quit to exit.\n");
            
            // Message sending loop
            while (client.isConnected()) {
                if (scanner.hasNextLine()) {
                    String message = scanner.nextLine();
                    
                    if (message.trim().isEmpty()) {
                        continue;
                    }
                    
                    if (!client.sendMessage(message)) {
                        System.err.println("Failed to send message. Connection may be lost.");
                        break;
                    }
                    
                    if ("/quit".equalsIgnoreCase(message.trim())) {
                        break;
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Client interrupted");
            System.exit(1);
        }
        
        System.out.println("Goodbye!");
    }
}

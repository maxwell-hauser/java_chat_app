package com.chatapp.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Modern chat client using Java 21 features.
 * Supports asynchronous message sending and receiving.
 */
public class ChatClient implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ChatClient.class);
    
    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final ExecutorService executorService;
    private volatile boolean connected;
    private Consumer<String> messageHandler;

    /**
     * Creates a chat client for the specified host and port.
     *
     * @param host The server hostname
     * @param port The server port
     */
    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.connected = false;
    }

    /**
     * Creates a chat client for localhost.
     *
     * @param port The server port
     */
    public ChatClient(int port) {
        this("localhost", port);
    }

    /**
     * Connects to the chat server.
     *
     * @throws IOException if connection fails
     */
    public void connect() throws IOException {
        if (connected) {
            throw new IllegalStateException("Client is already connected");
        }

        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            
            logger.info("Connected to server at {}:{}", host, port);
            
            // Start listening for messages
            executorService.submit(this::listenForMessages);
            
        } catch (IOException e) {
            logger.error("Failed to connect to server", e);
            throw e;
        }
    }

    /**
     * Sets the handler for incoming messages.
     *
     * @param handler Consumer that processes received messages
     */
    public void setMessageHandler(Consumer<String> handler) {
        this.messageHandler = handler;
    }

    /**
     * Listens for incoming messages from the server.
     */
    private void listenForMessages() {
        try {
            String message;
            while (connected && (message = in.readLine()) != null) {
                if (messageHandler != null) {
                    final String msg = message;
                    messageHandler.accept(msg);
                } else {
                    System.out.println(message);
                }
            }
        } catch (IOException e) {
            if (connected) {
                logger.error("Error reading from server", e);
            }
        } finally {
            disconnect();
        }
    }

    /**
     * Sends a message to the server.
     *
     * @param message The message to send
     * @return true if message was sent successfully
     */
    public boolean sendMessage(String message) {
        if (!connected || out == null) {
            logger.warn("Cannot send message: not connected");
            return false;
        }

        try {
            out.println(message);
            return true;
        } catch (Exception e) {
            logger.error("Error sending message", e);
            return false;
        }
    }

    /**
     * Registers a username with the server.
     *
     * @param username The desired username
     * @return true if registration was successful
     */
    public boolean registerUsername(String username) {
        if (!connected) {
            throw new IllegalStateException("Not connected to server");
        }
        
        return sendMessage(username);
    }

    /**
     * Disconnects from the server.
     */
    public void disconnect() {
        if (!connected) {
            return;
        }
        
        connected = false;
        
        try {
            if (out != null) {
                out.println("/quit");
            }
        } catch (Exception e) {
            logger.debug("Error sending quit command", e);
        }
        
        closeResources();
        executorService.shutdown();
        
        logger.info("Disconnected from server");
    }

    /**
     * Closes all I/O resources.
     */
    private void closeResources() {
        try {
            if (in != null) in.close();
        } catch (IOException e) {
            logger.debug("Error closing input stream", e);
        }
        
        if (out != null) {
            out.close();
        }
        
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            logger.debug("Error closing socket", e);
        }
    }

    @Override
    public void close() {
        disconnect();
    }

    /**
     * Checks if the client is connected to the server.
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    /**
     * Gets the host the client is connected to.
     */
    public String getHost() {
        return host;
    }

    /**
     * Gets the port the client is connected to.
     */
    public int getPort() {
        return port;
    }
}

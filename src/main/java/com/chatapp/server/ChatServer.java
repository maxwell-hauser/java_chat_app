package com.chatapp.server;

import com.chatapp.common.ChatConfig;
import com.chatapp.common.Message;
import me.alexpanov.net.FreePortFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Modern multi-threaded chat server using Java 21 features.
 * Supports multiple concurrent clients with unique usernames.
 * Uses virtual threads for scalable concurrent connections.
 */
public class ChatServer implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);
    
    private final int port;
    private final int maxClients;
    private final boolean verbose;
    private final ConcurrentHashMap<String, ClientConnection> connectedClients;
    private final ExecutorService executorService;
    private ServerSocket serverSocket;
    private volatile boolean running;

    /**
     * Creates a chat server with specified configuration.
     *
     * @param port The port to listen on
     * @param maxClients Maximum number of concurrent clients
     * @param verbose Whether to enable verbose logging
     */
    public ChatServer(int port, int maxClients, boolean verbose) {
        this.port = port;
        this.maxClients = maxClients;
        this.verbose = verbose;
        this.connectedClients = new ConcurrentHashMap<>();
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.running = false;
    }

    /**
     * Creates a chat server with default configuration.
     */
    public ChatServer() {
        this(FreePortFinder.findFreeLocalPort(), 
             ChatConfig.getMaxClients(), 
             ChatConfig.isVerboseDefault());
    }

    /**
     * Starts the server and begins accepting client connections.
     *
     * @throws IOException if server cannot be started
     */
    public void start() throws IOException {
        if (running) {
            throw new IllegalStateException("Server is already running");
        }

        serverSocket = new ServerSocket(port);
        running = true;
        
        logger.info("Server started on port: {}", port);
        if (verbose) {
            System.out.println("Server started on port: " + port);
            System.out.println("Now listening for connections...");
        }

        executorService.submit(this::acceptClients);
    }

    /**
     * Accepts incoming client connections.
     */
    private void acceptClients() {
        while (running && !serverSocket.isClosed()) {
            try {
                if (connectedClients.size() < maxClients) {
                    Socket clientSocket = serverSocket.accept();
                    executorService.submit(() -> handleClient(clientSocket));
                } else {
                    // Brief pause if at capacity
                    Thread.sleep(100);
                }
            } catch (IOException e) {
                if (running) {
                    logger.error("Error accepting client connection", e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Handles communication with a single client.
     *
     * @param socket The client socket connection
     */
    private void handleClient(Socket socket) {
        String username = null;
        
        try (socket;
             BufferedReader in = new BufferedReader(
                 new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            if (verbose) {
                logger.info("Client connected: {}", socket.getInetAddress());
            }

            // Username registration loop
            username = registerClient(in, out);
            if (username == null) {
                return; // Client disconnected during registration
            }

            // Store client connection
            ClientConnection connection = new ClientConnection(username, out, socket);
            connectedClients.put(username, connection);

            // Welcome message
            out.println("Welcome to the chat group, " + username.toUpperCase() + "!");
            broadcastMessage(Message.system(username.toUpperCase() + " has joined."));
            
            if (verbose) {
                logger.info("{} has joined", username);
            }

            // Message handling loop
            handleClientMessages(in, username);

        } catch (IOException e) {
            logger.error("Error handling client", e);
        } finally {
            if (username != null) {
                disconnectClient(username);
            }
        }
    }

    /**
     * Registers a new client with a unique username.
     *
     * @param in Input reader from client
     * @param out Output writer to client
     * @return The registered username, or null if registration failed
     * @throws IOException if communication error occurs
     */
    private String registerClient(BufferedReader in, PrintWriter out) throws IOException {
        while (true) {
            out.println("Enter username:\t");
            String username = in.readLine();
            
            if (username == null) {
                return null; // Client disconnected
            }
            
            username = username.trim();
            
            if (!username.isEmpty() && !connectedClients.containsKey(username)) {
                return username;
            }
            
            out.println("INVALID NAME");
        }
    }

    /**
     * Handles incoming messages from a client.
     *
     * @param in Input reader from client
     * @param username The client's username
     * @throws IOException if communication error occurs
     */
    private void handleClientMessages(BufferedReader in, String username) throws IOException {
        String message;
        while ((message = in.readLine()) != null) {
            message = message.trim();
            
            if (message.isEmpty()) {
                continue;
            }
            
            if ("/quit".equalsIgnoreCase(message)) {
                break;
            }
            
            broadcastMessage(Message.user(username, message));
        }
    }

    /**
     * Disconnects a client and notifies others.
     *
     * @param username The username of the client to disconnect
     */
    private void disconnectClient(String username) {
        ClientConnection connection = connectedClients.remove(username);
        if (connection != null) {
            if (verbose) {
                logger.info("{} is leaving", username);
            }
            broadcastMessage(Message.system(username + " has left."));
            
            try {
                connection.socket().close();
            } catch (IOException e) {
                logger.warn("Error closing socket for {}", username, e);
            }
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param message The message to broadcast
     */
    private void broadcastMessage(Message message) {
        String formattedMessage = message.format();
        
        connectedClients.values().forEach(connection -> {
            try {
                connection.out().println(formattedMessage);
            } catch (Exception e) {
                logger.warn("Failed to send message to {}", connection.username(), e);
            }
        });
    }

    /**
     * Stops the server and disconnects all clients.
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        running = false;
        
        // Disconnect all clients
        connectedClients.keySet().forEach(this::disconnectClient);
        
        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("Error closing server socket", e);
        }
        
        executorService.shutdown();
        
        if (verbose) {
            logger.info("Server stopped");
        }
    }

    @Override
    public void close() {
        stop();
    }

    /**
     * Gets the port the server is listening on.
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the number of currently connected clients.
     */
    public int getClientCount() {
        return connectedClients.size();
    }

    /**
     * Checks if the server is running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Represents a connected client.
     */
    private record ClientConnection(String username, PrintWriter out, Socket socket) {}
}

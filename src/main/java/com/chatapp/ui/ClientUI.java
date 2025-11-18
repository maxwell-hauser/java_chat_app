package com.chatapp.ui;

import com.chatapp.client.ChatClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Graphical user interface for the chat client.
 * Allows users to connect to a server, set a username, and send messages.
 */
public class ClientUI extends JFrame implements ActionListener {
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("hh:mm a");
    
    private ChatClient client;
    
    // UI Components
    private JTextField usernameField;
    private JTextField portField;
    private JTextField hostField;
    private JButton connectButton;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JLabel statusLabel;
    
    /**
     * Launches the client UI application.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Fall back to default look and feel
            }
            
            ClientUI frame = new ClientUI();
            frame.setVisible(true);
        });
    }
    
    /**
     * Creates the client UI frame.
     */
    public ClientUI() {
        initializeComponents();
        redirectSystemOutput();
        
        setTitle("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 550);
        setLocationRelativeTo(null);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::disconnect));
    }
    
    /**
     * Initializes all UI components.
     */
    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);
        
        // North panel - Connection settings
        JPanel northPanel = createConnectionPanel();
        mainPanel.add(northPanel, BorderLayout.NORTH);
        
        // Center panel - Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chatArea.setBackground(new Color(250, 250, 250));
        
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // South panel - Message input
        JPanel southPanel = createMessagePanel();
        mainPanel.add(southPanel, BorderLayout.SOUTH);
        
        // Initially disable message controls
        messageField.setEnabled(false);
        sendButton.setEnabled(false);
    }
    
    /**
     * Creates the connection panel.
     */
    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        
        JLabel titleLabel = new JLabel("Chat Client", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 28));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel fieldsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        // Host field
        fieldsPanel.add(new JLabel("Host:"));
        hostField = new JTextField("localhost", 12);
        fieldsPanel.add(hostField);
        
        // Port field
        fieldsPanel.add(new JLabel("Port:"));
        portField = new JTextField("8080", 6);
        fieldsPanel.add(portField);
        
        // Username field
        fieldsPanel.add(new JLabel("Username:"));
        usernameField = new JTextField(12);
        fieldsPanel.add(usernameField);
        
        // Connect button
        connectButton = new JButton("Connect");
        connectButton.setFont(new Font("Verdana", Font.BOLD, 12));
        connectButton.addActionListener(this);
        fieldsPanel.add(connectButton);
        
        panel.add(fieldsPanel, BorderLayout.CENTER);
        
        // Status label
        statusLabel = new JLabel("Status: Disconnected", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Verdana", Font.PLAIN, 12));
        panel.add(statusLabel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Creates the message input panel.
     */
    private JPanel createMessagePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        
        messageField = new JTextField();
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
        panel.add(messageField, BorderLayout.CENTER);
        
        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Verdana", Font.BOLD, 14));
        sendButton.setPreferredSize(new Dimension(100, 35));
        sendButton.addActionListener(this);
        panel.add(sendButton, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Redirects System.out to the chat text area.
     */
    private void redirectSystemOutput() {
        PrintStream printStream = new PrintStream(new TextAreaOutputStream(chatArea), true);
        System.setOut(printStream);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == connectButton) {
            if (client == null || !client.isConnected()) {
                connect();
            } else {
                disconnect();
            }
        } else if (e.getSource() == sendButton) {
            sendMessage();
        }
    }
    
    /**
     * Connects to the chat server.
     */
    private void connect() {
        String host = hostField.getText().trim();
        String portText = portField.getText().trim();
        String username = usernameField.getText().trim();
        
        // Validation
        if (host.isEmpty() || portText.isEmpty() || username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please fill in all fields",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Port must be a valid number",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            client = new ChatClient(host, port);
            client.setMessageHandler(this::displayMessage);
            client.connect();
            client.registerUsername(username);
            
            // Update UI
            connectButton.setText("Disconnect");
            connectButton.setBackground(new Color(220, 100, 100));
            statusLabel.setText("Status: Connected to " + host + ":" + port);
            
            messageField.setEnabled(true);
            sendButton.setEnabled(true);
            messageField.requestFocus();
            
            hostField.setEnabled(false);
            portField.setEnabled(false);
            usernameField.setEnabled(false);
            
            logMessage("Connected to server successfully");
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to connect:\n" + ex.getMessage(),
                "Connection Error",
                JOptionPane.ERROR_MESSAGE);
            logError("Failed to connect: " + ex.getMessage());
        }
    }
    
    /**
     * Disconnects from the chat server.
     */
    private void disconnect() {
        if (client != null) {
            client.disconnect();
            client = null;
        }
        
        // Update UI
        connectButton.setText("Connect");
        connectButton.setBackground(null);
        statusLabel.setText("Status: Disconnected");
        
        messageField.setEnabled(false);
        sendButton.setEnabled(false);
        
        hostField.setEnabled(true);
        portField.setEnabled(true);
        usernameField.setEnabled(true);
        
        logMessage("Disconnected from server");
    }
    
    /**
     * Sends the current message.
     */
    private void sendMessage() {
        String message = messageField.getText().trim();
        
        if (message.isEmpty()) {
            return;
        }
        
        if (client != null && client.isConnected()) {
            client.sendMessage(message);
            messageField.setText("");
            
            if ("/quit".equalsIgnoreCase(message)) {
                disconnect();
            }
        }
    }
    
    /**
     * Displays a message in the chat area.
     */
    private void displayMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = TIME_FORMATTER.format(LocalDateTime.now());
            chatArea.append(String.format("[%s] %s%n", timestamp, message));
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }
    
    /**
     * Logs a message.
     */
    private void logMessage(String message) {
        String timestamp = TIME_FORMATTER.format(LocalDateTime.now());
        System.out.printf("[%s] %s%n", timestamp, message);
    }
    
    /**
     * Logs an error message.
     */
    private void logError(String message) {
        String timestamp = TIME_FORMATTER.format(LocalDateTime.now());
        System.err.printf("[%s] ERROR: %s%n", timestamp, message);
    }
}

package com.chatapp.ui;

import com.chatapp.server.ChatServer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Graphical user interface for the chat server.
 * Provides controls to start/stop the server and displays logs.
 */
public class ServerUI extends JFrame implements ActionListener {
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("hh:mm a");
    
    private ChatServer server;
    private JTextArea logsArea;
    private JButton startStopButton;
    private JLabel titleLabel;
    private JLabel statusLabel;
    
    /**
     * Launches the server UI application.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Fall back to default look and feel
            }
            
            ServerUI frame = new ServerUI();
            frame.setVisible(true);
        });
    }
    
    /**
     * Creates the server UI frame.
     */
    public ServerUI() {
        initializeComponents();
        redirectSystemOutput();
        
        setTitle("Chat Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopServer));
    }
    
    /**
     * Initializes all UI components.
     */
    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(mainPanel);
        
        // North panel with title and status
        JPanel northPanel = new JPanel(new BorderLayout(5, 5));
        
        titleLabel = new JLabel("Chat Server", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 32));
        northPanel.add(titleLabel, BorderLayout.NORTH);
        
        statusLabel = new JLabel("Status: Stopped", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Verdana", Font.PLAIN, 14));
        northPanel.add(statusLabel, BorderLayout.SOUTH);
        
        mainPanel.add(northPanel, BorderLayout.NORTH);
        
        // Center panel with logs
        logsArea = new JTextArea();
        logsArea.setEditable(false);
        logsArea.setLineWrap(true);
        logsArea.setWrapStyleWord(true);
        logsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logsArea.setBackground(new Color(40, 40, 40));
        logsArea.setForeground(new Color(200, 200, 200));
        
        JScrollPane scrollPane = new JScrollPane(logsArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // South panel with control button
        JPanel southPanel = new JPanel();
        startStopButton = new JButton("Start Server");
        startStopButton.setFont(new Font("Verdana", Font.BOLD, 16));
        startStopButton.setPreferredSize(new Dimension(200, 50));
        startStopButton.addActionListener(this);
        southPanel.add(startStopButton);
        
        mainPanel.add(southPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Redirects System.out to the logs text area.
     */
    private void redirectSystemOutput() {
        PrintStream printStream = new PrintStream(new TextAreaOutputStream(logsArea), true);
        System.setOut(printStream);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startStopButton) {
            if (server == null || !server.isRunning()) {
                startServer();
            } else {
                stopServer();
            }
        }
    }
    
    /**
     * Starts the chat server.
     */
    private void startServer() {
        try {
            server = new ChatServer();
            server.start();
            
            startStopButton.setText("Stop Server");
            startStopButton.setBackground(new Color(220, 100, 100));
            
            int port = server.getPort();
            titleLabel.setText("Chat Server: Port " + port);
            statusLabel.setText("Status: Running on port " + port);
            
            logMessage("Server started successfully on port " + port);
            
        } catch (IOException ex) {
            logError("Failed to start server: " + ex.getMessage());
            JOptionPane.showMessageDialog(this,
                "Failed to start server:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Stops the chat server.
     */
    private void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
            
            startStopButton.setText("Start Server");
            startStopButton.setBackground(null);
            
            titleLabel.setText("Chat Server");
            statusLabel.setText("Status: Stopped");
            
            logMessage("Server stopped");
        }
    }
    
    /**
     * Logs a message with timestamp.
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

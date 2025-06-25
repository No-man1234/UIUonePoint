package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.util.ChatLauncher;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.*;
import java.net.Socket;

public class ChatController {
    @FXML private TextArea chatArea;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private Label statusLabel;
    
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private String username;
    private boolean isSeller;
    private String chatId;

    public void initialize() {
        System.out.println("ChatController initialized");
        sendButton.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage());
    }

    public void setUserInfo(String username, boolean isSeller, String chatId) {
        System.out.println("Setting user info - Username: " + username + ", IsSeller: " + isSeller + ", ChatId: " + chatId);
        this.username = username;
        this.isSeller = isSeller;
        this.chatId = chatId;
        connectToServer();
    }

    private void connectToServer() {
        try {
            System.out.println("Attempting to connect to chat server...");
            socket = new Socket("localhost", 5000);
            System.out.println("Socket connected successfully");
            
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Streams initialized");

            // Send username and chatId to server, separated by ::
            System.out.println("Sending username and chatId to server: " + username + "::" + chatId);
            out.println(username + "::" + chatId);

            // Start a thread to listen for messages
            new Thread(() -> {
                try {
                    System.out.println("Starting message listener thread");
                    String line;
                    while ((line = in.readLine()) != null) {
                        final String message = line;
                        System.out.println("Received message: " + message);
                        Platform.runLater(() -> {
                            if (message.startsWith("MESSAGE")) {
                                chatArea.appendText(message.substring(8) + "\n");
                            } else if (message.equals("NAME_TAKEN")) {
                                statusLabel.setText("Username is taken. Please choose another.");
                                System.out.println("Username was taken");
                            } else if (message.equals("NAME_ACCEPTED")) {
                                statusLabel.setText("Connected to chat server");
                                System.out.println("Username was accepted");
                            }
                        });
                    }
                } catch (IOException e) {
                    System.out.println("Error in message listener: " + e.getMessage());
                    Platform.runLater(() -> statusLabel.setText("Connection lost"));
                }
            }).start();

        } catch (IOException e) {
            System.out.println("Failed to connect to server: " + e.getMessage());
            statusLabel.setText("Could not connect to server");
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            System.out.println("Sending message: " + message);
            out.println(message);
            messageField.clear();
        }
    }

    public void closeConnection() {
        System.out.println("Closing chat connection");
        try {
            if (socket != null) socket.close();
            if (out != null) out.close();
            if (in != null) in.close();
            System.out.println("Chat connection closed successfully");
        } catch (IOException e) {
            System.out.println("Error closing chat connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 
package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.dao.UserDAO;
import com.example.uiuonepoint.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class SignupController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private Label messageLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    protected void handleSignup() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();

        // Validate input
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || 
            fullName.isEmpty() || email.isEmpty()) {
            messageLabel.setText("Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setText("Passwords do not match");
            return;
        }

        if (username.length() < 3) {
            messageLabel.setText("Username must be at least 3 characters long");
            return;
        }

        if (password.length() < 6) {
            messageLabel.setText("Password must be at least 6 characters long");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            messageLabel.setText("Please enter a valid email address");
            return;
        }

        // Check if username already exists
        if (userDAO.findByUsername(username).isPresent()) {
            messageLabel.setText("Username already exists");
            return;
        }

        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // In a real app, hash the password
        user.setEmail(email);
        user.setRole("CUSTOMER"); // Default role for new users

        try {
            userDAO.save(user);
            messageLabel.setText("Account created successfully!");
            messageLabel.getStyleClass().add("success-message");
            
            // Automatically switch to login after 2 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::handleBackToLogin);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            messageLabel.setText("Error creating account");
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleBackToLogin() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/uiuonepoint/login.fxml"));

            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error loading login page");
        }
    }
} 
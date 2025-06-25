package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.dao.UserDAO;
import com.example.uiuonepoint.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private final UserDAO userDAO = new UserDAO();

    private void loadView(String fxmlFile, User user) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();

            // Pass user data to the next controller if needed
            Object controller = fxmlLoader.getController();
            if (controller instanceof AdminDashboardController) {
                ((AdminDashboardController) controller).initData(user);
            } else if (controller instanceof SectionSelectorController) {
                ((SectionSelectorController) controller).initData(user);
            } else if (controller instanceof SellerDashboardController) {
                ((SellerDashboardController) controller).initData(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
            messageLabel.setText("Error loading view: " + e.getMessage());
        }
    }

    @FXML
    protected void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both username and password");
            return;
        }

        Optional<User> userOptional = userDAO.findByUsername(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (password.equals(user.getPassword())) {
                String fxmlFile;
                if (user.getRole().equals("ADMIN")) {
                    fxmlFile = "/com/example/uiuonepoint/admin-dashboard.fxml";
                } else if (user.getRole().equals("SELLER")) {
                    fxmlFile = "/com/example/uiuonepoint/seller-dashboard.fxml";
                } else {
                    fxmlFile = "/com/example/uiuonepoint/section-selector.fxml";
                }
                loadView(fxmlFile, user);
            } else {
                messageLabel.setText("Invalid password");
            }
        } else {
            messageLabel.setText("User not found");
        }
    }

    @FXML
    protected void handleSignup() {
        loadView("/com/example/uiuonepoint/signup.fxml", null);
    }
}
 
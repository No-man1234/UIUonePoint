package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public abstract class AdminBaseController {
    @FXML
    protected Label welcomeLabel;

    protected User currentUser;

    public void initData(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
        onInitialize();
    }

    // Template method to be implemented by subclasses
    protected abstract void onInitialize();

    protected void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    protected void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 
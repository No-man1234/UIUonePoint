package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.io.IOException;

public abstract class SellerBaseController {
    protected User currentUser;
    @FXML protected VBox root;

    public void initData(User user) {
        this.currentUser = user;
        onInitialize();
    }

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

    protected void loadView(String fxmlFile) {
        try {
            // Load the new view
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/uiuonepoint/" + fxmlFile));
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());

            // Get the current stage from the root node
            Stage currentStage = (Stage) root.getScene().getWindow();
            currentStage.setScene(scene);
            currentStage.setMaximized(true);
            currentStage.centerOnScreen();

            Object controller = fxmlLoader.getController();
            if (controller instanceof SellerBaseController) {
                ((SellerBaseController) controller).initData(currentUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not load view: " + e.getMessage());
        }
    }
} 
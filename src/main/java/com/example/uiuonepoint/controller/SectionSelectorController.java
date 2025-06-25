package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.dao.SellerRequestDAO;
import com.example.uiuonepoint.model.SellerRequest;
import com.example.uiuonepoint.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.stage.Modality;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;

public class SectionSelectorController extends CustomerBaseController {
    @FXML
    private Label welcomeLabel;

    @FXML
    private Button canteenButton;

    @FXML
    private Button groceryButton;

    @FXML
    private Button myOrdersButton;
    
    @FXML
    private Button becomeSellerButton;

    @FXML
    private Button eShoppingButton;

    @FXML
    private Label becomeSellerLabel;

    private User currentUser;

    private final SellerRequestDAO sellerRequestDAO = new SellerRequestDAO();

    @Override
    protected void onInitialize() {
        // Any initialization specific to SectionSelector can go here
        checkSellerRequestStatus();
    }

    public void initData(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
        
        // If user is already a seller or admin, hide the become seller button and label
        if (user.getRole().equals("SELLER") || user.getRole().equals("ADMIN")) {
            becomeSellerButton.setVisible(false);
            becomeSellerLabel.setVisible(false);
        } else {
            becomeSellerButton.setVisible(true);
            becomeSellerLabel.setVisible(true);
        }
        
        checkSellerRequestStatus();
    }
    
    private void checkSellerRequestStatus() {
        if (currentUser == null) return;
        
        try {
            Optional<SellerRequest> requestOpt = sellerRequestDAO.getByUserId(currentUser.getId());
            if (requestOpt.isPresent()) {
                SellerRequest request = requestOpt.get();
                switch (request.getStatus()) {
                    case "pending":
                        becomeSellerButton.setText("Pending");
                        becomeSellerButton.setDisable(true);
                        becomeSellerLabel.setVisible(true);
                        break;
                    case "approved":
                        becomeSellerButton.setText("Seller Dashboard");
                        becomeSellerButton.setDisable(false);
                        becomeSellerLabel.setVisible(false);
                        break;
                    case "rejected":
                        becomeSellerButton.setText("Rejected");
                        becomeSellerButton.setDisable(true);
                        becomeSellerLabel.setVisible(false);
                        break;
                }
            } else {
                // No request, show default state
                becomeSellerButton.setText("click here");
                becomeSellerButton.setDisable(false);
                becomeSellerLabel.setVisible(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void handleCanteenSection() {
        loadSection("canteen-view.fxml");
    }

    @FXML
    protected void handleGrocerySection() {
        loadSection("grocery-view.fxml");
    }

    @FXML
    protected void handleEShoppingSection() {
        if (currentUser == null) return;
        
        // If user is a seller, show the products manager
        if (currentUser.getRole().equals("SELLER")) {
            loadSection("eshopping-products-manager.fxml");
        } else {
            // For regular users, show the shopping view
            loadSection("eshopping-view.fxml");
        }
    }

    @FXML
    protected void handleLogout() {
        loadSection("login.fxml");
    }

    @FXML
    protected void handleMyOrders() {
        loadSection("my-orders.fxml");
    }
    
    @FXML
    protected void handleBecomeSeller() {
        if (currentUser == null) return;
        
        // If the user is already a SELLER, redirect to seller dashboard
        if (currentUser.getRole().equals("SELLER")) {
            loadSection("seller-dashboard.fxml");
            return;
        }
        
        try {
            Optional<SellerRequest> existingRequest = sellerRequestDAO.getByUserId(currentUser.getId());
            if (existingRequest.isPresent()) {
                String status = existingRequest.get().getStatus();
                if (status.equals("approved")) {
                    loadSection("seller-dashboard.fxml");
                    return;
                } else if (status.equals("pending")) {
                    showAlert(Alert.AlertType.INFORMATION, "Request Pending", "Your seller request is still pending approval.");
                    return;
                } else if (status.equals("rejected")) {
                    showAlert(Alert.AlertType.INFORMATION, "Request Rejected", "Your seller request was rejected.");
                    return;
                }
            }
            
            // Show dialog to confirm
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Become a Seller");
            dialog.setHeaderText("Apply to become a seller");
            dialog.setContentText("Please provide a reason why you want to become a seller:");
            
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String reason = result.get();
                
                SellerRequest request = new SellerRequest();
                request.setUserId(currentUser.getId());
                request.setStatus("pending");
                request.setReason(reason);
                
                sellerRequestDAO.save(request);
                
                becomeSellerButton.setText("Seller Request Pending");
                becomeSellerButton.setDisable(true);
                
                showAlert(Alert.AlertType.INFORMATION, "Request Submitted", "Your seller request has been submitted and is pending approval.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to process seller request. Please try again.");
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadSection(String fxmlFile) {
        try {
            String fxmlPath = "/com/example/uiuonepoint/" + fxmlFile;
            URL resourceUrl = getClass().getResource(fxmlPath);

            if (resourceUrl == null) {
                throw new IOException("Cannot find FXML file: " + fxmlPath);
            }

            FXMLLoader fxmlLoader = new FXMLLoader(resourceUrl);
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());

            Stage stage = (Stage) canteenButton.getScene().getWindow();
            stage.setScene(scene);
            
            // Configure stage properties
            stage.setMaximized(true);
            stage.setResizable(true);
            stage.centerOnScreen();

            // Pass user data to the next controller
            Object controller = fxmlLoader.getController();
            if (controller instanceof CustomerBaseController) {
                ((CustomerBaseController) controller).initData(currentUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading FXML: " + e.getMessage());
        }
    }
}
package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.dao.SellerRequestDAO;
import com.example.uiuonepoint.dao.UserDAO;
import com.example.uiuonepoint.model.SellerRequest;
import com.example.uiuonepoint.model.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class AdminSellerRequestsController extends AdminBaseController implements Initializable {
    @FXML private TableView<SellerRequest> requestsTable;
    @FXML private TableColumn<SellerRequest, String> idColumn;
    @FXML private TableColumn<SellerRequest, String> usernameColumn;
    @FXML private TableColumn<SellerRequest, String> dateColumn;
    @FXML private TableColumn<SellerRequest, String> reasonColumn;
    @FXML private TableColumn<SellerRequest, String> statusColumn;
    @FXML private TableColumn<SellerRequest, Void> actionsColumn;
    @FXML private ProgressIndicator loadingIndicator;
    
    private final SellerRequestDAO sellerRequestDAO = new SellerRequestDAO();
    private final UserDAO userDAO = new UserDAO();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadSellerRequests();
    }
    
    private void setupTableColumns() {
        // Set up table columns with cell value factories
        idColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));
        
        usernameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getUsername()));
        
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(dateFormat.format(cellData.getValue().getRequestDate())));
        
        reasonColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getReason()));
        
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus()));
        
        // Configure action column with approve/reject buttons
        actionsColumn.setCellFactory(new Callback<TableColumn<SellerRequest, Void>, TableCell<SellerRequest, Void>>() {
            @Override
            public TableCell<SellerRequest, Void> call(TableColumn<SellerRequest, Void> param) {
                return new TableCell<SellerRequest, Void>() {
                    private final Button approveButton = new Button("Approve");
                    private final Button rejectButton = new Button("Reject");
                    private final HBox pane = new HBox(5, approveButton, rejectButton);
                    
                    {
                        approveButton.getStyleClass().add("success-button");
                        rejectButton.getStyleClass().add("danger-button");
                        
                        approveButton.setOnAction(event -> {
                            SellerRequest request = getTableView().getItems().get(getIndex());
                            handleApprove(request);
                        });
                        
                        rejectButton.setOnAction(event -> {
                            SellerRequest request = getTableView().getItems().get(getIndex());
                            handleReject(request);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty) {
                            setGraphic(null);
                        } else {
                            SellerRequest request = getTableView().getItems().get(getIndex());
                            if ("pending".equals(request.getStatus())) {
                                setGraphic(pane);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                };
            }
        });
    }
    
    private void loadSellerRequests() {
        loadingIndicator.setVisible(true);
        
        new Thread(() -> {
            try {
                List<SellerRequest> requests = sellerRequestDAO.getAll();
                
                javafx.application.Platform.runLater(() -> {
                    requestsTable.setItems(FXCollections.observableArrayList(requests));
                    loadingIndicator.setVisible(false);
                });
            } catch (SQLException e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showError("Error", "Failed to load seller requests: " + e.getMessage());
                    loadingIndicator.setVisible(false);
                });
            }
        }).start();
    }
    
    private void handleApprove(SellerRequest request) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Approval");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to approve this seller request?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            loadingIndicator.setVisible(true);
            
            new Thread(() -> {
                boolean success = sellerRequestDAO.approveRequest(request.getId(), request.getUserId());
                
                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    if (success) {
                        showInformation("Success", "Seller request approved successfully!");
                        loadSellerRequests();
                    } else {
                        showError("Error", "Failed to approve seller request");
                    }
                });
            }).start();
        }
    }
    
    private void handleReject(SellerRequest request) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Rejection");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to reject this seller request?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            loadingIndicator.setVisible(true);
            
            new Thread(() -> {
                boolean success = sellerRequestDAO.rejectRequest(request.getId());
                
                javafx.application.Platform.runLater(() -> {
                    loadingIndicator.setVisible(false);
                    if (success) {
                        showInformation("Success", "Seller request rejected successfully!");
                        loadSellerRequests();
                    } else {
                        showError("Error", "Failed to reject seller request");
                    }
                });
            }).start();
        }
    }
    
    @FXML
    protected void handleBack() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/uiuonepoint/admin-dashboard.fxml"));
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());
            
            Stage stage = (Stage) requestsTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();
            
            AdminDashboardController controller = fxmlLoader.getController();
            controller.initData(currentUser);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not return to admin dashboard");
        }
    }
    
    @FXML
    protected void handleLogout() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/uiuonepoint/login.fxml"));
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());
            
            Stage stage = (Stage) requestsTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not log out");
        }
    }
    
    @Override
    protected void onInitialize() {
        // Not needed since we're implementing Initializable
    }
} 
package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.dao.GroceryOrderDAO;
import com.example.uiuonepoint.dao.UserDAO;
import com.example.uiuonepoint.model.GroceryOrder;
import com.example.uiuonepoint.model.GroceryOrderItem;
import com.example.uiuonepoint.model.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AdminGroceryOrdersController extends AdminBaseController {
    @FXML private TableView<GroceryOrder> pendingOrdersTable;
    @FXML private TableView<GroceryOrder> completedOrdersTable;
    @FXML private ProgressIndicator loadingIndicator;

    // Pending orders columns
    @FXML private TableColumn<GroceryOrder, String> pendingOrderIdColumn;
    @FXML private TableColumn<GroceryOrder, String> pendingCustomerColumn;
    @FXML private TableColumn<GroceryOrder, String> pendingDateColumn;
    @FXML private TableColumn<GroceryOrder, String> pendingItemsColumn;
    @FXML private TableColumn<GroceryOrder, String> pendingTotalColumn;
    @FXML private TableColumn<GroceryOrder, Void> pendingActionsColumn;

    // Completed orders columns
    @FXML private TableColumn<GroceryOrder, String> completedOrderIdColumn;
    @FXML private TableColumn<GroceryOrder, String> completedCustomerColumn;
    @FXML private TableColumn<GroceryOrder, String> completedDateColumn;
    @FXML private TableColumn<GroceryOrder, String> completedItemsColumn;
    @FXML private TableColumn<GroceryOrder, String> completedTotalColumn;

    private final GroceryOrderDAO groceryOrderDAO = new GroceryOrderDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void onInitialize() {
        setupColumns();
        loadOrders();
    }

    private void setupColumns() {
        // Setup pending orders columns
        pendingOrderIdColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        pendingCustomerColumn.setCellValueFactory(data -> {
            int userId = data.getValue().getUserId();
            String username = "Unknown";
            try {
                username = userDAO.get(userId)
                    .map(User::getUsername)
                    .orElse("Unknown");
            } catch (SQLException e) {
                e.printStackTrace();
                username = "Error";
            }
            return new SimpleStringProperty(username);
        });
        pendingDateColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getOrderTime().toString()));
        pendingItemsColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(formatOrderItems(data.getValue().getItems())));
        pendingTotalColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.format("৳%.2f", data.getValue().getTotal())));

        // Add action button for pending orders
        pendingActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button completeButton = new Button("Complete");

            {
                completeButton.getStyleClass().add("complete");
                completeButton.setOnAction(event -> {
                    GroceryOrder order = getTableView().getItems().get(getIndex());
                    handleCompleteOrder(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(completeButton);
                }
            }
        });

        // Setup completed orders columns
        completedOrderIdColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        completedCustomerColumn.setCellValueFactory(data -> {
            int userId = data.getValue().getUserId();
            String username = "Unknown";
            try {
                username = userDAO.get(userId)
                    .map(User::getUsername)
                    .orElse("Unknown");
            } catch (SQLException e) {
                e.printStackTrace();
                username = "Error";
            }
            return new SimpleStringProperty(username);
        });
        completedDateColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getOrderTime().toString()));
        completedItemsColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(formatOrderItems(data.getValue().getItems())));
        completedTotalColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.format("৳%.2f", data.getValue().getTotal())));
    }

    private void loadOrders() {
        loadingIndicator.setVisible(true);
        try {
            System.out.println("Loading grocery orders...");
            List<GroceryOrder> allOrders = groceryOrderDAO.getAll();
            System.out.println("Total grocery orders loaded: " + allOrders.size());
            
            List<GroceryOrder> pendingOrders = allOrders.stream()
                .filter(order -> "pending".equalsIgnoreCase(order.getStatus()))
                .collect(Collectors.toList());
            System.out.println("Pending orders: " + pendingOrders.size());
            
            List<GroceryOrder> completedOrders = allOrders.stream()
                .filter(order -> "completed".equalsIgnoreCase(order.getStatus()))
                .collect(Collectors.toList());
            System.out.println("Completed orders: " + completedOrders.size());

            pendingOrdersTable.setItems(FXCollections.observableArrayList(pendingOrders));
            completedOrdersTable.setItems(FXCollections.observableArrayList(completedOrders));
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error", "Failed to load orders: " + e.getMessage());
        } finally {
            loadingIndicator.setVisible(false);
        }
    }

    private void handleCompleteOrder(GroceryOrder order) {
        try {
            order.setStatus("completed");  // Changed to lowercase to match database
            groceryOrderDAO.update(order);
            loadOrders(); // Refresh the tables
            showInformation("Success", "Order marked as completed!");
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error", "Failed to complete order: " + e.getMessage());
        }
    }

    private String formatOrderItems(List<GroceryOrderItem> items) {
        return items.stream()
            .map(item -> String.format("%dx %s", item.getQuantity(), item.getGroceryItemName()))
            .collect(Collectors.joining(", "));
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/uiuonepoint/admin-dashboard.fxml"));
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());
            
            Stage stage = (Stage) pendingOrdersTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();

            AdminDashboardController controller = fxmlLoader.getController();
            controller.initData(currentUser);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not load admin dashboard: " + e.getMessage());
        }
    }
} 
package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.dao.FoodOrderDAO;
import com.example.uiuonepoint.dao.GroceryOrderDAO;
import com.example.uiuonepoint.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.stage.Stage;
import javafx.scene.control.ProgressIndicator;
import javafx.application.Platform;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class MyOrdersController extends CustomerBaseController {
    @FXML private TableView<FoodOrder> pendingFoodOrdersTable;
    @FXML private TableView<FoodOrder> completedFoodOrdersTable;
    @FXML private TableView<GroceryOrder> pendingGroceryOrdersTable;
    @FXML private TableView<GroceryOrder> completedGroceryOrdersTable;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label welcomeLabel;

    private final FoodOrderDAO foodOrderDAO = new FoodOrderDAO();
    private final GroceryOrderDAO groceryOrderDAO = new GroceryOrderDAO();

    @FXML
    public void initialize() {
        // Setup tables structure
        setupFoodOrderTables();
        setupGroceryOrderTables();
    }

    @Override
    protected void onInitialize() {
        // This will be called after user data is set
        if (currentUser != null) {
            loadOrders();
        }
    }

    @Override
    public void initData(User user) {
        super.initData(user);
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername());
            // Orders will be loaded in onInitialize()
        }
    }

    private void setupFoodOrderTables() {
        // Setup Food Orders Tables
        setupFoodOrderColumns(pendingFoodOrdersTable);
        setupFoodOrderColumns(completedFoodOrdersTable);
    }

    private void setupGroceryOrderTables() {
        // Setup Grocery Orders Tables
        setupGroceryOrderColumns(pendingGroceryOrdersTable);
        setupGroceryOrderColumns(completedGroceryOrdersTable);
    }

    private void setupFoodOrderColumns(TableView<FoodOrder> table) {
        TableColumn<FoodOrder, String> idCol = (TableColumn<FoodOrder, String>) table.getColumns().get(0);
        TableColumn<FoodOrder, String> dateCol = (TableColumn<FoodOrder, String>) table.getColumns().get(1);
        TableColumn<FoodOrder, String> statusCol = (TableColumn<FoodOrder, String>) table.getColumns().get(2);
        TableColumn<FoodOrder, String> totalCol = (TableColumn<FoodOrder, String>) table.getColumns().get(3);
        TableColumn<FoodOrder, String> itemsCol = (TableColumn<FoodOrder, String>) table.getColumns().get(4);

        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrderTime().toString()));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        totalCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("৳%.2f", data.getValue().getTotal())));
        itemsCol.setCellValueFactory(data -> new SimpleStringProperty(formatOrderItems(data.getValue().getItems())));
    }

    private void setupGroceryOrderColumns(TableView<GroceryOrder> table) {
        TableColumn<GroceryOrder, String> idCol = (TableColumn<GroceryOrder, String>) table.getColumns().get(0);
        TableColumn<GroceryOrder, String> dateCol = (TableColumn<GroceryOrder, String>) table.getColumns().get(1);
        TableColumn<GroceryOrder, String> statusCol = (TableColumn<GroceryOrder, String>) table.getColumns().get(2);
        TableColumn<GroceryOrder, String> totalCol = (TableColumn<GroceryOrder, String>) table.getColumns().get(3);
        TableColumn<GroceryOrder, String> itemsCol = (TableColumn<GroceryOrder, String>) table.getColumns().get(4);

        idCol.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrderTime().toString()));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        totalCol.setCellValueFactory(data -> new SimpleStringProperty(String.format("৳%.2f", data.getValue().getTotal())));
        itemsCol.setCellValueFactory(data -> new SimpleStringProperty(formatOrderItems(data.getValue().getItems())));
    }

    private String formatOrderItems(List<?> items) {
        return items.stream()
                .map(item -> {
                    if (item instanceof FoodOrderItem) {
                        FoodOrderItem foi = (FoodOrderItem) item;
                        return String.format("%dx %s", foi.getQuantity(), foi.getFoodItemName());
                    } else if (item instanceof GroceryOrderItem) {
                        GroceryOrderItem goi = (GroceryOrderItem) item;
                        return String.format("%dx %s", goi.getQuantity(), goi.getGroceryItemName());
                    }
                    return "";
                })
                .collect(Collectors.joining(", "));
    }

    private void loadOrders() {
        loadingIndicator.setVisible(true);

        // Run in background thread
        new Thread(() -> {
            try {
                // Load Food Orders
                List<FoodOrder> foodOrders = foodOrderDAO.getByUserId(currentUser.getId());
                List<FoodOrder> pendingFood = foodOrders.stream()
                        .filter(order -> "pending".equalsIgnoreCase(order.getStatus()))
                        .collect(Collectors.toList());
                List<FoodOrder> completedFood = foodOrders.stream()
                        .filter(order -> "completed".equalsIgnoreCase(order.getStatus()))
                        .collect(Collectors.toList());

                // Load Grocery Orders
                List<GroceryOrder> groceryOrders = groceryOrderDAO.getByUserId(currentUser.getId());
                List<GroceryOrder> pendingGrocery = groceryOrders.stream()
                        .filter(order -> "pending".equalsIgnoreCase(order.getStatus()))
                        .collect(Collectors.toList());
                List<GroceryOrder> completedGrocery = groceryOrders.stream()
                        .filter(order -> "completed".equalsIgnoreCase(order.getStatus()))
                        .collect(Collectors.toList());

                // Update UI in JavaFX Application Thread
                Platform.runLater(() -> {
                    pendingFoodOrdersTable.setItems(FXCollections.observableArrayList(pendingFood));
                    completedFoodOrdersTable.setItems(FXCollections.observableArrayList(completedFood));
                    pendingGroceryOrdersTable.setItems(FXCollections.observableArrayList(pendingGrocery));
                    completedGroceryOrdersTable.setItems(FXCollections.observableArrayList(completedGrocery));
                    loadingIndicator.setVisible(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Error", "Failed to load orders: " + e.getMessage());
                    loadingIndicator.setVisible(false);
                });
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    protected void handleBack() {
        try {
            String previousView = getPreviousView();
            URL fxmlUrl = getClass().getResource("/com/example/uiuonepoint/" + previousView);
            if (fxmlUrl == null) {
                throw new IOException("Cannot find FXML file: " + previousView);
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());
            
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();

            Object controller = fxmlLoader.getController();
            if (controller instanceof CustomerBaseController) {
                ((CustomerBaseController) controller).initData(currentUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not load view: " + e.getMessage());
        }
    }

    @FXML
    protected void handleBackinMyOrders() {
        handleBack();
    }

    private String getPreviousView() {
        // Determine which view to return to based on where we came from
        // This could be stored in a field when navigating to this view
        return "section-selector.fxml";
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 
package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.dao.EShoppingOrderDAO;
import com.example.uiuonepoint.dao.UserDAO;
import com.example.uiuonepoint.model.EShoppingOrder;
import com.example.uiuonepoint.model.User;
import com.example.uiuonepoint.util.ChatLauncher;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ComboBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Optional;
import java.io.IOException;

public class EShoppingOrdersController extends SellerBaseController implements Initializable {
    @FXML private Label welcomeLabel;
    @FXML private TableView<EShoppingOrder> ordersTable;
    @FXML private TableColumn<EShoppingOrder, Integer> orderIdColumn;
    @FXML private TableColumn<EShoppingOrder, String> productNameColumn;
    @FXML private TableColumn<EShoppingOrder, String> customerColumn;
    @FXML private TableColumn<EShoppingOrder, String> mobileColumn;
    @FXML private TableColumn<EShoppingOrder, Integer> quantityColumn;
    @FXML private TableColumn<EShoppingOrder, Double> totalColumn;
    @FXML private TableColumn<EShoppingOrder, String> statusColumn;
    @FXML private TableColumn<EShoppingOrder, Void> actionsColumn;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ProgressIndicator loadingIndicator;

    private final EShoppingOrderDAO orderDAO = new EShoppingOrderDAO();
    private final UserDAO userDAO = new UserDAO();
    private FilteredList<EShoppingOrder> filteredOrders;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupStatusFilter();
        ordersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Set up a timer to refresh data every 30 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
            if (currentUser != null) {
                loadOrders();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @Override
    protected void onInitialize() {
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername() + " (Seller)");
            loadOrders();
        }
    }

    private void setupTableColumns() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        customerColumn.setCellValueFactory(cellData -> {
            try {
                Optional<User> user = userDAO.get(cellData.getValue().getUserId());
                return new javafx.beans.property.SimpleStringProperty(
                    user.map(User::getUsername).orElse("Unknown")
                );
            } catch (SQLException e) {
                return new javafx.beans.property.SimpleStringProperty("Error");
            }
        });
        mobileColumn.setCellValueFactory(new PropertyValueFactory<>("mobileNumber"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalColumn.setCellValueFactory(cellData -> {
            EShoppingOrder order = cellData.getValue();
            double total = order.getQuantity() * order.getProductPrice();
            return new javafx.beans.property.SimpleDoubleProperty(total).asObject();
        });
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Center align all columns
        centerAlignColumn(orderIdColumn);
        centerAlignColumn(productNameColumn);
        centerAlignColumn(customerColumn);
        centerAlignColumn(mobileColumn);
        centerAlignColumn(quantityColumn);
        centerAlignColumn(totalColumn);
        centerAlignColumn(statusColumn);

        // Setup actions column
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button updateButton = new Button("Status");
            private final Button chatButton = new Button("Chat");
            private final HBox buttonsBox = new HBox(10, updateButton, chatButton);

            {
                buttonsBox.setAlignment(Pos.CENTER);
                updateButton.setOnAction(event -> {
                    EShoppingOrder order = getTableView().getItems().get(getIndex());
                    showStatusUpdateDialog(order);
                });
                chatButton.setOnAction(event -> {
                    EShoppingOrder order = getTableView().getItems().get(getIndex());
                    ChatLauncher.launchChat(currentUser.getUsername(), true, String.valueOf(order.getId()));
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    EShoppingOrder order = getTableView().getItems().get(getIndex());
                    String status = order.getStatus();
                    if ("delivered".equalsIgnoreCase(status) || "canceled".equalsIgnoreCase(status)) {
                        setGraphic(null);
                    } else {
                        setGraphic(buttonsBox);
                    }
                }
            }
        });
    }

    // Helper method to center align a TableColumn
    private <T> void centerAlignColumn(TableColumn<EShoppingOrder, T> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
                setAlignment(Pos.CENTER);
            }
        });
    }

    private void setupStatusFilter() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
            "All", "pending", "received", "on_campus", "delivered", "canceled"
        ));
        statusFilterComboBox.setValue("All");
        statusFilterComboBox.setOnAction(e -> applyFilter());
    }

    private void loadOrders() {
        if (currentUser == null) return;
        
        loadingIndicator.setVisible(true);
        
        try {
            List<EShoppingOrder> orders = orderDAO.getBySellerId(currentUser.getId());
            System.out.println("Loaded " + orders.size() + " orders for seller ID: " + currentUser.getId());
            for (EShoppingOrder order : orders) {
                System.out.println("Order ID: " + order.getId() + 
                                 ", Product: " + order.getProductName() + 
                                 ", Status: " + order.getStatus());
            }
            
            filteredOrders = new FilteredList<>(FXCollections.observableArrayList(orders));
            SortedList<EShoppingOrder> sortedOrders = new SortedList<>(filteredOrders);
            sortedOrders.comparatorProperty().bind(ordersTable.comparatorProperty());
            ordersTable.setItems(sortedOrders);
            applyFilter();
        } catch (SQLException e) {
            System.err.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
            showError("Error", "Failed to load orders: " + e.getMessage());
        } finally {
            loadingIndicator.setVisible(false);
        }
    }

    private void applyFilter() {
        String selectedStatus = statusFilterComboBox.getValue();
        if (selectedStatus == null || selectedStatus.equals("All")) {
            filteredOrders.setPredicate(order -> true);
        } else {
            filteredOrders.setPredicate(order -> 
                order.getStatus().equals(selectedStatus)
            );
        }
    }

    private void showStatusUpdateDialog(EShoppingOrder order) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Update Order Status");
        dialog.setHeaderText("Update status for Order #" + order.getId());

        ComboBox<String> statusComboBox = new ComboBox<>(FXCollections.observableArrayList(
            "pending", "received", "on_campus", "delivered", "canceled"
        ));
        statusComboBox.setValue(order.getStatus());
        statusComboBox.setPrefWidth(200);

        dialog.getDialogPane().setContent(statusComboBox);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return statusComboBox.getValue();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newStatus -> {
            try {
                orderDAO.updateStatus(order.getId(), newStatus);
                loadOrders(); // Reload orders to reflect changes
                showInformation("Success", "Order status updated successfully");
            } catch (SQLException e) {
                showError("Error", "Failed to update order status: " + e.getMessage());
            }
        });
    }

    @FXML
    protected void handleBack() {
        loadView("seller-dashboard.fxml");
    }

    @FXML
    protected void handleLogout() {
        loadView("login.fxml");
    }

    @Override
    protected void loadView(String fxmlFile) {
        try {
            URL fxmlUrl = getClass().getResource("/com/example/uiuonepoint/" + fxmlFile);
            if (fxmlUrl == null) {
                throw new IOException("Cannot find FXML file: " + fxmlFile);
            }

            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            
            // Create root and scene with screen dimensions
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());
            
            // Get the stage and set the new scene
            Stage stage = (Stage) ordersTable.getScene().getWindow();
            stage.setScene(scene);
            
            // Configure stage properties
            stage.setMaximized(true);
            stage.setResizable(true);
            stage.centerOnScreen();

            // Pass user data to the next controller
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
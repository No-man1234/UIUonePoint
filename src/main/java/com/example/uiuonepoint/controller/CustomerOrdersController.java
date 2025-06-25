package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.dao.EShoppingOrderDAO;
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
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.List;
import java.io.IOException;

public class CustomerOrdersController extends CustomerBaseController implements Initializable {
    @FXML private TableView<EShoppingOrder> ordersTable;
    @FXML private TableColumn<EShoppingOrder, Integer> orderIdColumn;
    @FXML private TableColumn<EShoppingOrder, String> productNameColumn;
    @FXML private TableColumn<EShoppingOrder, Integer> quantityColumn;
    @FXML private TableColumn<EShoppingOrder, Double> totalColumn;
    @FXML private TableColumn<EShoppingOrder, String> statusColumn;
    @FXML private TableColumn<EShoppingOrder, String> dateColumn;
    @FXML private TableColumn<EShoppingOrder, Void> chatColumn;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ProgressIndicator loadingIndicator;

    private final EShoppingOrderDAO orderDAO = new EShoppingOrderDAO();
    private FilteredList<EShoppingOrder> filteredOrders;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupStatusFilter();
        
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
            loadOrders();
        }
    }

    private void setupTableColumns() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        totalColumn.setCellValueFactory(cellData -> {
            EShoppingOrder order = cellData.getValue();
            double total = order.getQuantity() * order.getProductPrice();
            return new javafx.beans.property.SimpleDoubleProperty(total).asObject();
        });
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Setup chat column
        chatColumn.setCellFactory(col -> new TableCell<>() {
            private final Button chatButton = new Button("Chat");
            private final HBox buttonsBox = new HBox(10, chatButton);

            {
                buttonsBox.setAlignment(Pos.CENTER);
                chatButton.setOnAction(event -> {
                    EShoppingOrder order = getTableView().getItems().get(getIndex());
                    ChatLauncher.launchChat(currentUser.getUsername(), false, String.valueOf(order.getId()));
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
            List<EShoppingOrder> orders = orderDAO.getByUserId(currentUser.getId());
            filteredOrders = new FilteredList<>(FXCollections.observableArrayList(orders));
            SortedList<EShoppingOrder> sortedOrders = new SortedList<>(filteredOrders);
            sortedOrders.comparatorProperty().bind(ordersTable.comparatorProperty());
            ordersTable.setItems(sortedOrders);
            applyFilter();
        } catch (SQLException e) {
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

    @FXML
    protected void handleBack() {
        loadView("eshopping-view.fxml");
    }

    @FXML
    protected void handleLogout() {
        loadView("login.fxml");
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/uiuonepoint/" + fxmlFile));
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());

            Stage stage = (Stage) ordersTable.getScene().getWindow();
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
} 
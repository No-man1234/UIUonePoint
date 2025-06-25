package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.dao.GroceryItemDAO;
import com.example.uiuonepoint.dao.GroceryOrderDAO;
import com.example.uiuonepoint.model.GroceryItem;
import com.example.uiuonepoint.model.GroceryOrder;
import com.example.uiuonepoint.model.GroceryOrderItem;
import com.example.uiuonepoint.model.User;
import com.example.uiuonepoint.util.CartManager;
import com.example.uiuonepoint.util.ImageCache;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.stage.Stage;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import javafx.scene.Cursor;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroceryViewController extends CustomerBaseController {
    @FXML private ComboBox<String> filterComboBox;
    @FXML private GridPane groceryItemsGrid;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> cartItemColumn;
    @FXML private TableColumn<CartItem, Integer> quantityColumn;
    @FXML private TableColumn<CartItem, Double> itemPriceColumn;
    @FXML private TableColumn<CartItem, Double> totalColumn;
    @FXML private TableColumn<CartItem, Void> removeColumn;
    @FXML private Label totalAmountLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private StackPane mainContainer;

    private final GroceryItemDAO groceryItemDAO = new GroceryItemDAO();
    private final GroceryOrderDAO groceryOrderDAO = new GroceryOrderDAO();
    private final CartManager cartManager = CartManager.getInstance();
    private ObservableList<CartItem> cartItems;
    private double totalAmount = 0.0;

    @FXML
    public void initialize() {
        // Initialize filter combo box with categories
        filterComboBox.setItems(FXCollections.observableArrayList("All", "Snacks", "Beverages", "Stationery", "Others"));
        filterComboBox.setValue("All");
        
        // Initialize cart items
        cartItems = FXCollections.observableArrayList();
        ObservableList<Object> savedCartItems = cartManager.getGroceryCartItems();
        if (savedCartItems != null) {
            for (Object item : savedCartItems) {
                if (item instanceof CartItem) {
                    cartItems.add((CartItem) item);
                }
            }
        }
        
        // Calculate total amount
        totalAmount = cartItems.stream()
                .mapToDouble(CartItem::getTotal)
                .sum();
        updateTotalAmount();
        
        // Setup tables
        setupCartTable();
        setupFilterListener();

        // Load grocery items
        loadGroceryItems();
    }

    @Override
    public void initData(User user) {
        System.out.println("GroceryViewController.initData called with user: " + (user != null ? user.getUsername() : "null"));
        super.initData(user);
        
        // Update total amount after user is set
        if (currentUser != null) {
            updateTotalAmount();
        }
    }

    @Override
    protected void onInitialize() {
        System.out.println("GroceryViewController onInitialize called");
        if (currentUser == null) {
            showError("Error", "No user data available");
            return;
        }
        updateTotalAmount();
    }

    private void setupGroceryItemsTable() {
        // Remove old table setup code
    }

    private void displayGroceryItems(List<GroceryItem> items) {
        groceryItemsGrid.getChildren().clear();
        int columnCount = 3; // Number of items per row
        int row = 0;
        int col = 0;

        for (GroceryItem item : items) {
            VBox itemCard = createGroceryItemCard(item);
            groceryItemsGrid.add(itemCard, col, row);
            
            col++;
            if (col >= columnCount) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createGroceryItemCard(GroceryItem item) {
        VBox card = new VBox(10);
        card.getStyleClass().add("item-card");
        card.setPrefWidth(200);
        card.setMaxWidth(200);
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(event -> showItemDetails(item));

        // Image
        ImageView imageView = new ImageView();
        imageView.getStyleClass().add("item-image");
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        
        // Use ImageCache to load image efficiently
        ImageCache.getImage(item.getImage(), false, image -> imageView.setImage(image));

        // Name
        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("item-name");

        // Price
        Label priceLabel = new Label(String.format("৳%.2f", item.getPrice()));
        priceLabel.getStyleClass().add("item-price");

        // Stock
        Label stockLabel = new Label("Stock: " + item.getStockQuantity());
        stockLabel.getStyleClass().add("item-stock");

        // Add to Cart Button
        Button addButton = new Button("Add");
        addButton.getStyleClass().add("add-to-cart-button");
        addButton.setOnAction(event -> addToCart(item));

        card.getChildren().addAll(imageView, nameLabel, priceLabel, stockLabel, addButton);
        return card;
    }

    private void setupCartTable() {
        cartTable.setItems(cartItems);
        cartItemColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        quantityColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        itemPriceColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getUnitPrice()).asObject());
        totalColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getTotal()).asObject());
        
        setupRemoveFromCartColumn();
    }

    private void setupRemoveFromCartColumn() {
        removeColumn.setCellFactory(param -> new TableCell<CartItem, Void>() {
            private final HBox container = new HBox(5);
            private final Button decrementButton = new Button("-");
            private final Button removeButton = new Button("×");

            {
                decrementButton.getStyleClass().clear();
                removeButton.getStyleClass().clear();
                decrementButton.getStyleClass().add("table-button-remove");
                removeButton.getStyleClass().add("table-button-remove");
                
                decrementButton.setOnAction(event -> {
                    CartItem item = getTableRow().getItem();
                    if (item != null) {
                        if (item.getQuantity() > 1) {
                            item.decrementQuantity();
                            cartTable.refresh();
                            updateTotalAmount();
                        } else {
                            cartItems.remove(item);
                            updateTotalAmount();
                        }
                    }
                });

                removeButton.setOnAction(event -> {
                    CartItem item = getTableRow().getItem();
                    if (item != null) {
                        cartItems.remove(item);
                        updateTotalAmount();
                    }
                });

                container.setAlignment(Pos.CENTER);
                container.getChildren().addAll(decrementButton, removeButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
                setContentDisplay(ContentDisplay.CENTER);
            }
        });
    }

    private void setupFilterListener() {
        filterComboBox.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> loadGroceryItems()
        );
    }

    private void loadGroceryItems() {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
        }

        new Thread(() -> {
            try {
                String category = filterComboBox.getValue();
                final List<GroceryItem> items;

                if (category == null || category.equals("All")) {
                    items = groceryItemDAO.getAll();
                } else {
                    items = groceryItemDAO.getByCategory(category);
                }

                Platform.runLater(() -> {
                    try {
                        if (items == null || items.isEmpty()) {
                            showInformation("No Items", "No grocery items available at the moment");
                        }
                        displayGroceryItems(items);
                    } finally {
                        if (loadingIndicator != null) {
                            loadingIndicator.setVisible(false);
                        }
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    showError("Data Loading Error", "Failed to load grocery items: " + e.getMessage());
                    if (loadingIndicator != null) {
                        loadingIndicator.setVisible(false);
                    }
                });
                e.printStackTrace();
            }
        }).start();
    }

    public void addToCart(GroceryItem item) {
        System.out.println("Adding to cart: " + item.getName());
        for (CartItem cartItem : cartItems) {
            if (cartItem.getGroceryItemId() == item.getId()) {
                if (cartItem.getQuantity() < item.getStockQuantity()) {
                    cartItem.incrementQuantity();
                    cartTable.refresh();
                    updateTotalAmount();
                } else {
                    showError("Error", "Not enough stock available");
                }
                return;
            }
        }
        
        cartItems.add(new CartItem(item));
        updateTotalAmount();
        System.out.println("Cart updated, new size: " + cartItems.size());
    }

    private void updateTotalAmount() {
        if (cartItems != null) {
            totalAmount = cartItems.stream()
                    .mapToDouble(CartItem::getTotal)
                    .sum();
            totalAmountLabel.setText(String.format("Total Amount: ৳%.2f", totalAmount));
        }
    }

    @FXML
    protected void handlePlaceOrder() {
        if (cartItems == null || cartItems.isEmpty()) {
            showError("Error", "Your cart is empty");
            return;
        }

        try {
            GroceryOrder order = new GroceryOrder();
            order.setUserId(currentUser.getId());
            
            List<GroceryOrderItem> orderItems = new ArrayList<>();
            for (CartItem item : cartItems) {
                GroceryOrderItem orderItem = new GroceryOrderItem();
                orderItem.setGroceryItemId(item.getGroceryItemId());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setUnitPrice(item.getUnitPrice());
                orderItems.add(orderItem);
            }
            
            order.setItems(orderItems);
            groceryOrderDAO.save(order);
            
            // Clear cart after successful order
            cartItems.clear();
            cartManager.clearGroceryCart();
            updateTotalAmount();
            
            showInformation("Success", "Order placed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to place order. Please try again.");
        }
    }

    @FXML
    protected void handleViewOrders() {
        loadView("my-orders.fxml");
    }

    @FXML
    protected void handleBack() {
        try {
            // Save cart state before navigating away
            cartManager.setGroceryCartItems(FXCollections.observableArrayList(cartItems));
            cartManager.setGroceryTotalAmount(totalAmount);
            
            loadView("section-selector.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Could not load view: " + e.getMessage());
        }
    }

    @FXML
    protected void handleLogout() {
        loadView("login.fxml");
    }

    private void loadView(String fxmlFile) {
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
            Stage stage = (Stage) filterComboBox.getScene().getWindow();
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
            showError("Error", "Could not load view: " + e.getMessage());
        }
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

    private boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void showItemDetails(GroceryItem item) {
        try {
            // Load the FXML for the details popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/uiuonepoint/item-details.fxml"));
            Parent root = loader.load();
            
            // Get the controller and set the item data
            ItemDetailsController controller = loader.getController();
            controller.setItem(item);
            controller.setParentController(this);
            
            // Create and show the popup stage
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Item Details");
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not load item details");
        }
    }

    public static class CartItem {
        private final int groceryItemId;
        private final String name;
        private final double unitPrice;
        private int quantity;

        public CartItem(GroceryItem item) {
            this.groceryItemId = item.getId();
            this.name = item.getName();
            this.unitPrice = item.getPrice();
            this.quantity = 1;
        }

        public int getGroceryItemId() { return groceryItemId; }
        public String getName() { return name; }
        public double getUnitPrice() { return unitPrice; }
        public int getQuantity() { return quantity; }
        public double getTotal() { return unitPrice * quantity; }

        public void incrementQuantity() { quantity++; }
        public void decrementQuantity() { if (quantity > 1) quantity--; }
    }
} 
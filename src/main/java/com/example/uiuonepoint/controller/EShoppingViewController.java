package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.dao.EShoppingProductDAO;
import com.example.uiuonepoint.model.EShoppingProduct;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class EShoppingViewController extends CustomerBaseController {
    @FXML private Label welcomeLabel;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private GridPane productsGrid;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private TextField searchField;

    private final EShoppingProductDAO productDAO = new EShoppingProductDAO();

    @Override
    protected void onInitialize() {
        // Check if user is a seller (they should use the products manager instead)
        if (currentUser != null && currentUser.getRole().equals("SELLER")) {
            showError("Access Denied", "Sellers should use the Products Manager to manage their products.");
            loadView("eshopping-products-manager.fxml");
            return;
        }

        welcomeLabel.setText("Welcome, " + currentUser.getUsername());
        setupFilterComboBox();
        loadProducts();
    }

    private void setupFilterComboBox() {
        filterComboBox.setItems(FXCollections.observableArrayList(
            "All", "Electronics", "Clothing", "Books", "Accessories", "Others"
        ));
        filterComboBox.setValue("All");
        filterComboBox.setOnAction(e -> loadProducts());
    }

    private void loadProducts() {
        loadingIndicator.setVisible(true);
        productsGrid.getChildren().clear();
        
        try {
            List<EShoppingProduct> products;
            String category = filterComboBox.getValue();
            String searchText = searchField.getText().trim();
            
            if (category == null || category.equals("All")) {
                products = productDAO.getAll();
            } else {
                products = productDAO.getByCategory(category);
            }
            
            // Apply search filter if search text is not empty
            if (!searchText.isEmpty()) {
                products.removeIf(p -> !p.getName().toLowerCase().contains(searchText.toLowerCase()) &&
                                     !p.getDescription().toLowerCase().contains(searchText.toLowerCase()));
            }
            
            int row = 0;
            int col = 0;
            int maxCols = 3;
            
            for (EShoppingProduct product : products) {
                VBox productBox = createProductBox(product);
                productsGrid.add(productBox, col, row);
                
                col++;
                if (col >= maxCols) {
                    col = 0;
                    row++;
                }
            }
        } catch (SQLException e) {
            showError("Error", "Failed to load products: " + e.getMessage());
        } finally {
            loadingIndicator.setVisible(false);
        }
    }

    private VBox createProductBox(EShoppingProduct product) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        box.setPrefWidth(250);
        
        ImageView imageView = new ImageView();
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            try {
                Image image = new Image(product.getImagePath());
                imageView.setImage(image);
                imageView.setFitWidth(200);
                imageView.setFitHeight(200);
                imageView.setPreserveRatio(true);
            } catch (Exception e) {
                // Handle image loading error
            }
        }
        
        Text name = new Text(product.getName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        
        Text price = new Text(String.format("৳%.2f", product.getPrice()));
        price.setStyle("-fx-font-size: 12;");
        
        Text description = new Text(product.getDescription());
        description.setWrappingWidth(200);
        
        Button viewButton = new Button("View Details");
        viewButton.setOnAction(e -> showProductDetails(product));
        
        box.getChildren().addAll(imageView, name, price, description, viewButton);
        return box;
    }

    private void showProductDetails(EShoppingProduct product) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/uiuonepoint/eshopping-product-details.fxml"));
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());

            Stage stage = (Stage) productsGrid.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();

            EShoppingProductDetailsController controller = fxmlLoader.getController();
            controller.initData(currentUser);
            controller.setProduct(product);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not load product details: " + e.getMessage());
        }
    }

    @FXML
    protected void handleViewOrders() {
        loadView("customer-orders.fxml");
    }

    @FXML
    protected void handleBack() {
        loadView("section-selector.fxml");
    }

    @FXML
    protected void handleLogout() {
        loadView("login.fxml");
    }

    protected void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    protected void loadView(String fxmlFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/uiuonepoint/" + fxmlFile));
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());

            Stage stage = (Stage) productsGrid.getScene().getWindow();
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
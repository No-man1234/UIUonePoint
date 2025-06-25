package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.dao.EShoppingProductDAO;
import com.example.uiuonepoint.model.EShoppingProduct;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EShoppingProductsManagerController extends SellerBaseController {
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField stockQuantityField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Button uploadImageButton;
    @FXML private Label imagePathLabel;
    @FXML private TextField imagePathField;
    @FXML private ImageView imagePreview;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private TableView<EShoppingProduct> productsTable;
    @FXML private TableColumn<EShoppingProduct, Integer> idColumn;
    @FXML private TableColumn<EShoppingProduct, String> nameColumn;
    @FXML private TableColumn<EShoppingProduct, Double> priceColumn;
    @FXML private TableColumn<EShoppingProduct, String> descriptionColumn;
    @FXML private TableColumn<EShoppingProduct, String> categoryColumn;
    @FXML private TableColumn<EShoppingProduct, Integer> stockQuantityColumn;
    @FXML private TableColumn<EShoppingProduct, Void> actionsColumn;
    @FXML private VBox productDialog;

    private final EShoppingProductDAO productDAO = new EShoppingProductDAO();
    private EShoppingProduct selectedProduct;
    private String selectedImagePath;
    private static final String UPLOAD_DIR = "src/main/resources/com/example/uiuonepoint/uploads/";

    @Override
    protected void onInitialize() {
        // Check if user is a seller
        if (currentUser == null || !currentUser.getRole().equals("SELLER")) {
            showError("Access Denied", "Only sellers can access this page.");
            loadView("section-selector.fxml");
            return;
        }

        setupTable();
        setupCategoryComboBox();
        setupFilterComboBox();
        setupImageUpload();
        loadProducts();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        stockQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        
        setupActionsColumn();
    }

    private void setupCategoryComboBox() {
        categoryComboBox.setItems(FXCollections.observableArrayList(
            "Electronics", "Clothing", "Books", "Accessories", "Others"
        ));
    }

    private void setupFilterComboBox() {
        filterComboBox.setItems(FXCollections.observableArrayList(
            "All", "Electronics", "Clothing", "Books", "Accessories", "Others"
        ));
        filterComboBox.setValue("All");
        filterComboBox.setOnAction(e -> loadProducts());
    }

    private void setupImageUpload() {
        uploadImageButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Product Image");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            
            File selectedFile = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());
            if (selectedFile != null) {
                selectedImagePath = selectedFile.getAbsolutePath();
                imagePathField.setText(selectedFile.getName());
                imagePreview.setImage(new Image(selectedFile.toURI().toString()));
            }
        });
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<EShoppingProduct, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().add("table-button");
                deleteButton.getStyleClass().add("table-button-remove");
                
                editButton.setOnAction(event -> {
                    EShoppingProduct product = getTableRow().getItem();
                    if (product != null) {
                        editProduct(product);
                    }
                });
                
                deleteButton.setOnAction(event -> {
                    EShoppingProduct product = getTableRow().getItem();
                    if (product != null) {
                        deleteProduct(product);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadProducts() {
        loadingIndicator.setVisible(true);
        
        try {
            List<EShoppingProduct> products;
            String category = filterComboBox.getValue();
            
            if (category == null || category.equals("All")) {
                products = productDAO.getBySellerId(currentUser.getId());
            } else {
                products = productDAO.getByCategory(category);
                // Filter by seller ID
                products.removeIf(p -> p.getSellerId() != currentUser.getId());
            }
            
            productsTable.setItems(FXCollections.observableArrayList(products));
        } catch (SQLException e) {
            showError("Database Error", "Failed to load products: " + e.getMessage());
        } finally {
            loadingIndicator.setVisible(false);
        }
    }

    private void editProduct(EShoppingProduct product) {
        selectedProduct = product;
        nameField.setText(product.getName());
        priceField.setText(String.valueOf(product.getPrice()));
        stockQuantityField.setText(String.valueOf(product.getStockQuantity()));
        descriptionField.setText(product.getDescription());
        categoryComboBox.setValue(product.getCategory());
        
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            selectedImagePath = product.getImagePath();
            imagePathLabel.setText("Current image");
            imagePreview.setImage(new Image(product.getImagePath()));
        }
    }

    private void deleteProduct(EShoppingProduct product) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this product?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                productDAO.delete(product.getId(), currentUser.getId());
                loadProducts();
                showInformation("Success", "Product deleted successfully!");
            } catch (SQLException e) {
                showError("Database Error", "Failed to delete product: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            EShoppingProduct product = selectedProduct != null ? selectedProduct : new EShoppingProduct();
            product.setSellerId(currentUser.getId());
            product.setName(nameField.getText().trim());
            product.setPrice(Double.parseDouble(priceField.getText().trim()));
            product.setStockQuantity(Integer.parseInt(stockQuantityField.getText().trim()));
            product.setDescription(descriptionField.getText().trim());
            product.setCategory(categoryComboBox.getValue());
            if (selectedImagePath == null && selectedProduct != null) {
                product.setImagePath(selectedProduct.getImagePath());
            } else {
                product.setImagePath(selectedImagePath);
            }

            if (selectedProduct == null) {
                productDAO.save(product);
                showInformation("Success", "Product added successfully!");
            } else {
                productDAO.update(product);
                showInformation("Success", "Product updated successfully!");
            }

            loadProducts();
            clearForm();
        } catch (SQLException e) {
            showError("Database Error", "Failed to save product: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Validation Error", "Name is required");
            return false;
        }

        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price <= 0) {
                showError("Validation Error", "Price must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Validation Error", "Invalid price format");
            return false;
        }

        try {
            int stock = Integer.parseInt(stockQuantityField.getText().trim());
            if (stock < 0) {
                showError("Validation Error", "Stock quantity cannot be negative");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Validation Error", "Invalid stock quantity format");
            return false;
        }

        if (categoryComboBox.getValue() == null) {
            showError("Validation Error", "Category is required");
            return false;
        }

        return true;
    }

    private void clearForm() {
        selectedProduct = null;
        nameField.clear();
        priceField.clear();
        stockQuantityField.clear();
        descriptionField.clear();
        categoryComboBox.setValue(null);
        selectedImagePath = null;
        imagePathField.clear();
        imagePathLabel.setText("No image selected");
        imagePreview.setImage(null);
    }

    @FXML
    protected void handleClear() {
        clearForm();
    }

    @FXML
    protected void handleBack() {
        loadView("seller-dashboard.fxml");
    }

    @FXML
    protected void handleLogout() {
        loadView("login.fxml");
    }

    @FXML
    protected void handleAddProduct() {
        clearForm();
        // Show the product dialog (assuming productDialog is a VBox)
        productDialog.setVisible(true);
    }

    @FXML
    protected void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(productsTable.getScene().getWindow());
        if (selectedFile != null) {
            selectedImagePath = selectedFile.getAbsolutePath();
            // If you have an imagePathField or imagePathLabel, update it here
            if (imagePathLabel != null) imagePathLabel.setText(selectedFile.getName());
            if (imagePreview != null) imagePreview.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    protected void handleCancel() {
        clearForm();
        // Hide the product dialog (assuming productDialog is a VBox)
        productDialog.setVisible(false);
    }
} 
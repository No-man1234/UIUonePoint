package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.dao.GroceryItemDAO;
import com.example.uiuonepoint.model.GroceryItem;
import com.example.uiuonepoint.model.User;
import com.example.uiuonepoint.util.ImgBBUploader;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroceryItemsManagerController extends AdminBaseController {
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField stockQuantityField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Button uploadImageButton;
    @FXML private Label imagePathLabel;
    @FXML private TextField imagePathField;
    @FXML private ImageView imagePreview;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private TableView<GroceryItem> groceryItemsTable;
    @FXML private TableColumn<GroceryItem, Integer> idColumn;
    @FXML private TableColumn<GroceryItem, String> nameColumn;
    @FXML private TableColumn<GroceryItem, Double> priceColumn;
    @FXML private TableColumn<GroceryItem, String> categoryColumn;
    @FXML private TableColumn<GroceryItem, Integer> stockQuantityColumn;
    @FXML private TableColumn<GroceryItem, Void> actionsColumn;

    private final GroceryItemDAO groceryItemDAO = new GroceryItemDAO();
    private GroceryItem selectedItem;
    private String selectedImagePath;
    private static final String UPLOAD_DIR = "src/main/resources/com/example/uiuonepoint/uploads/";

    @Override
    protected void onInitialize() {
        setupTable();
        loadGroceryItems();
        setupFilterListener();
        setupImageUpload();
    }

    private void setupImageUpload() {
        uploadImageButton.setOnAction(event -> {
            handleImageBrowse();
        });
    }

    @FXML
    private void handleImageBrowse() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(uploadImageButton.getScene().getWindow());
        if (selectedFile != null) {
            try {
                loadingIndicator.setVisible(true);
                // Upload to ImgBB
                String imgbbUrl = ImgBBUploader.uploadImage(selectedFile);
                selectedImagePath = imgbbUrl;
                imagePathField.setText(selectedFile.getName());
                imagePathLabel.setText("Image uploaded successfully");
                
                // Preview the image
                Image image = new Image(imgbbUrl);
                imagePreview.setImage(image);
            } catch (IOException e) {
                showError("File Upload Error", "Failed to upload image: " + e.getMessage());
            } finally {
                loadingIndicator.setVisible(false);
            }
        }
    }

    private void setupTable() {
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        priceColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        stockQuantityColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStockQuantity()).asObject());
        
        // Center align all columns
        idColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
        
        nameColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
        
        // Format price column and center align
        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("৳%.2f", price));
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
        
        categoryColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
        
        stockQuantityColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
        
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<GroceryItem, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.getStyleClass().add("table-button");
                deleteButton.getStyleClass().add("table-button-remove");
                
                editButton.setOnAction(event -> {
                    GroceryItem item = getTableRow().getItem();
                    if (item != null) {
                        editItem(item);
                    }
                });
                
                deleteButton.setOnAction(event -> {
                    GroceryItem item = getTableRow().getItem();
                    if (item != null) {
                        deleteItem(item);
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

    private void setupFilterListener() {
        filterComboBox.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> loadGroceryItems()
        );
    }

    private void loadGroceryItems() {
        String category = filterComboBox.getValue();
        List<GroceryItem> items = new ArrayList<>();
        
        try {
            if (category == null || category.equals("All")) {
                items = groceryItemDAO.getAll();
            } else {
                items = groceryItemDAO.getByCategory(category);
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to load grocery items: " + e.getMessage());
        }
        
        groceryItemsTable.setItems(FXCollections.observableArrayList(items));
    }

    @FXML
    protected void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            GroceryItem item = selectedItem != null ? selectedItem : new GroceryItem();
            item.setName(nameField.getText().trim());
            item.setPrice(Double.parseDouble(priceField.getText().trim()));
            item.setStockQuantity(Integer.parseInt(stockQuantityField.getText().trim()));
            item.setCategory(categoryComboBox.getValue());
            if (selectedImagePath == null && selectedItem != null) {
                item.setImage(selectedItem.getImage());
            } else {
                item.setImage(selectedImagePath);
            }

            if (selectedItem == null) {
                groceryItemDAO.save(item);
                showInformation("Success", "Grocery item '" + item.getName() + "' has been added successfully!");
            } else {
                groceryItemDAO.update(item);
                showInformation("Success", "Grocery item '" + item.getName() + "' has been updated successfully!");
            }

            loadGroceryItems();
            clearForm();
        } catch (SQLException e) {
            showError("Database Error", "Failed to save grocery item: " + e.getMessage());
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

    private void editItem(GroceryItem item) {
        selectedItem = item;
        nameField.setText(item.getName());
        priceField.setText(String.valueOf(item.getPrice()));
        stockQuantityField.setText(String.valueOf(item.getStockQuantity()));
        categoryComboBox.setValue(item.getCategory());
        
        if (item.getImage() != null && !item.getImage().isEmpty()) {
            selectedImagePath = item.getImage();
            imagePathLabel.setText("Current image");
            imagePreview.setImage(new Image(item.getImage()));
        }
    }

    private void deleteItem(GroceryItem item) {
        if (showConfirmation("Delete Item", "Are you sure you want to delete this item?")) {
            try {
                groceryItemDAO.delete(item.getId());
                loadGroceryItems();
                showInformation("Success", "Item deleted successfully!");
            } catch (SQLException e) {
                showError("Database Error", "Failed to delete item: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void handleClear() {
        clearForm();
    }

    private void clearForm() {
        selectedItem = null;
        nameField.clear();
        priceField.clear();
        stockQuantityField.clear();
        categoryComboBox.setValue(null);
        selectedImagePath = null;
        imagePathField.clear();
        imagePathLabel.setText("No image selected");
        imagePreview.setImage(null);
    }

    @FXML
    protected void handleBack() {
        loadView("admin-dashboard.fxml");
    }

    @FXML
    protected void handleLogout() {
        loadView("login.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/uiuonepoint/" + fxmlFile));
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(fxmlLoader.load(), screenBounds.getWidth(), screenBounds.getHeight());
            
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.centerOnScreen();

            Object controller = fxmlLoader.getController();
            if (controller instanceof AdminBaseController) {
                ((AdminBaseController) controller).initData(currentUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not load view: " + e.getMessage());
        }
    }

    private boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
} 
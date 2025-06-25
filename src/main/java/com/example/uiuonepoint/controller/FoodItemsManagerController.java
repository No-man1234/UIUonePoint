package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.dao.FoodItemDAO;
import com.example.uiuonepoint.model.FoodItem;
import com.example.uiuonepoint.util.ImgBBUploader;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FoodItemsManagerController extends AdminBaseController {
    @FXML private TextField nameField;
    @FXML private TextField priceField;
    @FXML private TextField stockQuantityField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private CheckBox availableCheckBox;
    @FXML private Button uploadImageButton;
    @FXML private Label imagePathLabel;
    @FXML private TextField imagePathField;
    @FXML private ImageView imagePreview;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private TableView<FoodItem> foodItemsTable;
    @FXML private TableColumn<FoodItem, Integer> idColumn;
    @FXML private TableColumn<FoodItem, String> nameColumn;
    @FXML private TableColumn<FoodItem, Double> priceColumn;
    @FXML private TableColumn<FoodItem, String> categoryColumn;
    @FXML private TableColumn<FoodItem, Boolean> availableColumn;
    @FXML private TableColumn<FoodItem, Integer> stockQuantityColumn;
    @FXML private TableColumn<FoodItem, Void> actionsColumn;

    private final FoodItemDAO foodItemDAO = new FoodItemDAO();
    private FoodItem selectedItem;
    private String selectedImagePath;
    private static final String UPLOAD_DIR = "src/main/resources/com/example/uiuonepoint/uploads/";

    @Override
    protected void onInitialize() {
        setupTable();
        loadFoodItems();
        setupFilterListener();
        setupImageUpload();
        
        // Initialize stock quantity column
        stockQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
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
        availableColumn.setCellValueFactory(data -> new SimpleBooleanProperty(data.getValue().isAvailable()));
        
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
        
        availableColumn.setCellFactory(column -> new TableCell<FoodItem, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            
            {
                checkBox.setDisable(false);
                checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        FoodItem item = getTableRow().getItem();
                        item.setAvailable(newVal);
                        try {
                            foodItemDAO.update(item);
                            loadFoodItems(); // Refresh the table
                        } catch (SQLException e) {
                            showError("Database Error", "Failed to update availability: " + e.getMessage());
                            checkBox.setSelected(oldVal); // Revert on error
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Boolean available, boolean empty) {
                super.updateItem(available, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(available != null && available);
                    setGraphic(checkBox);
                }
                setAlignment(javafx.geometry.Pos.CENTER);
            }
        });
        
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox container = new HBox(5, editButton, deleteButton);

            {
                // Style buttons
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");
                container.setStyle("-fx-alignment: center;");

                editButton.setOnAction(event -> {
                    FoodItem item = getTableRow().getItem();
                    if (item != null) {
                        populateForm(item);
                    }
                });

                deleteButton.setOnAction(event -> {
                    FoodItem item = getTableRow().getItem();
                    if (item != null) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Delete Item");
                        alert.setHeaderText(null);
                        alert.setContentText("Are you sure you want to delete " + item.getName() + "?");
                        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                            try {
                                foodItemDAO.delete(item.getId());
                                loadFoodItems();
                                clearForm(); // Clear form if the deleted item was being edited
                            } catch (SQLException e) {
                                showError("Database Error", "Failed to delete item: " + e.getMessage());
                            }
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void setupFilterListener() {
        filterComboBox.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> loadFoodItems()
        );
    }

    private void loadFoodItems() {
        String category = filterComboBox.getValue();
        List<FoodItem> items = new ArrayList<>();
        
        try {
            if (category == null || category.equals("All")) {
                items = foodItemDAO.getAll();
            } else {
                items = foodItemDAO.getByCategory(category);
            }
        } catch (SQLException e) {
            showError("Database Error", "Failed to load food items: " + e.getMessage());
        }
        
        foodItemsTable.setItems(FXCollections.observableArrayList(items));
    }

    @FXML
    protected void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            FoodItem item = selectedItem != null ? selectedItem : new FoodItem();
            item.setName(nameField.getText().trim());
            item.setPrice(Double.parseDouble(priceField.getText().trim()));
            int stock = Integer.parseInt(stockQuantityField.getText().trim());
            item.setStockQuantity(stock);
            item.setDescription(descriptionField.getText().trim());
            item.setCategory(categoryComboBox.getValue());
            // Set available: auto-false if stock is 0, else use checkbox
            if (stock == 0) {
                item.setAvailable(false);
            } else {
                item.setAvailable(availableCheckBox.isSelected());
            }
            if (selectedImagePath == null && selectedItem != null) {
                item.setImage(selectedItem.getImage());
            } else {
                item.setImage(selectedImagePath);
            }

            if (selectedItem == null) {
                foodItemDAO.save(item);
                showInformation("Success", "Food item added successfully!");
            } else {
                foodItemDAO.update(item);
                showInformation("Success", "Food item updated successfully!");
            }

            loadFoodItems();
            clearForm();
        } catch (SQLException e) {
            showError("Database Error", "Failed to save food item: " + e.getMessage());
        }
    }

    @FXML
    protected void handleClear() {
        clearForm();
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
            } else if (controller instanceof AdminBaseController) {
                ((AdminBaseController) controller).initData(currentUser);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not load view: " + e.getMessage());
        }
    }

    private void populateForm(FoodItem item) {
        selectedItem = item;
        nameField.setText(item.getName());
        priceField.setText(String.valueOf(item.getPrice()));
        stockQuantityField.setText(String.valueOf(item.getStockQuantity()));
        descriptionField.setText(item.getDescription());
        categoryComboBox.setValue(item.getCategory());
        availableCheckBox.setSelected(item.isAvailable());
        selectedImagePath = item.getImagePath();
        imagePathField.setText(item.getImagePath() != null ? item.getImagePath().substring(item.getImagePath().lastIndexOf("/") + 1) : "");
        imagePathLabel.setText(selectedImagePath != null ? "Current image: " + selectedImagePath : "No image selected");
    }

    private void clearForm() {
        selectedItem = null;
        nameField.clear();
        priceField.clear();
        stockQuantityField.clear();
        descriptionField.clear();
        categoryComboBox.setValue(null);
        availableCheckBox.setSelected(false);
        selectedImagePath = null;
        imagePathField.clear();
        imagePathLabel.setText("No image selected");
    }

    private boolean validateInput() {
        List<String> errors = new ArrayList<>();

        if (nameField.getText().trim().isEmpty()) {
            errors.add("Name is required");
        }

        try {
            double price = Double.parseDouble(priceField.getText().trim());
            if (price <= 0) {
                errors.add("Price must be greater than 0");
            }
        } catch (NumberFormatException e) {
            errors.add("Price must be a valid number");
        }

        try {
            int stock = Integer.parseInt(stockQuantityField.getText().trim());
            if (stock < 0) {
                errors.add("Stock quantity cannot be negative");
            }
        } catch (NumberFormatException e) {
            errors.add("Stock quantity must be a valid number");
        }

        if (categoryComboBox.getValue() == null || categoryComboBox.getValue().trim().isEmpty()) {
            errors.add("Category is required");
        }

        if (!errors.isEmpty()) {
            showError("Validation Error", String.join("\n", errors));
            return false;
        }

        return true;
    }
} 
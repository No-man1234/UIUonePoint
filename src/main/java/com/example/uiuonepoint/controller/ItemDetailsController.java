package com.example.uiuonepoint.controller;

import com.example.uiuonepoint.model.FoodItem;
import com.example.uiuonepoint.model.GroceryItem;
import com.example.uiuonepoint.util.ImageCache;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ItemDetailsController {
    @FXML private ImageView itemImageView;
    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label categoryLabel;
    @FXML private Label stockLabel;
    @FXML private Label descriptionLabel;
    @FXML private Button addToCartButton;
    @FXML private Button closeButton;
    @FXML private VBox detailsContainer;

    private Object item;
    private Object parentController;

    public void setItem(Object item) {
        this.item = item;
        updateUI();
    }

    public void setParentController(Object controller) {
        this.parentController = controller;
    }

    private void updateUI() {
        if (item != null) {
            String name = "";
            double price = 0;
            String category = "";
            int stock = 0;
            String description = "";
            String imageUrl = "";
            boolean isFood = false;

            if (item instanceof FoodItem) {
                FoodItem foodItem = (FoodItem) item;
                name = foodItem.getName();
                price = foodItem.getPrice();
                category = foodItem.getCategory();
                stock = foodItem.getStockQuantity();
                description = foodItem.getDescription();
                imageUrl = foodItem.getImage();
                isFood = true;
            } else if (item instanceof GroceryItem) {
                GroceryItem groceryItem = (GroceryItem) item;
                name = groceryItem.getName();
                price = groceryItem.getPrice();
                category = groceryItem.getCategory();
                stock = groceryItem.getStockQuantity();
                description = groceryItem.getDescription();
                imageUrl = groceryItem.getImage();
                isFood = false;
            }

            nameLabel.setText(name);
            priceLabel.setText(String.format("৳%.2f", price));
            categoryLabel.setText("Category: " + category);
            stockLabel.setText("Stock: " + stock);
            descriptionLabel.setText(description != null ? description : "No description available");

            // Use ImageCache to load image efficiently 
            // Request image at higher resolution for the details view
            itemImageView.setFitWidth(300);
            itemImageView.setFitHeight(200);
            ImageCache.getImage(imageUrl, isFood, 300, 200, image -> itemImageView.setImage(image));
        }
    }

    @FXML
    private void handleAddToCart() {
        if (parentController != null) {
            if (parentController instanceof CanteenViewController && item instanceof FoodItem) {
                ((CanteenViewController) parentController).addToCart((FoodItem) item);
            } else if (parentController instanceof GroceryViewController && item instanceof GroceryItem) {
                ((GroceryViewController) parentController).addToCart((GroceryItem) item);
            }
        }
        closeWindow();
    }

    @FXML
    private void handleClose() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) detailsContainer.getScene().getWindow();
        stage.close();
    }
} 
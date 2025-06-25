package com.example.uiuonepoint.model;

import javafx.beans.property.*;

public class FoodItem {
    private final IntegerProperty id;
    private final StringProperty name;
    private final DoubleProperty price;
    private final StringProperty description;
    private final StringProperty category;
    private final BooleanProperty available;
    private final StringProperty image;
    private final IntegerProperty stockQuantity;
    private final StringProperty imagePath;

    public FoodItem() {
        this.id = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();
        this.price = new SimpleDoubleProperty();
        this.description = new SimpleStringProperty();
        this.category = new SimpleStringProperty();
        this.available = new SimpleBooleanProperty();
        this.image = new SimpleStringProperty();
        this.stockQuantity = new SimpleIntegerProperty();
        this.imagePath = new SimpleStringProperty();
    }

    public FoodItem(int id, String name, double price, String description, String category, boolean available, String image, int stockQuantity) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.description = new SimpleStringProperty(description);
        this.category = new SimpleStringProperty(category);
        this.available = new SimpleBooleanProperty(available);
        this.image = new SimpleStringProperty(image);
        this.stockQuantity = new SimpleIntegerProperty(stockQuantity);
        this.imagePath = new SimpleStringProperty();
    }

    // Property getters
    public IntegerProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public DoubleProperty priceProperty() { return price; }
    public StringProperty descriptionProperty() { return description; }
    public StringProperty categoryProperty() { return category; }
    public BooleanProperty availableProperty() { return available; }
    public StringProperty imageProperty() { return image; }
    public IntegerProperty stockQuantityProperty() { return stockQuantity; }
    public StringProperty imagePathProperty() { return imagePath; }

    // Value getters and setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }

    public double getPrice() { return price.get(); }
    public void setPrice(double price) { this.price.set(price); }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }

    public String getCategory() { return category.get(); }
    public void setCategory(String category) { this.category.set(category); }

    public boolean isAvailable() { return available.get(); }
    public void setAvailable(boolean available) { this.available.set(available); }

    public String getImage() { return image.get(); }
    public void setImage(String image) { this.image.set(image); }

    public int getStockQuantity() { return stockQuantity.get(); }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity.set(stockQuantity); }

    public String getImagePath() {
        return imagePath.get();
    }

    public void setImagePath(String imagePath) {
        this.imagePath.set(imagePath);
    }
} 
package com.example.uiuonepoint.model;

public class GroceryItem {
    private int id;
    private String name;
    private double price;
    private String category;
    private int stockQuantity;
    private String image;
    private String description;

    public GroceryItem() {}

    public GroceryItem(int id, String name, double price, String category, int stockQuantity, String image) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.image = image;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isLowStock() {
        return stockQuantity < 10;
    }
} 
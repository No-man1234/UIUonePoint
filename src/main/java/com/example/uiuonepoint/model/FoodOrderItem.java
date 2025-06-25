package com.example.uiuonepoint.model;

public class FoodOrderItem {
    private int id;
    private int orderId;
    private int foodItemId;
    private String foodItemName;
    private int quantity;
    private double unitPrice;

    public FoodOrderItem() {}

    public FoodOrderItem(int id, int orderId, int foodItemId, String foodItemName, int quantity, double unitPrice) {
        this.id = id;
        this.orderId = orderId;
        this.foodItemId = foodItemId;
        this.foodItemName = foodItemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getFoodItemId() { return foodItemId; }
    public void setFoodItemId(int foodItemId) { this.foodItemId = foodItemId; }

    public String getFoodItemName() { return foodItemName; }
    public void setFoodItemName(String foodItemName) { this.foodItemName = foodItemName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
} 
package com.example.uiuonepoint.model;

public class GroceryOrderItem {
    private int id;
    private int orderId;
    private int groceryItemId;
    private String groceryItemName;
    private int quantity;
    private double unitPrice;

    public GroceryOrderItem() {}

    public GroceryOrderItem(int id, int orderId, int groceryItemId, String groceryItemName, int quantity, double unitPrice) {
        this.id = id;
        this.orderId = orderId;
        this.groceryItemId = groceryItemId;
        this.groceryItemName = groceryItemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getGroceryItemId() { return groceryItemId; }
    public void setGroceryItemId(int groceryItemId) { this.groceryItemId = groceryItemId; }

    public String getGroceryItemName() { return groceryItemName; }
    public void setGroceryItemName(String groceryItemName) { this.groceryItemName = groceryItemName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getSubtotal() {
        return quantity * unitPrice;
    }

    @Override
    public String toString() {
        return "GroceryOrderItem{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", groceryItemId=" + groceryItemId +
                ", groceryItemName='" + groceryItemName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                '}';
    }
} 
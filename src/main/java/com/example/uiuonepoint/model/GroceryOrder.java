package com.example.uiuonepoint.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GroceryOrder {
    private int id;
    private int userId;
    private String status;
    private Timestamp orderTime;
    private List<GroceryOrderItem> items;

    public GroceryOrder() {
        this.items = new ArrayList<>();
    }

    public GroceryOrder(int id, int userId, String status, Timestamp orderTime) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.orderTime = orderTime;
        this.items = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getOrderTime() { return orderTime; }
    public void setOrderTime(Timestamp orderTime) { this.orderTime = orderTime; }

    public List<GroceryOrderItem> getItems() { return items; }
    public void setItems(List<GroceryOrderItem> items) { this.items = items; }

    public double getTotal() {
        return items.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();
    }
} 
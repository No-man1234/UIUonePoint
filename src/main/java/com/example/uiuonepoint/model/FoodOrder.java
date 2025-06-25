package com.example.uiuonepoint.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FoodOrder {
    private int id;
    private int userId;
    private Timestamp orderTime;
    private String status;
    private List<FoodOrderItem> items;

    public FoodOrder() {
        this.items = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Timestamp getOrderTime() { return orderTime; }
    public void setOrderTime(Timestamp orderTime) { this.orderTime = orderTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<FoodOrderItem> getItems() { return items; }
    public void setItems(List<FoodOrderItem> items) { this.items = items; }

    public double getTotal() {
        return items.stream()
            .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
            .sum();
    }
} 
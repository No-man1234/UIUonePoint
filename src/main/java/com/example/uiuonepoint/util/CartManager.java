package com.example.uiuonepoint.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CartManager {
    private static CartManager instance;
    private ObservableList<Object> canteenCartItems;
    private ObservableList<Object> groceryCartItems;
    private double canteenTotalAmount;
    private double groceryTotalAmount;

    private CartManager() {
        canteenCartItems = FXCollections.observableArrayList();
        groceryCartItems = FXCollections.observableArrayList();
        canteenTotalAmount = 0.0;
        groceryTotalAmount = 0.0;
    }

    public static CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public ObservableList<Object> getCanteenCartItems() {
        return canteenCartItems;
    }

    public ObservableList<Object> getGroceryCartItems() {
        return groceryCartItems;
    }

    public void setCanteenCartItems(ObservableList<Object> items) {
        this.canteenCartItems = items;
    }

    public void setGroceryCartItems(ObservableList<Object> items) {
        this.groceryCartItems = items;
    }

    public double getCanteenTotalAmount() {
        return canteenTotalAmount;
    }

    public double getGroceryTotalAmount() {
        return groceryTotalAmount;
    }

    public void setCanteenTotalAmount(double amount) {
        this.canteenTotalAmount = amount;
    }

    public void setGroceryTotalAmount(double amount) {
        this.groceryTotalAmount = amount;
    }

    public void clearCanteenCart() {
        canteenCartItems.clear();
        canteenTotalAmount = 0.0;
    }

    public void clearGroceryCart() {
        groceryCartItems.clear();
        groceryTotalAmount = 0.0;
    }
} 
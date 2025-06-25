package com.example.uiuonepoint.dao;

import com.example.uiuonepoint.model.GroceryItem;
import com.example.uiuonepoint.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroceryItemDAO implements DAO<GroceryItem> {
    
    @Override
    public Optional<GroceryItem> get(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM grocery_items WHERE id = ?")) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                GroceryItem item = new GroceryItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getString("category"),
                    rs.getInt("stock_quantity"),
                    rs.getString("image")
                );
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<GroceryItem> getAll() throws SQLException {
        List<GroceryItem> items = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM grocery_items")) {
            
            while (rs.next()) {
                GroceryItem item = new GroceryItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getString("category"),
                    rs.getInt("stock_quantity"),
                    rs.getString("image")
                );
                items.add(item);
            }
        }
        return items;
    }

    @Override
    public int save(GroceryItem item) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO grocery_items (name, price, category, stock_quantity, image) VALUES (?, ?, ?, ?, ?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, item.getName());
            stmt.setDouble(2, item.getPrice());
            stmt.setString(3, item.getCategory());
            stmt.setInt(4, item.getStockQuantity());
            stmt.setString(5, item.getImage());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    item.setId(id);
                    return id;
                }
                throw new SQLException("Failed to get generated ID after saving grocery item");
            }
        }
    }

    @Override
    public void update(GroceryItem item) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE grocery_items SET name = ?, price = ?, category = ?, stock_quantity = ?, image = ? WHERE id = ?")) {
            
            stmt.setString(1, item.getName());
            stmt.setDouble(2, item.getPrice());
            stmt.setString(3, item.getCategory());
            stmt.setInt(4, item.getStockQuantity());
            stmt.setString(5, item.getImage());
            stmt.setInt(6, item.getId());
            
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM grocery_items WHERE id = ?")) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<GroceryItem> getByCategory(String category) throws SQLException {
        List<GroceryItem> items = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM grocery_items WHERE category = ?")) {
            
            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                GroceryItem item = new GroceryItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getString("category"),
                    rs.getInt("stock_quantity"),
                    rs.getString("image")
                );
                items.add(item);
            }
        }
        return items;
    }

    public List<GroceryItem> getLowStockItems() {
        List<GroceryItem> groceryItems = new ArrayList<>();
        String query = "SELECT * FROM grocery_items WHERE stock_quantity < 10";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                GroceryItem item = new GroceryItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getString("category"),
                    rs.getInt("stock_quantity"),
                    rs.getString("image")
                );
                groceryItems.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return groceryItems;
    }

    public boolean updateStock(int id, int quantity) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE grocery_items SET stock_quantity = ? WHERE id = ?")) {
            
            stmt.setInt(1, quantity);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        }
    }
} 
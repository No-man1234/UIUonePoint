package com.example.uiuonepoint.dao;

import com.example.uiuonepoint.model.FoodItem;
import com.example.uiuonepoint.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FoodItemDAO implements DAO<FoodItem> {
    
    @Override
    public Optional<FoodItem> get(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM food_items WHERE id = ?")) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                FoodItem item = new FoodItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getBoolean("available"),
                    rs.getString("image"),
                    rs.getInt("stock_quantity")
                );
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<FoodItem> getAll() throws SQLException {
        List<FoodItem> foodItems = new ArrayList<>();
        String query = "SELECT * FROM food_items";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            System.out.println("Executing query: " + query);
            int count = 0;
            
            while (rs.next()) {
                count++;
                System.out.println("Reading food item #" + count + ":");
                System.out.println("  ID: " + rs.getInt("id"));
                System.out.println("  Name: " + rs.getString("name"));
                System.out.println("  Price: " + rs.getDouble("price"));
                System.out.println("  Category: " + rs.getString("category"));
                System.out.println("  Available: " + rs.getBoolean("available"));
                System.out.println("  Stock: " + rs.getInt("stock_quantity"));
                
                FoodItem item = new FoodItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getBoolean("available"),
                    rs.getString("image"),
                    rs.getInt("stock_quantity")
                );
                foodItems.add(item);
            }
            
            System.out.println("Total food items found: " + count);
            
            if (count == 0) {
                System.out.println("No food items found in the database!");
            }
            
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
        }
        return foodItems;
    }

    @Override
    public int save(FoodItem item) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO food_items (name, price, description, category, available, image, stock_quantity) VALUES (?, ?, ?, ?, ?, ?, ?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, item.getName());
            stmt.setDouble(2, item.getPrice());
            stmt.setString(3, item.getDescription());
            stmt.setString(4, item.getCategory());
            stmt.setBoolean(5, item.isAvailable());
            stmt.setString(6, item.getImage());
            stmt.setInt(7, item.getStockQuantity());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    item.setId(id);
                    return id;
                }
                throw new SQLException("Failed to get generated ID after saving food item");
            }
        }
    }

    @Override
    public void update(FoodItem item) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE food_items SET name = ?, price = ?, description = ?, category = ?, available = ?, image = ?, stock_quantity = ? WHERE id = ?")) {
            
            stmt.setString(1, item.getName());
            stmt.setDouble(2, item.getPrice());
            stmt.setString(3, item.getDescription());
            stmt.setString(4, item.getCategory());
            stmt.setBoolean(5, item.isAvailable());
            stmt.setString(6, item.getImage());
            stmt.setInt(7, item.getStockQuantity());
            stmt.setInt(8, item.getId());
            
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM food_items WHERE id = ?")) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<FoodItem> getByCategory(String category) throws SQLException {
        List<FoodItem> foodItems = new ArrayList<>();
        String query = "SELECT * FROM food_items WHERE category = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, category);
            System.out.println("Executing query for category '" + category + "': " + query);
            
            ResultSet rs = pstmt.executeQuery();
            int count = 0;
            
            while (rs.next()) {
                count++;
                System.out.println("Reading food item #" + count + " in category " + category + ":");
                System.out.println("  ID: " + rs.getInt("id"));
                System.out.println("  Name: " + rs.getString("name"));
                System.out.println("  Price: " + rs.getDouble("price"));
                System.out.println("  Available: " + rs.getBoolean("available"));
                System.out.println("  Stock: " + rs.getInt("stock_quantity"));
                
                FoodItem item = new FoodItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getBoolean("available"),
                    rs.getString("image"),
                    rs.getInt("stock_quantity")
                );
                foodItems.add(item);
            }
            
            System.out.println("Total food items found in category '" + category + "': " + count);
            
        } catch (SQLException e) {
            System.err.println("Error executing category query: " + e.getMessage());
            e.printStackTrace();
        }
        return foodItems;
    }

    public List<FoodItem> getAvailableItems() throws SQLException {
        List<FoodItem> foodItems = new ArrayList<>();
        String query = "SELECT * FROM food_items WHERE available = true";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                FoodItem item = new FoodItem(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getBoolean("available"),
                    rs.getString("image"),
                    rs.getInt("stock_quantity")
                );
                foodItems.add(item);
            }
        }
        return foodItems;
    }

    public boolean updateStock(int id, int quantity) throws SQLException {
        String query = "UPDATE food_items SET available = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setBoolean(1, quantity > 0);
            pstmt.setInt(2, id);
            
            return pstmt.executeUpdate() > 0;
        }
    }
} 
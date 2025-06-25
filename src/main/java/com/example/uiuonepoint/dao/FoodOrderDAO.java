package com.example.uiuonepoint.dao;

import com.example.uiuonepoint.model.FoodOrder;
import com.example.uiuonepoint.model.FoodOrderItem;
import com.example.uiuonepoint.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FoodOrderDAO implements DAO<FoodOrder> {
    
    @Override
    public Optional<FoodOrder> get(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM food_orders WHERE id = ?")) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                FoodOrder order = new FoodOrder();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setOrderTime(rs.getTimestamp("order_time"));
                order.setStatus(rs.getString("status"));
                order.setItems(getFoodOrderItems(conn, order.getId()));
                return Optional.of(order);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<FoodOrder> getAll() throws SQLException {
        List<FoodOrder> orders = new ArrayList<>();
        System.out.println("\n[FoodOrderDAO] Getting all food orders...");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("[FoodOrderDAO] Connected to database");
            
            // First, get all orders
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM food_orders")) {
                
                System.out.println("[FoodOrderDAO] Executing query: SELECT * FROM food_orders");
                int count = 0;
                
                while (rs.next()) {
                    count++;
                    int orderId = rs.getInt("id");
                    System.out.println("\n[FoodOrderDAO] Processing order #" + count + ":");
                    System.out.println("[FoodOrderDAO] Order ID: " + orderId);
                    System.out.println("[FoodOrderDAO] User ID: " + rs.getInt("user_id"));
                    System.out.println("[FoodOrderDAO] Status: " + rs.getString("status"));
                    
                    FoodOrder order = new FoodOrder();
                    order.setId(orderId);
                    order.setUserId(rs.getInt("user_id"));
                    order.setOrderTime(rs.getTimestamp("order_time"));
                    order.setStatus(rs.getString("status"));
                    
                    // Get order items
                    List<FoodOrderItem> items = getFoodOrderItems(conn, orderId);
                    System.out.println("[FoodOrderDAO] Found " + items.size() + " items for order " + orderId);
                    order.setItems(items);
                    
                    orders.add(order);
                }
                
                System.out.println("\n[FoodOrderDAO] Total orders found: " + count);
                if (count == 0) {
                    System.out.println("[FoodOrderDAO] No orders found in the database!");
                }
            }
        } catch (SQLException e) {
            System.err.println("[FoodOrderDAO] Error getting all orders: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return orders;
    }

    @Override
    public int save(FoodOrder order) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);  // Start transaction

            // Calculate total amount
            double totalAmount = order.getItems().stream()
                    .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                    .sum();

            // Insert order
            try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO food_orders (user_id, status, order_time, total_amount) VALUES (?, 'pending', CURRENT_TIMESTAMP, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
                
                stmt.setInt(1, order.getUserId());
                stmt.setDouble(2, totalAmount);
                stmt.executeUpdate();

                // Get generated order ID
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        order.setId(generatedKeys.getInt(1));
                    } else {
                        throw new SQLException("Failed to get generated order ID");
                    }
                }
            }

            // Insert order items and update stock
            for (FoodOrderItem item : order.getItems()) {
                // Check stock availability first
                try (PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT stock_quantity FROM food_items WHERE id = ? FOR UPDATE")) {
                    
                    checkStmt.setInt(1, item.getFoodItemId());
                    ResultSet rs = checkStmt.executeQuery();
                    
                    if (rs.next()) {
                        int currentStock = rs.getInt("stock_quantity");
                        if (currentStock < item.getQuantity()) {
                            throw new SQLException("Insufficient stock for food item ID: " + item.getFoodItemId());
                        }
                    } else {
                        throw new SQLException("Food item not found with ID: " + item.getFoodItemId());
                    }
                }

                // Insert order item
                try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO food_order_items (order_id, food_item_id, quantity, unit_price) VALUES (?, ?, ?, ?)")) {
                    
                    stmt.setInt(1, order.getId());
                    stmt.setInt(2, item.getFoodItemId());
                    stmt.setInt(3, item.getQuantity());
                    stmt.setDouble(4, item.getUnitPrice());
                    stmt.executeUpdate();
                }

                // Update stock quantity
                try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE food_items SET stock_quantity = stock_quantity - ? WHERE id = ?")) {
                    
                    stmt.setInt(1, item.getQuantity());
                    stmt.setInt(2, item.getFoodItemId());
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new SQLException("Failed to update stock for food item ID: " + item.getFoodItemId());
                    }
                }
            }

            conn.commit();  // Commit transaction
            return order.getId();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();  // Rollback on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;  // Re-throw the exception
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void update(FoodOrder order) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE food_orders SET status = ? WHERE id = ?")) {
            
            stmt.setString(1, order.getStatus());
            stmt.setInt(2, order.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM food_orders WHERE id = ?")) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<FoodOrder> getByUserId(int userId) throws SQLException {
        List<FoodOrder> orders = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM food_orders WHERE user_id = ?")) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                FoodOrder order = new FoodOrder();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setOrderTime(rs.getTimestamp("order_time"));
                order.setStatus(rs.getString("status"));
                order.setItems(getFoodOrderItems(conn, order.getId()));
                orders.add(order);
            }
        }
        return orders;
    }

    public List<FoodOrder> getPendingOrdersForCancellation() throws SQLException {
        List<FoodOrder> orders = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM food_orders WHERE status = 'pending'")) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                FoodOrder order = new FoodOrder();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setOrderTime(rs.getTimestamp("order_time"));
                order.setStatus(rs.getString("status"));
                orders.add(order);
            }
        }
        return orders;
    }

    private List<FoodOrderItem> getFoodOrderItems(Connection conn, int orderId) throws SQLException {
        List<FoodOrderItem> items = new ArrayList<>();
        String query = "SELECT i.*, f.name as item_name FROM food_order_items i " +
                      "JOIN food_items f ON i.food_item_id = f.id " +
                      "WHERE i.order_id = ?";
                      
        System.out.println("[FoodOrderDAO] Getting items for order " + orderId);
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            System.out.println("[FoodOrderDAO] Executing query: " + query.replace("?", String.valueOf(orderId)));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    FoodOrderItem item = new FoodOrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setFoodItemId(rs.getInt("food_item_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setFoodItemName(rs.getString("item_name"));
                    items.add(item);
                    
                    System.out.println("[FoodOrderDAO] Found item: " + item.getFoodItemName() + 
                                     " (Quantity: " + item.getQuantity() + ")");
                }
            }
        }
        return items;
    }

    public int getPendingOrdersCount() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM food_orders WHERE status = 'Pending'")) {
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public double getTodaySales() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT SUM(total_amount) FROM food_orders " +
                 "WHERE DATE(order_time) = CURRENT_DATE AND status = 'completed'")) {
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public double getAllTimeSales() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT SUM(total_amount) FROM food_orders WHERE status = 'completed'")) {
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    public int getItemOrderCount(int itemId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT SUM(quantity) FROM food_order_items WHERE food_item_id = ?")) {
            
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
} 
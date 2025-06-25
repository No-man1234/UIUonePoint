package com.example.uiuonepoint.dao;

import com.example.uiuonepoint.model.GroceryOrder;
import com.example.uiuonepoint.model.GroceryOrderItem;
import com.example.uiuonepoint.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroceryOrderDAO implements DAO<GroceryOrder> {
    
    public GroceryOrderDAO() {
    }

    @Override
    public Optional<GroceryOrder> get(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM grocery_orders WHERE id = ?")) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                GroceryOrder order = new GroceryOrder();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setOrderTime(rs.getTimestamp("order_time"));
                order.setStatus(rs.getString("status"));
                order.setItems(getGroceryOrderItems(conn, order.getId()));
                return Optional.of(order);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<GroceryOrder> getAll() throws SQLException {
        List<GroceryOrder> orders = new ArrayList<>();
        System.out.println("\n[GroceryOrderDAO] Getting all grocery orders...");
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("[GroceryOrderDAO] Connected to database");
            
            // First, get all orders
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM grocery_orders")) {
                
                System.out.println("[GroceryOrderDAO] Executing query: SELECT * FROM grocery_orders");
                int count = 0;
                
                while (rs.next()) {
                    count++;
                    int orderId = rs.getInt("id");
                    System.out.println("\n[GroceryOrderDAO] Processing order #" + count + ":");
                    System.out.println("[GroceryOrderDAO] Order ID: " + orderId);
                    System.out.println("[GroceryOrderDAO] User ID: " + rs.getInt("user_id"));
                    System.out.println("[GroceryOrderDAO] Status: " + rs.getString("status"));
                    
                    GroceryOrder order = new GroceryOrder();
                    order.setId(orderId);
                    order.setUserId(rs.getInt("user_id"));
                    order.setOrderTime(rs.getTimestamp("order_time"));
                    order.setStatus(rs.getString("status"));
                    
                    // Get order items
                    List<GroceryOrderItem> items = getGroceryOrderItems(conn, orderId);
                    System.out.println("[GroceryOrderDAO] Found " + items.size() + " items for order " + orderId);
                    order.setItems(items);
                    
                    orders.add(order);
                }
                
                System.out.println("\n[GroceryOrderDAO] Total orders found: " + count);
                if (count == 0) {
                    System.out.println("[GroceryOrderDAO] No orders found in the database!");
                }
            }
        } catch (SQLException e) {
            System.err.println("[GroceryOrderDAO] Error getting all orders: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return orders;
    }

    public List<GroceryOrder> getByUserId(int userId) throws SQLException {
        List<GroceryOrder> orders = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM grocery_orders WHERE user_id = ?")) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                GroceryOrder order = new GroceryOrder();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setOrderTime(rs.getTimestamp("order_time"));
                order.setStatus(rs.getString("status"));
                order.setItems(getGroceryOrderItems(conn, order.getId()));
                orders.add(order);
            }
        }
        return orders;
    }

    @Override
    public int save(GroceryOrder order) throws SQLException {
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
                "INSERT INTO grocery_orders (user_id, status, order_time, total_amount) VALUES (?, 'pending', CURRENT_TIMESTAMP, ?)",
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
            for (GroceryOrderItem item : order.getItems()) {
                // Check stock availability first
                try (PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT stock_quantity FROM grocery_items WHERE id = ? FOR UPDATE")) {
                    
                    checkStmt.setInt(1, item.getGroceryItemId());
                    ResultSet rs = checkStmt.executeQuery();
                    
                    if (rs.next()) {
                        int currentStock = rs.getInt("stock_quantity");
                        if (currentStock < item.getQuantity()) {
                            throw new SQLException("Insufficient stock for grocery item ID: " + item.getGroceryItemId());
                        }
                    } else {
                        throw new SQLException("Grocery item not found with ID: " + item.getGroceryItemId());
                    }
                }

                // Insert order item
                try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO grocery_order_items (order_id, grocery_item_id, quantity, unit_price) VALUES (?, ?, ?, ?)")) {
                    
                    stmt.setInt(1, order.getId());
                    stmt.setInt(2, item.getGroceryItemId());
                    stmt.setInt(3, item.getQuantity());
                    stmt.setDouble(4, item.getUnitPrice());
                    stmt.executeUpdate();
                }

                // Update stock quantity
                try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE grocery_items SET stock_quantity = stock_quantity - ? WHERE id = ?")) {
                    
                    stmt.setInt(1, item.getQuantity());
                    stmt.setInt(2, item.getGroceryItemId());
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected == 0) {
                        throw new SQLException("Failed to update stock for grocery item ID: " + item.getGroceryItemId());
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
    public void update(GroceryOrder order) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE grocery_orders SET status = ? WHERE id = ?")) {
            
            stmt.setString(1, order.getStatus());
            stmt.setInt(2, order.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM grocery_orders WHERE id = ?")) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<GroceryOrder> getPendingOrdersForCancellation() throws SQLException {
        List<GroceryOrder> orders = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM grocery_orders WHERE status = 'pending'")) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                GroceryOrder order = new GroceryOrder();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setOrderTime(rs.getTimestamp("order_time"));
                order.setStatus(rs.getString("status"));
                orders.add(order);
            }
        }
        return orders;
    }

    private List<GroceryOrderItem> getGroceryOrderItems(Connection conn, int orderId) throws SQLException {
        List<GroceryOrderItem> items = new ArrayList<>();
        String query = "SELECT i.*, g.name as item_name FROM grocery_order_items i " +
                      "JOIN grocery_items g ON i.grocery_item_id = g.id " +
                      "WHERE i.order_id = ?";
                      
        System.out.println("[GroceryOrderDAO] Getting items for order " + orderId);
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            System.out.println("[GroceryOrderDAO] Executing query: " + query.replace("?", String.valueOf(orderId)));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    GroceryOrderItem item = new GroceryOrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setGroceryItemId(rs.getInt("grocery_item_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setGroceryItemName(rs.getString("item_name"));
                    items.add(item);
                    
                    System.out.println("[GroceryOrderDAO] Found item: " + item.getGroceryItemName() + 
                                     " (Quantity: " + item.getQuantity() + ")");
                }
            }
        }
        return items;
    }

    public int getPendingOrdersCount() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM grocery_orders WHERE status = 'Pending'")) {
            
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
                 "SELECT SUM(total_amount) FROM grocery_orders " +
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
                 "SELECT SUM(total_amount) FROM grocery_orders WHERE status = 'completed'")) {
            
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
                 "SELECT SUM(quantity) FROM grocery_order_items WHERE grocery_item_id = ?")) {
            
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
} 
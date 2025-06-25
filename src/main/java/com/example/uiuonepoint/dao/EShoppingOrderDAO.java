package com.example.uiuonepoint.dao;

import com.example.uiuonepoint.model.EShoppingOrder;
import com.example.uiuonepoint.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EShoppingOrderDAO {
    private final Connection connection;

    public EShoppingOrderDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public void save(EShoppingOrder order) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);  // Start transaction

            // Check stock availability first
            try (PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT stock_quantity FROM eshopping_products WHERE id = ? FOR UPDATE")) {
                
                checkStmt.setInt(1, order.getProductId());
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    int currentStock = rs.getInt("stock_quantity");
                    if (currentStock < order.getQuantity()) {
                        throw new SQLException("Insufficient stock for product ID: " + order.getProductId());
                    }
                } else {
                    throw new SQLException("Product not found with ID: " + order.getProductId());
                }
            }

            // Insert order
            String sql = "INSERT INTO eshopping_orders (user_id, product_id, quantity, mobile_number, status) " +
                        "VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, order.getUserId());
                stmt.setInt(2, order.getProductId());
                stmt.setInt(3, order.getQuantity());
                stmt.setString(4, order.getMobileNumber());
                stmt.setString(5, order.getStatus());
                
                stmt.executeUpdate();
                
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        order.setId(rs.getInt(1));
                    }
                }
            }

            // Update stock quantity
            try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE eshopping_products SET stock_quantity = stock_quantity - ? WHERE id = ?")) {
                
                stmt.setInt(1, order.getQuantity());
                stmt.setInt(2, order.getProductId());
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("Failed to update stock for product ID: " + order.getProductId());
                }
            }

            conn.commit();  // Commit transaction
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

    public void updateStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE eshopping_orders SET status = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        }
    }

    public List<EShoppingOrder> getByUserId(int userId) throws SQLException {
        List<EShoppingOrder> orders = new ArrayList<>();
        String sql = "SELECT o.*, p.name as product_name, p.price as product_price, u.username as seller_name " +
                    "FROM eshopping_orders o " +
                    "JOIN eshopping_products p ON o.product_id = p.id " +
                    "JOIN users u ON p.seller_id = u.id " +
                    "WHERE o.user_id = ? " +
                    "ORDER BY o.created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        }
        
        return orders;
    }

    public List<EShoppingOrder> getBySellerId(int sellerId) throws SQLException {
        List<EShoppingOrder> orders = new ArrayList<>();
        String sql = "SELECT o.*, p.name as product_name, p.price as product_price, u.username as seller_name " +
                    "FROM eshopping_orders o " +
                    "JOIN eshopping_products p ON o.product_id = p.id " +
                    "JOIN users u ON p.seller_id = u.id " +
                    "WHERE p.seller_id = ? " +
                    "ORDER BY o.created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sellerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        }
        
        return orders;
    }

    public Optional<EShoppingOrder> getById(int id) throws SQLException {
        String sql = "SELECT o.*, p.name as product_name, p.price as product_price, u.username as seller_name " +
                    "FROM eshopping_orders o " +
                    "JOIN eshopping_products p ON o.product_id = p.id " +
                    "JOIN users u ON p.seller_id = u.id " +
                    "WHERE o.id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToOrder(rs));
                }
            }
        }
        
        return Optional.empty();
    }

    public int getPendingOrdersCount(int sellerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM eshopping_orders o " +
                    "JOIN eshopping_products p ON o.product_id = p.id " +
                    "WHERE p.seller_id = ? AND o.status = 'pending'";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sellerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }

    public double getTodaySales(int sellerId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(o.quantity * p.price), 0) as total_sales " +
                    "FROM eshopping_orders o " +
                    "JOIN eshopping_products p ON o.product_id = p.id " +
                    "WHERE p.seller_id = ? " +
                    "AND DATE(o.created_at) = CURRENT_DATE " +
                    "AND o.status = 'delivered'";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sellerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_sales");
                }
            }
        }
        
        return 0.0;
    }

    private EShoppingOrder mapResultSetToOrder(ResultSet rs) throws SQLException {
        EShoppingOrder order = new EShoppingOrder();
        order.setId(rs.getInt("id"));
        order.setUserId(rs.getInt("user_id"));
        order.setProductId(rs.getInt("product_id"));
        order.setQuantity(rs.getInt("quantity"));
        order.setMobileNumber(rs.getString("mobile_number"));
        order.setStatus(rs.getString("status"));
        order.setCreatedAt(rs.getString("created_at"));
        order.setUpdatedAt(rs.getString("updated_at"));
        
        // Additional fields
        order.setProductName(rs.getString("product_name"));
        order.setProductPrice(rs.getDouble("product_price"));
        order.setSellerName(rs.getString("seller_name"));
        
        return order;
    }
} 
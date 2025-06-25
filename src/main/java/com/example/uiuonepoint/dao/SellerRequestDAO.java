package com.example.uiuonepoint.dao;

import com.example.uiuonepoint.model.SellerRequest;
import com.example.uiuonepoint.model.User;
import com.example.uiuonepoint.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SellerRequestDAO implements DAO<SellerRequest> {
    
    @Override
    public Optional<SellerRequest> get(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT sr.*, u.username FROM seller_requests sr " +
                 "JOIN users u ON sr.user_id = u.id " +
                 "WHERE sr.id = ?")) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                SellerRequest request = new SellerRequest(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("status"),
                    rs.getTimestamp("request_date"),
                    rs.getString("reason")
                );
                return Optional.of(request);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<SellerRequest> getAll() throws SQLException {
        List<SellerRequest> requests = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT sr.*, u.username FROM seller_requests sr " +
                 "JOIN users u ON sr.user_id = u.id")) {
            
            while (rs.next()) {
                SellerRequest request = new SellerRequest(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("status"),
                    rs.getTimestamp("request_date"),
                    rs.getString("reason")
                );
                requests.add(request);
            }
        }
        return requests;
    }

    @Override
    public int save(SellerRequest request) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO seller_requests (user_id, status, request_date, reason) " +
                 "VALUES (?, ?, CURRENT_TIMESTAMP, ?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, request.getUserId());
            stmt.setString(2, request.getStatus());
            stmt.setString(3, request.getReason());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    request.setId(id);
                    return id;
                }
                throw new SQLException("Failed to get generated ID after saving seller request");
            }
        }
    }

    @Override
    public void update(SellerRequest request) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE seller_requests SET status = ? WHERE id = ?")) {
            
            stmt.setString(1, request.getStatus());
            stmt.setInt(2, request.getId());
            
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM seller_requests WHERE id = ?")) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<SellerRequest> getPendingRequests() throws SQLException {
        List<SellerRequest> requests = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT sr.*, u.username FROM seller_requests sr " +
                 "JOIN users u ON sr.user_id = u.id " +
                 "WHERE sr.status = 'pending'")) {
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                SellerRequest request = new SellerRequest(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("status"),
                    rs.getTimestamp("request_date"),
                    rs.getString("reason")
                );
                requests.add(request);
            }
        }
        return requests;
    }

    public Optional<SellerRequest> getByUserId(int userId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT sr.*, u.username FROM seller_requests sr " +
                 "JOIN users u ON sr.user_id = u.id " +
                 "WHERE sr.user_id = ?")) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                SellerRequest request = new SellerRequest(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("status"),
                    rs.getTimestamp("request_date"),
                    rs.getString("reason")
                );
                return Optional.of(request);
            }
        }
        return Optional.empty();
    }

    public boolean approveRequest(int requestId, int userId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Update request status
            try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE seller_requests SET status = 'approved' WHERE id = ?")) {
                stmt.setInt(1, requestId);
                stmt.executeUpdate();
            }

            // Update user role
            try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE users SET role = 'SELLER' WHERE id = ?")) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
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

    public boolean rejectRequest(int requestId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE seller_requests SET status = 'rejected' WHERE id = ?")) {
            
            stmt.setInt(1, requestId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
} 
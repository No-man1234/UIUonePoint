package com.example.uiuonepoint.dao;

import com.example.uiuonepoint.model.EShoppingProduct;
import com.example.uiuonepoint.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EShoppingProductDAO {
    private final Connection connection;

    public EShoppingProductDAO() {
        this.connection = DatabaseConnection.getConnection();
    }

    public void save(EShoppingProduct product) throws SQLException {
        String sql = "INSERT INTO eshopping_products (seller_id, name, description, price, stock_quantity, category, image_path) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, product.getSellerId());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getDescription());
            stmt.setDouble(4, product.getPrice());
            stmt.setInt(5, product.getStockQuantity());
            stmt.setString(6, product.getCategory());
            stmt.setString(7, product.getImagePath());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    product.setId(rs.getInt(1));
                }
            }
        }
    }

    public void update(EShoppingProduct product) throws SQLException {
        // First verify that the product belongs to the seller
        String verifySql = "SELECT seller_id FROM eshopping_products WHERE id = ?";
        try (PreparedStatement verifyStmt = connection.prepareStatement(verifySql)) {
            verifyStmt.setInt(1, product.getId());
            try (ResultSet rs = verifyStmt.executeQuery()) {
                if (!rs.next() || rs.getInt("seller_id") != product.getSellerId()) {
                    throw new SQLException("Product does not belong to this seller");
                }
            }
        }

        String sql = "UPDATE eshopping_products SET name = ?, description = ?, price = ?, " +
                    "stock_quantity = ?, category = ?, image_path = ? WHERE id = ? AND seller_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getStockQuantity());
            stmt.setString(5, product.getCategory());
            stmt.setString(6, product.getImagePath());
            stmt.setInt(7, product.getId());
            stmt.setInt(8, product.getSellerId());
            
            stmt.executeUpdate();
        }
    }

    public void delete(int id, int sellerId) throws SQLException {
        // First verify that the product belongs to the seller
        String verifySql = "SELECT seller_id FROM eshopping_products WHERE id = ?";
        try (PreparedStatement verifyStmt = connection.prepareStatement(verifySql)) {
            verifyStmt.setInt(1, id);
            try (ResultSet rs = verifyStmt.executeQuery()) {
                if (!rs.next() || rs.getInt("seller_id") != sellerId) {
                    throw new SQLException("Product does not belong to this seller");
                }
            }
        }

        String sql = "DELETE FROM eshopping_products WHERE id = ? AND seller_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, sellerId);
            stmt.executeUpdate();
        }
    }

    public List<EShoppingProduct> getAll() throws SQLException {
        List<EShoppingProduct> products = new ArrayList<>();
        String sql = "SELECT * FROM eshopping_products ORDER BY created_at DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        
        return products;
    }

    public List<EShoppingProduct> getBySellerId(int sellerId) throws SQLException {
        List<EShoppingProduct> products = new ArrayList<>();
        String sql = "SELECT * FROM eshopping_products WHERE seller_id = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sellerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        
        return products;
    }

    public Optional<EShoppingProduct> getById(int id) throws SQLException {
        String sql = "SELECT * FROM eshopping_products WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProduct(rs));
                }
            }
        }
        
        return Optional.empty();
    }

    public List<EShoppingProduct> getByCategory(String category) throws SQLException {
        List<EShoppingProduct> products = new ArrayList<>();
        String sql = "SELECT * FROM eshopping_products WHERE category = ? ORDER BY created_at DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, category);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        
        return products;
    }

    private EShoppingProduct mapResultSetToProduct(ResultSet rs) throws SQLException {
        EShoppingProduct product = new EShoppingProduct();
        product.setId(rs.getInt("id"));
        product.setSellerId(rs.getInt("seller_id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getDouble("price"));
        product.setStockQuantity(rs.getInt("stock_quantity"));
        product.setCategory(rs.getString("category"));
        product.setImagePath(rs.getString("image_path"));
        product.setCreatedAt(rs.getString("created_at"));
        return product;
    }
} 
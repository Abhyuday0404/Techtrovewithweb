// src/java/managers/ProductManager.java
package managers;

import db.DBUtil;
import models.Product;
import core.IdGenerator; 

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductManager {

    public ProductManager() throws SQLException {
        // Constructor
    }

    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM Products ORDER BY Name"; // ImageURL no longer selected
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    public Product getProductById(String productId) throws SQLException {
        Product product = null;
        String sql = "SELECT * FROM Products WHERE ProductID = ?"; // ImageURL no longer selected
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    product = mapResultSetToProduct(rs);
                }
            }
        }
        return product;
    }

    public List<Product> searchProductsByName(String searchTerm) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM Products WHERE Name LIKE ? ORDER BY Name"; // ImageURL no longer selected
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        java.sql.Date sqlMfgDate = rs.getDate("ManufactureDate");
        return new Product(
            rs.getString("ProductID"),
            rs.getString("Name"),
            rs.getString("Brand"),
            rs.getString("Model"),
            rs.getString("Description"),
            rs.getDouble("Price"),
            rs.getInt("Stock"),
            (sqlMfgDate != null) ? sqlMfgDate.toLocalDate() : null,
            rs.getString("CategoryID")
            // rs.getString("ImageURL") // REMOVED
        );
    }

    public void addProduct(Product product) throws SQLException {
        if (product.getProductId() == null || product.getProductId().trim().isEmpty()) {
            product.setProductId(IdGenerator.generateProductId());
        }
        // SQL and PreparedStatement updated
        String sql = "INSERT INTO Products (ProductID, Name, Brand, Model, Description, Price, Stock, ManufactureDate, CategoryID) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"; // ImageURL column removed
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getProductId()); 
            pstmt.setString(2, product.getName());
            pstmt.setString(3, product.getBrand());
            pstmt.setString(4, product.getModel());
            pstmt.setString(5, product.getDescription());
            pstmt.setDouble(6, product.getPrice());
            pstmt.setInt(7, product.getStock());
            pstmt.setDate(8, product.getManufactureDate() != null ? java.sql.Date.valueOf(product.getManufactureDate()) : null);
            pstmt.setString(9, product.getCategoryId());
            // pstmt.setString(10, product.getImageUrl()); // REMOVED
            pstmt.executeUpdate();
            System.out.println("Product added: " + product.getName() + " (ID: " + product.getProductId() + ")");
        }
    }

    public boolean updateProduct(Product product) throws SQLException {
        if (product.getProductId() == null || product.getProductId().trim().isEmpty()) {
            throw new SQLException("Product ID cannot be null or empty for an update operation.");
        }
        // SQL and PreparedStatement updated
        String sql = "UPDATE Products SET Name = ?, Brand = ?, Model = ?, Description = ?, Price = ?, " +
                     "Stock = ?, ManufactureDate = ?, CategoryID = ? WHERE ProductID = ?"; // ImageURL removed
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getBrand());
            pstmt.setString(3, product.getModel());
            pstmt.setString(4, product.getDescription());
            pstmt.setDouble(5, product.getPrice());
            pstmt.setInt(6, product.getStock());
            pstmt.setDate(7, product.getManufactureDate() != null ? java.sql.Date.valueOf(product.getManufactureDate()) : null);
            pstmt.setString(8, product.getCategoryId());
            pstmt.setString(9, product.getProductId()); // This is the WHERE clause parameter
            // Old index 9 was imageUrl, old index 10 was productId for WHERE
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Product updated: " + product.getName());
            }
            return rowsAffected > 0;
        }
    }

    public boolean deleteProduct(String productId) throws SQLException {
         if (productId == null || productId.trim().isEmpty()) {
            throw new SQLException("Product ID cannot be null or empty for a delete operation.");
        }
        String sql = "DELETE FROM Products WHERE ProductID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Product deleted: " + productId);
            }
            return rowsAffected > 0;
        }
    }
}
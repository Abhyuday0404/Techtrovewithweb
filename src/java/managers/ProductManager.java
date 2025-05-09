// Place in: src/java/managers/ProductManager.java
package managers;

import db.DBUtil;
import models.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductManager {
    public ProductManager() throws SQLException {
        // Constructor can be empty or initialize things if needed
        // Ensure DBUtil can provide a connection when methods are called
    }

    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM Products ORDER BY Name";
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
        String sql = "SELECT * FROM Products WHERE ProductID = ?";
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
        String sql = "SELECT * FROM Products WHERE Name LIKE ? ORDER BY Name";
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


    // Add CRUD methods here from your Part 1: addProduct, updateProduct, deleteProduct
    // public void addProduct(...) throws SQLException { ... }
    // public boolean updateProduct(...) throws SQLException { ... }
    // public boolean deleteProduct(...) throws SQLException { ... }

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
            rs.getString("CategoryID"),
            rs.getString("ImageURL")
        );
    }
    // Add these methods to your existing src/java/managers/ProductManager.java

    public void addProduct(Product product) throws SQLException {
        String sql = "INSERT INTO Products (ProductID, Name, Brand, Model, Description, Price, Stock, ManufactureDate, CategoryID, ImageURL) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            pstmt.setString(10, product.getImageUrl());
            pstmt.executeUpdate();
            System.out.println("Product added: " + product.getName());
        }
    }

    public boolean updateProduct(Product product) throws SQLException {
        String sql = "UPDATE Products SET Name = ?, Brand = ?, Model = ?, Description = ?, Price = ?, " +
                     "Stock = ?, ManufactureDate = ?, CategoryID = ?, ImageURL = ? WHERE ProductID = ?";
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
            pstmt.setString(9, product.getImageUrl());
            pstmt.setString(10, product.getProductId());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) System.out.println("Product updated: " + product.getName());
            return rowsAffected > 0;
        }
    }

    public boolean deleteProduct(String productId) throws SQLException {
        // Consider checking for dependencies (e.g., if product is in active orders/carts
        // where ON DELETE SET NULL isn't sufficient or desired) before deleting.
        // The DB schema for OrderDetails and Feedback uses ON DELETE SET NULL for ProductID.
        // Cart uses ON DELETE CASCADE for ProductID.
        String sql = "DELETE FROM Products WHERE ProductID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, productId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) System.out.println("Product deleted: " + productId);
            return rowsAffected > 0;
        }
    }
}
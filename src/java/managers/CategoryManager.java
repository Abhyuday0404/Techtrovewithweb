package managers;

import db.DBUtil;
import models.Category;
import core.IdGenerator; // Assuming IdGenerator has generateCategoryId()

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryManager {

    public CategoryManager() throws SQLException {
        // Constructor can be empty, DBUtil handles connections
    }

    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM Categories ORDER BY Name";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                categories.add(new Category(rs.getString("CategoryID"), rs.getString("Name")));
            }
        }
        return categories;
    }

    public Category getCategoryById(String categoryId) throws SQLException {
        Category category = null;
        String sql = "SELECT * FROM Categories WHERE CategoryID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    category = new Category(rs.getString("CategoryID"), rs.getString("Name"));
                }
            }
        }
        return category;
    }

    public String addCategory(String categoryName) throws SQLException {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        // Check if category name already exists to prevent duplicates
        if (getCategoryByName(categoryName) != null) {
            throw new SQLException("Category with name '" + categoryName + "' already exists.");
        }

        String newCategoryId = IdGenerator.generateCategoryId();
        String sql = "INSERT INTO Categories (CategoryID, Name) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newCategoryId);
            pstmt.setString(2, categoryName.trim());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }
            System.out.println("Category added: " + categoryName + " (ID: " + newCategoryId + ")");
            return newCategoryId;
        }
    }
    
    public Category getCategoryByName(String categoryName) throws SQLException {
        Category category = null;
        String sql = "SELECT * FROM Categories WHERE Name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoryName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    category = new Category(rs.getString("CategoryID"), rs.getString("Name"));
                }
            }
        }
        return category;
    }


    public boolean updateCategory(String categoryId, String newCategoryName) throws SQLException {
        if (categoryId == null || categoryId.trim().isEmpty() || newCategoryName == null || newCategoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID and new name cannot be empty.");
        }
         // Optional: Check if new name would conflict with an existing different category
        Category existingByName = getCategoryByName(newCategoryName);
        if (existingByName != null && !existingByName.getCategoryId().equals(categoryId)) {
            throw new SQLException("Another category with the name '" + newCategoryName + "' already exists.");
        }

        String sql = "UPDATE Categories SET Name = ? WHERE CategoryID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newCategoryName.trim());
            pstmt.setString(2, categoryId);
            int affectedRows = pstmt.executeUpdate();
            if(affectedRows > 0) System.out.println("Category updated: " + categoryId + " to name " + newCategoryName);
            return affectedRows > 0;
        }
    }

    public boolean deleteCategory(String categoryId) throws SQLException {
         if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be empty.");
        }
        // Before deleting, check if any products are using this category.
        // The DB schema uses ON DELETE SET NULL for Products.CategoryID,
        // so direct deletion is possible, but an admin might want a warning or to reassign products.
        // For simplicity here, we'll just delete.
        // To check: SELECT COUNT(*) FROM Products WHERE CategoryID = ?
        
        // Check if products are associated
        String checkProductsSql = "SELECT COUNT(*) AS product_count FROM Products WHERE CategoryID = ?";
        int productCount = 0;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkProductsSql)) {
            checkStmt.setString(1, categoryId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    productCount = rs.getInt("product_count");
                }
            }
        }

        if (productCount > 0) {
            // In a real app, you might prevent deletion or ask for confirmation.
            // For this assignment, we can choose to allow it (products will have NULL categoryID)
            // or throw an error. Let's throw an error to be safer for data integrity awareness.
             throw new SQLException("Cannot delete category '" + categoryId + "'. It is associated with " + productCount + " product(s). Please reassign products first or update schema for cascade delete (not recommended here).");
            // If allowing: System.out.println("Warning: Deleting category " + categoryId + " which is used by " + productCount + " products. Their CategoryID will be set to NULL.");
        }


        String sql = "DELETE FROM Categories WHERE CategoryID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoryId);
            int affectedRows = pstmt.executeUpdate();
             if(affectedRows > 0) System.out.println("Category deleted: " + categoryId);
            return affectedRows > 0;
        }
    }
}
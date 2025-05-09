// src/java/db/DatabaseInitializer.java
package db;

import core.IdGenerator;
import core.PasswordUtil;
import models.User; // For UserRole enum

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.sql.Timestamp;

public class DatabaseInitializer {

    public static void initializeDatabaseSchema() {
        if (!DBUtil.isConfigured()) {
            String errorMsg = "Database connection is not configured. Cannot initialize schema.";
            System.err.println(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {
            System.out.println("Starting database schema initialization...");

            // Users Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Users (" +
                    "UserID VARCHAR(50) PRIMARY KEY," +
                    "FullName VARCHAR(255) NOT NULL," +
                    "Email VARCHAR(255) NOT NULL UNIQUE," +
                    "Password VARCHAR(255) NOT NULL," + // Store hashed passwords in production
                    "PhoneNumber VARCHAR(20)," +
                    "Address TEXT," +
                    "Role VARCHAR(50) NOT NULL CHECK (Role IN ('USER', 'ADMIN'))," + // Enum-like check
                    "RegistrationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "ProfilePicture VARCHAR(255)" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'Users' created or already exists.");

            // Admins Table (linking UserID to specific admin roles/permissions)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Admins (" +
                    "AdminID VARCHAR(50) PRIMARY KEY," +
                    "UserID VARCHAR(50) NOT NULL UNIQUE," +
                    "Permissions TEXT," + // e.g., comma-separated list of permissions
                    "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'Admins' created or already exists.");

            // Categories Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Categories (" +
                    "CategoryID VARCHAR(50) PRIMARY KEY," +
                    "Name VARCHAR(255) NOT NULL UNIQUE" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'Categories' created or already exists.");

            // Products Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Products (" +
                    "ProductID VARCHAR(50) PRIMARY KEY," +
                    "Name VARCHAR(255) NOT NULL," +
                    "Brand VARCHAR(100)," +
                    "Model VARCHAR(100)," +
                    "Description TEXT," +
                    "Price DECIMAL(10, 2) NOT NULL CHECK (Price >= 0)," +
                    "Stock INT NOT NULL CHECK (Stock >= 0)," +
                    "ManufactureDate DATE," +
                    "CategoryID VARCHAR(50)," +
                    "ImageURL VARCHAR(512)," + // Increased length for image URLs
                    "FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID) ON DELETE SET NULL" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'Products' created or already exists.");

            // Cart Table (stores items in user's shopping cart)
            // A unique constraint on UserID+ProductID ensures a product appears only once per user cart (quantity is updated)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Cart (" +
                    "CartID VARCHAR(50) PRIMARY KEY," + // Or (UserID, ProductID) as composite PK
                    "UserID VARCHAR(50) NOT NULL," +
                    "ProductID VARCHAR(50) NOT NULL," +
                    "Quantity INT NOT NULL CHECK (Quantity > 0)," +
                    "AddedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE," +
                    "FOREIGN KEY (ProductID) REFERENCES Products(ProductID) ON DELETE CASCADE," + // Cascade delete if product removed
                    "UNIQUE KEY (UserID, ProductID)" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'Cart' created or already exists.");

            // Orders Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Orders (" +
                    "OrderID VARCHAR(50) PRIMARY KEY," +
                    "UserID VARCHAR(50)," + // Can be NULL if user is deleted and we want to keep order history
                    "OrderDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "TotalAmount DECIMAL(10, 2) NOT NULL CHECK (TotalAmount >= 0)," +
                    "Status VARCHAR(50) DEFAULT 'PENDING'," + // e.g., PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
                    "ShippingAddress TEXT," + // Snapshot of address at time of order
                    "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE SET NULL" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'Orders' created or already exists.");

            // OrderDetails Table (line items for each order)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS OrderDetails (" +
                    "OrderDetailID VARCHAR(50) PRIMARY KEY," +
                    "OrderID VARCHAR(50) NOT NULL," +
                    "ProductID VARCHAR(50)," + // Can be NULL if product is deleted
                    "ProductNameSnapshot VARCHAR(255) NOT NULL," + // Name of product at time of order
                    "Quantity INT NOT NULL CHECK (Quantity > 0)," +
                    "PriceAtPurchase DECIMAL(10, 2) NOT NULL CHECK (PriceAtPurchase >= 0)," + // Price of product at time of order
                    "FOREIGN KEY (OrderID) REFERENCES Orders(OrderID) ON DELETE CASCADE," +
                    "FOREIGN KEY (ProductID) REFERENCES Products(ProductID) ON DELETE SET NULL" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'OrderDetails' created or already exists.");

            // Payments Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Payments (" +
                    "PaymentID VARCHAR(50) PRIMARY KEY," +
                    "OrderID VARCHAR(50) NOT NULL," +
                    "PaymentMethod VARCHAR(100)," + // e.g., "COD_DEMO", "Credit Card Placeholder"
                    "TransactionID VARCHAR(255)," + // External transaction ID from payment gateway
                    "PaymentDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "Status VARCHAR(50) NOT NULL CHECK (Status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED'))," +
                    "FOREIGN KEY (OrderID) REFERENCES Orders(OrderID) ON DELETE CASCADE" + // Or RESTRICT if payments shouldn't be deleted with order
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'Payments' created or already exists.");

            // Feedback Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Feedback (" +
                    "FeedbackID VARCHAR(50) PRIMARY KEY," +
                    "UserID VARCHAR(50)," +
                    "ProductID VARCHAR(50)," +
                    "Message TEXT," +
                    "Rating INT CHECK (Rating >= 1 AND Rating <= 5)," + // Rating from 1 to 5
                    "Timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE SET NULL," + // Feedback remains even if user deleted
                    "FOREIGN KEY (ProductID) REFERENCES Products(ProductID) ON DELETE SET NULL" + // Feedback remains even if product deleted
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'Feedback' created or already exists.");

            // PurchaseHistory Table (can be simpler, or more detailed linking users to orders directly)
            // This seems a bit redundant if Orders table has UserID.
            // If it's for a different purpose, define accordingly.
            // For now, let's assume it's a direct link for quick lookup or if Orders.UserID could be NULL.
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS PurchaseHistory (" +
                    "HistoryID VARCHAR(50) PRIMARY KEY," +
                    "UserID VARCHAR(50) NOT NULL," +
                    "OrderID VARCHAR(50) NOT NULL," +
                    "PurchaseDate TIMESTAMP NOT NULL," + // Can be OrderDate from Orders table
                    "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE," +
                    "FOREIGN KEY (OrderID) REFERENCES Orders(OrderID) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'PurchaseHistory' created or already exists.");

            System.out.println("Schema initialization process completed.");
            
            // Add default users if they don't exist
            addDefaultUserIfNotExists(conn, IdGenerator.DEFAULT_USER_ID, "Default User", "user@example.com", "password", User.UserRole.USER);
            addDefaultAdminIfNotExists(conn, IdGenerator.DEFAULT_ADMIN_ID, "Default Admin", "admin@example.com", "adminpass", User.UserRole.ADMIN);


        } catch (SQLException e) {
            String errorMsg = "Error initializing database schema: " + e.getMessage();
            System.err.println(errorMsg);
            e.printStackTrace();
            throw new RuntimeException(errorMsg, e);
        }
    }
    
    private static void addDefaultUserIfNotExists(Connection conn, String userId, String fullName, String email, String password, User.UserRole role) throws SQLException {
        String checkUserSql = "SELECT COUNT(*) FROM Users WHERE UserID = ? OR Email = ?";
        try (PreparedStatement checkPstmt = conn.prepareStatement(checkUserSql)) {
            checkPstmt.setString(1, userId);
            checkPstmt.setString(2, email);
            try (ResultSet rs = checkPstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // User does not exist, add them
                    String insertUserSql = "INSERT INTO Users (UserID, FullName, Email, Password, Role, RegistrationDate) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertPstmt = conn.prepareStatement(insertUserSql)) {
                        insertPstmt.setString(1, userId);
                        insertPstmt.setString(2, fullName);
                        insertPstmt.setString(3, email);
                        insertPstmt.setString(4, PasswordUtil.hashPassword(password)); // Hash password
                        insertPstmt.setString(5, role.name());
                        insertPstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                        insertPstmt.executeUpdate();
                        System.out.println("Default user '" + fullName + "' added.");
                        
                        // If Admin role, also add to Admins table
                        if (role == User.UserRole.ADMIN) {
                            String adminId = IdGenerator.generateAdminId(); // Use a generic Admin ID prefix
                            String insertAdminSql = "INSERT INTO Admins (AdminID, UserID, Permissions) VALUES (?, ?, ?)";
                            try (PreparedStatement insertAdminPstmt = conn.prepareStatement(insertAdminSql)) {
                                insertAdminPstmt.setString(1, adminId);
                                insertAdminPstmt.setString(2, userId);
                                insertAdminPstmt.setString(3, "ALL"); // Default full permissions
                                insertAdminPstmt.executeUpdate();
                                System.out.println("Default admin role setup for UserID: " + userId);
                            }
                        }
                    }
                } else {
                    System.out.println("Default user '" + fullName + "' or email '" + email + "' already exists.");
                }
            }
        }
    }

    // Simplified calls for LoginServlet's default users
    private static final String DEFAULT_USER_ID_FOR_LOGIN_SERVLET = "USR_USER_DEF";
    private static final String DEFAULT_ADMIN_ID_FOR_LOGIN_SERVLET = "USR_ADMIN_DEF";

    private static void addDefaultAdminIfNotExists(Connection conn, String userId, String fullName, String email, String password, User.UserRole role) throws SQLException {
        addDefaultUserIfNotExists(conn, userId, fullName, email, password, role);
    }
}
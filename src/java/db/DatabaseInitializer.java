package db;

import core.IdGenerator;
import core.PasswordUtil;
import models.User; // For UserRole enum

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate; // For dummy product data
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
                    "Password VARCHAR(255) NOT NULL," +
                    "PhoneNumber VARCHAR(20)," +
                    "Address TEXT," +
                    "Role VARCHAR(50) NOT NULL CHECK (Role IN ('USER', 'ADMIN'))," +
                    "RegistrationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "ProfilePicture VARCHAR(255)" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'Users' created or already exists.");

            // Admins Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Admins (" +
                    "AdminID VARCHAR(50) PRIMARY KEY," +
                    "UserID VARCHAR(50) NOT NULL UNIQUE," +
                    "Permissions TEXT," +
                    "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'Admins' created or already exists.");

            // Categories Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Categories (" +
                    "CategoryID VARCHAR(50) PRIMARY KEY," +
                    "Name VARCHAR(255) NOT NULL UNIQUE" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'Categories' created or already exists.");
            // Add a default category if needed for dummy products
            addCategoryIfNotExists(conn, "CAT_GEN_ELEC", "General Electronics");

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
                    "ImageURL VARCHAR(512)," +
                    "FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID) ON DELETE SET NULL" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'Products' created or already exists.");

            // Cart Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Cart (" +
                    "CartID VARCHAR(50) PRIMARY KEY," +
                    "UserID VARCHAR(50) NOT NULL," +
                    "ProductID VARCHAR(50) NOT NULL," +
                    "Quantity INT NOT NULL CHECK (Quantity > 0)," +
                    "AddedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE," +
                    "FOREIGN KEY (ProductID) REFERENCES Products(ProductID) ON DELETE CASCADE," +
                    "UNIQUE KEY (UserID, ProductID)" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'Cart' created or already exists.");

            // Orders Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Orders (" +
                    "OrderID VARCHAR(50) PRIMARY KEY," +
                    "UserID VARCHAR(50)," +
                    "OrderDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "TotalAmount DECIMAL(10, 2) NOT NULL CHECK (TotalAmount >= 0)," +
                    "Status VARCHAR(50) DEFAULT 'PENDING'," +
                    "ShippingAddress TEXT," +
                    "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE SET NULL" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'Orders' created or already exists.");

            // OrderDetails Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS OrderDetails (" +
                    "OrderDetailID VARCHAR(50) PRIMARY KEY," +
                    "OrderID VARCHAR(50) NOT NULL," +
                    "ProductID VARCHAR(50)," +
                    "ProductNameSnapshot VARCHAR(255) NOT NULL," +
                    "Quantity INT NOT NULL CHECK (Quantity > 0)," +
                    "PriceAtPurchase DECIMAL(10, 2) NOT NULL CHECK (PriceAtPurchase >= 0)," +
                    "FOREIGN KEY (OrderID) REFERENCES Orders(OrderID) ON DELETE CASCADE," +
                    "FOREIGN KEY (ProductID) REFERENCES Products(ProductID) ON DELETE SET NULL" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'OrderDetails' created or already exists.");

            // Payments Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Payments (" +
                    "PaymentID VARCHAR(50) PRIMARY KEY," +
                    "OrderID VARCHAR(50) NOT NULL," +
                    "PaymentMethod VARCHAR(100)," +
                    "TransactionID VARCHAR(255)," +
                    "PaymentDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "Status VARCHAR(50) NOT NULL CHECK (Status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED'))," +
                    "FOREIGN KEY (OrderID) REFERENCES Orders(OrderID) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'Payments' created or already exists.");

            // Feedback Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Feedback (" +
                    "FeedbackID VARCHAR(50) PRIMARY KEY," +
                    "UserID VARCHAR(50)," +
                    "ProductID VARCHAR(50)," +
                    "Message TEXT," +
                    "Rating INT CHECK (Rating >= 1 AND Rating <= 5)," +
                    "Timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE SET NULL," +
                    "FOREIGN KEY (ProductID) REFERENCES Products(ProductID) ON DELETE SET NULL" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'Feedback' created or already exists.");

            // PurchaseHistory Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS PurchaseHistory (" +
                    "HistoryID VARCHAR(50) PRIMARY KEY," +
                    "UserID VARCHAR(50) NOT NULL," +
                    "OrderID VARCHAR(50) NOT NULL," +
                    "PurchaseDate TIMESTAMP NOT NULL," +
                    "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE," +
                    "FOREIGN KEY (OrderID) REFERENCES Orders(OrderID) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB;");
            System.out.println("Table 'PurchaseHistory' created or already exists.");

            System.out.println("Schema initialization process completed.");

            // Add default users if they don't exist
            addDefaultUserIfNotExists(conn, IdGenerator.DEFAULT_USER_ID, "Default User", "user@example.com", "password", User.UserRole.USER);
            addDefaultAdminIfNotExists(conn, IdGenerator.DEFAULT_ADMIN_ID, "Default Admin", "admin@example.com", "adminpass", User.UserRole.ADMIN);

            // Add dummy products
            addProductIfNotExists(conn, IdGenerator.generateProductId(), "Demo Laptop Pro", "TechBrand", "LP-X1",
                                  "A powerful laptop for professionals and gamers.", 1299.99, 15, LocalDate.now().minusMonths(2),
                                  "CAT_GEN_ELEC", "https://via.placeholder.com/300x200.png?text=Laptop+Pro"); // Assuming CAT_GEN_ELEC exists

            addProductIfNotExists(conn, IdGenerator.generateProductId(), "SmartPhone Basic", "ConnectAll", "SP-B50",
                                  "Reliable and affordable smartphone for daily use.", 249.00, 50, LocalDate.now().minusMonths(1),
                                  "CAT_GEN_ELEC", "https://via.placeholder.com/300x200.png?text=Smartphone");

            addProductIfNotExists(conn, IdGenerator.generateProductId(), "Wireless Headphones", "SoundWave", "WH-700",
                                  "Noise-cancelling over-ear headphones with great sound.", 149.95, 30, LocalDate.now().minusWeeks(2),
                                  "CAT_GEN_ELEC", "https://via.placeholder.com/300x200.png?text=Headphones");


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
                    String insertUserSql = "INSERT INTO Users (UserID, FullName, Email, Password, Role, RegistrationDate) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertPstmt = conn.prepareStatement(insertUserSql)) {
                        insertPstmt.setString(1, userId);
                        insertPstmt.setString(2, fullName);
                        insertPstmt.setString(3, email);
                        insertPstmt.setString(4, PasswordUtil.hashPassword(password));
                        insertPstmt.setString(5, role.name());
                        insertPstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                        insertPstmt.executeUpdate();
                        System.out.println("Default user '" + fullName + "' added.");

                        if (role == User.UserRole.ADMIN) {
                            String adminId = IdGenerator.generateAdminId();
                            String insertAdminSql = "INSERT INTO Admins (AdminID, UserID, Permissions) VALUES (?, ?, ?)";
                            try (PreparedStatement insertAdminPstmt = conn.prepareStatement(insertAdminSql)) {
                                insertAdminPstmt.setString(1, adminId);
                                insertAdminPstmt.setString(2, userId);
                                insertAdminPstmt.setString(3, "ALL");
                                insertAdminPstmt.executeUpdate();
                                System.out.println("Default admin role setup for UserID: " + userId);
                            }
                        }
                    }
                } else {
                    System.out.println("Default user '" + fullName + "' or email '" + email + "' already exists or error checking.");
                }
            }
        }
    }

    private static void addDefaultAdminIfNotExists(Connection conn, String userId, String fullName, String email, String password, User.UserRole role) throws SQLException {
        addDefaultUserIfNotExists(conn, userId, fullName, email, password, role);
    }

    private static void addCategoryIfNotExists(Connection conn, String categoryId, String categoryName) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM Categories WHERE CategoryID = ? OR Name = ?";
        try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
            checkPstmt.setString(1, categoryId);
            checkPstmt.setString(2, categoryName);
            try (ResultSet rs = checkPstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertSql = "INSERT INTO Categories (CategoryID, Name) VALUES (?, ?)";
                    try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                        insertPstmt.setString(1, categoryId);
                        insertPstmt.setString(2, categoryName);
                        insertPstmt.executeUpdate();
                        System.out.println("Default category '" + categoryName + "' added.");
                    }
                } else {
                     System.out.println("Default category '" + categoryName + "' already exists or error checking.");
                }
            }
        }
    }

    private static void addProductIfNotExists(Connection conn, String productId, String name, String brand, String model,
                                             String description, double price, int stock, LocalDate mfgDate,
                                             String categoryId, String imageUrl) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM Products WHERE ProductID = ?";
        try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
            checkPstmt.setString(1, productId);
            try (ResultSet rs = checkPstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertSql = "INSERT INTO Products (ProductID, Name, Brand, Model, Description, Price, Stock, ManufactureDate, CategoryID, ImageURL) " +
                                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                        insertPstmt.setString(1, productId);
                        insertPstmt.setString(2, name);
                        insertPstmt.setString(3, brand);
                        insertPstmt.setString(4, model);
                        insertPstmt.setString(5, description);
                        insertPstmt.setDouble(6, price);
                        insertPstmt.setInt(7, stock);
                        insertPstmt.setDate(8, mfgDate != null ? java.sql.Date.valueOf(mfgDate) : null);
                        insertPstmt.setString(9, categoryId);
                        insertPstmt.setString(10, imageUrl);
                        insertPstmt.executeUpdate();
                        System.out.println("Default product '" + name + "' added.");
                    }
                } else {
                    System.out.println("Default product '" + name + "' (ID: " + productId + ") already exists or error checking.");
                }
            }
        }
    }
}
// src/java/db/DatabaseInitializer.java
package db;

import core.IdGenerator;
import models.Category;
import models.User;
import core.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DatabaseInitializer {

    public static void initializeDatabaseSchema() {
        // ... (DBUtil check and sysout remain the same) ...
        if (!DBUtil.isConfigured()) {
            System.out.println("DatabaseInitializer: DBUtil not configured. Skipping schema creation.");
            throw new IllegalStateException("Database connection is not configured. Cannot initialize schema.");
        }
        System.out.println("DatabaseInitializer: Initializing database schema (creating tables if they don't exist)...");


        String[] createTableSQLs = {
            // ... (Users, Admins, Categories tables remain the same) ...
            "CREATE TABLE IF NOT EXISTS Users (" +
            "    UserID VARCHAR(50) PRIMARY KEY," +
            "    FullName VARCHAR(100) NOT NULL," +
            "    Email VARCHAR(100) NOT NULL UNIQUE," +
            "    Password VARCHAR(255) NOT NULL," +
            "    PhoneNumber VARCHAR(20)," +
            "    Address TEXT," +
            "    Role VARCHAR(20) NOT NULL CHECK (Role IN ('USER', 'ADMIN'))," +
            "    RegistrationDate DATETIME NOT NULL," +
            "    ProfilePictureURL VARCHAR(255)" + // This is for User profile, can remain if desired, or be removed too
            ")",

            "CREATE TABLE IF NOT EXISTS Admins (" +
            "    AdminID VARCHAR(50) PRIMARY KEY," +
            "    UserID VARCHAR(50) NOT NULL UNIQUE," +
            "    Permissions TEXT," +
            "    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE" +
            ")",

            "CREATE TABLE IF NOT EXISTS Categories (" +
            "    CategoryID VARCHAR(50) PRIMARY KEY," +
            "    Name VARCHAR(100) NOT NULL UNIQUE" +
            ")",

            // Products Table - REMOVE ImageURL
            "CREATE TABLE IF NOT EXISTS Products (" +
            "    ProductID VARCHAR(50) PRIMARY KEY," +
            "    Name VARCHAR(255) NOT NULL," +
            "    Brand VARCHAR(100)," +
            "    Model VARCHAR(100)," +
            "    Description TEXT," +
            "    Price DECIMAL(10, 2) NOT NULL CHECK (Price >= 0)," +
            "    Stock INT NOT NULL CHECK (Stock >= 0)," +
            "    ManufactureDate DATE," +
            "    CategoryID VARCHAR(50)," +
            // "    ImageURL VARCHAR(255)," + // REMOVED
            "    FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID) ON DELETE SET NULL" +
            ")",

            // ... (Cart, Orders, OrderDetails, Feedback, Payments, PurchaseHistory tables remain the same) ...
            "CREATE TABLE IF NOT EXISTS Cart (" +
            "    CartID VARCHAR(50) PRIMARY KEY," +
            "    UserID VARCHAR(50) NOT NULL," +
            "    ProductID VARCHAR(50) NOT NULL," +
            "    Quantity INT NOT NULL CHECK (Quantity > 0)," +
            "    AddedDate DATETIME DEFAULT CURRENT_TIMESTAMP," +
            "    UNIQUE (UserID, ProductID)," +
            "    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE," +
            "    FOREIGN KEY (ProductID) REFERENCES Products(ProductID) ON DELETE CASCADE" +
            ")",

            "CREATE TABLE IF NOT EXISTS Orders (" +
            "    OrderID VARCHAR(50) PRIMARY KEY," +
            "    UserID VARCHAR(50)," +
            "    OrderDate DATETIME NOT NULL," +
            "    TotalAmount DECIMAL(12, 2) NOT NULL CHECK (TotalAmount >= 0)," +
            "    ShippingAddress TEXT," +
            "    OrderStatus VARCHAR(50) DEFAULT 'PENDING'," +
            "    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE SET NULL" +
            ")",

            "CREATE TABLE IF NOT EXISTS OrderDetails (" +
            "    OrderDetailID VARCHAR(50) PRIMARY KEY," +
            "    OrderID VARCHAR(50) NOT NULL," +
            "    ProductID VARCHAR(50)," +
            "    ProductName VARCHAR(255) NOT NULL," +
            "    Quantity INT NOT NULL CHECK (Quantity > 0)," +
            "    PriceAtOrder DECIMAL(10, 2) NOT NULL CHECK (PriceAtOrder >= 0)," +
            "    FOREIGN KEY (OrderID) REFERENCES Orders(OrderID) ON DELETE CASCADE," +
            "    FOREIGN KEY (ProductID) REFERENCES Products(ProductID) ON DELETE SET NULL" +
            ")",

            "CREATE TABLE IF NOT EXISTS Feedback (" +
            "    FeedbackID VARCHAR(50) PRIMARY KEY," +
            "    UserID VARCHAR(50)," +
            "    ProductID VARCHAR(50)," +
            "    Message TEXT," +
            "    Rating INT NOT NULL CHECK (Rating >= 1 AND Rating <= 5)," +
            "    Timestamp DATETIME NOT NULL," +
            "    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE SET NULL," +
            "    FOREIGN KEY (ProductID) REFERENCES Products(ProductID) ON DELETE SET NULL" +
            ")",

            "CREATE TABLE IF NOT EXISTS Payments (" +
            "    PaymentID VARCHAR(50) PRIMARY KEY," +
            "    OrderID VARCHAR(50) NOT NULL," +
            "    PaymentMethod VARCHAR(100)," +
            "    TransactionID VARCHAR(255)," +
            "    PaymentDate DATETIME NOT NULL," +
            "    Status VARCHAR(50) NOT NULL," +
            "    FOREIGN KEY (OrderID) REFERENCES Orders(OrderID) ON DELETE CASCADE" +
            ")",
            
            "CREATE TABLE IF NOT EXISTS PurchaseHistory (" +
            "   HistoryID VARCHAR(50) PRIMARY KEY, " +
            "   UserID VARCHAR(50), " +
            "   OrderID VARCHAR(50) NOT NULL, " +
            "   PurchaseDate DATETIME NOT NULL, " +
            "   FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE SET NULL, " +
            "   FOREIGN KEY (OrderID) REFERENCES Orders(OrderID) ON DELETE CASCADE" +
            ")"
        };

        // ... (try-catch block for executing SQLs remains the same) ...
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : createTableSQLs) {
                stmt.executeUpdate(sql);
            }
            System.out.println("Database schema initialization: Tables created/verified successfully.");
            initializeSampleData(conn); 

        } catch (SQLException e) {
            System.err.println("Database schema initialization FAILED: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Fatal: Could not initialize database schema.", e);
        }
    }
    // ... (overloaded initializeSampleData() remains the same) ...
    public static void initializeSampleData() {
         if (!DBUtil.isConfigured()) {
            System.out.println("DatabaseInitializer: DBUtil not configured. Skipping sample data initialization.");
            return;
        }
        try(Connection conn = DBUtil.getConnection()) {
            initializeSampleData(conn);
        } catch (SQLException e) {
            System.err.println("DatabaseInitializer: Error getting connection for sample data: " + e.getMessage());
        }
    } 

    public static void initializeSampleData(Connection conn) {
        // ... (try-catch structure, sampleDataExists check, category creation remain the same) ...
        System.out.println("DatabaseInitializer: Checking and initializing sample data if needed...");
        boolean originalAutoCommit = false;
        try {
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            if (sampleDataExists(conn)) {
                System.out.println("DatabaseInitializer: Sample data appears to exist. Skipping re-initialization.");
                conn.rollback(); 
                return;
            }

            Category catLaptops = addCategoryIfNotExists(conn, "CAT_LAPTOP", "Laptops");
            Category catSmartphones = addCategoryIfNotExists(conn, "CAT_SMARTPHONE", "Smartphones");
            Category catAccessories = addCategoryIfNotExists(conn, "CAT_ACCESSORY", "Accessories");
            Category catGenElec = addCategoryIfNotExists(conn, "CAT_GEN_ELEC", "General Electronics");

            // Update addProductIfNotExists calls - remove the last argument (imageUrl)
            addProductIfNotExists(conn, "PROD_LP001", "Demo Laptop Pro", "TechBrand", "Model X1",
                "A powerful and versatile laptop.", 1299.99, 15, LocalDate.now().minusMonths(3),
                catLaptops != null ? catLaptops.getCategoryId() : catGenElec.getCategoryId());

            addProductIfNotExists(conn, "PROD_SP001", "SmartPhone Basic", "ConnectAll", "C1",
                "An affordable smartphone with essential features.", 249.00, 50, LocalDate.now().minusMonths(1),
                catSmartphones != null ? catSmartphones.getCategoryId() : catGenElec.getCategoryId());

            addProductIfNotExists(conn, "PROD_AC001", "Wireless Ergonomic Mouse", "ClickTech", "ErgoM",
                "Comfortable wireless mouse.", 29.99, 75, LocalDate.now().minusDays(45),
                catAccessories != null ? catAccessories.getCategoryId() : catGenElec.getCategoryId());
            
            addProductIfNotExists(conn, "PROD_AC002", "RGB Gaming Keyboard", "GamerGear", "StrikePro",
                "Mechanical gaming keyboard with RGB.", 89.50, 30, LocalDate.now().minusDays(20),
                 catAccessories != null ? catAccessories.getCategoryId() : catGenElec.getCategoryId());

            // ... (user creation, commit, catch, finally blocks remain the same) ...
            addDemoUserIfNotExists(conn, IdGenerator.DEFAULT_USER_ID, "Default User", "user@example.com",
                    PasswordUtil.hashPassword("password"), null, null, User.UserRole.USER,
                    LocalDateTime.now().minusDays(10), null);

            addDemoUserIfNotExists(conn, IdGenerator.DEFAULT_ADMIN_ID, "Default Admin", "admin@example.com",
                    PasswordUtil.hashPassword("adminpass"), null, null, User.UserRole.ADMIN,
                    LocalDateTime.now().minusDays(10), null);
            
            addAdminRoleLinkIfNotExists(conn, IdGenerator.DEFAULT_ADMIN_ID);

            conn.commit();
            System.out.println("DatabaseInitializer: Sample data initialization completed successfully.");

        } catch (SQLException e) {
            System.err.println("DatabaseInitializer: Error during sample data initialization: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null && !conn.getAutoCommit()) { 
                    conn.rollback();
                    System.err.println("DatabaseInitializer: Transaction rolled back due to error.");
                }
            } catch (SQLException ex) {
                System.err.println("DatabaseInitializer: Error rolling back transaction: " + ex.getMessage());
            }
        } finally {
             try {
                if (conn != null) {
                    conn.setAutoCommit(originalAutoCommit); 
                }
            } catch (SQLException ex) {
                System.err.println("DatabaseInitializer: Error restoring auto-commit state: " + ex.getMessage());
            }
        }
    }

    // ... (sampleDataExists, addCategoryIfNotExists remain the same) ...
    private static boolean sampleDataExists(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Products WHERE ProductID = ?"; 
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "PROD_LP001"); 
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    } 

    private static Category addCategoryIfNotExists(Connection conn, String categoryId, String name) throws SQLException {
        String checkSql = "SELECT Name FROM Categories WHERE CategoryID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Category(categoryId, rs.getString("Name"));
                }
            }
        }
        String insertSql = "INSERT INTO Categories (CategoryID, Name) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, categoryId);
            pstmt.setString(2, name);
            pstmt.executeUpdate();
            System.out.println("Added sample category: " + name);
            return new Category(categoryId, name);
        }
    } 


    // Update addProductIfNotExists method signature and SQL - remove imageUrl
    private static void addProductIfNotExists(Connection conn, String productId, String name, String brand, String model,
                                           String description, double price, int stock, LocalDate mfgDate,
                                           String categoryId) throws SQLException { // imageUrl removed
        String checkSql = "SELECT Name FROM Products WHERE ProductID = ?";
        // ... (check if product exists remains the same) ...
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        // SQL and PreparedStatement updated
        String insertSql = "INSERT INTO Products (ProductID, Name, Brand, Model, Description, Price, Stock, ManufactureDate, CategoryID) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"; // ImageURL column removed
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, productId);
            pstmt.setString(2, name);
            pstmt.setString(3, brand);
            pstmt.setString(4, model);
            pstmt.setString(5, description);
            pstmt.setDouble(6, price);
            pstmt.setInt(7, stock);
            pstmt.setDate(8, mfgDate != null ? java.sql.Date.valueOf(mfgDate) : null);
            pstmt.setString(9, categoryId);
            // pstmt.setString(10, imageUrl); // REMOVED
            pstmt.executeUpdate();
            System.out.println("Added sample product: " + name);
        }
    }
    // ... (addDemoUserIfNotExists, addAdminRoleLinkIfNotExists remain the same) ...
    private static void addDemoUserIfNotExists(Connection conn, String userId, String fullName, String email,
                                        String hashedPassword, String phoneNumber, String address,
                                        User.UserRole role, LocalDateTime registrationDate, String profilePicture) throws SQLException {
        String checkSql = "SELECT FullName FROM Users WHERE UserID = ? OR Email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }
        String insertSql = "INSERT INTO Users (UserID, FullName, Email, Password, PhoneNumber, Address, Role, RegistrationDate, ProfilePictureURL) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, fullName);
            pstmt.setString(3, email);
            pstmt.setString(4, hashedPassword);
            pstmt.setString(5, phoneNumber);
            pstmt.setString(6, address);
            pstmt.setString(7, role.name());
            pstmt.setTimestamp(8, java.sql.Timestamp.valueOf(registrationDate));
            pstmt.setString(9, profilePicture);
            pstmt.executeUpdate();
            System.out.println("Added demo user: " + fullName + " with role " + role.name());
        }
    } 

    private static void addAdminRoleLinkIfNotExists(Connection conn, String userId) throws SQLException {
        String checkAdminSql = "SELECT AdminID FROM Admins WHERE UserID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(checkAdminSql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }
        String insertAdminSql = "INSERT INTO Admins (AdminID, UserID, Permissions) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertAdminSql)) {
            pstmt.setString(1, IdGenerator.generateAdminId());
            pstmt.setString(2, userId);
            pstmt.setString(3, "ALL"); 
            pstmt.executeUpdate();
            System.out.println("Added Admin role link for UserID: " + userId);
        }
    } 
}
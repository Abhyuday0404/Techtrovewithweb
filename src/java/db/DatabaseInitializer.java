package db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    // Table creation SQL statements (copied and adapted from your Part 1)
    private static final String CREATE_USERS_TABLE =
        "CREATE TABLE IF NOT EXISTS Users (" +
        "UserID VARCHAR(20) PRIMARY KEY," +
        "FullName VARCHAR(100) NOT NULL," +
        "Email VARCHAR(100) UNIQUE NOT NULL," +
        "Password VARCHAR(255) NOT NULL," + // Store hashed passwords in a real app
        "PhoneNumber VARCHAR(20)," +
        "Address TEXT," +
        "Role VARCHAR(10) NOT NULL DEFAULT 'USER'," + // USER, ADMIN
        "RegistrationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "ProfilePicture VARCHAR(255)" +
        ")";

    private static final String CREATE_ADMINS_TABLE =
        "CREATE TABLE IF NOT EXISTS Admins (" +
        "AdminID VARCHAR(20) PRIMARY KEY," +
        "UserID VARCHAR(20) NOT NULL UNIQUE," +
        "Permissions TEXT," + // e.g., "ALL", "MANAGE_PRODUCTS,VIEW_ORDERS"
        "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE" +
        ")";

    private static final String CREATE_CATEGORIES_TABLE =
        "CREATE TABLE IF NOT EXISTS Categories (" +
        "CategoryID VARCHAR(20) PRIMARY KEY," +
        "Name VARCHAR(100) NOT NULL UNIQUE" +
        ")";

    private static final String CREATE_PRODUCTS_TABLE =
        "CREATE TABLE IF NOT EXISTS Products (" +
        "ProductID VARCHAR(20) PRIMARY KEY," +
        "Name VARCHAR(150) NOT NULL," + // Increased length for product names
        "Brand VARCHAR(100)," +
        "Model VARCHAR(100)," +
        "Description TEXT," +
        "Price DECIMAL(10, 2) NOT NULL," +
        "Stock INT NOT NULL," +
        "ManufactureDate DATE," +
        "CategoryID VARCHAR(20)," +
        "ImageURL VARCHAR(255)," +
        "FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID) ON DELETE SET NULL" +
        ")";

    private static final String CREATE_CART_TABLE =
        "CREATE TABLE IF NOT EXISTS Cart (" +
        "CartID VARCHAR(20) PRIMARY KEY," +
        "UserID VARCHAR(20) NOT NULL," +
        "ProductID VARCHAR(20) NOT NULL," +
        "Quantity INT NOT NULL," +
        "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE CASCADE," +
        "FOREIGN KEY (ProductID) REFERENCES Products(ProductID) ON DELETE CASCADE," +
        "UNIQUE (UserID, ProductID)" + // A user can only have one entry per product in cart
        ")";

    private static final String CREATE_ORDERS_TABLE =
        "CREATE TABLE IF NOT EXISTS Orders (" +
        "OrderID VARCHAR(20) PRIMARY KEY," +
        "UserID VARCHAR(20)," + // Can be NULL if user is deleted
        "OrderDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
        "TotalAmount DECIMAL(10, 2) NOT NULL," +
        "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE SET NULL" +
        ")";

    private static final String CREATE_ORDER_DETAILS_TABLE =
        "CREATE TABLE IF NOT EXISTS OrderDetails (" +
        "OrderDetailID VARCHAR(20) PRIMARY KEY," +
        "OrderID VARCHAR(20) NOT NULL," +
        "ProductID VARCHAR(20)," + // Can be NULL if product is deleted
        "Quantity INT NOT NULL," +
        "Price DECIMAL(10, 2) NOT NULL," + // Price at the time of order
        "FOREIGN KEY (OrderID) REFERENCES Orders(OrderID) ON DELETE CASCADE," +
        "FOREIGN KEY (ProductID) REFERENCES Products(ProductID) ON DELETE SET NULL" +
        ")";

    private static final String CREATE_FEEDBACK_TABLE =
        "CREATE TABLE IF NOT EXISTS Feedback (" +
        "FeedbackID VARCHAR(20) PRIMARY KEY," +
        "UserID VARCHAR(20)," +
        "ProductID VARCHAR(20)," +
        "Message TEXT," +
        "Rating INT NOT NULL," + // e.g., 1-5
        "Timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE SET NULL," +
        "FOREIGN KEY (ProductID) REFERENCES Products(ProductID) ON DELETE SET NULL" +
        ")";

    private static final String CREATE_PAYMENTS_TABLE =
        "CREATE TABLE IF NOT EXISTS Payments (" +
        "PaymentID VARCHAR(20) PRIMARY KEY," +
        "OrderID VARCHAR(20) NOT NULL," +
        "PaymentMethod VARCHAR(50) NOT NULL," +
        "TransactionID VARCHAR(100)," + // Optional, from payment gateway
        "PaymentDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "Status VARCHAR(20) NOT NULL DEFAULT 'PENDING'," + // PENDING, COMPLETED, FAILED, REFUNDED
        "FOREIGN KEY (OrderID) REFERENCES Orders(OrderID) ON DELETE CASCADE" +
        ")";

    // This table is a bit redundant if Orders already links UserID.
    // It might be for quick lookups or if orders could exist without a user temporarily.
    // For simplicity, if Orders.UserID is ON DELETE SET NULL, PurchaseHistory.UserID should also be.
    private static final String CREATE_PURCHASE_HISTORY_TABLE =
        "CREATE TABLE IF NOT EXISTS PurchaseHistory (" +
        "HistoryID VARCHAR(20) PRIMARY KEY," +
        "UserID VARCHAR(20)," +
        "OrderID VARCHAR(20) NOT NULL UNIQUE," + // An order should only appear once
        "PurchaseDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE SET NULL," +
        "FOREIGN KEY (OrderID) REFERENCES Orders(OrderID) ON DELETE CASCADE" +
        ")";

    // Default User/Admin IDs (match MainLoginFrame from Part 1)
    private static final String DEFAULT_USER_ID = "USR_USER_DEF";
    private static final String DEFAULT_ADMIN_USER_ID = "USR_ADMIN_DEF"; // User ID for the default admin
    private static final String DEFAULT_ADMIN_ID = "ADM_ADMIN_DEF";   // Admin table's own ID

    public static void initializeDatabaseSchema() {
        System.out.println("Attempting to initialize database schema...");
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("Executing CREATE_USERS_TABLE...");
            stmt.executeUpdate(CREATE_USERS_TABLE);
            System.out.println("Executing CREATE_ADMINS_TABLE...");
            stmt.executeUpdate(CREATE_ADMINS_TABLE);
            System.out.println("Executing CREATE_CATEGORIES_TABLE...");
            stmt.executeUpdate(CREATE_CATEGORIES_TABLE);
            System.out.println("Executing CREATE_PRODUCTS_TABLE...");
            stmt.executeUpdate(CREATE_PRODUCTS_TABLE);
            System.out.println("Executing CREATE_CART_TABLE...");
            stmt.executeUpdate(CREATE_CART_TABLE);
            System.out.println("Executing CREATE_ORDERS_TABLE...");
            stmt.executeUpdate(CREATE_ORDERS_TABLE);
            System.out.println("Executing CREATE_ORDER_DETAILS_TABLE...");
            stmt.executeUpdate(CREATE_ORDER_DETAILS_TABLE);
            System.out.println("Executing CREATE_FEEDBACK_TABLE...");
            stmt.executeUpdate(CREATE_FEEDBACK_TABLE);
            System.out.println("Executing CREATE_PAYMENTS_TABLE...");
            stmt.executeUpdate(CREATE_PAYMENTS_TABLE);
            System.out.println("Executing CREATE_PURCHASE_HISTORY_TABLE...");
            stmt.executeUpdate(CREATE_PURCHASE_HISTORY_TABLE);
            System.out.println("All tables created or already exist.");

            // Add default user and admin if they don't exist (idempotent)
            // For simplicity in a web app, we are not hashing passwords here.
            // In a real app, passwords should ALWAYS be hashed.
            String insertDefaultUser = String.format(
                "INSERT IGNORE INTO Users (UserID, FullName, Email, Password, Role) VALUES ('%s', 'Default User', 'user@example.com', 'password', 'USER')",
                DEFAULT_USER_ID);
            stmt.executeUpdate(insertDefaultUser);
            System.out.println("Default user ensured.");

            String insertDefaultAdminUser = String.format(
                "INSERT IGNORE INTO Users (UserID, FullName, Email, Password, Role) VALUES ('%s', 'Default Admin', 'admin@example.com', 'adminpass', 'ADMIN')",
                DEFAULT_ADMIN_USER_ID);
            stmt.executeUpdate(insertDefaultAdminUser);
            System.out.println("Default admin user ensured.");

            String insertDefaultAdminLink = String.format(
                "INSERT IGNORE INTO Admins (AdminID, UserID, Permissions) VALUES ('%s', '%s', 'ALL')",
                DEFAULT_ADMIN_ID, DEFAULT_ADMIN_USER_ID);
            stmt.executeUpdate(insertDefaultAdminLink);
            System.out.println("Default admin link ensured.");

            System.out.println("Database schema initialization complete.");

        } catch (SQLException e) {
            System.err.println("SQLException during database schema initialization: " + e.getMessage());
            e.printStackTrace();
            // Depending on the web app lifecycle, you might rethrow this or handle it
            // to prevent the app from starting if the DB is not set up.
            throw new RuntimeException("Failed to initialize database schema.", e);
        } catch (IllegalStateException e) {
            System.err.println("Database connection not configured. Cannot initialize schema: " + e.getMessage());
             throw new RuntimeException("Database connection not configured. Cannot initialize schema.", e);
        }
    }

     // Private constructor to prevent instantiation
    private DatabaseInitializer() {}
}
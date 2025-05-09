package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {

    // --- Connection Parameters - These will be "set" by a setup process (e.g., from a form/listener) ---
    // For now, we'll provide placeholders. In a real app, these would not be hardcoded here long-term.
    private static String JDBC_URL = ""; // e.g., "jdbc:mysql://localhost:3306/techtrovedb"
    private static String DB_USER = "";  // e.g., "root"
    private static String DB_PASSWORD = ""; // e.g., "your_password"
    private static boolean configured = false;

    // Load the MySQL JDBC driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // For MySQL Connector/J 8.x and later
            // Class.forName("com.mysql.jdbc.Driver"); // For MySQL Connector/J 5.x
            System.out.println("MySQL JDBC Driver Registered!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            // This is a critical error, the application won't be able to connect to DB
            throw new RuntimeException("MySQL JDBC Driver not found.", e);
        }
    }

    /**
     * Configures the database connection parameters.
     * This method should be called once during application startup.
     *
     * @param url       The JDBC URL for the database.
     * @param user      The database username.
     * @param password  The database password.
     * @param dbName    The database name.
     */
    public static void configureConnection(String host, String port, String user, String password, String dbName) {
        if (host == null || host.trim().isEmpty()) host = "localhost";
        if (port == null || port.trim().isEmpty()) port = "3306";
        // Example: jdbc:mysql://localhost:3306/techtrovedb?useSSL=false&serverTimezone=UTC
        DBUtil.JDBC_URL = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        DBUtil.DB_USER = user;
        DBUtil.DB_PASSWORD = password;
        DBUtil.configured = true;
        System.out.println("DBUtil configured: " + JDBC_URL);
    }

    /**
     * Establishes and returns a connection to the database.
     *
     * @return A Connection object.
     * @throws SQLException if a database access error occurs or the URL is null.
     * @throws IllegalStateException if the DBUtil has not been configured.
     */
    public static Connection getConnection() throws SQLException {
        if (!configured || JDBC_URL == null || JDBC_URL.trim().isEmpty()) {
            throw new IllegalStateException("Database connection parameters are not configured. Call DBUtil.configureConnection() first.");
        }
        return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWORD);
    }

    public static boolean isConfigured() {
        return configured;
    }

    // Private constructor to prevent instantiation
    private DBUtil() {
    }
}
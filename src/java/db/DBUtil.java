package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement; // Not strictly needed here, but often useful in DBUtil

public class DBUtil {

    private static String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    // Template for connecting to the server without specifying a DB (for CREATE DATABASE)
    private static String SERVER_URL_TEMPLATE = "jdbc:mysql://%s:%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    // Template for connecting to a specific database
    private static String DB_URL_TEMPLATE = "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    // Store the configured parameters
    private static String dbHost;
    private static String dbPort;
    private static String dbUser;
    private static String dbPassword;
    private static String dbName;

    private static String currentJdbcUrl = ""; // This will hold the full URL to the specific DB
    private static boolean configured = false;

    static {
        try {
            Class.forName(JDBC_DRIVER);
            System.out.println("MySQL JDBC Driver Registered!");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
            throw new RuntimeException("MySQL JDBC Driver not found.", e);
        }
    }

    /**
     * Configures the database connection parameters.
     * This method prepares DBUtil to connect to a specific database.
     * If dbName is provided, it fully configures for that DB.
     * If dbName is null/empty, it prepares for server-level connection (e.g., to create the DB).
     */
    public static void configureConnection(String host, String port, String user, String password, String name) {
        dbHost = (host == null || host.trim().isEmpty()) ? "localhost" : host.trim();
        dbPort = (port == null || port.trim().isEmpty()) ? "3306" : port.trim();
        dbUser = user; // User can be empty string for some configs
        dbPassword = password; // Password can be empty
        dbName = name; // dbName can be null initially if we need to create it

        if (dbName != null && !dbName.trim().isEmpty()) {
            currentJdbcUrl = String.format(DB_URL_TEMPLATE, dbHost, dbPort, dbName.trim());
            configured = true; // Fully configured for a specific database
            System.out.println("DBUtil configured for database: " + currentJdbcUrl);
        } else {
            // Configured for server connection, not a specific DB yet
            // This state is primarily used by getServerConnection and then finalized
            currentJdbcUrl = String.format(SERVER_URL_TEMPLATE, dbHost, dbPort);
            // 'configured' might be considered false here until dbName is set,
            // or true meaning "server connection is configured"
            // Let's keep it true to allow getServerConnection to work based on these details.
            configured = true;
            System.out.println("DBUtil configured for server (no specific DB name yet): " + currentJdbcUrl);
        }
    }

    /**
     * Establishes and returns a connection to the configured database.
     * Requires that dbName was previously set in configureConnection or setDatabaseNameAndFinalizeConfiguration.
     *
     * @return A Connection object to the specific database.
     * @throws SQLException if a database access error occurs.
     * @throws IllegalStateException if DBUtil is not fully configured for a specific database.
     */
    public static Connection getConnection() throws SQLException {
        if (!isConfigured()) { // isConfigured checks if dbName is also set
            throw new IllegalStateException("Database connection is not fully configured for a specific database (dbName missing). Call configureConnection() with a dbName or setDatabaseNameAndFinalizeConfiguration() first.");
        }
        if (currentJdbcUrl == null || currentJdbcUrl.trim().isEmpty() || !currentJdbcUrl.contains("/" + dbName)) {
             throw new IllegalStateException("JDBC URL is not correctly set for the database: " + dbName);
        }
        return DriverManager.getConnection(currentJdbcUrl, dbUser, dbPassword);
    }

    /**
     * Gets a connection to the MySQL server itself, without specifying a database name in the URL.
     * Useful for operations like CREATE DATABASE.
     * Uses the host, port, user, and password passed as arguments, NOT necessarily the ones stored in DBUtil fields,
     * to avoid issues if DBUtil is not yet fully configured or being reconfigured.
     *
     * @param host     The database server host.
     * @param port     The database server port.
     * @param user     The database username.
     * @param password The database password.
     * @return A Connection object to the MySQL server.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getServerConnection(String host, String port, String user, String password) throws SQLException { // Corrected line
        String serverOnlyUrl = String.format(SERVER_URL_TEMPLATE,
                (host == null || host.trim().isEmpty()) ? "localhost" : host.trim(),
                (port == null || port.trim().isEmpty()) ? "3306" : port.trim()
        );
        return DriverManager.getConnection(serverOnlyUrl, user, password);
    }


    /**
     * Checks if DBUtil is fully configured to connect to a specific database (i.e., dbName is set).
     * @return true if fully configured, false otherwise.
     */
    public static boolean isConfigured() {
        return configured && dbName != null && !dbName.trim().isEmpty();
    }

    /**
     * Resets all stored DBUtil configuration parameters.
     */
    public static void resetConfiguration() {
        configured = false;
        currentJdbcUrl = "";
        dbHost = null;
        dbPort = null;
        dbUser = null;
        dbPassword = null;
        dbName = null;
        System.out.println("DBUtil configuration has been reset.");
    }


    /**
     * Sets the database name and finalizes the JDBC URL for DBUtil.
     * This is typically called after the database has been created.
     * It assumes dbHost and dbPort were set by a prior call to configureConnection (even if dbName was initially null).
     * @param name The name of the database to connect to.
     * @throws IllegalArgumentException if name is null or empty.
     * @throws IllegalStateException if dbHost or dbPort were not previously set.
     */
    public static void setDatabaseNameAndFinalizeConfiguration(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Database name cannot be null or empty for final configuration.");
        }
        dbName = name.trim();

        if (dbHost != null && dbPort != null) {
             currentJdbcUrl = String.format(DB_URL_TEMPLATE, dbHost, dbPort, dbName);
             configured = true; // Ensures it's marked as fully configured now
             System.out.println("DBUtil configuration finalized for database: " + currentJdbcUrl);
        } else {
            // This case should ideally not be reached if configureConnection was called first
            throw new IllegalStateException("Cannot finalize DB configuration: host or port not set previously.");
        }
    }

    // Private constructor to prevent instantiation
    private DBUtil() {
    }
}
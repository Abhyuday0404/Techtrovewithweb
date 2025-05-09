// src/java/app/servlets/DatabaseSetupServlet.java
package app.servlets;

import db.DBUtil;
import db.DatabaseInitializer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "DatabaseSetupServlet", urlPatterns = {"/DatabaseSetupServlet"})
public class DatabaseSetupServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String host = request.getParameter("db_host");
        String port = request.getParameter("db_port");
        String dbNameParam = request.getParameter("db_name");
        String user = request.getParameter("db_user");
        String password = request.getParameter("db_password");

        ServletContext context = getServletContext();
        String redirectPage = "db_setup.jsp";

        if (host == null || host.trim().isEmpty() ||
            port == null || port.trim().isEmpty() ||
            dbNameParam == null || dbNameParam.trim().isEmpty() ||
            user == null || user.trim().isEmpty()) {
            response.sendRedirect(redirectPage + "?error=missing_params");
            return;
        }

        try {
            // 1. Connect to MySQL server (without specifying a database) to create the database
            System.out.println("DatabaseSetupServlet: Attempting to connect to MySQL server...");
            try (Connection serverConn = DBUtil.getServerConnection(host, port, user, password);
                 Statement stmt = serverConn.createStatement()) {
                System.out.println("DatabaseSetupServlet: Connected to MySQL server. Creating database if not exists: " + dbNameParam);
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbNameParam + "`");
                System.out.println("DatabaseSetupServlet: Database '" + dbNameParam + "' ensured/created.");
            } catch (SQLException e) {
                System.err.println("DatabaseSetupServlet: Error creating database '" + dbNameParam + "': " + e.getMessage());
                e.printStackTrace();
                request.setAttribute("dbErrorMsg", "Failed to create database: " + e.getMessage());
                DBUtil.resetConfiguration();
                context.setAttribute("dbConfigNeeded", true);
                response.sendRedirect(redirectPage + "?error=db_creation_failed");
                return;
            }

            // 2. Configure DBUtil to use the specified (and now existing) database
            System.out.println("DatabaseSetupServlet: Configuring DBUtil for database: " + dbNameParam);
            DBUtil.configureConnection(host, port, user, password, dbNameParam); // This sets up the full JDBC_URL

            // 3. Test connection to the specific database
            System.out.println("DatabaseSetupServlet: Attempting to test connection to database '" + dbNameParam + "'...");
            try (Connection dbConn = DBUtil.getConnection()) {
                if (dbConn != null && !dbConn.isClosed()) {
                    System.out.println("DatabaseSetupServlet: Connection test to database '" + dbNameParam + "' successful!");
                    context.setAttribute("dbConfigNeeded", false);

                    // 4. Initialize schema (create tables)
                    System.out.println("DatabaseSetupServlet: Initializing database schema...");
                    DatabaseInitializer.initializeDatabaseSchema(); // Uses DBUtil.getConnection()
                    System.out.println("DatabaseSetupServlet: Database schema initialized successfully.");
                    context.setAttribute("dbInitialized", true);
                    context.removeAttribute("dbInitializationFailed");
                    context.removeAttribute("dbInitializationError");

                    redirectPage = "db_setup.jsp?success=configured";
                } else {
                    throw new SQLException("Connection test failed (conn is null or closed after configuration).");
                }
            }

        } catch (SQLException e) {
            System.err.println("DatabaseSetupServlet: SQLException during setup: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("dbErrorMsg", "DB Setup Error: " + e.getMessage());
            DBUtil.resetConfiguration();
            context.setAttribute("dbConfigNeeded", true);
            redirectPage = "db_setup.jsp?error=connection_failed";
        } catch (IllegalStateException e) {
            System.err.println("DatabaseSetupServlet: IllegalStateException: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("dbErrorMsg", "Configuration Error: " + e.getMessage());
            DBUtil.resetConfiguration();
            context.setAttribute("dbConfigNeeded", true);
            redirectPage = "db_setup.jsp?error=config_error";
        } catch (RuntimeException e) { // Catch schema initialization failure
            System.err.println("DatabaseSetupServlet: RuntimeException (likely schema init failure): " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("dbErrorMsg", "Schema initialization failed: " + e.getMessage());
            // DBUtil might be configured, but schema failed
            context.setAttribute("dbConfigNeeded", false); // It was configured
            context.setAttribute("dbInitializationFailed", true);
            context.setAttribute("dbInitializationError", e.getMessage());
            redirectPage = "db_setup.jsp?error=init_failed";
        }

        System.out.println("DatabaseSetupServlet: Redirecting to " + redirectPage);
        response.sendRedirect(redirectPage);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("DatabaseSetupServlet: GET request, forwarding to db_setup.jsp");
        request.getRequestDispatcher("/db_setup.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Handles initial database connection setup, database creation, and schema initialization.";
    }
}
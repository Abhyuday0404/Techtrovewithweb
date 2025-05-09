package app.servlets;

import db.DBUtil;
import db.DatabaseInitializer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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
        String dbName = request.getParameter("db_name");
        String user = request.getParameter("db_user");
        String password = request.getParameter("db_password");

        ServletContext context = getServletContext();
        String redirectPage = "db_setup.jsp"; // Default back to setup page

        if (host == null || host.trim().isEmpty() ||
            port == null || port.trim().isEmpty() ||
            dbName == null || dbName.trim().isEmpty() ||
            user == null || user.trim().isEmpty()) {
            // Password can be empty for some MySQL setups, so not checking it for emptiness here explicitly as a requirement
            response.sendRedirect(redirectPage + "?error=missing_params");
            return;
        }

        try {
            System.out.println("DatabaseSetupServlet: Configuring DBUtil...");
            DBUtil.configureConnection(host, port, user, password, dbName);
            System.out.println("DatabaseSetupServlet: DBUtil configured. Attempting to test connection...");

            // Test connection
            try (Connection conn = DBUtil.getConnection()) {
                if (conn != null && !conn.isClosed()) {
                    System.out.println("DatabaseSetupServlet: Connection test successful!");
                    context.setAttribute("dbConfigNeeded", false); // Update context attribute

                    // Now attempt to initialize schema
                    System.out.println("DatabaseSetupServlet: Initializing database schema...");
                    DatabaseInitializer.initializeDatabaseSchema();
                    System.out.println("DatabaseSetupServlet: Database schema initialized successfully.");
                    context.setAttribute("dbInitialized", true);
                    context.removeAttribute("dbInitializationFailed"); // Clear any previous failure flag
                    context.removeAttribute("dbInitializationError");

                    // Redirect to setup page with success message, which will then redirect to login
                    redirectPage = "db_setup.jsp?success=configured";
                } else {
                    System.err.println("DatabaseSetupServlet: Connection test failed (conn is null or closed).");
                    request.setAttribute("dbErrorMsg", "Connection test failed. Connection object was null or closed.");
                    DBUtil.configureConnection(null,null,null,null,null); // Reset configuration on failure
                    context.setAttribute("dbConfigNeeded", true);
                    redirectPage = "db_setup.jsp?error=connection_failed";
                }
            }

        } catch (SQLException e) {
            System.err.println("DatabaseSetupServlet: SQLException during setup: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("dbErrorMsg", e.getMessage());
            DBUtil.configureConnection(null,null,null,null,null); // Reset configuration on failure
            context.setAttribute("dbConfigNeeded", true);
            redirectPage = "db_setup.jsp?error=connection_failed";
        } catch (IllegalStateException e) { // Catch if DBUtil was already configured differently or not at all
            System.err.println("DatabaseSetupServlet: IllegalStateException: " + e.getMessage());
             request.setAttribute("dbErrorMsg", e.getMessage());
            context.setAttribute("dbConfigNeeded", true);
            redirectPage = "db_setup.jsp?error=connection_failed";
        }
         catch (RuntimeException e) { // Catch schema initialization failure
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
        // Use sendRedirect for GET requests (clears form POST data)
        response.sendRedirect(redirectPage);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Forward to the JSP page if accessed via GET (e.g. direct URL or refresh)
        // This simply shows the form again. The main logic is in POST.
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
        return "Handles initial database connection setup and schema initialization.";
    }
}
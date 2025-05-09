package app.servlets.auth;

import models.User;
import db.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
// No need for java.sql here for the simplified login

@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet", "/login"})
public class LoginServlet extends HttpServlet {

    private final String DEFAULT_USER_ID = "USR_USER_DEF";
    private final String DEFAULT_ADMIN_ID = "USR_ADMIN_DEF";

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("LoginServlet: Received GET request.");
        HttpSession session = request.getSession(false); // Don't create if it doesn't exist

        // Handle logout message
        if ("true".equals(request.getParameter("logout"))) {
            request.setAttribute("logoutMessage", "You have been logged out successfully.");
        }

        // Handle error messages passed via session (e.g., from filter or failed POST)
        if (session != null) {
            String loginError = (String) session.getAttribute("loginError");
            if (loginError != null) {
                request.setAttribute("loginError", loginError);
                session.removeAttribute("loginError"); // Clear after displaying
            }
        }

        // Check if DB is configured. If not, redirect to setup.
        Boolean dbConfigNeeded = (Boolean) getServletContext().getAttribute("dbConfigNeeded");
        if (dbConfigNeeded != null && dbConfigNeeded) {
            System.out.println("LoginServlet: DB not configured, redirecting to db_setup.jsp");
            response.sendRedirect(request.getContextPath() + "/db_setup.jsp");
            return;
        }
        Boolean dbInitFailed = (Boolean) getServletContext().getAttribute("dbInitializationFailed");
        if (dbInitFailed != null && dbInitFailed) {
            System.out.println("LoginServlet: DB Init failed, redirecting to db_setup.jsp");
            response.sendRedirect(request.getContextPath() + "/db_setup.jsp?error=init_failed");
            return;
        }

        System.out.println("LoginServlet: Forwarding to /WEB-INF/jsp/login.jsp");
        request.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("LoginServlet: Received POST request (login attempt).");
        String role = request.getParameter("role");
        HttpSession session = request.getSession(true); // Create session if one doesn't exist for login

        if (role == null || role.isEmpty()) {
            session.setAttribute("loginError", "Please select a role.");
            response.sendRedirect(request.getContextPath() + "/LoginServlet"); // Use context path for safety
            return;
        }

        User loggedInUser = null;
        String targetDashboardServletPath = ""; // Path to the dashboard servlet

        if ("user".equalsIgnoreCase(role)) {
            loggedInUser = new User(DEFAULT_USER_ID, "Default User", "user@example.com", "password", null, null, User.UserRole.USER, null, null);
            targetDashboardServletPath = request.getContextPath() + "/UserDashboardServlet";
            System.out.println("LoginServlet: 'User' role selected. Default User ID: " + DEFAULT_USER_ID);
        } else if ("admin".equalsIgnoreCase(role)) {
            loggedInUser = new User(DEFAULT_ADMIN_ID, "Default Admin", "admin@example.com", "adminpass", null, null, User.UserRole.ADMIN, null, null);
            targetDashboardServletPath = request.getContextPath() + "/AdminDashboardServlet";
            System.out.println("LoginServlet: 'Admin' role selected. Default Admin ID: " + DEFAULT_ADMIN_ID);
        } else {
            session.setAttribute("loginError", "Invalid role selected.");
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        if (loggedInUser != null) {
            session.setAttribute("loggedInUser", loggedInUser);
            session.removeAttribute("loginError"); // Clear any previous errors

            // Check if there was a page the user was trying to access before being sent to login
            String redirectAfterLogin = (String) session.getAttribute("redirectAfterLogin");
            if (redirectAfterLogin != null && !redirectAfterLogin.isEmpty()) {
                session.removeAttribute("redirectAfterLogin"); // Clear it after use
                System.out.println("LoginServlet: Redirecting to originally requested URL: " + redirectAfterLogin);
                response.sendRedirect(redirectAfterLogin); // redirectAfterLogin should include context path
            } else {
                // Default redirect to the appropriate dashboard
                System.out.println("LoginServlet: User '" + loggedInUser.getUserId() + "' logged in. Redirecting to " + targetDashboardServletPath);
                response.sendRedirect(targetDashboardServletPath);
            }
        } else {
            session.setAttribute("loginError", "Login failed (user object not created).");
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
        }
    }
}
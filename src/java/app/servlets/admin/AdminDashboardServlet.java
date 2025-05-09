package app.servlets.admin;

import models.User; // For checking user role

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/AdminDashboardServlet", "/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("AdminDashboardServlet: Received GET request.");
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("loggedInUser") == null) {
            System.out.println("AdminDashboardServlet: No logged-in user. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser.getRole() != User.UserRole.ADMIN) {
            System.out.println("AdminDashboardServlet: User is not ADMIN. Redirecting to login (or user dashboard).");
            // Invalidate session or redirect to user dashboard if they are a regular user
            // For simplicity, redirecting to login with an error.
            session.setAttribute("loginError", "Access denied. Admin privileges required.");
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        request.setAttribute("adminUser", loggedInUser); // For display purposes on dashboard
        System.out.println("AdminDashboardServlet: Forwarding to admin dashboard for user: " + loggedInUser.getUserId());
        request.getRequestDispatcher("/WEB-INF/jsp/admin/dashboard.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Handle any POST actions from admin dashboard if needed in future
        doGet(request, response);
    }
}
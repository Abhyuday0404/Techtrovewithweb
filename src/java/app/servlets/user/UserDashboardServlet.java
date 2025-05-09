package app.servlets.user;

import models.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "UserDashboardServlet", urlPatterns = {"/UserDashboardServlet", "/user/dashboard"})
public class UserDashboardServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("UserDashboardServlet: Received GET request.");
        HttpSession session = request.getSession(false); // Don't create new session if not exists

        if (session == null || session.getAttribute("loggedInUser") == null) {
            System.out.println("UserDashboardServlet: No logged-in user found. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser.getRole() != User.UserRole.USER) {
            System.out.println("UserDashboardServlet: User is not of USER role. Redirecting to login.");
            // Or redirect to an appropriate error page or admin dashboard if they are admin
            session.invalidate(); // Log them out as a precaution
            response.sendRedirect(request.getContextPath() + "/LoginServlet?error=auth_failed");
            return;
        }

        // Set user object for the JSP to use (e.g., display name)
        request.setAttribute("user", loggedInUser);

        System.out.println("UserDashboardServlet: Forwarding to user dashboard JSP for user: " + loggedInUser.getUserId());
        request.getRequestDispatcher("/WEB-INF/jsp/user/dashboard.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Handle any POST actions from the user dashboard if needed in the future
        // For now, just redirect to GET
        doGet(request, response);
    }
}
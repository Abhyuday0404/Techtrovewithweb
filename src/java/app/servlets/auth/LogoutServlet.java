package app.servlets.auth;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LogoutServlet", urlPatterns = {"/LogoutServlet", "/logout"})
public class LogoutServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("LogoutServlet: Received GET request (logging out).");
        HttpSession session = request.getSession(false); // Get existing session, don't create new

        if (session != null) {
            String userId = "UnknownUser";
            if (session.getAttribute("loggedInUser") != null) {
                // Attempt to get user ID for logging, if User object exists
                try {
                    models.User user = (models.User) session.getAttribute("loggedInUser");
                    userId = user.getUserId();
                } catch (ClassCastException e) {
                    // Log if the object is not a User type, but still invalidate
                    System.err.println("LogoutServlet: Session attribute 'loggedInUser' was not of type models.User.");
                }
            }
            session.invalidate(); // Invalidate the session
            System.out.println("LogoutServlet: Session invalidated for user (approx): " + userId);
        } else {
            System.out.println("LogoutServlet: No active session found to invalidate.");
        }

        // Set a logout message for the login page
        // Using request attribute as session is now invalid
        // Better to use a query parameter for stateless message passing after logout
        System.out.println("LogoutServlet: Redirecting to LoginServlet with logout message.");
        response.sendRedirect(request.getContextPath() + "/LoginServlet?logout=true");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Logout should ideally always be a GET or a specific POST action,
        // but redirecting GET ensures it happens.
        doGet(request, response);
    }
}
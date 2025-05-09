package app.servlets.admin;

import managers.FeedbackManager;
import models.Feedback;
import models.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

@WebServlet(name = "AdminFeedbackServlet", urlPatterns = {"/AdminFeedbackServlet", "/admin/feedback"})
public class AdminFeedbackServlet extends HttpServlet {

    private FeedbackManager feedbackManager;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            feedbackManager = new FeedbackManager();
            System.out.println("AdminFeedbackServlet: FeedbackManager initialized.");
        } catch (SQLException e) {
            System.err.println("AdminFeedbackServlet: Failed to initialize FeedbackManager: " + e.getMessage());
            throw new ServletException("Failed to initialize FeedbackManager for admin feedback view", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("AdminFeedbackServlet: Received GET request.");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null || loggedInUser.getRole() != User.UserRole.ADMIN) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        String successMessage = (String) session.getAttribute("feedbackAdminSuccess"); // For future actions like delete
        String errorMessage = (String) session.getAttribute("feedbackAdminError");
        if(successMessage != null) request.setAttribute("successMessage", successMessage);
        if(errorMessage != null) request.setAttribute("errorMessage", errorMessage);
        session.removeAttribute("feedbackAdminSuccess");
        session.removeAttribute("feedbackAdminError");


        List<Feedback> allFeedback = new ArrayList<>();
        try {
            allFeedback = feedbackManager.getAllFeedbackWithDetails(); // Method that joins user and product names
            request.setAttribute("feedbackList", allFeedback);
            System.out.println("AdminFeedbackServlet: Fetched " + allFeedback.size() + " feedback entries.");
        } catch (SQLException e) {
            System.err.println("AdminFeedbackServlet: Error fetching all feedback: " + e.getMessage());
            request.setAttribute("errorMessage", "Database error fetching feedback: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/jsp/admin/feedback_view.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Handle POST actions if any (e.g., delete feedback in future)
        System.out.println("AdminFeedbackServlet: Received POST request.");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null || loggedInUser.getRole() != User.UserRole.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
            return;
        }

        String action = request.getParameter("action");
        String feedbackId = request.getParameter("feedbackId");

        if ("delete".equals(action) && feedbackId != null && !feedbackId.isEmpty()) {
            try {
                // boolean deleted = feedbackManager.deleteFeedback(feedbackId); // Assuming this method exists
                // if (deleted) {
                //     session.setAttribute("feedbackAdminSuccess", "Feedback ID " + feedbackId + " deleted successfully.");
                // } else {
                //     session.setAttribute("feedbackAdminError", "Could not delete feedback ID " + feedbackId + ". It might not exist.");
                // }
                session.setAttribute("feedbackAdminError", "Delete functionality for feedback is not yet fully implemented in this demo version.");

            } 
            // catch (SQLException e) {
            //    System.err.println("AdminFeedbackServlet: Error deleting feedback " + feedbackId + ": " + e.getMessage());
            //    session.setAttribute("feedbackAdminError", "Database error deleting feedback: " + e.getMessage());
            // }
            catch (Exception e) { // Catch any other unexpected errors
                System.err.println("AdminFeedbackServlet: Unexpected error during delete attempt for feedback " + feedbackId + ": " + e.getMessage());
                session.setAttribute("feedbackAdminError", "An unexpected error occurred: " + e.getMessage());
            }
        } else {
            session.setAttribute("feedbackAdminError", "Invalid action or missing feedback ID for POST request.");
        }
        response.sendRedirect(request.getContextPath() + "/AdminFeedbackServlet"); // Redirect to GET to show messages
    }
}
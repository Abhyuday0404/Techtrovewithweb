// src/java/app/servlets/user/UserFeedbackServlet.java
package app.servlets.user;

import managers.FeedbackManager;
import managers.OrderManager;
import models.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet(name = "UserFeedbackServlet", urlPatterns = {"/UserFeedbackServlet", "/user/feedback"})
public class UserFeedbackServlet extends HttpServlet {

    private FeedbackManager feedbackManager;
    private OrderManager orderManager;

    // Public static inner class for ProductSelectionItem
    public static class ProductSelectionItem {
        private final String productId;
        private final String displayName;

        public ProductSelectionItem(String productId, String displayName) {
            this.productId = productId;
            this.displayName = displayName;
        }
        public String getProductId() { return productId; }
        public String getDisplayName() { return displayName; }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            feedbackManager = new FeedbackManager();
            orderManager = new OrderManager();
        } catch (SQLException e) {
            throw new ServletException("Failed to initialize managers for UserFeedbackServlet", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }
        
        String successMessage = (String) session.getAttribute("feedbackSuccess");
        String errorMessage = (String) session.getAttribute("feedbackError");
        if (successMessage != null) {
            request.setAttribute("successMessage", successMessage);
            session.removeAttribute("feedbackSuccess");
        }
        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
            session.removeAttribute("feedbackError");
        }

        List<ProductSelectionItem> productSelectionList = new ArrayList<>();
        try {
            List<Map<String, String>> userOrderedProducts = orderManager.getDistinctOrderedProductsByUserId(loggedInUser.getUserId());
            for (Map<String, String> productData : userOrderedProducts) {
                productSelectionList.add(
                    new ProductSelectionItem(productData.get("productId"), productData.get("productName"))
                );
            }
            request.setAttribute("orderedProducts", productSelectionList);
        } catch (SQLException e) {
            request.setAttribute("errorMessage", "Could not load your ordered products for feedback. " + e.getMessage());
            System.err.println("Error fetching distinct ordered products for user " + loggedInUser.getUserId() + ": " + e.getMessage());
        }
        request.getRequestDispatcher("/WEB-INF/jsp/user/feedback_form.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        String productId = request.getParameter("productId");
        String ratingStr = request.getParameter("rating");
        String comment = request.getParameter("comment");

        if (productId == null || productId.trim().isEmpty() || ratingStr == null || ratingStr.isEmpty()) {
            session.setAttribute("feedbackError", "Product and Rating are required.");
            response.sendRedirect(request.getContextPath() + "/UserFeedbackServlet");
            return;
        }

        try {
            int rating = Integer.parseInt(ratingStr);
            if (rating < 1 || rating > 5) {
                 session.setAttribute("feedbackError", "Rating must be between 1 and 5.");
                 response.sendRedirect(request.getContextPath() + "/UserFeedbackServlet");
                 return;
            }
            feedbackManager.addFeedback(loggedInUser.getUserId(), productId, rating, comment);
            session.setAttribute("feedbackSuccess", "Thank you! Your feedback has been submitted.");
        } catch (NumberFormatException e) {
            session.setAttribute("feedbackError", "Invalid rating. Please select a star rating.");
        } catch (IllegalArgumentException | SQLException e) {
            session.setAttribute("feedbackError", "Error submitting feedback: " + e.getMessage());
             System.err.println("Error submitting feedback for user " + loggedInUser.getUserId() + ": " + e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/UserFeedbackServlet");
    }
}
package app.servlets.user;

import managers.FeedbackManager;
import managers.OrderManager; // To get list of ordered products
import models.Feedback;
import models.Order;
import models.OrderDetail;
import models.Product; // For ProductSelectionItem if you make one
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
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.stream.Collectors;


@WebServlet(name = "UserFeedbackServlet", urlPatterns = {"/UserFeedbackServlet", "/user/feedback"})
public class UserFeedbackServlet extends HttpServlet {

    private FeedbackManager feedbackManager;
    private OrderManager orderManager;
    // private ProductManager productManager; // If fetching product names directly for dropdown

    // Helper class for dropdown items
    public static class ProductSelectionItem {
        private String productId;
        private String displayName;
        public ProductSelectionItem(String productId, String displayName) { this.productId = productId; this.displayName = displayName; }
        public String getProductId() { return productId; }
        public String getDisplayName() { return displayName; }
    }


    @Override
    public void init() throws ServletException {
        super.init();
        try {
            feedbackManager = new FeedbackManager();
            orderManager = new OrderManager();
            // productManager = new ProductManager();
            System.out.println("UserFeedbackServlet: Managers initialized.");
        } catch (SQLException e) {
            System.err.println("UserFeedbackServlet: Failed to initialize managers: " + e.getMessage());
            throw new ServletException("Failed to initialize managers for feedback", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("UserFeedbackServlet: Received GET request.");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        // Get session messages for display
        String successMessage = (String) session.getAttribute("feedbackSuccess");
        String errorMessage = (String) session.getAttribute("feedbackError");
        if (successMessage != null) request.setAttribute("successMessage", successMessage);
        if (errorMessage != null) request.setAttribute("errorMessage", errorMessage);
        session.removeAttribute("feedbackSuccess");
        session.removeAttribute("feedbackError");

        // Fetch products the user has ordered to populate the dropdown
        List<ProductSelectionItem> orderedProductsForDropdown = new ArrayList<>();
        try {
            List<Order> userOrders = orderManager.getOrdersByUserId(loggedInUser.getUserId());
            // Use a Map to collect unique products by ID to avoid duplicates in dropdown
            Map<String, String> uniqueProductsMap = new HashMap<>();

            for (Order order : userOrders) {
                if (order.getOrderDetails() != null) {
                    for (OrderDetail detail : order.getOrderDetails()) {
                        if (detail.getProductId() != null && detail.getProductName() != null) {
                            // Key: ProductID, Value: ProductName (details.getProductName already fetched by OrderManager)
                            uniqueProductsMap.putIfAbsent(detail.getProductId(), detail.getProductName());
                        }
                    }
                }
            }
            // Convert map to list of ProductSelectionItem
            for(Map.Entry<String, String> entry : uniqueProductsMap.entrySet()){
                orderedProductsForDropdown.add(new ProductSelectionItem(entry.getKey(), entry.getValue() + " (ID: " + entry.getKey() + ")"));
            }
            // Sort for better UX
            orderedProductsForDropdown.sort(Comparator.comparing(ProductSelectionItem::getDisplayName));

        } catch (SQLException e) {
            System.err.println("UserFeedbackServlet: Error fetching ordered products for user " + loggedInUser.getUserId() + ": " + e.getMessage());
            request.setAttribute("errorMessage", "Could not load products for feedback. " + (errorMessage == null ? "" : errorMessage));
        }

        request.setAttribute("orderedProducts", orderedProductsForDropdown);
        request.getRequestDispatcher("/WEB-INF/jsp/user/feedback_form.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("UserFeedbackServlet: Received POST request (submit feedback).");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        String productId = request.getParameter("productId");
        String ratingStr = request.getParameter("rating");
        String comment = request.getParameter("comment");

        if (productId == null || productId.isEmpty() || ratingStr == null || ratingStr.isEmpty()) {
            session.setAttribute("feedbackError", "Product and Rating are required.");
            response.sendRedirect(request.getContextPath() + "/UserFeedbackServlet");
            return;
        }

        try {
            int rating = Integer.parseInt(ratingStr);
            feedbackManager.addFeedback(loggedInUser.getUserId(), productId, rating, comment);
            session.setAttribute("feedbackSuccess", "Thank you! Your feedback has been submitted.");
            System.out.println("UserFeedbackServlet: Feedback submitted for product " + productId + " by user " + loggedInUser.getUserId());
        } catch (NumberFormatException e) {
            session.setAttribute("feedbackError", "Invalid rating value.");
            System.err.println("UserFeedbackServlet: Invalid rating format: " + ratingStr);
        } catch (IllegalArgumentException e) {
            session.setAttribute("feedbackError", "Submission error: " + e.getMessage());
            System.err.println("UserFeedbackServlet: Illegal argument: " + e.getMessage());
        } catch (SQLException e) {
            session.setAttribute("feedbackError", "Database error submitting feedback. Please try again. " + e.getMessage());
            System.err.println("UserFeedbackServlet: SQL error submitting feedback: " + e.getMessage());
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/UserFeedbackServlet"); // Redirect to GET to show messages and refresh form
    }
}
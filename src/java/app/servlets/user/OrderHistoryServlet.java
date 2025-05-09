package app.servlets.user;

import managers.OrderManager;
import models.Order;
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
import java.util.ArrayList; // Import

@WebServlet(name = "OrderHistoryServlet", urlPatterns = {"/OrderHistoryServlet", "/user/orders"})
public class OrderHistoryServlet extends HttpServlet {

    private OrderManager orderManager;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            orderManager = new OrderManager();
            System.out.println("OrderHistoryServlet: OrderManager initialized.");
        } catch (SQLException e) {
            System.err.println("OrderHistoryServlet: Failed to initialize OrderManager: " + e.getMessage());
            throw new ServletException("Failed to initialize OrderManager for order history", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("OrderHistoryServlet: Received GET request.");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            System.out.println("OrderHistoryServlet: User not logged in. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        String action = request.getParameter("action");
        if ("confirmation".equals(action)) {
            String lastOrderId = (String) session.getAttribute("lastOrderId");
            if (lastOrderId != null) {
                request.setAttribute("orderSuccessMessage", "Your order (ID: " + lastOrderId + ") has been placed successfully!");
                 // Optionally fetch the order details for display on confirmation
                try {
                    Order confirmedOrder = orderManager.getOrderById(lastOrderId);
                    request.setAttribute("confirmedOrder", confirmedOrder);
                } catch (SQLException e) {
                    System.err.println("OrderHistoryServlet: Error fetching confirmed order " + lastOrderId + ": " + e.getMessage());
                    // Continue to show history, confirmation message will still appear
                }
                // session.removeAttribute("lastOrderId"); // Remove after displaying once, or keep if needed
            }
            // Fall through to display order history along with confirmation
        }
        
        // Clear any other session messages
        String orderSuccess = (String) session.getAttribute("orderSuccessMessage");
        if (orderSuccess != null && !"confirmation".equals(action)) { // Avoid double setting if already set by confirmation
             request.setAttribute("orderSuccessMessage", orderSuccess);
             session.removeAttribute("orderSuccessMessage");
        }


        List<Order> orders = new ArrayList<>();
        try {
            orders = orderManager.getOrdersByUserId(loggedInUser.getUserId());
            System.out.println("OrderHistoryServlet: Fetched " + orders.size() + " orders for user " + loggedInUser.getUserId());
        } catch (SQLException e) {
            System.err.println("OrderHistoryServlet: Error fetching order history for user " + loggedInUser.getUserId() + ": " + e.getMessage());
            request.setAttribute("errorMessage", "Could not load your order history. Please try again later.");
        }

        request.setAttribute("orders", orders);
        request.getRequestDispatcher("/WEB-INF/jsp/user/order_history.jsp").forward(request, response);
    }

    // doPost might be used if we want to view details of a specific order from this page via a form submission
    // For now, doGet handles display.
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
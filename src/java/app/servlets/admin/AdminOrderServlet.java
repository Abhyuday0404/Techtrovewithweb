package app.servlets.admin;

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
import java.util.ArrayList;

@WebServlet(name = "AdminOrderServlet", urlPatterns = {"/AdminOrderServlet", "/admin/orders"})
public class AdminOrderServlet extends HttpServlet {

    private OrderManager orderManager;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            orderManager = new OrderManager();
            System.out.println("AdminOrderServlet: OrderManager initialized.");
        } catch (SQLException e) {
            System.err.println("AdminOrderServlet: Failed to initialize OrderManager: " + e.getMessage());
            throw new ServletException("Failed to initialize OrderManager for admin order view", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("AdminOrderServlet: Received GET request.");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null || loggedInUser.getRole() != User.UserRole.ADMIN) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        String successMessage = (String) session.getAttribute("orderAdminSuccess"); // For future actions
        String errorMessage = (String) session.getAttribute("orderAdminError");
        if(successMessage != null) request.setAttribute("successMessage", successMessage);
        if(errorMessage != null) request.setAttribute("errorMessage", errorMessage);
        session.removeAttribute("orderAdminSuccess");
        session.removeAttribute("orderAdminError");

        List<Order> allOrders = new ArrayList<>();
        try {
            allOrders = orderManager.getAllOrders(); // Fetch all orders
            request.setAttribute("orders", allOrders);
            System.out.println("AdminOrderServlet: Fetched " + allOrders.size() + " total orders.");
        } catch (SQLException e) {
            System.err.println("AdminOrderServlet: Error fetching all orders: " + e.getMessage());
            request.setAttribute("errorMessage", "Database error fetching orders: " + e.getMessage());
        }

        request.getRequestDispatcher("/WEB-INF/jsp/admin/order_view.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Handle POST actions if any (e.g., update order status in future)
        System.out.println("AdminOrderServlet: Received POST request. Redirecting to GET.");
        doGet(request, response);
    }
}
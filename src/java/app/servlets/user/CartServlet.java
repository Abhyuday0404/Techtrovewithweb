package app.servlets.user;

import managers.CartManager;
import models.CartItem;
import models.User;
import exceptions.InvalidProductIdException;
import exceptions.NoQuantityLeftException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList; // Import ArrayList

@WebServlet(name = "CartServlet", urlPatterns = {"/CartServlet", "/cart"})
public class CartServlet extends HttpServlet {

    private CartManager cartManager;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            cartManager = new CartManager();
            System.out.println("CartServlet: CartManager initialized.");
        } catch (SQLException e) {
            System.err.println("CartServlet: Failed to initialize CartManager: " + e.getMessage());
            throw new ServletException("Failed to initialize CartManager", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("CartServlet: Received GET request.");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            System.out.println("CartServlet: User not logged in. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        String errorMessage = (String) session.getAttribute("cartError");
        String successMessage = (String) session.getAttribute("cartSuccess");
        if (errorMessage != null) request.setAttribute("errorMessage", errorMessage);
        if (successMessage != null) request.setAttribute("successMessage", successMessage);
        session.removeAttribute("cartError");
        session.removeAttribute("cartSuccess");


        List<CartItem> cartItems = new ArrayList<>(); // Initialize to empty
        double totalAmount = 0.0;

        try {
            cartItems = cartManager.getCartItems(loggedInUser.getUserId());
            for (CartItem item : cartItems) {
                totalAmount += item.getSubtotal();
            }
            System.out.println("CartServlet: Fetched " + cartItems.size() + " items for user " + loggedInUser.getUserId() + ". Total: " + totalAmount);
        } catch (SQLException e) {
            System.err.println("CartServlet: SQL error fetching cart items for user " + loggedInUser.getUserId() + ": " + e.getMessage());
            request.setAttribute("errorMessage", "Error loading your cart. Please try again later.");
        }

        request.setAttribute("cartItems", cartItems);
        request.setAttribute("totalAmount", totalAmount);
        request.getRequestDispatcher("/WEB-INF/jsp/user/cart.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("CartServlet: Received POST request.");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            System.out.println("CartServlet POST: User not logged in. Responding with error/redirect.");
            // For AJAX, might send JSON error. For form, redirect.
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        String action = request.getParameter("action");
        String productId = request.getParameter("productId");
        String cartId = request.getParameter("cartId"); // For update/remove

        System.out.println("CartServlet POST: Action='" + action + "', ProductID='" + productId + "', CartID='" + cartId + "' for User: " + loggedInUser.getUserId());


        try {
            if ("add".equals(action) && productId != null) {
                int quantity = 1; // Default quantity
                String quantityStr = request.getParameter("quantity");
                if (quantityStr != null && !quantityStr.isEmpty()) {
                    try {
                        quantity = Integer.parseInt(quantityStr);
                        if (quantity < 1) quantity = 1; // Ensure positive quantity
                    } catch (NumberFormatException e) {
                        System.err.println("CartServlet: Invalid quantity format for add: " + quantityStr);
                        session.setAttribute("cartError", "Invalid quantity specified.");
                        // quantity remains 1
                    }
                }
                cartManager.addToCart(loggedInUser.getUserId(), productId, quantity);
                session.setAttribute("cartSuccess", "Product added to cart!");
                System.out.println("CartServlet: Product " + productId + " (qty " + quantity + ") added for user " + loggedInUser.getUserId());

            } else if ("update".equals(action) && cartId != null) {
                String quantityStr = request.getParameter("quantity");
                if (quantityStr != null && !quantityStr.isEmpty()) {
                    int newQuantity = Integer.parseInt(quantityStr);
                    cartManager.updateQuantity(loggedInUser.getUserId(), cartId, newQuantity);
                    session.setAttribute("cartSuccess", "Cart updated successfully.");
                    System.out.println("CartServlet: Cart item " + cartId + " updated to qty " + newQuantity + " for user " + loggedInUser.getUserId());
                } else {
                     session.setAttribute("cartError", "Quantity not provided for update.");
                }
            } else if ("remove".equals(action) && cartId != null) {
                cartManager.removeFromCart(loggedInUser.getUserId(), cartId);
                session.setAttribute("cartSuccess", "Product removed from cart.");
                System.out.println("CartServlet: Cart item " + cartId + " removed for user " + loggedInUser.getUserId());
            } else {
                System.out.println("CartServlet: Invalid or missing action/parameters.");
                session.setAttribute("cartError", "Invalid cart operation.");
            }
        } catch (NoQuantityLeftException e) {
            System.err.println("CartServlet (NoQuantityLeftException): " + e.getMessage());
            session.setAttribute("cartError", e.getMessage());
        } catch (InvalidProductIdException e) {
            System.err.println("CartServlet (InvalidProductIdException): " + e.getMessage());
            session.setAttribute("cartError", "Invalid product specified: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("CartServlet (SQLException): " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("cartError", "A database error occurred while updating your cart. Please try again.");
        } catch (IllegalArgumentException e) {
            System.err.println("CartServlet (IllegalArgumentException): " + e.getMessage());
            session.setAttribute("cartError", "Invalid input for cart operation: " + e.getMessage());
        } catch (Exception e) { // Catch-all for unexpected issues
            System.err.println("CartServlet (Unexpected Exception): " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("cartError", "An unexpected error occurred. Please try again.");
        }

        response.sendRedirect(request.getContextPath() + "/CartServlet"); // Redirect back to cart page to show changes/errors
    }
}
package app.servlets.user;

import managers.CartManager;
import models.CartItem;
import models.User;
import exceptions.InvalidQuantityException;
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

@WebServlet(name = "CartServlet", urlPatterns = {"/CartServlet", "/cart"})
public class CartServlet extends HttpServlet {

    private CartManager cartManager;

    @Override
    public void init() throws ServletException {
        try {
            cartManager = new CartManager();
            System.out.println("CartServlet: CartManager initialized.");
        } catch (SQLException e) {
            System.err.println("CartServlet: Error initializing CartManager: " + e.getMessage());
            throw new ServletException("Error initializing CartManager", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("CartServlet: Received GET request.");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        try {
            List<CartItem> cartItems = cartManager.getCartItems(loggedInUser.getUserId());
            double totalAmount = cartManager.calculateTotal(cartItems);

            request.setAttribute("cartItems", cartItems);
            request.setAttribute("totalAmount", totalAmount);

            // Forward any messages from POST operations
            String successMessage = (String) session.getAttribute("cartSuccess");
            String errorMessage = (String) session.getAttribute("cartError");
            if (successMessage != null) {
                request.setAttribute("cartSuccess", successMessage);
                session.removeAttribute("cartSuccess");
            }
            if (errorMessage != null) {
                request.setAttribute("cartError", errorMessage);
                session.removeAttribute("cartError");
            }
            System.out.println("CartServlet: Fetched " + cartItems.size() + " items for user " + loggedInUser.getUserId() + ". Total: " + totalAmount);
            request.getRequestDispatcher("/WEB-INF/jsp/user/cart.jsp").forward(request, response);

        } catch (SQLException e) {
            System.err.println("CartServlet GET: SQL error fetching cart: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("cartError", "Could not load your cart. Please try again later.");
            request.getRequestDispatcher("/WEB-INF/jsp/user/cart.jsp").forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("CartServlet: Received POST request.");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in.");
            return;
        }

        String action = request.getParameter("action");
        String productId = request.getParameter("productId"); // Used for 'add'
        String cartId = request.getParameter("cartId");       // Used for 'update' and 'remove'

        System.out.println("CartServlet POST: Action='" + action + "', ProductID='" + productId + "', CartID='" + cartId + "' for User: " + loggedInUser.getUserId());


        try {
            if ("add".equals(action) && productId != null && !productId.isEmpty()) {
                String quantityStr = request.getParameter("quantity");
                int quantity = 1; // Default quantity
                if (quantityStr != null && !quantityStr.isEmpty()) {
                    try {
                        quantity = Integer.parseInt(quantityStr);
                    } catch (NumberFormatException e) {
                        session.setAttribute("cartError", "Invalid quantity format.");
                        response.sendRedirect(request.getContextPath() + "/ProductServlet"); // Or back to product page
                        return;
                    }
                }
                // Using the method name from the CartManager I provided
                cartManager.addItemToCart(loggedInUser.getUserId(), productId, quantity);
                session.setAttribute("cartSuccess", "Product added to cart successfully!");
                System.out.println("CartServlet: Product " + productId + " (qty " + quantity + ") added for user " + loggedInUser.getUserId());

            } else if ("update".equals(action) && cartId != null && !cartId.isEmpty()) {
                String quantityStr = request.getParameter("quantity");
                if (quantityStr != null && !quantityStr.isEmpty()) {
                    int newQuantity = Integer.parseInt(quantityStr);
                    if (newQuantity > 0) {
                        // Using the method name from the CartManager I provided
                        cartManager.updateCartItemQuantity(cartId, newQuantity);
                        session.setAttribute("cartSuccess", "Cart updated successfully.");
                        System.out.println("CartServlet: Cart item " + cartId + " updated to qty " + newQuantity + " for user " + loggedInUser.getUserId());
                    } else { // Quantity is 0 or less, treat as remove
                        cartManager.removeItemFromCart(cartId);
                        session.setAttribute("cartSuccess", "Product removed from cart.");
                        System.out.println("CartServlet: Cart item " + cartId + " removed (qty <= 0) for user " + loggedInUser.getUserId());
                    }
                } else {
                    session.setAttribute("cartError", "Quantity not provided for update.");
                }

            } else if ("remove".equals(action) && cartId != null && !cartId.isEmpty()) {
                // Using the method name from the CartManager I provided
                cartManager.removeItemFromCart(cartId);
                session.setAttribute("cartSuccess", "Product removed from cart.");
                System.out.println("CartServlet: Cart item " + cartId + " removed for user " + loggedInUser.getUserId());

            } else {
                System.out.println("CartServlet: Invalid or missing action/parameters.");
                session.setAttribute("cartError", "Invalid cart operation.");
            }
        } catch (NoQuantityLeftException e) {
            System.err.println("CartServlet (NoQuantityLeftException): " + e.getMessage());
            session.setAttribute("cartError", e.getMessage());
        } catch (InvalidQuantityException e) {
            System.err.println("CartServlet (InvalidQuantityException): " + e.getMessage());
            session.setAttribute("cartError", e.getMessage());
        } catch (SQLException e) {
            System.err.println("CartServlet (SQLException): " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("cartError", "A database error occurred while updating your cart. Please try again.");
        } catch (NumberFormatException e) {
            System.err.println("CartServlet (NumberFormatException): Invalid quantity format - " + e.getMessage());
            session.setAttribute("cartError", "Invalid quantity provided.");
        } catch (Exception e) { // Catch-all for unexpected issues
            System.err.println("CartServlet (Unexpected Exception): " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("cartError", "An unexpected error occurred.");
        }

        // Redirect back to the cart page to show results/messages for POST actions
        response.sendRedirect(request.getContextPath() + "/CartServlet");
    }
}
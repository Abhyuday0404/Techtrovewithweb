package app.servlets.user;

import managers.CartManager;
import managers.OrderManager;
import models.CartItem;
import models.User;
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
import java.util.ArrayList; // Import

@WebServlet(name = "CheckoutServlet", urlPatterns = {"/CheckoutServlet", "/checkout"})
public class CheckoutServlet extends HttpServlet {

    private CartManager cartManager;
    private OrderManager orderManager;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            cartManager = new CartManager();
            orderManager = new OrderManager();
            System.out.println("CheckoutServlet: Managers initialized.");
        } catch (SQLException e) {
            System.err.println("CheckoutServlet: Failed to initialize managers: " + e.getMessage());
            throw new ServletException("Failed to initialize managers for checkout", e);
        }
    }

    // GET: Display checkout page with cart summary
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("CheckoutServlet: Received GET request.");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            System.out.println("CheckoutServlet GET: User not logged in. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        List<CartItem> cartItems = new ArrayList<>();
        double totalAmount = 0.0;
        String errorMessage = (String) session.getAttribute("checkoutError"); // Get error from POST if any
        if(errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
            session.removeAttribute("checkoutError");
        }


        try {
            cartItems = cartManager.getCartItems(loggedInUser.getUserId());
            if (cartItems.isEmpty()) {
                System.out.println("CheckoutServlet GET: Cart is empty for user " + loggedInUser.getUserId() + ". Redirecting to cart page.");
                session.setAttribute("cartError", "Your cart is empty. Cannot proceed to checkout.");
                response.sendRedirect(request.getContextPath() + "/CartServlet");
                return;
            }
            for (CartItem item : cartItems) {
                totalAmount += item.getSubtotal();
            }
        } catch (SQLException e) {
            System.err.println("CheckoutServlet GET: Error fetching cart items: " + e.getMessage());
            request.setAttribute("errorMessage", "Error retrieving your cart for checkout. Please try again.");
            // cartItems will be empty, JSP should handle this
        }

        request.setAttribute("cartItems", cartItems);
        request.setAttribute("totalAmount", totalAmount);
        System.out.println("CheckoutServlet GET: Forwarding to checkout.jsp for user " + loggedInUser.getUserId());
        request.getRequestDispatcher("/WEB-INF/jsp/user/checkout.jsp").forward(request, response);
    }

    // POST: Process the order placement
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("CheckoutServlet: Received POST request (place order).");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            System.out.println("CheckoutServlet POST: User not logged in. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        // Confirm action (e.g., if there's a confirm button with a specific name/value)
        // String confirmOrder = request.getParameter("confirmOrder");
        // if (confirmOrder == null) {
        //     session.setAttribute("checkoutError", "Order confirmation missing.");
        //     response.sendRedirect(request.getContextPath() + "/CheckoutServlet"); // Back to GET
        //     return;
        // }


        List<CartItem> cartItems;
        double totalAmount = 0.0;

        try {
            cartItems = cartManager.getCartItems(loggedInUser.getUserId());
            if (cartItems.isEmpty()) {
                System.out.println("CheckoutServlet POST: Cart became empty before placing order. Redirecting to cart.");
                session.setAttribute("cartError", "Your cart is empty. Order not placed.");
                response.sendRedirect(request.getContextPath() + "/CartServlet");
                return;
            }
            for (CartItem item : cartItems) {
                totalAmount += item.getSubtotal();
            }

            // Attempt to place the order
            String orderId = orderManager.placeOrder(loggedInUser.getUserId(), cartItems, totalAmount);
            System.out.println("CheckoutServlet POST: Order placed successfully for user " + loggedInUser.getUserId() + ". Order ID: " + orderId);

            session.setAttribute("orderSuccessMessage", "Your order (ID: " + orderId + ") has been placed successfully! Thank you for shopping with TechTrove.");
            session.setAttribute("lastOrderId", orderId); // For displaying on order history or confirmation page
            response.sendRedirect(request.getContextPath() + "/OrderHistoryServlet?action=confirmation"); // Redirect to order history or a confirmation page

        } catch (NoQuantityLeftException e) {
            System.err.println("CheckoutServlet POST: NoQuantityLeftException: " + e.getMessage());
            session.setAttribute("checkoutError", "Order failed: " + e.getMessage() + " Please update your cart.");
            response.sendRedirect(request.getContextPath() + "/CartServlet"); // Send to cart to fix quantities
        } catch (SQLException e) {
            System.err.println("CheckoutServlet POST: SQLException during order placement: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("checkoutError", "A database error occurred while placing your order. Please try again.");
            response.sendRedirect(request.getContextPath() + "/CheckoutServlet"); // Back to GET (checkout summary)
        } catch (IllegalArgumentException e) {
            System.err.println("CheckoutServlet POST: IllegalArgumentException: " + e.getMessage());
            session.setAttribute("checkoutError", "Invalid data for order: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/CheckoutServlet");
        } catch (Exception e) {
            System.err.println("CheckoutServlet POST: Unexpected error during order placement: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("checkoutError", "An unexpected error occurred. Please try again later.");
            response.sendRedirect(request.getContextPath() + "/CheckoutServlet");
        }
    }
}
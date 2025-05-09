// src/java/app/servlets/user/CheckoutServlet.java
package app.servlets.user;

import managers.CartManager;
import managers.OrderManager;
import managers.PaymentManager;
import models.CartItem;
import models.User;
import models.OrderDetail; // Ensure this is imported

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
// No specific need for LocalDateTime here unless for future shipping date estimates etc.

@WebServlet(name = "CheckoutServlet", urlPatterns = {"/CheckoutServlet", "/checkout"})
public class CheckoutServlet extends HttpServlet {

    private CartManager cartManager;
    private OrderManager orderManager;
    private PaymentManager paymentManager;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            cartManager = new CartManager();
            orderManager = new OrderManager();
            paymentManager = new PaymentManager();
            System.out.println("CheckoutServlet: Managers initialized.");
        } catch (SQLException e) {
            System.err.println("CheckoutServlet: Failed to initialize managers: " + e.getMessage());
            throw new ServletException("Failed to initialize managers for CheckoutServlet", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("CheckoutServlet: Received GET request.");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            System.out.println("CheckoutServlet: User not logged in. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        // Clear any previous checkout errors from session if user revisits GET
        String checkoutError = (String) session.getAttribute("checkoutError");
        if (checkoutError != null) {
            request.setAttribute("errorMessage", checkoutError); // Display it once
            session.removeAttribute("checkoutError");          // Clear from session
        }


        try {
            // Corrected method call: getCartItems instead of getCartItemsByUserId
            List<CartItem> cartItems = cartManager.getCartItems(loggedInUser.getUserId());
            if (cartItems.isEmpty()) {
                System.out.println("CheckoutServlet: Cart is empty. Redirecting to cart page.");
                request.setAttribute("errorMessage", "Your cart is empty. Add items before checking out.");
                // Forward to cart page if empty, so they see message and cart.
                request.getRequestDispatcher("/WEB-INF/jsp/user/cart.jsp").forward(request, response);
                return;
            }
            double totalAmount = cartManager.calculateTotal(cartItems);

            request.setAttribute("cartItems", cartItems);
            request.setAttribute("totalAmount", totalAmount);
            System.out.println("CheckoutServlet: Forwarding to checkout.jsp with " + cartItems.size() + " items, total: " + totalAmount);
            request.getRequestDispatcher("/WEB-INF/jsp/user/checkout.jsp").forward(request, response);

        } catch (SQLException e) {
            System.err.println("CheckoutServlet: SQL error during GET: " + e.getMessage());
            request.setAttribute("errorMessage", "Error retrieving cart for checkout: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/jsp/user/cart.jsp").forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("CheckoutServlet: Received POST request (placing order).");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            System.out.println("CheckoutServlet: User not logged in for POST. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        try {
            // Corrected method call: getCartItems instead of getCartItemsByUserId
            List<CartItem> cartItems = cartManager.getCartItems(loggedInUser.getUserId());
            if (cartItems.isEmpty()) {
                System.out.println("CheckoutServlet: Cart is empty on POST. Redirecting to CartServlet.");
                session.setAttribute("cartError", "Your cart is empty. Cannot place order.");
                response.sendRedirect(request.getContextPath() + "/CartServlet");
                return;
            }

            double totalAmount = cartManager.calculateTotal(cartItems);
            // For this demo, shipping address can be a placeholder or fetched from User model if available
            String shippingAddress = (loggedInUser.getAddress() != null && !loggedInUser.getAddress().isEmpty())
                                     ? loggedInUser.getAddress()
                                     : "Default Shipping Address - Placeholder";

            // Convert List<CartItem> to List<OrderDetail>
            List<OrderDetail> orderDetails = new ArrayList<>();
            for (CartItem cartItem : cartItems) {
                orderDetails.add(new OrderDetail(
                    null, // OrderDetailID will be generated by OrderManager/DB
                    null, // OrderID will be set by OrderManager
                    cartItem.getProduct().getProductId(),
                    cartItem.getProduct().getName(),    // Product name at the time of order
                    cartItem.getQuantity(),
                    cartItem.getProduct().getPrice()    // Price at the time of order
                ));
            }

            // Call the corrected method in OrderManager
            String orderId = orderManager.createOrder(loggedInUser.getUserId(), orderDetails, totalAmount, shippingAddress);

            System.out.println("CheckoutServlet: Order creation attempted. Order ID: " + orderId);

            // If order placed successfully:
            // 1. Record payment (simulated)
            paymentManager.recordPayment(orderId, totalAmount, "Cash on Delivery (Demo)");
            System.out.println("CheckoutServlet: Payment recorded for order " + orderId);

            // 2. Clear the user's cart
            cartManager.clearCart(loggedInUser.getUserId());
            System.out.println("CheckoutServlet: Cart cleared for user " + loggedInUser.getUserId());

            // 3. Redirect to order confirmation/history page
            session.setAttribute("lastOrderId", orderId); // For potential use on confirmation page
            session.setAttribute("orderSuccessMessage", "Your order (ID: " + orderId + ") has been placed successfully!");
            System.out.println("CheckoutServlet: Redirecting to OrderHistoryServlet for confirmation.");
            response.sendRedirect(request.getContextPath() + "/OrderHistoryServlet?action=confirmation");

        } catch (SQLException e) {
            System.err.println("CheckoutServlet: SQL Error during order placement: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("checkoutError", "Failed to place your order due to a database error. Please try again. Details: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/CheckoutServlet"); // Redirect back to GET to show checkout page with error
        } catch (Exception e) { // Catch any other unexpected errors
            System.err.println("CheckoutServlet: Unexpected error during order placement: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("checkoutError", "An unexpected error occurred while placing your order: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/CheckoutServlet");
        }
    }
}
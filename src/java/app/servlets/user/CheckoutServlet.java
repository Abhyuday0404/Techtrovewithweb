// src/java/app/servlets/user/CheckoutServlet.java
package app.servlets.user;

import managers.CartManager;
import managers.OrderManager;
import managers.PaymentManager;
import models.CartItem;
import models.User;
import exceptions.NoQuantityLeftException;
import exceptions.InvalidQuantityException; // If CartManager methods throw this

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

@WebServlet(name = "CheckoutServlet", urlPatterns = {"/CheckoutServlet", "/checkout"})
public class CheckoutServlet extends HttpServlet {

    private CartManager cartManager;
    private OrderManager orderManager;
    private PaymentManager paymentManager;

    @Override
    public void init() throws ServletException {
        try {
            cartManager = new CartManager();
            orderManager = new OrderManager();
            paymentManager = new PaymentManager();
            System.out.println("CheckoutServlet: Managers initialized.");
        } catch (SQLException e) {
            System.err.println("CheckoutServlet: Error initializing managers: " + e.getMessage());
            throw new ServletException("Error initializing managers for checkout", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("CheckoutServlet: Received GET request.");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        try {
            List<CartItem> cartItems = cartManager.getCartItems(loggedInUser.getUserId());
            // Now that calculateTotal exists in CartManager:
            double totalAmount = cartManager.calculateTotal(cartItems);

            if (cartItems.isEmpty()) {
                session.setAttribute("cartMessage", "Your cart is empty. Add some products to checkout.");
                response.sendRedirect(request.getContextPath() + "/CartServlet");
                return;
            }

            request.setAttribute("cartItems", cartItems);
            request.setAttribute("totalAmount", totalAmount);
            request.setAttribute("user", loggedInUser);

            System.out.println("CheckoutServlet GET: Forwarding to checkout.jsp for user " + loggedInUser.getUserId());
            request.getRequestDispatcher("/WEB-INF/jsp/user/checkout.jsp").forward(request, response);

        } catch (SQLException e) {
            System.err.println("CheckoutServlet GET: SQL error fetching cart for checkout: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Could not load your cart for checkout. Please try again.");
            request.getRequestDispatcher("/WEB-INF/jsp/user/checkout.jsp").forward(request, response); // Forward to show error on page
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("CheckoutServlet: Received POST request (place order).");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        List<CartItem> cartItems = new ArrayList<>(); // Initialize to prevent null pointer if first try fails
        double totalAmount = 0.0;

        try {
            cartItems = cartManager.getCartItems(loggedInUser.getUserId());
            // Now that calculateTotal exists in CartManager:
            totalAmount = cartManager.calculateTotal(cartItems);

            if (cartItems.isEmpty()) {
                session.setAttribute("checkoutError", "Your cart is empty. Cannot place order.");
                response.sendRedirect(request.getContextPath() + "/CartServlet");
                return;
            }

            String shippingAddress = "Default Shipping Address - Please Update Profile";
            if (loggedInUser.getAddress() != null && !loggedInUser.getAddress().trim().isEmpty()) {
                shippingAddress = loggedInUser.getAddress();
            }
            // String formShippingAddress = request.getParameter("shippingAddress"); // If you add this to your JSP form
            // if (formShippingAddress != null && !formShippingAddress.trim().isEmpty()) {
            //     shippingAddress = formShippingAddress;
            // }

            String orderId = orderManager.placeOrder(loggedInUser.getUserId(), cartItems, totalAmount, shippingAddress);
            System.out.println("CheckoutServlet POST: Order placed with ID: " + orderId);

            String paymentDetails = "Cash on Delivery (Demo)";
            paymentManager.recordPayment(orderId, totalAmount, paymentDetails);
            System.out.println("CheckoutServlet POST: Demo payment recorded for order ID: " + orderId);

            cartManager.clearCart(loggedInUser.getUserId());
            System.out.println("CheckoutServlet POST: Cart cleared for user: " + loggedInUser.getUserId());

            session.setAttribute("orderSuccessMessage", "Your order (ID: " + orderId + ") has been placed successfully!");
            session.setAttribute("lastOrderId", orderId);
            response.sendRedirect(request.getContextPath() + "/OrderHistoryServlet?action=confirmation");

        } catch (NoQuantityLeftException e) { // This catch block is now valid if OrderManager.placeOrder declares it
            System.err.println("CheckoutServlet POST: NoQuantityLeftException: " + e.getMessage());
            session.setAttribute("checkoutError", "Order failed: " + e.getMessage() + " Please update your cart.");
            response.sendRedirect(request.getContextPath() + "/CartServlet");
        }
        catch (SQLException e) {
            System.err.println("CheckoutServlet POST: SQLException during order placement: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "A database error occurred while placing your order. Please try again. Details: " + e.getMessage());
            request.setAttribute("cartItems", cartItems);
            request.setAttribute("totalAmount", totalAmount);
            request.setAttribute("user", loggedInUser);
            request.getRequestDispatcher("/WEB-INF/jsp/user/checkout.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("CheckoutServlet POST: Unexpected error during order placement: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An unexpected error occurred while processing your order. Please contact support.");
            request.setAttribute("cartItems", cartItems);
            request.setAttribute("totalAmount", totalAmount);
            request.setAttribute("user", loggedInUser);
            request.getRequestDispatcher("/WEB-INF/jsp/user/checkout.jsp").forward(request, response);
        }
    }
}
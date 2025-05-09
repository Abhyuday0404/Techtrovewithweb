<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="models.CartItem" %>
<%@ page import="models.Product" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>

<%
    List<CartItem> cartItems = (List<CartItem>) request.getAttribute("cartItems");
    Double totalAmountObject = (Double) request.getAttribute("totalAmount");
    double totalAmount = (totalAmountObject != null) ? totalAmountObject : 0.0;
    String errorMessage = (String) request.getAttribute("errorMessage");
    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>TechTrove - Confirm Your Order</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .checkout-container { padding: 20px; max-width: 700px; margin: auto; }
        .page-header { text-align: center; margin-bottom: 20px; color: #333; }
        .error-message-bar { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; padding: 10px; border-radius: 4px; margin-bottom: 20px; text-align: center;}

        .order-summary-box { background-color: #fff; border: 1px solid #ddd; border-radius: 8px; padding: 20px; margin-bottom: 25px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
        .order-summary-box h2 { margin-top: 0; border-bottom: 1px solid #eee; padding-bottom: 10px; margin-bottom: 15px; font-size: 1.3em; color: #333;}
        .summary-table { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
        .summary-table td { padding: 8px 0; }
        .summary-table .item-name { color: #555; }
        .summary-table .item-details { text-align: right; }
        .summary-table .total-row td { border-top: 2px solid #333; font-weight: bold; padding-top: 10px; font-size: 1.1em; }
        .summary-table .total-row .total-label { text-align: left; }
        .summary-table .total-row .total-value { color: #28a745; }

        .payment-info, .shipping-info { margin-bottom: 25px; } /* Placeholders for future expansion */
        .payment-info h3, .shipping-info h3 { font-size: 1.1em; color: #444; margin-bottom: 10px; }
        .payment-info p, .shipping-info p { font-size: 0.95em; color: #666; line-height: 1.6; }


        .checkout-actions { text-align: center; }
        .btn-place-order {
            background-color: #28a745; color: white; padding: 12px 30px;
            text-decoration: none; border-radius: 5px; font-size: 1.2em; font-weight: bold;
            border: none; cursor: pointer; transition: background-color 0.3s ease;
        }
        .btn-place-order:hover { background-color: #218838; }
        .btn-place-order:disabled { background-color: #ccc; cursor: not-allowed; }
        .back-to-cart-link { display: block; text-align: center; margin-top: 20px; color: #007bff; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/user_navbar.jsp" />

    <div class="checkout-container">
        <h1 class="page-header">Review Your Order</h1>

        <% if (errorMessage != null) { %>
            <div class="error-message-bar"><%= errorMessage %></div>
        <% } %>

        <% if (cartItems != null && !cartItems.isEmpty()) { %>
            <div class="order-summary-box">
                <h2>Order Summary</h2>
                <table class="summary-table">
                    <tbody>
                        <% for (CartItem item : cartItems) {
                            Product product = item.getProduct();
                        %>
                        <tr>
                            <td class="item-name"><%= product.getName() %> (x<%= item.getQuantity() %>)</td>
                            <td class="item-details"><%= currencyFormatter.format(item.getSubtotal()) %></td>
                        </tr>
                        <% } %>
                        <tr class="total-row">
                            <td class="total-label">Grand Total:</td>
                            <td class="total-value item-details"><%= currencyFormatter.format(totalAmount) %></td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="shipping-info"> <%-- Placeholder --%>
                <h3>Shipping Information</h3>
                <p>Standard Shipping (3-5 Business Days)</p>
                <p>Address: [User's Address will go here - fetch from User object in session later]</p>
            </div>

            <div class="payment-info"> <%-- Placeholder --%>
                <h3>Payment Method</h3>
                <p>Payment will be Cash on Delivery (COD) for this demo.</p>
                <p style="font-size:0.9em; color:#888;">(Full payment gateway integration is outside the scope of this version.)</p>
            </div>

            <div class="checkout-actions">
                <form action="${pageContext.request.contextPath}/CheckoutServlet" method="post">
                    <%-- Add a hidden field or button name if specific confirmation is needed by servlet POST --%>
                    <%-- <input type="hidden" name="confirmOrder" value="true"> --%>
                    <button type="submit" class="btn-place-order" <%= (cartItems == null || cartItems.isEmpty()) ? "disabled" : "" %>>
                        Place Your Order
                    </button>
                </form>
            </div>
        <% } else if (errorMessage == null) { // Only show if no specific error and cart is empty
        %>
            <p class="empty-cart-message" style="text-align:center; padding:20px; background-color:#fff;">
                Your cart is empty. You cannot proceed to checkout.
            </p>
        <% } %>
         <a href="${pageContext.request.contextPath}/CartServlet" class="back-to-cart-link">« Back to Cart</a>
    </div>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
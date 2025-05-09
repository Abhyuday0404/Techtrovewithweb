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
    String successMessage = (String) request.getAttribute("successMessage");

    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>TechTrove - Your Shopping Cart</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .cart-container { padding: 20px; max-width: 900px; margin: auto; }
        .page-header { text-align: center; margin-bottom: 20px; color: #333; }
        .message-bar { padding: 10px; border-radius: 4px; margin-bottom: 15px; text-align: center; }
        .error-message-bar { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .success-message-bar { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }

        .cart-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; background-color: #fff; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
        .cart-table th, .cart-table td { border: 1px solid #ddd; padding: 12px; text-align: left; }
        .cart-table th { background-color: #f8f9fa; font-weight: bold; color: #333; }
        .cart-table td.product-name { font-weight: bold; }
        .cart-table td.product-image img { max-width: 60px; height: auto; vertical-align: middle; margin-right: 10px; }
        .cart-table input[type="number"] { width: 50px; padding: 5px; text-align: center; border: 1px solid #ccc; border-radius: 3px;}
        .cart-table .action-btn {
            padding: 6px 10px; border: none; border-radius: 3px; cursor: pointer;
            text-decoration: none; font-size: 0.9em; margin-left: 5px;
        }
        .btn-update { background-color: #ffc107; color: #212529; } .btn-update:hover{ background-color: #e0a800; }
        .btn-remove { background-color: #dc3545; color: white; } .btn-remove:hover{ background-color: #c82333; }

        .cart-summary { text-align: right; margin-bottom: 20px; }
        .cart-summary h3 { font-size: 1.4em; color: #28a745; }
        .cart-actions { text-align: right; }
        .btn-checkout {
            background-color: #28a745; color: white; padding: 12px 25px;
            text-decoration: none; border-radius: 5px; font-size: 1.1em;
            border: none; cursor: pointer; transition: background-color 0.3s ease;
        }
        .btn-checkout:hover { background-color: #218838; }
        .btn-checkout:disabled { background-color: #ccc; cursor: not-allowed; }
        .empty-cart-message { text-align: center; font-size: 1.2em; color: #777; padding: 30px; background-color: #fff; border-radius: 5px; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/user_navbar.jsp" />

    <div class="cart-container">
        <h1 class="page-header">Your Shopping Cart</h1>

        <% if (errorMessage != null) { %>
            <div class="message-bar error-message-bar"><%= errorMessage %></div>
        <% } %>
        <% if (successMessage != null) { %>
            <div class="message-bar success-message-bar"><%= successMessage %></div>
        <% } %>

        <% if (cartItems != null && !cartItems.isEmpty()) { %>
            <table class="cart-table">
                <thead>
                    <tr>
                        <th colspan="2">Product</th>
                        <th>Price</th>
                        <th>Quantity</th>
                        <th>Subtotal</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (CartItem item : cartItems) {
                        Product product = item.getProduct();
                    %>
                        <tr>
                            <td class="product-image">
                                <img src="${product.getImageUrl() != null && !product.getImageUrl().isEmpty() ? product.getImageUrl() : pageContext.request.contextPath += '/images/default_product.png'}" alt="<%= product.getName() %>">
                            </td>
                            <td class="product-name"><%= product.getName() %><br><small style="color:#777;">Brand: <%= product.getBrand() %></small></td>
                            <td><%= currencyFormatter.format(product.getPrice()) %></td>
                            <td>
                                <form action="${pageContext.request.contextPath}/CartServlet" method="post" style="display:inline;">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="cartId" value="<%= item.getCartId() %>">
                                    <input type="number" name="quantity" value="<%= item.getQuantity() %>" min="0" max="<%= product.getStock() + item.getQuantity() /* Allow current qty + remaining stock */ %>" required>
                                    <button type="submit" class="action-btn btn-update">Update</button>
                                </form>
                            </td>
                            <td><%= currencyFormatter.format(item.getSubtotal()) %></td>
                            <td>
                                <form action="${pageContext.request.contextPath}/CartServlet" method="post" style="display:inline;">
                                    <input type="hidden" name="action" value="remove">
                                    <input type="hidden" name="cartId" value="<%= item.getCartId() %>">
                                    <button type="submit" class="action-btn btn-remove">Remove</button>
                                </form>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>

            <div class="cart-summary">
                <h3>Total: <%= currencyFormatter.format(totalAmount) %></h3>
            </div>

            <div class="cart-actions">
                 <a href="${pageContext.request.contextPath}/ProductServlet" style="margin-right:15px; color: #007bff;">« Continue Shopping</a>
                <form action="${pageContext.request.contextPath}/CheckoutServlet" method="get" style="display:inline;"> <%-- Or POST if preferred --%>
                    <button type="submit" class="btn-checkout" <%= (cartItems == null || cartItems.isEmpty()) ? "disabled" : "" %>>
                        Proceed to Checkout
                    </button>
                </form>
            </div>

        <% } else { %>
            <p class="empty-cart-message">Your shopping cart is currently empty. <a href="${pageContext.request.contextPath}/ProductServlet">Start shopping!</a></p>
        <% } %>

    </div>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
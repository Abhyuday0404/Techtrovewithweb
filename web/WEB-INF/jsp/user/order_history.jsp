<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="models.Order" %>
<%@ page import="models.OrderDetail" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.Locale" %>

<%
    List<Order> orders = (List<Order>) request.getAttribute("orders");
    Order confirmedOrder = (Order) request.getAttribute("confirmedOrder"); // For order confirmation display
    String errorMessage = (String) request.getAttribute("errorMessage");
    String successMessage = (String) request.getAttribute("orderSuccessMessage"); // Consistent name

    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>TechTrove - Order History</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .history-container { padding: 20px; max-width: 1000px; margin: auto; }
        .page-header { text-align: center; margin-bottom: 20px; color: #333; }
        .message-bar { padding: 10px; border-radius: 4px; margin-bottom: 15px; text-align: center; }
        .error-message-bar { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .success-message-bar { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; font-size: 1.1em; padding: 15px;}

        .order-card { background-color: #fff; border: 1px solid #ddd; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
        .order-header { background-color: #f8f9fa; padding: 12px 15px; border-bottom: 1px solid #ddd; display: flex; justify-content: space-between; align-items: center; }
        .order-header .order-id { font-weight: bold; color: #007bff; }
        .order-header .order-date { font-size: 0.9em; color: #555; }
        .order-body { padding: 15px; }
        .order-total { font-size: 1.1em; font-weight: bold; margin-bottom: 10px; }
        .order-status { /* For future use */ font-style: italic; color: #6c757d; margin-bottom: 15px; }

        .order-items-table { width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 0.9em; }
        .order-items-table th, .order-items-table td { border: 1px solid #eee; padding: 8px; text-align: left; }
        .order-items-table th { background-color: #f9f9f9; }
        .order-items-table td.item-qty { text-align: center; }
        .order-items-table td.item-price, .order-items-table td.item-subtotal { text-align: right; }

        .btn-view-details, .btn-reorder { /* Example buttons for future */
            padding: 6px 12px; text-decoration: none; border-radius: 4px; font-size: 0.9em; margin-right: 5px;
            background-color: #6c757d; color:white; border:none; cursor: pointer;
        }
        .btn-view-details:hover { background-color: #5a6268; }
        .no-orders { text-align: center; font-size: 1.2em; color: #777; padding: 30px; background-color: #fff; border-radius: 5px;}
        .confirmed-order-details { border: 2px solid #28a745; padding: 15px; margin-bottom: 20px; border-radius: 5px; background-color: #f0fff0; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/user_navbar.jsp" />

    <div class="history-container">
        <h1 class="page-header">Your Order History</h1>

        <% if (successMessage != null) { %>
            <div class="message-bar success-message-bar"><%= successMessage %></div>
        <% } %>
        <% if (errorMessage != null) { %>
            <div class="message-bar error-message-bar"><%= errorMessage %></div>
        <% } %>

        <%-- Display details of the just-confirmed order --%>
        <% if (confirmedOrder != null) { %>
            <div class="order-card confirmed-order-details">
                <h3>Details for Order: <%= confirmedOrder.getOrderId() %></h3>
                 <p><strong>Date:</strong> <%= confirmedOrder.getOrderDate().format(dateTimeFormatter) %></p>
                 <p><strong>Total:</strong> <%= currencyFormatter.format(confirmedOrder.getTotalAmount()) %></p>
                <h4>Items:</h4>
                <table class="order-items-table">
                    <thead><tr><th>Product</th><th>Quantity</th><th>Price</th><th>Subtotal</th></tr></thead>
                    <tbody>
                    <% for(OrderDetail detail : confirmedOrder.getOrderDetails()) { %>
                        <tr>
                            <td><%= detail.getProductName() %></td>
                            <td class="item-qty"><%= detail.getQuantity() %></td>
                            <td class="item-price"><%= currencyFormatter.format(detail.getPrice()) %></td>
                            <td class="item-subtotal"><%= currencyFormatter.format(detail.getSubtotal()) %></td>
                        </tr>
                    <% } %>
                    </tbody>
                </table>
                 <hr style="margin: 20px 0;">
            </div>
        <% } %>


        <% if (orders != null && !orders.isEmpty()) { %>
            <% for (Order order : orders) { %>
                <div class="order-card">
                    <div class="order-header">
                        <span class="order-id">Order ID: <%= order.getOrderId() %></span>
                        <span class="order-date">Placed on: <%= order.getOrderDate().format(dateTimeFormatter) %></span>
                    </div>
                    <div class="order-body">
                        <p class="order-total">Total Amount: <%= currencyFormatter.format(order.getTotalAmount()) %></p>
                        <%-- <p class="order-status">Status: Shipped</p> --%>
                        
                        <% if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) { %>
                            <p><strong>Items:</strong></p>
                            <table class="order-items-table">
                                <thead>
                                    <tr>
                                        <th>Product Name</th>
                                        <th style="text-align:center;">Quantity</th>
                                        <th style="text-align:right;">Price Paid</th>
                                        <th style="text-align:right;">Subtotal</th>
                                    </tr>
                                </thead>
                                <tbody>
                                <% for (OrderDetail detail : order.getOrderDetails()) { %>
                                    <tr>
                                        <td><%= detail.getProductName() != null ? detail.getProductName() : "(Product details unavailable)" %></td>
                                        <td class="item-qty"><%= detail.getQuantity() %></td>
                                        <td class="item-price"><%= currencyFormatter.format(detail.getPrice()) %></td>
                                        <td class="item-subtotal"><%= currencyFormatter.format(detail.getSubtotal()) %></td>
                                    </tr>
                                <% } %>
                                </tbody>
                            </table>
                        <% } else { %>
                            <p><em>No item details available for this order.</em></p>
                        <% } %>
                        
                        <%-- Placeholder for future actions like "View Details Modal" or "Reorder" --%>
                        <%--
                        <div style="margin-top: 15px; text-align: right;">
                            <a href="#" class="btn-view-details">View Details</a>
                        </div>
                        --%>
                    </div>
                </div>
            <% } %>
        <% } else if (errorMessage == null && successMessage == null && confirmedOrder == null) { // Only show if no messages and no orders at all %>
            <p class="no-orders">You have no past orders. <a href="${pageContext.request.contextPath}/ProductServlet">Start shopping now!</a></p>
        <% } %>
    </div>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
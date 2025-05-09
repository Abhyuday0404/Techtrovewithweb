<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="models.Order" %>
<%@ page import="models.OrderDetail" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    List<Order> orders = (List<Order>) request.getAttribute("orders");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String orderSuccessMessage = (String) request.getAttribute("orderSuccessMessage");

    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>TechTrove - Your Order History</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .history-container { padding: 20px; max-width: 900px; margin: auto; }
        .page-header { text-align: center; margin-bottom: 20px; color: #333; }
        .message-bar { padding: 10px; border-radius: 4px; margin-bottom: 15px; text-align: center; }
        .error-message-bar { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .success-message-bar { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }

        .order-card {
            background-color: #fff; border: 1px solid #ddd; border-radius: 8px;
            margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        }
        .order-header {
            background-color: #f8f9fa; padding: 10px 15px; border-bottom: 1px solid #ddd;
            display: flex; justify-content: space-between; align-items: center; font-size:0.9em;
        }
        .order-header .order-id { font-weight: bold; color: #007bff; }
        .order-header .order-date { color: #6c757d; }
        .order-body { padding: 15px; }
        .order-body h4 { margin-top:0; margin-bottom:10px; font-size:1.1em; color:#444; }
        .order-items-table { width: 100%; border-collapse: collapse; margin-bottom: 10px; font-size: 0.9em;}
        .order-items-table th, .order-items-table td { border: 1px solid #eee; padding: 6px 8px; text-align: left; }
        .order-items-table th { background-color: #f0f0f0; }
        .order-items-table td.item-price, .order-items-table td.item-subtotal { text-align: right; }
        .order-footer {
            padding: 10px 15px; border-top: 1px solid #ddd; text-align: right;
            font-weight: bold; font-size: 1.1em;
        }
        .order-footer .total-label { float:left; color:#555;}
        .order-footer .total-amount { color: #28a745; }
        .no-orders-message { text-align: center; padding: 20px; font-style: italic; color: #777; background-color:#fff; border-radius:5px;}
        .no-item-details { padding: 10px; font-style: italic; color: #777; text-align:center;}
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/user_navbar.jsp" />

    <div class="history-container">
        <h1 class="page-header">Your Order History</h1>

        <% if (orderSuccessMessage != null) { %>
            <div class="message-bar success-message-bar"><%= orderSuccessMessage %></div>
        <% } %>
        <% if (errorMessage != null) { %>
            <div class="message-bar error-message-bar"><%= errorMessage %></div>
        <% } %>

        <% if (orders != null && !orders.isEmpty()) { %>
            <% for (Order order : orders) { %>
                <div class="order-card">
                    <div class="order-header">
                        <span class="order-id">Order ID: <%= order.getOrderId() %></span>
                        <span class="order-date">Placed on: <%= order.getOrderDate() != null ? order.getOrderDate().format(dateTimeFormatter) : "N/A" %></span>
                    </div>
                    <div class="order-body">
                        <h4>Items Ordered:</h4>
                        <% List<OrderDetail> details = order.getOrderDetails();
                           if (details != null && !details.isEmpty()) {
                        %>
                            <table class="order-items-table">
                                <thead>
                                    <tr>
                                        <th>Product Name</th>
                                        <th>Quantity</th>
                                        <th class="item-price">Price per Unit</th>
                                        <th class="item-subtotal">Subtotal</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (OrderDetail item : details) { %>
                                        <tr>
                                            <td><%= item.getProductName() != null ? item.getProductName() : "(Product details unavailable)" %></td>
                                            <td><%= item.getQuantity() %></td>
                                            <td class="item-price"><%= currencyFormatter.format(item.getPrice()) %></td>
                                            <td class="item-subtotal"><%= currencyFormatter.format(item.getSubtotal()) %></td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        <% } else { %>
                            <p class="no-item-details">No item details available for this order.</p>
                        <% } %>
                    </div>
                    <div class="order-footer">
                        <span class="total-label">Total Amount:</span>
                        <span class="total-amount"><%= currencyFormatter.format(order.getTotalAmount()) %></span>
                    </div>
                </div>
            <% } %>
        <% } else if (errorMessage == null && orderSuccessMessage == null) { 
        %>
            <p class="no-orders-message">You have not placed any orders yet.</p>
        <% } %>
    </div>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
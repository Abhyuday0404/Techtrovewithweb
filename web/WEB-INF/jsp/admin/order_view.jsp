<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="models.Order" %>
<%@ page import="models.OrderDetail" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.Locale" %>

<%
    List<Order> orders = (List<Order>) request.getAttribute("orders");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String successMessage = (String) request.getAttribute("successMessage"); // For future admin actions

    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>TechTrove - View All Orders</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .admin-content-container { padding: 20px; max-width: 1200px; margin: auto; }
        .page-header { text-align: center; margin-bottom: 20px; color: #333; }
        .message-bar { padding: 10px; border-radius: 4px; margin-bottom: 15px; text-align: center; }
        .error-message-bar { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .success-message-bar { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }

        .admin-table { width: 100%; border-collapse: collapse; background-color: #fff; box-shadow: 0 2px 5px rgba(0,0,0,0.05); font-size: 0.9em; }
        .admin-table th, .admin-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        .admin-table th { background-color: #e9ecef; font-weight: bold; color: #495057; }
        
        .order-details-toggle { cursor: pointer; color: #007bff; text-decoration: underline; font-size: 0.9em; }
        .order-details-section { background-color: #fdfdfd; padding: 10px; margin-top: 5px; border: 1px dashed #eee; display: none; /* Hidden by default */ }
        .order-items-table { width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 0.9em; }
        .order-items-table th, .order-items-table td { border: 1px solid #f0f0f0; padding: 6px; text-align: left; }
        .order-items-table th { background-color: #f9f9f9; }
        .order-items-table td.item-qty { text-align: center; }
        .order-items-table td.item-price, .order-items-table td.item-subtotal { text-align: right; }
        .no-data-message { text-align: center; padding: 20px; font-style: italic; color: #777;}
    </style>
    <script>
        function toggleOrderDetails(orderId) {
            var detailsSection = document.getElementById("details-" + orderId);
            if (detailsSection) {
                if (detailsSection.style.display === "none" || detailsSection.style.display === "") {
                    detailsSection.style.display = "table-row"; // Or "block" if it's not a direct child of <tbody>
                                                              // For a new <tr>, it needs to be table-row
                } else {
                    detailsSection.style.display = "none";
                }
            }
        }
    </script>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/admin_navbar.jsp" />

    <div class="admin-content-container">
        <h1 class="page-header">All Customer Orders</h1>

        <% if (successMessage != null) { %>
            <div class="message-bar success-message-bar"><%= successMessage %></div>
        <% } %>
        <% if (errorMessage != null) { %>
            <div class="message-bar error-message-bar"><%= errorMessage %></div>
        <% } %>

        <% if (orders != null && !orders.isEmpty()) { %>
            <table class="admin-table">
                <thead>
                    <tr>
                        <th>Order ID</th>
                        <th>Customer Name</th>
                        <th>User ID</th>
                        <th>Order Date</th>
                        <th>Total Amount</th>
                        <th>Items</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Order order : orders) { %>
                        <tr>
                            <td><%= order.getOrderId() %></td>
                            <td><%= order.getCustomerName() != null ? order.getCustomerName() : "(N/A)" %></td>
                            <td><%= order.getUserId() != null ? order.getUserId() : "(N/A)" %></td>
                            <td><%= order.getOrderDate().format(dateTimeFormatter) %></td>
                            <td><%= currencyFormatter.format(order.getTotalAmount()) %></td>
                            <td><%= order.getOrderDetails() != null ? order.getOrderDetails().size() : 0 %></td>
                            <td>
                                <span class="order-details-toggle" onclick="toggleOrderDetails('<%= order.getOrderId() %>')">View Details</span>
                                <%-- Add other actions like "Update Status" in future --%>
                            </td>
                        </tr>
                        <tr id="details-<%= order.getOrderId() %>" class="order-details-section">
                            <td colspan="7">
                                <h4>Order Items for #<%= order.getOrderId() %>:</h4>
                                <% if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) { %>
                                    <table class="order-items-table">
                                        <thead>
                                            <tr>
                                                <th>Product Name</th>
                                                <th>Product ID</th>
                                                <th style="text-align:center;">Qty</th>
                                                <th style="text-align:right;">Price Paid</th>
                                                <th style="text-align:right;">Subtotal</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                        <% for (OrderDetail detail : order.getOrderDetails()) { %>
                                            <tr>
                                                <td><%= detail.getProductName() != null ? detail.getProductName() : "(Product info N/A)" %></td>
                                                <td><%= detail.getProductId() != null ? detail.getProductId() : "(N/A)" %></td>
                                                <td class="item-qty"><%= detail.getQuantity() %></td>
                                                <td class="item-price"><%= currencyFormatter.format(detail.getPrice()) %></td>
                                                <td class="item-subtotal"><%= currencyFormatter.format(detail.getSubtotal()) %></td>
                                            </tr>
                                        <% } %>
                                        </tbody>
                                    </table>
                                <% } else { %>
                                    <p><em>No item details found for this order.</em></p>
                                <% } %>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        <% } else if (errorMessage == null) { %>
            <p class="no-data-message">No orders found in the system.</p>
        <% } %>
    </div>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
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

    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
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

        .admin-table { width: 100%; border-collapse: collapse; background-color: #fff; box-shadow: 0 2px 5px rgba(0,0,0,0.05); font-size: 0.9em; }
        .admin-table th, .admin-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        .admin-table th { background-color: #e9ecef; font-weight: bold; color: #495057; }
        .actions a { margin-right: 5px; text-decoration: none; padding: 3px 7px; border-radius: 3px; color:white; }
        .actions a.view-details-link { background-color: #17a2b8; }
        .no-data-message { text-align: center; padding: 20px; font-style: italic; color: #777;}
        .order-details-row { background-color: #fdfdfd; }
        .order-details-cell { padding: 15px !important; }
        .order-details-cell h5 { margin-top: 0; margin-bottom: 10px; color: #555;}
        .order-items-sub-table { width:100%; margin-top:5px; font-size:0.95em; border-collapse: collapse;}
        .order-items-sub-table th, .order-items-sub-table td {border: 1px solid #eee; padding: 6px; text-align:left;}
        .order-items-sub-table th {background-color:#f8f8f8;}
        .order-items-sub-table .price-col { text-align:right; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/admin_navbar.jsp" />

    <div class="admin-content-container">
        <h1 class="page-header">All Customer Orders</h1>

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
                        <th>Items Count</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Order order : orders) { %>
                        <tr>
                            <td><%= order.getOrderId() %></td>
                            <td><%= order.getCustomerName() != null ? order.getCustomerName() : "(N/A)" %></td>
                            <td><%= order.getUserId() != null ? order.getUserId() : "(N/A)" %></td>
                            <td><%= order.getOrderDate() != null ? order.getOrderDate().format(dateTimeFormatter) : "N/A" %></td>
                            <td><%= currencyFormatter.format(order.getTotalAmount()) %></td>
                            <td><%= order.getOrderDetails() != null ? order.getOrderDetails().size() : 0 %></td>
                            <td class="actions">
                                <a href="#" class="view-details-link" onclick="toggleOrderDetails('details_<%= order.getOrderId() %>'); return false;">View Details</a>
                            </td>
                        </tr>
                        <tr id="details_<%= order.getOrderId() %>" style="display:none;" class="order-details-row">
                            <td colspan="7" class="order-details-cell">
                                <h5>Order Items for #<%= order.getOrderId() %>:</h5>
                                <% List<OrderDetail> details = order.getOrderDetails();
                                   if (details != null && !details.isEmpty()) {
                                %>
                                    <table class="order-items-sub-table">
                                        <thead>
                                            <tr>
                                                <th>Product Name</th>
                                                <th>Product ID</th>
                                                <th>Quantity</th>
                                                <th class="price-col">Price@Order</th>
                                                <th class="price-col">Subtotal</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                        <% for (OrderDetail item : details) { %>
                                            <tr>
                                                <td><%= item.getProductName() != null ? item.getProductName() : "(Product details unavailable)" %></td>
                                                <td><%= item.getProductId() != null ? item.getProductId() : "N/A" %></td>
                                                <td><%= item.getQuantity() %></td>
                                                <td class="price-col"><%= currencyFormatter.format(item.getPrice()) %></td>
                                                <td class="price-col"><%= currencyFormatter.format(item.getSubtotal()) %></td>
                                            </tr>
                                        <% } %>
                                        </tbody>
                                    </table>
                                <% } else { %>
                                    <p>No item details found for this order.</p>
                                <% } %>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        <% } else if (errorMessage == null) { %>
            <p class="no-data-message">No orders have been placed yet.</p>
        <% } %>
    </div>

    <script>
        function toggleOrderDetails(rowId) {
            var row = document.getElementById(rowId);
            if (row) {
                row.style.display = (row.style.display === 'none' || row.style.display === '') ? 'table-row' : 'none';
            }
        }
    </script>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
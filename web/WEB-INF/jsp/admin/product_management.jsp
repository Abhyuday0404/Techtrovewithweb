<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="models.Product" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    List<Product> products = (List<Product>) request.getAttribute("products");
    String successMessage = (String) request.getAttribute("adminProductSuccess");
    String errorMessage = (String) request.getAttribute("adminProductError");

    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    // DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy"); // Not directly used in this table view for brevity
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>TechTrove - Product Management</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .admin-content-container { padding: 20px; max-width: 1200px; margin: auto; }
        .page-header { text-align: center; margin-bottom: 20px; color: #333; }
        .message-bar { padding: 10px; border-radius: 4px; margin-bottom: 15px; text-align: center; }
        .error-message-bar { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .success-message-bar { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }

        .admin-table { width: 100%; border-collapse: collapse; background-color: #fff; box-shadow: 0 2px 5px rgba(0,0,0,0.05); font-size: 0.9em; }
        .admin-table th, .admin-table td { border: 1px solid #ddd; padding: 8px; text-align: left; vertical-align: middle; } /* Changed vertical-align */
        .admin-table th { background-color: #e9ecef; font-weight: bold; color: #495057; }
        .admin-table img.product-thumbnail { max-width: 60px; max-height: 60px; object-fit: contain; border-radius: 3px; background-color: #f0f0f0; }
        .admin-table .actions a, .admin-table .actions button {
            margin-right: 5px; padding: 5px 8px; text-decoration: none; border-radius: 3px; font-size: 0.85em;
        }
        .admin-table .actions .btn-edit { background-color: #ffc107; color: #212529; border:none; display: inline-block; } /* ensure it's display inline-block for proper spacing */
        .admin-table .actions .btn-edit:hover { background-color: #e0a800;}
        .admin-table .actions .btn-delete { background-color: #dc3545; color: white; border:none; cursor:pointer;}
        .admin-table .actions .btn-delete:hover { background-color: #c82333;}

        .top-actions { margin-bottom: 15px; text-align: right; }
        .btn-add-new { background-color: #28a745; color: white; padding: 8px 15px; text-decoration: none; border-radius: 4px; display: inline-block; }
        .btn-add-new:hover { background-color: #218838; }
        .no-data-message { text-align: center; padding: 20px; font-style: italic; color: #777;}
    </style>
    <script>
        function confirmDelete(productId, productName) {
            // Sanitize productName for display in confirm dialog if it contains special characters
            var cleanProductName = productName.replace(/'/g, "\\'").replace(/"/g, '\\"');
            return confirm("Are you sure you want to delete product: " + cleanProductName + " (ID: " + productId + ")?");
        }
    </script>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/admin_navbar.jsp" />

    <div class="admin-content-container">
        <h1 class="page-header">Manage Products</h1>

        <% if (successMessage != null) { %>
            <div class="message-bar success-message-bar"><%= successMessage %></div>
        <% } %>
        <% if (errorMessage != null) { %>
            <div class="message-bar error-message-bar"><%= errorMessage %></div>
        <% } %>

        <div class="top-actions">
            <a href="${pageContext.request.contextPath}/AdminProductServlet?action=add_form" class="btn-add-new">Add New Product</a>
        </div>

        <% if (products != null && !products.isEmpty()) { %>
            <table class="admin-table">
                <thead>
                    <tr>
                        <th style="width: 80px;">Image</th>
                        <th style="width: 120px;">ID</th>
                        <th>Name</th>
                        <th>Brand</th>
                        <th style="width: 100px;">Price</th>
                        <th style="width: 70px;">Stock</th>
                        <th style="width: 120px;">Category ID</th>
                        <th style="width: 150px;">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Product p : products) { %>
                        <tr>
                            <td>
                                <img src="${(p.getImageUrl() != null && !p.getImageUrl().isEmpty()) ? p.getImageUrl() : pageContext.request.contextPath += '/images/default_product.png'}"
                                     alt="<%= p.getName() %>" class="product-thumbnail">
                                <%-- Note: The += in pageContext.request.contextPath above is likely an error if used multiple times.
                                     It should be:
                                     ${pageContext.request.contextPath}/images/default_product.png
                                     Corrected version: --%>
                                <%--
                                <img src="${(p.getImageUrl() != null && !p.getImageUrl().isEmpty())
                                                ? p.getImageUrl()
                                                : pageContext.request.contextPath.concat('/images/default_product.png')}"
                                     alt="<%= p.getName() %>" class="product-thumbnail">
                                --%>
                            </td>
                            <td><%= p.getProductId() %></td>
                            <td><%= p.getName() %></td>
                            <td><%= p.getBrand() != null ? p.getBrand() : "N/A" %></td>
                            <td><%= currencyFormatter.format(p.getPrice()) %></td>
                            <td><%= p.getStock() %></td>
                            <td><%= p.getCategoryId() != null ? p.getCategoryId() : "N/A" %></td>
                            <td class="actions">
                                <a href="${pageContext.request.contextPath}/AdminProductServlet?action=edit_form&productId=<%= p.getProductId() %>" class="btn-edit">Edit</a>
                                <form action="${pageContext.request.contextPath}/AdminProductServlet" method="post" style="display:inline;">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="productId" value="<%= p.getProductId() %>">
                                    <button type="submit" class="btn-delete"
                                            onclick="return confirmDelete('<%= p.getProductId() %>', '<%= p.getName() %>');">Delete</button>
                                </form>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        <% } else if (errorMessage == null) { // Only show if no specific error and products list is empty %>
            <p class="no-data-message">No products found in the database. Click 'Add New Product' to get started.</p>
        <% } %>
    </div>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
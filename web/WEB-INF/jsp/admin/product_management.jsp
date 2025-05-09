<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="models.Product" %>
<%@ page import="models.Category" %> <%-- For displaying category name --%>
<%@ page import="managers.CategoryManager" %> <%-- To fetch category name if needed --%>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>

<%
    List<Product> products = (List<Product>) request.getAttribute("products");
    List<Category> categories = (List<Category>) request.getAttribute("categories"); // For mapping ID to Name
    String errorMessage = (String) request.getAttribute("errorMessage");
    String successMessage = (String) request.getAttribute("successMessage");

    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Create a map for quick category name lookup
    Map<String, String> categoryMap = new HashMap<>();
    if (categories != null) {
        for (Category cat : categories) {
            categoryMap.put(cat.getCategoryId(), cat.getCategoryName());
        }
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>TechTrove - Product Management</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .admin-content-container { padding: 20px; max-width: 1200px; margin: auto; }
        .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
        .page-header h1 { color: #333; margin: 0; }
        .btn-add-new { background-color: #28a745; color: white; padding: 10px 15px; text-decoration: none; border-radius: 5px; font-size: 0.9em; }
        .btn-add-new:hover { background-color: #218838; }

        .message-bar { padding: 10px; border-radius: 4px; margin-bottom: 15px; text-align: center; }
        .error-message-bar { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .success-message-bar { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }

        .admin-table { width: 100%; border-collapse: collapse; background-color: #fff; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
        .admin-table th, .admin-table td { border: 1px solid #ddd; padding: 10px; text-align: left; font-size: 0.9em; }
        .admin-table th { background-color: #e9ecef; font-weight: bold; color: #495057; }
        .admin-table td.actions a, .admin-table td.actions button {
            margin-right: 5px; padding: 5px 8px; text-decoration: none; border-radius: 3px; font-size: 0.85em;
            border: none; cursor: pointer;
        }
        .admin-table .btn-edit { background-color: #ffc107; color: #212529; } .btn-edit:hover { background-color: #e0a800;}
        .admin-table .btn-delete { background-color: #dc3545; color: white; } .btn-delete:hover { background-color: #c82333;}
        .no-data-message { text-align: center; padding: 20px; font-style: italic; color: #777;}
    </style>
    <script>
        function confirmDelete(productId) {
            if (confirm("Are you sure you want to delete product ID: " + productId + "? This action cannot be undone.")) {
                // Create a form and submit it for POST-based deletion
                var form = document.createElement('form');
                form.method = 'post';
                form.action = '${pageContext.request.contextPath}/AdminProductServlet';

                var actionInput = document.createElement('input');
                actionInput.type = 'hidden';
                actionInput.name = 'action';
                actionInput.value = 'delete_product';
                form.appendChild(actionInput);

                var idInput = document.createElement('input');
                idInput.type = 'hidden';
                idInput.name = 'id'; // Use 'id' as expected by AdminProductServlet's handleDeleteProduct
                idInput.value = productId;
                form.appendChild(idInput);

                document.body.appendChild(form);
                form.submit();
            }
            return false; // Prevent default link behavior if it was an <a> tag
        }
    </script>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/admin_navbar.jsp" />

    <div class="admin-content-container">
        <div class="page-header">
            <h1>Product Management</h1>
            <a href="${pageContext.request.contextPath}/AdminProductServlet?action=add_form" class="btn-add-new">Add New Product</a>
        </div>

        <% if (successMessage != null) { %>
            <div class="message-bar success-message-bar"><%= successMessage %></div>
        <% } %>
        <% if (errorMessage != null) { %>
            <div class="message-bar error-message-bar"><%= errorMessage %></div>
        <% } %>

        <% if (products != null && !products.isEmpty()) { %>
            <table class="admin-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Brand</th>
                        <th>Category</th>
                        <th>Price</th>
                        <th>Stock</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Product product : products) { %>
                        <tr>
                            <td><%= product.getProductId() %></td>
                            <td><%= product.getName() %></td>
                            <td><%= product.getBrand() != null ? product.getBrand() : "N/A" %></td>
                            <td>
                                <% String catName = categoryMap.get(product.getCategoryId()); %>
                                <%= (catName != null ? catName : (product.getCategoryId() != null ? product.getCategoryId() : "N/A")) %>
                            </td>
                            <td><%= currencyFormatter.format(product.getPrice()) %></td>
                            <td><%= product.getStock() %></td>
                            <td class="actions">
                                <a href="${pageContext.request.contextPath}/AdminProductServlet?action=edit_form&id=<%= product.getProductId() %>" class="btn-edit">Edit</a>
                                <button onclick="return confirmDelete('<%= product.getProductId() %>');" class="btn-delete">Delete</button>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        <% } else if (errorMessage == null) { %>
            <p class="no-data-message">No products found. <a href="${pageContext.request.contextPath}/AdminProductServlet?action=add_form">Add the first product!</a></p>
        <% } %>
    </div>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
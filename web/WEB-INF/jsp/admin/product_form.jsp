<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="models.Product" %>
<%@ page import="models.Category" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDate" %>

<%
    Product product = (Product) request.getAttribute("product"); 
    List<Category> categories = (List<Category>) request.getAttribute("categories");
    boolean isEditMode = (product != null);
    String formAction = isEditMode ? "update_product" : "add_product";
    String pageTitle = isEditMode ? "Edit Product" : "Add New Product";
    String submitButtonText = isEditMode ? "Update Product" : "Add Product";
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>TechTrove - <%= pageTitle %></title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .admin-form-container { padding: 20px; max-width: 700px; margin: auto; }
        .page-header { text-align: center; margin-bottom: 20px; color: #333; }
        .form-box { background-color: #fff; border: 1px solid #ddd; border-radius: 8px; padding: 25px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; color: #555; }
        .form-group input[type="text"],
        .form-group input[type="number"],
        .form-group input[type="date"],
        .form-group textarea,
        .form-group select {
            width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box; font-size: 0.95em;
        }
        .form-group textarea { min-height: 80px; resize: vertical; }
        .form-actions { margin-top: 20px; text-align: right; }
        .btn-submit { background-color: #007bff; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 1em; }
        .btn-submit:hover { background-color: #0056b3; }
        .btn-cancel { background-color: #6c757d; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 1em; text-decoration: none; margin-left:10px;}
        .btn-cancel:hover { background-color: #545b62; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/admin_navbar.jsp" />

    <div class="admin-form-container">
        <div class="page-header">
            <h1><%= pageTitle %></h1>
        </div>

        <div class="form-box">
            <form action="${pageContext.request.contextPath}/AdminProductServlet" method="post">
                <input type="hidden" name="action" value="<%= formAction %>">
                <% if (isEditMode) { %>
                    <input type="hidden" name="productId" value="<%= product.getProductId() %>">
                <% } %>

                <div class="form-group">
                    <label for="name">Product Name:</label>
                    <input type="text" id="name" name="name" value="<%= isEditMode && product.getName() != null ? product.getName() : "" %>" required>
                </div>
                <div class="form-group">
                    <label for="brand">Brand:</label>
                    <input type="text" id="brand" name="brand" value="<%= isEditMode && product.getBrand() != null ? product.getBrand() : "" %>">
                </div>
                <div class="form-group">
                    <label for="model">Model:</label>
                    <input type="text" id="model" name="model" value="<%= isEditMode && product.getModel() != null ? product.getModel() : "" %>">
                </div>
                <div class="form-group">
                    <label for="description">Description:</label>
                    <textarea id="description" name="description" rows="3"><%= isEditMode && product.getDescription() != null ? product.getDescription() : "" %></textarea>
                </div>
                <div class="form-group">
                    <label for="price">Price (₹):</label>
                    <input type="number" id="price" name="price" step="0.01" min="0" value="<%= isEditMode ? product.getPrice() : "0.00" %>" required>
                </div>
                <div class="form-group">
                    <label for="stock">Stock Quantity:</label>
                    <input type="number" id="stock" name="stock" min="0" value="<%= isEditMode ? product.getStock() : "0" %>" required>
                </div>
                <div class="form-group">
                    <label for="manufactureDate">Manufacture Date (Optional):</label>
                    <input type="date" id="manufactureDate" name="manufactureDate" value="<%= isEditMode && product.getManufactureDate() != null ? product.getManufactureDate().toString() : "" %>">
                </div>
                <div class="form-group">
                    <label for="categoryId">Category (Optional):</label>
                    <select id="categoryId" name="categoryId">
                        <option value="">-- Select Category --</option>
                        <% if (categories != null) {
                            for (Category category : categories) {
                                String selected = (isEditMode && product.getCategoryId() != null && product.getCategoryId().equals(category.getCategoryId())) ? "selected" : "";
                        %>
                            <option value="<%= category.getCategoryId() %>" <%= selected %>><%= category.getCategoryName() %></option>
                        <%  }
                           }
                        %>
                    </select>
                </div>
                <%-- Image URL input field REMOVED
                 <div class="form-group">
                    <label for="imageUrl">Image Path (Optional):</label>
                    <input type="text" id="imageUrl" name="imageUrl" placeholder="e.g., images/your_image.png" value="<%= isEditMode && product.getImageUrl() != null ? product.getImageUrl() : "" %>">
                </div>
                --%>
                <div class="form-actions">
                    <button type="submit" class="btn-submit"><%= submitButtonText %></button>
                    <a href="${pageContext.request.contextPath}/AdminProductServlet?action=list" class="btn-cancel">Cancel</a>
                </div>
            </form>
        </div>
    </div>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
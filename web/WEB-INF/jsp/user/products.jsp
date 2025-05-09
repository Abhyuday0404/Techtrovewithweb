<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="models.Product" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.util.Locale" %>

<%
    List<Product> products = (List<Product>) request.getAttribute("products");
    String errorMessage = (String) request.getAttribute("errorMessage");
    NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN")); // For ₹
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>TechTrove - Browse Products</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .products-container { padding: 20px; max-width: 1200px; margin: auto; }
        .page-header { text-align: center; margin-bottom: 20px; color: #333; }
        .search-filter-bar { margin-bottom: 20px; padding: 15px; background-color: #f8f9fa; border-radius: 5px; display: flex; justify-content: space-between; align-items: center; }
        .search-filter-bar input[type="text"], .search-filter-bar select { padding: 8px; margin-right: 10px; border: 1px solid #ddd; border-radius: 4px; }
        .search-filter-bar button { padding: 8px 15px; background-color: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer; }
        .search-filter-bar button:hover { background-color: #0056b3; }

        .product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
        .product-card {
            background-color: #fff; border: 1px solid #e0e0e0; border-radius: 8px;
            padding: 15px; text-align: center; box-shadow: 0 2px 5px rgba(0,0,0,0.05);
            display: flex; flex-direction: column; justify-content: space-between;
        }
        .product-card img {
            max-width: 100%; height: 180px; object-fit: contain; margin-bottom: 15px; border-radius: 4px;
            background-color: #f0f0f0; /* Placeholder background */
        }
        .product-card h3 { font-size: 1.1em; margin: 10px 0 5px 0; color: #333; min-height: 44px; /* For 2 lines of text */ }
        .product-card .brand { font-size: 0.9em; color: #777; margin-bottom: 10px; }
        .product-card .price { font-size: 1.2em; font-weight: bold; color: #28a745; margin-bottom: 10px; }
        .product-card .stock { font-size: 0.9em; color: #555; margin-bottom: 15px; }
        .product-card .stock.low { color: #dc3545; font-weight: bold; }
        .product-card .add-to-cart-form { display: flex; align-items: center; justify-content: center; }
        .product-card .add-to-cart-form input[type="number"] { width: 50px; padding: 8px; text-align: center; margin-right: 10px; border: 1px solid #ccc; border-radius: 4px;}
        .product-card .btn-add-to-cart {
            background-color: #007bff; color: white; border: none; padding: 10px 15px;
            text-decoration: none; border-radius: 4px; cursor: pointer; font-size: 0.9em;
            transition: background-color 0.3s ease;
        }
        .product-card .btn-add-to-cart:hover { background-color: #0056b3; }
        .no-products { text-align: center; font-size: 1.2em; color: #777; padding: 30px; }
        .error-message-bar { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; padding: 10px; border-radius: 4px; margin-bottom: 20px; text-align: center;}
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/user_navbar.jsp" />

    <div class="products-container">
        <h1 class="page-header">Browse Our Products</h1>

        <div class="search-filter-bar">
            <form action="${pageContext.request.contextPath}/ProductServlet" method="get" style="display:flex;">
                <input type="text" name="search" placeholder="Search products..." value="${param.search != null ? param.search : ''}">
                <%-- Example Category Filter (Needs CategoryManager and categories attribute from servlet)
                <select name="category">
                    <option value="">All Categories</option>
                    <c:forEach var="cat" items="${categories}">
                        <option value="${cat.categoryId}" ${param.category == cat.categoryId ? 'selected' : ''}>${cat.categoryName}</option>
                    </c:forEach>
                </select>
                --%>
                <button type="submit">Search</button>
            </form>
        </div>

        <% if (errorMessage != null) { %>
            <div class="error-message-bar"><%= errorMessage %></div>
        <% } %>

        <% if (products != null && !products.isEmpty()) { %>
            <div class="product-grid">
                <% for (Product product : products) { %>
                    <div class="product-card">
                        <%-- Use a default image if product.getImageUrl() is null or empty --%>
                        <img src="${product.getImageUrl() != null && !product.getImageUrl().isEmpty() ? product.getImageUrl() : pageContext.request.contextPath += '/images/default_product.png'}" alt="<%= product.getName() %>">
                        <h3><%= product.getName() %></h3>
                        <p class="brand"><%= product.getBrand() != null ? product.getBrand() : "N/A" %></p>
                        <p class="price"><%= currencyFormatter.format(product.getPrice()) %></p>
                        <p class="stock <%= product.getStock() < 10 && product.getStock() > 0 ? "low" : "" %>">
                            <% if (product.getStock() > 0) { %>
                                Stock: <%= product.getStock() %> available <%= product.getStock() < 10 ? " (Low Stock!)" : "" %>
                            <% } else { %>
                                <span style="color:red; font-weight:bold;">Out of Stock</span>
                            <% } %>
                        </p>

                        <% if (product.getStock() > 0) { %>
                            <form action="${pageContext.request.contextPath}/CartServlet" method="post" class="add-to-cart-form">
                                <input type="hidden" name="action" value="add">
                                <input type="hidden" name="productId" value="<%= product.getProductId() %>">
                                <input type="number" name="quantity" value="1" min="1" max="<%= product.getStock() %>" required>
                                <button type="submit" class="btn-add-to-cart">Add to Cart</button>
                            </form>
                        <% } else { %>
                             <button type="button" class="btn-add-to-cart" disabled style="background-color: #ccc;">Add to Cart</button>
                        <% } %>
                    </div>
                <% } %>
            </div>
        <% } else if (errorMessage == null) { %>
            <p class="no-products">No products found matching your criteria or the store is currently empty.</p>
        <% } %>
    </div>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
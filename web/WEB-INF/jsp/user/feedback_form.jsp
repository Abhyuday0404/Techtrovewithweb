<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="app.servlets.user.UserFeedbackServlet.ProductSelectionItem" %> <%-- Import the inner class --%>

<%
    List<ProductSelectionItem> orderedProducts = (List<ProductSelectionItem>) request.getAttribute("orderedProducts");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String successMessage = (String) request.getAttribute("successMessage");
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>TechTrove - Submit Feedback</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .feedback-container { padding: 20px; max-width: 600px; margin: auto; }
        .page-header { text-align: center; margin-bottom: 20px; color: #333; }
        .message-bar { padding: 10px; border-radius: 4px; margin-bottom: 15px; text-align: center; }
        .error-message-bar { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .success-message-bar { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }

        .feedback-form-box { background-color: #fff; border: 1px solid #ddd; border-radius: 8px; padding: 25px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
        .form-group { margin-bottom: 20px; }
        .form-group label { display: block; margin-bottom: 8px; font-weight: bold; color: #555; }
        .form-group select, .form-group textarea, .form-group .rating-stars input[type="radio"] {
            width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; box-sizing: border-box;
        }
        .form-group textarea { min-height: 100px; resize: vertical; }
        .rating-stars { display: flex; justify-content: flex-start; flex-direction: row-reverse; /* To make stars select left-to-right visually */ }
        .rating-stars input[type="radio"] { display: none; } /* Hide actual radio buttons */
        .rating-stars label {
            font-size: 2em; color: #ddd; cursor: pointer; padding: 0 2px;
            transition: color 0.2s;
        }
        /* Star interaction: when a radio is checked, all preceding (in DOM, visually succeeding due to row-reverse) labels change color */
        .rating-stars input[type="radio"]:checked ~ label,
        .rating-stars label:hover,
        .rating-stars label:hover ~ label { /* Hover effect */
            color: #ffc107; /* Gold color for selected/hovered stars */
        }
        /* Ensure stars to the right of the hovered one (visually left) stay gold if a selection is already made */
        .rating-stars input[type="radio"]:checked + label:hover ~ label {
             color: #ffc107;
        }


        .btn-submit-feedback {
            background-color: #007bff; color: white; padding: 12px 25px;
            text-decoration: none; border-radius: 5px; font-size: 1.1em;
            border: none; cursor: pointer; display: block; width: 100%;
            transition: background-color 0.3s ease;
        }
        .btn-submit-feedback:hover { background-color: #0056b3; }
        .no-products-message { text-align: center; font-style: italic; color: #777; margin-top: 15px; }
    </style>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/user_navbar.jsp" />

    <div class="feedback-container">
        <h1 class="page-header">Share Your Feedback</h1>

        <% if (successMessage != null) { %>
            <div class="message-bar success-message-bar"><%= successMessage %></div>
        <% } %>
        <% if (errorMessage != null) { %>
            <div class="message-bar error-message-bar"><%= errorMessage %></div>
        <% } %>

        <div class="feedback-form-box">
            <form action="${pageContext.request.contextPath}/UserFeedbackServlet" method="post">
                <div class="form-group">
                    <label for="productId">Select Product:</label>
                    <select id="productId" name="productId" required <%= (orderedProducts == null || orderedProducts.isEmpty()) ? "disabled" : "" %>>
                        <option value="">-- Choose a product you've ordered --</option>
                        <% if (orderedProducts != null) {
                            for (ProductSelectionItem item : orderedProducts) { %>
                                <option value="<%= item.getProductId() %>"><%= item.getDisplayName() %></option>
                        <%  }
                           }
                        %>
                    </select>
                    <% if (orderedProducts == null || orderedProducts.isEmpty() && errorMessage == null) { %>
                        <p class="no-products-message">You haven't ordered any products yet, or products are still loading.</p>
                    <% } %>
                </div>

                <div class="form-group">
                    <label>Your Rating (1=Poor, 5=Excellent):</label>
                    <div class="rating-stars">
                        <%-- Stars are in reverse order for CSS trick with ~ selector --%>
                        <input type="radio" id="star5" name="rating" value="5" required><label for="star5">★</label>
                        <input type="radio" id="star4" name="rating" value="4"><label for="star4">★</label>
                        <input type="radio" id="star3" name="rating" value="3"><label for="star3">★</label>
                        <input type="radio" id="star2" name="rating" value="2"><label for="star2">★</label>
                        <input type="radio" id="star1" name="rating" value="1"><label for="star1">★</label>
                    </div>
                </div>

                <div class="form-group">
                    <label for="comment">Your Comments (optional):</label>
                    <textarea id="comment" name="comment" rows="4" placeholder="Tell us more about your experience..."></textarea>
                </div>

                <button type="submit" class="btn-submit-feedback" <%= (orderedProducts == null || orderedProducts.isEmpty()) ? "disabled" : "" %>>Submit Feedback</button>
            </form>
        </div>
    </div>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
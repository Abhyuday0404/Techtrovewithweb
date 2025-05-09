<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="models.User" %>
<%
    User user = (User) request.getAttribute("user");
    if (user == null) {
        // Fallback if user attribute is not set, though servlet should handle this
        response.sendRedirect(request.getContextPath() + "/LoginServlet");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>TechTrove - User Dashboard</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        /* Basic Dashboard Styles - Will be expanded */
        .dashboard-container { padding: 20px; max-width: 1200px; margin: auto; }
        .welcome-header { text-align: center; margin-bottom: 30px; color: #333; }
        .dashboard-content { background-color: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .quick-links { list-style-type: none; padding: 0; display: flex; flex-wrap: wrap; justify-content: space-around; }
        .quick-links li { margin: 10px; }
        .quick-links a {
            display: block; text-decoration: none; color: #fff; background-color: #007bff;
            padding: 15px 25px; border-radius: 5px; text-align: center;
            transition: background-color 0.3s ease; min-width: 180px;
        }
        .quick-links a:hover { background-color: #0056b3; }
    </style>
</head>
<body>
    <%-- Include User Navbar --%>
    <jsp:include page="/WEB-INF/jsp/common/user_navbar.jsp" />

    <div class="dashboard-container">
        <h1 class="welcome-header">Welcome to Your Dashboard, <%= user.getFullName() != null ? user.getFullName() : user.getUserId() %>!</h1>

        <div class="dashboard-content">
            <h2>Quick Links</h2>
            <ul class="quick-links">
                <li><a href="${pageContext.request.contextPath}/ProductServlet">Browse Products</a></li>
                <li><a href="${pageContext.request.contextPath}/CartServlet">View Your Cart</a></li>
                <li><a href="${pageContext.request.contextPath}/OrderHistoryServlet">Order History</a></li>
                <li><a href="${pageContext.request.contextPath}/UserFeedbackServlet">Give Feedback</a></li>
            </ul>

            <p style="text-align: center; margin-top: 30px;">
                This is your central hub for managing your TechTrove activities.
            </p>
            <%-- More dashboard content will be added here later --%>
        </div>
    </div>

    <%-- Include Footer --%>
    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
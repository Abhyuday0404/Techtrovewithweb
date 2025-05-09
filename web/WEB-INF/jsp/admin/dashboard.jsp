<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="models.User" %>
<%
    User adminUser = (User) request.getAttribute("adminUser");
    if (adminUser == null) {
        // Fallback, though servlet should prevent this
        response.sendRedirect(request.getContextPath() + "/LoginServlet");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>TechTrove - Admin Dashboard</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        /* Reusing some styles from user dashboard for consistency, can be specialized */
        .dashboard-container { padding: 20px; max-width: 1200px; margin: auto; }
        .welcome-header { text-align: center; margin-bottom: 30px; color: #333; }
        .dashboard-content { background-color: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
        .admin-quick-links { list-style-type: none; padding: 0; display: flex; flex-wrap: wrap; justify-content: space-around; }
        .admin-quick-links li { margin: 10px; }
        .admin-quick-links a {
            display: block; text-decoration: none; color: #fff; background-color: #17a2b8; /* Admin accent color */
            padding: 15px 25px; border-radius: 5px; text-align: center;
            transition: background-color 0.3s ease; min-width: 200px;
        }
        .admin-quick-links a:hover { background-color: #138496; } /* Darker admin accent */
        .stats-overview { /* Placeholder for future stats cards */
            display: flex;
            justify-content: space-around;
            margin-top: 30px;
            text-align: center;
        }
        .stat-card {
            background-color: #e9ecef;
            padding: 20px;
            border-radius: 5px;
            width: 22%;
        }
        .stat-card h3 { margin-top: 0; color: #495057; }
        .stat-card p { font-size: 1.5em; font-weight: bold; color: #007bff; }
    </style>
</head>
<body>
    <%-- Include Admin Navbar --%>
    <jsp:include page="/WEB-INF/jsp/common/admin_navbar.jsp" />

    <div class="dashboard-container">
        <h1 class="welcome-header">Admin Dashboard</h1>
        <p style="text-align:center; font-size:1.1em; margin-bottom:25px;">Welcome, <%= adminUser.getFullName() != null ? adminUser.getFullName() : adminUser.getUserId() %>!</p>

        <div class="dashboard-content">
            <h2>Management Tools</h2>
            <ul class="admin-quick-links">
                <li><a href="${pageContext.request.contextPath}/AdminProductServlet">Manage Products</a></li>
                <li><a href="${pageContext.request.contextPath}/AdminOrderServlet">View Orders</a></li>
                <li><a href="${pageContext.request.contextPath}/AdminFeedbackServlet">View Feedback</a></li>
                <%-- <li><a href="#">Manage Users</a></li> --%>
                <%-- <li><a href="#">Site Settings</a></li> --%>
            </ul>

            <%-- Placeholder for overview statistics --%>
            <div class="stats-overview" style="display:none;"> <%-- Hidden for now --%>
                <div class="stat-card">
                    <h3>Total Products</h3>
                    <p>[Count]</p>
                </div>
                <div class="stat-card">
                    <h3>Pending Orders</h3>
                    <p>[Count]</p>
                </div>
                <div class="stat-card">
                    <h3>Total Users</h3>
                    <p>[Count]</p>
                </div>
                <div class="stat-card">
                    <h3>New Feedback</h3>
                    <p>[Count]</p>
                </div>
            </div>
        </div>
    </div>

    <%-- Include Footer --%>
    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
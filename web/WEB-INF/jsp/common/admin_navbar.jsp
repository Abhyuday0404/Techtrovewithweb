<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="models.User" %>
<%
    User loggedInAdmin = (User) session.getAttribute("loggedInUser");
    String adminName = "Admin"; // Default if something goes wrong
    if (loggedInAdmin != null) {
        adminName = loggedInAdmin.getFullName() != null ? loggedInAdmin.getFullName() : loggedInAdmin.getUserId();
    }
%>
<%-- Reusing navbar styles from user_navbar.jsp, but can be customized --%>
<style>
    .admin-navbar { /* Specific class if different styling needed */
        background-color: #20232a; /* Darker, distinct admin theme */
        padding: 10px 20px;
        overflow: hidden;
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    .admin-navbar a {
        float: left;
        display: block;
        color: #e6e6e6; /* Lighter text for darker bg */
        text-align: center;
        padding: 14px 16px;
        text-decoration: none;
        font-size: 17px;
        transition: background-color 0.3s ease, color 0.3s ease;
    }
    .admin-navbar a:hover {
        background-color: #17a2b8; /* Admin accent color */
        color: white;
    }
    .admin-navbar a.active {
        background-color: #17a2b8;
        color: white;
    }
    .admin-navbar .app-title {
        font-size: 22px;
        font-weight: bold;
        color: #61dafb; /* Different accent for admin title */
    }
    .admin-navbar .user-info {
        float: right;
        color: #f8f9fa;
        padding: 14px 16px;
    }
    .admin-navbar .logout-link {
        float: right;
        background-color: #dc3545; /* Red for logout */
    }
    .admin-navbar .logout-link:hover {
        background-color: #c82333; /* Darker red on hover */
    }
</style>

<div class="admin-navbar"> <%-- Changed class for potentially different styling --%>
    <a href="${pageContext.request.contextPath}/AdminDashboardServlet" class="app-title">TechTrove - Admin</a>
    <a href="${pageContext.request.contextPath}/AdminDashboardServlet">Dashboard</a>
    <a href="${pageContext.request.contextPath}/AdminProductServlet">Products</a>
    <a href="${pageContext.request.contextPath}/AdminOrderServlet">Orders</a>
    <a href="${pageContext.request.contextPath}/AdminFeedbackServlet">Feedback</a>
    <%-- <a href="#">Users</a> --%>

    <% if (loggedInAdmin != null) { %>
        <a href="${pageContext.request.contextPath}/LogoutServlet" class="logout-link">Logout</a>
        <span class="user-info">Admin: <%= adminName %></span>
    <% } else { %>
        <a href="${pageContext.request.contextPath}/LoginServlet" style="float:right;">Login</a>
    <% } %>
</div>
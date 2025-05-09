<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="models.User" %>
<%
    User loggedInUser = (User) session.getAttribute("loggedInUser");
    String userName = "Guest";
    if (loggedInUser != null) {
        userName = loggedInUser.getFullName() != null ? loggedInUser.getFullName() : loggedInUser.getUserId();
    }
%>
<style>
    .navbar {
        background-color: #343a40; /* Dark background */
        padding: 10px 20px;
        overflow: hidden;
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    .navbar a {
        float: left;
        display: block;
        color: #f2f2f2; /* Light text */
        text-align: center;
        padding: 14px 16px;
        text-decoration: none;
        font-size: 17px;
        transition: background-color 0.3s ease, color 0.3s ease;
    }
    .navbar a:hover {
        background-color: #007bff; /* Primary color on hover */
        color: white;
    }
    .navbar a.active { /* Style for the active/current link */
        background-color: #007bff;
        color: white;
    }
    .navbar .app-title {
        font-size: 22px;
        font-weight: bold;
        color: #00d4ff; /* A bright accent for the title */
    }
    .navbar .user-info {
        float: right;
        color: #f8f9fa;
        padding: 14px 16px;
    }
    .navbar .logout-link {
        float: right;
        background-color: #dc3545; /* Red for logout */
    }
    .navbar .logout-link:hover {
        background-color: #c82333; /* Darker red on hover */
    }
</style>

<div class="navbar">
    <a href="${pageContext.request.contextPath}/UserDashboardServlet" class="app-title">TechTrove</a>
    <a href="${pageContext.request.contextPath}/UserDashboardServlet" <%-- Add 'active' class based on current page later --%>>Dashboard</a>
    <a href="${pageContext.request.contextPath}/ProductServlet">Products</a>
    <a href="${pageContext.request.contextPath}/CartServlet">Cart</a>
    <a href="${pageContext.request.contextPath}/OrderHistoryServlet">Orders</a>
    <a href="${pageContext.request.contextPath}/UserFeedbackServlet">Feedback</a>

    <% if (loggedInUser != null) { %>
        <a href="${pageContext.request.contextPath}/LogoutServlet" class="logout-link">Logout</a>
        <span class="user-info">Logged in as: <%= userName %></span>
    <% } else { %>
        <a href="${pageContext.request.contextPath}/LoginServlet" style="float:right;">Login</a>
    <% } %>
</div>
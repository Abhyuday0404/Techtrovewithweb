<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="models.User" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>TechTrove - Login</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; display: flex; justify-content: center; align-items: center; min-height: 90vh; }
        .container { background-color: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.1); width: 100%; max-width: 400px; }
        h1 { text-align: center; color: #333; margin-bottom: 25px; }
        .form-group { margin-bottom: 20px; }
        .form-group label { display: block; margin-bottom: 8px; color: #555; font-weight: bold; }
        .form-group input[type="email"], .form-group input[type="password"], .form-group select {
            width: calc(100% - 22px); padding: 10px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box;
        }
        .form-group input[type="submit"] {
            background-color: #28a745; color: white; padding: 12px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; width: 100%;
        }
        .form-group input[type="submit"]:hover { background-color: #218838; }
        .message-bar { padding: 10px; border-radius: 4px; margin-bottom: 15px; text-align: center; }
        .error-message { color: #d9534f; background-color: #f2dede; border: 1px solid #ebccd1; }
        .info-message { color: #004085; background-color: #cce5ff; border: 1px solid #b8daff; }
        .success-message { color: #155724; background-color: #d4edda; border: 1px solid #c3e6cb;}
    </style>
</head>
<body>
    <div class="container">
        <h1>TechTrove Access</h1>

        <%-- Display any login error messages from request (set by GET) or session (set by filter/POST) --%>
        <% String loginError = (String) request.getAttribute("loginError");
           if (loginError == null) loginError = (String) session.getAttribute("loginError"); // Check session as fallback
           if (loginError != null) {
        %>
            <p class="message-bar error-message"><%= loginError %></p>
        <%
            if (session.getAttribute("loginError") != null) session.removeAttribute("loginError"); // Clear from session if it was there
           }
        %>

        <%-- Display logout message --%>
        <% String logoutMessage = (String) request.getAttribute("logoutMessage");
           if (logoutMessage != null) {
        %>
            <p class="message-bar success-message"><%= logoutMessage %></p>
        <% } %>


        <p class="message-bar info-message">
            Select your role to proceed. <br/>
            (No email/password required for this demonstration version - uses default IDs)
        </p>

        <%-- For debugging redirectAfterLogin
        String redirectUrl = (String) session.getAttribute("redirectAfterLogin");
        if (redirectUrl != null) {
        %>
            <p style="font-size:0.8em; color:grey; text-align:center;">Debug: Will redirect to <%= redirectUrl %> after login.</p>
        <%
        }
        --%>


        <form action="${pageContext.request.contextPath}/LoginServlet" method="post">
            <div class="form-group">
                <label for="role">Login as:</label>
                <select id="role" name="role" required>
                    <option value="">-- Select Role --</option>
                    <option value="user">User (Customer)</option>
                    <option value="admin">Administrator</option>
                </select>
            </div>

            <div class="form-group">
                <label for="email">Email (optional for demo):</label>
                <input type="email" id="email" name="email" placeholder="Enter your email">
            </div>
            <div class="form-group">
                <label for="password">Password (optional for demo):</label>
                <input type="password" id="password" name="password" placeholder="Enter your password">
            </div>

            <div class="form-group">
                <input type="submit" value="Login">
            </div>
        </form>
    </div>
</body>
</html>
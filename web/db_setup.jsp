<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>TechTrove - Database Setup</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; display: flex; justify-content: center; align-items: center; min-height: 90vh; }
        .container { background-color: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.1); width: 100%; max-width: 450px; }
        h1 { text-align: center; color: #333; margin-bottom: 20px; }
        .form-group { margin-bottom: 15px; }
        .form-group label { display: block; margin-bottom: 5px; color: #555; font-weight: bold; }
        .form-group input[type="text"], .form-group input[type="password"] {
            width: calc(100% - 20px); padding: 10px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box;
        }
        .form-group input[type="submit"] {
            background-color: #007bff; color: white; padding: 12px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; width: 100%;
        }
        .form-group input[type="submit"]:hover { background-color: #0056b3; }
        .error-message { color: #d9534f; background-color: #f2dede; border: 1px solid #ebccd1; padding: 10px; border-radius: 4px; margin-bottom: 15px; text-align: center; }
        .success-message { color: #3c763d; background-color: #dff0d8; border: 1px solid #d6e9c6; padding: 10px; border-radius: 4px; margin-bottom: 15px; text-align: center; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Database Configuration</h1>

        <%-- Display any error messages passed from a servlet or previous attempt --%>
        <% String error = request.getParameter("error");
           if (error != null && !error.isEmpty()) {
               String errorMessage = "";
               if ("connection_failed".equals(error)) {
                   errorMessage = "Connection failed. Please check your details and try again. Error: " + request.getAttribute("dbErrorMsg");
               } else if ("init_failed".equals(error)) {
                   errorMessage = "Database configured, but schema initialization failed. Please check server logs and database permissions. Error: " + application.getAttribute("dbInitializationError");
               } else if ("missing_params".equals(error)) {
                   errorMessage = "All fields are required.";
               } else {
                   errorMessage = "An unknown error occurred during setup.";
               }
        %>
            <p class="error-message"><%= errorMessage %></p>
        <% } %>

        <% String success = request.getParameter("success");
           if (success != null && "configured".equals(success)) {
        %>
            <p class="success-message">Database configured and initialized successfully! Redirecting to login...</p>
            <script type="text/javascript">
                setTimeout(function() {
                    window.location.href = '${pageContext.request.contextPath}/LoginServlet'; // Redirect to Login
                }, 3000); // 3 second delay
            </script>
        <% } else { %>
            <p>Please provide your MySQL database connection details. The application will attempt to create the necessary tables if they don't exist.</p>
            <form action="${pageContext.request.contextPath}/DatabaseSetupServlet" method="post"> <%-- We will create this servlet --%>
                <div class="form-group">
                    <label for="host">Database Host:</label>
                    <input type="text" id="host" name="db_host" value="localhost" required>
                </div>
                <div class="form-group">
                    <label for="port">Database Port:</label>
                    <input type="text" id="port" name="db_port" value="3306" required>
                </div>
                <div class="form-group">
                    <label for="dbname">Database Name:</label>
                    <input type="text" id="dbname" name="db_name" value="techtrovedb" required>
                </div>
                <div class="form-group">
                    <label for="user">Database User:</label>
                    <input type="text" id="user" name="db_user" value="root" required>
                </div>
                <div class="form-group">
                    <label for="password">Database Password:</label>
                    <input type="password" id="password" name="db_password">
                </div>
                <div class="form-group">
                    <input type="submit" value="Configure & Initialize Database">
                </div>
            </form>
        <% } %>
    </div>
</body>
</html>
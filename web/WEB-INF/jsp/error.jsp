<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%-- isErrorPage="true" makes the 'exception' implicit object available --%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>TechTrove - Error</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        body { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 90vh; text-align: center; background-color: #f8f9fa;}
        .error-container { background-color: #fff; padding: 30px 40px; border-radius: 8px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); max-width: 600px; }
        .error-container h1 { color: #dc3545; font-size: 2.5em; margin-bottom: 10px; }
        .error-container p { color: #333; font-size: 1.1em; margin-bottom: 20px; }
        .error-details { background-color: #f1f1f1; border: 1px solid #ddd; padding: 15px; border-radius: 4px; text-align: left; font-size: 0.9em; color: #555; max-height: 200px; overflow-y: auto; }
        .error-details pre { white-space: pre-wrap; word-wrap: break-word; }
        .home-link { display: inline-block; margin-top: 25px; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px; }
        .home-link:hover { background-color: #0056b3; }
    </style>
</head>
<body>
    <div class="error-container">
        <h1>Oops! Something Went Wrong</h1>
        <%
            String statusCode = request.getAttribute("javax.servlet.error.status_code") != null ?
                                request.getAttribute("javax.servlet.error.status_code").toString() : "N/A";
            String errorMessage = request.getAttribute("javax.servlet.error.message") != null ?
                                  (String) request.getAttribute("javax.servlet.error.message") : "An unexpected error occurred.";
            String requestUri = request.getAttribute("javax.servlet.error.request_uri") != null ?
                                (String) request.getAttribute("javax.servlet.error.request_uri") : "N/A";

            // If isErrorPage="true", the 'exception' object is available
            Throwable throwable = exception; // or (Throwable) request.getAttribute("javax.servlet.error.exception");

            if ("N/A".equals(statusCode) && throwable == null) {
                 errorMessage = "An unspecified error has occurred.";
            } else if (throwable != null && "An unexpected error occurred.".equals(errorMessage)) {
                errorMessage = throwable.getMessage() != null ? throwable.getMessage() : throwable.getClass().getSimpleName();
            }
        %>
        <p>We apologize for the inconvenience. We've encountered an issue.</p>
        <p><strong>Status Code:</strong> <%= statusCode %></p>
        <p><strong>Error:</strong> <%= errorMessage %></p>
        <p><strong>Requested URI:</strong> <%= requestUri %></p>

        <% if (throwable != null) { %>
            <div class="error-details">
                <h4>Technical Details:</h4>
                <pre><%= throwable.toString() %></pre>
                <%-- For security, only show full stack trace in development environments
                <%
                    java.io.StringWriter sw = new java.io.StringWriter();
                    java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                    throwable.printStackTrace(pw);
                    out.println("<pre>" + sw.toString() + "</pre>");
                %>
                --%>
            </div>
        <% } %>

        <a href="${pageContext.request.contextPath}/index.jsp" class="home-link">Go to Homepage</a>
    </div>
</body>
</html>
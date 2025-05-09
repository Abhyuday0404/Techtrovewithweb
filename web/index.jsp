<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="db.DBUtil" %>
<%
    // Check if database configuration is needed
    // This attribute is set by AppContextListener
    Boolean dbConfigNeeded = (Boolean) application.getAttribute("dbConfigNeeded");
    Boolean dbInitializationFailed = (Boolean) application.getAttribute("dbInitializationFailed");

    String targetPage;

    if (dbConfigNeeded != null && dbConfigNeeded) {
        // If DBUtil is not configured, redirect to a setup page
        targetPage = "db_setup.jsp"; // We will create this page next
        System.out.println("index.jsp: DB configuration needed. Redirecting to " + targetPage);
    } else if (dbInitializationFailed != null && dbInitializationFailed) {
        // If DB was configured but initialization FAILED, show an error or redirect to setup with error
        System.out.println("index.jsp: DB initialization failed. Redirecting to db_setup.jsp with error.");
        targetPage = "db_setup.jsp?error=init_failed"; // Pass error flag
    }
    else {
        // If DB is configured and (presumably) initialized, proceed to login
        targetPage = getServletContext().getContextPath() + "/WEB-INF/jsp/login.jsp"; // Go to login
        // Better: redirect to a LoginServlet which then forwards to login.jsp
        // For now, let's try direct JSP forwarding to see if it works, then switch to servlet if needed
        // Or even simpler:
        targetPage = "LoginServlet"; // We'll create this servlet to handle login display
        System.out.println("index.jsp: DB configured. Redirecting to " + targetPage);
    }
    response.sendRedirect(targetPage);
%>
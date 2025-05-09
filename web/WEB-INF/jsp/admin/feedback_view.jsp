<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="models.Feedback" %>
<%@ page import="java.time.format.DateTimeFormatter" %>

<%
    List<Feedback> feedbackList = (List<Feedback>) request.getAttribute("feedbackList");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String successMessage = (String) request.getAttribute("successMessage"); // For future admin actions

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
%>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>TechTrove - View Customer Feedback</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .admin-content-container { padding: 20px; max-width: 1200px; margin: auto; }
        .page-header { text-align: center; margin-bottom: 20px; color: #333; }
        .message-bar { padding: 10px; border-radius: 4px; margin-bottom: 15px; text-align: center; }
        .error-message-bar { background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
        .success-message-bar { background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb; }

        .admin-table { width: 100%; border-collapse: collapse; background-color: #fff; box-shadow: 0 2px 5px rgba(0,0,0,0.05); font-size: 0.9em; }
        .admin-table th, .admin-table td { border: 1px solid #ddd; padding: 8px; text-align: left; vertical-align: top;}
        .admin-table th { background-color: #e9ecef; font-weight: bold; color: #495057; }
        .admin-table td.feedback-message { max-width: 300px; word-wrap: break-word; }
        .admin-table .actions button {
            padding: 5px 8px; text-decoration: none; border-radius: 3px; font-size: 0.85em;
            border: none; cursor: pointer; background-color: #dc3545; color:white;
        }
         .admin-table .actions button:hover { background-color: #c82333; }
        .no-data-message { text-align: center; padding: 20px; font-style: italic; color: #777;}
    </style>
    <script>
        function confirmDeleteFeedback(feedbackId) {
            if (confirm("Are you sure you want to delete feedback ID: " + feedbackId + "?")) {
                var form = document.createElement('form');
                form.method = 'post';
                form.action = '${pageContext.request.contextPath}/AdminFeedbackServlet';
                
                var actionInput = document.createElement('input');
                actionInput.type = 'hidden'; actionInput.name = 'action'; actionInput.value = 'delete';
                form.appendChild(actionInput);

                var idInput = document.createElement('input');
                idInput.type = 'hidden'; idInput.name = 'feedbackId'; idInput.value = feedbackId;
                form.appendChild(idInput);
                
                document.body.appendChild(form);
                form.submit();
            }
            return false;
        }
    </script>
</head>
<body>
    <jsp:include page="/WEB-INF/jsp/common/admin_navbar.jsp" />

    <div class="admin-content-container">
        <h1 class="page-header">Customer Feedback</h1>

        <% if (successMessage != null) { %>
            <div class="message-bar success-message-bar"><%= successMessage %></div>
        <% } %>
        <% if (errorMessage != null) { %>
            <div class="message-bar error-message-bar"><%= errorMessage %></div>
        <% } %>

        <% if (feedbackList != null && !feedbackList.isEmpty()) { %>
            <table class="admin-table">
                <thead>
                    <tr>
                        <th>Feedback ID</th>
                        <th>User</th>
                        <th>Product</th>
                        <th>Rating</th>
                        <th style="width:35%;">Comment</th>
                        <th>Date</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (Feedback fb : feedbackList) { %>
                        <tr>
                            <td><%= fb.getFeedbackId() %></td>
                            <td>
                                <%= fb.getUserName() != null ? fb.getUserName() : "(N/A)" %><br>
                                <small>(<%= fb.getUserId() != null ? fb.getUserId() : "User ID N/A" %>)</small>
                            </td>
                            <td>
                                <%= fb.getProductName() != null ? fb.getProductName() : "(N/A)" %><br>
                                <small>(<%= fb.getProductId() != null ? fb.getProductId() : "Product ID N/A" %>)</small>
                            </td>
                            <td><%= fb.getRating() %> / 5</td>
                            <td class="feedback-message"><%= fb.getMessage() != null && !fb.getMessage().isEmpty() ? fb.getMessage() : "<em>No comment</em>" %></td>
                            <td><%= fb.getTimestamp() != null ? fb.getTimestamp().format(dateTimeFormatter) : "N/A" %></td>
                            <td class="actions">
                                <%-- Delete button (functionality in servlet is placeholder) --%>
                                <button onclick="return confirmDeleteFeedback('<%= fb.getFeedbackId() %>');" title="Delete Feedback">Delete</button>
                            </td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        <% } else if (errorMessage == null) { %>
            <p class="no-data-message">No feedback has been submitted yet.</p>
        <% } %>
    </div>

    <jsp:include page="/WEB-INF/jsp/common/footer.jsp" />
</body>
</html>
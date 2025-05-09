<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<style>
    .footer {
        background-color: #343a40; /* Dark background, same as navbar */
        color: #f8f9fa; /* Light text */
        text-align: center;
        padding: 20px 0;
        margin-top: 40px; /* Space above the footer */
        font-size: 0.9em;
        border-top: 3px solid #007bff; /* Primary color accent line */
    }
    .footer p {
        margin: 5px 0;
    }
    .footer a {
        color: #00d4ff; /* Accent color for links */
        text-decoration: none;
    }
    .footer a:hover {
        text-decoration: underline;
    }
</style>

<div class="footer">
    <p>© <%= new java.util.GregorianCalendar().get(java.util.Calendar.YEAR) %> TechTrove Inc. All Rights Reserved.</p>
    <p>Your one-stop shop for the latest electronics!</p>
    <p>
        <a href="#">About Us</a> |
        <a href="#">Contact</a> |
        <a href="#">Privacy Policy</a> |
        <a href="#">Terms of Service</a>
    </p>
</div>
package app.filters;

import models.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// Apply this filter to relevant URL patterns.
// For simplicity, we might start broad and then refine.
// Or define specific protected paths.
// Example: @WebFilter(filterName = "AuthenticationFilter", urlPatterns = {"/user/*", "/admin/*", "/cart", "/checkout", "/orders", "/feedback"})
// For now, let's make it more targeted and check within the filter logic.
@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {"/*"}) // Apply to all, then exclude public
public class AuthenticationFilter implements Filter {

    // Paths that do NOT require authentication
    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList(
            "/LoginServlet", "/login", // Login page and its servlet
            "/DatabaseSetupServlet", "/db_setup.jsp", // Database setup
            "/index.jsp", // Welcome page (might redirect to login or setup)
            "/css/", "/images/", "/js/" // Static resources
    ));

    // Paths that require ADMIN role
    private static final Set<String> ADMIN_PATHS = new HashSet<>(Arrays.asList(
            "/AdminDashboardServlet", "/admin/dashboard",
            "/AdminProductServlet", "/admin/products",
            "/AdminOrderServlet", "/admin/orders",
            "/AdminFeedbackServlet", "/admin/feedback"
            // Add other admin-specific servlet URL patterns here
    ));

    // Paths that require USER role (or any authenticated user if not admin)
    private static final Set<String> USER_PATHS = new HashSet<>(Arrays.asList(
            "/UserDashboardServlet", "/user/dashboard",
            "/ProductServlet", "/products", // Viewing products
            "/CartServlet", "/cart",
            "/CheckoutServlet", "/checkout",
            "/OrderHistoryServlet", "/user/orders",
            "/UserFeedbackServlet", "/user/feedback"
            // Add other user-specific servlet URL patterns here
    ));


    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("AuthenticationFilter: Initialized.");
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        HttpSession session = request.getSession(false); // Don't create session if it doesn't exist

        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();
        String path = requestURI.substring(contextPath.length()); // Get path relative to context root

        System.out.println("AuthenticationFilter: Intercepting request for: " + path);

        // Allow access to public paths and static resources
        boolean isPublicPath = PUBLIC_PATHS.stream().anyMatch(publicPath -> {
            if (publicPath.endsWith("/")) { // e.g. /css/
                return path.startsWith(publicPath);
            }
            return path.equals(publicPath);
        });

        if (isPublicPath) {
            System.out.println("AuthenticationFilter: Public path, allowing access: " + path);
            chain.doFilter(req, res); // Allow access
            return;
        }

        // Check if user is logged in
        User loggedInUser = null;
        if (session != null) {
            loggedInUser = (User) session.getAttribute("loggedInUser");
        }

        if (loggedInUser == null) {
            System.out.println("AuthenticationFilter: No logged-in user. Redirecting to login for path: " + path);
            session = request.getSession(true); // Create session to store attempted URL
            session.setAttribute("redirectAfterLogin", requestURI + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
            session.setAttribute("loginError", "Please log in to access this page.");
            response.sendRedirect(contextPath + "/LoginServlet");
            return;
        }

        // User is logged in, check role-based access
        User.UserRole userRole = loggedInUser.getRole();
        System.out.println("AuthenticationFilter: User " + loggedInUser.getUserId() + " with role " + userRole + " accessing " + path);


        // Check Admin Paths
        boolean isAdminPath = ADMIN_PATHS.stream().anyMatch(adminPath -> path.equals(adminPath) || path.startsWith(adminPath + "/"));
        if (isAdminPath) {
            if (userRole == User.UserRole.ADMIN) {
                System.out.println("AuthenticationFilter: Admin access granted to: " + path);
                chain.doFilter(req, res);
            } else {
                System.out.println("AuthenticationFilter: Admin access DENIED to: " + path + " for user role: " + userRole);
                session.setAttribute("loginError", "Access Denied. Admin privileges required.");
                response.sendRedirect(contextPath + "/LoginServlet"); // Or to a generic access denied page
            }
            return;
        }

        // Check User Paths (any authenticated user can access these, unless it's specifically an admin path)
        // Note: If a path is in both ADMIN_PATHS and USER_PATHS, admin check takes precedence.
        // For general user paths, just being logged in is often enough.
        boolean isUserPath = USER_PATHS.stream().anyMatch(userPath -> path.equals(userPath) || path.startsWith(userPath + "/"));
         if (isUserPath) {
            // Allow any authenticated user (ADMINs are also authenticated users)
            System.out.println("AuthenticationFilter: User access granted to: " + path);
            chain.doFilter(req, res);
            return;
        }

        // If the path is not public, not admin (and user is not admin), and not explicitly user-path,
        // it could be an unmapped authenticated area or a mistake.
        // For safety, if it's not explicitly allowed and user is not admin, deny or redirect.
        // However, if we want any logged-in user to access unspecified paths by default:
        System.out.println("AuthenticationFilter: Path " + path + " not specifically restricted. Allowing access for logged-in user.");
        chain.doFilter(req, res);
        // OR, for stricter control:
        // System.out.println("AuthenticationFilter: Path " + path + " not explicitly mapped. Access denied by default for non-admins.");
        // response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access to this resource is not explicitly granted.");
    }

    public void destroy() {
        System.out.println("AuthenticationFilter: Destroyed.");
    }
}
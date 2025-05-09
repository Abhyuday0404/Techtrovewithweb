package app.servlets.user;

import managers.ProductManager;
import models.Product;
import models.User; // For checking user session

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList; // Import ArrayList

@WebServlet(name = "ProductServlet", urlPatterns = {"/ProductServlet", "/products"})
public class ProductServlet extends HttpServlet {

    private ProductManager productManager;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            productManager = new ProductManager();
            System.out.println("ProductServlet: ProductManager initialized.");
        } catch (SQLException e) {
            System.err.println("ProductServlet: Failed to initialize ProductManager: " + e.getMessage());
            throw new ServletException("Failed to initialize ProductManager", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("ProductServlet: Received GET request.");
        HttpSession session = request.getSession(false);

        // Basic authentication check (can be enhanced by a filter later)
        if (session == null || session.getAttribute("loggedInUser") == null) {
            System.out.println("ProductServlet: No logged-in user. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }
        // User loggedInUser = (User) session.getAttribute("loggedInUser"); // Not strictly needed just for viewing products

        String categoryId = request.getParameter("category");
        String searchQuery = request.getParameter("search");
        String errorMessage = null;
        List<Product> products = new ArrayList<>(); // Initialize to empty list

        try {
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                System.out.println("ProductServlet: Searching for products with query: " + searchQuery);
                products = productManager.searchProductsByName(searchQuery.trim());
            } else if (categoryId != null && !categoryId.trim().isEmpty()) {
                System.out.println("ProductServlet: Fetching products for category ID: " + categoryId);
                // Assuming ProductManager has a getProductsByCategoryId method
                // products = productManager.getProductsByCategoryId(categoryId);
                // For now, if category filter is present but method not implemented, show all or error
                // Let's show all for now and add category filtering to ProductManager later if needed.
                System.out.println("ProductServlet: Category filtering not fully implemented in ProductManager, fetching all products.");
                products = productManager.getAllProducts();
            } else {
                System.out.println("ProductServlet: Fetching all products.");
                products = productManager.getAllProducts();
            }
        } catch (SQLException e) {
            System.err.println("ProductServlet: SQL error fetching products: " + e.getMessage());
            e.printStackTrace();
            errorMessage = "Error retrieving products from the database. Please try again later.";
            // products will remain an empty list
        } catch (Exception e) {
            System.err.println("ProductServlet: Unexpected error fetching products: " + e.getMessage());
            e.printStackTrace();
            errorMessage = "An unexpected error occurred. Please try again later.";
        }

        if (errorMessage != null) {
            request.setAttribute("errorMessage", errorMessage);
        }
        request.setAttribute("products", products);
        // TODO: Fetch categories for a filter dropdown later
        // request.setAttribute("categories", categoryManager.getAllCategories());

        System.out.println("ProductServlet: Forwarding to products.jsp with " + products.size() + " products.");
        request.getRequestDispatcher("/WEB-INF/jsp/user/products.jsp").forward(request, response);
    }

    // doPost could handle adding to cart from this page if buttons are directly on product list
    // For now, we'll assume a separate CartServlet handles cart actions.
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Example: Could be an "add to cart" action
        // String productId = request.getParameter("productId");
        // String quantity = request.getParameter("quantity");
        // if (action.equals("addToCart")) { ... call cartManager ... }
        System.out.println("ProductServlet: Received POST request. Redirecting to GET.");
        doGet(request, response); // Or handle specific actions
    }
}
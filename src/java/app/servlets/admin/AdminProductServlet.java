package app.servlets.admin;

import managers.ProductManager;
import managers.CategoryManager;
import models.Product;
import models.Category;
import models.User;
// import core.IdGenerator; // Not strictly needed here if ProductManager handles ID generation

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
// import java.util.ArrayList; // Not directly used here

@WebServlet(name = "AdminProductServlet", urlPatterns = {"/AdminProductServlet", "/admin/products"})
public class AdminProductServlet extends HttpServlet {

    private ProductManager productManager;
    private CategoryManager categoryManager;

    @Override
    public void init() throws ServletException {
        try {
            productManager = new ProductManager();
            categoryManager = new CategoryManager();
            System.out.println("AdminProductServlet: Initialized ProductManager and CategoryManager.");
        } catch (SQLException e) {
            System.err.println("AdminProductServlet: Error initializing managers: " + e.getMessage());
            throw new ServletException("Error initializing managers", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("AdminProductServlet: GET request received.");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null || loggedInUser.getRole() != User.UserRole.ADMIN) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "list"; // Default action

        try {
            switch (action) {
                case "add_form":
                    System.out.println("AdminProductServlet: Action 'add_form'");
                    showProductForm(request, response, null); // null product for add
                    break;
                case "edit_form":
                    System.out.println("AdminProductServlet: Action 'edit_form'");
                    String productIdToEdit = request.getParameter("productId");
                    Product productToEdit = productManager.getProductById(productIdToEdit);
                    showProductForm(request, response, productToEdit);
                    break;
                case "delete": // Delete is handled by POST in the JSP form for safety, but can be GET with confirmation
                    System.out.println("AdminProductServlet: GET Action 'delete' - should ideally be POST or confirm heavily");
                    // For consistency with the form, this direct GET delete isn't used by product_management.jsp
                    // If you want GET delete, implement confirmation here or ensure it's idempotent
                    // deleteProduct(request, response);
                     response.sendRedirect(request.getContextPath() + "/AdminProductServlet?action=list&error=DeleteViaGETNotRecommended");
                    break;
                case "list":
                default:
                    System.out.println("AdminProductServlet: Action 'list' (default)");
                    listProducts(request, response);
                    break;
            }
        } catch (SQLException e) {
            System.err.println("AdminProductServlet: SQL Error in doGet - " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("adminProductError", "Database error: " + e.getMessage());
            try { // Add try-catch here for listProducts
                listProducts(request, response); // Show list with error
            } catch (ServletException | IOException | SQLException ex) { // Catch potential exceptions from listProducts
                System.err.println("AdminProductServlet: Error trying to display product list after an initial error: " + ex.getMessage());
                // Forward to a generic error page or send a simple error response
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal error occurred while trying to display products.");
            }
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("AdminProductServlet: POST request received.");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null || loggedInUser.getRole() != User.UserRole.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/AdminProductServlet?action=list&error=NoAction");
            return;
        }

        try {
            switch (action) {
                case "add_product":
                    System.out.println("AdminProductServlet: POST Action 'add_product'");
                    addProduct(request, response);
                    break;
                case "update_product":
                    System.out.println("AdminProductServlet: POST Action 'update_product'");
                    updateProduct(request, response);
                    break;
                case "delete": // POST delete is preferred
                    System.out.println("AdminProductServlet: POST Action 'delete'");
                    deleteProduct(request, response);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/AdminProductServlet?action=list&error=InvalidPostAction");
                    break;
            }
        } catch (SQLException e) {
            System.err.println("AdminProductServlet: SQL Error in doPost - " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("adminProductError", "Database operation failed: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/AdminProductServlet?action=list");
        } catch (NumberFormatException e) {
            System.err.println("AdminProductServlet: NumberFormat Error in doPost - " + e.getMessage());
            session.setAttribute("adminProductError", "Invalid number format for price or stock.");
            response.sendRedirect(request.getContextPath() + "/AdminProductServlet?action=list");
        }
    }

    private void listProducts(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        List<Product> products = productManager.getAllProducts();
        request.setAttribute("products", products);

        HttpSession session = request.getSession(false);
        if (session != null) {
            String successMessage = (String) session.getAttribute("adminProductSuccess");
            String errorMessage = (String) session.getAttribute("adminProductError");
            if (successMessage != null) request.setAttribute("adminProductSuccess", successMessage);
            if (errorMessage != null) request.setAttribute("adminProductError", errorMessage);
            session.removeAttribute("adminProductSuccess");
            session.removeAttribute("adminProductError");
        }

        request.getRequestDispatcher("/WEB-INF/jsp/admin/product_management.jsp").forward(request, response);
    }

    private void showProductForm(HttpServletRequest request, HttpServletResponse response, Product product)
            throws SQLException, ServletException, IOException {
        List<Category> categories = categoryManager.getAllCategories();
        request.setAttribute("categories", categories);
        if (product != null) {
            request.setAttribute("product", product);
        }
        request.getRequestDispatcher("/WEB-INF/jsp/admin/product_form.jsp").forward(request, response);
    }

    private void addProduct(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, NumberFormatException {
        String name = request.getParameter("name");
        String brand = request.getParameter("brand");
        String model = request.getParameter("model");
        String description = request.getParameter("description");
        double price = Double.parseDouble(request.getParameter("price"));
        int stock = Integer.parseInt(request.getParameter("stock"));
        String mfgDateStr = request.getParameter("manufactureDate");
        String categoryId = request.getParameter("categoryId");
        String imageUrl = request.getParameter("imageUrl");

        LocalDate manufactureDate = null;
        if (mfgDateStr != null && !mfgDateStr.isEmpty()) {
            try {
                manufactureDate = LocalDate.parse(mfgDateStr);
            } catch (DateTimeParseException e) {
                 System.err.println("Invalid manufacture date format for add: " + mfgDateStr + " - Error: " + e.getMessage());
                 // Optionally set error message and redirect back to form
            }
        }
        if (categoryId != null && categoryId.isEmpty()) {
            categoryId = null;
        }
        if (imageUrl != null && imageUrl.trim().isEmpty()) {
            imageUrl = null;
        }

        Product newProduct = new Product(null, name, brand, model, description, price, stock, manufactureDate, categoryId, imageUrl);
        productManager.addProduct(newProduct);

        request.getSession().setAttribute("adminProductSuccess", "Product '" + name + "' added successfully!");
        response.sendRedirect(request.getContextPath() + "/AdminProductServlet?action=list");
    }

    private void updateProduct(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, NumberFormatException {
        String productId = request.getParameter("productId");
        String name = request.getParameter("name");
        String brand = request.getParameter("brand");
        String model = request.getParameter("model");
        String description = request.getParameter("description");
        double price = Double.parseDouble(request.getParameter("price"));
        int stock = Integer.parseInt(request.getParameter("stock"));
        String mfgDateStr = request.getParameter("manufactureDate");
        String categoryId = request.getParameter("categoryId");
        String imageUrl = request.getParameter("imageUrl");

        LocalDate manufactureDate = null;
        if (mfgDateStr != null && !mfgDateStr.isEmpty()) {
             try {
                manufactureDate = LocalDate.parse(mfgDateStr);
            } catch (DateTimeParseException e) {
                 System.err.println("Invalid manufacture date format for update: " + mfgDateStr + " - Error: " + e.getMessage());
            }
        }
        if (categoryId != null && categoryId.isEmpty()) {
            categoryId = null;
        }
        if (imageUrl != null && imageUrl.trim().isEmpty()) {
            imageUrl = null;
        }

        Product productToUpdate = new Product(productId, name, brand, model, description, price, stock, manufactureDate, categoryId, imageUrl);
        boolean updated = productManager.updateProduct(productToUpdate);

        if(updated) {
            request.getSession().setAttribute("adminProductSuccess", "Product '" + name + "' updated successfully!");
        } else {
            request.getSession().setAttribute("adminProductError", "Failed to update product '" + name + "'. It might not exist.");
        }
        response.sendRedirect(request.getContextPath() + "/AdminProductServlet?action=list");
    }

    private void deleteProduct(HttpServletRequest request, HttpServletResponse response) // This is now called by POST
            throws SQLException, IOException {
        String productId = request.getParameter("productId");
        if (productId == null || productId.trim().isEmpty()) {
            request.getSession().setAttribute("adminProductError", "Product ID for deletion was missing.");
            response.sendRedirect(request.getContextPath() + "/AdminProductServlet?action=list");
            return;
        }

        Product p = productManager.getProductById(productId);
        boolean deleted = productManager.deleteProduct(productId);

        if(deleted && p != null) {
            request.getSession().setAttribute("adminProductSuccess", "Product '" + p.getName() + "' (ID: "+productId+") deleted successfully!");
        } else if (deleted) { // Product existed but could not retrieve its details before delete for some reason
            request.getSession().setAttribute("adminProductSuccess", "Product ID: "+productId+" deleted successfully!");
        } else { // Deletion failed
             request.getSession().setAttribute("adminProductError", "Failed to delete product ID: " + productId + ". It might not exist or has dependencies.");
        }
        response.sendRedirect(request.getContextPath() + "/AdminProductServlet?action=list");
    }
}
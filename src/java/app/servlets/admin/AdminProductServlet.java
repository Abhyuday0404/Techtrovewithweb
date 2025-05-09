package app.servlets.admin;

import managers.ProductManager;
import managers.CategoryManager;
import models.Product;
import models.Category;
import models.User;
import core.IdGenerator; // For generating new Product IDs
import exceptions.InvalidPriceException;
import exceptions.InvalidQuantityException;


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
import java.util.ArrayList;

@WebServlet(name = "AdminProductServlet", urlPatterns = {"/AdminProductServlet", "/admin/products"})
public class AdminProductServlet extends HttpServlet {

    private ProductManager productManager;
    private CategoryManager categoryManager;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            productManager = new ProductManager();
            categoryManager = new CategoryManager();
            System.out.println("AdminProductServlet: Managers initialized.");
        } catch (SQLException e) {
            System.err.println("AdminProductServlet: Failed to initialize managers: " + e.getMessage());
            throw new ServletException("Failed to initialize managers for admin product management", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("AdminProductServlet: Received GET request.");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null || loggedInUser.getRole() != User.UserRole.ADMIN) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "list"; // Default action

        String forwardPage = "/WEB-INF/jsp/admin/product_management.jsp";
        String successMessage = (String) session.getAttribute("productSuccess");
        String errorMessage = (String) session.getAttribute("productError");

        if(successMessage != null) request.setAttribute("successMessage", successMessage);
        if(errorMessage != null) request.setAttribute("errorMessage", errorMessage);
        session.removeAttribute("productSuccess");
        session.removeAttribute("productError");


        try {
            List<Category> categories = categoryManager.getAllCategories();
            request.setAttribute("categories", categories);

            switch (action) {
                case "add_form":
                case "edit_form":
                    System.out.println("AdminProductServlet: Action=" + action);
                    String productIdToEdit = request.getParameter("id");
                    if ("edit_form".equals(action) && productIdToEdit != null) {
                        Product productToEdit = productManager.getProductById(productIdToEdit);
                        if (productToEdit != null) {
                            request.setAttribute("product", productToEdit);
                        } else {
                             session.setAttribute("productError", "Product with ID " + productIdToEdit + " not found for editing.");
                             response.sendRedirect(request.getContextPath() + "/AdminProductServlet?action=list");
                             return;
                        }
                    }
                    // For 'add_form', product attribute will be null, JSP handles it
                    forwardPage = "/WEB-INF/jsp/admin/product_form.jsp";
                    break;
                case "list":
                default:
                    System.out.println("AdminProductServlet: Action=list (or default)");
                    List<Product> products = productManager.getAllProducts();
                    request.setAttribute("products", products);
                    break;
            }
        } catch (SQLException e) {
            System.err.println("AdminProductServlet GET Error: " + e.getMessage());
            request.setAttribute("errorMessage", "Database error: " + e.getMessage());
             if (!action.equals("list")) { // If error on form load, redirect to list with message
                session.setAttribute("productError", "Error preparing form: " + e.getMessage());
                response.sendRedirect(request.getContextPath() + "/AdminProductServlet?action=list");
                return;
            }
            // For list action, error will be displayed on the list page.
        }
        request.getRequestDispatcher(forwardPage).forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("AdminProductServlet: Received POST request.");
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null || loggedInUser.getRole() != User.UserRole.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            session.setAttribute("productError", "No action specified.");
            response.sendRedirect(request.getContextPath() + "/AdminProductServlet?action=list");
            return;
        }

        try {
            switch (action) {
                case "add_product":
                    handleAddProduct(request, session);
                    break;
                case "update_product":
                    handleUpdateProduct(request, session);
                    break;
                case "delete_product":
                    handleDeleteProduct(request, session);
                    break;
                default:
                    session.setAttribute("productError", "Invalid action: " + action);
                    break;
            }
        } catch (SQLException e) {
            System.err.println("AdminProductServlet POST SQLException: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("productError", "Database operation failed: " + e.getMessage());
        } catch (IllegalArgumentException | DateTimeParseException | InvalidPriceException | InvalidQuantityException e) {
            System.err.println("AdminProductServlet POST Data Error: " + e.getMessage());
            session.setAttribute("productError", "Invalid data provided: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("AdminProductServlet POST Unexpected Error: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("productError", "An unexpected error occurred: " + e.getMessage());
        }

        // Redirect back to the product list page to show messages
        response.sendRedirect(request.getContextPath() + "/AdminProductServlet?action=list");
    }

    private void handleAddProduct(HttpServletRequest request, HttpSession session) throws SQLException, IllegalArgumentException, DateTimeParseException, InvalidPriceException, InvalidQuantityException {
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
            manufactureDate = LocalDate.parse(mfgDateStr); // Assumes yyyy-MM-dd format from <input type="date">
        }
        if (price < 0) throw new InvalidPriceException("Price cannot be negative.");
        if (stock < 0) throw new InvalidQuantityException("Stock cannot be negative.");


        // The ProductManager's addProduct method needs to exist and be implemented
        // For now, assuming a simple Product constructor and ProductManager.addProduct method
        String newProductId = IdGenerator.generateProductId(); // Generate ID before creating product
        Product newProduct = new Product(newProductId, name, brand, model, description, price, stock, manufactureDate, 
                                         (categoryId !=null && categoryId.isEmpty()) ? null : categoryId , 
                                         (imageUrl !=null && imageUrl.isEmpty()) ? null : imageUrl);
        
        // This assumes productManager.addProduct(Product product) exists
        productManager.addProduct(newProduct); // This should be implemented in ProductManager
        session.setAttribute("productSuccess", "Product '" + name + "' added successfully!");
    }

    private void handleUpdateProduct(HttpServletRequest request, HttpSession session) throws SQLException, IllegalArgumentException, DateTimeParseException, InvalidPriceException, InvalidQuantityException {
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

        if (productId == null || productId.isEmpty()) {
            throw new IllegalArgumentException("Product ID is required for update.");
        }
        if (price < 0) throw new InvalidPriceException("Price cannot be negative.");
        if (stock < 0) throw new InvalidQuantityException("Stock cannot be negative.");

        LocalDate manufactureDate = null;
        if (mfgDateStr != null && !mfgDateStr.isEmpty()) {
            manufactureDate = LocalDate.parse(mfgDateStr);
        }
        
        Product productToUpdate = new Product(productId, name, brand, model, description, price, stock, manufactureDate, 
                                             (categoryId !=null && categoryId.isEmpty()) ? null : categoryId, 
                                             (imageUrl !=null && imageUrl.isEmpty()) ? null : imageUrl);

        // This assumes productManager.updateProduct(Product product) exists
        boolean updated = productManager.updateProduct(productToUpdate); // This should be implemented in ProductManager
        if (updated) {
            session.setAttribute("productSuccess", "Product '" + name + "' updated successfully!");
        } else {
            session.setAttribute("productError", "Failed to update product '" + name + "'. It might not exist or no changes were made.");
        }
    }

    private void handleDeleteProduct(HttpServletRequest request, HttpSession session) throws SQLException {
        String productId = request.getParameter("id"); // Usually passed as 'id' from a link
        if (productId == null || productId.isEmpty()) {
            session.setAttribute("productError", "Product ID required for deletion.");
            return;
        }
        // This assumes productManager.deleteProduct(String productId) exists
        boolean deleted = productManager.deleteProduct(productId); // This should be implemented in ProductManager
        if (deleted) {
            session.setAttribute("productSuccess", "Product ID '" + productId + "' deleted successfully!");
        } else {
            session.setAttribute("productError", "Failed to delete product ID '" + productId + "'. It might not exist or cannot be deleted (e.g., order dependencies).");
        }
    }
}
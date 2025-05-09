// src/java/app/servlets/admin/AdminProductServlet.java
package app.servlets.admin;

import managers.ProductManager;
import managers.CategoryManager; 
import models.Product;
import models.Category; 
import models.User;
import core.IdGenerator; // <<< ADDED THIS IMPORT

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
// import java.util.ArrayList; // Not explicitly used

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
        } catch (SQLException e) {
            throw new ServletException("Failed to initialize managers for AdminProductServlet", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null || loggedInUser.getRole() != User.UserRole.ADMIN) {
            response.sendRedirect(request.getContextPath() + "/LoginServlet");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "list"; 

        String targetPage = "/WEB-INF/jsp/admin/product_management.jsp"; 

        try {
            switch (action) {
                case "add_form":
                    request.setAttribute("categories", categoryManager.getAllCategories());
                    targetPage = "/WEB-INF/jsp/admin/product_form.jsp";
                    break;
                case "edit_form":
                    String productIdToEdit = request.getParameter("productId");
                    Product productToEdit = productManager.getProductById(productIdToEdit);
                    if (productToEdit != null) {
                        request.setAttribute("product", productToEdit);
                        request.setAttribute("categories", categoryManager.getAllCategories());
                        targetPage = "/WEB-INF/jsp/admin/product_form.jsp";
                    } else {
                        session.setAttribute("adminProductError", "Product not found for editing.");
                        response.sendRedirect(request.getContextPath() + "/AdminProductServlet?action=list");
                        return;
                    }
                    break;
                case "list":
                default:
                    List<Product> products = productManager.getAllProducts();
                    request.setAttribute("products", products);
                    String successMsg = (String) session.getAttribute("adminProductSuccess");
                    String errorMsg = (String) session.getAttribute("adminProductError");
                    if (successMsg != null) { request.setAttribute("adminProductSuccess", successMsg); session.removeAttribute("adminProductSuccess");}
                    if (errorMsg != null) { request.setAttribute("adminProductError", errorMsg); session.removeAttribute("adminProductError");}
                    targetPage = "/WEB-INF/jsp/admin/product_management.jsp";
                    break;
            }
        } catch (SQLException e) {
            session.setAttribute("adminProductError", "Database error: " + e.getMessage());
            targetPage = "/WEB-INF/jsp/admin/product_management.jsp"; 
            try { request.setAttribute("products", productManager.getAllProducts()); } catch (SQLException ex) {
                 System.err.println("AdminProductServlet: Could not fetch products for error display: " + ex.getMessage());
            }
        }
        request.getRequestDispatcher(targetPage).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loggedInUser = (session != null) ? (User) session.getAttribute("loggedInUser") : null;

        if (loggedInUser == null || loggedInUser.getRole() != User.UserRole.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            response.sendRedirect(request.getContextPath() + "/AdminProductServlet?action=list");
            return;
        }

        try {
            switch (action) {
                case "add_product":
                case "update_product":
                    String productId = request.getParameter("productId"); 
                    String name = request.getParameter("name");
                    String brand = request.getParameter("brand");
                    String model = request.getParameter("model");
                    String description = request.getParameter("description");
                    double price = Double.parseDouble(request.getParameter("price"));
                    int stock = Integer.parseInt(request.getParameter("stock"));
                    String mfgDateStr = request.getParameter("manufactureDate");
                    String categoryId = request.getParameter("categoryId");
                    
                    LocalDate mfgDate = null;
                    if (mfgDateStr != null && !mfgDateStr.isEmpty()) {
                        try { mfgDate = LocalDate.parse(mfgDateStr); }
                        catch (DateTimeParseException e) { 
                            System.err.println("AdminProductServlet: Could not parse mfgDate: " + mfgDateStr + " Error: " + e.getMessage());
                        }
                    }
                    if (categoryId != null && categoryId.isEmpty()) categoryId = null; 
                    
                    Product product = new Product(
                        (productId != null && !productId.isEmpty() ? productId : IdGenerator.generateProductId()), 
                        name, brand, model, description, price, stock, mfgDate, categoryId
                    );

                    if ("add_product".equals(action)) {
                        productManager.addProduct(product);
                        session.setAttribute("adminProductSuccess", "Product '" + name + "' added successfully!");
                    } else { 
                        productManager.updateProduct(product);
                        session.setAttribute("adminProductSuccess", "Product '" + name + "' updated successfully!");
                    }
                    break;
                case "delete":
                    String productIdToDelete = request.getParameter("productId");
                    Product prodToDelete = productManager.getProductById(productIdToDelete); 
                    if (prodToDelete != null) {
                        productManager.deleteProduct(productIdToDelete);
                        session.setAttribute("adminProductSuccess", "Product '" + prodToDelete.getName() + "' (ID: " + productIdToDelete + ") deleted successfully!");
                    } else {
                         session.setAttribute("adminProductError", "Could not delete product. ID " + productIdToDelete + " not found.");
                    }
                    break;
                default:
                    session.setAttribute("adminProductError", "Invalid action specified.");
                    break;
            }
        } catch (NumberFormatException e) {
            session.setAttribute("adminProductError", "Invalid number format for price or stock: " + e.getMessage());
        } catch (SQLException e) {
            session.setAttribute("adminProductError", "Database error processing product: " + e.getMessage());
             e.printStackTrace();
        } catch (Exception e) { 
            session.setAttribute("adminProductError", "An unexpected error occurred: " + e.getMessage());
            e.printStackTrace(); 
        }
        response.sendRedirect(request.getContextPath() + "/AdminProductServlet?action=list");
    }
}
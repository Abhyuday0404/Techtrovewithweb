package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents an electronic product available in the store.
 * Corresponds to a row in the Products database table.
 */
public class Product {
    private String productId;       // Primary key (e.g., PROD_XYZ)
    private String name;            // Product name (e.g., "Laptop XYZ")
    private String brand;           // Manufacturer brand (e.g., "Dell", "Apple")
    private String model;           // Specific model number/name (e.g., "XPS 15 9530")
    private String description;     // Detailed description of the product
    private double price;           // Current selling price
    private int stock;              // Current quantity available in inventory
    private LocalDate manufactureDate; // Date of manufacture (optional)
    private String imageUrl;        // URL or path to the product's image (optional)
    private String categoryId;      // Foreign key linking to the Categories table (optional)

    // Formatter for dates in toString() or other display methods
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Constructs a Product object.
     *
     * @param productId       The unique ID for the product.
     * @param name            The name of the product (required).
     * @param brand           The manufacturer brand (optional).
     * @param model           The specific model (optional).
     * @param description     A description of the product (optional).
     * @param price           The selling price (must be non-negative).
     * @param stock           The available stock quantity (must be non-negative).
     * @param manufactureDate The date of manufacture (optional, can be null).
     * @param categoryId      The ID of the category this product belongs to (optional).
     * @param imageUrl        The URL or path to the product image (optional).
     */
    public Product(String productId, String name, String brand, String model, String description,
                   double price, int stock, LocalDate manufactureDate, String categoryId, String imageUrl) {

        // --- Validation ---
        if (productId == null || productId.trim().isEmpty())
            throw new IllegalArgumentException("Product ID cannot be null or empty.");
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Product name cannot be null or empty.");
        if (price < 0)
            throw new IllegalArgumentException("Product price cannot be negative.");
        if (stock < 0)
            throw new IllegalArgumentException("Product stock cannot be negative.");

        // Assign values
        this.productId = productId;
        this.name = name;
        this.brand = brand; // Allow null
        this.model = model; // Allow null
        this.description = description; // Allow null
        this.price = price;
        this.stock = stock;
        this.manufactureDate = manufactureDate; // Allow null
        this.imageUrl = imageUrl; // Allow null
        this.categoryId = categoryId; // Allow null
    }

    // --- Getters ---
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public LocalDate getManufactureDate() { return manufactureDate; }
    public String getImageUrl() { return imageUrl; }
    public String getCategoryId() { return categoryId; }

    // --- Setters (Allow modification of product details) ---
    public void setProductId(String productId) {
         if (productId == null || productId.trim().isEmpty())
             throw new IllegalArgumentException("Product ID cannot be set to null or empty.");
        this.productId = productId;
    }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty())
             throw new IllegalArgumentException("Product name cannot be set to null or empty.");
        this.name = name;
    }
    public void setBrand(String brand) { this.brand = brand; }
    public void setModel(String model) { this.model = model; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("Product price cannot be negative.");
        this.price = price;
    }
    public void setStock(int stock) {
        if (stock < 0) throw new IllegalArgumentException("Product stock cannot be negative.");
        this.stock = stock;
    }
    public void setManufactureDate(LocalDate manufactureDate) { this.manufactureDate = manufactureDate; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }


    // --- Overrides ---
    @Override
    public String toString() {
        return "Product{" +
               "id='" + productId + '\'' +
               ", name='" + name + '\'' +
               ", brand='" + (brand != null ? brand : "N/A") + '\'' +
               ", model='" + (model != null ? model : "N/A") + '\'' +
               ", price=₹" + String.format("%.2f", price) +
               ", stock=" + stock +
               ", mfgDate=" + (manufactureDate != null ? manufactureDate.format(DATE_FORMATTER) : "N/A") +
               ", catId='" + (categoryId != null ? categoryId : "N/A") + '\'' +
               // Optionally include description snippet or image URL if needed
               '}';
    }

    /**
     * Checks for equality based primarily on the productId.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        // Products are uniquely identified by their ID
        return Objects.equals(productId, product.productId);
    }

    /**
     * Generates a hash code based primarily on the productId.
     */
    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
}
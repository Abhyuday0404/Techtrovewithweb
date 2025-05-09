// src/java/models/Product.java
package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Product {
    private String productId;
    private String name;
    private String brand;
    private String model;
    private String description;
    private double price;
    private int stock;
    private LocalDate manufactureDate;
    private String categoryId;
    // private String imageUrl; // REMOVED

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Product(String productId, String name, String brand, String model, String description,
                   double price, int stock, LocalDate manufactureDate, String categoryId) { // imageUrl REMOVED from parameters
        if (productId == null || productId.trim().isEmpty())
            throw new IllegalArgumentException("Product ID cannot be null or empty.");
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Product Name cannot be null or empty.");
        if (price < 0)
            throw new IllegalArgumentException("Price cannot be negative.");
        if (stock < 0)
            throw new IllegalArgumentException("Stock cannot be negative.");

        this.productId = productId;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.manufactureDate = manufactureDate; // Can be null
        this.categoryId = categoryId;           // Can be null
        // this.imageUrl = imageUrl; // REMOVED
    }

    // Getters
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public LocalDate getManufactureDate() { return manufactureDate; }
    public String getCategoryId() { return categoryId; }
    // public String getImageUrl() { return imageUrl; } // REMOVED

    // Setters
    public void setProductId(String productId) {
        if (productId == null || productId.trim().isEmpty()) throw new IllegalArgumentException("Product ID cannot be empty.");
        this.productId = productId;
    }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Product Name cannot be empty.");
        this.name = name;
    }
    public void setBrand(String brand) { this.brand = brand; }
    public void setModel(String model) { this.model = model; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative.");
        this.price = price;
    }
    public void setStock(int stock) {
        if (stock < 0) throw new IllegalArgumentException("Stock cannot be negative.");
        this.stock = stock;
    }
    public void setManufactureDate(LocalDate manufactureDate) { this.manufactureDate = manufactureDate; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    // public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; } // REMOVED

    @Override
    public String toString() {
        return "Product{" +
               "productId='" + productId + '\'' +
               ", name='" + name + '\'' +
               ", brand='" + (brand != null ? brand : "N/A") + '\'' +
               ", price=₹" + String.format("%.2f", price) +
               ", stock=" + stock +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productId, product.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId);
    }
}
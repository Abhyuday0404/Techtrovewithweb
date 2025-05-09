package models;

/**
 * Represents an electronics category (e.g., Laptops, Smartphones).
 * Corresponds to a row in the Categories database table.
 */
public class Category {
    private String categoryId;   // Primary key (e.g., CAT_LAPTOP)
    private String categoryName; // Display name (e.g., "Laptops")

    /**
     * Constructs a Category object.
     *
     * @param categoryId   The unique ID for the category.
     * @param categoryName The display name for the category.
     */
    public Category(String categoryId, String categoryName) {
        // Validation for required fields
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be null or empty.");
        }
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category Name cannot be null or empty.");
        }
        this.categoryId = categoryId.trim();
        this.categoryName = categoryName.trim();
    }

    // --- Getters ---
    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    // --- Setters ---
    // Setting ID after creation might be discouraged depending on usage.
    public void setCategoryId(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Category ID cannot be set to null or empty.");
        }
        this.categoryId = categoryId.trim();
    }

    public void setCategoryName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category Name cannot be set to null or empty.");
        }
        this.categoryName = categoryName.trim();
    }

    // --- Overrides ---

    /**
     * Returns a string representation suitable for display (e.g., in lists or combo boxes).
     * Example: "Laptops (CAT_LAPTOP)"
     */
    @Override
    public String toString() {
        return categoryName + " (" + categoryId + ")";
    }

    /**
     * Checks for equality based on the categoryId.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        // Categories are uniquely identified by their ID
        return java.util.Objects.equals(categoryId, category.categoryId);
    }

    /**
     * Generates a hash code based on the categoryId.
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(categoryId);
    }
}
package models;

/**
 * Represents an administrator user role link in the database.
 * This class primarily links a User record (via UserID) to admin-specific
 * details like permissions.
 */
public class Admin {
    private String adminId;     // Primary key for the Admins table itself (e.g., ADM_XYZ)
    private String userId;      // Foreign key linking to the Users table (e.g., USR_ABC)
    private String permissions; // Describes admin capabilities (e.g., "ALL", "MANAGE_PRODUCTS,VIEW_ORDERS")

    /**
     * Constructs an Admin object.
     *
     * @param adminId     The unique ID for this admin entry.
     * @param userId      The ID of the User who has admin privileges.
     * @param permissions A description of the admin's permissions.
     */
    public Admin(String adminId, String userId, String permissions) {
         // Basic validation
         if (adminId == null || adminId.trim().isEmpty() || userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin ID and User ID cannot be null or empty.");
        }
        this.adminId = adminId;
        this.userId = userId;
        this.permissions = permissions; // Permissions string can be null or empty initially
    }

    // --- Getters ---
    public String getAdminId() {
        return adminId;
    }

    public String getUserId() {
        return userId;
    }

    public String getPermissions() {
        return permissions;
    }

    // --- Setters ---
    public void setAdminId(String adminId) {
         if (adminId == null || adminId.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin ID cannot be set to null or empty.");
        }
        this.adminId = adminId;
    }

    public void setUserId(String userId) {
         if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be set to null or empty.");
        }
        this.userId = userId;
    }

    public void setPermissions(String permissions) {
        // Allow setting permissions to null or empty if needed
        this.permissions = permissions;
    }

    // --- Overrides ---
    @Override
    public String toString() {
        return "Admin{" +
                "adminId='" + adminId + '\'' +
                ", userId='" + userId + '\'' +
                ", permissions='" + (permissions != null ? permissions : "N/A") + '\'' +
                '}';
    }

    /**
     * Checks if this admin role has a specific permission string.
     * Handles simple comma-separated lists and the special "ALL" permission case (case-insensitive).
     *
     * @param permission The permission string to check for (e.g., "MANAGE_PRODUCTS").
     * @return true if the admin has the permission, false otherwise.
     */
    public boolean hasPermission(String permission) {
        if (this.permissions == null || permission == null || permission.trim().isEmpty()) {
            return false; // No permissions defined or invalid permission requested
        }

        String upperPerms = this.permissions.trim().toUpperCase();
        String upperRequested = permission.trim().toUpperCase();

        // Check for universal permission
        if ("ALL".equals(upperPerms)) {
            return true;
        }

        // Check within a comma-separated list
        String[] grantedPermissions = upperPerms.split(",");
        for (String granted : grantedPermissions) {
            if (upperRequested.equals(granted.trim())) {
                return true; // Found the specific permission
            }
        }

        return false; // Permission not found
    }

     // --- equals() and hashCode() ---
     // Based on adminId as it should be unique for the Admins table entry.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Admin admin = (Admin) o;
        return java.util.Objects.equals(adminId, admin.adminId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(adminId);
    }
}
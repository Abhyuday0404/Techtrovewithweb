package core;

import java.util.UUID;

public class IdGenerator {

    // Constants used by LoginServlet and DatabaseInitializer
    public static final String DEFAULT_USER_ID = "USR_USER_DEF";
    public static final String DEFAULT_ADMIN_ID = "USR_ADMIN_DEF";


    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    public static String generateUserId() {
        // Ensure generated IDs don't conflict with default ones if using a different pattern
        return "USR_" + generateUniqueId().substring(0, 12).replace("-", "").toUpperCase();
    }

    public static String generateAdminId() {
        return "ADM_" + generateUniqueId().substring(0, 12).replace("-", "").toUpperCase();
    }

    public static String generateProductId() {
        return "PROD_" + generateUniqueId().substring(0, 10).replace("-", "").toUpperCase();
    }

    public static String generateCategoryId() {
        return "CAT_" + generateUniqueId().substring(0, 8).replace("-", "").toUpperCase();
    }

    public static String generateCartId() { // For individual cart entries if needed, or user's cart session
        return "CRT_" + generateUniqueId().substring(0, 12).replace("-", "").toUpperCase();
    }

    public static String generateOrderId() {
        return "ORD_" + generateUniqueId().substring(0, 12).replace("-", "").toUpperCase();
    }

    public static String generateOrderDetailId() {
        return "ODTL_" + generateUniqueId().substring(0, 10).replace("-", "").toUpperCase();
    }

    public static String generatePaymentId() {
        return "PAY_" + generateUniqueId().substring(0, 10).replace("-", "").toUpperCase();
    }

    public static String generateFeedbackId() {
        return "FDBK_" + generateUniqueId().substring(0, 10).replace("-", "").toUpperCase();
    }

    public static String generatePurchaseHistoryId() {
        return "PH_" + generateUniqueId().substring(0, 12).replace("-", "").toUpperCase();
    }
}
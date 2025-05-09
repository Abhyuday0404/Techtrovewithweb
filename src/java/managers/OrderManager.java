// src/java/managers/OrderManager.java
package managers;

import db.DBUtil;
import models.Order;
import models.OrderDetail;
import models.Product; 
import core.IdGenerator;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class OrderManager {

    public OrderManager() throws SQLException {
        // Constructor
    }

    // ... (getOrderDetailByOrderId, getOrderById, getOrdersByUserId, getAllOrders methods remain the same) ...
    public List<OrderDetail> getOrderDetailsByOrderId(String orderId) throws SQLException {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT * FROM OrderDetails WHERE OrderID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    details.add(new OrderDetail(
                        rs.getString("OrderDetailID"),
                        rs.getString("OrderID"),
                        rs.getString("ProductID"),
                        rs.getString("ProductName"),
                        rs.getInt("Quantity"),
                        rs.getDouble("PriceAtOrder")
                    ));
                }
            }
        }
        return details;
    }

    public Order getOrderById(String orderId) throws SQLException {
        Order order = null;
        String sql = "SELECT o.*, u.FullName AS CustomerName " +
                     "FROM Orders o " +
                     "LEFT JOIN Users u ON o.UserID = u.UserID " +
                     "WHERE o.OrderID = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp orderTs = rs.getTimestamp("OrderDate");
                    LocalDateTime orderDateTime = (orderTs != null) ? orderTs.toLocalDateTime() : null;
                    
                    order = new Order(
                        rs.getString("OrderID"),
                        rs.getString("UserID"),
                        rs.getString("CustomerName"),
                        orderDateTime,
                        rs.getDouble("TotalAmount")
                    );
                    order.setOrderDetails(getOrderDetailsByOrderId(order.getOrderId()));
                }
            }
        }
        return order;
    }

    public List<Order> getOrdersByUserId(String userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.FullName AS CustomerName " +
                     "FROM Orders o " +
                     "LEFT JOIN Users u ON o.UserID = u.UserID " +
                     "WHERE o.UserID = ? ORDER BY o.OrderDate DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp orderTs = rs.getTimestamp("OrderDate");
                    LocalDateTime orderDateTime = (orderTs != null) ? orderTs.toLocalDateTime() : null;
                    Order order = new Order(
                        rs.getString("OrderID"),
                        rs.getString("UserID"),
                        rs.getString("CustomerName"), 
                        orderDateTime,
                        rs.getDouble("TotalAmount")
                    );
                    order.setOrderDetails(getOrderDetailsByOrderId(order.getOrderId()));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    public List<Order> getAllOrders() throws SQLException { // For Admin
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.FullName AS CustomerName " +
                     "FROM Orders o " +
                     "LEFT JOIN Users u ON o.UserID = u.UserID " +
                     "ORDER BY o.OrderDate DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                 Timestamp orderTs = rs.getTimestamp("OrderDate");
                 LocalDateTime orderDateTime = (orderTs != null) ? orderTs.toLocalDateTime() : null;
                Order order = new Order(
                    rs.getString("OrderID"),
                    rs.getString("UserID"),
                    rs.getString("CustomerName"),
                    orderDateTime,
                    rs.getDouble("TotalAmount")
                );
                order.setOrderDetails(getOrderDetailsByOrderId(order.getOrderId()));
                orders.add(order);
            }
        }
        return orders;
    }


    public String createOrder(String userId, List<OrderDetail> items, double totalAmount, String shippingAddress) throws SQLException {
        String orderId = IdGenerator.generateOrderId();
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            String orderSql = "INSERT INTO Orders (OrderID, UserID, OrderDate, TotalAmount, ShippingAddress, OrderStatus) " +
                              "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement orderPstmt = conn.prepareStatement(orderSql)) {
                orderPstmt.setString(1, orderId);
                orderPstmt.setString(2, userId);
                orderPstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                orderPstmt.setDouble(4, totalAmount);
                orderPstmt.setString(5, shippingAddress);
                orderPstmt.setString(6, "PENDING");
                orderPstmt.executeUpdate();
            }

            String detailSql = "INSERT INTO OrderDetails (OrderDetailID, OrderID, ProductID, ProductName, Quantity, PriceAtOrder) " +
                               "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement detailPstmt = conn.prepareStatement(detailSql)) {
                for (OrderDetail item : items) {
                    String orderDetailId = IdGenerator.generateOrderDetailId(); // Generate ID here
                    item.setOrderDetailId(orderDetailId); // Set the generated ID on the item
                    item.setOrderId(orderId);             // Set the parent OrderID on the item

                    detailPstmt.setString(1, item.getOrderDetailId()); // Use the set ID
                    detailPstmt.setString(2, item.getOrderId());       // Use the set OrderID
                    detailPstmt.setString(3, item.getProductId());
                    detailPstmt.setString(4, item.getProductName());
                    detailPstmt.setInt(5, item.getQuantity());
                    detailPstmt.setDouble(6, item.getPrice());
                    detailPstmt.addBatch();

                    String updateStockSql = "UPDATE Products SET Stock = Stock - ? WHERE ProductID = ? AND Stock >= ?";
                    try (PreparedStatement stockPstmt = conn.prepareStatement(updateStockSql)) {
                        stockPstmt.setInt(1, item.getQuantity());
                        stockPstmt.setString(2, item.getProductId());
                        stockPstmt.setInt(3, item.getQuantity());
                        int rowsAffected = stockPstmt.executeUpdate();
                        if (rowsAffected == 0) {
                            conn.rollback();
                            throw new SQLException("Insufficient stock for Product: " + item.getProductName());
                        }
                    }
                }
                detailPstmt.executeBatch();
            }

            conn.commit();
            return orderId;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("Error rolling back order: " + ex.getMessage());}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { System.err.println("Error closing connection for order: " + e.getMessage());}
            }
        }
    }
    
    public List<Map<String, String>> getDistinctOrderedProductsByUserId(String userId) throws SQLException {
        List<Map<String, String>> orderedProducts = new ArrayList<>();
        String sql = "SELECT DISTINCT od.ProductID, od.ProductName " +
                     "FROM OrderDetails od " +
                     "JOIN Orders o ON od.OrderID = o.OrderID " +
                     "WHERE o.UserID = ? AND od.ProductID IS NOT NULL " +
                     "ORDER BY od.ProductName ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> productInfo = new HashMap<>();
                    productInfo.put("productId", rs.getString("ProductID"));
                    productInfo.put("productName", rs.getString("ProductName"));
                    orderedProducts.add(productInfo);
                }
            }
        }
        return orderedProducts;
    }
}
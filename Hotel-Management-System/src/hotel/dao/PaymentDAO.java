package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.models.Payment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {
    public static boolean addPayment(Payment payment) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "INSERT INTO payments (booking_id, amount, payment_type, " +
                    "payment_date, transaction_id, status, notes) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, payment.getBookingId());
            pstmt.setDouble(2, payment.getAmount());
            pstmt.setString(3, payment.getPaymentType());
            pstmt.setTimestamp(4, Timestamp.valueOf(payment.getPaymentDate()));
            pstmt.setString(5, payment.getTransactionId());
            pstmt.setString(6, payment.getStatus());
            pstmt.setString(7, payment.getNotes());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        payment.setPaymentId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;

        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("❌ [PaymentDAO] Cleanup error: " + e.getMessage());
            }
        }
    }
    public static List<Payment> getTodayPayments() throws SQLException {
        List<Payment> payments = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT * FROM payments WHERE DATE(payment_date) = CURDATE() ORDER BY payment_date DESC";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                payments.add(extractPaymentFromResultSet(rs));
            }
        }
        return payments;
    }

    // REMOVE THIS DUPLICATE METHOD - Keeping only one version
    /*
    public static double getTotalRevenueToday() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE DATE(payment_date) = CURDATE()";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }
    */

    public static List<Payment> getPaymentsByBookingId(int bookingId) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();
        String query = "SELECT * FROM payments WHERE booking_id = ? ORDER BY payment_date DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, bookingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(extractPaymentFromResultSet(rs));
                }
            }
        }
        return payments;
    }

    // Keep only this version of getTotalRevenueToday() - it's more readable with column name
    public static double getTotalRevenueToday() throws SQLException {
        String query = "SELECT COALESCE(SUM(amount), 0) as total_revenue " +
                "FROM payments WHERE DATE(payment_date) = CURDATE()";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getDouble("total_revenue");
            }
        }
        return 0.0;
    }

    private static Payment extractPaymentFromResultSet(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setBookingId(rs.getInt("booking_id"));
        payment.setAmount(rs.getDouble("amount"));
        payment.setPaymentType(rs.getString("payment_type"));

        Timestamp paymentTimestamp = rs.getTimestamp("payment_date");
        if (paymentTimestamp != null) {
            payment.setPaymentDate(paymentTimestamp.toLocalDateTime());
        }

        payment.setTransactionId(rs.getString("transaction_id"));
        payment.setStatus(rs.getString("status"));
        payment.setNotes(rs.getString("notes"));
        return payment;
    }
}
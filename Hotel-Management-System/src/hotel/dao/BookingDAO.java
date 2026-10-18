package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.models.Booking;
import java.sql.*;
import javax.swing.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public static boolean addBooking(Booking booking) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            if (!customerExists(conn, booking.getCustomerId())) {
                conn.rollback();
                return false;
            }

            boolean roomExists = roomExists(conn, booking.getRoomId());
            if (!roomExists) {
                conn.rollback();
                return false;
            }

            boolean isAvailable = isRoomAvailable(conn, booking.getRoomId(),
                    booking.getCheckInDate(), booking.getCheckOutDate());
            if (!isAvailable) {
                conn.rollback();
                return false;
            }

            String sql = "INSERT INTO bookings (customer_id, room_id, check_in_date, " +
                    "check_out_date, total_amount, payment_status, status, booking_date, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, booking.getCustomerId());
            pstmt.setInt(2, booking.getRoomId());
            pstmt.setDate(3, Date.valueOf(booking.getCheckInDate()));
            pstmt.setDate(4, Date.valueOf(booking.getCheckOutDate()));
            pstmt.setDouble(5, booking.getTotalAmount());
            pstmt.setString(6, booking.getPaymentStatus());
            pstmt.setString(7, booking.getBookingStatus());
            pstmt.setDate(8, Date.valueOf(booking.getBookingDate()));
            pstmt.setTimestamp(9, Timestamp.valueOf(booking.getCreatedAt()));

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        booking.setBookingId(generatedKeys.getInt(1));

                        if (!updateRoomStatus(conn, booking.getRoomId(), "OCCUPIED")) {
                            conn.rollback();
                            return false;
                        }

                        conn.commit();
                        return true;
                    }
                }
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean customerExists(Connection conn, int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers WHERE customer_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    public static boolean isCustomerCheckedIn(int customerId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM bookings " +
                "WHERE customer_id = ? " +
                "AND status = 'CHECKED_IN' " +
                "AND CURDATE() BETWEEN check_in_date AND check_out_date";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }

    private static boolean roomExists(Connection conn, int roomNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms WHERE room_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomNumber);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static boolean updateRoomStatus(Connection conn, int roomNumber, String status) throws SQLException {
        String sql = "UPDATE rooms SET status = ? WHERE room_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, roomNumber);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        }
    }

    private static boolean isRoomAvailable(Connection conn, int roomId, LocalDate checkIn, LocalDate checkOut) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM bookings " +
                "WHERE room_id = ? " +
                "AND status IN ('CONFIRMED', 'CHECKED_IN') " +
                "AND NOT (check_out_date <= ? OR check_in_date >= ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, roomId);
            pstmt.setDate(2, Date.valueOf(checkIn));
            pstmt.setDate(3, Date.valueOf(checkOut));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    return count == 0;
                }
            }
        }
        return true;
    }

    public static List<Booking> getTodayBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM bookings WHERE DATE(booking_date) = CURDATE() " +
                    "AND status = 'CONFIRMED' ORDER BY check_in_date";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        }
        return bookings;
    }

    public static List<Booking> getActiveBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM bookings WHERE status IN ('CONFIRMED', 'CHECKED_IN') " +
                    "AND CURDATE() BETWEEN check_in_date AND check_out_date " +
                    "ORDER BY check_in_date DESC";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    bookings.add(extractBookingFromResultSet(rs));
                }
            }
        }
        return bookings;
    }

    public static Booking getBookingById(int bookingId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM bookings WHERE booking_id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, bookingId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return extractBookingFromResultSet(rs);
                    }
                }
            }
        }
        return null;
    }

    public static boolean updateBookingStatus(int bookingId, String status) {
        String query = "UPDATE bookings SET status = ? WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, bookingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private static Booking extractBookingFromResultSet(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingId(rs.getInt("booking_id"));
        booking.setCustomerId(rs.getInt("customer_id"));
        booking.setRoomId(rs.getInt("room_id"));

        Date checkInDate = rs.getDate("check_in_date");
        if (checkInDate != null) {
            booking.setCheckInDate(checkInDate.toLocalDate());
        }

        Date checkOutDate = rs.getDate("check_out_date");
        if (checkOutDate != null) {
            booking.setCheckOutDate(checkOutDate.toLocalDate());
        }

        Date bookingDate = rs.getDate("booking_date");
        if (bookingDate != null) {
            booking.setBookingDate(bookingDate.toLocalDate());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            booking.setCreatedAt(createdAt.toLocalDateTime());
        }

        booking.setTotalAmount(rs.getDouble("total_amount"));
        booking.setPaymentStatus(rs.getString("payment_status"));

        try {
            booking.setBookingStatus(rs.getString("status"));
        } catch (SQLException e) {
            booking.setBookingStatus(rs.getString("booking_status"));
        }

        return booking;
    }

    public static List<Booking> getUnpaidBookings() {
        List<Booking> unpaidBookings = new ArrayList<>();
        String query = "SELECT * FROM bookings WHERE payment_status = 'PENDING' " +
                "AND status IN ('CHECKED_IN', 'CONFIRMED') " +
                "AND CURDATE() BETWEEN check_in_date AND check_out_date";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                unpaidBookings.add(extractBookingFromResultSet(rs));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return unpaidBookings;
    }

    public static List<Booking> getBookingsByCustomerId(int customerId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM bookings WHERE customer_id = ? ORDER BY check_in_date DESC";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, customerId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        bookings.add(extractBookingFromResultSet(rs));
                    }
                }
            }
        }
        return bookings;
    }

    public static List<Booking> getBookingsByRoomId(int roomId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM bookings WHERE room_id = ? ORDER BY check_in_date DESC";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, roomId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        bookings.add(extractBookingFromResultSet(rs));
                    }
                }
            }
        }
        return bookings;
    }

    public static boolean updatePaymentStatus(int bookingId, String paymentStatus) {
        String query = "UPDATE bookings SET payment_status = ? WHERE booking_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, paymentStatus);
            pstmt.setInt(2, bookingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean isRoomAvailable(int roomId, LocalDate checkIn, LocalDate checkOut) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return isRoomAvailable(conn, roomId, checkIn, checkOut);
        }
    }
}
package hotel.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
// Add at the top of your DAO files
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import hotel.database.DatabaseConnection;

public class Booking {
    private int bookingId;
    private int customerId;
    private int roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numberOfGuests;
    private String bookingStatus;
    private String paymentStatus;
    private double totalAmount;
    // private String specialRequests;
    private LocalDate bookingDate;
    private LocalDateTime createdAt;

    // Constructors
    public Booking() {
        this.bookingStatus = "CONFIRMED";
        this.paymentStatus = "PENDING";
        this.bookingDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
    }

    public Booking(int customerId, int roomId, LocalDate checkInDate,
                   LocalDate checkOutDate, int numberOfGuests) {
        this();
        this.customerId = customerId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfGuests = numberOfGuests;
    }

    // Getters and Setters
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

     public void setNumberOfGuests(int numberOfGuests) { this.numberOfGuests = numberOfGuests; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; } // Added

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }


    public LocalDate getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; } // Added
    public static boolean isCustomerCheckedIn(int customerId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM bookings " +
                "WHERE customer_id = ? " +
                "AND status IN ('CHECKED_IN') " +
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

    public int getNumberOfNights() {
        if (checkInDate != null && checkOutDate != null) {
            return checkInDate.until(checkOutDate).getDays();
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Booking #" + bookingId + " - Room " + roomId + " (" + bookingStatus + ")";
    }
}
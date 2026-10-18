package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.models.Staff;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    public static List<Staff> getAllStaff() throws SQLException {
        List<Staff> staffList = new ArrayList<>();
        String query = "SELECT * FROM staff ORDER BY first_name, last_name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                staffList.add(extractStaffFromResultSet(rs));
            }
        }
        return staffList;
    }

    public static Staff getStaffById(int staffId) throws SQLException {
        String query = "SELECT * FROM staff WHERE staff_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, staffId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractStaffFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public static boolean addStaff(Staff staff) throws SQLException {
        String query = "INSERT INTO staff (first_name, last_name, email, phone, position, " +
                "department, hire_date, salary, status, address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, staff.getFirstName());
            ps.setString(2, staff.getLastName());
            ps.setString(3, staff.getEmail());
            ps.setString(4, staff.getPhone());
            ps.setString(5, staff.getPosition());
            ps.setString(6, staff.getDepartment());
            ps.setDate(7, java.sql.Date.valueOf(staff.getHireDate()));
            ps.setDouble(8, staff.getSalary());
            ps.setString(9, staff.getStatus() != null ? staff.getStatus() : "ACTIVE");
            ps.setString(10, staff.getAddress());

            return ps.executeUpdate() > 0;
        }
    }

    public static boolean updateStaff(Staff staff) throws SQLException {
        String query = "UPDATE staff SET first_name = ?, last_name = ?, email = ?, phone = ?, " +
                "position = ?, department = ?, salary = ?, status = ?, address = ?, " +
                "emergency_contact = ? WHERE staff_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, staff.getFirstName());
            pstmt.setString(2, staff.getLastName());
            pstmt.setString(3, staff.getEmail());
            pstmt.setString(4, staff.getPhone());
            pstmt.setString(5, staff.getPosition());
            pstmt.setString(6, staff.getDepartment());
            pstmt.setDouble(7, staff.getSalary());
            pstmt.setString(8, staff.getStatus());
            pstmt.setString(9, staff.getAddress());
            pstmt.setString(10, staff.getEmergencyContact());
            pstmt.setInt(11, staff.getStaffId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public static boolean deleteStaff(int staffId) throws SQLException {
        String query = "DELETE FROM staff WHERE staff_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, staffId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public static List<Staff> getActiveStaff() throws SQLException {
        List<Staff> staffList = new ArrayList<>();
        String query = "SELECT * FROM staff WHERE status = 'ACTIVE' ORDER BY first_name, last_name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                staffList.add(extractStaffFromResultSet(rs));
            }
        }
        return staffList;
    }

    private static Staff extractStaffFromResultSet(ResultSet rs) throws SQLException {
        Staff staff = new Staff(
                rs.getInt("staff_id"),
                rs.getString("first_name") + " " + rs.getString("last_name"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("position"),
                rs.getString("department"),
                rs.getDouble("salary"),
                rs.getString("hire_date")
        );
        staff.setStatus(rs.getString("status"));
        staff.setAddress(rs.getString("address"));
        staff.setEmergencyContact(rs.getString("emergency_contact"));
        return staff;
    }
}
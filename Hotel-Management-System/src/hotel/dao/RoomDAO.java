package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.models.Room;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public static List<Room> getAvailableRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT r.* FROM rooms r " +
                "WHERE r.status = 'AVAILABLE' " +
                "ORDER BY r.room_number";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }
        }
        return rooms;
    }

    public static int countRoomsByStatus(String status) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM rooms WHERE status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }
        return 0;
    }

    public static int countAllRooms() throws SQLException {
        String query = "SELECT COUNT(*) as count FROM rooms";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }

    public static boolean addRoom(Room room) throws SQLException {
        String query = "INSERT INTO rooms (room_number, room_type, bed_type, floor, " +
                "price_per_night, status, max_occupancy, view_type, amenities) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, room.getRoomNumber());
            pstmt.setString(2, room.getRoomType());
            pstmt.setString(3, room.getBedType());
            pstmt.setInt(4, room.getFloor());
            pstmt.setDouble(5, room.getPricePerNight());
            pstmt.setString(6, room.getStatus());
            pstmt.setInt(7, room.getMaxOccupancy());
            pstmt.setString(8, room.getViewType());
            pstmt.setString(9, room.getAmenities());

            return pstmt.executeUpdate() > 0;
        }
    }

    public static Room getRoomByNumber(int roomNumber) throws SQLException {
        String query = "SELECT * FROM rooms WHERE room_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, roomNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractRoomFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public static boolean updateRoom(Room room) throws SQLException {
        String query = "UPDATE rooms SET room_type = ?, bed_type = ?, floor = ?, " +
                "price_per_night = ?, status = ?, max_occupancy = ?, " +
                "view_type = ?, amenities = ? WHERE room_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, room.getRoomType());
            pstmt.setString(2, room.getBedType());
            pstmt.setInt(3, room.getFloor());
            pstmt.setDouble(4, room.getPricePerNight());
            pstmt.setString(5, room.getStatus());
            pstmt.setInt(6, room.getMaxOccupancy());
            pstmt.setString(7, room.getViewType());
            pstmt.setString(8, room.getAmenities());
            pstmt.setInt(9, room.getRoomNumber());

            return pstmt.executeUpdate() > 0;
        }
    }

    public static boolean deleteRoom(int roomNumber) throws SQLException {
        String query = "DELETE FROM rooms WHERE room_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, roomNumber);
            return pstmt.executeUpdate() > 0;
        }
    }

    public static boolean updateRoomStatus(int roomNumber, String status) throws SQLException {
        String query = "UPDATE rooms SET status = ? WHERE room_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, roomNumber);
            return pstmt.executeUpdate() > 0;
        }
    }

    public static int countAvailableRooms() throws SQLException {
        String query = "SELECT COUNT(*) as count FROM rooms WHERE status = 'AVAILABLE'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }

    public static List<Room> getAllRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT * FROM rooms ORDER BY room_number";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                rooms.add(extractRoomFromResultSet(rs));
            }
        }
        return rooms;
    }

    private static Room extractRoomFromResultSet(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setRoomNumber(rs.getInt("room_number"));
        room.setRoomType(rs.getString("room_type"));
        room.setPricePerNight(rs.getDouble("price_per_night"));
        room.setStatus(rs.getString("status"));

        try {
            room.setBedType(rs.getString("bed_type"));
        } catch (SQLException e) {}

        try {
            room.setFloor(rs.getInt("floor"));
        } catch (SQLException e) {}

        try {
            room.setMaxOccupancy(rs.getInt("max_occupancy"));
        } catch (SQLException e) {}

        try {
            room.setViewType(rs.getString("view_type"));
        } catch (SQLException e) {}

        try {
            room.setAmenities(rs.getString("amenities"));
        } catch (SQLException e) {}

        return room;
    }
}
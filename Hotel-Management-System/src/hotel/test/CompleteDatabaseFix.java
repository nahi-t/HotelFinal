package hotel.test;

import hotel.database.DatabaseConnection;
import java.sql.*;

public class CompleteDatabaseFix {
    public static void main(String[] args) {
        System.out.println("🚀 STARTING COMPLETE DATABASE FIX");
        System.out.println("==================================");


        System.out.println("📋 Step 1: Resetting database...");
         try { Thread.sleep(2000); } catch (InterruptedException e) {}

        System.out.println("🔍 Step 2: Verifying database structure...");
        verifyAndFixStructure();

        System.out.println("🧪 Step 3: Testing booking system...");
        testBookingSystem();

        System.out.println("✅ COMPLETE DATABASE FIX FINISHED");
        System.out.println("==================================");
    }

    private static void verifyAndFixStructure() {
        try (Connection conn = DatabaseConnection.getConnection()) {
             String checkBookingSQL =
                    "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_KEY " +
                            "FROM INFORMATION_SCHEMA.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bookings' " +
                            "ORDER BY ORDINAL_POSITION";

            System.out.println("\n📊 Bookings Table Structure:");
            System.out.println("----------------------------------");

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkBookingSQL)) {

                while (rs.next()) {
                    String column = rs.getString("COLUMN_NAME");
                    String type = rs.getString("DATA_TYPE");
                    String nullable = rs.getString("IS_NULLABLE");
                    String key = rs.getString("COLUMN_KEY");

                    System.out.printf("%-20s %-15s %-5s %s%n",
                            column, type, nullable, key);
                }
            }


            System.out.println("\n📈 Data Counts:");
            System.out.println("----------------------------------");
            String[] tables = {"customers", "rooms", "bookings"};
            for (String table : tables) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM " + table)) {
                    if (rs.next()) {
                        System.out.println(table + ": " + rs.getInt("count") + " records");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error checking structure: " + e.getMessage());
        }
    }

    private static void testBookingSystem() {
        try (Connection conn = DatabaseConnection.getConnection()) {
             System.out.println("\n🧪 Test 1: Creating test booking...");

             int customerId = getFirstCustomerId(conn);
            int roomId = getFirstAvailableRoom(conn);

            if (customerId > 0 && roomId > 0) {
                String insertSQL = "INSERT INTO bookings (customer_id, room_id, " +
                        "check_in_date, check_out_date, total_amount, " +
                        "payment_status, status) " +
                        "VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), " +
                        "200.00, 'PAID', 'CONFIRMED')";

                try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                    pstmt.setInt(1, customerId);
                    pstmt.setInt(2, roomId);
                    int rows = pstmt.executeUpdate();

                    if (rows > 0) {
                        System.out.println("✅ Test booking created successfully!");
                        System.out.println("   Customer ID: " + customerId);
                        System.out.println("   Room ID: " + roomId);

                         try (Statement stmt = conn.createStatement();
                             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM bookings")) {
                            if (rs.next()) {
                                System.out.println("   Total bookings: " + rs.getInt(1));
                            }
                        }
                    } else {
                        System.out.println("❌ Failed to create test booking");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Test error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int getFirstCustomerId(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT customer_id FROM customers ORDER BY customer_id LIMIT 1")) {
            return rs.next() ? rs.getInt("customer_id") : 0;
        }
    }

    private static int getFirstAvailableRoom(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT room_number FROM rooms WHERE status = 'AVAILABLE' LIMIT 1")) {
            return rs.next() ? rs.getInt("room_number") : 0;
        }
    }
}
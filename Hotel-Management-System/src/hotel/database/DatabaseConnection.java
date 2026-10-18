package hotel.database;

import java.sql.*;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class DatabaseConnection {
    private static Connection connection = null;
    private static boolean useInMemory = false;
    private static String url;
    private static String user;
    private static String password;
    private static final String CONFIG_FILE = "config.properties";
    private static boolean initializationDone = false;

    static {
        loadConfiguration();
    }

    private DatabaseConnection() {}

    private static void loadConfiguration() {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(CONFIG_FILE));

            url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/hotel_db");
            user = props.getProperty("db.user", "root");
            password = props.getProperty("db.password", "Y2721#Tbet");

            url += "?autoReconnect=true" +
                    "&useSSL=false" +
                    "&allowPublicKeyRetrieval=true" +
                    "&serverTimezone=UTC" +
                    "&characterEncoding=UTF-8";

        } catch (IOException e) {
            url = "jdbc:mysql://localhost:3306/hotel_db" +
                    "?autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            user = "root";
            password = "Y2721#Tbet";
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            createConnection();
        }
        return connection;
    }

    private static synchronized void createConnection() throws SQLException {
        if (useInMemory) {
            connection = createInMemoryDatabase();
            return;
        }

        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, password);
            connection.setAutoCommit(true);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            if (!initializationDone) {
                initializeDatabase();
                initializationDone = true;
            }

        } catch (ClassNotFoundException e) {
            fallbackToInMemory();
        }
    }

    private static void fallbackToInMemory() {
        useInMemory = true;
        connection = createInMemoryDatabase();
    }

    private static Connection createInMemoryDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection sqliteConn = DriverManager.getConnection("jdbc:sqlite::memory:");
            initializeInMemoryDatabase(sqliteConn);
            return sqliteConn;
        } catch (Exception e) {
            return null;
        }
    }

    private static void initializeInMemoryDatabase(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();

        String[] createTables = {
                "CREATE TABLE users (" +
                        "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username VARCHAR(50) UNIQUE NOT NULL," +
                        "password VARCHAR(255) NOT NULL," +
                        "name VARCHAR(100) NOT NULL," +
                        "email VARCHAR(100) UNIQUE NOT NULL," +
                        "phone VARCHAR(20)," +
                        "address TEXT," +
                        "role VARCHAR(20) NOT NULL," +
                        "is_active BOOLEAN DEFAULT true," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")",

                "CREATE TABLE rooms (" +
                        "room_number INTEGER PRIMARY KEY," +
                        "room_type VARCHAR(20) NOT NULL," +
                        "bed_type VARCHAR(20) NOT NULL," +
                        "floor INTEGER NOT NULL," +
                        "price_per_night DECIMAL(10,2) NOT NULL," +
                        "status VARCHAR(20) DEFAULT 'AVAILABLE'," +
                        "needs_cleaning BOOLEAN DEFAULT false," +
                        "needs_maintenance BOOLEAN DEFAULT false," +
                        "amenities TEXT," +
                        "max_occupancy INTEGER NOT NULL," +
                        "view_type VARCHAR(20)," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")",

                "CREATE TABLE staff (" +
                        "staff_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "first_name VARCHAR(50) NOT NULL," +
                        "last_name VARCHAR(50) NOT NULL," +
                        "email VARCHAR(100) UNIQUE NOT NULL," +
                        "phone VARCHAR(20)," +
                        "position VARCHAR(50) NOT NULL," +
                        "department VARCHAR(50)," +
                        "hire_date DATE," +
                        "salary DECIMAL(10,2)," +
                        "status VARCHAR(20) DEFAULT 'ACTIVE'," +
                        "address TEXT," +
                        "emergency_contact VARCHAR(100)," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")",

                "CREATE TABLE bookings (" +
                        "booking_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "customer_id INTEGER NOT NULL," +
                        "room_id INTEGER NOT NULL," +
                        "check_in_date DATE NOT NULL," +
                        "check_out_date DATE NOT NULL," +
                        "total_amount DECIMAL(10,2) NOT NULL," +
                        "payment_status VARCHAR(20) DEFAULT 'PENDING'," +
                        "status VARCHAR(20) DEFAULT 'CONFIRMED'," +
                        "booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")",

                "CREATE TABLE payments (" +
                        "payment_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "booking_id INTEGER NOT NULL," +
                        "amount DECIMAL(10,2) NOT NULL," +
                        "payment_type VARCHAR(30) NOT NULL," +
                        "payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "transaction_id VARCHAR(100)," +
                        "status VARCHAR(20) DEFAULT 'COMPLETED'," +
                        "notes TEXT," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")",



                "CREATE TABLE settings (" +
                        "setting_key VARCHAR(100) PRIMARY KEY," +
                        "setting_value TEXT NOT NULL," +
                        "description TEXT," +
                        "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")",

                "CREATE TABLE daily_reports (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "report_date DATE NOT NULL," +
                        "generated_by VARCHAR(100) NOT NULL," +
                        "occupancy_rate DECIMAL(5,2)," +
                        "daily_revenue DECIMAL(10,2)," +
                        "total_bookings INTEGER," +
                        "report_content TEXT," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")"
        };

        for (String sql : createTables) {
            stmt.execute(sql);
        }

        insertDefaultData(stmt);
        stmt.close();
    }

    private static void insertDefaultData(Statement stmt) throws SQLException {
        stmt.execute("INSERT OR IGNORE INTO users (username, password, name, email, role) " +
                "VALUES ('admin', 'admin123', 'Administrator', 'admin@hotel.com', 'ADMIN')");

        stmt.execute("INSERT OR IGNORE INTO users (username, password, name, email, role) " +
                "VALUES ('reception', 'reception123', 'Receptionist', 'reception@hotel.com', 'RECEPTION')");

        String[] insertRooms = {
                "INSERT OR IGNORE INTO rooms (room_number, room_type, bed_type, floor, price_per_night, max_occupancy, view_type, amenities) " +
                        "VALUES (101, 'STANDARD', 'DOUBLE', 1, 100.00, 2, 'CITY', 'TV, WiFi, AC, Mini-bar')",
                "INSERT OR IGNORE INTO rooms (room_number, room_type, bed_type, floor, price_per_night, max_occupancy, view_type, amenities) " +
                        "VALUES (102, 'STANDARD', 'SINGLE', 1, 80.00, 1, 'GARDEN', 'TV, WiFi, AC')",
                "INSERT OR IGNORE INTO rooms (room_number, room_type, bed_type, floor, price_per_night, max_occupancy, view_type, amenities) " +
                        "VALUES (103, 'STANDARD', 'DOUBLE', 1, 100.00, 2, 'CITY', 'TV, WiFi, AC, Mini-bar')",
                "INSERT OR IGNORE INTO rooms (room_number, room_type, bed_type, floor, price_per_night, max_occupancy, view_type, amenities) " +
                        "VALUES (201, 'DELUXE', 'KING', 2, 150.00, 2, 'SEA', 'TV, WiFi, AC, Mini-bar, Safe')",
                "INSERT OR IGNORE INTO rooms (room_number, room_type, bed_type, floor, price_per_night, max_occupancy, view_type, amenities) " +
                        "VALUES (202, 'DELUXE', 'TWIN', 2, 140.00, 2, 'CITY', 'TV, WiFi, AC, Mini-bar, Safe')",
                "INSERT OR IGNORE INTO rooms (room_number, room_type, bed_type, floor, price_per_night, max_occupancy, view_type, amenities) " +
                        "VALUES (301, 'SUITE', 'KING', 3, 250.00, 4, 'SEA', 'TV, WiFi, AC, Mini-bar, Safe, Jacuzzi')",
                "INSERT OR IGNORE INTO rooms (room_number, room_type, bed_type, floor, price_per_night, max_occupancy, view_type, amenities) " +
                        "VALUES (302, 'EXECUTIVE', 'KING', 3, 350.00, 3, 'PANORAMIC', 'TV, WiFi, AC, Mini-bar, Safe, Jacuzzi, Butler Service')"
        };

        for (String sql : insertRooms) {
            try {
                stmt.execute(sql);
            } catch (SQLException e) {}
        }

        String[] insertCustomers = {
                "INSERT OR IGNORE INTO customers (name, email, phone, address, age, country) VALUES " +
                        "('Yabsra Temtim', 'yab.tm@email.com', '+251-955365402', 'Addis Ababa', 28, 'Ethiopia')",
                "INSERT OR IGNORE INTO customers (name, email, phone, address, age, country) VALUES " +
                        "('Mhret Alemneh', 'mhri.al@email.com', '+251-977123422', 'Welkite', 32, 'Ethiopia')",
                "INSERT OR IGNORE INTO customers (name, email, phone, address, age, country) VALUES " +
                        "('Rediet Banteyirga', 'red.bante@email.com', '+251-777324534', 'Addis Ababa', 25, 'Ethiopia')",
                "INSERT OR IGNORE INTO customers (name, email, phone, address, age, country) VALUES " +
                        "('Kalkidan Tsehaye', 'kal.t@email.com', '+251-932212467', 'Bahir Dar', 35, 'Ethiopia')",
                "INSERT OR IGNORE INTO customers (name, email, phone, address, age, country) VALUES " +
                        "('Eskedar Nigussie', 'esku.ng@email.com', '+251-55982311', 'Hawassa', 40, 'Ethiopia')",
                "INSERT OR IGNORE INTO customers (name, email, phone, address, age, country) VALUES " +
                        "('Blen Teshome', 'blu.teshe@email.com', '+251-733467291', 'Addis Ababa', 29, 'Ethiopia')"
        };

        for (String sql : insertCustomers) {
            try {
                stmt.execute(sql);
            } catch (SQLException e) {}
        }

        String[] insertStaff = {
                "INSERT OR IGNORE INTO staff (first_name, last_name, email, phone, position, department, hire_date, salary, status, address) VALUES " +
                        "('Alemneh', 'Kebede', 'alem@hotel.com', '+251-988776345', 'Manager', 'Administration', '2023-01-15', 5000.00, 'ACTIVE', 'Addis Ababa')",
                "INSERT OR IGNORE INTO staff (first_name, last_name, email, phone, position, department, hire_date, salary, status, address) VALUES " +
                        "('Almaz', 'Tesfaye', 'almaz@hotel.com', '+251-723221214', 'Receptionist', 'Front Desk', '2023-03-20', 3000.00, 'ACTIVE', 'Addis Ababa')",
                "INSERT OR IGNORE INTO staff (first_name, last_name, email, phone, position, department, hire_date, salary, status, address) VALUES " +
                        "('Mohammad', 'Ahmed', 'mohammad@hotel.com', '+251-934339022', 'Housekeeper', 'Housekeeping', '2023-02-10', 2500.00, 'ACTIVE', 'Addis Ababa')",
                "INSERT OR IGNORE INTO staff (first_name, last_name, email, phone, position, department, hire_date, salary, status, address) VALUES " +
                        "('Minilik', 'Haile', 'minilik@hotel.com', '+251-788964322', 'Maintenance', 'Technical', '2023-04-05', 3200.00, 'ACTIVE', 'Addis Ababa')",
                "INSERT OR IGNORE INTO staff (first_name, last_name, email, phone, position, department, hire_date, salary, status, address) VALUES " +
                        "('Aschalew', 'Girma', 'aschalew@hotel.com', '+251-23177718', 'Head Chef', 'Kitchen', '2023-05-10', 4000.00, 'ACTIVE', 'Addis Ababa')"
        };

        for (String sql : insertStaff) {
            try {
                stmt.execute(sql);
            } catch (SQLException e) {}
        }



        String[] insertSettings = {
                "INSERT OR IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                        "('hotel_name', 'Ethiopian Luxury Hotel', 'Hotel Name')",
                "INSERT OR IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                        "('contact_email', 'info@ethiopianhotel.com', 'Contact Email Address')",
                "INSERT OR IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                        "('phone_number', '+251-11-123-4567', 'Hotel Phone Number')",
                "INSERT OR IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                        "('address', 'Addis Ababa, Ethiopia', 'Hotel Physical Address')",
                "INSERT OR IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                        "('check_in_time', '14:00', 'Standard Check-in Time')",
                "INSERT OR IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                        "('check_out_time', '12:00', 'Standard Check-out Time')",
                "INSERT OR IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                        "('tax_rate', '15', 'Tax Rate Percentage')",
                "INSERT OR IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                        "('currency', 'USD', 'Default Currency')",
                "INSERT OR IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                        "('booking_timeout', '30', 'Booking timeout in minutes')",
                "INSERT OR IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                        "('min_booking_days', '1', 'Minimum booking days')",
                "INSERT OR IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                        "('max_booking_days', '30', 'Maximum booking days')",
                "INSERT OR IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                        "('cancellation_hours', '24', 'Cancellation notice in hours')",
                "INSERT OR IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                        "('late_checkout_fee', '50', 'Late checkout fee percentage')",
                "INSERT OR IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                        "('breakfast_included', 'true', 'Breakfast included in room rate')",
                "INSERT OR IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                        "('wifi_included', 'true', 'WiFi included in room rate')",
                "INSERT OR IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                        "('parking_fee', '10', 'Daily parking fee')"
        };

        for (String sql : insertSettings) {
            try {
                stmt.execute(sql);
            } catch (SQLException e) {}
        }
    }

    private static void initializeDatabase() {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = getConnection();
            if (conn == null || conn.isClosed()) {
                return;
            }

            stmt = conn.createStatement();

            if (!useInMemory) {
                String[] createTables = {
                        "CREATE TABLE IF NOT EXISTS users (" +
                                "user_id INT AUTO_INCREMENT PRIMARY KEY," +
                                "username VARCHAR(50) UNIQUE NOT NULL," +
                                "password VARCHAR(255) NOT NULL," +
                                "name VARCHAR(100) NOT NULL," +
                                "email VARCHAR(100) UNIQUE NOT NULL," +
                                "phone VARCHAR(20)," +
                                "address TEXT," +
                                "role VARCHAR(20) NOT NULL," +
                                "is_active BOOLEAN DEFAULT true," +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                ")",

                        "CREATE TABLE IF NOT EXISTS rooms (" +
                                "room_number INT PRIMARY KEY," +
                                "room_type VARCHAR(20) NOT NULL," +
                                "bed_type VARCHAR(20) NOT NULL," +
                                "floor INT NOT NULL," +
                                "price_per_night DECIMAL(10,2) NOT NULL," +
                                "status VARCHAR(20) DEFAULT 'AVAILABLE'," +
                                "needs_cleaning BOOLEAN DEFAULT false," +
                                "needs_maintenance BOOLEAN DEFAULT false," +
                                "amenities TEXT," +
                                "max_occupancy INT NOT NULL," +
                                "view_type VARCHAR(20)," +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                ")",

                        "CREATE TABLE IF NOT EXISTS staff (" +
                                "staff_id INT AUTO_INCREMENT PRIMARY KEY," +
                                "first_name VARCHAR(50) NOT NULL," +
                                "last_name VARCHAR(50) NOT NULL," +
                                "email VARCHAR(100) UNIQUE NOT NULL," +
                                "phone VARCHAR(20)," +
                                "position VARCHAR(50) NOT NULL," +
                                "department VARCHAR(50)," +
                                "hire_date DATE," +
                                "salary DECIMAL(10,2)," +
                                "status VARCHAR(20) DEFAULT 'ACTIVE'," +
                                "address TEXT," +
                                "emergency_contact VARCHAR(100)," +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                ")",

                        "CREATE TABLE IF NOT EXISTS bookings (" +
                                "booking_id INT AUTO_INCREMENT PRIMARY KEY," +
                                "customer_id INT NOT NULL," +
                                "room_id INT NOT NULL," +
                                "check_in_date DATE NOT NULL," +
                                "check_out_date DATE NOT NULL," +
                                "total_amount DECIMAL(10,2) NOT NULL," +
                                "payment_status VARCHAR(20) DEFAULT 'PENDING'," +
                                "status VARCHAR(20) DEFAULT 'CONFIRMED'," +
                                "booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                ")",

                        "CREATE TABLE IF NOT EXISTS payments (" +
                                "payment_id INT AUTO_INCREMENT PRIMARY KEY," +
                                "booking_id INT NOT NULL," +
                                "amount DECIMAL(10,2) NOT NULL," +
                                "payment_type VARCHAR(30) NOT NULL," +
                                "payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                                "transaction_id VARCHAR(100)," +
                                "status VARCHAR(20) DEFAULT 'COMPLETED'," +
                                "notes TEXT," +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                ")",

                        "CREATE TABLE IF NOT EXISTS settings (" +
                                "setting_key VARCHAR(100) PRIMARY KEY," +
                                "setting_value TEXT NOT NULL," +
                                "description TEXT," +
                                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                                ")",

                        "CREATE TABLE IF NOT EXISTS daily_reports (" +
                                "id INT AUTO_INCREMENT PRIMARY KEY," +
                                "report_date DATE NOT NULL," +
                                "generated_by VARCHAR(100) NOT NULL," +
                                "occupancy_rate DECIMAL(5,2)," +
                                "daily_revenue DECIMAL(10,2)," +
                                "total_bookings INT," +
                                "report_content TEXT," +
                                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                                ")"
                };

                for (String sql : createTables) {
                    try {
                        stmt.execute(sql);
                    } catch (SQLException e) {}
                }
            }

            insertDefaultDataMySQL(stmt);

        } catch (SQLException e) {
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        }
    }

    private static void insertDefaultDataMySQL(Statement stmt) throws SQLException {
        stmt.execute("INSERT IGNORE INTO users (username, password, name, email, role) " +
                "VALUES ('admin', 'admin123', 'Administrator', 'admin@hotel.com', 'ADMIN')");

        stmt.execute("INSERT IGNORE INTO users (username, password, name, email, role, is_active) " +
                "VALUES ('reception', 'reception123', 'Receptionist', 'reception@hotel.com', 'RECEPTION', true)");

        stmt.execute("INSERT IGNORE INTO users (username, password, name, email, role) " +
                "VALUES ('manager', 'manager123', 'Manager', 'manager@hotel.com', 'MANAGER')");

        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM rooms");
        if (rs.next() && rs.getInt(1) == 0) {
            String[] insertRooms = {
                    "INSERT IGNORE INTO rooms (room_number, room_type, bed_type, floor, price_per_night, max_occupancy, view_type, amenities) " +
                            "VALUES (101, 'STANDARD', 'DOUBLE', 1, 100.00, 2, 'CITY', 'TV, WiFi, AC, Mini-bar')",
                    "INSERT IGNORE INTO rooms (room_number, room_type, bed_type, floor, price_per_night, max_occupancy, view_type, amenities) " +
                            "VALUES (102, 'STANDARD', 'SINGLE', 1, 80.00, 1, 'GARDEN', 'TV, WiFi, AC')",
                    "INSERT IGNORE INTO rooms (room_number, room_type, bed_type, floor, price_per_night, max_occupancy, view_type, amenities) " +
                            "VALUES (103, 'STANDARD', 'DOUBLE', 1, 100.00, 2, 'CITY', 'TV, WiFi, AC, Mini-bar')",
                    "INSERT IGNORE INTO rooms (room_number, room_type, bed_type, floor, price_per_night, max_occupancy, view_type, amenities) " +
                            "VALUES (201, 'DELUXE', 'KING', 2, 150.00, 2, 'SEA', 'TV, WiFi, AC, Mini-bar, Safe')",
                    "INSERT IGNORE INTO rooms (room_number, room_type, bed_type, floor, price_per_night, max_occupancy, view_type, amenities) " +
                            "VALUES (202, 'DELUXE', 'TWIN', 2, 140.00, 2, 'CITY', 'TV, WiFi, AC, Mini-bar, Safe')",
                    "INSERT IGNORE INTO rooms (room_number, room_type, bed_type, floor, price_per_night, max_occupancy, view_type, amenities) " +
                            "VALUES (301, 'SUITE', 'KING', 3, 250.00, 4, 'SEA', 'TV, WiFi, AC, Mini-bar, Safe, Jacuzzi')",
                    "INSERT IGNORE INTO rooms (room_number, room_type, bed_type, floor, price_per_night, max_occupancy, view_type, amenities) " +
                            "VALUES (302, 'EXECUTIVE', 'KING', 3, 350.00, 3, 'PANORAMIC', 'TV, WiFi, AC, Mini-bar, Safe, Jacuzzi, Butler Service')"
            };

            for (String sql : insertRooms) {
                try {
                    stmt.executeUpdate(sql);
                } catch (SQLException e) {}
            }
        }
        rs.close();

        rs = stmt.executeQuery("SELECT COUNT(*) FROM customers");
        if (rs.next() && rs.getInt(1) == 0) {
            String[] insertCustomers = {
                    "INSERT IGNORE INTO customers (name, email, phone, address, age, country, registration_date) VALUES " +
                            "('Yabsra Temtim', 'yab.tm@email.com', '+251-955365402', 'Addis Ababa', 28, 'Ethiopia', CURRENT_DATE)",
                    "INSERT IGNORE INTO customers (name, email, phone, address, age, country, registration_date) VALUES " +
                            "('Mhret Alemneh', 'mhri.al@email.com', '+251-977123422', 'Welkite', 32, 'Ethiopia', CURRENT_DATE)",
                    "INSERT IGNORE INTO customers (name, email, phone, address, age, country, registration_date) VALUES " +
                            "('Rediet Banteyirga', 'red.bante@email.com', '+251-777324534', 'Addis Ababa', 25, 'Ethiopia', CURRENT_DATE)",
                    "INSERT IGNORE INTO customers (name, email, phone, address, age, country, registration_date) VALUES " +
                            "('Kalkidan Tsehaye', 'kal.t@email.com', '+251-932212467', 'Bahir Dar', 35, 'Ethiopia', CURRENT_DATE)",
                    "INSERT IGNORE INTO customers (name, email, phone, address, age, country, registration_date) VALUES " +
                            "('Eskedar Nigussie', 'esku.ng@email.com', '+251-55982311', 'Hawassa', 40, 'Ethiopia', CURRENT_DATE)",
                    "INSERT IGNORE INTO customers (name, email, phone, address, age, country, registration_date) VALUES " +
                            "('Blen Teshome', 'blu.teshe@email.com', '+251-733467291', 'Addis Ababa', 29, 'Ethiopia', CURRENT_DATE)"
            };

            for (String sql : insertCustomers) {
                try {
                    stmt.executeUpdate(sql);
                } catch (SQLException e) {}
            }
        }
        rs.close();

        rs = stmt.executeQuery("SELECT COUNT(*) FROM staff");
        if (rs.next() && rs.getInt(1) == 0) {
            String[] insertStaff = {
                    "INSERT IGNORE INTO staff (first_name, last_name, email, phone, position, department, hire_date, salary) VALUES " +
                            "('Alemneh', 'melese', 'alem@hotel.com', '+251-988776345', 'Manager', 'Administration', '2023-01-15', 5000.00)",
                    "INSERT IGNORE INTO staff (first_name, last_name, email, phone, position, department, hire_date, salary) VALUES " +
                            "('Almaz', 'taye', 'almi@hotel.com', '+251-723221214', 'Receptionist', 'Front Desk', '2023-03-20', 3000.00)",
                    "INSERT IGNORE INTO staff (first_name, last_name, email, phone, position, department, hire_date, salary) VALUES " +
                            "('Mohammad', 'alema', 'moh@hotel.com', '+251-934339022', 'Housekeeper', 'Housekeeping', '2023-02-10', 2500.00)",
                    "INSERT IGNORE INTO staff (first_name, last_name, email, phone, position, department, hire_date, salary) VALUES " +
                            "('Minilik', 'mengistu', 'mila@hotel.com', '+251-788964322', 'Maintenance', 'Technical', '2023-04-05', 3200.00)",
                    "INSERT IGNORE INTO staff (first_name, last_name, email, phone, position, department, hire_date, salary) VALUES " +
                            "('Aschalew', 'tefera', 'asche@hotel.com', '+251-23177718', 'Head Chef', 'Kitchen', '2023-05-10', 4000.00)"
            };

            for (String sql : insertStaff) {
                try {
                    stmt.executeUpdate(sql);
                } catch (SQLException e) {}
            }
        }
        rs.close();


        rs = stmt.executeQuery("SELECT COUNT(*) FROM settings");
        if (rs.next() && rs.getInt(1) == 0) {
            String[] insertSettings = {
                    "INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                            "('hotel_name', 'Ethiopian Luxury Hotel', 'Hotel Name')",
                    "INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                            "('contact_email', 'info@ethiopianhotel.com', 'Contact Email Address')",
                    "INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                            "('phone_number', '+251-11-123-4567', 'Hotel Phone Number')",
                    "INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                            "('address', 'Addis Ababa, Ethiopia', 'Hotel Physical Address')",
                    "INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                            "('check_in_time', '14:00', 'Standard Check-in Time')",
                    "INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                            "('check_out_time', '12:00', 'Standard Check-out Time')",
                    "INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                            "('tax_rate', '15', 'Tax Rate Percentage')",
                    "INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                            "('currency', 'USD', 'Default Currency')",
                    "INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                            "('booking_timeout', '30', 'Booking timeout in minutes')",
                    "INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                            "('min_booking_days', '1', 'Minimum booking days')",
                    "INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                            "('max_booking_days', '30', 'Maximum booking days')",
                    "INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                            "('cancellation_hours', '24', 'Cancellation notice in hours')",
                    "INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                            "('late_checkout_fee', '50', 'Late checkout fee percentage')",
                    "INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                            "('breakfast_included', 'true', 'Breakfast included in room rate')",
                    "INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                            "('wifi_included', 'true', 'WiFi included in room rate')",
                    "INSERT IGNORE INTO settings (setting_key, setting_value, description) VALUES " +
                            "('parking_fee', '10', 'Daily parking fee')"
            };

            for (String sql : insertSettings) {
                try {
                    stmt.executeUpdate(sql);
                } catch (SQLException e) {}
            }
        }
        rs.close();
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
            } finally {
                connection = null;
            }
        }
    }
}
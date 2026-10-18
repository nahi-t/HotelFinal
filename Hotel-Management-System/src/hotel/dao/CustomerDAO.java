package hotel.dao;

import hotel.database.DatabaseConnection;
import hotel.models.Customer;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

     public static Customer extractCustomerFromResultSet(ResultSet rs) throws SQLException {
        Customer customer = new Customer();

         customer.setCustomerId(rs.getInt("customer_id"));
        customer.setName(rs.getString("name"));
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("phone"));
        customer.setAddress(rs.getString("address"));
        customer.setCountry(rs.getString("country"));

         String idProof = rs.getString("id_proof");
        if (idProof != null) customer.setIdProof(idProof);

        String idNumber = rs.getString("id_number");
        if (idNumber != null) customer.setIdNumber(idNumber);

         Date regDate = rs.getDate("registration_date");
        if (regDate != null) {
            customer.setRegistrationDate(regDate.toLocalDate());
        } else {
            customer.setRegistrationDate(LocalDate.now());
        }

         int age = rs.getInt("age");
        if (!rs.wasNull()) {
            customer.setAge(age);
        }

         int loyaltyPoints = rs.getInt("loyalty_points");
        if (!rs.wasNull()) {
            customer.setLoyaltyPoints(loyaltyPoints);
        }

         String preferences = rs.getString("preferences");
        if (preferences != null) {
            customer.setPreferences(preferences);
        }

        return customer;
    }


    public static Customer getCustomerById(int customerId) throws SQLException {
        String query = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractCustomerFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public static Customer getCustomerByPhone(String phone) throws SQLException {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }

        String query = "SELECT * FROM customers WHERE phone = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, phone.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractCustomerFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public static Customer getCustomerByIdNumber(String idNumber) throws SQLException {
        if (idNumber == null || idNumber.trim().isEmpty()) {
            return null;
        }

        String query = "SELECT * FROM customers WHERE id_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, idNumber.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractCustomerFromResultSet(rs);
                }
            }
        }
        return null;
    }



    public static boolean addCustomer(Customer customer) throws SQLException {
        String query = "INSERT INTO customers (name, email, phone, id_proof, id_number, " +
                "address, country, registration_date, age, preferences, loyalty_points) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getPhone());
            pstmt.setString(4, customer.getIdProof());
            pstmt.setString(5, customer.getIdNumber());
            pstmt.setString(6, customer.getAddress());
            pstmt.setString(7, customer.getCountry());
            pstmt.setDate(8, java.sql.Date.valueOf(
                    customer.getRegistrationDate() != null ?
                            customer.getRegistrationDate() : LocalDate.now()
            ));

             if (customer.getAge() != null) {
                pstmt.setInt(9, customer.getAge());
            } else {
                pstmt.setNull(9, Types.INTEGER);
            }

            pstmt.setString(10, customer.getPreferences());

             if (customer.getLoyaltyPoints() != null) {
                pstmt.setInt(11, customer.getLoyaltyPoints());
            } else {
                pstmt.setNull(11, Types.INTEGER);
            }

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        customer.setCustomerId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }


    public static List<Customer> getAllCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String query = "SELECT * FROM customers ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                customers.add(extractCustomerFromResultSet(rs));
            }
        }
        return customers;
    }

    public static List<Customer> searchCustomers(String searchText, String type) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        String query;

         query = switch (type) {
            case "Name" -> "SELECT * FROM customers WHERE name LIKE ? ORDER BY name";
            case "Phone" -> "SELECT * FROM customers WHERE phone LIKE ? ORDER BY name";
            case "Email" -> "SELECT * FROM customers WHERE email LIKE ? ORDER BY name";
            default -> "SELECT * FROM customers WHERE name LIKE ? OR phone LIKE ? OR email LIKE ? ORDER BY name";
        };

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            switch (type) {
                case "Name", "Phone", "Email" -> pstmt.setString(1, "%" + searchText + "%");
                default -> {
                    pstmt.setString(1, "%" + searchText + "%");
                    pstmt.setString(2, "%" + searchText + "%");
                    pstmt.setString(3, "%" + searchText + "%");
                }
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(extractCustomerFromResultSet(rs));
                }
            }
        }
        return customers;
    }
}
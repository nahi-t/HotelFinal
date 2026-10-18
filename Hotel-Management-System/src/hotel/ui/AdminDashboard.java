package hotel.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import hotel.database.DatabaseConnection;

public class AdminDashboard extends JFrame {
    private final String currentUser;
    private javax.swing.Timer timer;
    private static final Logger LOGGER = Logger.getLogger(AdminDashboard.class.getName());
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JLabel timeLabel;

    public AdminDashboard(String username) {
        this.currentUser = username;
        initComponents();
        setupWindow();


    }

    private void initComponents() {
        setTitle("Hotel Management System - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to set look and feel", ex);
        }

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));

        mainPanel.add(createHeader(), BorderLayout.NORTH);
        mainPanel.add(createMainLayout(), BorderLayout.CENTER);
        mainPanel.add(createFooter(), BorderLayout.SOUTH);

        add(mainPanel);

        timer = new javax.swing.Timer(60000, e -> updateTime());
        timer.start();
    }

    private JPanel createMainLayout() {
        JPanel mainLayout = new JPanel(new BorderLayout());
        leftPanel = createNavigationPanel();
        leftPanel.setPreferredSize(new Dimension(250, 0));
        rightPanel = createAnalyticsPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(2);
        splitPane.setEnabled(false);
        mainLayout.add(splitPane, BorderLayout.CENTER);
        return mainLayout;
    }

    private JPanel createNavigationPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setBackground(new Color(31, 41, 55));
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel logo = new JLabel("ADMIN PANEL");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        logo.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        navPanel.add(logo);

        JLabel userInfo = new JLabel("User: " + currentUser);
        userInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userInfo.setForeground(new Color(156, 163, 175));
        userInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        userInfo.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        navPanel.add(userInfo);

        String[][] navItems = {
                {"Dashboard", "active"},
                {"Staff Management", ""},
                {"Room Management", ""},
                {"Performance Reports", ""},
                {"System Settings", ""}
        };

        ButtonGroup buttonGroup = new ButtonGroup();
        for (String[] item : navItems) {
            JToggleButton navBtn = createNavButton(item[0], item[1].equals("active"));
            navBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            navBtn.setMaximumSize(new Dimension(230, 45));

            switch (item[0]) {
                case "Dashboard": navBtn.addActionListener(e -> showAnalyticsPanel()); break;
                case "Staff Management": navBtn.addActionListener(e -> showStaffManagementPanel()); break;
                case "Room Management": navBtn.addActionListener(e -> showRoomManagementPanel()); break;
                case "Performance Reports": navBtn.addActionListener(e -> showPerformanceReports()); break;
                case "System Settings": navBtn.addActionListener(e -> showSystemSettings()); break;
            }

            buttonGroup.add(navBtn);
            navPanel.add(navBtn);
            navPanel.add(Box.createVerticalStrut(8));
        }

        navPanel.add(Box.createVerticalGlue());
        JButton logoutBtn = createLogoutButton();
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(230, 40));
        navPanel.add(logoutBtn);

        return navPanel;
    }

    private JToggleButton createNavButton(String text, boolean active) {
        JToggleButton button = new JToggleButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);

        // Set initial state
        if (active) {
            button.setSelected(true);
            button.setBackground(new Color(59, 130, 246));
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(31, 41, 55));
            button.setForeground(new Color(209, 213, 219));
            button.setBorderPainted(false);
        }

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                if (!button.isSelected()) {
                    button.setBackground(new Color(75, 85, 99));
                }
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                if (!button.isSelected()) {
                    button.setBackground(new Color(31, 41, 55));
                }
            }

            @Override
            public void mousePressed(MouseEvent evt) {
                if (!button.isSelected()) {
                    button.setBackground(new Color(59, 130, 246));
                }
            }
        });

        button.addChangeListener(e -> {
            if (button.isSelected()) {
                button.setBackground(new Color(59, 130, 246));
                button.setForeground(Color.WHITE);
            } else {
                button.setBackground(new Color(31, 41, 55));
                button.setForeground(new Color(209, 213, 219));
            }
        });

        return button;
    }

    private JButton createLogoutButton() {
        JButton button = new JButton("Logout");
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(new Color(239, 68, 68));
        button.setForeground(Color.black);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setContentAreaFilled(true);
        button.setOpaque(true);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(220, 38, 38));
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(239, 68, 68));
            }

            @Override
            public void mousePressed(MouseEvent evt) {
                button.setBackground(new Color(185, 28, 28));
            }
        });

        button.addActionListener(e -> logout());
        return button;
    }

    private JPanel createAnalyticsPanel() {
        JPanel analyticsPanel = new JPanel(new BorderLayout());
        analyticsPanel.setBackground(new Color(245, 247, 250));
        analyticsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Hotel Operations Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        analyticsPanel.add(title, BorderLayout.NORTH);

        JPanel statsPanel = createRealTimeStats();
        analyticsPanel.add(statsPanel, BorderLayout.CENTER);
        return analyticsPanel;
    }

    private JPanel createRealTimeStats() {
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBackground(new Color(245, 247, 250));
        String[][] statsData = getRealTimeStatistics();
        for (String[] stat : statsData) {
            statsPanel.add(createStatCard(stat[0], stat[1], stat[2], stat[3]));
        }
        return statsPanel;
    }

    private JPanel createStatCard(String title, String value, String change, String description) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        titleLabel.setForeground(new Color(107, 114, 128));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(new Color(31, 41, 55));

        JLabel changeLabel = new JLabel(change);
        changeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (change.contains("+")) changeLabel.setForeground(new Color(34, 197, 94));
        else if (change.contains("-")) changeLabel.setForeground(new Color(239, 68, 68));
        else changeLabel.setForeground(new Color(107, 114, 128));

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(156, 163, 175));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(valueLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(changeLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(descLabel);
        card.add(leftPanel, BorderLayout.CENTER);
        return card;
    }

    private String[][] getRealTimeStatistics() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            LocalDate today = LocalDate.now();

            // 1. Get occupancy data
            String occupancyQuery = "SELECT " +
                    "COUNT(DISTINCT r.room_number) as total_rooms, " +
                    "COUNT(DISTINCT CASE WHEN b.status = 'CHECKED_IN' AND CURDATE() BETWEEN b.check_in_date AND b.check_out_date THEN r.room_number END) as occupied " +
                    "FROM rooms r " +
                    "LEFT JOIN bookings b ON r.room_number = b.room_id " +
                    "WHERE r.status != 'MAINTENANCE'";

            int occupiedRooms = 0;
            int totalRooms = 0;

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(occupancyQuery)) {
                if (rs.next()) {
                    totalRooms = rs.getInt("total_rooms");
                    occupiedRooms = rs.getInt("occupied");
                }
            }


            int yesterdayOccupied = 0;
            String yesterdayOccupancyQuery = "SELECT COUNT(DISTINCT room_id) as occupied " +
                    "FROM bookings WHERE DATE(check_in_date) = DATE_SUB(CURDATE(), INTERVAL 1 DAY) " +
                    "AND status = 'CHECKED_IN'";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(yesterdayOccupancyQuery)) {
                if (rs.next()) {
                    yesterdayOccupied = rs.getInt("occupied");
                }
            }

            int occupancyChange = yesterdayOccupied > 0 ?
                    ((occupiedRooms - yesterdayOccupied) * 100) / yesterdayOccupied : 0;
            int occupancyPercent = totalRooms > 0 ? (occupiedRooms * 100) / totalRooms : 0;

            //  Get today's revenue
            double todayRevenue = 0;
            String revenueQuery = "SELECT COALESCE(SUM(amount), 0) as revenue " +
                    "FROM payments WHERE DATE(payment_date) = CURDATE()";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(revenueQuery)) {
                if (rs.next()) todayRevenue = rs.getDouble("revenue");
            }

            // Get yesterday's revenue for comparison
            double yesterdayRevenue = 0;
            String yesterdayQuery = "SELECT COALESCE(SUM(amount), 0) as revenue " +
                    "FROM payments WHERE DATE(payment_date) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(yesterdayQuery)) {
                if (rs.next()) yesterdayRevenue = rs.getDouble("revenue");
            }

            double revenueChange = yesterdayRevenue > 0 ?
                    ((todayRevenue - yesterdayRevenue) / yesterdayRevenue) * 100 : 0;
            String revenueChangeStr = String.format("%.1f%%", revenueChange);

            //  Get active staff
            int activeStaff = 0;
            try {
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet columns = meta.getColumns(null, null, "users", "is_active");

                if (columns.next()) {
                    String staffQuery = "SELECT COUNT(*) as active FROM staff WHERE status = 'ACTIVE'";
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(staffQuery)) {
                        if (rs.next()) activeStaff = rs.getInt("active");
                    }
                } else {

                    String staffQuery = "SELECT COUNT(*) as active FROM users WHERE role IN ('ADMIN', 'MANAGER', 'RECEPTION')";
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery(staffQuery)) {
                        if (rs.next()) activeStaff = rs.getInt("active");
                    }
                }
            } catch (SQLException e) {
                activeStaff = 0;
            }


            int pendingBookings = 0;
            try {
                String pendingQuery = "SELECT COUNT(*) as pending FROM bookings WHERE status = 'PENDING'";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(pendingQuery)) {
                    if (rs.next()) pendingBookings = rs.getInt("pending");
                }
            } catch (SQLException e) {
                pendingBookings = 0;
            }

            return new String[][] {
                    {"Current Occupancy", occupiedRooms + "/" + totalRooms,
                            occupancyPercent + "% " + (occupancyChange > 0 ? "+" : ""),
                            occupancyChange + " from yesterday"},
                    {"Today's Revenue", String.format("$%.2f", todayRevenue),
                            revenueChangeStr + (revenueChange > 0 ? " +" : ""),
                            "Revenue generated today"},
                    {"Active Staff", String.valueOf(activeStaff),
                            activeStaff > 5 ? "Optimal" : "Limited",
                            "Staff currently active"},
                    {"Pending Bookings", String.valueOf(pendingBookings),
                            pendingBookings > 10 ? "High" : "Normal",
                            "Bookings awaiting confirmation"}
            };

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get statistics: " + ex.getMessage(), ex);
            return new String[][] {
                    {"Current Occupancy", "0/0", "0%", "Rooms currently occupied"},
                    {"Today's Revenue", "$0.00", "0%", "Revenue generated today"},
                    {"Active Staff", "0", "Off Duty", "Staff currently working"},
                    {"Pending Bookings", "0", "Normal", "Bookings awaiting confirmation"}
            };
        }
    }

    //   DATA FETCHING METHODS

    private Map<String, Double> getFinancialData() {
        Map<String, Double> data = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Total Revenue
            String totalRevenueQuery = "SELECT COALESCE(SUM(amount), 0) as total FROM payments";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(totalRevenueQuery)) {
                if (rs.next()) data.put("total_revenue", rs.getDouble("total"));
            }

            // Monthly Average
            String monthlyAvgQuery = "SELECT COALESCE(AVG(daily_revenue), 0) as monthly_avg FROM (" +
                    "SELECT DATE(payment_date) as payment_day, SUM(amount) as daily_revenue " +
                    "FROM payments " +
                    "WHERE payment_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
                    "GROUP BY DATE(payment_date)" +
                    ") daily_revenues";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(monthlyAvgQuery)) {
                if (rs.next()) data.put("monthly_avg", rs.getDouble("monthly_avg"));
            }

            // Occupancy Rate
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "bookings", null);
            if (tables.next()) {
                String occupancyQuery = "SELECT " +
                        "(SELECT COUNT(DISTINCT room_id) FROM bookings " +
                        "WHERE status IN ('CONFIRMED', 'ACTIVE', 'CHECKED_IN') " +
                        "AND CURDATE() BETWEEN check_in_date AND check_out_date) as occupied_rooms, " +
                        "(SELECT COUNT(*) FROM rooms WHERE status != 'MAINTENANCE') as total_rooms";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(occupancyQuery)) {
                    if (rs.next()) {
                        int occupiedRooms = rs.getInt("occupied_rooms");
                        int totalRooms = rs.getInt("total_rooms");
                        double occupancyRate = totalRooms > 0 ? (occupiedRooms * 100.0) / totalRooms : 0;
                        data.put("occupancy_rate", occupancyRate);
                    }
                }
            } else {
                String occupancyQuery = "SELECT " +
                        "COUNT(CASE WHEN status = 'OCCUPIED' THEN 1 END) as occupied_rooms, " +
                        "COUNT(*) as total_rooms " +
                        "FROM rooms WHERE status != 'MAINTENANCE'";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(occupancyQuery)) {
                    if (rs.next()) {
                        int occupiedRooms = rs.getInt("occupied_rooms");
                        int totalRooms = rs.getInt("total_rooms");
                        double occupancyRate = totalRooms > 0 ? (occupiedRooms * 100.0) / totalRooms : 0;
                        data.put("occupancy_rate", occupancyRate);
                    }
                }
            }

            // Average Room Rate
            String avgRateQuery = "SELECT COALESCE(AVG(total_amount / " +
                    "GREATEST(DATEDIFF(check_out_date, check_in_date), 1)), 0) as avg_rate " +
                    "FROM bookings WHERE status = 'COMPLETED' " +
                    "AND DATEDIFF(check_out_date, check_in_date) > 0";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(avgRateQuery)) {
                if (rs.next()) data.put("avg_room_rate", rs.getDouble("avg_rate"));
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get financial data", ex);
            data.put("total_revenue", 0.0);
            data.put("monthly_avg", 0.0);
            data.put("occupancy_rate", 0.0);
            data.put("avg_room_rate", 0.0);
        }
        return data;
    }

    private String generateRevenueChart() {
        StringBuilder chart = new StringBuilder();
        chart.append("Monthly Revenue Trend (Last 6 Months)\n");
        chart.append("=========================================\n\n");

        try (Connection conn = DatabaseConnection.getConnection()) {
            String revenueQuery = "SELECT " +
                    "DATE_FORMAT(payment_date, '%b %Y') as month_year, " +
                    "COALESCE(SUM(amount), 0) as revenue " +
                    "FROM payments " +
                    "WHERE payment_date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
                    "GROUP BY YEAR(payment_date), MONTH(payment_date), DATE_FORMAT(payment_date, '%b %Y') " +
                    "ORDER BY YEAR(payment_date), MONTH(payment_date)";

            List<Double> revenues = new ArrayList<>();
            List<String> months = new ArrayList<>();

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(revenueQuery)) {
                while (rs.next()) {
                    String monthYear = rs.getString("month_year");
                    double revenue = rs.getDouble("revenue");
                    months.add(monthYear);
                    revenues.add(revenue);
                }
            }

            if (revenues.isEmpty()) {
                return "No revenue data available.\n";
            }

            double maxRevenue = 0;
            for (double rev : revenues) if (rev > maxRevenue) maxRevenue = rev;

            for (int i = 0; i < months.size(); i++) {
                chart.append(String.format("%-8s | ", months.get(i)));
                int barLength = maxRevenue > 0 ? (int) ((revenues.get(i) / maxRevenue) * 40) : 0;
                for (int j = 0; j < barLength; j++) chart.append("*");
                chart.append(String.format(" $%,.2f\n", revenues.get(i)));
            }

            if (revenues.size() >= 2) {
                double firstMonth = revenues.get(0);
                double lastMonth = revenues.get(revenues.size() - 1);
                double growth = firstMonth > 0 ? ((lastMonth - firstMonth) / firstMonth) * 100 : 0;
                chart.append("\n-----------------------------------------\n");
                chart.append(String.format("Growth: %+.1f%% over period\n", growth));
                chart.append(String.format("Total: $%,.2f", revenues.stream().mapToDouble(Double::doubleValue).sum()));
            }

        } catch (SQLException ex) {
            chart.append("Loading revenue data...\n");
        }
        return chart.toString();
    }

    private Object[][] getOccupancyData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT " +
                    "room_type, " +
                    "COUNT(*) as total_rooms, " +
                    "SUM(CASE WHEN status = 'OCCUPIED' THEN 1 ELSE 0 END) as occupied, " +
                    "SUM(CASE WHEN status = 'AVAILABLE' THEN 1 ELSE 0 END) as available " +
                    "FROM rooms " +
                    "GROUP BY room_type " +
                    "ORDER BY room_type";

            List<Object[]> rows = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    String roomType = rs.getString("room_type");
                    int total = rs.getInt("total_rooms");
                    int occupied = rs.getInt("occupied");
                    int available = rs.getInt("available");
                    double occupancyPercent = total > 0 ? (occupied * 100.0) / total : 0;
                    rows.add(new Object[]{
                            roomType != null ? roomType : "Unknown",
                            total,
                            occupied,
                            available,
                            String.format("%.1f%%", occupancyPercent)
                    });
                }
            }

            if (rows.isEmpty()) {
                return new Object[0][];
            }
            return rows.toArray(new Object[0][]);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get occupancy data", ex);
            return new Object[0][];
        }
    }

    //   PERFORMANCE REPORTS PANEL
    private void showPerformanceReports() {
        switchRightPanel(createPerformanceReportsPanel());
    }

    private JPanel createPerformanceReportsPanel() {
        JPanel reportsPanel = new JPanel(new BorderLayout());
        reportsPanel.setBackground(Color.WHITE);
        reportsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Performance Reports & Analytics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        reportsPanel.add(title, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.addTab("Financial Reports", createFinancialReportsTab());
        tabbedPane.addTab("Occupancy Analytics", createOccupancyAnalyticsTab());
        reportsPanel.add(tabbedPane, BorderLayout.CENTER);
        return reportsPanel;
    }

    private JPanel createFinancialReportsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Map<String, Double> financialData = getFinancialData();
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        summaryPanel.setBackground(Color.WHITE);

        String[][] summaryItems = {
                {"Total Revenue", String.format("$%.2f", financialData.getOrDefault("total_revenue", 0.0))},
                {"Monthly Avg", String.format("$%.2f", financialData.getOrDefault("monthly_avg", 0.0))},
                {"Occupancy Rate", String.format("%.1f%%", financialData.getOrDefault("occupancy_rate", 0.0))},
                {"Avg Room Rate", String.format("$%.2f", financialData.getOrDefault("avg_room_rate", 0.0))}
        };

        for (String[] item : summaryItems) {
            summaryPanel.add(createMetricCard(item[0], item[1]));
        }
        panel.add(summaryPanel, BorderLayout.NORTH);

        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JLabel chartLabel = new JLabel("Monthly Revenue Trend", SwingConstants.CENTER);
        chartLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        chartPanel.add(chartLabel, BorderLayout.NORTH);

        JTextArea chartArea = new JTextArea();
        chartArea.setEditable(false);
        chartArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        chartArea.setText(generateRevenueChart());
        chartArea.setBackground(new Color(248, 250, 252));
        chartArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chartPanel.add(new JScrollPane(chartArea), BorderLayout.CENTER);
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createOccupancyAnalyticsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Room Occupancy Analytics", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        String[] columns = {"Room Type", "Total Rooms", "Occupied", "Vacant", "Occupancy %"};
        Object[][] data = getOccupancyData();

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(59, 130, 246));
        table.getTableHeader().setForeground(Color.black);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

     private void showSystemSettings() {
        switchRightPanel(createSystemSettingsPanel());
    }

    private JPanel createSystemSettingsPanel() {
        JPanel settingsPanel = new JPanel(new BorderLayout());
        settingsPanel.setBackground(Color.WHITE);
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("System Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        settingsPanel.add(title, BorderLayout.NORTH);

        JTabbedPane settingsTabs = new JTabbedPane();
        settingsTabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        settingsTabs.addTab("General Settings", createGeneralSettingsTab());
        settingsTabs.addTab("Users & Roles", createUserManagementTab());
        settingsPanel.add(settingsTabs, BorderLayout.CENTER);
        return settingsPanel;
    }

    //  SETTINGS DATABASE METHODS
    private Map<String, String> loadSettingsFromDatabase() {
        Map<String, String> settings = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
             DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "settings", null);

            if (!tables.next()) {
                 createSettingsTable(conn);
            }

            // Load all settings
            String query = "SELECT setting_key, setting_value FROM settings";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    String key = rs.getString("setting_key");
                    String value = rs.getString("setting_value");
                    settings.put(key, value);
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load settings from database", ex);
        }

        return settings;
    }

    private void createSettingsTable(Connection conn) throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS settings (" +
                "setting_key VARCHAR(100) PRIMARY KEY," +
                "setting_value TEXT NOT NULL," +
                "description TEXT," +
                "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        }

        // Insert default settings
        insertDefaultSettings(conn);
    }

    private void insertDefaultSettings(Connection conn) throws SQLException {
        String[][] defaultSettings = {
                {"hotel_name", "Ethiopian Luxury Hotel", "Hotel Name"},
                {"contact_email", "info@ethiopianhotel.com", "Contact Email Address"},
                {"phone_number", "+251-11-123-4567", "Hotel Phone Number"},
                {"address", "Addis Ababa, Ethiopia", "Hotel Physical Address"},
                {"check_in_time", "14:00", "Standard Check-in Time"},
                {"check_out_time", "12:00", "Standard Check-out Time"},
                {"tax_rate", "15", "Tax Rate Percentage"},
                {"currency", "USD", "Default Currency"},
                {"booking_timeout", "30", "Booking timeout in minutes"},
                {"min_booking_days", "1", "Minimum booking days"},
                {"max_booking_days", "30", "Maximum booking days"},
                {"cancellation_hours", "24", "Cancellation notice in hours"},
                {"late_checkout_fee", "50", "Late checkout fee percentage"},
                {"breakfast_included", "true", "Breakfast included in room rate"},
                {"wifi_included", "true", "WiFi included in room rate"},
                {"parking_fee", "10", "Daily parking fee"}
        };

        String insertSQL = "INSERT INTO settings (setting_key, setting_value, description) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            for (String[] setting : defaultSettings) {
                pstmt.setString(1, setting[0]);
                pstmt.setString(2, setting[1]);
                pstmt.setString(3, setting[2]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private JPanel createGeneralSettingsTab() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create main form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);


        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel hotelInfo = new JLabel("General Settings");
        hotelInfo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        hotelInfo.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        formPanel.add(hotelInfo, gbc);

         Map<String, String> settings = loadSettingsFromDatabase();


        String[][] generalSettings = {
                {"Hotel Name:", "hotel_name"},
                {"Contact Email:", "contact_email"},
                {"Phone Number:", "phone_number"},
                {"Address:", "address"},
                {"Check-in Time:", "check_in_time"},
                {"Check-out Time:", "check_out_time"},
                {"Tax Rate %:", "tax_rate"},
                {"Currency:", "currency"},
                {"Booking Timeout (min):", "booking_timeout"},
                {"Min Booking Days:", "min_booking_days"},
                {"Max Booking Days:", "max_booking_days"}
        };

         Map<String, JTextField> settingFields = new HashMap<>();

        for (int i = 0; i < generalSettings.length; i++) {
            String labelText = generalSettings[i][0];
            String settingKey = generalSettings[i][1];

             String currentValue = settings.getOrDefault(settingKey, getDefaultSettingValue(settingKey));

            gbc.gridx = 0;
            gbc.gridy = i + 1;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.3;

            JLabel label = new JLabel(labelText);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            label.setPreferredSize(new Dimension(150, 25));
            formPanel.add(label, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.7;
            JTextField field = new JTextField(currentValue, 20);
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
             settingFields.put(settingKey, field);
            formPanel.add(field, gbc);
        }

        panel.add(formPanel, BorderLayout.CENTER);

        // Create button panel at bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setBackground(new Color(59, 130, 246));
        saveBtn.setForeground(Color.black);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        saveBtn.addActionListener(e -> saveGeneralSettings(settingFields));

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(156, 163, 175));
        refreshBtn.setForeground(Color.black);
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        refreshBtn.addActionListener(e -> refreshGeneralSettings(settingFields));

        JButton resetBtn = new JButton("Reset to Defaults");
        resetBtn.setBackground(new Color(239, 68, 68));
        resetBtn.setForeground(Color.black);
        resetBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        resetBtn.addActionListener(e -> resetSettingsToDefaults(settingFields));

        buttonPanel.add(resetBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(saveBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private String getDefaultSettingValue(String key) {
        Map<String, String> defaults = new HashMap<>();
        defaults.put("hotel_name", "Ethiopian Luxury Hotel");
        defaults.put("contact_email", "info@ethiopianhotel.com");
        defaults.put("phone_number", "+251-11-123-4567");
        defaults.put("address", "Addis Ababa, Ethiopia");
        defaults.put("check_in_time", "14:00");
        defaults.put("check_out_time", "12:00");
        defaults.put("tax_rate", "15");
        defaults.put("currency", "USD");
        defaults.put("booking_timeout", "30");
        defaults.put("min_booking_days", "1");
        defaults.put("max_booking_days", "30");
        defaults.put("cancellation_hours", "24");
        defaults.put("late_checkout_fee", "50");
        defaults.put("breakfast_included", "true");
        defaults.put("wifi_included", "true");
        defaults.put("parking_fee", "10");

        return defaults.getOrDefault(key, "");
    }

    private void saveGeneralSettings(Map<String, JTextField> settingFields) {
        try (Connection conn = DatabaseConnection.getConnection()) {
             String upsertSQL = "INSERT INTO settings (setting_key, setting_value, last_updated) " +
                    "VALUES (?, ?, CURRENT_TIMESTAMP) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "setting_value = VALUES(setting_value), " +
                    "last_updated = VALUES(last_updated)";

            try (PreparedStatement pstmt = conn.prepareStatement(upsertSQL)) {
                int updatedCount = 0;
                for (Map.Entry<String, JTextField> entry : settingFields.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue().getText().trim();

                     if (value.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                "Setting '" + key + "' cannot be empty!",
                                "Validation Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                     if (key.equals("tax_rate") || key.equals("booking_timeout") ||
                            key.equals("min_booking_days") || key.equals("max_booking_days")) {
                        try {
                            double numValue = Double.parseDouble(value.replace("%", ""));
                            if (numValue < 0) {
                                JOptionPane.showMessageDialog(this,
                                        "Setting '" + key + "' must be a positive number!",
                                        "Validation Error",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        } catch (NumberFormatException e) {
                            JOptionPane.showMessageDialog(this,
                                    "Setting '" + key + "' must be a valid number!",
                                    "Validation Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    pstmt.setString(1, key);
                    pstmt.setString(2, value);
                    pstmt.addBatch();
                    updatedCount++;
                }

                int[] results = pstmt.executeBatch();
                int successfulUpdates = 0;
                for (int result : results) {
                    if (result >= 0) successfulUpdates++;
                }

                 applySettingsToSystem();

                JOptionPane.showMessageDialog(this,
                        String.format("%d settings saved successfully! Changes will take effect immediately.", successfulUpdates),
                        "Settings Saved",
                        JOptionPane.INFORMATION_MESSAGE);

            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to save settings to database", ex);
            JOptionPane.showMessageDialog(this,
                    "Failed to save settings! Error: " + ex.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applySettingsToSystem() {
         try {
            Map<String, String> settings = loadSettingsFromDatabase();

             if (settings.containsKey("tax_rate")) {
                try {
                    double taxRate = Double.parseDouble(settings.get("tax_rate"));
                     System.out.println("Tax rate updated to: " + taxRate + "%");
                } catch (NumberFormatException e) {
                    System.err.println("Invalid tax rate format: " + settings.get("tax_rate"));
                }
            }

             if (settings.containsKey("currency")) {
                String currency = settings.get("currency");
                System.out.println("Currency updated to: " + currency);
            }

             System.out.println("System settings have been updated and applied.");

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to apply settings to system", e);
        }
    }

    private void refreshGeneralSettings(Map<String, JTextField> settingFields) {
        Map<String, String> currentSettings = loadSettingsFromDatabase();

         for (Map.Entry<String, JTextField> entry : settingFields.entrySet()) {
            String key = entry.getKey();
            JTextField field = entry.getValue();

            String currentValue = currentSettings.get(key);
            if (currentValue != null) {
                field.setText(currentValue);
            } else {
                field.setText(getDefaultSettingValue(key));
            }
        }

        JOptionPane.showMessageDialog(this,
                "Settings refreshed from database!",
                "Refresh Complete",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void resetSettingsToDefaults(Map<String, JTextField> settingFields) {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to reset all settings to defaults? This will overwrite all current settings.",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                 String deleteSQL = "DELETE FROM settings";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(deleteSQL);
                }

                 insertDefaultSettings(conn);

                 refreshGeneralSettings(settingFields);

                 applySettingsToSystem();

                JOptionPane.showMessageDialog(this,
                        "All settings have been reset to defaults!",
                        "Reset Complete",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Failed to reset settings", ex);
                JOptionPane.showMessageDialog(this,
                        "Failed to reset settings! Error: " + ex.getMessage(),
                        "Reset Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // SETTINGS UTILITY METHODS
    public static String getSetting(String key, String defaultValue) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT setting_value FROM settings WHERE setting_key = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, key);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("setting_value");
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Failed to get setting: " + key, ex);
        }
        return defaultValue;
    }

    public static double getSettingAsDouble(String key, double defaultValue) {
        try {
            String value = getSetting(key, String.valueOf(defaultValue));
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getSettingAsBoolean(String key, boolean defaultValue) {
        String value = getSetting(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value) || value.equalsIgnoreCase("1") ||
                value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true");
    }

    //   USER MANAGEMENT TAB
    private JPanel createUserManagementTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("User Accounts & Permissions", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        String[] columns = {"Username", "Role", "Created Date", "Status", "Actions"};
        Object[][] userData = getUserData();

        DefaultTableModel model = new DefaultTableModel(userData, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(59, 130, 246));
        table.getTableHeader().setForeground(Color.black);

        // Set up button renderer and editor
        table.getColumn("Actions").setCellRenderer(new ButtonRenderer());
        table.getColumn("Actions").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton addUserBtn = new JButton("Add New User");
        addUserBtn.setBackground(new Color(34, 197, 94));
        addUserBtn.setForeground(Color.black);
        addUserBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addUserBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addUserBtn.addActionListener(e -> addNewUser());
        buttonPanel.add(addUserBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(new Color(59, 130, 246));
        refreshBtn.setForeground(Color.black);
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> refreshUserTable());
        buttonPanel.add(refreshBtn);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private Object[][] getUserData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet usersTable = meta.getTables(null, null, "users", null);
            if (!usersTable.next()) {
                return new Object[0][];
            }

            String query = "SELECT " +
                    "username, " +
                    "role, " +
                    "created_at, " +
                    "is_active " +
                    "FROM users " +
                    "ORDER BY role, username";

            List<Object[]> rows = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    String role = rs.getString("role");
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    boolean isActive = rs.getBoolean("is_active");

                    String createdDate = createdAt != null ?
                            createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) :
                            "Unknown";

                    String status = isActive ? "Active" : "Inactive";

                    rows.add(new Object[]{
                            username,
                            role,
                            createdDate,
                            status,
                            "Edit"
                    });
                }
            }

            if (rows.isEmpty()) {
                return new Object[0][];
            }

            return rows.toArray(new Object[0][]);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get user data", ex);
            return new Object[0][];
        }
    }

    private void addNewUser() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField nameField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"ADMIN", "MANAGER", "RECEPTION"});
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});

        panel.add(new JLabel("Username*:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password*:"));
        panel.add(passwordField);
        panel.add(new JLabel("Full Name*:"));
        panel.add(nameField);
        panel.add(new JLabel("Email*:"));
        panel.add(emailField);
        panel.add(new JLabel("Role*:"));
        panel.add(roleCombo);
        panel.add(new JLabel("Status:"));
        panel.add(statusCombo);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String role = (String) roleCombo.getSelectedItem();
            String status = (String) statusCombo.getSelectedItem();

            if (username.isEmpty() || password.isEmpty() || name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please fill all required fields (*)!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid email address!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (usernameExists(username)) {
                JOptionPane.showMessageDialog(this,
                        "Username '" + username + "' already exists! Please choose a different username.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (emailExists(email)) {
                JOptionPane.showMessageDialog(this,
                        "Email '" + email + "' is already registered! Please use a different email.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean isActive = status.equals("Active");

            if (saveUserToDatabase(username, password, name, email, role, isActive)) {
                JOptionPane.showMessageDialog(this,
                        "User '" + username + "' added successfully! Role: " + role + " Status: " + status + " Email: " + email,
                        "User Added", JOptionPane.INFORMATION_MESSAGE);

                refreshUserTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to add user! Please check database connection.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean usernameExists(String username) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT COUNT(*) as count FROM users WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count") > 0;
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error checking username", ex);
        }
        return false;
    }

    private boolean emailExists(String email) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT COUNT(*) as count FROM users WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, email);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("count") > 0;
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error checking email", ex);
        }
        return false;
    }

    private boolean saveUserToDatabase(String username, String password, String name,
                                       String email, String role, boolean isActive) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO users (username, password, name, email, role, is_active, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, NOW())";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, name);
                pstmt.setString(4, email);
                pstmt.setString(5, role);
                pstmt.setBoolean(6, isActive);

                int rowsAffected = pstmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error saving user to database", ex);
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void refreshUserTable() {
        if (rightPanel.getComponentCount() > 0) {
            Component currentPanel = rightPanel.getComponent(0);

            if (currentPanel instanceof JPanel) {
                JPanel mainPanel = (JPanel) currentPanel;

                Component[] mainComponents = mainPanel.getComponents();
                for (Component mainComp : mainComponents) {
                    if (mainComp instanceof JTabbedPane) {
                        JTabbedPane tabbedPane = (JTabbedPane) mainComp;

                        int selectedIndex = tabbedPane.getSelectedIndex();
                        Component selectedTab = tabbedPane.getComponentAt(selectedIndex);

                        if (selectedTab instanceof JPanel) {
                            findAndRefreshUserTable((JPanel) selectedTab);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void findAndRefreshUserTable(JPanel panel) {
        Component[] components = panel.getComponents();
        for (Component comp : components) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                Component view = scrollPane.getViewport().getView();
                if (view instanceof JTable) {
                    JTable table = (JTable) view;
                    DefaultTableModel model = (DefaultTableModel) table.getModel();

                    model.setRowCount(0);
                    Object[][] newData = getUserData();
                    for (Object[] row : newData) {
                        model.addRow(row);
                    }

                    break;
                }
            }
        }
    }


    private JPanel createMetricCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(107, 114, 128));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(new Color(31, 41, 55));

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.add(titleLabel, BorderLayout.NORTH);
        content.add(valueLabel, BorderLayout.CENTER);
        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // PANEL SWITCHING
    private void showAnalyticsPanel() {
        switchRightPanel(createAnalyticsPanel());
    }

    private void showStaffManagementPanel() {
        try {
            Class<?> staffPanelClass = Class.forName("hotel.ui.StaffManagementPanel");
            JPanel staffPanel = (JPanel) staffPanelClass.getDeclaredConstructor().newInstance();
            switchRightPanel(staffPanel);
        } catch (Exception e) {
            JPanel placeholder = new JPanel(new BorderLayout());
            placeholder.setBackground(Color.WHITE);
            JLabel message = new JLabel("Staff Management Panel", SwingConstants.CENTER);
            message.setFont(new Font("Segoe UI", Font.BOLD, 24));
            message.setForeground(new Color(59, 130, 246));
            placeholder.add(message, BorderLayout.CENTER);
            switchRightPanel(placeholder);
        }
    }

    private void showRoomManagementPanel() {
        try {
            Class<?> roomPanelClass = Class.forName("hotel.ui.RoomManagementPanel");
            JPanel roomPanel = (JPanel) roomPanelClass.getDeclaredConstructor().newInstance();
            switchRightPanel(roomPanel);
        } catch (Exception e) {
            JPanel placeholder = new JPanel(new BorderLayout());
            placeholder.setBackground(Color.WHITE);
            JLabel message = new JLabel("Room Management Panel", SwingConstants.CENTER);
            message.setFont(new Font("Segoe UI", Font.BOLD, 24));
            message.setForeground(new Color(59, 130, 246));
            placeholder.add(message, BorderLayout.CENTER);
            switchRightPanel(placeholder);
        }
    }

    private void switchRightPanel(JPanel newPanel) {
        rightPanel.removeAll();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(newPanel, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }


    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 25, 15, 25)
        ));

        JLabel title = new JLabel("Hotel Management System - Admin Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(37, 99, 235));

        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timeLabel.setForeground(new Color(107, 114, 128));
        updateTimeLabel();

        header.add(title, BorderLayout.WEST);
        header.add(timeLabel, BorderLayout.EAST);
        return header;
    }

    private void updateTimeLabel() {
        String time = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("hh:mm a, EEE, MMM dd yyyy")
        );
        timeLabel.setText("Time: " + time);
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(31, 41, 55));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        JLabel footerText = new JLabel("© 2026 Hotel Management System  | Admin Panel");
        footerText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerText.setForeground(new Color(156, 163, 175));

        JLabel versionLabel = new JLabel("Status: Connected");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(34, 197, 94));

        footer.add(footerText, BorderLayout.WEST);
        footer.add(versionLabel, BorderLayout.EAST);
        return footer;
    }

    private void updateTime() {
        SwingUtilities.invokeLater(() -> updateTimeLabel());
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            timer.stop();
            DatabaseConnection.closeConnection();
            dispose();
        }
    }

    private void setupWindow() {
        try {
            ImageIcon icon = new ImageIcon("images/icon.png");
            setIconImage(icon.getImage());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to load icon", ex);
        }
    }

     class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            setBackground(new Color(59, 130, 246));
            setForeground(Color.black);
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int editedRow;
        private JTable currentTable;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentTable = table;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            button.setBackground(new Color(59, 130, 246));
            button.setForeground(Color.black);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            isPushed = true;
            editedRow = row;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed && currentTable != null) {
                DefaultTableModel model = (DefaultTableModel) currentTable.getModel();

                // Get user data from the clicked row
                String username = (String) model.getValueAt(editedRow, 0);
                String role = (String) model.getValueAt(editedRow, 1);
                String status = (String) model.getValueAt(editedRow, 3);

                 editUser(username, role, status, model);
            }
            isPushed = false;
            return label;
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        private void editUser(String username, String role, String status, DefaultTableModel model) {
             JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

            JTextField usernameField = new JTextField(username, 20);
            usernameField.setEditable(false);
            JComboBox<String> roleCombo = new JComboBox<>(new String[]{"ADMIN", "MANAGER", "RECEPTION"});
            roleCombo.setSelectedItem(role);
            JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Active", "Inactive"});
            statusCombo.setSelectedItem(status);

            panel.add(new JLabel("Username:"));
            panel.add(usernameField);
            panel.add(new JLabel("Role:"));
            panel.add(roleCombo);
            panel.add(new JLabel("Status:"));
            panel.add(statusCombo);

            int result = JOptionPane.showConfirmDialog(AdminDashboard.this, panel,
                    "Edit User: " + username, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String newRole = (String) roleCombo.getSelectedItem();
                String newStatus = (String) statusCombo.getSelectedItem();

                 if (updateUserInDatabase(username, newRole, newStatus.equals("Active"))) {
                     model.setValueAt(newRole, editedRow, 1);
                    model.setValueAt(newStatus, editedRow, 3);

                    JOptionPane.showMessageDialog(AdminDashboard.this,
                            "User '" + username + "' updated successfully!\n" +
                                    "New Role: " + newRole + "\n" +
                                    "New Status: " + newStatus,
                            "User Updated", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(AdminDashboard.this,
                            "Failed to update user! Please check database connection.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        private boolean updateUserInDatabase(String username, String newRole, boolean isActive) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String query = "UPDATE users SET role = ?, is_active = ? WHERE username = ?";

                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, newRole);
                    pstmt.setBoolean(2, isActive);
                    pstmt.setString(3, username);

                    int rowsAffected = pstmt.executeUpdate();
                    return rowsAffected > 0;
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error updating user in database", ex);
                JOptionPane.showMessageDialog(AdminDashboard.this,
                        "Database error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }

    //  MAIN METHOD
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {

                String username = "admin";
                AdminDashboard dashboard = new AdminDashboard(username);
                dashboard.setVisible(true);
                dashboard.setLocationRelativeTo(null);
                System.out.println("Admin Dashboard started successfully!");
            } catch (Exception e) {
                System.err.println("Failed to start Admin Dashboard: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to start application: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
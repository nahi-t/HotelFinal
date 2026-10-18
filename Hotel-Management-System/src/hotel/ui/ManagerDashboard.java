package hotel.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;
import hotel.database.DatabaseConnection;

public class ManagerDashboard extends JFrame {
    private final String currentUser;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private static final Logger LOGGER = Logger.getLogger(ManagerDashboard.class.getName());

    public ManagerDashboard(String username) {
        this.currentUser = username;
        initComponents();
        setupWindow();
    }

    private void initComponents() {
        setTitle("Hotel Management System - Manager Dashboard");
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
    }

    private JPanel createMainLayout() {
        JPanel mainLayout = new JPanel(new BorderLayout());

         leftPanel = createManagerNavPanel();
        leftPanel.setPreferredSize(new Dimension(250, 0));

         rightPanel = createPerformancePanel(); // Default view
        rightPanel.setPreferredSize(new Dimension(1150, 0));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(2);
        splitPane.setEnabled(false);

        mainLayout.add(splitPane, BorderLayout.CENTER);

        return mainLayout;
    }

    private JPanel createManagerNavPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setBackground(new Color(20, 184, 166));
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel navTitle = new JLabel("MANAGER PANEL");
        navTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        navTitle.setForeground(Color.WHITE);
        navTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        navTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        navPanel.add(navTitle);

         String[][] navItems = {
                {"Performance Overview", "active"},
                {"Room Status", ""},
                {"Staff Overview", ""},
                {"Daily Reports", ""}
        };

        ButtonGroup buttonGroup = new ButtonGroup();

        for (String[] item : navItems) {
            JToggleButton navBtn = createManagerNavButton(item[0], item[1].equals("active"));
            navBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            navBtn.setMaximumSize(new Dimension(220, 45));

            switch (item[0]) {
                case "Performance Overview":
                    navBtn.addActionListener(e -> showPerformancePanel());
                    break;
                case "Room Status":
                    navBtn.addActionListener(e -> showRoomStatusPanel());
                    break;
                case "Staff Overview":
                    navBtn.addActionListener(e -> showStaffOverviewPanel());
                    break;
                case "Daily Reports":
                    navBtn.addActionListener(e -> showDailyReportsPanel());
                    break;
            }

            buttonGroup.add(navBtn);
            navPanel.add(navBtn);
            navPanel.add(Box.createVerticalStrut(10));
        }

        navPanel.add(Box.createVerticalGlue());

        JButton logoutBtn = createManagerLogoutButton();
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(220, 45));
        navPanel.add(logoutBtn);

        return navPanel;
    }

    private JToggleButton createManagerNavButton(String text, boolean active) {
        JToggleButton button = new JToggleButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(20, 184, 166));
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setFocusPainted(false);

        if (active) {
            button.setSelected(true);
            button.setBackground(new Color(20, 184, 166));
            button.setForeground(Color.WHITE);
        }

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                if (!button.isSelected()) {
                    button.setBackground(new Color(240, 240, 240));
                }
            }
            public void mouseExited(MouseEvent evt) {
                if (!button.isSelected()) {
                    button.setBackground(Color.WHITE);
                }
            }

            // FIX: Update button state on click
            public void mouseClicked(MouseEvent evt) {
                // Clear selection from other buttons in group
                Container parent = button.getParent();
                for (Component comp : parent.getComponents()) {
                    if (comp instanceof JToggleButton && comp != button) {
                        ((JToggleButton) comp).setSelected(false);
                        ((JToggleButton) comp).setBackground(Color.WHITE);
                        ((JToggleButton) comp).setForeground(new Color(20, 184, 166));
                    }
                }
                // Set current button as selected
                button.setSelected(true);
                button.setBackground(new Color(20, 184, 166));
                button.setForeground(Color.WHITE);
            }
        });

        return button;
    }

    private JButton createManagerLogoutButton() {
        JButton button = new JButton("Logout");
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(239, 68, 68));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        button.addActionListener(e -> logout());

        return button;
    }

    private JPanel createPerformancePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Performance Overview - " + LocalDate.now());
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(title, BorderLayout.NORTH);

        // KPI Cards
        JPanel kpiPanel = createKPIPanel();
        panel.add(kpiPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createKPIPanel() {
        JPanel kpiPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        kpiPanel.setBackground(new Color(245, 247, 250));

        try {
            double monthlyRevenue = getMonthlyRevenue();
            double dailyRevenue = getTodayRevenue();
            int occupancyRate = getOccupancyRate();
            int staffProductivity = getStaffProductivity();
            double avgRoomRate = getAverageRoomRate();

            // Get trends from database (vs previous month)
            double monthlyTrend = getMonthlyRevenueTrend();
            double dailyTrend = getDailyRevenueTrend();
            int occupancyTrend = getOccupancyTrend();
            int productivityTrend = getProductivityTrend();
            double roomRateTrend = getRoomRateTrend();
            int satisfactionTrend = getSatisfactionTrend();

            kpiPanel.add(createKPICard("Monthly Revenue",
                    String.format("$%.2f", monthlyRevenue),
                    formatTrend(monthlyTrend),
                    new Color(34, 197, 94)));

            kpiPanel.add(createKPICard("Today's Revenue",
                    String.format("$%.2f", dailyRevenue),
                    formatTrend(dailyTrend),
                    new Color(59, 130, 246)));

            kpiPanel.add(createKPICard("Occupancy Rate",
                    occupancyRate + "%",
                    formatTrend(occupancyTrend),
                    new Color(245, 158, 11)));



            kpiPanel.add(createKPICard("Avg Room Rate",
                    String.format("$%.2f", avgRoomRate),
                    formatTrend(roomRateTrend),
                    new Color(139, 92, 246)));


        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load KPIs", ex);
            kpiPanel.add(createKPICard("Monthly Revenue", "$0.00", "No data", Color.GRAY));
            kpiPanel.add(createKPICard("Today's Revenue", "$0.00", "No data", Color.GRAY));
            kpiPanel.add(createKPICard("Occupancy Rate", "0%", "No data", Color.GRAY));
             kpiPanel.add(createKPICard("Avg Room Rate", "$0.00", "No data", Color.GRAY));
         }

        return kpiPanel;
    }

    private String formatTrend(double trend) {
        if (trend == 0) return "No change";
        String trendText = String.format("%.1f%%", Math.abs(trend));
        return trend > 0 ? "+" + trendText : "-" + trendText;
    }

    private String formatTrend(int trend) {
        if (trend == 0) return "No change";
        String trendText = Math.abs(trend) + "%";
        return trend > 0 ? "+" + trendText : "-" + trendText;
    }

    private JPanel createKPICard(String title, String value, String trend, Color color) {
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
        valueLabel.setForeground(color);

        JLabel trendLabel = new JLabel(trend);
        trendLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

         if (trend.startsWith("+")) {
            trendLabel.setForeground(new Color(34, 197, 94));
        } else if (trend.startsWith("-")) {
            trendLabel.setForeground(new Color(239, 68, 68));
        } else {
            trendLabel.setForeground(new Color(107, 114, 128));
        }

        JLabel descLabel = new JLabel("vs previous period");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(156, 163, 175));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(valueLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(trendLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(descLabel);

        card.add(leftPanel, BorderLayout.CENTER);

         String trendIcon = trend.startsWith("+") ? "▲" : trend.startsWith("-") ? "▼" : "●";
        JLabel iconLabel = new JLabel(trendIcon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));

        card.add(iconLabel, BorderLayout.EAST);

        return card;
    }

    private double getMonthlyRevenue() throws SQLException {
        String query = "SELECT COALESCE(SUM(amount), 0) as revenue FROM payments " +
                "WHERE MONTH(payment_date) = MONTH(CURDATE()) " +
                "AND YEAR(payment_date) = YEAR(CURDATE())";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getDouble("revenue") : 0;
        }
    }

    private double getMonthlyRevenueTrend() throws SQLException {
        String query = "SELECT " +
                "(SELECT COALESCE(SUM(amount), 0) FROM payments " +
                " WHERE MONTH(payment_date) = MONTH(CURDATE()) " +
                " AND YEAR(payment_date) = YEAR(CURDATE())) as current_month, " +
                "(SELECT COALESCE(SUM(amount), 0) FROM payments " +
                " WHERE MONTH(payment_date) = MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)) " +
                " AND YEAR(payment_date) = YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))) as previous_month";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                double current = rs.getDouble("current_month");
                double previous = rs.getDouble("previous_month");
                return previous > 0 ? ((current - previous) / previous) * 100 : 0;
            }
        }
        return 0;
    }

    private double getTodayRevenue() throws SQLException {
        String query = "SELECT COALESCE(SUM(amount), 0) as revenue FROM payments " +
                "WHERE DATE(payment_date) = CURDATE()";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getDouble("revenue") : 0;
        }
    }

    private double getDailyRevenueTrend() throws SQLException {
        String query = "SELECT " +
                "(SELECT COALESCE(SUM(amount), 0) FROM payments " +
                " WHERE DATE(payment_date) = CURDATE()) as today, " +
                "(SELECT COALESCE(SUM(amount), 0) FROM payments " +
                " WHERE DATE(payment_date) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)) as yesterday";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                double today = rs.getDouble("today");
                double yesterday = rs.getDouble("yesterday");
                return yesterday > 0 ? ((today - yesterday) / yesterday) * 100 : 0;
            }
        }
        return 0;
    }

    private int getOccupancyRate() throws SQLException {
        String query = "SELECT COUNT(*) as total, " +
                "SUM(CASE WHEN status = 'OCCUPIED' THEN 1 ELSE 0 END) as occupied " +
                "FROM rooms";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int total = rs.getInt("total");
                int occupied = rs.getInt("occupied");
                return total > 0 ? Math.round((occupied * 100f) / total) : 0;
            }
        }
        return 0;
    }

    private int getOccupancyTrend() throws SQLException {
        String query = "SELECT " +
                "(SELECT COUNT(*) FROM rooms WHERE status = 'OCCUPIED') as current_occupied, " +
                "(SELECT COUNT(*) FROM rooms) as total_rooms, " +
                "(SELECT COUNT(*) FROM rooms WHERE status = 'OCCUPIED' " +
                " AND DATE(updated_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)) as previous_occupied";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int current = rs.getInt("current_occupied");
                int total = rs.getInt("total_rooms");
                int previous = rs.getInt("previous_occupied");

                double currentRate = total > 0 ? (current * 100.0) / total : 0;
                double previousRate = total > 0 ? (previous * 100.0) / total : 0;

                return (int) Math.round(currentRate - previousRate);
            }
        }
        return 0;
    }

    private int getStaffProductivity() throws SQLException {
         String checkQuery = "SELECT COUNT(*) as task_count FROM tasks " +
                "WHERE DATE(completed_date) = CURDATE() " +
                "AND status = 'COMPLETED'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkQuery)) {
            if (rs.next()) {
                int completedTasks = rs.getInt("task_count");
                 return Math.min(completedTasks * 10, 100);
            }
        }
        return 0;
    }

    private int getProductivityTrend() throws SQLException {
        String query = "SELECT " +
                "(SELECT COUNT(*) FROM tasks WHERE DATE(completed_date) = CURDATE() AND status = 'COMPLETED') as today, " +
                "(SELECT COUNT(*) FROM tasks WHERE DATE(completed_date) = DATE_SUB(CURDATE(), INTERVAL 1 DAY) AND status = 'COMPLETED') as yesterday";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int today = rs.getInt("today");
                int yesterday = rs.getInt("yesterday");
                return yesterday > 0 ? (int) Math.round(((today - yesterday) * 100.0) / yesterday) : 0;
            }
        }
        return 0;
    }

    private double getAverageRoomRate() throws SQLException {
        String query = "SELECT AVG(price_per_night) as avg_rate FROM rooms " +
                "WHERE status = 'OCCUPIED'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getDouble("avg_rate") : 0;
        }
    }

    private double getRoomRateTrend() throws SQLException {
        String query = "SELECT " +
                "(SELECT AVG(price_per_night) FROM rooms WHERE status = 'OCCUPIED') as current_rate, " +
                "(SELECT AVG(price_per_night) FROM rooms WHERE status = 'OCCUPIED' " +
                " AND DATE(updated_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)) as previous_rate";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                double current = rs.getDouble("current_rate");
                double previous = rs.getDouble("previous_rate");
                return previous > 0 ? ((current - previous) / previous) * 100 : 0;
            }
        }
        return 0;
    }



    private int getSatisfactionTrend() throws SQLException {

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'reviews'")) {
            if (rs.next()) {
                String query = "SELECT " +
                        "(SELECT COALESCE(AVG(rating), 0) FROM reviews " +
                        " WHERE MONTH(review_date) = MONTH(CURDATE())) as current_month, " +
                        "(SELECT COALESCE(AVG(rating), 0) FROM reviews " +
                        " WHERE MONTH(review_date) = MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))) as previous_month";

                try (ResultSet rs2 = stmt.executeQuery(query)) {
                    if (rs2.next()) {
                        double current = rs2.getDouble("current_month");
                        double previous = rs2.getDouble("previous_month");
                        return previous > 0 ? (int) Math.round(((current - previous) / previous) * 100) : 0;
                    }
                }
            }
        }
        return 0;
    }

    //  ROOM STATUS PANEL
    private JPanel createRoomStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Room Status Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(title, BorderLayout.NORTH);

        try {
            JPanel statusPanel = new JPanel(new GridLayout(2, 2, 20, 20));

            Map<String, Integer> roomStatus = getRoomStatusCounts();

            statusPanel.add(createStatusCard("Available Rooms", roomStatus.getOrDefault("AVAILABLE", 0), new Color(34, 197, 94)));
            statusPanel.add(createStatusCard("Occupied Rooms", roomStatus.getOrDefault("OCCUPIED", 0), new Color(239, 68, 68)));
            statusPanel.add(createStatusCard("Reserved Rooms", roomStatus.getOrDefault("RESERVED", 0), new Color(245, 158, 11)));
            statusPanel.add(createStatusCard("Under Maintenance", roomStatus.getOrDefault("MAINTENANCE", 0), new Color(107, 114, 128)));

            panel.add(statusPanel, BorderLayout.CENTER);


            JButton refreshBtn = new JButton("Refresh Status");
            refreshBtn.addActionListener(e -> showRoomStatusPanel());
            panel.add(refreshBtn, BorderLayout.SOUTH);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load room status", ex);
            panel.add(new JLabel("Error loading room status"), BorderLayout.CENTER);
        }

        return panel;
    }

    private Map<String, Integer> getRoomStatusCounts() throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        String query = "SELECT status, COUNT(*) as count FROM rooms GROUP BY status";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                counts.put(rs.getString("status"), rs.getInt("count"));
            }
        }
        return counts;
    }

    private JPanel createStatusCard(String title, int count, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel countLabel = new JLabel(String.valueOf(count));
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        countLabel.setForeground(color);
        countLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(countLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);

        return card;
    }

    //  STAFF OVERVIEW PANEL
    private JPanel createStaffOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Staff Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(title, BorderLayout.NORTH);

        try {
            JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));

            Map<String, Integer> staffStats = getStaffStatistics();

            statsPanel.add(createStaffStatCard("Total Staff",
                    String.valueOf(staffStats.getOrDefault("TOTAL", 0)), new Color(59, 130, 246)));
            statsPanel.add(createStaffStatCard("Active Staff",
                    String.valueOf(staffStats.getOrDefault("ACTIVE", 0)), new Color(34, 197, 94)));
            statsPanel.add(createStaffStatCard("On Leave",
                    String.valueOf(staffStats.getOrDefault("ON_LEAVE", 0)), new Color(245, 158, 11)));
            statsPanel.add(createStaffStatCard("Inactive",
                    String.valueOf(staffStats.getOrDefault("INACTIVE", 0)), new Color(239, 68, 68)));

            panel.add(statsPanel, BorderLayout.NORTH);

            // Staff table
            JTable staffTable = createStaffTable();
            JScrollPane scrollPane = new JScrollPane(staffTable);
            panel.add(scrollPane, BorderLayout.CENTER);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load staff data", ex);
            panel.add(new JLabel("Error loading staff data"), BorderLayout.CENTER);
        }

        return panel;
    }

    private Map<String, Integer> getStaffStatistics() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String query = "SELECT status, COUNT(*) as count FROM staff GROUP BY status";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int total = 0;
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                stats.put(status, count);
                total += count;
            }
            stats.put("TOTAL", total);
        }
        return stats;
    }

    private JTable createStaffTable() throws SQLException {
        String[] columns = {"ID", "First Name", "Last Name", "Position", "Department", "Status"};
        List<Object[]> data = new ArrayList<>();

         String query = "SELECT staff_id, first_name, last_name, position, department, status FROM staff ORDER BY first_name, last_name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                data.add(new Object[]{
                        rs.getInt("staff_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("position"),
                        rs.getString("department"),
                        rs.getString("status")
                });
            }
        }

        Object[][] tableData = data.toArray(new Object[0][]);
        JTable table = new JTable(tableData, columns);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(30);

         table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 5) {
                    String status = value.toString();
                    if ("ACTIVE".equals(status)) {
                        c.setBackground(new Color(220, 255, 220));
                        c.setForeground(new Color(0, 128, 0));
                    } else if ("ON_LEAVE".equals(status)) {
                        c.setBackground(new Color(255, 240, 220));
                        c.setForeground(new Color(128, 80, 0));
                    } else {
                        c.setBackground(new Color(255, 220, 220));
                        c.setForeground(new Color(128, 0, 0));
                    }
                } else {
                    if (isSelected) {
                        c.setBackground(table.getSelectionBackground());
                        c.setForeground(table.getSelectionForeground());
                    } else {
                        c.setBackground(table.getBackground());
                        c.setForeground(table.getForeground());
                    }
                }

                return c;
            }
        });

        return table;
    }

    private JPanel createStaffStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);

        return card;
    }

    //  DAILY REPORTS PANEL
    private JPanel createDailyReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Daily Operations Report - " + LocalDate.now());
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(title, BorderLayout.NORTH);

        try {
            List<Object[]> dailyOps = getDailyOperations();
            JPanel reportPanel = new JPanel(new GridLayout(dailyOps.size(), 1, 10, 10));

            for (Object[] op : dailyOps) {
                reportPanel.add(createReportCard((String) op[0], (Integer) op[1]));
            }

            panel.add(reportPanel, BorderLayout.CENTER);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load daily report", ex);
            panel.add(new JLabel("Error loading daily report"), BorderLayout.CENTER);
        }

        return panel;
    }

    private List<Object[]> getDailyOperations() throws SQLException {
        List<Object[]> operations = new ArrayList<>();

        String[] queries = {
                "SELECT 'Check-ins' as type, COUNT(*) as count FROM bookings WHERE DATE(check_in_date) = CURDATE()",
                "SELECT 'Check-outs' as type, COUNT(*) as count FROM bookings WHERE DATE(check_out_date) = CURDATE()",
                "SELECT 'New Bookings' as type, COUNT(*) as count FROM bookings WHERE DATE(booking_date) = CURDATE()",
                "SELECT 'New Guests' as type, COUNT(*) as count FROM customers WHERE DATE(created_at) = CURDATE()"
        };

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String query : queries) {
                try (ResultSet rs = stmt.executeQuery(query)) {
                    if (rs.next()) {
                        operations.add(new Object[]{rs.getString("type"), rs.getInt("count")});
                    }
                }
            }
        }
        return operations;
    }

    private JPanel createReportCard(String operation, int count) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel countLabel = new JLabel(String.valueOf(count));
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        countLabel.setForeground(new Color(59, 130, 246));

        JLabel opLabel = new JLabel(operation);
        opLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        card.add(countLabel, BorderLayout.WEST);
        card.add(opLabel, BorderLayout.CENTER);

        return card;
    }

    // Panel switching methods
    private void showPerformancePanel() {
        switchRightPanel(createPerformancePanel());
    }

    private void showRoomStatusPanel() {
        switchRightPanel(createRoomStatusPanel());
    }

    private void showStaffOverviewPanel() {
        switchRightPanel(createStaffOverviewPanel());
    }

    private void showDailyReportsPanel() {
        switchRightPanel(createDailyReportsPanel());
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
        header.setBackground(new Color(20, 184, 166));
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JLabel title = new JLabel("Manager Dashboard - " + currentUser);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel dateLabel = new JLabel(LocalDate.now() + " | Hotel Management");
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(Color.WHITE);

        header.add(title, BorderLayout.WEST);
        header.add(dateLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(31, 41, 55));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        JLabel footerText = new JLabel("© 2026 Hotel Management System | Manager Panel");
        footerText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerText.setForeground(new Color(156, 163, 175));

        footer.add(footerText, BorderLayout.CENTER);
        return footer;
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }

    private void setupWindow() {
        try {
            ImageIcon icon = new ImageIcon("images/icon.png");
            setIconImage(icon.getImage());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Icon not found", ex);
        }
    }
}
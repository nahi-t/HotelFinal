package hotel.ui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.sql.SQLException ;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import hotel.database.DatabaseConnection;
import hotel.models.Staff;
import hotel.dao.StaffDAO;

public class StaffManagementPanel extends JPanel {
    private JTable staffTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton;
    private static final Logger LOGGER = Logger.getLogger(StaffManagementPanel.class.getName());

    public StaffManagementPanel() {
        initComponents();
        loadStaff();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // Header
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Center with table
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel title = new JLabel("👥 Staff Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(31, 41, 55));

        header.add(title, BorderLayout.WEST);
        return header;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(245, 247, 250));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Table model
        String[] columns = {"ID", "Name", "Position", "Department", "Phone", "Email", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        staffTable = new JTable(tableModel);
        staffTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        staffTable.setRowHeight(35);
        staffTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        staffTable.getTableHeader().setBackground(new Color(249, 250, 251));
        staffTable.setGridColor(new Color(229, 231, 235));
        staffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom renderer for status
        staffTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                if (column == 6) { // Status column
                    String status = value.toString();
                    if (status.equals("ACTIVE")) {
                        c.setBackground(new Color(220, 255, 220));
                        c.setForeground(new Color(0, 128, 0));
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

                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        staffTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = staffTable.getSelectedRow() != -1;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });

        JScrollPane scrollPane = new JScrollPane(staffTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        return centerPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        addButton = createStyledButton("➕ Add Staff", new Color(34, 197, 94));
        editButton = createStyledButton("✏️ Edit Staff", new Color(59, 130, 246));
        deleteButton = createStyledButton("🗑️ Delete Staff", new Color(239, 68, 68));
        refreshButton = createStyledButton("🔄 Refresh", new Color(107, 114, 128));

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        addButton.addActionListener(e -> showAddStaffDialog());
        editButton.addActionListener(e -> showEditStaffDialog());
        deleteButton.addActionListener(e -> deleteSelectedStaff());
        refreshButton.addActionListener(e -> loadStaff());

        buttonPanel.add(refreshButton);
        buttonPanel.add(Box.createHorizontalStrut(50));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        return buttonPanel;
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.black);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker()),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }
    private void loadStaff() {
        tableModel.setRowCount(0);

        try {
            List<Staff> staffList = StaffDAO.getAllStaff();

            for (Staff staff : staffList) {
                Object[] row = {
                        staff.getStaffId(),
                        staff.getFirstName() + " " + staff.getLastName(),
                        staff.getPosition(),
                        staff.getDepartment(),
                        staff.getPhone(),
                        staff.getEmail(),
                        staff.getStatus()
                };
                tableModel.addRow(row);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load staff", ex);
            JOptionPane.showMessageDialog(this,
                    "Error loading staff: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddStaffDialog() {
        StaffDialog dialog = new StaffDialog(null, "Add New Staff");
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Staff newStaff = dialog.getStaff();
            try {
                if (StaffDAO.addStaff(newStaff)) {
                    JOptionPane.showMessageDialog(this,
                            "Staff added successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadStaff();
                }
            } catch ( Exception ex) {
              LOGGER.log(Level.SEVERE, "Failed to add staff", ex);
                JOptionPane.showMessageDialog(this,
                       "Error adding staff: " + ex.getMessage(),
                       "Database Error",
                        JOptionPane.ERROR_MESSAGE);
           }
        }
    }

    private void showEditStaffDialog() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) return;

        int staffId = (int) tableModel.getValueAt(selectedRow, 0);

        try {
            Staff staff = StaffDAO.getStaffById(staffId);
            if (staff != null) {
                StaffDialog dialog = new StaffDialog(staff, "Edit Staff");
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    Staff updatedStaff = dialog.getStaff();
                    if (StaffDAO.updateStaff(updatedStaff)) {
                        JOptionPane.showMessageDialog(this,
                                "Staff updated successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadStaff();
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load staff details", ex);
        }
    }

    private void deleteSelectedStaff() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) return;

        int staffId = (int) tableModel.getValueAt(selectedRow, 0);
        String staffName = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete " + staffName + "?\n" +
                        "This action cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (StaffDAO.deleteStaff(staffId)) {
                    JOptionPane.showMessageDialog(this,
                            "Staff deleted successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadStaff();
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Failed to delete staff", ex);
                JOptionPane.showMessageDialog(this,
                        "Error deleting staff: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
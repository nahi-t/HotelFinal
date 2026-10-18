package hotel.ui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import hotel.database.DatabaseConnection;
import hotel.dao.RoomDAO;
import hotel.models.Room;

public class RoomManagementPanel extends JPanel {
    private JTable roomTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<String> typeFilterCombo;
    private JButton addButton, editButton, deleteButton, refreshButton;
    private static final Logger LOGGER = Logger.getLogger(RoomManagementPanel.class.getName());

    public RoomManagementPanel() {
        initComponents();
        loadRooms();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Create center panel with table
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Create button panel
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

        JLabel title = new JLabel("🏨 Room Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(31, 41, 55));

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterPanel.setBackground(Color.WHITE);

        JLabel filterLabel = new JLabel("Filter by:");
        filterLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        statusFilterCombo = new JComboBox<>(new String[]{"All Status", "AVAILABLE", "OCCUPIED", "RESERVED", "MAINTENANCE", "OUT_OF_ORDER"});
        statusFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusFilterCombo.addActionListener(e -> filterRooms());

        typeFilterCombo = new JComboBox<>(new String[]{"All Types", "STANDARD", "DELUXE", "SUITE", "EXECUTIVE"});
        typeFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        typeFilterCombo.addActionListener(e -> filterRooms());

        filterPanel.add(filterLabel);
        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusFilterCombo);
        filterPanel.add(new JLabel("Type:"));
        filterPanel.add(typeFilterCombo);

        header.add(title, BorderLayout.WEST);
        header.add(filterPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(245, 247, 250));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create table model
        String[] columns = {"Room No.", "Type", "Bed Type", "Floor", "Price/Night", "Status", "Max Guests", "View", "Amenities"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        roomTable = new JTable(tableModel);
        roomTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        roomTable.setRowHeight(35);
        roomTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        roomTable.getTableHeader().setBackground(new Color(249, 250, 251));
        roomTable.setGridColor(new Color(229, 231, 235));
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

         roomTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 5) {
                    String status = value.toString();
                    switch (status) {
                        case "AVAILABLE":
                            c.setBackground(new Color(220, 255, 220));
                            c.setForeground(new Color(0, 128, 0));
                            break;
                        case "OCCUPIED":
                            c.setBackground(new Color(255, 220, 220));
                            c.setForeground(new Color(128, 0, 0));
                            break;
                        case "RESERVED":
                            c.setBackground(new Color(255, 240, 220));
                            c.setForeground(new Color(128, 80, 0));
                            break;
                        case "MAINTENANCE":
                            c.setBackground(new Color(220, 220, 220));
                            c.setForeground(new Color(64, 64, 64));
                            break;
                        case "OUT_OF_ORDER":
                            c.setBackground(new Color(128, 0, 0));
                            c.setForeground(Color.WHITE);
                            break;
                        default:
                            if (isSelected) {
                                c.setBackground(table.getSelectionBackground());
                                c.setForeground(table.getSelectionForeground());
                            } else {
                                c.setBackground(table.getBackground());
                                c.setForeground(table.getForeground());
                            }
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

         roomTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = roomTable.getSelectedRow() != -1;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });

        JScrollPane scrollPane = new JScrollPane(roomTable);
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

        addButton = createStyledButton("➕ Add Room", new Color(34, 197, 94));
        editButton = createStyledButton("✏️ Edit Room", new Color(59, 130, 246));
        deleteButton = createStyledButton("🗑️ Delete Room", new Color(239, 68, 68));
        refreshButton = createStyledButton("🔄 Refresh", new Color(107, 114, 128));

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

         addButton.addActionListener(e -> showAddRoomDialog());
        editButton.addActionListener(e -> showEditRoomDialog());
        deleteButton.addActionListener(e -> deleteSelectedRoom());
        refreshButton.addActionListener(e -> loadRooms());

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

    private void loadRooms() {
        tableModel.setRowCount(0);

        try {
            // Use DAO to get rooms
            List<Room> rooms = RoomDAO.getAllRooms();

            for (Room room : rooms) {
                Object[] row = {
                        room.getRoomNumber(),
                        room.getRoomType(),
                        room.getBedType(),
                        room.getFloor(),
                        String.format("$%.2f", room.getPricePerNight()),
                        room.getStatus(),
                        room.getMaxOccupancy(),
                        room.getViewType(),
                        room.getAmenities()
                };
                tableModel.addRow(row);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load rooms", ex);
            JOptionPane.showMessageDialog(this,
                    "Error loading rooms: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterRooms() {
        String statusFilter = (String) statusFilterCombo.getSelectedItem();
        String typeFilter = (String) typeFilterCombo.getSelectedItem();

        try {
            List<Room> allRooms = RoomDAO.getAllRooms();
            tableModel.setRowCount(0);

            for (Room room : allRooms) {
                boolean statusMatch = statusFilter.equals("All Status") || room.getStatus().equals(statusFilter);
                boolean typeMatch = typeFilter.equals("All Types") || room.getRoomType().equals(typeFilter);

                if (statusMatch && typeMatch) {
                    Object[] row = {
                            room.getRoomNumber(),
                            room.getRoomType(),
                            room.getBedType(),
                            room.getFloor(),
                            String.format("$%.2f", room.getPricePerNight()),
                            room.getStatus(),
                            room.getMaxOccupancy(),
                            room.getViewType(),
                            room.getAmenities()
                    };
                    tableModel.addRow(row);
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to filter rooms", ex);
        }
    }

    private void showAddRoomDialog() {
        RoomDialog dialog = new RoomDialog(null, "Add New Room");
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Room newRoom = dialog.getRoom();
            try {
                if (RoomDAO.addRoom(newRoom)) {
                    JOptionPane.showMessageDialog(this,
                            "Room added successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadRooms();
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Failed to add room", ex);
                JOptionPane.showMessageDialog(this,
                        "Error adding room: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditRoomDialog() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) return;

        int roomNumber = (int) tableModel.getValueAt(selectedRow, 0);

        try {
            Room room = RoomDAO.getRoomByNumber(roomNumber);
            if (room != null) {
                RoomDialog dialog = new RoomDialog(room, "Edit Room");
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    Room updatedRoom = dialog.getRoom();
                    if (RoomDAO.updateRoom(updatedRoom)) {
                        JOptionPane.showMessageDialog(this,
                                "Room updated successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadRooms();
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load room details", ex);
        }
    }

    private void deleteSelectedRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) return;

        int roomNumber = (int) tableModel.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Room " + roomNumber + "?\n" +
                        "This action cannot be undone.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (RoomDAO.deleteRoom(roomNumber)) {
                    JOptionPane.showMessageDialog(this,
                            "Room deleted successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadRooms();
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Failed to delete room", ex);
                JOptionPane.showMessageDialog(this,
                        "Error deleting room: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
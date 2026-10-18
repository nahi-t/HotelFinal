// file: RoomDialog.java
package hotel.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

import hotel.models.Room;

public class RoomDialog extends JDialog {
    private JTextField roomNumberField;
    private JComboBox<String> roomTypeCombo;
    private JComboBox<String> bedTypeCombo;
    private JTextField floorField;
    private JTextField priceField;
    private JTextField maxOccupancyField;
    private JComboBox<String> viewTypeCombo;
    private JComboBox<String> statusCombo;
    private JCheckBox needsCleaningCheck;
    private JCheckBox needsMaintenanceCheck;
    private JTextArea amenitiesArea;

    private Room room;
    private boolean confirmed = false;

    public RoomDialog(Room room, String title) {
        this.room = room;
        initComponents(title);
    }

    private void initComponents(String title) {
        setTitle(title);
        setModal(true);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Room Number
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Room Number:"), gbc);

        gbc.gridx = 1;
        roomNumberField = new JTextField(20);
        if (room != null) {
            roomNumberField.setText(String.valueOf(room.getRoomNumber()));
            roomNumberField.setEditable(false);
        }
        formPanel.add(roomNumberField, gbc);

        // Room Type
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Room Type:"), gbc);

        gbc.gridx = 1;
        roomTypeCombo = new JComboBox<>(new String[]{"STANDARD", "DELUXE", "SUITE", "EXECUTIVE"});
        if (room != null) roomTypeCombo.setSelectedItem(room.getRoomType());
        formPanel.add(roomTypeCombo, gbc);

        // Bed Type
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Bed Type:"), gbc);

        gbc.gridx = 1;
        bedTypeCombo = new JComboBox<>(new String[]{"SINGLE", "DOUBLE", "TWIN", "KING"});
        if (room != null) bedTypeCombo.setSelectedItem(room.getBedType());
        formPanel.add(bedTypeCombo, gbc);

        // Floor
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Floor:"), gbc);

        gbc.gridx = 1;
        floorField = new JTextField(20);
        if (room != null) floorField.setText(String.valueOf(room.getFloor()));
        formPanel.add(floorField, gbc);

        // Price per Night
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Price per Night:"), gbc);

        gbc.gridx = 1;
        priceField = new JTextField(20);
        if (room != null) priceField.setText(String.valueOf(room.getPricePerNight()));
        formPanel.add(priceField, gbc);

        // Max Occupancy
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Max Occupancy:"), gbc);

        gbc.gridx = 1;
        maxOccupancyField = new JTextField(20);
        if (room != null) maxOccupancyField.setText(String.valueOf(room.getMaxOccupancy()));
        formPanel.add(maxOccupancyField, gbc);

        // View Type
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(new JLabel("View Type:"), gbc);

        gbc.gridx = 1;
        viewTypeCombo = new JComboBox<>(new String[]{"SEA", "GARDEN", "CITY", "POOL", "MOUNTAIN"});
        if (room != null) viewTypeCombo.setSelectedItem(room.getViewType());
        formPanel.add(viewTypeCombo, gbc);

        // Status
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(new JLabel("Status:"), gbc);

        gbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"AVAILABLE", "OCCUPIED", "RESERVED", "MAINTENANCE", "OUT_OF_ORDER"});
        if (room != null) statusCombo.setSelectedItem(room.getStatus());
        formPanel.add(statusCombo, gbc);

        // Checkboxes
        gbc.gridx = 0;
        gbc.gridy = 8;
        formPanel.add(new JLabel("Flags:"), gbc);

        gbc.gridx = 1;
        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        needsCleaningCheck = new JCheckBox("Needs Cleaning");
        needsMaintenanceCheck = new JCheckBox("Needs Maintenance");

        if (room != null) {
            needsCleaningCheck.setSelected(room.isNeedsCleaning());
            needsMaintenanceCheck.setSelected(room.isNeedsMaintenance());
        }

        checkPanel.add(needsCleaningCheck);
        checkPanel.add(needsMaintenanceCheck);
        formPanel.add(checkPanel, gbc);

        // Amenities
        gbc.gridx = 0;
        gbc.gridy = 9;
        formPanel.add(new JLabel("Amenities (comma-separated):"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        amenitiesArea = new JTextArea(3, 20);
        amenitiesArea.setLineWrap(true);
        amenitiesArea.setWrapStyleWord(true);
        if (room != null && room.getAmenities() != null) {
            amenitiesArea.setText(room.getAmenities());
        } else {
            amenitiesArea.setText("TV, WiFi, AC, Mini-bar");
        }
        formPanel.add(new JScrollPane(amenitiesArea), gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));

        JButton saveButton = new JButton("💾 Save");
        saveButton.setBackground(new Color(34, 197, 94));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.addActionListener(e -> saveRoom());

        JButton cancelButton = new JButton("❌ Cancel");
        cancelButton.setBackground(new Color(239, 68, 68));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void saveRoom() {
        try {
            // Validate inputs
            int roomNumber = Integer.parseInt(roomNumberField.getText().trim());
            String roomType = (String) roomTypeCombo.getSelectedItem();
            String bedType = (String) bedTypeCombo.getSelectedItem();
            int floor = Integer.parseInt(floorField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());
            int maxOccupancy = Integer.parseInt(maxOccupancyField.getText().trim());
            String viewType = (String) viewTypeCombo.getSelectedItem();
            String status = (String) statusCombo.getSelectedItem();
            boolean needsCleaning = needsCleaningCheck.isSelected();
            boolean needsMaintenance = needsMaintenanceCheck.isSelected();
            String amenities = amenitiesArea.getText().trim();

            if (roomNumber <= 0 || floor <= 0 || price <= 0 || maxOccupancy <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid positive numbers for room number, floor, price, and max occupancy.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create or update room object
            if (room == null) {
                room = new Room(roomNumber, roomType, bedType, floor, price, maxOccupancy, viewType);
            } else {
                room.setRoomType(roomType);
                room.setBedType(bedType);
                room.setFloor(floor);
                room.setPricePerNight(price);
                room.setMaxOccupancy(maxOccupancy);
                room.setViewType(viewType);
            }

            room.setStatus(status);
            room.setNeedsCleaning(needsCleaning);
            room.setNeedsMaintenance(needsMaintenance);
            room.setAmenities(amenities);

            confirmed = true;
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numbers for room number, floor, price, and max occupancy.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public Room getRoom() {
        return room;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
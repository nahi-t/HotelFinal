package hotel.ui;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import hotel.database.DatabaseConnection;
import hotel.dao.*;
import hotel.models.*;

public class ReceptionDashboard extends JFrame {
    private final String currentUser;
    private static final Logger LOGGER = Logger.getLogger(ReceptionDashboard.class.getName());
    private JPanel leftPanel;
    private JPanel rightPanel;
    private javax.swing.Timer statsTimer;
    private JLabel statsLabel;
    private JTable guestTable;
    private DefaultTableModel guestTableModel;
    private JComboBox<String> roomComboBox;

    public ReceptionDashboard(String username) {
        this.currentUser = username;
        initComponents();
        setupWindow();
        startStatsUpdateTimer();
    }

    private void initComponents() {
        setTitle("Hotel Management System - Reception Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 850);
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

        leftPanel = createReceptionNavPanel();
        leftPanel.setPreferredSize(new Dimension(220, 0));

        rightPanel = createTodayOverviewPanel();
        rightPanel.setPreferredSize(new Dimension(1180, 0));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(220);
        splitPane.setDividerSize(2);
        splitPane.setEnabled(false);

        mainLayout.add(splitPane, BorderLayout.CENTER);

        return mainLayout;
    }

    private JPanel createReceptionNavPanel() {
        JPanel navPanel = new JPanel();
        navPanel.setBackground(new Color(31, 41, 55));
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel userLabel = new JLabel(currentUser);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        userLabel.setForeground(Color.WHITE);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        userLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        navPanel.add(userLabel);

        JLabel navTitle = new JLabel("RECEPTION DESK");
        navTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        navTitle.setForeground(new Color(255, 255, 255));
        navTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        navTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        navPanel.add(navTitle);

        String[][] quickActions = {
                {"Check-in", "checkin"},
                {"Check-out", "checkout"},
                {"Guest Search", "search"},
                {"Generate Bill", "bill"},
                {"Daily Report", "report"}
        };

        for (String[] action : quickActions) {
            JButton actionBtn = createModernActionButton(action[0], action[1]);
            actionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            actionBtn.setMaximumSize(new Dimension(200, 45));

            switch (action[1]) {
                case "checkin" -> actionBtn.addActionListener(e -> showQuickCheckinPanel());
                case "checkout" -> actionBtn.addActionListener(e -> showQuickCheckoutPanel());
                case "search" -> actionBtn.addActionListener(e -> showGuestSearchPanel());
                case "bill" -> actionBtn.addActionListener(e -> showBillGenerationPanel());
                case "report" -> actionBtn.addActionListener(e -> showDailyReportPanel());
            }

            navPanel.add(actionBtn);
            navPanel.add(Box.createVerticalStrut(8));
        }

        navPanel.add(Box.createVerticalGlue());

        JButton logoutBtn = createReceptionLogoutButton();
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(200, 40));
        navPanel.add(logoutBtn);

        return navPanel;
    }

    private JButton createModernActionButton(String text, String actionType) {
        JButton button = new JButton("<html><center><small>" + text + "</small></center></html>");
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(new Color(55, 65, 81));
        button.setForeground(Color.black);
        button.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);

        Color hoverColor = new Color(75, 85, 99);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent evt) {
                button.setBackground(new Color(55, 65, 81));
            }
        });

        return button;
    }

    private void showQuickCheckinPanel() {
        switchRightPanel(createQuickCheckinPanel());
    }

    private JPanel createQuickCheckinPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel headerPanel = createCheckinHeaderPanel();
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(Color.WHITE);
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField nameField = createStyledTextField();
        JTextField phoneField = createStyledTextField();
        JTextField emailField = createStyledTextField();
        JTextField idNumberField = createStyledTextField();
        JComboBox<String> idTypeCombo = new JComboBox<>(new String[]{"Passport", "ID Card", "Driver License", "Other"});
        styleComboBox(idTypeCombo);
        JTextField addressField = createStyledTextField();
        JTextField countryField = createStyledTextField();
        countryField.setText("Ethiopia");

        JSpinner nightsSpinner = createNightsSpinner();

        JPanel guestInfoCard = createGuestInfoCard(nameField, phoneField, emailField, idNumberField,
                idTypeCombo, addressField, countryField);

        JPanel roomSelectionCard = createRoomSelectionCard(nightsSpinner);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        formCard.add(guestInfoCard, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 0.5;
        formCard.add(roomSelectionCard, gbc);

        JPanel buttonPanel = createCheckinButtonPanel(nameField, phoneField, emailField, idTypeCombo,
                idNumberField, addressField, countryField, nightsSpinner);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        formCard.add(buttonPanel, gbc);

        loadAvailableRooms();

        roomComboBox.addActionListener(e -> {
            if (roomComboBox.getSelectedIndex() >= 0 && roomComboBox.getItemCount() > 0) {
                String selectedItem = (String) roomComboBox.getSelectedItem();
                if (selectedItem != null && !selectedItem.equals("Select Room") &&
                        !selectedItem.equals("No rooms available") &&
                        !selectedItem.equals("Error loading rooms")) {
                    JTextArea detailsArea = (JTextArea) findComponentByName(roomSelectionCard, "roomDetailsArea");
                    if (detailsArea != null) {
                        updateRoomDetails(roomComboBox, detailsArea);
                    }
                }
            }
        });

        if (roomComboBox != null && roomComboBox.getItemCount() > 0) {
            roomComboBox.setSelectedIndex(0);
            JTextArea detailsArea = (JTextArea) findComponentByName(roomSelectionCard, "roomDetailsArea");
            if (detailsArea != null) {
                updateRoomDetails(roomComboBox, detailsArea);
            }
        }

        JScrollPane scrollPane = new JScrollPane(formCard);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private Component findComponentByName(Container container, String name) {
        for (Component comp : container.getComponents()) {
            if (name.equals(comp.getName())) {
                return comp;
            }
            if (comp instanceof Container) {
                Component found = findComponentByName((Container) comp, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private JPanel createCheckinHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("Guest Check-in");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(30, 41, 59));

        JLabel subtitle = new JLabel("Complete guest information and select room");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 116, 139));

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        return headerPanel;
    }

    private JSpinner createNightsSpinner() {
        JSpinner nightsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
        JComponent spinnerEditor = nightsSpinner.getEditor();
        if (spinnerEditor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) spinnerEditor).getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 14));
            ((JSpinner.DefaultEditor) spinnerEditor).getTextField().setBackground(Color.WHITE);
            ((JSpinner.DefaultEditor) spinnerEditor).getTextField().setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
        }
        nightsSpinner.setPreferredSize(new Dimension(100, 40));
        return nightsSpinner;
    }

    private JPanel createGuestInfoCard(JTextField nameField, JTextField phoneField, JTextField emailField,
                                       JTextField idNumberField, JComboBox<String> idTypeCombo,
                                       JTextField addressField, JTextField countryField) {
        JPanel guestInfoCard = new JPanel(new GridBagLayout());
        guestInfoCard.setBackground(new Color(248, 250, 252));
        guestInfoCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(59, 130, 225), 1),
                "Guest Information",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(59, 130, 246)
        ));

        GridBagConstraints guestGbc = new GridBagConstraints();
        guestGbc.insets = new Insets(8, 10, 8, 10);
        guestGbc.anchor = GridBagConstraints.WEST;
        guestGbc.fill = GridBagConstraints.HORIZONTAL;

        int guestRow = 0;
        addGuestField(guestInfoCard, guestGbc, "Full Name *:", nameField, guestRow++);
        addGuestField(guestInfoCard, guestGbc, "Email:", emailField, guestRow++);

        guestGbc.gridx = 0;
        guestGbc.gridy = guestRow;
        JLabel idTypeLabel = new JLabel("ID Type:");
        idTypeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        guestInfoCard.add(idTypeLabel, guestGbc);

        guestGbc.gridx = 1;
        guestGbc.gridwidth = 1;
        idTypeCombo.setPreferredSize(new Dimension(200, 40));
        guestInfoCard.add(idTypeCombo, guestGbc);

        guestRow++;
        addGuestField(guestInfoCard, guestGbc, "ID Number:", idNumberField, guestRow++);
        addGuestField(guestInfoCard, guestGbc, "Address:", addressField, guestRow++);
        addGuestField(guestInfoCard, guestGbc, "Country:", countryField, guestRow++);
        addGuestField(guestInfoCard, guestGbc, "Phone *:", phoneField, guestRow++);

        return guestInfoCard;
    }

    private JPanel createRoomSelectionCard(JSpinner nightsSpinner) {
        JPanel roomSelectionCard = new JPanel(new GridBagLayout());
        roomSelectionCard.setBackground(new Color(248, 250, 252));
        roomSelectionCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                "Room Selection",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(59, 130, 246)
        ));

        GridBagConstraints roomGbc = new GridBagConstraints();
        roomGbc.insets = new Insets(10, 10, 10, 10);
        roomGbc.anchor = GridBagConstraints.WEST;
        roomGbc.fill = GridBagConstraints.HORIZONTAL;

        int roomRow = 0;

        roomGbc.gridx = 0;
        roomGbc.gridy = roomRow;
        JLabel roomLabel = new JLabel("Select Room *:");
        roomLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        roomSelectionCard.add(roomLabel, roomGbc);

        roomGbc.gridx = 1;
        roomComboBox = new JComboBox<>();
        styleComboBox(roomComboBox);
        roomComboBox.setPreferredSize(new Dimension(250, 40));
        roomSelectionCard.add(roomComboBox, roomGbc);

        roomRow++;

        roomGbc.gridx = 0;
        roomGbc.gridy = roomRow;
        JLabel nightsLabel = new JLabel("Number of Nights *:");
        nightsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        roomSelectionCard.add(nightsLabel, roomGbc);

        roomGbc.gridx = 1;
        roomSelectionCard.add(nightsSpinner, roomGbc);

        roomRow++;

        roomGbc.gridx = 0;
        roomGbc.gridy = roomRow;
        roomGbc.gridwidth = 2;
        roomGbc.fill = GridBagConstraints.BOTH;
        roomGbc.weighty = 1.0;

        JTextArea roomDetailsArea = new JTextArea(8, 30);
        roomDetailsArea.setName("roomDetailsArea");
        roomDetailsArea.setEditable(false);
        roomDetailsArea.setLineWrap(true);
        roomDetailsArea.setWrapStyleWord(true);
        roomDetailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        roomDetailsArea.setBackground(new Color(248, 250, 252));
        roomDetailsArea.setForeground(new Color(30, 41, 59));
        roomDetailsArea.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                "Selected Room Details",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(59, 130, 246)
        ));

        roomDetailsArea.setText("Room 101 - STANDARD\n" +
                "Bed Type: DOUBLE\n" +
                "Price: $100.00 per night\n" +
                "Max Occupancy: 2\n" +
                "Amenities: TV, WiFi, AC, Mini-bar");

        JScrollPane roomDetailsScroll = new JScrollPane(roomDetailsArea);
        roomDetailsScroll.setBorder(BorderFactory.createEmptyBorder());
        roomDetailsScroll.setPreferredSize(new Dimension(300, 200));
        roomSelectionCard.add(roomDetailsScroll, roomGbc);

        return roomSelectionCard;
    }

    private JPanel createCheckinButtonPanel(JTextField nameField, JTextField phoneField,
                                            JTextField emailField, JComboBox<String> idTypeCombo,
                                            JTextField idNumberField, JTextField addressField,
                                            JTextField countryField, JSpinner nightsSpinner) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        JButton checkinBtn = new JButton("Complete Check-in");
        checkinBtn.setBackground(new Color(34, 197, 94));
        checkinBtn.setForeground(Color.WHITE);
        checkinBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        checkinBtn.setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));
        checkinBtn.setFocusPainted(false);
        checkinBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        checkinBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                checkinBtn.setBackground(new Color(22, 163, 74));
            }
            public void mouseExited(MouseEvent evt) {
                checkinBtn.setBackground(new Color(34, 197, 94));
            }
        });

        checkinBtn.addActionListener(e -> {
            if (processGuestCheckin(nameField, phoneField, emailField, idTypeCombo,
                    idNumberField, addressField, countryField,
                    roomComboBox, nightsSpinner)) {
                nameField.setText("");
                phoneField.setText("");
                emailField.setText("");
                idNumberField.setText("");
                addressField.setText("");
                countryField.setText("Ethiopia");
                idTypeCombo.setSelectedIndex(0);
                nightsSpinner.setValue(1);

                loadAvailableRooms();
                if (roomComboBox != null && roomComboBox.getItemCount() > 0) {
                    roomComboBox.setSelectedIndex(0);
                    JTextArea detailsArea = (JTextArea) findComponentByName(rightPanel, "roomDetailsArea");
                    if (detailsArea != null) {
                        updateRoomDetails(roomComboBox, detailsArea);
                    }
                }
            }
        });

        buttonPanel.add(checkinBtn);
        return buttonPanel;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField(25);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setBackground(new Color(255, 255, 255));
        return field;
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
                return this;
            }
        });
    }

    private void addGuestField(JPanel panel, GridBagConstraints gbc,
                               String labelText, JComponent field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 3;
        panel.add(field, gbc);
    }

    private void updateRoomDetails(JComboBox<String> roomCombo, JTextArea detailsArea) {
        String selected = (String) roomCombo.getSelectedItem();

        if (selected == null || selected.equals("Select Room") ||
                selected.equals("No rooms available") || selected.equals("Error loading rooms")) {
            detailsArea.setText("");
            return;
        }

        try {
            String[] parts = selected.split(" ");
            if (parts.length >= 2) {
                String roomNumberStr = parts[1];
                int roomNumber = Integer.parseInt(roomNumberStr);

                Room room = RoomDAO.getRoomByNumber(roomNumber);
                if (room != null) {
                    String details = String.format(
                            "Room %d - %s\n" +
                                    "Bed Type: %s\n" +
                                    "Price: $%.2f per night\n" +
                                    "Max Occupancy: %d\n" +
                                    "Amenities: %s",
                            room.getRoomNumber(),
                            room.getRoomType(),
                            room.getBedType(),
                            room.getPricePerNight(),
                            room.getMaxOccupancy(),
                            room.getAmenities()
                    );
                    detailsArea.setText(details);
                } else {
                    detailsArea.setText("Room details not found for Room " + roomNumber);
                }
            } else {
                detailsArea.setText("Invalid room selection format");
            }
        } catch (NumberFormatException e) {
            detailsArea.setText("Error: Invalid room number format in: " + selected);
        } catch (Exception e) {
            detailsArea.setText("Error loading room details");
        }
    }

    private boolean processGuestCheckin(JTextField nameField, JTextField phoneField,
                                        JTextField emailField, JComboBox<String> idTypeCombo,
                                        JTextField idNumberField, JTextField addressField,
                                        JTextField countryField, JComboBox<String> roomCombo,
                                        JSpinner nightsSpinner) {

        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full name is required", "Error", JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return false;
        }

        if (phoneField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Phone number is required", "Error", JOptionPane.ERROR_MESSAGE);
            phoneField.requestFocus();
            return false;
        }

        String selectedRoom = (String) roomCombo.getSelectedItem();
        if (selectedRoom == null || selectedRoom.equals("Select Room") ||
                selectedRoom.equals("No rooms available") || selectedRoom.equals("Error loading rooms")) {
            JOptionPane.showMessageDialog(this, "Please select an available room", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            String phone = phoneField.getText().trim();
            String idNumber = idNumberField.getText().trim();

            Customer existingCustomerByPhone = CustomerDAO.getCustomerByPhone(phone);
            if (existingCustomerByPhone != null) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "A customer with phone number " + phone + " already exists:\n" +
                                "Name: " + existingCustomerByPhone.getName() + "\n" +
                                "Email: " + existingCustomerByPhone.getEmail() + "\n\n" +
                                "Do you want to use this existing customer profile?",
                        "Existing Customer Found",
                        JOptionPane.YES_NO_CANCEL_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    return processCheckinWithExistingCustomer(existingCustomerByPhone, roomCombo, nightsSpinner);
                } else if (choice == JOptionPane.CANCEL_OPTION) {
                    return false;
                }
            }

            if (!idNumber.isEmpty()) {
                Customer existingCustomerById = CustomerDAO.getCustomerByIdNumber(idNumber);
                if (existingCustomerById != null) {
                    int choice = JOptionPane.showConfirmDialog(this,
                            "A customer with ID number " + idNumber + " already exists:\n" +
                                    "Name: " + existingCustomerById.getName() + "\n" +
                                    "Phone: " + existingCustomerById.getPhone() + "\n\n" +
                                    "Do you want to use this existing customer profile?",
                            "Existing Customer Found",
                            JOptionPane.YES_NO_CANCEL_OPTION);

                    if (choice == JOptionPane.YES_OPTION) {
                        return processCheckinWithExistingCustomer(existingCustomerById, roomCombo, nightsSpinner);
                    } else if (choice == JOptionPane.CANCEL_OPTION) {
                        return false;
                    }
                }
            }

            Customer customer = new Customer();
            customer.setName(nameField.getText().trim());
            customer.setEmail(emailField.getText().trim());
            customer.setPhone(phoneField.getText().trim());
            customer.setIdProof((String) idTypeCombo.getSelectedItem());
            customer.setIdNumber(idNumberField.getText().trim());
            customer.setAddress(addressField.getText().trim());
            customer.setCountry(countryField.getText().isEmpty() ? "Ethiopia" : countryField.getText().trim());
            customer.setRegistrationDate(LocalDate.now());

            if (!CustomerDAO.addCustomer(customer)) {
                throw new Exception("Failed to save customer to database");
            }

            String roomStr = selectedRoom.split(" ")[1];
            int roomNumber = Integer.parseInt(roomStr);
            Room room = RoomDAO.getRoomByNumber(roomNumber);
            if (room == null) {
                throw new Exception("Selected room not found in database");
            }

            int nights = (int) nightsSpinner.getValue();
            double totalAmount = room.getPricePerNight() * nights;
            LocalDate checkInDate = LocalDate.now();
            LocalDate checkOutDate = checkInDate.plusDays((int) nightsSpinner.getValue());

            Booking booking = new Booking();
            booking.setCustomerId(customer.getCustomerId());
            booking.setRoomId(roomNumber);
            booking.setCheckInDate(checkInDate);
            booking.setCheckOutDate(checkOutDate);
            booking.setBookingStatus("CHECKED_IN");
            booking.setPaymentStatus("PENDING");
            booking.setBookingDate(LocalDate.now());
            booking.setCreatedAt(LocalDateTime.now());
            booking.setNumberOfGuests(1);
            booking.setTotalAmount(totalAmount);

            if (!BookingDAO.addBooking(booking)) {
                throw new Exception("Failed to save booking");
            }

            if (!RoomDAO.updateRoomStatus(roomNumber, "OCCUPIED")) {
                throw new Exception("Failed to update room status");
            }

            JOptionPane.showMessageDialog(this,
                    String.format("Check-in Successful!\n\nGuest Information:\n• Name: %s\n• Phone: %s\n• Email: %s\n• Guest ID: %d\n\nRoom Assignment:\n• Room: %d (%s)\n• Check-in: %s\n• Check-out: %s\n• Nights: %d\n• Total Amount: $%.2f\n\nBooking ID: %d",
                            customer.getName(), customer.getPhone(), customer.getEmail(), customer.getCustomerId(),
                            roomNumber, room.getRoomType(), checkInDate, checkOutDate,
                            (int) nightsSpinner.getValue(), totalAmount, booking.getBookingId()),
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            return true;

        } catch (NumberFormatException ex) {
            LOGGER.log(Level.SEVERE, "Invalid room number format", ex);
            JOptionPane.showMessageDialog(this, "Invalid room selection: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Check-in failed", ex);
            JOptionPane.showMessageDialog(this, "Check-in failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void loadAllGuests() {
        try {
            List<Customer> customers = CustomerDAO.getAllCustomers();
            updateGuestTableWithRoomInfo(customers);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load guests", ex);
            JOptionPane.showMessageDialog(this,
                    "Error loading guests: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showQuickCheckoutPanel() {
        switchRightPanel(createQuickCheckoutPanel());
    }

    private boolean processCheckinWithExistingCustomer(Customer existingCustomer,
                                                       JComboBox<String> roomCombo,
                                                       JSpinner nightsSpinner) {
        try {
            boolean isAlreadyCheckedIn = BookingDAO.isCustomerCheckedIn(existingCustomer.getCustomerId());
            if (isAlreadyCheckedIn) {
                JOptionPane.showMessageDialog(this,
                        "Customer " + existingCustomer.getName() + " is already checked in!\n" +
                                "Please check out the existing booking first.",
                        "Customer Already Checked In",
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }

            return processRoomAssignment(existingCustomer, roomCombo, nightsSpinner);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Check-in with existing customer failed", ex);
            JOptionPane.showMessageDialog(this, "Check-in failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean processRoomAssignment(Customer customer, JComboBox<String> roomCombo, JSpinner nightsSpinner) {
        try {
            String selectedRoom = (String) roomCombo.getSelectedItem();
            String roomStr = selectedRoom.split(" ")[1];
            int roomNumber = Integer.parseInt(roomStr);
            Room room = RoomDAO.getRoomByNumber(roomNumber);

            if (room == null) {
                throw new Exception("Selected room not found in database");
            }

            int nights = (int) nightsSpinner.getValue();
            double totalAmount = room.getPricePerNight() * nights;

            LocalDate checkInDate = LocalDate.now();
            LocalDate checkOutDate = checkInDate.plusDays(nights);

            Booking booking = new Booking();
            booking.setCustomerId(customer.getCustomerId());
            booking.setRoomId(roomNumber);
            booking.setCheckInDate(checkInDate);
            booking.setCheckOutDate(checkOutDate);
            booking.setBookingStatus("CHECKED_IN");
            booking.setPaymentStatus("PENDING");
            booking.setBookingDate(LocalDate.now());
            booking.setCreatedAt(LocalDateTime.now());
            booking.setNumberOfGuests(1);
            booking.setTotalAmount(totalAmount);

            if (!BookingDAO.addBooking(booking)) {
                throw new Exception("Failed to save booking");
            }

            if (!RoomDAO.updateRoomStatus(roomNumber, "OCCUPIED")) {
                throw new Exception("Failed to update room status");
            }

            String message = String.format("Check-in Successful!\n\nGuest Information:\n• Name: %s\n• Phone: %s\n• Email: %s\n• Guest ID: %d\n\nRoom Assignment:\n• Room: %d (%s)\n• Check-in: %s\n• Check-out: %s\n• Nights: %d\n• Total Amount: $%.2f\n\nBooking ID: %d",
                    customer.getName(),
                    customer.getPhone(),
                    customer.getEmail(),
                    customer.getCustomerId(),
                    roomNumber,
                    room.getRoomType(),
                    checkInDate,
                    checkOutDate,
                    nights,
                    totalAmount,
                    booking.getBookingId()
            );

            JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);

            return true;

        } catch (Exception ex) {
            throw new RuntimeException("Room assignment failed: " + ex.getMessage(), ex);
        }
    }

    private JPanel createQuickCheckoutPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Guest Check-out");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);

        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search Guest");
        searchBtn.setBackground(new Color(59, 130, 246));
        searchBtn.setForeground(Color.WHITE);

        searchPanel.add(new JLabel("Search by Name/Room:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        String[] columns = {"Booking ID", "Guest Name", "Room", "Check-in", "Check-out", "Total Amount", "Action"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };

        JTable checkoutTable = new JTable(model);
        checkoutTable.setRowHeight(30);

        TableColumn actionColumn = checkoutTable.getColumnModel().getColumn(6);
        actionColumn.setCellRenderer(new ButtonRenderer("Check-out"));
        actionColumn.setCellEditor(new ButtonEditor(new JCheckBox(), checkoutTable, model, this));

        JScrollPane scrollPane = new JScrollPane(checkoutTable);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadCheckoutGuests(model));
        buttonPanel.add(refreshBtn);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(mainPanel, BorderLayout.CENTER);

        loadCheckoutGuests(model);

        searchBtn.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            if (searchText.isEmpty()) {
                loadCheckoutGuests(model);
            } else {
                searchCheckoutGuests(searchText, model);
            }
        });

        return panel;
    }

    private void loadCheckoutGuests(DefaultTableModel model) {
        model.setRowCount(0);

        try {
            List<Booking> activeBookings = BookingDAO.getActiveBookings();

            for (Booking booking : activeBookings) {
                Customer customer = CustomerDAO.getCustomerById(booking.getCustomerId());
                if (customer != null) {
                    model.addRow(new Object[]{
                            booking.getBookingId(),
                            customer.getName(),
                            booking.getRoomId(),
                            booking.getCheckInDate(),
                            booking.getCheckOutDate(),
                            String.format("$%.2f", booking.getTotalAmount()),
                            "Check-out"
                    });
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to load checkout guests", ex);
            JOptionPane.showMessageDialog(this, "Error loading guests: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchCheckoutGuests(String searchText, DefaultTableModel model) {
        model.setRowCount(0);

        try {
            List<Booking> activeBookings = BookingDAO.getActiveBookings();

            for (Booking booking : activeBookings) {
                Customer customer = CustomerDAO.getCustomerById(booking.getCustomerId());
                if (customer != null) {
                    String guestName = customer.getName().toLowerCase();
                    String roomNumber = String.valueOf(booking.getRoomId());

                    if (guestName.contains(searchText.toLowerCase()) ||
                            roomNumber.contains(searchText)) {
                        model.addRow(new Object[]{
                                booking.getBookingId(),
                                customer.getName(),
                                booking.getRoomId(),
                                booking.getCheckInDate(),
                                booking.getCheckOutDate(),
                                String.format("$%.2f", booking.getTotalAmount()),
                                "Check-out"
                        });
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to search guests", ex);
        }
    }

    public void processCheckout(int bookingId, int row, DefaultTableModel model) {
        try {
            Booking booking = BookingDAO.getBookingById(bookingId);
            if (booking == null) {
                throw new Exception("Booking not found");
            }

            if (booking.getPaymentStatus().equals("PENDING")) {
                Payment payment = new Payment();
                payment.setPaymentDate(LocalDateTime.now());
                payment.setAmount(booking.getTotalAmount());
                payment.setPaymentType("CASH");
                payment.setStatus("COMPLETED");
                payment.setBookingId(bookingId);
                payment.setTransactionId("CASH-" + bookingId + "-" + System.currentTimeMillis());

                if (!PaymentDAO.addPayment(payment)) {
                    LOGGER.warning("Failed to record payment for booking " + bookingId);
                } else {
                    if (!BookingDAO.updatePaymentStatus(bookingId, "PAID")) {
                        LOGGER.warning("Failed to update payment status for booking " + bookingId);
                    }
                    booking.setPaymentStatus("PAID");
                }
            }

            if (!BookingDAO.updateBookingStatus(bookingId, "COMPLETED")) {
                throw new Exception("Failed to update booking status");
            }

            if (!RoomDAO.updateRoomStatus(booking.getRoomId(), "AVAILABLE")) {
                throw new Exception("Failed to update room status");
            }

            Customer customer = CustomerDAO.getCustomerById(booking.getCustomerId());
            Room room = RoomDAO.getRoomByNumber(booking.getRoomId());

            JOptionPane.showMessageDialog(this,
                    String.format("Check-out Successful!\n\nGuest: %s\nRoom: %d (%s)\nCheck-in: %s\nCheck-out: %s\nTotal Amount: $%.2f\nPayment Status: %s\n\nRoom %d is now available.",
                            customer != null ? customer.getName() : "Unknown",
                            booking.getRoomId(),
                            room != null ? room.getRoomType() : "Unknown",
                            booking.getCheckInDate(),
                            LocalDate.now(),
                            booking.getTotalAmount(),
                            booking.getPaymentStatus(),
                            booking.getRoomId()),
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            if (row >= 0 && row < model.getRowCount()) {
                model.removeRow(row);
            }

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Check-out failed", ex);
            JOptionPane.showMessageDialog(this, "Check-out failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showGuestSearchPanel() {
        switchRightPanel(createGuestSearchPanel());
    }

    private JPanel createGuestSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Search Guest");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);

        JTextField searchField = new JTextField(25);
        JComboBox<String> searchType = new JComboBox<>(new String[]{"Name", "Phone", "Email", "Room Number"});
        JButton searchBtn = new JButton("Search");
        searchBtn.setBackground(new Color(59, 130, 246));
        searchBtn.setForeground(Color.WHITE);

        searchBtn.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            String type = (String) searchType.getSelectedItem();
            searchAllGuests(searchText, type);
        });

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            loadAllGuests();
        });

        searchPanel.add(new JLabel("Search by:"));
        searchPanel.add(searchType);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(clearBtn);

        String[] columns = {"Guest ID", "Name", "Phone", "Email", "Address", "Room", "Status"};
        guestTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        guestTable = new JTable(guestTableModel);
        guestTable.setRowHeight(30);
        guestTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        guestTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = guestTable.getSelectedRow();
                    if (row >= 0) {
                        int guestId = (int) guestTableModel.getValueAt(row, 0);
                        showGuestDetails(guestId);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(guestTable);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(mainPanel, BorderLayout.CENTER);

        loadAllGuests();

        return panel;
    }

    private void searchAllGuests(String searchText, String type) {
        try {
            List<Customer> customers;

            if (searchText == null || searchText.trim().isEmpty()) {
                customers = CustomerDAO.getAllCustomers();
            } else {
                customers = CustomerDAO.searchCustomers(searchText, type);
            }

            updateGuestTableWithRoomInfo(customers);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to search guests", ex);
            JOptionPane.showMessageDialog(this,
                    "Error searching guests: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateGuestTableWithRoomInfo(List<Customer> customers) {
        guestTableModel.setRowCount(0);

        for (Customer customer : customers) {
            try {
                String roomInfo = "N/A";
                String status = "Not checked in";

                Connection conn = DatabaseConnection.getConnection();
                String query = "SELECT b.room_id, b.status FROM bookings b " +
                        "WHERE b.customer_id = ? AND b.status IN ('CONFIRMED', 'CHECKED_IN') " +
                        "AND CURDATE() BETWEEN b.check_in_date AND b.check_out_date " +
                        "LIMIT 1";

                try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setInt(1, customer.getCustomerId());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            roomInfo = String.valueOf(rs.getInt("room_id"));
                            status = rs.getString("status");
                        }
                    }
                }

                guestTableModel.addRow(new Object[]{
                        customer.getCustomerId(),
                        customer.getName(),
                        customer.getPhone(),
                        customer.getEmail(),
                        customer.getAddress(),
                        roomInfo,
                        status
                });

            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Failed to get room info for customer " + customer.getCustomerId(), ex);
            }
        }
    }

    private void showGuestDetails(int guestId) {
        try {
            Customer customer = CustomerDAO.getCustomerById(guestId);
            if (customer == null) {
                JOptionPane.showMessageDialog(this, "Guest not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Booking> bookings = BookingDAO.getBookingsByCustomerId(guestId);
            Booking currentBooking = null;
            Room currentRoom = null;

            for (Booking booking : bookings) {
                if (booking.getBookingStatus().equals("CHECKED_IN") ||
                        booking.getBookingStatus().equals("CONFIRMED")) {
                    currentBooking = booking;
                    currentRoom = RoomDAO.getRoomByNumber(booking.getRoomId());
                    break;
                }
            }

            JDialog detailsDialog = new JDialog(this, "Guest Details", true);
            detailsDialog.setSize(500, 400);
            detailsDialog.setLocationRelativeTo(this);
            detailsDialog.setLayout(new BorderLayout());

            JPanel detailsPanel = new JPanel(new BorderLayout());
            detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            StringBuilder info = new StringBuilder();
            info.append("Guest Information\n");
            info.append("--------------------\n");
            info.append("Name: ").append(customer.getName()).append("\n");
            info.append("Email: ").append(customer.getEmail()).append("\n");
            info.append("Phone: ").append(customer.getPhone()).append("\n");
            info.append("Address: ").append(customer.getAddress()).append("\n");
            info.append("ID Type: ").append(customer.getIdProof()).append("\n");
            info.append("ID Number: ").append(customer.getIdNumber()).append("\n");
            info.append("Country: ").append(customer.getCountry()).append("\n");

            if (currentBooking != null && currentRoom != null) {
                info.append("\nCurrent Stay\n");
                info.append("--------------------\n");
                info.append("Room: ").append(currentRoom.getRoomNumber())
                        .append(" (").append(currentRoom.getRoomType()).append(")\n");
                info.append("Check-in: ").append(currentBooking.getCheckInDate()).append("\n");
                info.append("Check-out: ").append(currentBooking.getCheckOutDate()).append("\n");
                info.append("Status: ").append(currentBooking.getBookingStatus()).append("\n");
                info.append("Payment: ").append(currentBooking.getPaymentStatus()).append("\n");
                info.append("Total: $").append(String.format("%.2f", currentBooking.getTotalAmount())).append("\n");
            }

            JTextArea infoArea = new JTextArea(info.toString());
            infoArea.setEditable(false);
            infoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            detailsPanel.add(new JScrollPane(infoArea), BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            JButton closeBtn = new JButton("Close");
            closeBtn.addActionListener(e -> detailsDialog.dispose());
            buttonPanel.add(closeBtn);

            detailsDialog.add(detailsPanel, BorderLayout.CENTER);
            detailsDialog.add(buttonPanel, BorderLayout.SOUTH);
            detailsDialog.setVisible(true);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load guest details", ex);
            JOptionPane.showMessageDialog(this, "Error loading guest details", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showBillGenerationPanel() {
        switchRightPanel(createBillGenerationPanel());
    }

    private JPanel createBillGenerationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Generate Bill");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);

        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Find Booking");
        searchBtn.setBackground(new Color(59, 130, 246));
        searchBtn.setForeground(Color.WHITE);

        searchPanel.add(new JLabel("Search by Guest Name/Booking ID:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        JPanel billPanel = createBillDetailsPanel();
        billPanel.setVisible(false);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(billPanel, BorderLayout.CENTER);

        panel.add(mainPanel, BorderLayout.CENTER);

        searchBtn.addActionListener(e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                searchBookingForBilling(searchText, billPanel);
            }
        });

        return panel;
    }

    private JPanel createBillDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder("Bill Details"));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel hotelLabel = new JLabel("ETHIOPIAN GRAND HOTEL");
        hotelLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel billLabel = new JLabel("INVOICE");
        billLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        billLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel dateLabel = new JLabel("Date: " + LocalDate.now());
        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        headerPanel.add(hotelLabel, BorderLayout.WEST);
        headerPanel.add(billLabel, BorderLayout.CENTER);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel guestNameLabel = new JLabel("Guest: ");
        JLabel roomLabel = new JLabel("Room: ");
        JLabel checkinLabel = new JLabel("Check-in: ");
        JLabel checkoutLabel = new JLabel("Check-out: ");
        JLabel nightsLabel = new JLabel("Nights: ");
        JLabel roomChargeLabel = new JLabel("Room Charge: ");
        JLabel servicesLabel = new JLabel("Services: ");
        JLabel taxLabel = new JLabel("Tax (15%): ");
        JLabel totalLabel = new JLabel("TOTAL: ");

        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(guestNameLabel, gbc);
        gbc.gridy = 1;
        contentPanel.add(roomLabel, gbc);
        gbc.gridy = 2;
        contentPanel.add(checkinLabel, gbc);
        gbc.gridy = 3;
        contentPanel.add(checkoutLabel, gbc);
        gbc.gridy = 4;
        contentPanel.add(nightsLabel, gbc);
        contentPanel.add(new JSeparator(), gbc);
        gbc.gridy = 5;
        contentPanel.add(roomChargeLabel, gbc);
        gbc.gridy = 6;
        contentPanel.add(servicesLabel, gbc);
        gbc.gridy = 7;
        contentPanel.add(taxLabel, gbc);
        contentPanel.add(new JSeparator(), gbc);
        gbc.gridy = 8;
        contentPanel.add(totalLabel, gbc);

        panel.putClientProperty("guestNameLabel", guestNameLabel);
        panel.putClientProperty("roomLabel", roomLabel);
        panel.putClientProperty("checkinLabel", checkinLabel);
        panel.putClientProperty("checkoutLabel", checkoutLabel);
        panel.putClientProperty("nightsLabel", nightsLabel);
        panel.putClientProperty("roomChargeLabel", roomChargeLabel);
        panel.putClientProperty("servicesLabel", servicesLabel);
        panel.putClientProperty("taxLabel", taxLabel);
        panel.putClientProperty("totalLabel", totalLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void searchBookingForBilling(String searchText, JPanel billPanel) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT b.*, c.name as guest_name, r.room_type, r.price_per_night " +
                    "FROM bookings b " +
                    "JOIN customers c ON b.customer_id = c.customer_id " +
                    "JOIN rooms r ON b.room_id = r.room_number " +
                    "WHERE (c.name LIKE ? OR b.booking_id = ?) " +
                    "AND b.status IN ('CHECKED_IN', 'CONFIRMED') " +
                    "LIMIT 1";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, "%" + searchText + "%");
                try {
                    pstmt.setInt(2, Integer.parseInt(searchText));
                } catch (NumberFormatException e) {
                    pstmt.setInt(2, 0);
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int bookingId = rs.getInt("booking_id");
                        String guestName = rs.getString("guest_name");
                        int roomNumber = rs.getInt("room_id");
                        String roomType = rs.getString("room_type");
                        double pricePerNight = rs.getDouble("price_per_night");
                        Date checkInDate = rs.getDate("check_in_date");
                        Date checkOutDate = rs.getDate("check_out_date");

                        long nights = (checkOutDate.getTime() - checkInDate.getTime()) / (1000 * 60 * 60 * 24);
                        double roomCharge = pricePerNight * nights;
                        double services = 50.00;
                        double tax = (roomCharge + services) * 0.15;
                        double total = roomCharge + services + tax;

                        ((JLabel)billPanel.getClientProperty("guestNameLabel")).setText("Guest: " + guestName);
                        ((JLabel)billPanel.getClientProperty("roomLabel")).setText("Room: " + roomNumber + " (" + roomType + ")");
                        ((JLabel)billPanel.getClientProperty("checkinLabel")).setText("Check-in: " + checkInDate);
                        ((JLabel)billPanel.getClientProperty("checkoutLabel")).setText("Check-out: " + checkOutDate);
                        ((JLabel)billPanel.getClientProperty("nightsLabel")).setText("Nights: " + nights);
                        ((JLabel)billPanel.getClientProperty("roomChargeLabel")).setText("Room Charge: $" + String.format("%.2f", roomCharge));
                        ((JLabel)billPanel.getClientProperty("servicesLabel")).setText("Services: $" + String.format("%.2f", services));
                        ((JLabel)billPanel.getClientProperty("taxLabel")).setText("Tax (15%): $" + String.format("%.2f", tax));
                        ((JLabel)billPanel.getClientProperty("totalLabel")).setText("TOTAL: $" + String.format("%.2f", total));

                        billPanel.setVisible(true);
                        billPanel.putClientProperty("bookingId", bookingId);
                        billPanel.putClientProperty("totalAmount", total);

                    } else {
                        JOptionPane.showMessageDialog(billPanel, "No active booking found", "Not Found", JOptionPane.WARNING_MESSAGE);
                        billPanel.setVisible(false);
                    }
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to search booking", ex);
            JOptionPane.showMessageDialog(billPanel, "Error searching booking: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDailyReportPanel() {
        switchRightPanel(createDailyReportPanel());
    }

    private JPanel createDailyReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Daily Report - " + LocalDate.now());
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(title, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Keep only Today's Summary and Financial Report
        tabbedPane.addTab("Today's Summary", createTodaySummaryTab());
        tabbedPane.addTab("Financial Report", createFinancialReportTab());

        panel.add(tabbedPane, BorderLayout.CENTER);

        return panel;
    }
    private JPanel createTodaySummaryTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] labels = {
                "Today's Arrivals:",
                "Today's Departures:",
                "Active Guests:",
                "New Bookings:",
                "Today's Revenue:",
                "Available Rooms:",
                "Total Rooms:",
                "Occupancy Rate:"
        };

        String[] values = new String[labels.length];

        try {
            double todayRevenue = getTodayRevenueFromPayments();
            values[4] = String.format("$%.2f", todayRevenue);

            values[0] = String.valueOf(getTodaysArrivals());
            values[1] = String.valueOf(getTodaysDepartures());
            values[2] = String.valueOf(getActiveGuests());
            values[3] = String.valueOf(getNewBookings());
            values[5] = String.valueOf(getAvailableRooms());
            values[6] = String.valueOf(getTotalRooms());

            int availableRooms = Integer.parseInt(values[5]);
            int totalRooms = Integer.parseInt(values[6]);
            int occupiedRooms = totalRooms - availableRooms;
            double occupancyRate = totalRooms > 0 ? (occupiedRooms * 100.0) / totalRooms : 0;
            values[7] = String.format("%.1f%%", occupancyRate);

            JPanel summaryPanel = new JPanel(new GridLayout(labels.length, 2, 10, 10));
            summaryPanel.setBackground(Color.WHITE);

            for (int i = 0; i < labels.length; i++) {
                JLabel label = new JLabel(labels[i]);
                label.setFont(new Font("Segoe UI", Font.BOLD, 12));

                JLabel value = new JLabel(values[i]);
                value.setFont(new Font("Segoe UI", Font.PLAIN, 12));

                summaryPanel.add(label);
                summaryPanel.add(value);
            }

            panel.add(summaryPanel, BorderLayout.CENTER);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to load daily summary", ex);
            panel.add(new JLabel("Error loading daily summary"), BorderLayout.CENTER);
        }

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("Refresh Report");
        refreshBtn.addActionListener(e -> {
            Component parent = panel.getParent();
            if (parent instanceof JTabbedPane) {
                int index = ((JTabbedPane)parent).indexOfComponent(panel);
                ((JTabbedPane)parent).setComponentAt(index, createTodaySummaryTab());
            }
        });

        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private int getTodaysArrivals() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) as arrivals FROM bookings " +
                             "WHERE DATE(check_in_date) = CURDATE() " +
                             "AND status IN ('CHECKED_IN')")) {
            return rs.next() ? rs.getInt("arrivals") : 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get today's arrivals", ex);
            return 0;
        }
    }

    private int getTodaysDepartures() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) as departures FROM bookings " +
                             "WHERE DATE(check_out_date) = CURDATE() " +
                             "AND status = 'COMPLETED'")) {
            return rs.next() ? rs.getInt("departures") : 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get today's departures", ex);
            return 0;
        }
    }

    private int getActiveGuests() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(DISTINCT customer_id) as active_guests FROM bookings " +
                             "WHERE CURDATE() >= check_in_date " +
                             "AND CURDATE() <= check_out_date " +
                             "AND status IN ('CHECKED_IN', 'CONFIRMED')")) {
            return rs.next() ? rs.getInt("active_guests") : 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get active guests", ex);
            return 0;
        }
    }

    private int getNewBookings() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) as new_bookings FROM bookings " +
                             "WHERE DATE(booking_date) = CURDATE()")) {
            return rs.next() ? rs.getInt("new_bookings") : 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get new bookings", ex);
            return 0;
        }
    }

    private int getAvailableRooms() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) as available_rooms FROM rooms WHERE status = 'AVAILABLE'")) {
            return rs.next() ? rs.getInt("available_rooms") : 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get available rooms", ex);
            return 0;
        }
    }

    private int getTotalRooms() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) as total_rooms FROM rooms")) {
            return rs.next() ? rs.getInt("total_rooms") : 0;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get total rooms", ex);
            return 0;
        }
    }

    private JPanel createGuestActivityTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        try {
            Connection conn = DatabaseConnection.getConnection();

            String query = "SELECT b.booking_id, c.name as guest_name, " +
                    "b.room_id, b.check_in_date, b.check_out_date, " +
                    "b.status, b.payment_status, b.total_amount " +
                    "FROM bookings b " +
                    "JOIN customers c ON b.customer_id = c.customer_id " +
                    "WHERE ( " +
                    "   (DATE(b.check_in_date) = CURDATE() AND b.status IN ('CHECKED_IN', 'CONFIRMED')) OR " +
                    "   (DATE(b.check_out_date) = CURDATE() AND b.status = 'COMPLETED') " +
                    ") " +
                    "ORDER BY CASE " +
                    "   WHEN DATE(b.check_in_date) = CURDATE() THEN 1 " +
                    "   ELSE 2 " +
                    "END, b.check_in_date DESC";

            String[] columns = {"Booking ID", "Guest Name", "Room", "Check-in", "Check-out", "Status", "Payment", "Amount"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("booking_id"),
                            rs.getString("guest_name"),
                            rs.getInt("room_id"),
                            rs.getDate("check_in_date"),
                            rs.getDate("check_out_date"),
                            rs.getString("status"),
                            rs.getString("payment_status"),
                            String.format("$%.2f", rs.getDouble("total_amount"))
                    });
                }
            }

            JTable activityTable = new JTable(model);
            activityTable.setRowHeight(25);
            activityTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

            JScrollPane scrollPane = new JScrollPane(activityTable);
            panel.add(scrollPane, BorderLayout.CENTER);

            JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            summaryPanel.setBackground(Color.WHITE);

            String summaryQuery = "SELECT " +
                    "COUNT(DISTINCT b.booking_id) as total_activities, " +
                    "SUM(CASE WHEN DATE(b.check_in_date) = CURDATE() AND b.status IN ('CHECKED_IN', 'CONFIRMED') THEN 1 ELSE 0 END) as checked_in_today, " +
                    "SUM(CASE WHEN DATE(b.check_out_date) = CURDATE() AND b.status = 'COMPLETED') THEN 1 ELSE 0 END) as checked_out_today " +
                    "FROM bookings b " +
                    "WHERE (DATE(b.check_in_date) = CURDATE() OR DATE(b.check_out_date) = CURDATE())";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(summaryQuery)) {
                if (rs.next()) {
                    JLabel summary = new JLabel(String.format(
                            "Total Activities: %d | Checked-in Today: %d | Checked-out Today: %d",
                            rs.getInt("total_activities"),
                            rs.getInt("checked_in_today"),
                            rs.getInt("checked_out_today")
                    ));
                    summary.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    summaryPanel.add(summary);
                }
            }

            panel.add(summaryPanel, BorderLayout.NORTH);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load guest activity", ex);
            panel.add(new JLabel("Error loading guest activity"), BorderLayout.CENTER);
        }

        return panel;
    }

    private JPanel createFinancialReportTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        try {
            Map<String, Object> paymentData = getTodayPaymentBreakdown();

            double totalRevenue = getTodayRevenueFromPayments();
            int paidCount = (int) paymentData.get("PAID_COUNT");
            double paidAmount = (double) paymentData.get("PAID_AMOUNT");
            int pendingCount = (int) paymentData.get("PENDING_COUNT");
            double pendingAmount = (double) paymentData.get("PENDING_AMOUNT");
            List<Object[]> todayPayments = (List<Object[]>) paymentData.get("TODAY_PAYMENTS");

            JPanel summaryPanel = new JPanel(new GridLayout(4, 2, 10, 10));
            summaryPanel.setBackground(Color.WHITE);
            summaryPanel.setBorder(BorderFactory.createTitledBorder("Revenue Summary"));

            summaryPanel.add(new JLabel("Total Revenue Today:"));
            summaryPanel.add(new JLabel(String.format("$%.2f", totalRevenue)));

            summaryPanel.add(new JLabel("PAID Bookings:"));
            summaryPanel.add(new JLabel(paidCount + " ($" + String.format("%.2f", paidAmount) + ")"));

            summaryPanel.add(new JLabel("PENDING Bookings:"));
            summaryPanel.add(new JLabel(pendingCount + " ($" + String.format("%.2f", pendingAmount) + ")"));

            String[] columns = {"Payment ID", "Guest Name", "Amount", "Payment Type", "Date"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);

            for (Object[] payment : todayPayments) {
                model.addRow(payment);
            }

            JTable paymentsTable = new JTable(model);
            paymentsTable.setRowHeight(25);

            JScrollPane tableScroll = new JScrollPane(paymentsTable);
            tableScroll.setBorder(BorderFactory.createTitledBorder("Today's Payments"));

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(summaryPanel, BorderLayout.NORTH);
            mainPanel.add(tableScroll, BorderLayout.CENTER);

            panel.add(mainPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            panel.add(buttonPanel, BorderLayout.SOUTH);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to load financial report", ex);
            panel.add(new JLabel("Error loading financial report"), BorderLayout.CENTER);
        }

        return panel;
    }

    private void switchRightPanel(JPanel newPanel) {
        rightPanel.removeAll();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(newPanel, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void updateStatsLabel() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        statsLabel.setText("Last updated: " + time + " | " + getConnectionStatus());
    }

    private String getConnectionStatus() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return conn != null && !conn.isClosed() ? "Connected" : "Disconnected";
        } catch (SQLException ex) {
            return "Disconnected";
        }
    }

    private void startStatsUpdateTimer() {
        statsTimer = new javax.swing.Timer(30000, e -> {
            updateStatsLabel();
            if (rightPanel.getComponentCount() > 0) {
                Component comp = rightPanel.getComponent(0);
                if (comp instanceof JPanel panel) {
                    if (panel.getComponentCount() > 0) {
                        Component innerComp = panel.getComponent(0);
                        if (innerComp instanceof JLabel titleLabel) {
                            if (titleLabel.getText().contains("Today's Overview")) {
                                switchRightPanel(createTodayOverviewPanel());
                            }
                        }
                    }
                }
            }
        });
        statsTimer.start();
    }

    private void loadAvailableRooms() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            List<Room> availableRooms = new ArrayList<>();
            List<Room> allRooms = RoomDAO.getAllRooms();

            for (Room room : allRooms) {
                if ("AVAILABLE".equalsIgnoreCase(room.getStatus())) {
                    boolean isAvailable = BookingDAO.isRoomAvailable(
                            room.getRoomNumber(),
                            today,
                            tomorrow
                    );

                    if (isAvailable) {
                        availableRooms.add(room);
                    }
                }
            }

            if (roomComboBox != null) {
                roomComboBox.removeAllItems();

                if (availableRooms.isEmpty()) {
                    roomComboBox.addItem("No rooms available");
                } else {
                    roomComboBox.addItem("Select Room");
                    for (Room room : availableRooms) {
                        String display = String.format("Room %d - %s ($%.2f/night)",
                                room.getRoomNumber(),
                                room.getRoomType(),
                                room.getPricePerNight());
                        roomComboBox.addItem(display);
                    }
                }
            }

        } catch (SQLException e) {
            if (roomComboBox != null) {
                roomComboBox.removeAllItems();
                roomComboBox.addItem("Error loading rooms");
            }

            JOptionPane.showMessageDialog(this,
                    "Error loading rooms: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createTodayOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel headerPanel = createOverviewHeaderPanel();
        panel.add(headerPanel, BorderLayout.NORTH);

        JPanel statsPanel = createReceptionStats();
        panel.add(statsPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createOverviewHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("Today's Overview - " + LocalDate.now());
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));

        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statsLabel.setForeground(new Color(107, 114, 128));
        updateStatsLabel();

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(statsLabel, BorderLayout.EAST);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        return headerPanel;
    }

    private JPanel createReceptionStats() {
        JPanel statsPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        statsPanel.setBackground(new Color(245, 247, 250));

        try {
            Map<String, Integer> stats = getReceptionStats();

            statsPanel.add(createModernStatCard("Today's Arrivals",
                    String.valueOf(stats.get("arrivals")), new Color(34, 197, 94), "Check-ins today"));

            statsPanel.add(createModernStatCard("Today's Departures",
                    String.valueOf(stats.get("departures")), new Color(239, 68, 68), "Check-outs today"));

            statsPanel.add(createModernStatCard("Current Guests",
                    String.valueOf(stats.get("current_guests")), new Color(59, 130, 246), "Guests staying"));

            statsPanel.add(createModernStatCard("Available Rooms",
                    String.valueOf(stats.get("available_rooms")), new Color(20, 184, 166), "Rooms available"));

            statsPanel.add(createModernStatCard("Today's Revenue",
                    String.format("$%.2f", stats.get("revenue")/100.0), new Color(168, 85, 247), "Revenue generated"));

            statsPanel.add(createModernStatCard("Occupancy Rate",
                    stats.get("occupancy_rate") + "%", new Color(14, 165, 233), "Hotel occupancy"));

            statsPanel.add(createModernStatCard("New Bookings",
                    String.valueOf(stats.get("new_bookings")), new Color(245, 158, 11), "Bookings today"));

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to load reception stats", ex);
            statsPanel.add(createModernStatCard("Today's Arrivals", "0", new Color(34, 197, 94), "Check-ins today"));
            statsPanel.add(createModernStatCard("Today's Departures", "0", new Color(239, 68, 68), "Check-outs today"));
            statsPanel.add(createModernStatCard("Current Guests", "0", new Color(59, 130, 246), "Guests staying"));
            statsPanel.add(createModernStatCard("Available Rooms", "0", new Color(20, 184, 166), "Rooms available"));
        }

        return statsPanel;
    }

    private JPanel createModernStatCard(String title, String value, Color color, String subtitle) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JPanel iconPanel = new JPanel(new BorderLayout());
        iconPanel.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
        iconPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        iconPanel.setPreferredSize(new Dimension(80, 80));

        card.add(iconPanel, BorderLayout.WEST);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(new Color(31, 41, 55));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(107, 114, 128));

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(156, 163, 175));

        contentPanel.add(valueLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(2));
        contentPanel.add(subtitleLabel);

        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private double getTodayRevenueFromPayments() {
        double revenue = 0.0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT COALESCE(SUM(amount), 0) as today_revenue " +
                    "FROM payments " +
                    "WHERE DATE(payment_date) = CURDATE() " +
                    "AND status = 'COMPLETED'";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    revenue = rs.getDouble("today_revenue");
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get today's revenue from payments", ex);
        }

        return revenue;
    }

    private Map<String, Object> getTodayPaymentBreakdown() {
        Map<String, Object> result = new HashMap<>();
        result.put("PAID_COUNT", 0);
        result.put("PAID_AMOUNT", 0.0);
        result.put("PENDING_COUNT", 0);
        result.put("PENDING_AMOUNT", 0.0);
        result.put("TODAY_PAYMENTS", new ArrayList<Object[]>());

        try (Connection conn = DatabaseConnection.getConnection()) {
            String statusQuery = "SELECT " +
                    "    COALESCE(payment_status, 'PENDING') as payment_status, " +
                    "    COUNT(*) as count, " +
                    "    COALESCE(SUM(total_amount), 0) as amount " +
                    "FROM bookings " +
                    "WHERE DATE(booking_date) = CURDATE() " +
                    "GROUP BY payment_status";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(statusQuery)) {

                while (rs.next()) {
                    String status = rs.getString("payment_status");
                    int count = rs.getInt("count");
                    double amount = rs.getDouble("amount");

                    if ("PAID".equals(status)) {
                        result.put("PAID_COUNT", count);
                        result.put("PAID_AMOUNT", amount);
                    } else if ("PENDING".equals(status)) {
                        result.put("PENDING_COUNT", count);
                        result.put("PENDING_AMOUNT", amount);
                    }
                }
            }

            String paymentsQuery = "SELECT p.payment_id, c.name as guest_name, p.amount, " +
                    "p.payment_type, p.payment_date " +
                    "FROM payments p " +
                    "JOIN bookings b ON p.booking_id = b.booking_id " +
                    "JOIN customers c ON b.customer_id = c.customer_id " +
                    "WHERE DATE(p.payment_date) = CURDATE() " +
                    "AND p.status = 'COMPLETED' " +
                    "ORDER BY p.payment_date DESC";

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(paymentsQuery)) {

                List<Object[]> paymentsList = new ArrayList<>();
                while (rs.next()) {
                    paymentsList.add(new Object[]{
                            rs.getInt("payment_id"),
                            rs.getString("guest_name"),
                            String.format("$%.2f", rs.getDouble("amount")),
                            rs.getString("payment_type"),
                            rs.getTimestamp("payment_date")
                    });
                }
                result.put("TODAY_PAYMENTS", paymentsList);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to get payment breakdown", ex);
        }

        return result;
    }

    private Map<String, Integer> getReceptionStats() {
        Map<String, Integer> stats = new HashMap<>();

        try {
            stats.put("arrivals", getTodaysArrivals());
            stats.put("departures", getTodaysDepartures());
            stats.put("current_guests", getActiveGuests());
            stats.put("new_bookings", getNewBookings());

            double todayRevenue = getTodayRevenueFromPayments();
            stats.put("revenue", (int)(todayRevenue * 100));

            stats.put("available_rooms", getAvailableRooms());
            stats.put("total_rooms", getTotalRooms());

            int availableRooms = stats.get("available_rooms");
            int totalRooms = stats.get("total_rooms");
            int occupiedRooms = totalRooms - availableRooms;
            int occupancyRate = totalRooms > 0 ? (occupiedRooms * 100) / totalRooms : 0;
            stats.put("occupancy_rate", occupancyRate);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to get reception stats", ex);

            stats.put("arrivals", 0);
            stats.put("departures", 0);
            stats.put("current_guests", 0);
            stats.put("new_bookings", 0);
            stats.put("revenue", 0);
            stats.put("available_rooms", 0);
            stats.put("total_rooms", 0);
            stats.put("occupancy_rate", 0);
        }

        return stats;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(31, 41, 55));
        header.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));

        JButton homeBtn = new JButton("Dashboard");
        homeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        homeBtn.setBackground(new Color(59, 130, 246));
        homeBtn.setForeground(Color.red);
        homeBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        homeBtn.addActionListener(e -> showDashboard());

        JLabel title = new JLabel("Reception Dashboard - " + currentUser);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timeLabel.setForeground(Color.WHITE);
        updateHeaderTimeLabel(timeLabel);
        header.add(homeBtn, BorderLayout.WEST);
        header.add(title, BorderLayout.CENTER);
        header.add(timeLabel, BorderLayout.EAST);

        new javax.swing.Timer(60000, e -> updateHeaderTimeLabel(timeLabel)).start();

        return header;
    }

    private void showDashboard() {
        switchRightPanel(createTodayOverviewPanel());
    }

    private void updateHeaderTimeLabel(JLabel label) {
        String time = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("hh:mm a, EEE, MMM dd yyyy")
        );
        label.setText(time);
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(31, 41, 55));
        footer.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        JLabel footerText = new JLabel("© 2026 Hotel Management System | Reception Desk");
        footerText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerText.setForeground(new Color(156, 163, 175));

        JLabel userLabel = new JLabel("Logged in as: " + currentUser);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userLabel.setForeground(new Color(156, 163, 175));

        footer.add(footerText, BorderLayout.WEST);
        footer.add(userLabel, BorderLayout.EAST);

        return footer;
    }

    private JButton createReceptionLogoutButton() {
        JButton button = new JButton("Logout");
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBackground(new Color(239, 68, 68));
        button.setForeground(Color.black);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.addActionListener(e -> logout());

        return button;
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout from reception desk?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (statsTimer != null) {
                statsTimer.stop();
            }
            this.dispose();
        }
    }

    private void setupWindow() {
        try {
            ImageIcon icon = new ImageIcon("images/icon.png");
            this.setIconImage(icon.getImage());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to load icon", ex);
        }

        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    static class ButtonRenderer extends JButton implements TableCellRenderer {
        private final String buttonText;

        public ButtonRenderer(String text) {
            this.buttonText = text;
            setOpaque(true);
            setBackground(new Color(59, 130, 246));
            setForeground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            setText(buttonText);
            return this;
        }
    }

    static class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private final JTable table;
        private final DefaultTableModel model;
        private final ReceptionDashboard dashboard;

        public ButtonEditor(JCheckBox checkBox, JTable table, DefaultTableModel model, ReceptionDashboard dashboard) {
            super(checkBox);
            this.table = table;
            this.model = model;
            this.dashboard = dashboard;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int row = table.getEditingRow();
                int bookingId = (int) model.getValueAt(row, 0);
                dashboard.processCheckout(bookingId, row, model);
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new ReceptionDashboard("reception").setVisible(true)
        );
    }
}
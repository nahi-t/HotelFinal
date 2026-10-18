package hotel.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;

import hotel.models.Staff;

public class StaffDialog extends JDialog {
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<String> positionCombo;
    private JComboBox<String> departmentCombo;
    private JComboBox<String> statusCombo;
    private JTextField salaryField;
    private JTextArea addressArea;

    private Staff staff;
    private boolean confirmed = false;

    public StaffDialog(Staff staff, String title) {
        this.staff = staff;
        initComponents(title);
    }

    private void initComponents(String title) {
        setTitle(title);
        setModal(true);
        setSize(500, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Full Name:"), gbc);

        gbc.gridx = 1;
        nameField = new JTextField(20);
        if (staff != null) nameField.setText(staff.getName());
        formPanel.add(nameField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        emailField = new JTextField(20);
        if (staff != null) emailField.setText(staff.getEmail());
        formPanel.add(emailField, gbc);

        // Phone
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Phone:"), gbc);

        gbc.gridx = 1;
        phoneField = new JTextField(20);
        if (staff != null) phoneField.setText(staff.getPhone());
        formPanel.add(phoneField, gbc);

        // Position
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Position:"), gbc);

        gbc.gridx = 1;
        positionCombo = new JComboBox<>(new String[]{"Manager", "Receptionist", "Housekeeping",
                "Chef", "Waiter", "Security"});
        if (staff != null) positionCombo.setSelectedItem(staff.getPosition());
        formPanel.add(positionCombo, gbc);

        // Department
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Department:"), gbc);

        gbc.gridx = 1;
        departmentCombo = new JComboBox<>(new String[]{"Management", "Reception", "Housekeeping",
                "Kitchen", "Service", "Security"});
        if (staff != null) departmentCombo.setSelectedItem(staff.getDepartment());
        formPanel.add(departmentCombo, gbc);

        // Status
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Status:"), gbc);

        gbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE", "ON_LEAVE"});
        if (staff != null) statusCombo.setSelectedItem(staff.getStatus());
        formPanel.add(statusCombo, gbc);

        // Salary
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(new JLabel("Salary:"), gbc);

        gbc.gridx = 1;
        salaryField = new JTextField(20);
        if (staff != null) salaryField.setText(String.valueOf(staff.getSalary()));
        formPanel.add(salaryField, gbc);

        // Address
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(new JLabel("Address:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        addressArea = new JTextArea(3, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        if (staff != null && staff.getAddress() != null) {
            addressArea.setText(staff.getAddress());
        }
        formPanel.add(new JScrollPane(addressArea), gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));

        JButton saveButton = new JButton("💾 Save");
        saveButton.setBackground(new Color(34, 197, 94));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.addActionListener(e -> saveStaff());

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

    private void saveStaff() {
        try {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String position = (String) positionCombo.getSelectedItem();
            String department = (String) departmentCombo.getSelectedItem();
            String status = (String) statusCombo.getSelectedItem();
            double salary = Double.parseDouble(salaryField.getText().trim());
            String address = addressArea.getText().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Name and phone are required fields!",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (salary < 0) {
                JOptionPane.showMessageDialog(this,
                        "Salary must be a positive number!",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (staff == null) {
                staff = new Staff();
                staff.setHireDate(LocalDate.now().toString());
            }

            staff.setFirstName(name.split("")[0]);
            staff.setLastName(name.split(" ").length > 1 ? name.split(" ")[1] : "");

            staff.setEmail(email);
            staff.setPhone(phone);
            staff.setPosition(position);
            staff.setDepartment(department);
            staff.setStatus(status);
            staff.setSalary(salary);
            staff.setAddress(address);

            confirmed = true;
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid salary amount!",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public Staff getStaff() {
        return staff;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
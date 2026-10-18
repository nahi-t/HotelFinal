package hotel.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import hotel.dao.UserDAO;
import hotel.models.User;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private static final Logger LOGGER = Logger.getLogger(LoginFrame.class.getName());

    public LoginFrame() {
        initComponents();
        setupWindow();
    }

    private void initComponents() {
        setTitle("Hotel Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    ImageIcon background = new ImageIcon("images/background.jpg");
                    g.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);
                } catch (Exception ex) {
                    Graphics2D g2d = (Graphics2D) g;
                    GradientPaint gradient = new GradientPaint(0, 0, new Color(37, 99, 235),
                            0, getHeight(), new Color(14, 165, 233));
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel logoLabel;
        try {
            ImageIcon logoIcon = new ImageIcon("images/logo.jpn");
            Image scaledLogo = logoIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            logoLabel = new JLabel(new ImageIcon(scaledLogo));
        } catch (Exception ex) {
            logoLabel = new JLabel("🏨");
            logoLabel.setFont(new Font("Arial", Font.PLAIN, 60));
        }
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("HOTEL LOGIN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Management System");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(209, 213, 219));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        JLabel roleLabel = new JLabel("Select Role:");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        roleComboBox = new JComboBox<>(new String[]{"Admin", "Manager", "Reception" });
        styleComboBox(roleComboBox);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField();
        styleTextField(usernameField);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passLabel.setForeground(Color.WHITE);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField();
        styleTextField(passwordField);

        JButton loginButton = new JButton("LOGIN");
        styleLoginButton(loginButton);
        loginButton.addActionListener(e -> login());

        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    login();
                }
            }
        });

        formPanel.add(roleLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(roleComboBox);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(userLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(usernameField);
        formPanel.add(Box.createVerticalStrut(15));
        formPanel.add(passLabel);
        formPanel.add(Box.createVerticalStrut(5));
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(30));
        formPanel.add(loginButton);

        centerPanel.add(logoLabel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(titleLabel);
        centerPanel.add(subtitleLabel);
        centerPanel.add(formPanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JLabel footerLabel = new JLabel("© 2024 Hotel Management System v3.0", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(209, 213, 219));
        footerLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        mainPanel.add(footerLabel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(Color.WHITE);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void styleTextField(JComponent field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    private void styleLoginButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setBackground(new Color(34, 197, 94));
        button.setForeground(Color.green);
        button.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(22, 163, 74));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(34, 197, 94));
            }
        });
    }
    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = (String) roleComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter username and password!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (role == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a role!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
             User user = UserDAO.authenticate(username, password, role);

            if (user != null) {
                JOptionPane.showMessageDialog(this,
                        "Login successful! Welcome " + user.getName(),
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                dispose();

                switch (role) {
                    case "Admin":
                        new AdminDashboard(user.getName()).setVisible(true);
                        break;
                    case "Manager":
                        new ManagerDashboard(user.getName()).setVisible(true);
                        break;
                    case "Reception":
                        new ReceptionDashboard(user.getName()).setVisible(true);
                        break;
                    default:
                        JOptionPane.showMessageDialog(this,
                                "Unknown role: " + role,
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        break;
                }

            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username, password or role!",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error during login", ex);
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void setupWindow() {
        try {
            ImageIcon icon = new ImageIcon("images/icon.jpn");
            setIconImage(icon.getImage());
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Failed to load icon", ex);
        }
    }
}
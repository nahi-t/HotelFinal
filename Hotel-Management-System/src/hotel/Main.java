package hotel;

import hotel.ui.LoginFrame;
import hotel.database.DatabaseConnection;

public class Main {
    static void main(String[] args) {

        System.out.println("Hotel Management System - Starting...");

        try {
            javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

         java.awt.EventQueue.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
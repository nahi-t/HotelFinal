// file: TestRoomManagement.java
package hotel.test;

import hotel.ui.RoomManagementPanel;
import javax.swing.*;

public class TestRoomManagement {
    static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Test Room Management");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 700);
            frame.setLocationRelativeTo(null);

            RoomManagementPanel roomPanel = new RoomManagementPanel();
            frame.add(roomPanel);

            frame.setVisible(true);
        });
    }
}
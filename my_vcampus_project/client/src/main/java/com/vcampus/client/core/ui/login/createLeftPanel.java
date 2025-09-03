package com.vcampus.client.core.ui.login;

import javax.swing.*;
import java.awt.*;

public class createLeftPanel {
    static JPanel createLeftPanel(LoginFrame loginFrame) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel imageLabel = new JLabel();
        try {
            imageLabel.setIcon(new ImageIcon(loginFrame.getClass().getResource("/figures/login_backgroud.jpg")));
        } catch (Exception e) {
            imageLabel.setText("VCampus");
            imageLabel.setFont(new Font("微软雅黑", Font.BOLD, 48));
            imageLabel.setForeground(new Color(255, 127, 80));
        }
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        panel.add(imageLabel, BorderLayout.CENTER);
        return panel;
    }
}

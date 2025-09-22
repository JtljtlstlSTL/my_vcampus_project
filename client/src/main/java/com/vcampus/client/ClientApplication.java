package com.vcampus.client;

import com.formdev.flatlaf.FlatLightLaf;
import com.vcampus.client.core.ui.login.LoginFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

/**
 * VCampus Client Application Entry
 *
 * @author VCampus Team
 * @version 1.0
 */
public class ClientApplication {
    private static final Logger logger = LoggerFactory.getLogger(ClientApplication.class);

    public static void main(String[] args) {
        // Print startup information
        printBanner();

        // Set system properties
        System.setProperty("sun.java2d.opengl", "true");

        // Set appearance
        setupLookAndFeel();

        // Start GUI
        SwingUtilities.invokeLater(() -> {
            // Show login interface first
            showLoginInterface();
        });
    }

    /**
     * Print startup banner
     */
    private static void printBanner() {
        System.out.println();
        System.out.println("VCampus Virtual Campus Client v1.0.0");
    System.out.println("https://github.com/JtljtlstlSTL/my_vcampus_project");
        System.out.println();
        System.out.println("Starting client application...");
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        System.out.println("Architecture: " + System.getProperty("os.arch"));
        System.out.println();
    }

    /**
     * 设置外观和感觉
     */
    private static void setupLookAndFeel() {
        try {
            // 使用FlatLaf现代化外观
            FlatLightLaf.setup();

            // 自定义UI属性
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            // 全局设置表格与表头字体为系统可用的中文/通用字体（不使用 emoji）
            try {
                java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
                java.util.Set<String> fam = new java.util.HashSet<>();
                for (String f : ge.getAvailableFontFamilyNames()) fam.add(f);
                String[] candidates = new String[]{"Microsoft YaHei","微软雅黑","Noto Sans CJK SC","SimSun","Arial Unicode MS","Segoe UI","Dialog"};
                String pick = Font.DIALOG;
                for (String c : candidates) if (fam.contains(c)) { pick = c; break; }
                UIManager.put("Table.font", new Font(pick, Font.PLAIN, 13));
                UIManager.put("TableHeader.font", new Font(pick, Font.BOLD, 13));
            } catch (Exception ignored) {}

            logger.info("FlatLaf appearance setup completed");

        } catch (Exception e) {
            logger.warn("Failed to setup FlatLaf, using system default: {}", e.getMessage());
            try {
                // 使用系统默认外观
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                logger.error("Failed to setup system look and feel", ex);
            }
        }
    }

    /**
     * Show login interface
     */
    private static void showLoginInterface() {
        try {
            // Create and show login frame
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);

            // Center the window on screen
            centerWindow(loginFrame);

            logger.info("Login interface displayed successfully");

        } catch (Exception e) {
            logger.error("Failed to show login interface", e);
            JOptionPane.showMessageDialog(null,
                    "Failed to start client application: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * Center window on screen
     */
    private static void centerWindow(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = window.getSize();

        int x = (screenSize.width - windowSize.width) / 2;
        int y = (screenSize.height - windowSize.height) / 2;

        window.setLocation(x, y);
    }
}

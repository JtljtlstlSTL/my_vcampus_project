package com.vcampus.client;

import com.formdev.flatlaf.FlatDarkLaf;
import com.vcampus.client.ui.LoginFrame;
import com.vcampus.client.ui.MainFrame;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

/**
 * VCampus客户端应用程序入口
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class ClientApplication {
    
    public static void main(String[] args) {
        // 打印启动信息
        printBanner();
        
        // 设置系统属性
        setupSystemProperties();
        
        // 设置外观
        setupLookAndFeel();
        
        // 启动GUI
        SwingUtilities.invokeLater(() -> {
            try {
                // 先显示登录界面
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                System.out.println("✅ VCampus client started successfully, showing login interface");
            } catch (Exception e) {
                System.err.println("💥 Client startup failed: " + e.getMessage());
                JOptionPane.showMessageDialog(null, 
                        "Client startup failed: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
    
    /**
     * 打印启动横幅
     */
    private static void printBanner() {
        System.out.println();
        System.out.println("██╗   ██╗ ██████╗ █████╗ ███╗   ███╗██████╗ ██╗   ██╗███████╗");
        System.out.println("██║   ██║██╔════╝██╔══██╗████╗ ████║██╔══██╗██║   ██║██╔════╝");
        System.out.println("██║   ██║██║     ███████║██╔████╔██║██████╔╝██║   ██║███████╗");
        System.out.println("╚██╗ ██╔╝██║     ██╔══██║██║╚██╔╝██║██╔═══╝ ██║   ██║╚════██║");
        System.out.println(" ╚████╔╝ ╚██████╗██║  ██║██║ ╚═╝ ██║██║     ╚██████╔╝███████║");
        System.out.println("  ╚═══╝   ╚═════╝╚═╝  ╚═╝╚═╝     ╚═╝╚═╝      ╚═════╝ ╚══════╝");
        System.out.println();
        System.out.println("🎨 Virtual Campus Client v1.0.0");
        System.out.println("📧 https://github.com/vcampus-project");
        System.out.println();
    }
    
    /**
     * Setup system properties
     */
    private static void setupSystemProperties() {
        // Enable anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        
        // Enable hardware acceleration
        System.setProperty("sun.java2d.opengl", "true");
        
        // Set encoding properties (fix encoding issues)
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("console.encoding", "UTF-8");
        System.setProperty("user.language", "en");
        System.setProperty("user.country", "US");
        System.setProperty("sun.jnu.encoding", "UTF-8");
        
        // Set console encoding
        try {
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
            System.setErr(new java.io.PrintStream(System.err, true, "UTF-8"));
        } catch (Exception e) {
            System.err.println("⚠️ Failed to set console encoding: " + e.getMessage());
        }
        
        System.out.println("🔧 System properties setup completed");
    }
    
    /**
     * 设置外观和感觉
     */
    private static void setupLookAndFeel() {
        try {
            // 使用FlatLaf现代化外观
            UIManager.setLookAndFeel(new FlatDarkLaf());
            
            // 自定义UI属性
            UIManager.put("Button.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 8);
            UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
            
            System.out.println("🎨 UI appearance setup completed");
            
        } catch (Exception e) {
            System.out.println("⚠️ Failed to set appearance, using system default: " + e.getMessage());
            try {
                // Use system default appearance
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("💥 Failed to set system appearance: " + ex.getMessage());
            }
        }
    }
}

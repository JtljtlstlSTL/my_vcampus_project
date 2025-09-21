package com.vcampus.client.core.ui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 带显示/隐藏切换功能的密码输入框组件
 *
 * @author VCampus Team
 * @version 1.0
 */
public class PasswordFieldWithToggle extends JPanel {

    private JPasswordField passwordField;
    private JLabel toggleLabel;
    private ImageIcon eyeOpenIcon;
    private ImageIcon eyeCloseIcon;
    private boolean isPasswordVisible = false;

    public PasswordFieldWithToggle() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {
        passwordField = new JPasswordField();
        passwordField.setEchoChar('●'); // 使用圆点作为密码字符

        // 加载眼睛图标
        try {
            eyeOpenIcon = new ImageIcon(getClass().getResource("/figures/eye_open.png"));
            eyeCloseIcon = new ImageIcon(getClass().getResource("/figures/eye_close.png"));

            // 缩放图标到合适大小
            Image openImg = eyeOpenIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            Image closeImg = eyeCloseIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);

            eyeOpenIcon = new ImageIcon(openImg);
            eyeCloseIcon = new ImageIcon(closeImg);

        } catch (Exception e) {
            // 如果图标加载失败，使用文本
            eyeOpenIcon = null;
            eyeCloseIcon = null;
        }

        toggleLabel = new JLabel();
        updateToggleIcon();
        toggleLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleLabel.setToolTipText("点击显示/隐藏密码");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        add(passwordField, BorderLayout.CENTER);

        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        iconPanel.setOpaque(false);
        iconPanel.add(toggleLabel);

        add(iconPanel, BorderLayout.EAST);
    }

    private void setupEventHandlers() {
        toggleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                togglePasswordVisibility();
            }
        });
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            passwordField.setEchoChar((char) 0); // 显示明文
        } else {
            passwordField.setEchoChar('●'); // 隐藏密码
        }

        updateToggleIcon();
    }

    private void updateToggleIcon() {
        if (eyeOpenIcon != null && eyeCloseIcon != null) {
            toggleLabel.setIcon(isPasswordVisible ? eyeOpenIcon : eyeCloseIcon);
            toggleLabel.setText("");
        } else {
            // 如果图标加载失败，使用文本
            toggleLabel.setIcon(null);
            toggleLabel.setText(isPasswordVisible ? "隐藏" : "显示");
        }
    }

    // 提供JPasswordField的常用方法
    public char[] getPassword() {
        return passwordField.getPassword();
    }

    public void setPassword(String password) {
        passwordField.setText(password);
    }

    public JPasswordField getPasswordField() {
        return passwordField;
    }

    public void setFont(Font font) {
        super.setFont(font);
        if (passwordField != null) {
            passwordField.setFont(font);
        }
    }

    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize);
        if (passwordField != null) {
            passwordField.setPreferredSize(preferredSize);
        }
    }
}

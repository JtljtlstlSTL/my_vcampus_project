package com.vcampus.client.core.ui.login;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class createRightPanel {
    static JPanel createRightPanel(final LoginFrame loginFrame) {
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(248, 249, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblCnName = new JLabel("统一登录门户");
        lblCnName.setFont(new Font("微软雅黑", Font.BOLD, 20));
        lblCnName.setForeground(new Color(255, 127, 80));

        JLabel lblEgName = new JLabel("VCampus");
        lblEgName.setFont(new Font("微软雅黑", Font.PLAIN, 8));
        lblEgName.setForeground(Color.GRAY);

        Color grayColor = new Color(80, 80, 80);

        JLabel lblUsername = new JLabel("用户名:");
        lblUsername.setFont(new Font("微软雅黑", Font.BOLD, 13));
        lblUsername.setForeground(grayColor);
        // 初始化 txtUsername，通过 Getter 访问
        JTextField txtUsername = loginFrame.getTxtUsername();
        txtUsername.setColumns(15);
        txtUsername.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        JLabel lblPassword = new JLabel("密码:");
        lblPassword.setFont(new Font("微软雅黑", Font.BOLD, 13));
        lblPassword.setForeground(grayColor);

        JPasswordField txtPassword = loginFrame.getTxtPassword();
        txtPassword.setColumns(15);
        txtPassword.setEchoChar('*');
        txtPassword.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        JLabel lblUsernameError = loginFrame.getLblUsernameError();
        lblUsernameError.setText("用户名不能为空！");
        lblUsernameError.setForeground(new Color(248, 249, 250)); // 初始与背景同色（看不见）
        lblUsernameError.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        lblUsernameError.setVisible(true); // 初始时显示，但颜色与背景一致
        // 设置固定宽度，确保错误信息能够完整显示
        lblUsernameError.setPreferredSize(new Dimension(150, lblUsernameError.getPreferredSize().height));

        JLabel lblPasswordError = loginFrame.getLblPasswordError();
        lblPasswordError.setText("密码不能为空！");
        lblPasswordError.setForeground(new Color(248, 249, 250)); // 初始与背景同色（看不见）
        lblPasswordError.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        lblPasswordError.setVisible(true); // 初始时显示，但颜色与背景一致
        // 固定宽度，基于最长文本"用户名或密码错误！"计算
        lblPasswordError.setPreferredSize(new Dimension(150, lblPasswordError.getPreferredSize().height));

        JButton btnLogin = loginFrame.getBtnLogin();
        btnLogin.setText("登录");
        btnLogin.setPreferredSize(new Dimension(300, 40));
        btnLogin.setBackground(new Color(255, 127, 80));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);

        txtUsername.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // 移除自动显示错误信息的逻辑，只在登录时验证
            }
        });
        txtUsername.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                // 当用户输入字符时，将错误标签颜色变回背景色
                if (!txtUsername.getText().trim().isEmpty()) {
                    lblUsernameError.setForeground(new Color(248, 249, 250));
                }
            }
            public void removeUpdate(DocumentEvent e) {
                // 当用户删除字符时，如果文本框为空，显示错误提示
                if (txtUsername.getText().trim().isEmpty()) {
                    lblUsernameError.setText("用户名不能为空！");
                    lblUsernameError.setForeground(Color.RED);
                } else {
                    lblUsernameError.setForeground(new Color(248, 249, 250));
                }
            }
            public void changedUpdate(DocumentEvent e) {}
        });

        txtPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // 移除自动显示错误信息的逻辑，只在登录时验证
            }
        });
        txtPassword.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                // 当用户输入字符时，将错误标签颜色变回背景色
                if (!new String(txtPassword.getPassword()).trim().isEmpty()) {
                    lblPasswordError.setForeground(new Color(248, 249, 250));
                }
            }
            public void removeUpdate(DocumentEvent e) {
                // 当用户删除字符时，如果密码框为空，显示错误提示
                if (new String(txtPassword.getPassword()).trim().isEmpty()) {
                    lblPasswordError.setText("密码不能为空！");
                    lblPasswordError.setForeground(Color.RED);
                } else {
                    lblPasswordError.setForeground(new Color(248, 249, 250));
                }
            }
            public void changedUpdate(DocumentEvent e) {}
        });

        gbc.insets = new Insets(20, 5, 8, 5);
        loginFrame.addComponent(rightPanel, lblCnName, 0, 0, 3, gbc);
        gbc.insets = new Insets(0, 5, 35, 5);
        loginFrame.addComponent(rightPanel, lblEgName, 0, 1, 2, gbc);

        gbc.insets = new Insets(8, 5, 8, 2);
        loginFrame.addComponent(rightPanel, lblUsername, 0, 2, 1, gbc);
        gbc.insets = new Insets(8, 0, 8, 5);
        loginFrame.addComponent(rightPanel, txtUsername, 1, 2, 2, gbc);

        gbc.insets = new Insets(0, 5, 0, 5);
        loginFrame.addComponent(rightPanel, lblUsernameError, 1, 3, 2, gbc);

        gbc.insets = new Insets(0, 5, 8, 2);
        loginFrame.addComponent(rightPanel, lblPassword, 0, 4, 1, gbc);
        gbc.insets = new Insets(0, 0, 8, 5);
        loginFrame.addComponent(rightPanel, txtPassword, 1, 4, 2, gbc);

        gbc.insets = new Insets(0, 5, 10, 5);
        loginFrame.addComponent(rightPanel, lblPasswordError, 1, 5, 2, gbc);

        gbc.insets = new Insets(25, 5, 15, 5);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        rightPanel.add(btnLogin, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        txtUsername.setBackground(new Color(220, 220, 220));
        txtUsername.setForeground(Color.BLACK);

        txtPassword.setBackground(new Color(220, 220, 220));
        txtPassword.setForeground(Color.BLACK);
        return rightPanel;
    }
}

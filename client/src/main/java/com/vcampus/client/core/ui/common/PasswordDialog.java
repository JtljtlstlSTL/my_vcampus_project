package com.vcampus.client.core.ui.common;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 密码修改对话框
 */
public class PasswordDialog extends JDialog {

    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private boolean confirmed = false;

    public PasswordDialog(Frame parent, String title) {
        super(parent, title, true);
        initComponents();
        setupEventHandlers();
    }

    private void initComponents() {
        setSize(400, 250);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // 旧密码
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("原密码:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        oldPasswordField = new JPasswordField(15);
        mainPanel.add(oldPasswordField, gbc);

        // 新密码
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        mainPanel.add(new JLabel("新密码:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        newPasswordField = new JPasswordField(15);
        mainPanel.add(newPasswordField, gbc);

        // 确认密码
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        mainPanel.add(new JLabel("确认密码:"), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        confirmPasswordField = new JPasswordField(15);
        mainPanel.add(confirmPasswordField, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnOK = new JButton("确定");
        JButton btnCancel = new JButton("取消");

        buttonPanel.add(btnOK);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

        // 按钮事件
        btnOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleOK();
            }
        });

        btnCancel.addActionListener(e -> dispose());
    }

    private void setupEventHandlers() {
        // 回车键确认
        getRootPane().setDefaultButton((JButton) ((JPanel) getContentPane().getComponent(1)).getComponent(0));

        // ESC键取消
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void handleOK() {
        String oldPassword = new String(oldPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // 验证输入
        if (oldPassword.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入原密码！", "错误", JOptionPane.ERROR_MESSAGE);
            oldPasswordField.requestFocus();
            return;
        }

        if (newPassword.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入新密码！", "错误", JOptionPane.ERROR_MESSAGE);
            newPasswordField.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(this, "新密码长度不能少于6位！", "错误", JOptionPane.ERROR_MESSAGE);
            newPasswordField.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "两次输入的新密码不一致！", "错误", JOptionPane.ERROR_MESSAGE);
            confirmPasswordField.requestFocus();
            return;
        }

        if (oldPassword.equals(newPassword)) {
            JOptionPane.showMessageDialog(this, "新密码不能与原密码相同！", "错误", JOptionPane.ERROR_MESSAGE);
            newPasswordField.requestFocus();
            return;
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getOldPassword() {
        return new String(oldPasswordField.getPassword());
    }

    public String getNewPassword() {
        return new String(newPasswordField.getPassword());
    }
}

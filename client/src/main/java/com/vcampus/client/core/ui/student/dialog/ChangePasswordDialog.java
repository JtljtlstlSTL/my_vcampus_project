package com.vcampus.client.core.ui.student.dialog;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * 修改密码对话框
 *
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class ChangePasswordDialog extends JDialog {
    
    private NettyClient nettyClient;
    private Map<String, Object> userData;
    private JPasswordField txtOldPassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;
    
    public ChangePasswordDialog(JFrame parent, NettyClient nettyClient, Map<String, Object> userData) {
        super(parent, "修改密码", true);
        this.nettyClient = nettyClient;
        this.userData = userData;
        
        initUI();
    }
    
    private void initUI() {
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 标题
        JLabel titleLabel = new JLabel("修改密码");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);
        
        // 原密码
        gbc.gridwidth = 1; gbc.gridy = 1;
        mainPanel.add(new JLabel("原密码:"), gbc);
        gbc.gridx = 1;
        txtOldPassword = new JPasswordField(20);
        mainPanel.add(txtOldPassword, gbc);
        
        // 新密码
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("新密码:"), gbc);
        gbc.gridx = 1;
        txtNewPassword = new JPasswordField(20);
        mainPanel.add(txtNewPassword, gbc);
        
        // 确认密码
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("确认密码:"), gbc);
        gbc.gridx = 1;
        txtConfirmPassword = new JPasswordField(20);
        mainPanel.add(txtConfirmPassword, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnOK = new JButton("确定");
        JButton btnCancel = new JButton("取消");
        buttonPanel.add(btnOK);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 事件处理
        btnOK.addActionListener(e -> handleChangePassword());
        btnCancel.addActionListener(e -> dispose());
    }
    
    private void handleChangePassword() {
        String oldPassword = new String(txtOldPassword.getPassword());
        String newPassword = new String(txtNewPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        
        // 验证输入
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写所有字段！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "新密码和确认密码不一致！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(this, "新密码长度不能少于6位！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 发送修改密码请求
        try {
            Map<String, String> requestData = new java.util.HashMap<>();
            requestData.put("cardNumber", userData.get("cardNumber").toString());
            requestData.put("oldPassword", oldPassword);
            requestData.put("newPassword", newPassword);
            
            Request request = new Request("changePassword", requestData);
            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if ("success".equals(response.getStatus())) {
                        JOptionPane.showMessageDialog(this, "密码修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "密码修改失败：" + response.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.error("修改密码时发生错误", throwable);
                    JOptionPane.showMessageDialog(this, "修改密码时发生错误：" + throwable.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
        } catch (Exception e) {
            log.error("修改密码时发生错误", e);
            JOptionPane.showMessageDialog(this, "修改密码时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}

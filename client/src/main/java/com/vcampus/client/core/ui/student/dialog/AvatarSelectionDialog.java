package com.vcampus.client.core.ui.student.dialog;

import com.vcampus.client.core.ui.student.AvatarManager;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Map;

/**
 * 头像选择对话框
 *
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class AvatarSelectionDialog extends JDialog {
    
    private Map<String, Object> userData;
    private JLabel sidebarAvatar;
    private JLabel profileAvatar;
    private AvatarManager avatarManager;
    
    public AvatarSelectionDialog(JFrame parent, Map<String, Object> userData, 
                               JLabel sidebarAvatar, JLabel profileAvatar) {
        super(parent, "更换头像", true);
        this.userData = userData;
        this.sidebarAvatar = sidebarAvatar;
        this.profileAvatar = profileAvatar;
        this.avatarManager = new AvatarManager(userData);
        
        initUI();
    }
    
    private void initUI() {
        setSize(400, 300);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // 当前头像显示
        JLabel currentAvatarLabel = new JLabel();
        currentAvatarLabel.setPreferredSize(new Dimension(100, 100));
        currentAvatarLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        avatarManager.loadUserAvatar(currentAvatarLabel, 100, 100);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        contentPanel.add(new JLabel("当前头像:", SwingConstants.CENTER), gbc);
        gbc.gridy = 1;
        contentPanel.add(currentAvatarLabel, gbc);
        
        // 按钮
        gbc.gridy = 2; gbc.gridwidth = 1;
        JButton btnChooseFile = new JButton("选择文件");
        JButton btnUseDefault = new JButton("使用默认");
        
        gbc.gridx = 0;
        contentPanel.add(btnChooseFile, gbc);
        gbc.gridx = 1;
        contentPanel.add(btnUseDefault, gbc);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // 确认取消按钮
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnOK = new JButton("确定");
        JButton btnCancel = new JButton("取消");
        buttonPanel.add(btnOK);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 事件处理
        btnChooseFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("图片文件", "jpg", "jpeg", "png", "gif"));
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    // 加载选择的图片
                    javax.imageio.ImageIO.read(selectedFile);
                    // 更新当前头像显示
                    currentAvatarLabel.setIcon(new ImageIcon(selectedFile.getAbsolutePath()));
                    // 保存头像路径到用户数据
                    userData.put("avatarPath", selectedFile.getAbsolutePath());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "选择的文件不是有效的图片格式！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        btnUseDefault.addActionListener(e -> {
            userData.remove("avatarPath");
            avatarManager.loadUserAvatar(currentAvatarLabel, 100, 100);
        });
        
        btnOK.addActionListener(e -> {
            // 更新头像
            if (sidebarAvatar != null) {
                avatarManager.loadUserAvatar(sidebarAvatar, 80, 80);
            }
            if (profileAvatar != null) {
                avatarManager.loadUserAvatar(profileAvatar, 120, 120);
            }
            dispose();
        });
        
        btnCancel.addActionListener(e -> dispose());
    }
}

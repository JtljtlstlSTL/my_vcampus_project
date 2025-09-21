package com.vcampus.client.core.ui.student.view;

import com.vcampus.client.core.ui.student.model.StudentModel;
import com.vcampus.client.core.ui.student.AvatarManager;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

/**
 * 个人信息面板
 *
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class ProfilePanel extends JPanel {
    
    private StudentModel model;
    private StudentView parentView;
    private AvatarManager avatarManager;
    
    public ProfilePanel(StudentModel model, StudentView parentView) {
        this.model = model;
        this.parentView = parentView;
        this.avatarManager = new AvatarManager(model.getUserData());
        
        initUI();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 创建主内容面板
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        
        // 创建头像和信息面板
        JPanel avatarInfoPanel = new JPanel(new BorderLayout());
        avatarInfoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 头像区域
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel avatarLabel = new JLabel();
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setPreferredSize(new Dimension(120, 120));
        avatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 加载用户头像
        avatarManager.loadUserAvatar(avatarLabel, 120, 120);
        
        // 添加点击事件
        avatarLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 这里需要调用控制器的方法
                // parentView.getController().showAvatarSelectionDialog(null, avatarLabel);
            }
        });
        
        avatarPanel.add(avatarLabel);
        avatarInfoPanel.add(avatarPanel, BorderLayout.NORTH);
        
        // 创建信息显示面板
        JPanel infoPanel = createInfoPanel();
        JScrollPane scrollPane = new JScrollPane(infoPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        
        avatarInfoPanel.add(scrollPane, BorderLayout.CENTER);
        mainContentPanel.add(avatarInfoPanel, BorderLayout.CENTER);
        
        // 添加按钮区域
        JPanel buttonPanel = createButtonPanel();
        mainContentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainContentPanel, BorderLayout.CENTER);
    }
    
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "个人信息"
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        Map<String, Object> studentData = model.getCurrentStudentData();
        
        // 创建信息标签
        String[] labels = {"学号", "姓名", "性别", "专业", "班级", "入学年份", "联系方式", "邮箱"};
        String[] keys = {"cardNumber", "userName", "gender", "major", "className", "enrollmentYear", "phone", "email"};
        
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            JLabel label = new JLabel(labels[i] + ":");
            label.setFont(new Font("微软雅黑", Font.BOLD, 14));
            infoPanel.add(label, gbc);
            
            gbc.gridx = 1;
            String value = studentData.get(keys[i]) != null ? studentData.get(keys[i]).toString() : "未设置";
            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            valueLabel.setForeground(new Color(100, 100, 100));
            infoPanel.add(valueLabel, gbc);
        }
        
        return infoPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JButton btnEdit = new JButton("编辑信息");
        JButton btnChangePassword = new JButton("修改密码");
        JButton btnChangeAvatar = new JButton("更换头像");
        
        // 设置按钮样式
        Font buttonFont = new Font("微软雅黑", Font.PLAIN, 14);
        btnEdit.setFont(buttonFont);
        btnChangePassword.setFont(buttonFont);
        btnChangeAvatar.setFont(buttonFont);
        
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnChangePassword);
        buttonPanel.add(btnChangeAvatar);
        
        return buttonPanel;
    }
}

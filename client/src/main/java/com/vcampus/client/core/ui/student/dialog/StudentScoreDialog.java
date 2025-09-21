package com.vcampus.client.core.ui.student.dialog;

import com.vcampus.client.core.net.NettyClient;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

/**
 * 学生成绩查询对话框
 *
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class StudentScoreDialog extends JDialog {
    
    private NettyClient nettyClient;
    private String studentId;
    
    public StudentScoreDialog(JFrame parent, NettyClient nettyClient, String studentId) {
        super(parent, "成绩查询", true);
        this.nettyClient = nettyClient;
        this.studentId = studentId;
        
        initUI();
    }
    
    private void initUI() {
        setSize(600, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("成绩查询", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        add(titleLabel, BorderLayout.NORTH);
        
        // 成绩表格
        String[] columnNames = {"课程名称", "学期", "成绩", "GPA"};
        Object[][] data = {}; // 空数据，实际应该从服务器获取
        
        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnClose = new JButton("关闭");
        btnClose.addActionListener(e -> dispose());
        buttonPanel.add(btnClose);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}

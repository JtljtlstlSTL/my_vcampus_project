package com.vcampus.client.core.ui.student.dialog;

import com.vcampus.client.core.net.NettyClient;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

/**
 * 课程评价对话框
 *
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class CourseEvaluationDialog extends JDialog {
    
    private NettyClient nettyClient;
    private String studentId;
    
    public CourseEvaluationDialog(JFrame parent, NettyClient nettyClient, String studentId) {
        super(parent, "课程评价", true);
        this.nettyClient = nettyClient;
        this.studentId = studentId;
        
        initUI();
    }
    
    private void initUI() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        JLabel titleLabel = new JLabel("课程评价", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        add(titleLabel, BorderLayout.NORTH);
        
        // 评价表单
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 课程选择
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("选择课程:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> courseCombo = new JComboBox<>(new String[]{"请选择课程"});
        formPanel.add(courseCombo, gbc);
        
        // 评价内容
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("评价内容:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH;
        JTextArea evaluationText = new JTextArea(5, 20);
        evaluationText.setLineWrap(true);
        evaluationText.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(evaluationText), gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnSubmit = new JButton("提交评价");
        JButton btnClose = new JButton("关闭");
        
        btnSubmit.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "评价提交成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
        });
        
        btnClose.addActionListener(e -> dispose());
        
        buttonPanel.add(btnSubmit);
        buttonPanel.add(btnClose);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}

package com.vcampus.client.core.ui.library;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 高级搜索对话框
 * 提供多条件组合搜索功能
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class AdvancedSearchDialog extends JDialog {
    
    private BookManagementPanel parentPanel;
    private Map<String, String> searchParams;
    
    // 搜索字段
    private JTextField titleField;
    private JTextField authorField;
    private JTextField publisherField;
    
    // 按钮
    private JButton searchButton;
    private JButton clearButton;
    private JButton cancelButton;
    
    public AdvancedSearchDialog(JFrame parent, BookManagementPanel parentPanel) {
        super(parent, "高级搜索", true);
        this.parentPanel = parentPanel;
        this.searchParams = new HashMap<>();
        
        initUI();
        setupEventHandlers();
        
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 249, 250));
        
        // 创建标题面板
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // 创建搜索表单
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel titleLabel = new JLabel("高级搜索");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(new Color(52, 73, 94));
        panel.add(titleLabel, BorderLayout.WEST);
        
        JLabel descLabel = new JLabel("设置多个搜索条件进行精确查找");
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        descLabel.setForeground(new Color(149, 165, 166));
        panel.add(descLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // 书名
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("书名:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        titleField = createTextField();
        panel.add(titleField, gbc);
        
        // 作者
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("作者:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        authorField = createTextField();
        panel.add(authorField, gbc);
        
        // 出版社
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("出版社:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        publisherField = createTextField();
        panel.add(publisherField, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        searchButton = createStyledButton("搜索", new Color(52, 152, 219));
        searchButton.setPreferredSize(new Dimension(100, 35));
        panel.add(searchButton);
        
        clearButton = createStyledButton("清空", new Color(149, 165, 166));
        clearButton.setPreferredSize(new Dimension(100, 35));
        panel.add(clearButton);
        
        cancelButton = createStyledButton("取消", new Color(231, 76, 60));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        panel.add(cancelButton);
        
        return panel;
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        label.setForeground(new Color(44, 62, 80));
        label.setPreferredSize(new Dimension(80, 35));
        return label;
    }
    
    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        field.setPreferredSize(new Dimension(200, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(backgroundColor.darker());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }
    
    private void setupEventHandlers() {
        // 搜索按钮
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });
        
        // 清空按钮
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
        
        // 取消按钮
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    private void performSearch() {
        // 收集搜索参数
        searchParams.clear();
        
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String publisher = publisherField.getText().trim();
        
        // 检查是否有搜索条件
        if (title.isEmpty() && author.isEmpty() && publisher.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请至少输入一个搜索条件", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 实现模糊综合搜索 - 将所有条件合并为一个关键词
        StringBuilder keywordBuilder = new StringBuilder();
        if (!title.isEmpty()) {
            keywordBuilder.append(title).append(" ");
        }
        if (!author.isEmpty()) {
            keywordBuilder.append(author).append(" ");
        }
        if (!publisher.isEmpty()) {
            keywordBuilder.append(publisher).append(" ");
        }
        
        String keyword = keywordBuilder.toString().trim();
        searchParams.put("keyword", keyword);
        
        // 执行搜索
        parentPanel.performAdvancedSearch(searchParams);
        dispose();
    }
    
    private void clearFields() {
        titleField.setText("");
        authorField.setText("");
        publisherField.setText("");
    }
}

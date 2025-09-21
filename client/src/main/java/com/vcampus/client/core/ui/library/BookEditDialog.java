package com.vcampus.client.core.ui.library;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 图书编辑对话框
 * 用于添加和编辑图书信息
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class BookEditDialog extends JDialog {
    
    private NettyClient nettyClient;
    private Map<String, Object> bookData;
    private boolean isEditMode;
    private boolean bookSaved = false;
    
    // 表单字段
    private JTextField titleField;
    private JTextField authorField;
    private JTextField publisherField;
    private JTextField isbnField;
    private JTextField categoryField;
    private JTextField locationField;
    private JTextField publishDateField;
    private JSpinner totalQtySpinner;
    private JComboBox<String> statusCombo;
    
    // 按钮
    private JButton saveButton;
    private JButton cancelButton;
    
    public BookEditDialog(JFrame parent, String title, Map<String, Object> bookData, NettyClient nettyClient) {
        super(parent, title, true);
        this.bookData = bookData;
        this.isEditMode = bookData != null;
        this.nettyClient = nettyClient;
        
        initUI();
        setupEventHandlers();
        loadBookData();
        
        setSize(600, 700);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 249, 250));
        
        // 创建标题面板
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // 创建表单面板
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
        
        String title = isEditMode ? "编辑图书" : "添加图书";
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(new Color(52, 73, 94));
        panel.add(titleLabel, BorderLayout.WEST);
        
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
        
        // 书名（必填）
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("书名 *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        titleField = createTextField();
        panel.add(titleField, gbc);
        
        // 作者（必填）
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("作者 *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        authorField = createTextField();
        panel.add(authorField, gbc);
        
        // 出版社（必填）
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("出版社 *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        publisherField = createTextField();
        panel.add(publisherField, gbc);
        
        // ISBN（必填）
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("ISBN *:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        isbnField = createTextField();
        panel.add(isbnField, gbc);
        
        // 分类
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("分类:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        categoryField = createTextField();
        panel.add(categoryField, gbc);
        
        // 位置
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("位置:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        locationField = createTextField();
        panel.add(locationField, gbc);
        
        // 出版日期
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("出版日期:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        publishDateField = createTextField();
        publishDateField.setToolTipText("格式：YYYY-MM-DD，如：2023-01-01");
        panel.add(publishDateField, gbc);
        
        // 总数量
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("总数量:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        totalQtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        totalQtySpinner.setPreferredSize(new Dimension(200, 35));
        panel.add(totalQtySpinner, gbc);
        
        // 状态（固定为在库，不可修改）
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(createLabel("状态:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        statusCombo = new JComboBox<>(new String[]{"在库"});
        statusCombo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusCombo.setPreferredSize(new Dimension(200, 35));
        statusCombo.setEnabled(false); // 禁用状态修改
        panel.add(statusCombo, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        
        saveButton = createStyledButton("💾 保存", new Color(46, 204, 113));
        saveButton.setPreferredSize(new Dimension(100, 35));
        panel.add(saveButton);
        
        cancelButton = createStyledButton("取消", new Color(231, 76, 60));
        cancelButton.setPreferredSize(new Dimension(100, 35));
        panel.add(cancelButton);
        
        return panel;
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        label.setForeground(new Color(44, 62, 80));
        label.setPreferredSize(new Dimension(100, 35));
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
        // 保存按钮
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveBook();
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
    
    private void loadBookData() {
        if (isEditMode && bookData != null) {
            titleField.setText(getStringValue(bookData.get("title")));
            authorField.setText(getStringValue(bookData.get("author")));
            publisherField.setText(getStringValue(bookData.get("publisher")));
            isbnField.setText(getStringValue(bookData.get("isbn")));
            categoryField.setText(getStringValue(bookData.get("category")));
            locationField.setText(getStringValue(bookData.get("location")));
            publishDateField.setText(getStringValue(bookData.get("publishDate")));
            
            Object totalQty = bookData.get("totalQty");
            if (totalQty instanceof Number) {
                totalQtySpinner.setValue(((Number) totalQty).intValue());
            }
            
            String status = getStringValue(bookData.get("status"));
            if (!status.isEmpty()) {
                statusCombo.setSelectedItem(status);
            }
        }
    }
    
    private String getStringValue(Object value) {
        return value != null ? value.toString() : "";
    }
    
    private void saveBook() {
        // 验证必填字段
        if (!validateFields()) {
            return;
        }
        
        // 收集表单数据
        Map<String, String> formData = collectFormData();
        
        // 发送请求
        if (nettyClient != null) {
            performSave(formData);
        } else {
            JOptionPane.showMessageDialog(this, "网络连接不可用", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validateFields() {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "书名不能为空", "验证错误", JOptionPane.ERROR_MESSAGE);
            titleField.requestFocus();
            return false;
        }
        
        if (authorField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "作者不能为空", "验证错误", JOptionPane.ERROR_MESSAGE);
            authorField.requestFocus();
            return false;
        }
        
        if (publisherField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "出版社不能为空", "验证错误", JOptionPane.ERROR_MESSAGE);
            publisherField.requestFocus();
            return false;
        }
        
        if (isbnField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "ISBN不能为空", "验证错误", JOptionPane.ERROR_MESSAGE);
            isbnField.requestFocus();
            return false;
        }
        
        // 验证ISBN格式
        if (!isValidIsbn(isbnField.getText().trim())) {
            JOptionPane.showMessageDialog(this, "ISBN格式不正确，请输入10位或13位数字", "验证错误", JOptionPane.ERROR_MESSAGE);
            isbnField.requestFocus();
            return false;
        }
        
        // 验证出版日期格式
        String publishDate = publishDateField.getText().trim();
        if (!publishDate.isEmpty() && !isValidDate(publishDate)) {
            JOptionPane.showMessageDialog(this, "出版日期格式不正确，请使用YYYY-MM-DD格式", "验证错误", JOptionPane.ERROR_MESSAGE);
            publishDateField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private boolean isValidIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return false;
        }
        String cleanIsbn = isbn.trim().replaceAll("[^0-9X]", "");
        return cleanIsbn.length() == 10 || cleanIsbn.length() == 13;
    }
    
    private boolean isValidDate(String date) {
        try {
            LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    private Map<String, String> collectFormData() {
        Map<String, String> formData = new HashMap<>();
        formData.put("title", titleField.getText().trim());
        formData.put("author", authorField.getText().trim());
        formData.put("publisher", publisherField.getText().trim());
        formData.put("isbn", isbnField.getText().trim());
        formData.put("category", categoryField.getText().trim());
        formData.put("location", locationField.getText().trim());
        formData.put("publishDate", publishDateField.getText().trim());
        formData.put("totalQty", totalQtySpinner.getValue().toString());
        formData.put("status", "在库"); // 固定状态为在库
        return formData;
    }
    
    private void performSave(Map<String, String> formData) {
        // 在后台线程中执行保存
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    log.info("开始保存图书，模式: {}", isEditMode ? "编辑" : "添加");
                    
                    // 构建请求
                    String endpoint = isEditMode ? "library/admin/book/update" : "library/admin/book/add";
                    Request request = new Request(endpoint);
                    
                    // 添加参数
                    for (Map.Entry<String, String> entry : formData.entrySet()) {
                        request.addParam(entry.getKey(), entry.getValue());
                    }
                    
                    // 如果是编辑模式，添加图书ID
                    if (isEditMode && bookData != null) {
                        request.addParam("bookId", bookData.get("id").toString());
                    }
                    
                    // 发送请求
                    Response response = nettyClient.sendRequest(request).get(10, TimeUnit.SECONDS);
                    
                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, 
                                isEditMode ? "图书信息更新成功！" : "图书添加成功！", 
                                "成功", 
                                JOptionPane.INFORMATION_MESSAGE);
                            bookSaved = true;
                            dispose();
                        });
                        log.info("图书保存成功，模式: {}", isEditMode ? "编辑" : "添加");
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "保存失败：" + errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                        });
                        log.warn("图书保存失败: {}", errorMsg);
                    }
                    
                } catch (Exception e) {
                    log.error("保存图书时发生错误", e);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "保存时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        });
    }
    
    public boolean isBookSaved() {
        return bookSaved;
    }
}
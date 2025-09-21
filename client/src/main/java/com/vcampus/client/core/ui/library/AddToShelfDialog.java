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
import java.awt.Window;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;

import lombok.extern.slf4j.Slf4j;

/**
 * 添加到书架对话框
 * 允许用户选择现有分类或创建新分类来添加图书到书架
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class AddToShelfDialog extends JDialog {
    
    private final NettyClient nettyClient;
    private final Map<String, Object> userData;
    private final String bookId;
    private final String bookTitle;
    
    // UI组件
    private JComboBox<String> categoryCombo;
    private JTextField newCategoryField;
    private JButton addButton;
    private JButton cancelButton;
    private JRadioButton existingCategoryRadio;
    private JRadioButton newCategoryRadio;
    
    public AddToShelfDialog(Window parent, NettyClient nettyClient, Map<String, Object> userData, 
                           String bookId, String bookTitle) {
        super(parent, "添加到书架", ModalityType.APPLICATION_MODAL);
        this.nettyClient = nettyClient;
        this.userData = userData;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        
        initUI();
        loadUserCategories();
        setupEventHandlers();
    }
    
    private void initUI() {
        setSize(450, 400); // 增加高度从300到400
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 标题
        JLabel titleLabel = new JLabel("将《" + bookTitle + "》添加到书架");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 内容面板
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // 按钮面板
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 分类选择
        JLabel categoryLabel = new JLabel("选择分类：");
        categoryLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(categoryLabel, gbc);
        
        // 现有分类单选按钮
        existingCategoryRadio = new JRadioButton("使用现有分类", true);
        existingCategoryRadio.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(existingCategoryRadio, gbc);
        
        // 分类下拉框
        categoryCombo = new JComboBox<>();
        categoryCombo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        categoryCombo.setPreferredSize(new Dimension(200, 30));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(categoryCombo, gbc);
        
        // 新分类单选按钮
        newCategoryRadio = new JRadioButton("创建新分类", false);
        newCategoryRadio.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(newCategoryRadio, gbc);
        
        // 新分类输入框
        newCategoryField = new JTextField();
        newCategoryField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        newCategoryField.setPreferredSize(new Dimension(200, 30));
        newCategoryField.setEnabled(false);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(newCategoryField, gbc);
        
        // 按钮组
        ButtonGroup categoryGroup = new ButtonGroup();
        categoryGroup.add(existingCategoryRadio);
        categoryGroup.add(newCategoryRadio);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panel.setBackground(Color.WHITE);
        
        addButton = new JButton("添加");
        addButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        addButton.setPreferredSize(new Dimension(80, 35));
        addButton.setBackground(new Color(52, 152, 219));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorderPainted(false);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        cancelButton = new JButton("取消");
        cancelButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        cancelButton.setPreferredSize(new Dimension(80, 35));
        cancelButton.setBackground(new Color(149, 165, 166));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panel.add(addButton);
        panel.add(cancelButton);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // 分类选择切换
        existingCategoryRadio.addActionListener(e -> {
            categoryCombo.setEnabled(true);
            newCategoryField.setEnabled(false);
        });
        
        newCategoryRadio.addActionListener(e -> {
            categoryCombo.setEnabled(false);
            newCategoryField.setEnabled(true);
            newCategoryField.requestFocus();
        });
        
        // 添加按钮
        addButton.addActionListener(e -> addToShelf());
        
        // 取消按钮
        cancelButton.addActionListener(e -> dispose());
        
        // 回车键添加
        getRootPane().setDefaultButton(addButton);
    }
    
    private void loadUserCategories() {
        // 在后台线程中加载用户分类
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    // 构建请求
                    Request request = new Request("library/bookshelf/categories");
                    
                    // 发送请求
                    CompletableFuture<Response> future = nettyClient.sendRequest(request);
                    Response response = future.get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<String> categories = (List<String>) response.getData();
                        
                        // 更新UI
                        SwingUtilities.invokeLater(() -> {
                            categoryCombo.removeAllItems();
                            for (String category : categories) {
                                categoryCombo.addItem(category);
                            }
                            if (categories.isEmpty()) {
                                // 如果没有现有分类，默认选择创建新分类
                                newCategoryRadio.setSelected(true);
                                categoryCombo.setEnabled(false);
                                newCategoryField.setEnabled(true);
                            }
                        });
                    }
                } catch (Exception e) {
                    System.err.println("加载用户分类失败: " + e.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "加载分类失败：" + e.getMessage(), 
                                "错误", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        });
    }
    
    private void addToShelf() {
        String categoryName;
        
        if (existingCategoryRadio.isSelected()) {
            categoryName = (String) categoryCombo.getSelectedItem();
            if (categoryName == null || categoryName.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请选择一个分类", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else {
            categoryName = newCategoryField.getText().trim();
            if (categoryName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入新分类名称", "提示", JOptionPane.WARNING_MESSAGE);
                newCategoryField.requestFocus();
                return;
            }
        }
        
        // 在后台线程中添加图书到书架
        addButton.setEnabled(false);
        addButton.setText("添加中...");
        
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    // 构建请求
                    Request request = new Request("library/bookshelf/add")
                            .addParam("bookId", bookId)
                            .addParam("categoryName", categoryName);
                    
                    // 发送请求
                    CompletableFuture<Response> future = nettyClient.sendRequest(request);
                    Response response = future.get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    SwingUtilities.invokeLater(() -> {
                        addButton.setEnabled(true);
                        addButton.setText("添加");
                        
                        if (response != null && response.isSuccess()) {
                            JOptionPane.showMessageDialog(this, "图书已成功添加到书架！", 
                                    "成功", JOptionPane.INFORMATION_MESSAGE);
                            dispose();
                        } else {
                            String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                            JOptionPane.showMessageDialog(this, "添加失败：" + errorMsg, 
                                    "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                } catch (Exception e) {
                    System.err.println("添加图书到书架失败: " + e.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        addButton.setEnabled(true);
                        addButton.setText("添加");
                        JOptionPane.showMessageDialog(this, "添加失败：" + e.getMessage(), 
                                "错误", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        });
    }
}

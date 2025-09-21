package com.vcampus.client.core.ui.library;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 图书荐购面板
 * 供学生和教师提交图书荐购申请并查看历史记录
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class BookRecommendationPanel extends JPanel implements LibraryPanel.RefreshablePanel {
    
    private final NettyClient nettyClient;
    private final Map<String, Object> userData;
    
    // 主面板组件
    private JPanel mainPanel;
    private JPanel formPanel;
    private JPanel historyPanel;
    
    // 荐购申请表单组件
    private JTextField titleField;
    private JTextField authorField;
    private JTextField publisherField;
    private JTextField isbnField;
    private JComboBox<String> categoryComboBox;
    private JSpinner quantitySpinner;
    private JTextArea reasonArea;
    private JButton submitButton;
    private JButton closeFormButton;
    
    // 荐购历史表格组件
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JScrollPane historyScrollPane;
    private JButton newApplicationButton;
    private JButton cancelApplicationButton;
    
    // 图书分类选项
    private static final String[] BOOK_CATEGORIES = {
        "A - 马克思主义、列宁主义、毛泽东思想、邓小平理论",
        "B - 哲学、宗教",
        "C - 社会科学总论",
        "D - 政治、法律",
        "E - 军事",
        "F - 经济",
        "G - 文化、科学、教育、体育",
        "H - 语言、文字",
        "I - 文学",
        "J - 艺术",
        "K - 历史、地理",
        "L - 教育",
        "M - 音乐",
        "N - 自然科学总论",
        "O - 数理科学和化学",
        "P - 天文学、地球科学",
        "Q - 生物科学",
        "R - 医药、卫生",
        "S - 农业科学",
        "T - 工业技术",
        "U - 交通运输",
        "V - 航空、航天",
        "W - 环境科学、安全科学",
        "X - 综合性图书",
        "Y - 其他",
        "Z - 综合性图书"
    };
    
    public BookRecommendationPanel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        
        initUI();
        setupEventHandlers();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 40, 15, 40));
        setBackground(new Color(248, 249, 250));
        
        // 创建主面板，使用水平布局
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 249, 250));
        mainPanel.setPreferredSize(new Dimension(1400, 0)); // 设置主面板最小宽度
        
        // 左侧：荐购历史表格
        historyPanel = createHistoryPanel();
        mainPanel.add(historyPanel, BorderLayout.CENTER);
        
        // 右侧：新申请表单（初始化时隐藏）
        formPanel = createRecommendationForm();
        formPanel.setPreferredSize(new Dimension(400, 0));
        formPanel.setVisible(false); // 初始化时隐藏表单
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * 创建荐购申请表单
     */
    private JPanel createRecommendationForm() {
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        formPanel.setPreferredSize(new Dimension(1000, 0)); // 设置表单面板最小宽度
        
        // 创建标题栏
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        
        JLabel formTitleLabel = new JLabel("新荐购申请", JLabel.LEFT);
        formTitleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        formTitleLabel.setForeground(new Color(52, 58, 64));
        titlePanel.add(formTitleLabel, BorderLayout.WEST);
        
        closeFormButton = createStyledButton("关闭", new Color(108, 117, 125), Color.WHITE);
        closeFormButton.setPreferredSize(new Dimension(60, 25));
        closeFormButton.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        closeFormButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                closeFormButton.setBackground(new Color(73, 80, 87));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                closeFormButton.setBackground(new Color(108, 117, 125));
            }
        });
        titlePanel.add(closeFormButton, BorderLayout.EAST);
        
        formPanel.add(titlePanel, BorderLayout.NORTH);
        
        // 创建表单字段
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 12, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL; // 让组件水平填充
        gbc.weightx = 1.0; // 让输入框占满剩余空间
        gbc.weighty = 0.0; // 其他字段不垂直扩展
        
        // 图书标题
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.0; // 标签不扩展
        fieldsPanel.add(new JLabel("图书标题 *:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; // 输入框占满剩余空间
        gbc.weighty = 0.0; // 输入框不垂直扩展
        titleField = new JTextField();
        titleField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        fieldsPanel.add(titleField, gbc);
        
        // 作者
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.0; // 标签不扩展
        fieldsPanel.add(new JLabel("作者 *:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; // 输入框占满剩余空间
        authorField = new JTextField();
        authorField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        fieldsPanel.add(authorField, gbc);
        
        // 出版社
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.0; // 标签不扩展
        fieldsPanel.add(new JLabel("出版社 *:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; // 输入框占满剩余空间
        publisherField = new JTextField();
        publisherField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        fieldsPanel.add(publisherField, gbc);
        
        // ISBN
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.weightx = 0.0; // 标签不扩展
        fieldsPanel.add(new JLabel("ISBN *:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; // 输入框占满剩余空间
        isbnField = new JTextField();
        isbnField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        fieldsPanel.add(isbnField, gbc);
        
        // 图书分类
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.weightx = 0.0; // 标签不扩展
        fieldsPanel.add(new JLabel("图书分类 *:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; // 输入框占满剩余空间
        categoryComboBox = new JComboBox<>(BOOK_CATEGORIES);
        categoryComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        // categoryComboBox.setPreferredSize(new Dimension(500, 25)); // 移除固定尺寸，让它自动填充
        fieldsPanel.add(categoryComboBox, gbc);
        
        // 荐购数量
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.weightx = 0.0; // 标签不扩展
        fieldsPanel.add(new JLabel("荐购数量 *:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; // 输入框占满剩余空间
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        quantitySpinner.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        // quantitySpinner.setPreferredSize(new Dimension(300, 25)); // 移除固定尺寸，让它自动填充
        fieldsPanel.add(quantitySpinner, gbc);
        
        // 荐购理由
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0.0; // 标签不扩展
        gbc.weighty = 0.0; // 标签不垂直扩展
        gbc.insets = new Insets(12, 10, 2, 10); // 进一步减小下边距，从5减小到2
        fieldsPanel.add(new JLabel("荐购理由 *:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; // 输入框占满剩余空间
        gbc.weighty = 1.0; // 输入框垂直扩展，占满剩余空间
        gbc.fill = GridBagConstraints.BOTH; // 水平和垂直都填充
        reasonArea = new JTextArea(25, 0); // 增加行数，让荐购理由输入框更高
        reasonArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        JScrollPane reasonScrollPane = new JScrollPane(reasonArea);
        // reasonScrollPane.setPreferredSize(new Dimension(0, 350)); // 移除固定高度，让它自动填充
        fieldsPanel.add(reasonScrollPane, gbc);
        
        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 3));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0)); // 进一步减小上边距，从5减小到2
        
        submitButton = createStyledButton("提交荐购申请", new Color(0, 123, 255), Color.WHITE);
        submitButton.setPreferredSize(new Dimension(120, 40));
        buttonPanel.add(submitButton);
        
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return formPanel;
    }
    
    /**
     * 创建荐购历史面板
     */
    private JPanel createHistoryPanel() {
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(Color.WHITE);
        historyPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // 创建工具栏
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setBackground(Color.WHITE);
        
        // 左侧标题
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("荐购历史", JLabel.LEFT);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(new Color(52, 58, 64));
        leftPanel.add(titleLabel);
        
        // 右侧按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightPanel.setBackground(Color.WHITE);
        
        newApplicationButton = createStyledButton("新申请", new Color(40, 167, 69), Color.WHITE);
        newApplicationButton.setPreferredSize(new Dimension(100, 35));
        newApplicationButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        
        cancelApplicationButton = createStyledButton("撤销申请", new Color(220, 53, 69), Color.WHITE);
        cancelApplicationButton.setPreferredSize(new Dimension(100, 35));
        cancelApplicationButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        
        // 添加悬停效果
        newApplicationButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                newApplicationButton.setBackground(new Color(34, 139, 34));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                newApplicationButton.setBackground(new Color(40, 167, 69));
            }
        });
        
        cancelApplicationButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cancelApplicationButton.setBackground(new Color(185, 28, 28));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                cancelApplicationButton.setBackground(new Color(220, 53, 69));
            }
        });
        
        rightPanel.add(newApplicationButton);
        rightPanel.add(cancelApplicationButton);
        
        toolbarPanel.add(leftPanel, BorderLayout.WEST);
        toolbarPanel.add(rightPanel, BorderLayout.EAST);
        
        historyPanel.add(toolbarPanel, BorderLayout.NORTH);
        
        // 创建历史记录表格
        String[] columnNames = {
            "荐购ID", "图书标题", "作者", "出版社", "ISBN", "分类", 
            "数量", "状态", "管理员反馈", "处理时间"
        };
        
        historyTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可编辑
            }
        };
        
        historyTable = new JTable(historyTableModel);
        historyTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        historyTable.setRowHeight(30);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setGridColor(new Color(220, 220, 220));
        historyTable.setShowGrid(true);
        historyTable.setIntercellSpacing(new Dimension(0, 0));
        
        // 美化表头
        historyTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        historyTable.getTableHeader().setBackground(new Color(52, 58, 64));
        historyTable.getTableHeader().setForeground(Color.WHITE);
        historyTable.getTableHeader().setPreferredSize(new Dimension(0, 35));
        
        // 设置交替行颜色
        historyTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(new Color(248, 249, 250));
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                    // 统一设置文字颜色为黑色
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(new Color(0, 123, 255));
                    c.setForeground(Color.WHITE);
                }
                
                return c;
            }
        });
        
        // 设置列宽
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // 荐购ID
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(150); // 图书标题
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(100); // 作者
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(120); // 出版社
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(120); // ISBN
        historyTable.getColumnModel().getColumn(5).setPreferredWidth(50);  // 分类
        historyTable.getColumnModel().getColumn(6).setPreferredWidth(50);  // 数量
        historyTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // 状态
        historyTable.getColumnModel().getColumn(8).setPreferredWidth(150); // 管理员反馈
        historyTable.getColumnModel().getColumn(9).setPreferredWidth(120); // 处理时间
        
        historyScrollPane = new JScrollPane(historyTable);
        historyScrollPane.setPreferredSize(new Dimension(1000, 400));
        historyScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        historyScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        historyScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // 美化滚动条
        historyScrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(200, 200, 200);
                this.trackColor = new Color(248, 249, 250);
            }
        });
        
        historyPanel.add(historyScrollPane, BorderLayout.CENTER);
        
        return historyPanel;
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 提交荐购申请
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitRecommendation();
            }
        });
        
        // 关闭申请表单
        closeFormButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideFormPanel();
            }
        });
        
        // 新申请按钮
        newApplicationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFormPanel();
            }
        });
        
        // 撤销申请按钮
        cancelApplicationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelRecommendation();
            }
        });
    }
    
    /**
     * 显示申请表单面板
     */
    private void showFormPanel() {
        formPanel.setVisible(true);
        mainPanel.add(formPanel, BorderLayout.EAST);
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    /**
     * 隐藏申请表单面板
     */
    private void hideFormPanel() {
        formPanel.setVisible(false);
        mainPanel.remove(formPanel);
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    /**
     * 撤销荐购申请
     */
    private void cancelRecommendation() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要撤销的荐购申请", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 检查状态是否为待审核
        String status = (String) historyTableModel.getValueAt(selectedRow, 7); // 状态列现在是第8列（索引7）
        if (!"待审核".equals(status)) {
            JOptionPane.showMessageDialog(this, "只能撤销待审核状态的荐购申请", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 确认撤销
        int result = JOptionPane.showConfirmDialog(this, 
            "确定要撤销选中的荐购申请吗？", 
            "确认撤销", 
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            // 获取荐购ID - 从表格数据中获取（第一列是荐购ID）
            Object recIdObj = historyTableModel.getValueAt(selectedRow, 0);
            if (recIdObj == null) {
                JOptionPane.showMessageDialog(this, "无法获取荐购ID，请刷新后重试", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                // 尝试将recId转换为整数
                Integer recId;
                if (recIdObj instanceof Number) {
                    recId = ((Number) recIdObj).intValue();
                } else {
                    recId = Integer.valueOf(recIdObj.toString());
                }
                
                Request request = new Request();
                request.setUri("/library/recommend/cancel");
                request.addParam("recId", recId.toString());
                
                CompletableFuture<Response> future = nettyClient.sendRequest(request);
                future.thenAccept(response -> {
                    SwingUtilities.invokeLater(() -> {
                        if (response.isSuccess()) {
                            JOptionPane.showMessageDialog(this, "荐购申请撤销成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                            loadRecommendationHistory();
                        } else {
                            JOptionPane.showMessageDialog(this, "撤销失败：" + response.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }).exceptionally(throwable -> {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "网络错误：" + throwable.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    });
                    return null;
                });
                
            } catch (Exception e) {
                log.error("撤销荐购申请时发生错误", e);
                JOptionPane.showMessageDialog(this, "撤销荐购申请时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 提交荐购申请
     */
    private void submitRecommendation() {
        try {
            // 验证表单
            if (!validateForm()) {
                return;
            }
            
            // 构建请求
            Request request = new Request();
            request.setUri("/library/recommend/submit");
            request.addParam("bookTitle", titleField.getText().trim());
            request.addParam("bookAuthor", authorField.getText().trim());
            request.addParam("bookPublisher", publisherField.getText().trim());
            request.addParam("bookIsbn", isbnField.getText().trim());
            // 从带中文解释的字符串中提取分类代码
            String selectedCategory = (String) categoryComboBox.getSelectedItem();
            String categoryCode = selectedCategory != null ? selectedCategory.split(" - ")[0] : "";
            request.addParam("bookCategory", categoryCode);
            request.addParam("recommendQty", quantitySpinner.getValue().toString());
            request.addParam("recommendReason", reasonArea.getText().trim());
            
            // 发送请求
            CompletableFuture<Response> future = nettyClient.sendRequest(request);
            future.thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(this, "荐购申请提交成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        clearForm();
                        // 隐藏表单面板并刷新历史记录
                        hideFormPanel();
                        loadRecommendationHistory();
                    } else {
                        JOptionPane.showMessageDialog(this, "荐购申请提交失败：" + response.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "网络错误：" + throwable.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
            
        } catch (Exception e) {
            log.error("提交荐购申请时发生错误", e);
            JOptionPane.showMessageDialog(this, "提交荐购申请时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 验证表单
     */
    private boolean validateForm() {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入图书标题", "验证错误", JOptionPane.WARNING_MESSAGE);
            titleField.requestFocus();
            return false;
        }
        
        if (authorField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入作者", "验证错误", JOptionPane.WARNING_MESSAGE);
            authorField.requestFocus();
            return false;
        }
        
        if (publisherField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入出版社", "验证错误", JOptionPane.WARNING_MESSAGE);
            publisherField.requestFocus();
            return false;
        }
        
        if (isbnField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入ISBN", "验证错误", JOptionPane.WARNING_MESSAGE);
            isbnField.requestFocus();
            return false;
        }
        
        if (reasonArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入荐购理由", "验证错误", JOptionPane.WARNING_MESSAGE);
            reasonArea.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * 清空表单
     */
    private void clearForm() {
        titleField.setText("");
        authorField.setText("");
        publisherField.setText("");
        isbnField.setText("");
        categoryComboBox.setSelectedIndex(0);
        quantitySpinner.setValue(1);
        reasonArea.setText("");
    }
    
    /**
     * 加载荐购历史记录
     */
    private void loadRecommendationHistory() {
        try {
            // 构建请求
            Request request = new Request();
            request.setUri("/library/recommend/history");
            
            // 发送请求
            CompletableFuture<Response> future = nettyClient.sendRequest(request);
            future.thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        updateHistoryTable(response.getData());
                    } else {
                        JOptionPane.showMessageDialog(this, "加载荐购历史失败：" + response.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "网络错误：" + throwable.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
            
        } catch (Exception e) {
            log.error("加载荐购历史时发生错误", e);
            JOptionPane.showMessageDialog(this, "加载荐购历史时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 更新历史记录表格
     */
    @SuppressWarnings("unchecked")
    private void updateHistoryTable(Object data) {
        historyTableModel.setRowCount(0); // 清空表格
        
        if (data instanceof List) {
            List<Map<String, Object>> historyList = (List<Map<String, Object>>) data;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            for (Map<String, Object> record : historyList) {
                // 处理数量显示为整数
                Object recommendQty = record.get("recommendQty");
                String quantityDisplay = recommendQty != null ? String.valueOf(((Number) recommendQty).intValue()) : "0";
                
                // 处理荐购ID显示为整数
                Object recId = record.get("recId");
                String recIdDisplay = recId != null ? String.valueOf(((Number) recId).intValue()) : "0";
                
                Object[] row = {
                    recIdDisplay,
                    record.get("bookTitle"),
                    record.get("bookAuthor"),
                    record.get("bookPublisher"),
                    record.get("bookIsbn"),
                    record.get("bookCategory"),
                    quantityDisplay,
                    getStatusDescription((String) record.get("status")),
                    record.get("adminFeedback"),
                    record.get("processTime")
                };
                historyTableModel.addRow(row);
            }
        }
    }
    
    /**
     * 获取状态描述
     */
    private String getStatusDescription(String status) {
        if (status == null) return "未知";
        
        switch (status) {
            case "PENDING": return "待审核";
            case "APPROVED": return "已通过";
            case "REJECTED": return "已拒绝";
            case "PURCHASED": return "已采购";
            default: return "未知";
        }
    }
    
    /**
     * 创建样式化按钮
     */
    private JButton createStyledButton(String text, Color backgroundColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 12));
        button.setBackground(backgroundColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }
    
    @Override
    public void refresh() {
        // 刷新荐购历史记录
        loadRecommendationHistory();
    }
}

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 借阅管理面板
 * 管理员专用，管理所有用户的借阅记录
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class BorrowManagementPanel extends JPanel implements LibraryPanel.RefreshablePanel {
    
    private final NettyClient nettyClient;
    private final Map<String, Object> userData;
    
    // 搜索组件
    private JTextField userSearchField;
    private JTextField bookSearchField;
    private JComboBox<String> statusCombo;
    
    // 日期搜索组件
    private JComboBox<String> yearCombo;
    private JComboBox<String> monthCombo;
    private JComboBox<String> dayCombo;
    private JComboBox<String> dateTypeCombo; // 日期类型：借阅日期/应还日期
    
    private JButton searchButton;
    
    // 右键菜单
    private JPopupMenu contextMenu;
    private JMenuItem refreshMenuItem;
    private JMenuItem clearMenuItem;
    
    // 表格组件
    private JTable borrowTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    
    // 操作按钮（已删除逾期处理功能）
    
    // 状态标签
    private JLabel statusLabel;
    
    public BorrowManagementPanel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        
        initUI();
        setupEventHandlers();
        loadBorrowRecords();
    }
    
    /**
     * 创建年份选择器
     */
    private JComboBox<String> createYearCombo() {
        String[] years = new String[11];
        int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
        years[0] = "全部";
        for (int i = 1; i <= 10; i++) {
            years[i] = String.valueOf(currentYear - 5 + i);
        }
        JComboBox<String> combo = new JComboBox<>(years);
        combo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        combo.setPreferredSize(new Dimension(90, 32));
        combo.setMaximumRowCount(10); // 限制下拉列表显示的行数
        return combo;
    }
    
    /**
     * 创建月份选择器
     */
    private JComboBox<String> createMonthCombo() {
        String[] months = {"全部", "01", "02", "03", "04", "05", "06", 
                          "07", "08", "09", "10", "11", "12"};
        JComboBox<String> combo = new JComboBox<>(months);
        combo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        combo.setPreferredSize(new Dimension(80, 32));
        combo.setMaximumRowCount(10); // 限制下拉列表显示的行数
        return combo;
    }
    
    /**
     * 创建日期选择器
     */
    private JComboBox<String> createDayCombo() {
        String[] days = new String[32];
        days[0] = "全部";
        for (int i = 1; i <= 31; i++) {
            days[i] = String.format("%02d", i);
        }
        JComboBox<String> combo = new JComboBox<>(days);
        combo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        combo.setPreferredSize(new Dimension(80, 32));
        combo.setMaximumRowCount(10); // 限制下拉列表显示的行数
        return combo;
    }
    
    /**
     * 构建日期字符串
     * @param year 年份
     * @param month 月份
     * @param day 日期
     * @return 格式化的日期字符串，如果任何部分为"全部"则返回空字符串
     */
    private String buildDateString(String year, String month, String day) {
        // 如果年份为"全部"，直接返回空字符串
        if ("全部".equals(year)) {
            return "";
        }
        
        StringBuilder dateStr = new StringBuilder();
        dateStr.append(year);
        
        // 如果月份为"全部"，只返回年份
        if ("全部".equals(month)) {
            return dateStr.toString();
        }
        
        dateStr.append("-").append(month);
        
        // 如果日期为"全部"，返回年月
        if ("全部".equals(day)) {
            return dateStr.toString();
        }
        
        dateStr.append("-").append(day);
        return dateStr.toString();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 创建标题和状态面板
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // 创建搜索面板
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.CENTER);
        
        // 创建操作面板
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel titleLabel = new JLabel("借阅管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.WEST);
        
        statusLabel = new JLabel("正在加载...");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);
        panel.add(statusLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(248, 249, 250));
        
        // 创建搜索栏 - 使用GridBagLayout进行更精确的布局控制
        JPanel searchBar = new JPanel(new GridBagLayout());
        searchBar.setBackground(new Color(255, 255, 255));
        searchBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 第一行：用户搜索和图书搜索
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel userLabel = new JLabel("用户:");
        userLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        userLabel.setForeground(new Color(52, 73, 94));
        searchBar.add(userLabel, gbc);
        
        gbc.gridx = 1;
        userSearchField = new JTextField(8);
        userSearchField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        userSearchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        userSearchField.setPreferredSize(new Dimension(100, 32));
        searchBar.add(userSearchField, gbc);
        
        gbc.gridx = 2;
        JLabel bookLabel = new JLabel("图书:");
        bookLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        bookLabel.setForeground(new Color(52, 73, 94));
        searchBar.add(bookLabel, gbc);
        
        gbc.gridx = 3;
        bookSearchField = new JTextField(8);
        bookSearchField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        bookSearchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        bookSearchField.setPreferredSize(new Dimension(100, 32));
        searchBar.add(bookSearchField, gbc);
        
        gbc.gridx = 4;
        JLabel statusLabel = new JLabel("状态:");
        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        statusLabel.setForeground(new Color(52, 73, 94));
        searchBar.add(statusLabel, gbc);
        
        gbc.gridx = 5;
        statusCombo = new JComboBox<>(new String[]{"全部", "已归还", "逾期", "续借"});
        statusCombo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        statusCombo.setPreferredSize(new Dimension(80, 32));
        searchBar.add(statusCombo, gbc);
        
        // 第二行：日期搜索
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel dateLabel = new JLabel("按日期搜索:");
        dateLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        dateLabel.setForeground(new Color(52, 73, 94));
        searchBar.add(dateLabel, gbc);
        
        // 年份选择
        gbc.gridx = 1;
        yearCombo = createYearCombo();
        searchBar.add(yearCombo, gbc);
        
        // 月份选择
        gbc.gridx = 2;
        monthCombo = createMonthCombo();
        searchBar.add(monthCombo, gbc);
        
        // 日期选择
        gbc.gridx = 3;
        dayCombo = createDayCombo();
        searchBar.add(dayCombo, gbc);
        
        // 日期类型选择
        gbc.gridx = 4;
        JLabel dateTypeLabel = new JLabel("搜索类型:");
        dateTypeLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        dateTypeLabel.setForeground(new Color(52, 73, 94));
        searchBar.add(dateTypeLabel, gbc);
        
        gbc.gridx = 5;
        dateTypeCombo = new JComboBox<>(new String[]{"借阅日期", "应还日期", "归还日期"});
        dateTypeCombo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        dateTypeCombo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        dateTypeCombo.setPreferredSize(new Dimension(120, 32));
        dateTypeCombo.setMaximumRowCount(10); // 限制下拉列表显示的行数
        searchBar.add(dateTypeCombo, gbc);
        
        // 按钮区域 - 放在右侧
        gbc.gridx = 6; gbc.gridy = 1; gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(new Color(255, 255, 255));
        
        searchButton = new JButton("搜索");
        searchButton.setPreferredSize(new Dimension(80, 32));
        searchButton.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));
        searchButton.setBorderPainted(false);
        searchButton.setFocusPainted(false);
        searchButton.setBackground(new Color(52, 152, 219));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonPanel.add(searchButton);
        
        searchBar.add(buttonPanel, gbc);
        
        // 创建表格
        String[] columnNames = {"用户ID", "用户名", "书名", "借阅日期", "应还日期", "归还日期", "状态"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可编辑
            }
        };
        
        borrowTable = new JTable(tableModel);
        borrowTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        borrowTable.setRowHeight(35);
        borrowTable.getTableHeader().setReorderingAllowed(false);
        
        // 设置表格样式
        borrowTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        borrowTable.setGridColor(new Color(220, 220, 220));
        borrowTable.setShowGrid(true);
        borrowTable.setIntercellSpacing(new Dimension(0, 0));
        
        // 设置表头样式
        borrowTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        borrowTable.getTableHeader().setBackground(new Color(52, 73, 94));
        borrowTable.getTableHeader().setForeground(Color.WHITE);
        borrowTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        
        // 设置行选择样式
        borrowTable.setSelectionBackground(new Color(52, 152, 219));
        borrowTable.setSelectionForeground(Color.WHITE);
        
        // 设置列宽
        borrowTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // 用户ID
        borrowTable.getColumnModel().getColumn(1).setPreferredWidth(120); // 用户名
        borrowTable.getColumnModel().getColumn(2).setPreferredWidth(200); // 书名
        borrowTable.getColumnModel().getColumn(3).setPreferredWidth(120); // 借阅日期
        borrowTable.getColumnModel().getColumn(4).setPreferredWidth(120); // 应还日期
        borrowTable.getColumnModel().getColumn(5).setPreferredWidth(120); // 归还日期
        borrowTable.getColumnModel().getColumn(6).setPreferredWidth(100); // 状态
        
        scrollPane = new JScrollPane(borrowTable);
        scrollPane.setPreferredSize(new Dimension(0, 400));
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        scrollPane.setBackground(Color.WHITE);
        
        panel.add(searchBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 创建右键菜单（在scrollPane创建之后）
        createContextMenu();
        
        return panel;
    }
    
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.setBackground(new Color(248, 249, 250));
        
        // 逾期处理功能已删除
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // 搜索按钮
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchBorrowRecords();
            }
        });
        
        
        
        // 逾期处理功能已删除
        
        // 表格选择事件
        borrowTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // 回车键搜索
        userSearchField.addActionListener(e -> searchBorrowRecords());
        bookSearchField.addActionListener(e -> searchBorrowRecords());
        
        // 日期选择器变化时不自动搜索，只通过搜索按钮触发
    }
    
    /**
     * 创建右键菜单
     */
    private void createContextMenu() {
        contextMenu = new JPopupMenu();
        
        // 刷新菜单项
        refreshMenuItem = new JMenuItem("刷新");
        refreshMenuItem.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        refreshMenuItem.addActionListener(e -> loadBorrowRecords());
        contextMenu.add(refreshMenuItem);
        
        // 清空搜索菜单项
        clearMenuItem = new JMenuItem("清空搜索");
        clearMenuItem.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        clearMenuItem.addActionListener(e -> clearSearchFields());
        contextMenu.add(clearMenuItem);
        
        // 为表格添加鼠标监听器
        borrowTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
        });
        
        // 为滚动面板添加鼠标监听器
        scrollPane.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showContextMenu(e);
                }
            }
        });
    }
    
    /**
     * 显示右键菜单
     */
    private void showContextMenu(java.awt.event.MouseEvent e) {
        contextMenu.show(e.getComponent(), e.getX(), e.getY());
    }
    
    /**
     * 清空所有搜索字段
     */
    private void clearSearchFields() {
        userSearchField.setText("");
        bookSearchField.setText("");
        statusCombo.setSelectedItem("全部");
        yearCombo.setSelectedItem("全部");
        monthCombo.setSelectedItem("全部");
        dayCombo.setSelectedItem("全部");
        dateTypeCombo.setSelectedItem("借阅日期");
        
        // 清空后自动加载所有记录
        loadBorrowRecords();
    }
    
    private void loadBorrowRecords() {
        // 在后台线程中加载借阅记录
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    log.info("开始加载借阅记录");
                    
                    // 构建请求
                    Request request = new Request("library/admin/borrow-records");
                    
                    // 发送请求
                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> records = (List<Map<String, Object>>) response.getData();
                        
                        // 更新表格
                        SwingUtilities.invokeLater(() -> updateBorrowTable(records));
                        
                        log.info("借阅记录加载成功，共 {} 条记录", records.size());
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("加载失败：" + errorMsg);
                            statusLabel.setForeground(Color.RED);
                        });
                        log.warn("借阅记录加载失败: {}", errorMsg);
                    }
                    
                } catch (Exception e) {
                    log.error("加载借阅记录时发生错误", e);
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("加载时发生错误：" + e.getMessage());
                        statusLabel.setForeground(Color.RED);
                    });
                }
            }).start();
        });
    }
    
    private void updateBorrowTable(List<Map<String, Object>> records) {
        // 清空表格
        tableModel.setRowCount(0);
        
        // 添加数据
        for (Map<String, Object> record : records) {
            Object[] row = {
                record.get("userId"),
                record.get("userName"),
                record.get("bookTitle"),
                formatDate(record.get("borrowDate")),
                formatDate(record.get("dueDate")),
                formatReturnDate(record.get("returnDate")), // 新增归还日期列
                formatStatus(record.get("status"))
            };
            tableModel.addRow(row);
        }
        
        // 更新状态标签
        statusLabel.setText("共 " + records.size() + " 条借阅记录");
        statusLabel.setForeground(Color.BLACK);
        
        // 更新按钮状态
        updateButtonStates();
    }
    
    private String formatDate(Object dateObj) {
        if (dateObj == null) {
            return "未归还";
        }
        
        if (dateObj instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd").format((Date) dateObj);
        } else if (dateObj instanceof String) {
            String dateStr = (String) dateObj;
            // 如果包含时间部分，只取日期部分
            if (dateStr.contains(" ")) {
                return dateStr.split(" ")[0];
            }
            return dateStr;
        } else {
            String dateStr = dateObj.toString();
            // 如果包含时间部分，只取日期部分
            if (dateStr.contains(" ")) {
                return dateStr.split(" ")[0];
            }
            return dateStr;
        }
    }
    
    private String formatReturnDate(Object dateObj) {
        if (dateObj == null) {
            return ""; // 正在借阅的书显示为空
        }
        
        if (dateObj instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd").format((Date) dateObj);
        } else if (dateObj instanceof String) {
            String dateStr = (String) dateObj;
            // 如果包含时间部分，只取日期部分
            if (dateStr.contains(" ")) {
                return dateStr.split(" ")[0];
            }
            return dateStr;
        } else {
            String dateStr = dateObj.toString();
            // 如果包含时间部分，只取日期部分
            if (dateStr.contains(" ")) {
                return dateStr.split(" ")[0];
            }
            return dateStr;
        }
    }
    
    private String formatInteger(Object value) {
        if (value == null) {
            return "0";
        }
        if (value instanceof Number) {
            return String.valueOf(((Number) value).intValue());
        }
        // 尝试解析浮点数格式
        try {
            double doubleValue = Double.parseDouble(value.toString());
            return String.valueOf((int) doubleValue);
        } catch (NumberFormatException e) {
            return value.toString();
        }
    }
    
    private String formatStatus(Object status) {
        if (status == null) {
            return "未知";
        }
        
        String statusStr = status.toString().toUpperCase();
        switch (statusStr) {
            case "BORROWED":
                return "已借出";
            case "RETURNED":
                return "已归还";
            case "OVERDUE":
                return "已逾期";
            case "RENEWED":
                return "已续借";
            default:
                return statusStr;
        }
    }
    
    private void updateButtonStates() {
        // 逾期处理功能已删除，此方法保留为空方法
    }
    
    private void searchBorrowRecords() {
        String userSearch = userSearchField.getText().trim();
        String bookSearch = bookSearchField.getText().trim();
        String status = (String) statusCombo.getSelectedItem();
        
        // 构建搜索日期
        String searchDate = buildDateString(
            (String) yearCombo.getSelectedItem(),
            (String) monthCombo.getSelectedItem(),
            (String) dayCombo.getSelectedItem()
        );
        
        // 获取日期类型
        String dateType = (String) dateTypeCombo.getSelectedItem();
        
        // 检查是否有任何搜索条件
        boolean hasSearchConditions = !userSearch.isEmpty() || !bookSearch.isEmpty() || 
                                    !"全部".equals(status) || !searchDate.isEmpty();
        
        if (!hasSearchConditions) {
            // 如果没有搜索条件，直接加载所有记录
            loadBorrowRecords();
            return;
        }
        
        // 在后台线程中执行搜索
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    log.info("开始搜索借阅记录: user={}, book={}, status={}, searchDate={}, dateType={}", 
                             userSearch, bookSearch, status, searchDate, dateType);
                    
                    // 构建搜索请求
                    Request request = new Request("library/admin/search-borrows");
                    if (!userSearch.isEmpty()) request.addParam("userSearch", userSearch);
                    if (!bookSearch.isEmpty()) request.addParam("bookSearch", bookSearch);
                    if (!"全部".equals(status)) {
                        // 状态映射：将显示的状态转换为后端状态
                        String backendStatus = status;
                        switch (status) {
                            case "已归还":
                                backendStatus = "RETURNED";
                                break;
                            case "逾期":
                                backendStatus = "OVERDUE";
                                break;
                            case "续借":
                                backendStatus = "RENEWED";
                                break;
                        }
                        request.addParam("status", backendStatus);
                    }
                    if (!searchDate.isEmpty()) {
                        if ("借阅日期".equals(dateType)) {
                            request.addParam("borrowDate", searchDate);
                        } else if ("应还日期".equals(dateType)) {
                            request.addParam("dueDate", searchDate);
                        } else if ("归还日期".equals(dateType)) {
                            request.addParam("returnDate", searchDate);
                        }
                    }
                    
                    // 发送请求
                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> records = (List<Map<String, Object>>) response.getData();
                        
                        // 更新表格
                        SwingUtilities.invokeLater(() -> updateBorrowTable(records));
                        
                        log.info("借阅记录搜索成功，找到 {} 条记录", records.size());
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "搜索失败：" + errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                        });
                        log.warn("借阅记录搜索失败: {}", errorMsg);
                    }
                    
                } catch (Exception e) {
                    log.error("搜索借阅记录时发生错误", e);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "搜索时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        });
    }
    
    
    // 逾期处理相关方法已删除
    
    @Override
    public void refresh() {
        // 刷新借阅管理面板
        loadBorrowRecords();
    }
}

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
 * 当前借阅面板
 * 显示用户当前借阅的图书，支持归还和续借操作
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class CurrentBorrowsPanel extends JPanel implements LibraryPanel.RefreshablePanel {
    
    private final NettyClient nettyClient;
    private final Map<String, Object> userData;
    
    // 表格组件
    private JTable borrowTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    
    // 保存原始数据，用于获取transId
    private List<Map<String, Object>> currentBorrowsData;
    
    // 操作按钮
    private JButton returnButton;
    private JButton renewButton;
    
    // 状态标签
    private JLabel statusLabel;
    
    public CurrentBorrowsPanel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        
        initUI();
        setupEventHandlers();
        loadCurrentBorrows();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 247, 250));
        
        // 创建标题和状态面板
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // 创建表格面板
        JPanel tablePanel = createTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // 创建操作面板
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 235, 240), 1),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        
        // 添加阴影效果
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 235, 240), 1),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        
        JLabel titleLabel = new JLabel("当前借阅");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(44, 62, 80));
        panel.add(titleLabel, BorderLayout.WEST);
        
        statusLabel = new JLabel("正在加载...");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(52, 152, 219));
        panel.add(statusLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 235, 240), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // 创建表格
        String[] columnNames = {"书名", "作者", "借阅日期", "应还日期", "状态"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可编辑
            }
        };
        
        borrowTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                
                // 交替行颜色
                if (row % 2 == 0) {
                    c.setBackground(new Color(250, 251, 252));
                } else {
                    c.setBackground(Color.WHITE);
                }
                
                // 选中行高亮
                if (isRowSelected(row)) {
                    c.setBackground(new Color(52, 152, 219, 20));
                }
                
                return c;
            }
        };
        
        borrowTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        borrowTable.setRowHeight(35);
        borrowTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        borrowTable.getTableHeader().setReorderingAllowed(false);
        borrowTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        borrowTable.getTableHeader().setBackground(new Color(52, 73, 94));
        borrowTable.getTableHeader().setForeground(Color.WHITE);
        borrowTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        borrowTable.setGridColor(new Color(240, 240, 240));
        borrowTable.setShowGrid(true);
        borrowTable.setIntercellSpacing(new Dimension(0, 0));
        
        // 设置列宽
        borrowTable.getColumnModel().getColumn(0).setPreferredWidth(250); // 书名
        borrowTable.getColumnModel().getColumn(1).setPreferredWidth(150); // 作者
        borrowTable.getColumnModel().getColumn(2).setPreferredWidth(120); // 借阅日期
        borrowTable.getColumnModel().getColumn(3).setPreferredWidth(120); // 应还日期
        borrowTable.getColumnModel().getColumn(4).setPreferredWidth(100); // 状态
        
        scrollPane = new JScrollPane(borrowTable);
        scrollPane.setPreferredSize(new Dimension(0, 350));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 235, 240), 1),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        
        returnButton = new JButton("归还");
        returnButton.setPreferredSize(new Dimension(120, 40));
        returnButton.setEnabled(false);
        returnButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        returnButton.setBorderPainted(false);
        returnButton.setFocusPainted(false);
        returnButton.setBackground(new Color(231, 76, 60));
        returnButton.setForeground(Color.WHITE);
        returnButton.setFont(new Font("微软雅黑", Font.BOLD, 13));
        returnButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        renewButton = new JButton("续借");
        renewButton.setPreferredSize(new Dimension(120, 40));
        renewButton.setEnabled(false);
        renewButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        renewButton.setBorderPainted(false);
        renewButton.setFocusPainted(false);
        renewButton.setBackground(new Color(52, 152, 219));
        renewButton.setForeground(Color.WHITE);
        renewButton.setFont(new Font("微软雅黑", Font.BOLD, 13));
        renewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panel.add(returnButton);
        panel.add(renewButton);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 13));
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
                if (button.isEnabled()) {
                    button.setBackground(backgroundColor.darker());
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(backgroundColor);
                }
            }
        });
        
        return button;
    }
    
    private void setupEventHandlers() {
        // 归还按钮
        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnSelectedBook();
            }
        });
        
        // 续借按钮
        renewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                renewSelectedBook();
            }
        });
        
        // 表格选择事件
        borrowTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
    }
    
    private void loadCurrentBorrows() {
        // 在后台线程中加载当前借阅
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    log.info("开始加载当前借阅记录");
                    
                    // 根据用户角色选择不同的接口
                    String borrowUri = "library/student/current-borrows";
                    String userRole = determineUserRole();
                    if ("teacher".equals(userRole) || "staff".equals(userRole)) {
                        borrowUri = "library/teacher/current-borrows";
                    }
                    
                    // 构建请求
                    Request request = new Request(borrowUri);
                    
                    // 发送请求
                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> borrows = (List<Map<String, Object>>) response.getData();
                        
                        // 更新表格
                        SwingUtilities.invokeLater(() -> updateBorrowTable(borrows));
                        
                        log.info("当前借阅记录加载成功，共 {} 条记录", borrows.size());
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("加载失败：" + errorMsg);
                            statusLabel.setForeground(Color.RED);
                        });
                        log.warn("当前借阅记录加载失败: {}", errorMsg);
                    }
                    
                } catch (Exception e) {
                    log.error("加载当前借阅记录时发生错误", e);
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("加载时发生错误：" + e.getMessage());
                        statusLabel.setForeground(Color.RED);
                    });
                }
            }).start();
        });
    }
    
    private void updateBorrowTable(List<Map<String, Object>> borrows) {
        // 保存原始数据
        this.currentBorrowsData = borrows;
        
        // 清空表格
        tableModel.setRowCount(0);
        
        // 添加数据
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (Map<String, Object> borrow : borrows) {
            Object[] row = {
                borrow.get("bookTitle"),
                borrow.get("bookAuthor"),
                formatDate(borrow.get("borrowTime")),
                formatDate(borrow.get("dueTime")),
                formatStatus(borrow.get("borrowStatus"))
            };
            tableModel.addRow(row);
        }
        
        // 更新状态标签
        statusLabel.setText("共 " + borrows.size() + " 条借阅记录");
        statusLabel.setForeground(Color.BLACK);
        
        // 更新按钮状态
        updateButtonStates();
    }
    
    private String formatDate(Object dateObj) {
        if (dateObj == null) {
            return "未知";
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
        int selectedRow = borrowTable.getSelectedRow();
        boolean hasSelection = selectedRow >= 0;
        
        returnButton.setEnabled(hasSelection);
        renewButton.setEnabled(hasSelection);
        
        // 根据借阅状态进一步控制按钮
        if (hasSelection) {
            String status = (String) tableModel.getValueAt(selectedRow, 4); // 状态列现在是第5列（索引4）
            // 只有正常借阅状态的图书才能归还和续借
            boolean canOperate = "已借出".equals(status) || "BORROWED".equals(status);
            returnButton.setEnabled(canOperate);
            renewButton.setEnabled(canOperate);
        }
    }
    
    private void returnSelectedBook() {
        int selectedRow = borrowTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要归还的图书", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 获取选中的借阅信息
        String bookTitle = (String) tableModel.getValueAt(selectedRow, 0); // 书名列现在是第1列（索引0）
        
        // 从原始数据中获取transId
        if (currentBorrowsData == null || selectedRow >= currentBorrowsData.size()) {
            JOptionPane.showMessageDialog(this, "无法获取借阅记录信息", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Map<String, Object> borrowData = currentBorrowsData.get(selectedRow);
        Object transIdObj = borrowData.get("transId");
        
        // 确保transId是整数格式
        Integer transId;
        if (transIdObj instanceof Number) {
            transId = ((Number) transIdObj).intValue();
        } else if (transIdObj instanceof String) {
            try {
                transId = Integer.parseInt((String) transIdObj);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "借阅记录ID格式错误", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            JOptionPane.showMessageDialog(this, "无法获取借阅记录ID", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 确认归还
        int result = JOptionPane.showConfirmDialog(this, 
                "确定要归还《" + bookTitle + "》吗？", 
                "确认归还", 
                JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            performReturn(transId);
        }
    }
    
    private void performReturn(Object transId) {
        // 在后台线程中执行归还
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    log.info("开始归还图书，借阅ID: {}", transId);
                    
                    // 根据用户角色选择不同的归还接口
                    String returnUri = "library/student/return";
                    String userRole = determineUserRole();
                    if ("teacher".equals(userRole) || "staff".equals(userRole)) {
                        returnUri = "library/teacher/return";
                    }
                    
                    // 构建归还请求
                    Request request = new Request(returnUri)
                            .addParam("transId", transId.toString());
                    
                    // 发送请求
                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "归还成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                            // 刷新借阅记录
                            loadCurrentBorrows();
                        });
                        log.info("图书归还成功，借阅ID: {}", transId);
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "归还失败：" + errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                        });
                        log.warn("图书归还失败: {}", errorMsg);
                    }
                    
                } catch (Exception e) {
                    log.error("归还图书时发生错误", e);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "归还时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        });
    }
    
    private void renewSelectedBook() {
        int selectedRow = borrowTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要续借的图书", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 获取选中的借阅信息
        String bookTitle = (String) tableModel.getValueAt(selectedRow, 0); // 书名列现在是第1列（索引0）
        String dueDate = (String) tableModel.getValueAt(selectedRow, 3); // 应还日期列现在是第4列（索引3）
        
        // 从原始数据中获取transId
        if (currentBorrowsData == null || selectedRow >= currentBorrowsData.size()) {
            JOptionPane.showMessageDialog(this, "无法获取借阅记录信息", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Map<String, Object> borrowData = currentBorrowsData.get(selectedRow);
        Object transIdObj = borrowData.get("transId");
        
        // 确保transId是整数格式
        Integer transId;
        if (transIdObj instanceof Number) {
            transId = ((Number) transIdObj).intValue();
        } else if (transIdObj instanceof String) {
            try {
                transId = Integer.parseInt((String) transIdObj);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "借阅记录ID格式错误", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            JOptionPane.showMessageDialog(this, "无法获取借阅记录ID", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 确认续借
        int result = JOptionPane.showConfirmDialog(this, 
                "确定要续借《" + bookTitle + "》吗？\n当前应还日期：" + dueDate, 
                "确认续借", 
                JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            performRenew(transId);
        }
    }
    
    private void performRenew(Object transId) {
        // 在后台线程中执行续借
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    log.info("开始续借图书，借阅ID: {}", transId);
                    
                    // 根据用户角色选择不同的续借接口
                    String renewUri = "library/student/renew";
                    String userRole = determineUserRole();
                    if ("teacher".equals(userRole) || "staff".equals(userRole)) {
                        renewUri = "library/teacher/renew";
                    }
                    
                    // 构建续借请求
                    Request request = new Request(renewUri)
                            .addParam("transId", transId.toString());
                    
                    // 发送请求
                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "续借成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                            // 刷新借阅记录
                            loadCurrentBorrows();
                        });
                        log.info("图书续借成功，借阅ID: {}", transId);
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "续借失败：" + errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                        });
                        log.warn("图书续借失败: {}", errorMsg);
                    }
                    
                } catch (Exception e) {
                    log.error("续借图书时发生错误", e);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "续借时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        });
    }
    
    private String determineUserRole() {
        Object primaryRole = userData.get("primaryRole");
        if (primaryRole != null) {
            return primaryRole.toString().toLowerCase();
        }
        
        Object roles = userData.get("roles");
        if (roles instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> roleList = (List<String>) roles;
            if (roleList.contains("admin")) {
                return "admin";
            } else if (roleList.contains("teacher") || roleList.contains("staff")) {
                return "teacher";
            }
        }
        
        return "student";
    }
    
    @Override
    public void refresh() {
        // 刷新当前借阅面板
        loadCurrentBorrows();
    }
}

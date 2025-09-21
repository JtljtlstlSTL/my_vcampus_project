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
 * 借阅历史面板
 * 显示用户的借阅历史记录
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class BorrowHistoryPanel extends JPanel implements LibraryPanel.RefreshablePanel {
    
    private final NettyClient nettyClient;
    private final Map<String, Object> userData;
    
    // 表格组件
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    
    // 操作按钮
    private JButton exportButton;
    
    // 状态标签
    private JLabel statusLabel;
    
    public BorrowHistoryPanel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        
        initUI();
        setupEventHandlers();
        loadBorrowHistory();
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
        
        JLabel titleLabel = new JLabel("借阅历史");
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
        String[] columnNames = {"书名", "作者", "借阅日期", "归还日期", "状态"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可编辑
            }
        };
        
        historyTable = new JTable(tableModel) {
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
        
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setRowHeight(35);
        historyTable.getTableHeader().setReorderingAllowed(false);
        historyTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        historyTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        historyTable.getTableHeader().setBackground(new Color(52, 73, 94));
        historyTable.getTableHeader().setForeground(Color.WHITE);
        historyTable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        historyTable.setGridColor(new Color(240, 240, 240));
        historyTable.setShowGrid(true);
        historyTable.setIntercellSpacing(new Dimension(0, 0));
        
        // 设置列宽
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(300); // 书名
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(180); // 作者
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(140); // 借阅日期
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(140); // 归还日期
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(120); // 状态
        
        scrollPane = new JScrollPane(historyTable);
        scrollPane.setPreferredSize(new Dimension(0, 400));
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
        
        exportButton = new JButton("导出");
        exportButton.setPreferredSize(new Dimension(120, 40));
        exportButton.setEnabled(false);
        exportButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        exportButton.setBorderPainted(false);
        exportButton.setFocusPainted(false);
        exportButton.setBackground(new Color(149, 165, 166));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFont(new Font("微软雅黑", Font.BOLD, 13));
        exportButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        panel.add(exportButton);
        
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
        // 导出按钮
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportHistory();
            }
        });
    }
    
    private void loadBorrowHistory() {
        // 在后台线程中加载借阅历史
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    log.info("开始加载借阅历史记录");
                    
                    // 根据用户角色选择不同的接口
                    String historyUri = "library/student/borrow-history";
                    String userRole = determineUserRole();
                    if ("teacher".equals(userRole) || "staff".equals(userRole)) {
                        historyUri = "library/teacher/borrow-history";
                    }
                    
                    // 构建请求
                    Request request = new Request(historyUri);
                    
                    // 发送请求
                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> history = (List<Map<String, Object>>) response.getData();
                        
                        // 更新表格
                        SwingUtilities.invokeLater(() -> updateHistoryTable(history));
                        
                        log.info("借阅历史记录加载成功，共 {} 条记录", history.size());
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("加载失败：" + errorMsg);
                            statusLabel.setForeground(Color.RED);
                        });
                        log.warn("借阅历史记录加载失败: {}", errorMsg);
                    }
                    
                } catch (Exception e) {
                    log.error("加载借阅历史记录时发生错误", e);
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("加载时发生错误：" + e.getMessage());
                        statusLabel.setForeground(Color.RED);
                    });
                }
            }).start();
        });
    }
    
    private void updateHistoryTable(List<Map<String, Object>> history) {
        // 清空表格
        tableModel.setRowCount(0);
        
        // 添加数据
        for (Map<String, Object> record : history) {
            Object[] row = {
                record.get("bookTitle"),
                record.get("bookAuthor"),
                formatDate(record.get("borrowTime")),
                formatDate(record.get("returnTime")),
                formatStatus(record.get("borrowStatus"))
            };
            tableModel.addRow(row);
        }
        
        // 更新状态标签
        statusLabel.setText("共 " + history.size() + " 条历史记录");
        statusLabel.setForeground(Color.BLACK);
        
        // 启用导出按钮
        exportButton.setEnabled(!history.isEmpty());
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
    
    private void exportHistory() {
        // 简单的导出功能，显示导出对话框
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出借阅历史");
        fileChooser.setSelectedFile(new java.io.File("借阅历史_" + 
                new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".txt"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            
            // 在后台线程中执行导出
            SwingUtilities.invokeLater(() -> {
                new Thread(() -> {
                    try {
                        exportToFile(file);
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "导出成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        });
                    } catch (Exception e) {
                        log.error("导出借阅历史时发生错误", e);
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "导出失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                }).start();
            });
        }
    }
    
    private void exportToFile(java.io.File file) throws Exception {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(file, java.nio.charset.StandardCharsets.UTF_8))) {
            // 写入标题
            writer.println("借阅历史记录");
            writer.println("导出时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            writer.println("用户：" + userData.get("userName"));
            writer.println();
            
            // 写入表头
            writer.println("借阅ID\t图书ID\t书名\t作者\t借阅日期\t归还日期\t状态");
            
            // 写入数据
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                StringBuilder line = new StringBuilder();
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    if (j > 0) line.append("\t");
                    Object value = tableModel.getValueAt(i, j);
                    line.append(value != null ? value.toString() : "");
                }
                writer.println(line.toString());
            }
            
            writer.println();
            writer.println("共 " + tableModel.getRowCount() + " 条记录");
        }
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
        // 刷新借阅历史面板
        loadBorrowHistory();
    }
}

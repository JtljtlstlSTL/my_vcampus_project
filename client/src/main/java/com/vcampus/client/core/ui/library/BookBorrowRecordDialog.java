package com.vcampus.client.core.ui.library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 图书借阅记录对话框
 * 显示指定图书的借阅历史记录
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class BookBorrowRecordDialog extends JDialog {
    
    private final String bookTitle;
    private final List<Map<String, Object>> borrowRecords;
    
    // UI组件
    private JTable recordTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    private JLabel summaryLabel;
    
    public BookBorrowRecordDialog(JFrame parent, String bookTitle, List<Map<String, Object>> borrowRecords) {
        super(parent, "《" + bookTitle + "》借阅记录", parent != null);
        this.bookTitle = bookTitle;
        this.borrowRecords = borrowRecords;
        
        initUI();
        loadData();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setSize(1000, 700);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
        
        // 设置对话框外观
        getContentPane().setBackground(new Color(248, 249, 250));
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // 创建标题面板
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // 创建内容面板
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // 标题
        JLabel titleLabel = new JLabel("《" + bookTitle + "》借阅记录");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(new Color(52, 152, 219));
        panel.add(titleLabel, BorderLayout.WEST);
        
        // 统计信息
        summaryLabel = new JLabel("正在加载...");
        summaryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        summaryLabel.setForeground(new Color(149, 165, 166));
        panel.add(summaryLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // 创建表格
        String[] columnNames = {"借阅人", "借阅时间", "应还时间", "实际归还时间", "状态", "续借次数"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可编辑
            }
        };
        
        recordTable = new JTable(tableModel);
        recordTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        recordTable.setRowHeight(30);
        recordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recordTable.getTableHeader().setReorderingAllowed(false);
        recordTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        recordTable.getTableHeader().setBackground(new Color(52, 152, 219));
        recordTable.getTableHeader().setForeground(Color.WHITE);
        recordTable.setGridColor(new Color(220, 220, 220));
        recordTable.setShowGrid(true);
        
        // 设置列宽 - 优化列宽分配
        recordTable.getColumnModel().getColumn(0).setPreferredWidth(120); // 借阅人
        recordTable.getColumnModel().getColumn(1).setPreferredWidth(160); // 借阅时间
        recordTable.getColumnModel().getColumn(2).setPreferredWidth(160); // 应还时间
        recordTable.getColumnModel().getColumn(3).setPreferredWidth(160); // 实际归还时间
        recordTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 状态
        recordTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // 续借次数
        
        // 设置表格自动调整模式
        recordTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        
        scrollPane = new JScrollPane(recordTable);
        scrollPane.setPreferredSize(new Dimension(0, 400));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // 设置滚动条样式
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(52, 152, 219);
                this.trackColor = new Color(248, 249, 250);
                this.thumbDarkShadowColor = new Color(41, 128, 185);
                this.thumbLightShadowColor = new Color(52, 152, 219);
                this.thumbHighlightColor = new Color(52, 152, 219);
            }
        });
        
        scrollPane.getHorizontalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(52, 152, 219);
                this.trackColor = new Color(248, 249, 250);
                this.thumbDarkShadowColor = new Color(41, 128, 185);
                this.thumbLightShadowColor = new Color(52, 152, 219);
                this.thumbHighlightColor = new Color(52, 152, 219);
            }
        });
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        // 关闭按钮
        JButton closeButton = createStyledButton("关闭", new Color(52, 152, 219));
        closeButton.setPreferredSize(new Dimension(100, 35));
        closeButton.addActionListener(e -> dispose());
        
        // 刷新按钮
        JButton refreshButton = createStyledButton("刷新", new Color(46, 204, 113));
        refreshButton.setPreferredSize(new Dimension(100, 35));
        refreshButton.addActionListener(e -> loadData());
        
        panel.add(closeButton);
        panel.add(refreshButton);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
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
    
    private void loadData() {
        // 清空表格
        tableModel.setRowCount(0);
        
        if (borrowRecords == null || borrowRecords.isEmpty()) {
            summaryLabel.setText("暂无借阅记录");
            summaryLabel.setForeground(new Color(149, 165, 166));
            return;
        }
        
        // 统计信息
        int totalRecords = borrowRecords.size();
        long currentBorrows = borrowRecords.stream()
                .filter(record -> "BORROWED".equals(record.get("status")))
                .count();
        long overdueRecords = borrowRecords.stream()
                .filter(record -> "OVERDUE".equals(record.get("status")))
                .count();
        
        summaryLabel.setText(String.format("共 %d 条记录，当前借出 %d 本，逾期 %d 本", 
                totalRecords, currentBorrows, overdueRecords));
        summaryLabel.setForeground(new Color(52, 152, 219));
        
        // 添加数据到表格
        for (Map<String, Object> record : borrowRecords) {
            Object cardNum = record.get("cardNum");
            Object borrowTime = record.get("borrowTime");
            Object dueTime = record.get("dueTime");
            Object returnTime = record.get("returnTime");
            Object status = record.get("status");
            Object renewCount = record.get("renewCount");
            
            // 格式化时间
            String formattedBorrowTime = formatDateTime(borrowTime);
            String formattedDueTime = formatDateTime(dueTime);
            String formattedReturnTime = formatDateTime(returnTime);
            
            // 格式化状态
            String displayStatus = formatStatus(status);
            
            // 格式化续借次数
            String formattedRenewCount = formatInteger(renewCount);
            
            Object[] row = {
                cardNum,
                formattedBorrowTime,
                formattedDueTime,
                formattedReturnTime,
                displayStatus,
                formattedRenewCount
            };
            tableModel.addRow(row);
        }
    }
    
    /**
     * 格式化日期时间
     */
    private String formatDateTime(Object dateTime) {
        if (dateTime == null) {
            return "-";
        }
        
        try {
            if (dateTime instanceof LocalDateTime) {
                return ((LocalDateTime) dateTime).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } else if (dateTime instanceof String) {
                // 尝试解析字符串格式的日期时间
                LocalDateTime parsed = LocalDateTime.parse(dateTime.toString().replace(" ", "T"));
                return parsed.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } else {
                return dateTime.toString();
            }
        } catch (Exception e) {
            return dateTime.toString();
        }
    }
    
    /**
     * 格式化状态
     */
    private String formatStatus(Object status) {
        if (status == null) {
            return "未知";
        }
        
        String statusStr = status.toString();
        switch (statusStr) {
            case "BORROWED":
                return "借阅中";
            case "RETURNED":
                return "已归还";
            case "OVERDUE":
                return "逾期";
            case "RENEWED":
                return "已续借";
            default:
                return statusStr;
        }
    }
    
    /**
     * 格式化数值为整数显示
     */
    private String formatInteger(Object value) {
        if (value == null) {
            return "0";
        }
        if (value instanceof Number) {
            return String.valueOf(((Number) value).intValue());
        }
        try {
            double doubleValue = Double.parseDouble(value.toString());
            return String.valueOf((int) doubleValue);
        } catch (NumberFormatException e) {
            return value.toString();
        }
    }
}

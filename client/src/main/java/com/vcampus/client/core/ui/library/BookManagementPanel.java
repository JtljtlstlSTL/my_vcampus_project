package com.vcampus.client.core.ui.library;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图书管理面板
 * 提供图书的增删改查功能，根据用户角色显示不同权限
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class BookManagementPanel extends JPanel implements LibraryPanel.RefreshablePanel {
    
    private static final Logger log = LoggerFactory.getLogger(BookManagementPanel.class);
    
    private final NettyClient nettyClient;
    private final Map<String, Object> userData;
    private final String userRole;
    
    // 搜索组件
    private JTextField searchField;
    private JComboBox<String> searchTypeCombo;
    private JButton searchButton;
    private JButton advancedSearchButton;
    private JButton categoryButton;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    // 右键菜单
    private JPopupMenu contextMenu;
    private JMenuItem refreshMenuItem;
    
    // 表格组件
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JScrollPane scrollPane;
    
    // 状态标签
    private JLabel statusLabel;
    
    public BookManagementPanel(NettyClient nettyClient, Map<String, Object> userData, String userRole) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        this.userRole = userRole;
        
        initUI();
        setupEventHandlers();
        loadBooks();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));
        
        // 创建标题和状态面板
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // 创建工具栏
        JPanel toolPanel = createToolPanel();
        add(toolPanel, BorderLayout.CENTER);
        
        // 创建操作面板
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel titleLabel = new JLabel("图书管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(new Color(52, 73, 94));
        panel.add(titleLabel, BorderLayout.WEST);
        
        statusLabel = new JLabel("正在加载...");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(52, 152, 219));
        panel.add(statusLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createToolPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // 创建标题
        JLabel tableTitle = new JLabel("图书列表");
        tableTitle.setFont(new Font("微软雅黑", Font.BOLD, 14));
        tableTitle.setForeground(new Color(52, 73, 94));
        tableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(tableTitle, BorderLayout.NORTH);
        
        // 创建搜索栏
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setBackground(Color.WHITE);
        JLabel searchLabel = new JLabel("搜索:");
        searchLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchLabel.setForeground(new Color(44, 62, 80));
        searchPanel.add(searchLabel);
        
        // 搜索类型选择
        searchTypeCombo = new JComboBox<>(new String[]{"综合搜索", "按书名", "按作者", "按出版社", "按ISBN", "按分类"});
        searchTypeCombo.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchTypeCombo.setPreferredSize(new Dimension(100, 35));
        searchPanel.add(searchTypeCombo);
        
        searchField = new JTextField(20);
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        searchPanel.add(searchField);
        searchButton = createStyledButton("搜索", new Color(52, 152, 219));
        searchButton.setPreferredSize(new Dimension(100, 35));
        searchPanel.add(searchButton);
        
        advancedSearchButton = createStyledButton("高级搜索", new Color(155, 89, 182));
        advancedSearchButton.setPreferredSize(new Dimension(100, 35));
        searchPanel.add(advancedSearchButton);
        
        // 分类按钮
        categoryButton = createStyledButton("分类浏览", new Color(230, 126, 34));
        categoryButton.setPreferredSize(new Dimension(100, 35));
        searchPanel.add(categoryButton);
        
        // 创建操作按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        addButton = createStyledButton("添加图书", new Color(46, 204, 113));
        addButton.setPreferredSize(new Dimension(120, 35));
        buttonPanel.add(addButton);
        
        editButton = createStyledButton("编辑", new Color(52, 152, 219));
        editButton.setPreferredSize(new Dimension(100, 35));
        editButton.setEnabled(false);
        buttonPanel.add(editButton);
        
        deleteButton = createStyledButton("删除", new Color(231, 76, 60));
        deleteButton.setPreferredSize(new Dimension(100, 35));
        deleteButton.setEnabled(false);
        buttonPanel.add(deleteButton);
        
        
        // 合并搜索和按钮面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // 创建表格
        String[] columnNames = {"ID", "书名", "作者", "出版社", "出版日期", "分类", "位置", "ISBN", "状态", "库存"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可编辑
            }
        };
        
        bookTable = new JTable(tableModel);
        bookTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        bookTable.setRowHeight(30);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.getTableHeader().setReorderingAllowed(false);
        bookTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        bookTable.getTableHeader().setBackground(new Color(52, 152, 219));
        bookTable.getTableHeader().setForeground(Color.WHITE);
        bookTable.setGridColor(new Color(220, 220, 220));
        bookTable.setShowGrid(true);
        
        // 设置列宽
        bookTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(200); // 书名
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(120); // 作者
        bookTable.getColumnModel().getColumn(3).setPreferredWidth(120); // 出版社
        bookTable.getColumnModel().getColumn(4).setPreferredWidth(100); // 出版日期
        bookTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // 分类
        bookTable.getColumnModel().getColumn(6).setPreferredWidth(120); // 位置
        bookTable.getColumnModel().getColumn(7).setPreferredWidth(120); // ISBN
        bookTable.getColumnModel().getColumn(8).setPreferredWidth(80);  // 状态
        bookTable.getColumnModel().getColumn(9).setPreferredWidth(60);  // 库存
        
        scrollPane = new JScrollPane(bookTable);
        scrollPane.setPreferredSize(new Dimension(0, 350));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        
        // 创建右键菜单（在scrollPane创建之后）
        createContextMenu();
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // 根据用户角色显示不同的操作按钮
        if ("admin".equals(userRole) || "manager".equals(userRole)) {
            JButton importButton = createStyledButton("批量导入", new Color(155, 89, 182));
            importButton.setPreferredSize(new Dimension(120, 35));
            importButton.addActionListener(e -> showImportDialog());
            panel.add(importButton);
            
            JButton exportButton = createStyledButton("导出数据", new Color(52, 152, 219));
            exportButton.setPreferredSize(new Dimension(120, 35));
            exportButton.addActionListener(e -> exportBooks());
            panel.add(exportButton);
        }
        
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
                searchBooks();
            }
        });
        
        // 添加按钮
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddBookDialog();
            }
        });
        
        // 编辑按钮
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editSelectedBook();
            }
        });
        
        // 删除按钮
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedBook();
            }
        });
        
        
        // 高级搜索按钮
        advancedSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAdvancedSearchDialog();
            }
        });
        
        // 分类按钮事件
        categoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCategoryDialog();
            }
        });
        
        // 表格选择事件
        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
        
        // 回车键搜索
        searchField.addActionListener(e -> searchBooks());
        
    }
    
    /**
     * 创建右键菜单
     */
    private void createContextMenu() {
        contextMenu = new JPopupMenu();
        
        // 刷新菜单项
        refreshMenuItem = new JMenuItem("刷新");
        refreshMenuItem.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        refreshMenuItem.addActionListener(e -> loadBooks());
        contextMenu.add(refreshMenuItem);
        
        // 为表格添加鼠标监听器
        bookTable.addMouseListener(new java.awt.event.MouseAdapter() {
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
    
    private void loadBooks() {
        // 在后台线程中加载图书列表
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    log.info("开始加载图书列表");
                    
                    // 构建请求
                    Request request = new Request("library/admin/book/get-all");
                    
                    // 发送请求
                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> books = (List<Map<String, Object>>) response.getData();
                        
                        // 更新表格
                        SwingUtilities.invokeLater(() -> updateBookTable(books));
                        
                        log.info("图书列表加载成功，共 {} 本图书", books.size());
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("加载失败：" + errorMsg);
                            statusLabel.setForeground(Color.RED);
                        });
                        log.warn("图书列表加载失败: {}", errorMsg);
                    }
                    
                } catch (Exception e) {
                    log.error("加载图书列表时发生错误", e);
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("加载时发生错误：" + e.getMessage());
                        statusLabel.setForeground(Color.RED);
                    });
                }
            }).start();
        });
    }
    
    private void updateBookTable(List<Map<String, Object>> books) {
        // 清空表格
        tableModel.setRowCount(0);
        
        // 添加数据
        for (Map<String, Object> book : books) {
            // 格式化ID和库存为整数
            Object bookId = book.get("id");
            Object stock = book.get("stock");
            
            // 将浮点数转换为整数显示
            String formattedId = formatInteger(bookId);
            String formattedStock = formatInteger(stock);
            
            // 格式化出版日期
            String publishDate = formatPublishDate(book.get("publishDate"));
            
            Object[] row = {
                formattedId,
                book.get("title"),
                book.get("author"),
                book.get("publisher"),
                publishDate,
                book.get("category"),
                book.get("location"),
                book.get("isbn"),
                formatBookStatus(book.get("status")),
                formattedStock
            };
            tableModel.addRow(row);
        }
        
        // 更新状态标签
        statusLabel.setText("共 " + books.size() + " 本图书");
        statusLabel.setForeground(Color.BLACK);
        
        // 更新按钮状态
        updateButtonStates();
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
        return value.toString();
    }
    
    /**
     * 格式化出版日期显示
     */
    private String formatPublishDate(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof java.time.LocalDate) {
            return value.toString();
        }
        if (value instanceof java.sql.Date) {
            return value.toString();
        }
        if (value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }
    
    /**
     * 格式化图书状态为中文显示
     */
    private String formatBookStatus(Object status) {
        if (status == null) {
            return "未知";
        }
        String statusStr = status.toString().toUpperCase();
        switch (statusStr) {
            case "AVAILABLE":
            case "IN_LIBRARY":
                return "在馆";
            case "BORROWED":
                return "在馆/已借阅";
            case "MAINTENANCE":
                return "维修中";
            case "LOST":
                return "丢失";
            case "DAMAGED":
                return "损坏";
            case "WITHDRAWN":
                return "下架";
            case "RESERVED":
                return "已预约";
            default:
                return statusStr;
        }
    }
    
    private void updateButtonStates() {
        int selectedRow = bookTable.getSelectedRow();
        boolean hasSelection = selectedRow >= 0;
        
        editButton.setEnabled(hasSelection);
        
        // 检查是否可以删除（状态为"在馆/已借阅"的图书不能删除）
        boolean canDelete = hasSelection && !isBorrowedBook(selectedRow);
        deleteButton.setEnabled(canDelete);
        
        // 更新删除按钮的提示文本
        if (hasSelection && !canDelete) {
            deleteButton.setToolTipText("该图书已借阅，无法删除");
        } else if (hasSelection) {
            deleteButton.setToolTipText("删除选中的图书");
        } else {
            deleteButton.setToolTipText("请先选择要删除的图书");
        }
    }
    
    /**
     * 检查选中图书是否为已借阅状态
     */
    private boolean isBorrowedBook(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) {
            return false;
        }
        
        try {
            // 获取状态列的值
            Object statusObj = tableModel.getValueAt(row, 8); // 状态列
            if (statusObj == null) {
                return false;
            }
            
            String status = statusObj.toString();
            
            // 如果状态显示为"在馆/已借阅"，则不能删除
            return "在馆/已借阅".equals(status);
            
        } catch (Exception e) {
            log.warn("检查图书借阅状态时发生错误", e);
            return true; // 出错时保守处理，不允许删除
        }
    }
    
    private void searchBooks() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadBooks();
            return;
        }
        
        String searchType = (String) searchTypeCombo.getSelectedItem();
        
        // 在后台线程中执行搜索
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    log.info("开始搜索图书: {}, 搜索类型: {}", searchText, searchType);
                    
                    Request request;
                    
                    // 根据搜索类型构建不同的请求
                    switch (searchType) {
                        case "综合搜索":
                            request = new Request("library/admin/book/search")
                                    .addParam("keyword", searchText);
                            break;
                        case "按书名":
                            request = new Request("library/admin/book/search")
                                    .addParam("title", searchText);
                            break;
                        case "按作者":
                            request = new Request("library/admin/book/search")
                                    .addParam("author", searchText);
                            break;
                        case "按出版社":
                            request = new Request("library/admin/book/search")
                                    .addParam("publisher", searchText);
                            break;
                        case "按ISBN":
                            request = new Request("library/admin/book/search")
                                    .addParam("isbn", searchText);
                            break;
                        case "按分类":
                            request = new Request("library/admin/book/search")
                                    .addParam("category", searchText);
                            break;
                        default:
                            request = new Request("library/admin/book/search")
                                    .addParam("keyword", searchText);
                    }
                    
                    // 发送请求
                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> books = (List<Map<String, Object>>) response.getData();
                        
                        // 更新表格
                        SwingUtilities.invokeLater(() -> updateBookTable(books));
                        
                        // 更新状态标签
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("搜索完成，找到 " + books.size() + " 本图书");
                            statusLabel.setForeground(new Color(46, 204, 113));
                        });
                        
                        log.info("图书搜索成功，找到 {} 本图书", books.size());
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("搜索失败：" + errorMsg);
                            statusLabel.setForeground(Color.RED);
                        });
                        log.warn("图书搜索失败: {}", errorMsg);
                    }
                    
                } catch (Exception e) {
                    log.error("搜索图书时发生错误", e);
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("搜索时发生错误：" + e.getMessage());
                        statusLabel.setForeground(Color.RED);
                    });
                }
            }).start();
        });
    }
    
    private void showAddBookDialog() {
        BookEditDialog dialog = new BookEditDialog((JFrame) SwingUtilities.getWindowAncestor(this), "添加图书", null, nettyClient);
        dialog.setVisible(true);
        
        if (dialog.isBookSaved()) {
            loadBooks(); // 刷新列表
        }
    }
    
    private void editSelectedBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的图书", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 获取选中的图书信息
        Map<String, Object> bookData = getSelectedBookData(selectedRow);
        
        BookEditDialog dialog = new BookEditDialog((JFrame) SwingUtilities.getWindowAncestor(this), "编辑图书", bookData, nettyClient);
        dialog.setVisible(true);
        
        if (dialog.isBookSaved()) {
            loadBooks(); // 刷新列表
        }
    }
    
    private void deleteSelectedBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的图书", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 获取选中的图书信息
        Object bookId = tableModel.getValueAt(selectedRow, 0);
        String bookTitle = (String) tableModel.getValueAt(selectedRow, 1);
        
        // 确认删除
        int result = JOptionPane.showConfirmDialog(this, 
                "确定要删除《" + bookTitle + "》吗？\n此操作不可撤销！", 
                "确认删除", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            performDelete(bookId);
        }
    }
    
    private void performDelete(Object bookId) {
        // 在后台线程中执行删除
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    log.info("开始删除图书，图书ID: {}", bookId);
                    
                    // 构建删除请求
                    Request request = new Request("library/admin/book/delete")
                            .addParam("bookId", bookId.toString());
                    
                    // 发送请求
                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "删除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                            loadBooks(); // 刷新列表
                        });
                        log.info("图书删除成功，图书ID: {}", bookId);
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "删除失败：" + errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                        });
                        log.warn("图书删除失败: {}", errorMsg);
                    }
                    
                } catch (Exception e) {
                    log.error("删除图书时发生错误", e);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "删除时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        });
    }
    
    private Map<String, Object> getSelectedBookData(int row) {
        // 从表格中获取图书数据
        Map<String, Object> bookData = new HashMap<>();
        bookData.put("id", tableModel.getValueAt(row, 0));
        bookData.put("title", tableModel.getValueAt(row, 1));
        bookData.put("author", tableModel.getValueAt(row, 2));
        bookData.put("publisher", tableModel.getValueAt(row, 3));
        bookData.put("publishDate", tableModel.getValueAt(row, 4));
        bookData.put("category", tableModel.getValueAt(row, 5));
        bookData.put("location", tableModel.getValueAt(row, 6));
        bookData.put("isbn", tableModel.getValueAt(row, 7));
        bookData.put("status", tableModel.getValueAt(row, 8));
        bookData.put("stock", tableModel.getValueAt(row, 9));
        return bookData;
    }
    
    private void showAdvancedSearchDialog() {
        AdvancedSearchDialog dialog = new AdvancedSearchDialog((JFrame) SwingUtilities.getWindowAncestor(this), this);
        dialog.setVisible(true);
    }
    
    private void showImportDialog() {
        BatchImportDialog dialog = new BatchImportDialog((JFrame) SwingUtilities.getWindowAncestor(this), this);
        dialog.setVisible(true);
    }
    
    private void exportBooks() {
        // 在后台线程中执行导出
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    // 选择保存位置
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("导出图书数据");
                    fileChooser.setSelectedFile(new java.io.File("图书数据_" + 
                        java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx"));
                    
                    // 设置文件过滤器
                    javax.swing.filechooser.FileNameExtensionFilter filter = 
                        new javax.swing.filechooser.FileNameExtensionFilter("Excel文件 (*.xlsx)", "xlsx");
                    fileChooser.setFileFilter(filter);
                    
                    int result = fileChooser.showSaveDialog(this);
                    if (result != JFileChooser.APPROVE_OPTION) {
                        return;
                    }
                    
                    java.io.File selectedFile = fileChooser.getSelectedFile();
                    if (!selectedFile.getName().toLowerCase().endsWith(".xlsx")) {
                        selectedFile = new java.io.File(selectedFile.getAbsolutePath() + ".xlsx");
                    }
                    
                    // 执行导出
                    exportToExcel(selectedFile);
                    
                } catch (Exception e) {
                    log.error("导出图书数据时发生错误", e);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "导出失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        });
    }
    
    /**
     * 导出图书数据到Excel文件
     */
    private void exportToExcel(java.io.File file) {
        try {
            // 创建Excel工作簿
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("图书数据");
            
            // 创建标题行
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "书名", "作者", "出版社", "出版日期", "分类", "位置", "ISBN", "状态", "库存"};
            
            // 设置标题样式
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
            
            // 创建标题单元格
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 获取表格数据
            int rowCount = tableModel.getRowCount();
            int colCount = tableModel.getColumnCount();
            
            // 创建数据行
            for (int i = 0; i < rowCount; i++) {
                org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(i + 1);
                for (int j = 0; j < colCount; j++) {
                    org.apache.poi.ss.usermodel.Cell cell = dataRow.createCell(j);
                    Object value = tableModel.getValueAt(i, j);
                    if (value != null) {
                        cell.setCellValue(value.toString());
                    }
                }
            }
            
            // 自动调整列宽
            for (int i = 0; i < colCount; i++) {
                sheet.autoSizeColumn(i);
                // 设置最小宽度
                int currentWidth = sheet.getColumnWidth(i);
                if (currentWidth < 2000) {
                    sheet.setColumnWidth(i, 2000);
                }
            }
            
            // 写入文件
            try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream(file)) {
                workbook.write(fileOut);
            }
            workbook.close();
            
            // 显示成功消息
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "导出成功！\n文件已保存到：" + file.getAbsolutePath() + "\n共导出 " + rowCount + " 条记录", 
                    "导出成功", 
                    JOptionPane.INFORMATION_MESSAGE);
            });
            
        } catch (Exception e) {
            log.error("导出Excel文件时发生错误", e);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "导出失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            });
        }
    }
    
    /**
     * 执行高级搜索
     */
    public void performAdvancedSearch(Map<String, String> searchParams) {
        // 在后台线程中执行搜索
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    log.info("开始执行高级搜索，参数: {}", searchParams);
                    
                    // 构建搜索请求
                    Request request = new Request("library/admin/book/search");
                    
                    // 添加搜索参数
                    for (Map.Entry<String, String> entry : searchParams.entrySet()) {
                        if (entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                            request.addParam(entry.getKey(), entry.getValue().trim());
                        }
                    }
                    
                    // 发送请求
                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> books = (List<Map<String, Object>>) response.getData();
                        
                        // 更新表格
                        SwingUtilities.invokeLater(() -> updateBookTable(books));
                        
                        // 更新状态标签
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("高级搜索完成，找到 " + books.size() + " 本图书");
                            statusLabel.setForeground(new Color(46, 204, 113));
                        });
                        
                        log.info("高级搜索成功，找到 {} 本图书", books.size());
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("高级搜索失败：" + errorMsg);
                            statusLabel.setForeground(Color.RED);
                        });
                        log.warn("高级搜索失败: {}", errorMsg);
                    }
                    
                } catch (Exception e) {
                    log.error("高级搜索时发生错误", e);
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("高级搜索时发生错误：" + e.getMessage());
                        statusLabel.setForeground(Color.RED);
                    });
                }
            }).start();
        });
    }
    
    @Override
    public void refresh() {
        // 刷新图书管理面板
        loadBooks();
    }
    
    /**
     * 显示分类选择对话框
     */
    private void showCategoryDialog() {
        // 中图法基本大类
        String[] categories = {
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
            "N - 自然科学总论",
            "O - 数理科学和化学",
            "P - 天文学、地球科学",
            "Q - 生物科学",
            "R - 医药、卫生",
            "S - 农业科学",
            "T - 工业技术",
            "U - 交通运输",
            "V - 航空、航天",
            "X - 环境科学、安全科学",
            "Z - 综合性图书"
        };
        
        // 创建分类选择对话框
        String selectedCategory = (String) JOptionPane.showInputDialog(
            this,
            "请选择要浏览的图书分类：",
            "分类浏览",
            JOptionPane.QUESTION_MESSAGE,
            null,
            categories,
            categories[0]
        );
        
        if (selectedCategory != null && !selectedCategory.trim().isEmpty()) {
            // 提取分类代码（第一个字符）
            String categoryCode = selectedCategory.substring(0, 1);
            searchBooksByCategory(categoryCode);
        }
    }
    
    /**
     * 根据分类搜索图书
     */
    private void searchBooksByCategory(String categoryCode) {
        // 在后台线程中执行搜索
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    log.info("开始按分类搜索图书: categoryCode={}", categoryCode);
                    
                    // 构建搜索请求
                    Request request = new Request("library/admin/book/search")
                            .addParam("category", categoryCode);
                    
                    // 发送请求
                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> books = (List<Map<String, Object>>) response.getData();
                        
                        // 更新表格
                        SwingUtilities.invokeLater(() -> updateBookTable(books));
                        
                        // 更新状态标签
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("分类浏览完成，找到 " + books.size() + " 本图书");
                            statusLabel.setForeground(new Color(46, 204, 113));
                        });
                        
                        log.info("按分类搜索图书成功: categoryCode={}, count={}", categoryCode, books.size());
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("分类搜索失败：" + errorMsg);
                            statusLabel.setForeground(Color.RED);
                        });
                        log.warn("按分类搜索图书失败: {}", errorMsg);
                    }
                    
                } catch (Exception e) {
                    log.error("按分类搜索图书时发生错误", e);
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("分类搜索时发生错误：" + e.getMessage());
                        statusLabel.setForeground(Color.RED);
                    });
                }
            }).start();
        });
    }
    
}

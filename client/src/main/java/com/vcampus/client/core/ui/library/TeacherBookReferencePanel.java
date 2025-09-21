package com.vcampus.client.core.ui.library;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 教师端图书借阅参考面板
 * 提供图书搜索和借阅记录查看功能
 * 教师可以搜索图书并查看其借阅记录，但不能进行图书管理操作
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class TeacherBookReferencePanel extends JPanel implements LibraryPanel.RefreshablePanel {
    
    private final NettyClient nettyClient;
    private final Map<String, Object> userData;
    
    // 搜索组件
    private JTextField searchField;
    private JButton searchButton;
    
    // 结果展示
    private JPanel bookCardsPanel;
    private JScrollPane scrollPane;
    private JLabel titleLabel; // 标题标签
    private boolean showingPopularBooks = true; // 是否显示热门图书
    
    // 选中的图书
    private Map<String, Object> selectedBook = null;
    private JButton viewBorrowRecordButton; // 查看借阅记录按钮
    
    // 右侧借阅记录显示区域
    private JPanel rightPanel; // 右侧面板
    private JPanel borrowRecordPanel; // 借阅记录面板
    private JScrollPane borrowRecordScrollPane; // 借阅记录滚动面板
    private JTable borrowRecordTable; // 借阅记录表格
    private DefaultTableModel borrowRecordTableModel; // 借阅记录表格模型
    private JLabel rightPanelTitleLabel; // 右侧面板标题标签
    
    
    public TeacherBookReferencePanel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        
        initUI();
        setupEventHandlers();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));
        
        // 创建主面板（左侧图书列表 + 右侧借阅记录）
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 249, 250));
        
        // 创建搜索面板
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);
        
        // 创建结果面板（左侧）
        JPanel resultPanel = createResultPanel();
        mainPanel.add(resultPanel, BorderLayout.CENTER);
        
        // 创建右侧借阅记录面板
        rightPanel = createRightPanel();
        mainPanel.add(rightPanel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 创建操作面板
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);
        
        // 加载热门借阅图书
        loadPopularBooks();
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // 创建智能搜索框
        searchField = new JTextField();
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 50)
        ));
        searchField.setPreferredSize(new Dimension(500, 40));
        searchField.setMinimumSize(new Dimension(400, 40));
        searchField.setMaximumSize(new Dimension(700, 40));
        
        // 设置占位符文本
        searchField.setText("输入书名、作者、ISBN、出版社等关键词进行搜索...");
        searchField.setForeground(new Color(149, 165, 166));
        
        // 添加焦点监听器来处理占位符
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals("输入书名、作者、ISBN、出版社等关键词进行搜索...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setText("输入书名、作者、ISBN、出版社等关键词进行搜索...");
                    searchField.setForeground(new Color(149, 165, 166));
                    // 当搜索框为空时，显示热门图书
                    if (!showingPopularBooks) {
                        loadPopularBooks();
                    }
                }
            }
        });
        
        // 创建搜索按钮
        searchButton = createSearchIconButton();
        
        // 创建清空按钮
        JButton clearButton = new JButton("清空");
        clearButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        clearButton.setForeground(new Color(52, 152, 219));
        clearButton.setBackground(Color.WHITE);
        clearButton.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 1));
        clearButton.setFocusPainted(false);
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.addActionListener(e -> clearSearchAndShowPopular());
        
        // 添加组件到面板
        panel.add(searchField);
        panel.add(searchButton);
        panel.add(clearButton);
        
        return panel;
    }
    
    private JButton createSearchIconButton() {
        JButton button = new JButton("搜索");
        button.setFont(new Font("微软雅黑", Font.BOLD, 13));
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(80, 40));
        button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        button.setEnabled(true);
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(41, 128, 185));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(new Color(52, 152, 219));
            }
        });
        
        return button;
    }
    
    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 创建标题标签
        titleLabel = new JLabel("热门借阅图书");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(new Color(52, 73, 94));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // 创建卡片容器
        bookCardsPanel = new JPanel();
        bookCardsPanel.setLayout(new BoxLayout(bookCardsPanel, BoxLayout.Y_AXIS));
        bookCardsPanel.setBackground(new Color(248, 249, 250));
        
        // 创建滚动面板
        scrollPane = new JScrollPane(bookCardsPanel);
        scrollPane.setPreferredSize(new Dimension(0, 400));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // 需要时显示水平滚动条
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // 需要时显示垂直滚动条
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // 右下角查看借阅记录按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        viewBorrowRecordButton = new JButton("查看借阅记录");
        viewBorrowRecordButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        viewBorrowRecordButton.setPreferredSize(new Dimension(120, 35));
        viewBorrowRecordButton.setBackground(new Color(155, 89, 182));
        viewBorrowRecordButton.setForeground(Color.WHITE);
        viewBorrowRecordButton.setFocusPainted(false);
        viewBorrowRecordButton.setBorderPainted(false);
        viewBorrowRecordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewBorrowRecordButton.setEnabled(false); // 初始状态为禁用
        
        // 添加悬停效果
        viewBorrowRecordButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (viewBorrowRecordButton.isEnabled()) {
                    viewBorrowRecordButton.setBackground(new Color(142, 68, 173));
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (viewBorrowRecordButton.isEnabled()) {
                    viewBorrowRecordButton.setBackground(new Color(155, 89, 182));
                }
            }
        });
        
        viewBorrowRecordButton.addActionListener(e -> {
            if (selectedBook != null) {
                showBorrowRecordFromCard(selectedBook);
            }
        });
        
        buttonPanel.add(viewBorrowRecordButton);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 创建右侧借阅记录面板
     */
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        panel.setPreferredSize(new Dimension(400, 0));
        panel.setMinimumSize(new Dimension(350, 0));
        
        // 标题栏（包含标题和关闭按钮）
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // 标题
        rightPanelTitleLabel = new JLabel("借阅记录");
        rightPanelTitleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        rightPanelTitleLabel.setForeground(new Color(44, 62, 80));
        rightPanelTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(rightPanelTitleLabel, BorderLayout.CENTER);
        
        // 关闭按钮
        JButton closeButton = new JButton("关闭");
        closeButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        closeButton.setForeground(new Color(149, 165, 166));
        closeButton.setBackground(Color.WHITE);
        closeButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(true);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.setPreferredSize(new Dimension(60, 30));
        
        // 添加悬停效果
        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                closeButton.setForeground(new Color(231, 76, 60));
                closeButton.setBackground(new Color(248, 249, 250));
                closeButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(231, 76, 60), 1),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                closeButton.setForeground(new Color(149, 165, 166));
                closeButton.setBackground(Color.WHITE);
                closeButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
        });
        
        // 关闭按钮事件
        closeButton.addActionListener(e -> hideBorrowRecordPanel());
        
        titlePanel.add(closeButton, BorderLayout.EAST);
        panel.add(titlePanel, BorderLayout.NORTH);
        
        // 借阅记录表格
        borrowRecordPanel = createBorrowRecordPanel();
        panel.add(borrowRecordPanel, BorderLayout.CENTER);
        
        // 默认隐藏右侧面板
        panel.setVisible(false);
        
        return panel;
    }
    
    /**
     * 创建借阅记录表格面板
     */
    private JPanel createBorrowRecordPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // 创建表格模型
        String[] columnNames = {"借阅人", "借阅时间", "归还时间", "状态"};
        borrowRecordTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可编辑
            }
        };
        
        // 创建表格
        borrowRecordTable = new JTable(borrowRecordTableModel);
        borrowRecordTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        borrowRecordTable.setRowHeight(25);
        borrowRecordTable.setGridColor(new Color(220, 220, 220));
        borrowRecordTable.setShowGrid(true);
        borrowRecordTable.setIntercellSpacing(new Dimension(0, 0));
        borrowRecordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // 设置表头样式
        JTableHeader header = borrowRecordTable.getTableHeader();
        header.setFont(new Font("微软雅黑", Font.BOLD, 12));
        header.setBackground(new Color(52, 58, 64));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 35));
        
        // 设置列宽
        borrowRecordTable.getColumnModel().getColumn(0).setPreferredWidth(80); // 借阅人
        borrowRecordTable.getColumnModel().getColumn(1).setPreferredWidth(100); // 借阅时间
        borrowRecordTable.getColumnModel().getColumn(2).setPreferredWidth(100); // 归还时间
        borrowRecordTable.getColumnModel().getColumn(3).setPreferredWidth(60); // 状态
        
        // 创建滚动面板
        borrowRecordScrollPane = new JScrollPane(borrowRecordTable);
        borrowRecordScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        borrowRecordScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        borrowRecordScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        // 自定义滚动条样式
        JScrollBar verticalScrollBar = borrowRecordScrollPane.getVerticalScrollBar();
        verticalScrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(200, 200, 200);
                this.trackColor = new Color(240, 240, 240);
            }
        });
        
        panel.add(borrowRecordScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setupEventHandlers() {
        // 搜索按钮
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSmartSearch();
            }
        });
        
        // 回车键搜索
        searchField.addActionListener(e -> performSmartSearch());
    }
    
    private void performSmartSearch() {
        // 获取搜索关键词
        String keyword = searchField.getText().trim();
        
        // 检查是否是占位符文本
        if (keyword.equals("输入书名、作者、ISBN、出版社等关键词进行搜索...")) {
            keyword = "";
        }
        
        // 验证搜索条件
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入搜索关键词", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 在后台线程中执行搜索
        final String searchKeyword = keyword;
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    System.out.println("开始智能搜索图书: keyword=" + searchKeyword);
                    
                    // 构建智能搜索请求
                    Request request = new Request("library/user/smartSearch");
                    request.addParam("keyword", searchKeyword);
                    
                    // 发送请求
                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> books = (List<Map<String, Object>>) response.getData();
                        
                        // 更新表格
                        SwingUtilities.invokeLater(() -> {
                            updateBookTable(books);
                            titleLabel.setText("搜索结果");
                            showingPopularBooks = false;
                        });
                        
                        System.out.println("图书搜索成功，找到 " + books.size() + " 本图书");
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "搜索失败：" + errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                        });
                        System.out.println("图书搜索失败: " + errorMsg);
                    }
                    
                } catch (Exception e) {
                    System.out.println("搜索图书时发生错误: " + e.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "搜索时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        });
    }
    
    private void updateBookTable(List<Map<String, Object>> books) {
        // 清空卡片容器
        bookCardsPanel.removeAll();
        
        // 清除选择状态
        selectedBook = null;
        viewBorrowRecordButton.setEnabled(false);
        titleLabel.setText("图书借阅参考");
        
        // 添加图书卡片
        for (Map<String, Object> book : books) {
            JPanel bookCard = createBookCard(book);
            bookCardsPanel.add(bookCard);
            bookCardsPanel.add(Box.createVerticalStrut(10)); // 卡片间距
        }
        
        // 如果没有结果，显示提示
        if (books.isEmpty()) {
            JLabel noResultLabel = new JLabel("没有找到符合条件的图书");
            noResultLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            noResultLabel.setForeground(new Color(149, 165, 166));
            noResultLabel.setHorizontalAlignment(SwingConstants.CENTER);
            noResultLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
            bookCardsPanel.add(noResultLabel);
        }
        
        // 刷新显示
        bookCardsPanel.revalidate();
        bookCardsPanel.repaint();
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
        // 尝试解析浮点数格式
        try {
            double doubleValue = Double.parseDouble(value.toString());
            return String.valueOf((int) doubleValue);
        } catch (NumberFormatException e) {
            return value.toString();
        }
    }
    
    /**
     * 检查图书是否可借阅
     */
    private boolean isBookAvailable(String status) {
        if (status == null) {
            return false;
        }
        // 支持多种可借状态
        return "可借".equals(status) || 
               "可借阅".equals(status) || 
               "在库".equals(status) || 
               "IN_LIBRARY".equals(status) || 
               "AVAILABLE".equals(status);
    }
    
    /**
     * 创建图书卡片
     */
    private JPanel createBookCard(Map<String, Object> book) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(800, 120));
        card.setMinimumSize(new Dimension(600, 100));
        card.setPreferredSize(new Dimension(750, 120));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果和点击选择功能
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(248, 249, 250));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (selectedBook != book) {
                    card.setBackground(Color.WHITE);
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                        BorderFactory.createEmptyBorder(15, 20, 15, 20)
                    ));
                }
            }
            
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                selectBook(book, card);
            }
        });
        
        // 左侧：图书图标
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(60, 0));
        
        // 使用emoji图标
        JLabel bookIcon = new JLabel("📖");
        bookIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        bookIcon.setHorizontalAlignment(SwingConstants.CENTER);
        bookIcon.setForeground(new Color(52, 152, 219));
        leftPanel.add(bookIcon, BorderLayout.CENTER);
        
        // 中间：图书信息
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        
        // 书名
        String title = (String) book.get("title");
        JLabel titleLabel = new JLabel(title != null ? title : "未知书名");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(new Color(44, 62, 80));
        centerPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 作者和出版社
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        infoPanel.setOpaque(false);
        infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        String author = (String) book.get("author");
        String publisher = (String) book.get("publisher");
        String category = (String) book.get("category");
        
        if (author != null && !author.isEmpty()) {
            JLabel authorLabel = new JLabel("作者: " + author);
            authorLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            authorLabel.setForeground(new Color(127, 140, 141));
            infoPanel.add(authorLabel);
        }
        
        if (publisher != null && !publisher.isEmpty()) {
            JLabel publisherLabel = new JLabel("出版社: " + publisher);
            publisherLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            publisherLabel.setForeground(new Color(127, 140, 141));
            infoPanel.add(publisherLabel);
        }
        
        if (category != null && !category.isEmpty()) {
            JLabel categoryLabel = new JLabel("分类: " + category);
            categoryLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            categoryLabel.setForeground(new Color(127, 140, 141));
            infoPanel.add(categoryLabel);
        }
        
        // 状态和库存信息
        JPanel statusInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        statusInfoPanel.setOpaque(false);
        
        String status = (String) book.get("status");
        Object stock = book.get("availQty");
        if (stock == null) {
            stock = book.get("stock");
        }
        String formattedStock = formatInteger(stock);
        
        JLabel statusLabel = new JLabel();
        String displayStatus = status;
        if ("IN_LIBRARY".equals(status)) {
            displayStatus = "在库";
        } else if ("AVAILABLE".equals(status)) {
            displayStatus = "可借阅";
        } else if ("BORROWED".equals(status)) {
            displayStatus = "已借出";
        }
        
        if ("在库".equals(displayStatus) || "可借阅".equals(displayStatus)) {
            statusLabel.setText("● " + displayStatus);
            statusLabel.setForeground(new Color(46, 204, 113));
        } else {
            statusLabel.setText("● " + displayStatus);
            statusLabel.setForeground(new Color(231, 76, 60));
        }
        statusLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        statusInfoPanel.add(statusLabel);
        
        JLabel stockLabel = new JLabel("可借: " + formattedStock + " 本");
        stockLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        stockLabel.setForeground(new Color(52, 152, 219));
        statusInfoPanel.add(stockLabel);
        
        infoPanel.add(statusInfoPanel);
        centerPanel.add(infoPanel, BorderLayout.CENTER);
        
        // 组装卡片（移除右侧按钮）
        card.add(leftPanel, BorderLayout.WEST);
        card.add(centerPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    /**
     * 选择图书
     */
    private void selectBook(Map<String, Object> book, JPanel card) {
        // 清除之前的选择
        if (selectedBook != null) {
            // 重置所有卡片的样式
            resetAllCardStyles();
        }
        
        // 设置当前选择
        selectedBook = book;
        
        // 设置选中卡片的样式
        card.setBackground(new Color(230, 244, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 3),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // 启用查看借阅记录按钮
        viewBorrowRecordButton.setEnabled(true);
        
        // 显示选中提示
        String title = (String) book.get("title");
        if (title != null) {
            titleLabel.setText("已选择: " + title);
        }
    }
    
    /**
     * 重置所有卡片的样式
     */
    private void resetAllCardStyles() {
        for (Component component : bookCardsPanel.getComponents()) {
            if (component instanceof JPanel) {
                JPanel card = (JPanel) component;
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
                ));
            }
        }
    }
    
    private void clearSearch() {
        searchField.setText("输入书名、作者、ISBN、出版社等关键词进行搜索...");
        searchField.setForeground(new Color(149, 165, 166));
        bookCardsPanel.removeAll();
        bookCardsPanel.revalidate();
        bookCardsPanel.repaint();
        
        // 清除选择状态
        selectedBook = null;
        viewBorrowRecordButton.setEnabled(false);
        titleLabel.setText("图书借阅参考");
        
        // 总是重新显示热门图书
        loadPopularBooks();
    }
    
    /**
     * 清空搜索并显示热门图书（供外部调用）
     */
    public void clearSearchAndShowPopular() {
        clearSearch();
    }
    
    
    /**
     * 从卡片查看借阅记录
     */
    private void showBorrowRecordFromCard(Map<String, Object> book) {
        Object bookId = book.get("bookId");
        if (bookId == null) {
            bookId = book.get("id");
        }
        String bookTitle = (String) book.get("title");
        
        // 确保bookId是整数格式
        Integer bookIdInt;
        try {
            if (bookId instanceof Number) {
                bookIdInt = ((Number) bookId).intValue();
            } else {
                // 尝试解析字符串格式的ID
                double doubleValue = Double.parseDouble(bookId.toString());
                bookIdInt = (int) doubleValue;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "图书ID格式错误：" + bookId, "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 在右侧面板显示借阅记录
        loadBorrowRecordToRightPanel(bookIdInt, bookTitle);
    }
    
    /**
     * 在右侧面板加载借阅记录
     */
    private void loadBorrowRecordToRightPanel(Integer bookId, String bookTitle) {
        // 在后台线程中获取借阅记录
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    System.out.println("开始获取图书借阅记录，图书ID: " + bookId);
                    
                    // 构建请求
                    Request request = new Request("library/teacher/book-borrow-history");
                    request.addParam("bookId", bookId.toString());
                    
                    // 发送请求
                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && response.isSuccess()) {
                        System.out.println("借阅记录响应成功，数据: " + response.getData());
                        
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> borrowRecords = (List<Map<String, Object>>) response.getData();
                        
                        System.out.println("图书借阅记录获取成功，共 " + (borrowRecords != null ? borrowRecords.size() : 0) + " 条记录");
                        
                        // 在右侧面板显示借阅记录
                        SwingUtilities.invokeLater(() -> {
                            updateBorrowRecordTable(borrowRecords, bookTitle);
                            showBorrowRecordPanel();
                        });
                    } else {
                        System.err.println("获取图书借阅记录失败: " + (response != null ? response.getMessage() : "响应为空"));
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, 
                                "获取借阅记录失败: " + (response != null ? response.getMessage() : "网络错误"), 
                                "错误", 
                                JOptionPane.ERROR_MESSAGE);
                        });
                    }
                } catch (Exception e) {
                    System.err.println("获取图书借阅记录时发生异常: " + e.getMessage());
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, 
                            "获取借阅记录时发生错误: " + e.getMessage(), 
                            "错误", 
                            JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        });
    }
    
    /**
     * 更新借阅记录表格
     */
    private void updateBorrowRecordTable(List<Map<String, Object>> borrowRecords, String bookTitle) {
        System.out.println("更新借阅记录表格，图书: " + bookTitle + ", 记录数: " + (borrowRecords != null ? borrowRecords.size() : 0));
        
        // 清空表格
        borrowRecordTableModel.setRowCount(0);
        
        if (borrowRecords == null || borrowRecords.isEmpty()) {
            // 显示无记录提示
            borrowRecordTableModel.addRow(new Object[]{"暂无借阅记录", "", "", ""});
            // 更新右侧面板标题
            rightPanelTitleLabel.setText("借阅记录 - " + bookTitle);
            System.out.println("显示无记录提示");
            return;
        }
        
        // 添加借阅记录到表格
        for (Map<String, Object> record : borrowRecords) {
            String borrower = (String) record.get("userName");
            if (borrower == null || borrower.trim().isEmpty()) {
                borrower = (String) record.get("cardNum");
            }
            if (borrower == null || borrower.trim().isEmpty()) {
                borrower = "未知";
            }
            
            String borrowTime = formatDateTime(record.get("borrowTime"));
            String returnTime = formatDateTime(record.get("returnTime"));
            
            String status = (String) record.get("status");
            String statusText = "未知";
            if ("BORROWED".equals(status)) {
                statusText = "已借出";
            } else if ("RETURNED".equals(status)) {
                statusText = "已归还";
            } else if ("OVERDUE".equals(status)) {
                statusText = "已逾期";
            }
            
            borrowRecordTableModel.addRow(new Object[]{borrower, borrowTime, returnTime, statusText});
        }
        
        // 更新右侧面板标题
        rightPanelTitleLabel.setText("借阅记录 - " + bookTitle);
    }
    
    /**
     * 格式化日期时间
     */
    private String formatDateTime(Object dateTime) {
        if (dateTime == null) {
            return "-";
        }
        
        try {
            if (dateTime instanceof String) {
                String dateStr = (String) dateTime;
                // 如果是完整的日期时间字符串，只取日期部分
                if (dateStr.contains(" ")) {
                    return dateStr.split(" ")[0];
                }
                return dateStr;
            }
            return dateTime.toString();
        } catch (Exception e) {
            return "-";
        }
    }
    
    /**
     * 显示借阅记录面板
     */
    private void showBorrowRecordPanel() {
        rightPanel.setVisible(true);
        // 重新布局以确保面板正确显示
        rightPanel.getParent().revalidate();
        rightPanel.getParent().repaint();
    }
    
    /**
     * 隐藏借阅记录面板
     */
    private void hideBorrowRecordPanel() {
        rightPanel.setVisible(false);
        // 清除选择状态
        selectedBook = null;
        viewBorrowRecordButton.setEnabled(false);
        titleLabel.setText("图书借阅参考");
        // 重新布局
        rightPanel.getParent().revalidate();
        rightPanel.getParent().repaint();
    }
    
    @Override
    public void refresh() {
        System.out.println("=== TeacherBookReferencePanel.refresh() 被调用 ===");
        // 刷新搜索面板，重新显示热门图书
        clearSearch();
    }
    
    /**
     * 刷新热门借阅图书（供外部调用）
     */
    public void refreshPopularBooks() {
        loadPopularBooks();
    }
    
    /**
     * 加载热门借阅图书
     */
    private void loadPopularBooks() {
        System.out.println("=== 开始加载热门图书 ===");
        // 在后台线程中加载热门图书
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    System.out.println("发送热门图书请求...");
                    // 构建请求
                    Request request = new Request("library/user/get-popular-books");
                    request.addParam("limit", "20"); // 最多20本
                    
                    // 发送请求
                    CompletableFuture<Response> future = nettyClient.sendRequest(request);
                    Response response = future.get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && response.isSuccess()) {
                        Object data = response.getData();
                        System.out.println("热门图书响应数据: " + data);
                        System.out.println("数据类型: " + (data != null ? data.getClass().getName() : "null"));
                        
                        if (data != null) {
                            try {
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> books = (List<Map<String, Object>>) data;
                                
                                System.out.println("热门图书数量: " + books.size());
                                
                                // 更新UI
                                SwingUtilities.invokeLater(() -> {
                                    updateBookTable(books);
                                    titleLabel.setText("热门借阅图书");
                                    showingPopularBooks = true;
                                });
                            } catch (ClassCastException e) {
                                System.out.println("数据类型转换失败: " + e.getMessage());
                                System.out.println("实际数据类型: " + data.getClass().getName());
                                
                                // 如果转换失败，尝试其他方式处理
                                SwingUtilities.invokeLater(() -> {
                                    titleLabel.setText("热门借阅图书");
                                    showingPopularBooks = true;
                                    // 显示一些示例图书
                                    showSampleBooks();
                                });
                            }
                        } else {
                            System.out.println("热门图书数据为null");
                            SwingUtilities.invokeLater(() -> {
                                titleLabel.setText("热门借阅图书");
                                showingPopularBooks = true;
                            });
                        }
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        System.out.println("热门图书请求失败: " + errorMsg);
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "加载热门图书失败：" + errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                } catch (Exception e) {
                    System.err.println("加载热门图书失败: " + e.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "加载热门图书失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        });
    }
    
    /**
     * 显示示例图书（当无法获取真实数据时）
     */
    private void showSampleBooks() {
        // 创建一些示例图书数据
        List<Map<String, Object>> sampleBooks = new ArrayList<>();
        
        Map<String, Object> book1 = new HashMap<>();
        book1.put("id", 1);
        book1.put("title", "Java编程思想");
        book1.put("author", "Bruce Eckel");
        book1.put("category", "计算机");
        book1.put("status", "可借");
        book1.put("stock", 5);
        sampleBooks.add(book1);
        
        Map<String, Object> book2 = new HashMap<>();
        book2.put("id", 2);
        book2.put("title", "数据结构与算法");
        book2.put("author", "Thomas H. Cormen");
        book2.put("category", "计算机");
        book2.put("status", "可借");
        book2.put("stock", 3);
        sampleBooks.add(book2);
        
        Map<String, Object> book3 = new HashMap<>();
        book3.put("id", 3);
        book3.put("title", "设计模式");
        book3.put("author", "Gang of Four");
        book3.put("category", "计算机");
        book3.put("status", "可借");
        book3.put("stock", 2);
        sampleBooks.add(book3);
        
        updateBookTable(sampleBooks);
    }
    
}

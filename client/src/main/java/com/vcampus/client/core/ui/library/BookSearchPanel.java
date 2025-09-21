package com.vcampus.client.core.ui.library;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 图书搜索面板
 * 提供图书搜索、借阅等功能
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class BookSearchPanel extends JPanel implements LibraryPanel.RefreshablePanel {
    
    private final NettyClient nettyClient;
    private final Map<String, Object> userData;
    
    // 搜索组件
    private JTextField searchField;
    private JButton searchButton;
    
    // 分类筛选组件
    private JComboBox<String> categoryComboBox;
    private String selectedCategory = "全部"; // 当前选中的分类，默认为"全部"
    
    // 结果展示
    private JPanel bookCardsPanel;
    private JScrollPane scrollPane;
    private JLabel titleLabel; // 标题标签
    private boolean showingPopularBooks = true; // 是否显示热门图书
    
    // 操作按钮（每个图书卡片都有自己的按钮，不需要全局按钮）
    
    // 详情面板
    private JPanel detailPanel;
    private JButton backButton;
    private boolean showingDetail = false;
    
    // 选中图书相关
    private Map<String, Object> selectedBook = null;
    private JButton borrowButton; // 全局借阅按钮
    private JLabel selectedBookLabel; // 显示选中图书信息的标签
    
    // 分割面板和右侧详情面板
    private JSplitPane splitPane; // 分割面板
    private JPanel rightDetailPanel; // 右侧详情面板容器
    private JPanel leftMainPanel; // 左侧主面板容器
    
    public BookSearchPanel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        
        initUI();
        setupEventHandlers();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));
        
        // 创建左侧主面板
        leftMainPanel = new JPanel(new BorderLayout());
        leftMainPanel.setBackground(new Color(248, 249, 250));
        
        // 创建搜索面板
        JPanel searchPanel = createSearchPanel();
        leftMainPanel.add(searchPanel, BorderLayout.NORTH);
        
        // 创建结果面板
        JPanel resultPanel = createResultPanel();
        leftMainPanel.add(resultPanel, BorderLayout.CENTER);
        
        // 创建操作面板
        JPanel actionPanel = createActionPanel();
        leftMainPanel.add(actionPanel, BorderLayout.SOUTH);
        
        // 创建右侧详情面板容器
        rightDetailPanel = new JPanel(new BorderLayout());
        rightDetailPanel.setBackground(Color.WHITE);
        rightDetailPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        rightDetailPanel.setVisible(false); // 初始隐藏
        
        // 创建分割面板
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftMainPanel, rightDetailPanel);
        splitPane.setDividerSize(8); // 设置分割线宽度
        splitPane.setResizeWeight(0.7); // 左侧面板占70%权重（7:3比例）
        splitPane.setOneTouchExpandable(false); // 禁用一键展开/收起
        splitPane.setEnabled(false); // 禁用拖拽调整分割线
        
        // 初始时隐藏右侧面板，不显示分割符
        rightDetailPanel.setVisible(false);
        splitPane.setDividerSize(0); // 隐藏分割线
        
        add(splitPane, BorderLayout.CENTER);
        
        // 初始化详情面板（默认隐藏）
        initDetailPanel();
        
        // 加载分类列表
        loadCategories();
        
        // 加载热门借阅图书
        loadPopularBooks();
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // 创建顶部面板（分类选择 + 搜索区域）
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(248, 249, 250));
        
        // 创建分类选择区域（左上角）
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        categoryPanel.setBackground(new Color(248, 249, 250));
        
        // 分类标签
        JLabel categoryLabel = new JLabel("分类筛选：");
        categoryLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        categoryLabel.setForeground(new Color(52, 73, 94));
        
        // 创建分类下拉框
        categoryComboBox = new JComboBox<>();
        categoryComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        categoryComboBox.setPreferredSize(new Dimension(150, 35));
        categoryComboBox.setBackground(Color.WHITE);
        categoryComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        // 添加分类选择事件监听器
        categoryComboBox.addActionListener(e -> onCategoryChanged());
        
        categoryPanel.add(categoryLabel);
        categoryPanel.add(categoryComboBox);
        
        // 创建搜索区域（右侧）
        JPanel searchAreaPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        searchAreaPanel.setBackground(new Color(248, 249, 250));
        
        // 创建智能搜索框
        searchField = new JTextField();
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 50)
        ));
        searchField.setPreferredSize(new Dimension(500, 40)); // 减小宽度，增加高度
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
        
        searchAreaPanel.add(searchField);
        searchAreaPanel.add(searchButton);
        searchAreaPanel.add(clearButton);
        
        topPanel.add(categoryPanel, BorderLayout.WEST);
        topPanel.add(searchAreaPanel, BorderLayout.CENTER);
        
        panel.add(topPanel, BorderLayout.CENTER);
        
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
        button.setEnabled(true); // 确保按钮启用
        
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
    
    private JButton createSearchIconButton() {
        JButton button = new JButton("搜索");
        button.setFont(new Font("微软雅黑", Font.BOLD, 13));
        button.setBackground(new Color(52, 152, 219));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(80, 40)); // 匹配搜索框高度
        button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        button.setEnabled(true); // 确保按钮启用
        
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
        
        // 创建顶部面板（标题 + 借阅按钮）
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(248, 249, 250));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // 创建标题标签
        titleLabel = new JLabel("热门借阅图书");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(new Color(52, 73, 94));
        
        // 创建右上角操作面板
        JPanel rightTopPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightTopPanel.setBackground(new Color(248, 249, 250));
        
        // 选中图书信息标签
        selectedBookLabel = new JLabel("请选择一本图书");
        selectedBookLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        selectedBookLabel.setForeground(new Color(127, 140, 141));
        selectedBookLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
        
        // 创建借阅按钮
        borrowButton = new JButton("借阅");
        borrowButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        borrowButton.setPreferredSize(new Dimension(80, 35));
        borrowButton.setBackground(new Color(46, 204, 113));
        borrowButton.setForeground(Color.WHITE);
        borrowButton.setFocusPainted(false);
        borrowButton.setBorderPainted(false);
        borrowButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        borrowButton.setEnabled(false); // 初始状态禁用
        borrowButton.addActionListener(e -> borrowSelectedBook());
        
        rightTopPanel.add(selectedBookLabel);
        rightTopPanel.add(borrowButton);
        
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(rightTopPanel, BorderLayout.EAST);
        
        // 创建卡片容器
        bookCardsPanel = new JPanel();
        bookCardsPanel.setLayout(new BoxLayout(bookCardsPanel, BoxLayout.Y_AXIS));
        bookCardsPanel.setBackground(new Color(248, 249, 250));
        
        // 创建滚动面板
        scrollPane = new JScrollPane(bookCardsPanel);
        scrollPane.setPreferredSize(new Dimension(0, 400));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionPanel() {
        // 由于卡片上已经有操作按钮，这里可以简化或移除
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // 删除提示标签，保持简洁
        
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
        final String searchKeyword = keyword; // 创建final变量供lambda使用
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    System.out.println("开始智能搜索图书: keyword=" + searchKeyword);
                    
                    // 构建智能搜索请求
                    Request request = new Request("library/user/smartSearch");
                    request.addParam("keyword", searchKeyword);
                    
                    // 添加分类筛选参数
                    String categoryCode = getSelectedCategoryCode();
                    if (categoryCode != null) {
                        request.addParam("category", categoryCode);
                    }
                    
                    // 发送请求
                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> books = (List<Map<String, Object>>) response.getData();
                        
                        // 更新表格
                        SwingUtilities.invokeLater(() -> {
                            updateBookTable(books);
                            updateTitleLabel();
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
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setMinimumSize(new Dimension(600, 100)); // 设置最小宽度
        card.setPreferredSize(new Dimension(800, 120)); // 设置首选尺寸
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果、选中状态和双击事件
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (selectedBook != book) { // 不是选中状态时才显示悬停效果
                    card.setBackground(new Color(248, 249, 250));
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                        BorderFactory.createEmptyBorder(15, 20, 15, 20)
                    ));
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (selectedBook != book) { // 不是选中状态时才恢复默认样式
                    card.setBackground(Color.WHITE);
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                        BorderFactory.createEmptyBorder(15, 20, 15, 20)
                    ));
                }
            }
            
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 1) {
                    // 单击事件：选中图书
                    selectBook(book, card);
                } else if (e.getClickCount() == 2) {
                    // 双击事件：显示图书详情
                    showBookDetailFromCard(book);
                }
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
        infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); // 限制高度，允许换行
        
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
        
        // 状态和库存信息放在中间靠后位置
        JPanel statusInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        statusInfoPanel.setOpaque(false);
        
        String status = (String) book.get("status");
        // 尝试获取库存数量，优先使用availQty字段（来自Book实体）
        Object stock = book.get("availQty");
        if (stock == null) {
            stock = book.get("stock"); // 备用字段
        }
        String formattedStock = formatInteger(stock);
        
        JLabel statusLabel = new JLabel();
        // 处理英文状态转换为中文显示
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
        
        // 添加双击提示
        JLabel doubleClickHint = new JLabel("双击查看详情");
        doubleClickHint.setFont(new Font("微软雅黑", Font.ITALIC, 10));
        doubleClickHint.setForeground(new Color(149, 165, 166));
        statusInfoPanel.add(doubleClickHint);
        
        // 将状态信息添加到信息面板
        infoPanel.add(statusInfoPanel);
        
        centerPanel.add(infoPanel, BorderLayout.CENTER);
        
        // 右侧：空白区域（已删除选中指示器）
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(20, 0));
        rightPanel.setMinimumSize(new Dimension(10, 0));
        
        // 组装卡片
        card.add(leftPanel, BorderLayout.WEST);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);
        
        return card;
    }
    
    /**
     * 选中图书
     */
    private void selectBook(Map<String, Object> book, JPanel card) {
        // 清除之前选中的图书
        if (selectedBook != null) {
            clearSelection();
        }
        
        // 设置新选中的图书
        selectedBook = book;
        
        // 更新卡片样式为选中状态
        card.setBackground(new Color(230, 247, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 3),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // 选中指示器已删除
        
        // 更新右上角信息
        String title = (String) book.get("title");
        selectedBookLabel.setText("已选择: " + (title != null ? title : "未知书名"));
        selectedBookLabel.setForeground(new Color(52, 152, 219));
        
        // 启用借阅按钮
        String status = (String) book.get("status");
        boolean canBorrow = isBookAvailable(status);
        borrowButton.setEnabled(canBorrow);
        if (canBorrow) {
            borrowButton.setBackground(new Color(46, 204, 113));
        } else {
            borrowButton.setBackground(new Color(189, 195, 199));
        }
        
        log.info("选中图书: {}", title);
    }
    
    /**
     * 清除选中状态
     */
    private void clearSelection() {
        if (selectedBook != null) {
            // 恢复所有卡片的默认样式
            for (Component comp : bookCardsPanel.getComponents()) {
                if (comp instanceof JPanel) {
                    JPanel card = (JPanel) comp;
                    card.setBackground(Color.WHITE);
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                        BorderFactory.createEmptyBorder(15, 20, 15, 20)
                    ));
                    
                    // 选中指示器已删除
                }
            }
            
            selectedBook = null;
            selectedBookLabel.setText("请选择一本图书");
            selectedBookLabel.setForeground(new Color(127, 140, 141));
            borrowButton.setEnabled(false);
            borrowButton.setBackground(new Color(189, 195, 199));
        }
    }
    
    
    /**
     * 借阅选中的图书
     */
    private void borrowSelectedBook() {
        if (selectedBook == null) {
            JOptionPane.showMessageDialog(this, "请先选择一本图书", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        borrowBookFromCard(selectedBook);
    }
    
    private void clearSearch() {
        searchField.setText("输入书名、作者、ISBN、出版社等关键词进行搜索...");
        searchField.setForeground(new Color(149, 165, 166));
        
        // 清除选中状态
        clearSelection();
        
        bookCardsPanel.removeAll();
        bookCardsPanel.revalidate();
        bookCardsPanel.repaint();
        
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
     * 从卡片借阅图书
     */
    private void borrowBookFromCard(Map<String, Object> book) {
        // 尝试获取图书ID，优先使用bookId字段（来自Book实体）
        Object bookId = book.get("bookId");
        if (bookId == null) {
            bookId = book.get("id"); // 备用字段
        }
        
        String bookTitle = (String) book.get("title");
        String status = (String) book.get("status");
        
        // 检查图书状态
        if (!isBookAvailable(status)) {
            JOptionPane.showMessageDialog(this, "该图书当前不可借阅", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 确认借阅
        int result = JOptionPane.showConfirmDialog(this, 
                "确定要借阅《" + bookTitle + "》吗？", 
                "确认借阅", 
                JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            performBorrow(bookId);
        }
    }
    
    /**
     * 从卡片显示图书详情
     */
    private void showBookDetailFromCard(Map<String, Object> book) {
        // 尝试获取图书ID，优先使用bookId字段（来自Book实体）
        Object bookId = book.get("bookId");
        if (bookId == null) {
            bookId = book.get("id"); // 备用字段
        }
        String bookTitle = (String) book.get("title");
        String author = (String) book.get("author");
        String publisher = (String) book.get("publisher");
        String category = (String) book.get("category");
        String status = (String) book.get("status");
        
        // 尝试获取库存数量，优先使用availQty字段（来自Book实体）
        Object stock = book.get("availQty");
        if (stock == null) {
            stock = book.get("stock"); // 备用字段
        }
        
        // 格式化数据
        String formattedId = formatInteger(bookId);
        String formattedStock = formatInteger(stock);
        
        // 显示图书详情面板
        showBookDetailPanel(formattedId, bookTitle, author, publisher, category, status, formattedStock);
    }
    
    private void performBorrow(Object bookId) {
        // 在后台线程中执行借阅
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    System.out.println("开始借阅图书，图书ID: " + bookId);
                    
                    // 根据用户角色选择不同的借阅接口
                    String borrowUri = "library/student/borrow";
                    String userRole = determineUserRole();
                    if ("teacher".equals(userRole) || "staff".equals(userRole)) {
                        borrowUri = "library/teacher/borrow";
                    }
                    
                    // 确保图书ID是整数格式
                    String bookIdStr;
                    if (bookId instanceof Number) {
                        bookIdStr = String.valueOf(((Number) bookId).intValue());
                    } else {
                        // 尝试解析浮点数格式的ID
                        try {
                            double doubleValue = Double.parseDouble(bookId.toString());
                            bookIdStr = String.valueOf((int) doubleValue);
                        } catch (NumberFormatException e) {
                            bookIdStr = bookId.toString();
                        }
                    }
                    
                    // 构建借阅请求
                    Request request = new Request(borrowUri)
                            .addParam("bookId", bookIdStr);
                    
                    // 发送请求
                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    // 添加详细的调试日志
                    System.out.println("借阅响应详情: " + (response != null ? 
                        "status=" + response.getStatus() + ", message=" + response.getMessage() : "response=null"));
                    
                    if (response != null && response.isSuccess()) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "借阅成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                            // 根据当前显示内容决定如何刷新
                            if (showingPopularBooks) {
                                // 如果当前显示热门图书，重新加载热门图书
                                loadPopularBooks();
                            } else {
                                // 如果当前显示分类图书，重新加载分类图书
                                loadBooksByCategory();
                            }
                        });
                        System.out.println("图书借阅成功，图书ID: " + bookId);
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "借阅失败：" + errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                        });
                        System.out.println("图书借阅失败: " + errorMsg);
                    }
                    
                } catch (Exception e) {
                    System.out.println("借阅图书时发生错误: " + e.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "借阅时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
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
        System.out.println("=== BookSearchPanel.refresh() 被调用 ===");
        // 如果当前显示详情面板，先返回搜索界面
        if (showingDetail) {
            hideBookDetailPanel();
        }
        // 刷新搜索面板，重新显示热门图书
        clearSearch();
        // clearSearch() 已经调用了 loadPopularBooks()，不需要重复调用
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
                    
                    // 添加分类筛选参数
                    String categoryCode = getSelectedCategoryCode();
                    if (categoryCode != null) {
                        request.addParam("category", categoryCode);
                    }
                    
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
                                    updateTitleLabel();
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
    
    /**
     * 初始化详情面板
     */
    private void initDetailPanel() {
        detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBackground(Color.WHITE);
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        detailPanel.setVisible(false);
    }
    
    /**
     * 显示图书详情面板
     */
    private void showBookDetailPanel(String bookId, String bookTitle, String author, 
                                   String publisher, String category, String status, String stock) {
        // 清空右侧详情面板
        rightDetailPanel.removeAll();
        
        // 创建关闭按钮（X图标）
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setPreferredSize(new Dimension(30, 30));
        closeButton.setBackground(Color.WHITE);
        closeButton.setForeground(new Color(150, 150, 150));
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(true);
        closeButton.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> hideBookDetailPanel());
        
        // 创建顶部面板（关闭按钮）
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        topPanel.add(closeButton);
        
        // 创建详情内容
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 20, 15, 20);
        gbc.anchor = GridBagConstraints.WEST;
        
        // 标题
        JLabel titleLabel = new JLabel("图书详情");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setForeground(new Color(52, 73, 94));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        contentPanel.add(titleLabel, gbc);
        
        // 详情信息
        String[] labels = {"书名：", "作者：", "出版社：", "分类：", "状态：", "库存："};
        String[] values = {bookTitle, author, publisher, category, status, stock};
        
        for (int i = 0; i < labels.length; i++) {
            // 标签
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("微软雅黑", Font.BOLD, 13));
            label.setForeground(new Color(52, 73, 94));
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.WEST;
            contentPanel.add(label, gbc);
            
            // 值
            JLabel value = new JLabel(values[i]);
            value.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            value.setForeground(new Color(44, 62, 80));
            gbc.gridx = 1; gbc.gridy = i + 1;
            gbc.anchor = GridBagConstraints.WEST;
            contentPanel.add(value, gbc);
        }
        
        // 创建操作按钮面板
        JPanel buttonPanel = createDetailButtonPanel(bookId, bookTitle, status);
        
        // 添加到右侧详情面板
        rightDetailPanel.add(topPanel, BorderLayout.NORTH);
        rightDetailPanel.add(contentPanel, BorderLayout.CENTER);
        rightDetailPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 显示右侧详情面板
        rightDetailPanel.setVisible(true);
        splitPane.setDividerSize(8); // 显示分割线
        // 分割线位置由resizeWeight(0.7)自动控制，无需手动设置
        
        // 重新布局
        revalidate();
        repaint();
    }
    
    
    /**
     * 创建详情页按钮面板
     */
    private JPanel createDetailButtonPanel(String bookId, String bookTitle, String status) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        // 加入书架按钮（居中显示，更大尺寸）
        JButton addToShelfBtn = createStyledButton("加入书架", new Color(52, 152, 219));
        addToShelfBtn.setPreferredSize(new Dimension(120, 40));
        addToShelfBtn.setFont(new Font("微软雅黑", Font.BOLD, 14));
        addToShelfBtn.addActionListener(e -> showAddToShelfDialog(bookId, bookTitle));
        
        buttonPanel.add(addToShelfBtn);
        
        return buttonPanel;
    }
    
    /**
     * 显示添加到书架对话框
     */
    private void showAddToShelfDialog(String bookId, String bookTitle) {
        // 先检查图书是否已在书架中
        checkBookInShelfBeforeShowDialog(bookId, bookTitle);
    }
    
    /**
     * 检查图书是否已在书架中，如果不在则显示添加对话框
     */
    private void checkBookInShelfBeforeShowDialog(String bookId, String bookTitle) {
        // 在后台线程中检查图书是否已在书架中
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    // 构建请求
                    Request request = new Request("library/bookshelf/check")
                            .addParam("bookId", bookId);
                    
                    // 发送请求
                    CompletableFuture<Response> future = nettyClient.sendRequest(request);
                    Response response = future.get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    SwingUtilities.invokeLater(() -> {
                        if (response != null && response.isSuccess()) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> result = (Map<String, Object>) response.getData();
                            Boolean inShelf = (Boolean) result.get("inShelf");
                            
                            if (Boolean.TRUE.equals(inShelf)) {
                                // 图书已在书架中，显示提示
                                JOptionPane.showMessageDialog(this, 
                                    "《" + bookTitle + "》已经在您的书架中了！", 
                                    "提示", 
                                    JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                // 图书不在书架中，显示添加对话框
                                AddToShelfDialog dialog = new AddToShelfDialog(
                                    SwingUtilities.getWindowAncestor(this), 
                                    nettyClient, 
                                    userData, 
                                    bookId, 
                                    bookTitle
                                );
                                dialog.setVisible(true);
                            }
                        } else {
                            // 检查失败，仍然显示添加对话框
                            AddToShelfDialog dialog = new AddToShelfDialog(
                                SwingUtilities.getWindowAncestor(this), 
                                nettyClient, 
                                userData, 
                                bookId, 
                                bookTitle
                            );
                            dialog.setVisible(true);
                        }
                    });
                } catch (Exception e) {
                    System.err.println("检查图书是否在书架中失败: " + e.getMessage());
                    // 检查失败，仍然显示添加对话框
                    SwingUtilities.invokeLater(() -> {
                        AddToShelfDialog dialog = new AddToShelfDialog(
                            SwingUtilities.getWindowAncestor(this), 
                            nettyClient, 
                            userData, 
                            bookId, 
                            bookTitle
                        );
                        dialog.setVisible(true);
                    });
                }
            }).start();
        });
    }
    
    /**
     * 隐藏详情面板，返回搜索界面
     */
    private void hideBookDetailPanel() {
        showingDetail = false;
        
        // 隐藏右侧详情面板
        rightDetailPanel.setVisible(false);
        rightDetailPanel.removeAll();
        
        // 隐藏分割线
        splitPane.setDividerSize(0);
        
        // 重新布局
        revalidate();
        repaint();
    }
    
    /**
     * 分类选择改变事件处理
     */
    private void onCategoryChanged() {
        selectedCategory = (String) categoryComboBox.getSelectedItem();
        log.info("分类选择改变: {}", selectedCategory);
        
        // 根据选择的分类决定显示内容
        if ("全部".equals(selectedCategory)) {
            // 选择"全部"时显示热门借阅图书
            showingPopularBooks = true;
            loadPopularBooks();
        } else {
            // 选择具体分类时显示该分类下的所有图书
            showingPopularBooks = false;
            loadBooksByCategory();
        }
    }
    
    /**
     * 加载分类列表
     */
    private void loadCategories() {
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    log.info("开始加载分类列表");
                    
                    // 构建请求
                    Request request = new Request("library/user/get-categories");
                    
                    // 发送请求
                    CompletableFuture<Response> future = nettyClient.sendRequest(request);
                    Response response = future.get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> categories = (List<Map<String, Object>>) response.getData();
                        
                        SwingUtilities.invokeLater(() -> {
                            updateCategoryComboBox(categories);
                        });
                        
                        log.info("分类列表加载成功，共 {} 个分类", categories.size());
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        log.error("加载分类列表失败: {}", errorMsg);
                        SwingUtilities.invokeLater(() -> {
                            // 使用默认分类
                            loadDefaultCategories();
                        });
                    }
                } catch (Exception e) {
                    log.error("加载分类列表时发生错误: {}", e.getMessage());
                    SwingUtilities.invokeLater(() -> {
                        // 使用默认分类
                        loadDefaultCategories();
                    });
                }
            }).start();
        });
    }
    
    /**
     * 更新分类下拉框
     */
    private void updateCategoryComboBox(List<Map<String, Object>> categories) {
        categoryComboBox.removeAllItems();
        
        // 添加"全部"选项
        categoryComboBox.addItem("全部");
        
        // 添加分类选项
        for (Map<String, Object> category : categories) {
            String categoryName = (String) category.get("categoryName");
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                categoryComboBox.addItem(categoryName);
            }
        }
        
        // 设置默认选中"全部"
        categoryComboBox.setSelectedItem("全部");
        selectedCategory = "全部";
    }
    
    /**
     * 加载默认分类（当服务器请求失败时使用）
     */
    private void loadDefaultCategories() {
        categoryComboBox.removeAllItems();
        
        // 添加"全部"选项
        categoryComboBox.addItem("全部");
        
        // 添加中图法22个基本分类
        String[] defaultCategories = {
            "马克思主义、列宁主义、毛泽东思想、邓小平理论",
            "哲学、宗教",
            "社会科学总论",
            "政治、法律",
            "军事",
            "经济",
            "文化、科学、教育、体育",
            "语言、文字",
            "文学",
            "艺术",
            "历史、地理",
            "自然科学总论",
            "数理科学和化学",
            "天文学、地球科学",
            "生物科学",
            "医药、卫生",
            "农业科学",
            "工业技术",
            "交通运输",
            "航空、航天",
            "环境科学、安全科学",
            "综合性图书"
        };
        
        for (String category : defaultCategories) {
            categoryComboBox.addItem(category);
        }
        
        // 设置默认选中"全部"
        categoryComboBox.setSelectedItem("全部");
        selectedCategory = "全部";
    }
    
    /**
     * 获取当前选中的分类代码
     */
    private String getSelectedCategoryCode() {
        if ("全部".equals(selectedCategory)) {
            return null; // null表示不筛选分类
        }
        
        // 根据分类名称映射到分类代码
        // 这里可以根据实际需要建立映射关系
        // 暂时返回分类名称本身，服务器端应该能处理
        return selectedCategory;
    }
    
    /**
     * 加载指定分类的所有图书
     */
    private void loadBooksByCategory() {
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    log.info("开始加载分类图书: {}", selectedCategory);
                    
                    // 构建请求 - 使用复合搜索，不指定关键词，只指定分类
                    Request request = new Request("library/user/search")
                            .addParam("category", selectedCategory);
                    
                    // 发送请求
                    CompletableFuture<Response> future = nettyClient.sendRequest(request);
                    Response response = future.get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> books = (List<Map<String, Object>>) response.getData();
                        
                        SwingUtilities.invokeLater(() -> {
                            updateBookTable(books);
                            updateTitleLabel();
                        });
                        
                        log.info("分类图书加载成功: count={}", books.size());
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        log.error("加载分类图书失败: {}", errorMsg);
                        SwingUtilities.invokeLater(() -> {
                            updateBookTable(new java.util.ArrayList<>());
                            updateTitleLabel();
                        });
                    }
                } catch (Exception e) {
                    log.error("加载分类图书异常", e);
                    SwingUtilities.invokeLater(() -> {
                        updateBookTable(new java.util.ArrayList<>());
                        updateTitleLabel();
                    });
                }
            }).start();
        });
    }
    
    /**
     * 更新标题标签
     */
    private void updateTitleLabel() {
        if (showingPopularBooks) {
            if ("全部".equals(selectedCategory)) {
                titleLabel.setText("热门借阅图书");
            } else {
                titleLabel.setText(selectedCategory + " - 热门借阅图书");
            }
        } else {
            if ("全部".equals(selectedCategory)) {
                titleLabel.setText("搜索结果");
            } else {
                titleLabel.setText(selectedCategory);
            }
        }
    }
}

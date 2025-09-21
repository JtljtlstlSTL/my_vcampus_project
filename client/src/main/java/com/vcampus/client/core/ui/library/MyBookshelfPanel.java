package com.vcampus.client.core.ui.library;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.DefaultCellEditor;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * 我的书架管理面板
 * 提供用户书架的管理功能，包括查看、分类、删除等
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class MyBookshelfPanel extends JPanel implements LibraryPanel.RefreshablePanel {
    
    private final NettyClient nettyClient;
    private final Map<String, Object> userData;
    
    // UI组件
    private JComboBox<String> categoryFilter;
    private JButton createCategoryButton;
    private JPanel bookshelfPanel;
    private JScrollPane scrollPane;
    private JLabel statusLabel;
    private JPanel sidebarPanel;
    private JPanel categoryListPanel;
    
    // 右侧面板组件
    private JSplitPane splitPane;
    private JPanel rightPanel;
    private JPanel mainContentPanel;
    
    // 数据存储
    private List<Map<String, Object>> currentBookshelf;
    private String currentDisplayCategory = null; // 当前显示的右侧分类
    
    public MyBookshelfPanel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        
        initUI();
        setupEventHandlers();
        loadBookshelf();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));
        
        // 创建顶部控制面板
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // 创建书架内容面板
        JPanel contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);
        
        // 创建状态栏
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        
        // 分类筛选
        JLabel filterLabel = new JLabel("分类筛选：");
        filterLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        panel.add(filterLabel);
        
        categoryFilter = new JComboBox<>();
        categoryFilter.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        categoryFilter.setPreferredSize(new Dimension(150, 30));
        categoryFilter.addItem("全部");
        panel.add(categoryFilter);
        
        // 移除手动刷新按钮，使用自动刷新
        
        // 创建分类按钮
        createCategoryButton = createStyledButton("创建分类", new Color(46, 204, 113));
        createCategoryButton.setPreferredSize(new Dimension(100, 30));
        panel.add(createCategoryButton);
        
        return panel;
    }
    
    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(248, 249, 250));
        
        // 创建分割面板
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerSize(5);
        splitPane.setResizeWeight(0.3); // 左侧占30%，右侧占70%
        splitPane.setBorder(null);
        
        // 创建左侧分类侧边栏
        sidebarPanel = createSidebarPanel();
        splitPane.setLeftComponent(sidebarPanel);
        
        // 创建右侧主内容区域
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(new Color(248, 249, 250));
        
        // 创建分类网格容器
        bookshelfPanel = new JPanel(new GridLayout(0, 6, 15, 15));
        bookshelfPanel.setBackground(new Color(248, 249, 250));
        
        // 创建滚动面板
        scrollPane = new JScrollPane(bookshelfPanel);
        scrollPane.setPreferredSize(new Dimension(0, 400));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);
        splitPane.setRightComponent(mainContentPanel);
        
        // 创建右侧详情面板（初始隐藏）
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(248, 249, 250));
        rightPanel.setVisible(false);
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSidebarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 242, 245));
        panel.setPreferredSize(new Dimension(200, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // 标题
        JLabel titleLabel = new JLabel("分类列表");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titleLabel.setForeground(new Color(52, 73, 94));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // 分类列表
        categoryListPanel = new JPanel();
        categoryListPanel.setLayout(new BoxLayout(categoryListPanel, BoxLayout.Y_AXIS));
        categoryListPanel.setBackground(new Color(240, 242, 245));
        
        // 全部分类按钮
        JButton allCategoriesBtn = createCategoryButton("全部", true);
        categoryListPanel.add(allCategoriesBtn);
        categoryListPanel.add(Box.createVerticalStrut(8));
        
        JScrollPane categoryScrollPane = new JScrollPane(categoryListPanel);
        categoryScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        categoryScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        categoryScrollPane.setBorder(null);
        categoryScrollPane.getViewport().setBackground(new Color(240, 242, 245));
        
        panel.add(categoryScrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createCategoryButton(String categoryName, boolean isSelected) {
        JButton button = new JButton(categoryName);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        button.setPreferredSize(new Dimension(170, 35));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (isSelected) {
            button.setBackground(new Color(52, 152, 219));
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(255, 255, 255));
            button.setForeground(new Color(52, 73, 94));
            button.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        }
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!isSelected) {
                    button.setBackground(new Color(248, 249, 250));
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!isSelected) {
                    button.setBackground(new Color(255, 255, 255));
                }
            }
        });
        
        // 添加点击事件
        button.addActionListener(e -> {
            if ("全部".equals(categoryName)) {
                // 显示全部分类
                showAllCategories();
            } else {
                // 显示具体分类的图书详情
                showCategoryBooksInRightPanel(categoryName);
            }
            // 更新分类筛选
            categoryFilter.setSelectedItem(categoryName);
        });
        
        return button;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBackground(new Color(248, 249, 250));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        statusLabel = new JLabel("正在加载...");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(127, 140, 141));
        panel.add(statusLabel);
        
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
    
    private void setupEventHandlers() {
        // 分类筛选
        categoryFilter.addActionListener(e -> filterByCategory());
        
        // 创建分类按钮
        createCategoryButton.addActionListener(e -> showCreateCategoryDialog());
    }
    
    private void loadBookshelf() {
        statusLabel.setText("正在加载书架...");
        
        // 在后台线程中加载书架数据
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    // 构建请求
                    Request request = new Request("library/bookshelf/get");
                    
                    // 发送请求
                    CompletableFuture<Response> future = nettyClient.sendRequest(request);
                    Response response = future.get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> bookshelf = (List<Map<String, Object>>) response.getData();
                        
                        // 更新UI
                        SwingUtilities.invokeLater(() -> {
                            updateBookshelfDisplay(bookshelf, true); // 显示所有分类（包括空分类）
                            loadCategories(); // 这会同时更新下拉框和侧边栏（包括空分类）
                            statusLabel.setText("书架加载完成，共 " + bookshelf.size() + " 本图书");
                        });
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("加载失败：" + errorMsg);
                            showErrorMessage("加载书架失败：" + errorMsg);
                        });
                    }
                } catch (Exception e) {
                    log.error("加载书架失败", e);
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("加载失败：" + e.getMessage());
                        showErrorMessage("加载书架失败：" + e.getMessage());
                    });
                }
            }).start();
        });
    }
    
    private void loadCategories() {
        // 在后台线程中加载分类列表
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
                        
                        // 更新分类下拉框和侧边栏
                        SwingUtilities.invokeLater(() -> {
                            // 更新下拉框
                            updateCategoryFilter(categories);
                            
                            // 更新侧边栏
                            updateSidebarCategoriesFromList(categories);
                        });
                    }
                } catch (Exception e) {
                    log.error("加载分类列表失败", e);
                }
            }).start();
        });
    }
    
    private void updateCategoryFilter(List<String> categories) {
        // 临时移除监听器以避免触发事件
        java.awt.event.ActionListener[] listeners = categoryFilter.getActionListeners();
        for (java.awt.event.ActionListener listener : listeners) {
            categoryFilter.removeActionListener(listener);
        }
        
        categoryFilter.removeAllItems();
        categoryFilter.addItem("全部");
        for (String category : categories) {
            categoryFilter.addItem(category);
        }
        
        // 重新添加监听器
        for (java.awt.event.ActionListener listener : listeners) {
            categoryFilter.addActionListener(listener);
        }
    }
    
    private void updateSidebarCategoriesFromList(List<String> categories) {
        // 清空现有分类按钮
        categoryListPanel.removeAll();
        
        // 添加"全部"按钮
        JButton allCategoriesBtn = createCategoryButton("全部", true);
        categoryListPanel.add(allCategoriesBtn);
        categoryListPanel.add(Box.createVerticalStrut(8));
        
        // 为每个分类创建按钮（包括空分类）
        for (String category : categories) {
            JButton categoryBtn = createCategoryButton(category, false);
            categoryListPanel.add(categoryBtn);
            categoryListPanel.add(Box.createVerticalStrut(8));
        }
        
        // 刷新面板
        categoryListPanel.revalidate();
        categoryListPanel.repaint();
    }
    
    private void updateBookshelfDisplay(List<Map<String, Object>> bookshelf) {
        updateBookshelfDisplay(bookshelf, false);
    }
    
    private void updateBookshelfDisplay(List<Map<String, Object>> bookshelf, boolean showAllCategories) {
        // 清空书架面板
        bookshelfPanel.removeAll();
        
        if (bookshelf.isEmpty() && !showAllCategories) {
            // 显示空状态
            JLabel emptyLabel = new JLabel("书架为空，快去添加一些图书吧！");
            emptyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
            emptyLabel.setForeground(new Color(149, 165, 166));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
            bookshelfPanel.add(emptyLabel);
        } else {
            // 按分类统计图书数量
            Map<String, Integer> categoryCounts = new java.util.HashMap<>();
            for (Map<String, Object> book : bookshelf) {
                // 调试：打印所有字段
                System.out.println("Book data: " + book);
                
                // 使用正确的驼峰命名字段
                String categoryName = (String) book.get("categoryName");
                System.out.println("Category name from categoryName: " + categoryName);
                
                // 如果categoryName为空，尝试其他可能的字段
                if (categoryName == null || categoryName.trim().isEmpty()) {
                    categoryName = (String) book.get("bookCategoryName");
                    System.out.println("Category name from bookCategoryName: " + categoryName);
                }
                
                if (categoryName == null || categoryName.trim().isEmpty()) {
                    categoryName = "未分类";
                }
                categoryCounts.put(categoryName, categoryCounts.getOrDefault(categoryName, 0) + 1);
            }
            
            System.out.println("Category counts: " + categoryCounts);
            
            // 如果需要显示所有分类（包括空分类）
            if (showAllCategories) {
                // 获取所有分类列表
                List<String> allCategories = getAllCategories();
                for (String category : allCategories) {
                    int bookCount = categoryCounts.getOrDefault(category, 0);
                    JPanel categoryCard = createCategoryCard(category, bookCount);
                    bookshelfPanel.add(categoryCard);
                }
            } else {
                // 只显示有图书的分类
                for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                    JPanel categoryCard = createCategoryCard(entry.getKey(), entry.getValue());
                    bookshelfPanel.add(categoryCard);
                }
            }
        }
        
        // 刷新显示
        bookshelfPanel.revalidate();
        bookshelfPanel.repaint();
    }
    
    private List<String> getAllCategories() {
        // 从分类下拉框中获取所有分类
        List<String> categories = new ArrayList<>();
        for (int i = 0; i < categoryFilter.getItemCount(); i++) {
            String category = categoryFilter.getItemAt(i);
            if (!"全部".equals(category)) {
                categories.add(category);
            }
        }
        return categories;
    }
    
    private JPanel createCategoryCard(String categoryName, int bookCount) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));
        card.setPreferredSize(new Dimension(140, 50)); // 高度减半
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(248, 249, 250));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                    BorderFactory.createEmptyBorder(6, 6, 6, 6)
                ));
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                    BorderFactory.createEmptyBorder(6, 6, 6, 6)
                ));
            }
            
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showCategoryBooksInRightPanel(categoryName);
            }
        });
        
        // 分类图标 - 使用更小的图标
        JLabel iconLabel = new JLabel("📚");
        iconLabel.setFont(new Font("Arial Unicode MS", Font.PLAIN, 16)); // 进一步减小图标大小
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        
        // 如果emoji字体不可用，使用备用方案
        try {
            Font emojiFont = new Font("Segoe UI Emoji", Font.PLAIN, 16);
            iconLabel.setFont(emojiFont);
        } catch (Exception e) {
            // 使用系统默认字体
            iconLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        }
        
        // 分类名称
        JLabel nameLabel = new JLabel(categoryName);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 11)); // 进一步减小字体大小
        nameLabel.setForeground(new Color(52, 73, 94));
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 图书数量
        JLabel countLabel = new JLabel(bookCount + " 本");
        countLabel.setFont(new Font("微软雅黑", Font.PLAIN, 8)); // 进一步减小字体大小
        countLabel.setForeground(new Color(149, 165, 166));
        countLabel.setHorizontalAlignment(SwingConstants.CENTER);
        countLabel.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
        
        // 组装卡片
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(iconLabel, BorderLayout.NORTH);
        contentPanel.add(nameLabel, BorderLayout.CENTER);
        contentPanel.add(countLabel, BorderLayout.SOUTH);
        
        card.add(contentPanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void showAllCategories() {
        // 清除当前显示的分类
        currentDisplayCategory = null;
        
        // 隐藏右侧详情面板，显示主内容面板
        rightPanel.setVisible(false);
        mainContentPanel.setVisible(true);
        splitPane.setRightComponent(mainContentPanel);
        splitPane.setDividerSize(5);
        splitPane.setDividerLocation(0.3);
    }
    
    private void showCategoryBooksInRightPanel(String categoryName) {
        // 在后台线程中加载分类图书
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    // 构建请求
                    Request request = new Request("library/bookshelf/get");
                    request.addParam("categoryName", categoryName);
                    
                    // 发送请求
                    CompletableFuture<Response> future = nettyClient.sendRequest(request);
                    Response response = future.get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    SwingUtilities.invokeLater(() -> {
                        if (response != null && response.isSuccess()) {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> books = (List<Map<String, Object>>) response.getData();
                            displayBooksInRightPanel(categoryName, books);
                        } else {
                            String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                            JOptionPane.showMessageDialog(this, 
                                "获取分类图书失败：" + errorMsg, 
                                "错误", 
                                JOptionPane.ERROR_MESSAGE);
                        }
                    });
                    
                } catch (Exception e) {
                    log.error("获取分类图书失败", e);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, 
                            "获取分类图书失败：" + e.getMessage(), 
                            "错误", 
                            JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        });
    }
    
    private void showCategoryBooksDialog(String categoryName, List<Map<String, Object>> books) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), 
                "分类：" + categoryName, ModalityType.APPLICATION_MODAL);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 标题
        JLabel titleLabel = new JLabel("分类：" + categoryName + " (" + books.size() + " 本图书)");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 图书列表
        JPanel booksPanel = new JPanel();
        booksPanel.setLayout(new BoxLayout(booksPanel, BoxLayout.Y_AXIS));
        
        if (books.isEmpty()) {
            JLabel emptyLabel = new JLabel("该分类暂无图书");
            emptyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
            booksPanel.add(emptyLabel);
        } else {
            for (Map<String, Object> book : books) {
                JPanel bookCard = createBookCardForDialog(book);
                booksPanel.add(bookCard);
                booksPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(booksPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 关闭按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private JPanel createBookCardForDialog(Map<String, Object> book) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setMinimumSize(new Dimension(600, 100));
        card.setPreferredSize(new Dimension(800, 120));
        
        // 左侧：图书信息
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        
        // 书名
        String bookTitle = (String) book.get("bookTitle");
        JLabel titleLabel = new JLabel(bookTitle != null ? bookTitle : "未知书名");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titleLabel.setForeground(new Color(52, 73, 94));
        leftPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 作者
        String author = (String) book.get("bookAuthor");
        JLabel authorLabel = new JLabel("作者：" + (author != null ? author : "未知"));
        authorLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        authorLabel.setForeground(new Color(127, 140, 141));
        authorLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        leftPanel.add(authorLabel, BorderLayout.CENTER);
        
        // 添加时间
        String addTime = (String) book.get("addTime");
        JLabel timeLabel = new JLabel("添加时间：" + (addTime != null ? addTime : "未知"));
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        timeLabel.setForeground(new Color(149, 165, 166));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        leftPanel.add(timeLabel, BorderLayout.SOUTH);
        
        // 右侧：操作按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        rightPanel.setOpaque(false);
        
        JButton detailButton = createStyledButton("详情", new Color(52, 152, 219));
        detailButton.setPreferredSize(new Dimension(80, 30));
        detailButton.addActionListener(e -> showBookDetailFromShelf(book));
        
        JButton removeButton = createStyledButton("移除", new Color(231, 76, 60));
        removeButton.setPreferredSize(new Dimension(80, 30));
        removeButton.addActionListener(e -> removeBookFromShelf(book));
        
        rightPanel.add(detailButton);
        rightPanel.add(removeButton);
        
        // 组装卡片
        card.add(leftPanel, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);
        
        return card;
    }
    
    private void showBookDetailFromShelf(Map<String, Object> book) {
        // 这里可以调用BookSearchPanel的详情显示方法
        // 或者创建一个新的详情对话框
        String bookTitle = (String) book.get("bookTitle");
        String author = (String) book.get("bookAuthor");
        String publisher = (String) book.get("bookPublisher");
        String category = (String) book.get("bookCategoryName");
        String status = (String) book.get("bookStatus");
        Object stock = book.get("availQty");
        
        // 格式化数据
        String formattedStock = formatInteger(stock);
        
        // 显示图书详情对话框
        showBookDetailDialog(bookTitle, author, publisher, category, status, formattedStock);
    }
    
    private void showBookDetailDialog(String bookTitle, String author, String publisher, 
                                    String category, String status, String stock) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), 
                "图书详情", ModalityType.APPLICATION_MODAL);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 标题
        JLabel titleLabel = new JLabel("图书详情");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 详情内容
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        String[] labels = {"书名：", "作者：", "出版社：", "分类：", "状态：", "库存："};
        String[] values = {bookTitle, author, publisher, category, status, stock};
        
        for (int i = 0; i < labels.length; i++) {
            // 标签
            JLabel label = new JLabel(labels[i]);
            label.setFont(new Font("微软雅黑", Font.BOLD, 12));
            label.setForeground(new Color(52, 73, 94));
            gbc.gridx = 0; gbc.gridy = i; gbc.gridwidth = 1;
            contentPanel.add(label, gbc);
            
            // 值
            JLabel value = new JLabel(values[i]);
            value.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            value.setForeground(new Color(44, 62, 80));
            gbc.gridx = 1; gbc.gridy = i;
            contentPanel.add(value, gbc);
        }
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // 关闭按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private void removeBookFromShelf(Map<String, Object> book) {
        String bookTitle = (String) book.get("bookTitle");
        Object bookIdObj = book.get("bookId");
        
        // 处理bookId格式问题
        String bookIdStr;
        if (bookIdObj instanceof Double) {
            // 如果是Double类型，转换为整数
            bookIdStr = String.valueOf(((Double) bookIdObj).intValue());
        } else if (bookIdObj instanceof Integer) {
            bookIdStr = String.valueOf(bookIdObj);
        } else if (bookIdObj != null) {
            bookIdStr = String.valueOf(bookIdObj);
        } else {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "无法移除图书：图书ID缺失", "错误", JOptionPane.ERROR_MESSAGE);
            });
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(this, 
                "确定要从书架中移除《" + bookTitle + "》吗？", 
                "确认移除", 
                JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            // 在后台线程中移除图书
            SwingUtilities.invokeLater(() -> {
                new Thread(() -> {
                    try {
                        // 构建请求
                        Request request = new Request("library/bookshelf/remove")
                                .addParam("bookId", bookIdStr);
                        
                        // 发送请求
                        CompletableFuture<Response> future = nettyClient.sendRequest(request);
                        Response response = future.get(10, java.util.concurrent.TimeUnit.SECONDS);
                        
                        SwingUtilities.invokeLater(() -> {
                            if (response != null && response.isSuccess()) {
                                JOptionPane.showMessageDialog(this, "图书已从书架中移除", 
                                        "成功", JOptionPane.INFORMATION_MESSAGE);
                                // 刷新书架
                                loadBookshelf();
                                
                                // 如果当前显示的是分类详情，刷新右侧面板
                                if (currentDisplayCategory != null) {
                                    showCategoryBooksInRightPanel(currentDisplayCategory);
                                }
                            } else {
                                String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                                JOptionPane.showMessageDialog(this, "移除失败：" + errorMsg, 
                                        "错误", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    } catch (Exception e) {
                        log.error("从书架移除图书失败", e);
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "移除失败：" + e.getMessage(), 
                                    "错误", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                }).start();
            });
        }
    }
    
    private JPanel createBookCard(Map<String, Object> book) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setMinimumSize(new Dimension(600, 100));
        card.setPreferredSize(new Dimension(800, 120));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果
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
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                    BorderFactory.createEmptyBorder(15, 20, 15, 20)
                ));
            }
        });
        
        // 左侧：图书图标
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(60, 0));
        
        JLabel bookIcon = new JLabel("📚");
        bookIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        bookIcon.setHorizontalAlignment(SwingConstants.CENTER);
        bookIcon.setForeground(new Color(52, 152, 219));
        leftPanel.add(bookIcon, BorderLayout.CENTER);
        
        // 中间：图书信息
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        
        // 书名
        String title = (String) book.get("bookTitle");
        JLabel titleLabel = new JLabel(title != null ? title : "未知书名");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(new Color(44, 62, 80));
        centerPanel.add(titleLabel, BorderLayout.NORTH);
        
        // 作者和出版社
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        infoPanel.setOpaque(false);
        
        String author = (String) book.get("bookAuthor");
        String publisher = (String) book.get("bookPublisher");
        String category = (String) book.get("categoryName");
        
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
            categoryLabel.setForeground(new Color(52, 152, 219));
            infoPanel.add(categoryLabel);
        }
        
        centerPanel.add(infoPanel, BorderLayout.CENTER);
        
        // 右侧：操作按钮
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(150, 0));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
        buttonPanel.setOpaque(false);
        
        JButton removeBtn = new JButton("移除");
        removeBtn.setFont(new Font("微软雅黑", Font.BOLD, 11));
        removeBtn.setPreferredSize(new Dimension(60, 30));
        removeBtn.setBackground(new Color(231, 76, 60));
        removeBtn.setForeground(Color.WHITE);
        removeBtn.setFocusPainted(false);
        removeBtn.setBorderPainted(false);
        removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeBtn.addActionListener(e -> removeFromShelf(book));
        
        buttonPanel.add(removeBtn);
        rightPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // 组装卡片
        card.add(leftPanel, BorderLayout.WEST);
        card.add(centerPanel, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);
        
        return card;
    }
    
    private void removeFromShelf(Map<String, Object> book) {
        String bookTitle = (String) book.get("bookTitle");
        Object bookId = book.get("bookId");
        
        int result = JOptionPane.showConfirmDialog(this, 
                "确定要从书架中移除《" + bookTitle + "》吗？", 
                "确认移除", 
                JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            // 在后台线程中移除图书
            SwingUtilities.invokeLater(() -> {
                new Thread(() -> {
                    try {
                        // 构建请求
                        Request request = new Request("library/bookshelf/remove")
                                .addParam("bookId", bookId.toString());
                        
                        // 发送请求
                        CompletableFuture<Response> future = nettyClient.sendRequest(request);
                        Response response = future.get(10, java.util.concurrent.TimeUnit.SECONDS);
                        
                        SwingUtilities.invokeLater(() -> {
                            if (response != null && response.isSuccess()) {
                                JOptionPane.showMessageDialog(this, "图书已从书架中移除", 
                                        "成功", JOptionPane.INFORMATION_MESSAGE);
                                loadBookshelf(); // 重新加载书架
                            } else {
                                String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                                JOptionPane.showMessageDialog(this, "移除失败：" + errorMsg, 
                                        "错误", JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    } catch (Exception e) {
                        log.error("移除图书失败", e);
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, "移除失败：" + e.getMessage(), 
                                    "错误", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                }).start();
            });
        }
    }
    
    private void filterByCategory() {
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        if ("全部".equals(selectedCategory)) {
            loadBookshelf(); // 这会显示所有分类（包括空分类）
        } else {
            loadBookshelfByCategory(selectedCategory);
        }
    }
    
    private void loadBookshelfByCategory(String categoryName) {
        statusLabel.setText("正在加载分类：" + categoryName);
        
        // 在后台线程中加载指定分类的书架数据
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    // 构建请求
                    Request request = new Request("library/bookshelf/get")
                            .addParam("categoryName", categoryName);
                    
                    // 发送请求
                    CompletableFuture<Response> future = nettyClient.sendRequest(request);
                    Response response = future.get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> bookshelf = (List<Map<String, Object>>) response.getData();
                        
                        // 更新UI
                        SwingUtilities.invokeLater(() -> {
                            updateBookshelfDisplay(bookshelf);
                            statusLabel.setText("分类 " + categoryName + " 共 " + bookshelf.size() + " 本图书");
                        });
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("加载失败：" + errorMsg);
                            showErrorMessage("加载分类失败：" + errorMsg);
                        });
                    }
                } catch (Exception e) {
                    log.error("加载分类书架失败", e);
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("加载失败：" + e.getMessage());
                        showErrorMessage("加载分类失败：" + e.getMessage());
                    });
                }
            }).start();
        });
    }
    
    private void showCreateCategoryDialog() {
        String categoryName = JOptionPane.showInputDialog(this, 
                "请输入新分类名称：", 
                "创建分类", 
                JOptionPane.QUESTION_MESSAGE);
        
        if (categoryName != null && !categoryName.trim().isEmpty()) {
            createCategory(categoryName.trim());
        }
    }
    
    private void createCategory(String categoryName) {
        // 在后台线程中创建分类
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    // 构建创建分类请求
                    Request createRequest = new Request("library/personal-category/create");
                    createRequest.addParam("categoryName", categoryName);
                    
                    // 发送请求
                    CompletableFuture<Response> future = nettyClient.sendRequest(createRequest);
                    Response response = future.get(10, java.util.concurrent.TimeUnit.SECONDS);
                    
                    if (response != null && response.isSuccess()) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, 
                                "分类 \"" + categoryName + "\" 创建成功！", 
                                "创建成功", 
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            // 刷新分类列表和书架数据
                            loadCategories();
                            loadBookshelf();
                        });
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this, 
                                "创建分类失败：" + errorMsg, 
                                "错误", 
                                JOptionPane.ERROR_MESSAGE);
                        });
                    }
                    
                } catch (Exception e) {
                    log.error("创建分类失败", e);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, 
                            "创建分类失败：" + e.getMessage(), 
                            "错误", 
                            JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        });
    }
    
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }
    
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
    
    private void updateSidebarCategories(List<Map<String, Object>> bookshelf) {
        // 清空现有分类按钮（保留"全部"按钮）
        categoryListPanel.removeAll();
        
        // 添加"全部"按钮
        JButton allCategoriesBtn = createCategoryButton("全部", true);
        categoryListPanel.add(allCategoriesBtn);
        categoryListPanel.add(Box.createVerticalStrut(8));
        
        // 从书架数据中提取分类
        Set<String> categories = new HashSet<>();
        for (Map<String, Object> book : bookshelf) {
            String categoryName = (String) book.get("categoryName");
            if (categoryName != null && !categoryName.trim().isEmpty()) {
                categories.add(categoryName);
            }
        }
        
        // 为每个分类创建按钮
        for (String category : categories) {
            JButton categoryBtn = createCategoryButton(category, false);
            categoryListPanel.add(categoryBtn);
            categoryListPanel.add(Box.createVerticalStrut(8));
        }
        
        // 刷新侧边栏
        categoryListPanel.revalidate();
        categoryListPanel.repaint();
    }
    
    @Override
    public void refresh() {
        loadBookshelf();
    }
    
    private void displayBooksInRightPanel(String categoryName, List<Map<String, Object>> books) {
        // 设置当前显示的分类
        currentDisplayCategory = categoryName;
        
        // 清空右侧面板
        rightPanel.removeAll();
        
        // 创建标题面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(248, 249, 250));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // 左侧：标题和副标题
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("分类：" + categoryName);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 73, 94));
        
        JLabel subtitleLabel = new JLabel("共 " + books.size() + " 本图书");
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        // 右侧：关闭按钮
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("微软雅黑", Font.BOLD, 16));
        closeButton.setForeground(new Color(231, 76, 60));
        closeButton.setBackground(new Color(248, 249, 250));
        closeButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> showAllCategories());
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        headerPanel.add(closeButton, BorderLayout.EAST);
        
        rightPanel.add(headerPanel, BorderLayout.NORTH);
        
        // 创建内容区域
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(248, 249, 250));
        
        if (books.isEmpty()) {
            // 显示空状态
            JLabel emptyLabel = new JLabel("该分类暂无图书");
            emptyLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setForeground(new Color(149, 165, 166));
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
            contentPanel.add(emptyLabel, BorderLayout.CENTER);
        } else {
            // 创建图书表格
            JTable bookTable = createBookTable(books);
            JScrollPane tableScrollPane = new JScrollPane(bookTable);
            tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            tableScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
            contentPanel.add(tableScrollPane, BorderLayout.CENTER);
        }
        
        rightPanel.add(contentPanel, BorderLayout.CENTER);
        
        // 显示右侧面板
        rightPanel.setVisible(true);
        splitPane.setRightComponent(rightPanel);
        splitPane.setDividerSize(5);
        splitPane.setDividerLocation(0.3);
        
        // 刷新面板
        rightPanel.revalidate();
        rightPanel.repaint();
    }
    
    private JTable createBookTable(List<Map<String, Object>> books) {
        // 表格列名
        String[] columnNames = {"图书标题", "作者", "ISBN", "操作"};
        
        // 准备表格数据
        Object[][] data = new Object[books.size()][4];
        for (int i = 0; i < books.size(); i++) {
            Map<String, Object> book = books.get(i);
            data[i][0] = book.get("bookTitle") != null ? book.get("bookTitle") : "未知标题";
            data[i][1] = book.get("bookAuthor") != null ? book.get("bookAuthor") : "未知作者";
            data[i][2] = book.get("bookIsbn") != null ? book.get("bookIsbn") : "未知ISBN";
            data[i][3] = "移除"; // 操作列
        }
        
        // 创建表格
        JTable table = new JTable(data, columnNames);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(35);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(220, 220, 220));
        table.setIntercellSpacing(new Dimension(0, 0));
        
        // 设置列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(300); // 图书标题
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // 作者
        table.getColumnModel().getColumn(2).setPreferredWidth(200); // ISBN
        table.getColumnModel().getColumn(3).setPreferredWidth(80);  // 操作
        
        // 设置表头样式
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(240, 242, 245));
        table.getTableHeader().setForeground(new Color(52, 73, 94));
        
        // 设置操作列的渲染器和编辑器
        table.getColumn("操作").setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JButton button = new JButton("移除");
                button.setFont(new Font("微软雅黑", Font.PLAIN, 11));
                button.setBackground(new Color(231, 76, 60));
                button.setForeground(Color.WHITE);
                button.setFocusPainted(false);
                button.setBorderPainted(false);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                return button;
            }
        });
        
        table.getColumn("操作").setCellEditor(new DefaultCellEditor(new JCheckBox()) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                JButton button = new JButton("移除");
                button.setFont(new Font("微软雅黑", Font.PLAIN, 11));
                button.setBackground(new Color(231, 76, 60));
                button.setForeground(Color.WHITE);
                button.setFocusPainted(false);
                button.setBorderPainted(false);
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
                
                // 添加点击事件
                button.addActionListener(e -> {
                    Map<String, Object> book = books.get(row);
                    removeBookFromShelf(book);
                });
                
                return button;
            }
        });
        
        return table;
    }
}

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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 管理员荐购管理面板
 * 供管理员审核荐购申请、查看统计信息
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class AdminRecommendationPanel extends JPanel implements LibraryPanel.RefreshablePanel {
    
    private final NettyClient nettyClient;
    private final Map<String, Object> userData;
    
    // 主面板组件
    
    // 待审核荐购表格
    private JTable pendingTable;
    private DefaultTableModel pendingTableModel;
    private JScrollPane pendingScrollPane;
    private JButton approveButton;
    private JButton rejectButton;
    
    // 所有荐购表格
    private JTable allRecommendationsTable;
    private DefaultTableModel allRecommendationsTableModel;
    private JScrollPane allRecommendationsScrollPane;
    private JComboBox<String> statusFilterComboBox;
    private JPanel contentPanel; // 内容面板引用
    
    // 统计信息面板
    
    // 状态筛选选项
    private static final String[] STATUS_OPTIONS = {
        "全部", "待审核", "已通过", "已拒绝"
    };
    
    public AdminRecommendationPanel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        
        initUI();
        setupEventHandlers();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(248, 249, 250));
        
        // 创建主面板，使用水平布局
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(248, 249, 250));

        // 创建右侧按钮面板（竖着排列）
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        buttonPanel.setPreferredSize(new Dimension(120, 0));
        
        // 创建选择按钮（使用JButton而不是JRadioButton）
        JButton pendingButton = createNavButton("待审核荐购", true);
        JButton allButton = createNavButton("所有荐购", false);
        
        buttonPanel.add(pendingButton);
        buttonPanel.add(allButton);
        
        // 添加弹性空间
        buttonPanel.add(Box.createVerticalGlue());
        
        // 创建内容面板
        contentPanel = new JPanel(new CardLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        
        // 创建各个面板
        JPanel pendingPanel = createPendingPanel();
        JPanel allRecommendationsPanel = createAllRecommendationsPanel();
        JPanel statisticsPanel = createStatisticsMainPanel();
        
        // 添加到内容面板
        contentPanel.add(pendingPanel, "pending");
        contentPanel.add(allRecommendationsPanel, "all");
        contentPanel.add(statisticsPanel, "stats");
        
        // 设置按钮事件
        pendingButton.addActionListener(e -> {
            selectNavButton(pendingButton, allButton);
            CardLayout layout = (CardLayout) contentPanel.getLayout();
            layout.show(contentPanel, "pending");
            // 切换到待审核界面时自动刷新
            loadPendingRecommendations();
        });

        allButton.addActionListener(e -> {
            selectNavButton(allButton, pendingButton);
            CardLayout layout = (CardLayout) contentPanel.getLayout();
            layout.show(contentPanel, "all");
            // 切换到所有荐购界面时自动刷新
            loadAllRecommendations();
        });
        
        // 添加组件到主面板
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * 创建待审核荐购面板
     */
    private JPanel createPendingPanel() {
        JPanel pendingPanel = new JPanel(new BorderLayout());
        pendingPanel.setBackground(Color.WHITE);
        pendingPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // 创建工具栏
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbarPanel.setBackground(Color.WHITE);
        
        
        approveButton = createStyledButton("通过", new Color(40, 167, 69), Color.WHITE);
        approveButton.setPreferredSize(new Dimension(80, 30));
        approveButton.setEnabled(false);
        toolbarPanel.add(approveButton);
        
        rejectButton = createStyledButton("拒绝", new Color(220, 53, 69), Color.WHITE);
        rejectButton.setPreferredSize(new Dimension(80, 30));
        rejectButton.setEnabled(false);
        toolbarPanel.add(rejectButton);
        
        
        pendingPanel.add(toolbarPanel, BorderLayout.NORTH);
        
        // 创建待审核荐购表格
        String[] columnNames = {
            "荐购ID", "用户卡号", "图书标题", "作者", "出版社", "ISBN", "分类", 
            "数量", "荐购理由", "荐购时间"
        };
        
        pendingTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        pendingTable = new JTable(pendingTableModel);
        
        // 美化表格样式
        pendingTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        pendingTable.setRowHeight(32);
        pendingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pendingTable.setGridColor(new Color(230, 230, 230));
        pendingTable.setShowGrid(true);
        pendingTable.setIntercellSpacing(new Dimension(0, 0));
        pendingTable.setBackground(Color.WHITE);
        pendingTable.setSelectionBackground(new Color(52, 144, 220));
        pendingTable.setSelectionForeground(Color.WHITE);
        
        // 美化表头
        JTableHeader header = pendingTable.getTableHeader();
        header.setFont(new Font("微软雅黑", Font.BOLD, 13));
        header.setBackground(new Color(52, 58, 64));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);
        
        // 设置列宽
        pendingTable.getColumnModel().getColumn(0).setPreferredWidth(70);  // 荐购ID
        pendingTable.getColumnModel().getColumn(1).setPreferredWidth(90);  // 用户卡号
        pendingTable.getColumnModel().getColumn(2).setPreferredWidth(180); // 图书标题
        pendingTable.getColumnModel().getColumn(3).setPreferredWidth(120); // 作者
        pendingTable.getColumnModel().getColumn(4).setPreferredWidth(140); // 出版社
        pendingTable.getColumnModel().getColumn(5).setPreferredWidth(140); // ISBN
        pendingTable.getColumnModel().getColumn(6).setPreferredWidth(60);  // 分类
        pendingTable.getColumnModel().getColumn(7).setPreferredWidth(60);  // 数量
        pendingTable.getColumnModel().getColumn(8).setPreferredWidth(220); // 荐购理由
        pendingTable.getColumnModel().getColumn(9).setPreferredWidth(140); // 荐购时间
        
        // 美化滚动面板
        pendingScrollPane = new JScrollPane(pendingTable);
        pendingScrollPane.setPreferredSize(new Dimension(1000, 400));
        pendingScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        pendingScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        pendingScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // 自定义滚动条样式
        pendingScrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(200, 200, 200);
                this.trackColor = new Color(248, 249, 250);
            }
        });
        
        // 添加行交替颜色效果
        pendingTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
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
                    // 保持选中状态的默认颜色
                }
                
                return c;
            }
        });
        
        pendingPanel.add(pendingScrollPane, BorderLayout.CENTER);
        
        return pendingPanel;
    }
    
    /**
     * 创建所有荐购面板
     */
    private JPanel createAllRecommendationsPanel() {
        JPanel allPanel = new JPanel(new BorderLayout());
        allPanel.setBackground(Color.WHITE);
        allPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // 创建工具栏
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setBackground(Color.WHITE);
        
        // 左侧筛选区域
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftPanel.setBackground(Color.WHITE);
        
        JLabel filterLabel = new JLabel("状态筛选:");
        filterLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        leftPanel.add(filterLabel);
        
        statusFilterComboBox = new JComboBox<>(STATUS_OPTIONS);
        statusFilterComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusFilterComboBox.setPreferredSize(new Dimension(100, 25));
        leftPanel.add(statusFilterComboBox);
        
        // 右侧信息详情按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightPanel.setBackground(Color.WHITE);
        
        JButton infoDetailButton = new JButton("信息详情");
        infoDetailButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        infoDetailButton.setPreferredSize(new Dimension(100, 30));
        infoDetailButton.setBackground(new Color(52, 144, 220));
        infoDetailButton.setForeground(Color.WHITE);
        infoDetailButton.setFocusPainted(false);
        infoDetailButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        // 添加按钮悬停效果
        infoDetailButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                infoDetailButton.setBackground(new Color(41, 128, 185));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                infoDetailButton.setBackground(new Color(52, 144, 220));
            }
        });
        
        // 添加按钮点击事件
        infoDetailButton.addActionListener(e -> {
            CardLayout layout = (CardLayout) contentPanel.getLayout();
            layout.show(contentPanel, "stats");
            loadStatisticsForMainPanel();
        });
        
        rightPanel.add(infoDetailButton);
        
        toolbarPanel.add(leftPanel, BorderLayout.WEST);
        toolbarPanel.add(rightPanel, BorderLayout.EAST);
        
        allPanel.add(toolbarPanel, BorderLayout.NORTH);
        
        // 创建所有荐购表格
        String[] columnNames = {
            "荐购ID", "用户卡号", "图书标题", "作者", "出版社", "ISBN", "分类", 
            "数量", "荐购时间", "状态", "管理员反馈", "处理时间"
        };
        
        allRecommendationsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可编辑
            }
        };
        
        allRecommendationsTable = new JTable(allRecommendationsTableModel);
        
        // 美化表格样式
        allRecommendationsTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        allRecommendationsTable.setRowHeight(32);
        allRecommendationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        allRecommendationsTable.setGridColor(new Color(230, 230, 230));
        allRecommendationsTable.setShowGrid(true);
        allRecommendationsTable.setIntercellSpacing(new Dimension(0, 0));
        allRecommendationsTable.setBackground(Color.WHITE);
        allRecommendationsTable.setSelectionBackground(new Color(52, 144, 220));
        allRecommendationsTable.setSelectionForeground(Color.WHITE);
        
        // 美化表头
        JTableHeader allHeader = allRecommendationsTable.getTableHeader();
        allHeader.setFont(new Font("微软雅黑", Font.BOLD, 13));
        allHeader.setBackground(new Color(52, 58, 64));
        allHeader.setForeground(Color.WHITE);
        allHeader.setPreferredSize(new Dimension(0, 40));
        allHeader.setReorderingAllowed(false);
        
        // 设置列宽
        allRecommendationsTable.getColumnModel().getColumn(0).setPreferredWidth(70);  // 荐购ID
        allRecommendationsTable.getColumnModel().getColumn(1).setPreferredWidth(90);  // 用户卡号
        allRecommendationsTable.getColumnModel().getColumn(2).setPreferredWidth(180); // 图书标题
        allRecommendationsTable.getColumnModel().getColumn(3).setPreferredWidth(120); // 作者
        allRecommendationsTable.getColumnModel().getColumn(4).setPreferredWidth(140); // 出版社
        allRecommendationsTable.getColumnModel().getColumn(5).setPreferredWidth(140); // ISBN
        allRecommendationsTable.getColumnModel().getColumn(6).setPreferredWidth(60);  // 分类
        allRecommendationsTable.getColumnModel().getColumn(7).setPreferredWidth(60);  // 数量
        allRecommendationsTable.getColumnModel().getColumn(8).setPreferredWidth(140); // 荐购时间
        allRecommendationsTable.getColumnModel().getColumn(9).setPreferredWidth(90);  // 状态
        allRecommendationsTable.getColumnModel().getColumn(10).setPreferredWidth(180); // 管理员反馈
        allRecommendationsTable.getColumnModel().getColumn(11).setPreferredWidth(140); // 处理时间
        
        // 美化滚动面板
        allRecommendationsScrollPane = new JScrollPane(allRecommendationsTable);
        allRecommendationsScrollPane.setPreferredSize(new Dimension(1000, 400));
        allRecommendationsScrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        allRecommendationsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        allRecommendationsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        // 自定义滚动条样式
        allRecommendationsScrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(200, 200, 200);
                this.trackColor = new Color(248, 249, 250);
            }
        });
        
        // 添加行交替颜色效果
        allRecommendationsTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
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
                    // 保持选中状态的默认颜色
                }
                
                return c;
            }
        });
        
        
        allPanel.add(allRecommendationsScrollPane, BorderLayout.CENTER);
        
        return allPanel;
    }
    
    
    /**
     * 创建统计卡片
     */
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel cardPanel = new JPanel(new BorderLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(108, 117, 125));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cardPanel.add(titleLabel, BorderLayout.NORTH);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        cardPanel.add(valueLabel, BorderLayout.CENTER);
        
        // 将valueLabel存储到cardPanel的client property中，以便后续获取
        cardPanel.putClientProperty("valueLabel", valueLabel);
        
        return cardPanel;
    }
    
    /**
     * 从统计卡片面板中获取值标签
     */
    private JLabel getValueLabelFromPanel(JPanel cardPanel) {
        return (JLabel) cardPanel.getClientProperty("valueLabel");
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        
        // 通过荐购申请
        approveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reviewRecommendation("approve");
            }
        });
        
        // 拒绝荐购申请
        rejectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reviewRecommendation("reject");
            }
        });
        
        
        // 状态筛选
        statusFilterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAllRecommendations();
            }
        });
        
        
        // 表格选择事件
        pendingTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = pendingTable.getSelectedRow() >= 0;
            approveButton.setEnabled(hasSelection);
            rejectButton.setEnabled(hasSelection);
        });
        
    }
    
    /**
     * 加载待审核荐购
     */
    private void loadPendingRecommendations() {
        try {
            Request request = new Request();
            request.setUri("/library/admin/recommend/pending");
            
            CompletableFuture<Response> future = nettyClient.sendRequest(request);
            future.thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        updatePendingTable(response.getData());
                    } else {
                        JOptionPane.showMessageDialog(this, "加载待审核荐购失败：" + response.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "网络错误：" + throwable.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
            
        } catch (Exception e) {
            log.error("加载待审核荐购时发生错误", e);
            JOptionPane.showMessageDialog(this, "加载待审核荐购时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 加载所有荐购
     */
    private void loadAllRecommendations() {
        try {
            Request request = new Request();
            String selectedStatus = (String) statusFilterComboBox.getSelectedItem();
            
            // 根据选择的状态使用不同的接口
            if ("全部".equals(selectedStatus)) {
                request.setUri("/library/admin/recommend/all");
            } else {
                request.setUri("/library/admin/recommend/by-status");
                request.addParam("status", getStatusValue(selectedStatus));
            }
            
            CompletableFuture<Response> future = nettyClient.sendRequest(request);
            future.thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        updateAllRecommendationsTable(response.getData());
                    } else {
                        JOptionPane.showMessageDialog(this, "加载荐购失败：" + response.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "网络错误：" + throwable.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
            
        } catch (Exception e) {
            log.error("加载荐购时发生错误", e);
            JOptionPane.showMessageDialog(this, "加载荐购时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    /**
     * 审核荐购申请
     */
    private void reviewRecommendation(String action) {
        int selectedRow = pendingTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请选择要审核的荐购申请", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String recIdStr = (String) pendingTableModel.getValueAt(selectedRow, 0);
        Integer recId = Integer.parseInt(recIdStr);
        String bookTitle = (String) pendingTableModel.getValueAt(selectedRow, 2);
        
        String feedback = "";
        if ("reject".equals(action)) {
            feedback = JOptionPane.showInputDialog(this, 
                "请输入拒绝理由：", "拒绝荐购申请", JOptionPane.QUESTION_MESSAGE);
            if (feedback == null || feedback.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "拒绝理由不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        try {
            Request request = new Request();
            request.setUri("/library/admin/recommend/review");
            request.addParam("recId", recId.toString());
            request.addParam("action", action);
            request.addParam("adminFeedback", feedback);
            
            CompletableFuture<Response> future = nettyClient.sendRequest(request);
            future.thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        String message = "approve".equals(action) ? "荐购申请已通过" : "荐购申请已拒绝";
                        JOptionPane.showMessageDialog(this, message, "成功", JOptionPane.INFORMATION_MESSAGE);
                        loadPendingRecommendations();
                        loadAllRecommendations();
                    } else {
                        JOptionPane.showMessageDialog(this, "审核失败：" + response.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "网络错误：" + throwable.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
            
        } catch (Exception e) {
            log.error("审核荐购申请时发生错误", e);
            JOptionPane.showMessageDialog(this, "审核荐购申请时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 更新待审核表格
     */
    @SuppressWarnings("unchecked")
    private void updatePendingTable(Object data) {
        pendingTableModel.setRowCount(0);
        
        if (data instanceof List) {
            List<Object> pendingList = (List<Object>) data;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            for (Object record : pendingList) {
                try {
                    // 处理Gson反序列化后的LinkedTreeMap对象
                    Map<String, Object> recordMap = (Map<String, Object>) record;
                    
                    // 处理数量显示为整数
                    Object recommendQty = recordMap.get("recommendQty");
                    String quantityDisplay = recommendQty != null ? String.valueOf(((Number) recommendQty).intValue()) : "0";
                    
                    // 处理荐购ID显示为整数
                    Object recId = recordMap.get("recId");
                    String recIdDisplay = recId != null ? String.valueOf(((Number) recId).intValue()) : "0";
                    
                    Object[] row = {
                        recIdDisplay,
                        recordMap.get("cardNum"),
                        recordMap.get("bookTitle"),
                        recordMap.get("bookAuthor"),
                        recordMap.get("bookPublisher"),
                        recordMap.get("bookIsbn"),
                        recordMap.get("bookCategory"),
                        quantityDisplay,
                        recordMap.get("recommendReason"),
                        recordMap.get("recommendTime")
                    };
                    pendingTableModel.addRow(row);
                } catch (Exception e) {
                    log.error("处理荐购记录时发生错误", e);
                }
            }
        }
    }
    
    /**
     * 更新所有荐购表格
     */
    @SuppressWarnings("unchecked")
    private void updateAllRecommendationsTable(Object data) {
        allRecommendationsTableModel.setRowCount(0);
        
        if (data instanceof List) {
            List<Map<String, Object>> allList = (List<Map<String, Object>>) data;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            for (Map<String, Object> record : allList) {
                // 处理数量显示为整数
                Object recommendQty = record.get("recommendQty");
                String quantityDisplay = recommendQty != null ? String.valueOf(((Number) recommendQty).intValue()) : "0";
                
                // 处理荐购ID显示为整数
                Object recId = record.get("recId");
                String recIdDisplay = recId != null ? String.valueOf(((Number) recId).intValue()) : "0";
                
                Object[] row = {
                    recIdDisplay,
                    record.get("cardNum"),
                    record.get("bookTitle"),
                    record.get("bookAuthor"),
                    record.get("bookPublisher"),
                    record.get("bookIsbn"),
                    record.get("bookCategory"),
                    quantityDisplay,
                    record.get("recommendTime"),
                    getStatusDescription((String) record.get("status")),
                    record.get("adminFeedback"),
                    record.get("processTime")
                };
                allRecommendationsTableModel.addRow(row);
            }
        }
    }
    
    
    /**
     * 获取状态值
     */
    private String getStatusValue(String statusDescription) {
        switch (statusDescription) {
            case "待审核": return "PENDING";
            case "已通过": return "APPROVED";
            case "已拒绝": return "REJECTED";
            default: return null;
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
            case "PURCHASED": return "已通过"; // 将已采购状态显示为已通过
            default: return "未知";
        }
    }
    
    /**
     * 选择导航按钮
     */
    private void selectNavButton(JButton selectedButton, JButton... otherButtons) {
        // 设置选中按钮的样式
        selectedButton.setBackground(new Color(0, 123, 255));
        selectedButton.setForeground(Color.WHITE);
        
        // 设置其他按钮的样式
        for (JButton button : otherButtons) {
            button.setBackground(new Color(248, 249, 250));
            button.setForeground(new Color(73, 80, 87));
        }
    }
    
    /**
     * 创建统计信息主面板
     */
    private JPanel createStatisticsMainPanel() {
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // 创建工具栏
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setBackground(Color.WHITE);
        
        // 左侧标题
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("荐购统计信息", JLabel.LEFT);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 58, 64));
        leftPanel.add(titleLabel);
        
        // 右侧按钮
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightPanel.setBackground(Color.WHITE);
        
        JButton backButton = new JButton("返回");
        backButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        backButton.setPreferredSize(new Dimension(100, 30));
        backButton.setBackground(new Color(108, 117, 125));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        // 添加悬停效果
        backButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                backButton.setBackground(new Color(73, 80, 87));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                backButton.setBackground(new Color(108, 117, 125));
            }
        });
        
        // 添加事件处理
        backButton.addActionListener(e -> {
            CardLayout layout = (CardLayout) contentPanel.getLayout();
            layout.show(contentPanel, "all");
        });
        
        rightPanel.add(backButton);
        
        toolbarPanel.add(leftPanel, BorderLayout.WEST);
        toolbarPanel.add(rightPanel, BorderLayout.EAST);
        
        statsPanel.add(toolbarPanel, BorderLayout.NORTH);
        
        // 创建统计信息显示区域
        JPanel statsDisplayPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsDisplayPanel.setBackground(Color.WHITE);
        statsDisplayPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 创建统计卡片
        JPanel totalCard = createStatCard("总荐购数", "0", new Color(52, 144, 220));
        JPanel pendingCard = createStatCard("待审核", "0", new Color(255, 193, 7));
        JPanel approvedCard = createStatCard("已通过", "0", new Color(40, 167, 69));
        JPanel rejectedCard = createStatCard("已拒绝", "0", new Color(220, 53, 69));
        
        statsDisplayPanel.add(totalCard);
        statsDisplayPanel.add(pendingCard);
        statsDisplayPanel.add(approvedCard);
        statsDisplayPanel.add(rejectedCard);
        
        statsPanel.add(statsDisplayPanel, BorderLayout.CENTER);
        
        // 存储统计面板引用，用于更新数据
        statsPanel.putClientProperty("statsDisplayPanel", statsDisplayPanel);
        
        return statsPanel;
    }
    
    
    /**
     * 为主面板加载统计信息
     */
    private void loadStatisticsForMainPanel() {
        try {
            Request request = new Request();
            request.setUri("/library/admin/recommend/statistics");
            request.addParam("adminCardNum", (String) userData.get("cardNum"));
            
            Response response = nettyClient.sendRequest(request).get();
            
            if (response.isSuccess()) {
                Map<String, Object> data = (Map<String, Object>) response.getData();
                if (data != null) {
                    // 找到统计面板并更新数据
                    Component[] components = contentPanel.getComponents();
                    for (Component comp : components) {
                        if (comp instanceof JPanel) {
                            JPanel panel = (JPanel) comp;
                            JPanel statsDisplayPanel = (JPanel) panel.getClientProperty("statsDisplayPanel");
                            if (statsDisplayPanel != null) {
                                updateStatisticsCards(statsDisplayPanel, data);
                                break;
                            }
                        }
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "加载统计信息失败: " + response.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            log.error("加载统计信息异常", e);
            JOptionPane.showMessageDialog(this, "加载统计信息异常: " + e.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    /**
     * 更新统计卡片
     */
    @SuppressWarnings("unchecked")
    private void updateStatisticsCards(JPanel contentPanel, Map<String, Object> data) {
        Component[] components = contentPanel.getComponents();
        
        // 解析服务器返回的Map<RecommendStatus, Long>格式数据
        Map<String, Object> statusCounts = (Map<String, Object>) data;
        
        // 计算总数
        long total = 0;
        long pending = 0;
        long approved = 0;
        long rejected = 0;
        
        // 遍历统计结果
        for (Map.Entry<String, Object> entry : statusCounts.entrySet()) {
            String status = entry.getKey();
            Object count = entry.getValue();
            long countValue = count instanceof Number ? ((Number) count).longValue() : 0;
            total += countValue;
            
            switch (status) {
                case "PENDING":
                    pending = countValue;
                    break;
                case "APPROVED":
                    approved = countValue;
                    break;
                case "REJECTED":
                    rejected = countValue;
                    break;
            }
        }
        
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof JPanel) {
                JPanel card = (JPanel) components[i];
                JLabel valueLabel = getValueLabelFromPanel(card);
                
                if (valueLabel != null) {
                    switch (i) {
                        case 0: // 总荐购数
                            valueLabel.setText(String.valueOf(total));
                            break;
                        case 1: // 待审核
                            valueLabel.setText(String.valueOf(pending));
                            break;
                        case 2: // 已通过
                            valueLabel.setText(String.valueOf(approved));
                            break;
                        case 3: // 已拒绝
                            valueLabel.setText(String.valueOf(rejected));
                            break;
                    }
                }
            }
        }
    }
    
    /**
     * 创建导航按钮
     */
    private JButton createNavButton(String text, boolean selected) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(110, 40));
        button.setMaximumSize(new Dimension(110, 40));
        button.setMinimumSize(new Dimension(110, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 设置选中和未选中的样式
        if (selected) {
            button.setBackground(new Color(0, 123, 255));
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(248, 249, 250));
            button.setForeground(new Color(73, 80, 87));
        }
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!button.getBackground().equals(new Color(0, 123, 255))) {
                    button.setBackground(new Color(220, 220, 220));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!button.getBackground().equals(new Color(0, 123, 255))) {
                    button.setBackground(new Color(248, 249, 250));
                }
            }
        });
        
        return button;
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
        // 刷新所有数据
        loadPendingRecommendations();
        loadAllRecommendations();
    }
    
    /**
     * 自动刷新所有数据
     */
    private void autoRefresh() {
        // 刷新所有数据以确保界面显示最新状态
        loadPendingRecommendations();
        loadAllRecommendations();
    }
    
    
}

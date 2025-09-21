package com.vcampus.client.core.ui.student;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.client.core.ui.UIUtils;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * 校园集市评论面板
 * 显示所有帖子，支持发表新帖子
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class CommentPanel extends JPanel {
    
    private NettyClient nettyClient;
    
    // UI组件
    private JList<CommentItem> commentList;
    private DefaultListModel<CommentItem> listModel;
    private JTextField inputField;
    private JButton postButton;
    private JButton refreshButton;
    private JLabel statusLabel;
    private Timer refreshTimer;
    
    // 数据存储
    private List<Map<String, Object>> comments;
    private Map<Integer, Boolean> likeStatusMap = new java.util.HashMap<>();
    private Set<Integer> processingLikes = new java.util.HashSet<>(); // 防止重复点击
    
    // 图标缓存
    private javax.swing.ImageIcon likeYesIcon;
    private javax.swing.ImageIcon likeNoIcon;
    
    // 样式常量
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);      // 现代蓝
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);       // 翠绿
    private static final Color SURFACE_COLOR = new Color(248, 250, 252);     // 浅灰背景
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);         // 深灰文字
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);    // 中灰文字
    private static final Color CARD_BG = Color.WHITE;                        // 卡片背景
    private static final Color BORDER_COLOR = new Color(226, 232, 240);      // 边框色
    private static final Color HOVER_COLOR = new Color(249, 250, 251);       // 悬停色
    
    public CommentPanel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        
        // 加载图标
        loadIcons();
        
        initUI();
        loadComments();
        startAutoRefresh();
    }
    
    private void loadIcons() {
        // 加载SVG图标，设置为较小的尺寸（12x12像素）
        likeYesIcon = UIUtils.loadIcon("/figures/like_yes.svg", 12, 12);
        likeNoIcon = UIUtils.loadIcon("/figures/like_no.svg", 12, 12);
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(SURFACE_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);
        
        // 创建主内容面板
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
        
        // 创建底部输入面板
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // 创建标题容器
        JPanel titleContainer = new JPanel(new BorderLayout());
        titleContainer.setOpaque(false);
        
        // 标题
        JLabel titleLabel = new JLabel("简易校园交流");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        
        // 副标题
        JLabel subtitleLabel = new JLabel("分享想法，交流心得，共建和谐校园");
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        
        titleContainer.add(titleLabel, BorderLayout.NORTH);
        titleContainer.add(subtitleLabel, BorderLayout.SOUTH);
        
        // 状态标签容器
        JPanel statusContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusContainer.setOpaque(false);
        
        statusLabel = new JLabel("正在加载...");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        statusLabel.setBackground(new Color(248, 250, 252));
        statusLabel.setOpaque(true);
        
        statusContainer.add(statusLabel);
        
        panel.add(titleContainer, BorderLayout.WEST);
        panel.add(statusContainer, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // 创建评论列表
        listModel = new DefaultListModel<>();
        commentList = new JList<>(listModel);
        commentList.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        commentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        commentList.setCellRenderer(new CommentListCellRenderer());
        
        // 设置列表行高，使评论条更宽
        commentList.setFixedCellHeight(80);
        
        // 添加点击事件监听器
        commentList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // 支持单击和双击点赞
                if (e.getClickCount() == 1 || e.getClickCount() == 2) {
                    int index = commentList.locationToIndex(e.getPoint());
                    if (index >= 0 && index < listModel.getSize()) {
                        CommentItem item = listModel.getElementAt(index);
                        
                        if (item.commentId != null) {
                            // 添加视觉反馈
                            commentList.setSelectedIndex(index);
                            toggleLike(item.commentId);
                        }
                    }
                }
            }
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                int index = commentList.locationToIndex(e.getPoint());
                if (index >= 0 && index < listModel.getSize()) {
                    CommentListCellRenderer renderer = (CommentListCellRenderer) commentList.getCellRenderer();
                    if (renderer != null) {
                        renderer.setHovered(true);
                        commentList.repaint();
                    }
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                CommentListCellRenderer renderer = (CommentListCellRenderer) commentList.getCellRenderer();
                if (renderer != null) {
                    renderer.setHovered(false);
                    commentList.repaint();
                }
            }
        });
        
        // 滚动面板
        JScrollPane scrollPane = new JScrollPane(commentList);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            "帖子列表",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 14),
            TEXT_PRIMARY
        ));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        // 设置列表背景色和间距
        commentList.setBackground(new Color(248, 250, 252));
        commentList.setSelectionBackground(new Color(230, 244, 255));
        commentList.setSelectionForeground(TEXT_PRIMARY);
        
        // 添加卡片间距
        commentList.setFixedCellHeight(80);
        commentList.setCellRenderer(new CommentListCellRenderer());
        
        // 设置列表间距
        commentList.setLayoutOrientation(JList.VERTICAL);
        commentList.setVisibleRowCount(-1);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 工具栏
        JPanel toolbar = createToolbar();
        panel.add(toolbar, BorderLayout.NORTH);
        
        return panel;
    }
    
    private JPanel createToolbar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // 左侧按钮区域
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setOpaque(false);
        
        refreshButton = new JButton("刷新") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制圆角背景
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // 绘制文字
                g2d.setColor(getForeground());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        refreshButton.setFont(new Font("微软雅黑", Font.BOLD, 13));
        refreshButton.setBackground(PRIMARY_COLOR);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setPreferredSize(new Dimension(100, 36));
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.addActionListener(e -> loadComments());
        
        // 添加悬停效果
        refreshButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                refreshButton.setBackground(PRIMARY_COLOR.brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                refreshButton.setBackground(PRIMARY_COLOR);
            }
        });
        
        leftPanel.add(refreshButton);
        
        // 右侧提示区域
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightPanel.setOpaque(false);
        
        JLabel infoLabel = new JLabel("提示：单击或双击帖子可点赞/取消点赞");
        infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        infoLabel.setForeground(TEXT_SECONDARY);
        infoLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        infoLabel.setBackground(new Color(248, 250, 252));
        infoLabel.setOpaque(true);
        
        rightPanel.add(infoLabel);
        
        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        // 创建渐变背景面板
        JPanel gradientPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 创建渐变背景
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(248, 250, 252),
                    0, getHeight(), new Color(241, 245, 249)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                g2d.dispose();
            }
        };
        gradientPanel.setLayout(panel.getLayout());
        gradientPanel.setBorder(panel.getBorder());
        
        // 将原有组件添加到渐变面板
        for (Component comp : panel.getComponents()) {
            gradientPanel.add(comp);
        }
        
        panel = gradientPanel;
        
        // 标题区域
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("发表新帖子");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        JLabel descLabel = new JLabel("分享你的想法，与同学们一起交流");
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_SECONDARY);
        
        titlePanel.add(titleLabel, BorderLayout.NORTH);
        titlePanel.add(descLabel, BorderLayout.SOUTH);
        
        // 输入区域
        JPanel inputArea = new JPanel(new BorderLayout(15, 0));
        inputArea.setOpaque(false);
        
        inputField = new JTextField();
        inputField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        inputField.setToolTipText("输入帖子内容（最多60字符）");
        inputField.setBackground(Color.WHITE);
        
        // 字符计数标签
        JLabel charCountLabel = new JLabel("0/60");
        charCountLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        charCountLabel.setForeground(TEXT_SECONDARY);
        charCountLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        
        // 监听输入变化
        inputField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateCharCount();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateCharCount();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateCharCount();
            }
            
            private void updateCharCount() {
                int length = inputField.getText().length();
                charCountLabel.setText(length + "/60");
                charCountLabel.setForeground(length > 60 ? Color.RED : TEXT_SECONDARY);
                postButton.setEnabled(length > 0 && length <= 60);
            }
        });
        
        inputArea.add(inputField, BorderLayout.CENTER);
        inputArea.add(charCountLabel, BorderLayout.EAST);
        
        // 按钮区域
        JPanel buttonArea = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonArea.setOpaque(false);
        
        postButton = new JButton("发表") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制圆角背景
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // 绘制文字
                g2d.setColor(getForeground());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        postButton.setFont(new Font("微软雅黑", Font.BOLD, 13));
        postButton.setBackground(SUCCESS_COLOR);
        postButton.setForeground(Color.WHITE);
        postButton.setFocusPainted(false);
        postButton.setBorderPainted(false);
        postButton.setPreferredSize(new Dimension(100, 40));
        postButton.setEnabled(false);
        postButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        postButton.addActionListener(e -> postComment());
        
        // 添加悬停效果
        postButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (postButton.isEnabled()) {
                    postButton.setBackground(SUCCESS_COLOR.brighter());
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (postButton.isEnabled()) {
                    postButton.setBackground(SUCCESS_COLOR);
                }
            }
        });
        
        // 回车键发表
        inputField.addActionListener(e -> {
            if (postButton.isEnabled()) {
                postComment();
            }
        });
        
        buttonArea.add(postButton);
        
        panel.add(titlePanel, BorderLayout.NORTH);
        panel.add(inputArea, BorderLayout.CENTER);
        panel.add(buttonArea, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadComments() {
        statusLabel.setText("正在加载...");
        statusLabel.setForeground(TEXT_SECONDARY);
        
        CompletableFuture.supplyAsync(() -> {
            try {
                Request request = new Request("comment/handleRequest")
                        .addParam("action", "GET_ALL_COMMENTS");
                
                Response response = nettyClient.sendRequest(request).get();
                return response;
            } catch (Exception e) {
                log.error("加载评论时发生错误", e);
                return null;
            }
        }).thenAccept(response -> {
            SwingUtilities.invokeLater(() -> {
                if (response != null && response.isSuccess()) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> commentData = (List<Map<String, Object>>) response.getData();
                    updateCommentList(commentData);
                    statusLabel.setText("加载完成 - 共 " + commentData.size() + " 条帖子");
                    statusLabel.setForeground(SUCCESS_COLOR);
                } else {
                    statusLabel.setText("加载失败: " + (response != null ? response.getMessage() : "网络错误"));
                    statusLabel.setForeground(Color.RED);
                }
            });
        });
    }
    
    private void updateCommentList(List<Map<String, Object>> commentData) {
        this.comments = commentData;
        listModel.clear();
        
        for (Map<String, Object> comment : commentData) {
            String cardNum = comment.get("cardNum").toString();
            String content = comment.get("content").toString();
            String postTime = comment.get("postTime").toString();
            
            // 安全地转换数值类型
            Integer commentId = null;
            Integer likeCount = 0;
            
            Object commentIdObj = comment.get("commentId");
            if (commentIdObj instanceof Number) {
                commentId = ((Number) commentIdObj).intValue();
            }
            
            Object likeCountObj = comment.get("likeCount");
            if (likeCountObj instanceof Number) {
                likeCount = ((Number) likeCountObj).intValue();
            }
            
            // 格式化时间
            try {
                LocalDateTime dateTime = LocalDateTime.parse(postTime.replace(" ", "T"));
                String formattedTime = dateTime.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
                postTime = formattedTime;
            } catch (Exception e) {
                // 如果时间解析失败，使用原始时间
            }
            
            // 检查点赞状态
            Boolean hasLiked = likeStatusMap.getOrDefault(commentId, false);
            
            // 创建评论数据对象，包含所有需要的信息
            CommentItem commentItem = new CommentItem(commentId, postTime, cardNum, content, likeCount != null ? likeCount : 0, hasLiked);
            listModel.addElement(commentItem);
        }
    }
    
    private void postComment() {
        String content = inputField.getText().trim();
        
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入帖子内容", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (content.length() > 60) {
            JOptionPane.showMessageDialog(this, "帖子内容不能超过60字符", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        postButton.setEnabled(false);
        postButton.setText("发表中...");
        
        CompletableFuture.supplyAsync(() -> {
            try {
                Request request = new Request("comment/handleRequest")
                        .addParam("action", "ADD_COMMENT")
                        .addParam("content", content);
                
                Response response = nettyClient.sendRequest(request).get();
                return response;
            } catch (Exception e) {
                log.error("发表评论时发生错误", e);
                return null;
            }
        }).thenAccept(response -> {
            SwingUtilities.invokeLater(() -> {
                postButton.setEnabled(true);
                postButton.setText("发表");
                
                if (response != null && response.isSuccess()) {
                    inputField.setText("");
                    loadComments();
                    JOptionPane.showMessageDialog(this, "帖子发表成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "发表失败: " + (response != null ? response.getMessage() : "网络错误"), 
                        "错误", 
                        JOptionPane.ERROR_MESSAGE);
                }
            });
        });
    }
    
    private void startAutoRefresh() {
        refreshTimer = new Timer(30000, e -> loadComments()); // 每30秒刷新一次
        refreshTimer.start();
    }
    
    public void stopAutoRefresh() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }
    
    /**
     * 切换点赞状态
     */
    private void toggleLike(Integer commentId) {
        // 防止重复点击
        if (processingLikes.contains(commentId)) {
            return;
        }
        
        processingLikes.add(commentId);
        
        // 立即更新UI显示点赞状态（乐观更新）
        Boolean currentStatus = likeStatusMap.getOrDefault(commentId, false);
        likeStatusMap.put(commentId, !currentStatus);
        updateCommentList(comments); // 立即刷新显示
        
        CompletableFuture.supplyAsync(() -> {
            try {
                Request request = new Request("comment/handleRequest")
                        .addParam("action", "TOGGLE_LIKE")
                        .addParam("commentId", commentId.toString());
                
                Response response = nettyClient.sendRequest(request).get();
                return response;
            } catch (Exception e) {
                log.error("切换点赞状态时发生错误", e);
                return null;
            }
        }).thenAccept(response -> {
            SwingUtilities.invokeLater(() -> {
                // 移除处理标记
                processingLikes.remove(commentId);
                
                if (response != null && response.isSuccess()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) response.getData();
                    
                    // 安全地转换数值类型
                    Integer newLikeCount = 0;
                    Object likeCountObj = result.get("likeCount");
                    if (likeCountObj instanceof Number) {
                        newLikeCount = ((Number) likeCountObj).intValue();
                    }
                    
                    Boolean hasLiked = false;
                    Object hasLikedObj = result.get("hasLiked");
                    if (hasLikedObj instanceof Boolean) {
                        hasLiked = (Boolean) hasLikedObj;
                    }
                    
                    // 更新点赞状态
                    likeStatusMap.put(commentId, hasLiked);
                    
                    // 更新评论数据
                    for (Map<String, Object> comment : comments) {
                        Object commentIdObj = comment.get("commentId");
                        Integer currentCommentId = null;
                        if (commentIdObj instanceof Number) {
                            currentCommentId = ((Number) commentIdObj).intValue();
                        }
                        
                        if (currentCommentId != null && currentCommentId.equals(commentId)) {
                            comment.put("likeCount", newLikeCount);
                            break;
                        }
                    }
                    
                    // 刷新显示
                    updateCommentList(comments);
                } else {
                    // 点赞失败，回滚UI状态
                    Boolean rollbackStatus = likeStatusMap.getOrDefault(commentId, false);
                    likeStatusMap.put(commentId, !rollbackStatus); // 回滚到之前的状态
                    updateCommentList(comments);
                    
                    // 显示错误信息
                    JOptionPane.showMessageDialog(this, 
                        "点赞操作失败: " + (response != null ? response.getMessage() : "网络错误"), 
                        "错误", 
                        JOptionPane.ERROR_MESSAGE);
                }
            });
        });
    }
    
    /**
     * 评论项数据类
     */
    public static class CommentItem {
        public final Integer commentId;
        public final String postTime;
        public final String cardNum;
        public final String content;
        public final Integer likeCount;
        public final Boolean hasLiked;
        
        public CommentItem(Integer commentId, String postTime, String cardNum, String content, Integer likeCount, Boolean hasLiked) {
            this.commentId = commentId;
            this.postTime = postTime;
            this.cardNum = cardNum;
            this.content = content;
            this.likeCount = likeCount;
            this.hasLiked = hasLiked;
        }
    }
    
    /**
     * 自定义列表单元格渲染器 - 卡片样式
     */
    private class CommentListCellRenderer extends JPanel implements ListCellRenderer<CommentItem> {
        private JLabel timeLabel;
        private JLabel userLabel;
        private JLabel contentLabel;
        private JLabel likeLabel;
        private JLabel iconLabel;
        private boolean isSelected = false;
        private boolean isHovered = false;
        
        public CommentListCellRenderer() {
            setLayout(new BorderLayout(0, 0));
            setOpaque(true);
            
            // 创建时间标签 - 增大字号
            timeLabel = new JLabel();
            timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            timeLabel.setForeground(TEXT_SECONDARY);
            
            // 创建用户标签 - 增大字号
            userLabel = new JLabel();
            userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            userLabel.setForeground(TEXT_SECONDARY);
            
            // 创建内容标签 - 增大字号
            contentLabel = new JLabel();
            contentLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
            contentLabel.setForeground(TEXT_PRIMARY);
            
            // 创建点赞标签（包含图标和数字）- 增大字号
            likeLabel = new JLabel();
            likeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            likeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            
            // 创建图标标签
            iconLabel = new JLabel();
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            // 布局
            JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            leftPanel.setOpaque(false);
            leftPanel.add(timeLabel);
            leftPanel.add(Box.createHorizontalStrut(8));
            leftPanel.add(userLabel);
            
            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            rightPanel.setOpaque(false);
            rightPanel.add(iconLabel);
            rightPanel.add(Box.createHorizontalStrut(3));
            rightPanel.add(likeLabel);
            
            add(leftPanel, BorderLayout.WEST);
            add(contentLabel, BorderLayout.CENTER);
            add(rightPanel, BorderLayout.EAST);
            
            // 设置卡片样式
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
            ));
        }
        
        @Override
        public Component getListCellRendererComponent(JList<? extends CommentItem> list, CommentItem value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
                return this;
            }
            
            // 保存状态
            this.isSelected = isSelected;
            
            // 设置时间
            timeLabel.setText("[" + value.postTime + "]");
            
            // 设置用户，增加卡号后的间距
            userLabel.setText(value.cardNum + ": ");
            
            // 设置内容，在内容和点赞之间添加6个空格
            contentLabel.setText(value.content + "      ");
            
            // 根据点赞数选择图标和颜色
            javax.swing.ImageIcon iconToUse;
            Color likeColor;
            if (value.likeCount > 0) {
                iconToUse = likeYesIcon;
                likeColor = new Color(220, 38, 38); // 红色
            } else {
                iconToUse = likeNoIcon;
                likeColor = TEXT_SECONDARY; // 灰色
            }
            
            // 设置图标
            if (iconToUse != null) {
                iconLabel.setIcon(iconToUse);
            } else {
                // 如果图标加载失败，使用emoji作为后备
                iconLabel.setText(value.likeCount > 0 ? "❤️" : "🤍");
                iconLabel.setIcon(null);
            }
            
            // 设置点赞数
            likeLabel.setText(String.valueOf(value.likeCount));
            likeLabel.setForeground(likeColor);
            
            // 设置卡片背景色和边框
            updateCardAppearance();
            
            return this;
        }
        
        /**
         * 更新卡片外观
         */
        private void updateCardAppearance() {
            if (isSelected) {
                // 选中状态：蓝色背景，深色边框
                setBackground(new Color(230, 244, 255));
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(59, 130, 246), 2),
                    BorderFactory.createEmptyBorder(16, 20, 16, 20)
                ));
            } else if (isHovered) {
                // 悬停状态：浅灰色背景，深色边框
                setBackground(new Color(248, 250, 252));
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(156, 163, 175), 1),
                    BorderFactory.createEmptyBorder(16, 20, 16, 20)
                ));
            } else {
                // 默认状态：白色背景，浅色边框
                setBackground(Color.WHITE);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                    BorderFactory.createEmptyBorder(16, 20, 16, 20)
                ));
            }
        }
        
        /**
         * 重写paintComponent方法添加圆角和阴影效果
         */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            
            // 绘制阴影
            g2d.setColor(new Color(0, 0, 0, 20));
            g2d.fillRoundRect(2, 2, width - 4, height - 4, 8, 8);
            
            // 绘制卡片背景
            g2d.setColor(getBackground());
            g2d.fillRoundRect(0, 0, width - 2, height - 2, 8, 8);
            
            // 绘制边框
            if (isSelected) {
                g2d.setColor(new Color(59, 130, 246));
                g2d.setStroke(new BasicStroke(2));
            } else if (isHovered) {
                g2d.setColor(new Color(156, 163, 175));
                g2d.setStroke(new BasicStroke(1));
            } else {
                g2d.setColor(new Color(226, 232, 240));
                g2d.setStroke(new BasicStroke(1));
            }
            g2d.drawRoundRect(0, 0, width - 2, height - 2, 8, 8);
            
            g2d.dispose();
            
            // 调用父类的paintComponent绘制内容
            super.paintComponent(g);
        }
        
        /**
         * 设置悬停状态
         */
        public void setHovered(boolean hovered) {
            this.isHovered = hovered;
            updateCardAppearance();
        }
    }
}

package com.vcampus.client.core.ui.card;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class CardPanel extends JPanel {
    private static final Logger log = LoggerFactory.getLogger(CardPanel.class);
    private final NettyClient nettyClient;
    private final Map<String, Object> userData;

    private JLabel lblName;
    private JLabel lblCardNum;
    private JLabel lblBalance;

    // 分屏相关组件
    private JSplitPane splitPane;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel detailPanel;
    private JTable transactionTable;
    private JScrollPane transactionScrollPane;

    // 字体大小常量 - 优化字体层次
    private static final int BASE_FONT_SIZE = 14;
    private static final int TITLE_FONT_SIZE = 20;
    private static final int LARGE_FONT_SIZE = 26;
    private static final int MEDIUM_FONT_SIZE = 16;
    private static final int SMALL_FONT_SIZE = 12;

    // 颜色主题常量
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);      // 蓝色主色
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);       // 绿色成功
    private static final Color WARNING_COLOR = new Color(251, 191, 36);      // 黄色警告
    private static final Color DANGER_COLOR = new Color(239, 68, 68);        // 红色危险
    private static final Color CARD_BG = new Color(248, 250, 252);           // 卡片背景
    private static final Color TEXT_PRIMARY = new Color(31, 41, 55);         // 主要文字
    private static final Color TEXT_SECONDARY = new Color(107, 114, 128);    // 次要文字

    // 右侧欢迎面板相关字段，便于在调整时访问
    private JPanel welcomePanelRoot;
    private JPanel welcomeCardPanel;
    private JLabel welcomeTitleLabel;
    private JLabel welcomeDescLabel;
    private List<JPanel> featureCardsList = new java.util.ArrayList<>();
    private List<JLabel> featureTitleLabels = new java.util.ArrayList<>();
    private List<JLabel> featureDescLabels = new java.util.ArrayList<>();

    public CardPanel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        initUI();
        loadCardSummaryAsync();
    }

    /**
     * 根据界面大小自动调整字体
     */
    private Font getScaledFont(int baseSize, int style) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double scaleFactor = Math.min(screenSize.width / 1920.0, screenSize.height / 1080.0);
        scaleFactor = Math.max(0.8, Math.min(1.2, scaleFactor));
        int scaledSize = (int) (baseSize * scaleFactor);
        return new Font("微软雅黑", style, scaledSize);
    }

    private Font getScaledFont(int baseSize) {
        return getScaledFont(baseSize, Font.PLAIN);
    }

    /**
     * 创建现代化按钮
     */
    private JButton createModernButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text) {
            private boolean isHovered = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制阴影
                g2.setColor(new Color(0, 0, 0, 20));
                g2.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 12, 12);

                // 绘制按钮背景
                Color currentColor = isHovered ? hoverColor : bgColor;
                g2.setColor(currentColor);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 10, 10);

                // 绘制文字
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }

            public void setHovered(boolean hovered) {
                this.isHovered = hovered;
                repaint();
            }
        };

        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(getScaledFont(MEDIUM_FONT_SIZE, Font.BOLD));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 45));

        // 添加鼠标悬停效果
        Color originalBg = button.getBackground();
        Color hoverBg = originalBg.brighter();

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(originalBg);
            }
        });

        return button;
    }

    /**
     * 创建信息卡片
     */
    private JPanel createInfoCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制阴影
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 15, 15);

                // 绘制卡片背景
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 12, 12);

                // 绘制边框
                g2.setColor(new Color(229, 231, 235));
                g2.drawRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 12, 12);
                g2.dispose();
            }
        };

        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setOpaque(false);
        return card;
    }

    /**
     * 公共刷新方法，用于从外部刷新校园卡余额和支付记录
     */
    public void refreshCardInfo() {
        loadCardSummaryAsync();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(CARD_BG);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // 标题区域 - 添加渐变背景
        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(59, 130, 246, 30),
                    getWidth(), getHeight(), new Color(147, 197, 253, 30)
                );
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.dispose();
            }
        };
        titlePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("校园卡中心");
        title.setFont(getScaledFont(TITLE_FONT_SIZE, Font.BOLD));
        title.setForeground(PRIMARY_COLOR);
        titlePanel.add(title);

        add(titlePanel, BorderLayout.NORTH);

        // 创建分屏布局
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        // 调整：初始仅占整体约 35% 给左侧，腾出更多空间给右侧
        splitPane.setDividerLocation(380); // 原为 450
        splitPane.setResizeWeight(0.30); // 原为 0.4，更多剩余空间分配给右侧
        splitPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        splitPane.setDividerSize(8);
        splitPane.setContinuousLayout(true);

        // 初始化左右面板
        initLeftPanel();
        initRightPanel();

        // 为左右面板设置最小与首选尺寸，保证左侧不至于过窄但右侧可扩展
        leftPanel.setMinimumSize(new Dimension(360, 0));
        leftPanel.setPreferredSize(new Dimension(380, 0));
        rightPanel.setMinimumSize(new Dimension(520, 0));

        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);

        // 当父容器尺寸变化时动态按比例（约 32~34%）重新设定分割位置，保证右侧更宽
        this.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                int totalW = getWidth();
                if (totalW > 0) {
                    int target = Math.max(360, (int)(totalW * 0.33));
                    // 避免频繁抖动：只有差异较大时才更新
                    if (Math.abs(splitPane.getDividerLocation() - target) > 8) {
                        splitPane.setDividerLocation(target);
                    }
                }
            }
        });
        // 首次布局后再按比例一次，确保启动时右侧得到更多宽度
        SwingUtilities.invokeLater(() -> {
            int totalW = getWidth();
            if (totalW > 0) splitPane.setDividerLocation(Math.max(360, (int)(totalW * 0.33)));
        });

        // 底部提示 - 美化样式
        JPanel hintPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hintPanel.setOpaque(false);
        hintPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JLabel hint = new JLabel("充值与消费操作将实时同步到服务器，点击功能按钮查看详细信息");
        hint.setFont(getScaledFont(SMALL_FONT_SIZE));
        hint.setForeground(TEXT_SECONDARY);

        hintPanel.add(hint);
        add(hintPanel, BorderLayout.SOUTH);
    }

    private void initLeftPanel() {
        leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);

        // 校园卡可视化面板 - 重新设计为现代风格
        JPanel cardVisual = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // 绘制卡片阴影
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillRoundRect(5, 5, w - 5, h - 5, 20, 20);

                // 绘制主卡片背景渐变
                GradientPaint cardGradient = new GradientPaint(
                    0, 0, new Color(34, 197, 94),
                    w, h, new Color(16, 185, 129)
                );
                g2.setPaint(cardGradient);
                g2.fillRoundRect(0, 0, w - 5, h - 5, 18, 18);

                // 绘制装饰图案
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillOval(w - 100, -50, 150, 150);
                g2.fillOval(-50, h - 100, 150, 150);

                // 绘制芯片图案
                g2.setColor(new Color(255, 255, 255, 180));
                g2.fillRoundRect(30, 50, 50, 35, 8, 8);
                g2.setColor(new Color(34, 197, 94));
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 4; j++) {
                        g2.fillRect(35 + j * 10, 55 + i * 8, 6, 4);
                    }
                }

                g2.dispose();
            }
        };

        cardVisual.setPreferredSize(new Dimension(420, 280));
        cardVisual.setLayout(new GridBagLayout());
        cardVisual.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 卡片信息标签
        lblName = new JLabel("姓名: --");
        lblName.setFont(getScaledFont(MEDIUM_FONT_SIZE, Font.BOLD));
        lblName.setForeground(Color.WHITE);

        lblCardNum = new JLabel("卡号: --");
        lblCardNum.setFont(getScaledFont(MEDIUM_FONT_SIZE, Font.BOLD));
        lblCardNum.setForeground(Color.WHITE);

        lblBalance = new JLabel("余额: -- 元");
        lblBalance.setFont(getScaledFont(24, Font.BOLD));
        lblBalance.setForeground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0; gbc.gridy = 0;
        cardVisual.add(lblName, gbc);
        gbc.gridy = 1;
        cardVisual.add(lblCardNum, gbc);
        gbc.gridy = 2;
        gbc.insets = new Insets(15, 8, 8, 8);
        cardVisual.add(lblBalance, gbc);

        // 功能按钮区域 - 重新设计
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(25, 8, 8, 8);

        JPanel buttonContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 半透明白色背景
                g2.setColor(new Color(255, 255, 255, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // 边框
                g2.setColor(new Color(255, 255, 255, 100));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        buttonContainer.setOpaque(false);
        buttonContainer.setLayout(new GridLayout(3, 1, 0, 12));
        buttonContainer.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JButton btnRecharge = createModernButton("充值", SUCCESS_COLOR, SUCCESS_COLOR.brighter());
        JButton btnCardInfo = createModernButton("卡片信息", PRIMARY_COLOR, PRIMARY_COLOR.brighter());
        JButton btnTrans = createModernButton("消费记录", new Color(139, 69, 19), new Color(160, 82, 45));

        btnRecharge.addActionListener(this::onRecharge);
        btnCardInfo.addActionListener(this::onShowCardInfo);
        btnTrans.addActionListener(this::onShowTransactions);

        buttonContainer.add(btnRecharge);
        buttonContainer.add(btnCardInfo);
        buttonContainer.add(btnTrans);

        cardVisual.add(buttonContainer, gbc);
        leftPanel.add(cardVisual, BorderLayout.CENTER);
    }

    private void initRightPanel() {
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        // 美化的默认欢迎界面
        showWelcomeInRightPanel();
    }

    /**
     * 显示欢迎界面
     */
    private void showWelcomeInRightPanel() {
        rightPanel.removeAll();
        rightPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(34, 197, 94), 2),
                "校园卡服务中心", 0, 0, getScaledFont(16, Font.BOLD), new Color(34, 197, 94)));

        // 使用字段保存以便自适应调整
        welcomePanelRoot = new JPanel();
        welcomePanelRoot.setLayout(new BoxLayout(welcomePanelRoot, BoxLayout.Y_AXIS));
        // 缩小外边距以节省垂直空间
        welcomePanelRoot.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        welcomePanelRoot.setBackground(new Color(248, 250, 252));
        welcomePanelRoot.setOpaque(true);

        // 欢迎卡片
         welcomeCardPanel = createInfoCard();
         // 缩小卡片内边距，减小高度
         welcomeCardPanel.setBorder(BorderFactory.createCompoundBorder(
             BorderFactory.createLineBorder(new Color(219, 234, 254), 1),
             BorderFactory.createEmptyBorder(12, 12, 12, 12)
         ));
         welcomeCardPanel.setBackground(new Color(219, 234, 254, 50));
         // 限制最大高度为合理值，避免内容过高或被设为 0
         welcomeCardPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

         welcomeTitleLabel = new JLabel("欢迎使用校园卡系统", SwingConstants.CENTER);
         welcomeTitleLabel.setFont(getScaledFont(LARGE_FONT_SIZE, Font.BOLD));
         welcomeTitleLabel.setForeground(new Color(29, 78, 216));
         welcomeTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

         welcomeDescLabel = new JLabel("请点击左侧功能按钮开始使用", SwingConstants.CENTER);
         welcomeDescLabel.setFont(getScaledFont(BASE_FONT_SIZE));
         welcomeDescLabel.setForeground(new Color(107, 114, 128));
         welcomeDescLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

         JPanel cardContent = new JPanel();
         cardContent.setLayout(new BoxLayout(cardContent, BoxLayout.Y_AXIS));
         cardContent.setOpaque(false);
         cardContent.add(Box.createVerticalStrut(20));
         cardContent.add(welcomeTitleLabel);
         cardContent.add(Box.createVerticalStrut(8));
         cardContent.add(welcomeDescLabel);

         welcomeCardPanel.add(cardContent, BorderLayout.CENTER);
         welcomePanelRoot.add(welcomeCardPanel);
         welcomePanelRoot.add(Box.createVerticalStrut(12));

         // 功能介绍
         String[][] features = {
            {"充值服务", "快速为校园卡充值，支持多种金额选择"},
            {"卡片信息", "查看详细的卡片信息和账户状态"},
            {"消费记录", "查看历史交易记录和消费明细"}
        };

        featureCardsList.clear();
        featureTitleLabels.clear();
        featureDescLabels.clear();

        for (String[] feature : features) {
            JPanel featureCard = createInfoCard();

            JLabel featureTitle = new JLabel(feature[0]);
            featureTitle.setFont(getScaledFont(BASE_FONT_SIZE, Font.BOLD));
            featureTitle.setForeground(new Color(55, 65, 81));

            JLabel featureDesc = new JLabel(feature[1]);
            featureDesc.setFont(getScaledFont(SMALL_FONT_SIZE));
            featureDesc.setForeground(new Color(107, 114, 128));

            JPanel featureContent = new JPanel(new BorderLayout());
            featureContent.setOpaque(false);

            JPanel featureInfo = new JPanel();
            featureInfo.setLayout(new BoxLayout(featureInfo, BoxLayout.Y_AXIS));
            featureInfo.setOpaque(false);
            featureInfo.add(featureTitle);
            featureInfo.add(Box.createVerticalStrut(3));
            featureInfo.add(featureDesc);

            featureContent.add(featureInfo, BorderLayout.CENTER);
            featureCard.add(featureContent, BorderLayout.CENTER);

            // 缩小功能卡片的最大高度，减少垂直间距
            featureCard.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
            featureCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

            welcomePanelRoot.add(featureCard);
            welcomePanelRoot.add(Box.createVerticalStrut(8));

            featureCardsList.add(featureCard);
            featureTitleLabels.add(featureTitle);
            featureDescLabels.add(featureDesc);
        }

        // 直接添加欢迎面板，减少额外滚动条（确保上方已缩小间距以适配常见视口）
        JScrollPane welcomeScroll = new JScrollPane(welcomePanelRoot, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        welcomeScroll.setBorder(null);
        welcomeScroll.getViewport().setBackground(new Color(248, 250, 252));
        welcomeScroll.getVerticalScrollBar().setUnitIncrement(16);
        rightPanel.add(welcomeScroll, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();

        // 在 rightPanel 尺寸变化或 splitPane 分割位置变化时调整布局
        rightPanel.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                adjustRightPanelLayout();
            }
        });
        splitPane.addPropertyChangeListener(evt -> {
            if ("dividerLocation".equals(evt.getPropertyName())) adjustRightPanelLayout();
        });

        // 一次性调用以应用当前尺寸
        SwingUtilities.invokeLater(this::adjustRightPanelLayout);
    }

    /**
     * 根据 rightPanel 大小调整字体、内边距与卡片间距，使服务中心内容自适应
     */
    private void adjustRightPanelLayout() {
        if (welcomePanelRoot == null || rightPanel == null) return;
        int w = rightPanel.getWidth();
        int h = rightPanel.getHeight();
        if (w <= 0) return;

        // 基于面板宽度计算缩放因子（经验值）
        double scale = (double) w / 600.0; // 600px 作为设计基准宽度
        scale = Math.max(0.7, Math.min(scale, 1.3));

        // 调整标题与描述字体
        welcomeTitleLabel.setFont(getScaledFont((int)(LARGE_FONT_SIZE * scale), Font.BOLD));
        welcomeDescLabel.setFont(getScaledFont((int)(BASE_FONT_SIZE * scale)));

        // 调整欢迎卡片内边距
        int pad = Math.max(12, (int)(30 * scale));
        welcomeCardPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(219, 234, 254), 2),
            BorderFactory.createEmptyBorder(pad, pad, pad, pad)
        ));

        // 调整功能卡片字体与内边距
        for (int i = 0; i < featureCardsList.size(); i++) {
            JPanel fc = featureCardsList.get(i);
            JLabel ft = featureTitleLabels.get(i);
            JLabel fd = featureDescLabels.get(i);
            ft.setFont(getScaledFont((int)(BASE_FONT_SIZE * scale), Font.BOLD));
            fd.setFont(getScaledFont((int)(SMALL_FONT_SIZE * scale)));
            int fpad = Math.max(12, (int)(18 * scale));
            // 修复：之前使用 compound 叠加旧 border 导致内边距不断增大，现在直接重设
            fc.setBorder(BorderFactory.createEmptyBorder(fpad, fpad, fpad, fpad));
        }

        welcomePanelRoot.revalidate();
        welcomePanelRoot.repaint();
    }

    // 添加processRecharge方法的实现
    private void processRecharge(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入充值金额", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "充值金额必须大于0", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (amount > 10000) {
                JOptionPane.showMessageDialog(this, "单次充值金额不能超过10000元", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 发送充值请求
            new Thread(() -> {
                try {
                    Request req = new Request(getUserUri()).addParam("action", "RECHARGE").addParam("amount", String.valueOf(amount));
                    req.setSession(nettyClient.getCurrentSession());
                    Response resp = nettyClient.sendRequest(req).get(8, java.util.concurrent.TimeUnit.SECONDS);

                    if (resp != null && "SUCCESS".equals(resp.getStatus())) {
                        SwingUtilities.invokeLater(() -> {
                            showRechargeSuccessInRightPanel(amount);
                            loadCardSummaryAsync(); // 刷新余额
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            String errorMsg = resp == null ? "充值失败，请重试" : resp.getMessage();
                            JOptionPane.showMessageDialog(CardPanel.this, errorMsg, "充值失败", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                } catch (Exception e) {
                    log.error("充值失败", e);
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(CardPanel.this, "充值失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的数字", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void loadCardSummaryAsync() {
        SwingUtilities.invokeLater(() -> new Thread(() -> {
            try {
                Request req = new Request(getUserUri()).addParam("action", "GET_BALANCE");
                req.setSession(nettyClient.getCurrentSession());
                Response resp = nettyClient.sendRequest(req).get(8, java.util.concurrent.TimeUnit.SECONDS);

                if (resp != null && "SUCCESS".equals(resp.getStatus())) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String,Object>) resp.getData();
                    SwingUtilities.invokeLater(() -> updateSummary(data));
                } else {
                    SwingUtilities.invokeLater(() -> showLoadError());
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> showLoadError());
            }
        }).start());
    }

    private String getUserUri() {
        Object type = userData.get("userType");
        if (type == null) type = userData.get("primaryRole");
        if (type == null) {
            Object roles = userData.get("roles");
            if (roles instanceof String[] && ((String[]) roles).length > 0) type = ((String[]) roles)[0];
            else if (roles instanceof List && !((List<?>) roles).isEmpty()) type = ((List<?>) roles).get(0);
        }
        String t = type == null ? "student" : type.toString().toLowerCase();
        if ("staff".equals(t) || "teacher".equals(t)) return "card/staff";
        return "card/student";
    }

    private void updateSummary(Map<String,Object> data) {
        if (data == null) {
            lblName.setText("姓名: --");
            lblCardNum.setText("卡号: --");
            lblBalance.setText("余额: -- 元");
            return;
        }

        Object nameObj = null;
        if (data.containsKey("name")) nameObj = data.get("name");
        else if (data.containsKey("userName")) nameObj = data.get("userName");
        else if (userData.containsKey("name")) nameObj = userData.get("name");
        else if (userData.containsKey("userName")) nameObj = userData.get("userName");
        String nameText = (nameObj == null || nameObj.toString().trim().isEmpty()) ? "--" : nameObj.toString();
        lblName.setText("姓名: " + nameText);

        Object cn = null;
        if (data.containsKey("cardNum")) cn = data.get("cardNum");
        else if (data.containsKey("card_num")) cn = data.get("card_num");
        else if (data.containsKey("CardNum")) cn = data.get("CardNum");
        else cn = userData.get("cardNum");
        if (cn == null) {
            try { var sess = nettyClient.getCurrentSession(); if (sess != null && sess.getUserId() != null) cn = sess.getUserId(); } catch (Exception ignored) {}
        }
        String cardText = "--";
        try {
            if (cn != null) {
                if (cn instanceof Number) {
                    BigDecimal bd = new BigDecimal(cn.toString());
                    cardText = bd.stripTrailingZeros().toPlainString();
                } else {
                    cardText = cn.toString();
                }
            }
        } catch (Exception ignored) {}
        lblCardNum.setText("卡号: " + cardText);

        Object bal = null;
        if (data.containsKey("balance")) bal = data.get("balance");
        else if (data.containsKey("Balance")) bal = data.get("Balance");
        else if (data.containsKey("amount")) bal = data.get("amount");
        else if (data.containsKey("Amount")) bal = data.get("Amount");
        String balText = "--";
        try {
            if (bal instanceof Number) {
                balText = new BigDecimal(bal.toString()).setScale(2, RoundingMode.HALF_UP).toPlainString();
            } else if (bal != null) {
                try {
                    BigDecimal bd = new BigDecimal(bal.toString());
                    balText = bd.setScale(2, RoundingMode.HALF_UP).toPlainString();
                } catch (Exception ex) {
                    balText = bal.toString();
                }
            }
        } catch (Exception e) { balText = "--"; }
        lblBalance.setText("余额: " + balText + " 元");
    }

    private void showLoadError() {
        lblCardNum.setText("卡号: --");
        lblBalance.setText("余额: -- 元");
    }

    private void onRecharge(ActionEvent ev) {
        showRechargeInRightPanel();
    }

    private void showRechargeInRightPanel() {
        rightPanel.removeAll();
        rightPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(34, 197, 94), 2),
                "充值服务", 0, 0, getScaledFont(20, Font.BOLD), new Color(34, 197, 94)));

        JPanel rechargePanel = new JPanel();
        rechargePanel.setLayout(new BoxLayout(rechargePanel, BoxLayout.Y_AXIS));
        rechargePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        rechargePanel.setBackground(new Color(249, 250, 251));

        JPanel balanceCard = createInfoCard();
        JLabel currentBalanceLabel = new JLabel("当前余额");
        currentBalanceLabel.setFont(getScaledFont(17, Font.BOLD));
        currentBalanceLabel.setForeground(new Color(75, 85, 99));

        String balanceText = lblBalance.getText();
        JLabel balanceValueLabel = new JLabel(balanceText);
        balanceValueLabel.setFont(getScaledFont(23, Font.BOLD));
        balanceValueLabel.setForeground(new Color(34, 197, 94));

        JPanel balanceContent = new JPanel(new BorderLayout());
        balanceContent.setOpaque(false);

        JPanel balanceInfo = new JPanel();
        balanceInfo.setLayout(new BoxLayout(balanceInfo, BoxLayout.Y_AXIS));
        balanceInfo.setOpaque(false);
        balanceInfo.add(currentBalanceLabel);
        balanceInfo.add(Box.createVerticalStrut(3));
        balanceInfo.add(balanceValueLabel);

        balanceContent.add(balanceInfo, BorderLayout.CENTER);
        balanceCard.add(balanceContent, BorderLayout.CENTER);

        rechargePanel.add(balanceCard);
        rechargePanel.add(Box.createVerticalStrut(10));

        JPanel amountCard = createInfoCard();
        JLabel amountLabel = new JLabel("充值金额");
        amountLabel.setFont(getScaledFont(17, Font.BOLD));
        amountLabel.setForeground(new Color(75, 85, 99));

        JTextField amountField = new JTextField(12);
        amountField.setFont(getScaledFont(20));
        amountField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        amountField.setBackground(Color.WHITE);

        JLabel unitLabel = new JLabel("元");
        unitLabel.setFont(getScaledFont(17));
        unitLabel.setForeground(new Color(107, 114, 128));

        JPanel amountInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
        amountInputPanel.setOpaque(false);
        amountInputPanel.add(amountLabel);

        JPanel fieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        fieldPanel.setOpaque(false);
        fieldPanel.add(amountField);
        fieldPanel.add(Box.createHorizontalStrut(6));
        fieldPanel.add(unitLabel);

        JPanel amountContent = new JPanel();
        amountContent.setLayout(new BoxLayout(amountContent, BoxLayout.Y_AXIS));
        amountContent.setOpaque(false);
        amountContent.add(amountInputPanel);
        amountContent.add(Box.createVerticalStrut(5));
        amountContent.add(fieldPanel);

        amountCard.add(amountContent, BorderLayout.CENTER);
        rechargePanel.add(amountCard);
        rechargePanel.add(Box.createVerticalStrut(10));

        JPanel quickCard = createInfoCard();
        JLabel quickLabel = new JLabel("快速充值");
        quickLabel.setFont(getScaledFont(17, Font.BOLD));
        quickLabel.setForeground(new Color(75, 85, 99));

        JPanel quickHeaderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        quickHeaderPanel.setOpaque(false);
        quickHeaderPanel.add(quickLabel);

        JPanel quickButtonsPanel = new JPanel(new GridLayout(2, 3, 8, 8));
        quickButtonsPanel.setOpaque(false);
        quickButtonsPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        int[] quickAmounts = {20, 50, 100, 200, 500, 1000};
        Color greenColor = new Color(34, 197, 94);

        for (int amount : quickAmounts) {
            JButton quickBtn = new JButton("<html><div style='text-align: center;'>" +
                                         "<b>" + amount + "</b><br><small>元</small></div></html>");
            quickBtn.setFont(getScaledFont(15));
            quickBtn.setBackground(Color.WHITE);
            quickBtn.setForeground(greenColor);
            quickBtn.setFocusPainted(false);
            quickBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(greenColor, 2),
                BorderFactory.createEmptyBorder(6, 4, 6, 4)
            ));
            quickBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            quickBtn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent evt) {
                    quickBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(greenColor, 3),
                        BorderFactory.createEmptyBorder(5, 3, 5, 3)
                    ));
                }
                public void mouseExited(MouseEvent evt) {
                    quickBtn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(greenColor, 2),
                        BorderFactory.createEmptyBorder(6, 4, 6, 4)
                    ));
                }
            });

            quickBtn.addActionListener(e -> amountField.setText(String.valueOf(amount)));
            quickButtonsPanel.add(quickBtn);
        }

        JPanel quickContent = new JPanel();
        quickContent.setLayout(new BoxLayout(quickContent, BoxLayout.Y_AXIS));
        quickContent.setOpaque(false);
        quickContent.add(quickHeaderPanel);
        quickContent.add(quickButtonsPanel);

        quickCard.add(quickContent, BorderLayout.CENTER);
        rechargePanel.add(quickCard);
        rechargePanel.add(Box.createVerticalStrut(15));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setOpaque(true);
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(34, 197, 94), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JButton confirmBtn = createModernButton("确认充值", new Color(34, 197, 94), new Color(34, 197, 94).brighter());
        confirmBtn.setFont(getScaledFont(18, Font.BOLD));
        confirmBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        confirmBtn.setPreferredSize(new Dimension(100, 35));
        confirmBtn.setOpaque(true);
        confirmBtn.addActionListener(e -> processRecharge(amountField.getText().trim()));

        buttonPanel.add(confirmBtn);

        rechargePanel.add(buttonPanel);
        rechargePanel.add(Box.createVerticalStrut(10));

        JScrollPane scrollPane = new JScrollPane(rechargePanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(249, 250, 251));

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setBlockIncrement(64);

        rightPanel.add(scrollPane, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void showRechargeSuccessInRightPanel(double amount) {
        rightPanel.removeAll();
        rightPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(34, 197, 94), 2),
                "充值成功", 0, 0, getScaledFont(16, Font.BOLD), new Color(34, 197, 94)));

        JPanel successPanel = new JPanel();
        successPanel.setLayout(new BoxLayout(successPanel, BoxLayout.Y_AXIS));
        successPanel.setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30));
        successPanel.setBackground(new Color(240, 253, 244));

        JPanel successCard = createInfoCard();
        successCard.setBackground(Color.WHITE);

        JLabel successMessage = new JLabel("充值成功！", SwingConstants.CENTER);
        successMessage.setFont(getScaledFont(LARGE_FONT_SIZE, Font.BOLD));
        successMessage.setForeground(new Color(34, 197, 94));
        successMessage.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel amountMessage = new JLabel("+ " + amount + " 元", SwingConstants.CENTER);
        amountMessage.setFont(getScaledFont(28, Font.BOLD));
        amountMessage.setForeground(new Color(34, 197, 94));
        amountMessage.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tipMessage = new JLabel("余额已更新，请查看左侧卡片信息", SwingConstants.CENTER);
        tipMessage.setFont(getScaledFont(BASE_FONT_SIZE));
        tipMessage.setForeground(new Color(107, 114, 128));
        tipMessage.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(successMessage);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(amountMessage);
        contentPanel.add(Box.createVerticalStrut(15));
        contentPanel.add(tipMessage);

        successCard.add(contentPanel, BorderLayout.CENTER);
        successPanel.add(successCard);
        successPanel.add(Box.createVerticalStrut(30));

        JButton backBtn = createModernButton("返回主页", new Color(34, 197, 94), new Color(34, 197, 94).brighter());
        backBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        backBtn.addActionListener(e -> showWelcomeInRightPanel());

        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonContainer.setOpaque(false);
        buttonContainer.add(backBtn);
        successPanel.add(buttonContainer);

        rightPanel.add(successPanel, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    @SuppressWarnings("unchecked")
    private void onShowTransactions(ActionEvent ev) {
        new Thread(() -> {
            try {
                Request req = new Request(getUserUri()).addParam("action", "TRANS_LIST");
                req.setSession(nettyClient.getCurrentSession());
                Response resp = nettyClient.sendRequest(req).get(8, java.util.concurrent.TimeUnit.SECONDS);

                if (resp != null && "SUCCESS".equals(resp.getStatus())) {
                    Object data = resp.getData();
                    SwingUtilities.invokeLater(() -> showTransactionsInRightPanel((List<Map<String,Object>>)data));
                } else {
                    SwingUtilities.invokeLater(() -> showErrorInRightPanel("查询交易记录失败"));
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> showErrorInRightPanel("获取交易记录失败：" + e.getMessage()));
            }
        }).start();
    }

    private void showTransactionsInRightPanel(List<Map<String,Object>> list) {
        rightPanel.removeAll();
        rightPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(59, 130, 246), 2),
                "消费记录", 0, 0, getScaledFont(16, Font.BOLD), new Color(59, 130, 246)));

        JPanel transPanel = new JPanel(new BorderLayout());
        transPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        transPanel.setBackground(new Color(248, 250, 252));

        if (list == null) list = java.util.Collections.emptyList();

        JPanel statsCard = createInfoCard();

        JLabel recordCountLabel = new JLabel("交易记录");
        recordCountLabel.setFont(getScaledFont(BASE_FONT_SIZE, Font.BOLD));
        recordCountLabel.setForeground(new Color(75, 85, 99));

        JLabel countValue = new JLabel(list.size() + " 条");
        countValue.setFont(getScaledFont(20, Font.BOLD));
        countValue.setForeground(new Color(59, 130, 246));

        JPanel statsContent = new JPanel(new BorderLayout());
        statsContent.setOpaque(false);

        JPanel statsInfo = new JPanel();
        statsInfo.setLayout(new BoxLayout(statsInfo, BoxLayout.Y_AXIS));
        statsInfo.setOpaque(false);
        statsInfo.add(recordCountLabel);
        statsInfo.add(Box.createVerticalStrut(5));
        statsInfo.add(countValue);

        statsContent.add(statsInfo, BorderLayout.CENTER);
        statsCard.add(statsContent, BorderLayout.CENTER);

        transPanel.add(statsCard, BorderLayout.NORTH);

        if (list.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
            emptyPanel.setOpaque(false);
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

            JLabel emptyText = new JLabel("暂无交易记录", SwingConstants.CENTER);
            emptyText.setFont(getScaledFont(16, Font.BOLD));
            emptyText.setForeground(new Color(107, 114, 128));
            emptyText.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel emptyTip = new JLabel("您还没有任何消费记录", SwingConstants.CENTER);
            emptyTip.setFont(getScaledFont(SMALL_FONT_SIZE));
            emptyTip.setForeground(new Color(156, 163, 175));
            emptyTip.setAlignmentX(Component.CENTER_ALIGNMENT);

            emptyPanel.add(emptyText);
            emptyPanel.add(Box.createVerticalStrut(8));
            emptyPanel.add(emptyTip);

            transPanel.add(emptyPanel, BorderLayout.CENTER);
        } else {
            String[] columns = {"金额", "类型", "时间"};
            Object[][] data = new Object[list.size()][3];

            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> record = list.get(i);
                data[i][0] = record.getOrDefault("amount", record.get("Amount"));
                data[i][1] = record.getOrDefault("type", record.get("Trans_type"));
                data[i][2] = record.getOrDefault("time", record.get("Trans_time"));
            }

            DefaultTableModel model = new DefaultTableModel(data, columns) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            transactionTable = new JTable(model);
            transactionTable.setFont(getScaledFont(13));
            transactionTable.getTableHeader().setFont(getScaledFont(13, Font.BOLD));
            transactionTable.setRowHeight((int)(35 * getScaleFactor()));
            transactionTable.setBackground(Color.WHITE);
            transactionTable.setGridColor(new Color(229, 231, 235));
            transactionTable.setSelectionBackground(new Color(219, 234, 254));
            transactionTable.setSelectionForeground(new Color(29, 78, 216));

            transactionTable.getTableHeader().setBackground(new Color(248, 250, 252));
            transactionTable.getTableHeader().setForeground(new Color(55, 65, 81));
            transactionTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(229, 231, 235)));

            transactionScrollPane = new JScrollPane(transactionTable);
            transactionScrollPane.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
            transactionScrollPane.getViewport().setBackground(Color.WHITE);
            transactionScrollPane.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

            transPanel.add(transactionScrollPane, BorderLayout.CENTER);
        }

        rightPanel.add(transPanel, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private double getScaleFactor() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double scaleFactor = Math.min(screenSize.width / 1920.0, screenSize.height / 1080.0);
        return Math.max(0.8, Math.min(1.2, scaleFactor));
    }

    private void onShowCardInfo(ActionEvent ev) {
        new Thread(() -> {
            try {
                Request req = new Request(getUserUri()).addParam("action", "GET_BALANCE");
                req.setSession(nettyClient.getCurrentSession());
                Response resp = nettyClient.sendRequest(req).get(8, java.util.concurrent.TimeUnit.SECONDS);

                if (resp != null && "SUCCESS".equals(resp.getStatus())) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) resp.getData();
                    SwingUtilities.invokeLater(() -> showCardInfoInRightPanel(data));
                } else {
                    SwingUtilities.invokeLater(() -> showErrorInRightPanel("获取卡片信息失败：" + (resp == null ? "无响应" : resp.getMessage())));
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> showErrorInRightPanel("获取卡片信息失败：" + e.getMessage()));
            }
        }).start();
    }

    private void showCardInfoInRightPanel(Map<String,Object> data) {
        rightPanel.removeAll();
        rightPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(147, 51, 234), 2),
                "卡片详细信息", 0, 0, getScaledFont(16, Font.BOLD), new Color(147, 51, 234)));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        infoPanel.setBackground(new Color(252, 250, 255));

        if (data == null) data = java.util.Collections.emptyMap();

        Object nameObj = null;
        if (data.containsKey("name")) nameObj = data.get("name");
        else if (data.containsKey("userName")) nameObj = data.get("userName");
        else if (userData.containsKey("name")) nameObj = userData.get("name");
        else if (userData.containsKey("userName")) nameObj = userData.get("userName");
        String name = (nameObj == null || nameObj.toString().trim().isEmpty()) ? "--" : nameObj.toString();

        Object cn = null;
        if (data.containsKey("cardNum")) cn = data.get("cardNum");
        else if (data.containsKey("card_num")) cn = data.get("card_num");
        else if (data.containsKey("CardNum")) cn = data.get("CardNum");
        else cn = userData.get("cardNum");
        if (cn == null) {
            try {
                var sess = nettyClient.getCurrentSession();
                if (sess != null && sess.getUserId() != null) cn = sess.getUserId();
            } catch (Exception ignored) {}
        }
        String cardText = "--";
        try {
            if (cn != null) {
                if (cn instanceof Number) cardText = new BigDecimal(cn.toString()).stripTrailingZeros().toPlainString();
                else cardText = cn.toString();
            }
        } catch (Exception ignored) {}

        Object bal = data.getOrDefault("balance", data.getOrDefault("Amount", data.get("amount")));
        String balText = "--";
        try {
            if (bal instanceof Number) {
                balText = new BigDecimal(bal.toString()).setScale(2, RoundingMode.HALF_UP).toPlainString();
            } else if (bal != null) {
                try {
                    BigDecimal bd = new BigDecimal(bal.toString());
                    balText = bd.setScale(2, RoundingMode.HALF_UP).toPlainString();
                } catch (Exception ex) {
                    balText = bal.toString();
                }
            }
        } catch (Exception e) {
            balText = "--";
        }

        String status = data.getOrDefault("status", "正常").toString();
        String expireDate = "--";

        Object expireObj = null;
        String[] expireFields = {"expireDate", "expire_date", "expiration", "validUntil", "valid_until"};
        for (String field : expireFields) {
            expireObj = data.get(field);
            if (expireObj != null) break;
        }

        if (expireObj != null) {
            try {
                if (expireObj instanceof Long) {
                    LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli((Long)expireObj), ZoneId.systemDefault());
                    expireDate = dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } else if (expireObj instanceof String) {
                    String dateStr = expireObj.toString();
                    if (dateStr.matches("\\d{4}-\\d{2}-\\d{2}.*")) {
                        expireDate = dateStr.substring(0, 10);
                    } else {
                        expireDate = dateStr;
                    }
                } else {
                    expireDate = expireObj.toString();
                }
            } catch (Exception ex) {
                expireDate = expireObj.toString();
            }
        } else {
            LocalDate defaultExpire = LocalDate.now().plusYears(4);
            expireDate = defaultExpire.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        String[][] infoData = {
            {"持卡人姓名", name, "#10B981"},
            {"卡号", cardText, "#3B82F6"},
            {"当前余额", balText + " 元", "#EF4444"},
            {"卡片状态", status, "#8B5CF6"},
            {"有效期至", expireDate, "#F59E0B"}
        };

        for (String[] info : infoData) {
            JPanel infoCard = createInfoCard();

            JLabel titleLabel = new JLabel(info[0]);
            titleLabel.setFont(getScaledFont(BASE_FONT_SIZE, Font.BOLD));
            titleLabel.setForeground(new Color(75, 85, 99));

            JLabel valueLabel = new JLabel(info[1]);
            valueLabel.setFont(getScaledFont(16, Font.BOLD));
            Color valueColor = Color.decode(info[2]);
            valueLabel.setForeground(valueColor);

            JPanel cardContent = new JPanel(new BorderLayout());
            cardContent.setOpaque(false);

            JPanel infoContent = new JPanel();
            infoContent.setLayout(new BoxLayout(infoContent, BoxLayout.Y_AXIS));
            infoContent.setOpaque(false);
            infoContent.add(titleLabel);
            infoContent.add(Box.createVerticalStrut(6));
            infoContent.add(valueLabel);

            cardContent.add(infoContent, BorderLayout.CENTER);
            infoCard.add(cardContent, BorderLayout.CENTER);

            infoPanel.add(infoCard);
            infoPanel.add(Box.createVerticalStrut(12));
        }

        // 添加滚动，防止小窗口高度不够时信息被截断
        JScrollPane infoScroll = new JScrollPane(infoPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoScroll.setBorder(null);
        infoScroll.getViewport().setBackground(new Color(252, 250, 255));
        infoScroll.getVerticalScrollBar().setUnitIncrement(16);
        rightPanel.add(infoScroll, BorderLayout.CENTER);

        // 新增：在卡片信息下方加入挂失/解除挂失按钮，允许用户自行切换卡状态
        JPanel statusActionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statusActionPanel.setOpaque(false);
        JButton btnToggleLost = createModernButton("挂失", DANGER_COLOR, DANGER_COLOR.brighter());
        btnToggleLost.setPreferredSize(new Dimension(120, 36));
        // 根据当前状态调整按钮文字
        String normalizedStatus = (data.getOrDefault("status", "正常")).toString();
        boolean isLost = "挂失".equalsIgnoreCase(normalizedStatus) || "lost".equalsIgnoreCase(normalizedStatus);
        if (isLost) {
            btnToggleLost.setText("解除挂失");
            btnToggleLost.setBackground(SUCCESS_COLOR);
        } else {
            btnToggleLost.setText("挂失");
            btnToggleLost.setBackground(DANGER_COLOR);
        }

        btnToggleLost.addActionListener(e -> {
            // 禁用按钮以防重复操作
            btnToggleLost.setEnabled(false);
            String targetStatus = btnToggleLost.getText().equals("挂失") ? "LOST" : "NORMAL";
            // 弹出确认
            int opt = JOptionPane.showConfirmDialog(this, btnToggleLost.getText().equals("挂失") ? "确认要挂失该卡？挂失后将无法进行任何消费操作。" : "确认解除挂失？", "确认", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) { btnToggleLost.setEnabled(true); return; }

            new Thread(() -> {
                try {
                    Request req = new Request(getUserUri()).addParam("action", "SET_STATUS").addParam("status", targetStatus);
                    req.setSession(nettyClient.getCurrentSession());
                    Response resp = nettyClient.sendRequest(req).get(8, java.util.concurrent.TimeUnit.SECONDS);
                    if (resp != null && "SUCCESS".equals(resp.getStatus())) {
                        SwingUtilities.invokeLater(() -> {
                            // 尝试尽快使用服务器返回的数据立即更新详细视图，提升可感知性
                            @SuppressWarnings("unchecked")
                            Map<String, Object> respData = resp.getData() instanceof Map ? (Map<String, Object>) resp.getData() : java.util.Collections.singletonMap("status", targetStatus);
                            JOptionPane.showMessageDialog(this, "卡片状态已更新", "成功", JOptionPane.INFORMATION_MESSAGE);
                            // 刷新当前面板的摘要
                            loadCardSummaryAsync();
                            // 刷新所有顶层窗口中的 CardPanel，确保全局视图同步更新（覆盖多窗口场景）
                            for (Window w : Window.getWindows()) {
                                try {
                                    if (w != null && w.isDisplayable()) findAndRefreshCardPanels(w);
                                } catch (Exception ignore) {}
                            }
                            // 更新按钮显示（根据目标状态切换文本与样式）
                            if ("LOST".equalsIgnoreCase(targetStatus)) {
                                btnToggleLost.setText("解除挂失");
                                btnToggleLost.setBackground(SUCCESS_COLOR);
                            } else {
                                btnToggleLost.setText("挂失");
                                btnToggleLost.setBackground(DANGER_COLOR);
                            }
                            // 立即用服务器返回的片段数据更新右侧详细视图，保证用户立刻看到状态变化
                            try {
                                showCardInfoInRightPanel(respData);
                            } catch (Exception ignore) {}
                            // 同时再发起完整的拉取以保证数据一致性
                            SwingUtilities.invokeLater(() -> onShowCardInfo(null));
                        });
                     } else {
                         SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, resp == null ? "操作失败" : resp.getMessage(), "错误", JOptionPane.ERROR_MESSAGE));
                     }
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "操作失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE));
                } finally {
                    SwingUtilities.invokeLater(() -> btnToggleLost.setEnabled(true));
                }
            }).start();
        });

        statusActionPanel.add(btnToggleLost);
        // 将按钮面板放到 rightPanel 的南部（如果已有组件，则添加到下方）
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(infoScroll, BorderLayout.CENTER);
        wrapper.add(statusActionPanel, BorderLayout.SOUTH);
        rightPanel.removeAll();
        rightPanel.add(wrapper, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    /**
     * 在右侧面板显示错误信息的统一方法
     */
    private void showErrorInRightPanel(String message) {
        if (rightPanel == null) return;
        rightPanel.removeAll();
        rightPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(239, 68, 68), 2),
                "错误信息", 0, 0, getScaledFont(16, Font.BOLD), new Color(239, 68, 68)));

        JPanel errorPanel = new JPanel();
        errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
        errorPanel.setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30));
        errorPanel.setBackground(new Color(254, 242, 242));

        JLabel errorLabel = new JLabel(message, SwingConstants.CENTER);
        errorLabel.setFont(getScaledFont(BASE_FONT_SIZE));
        errorLabel.setForeground(new Color(239, 68, 68));
        errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        errorPanel.add(errorLabel);

        rightPanel.add(errorPanel, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    // 在当前窗口或容器中查找所有 CardPanel 并调用 refreshCardInfo
    private void findAndRefreshCardPanels(Container root) {
        if (root == null) return;
        Container start;
        if (root instanceof RootPaneContainer) {
            start = (Container) ((RootPaneContainer) root).getContentPane();
        } else {
            start = root;
        }
        recurseRefresh(start);
    }

    private void recurseRefresh(Container c) {
        if (c == null) return;
        for (Component ch : c.getComponents()) {
            if (ch instanceof CardPanel) {
                try {
                    ((CardPanel) ch).refreshCardInfo();
                } catch (Exception ignore) {}
            } else if (ch instanceof Container) {
                recurseRefresh((Container) ch);
            }
        }
    }
}

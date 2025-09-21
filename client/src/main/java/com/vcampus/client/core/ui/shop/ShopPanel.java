package com.vcampus.client.core.ui.shop;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalQueries;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.vcampus.client.core.ui.UIUtils;

/**
 * 组合型 ShopPanel：负责组装子组件并管理表格数据与分页
 */
@Slf4j
public class ShopPanel extends JPanel {
    private final NettyClient nettyClient;
    private final Map<String, Object> userData;
    private boolean isAdminFlag;

    // 把表格抽取为子组件（去掉 final，manager 模式下为 null）
    private ProductTablePanel productTablePanel;
    private ProductGridPanel productGridPanel;

    // data
    private List<Map<String, Object>> fullProductList = new ArrayList<>();
    private int currentPage = 1;
    private int pageSize = 50;

    // subcomponents
    private SearchToolbar toolbar; // manager 模式下不需要
    private PaginationPanel pagination; // manager 模式下不需要

    // center cards
    private JPanel centerCards; // manager 模式下不需要
    private CardLayout centerLayout; // manager 模式下不需要
    // 学生/职工侧栏：我的订单显示（默认隐藏）
    private JPanel transactionPanel;
    private JPanel transactionListPanel;
    private JSplitPane mainSplit;
    // 侧栏购物车相关（使用同一侧栏容器切换显示）
    private JPanel cartListPanel;
    private JLabel cartSummaryLabel;
    private String sidebarMode = null; // null | "orders" | "cart"

    // 调试：显示当前URI与角色
    private JLabel debugLabel;
    private JButton debugToggleBtn;
    private boolean debugOn = true;

    // 侧栏购物车触发按钮
    private JButton cartButton;

    // 购物车对话框实例
    private CartDialog cartDialog;
    private boolean isCartVisible = false;

    // 本地缓存购物车数量。null 表示尚未初始化
    private Integer cachedCartCount = null;
    // 本次异步加载购物车时是否强制更新顶部购物车数字（用于删除/结算等变更后刷新）
    private boolean forceCartCountUpdateThisLoad = false;

    // 会话内乐观移除但未确认的商品 ID 集合
    private final java.util.Set<String> pendingRemovedIds = new java.util.HashSet<>();

    private int lastSidebarWidth = 300; // 调整为原来的60%（500 * 0.6 = 300）

    // 字体大小常量 - 添加响应式字体支持
    private static final int BASE_FONT_SIZE = 14;
    private static final int TITLE_FONT_SIZE = 20;
    private static final int BUTTON_FONT_SIZE = 13;
    private static final int SMALL_FONT_SIZE = 12;

    /**
     * 根据界面大小自动调整字体
     */
    private Font getScaledFont(int baseSize, int style) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double scaleFactor = Math.min(screenSize.width / 1920.0, screenSize.height / 1080.0);
        scaleFactor = Math.max(0.8, Math.min(1.2, scaleFactor)); // 限制缩放比例在0.8-1.2之间
        int scaledSize = (int) (baseSize * scaleFactor);
        return new Font("微软雅黑", style, scaledSize);
    }

    private Font getScaledFont(int baseSize) {
        return getScaledFont(baseSize, Font.PLAIN);
    }

    /**
     * 创建现代化按钮样式
     */
    private JButton createModernButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 背景
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // 文字
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(getScaledFont(BUTTON_FONT_SIZE, Font.BOLD));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public ShopPanel(NettyClient nettyClient, Map<String, Object> userData) {
        System.out.println("[RUNTIME ShopPanel LOADED (CART REMOVED)]");
        this.nettyClient = nettyClient;
        this.userData = userData;
        this.isAdminFlag = false; // 默认值

        // 统一判定：manager 或 严格 admin（仅 admin 而非 staff/teacher）都直接加载管理面板
        // 为避免误判导致普通用户无法看到商品，改为默认展示商城视图，只有在明确需要时才切换到管理面板。
        boolean detectedManager = false;
        boolean userManager = false;
        boolean adminStrict = false;
        try {
            userManager = isUserManager(userData);
            adminStrict = isAdminStrict(userData);
            detectedManager = userManager || adminStrict;
        } catch (Exception ignored) {}
        if (detectedManager) {
            // 不再直接返回管理面板，记录日志并在界面上提供一键切换按钮（后续在 header 中添加）
            System.out.println("[DEBUG] Detected manager/admin role in userData but will show shop view by default. Use 管理后台 按钮 to switch.");
            // 将标志记录到 isAdminFlag，以便 header 区域决定是否显示切换按钮
            this.isAdminFlag = true;
        }

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建带渐变背景的主面板
        JPanel backgroundPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 渐变背景 - 使用商城主题色（蓝绿色系）
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(240, 248, 255),
                    getWidth(), getHeight(), new Color(230, 247, 255)
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // 顶部区域：标题/分页在一行，工具栏在下一行
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // 创建带装饰的标题面板
        JPanel titleRow = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 标题区域背景
                GradientPaint titleGradient = new GradientPaint(
                    0, 0, new Color(72, 152, 227, 30),
                    getWidth(), getHeight(), new Color(58, 134, 255, 50)
                );
                g2.setPaint(titleGradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // 装饰圆圈
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillOval(getWidth() - 100, -30, 120, 120);
            }
        };
        titleRow.setOpaque(false);
        titleRow.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("校园商城");
        title.setFont(getScaledFont(TITLE_FONT_SIZE, Font.BOLD));
        title.setForeground(new Color(47, 79, 79));

        // 将标题和用户摘要放在同一个左侧容器
        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leftTitle.setOpaque(false);
        leftTitle.add(title);

        titleRow.add(leftTitle, BorderLayout.WEST);

        // right-side controls: pagination + cart button
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightControls.setOpaque(false);
        pagination = new PaginationPanel(pageSize);
        rightControls.add(pagination);

        // 新增购物车按钮（现代化样式）
        // 购物车图标：仅显示图片（无绿色圆角背景），点击打开购物车侧栏
        cartButton = new JButton();
        // 增大图标与按钮尺寸以便在高 DPI 屏幕上更清晰可见
        cartButton.setPreferredSize(new Dimension(52, 44));
        cartButton.setBorderPainted(false);
        cartButton.setContentAreaFilled(false);
        cartButton.setFocusPainted(false);
        cartButton.setOpaque(false);
        cartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        try {
            // 仅使用 SVG（更适合缩放），不回退到 PNG
            javax.swing.ImageIcon ic = UIUtils.loadIcon("/figures/shopcart.svg", 36, 36);
            if (ic == null) ic = UIUtils.loadIcon("/figures/icons/shopcart.svg", 36, 36);
            if (ic != null) cartButton.setIcon(ic);
         } catch (Exception ignored) {}
        cartButton.setToolTipText("购物车 (0)");
        rightControls.add(cartButton);
        // 点击切换侧栏购物车显示（若已打开则收起）
        cartButton.addActionListener(e -> toggleFloatingCart());

        // 为学生/教职工界面添加“交易记录”按钮（显示用户自己的订单/交易）
        JButton transBtn;
        try {
            // 使用与购物车相同的 SVG 尺寸以保证视觉一致性
            javax.swing.ImageIcon recIc = UIUtils.loadIcon("/figures/record.svg", 36, 36);
            if (recIc != null) {
                transBtn = new JButton();
                transBtn.setIcon(recIc);
                // 与购物车按钮保持一致的尺寸（便于对齐与可点击区域）
                transBtn.setPreferredSize(new Dimension(52, 44));
                transBtn.setBorderPainted(false);
                transBtn.setContentAreaFilled(false);
                transBtn.setFocusPainted(false);
                transBtn.setOpaque(false);
                transBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                transBtn.setToolTipText("交易记录");
            } else {
                transBtn = createModernButton("交易记录", new Color(99, 102, 241));
                transBtn.setPreferredSize(new Dimension(100, 35));
                transBtn.setToolTipText("查看交易记录");
            }
        } catch (Exception ignored) {
            transBtn = createModernButton("交易记录", new Color(99, 102, 241));
            transBtn.setPreferredSize(new Dimension(100, 35));
            transBtn.setToolTipText("查看交易记录");
        }
        rightControls.add(transBtn);
        transBtn.addActionListener(e -> {
            if (transactionPanel != null && transactionPanel.isVisible() && "orders".equals(sidebarMode)) {
                hideSidebar();
            } else {
                loadMyOrders();
            }
        });

        titleRow.add(rightControls, BorderLayout.EAST);

        // 如果当前不是 manager 模式但具备潜在管理员权限，提供一键切换管理后台按钮
        if (!userManager && (isAdminFlag || Boolean.TRUE.equals(userData.get("isAdmin")))) {
            JButton mgrBtn = createModernButton("管理后台", new Color(99, 102, 241));
            mgrBtn.setPreferredSize(new Dimension(100, 35));
            mgrBtn.setToolTipText("切换到商城管理界面");
            rightControls.add(mgrBtn);
            mgrBtn.addActionListener(e -> upgradeToManagerPanel());
            System.out.println("[DEBUG] ShopPanel 提供管理后台切换按钮 (isAdminFlag=" + isAdminFlag + ") userData=" + userData);
        }

        header.add(titleRow, BorderLayout.NORTH);

        // toolbar - 根据用户角色决定是否显示管理员控件，放在标题下方
        // 普通面板强制关闭管理员逻辑（manager/admin 检测在上方处理，不再重置 isAdminFlag）
        toolbar = new SearchToolbar(false);
        toolbar.setAdminMode(false);
        System.out.println("[DEBUG] ShopPanel constructed (normal mode, no admin features)");

        // 包装工具栏
        JPanel toolbarWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 工具栏背景
                g2.setColor(new Color(255, 255, 255, 200));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 10, 10);

                // 边框
                g2.setColor(new Color(200, 200, 200, 100));
                g2.drawRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 10, 10);
            }
        };
        toolbarWrapper.setOpaque(false);
        toolbarWrapper.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        toolbarWrapper.add(toolbar, BorderLayout.CENTER);
        header.add(toolbarWrapper, BorderLayout.SOUTH);

        backgroundPanel.add(header, BorderLayout.NORTH);

        // table panel
        productTablePanel = new ProductTablePanel(new String[]{"ID","编码","名称","价格","库存","状态","分类","描述","更新时间"});
        productGridPanel = new ProductGridPanel();

        // 根据当前用户权限决定网格的管理员模式：若 userData 显示为管理员或管理者则启用居中/管理员样式
        boolean gridAdmin = false;
        try { gridAdmin = userManager || adminStrict || Boolean.TRUE.equals(userData.get("isAdmin")); } catch (Exception ignored) {}
        productGridPanel.setAdminMode(gridAdmin);

        centerLayout = new CardLayout();
        centerCards = new JPanel(centerLayout);
        centerCards.setOpaque(false);
        centerCards.add(productGridPanel, "grid");
        centerCards.add(productTablePanel, "table");

        // 构建右侧订单侧栏（学生/职工使用）
        transactionPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 侧栏背景
                GradientPaint sidebarGradient = new GradientPaint(
                    0, 0, new Color(248, 250, 252),
                    getWidth(), getHeight(), new Color(241, 245, 249)
                );
                g2.setPaint(sidebarGradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // 边框
                g2.setColor(new Color(203, 213, 225));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
        };
        transactionPanel.setPreferredSize(new Dimension(216, 0)); // 调整为原来的60%（360 * 0.6 = 216）
        transactionPanel.setVisible(false);
        transactionPanel.setOpaque(false);

        JPanel transTop = new JPanel(new BorderLayout());
        transTop.setOpaque(false);
        JLabel transTitle = new JLabel("我的订单");
        transTitle.setFont(getScaledFont(16, Font.BOLD));
        transTitle.setForeground(new Color(71, 85, 105));
        transTitle.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        transTop.add(transTitle, BorderLayout.WEST);
        transactionPanel.add(transTop, BorderLayout.NORTH);

        transactionListPanel = new JPanel();
        transactionListPanel.setLayout(new BoxLayout(transactionListPanel, BoxLayout.Y_AXIS));
        transactionListPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        transactionListPanel.setOpaque(false);

        JScrollPane transScroll = new JScrollPane(transactionListPanel);
        transScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        transScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        // 优化滚动灵敏度：增加单位增量和块增量
        transScroll.getVerticalScrollBar().setUnitIncrement(80);  // 从50增加到80
        transScroll.getVerticalScrollBar().setBlockIncrement(320); // 从200增加到320
        // 启用平滑滚动
        transScroll.getViewport().putClientProperty("EnableWindowBlit", Boolean.TRUE);
        transScroll.setWheelScrollingEnabled(true);
        transScroll.setOpaque(false);
        transScroll.getViewport().setOpaque(false);
        transScroll.setBorder(null);
        transactionPanel.add(transScroll, BorderLayout.CENTER);

        // cartListPanel 与 summary（侧栏复用）
        cartListPanel = new JPanel();
        cartListPanel.setLayout(new BoxLayout(cartListPanel, BoxLayout.Y_AXIS));
        cartListPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        cartListPanel.setOpaque(false);
        cartSummaryLabel = new JLabel("合计: 0 件  ¥0.00");
        cartSummaryLabel.setFont(getScaledFont(14, Font.BOLD));
        cartSummaryLabel.setForeground(new Color(34, 197, 94));

        // JSplitPane布局：左侧为主内容，右侧为侧栏
        mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerCards, transactionPanel);
        mainSplit.setResizeWeight(0.7); // 左侧70%，右侧30%，让主商品区域更大
        mainSplit.setDividerSize(8);
        mainSplit.setContinuousLayout(true);
        mainSplit.setOneTouchExpandable(true); // 启用一键展开/折叠
        mainSplit.setOpaque(false);
        backgroundPanel.add(mainSplit, BorderLayout.CENTER);

        // 将视图切换按钮放到页面右下角（单独的底部右对齐面板）
        try {
            JToggleButton viewToggle = toolbar.getViewToggleButton();
            if (viewToggle != null) {
                JPanel bottomRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
                 bottomRight.setOpaque(false);
                 bottomRight.add(viewToggle);
                // 在右下角添加“我的订单”按钮，便于用户快速打开订单侧栏；优先使用 record.svg 图标
                JButton bottomMyOrdersBtn;
                try {
                    javax.swing.ImageIcon recIc2 = UIUtils.loadIcon("/figures/record.svg", 36, 36);
                    if (recIc2 != null) {
                        bottomMyOrdersBtn = new JButton();
                        bottomMyOrdersBtn.setIcon(recIc2);
                        // 与顶部按钮保持一致的视觉和可点击区域
                        bottomMyOrdersBtn.setPreferredSize(new Dimension(52, 44));
                        bottomMyOrdersBtn.setBorderPainted(false);
                        bottomMyOrdersBtn.setContentAreaFilled(false);
                        bottomMyOrdersBtn.setFocusPainted(false);
                        bottomMyOrdersBtn.setOpaque(false);
                        bottomMyOrdersBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        bottomMyOrdersBtn.setToolTipText("我的订单");
                    } else {
                        bottomMyOrdersBtn = createModernButton("我的订单", new Color(59, 130, 246));
                        bottomMyOrdersBtn.setPreferredSize(new Dimension(100, 35));
                        bottomMyOrdersBtn.setToolTipText("我的订单");
                    }
                } catch (Exception ignored) {
                    bottomMyOrdersBtn = createModernButton("我的订单", new Color(59, 130, 246));
                    bottomMyOrdersBtn.setPreferredSize(new Dimension(100, 35));
                    bottomMyOrdersBtn.setToolTipText("我的订单");
                }
                bottomMyOrdersBtn.addActionListener(e -> {
                    if (transactionPanel != null && transactionPanel.isVisible() && "orders".equals(sidebarMode)) {
                        hideSidebar();
                    } else {
                        loadMyOrders();
                    }
                });
                bottomRight.add(bottomMyOrdersBtn);
                backgroundPanel.add(bottomRight, BorderLayout.SOUTH);
            }
        } catch (Exception ignored) {}

        add(backgroundPanel, BorderLayout.CENTER);

        // 根据工具栏当前视图选择显示相应卡片（确保初始视图一致）
        SwingUtilities.invokeLater(() -> {
            System.out.println("[DEBUG] Initial view decision: isAdminFlag=" + isAdminFlag + ", toolbar.isGridSelected=" + toolbar.isGridSelected());
            if (isAdminFlag) {
                centerLayout.show(centerCards, "grid");
            } else if (toolbar.isGridSelected()) {
                centerLayout.show(centerCards, "grid");
            } else {
                centerLayout.show(centerCards, "table");
            }
        });

        productGridPanel.addProductClickListener(e -> {
            try {
                String cmd = e.getActionCommand();
                if (cmd == null) return;
                int idxInPage = -1;
                if (cmd.startsWith("detail:")) {
                    idxInPage = Integer.parseInt(cmd.substring("detail:".length()));
                    int start = (currentPage-1) * pageSize;
                    int globalIndex = start + idxInPage;
                    if (globalIndex >=0 && globalIndex < fullProductList.size()) {
                        Map<String,Object> p = fullProductList.get(globalIndex);
                        ProductDetailDialog dlg = new ProductDetailDialog(SwingUtilities.getWindowAncestor(ShopPanel.this), p, nettyClient, true, null);
                        dlg.setVisible(true);
                    }
                    return;
                } else if (cmd.startsWith("select:")) {
                    idxInPage = Integer.parseInt(cmd.substring("select:".length()));
                    // 已在网格内高亮处理 selectedIndex，这里不再打开详情，仅更新选中索引
                    return;
                } else {
                    // 兼容历史数据：命令可能直接是索引
                    try { idxInPage = Integer.parseInt(cmd); } catch (Exception ex) { return; }
                    int start = (currentPage-1) * pageSize;
                    int globalIndex = start + idxInPage;
                    if (globalIndex >=0 && globalIndex < fullProductList.size()) {
                        Map<String,Object> p = fullProductList.get(globalIndex);
                        ProductDetailDialog dlg = new ProductDetailDialog(SwingUtilities.getWindowAncestor(ShopPanel.this), p, nettyClient, true, null);
                        dlg.setVisible(true);
                    }
                }
            } catch (Exception ex) { log.error("处理网格点击失败", ex); }
        });

        productTablePanel.addTableMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = productTablePanel.getSelectedRowView();
                    if (viewRow < 0) return;
                    int modelRow = productTablePanel.convertRowIndexToModel(viewRow);
                    int index = (currentPage -1) * pageSize + modelRow;
                    if (index >=0 && index < fullProductList.size()) {
                        Map<String,Object> p = fullProductList.get(index);
                        ProductDetailDialog dlg = new ProductDetailDialog(SwingUtilities.getWindowAncestor(ShopPanel.this), p, nettyClient, true, null);
                        dlg.setVisible(true);
                    }
                }
            }
        });

        // hook listeners
        hookListeners();

        // initial load
        loadProducts();
        // 启动后静默同步一次购物车数量，避免首次或返回时显示 0
        fetchCartCountSilently();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                if ("cart".equals(sidebarMode) || "orders".equals(sidebarMode)) {
                    ensureSplitLayout(true);
                    // 尺寸变化后再刷新一次遮挡宽度
                    SwingUtilities.invokeLater(ShopPanel.this::updateGridOverlayWidth);
                }
            }
        });
        // 新增：监听分隔条位置变化，实时更新遮挡宽度
        if (mainSplit != null) {
            mainSplit.addPropertyChangeListener(evt -> {
                if ("dividerLocation".equals(evt.getPropertyName())) {
                    updateGridOverlayWidth();
                }
            });
        }
    }

    // 静默仅获取购物车数量（不展开侧栏，不弹框）
    private void fetchCartCountSilently() {
        new Thread(() -> {
            try {
                Request req = new Request(getUserUri()).addParam("action","GET_CART");
                req.setSession(nettyClient.getCurrentSession());
                Response resp = nettyClient.sendRequest(req).get(8, java.util.concurrent.TimeUnit.SECONDS);
                if (resp != null && "SUCCESS".equals(resp.getStatus()) && resp.getData() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String,Object> data = (Map<String,Object>) resp.getData();
                    int count = 0;
                    try { Object c = data.get("count"); if (c!=null) count = new java.math.BigDecimal(c.toString()).intValue(); } catch (Exception ignored) {}
                    final int cc = count;
                    SwingUtilities.invokeLater(() -> {
                        // 仅在本地尚未缓存购物车数量时，才用静默获取的值初始化显示
                        if (cachedCartCount == null) {
                            cachedCartCount = cc;
                            updateCartCountDisplay(cc);
                        }
                    });
                }
            } catch (Exception ex) { log.debug("静默获取购物车数量失败(忽略): {}", ex.toString()); }
        }).start();
    }

    // 统一显示更新方法（内部使用）
    private void updateCartCountDisplay(int count) {
        if (cartButton != null) cartButton.setToolTipText("购物车 (" + count + ")");
    }

    public void updateCartCount(int count) {
        // 直接更新本地缓存并刷新顶部显示，保证添加/删除后数字即时正确
        cachedCartCount = count;
        SwingUtilities.invokeLater(() -> updateCartCountDisplay(count));
    }

    private void loadCartAsync() {
        // 默认不强制更新顶部购物车数字（除非调用方在变更后传入特殊流程）
        loadCartAsync(false);
    }

    // overload: 允许调用方指定本次加载是否强制更新顶部购物车数字（用于删除/清空/结算后刷新）
    private void loadCartAsync(boolean forceUpdateCount) {
        sidebarMode = "cart";
        ensureSplitLayout(true);
        forceCartCountUpdateThisLoad = forceUpdateCount;
        new Thread(() -> {
            try {
                Request req = new Request(getUserUri()).addParam("action", "GET_CART");
                req.setSession(nettyClient.getCurrentSession());
                Response resp = nettyClient.sendRequest(req).get(15, java.util.concurrent.TimeUnit.SECONDS);
                System.out.println("[DEBUG] loadCartAsync resp=" + resp);
                if (resp != null && "SUCCESS".equals(resp.getStatus()) && resp.getData() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) resp.getData();
                    // 仅在当前侧栏模式仍为 cart 时才更新界面，避免并发请求导致切换到其它侧栏
                    SwingUtilities.invokeLater(() -> {
                        if ("cart".equals(sidebarMode)) displayCart(data);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "加载购物车失败", "错误", JOptionPane.ERROR_MESSAGE));
                }
            } catch (Exception e) {
                log.error("加载购物车失败", e);
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "加载购物车失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE));
            } finally {
                // 重置标志（displayCart 内也会重置以防万一）
            }
        }).start();
    }

    private void displayCart(Map<String, Object> cartData) {
        if (transactionPanel == null) return;

        // 清空之前的内容
        transactionPanel.removeAll();

        // 标题
        JPanel cartTop = new JPanel(new BorderLayout());
        cartTop.setOpaque(false);
        JLabel cartTitle = new JLabel("购物车");
        cartTitle.setFont(getScaledFont(16, Font.BOLD));
        cartTitle.setForeground(new Color(71, 85, 105));
        cartTitle.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        cartTop.add(cartTitle, BorderLayout.WEST);

        JButton closeBtn = new JButton("×");
        closeBtn.setFont(getScaledFont(16, Font.BOLD));
        closeBtn.setForeground(Color.GRAY);
        closeBtn.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        closeBtn.setContentAreaFilled(false);
        closeBtn.addActionListener(e -> hideSidebar());
        cartTop.add(closeBtn, BorderLayout.EAST);

        transactionPanel.add(cartTop, BorderLayout.NORTH);

        // 购物车内容
        cartListPanel.removeAll();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) cartData.getOrDefault("items", new ArrayList<>());

        // 过滤掉在本地已被乐观移除但尚未由服务器确认移除的商品
        java.util.List<Map<String, Object>> filteredItems = new ArrayList<>();
        for (Map<String, Object> it : items) {
            String pidStr = ShopUtils.findProductId(it);
            synchronized (pendingRemovedIds) {
                if (pidStr != null && pendingRemovedIds.contains(pidStr)) continue; // 跳过显示
            }
            filteredItems.add(it);
        }

        for (Map<String, Object> item : filteredItems) {
            JPanel itemPanel = createCartItemPanel(item);
            cartListPanel.add(itemPanel);
            cartListPanel.add(Box.createVerticalStrut(5));
        }

        JScrollPane cartScroll = new JScrollPane(cartListPanel);
        cartScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        cartScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        cartScroll.setOpaque(false);
        cartScroll.getViewport().setOpaque(false);
        cartScroll.setBorder(null);
        transactionPanel.add(cartScroll, BorderLayout.CENTER);

        // 底部汇总和操作按钮
        JPanel cartBottom = new JPanel(new BorderLayout());
        cartBottom.setOpaque(false);
        cartBottom.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        // 从过滤后的条目重新计算总数与总额，保证与 UI 一致（优先使用服务器数据，当 forceCartCountUpdateThisLoad 为 true 时保持服务器数值）
        int totalCount = 0;
        java.math.BigDecimal totalAmount = java.math.BigDecimal.ZERO;
        try {
            if (!filteredItems.isEmpty() && !forceCartCountUpdateThisLoad) {
                for (Map<String, Object> it : filteredItems) {
                    try {
                        Object cnt = it.containsKey("quantity") ? it.get("quantity") : it.get("qty");
                        int q = cnt == null ? 1 : new java.math.BigDecimal(cnt.toString()).intValue();
                        Object priceObj = it.get("price");
                        java.math.BigDecimal p = priceObj == null ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(priceObj.toString());
                        totalCount += q;
                        totalAmount = totalAmount.add(p.multiply(new java.math.BigDecimal(q)));
                    } catch (Exception ignored) { System.out.println("[DEBUG] displayCart item parse error: " + ignored); }
                }
            } else {
                Object count = cartData.get("count");
                if (count != null) totalCount = new java.math.BigDecimal(count.toString()).intValue();
                Object amount = cartData.get("totalAmount");
                if (amount != null) totalAmount = (amount instanceof java.math.BigDecimal) ? (java.math.BigDecimal) amount : new java.math.BigDecimal(amount.toString());
            }
        } catch (Exception ex) { System.out.println("[DEBUG] displayCart total calc error: " + ex); }

        // 总计标签
        cartSummaryLabel.setText(String.format("合计: %d 件  ¥%s", totalCount, totalAmount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()));
        cartBottom.add(cartSummaryLabel, BorderLayout.NORTH);

        // 操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);

        // 结算按钮
        JButton checkoutBtn = createModernButton("结算", new Color(34, 197, 94));
        checkoutBtn.setPreferredSize(new Dimension(80, 35));
        checkoutBtn.setEnabled(totalCount > 0);
        checkoutBtn.addActionListener(e -> checkoutCart());
        buttonPanel.add(checkoutBtn);

        // 清空购物车按钮
        JButton clearBtn = createModernButton("清空", new Color(239, 68, 68));
        clearBtn.setPreferredSize(new Dimension(80, 35));
        clearBtn.setEnabled(totalCount > 0);
        clearBtn.addActionListener(e -> clearCart());
        buttonPanel.add(clearBtn);

        cartBottom.add(buttonPanel, BorderLayout.SOUTH);

        transactionPanel.add(cartBottom, BorderLayout.SOUTH);

        // 每次展示购物车时都同步顶部购物车数字，保证与服务器数据一致（若使用乐观过滤则以过滤后的数值为准）
        cachedCartCount = totalCount;
        updateCartCountDisplay(totalCount);
        // 清除一次性强制标志
        forceCartCountUpdateThisLoad = false;
        transactionPanel.revalidate();
        transactionPanel.repaint();
    }

    private JPanel createCartItemPanel(Map<String, Object> item) {
        JPanel itemPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 卡片背景
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // 卡片阴影
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(2, 2, getWidth(), getHeight(), 10, 10);

                // 卡片边框
                g2.setColor(new Color(229, 231, 235));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
        };
        itemPanel.setLayout(new BorderLayout(10, 5));
        itemPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        itemPanel.setOpaque(false);
        itemPanel.setPreferredSize(new Dimension(0, 80));

        // 标记商品 id 以便恢复时使用
        String productIdStr = ShopUtils.findProductId(item);
         itemPanel.putClientProperty("productId", productIdStr);

        // 商品信息
        JPanel infoPanel = new JPanel(new BorderLayout(5, 3));
        infoPanel.setOpaque(false);

        String productName = item.getOrDefault("productName", "未知商品").toString();
        String price = "";
        String quantity = "1";

        try {
            Object priceObj = item.get("price");
            if (priceObj != null) {
                price = "¥" + String.format("%.2f", Double.parseDouble(priceObj.toString()));
            }
            Object qtyObj = item.containsKey("quantity") ? item.get("quantity") : item.get("qty");
            if (qtyObj != null) {
                quantity = qtyObj.toString();
            }
        } catch (Exception ignored) {}

        // 商品名称
        JLabel nameLabel = new JLabel(productName);
        nameLabel.setFont(getScaledFont(13, Font.BOLD));
        nameLabel.setForeground(new Color(31, 41, 55));

        // 价格和数量
        JLabel priceLabel = new JLabel(price + " × " + quantity);
        priceLabel.setFont(getScaledFont(12));
        priceLabel.setForeground(new Color(107, 114, 128));

        infoPanel.add(nameLabel, BorderLayout.NORTH);
        infoPanel.add(priceLabel, BorderLayout.SOUTH);

        // 操作按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);

        JButton removeBtn = new JButton("移除") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bgColor = new Color(239, 68, 68);
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        removeBtn.setFont(getScaledFont(11, Font.BOLD));
        removeBtn.setPreferredSize(new Dimension(50, 25));
        removeBtn.setContentAreaFilled(false);
        removeBtn.setBorderPainted(false);
        removeBtn.setFocusPainted(false);
        removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 绑定商品ID到按钮：优先使用 actionCommand，同时放入 clientProperty 作为备份
        removeBtn.setActionCommand(productIdStr == null ? "" : productIdStr);
        removeBtn.putClientProperty("productId", productIdStr);

        // 优化：乐观更新 UI，立刻从列表中移除条目并更新计数；请求失败时恢复
        removeBtn.addActionListener(e -> {
            // 防止重复点击
            removeBtn.setEnabled(false);
            String oldText = removeBtn.getText();
            removeBtn.setText("移除中...");

            // 在 EDT 中进行乐观 UI 更新
            SwingUtilities.invokeLater(() -> {
                // 尝试更新顶部计数与汇总（尽量保守）
                int deltaCount = 1;
                try {
                    Object qty = item.containsKey("quantity") ? item.get("quantity") : item.get("qty");
                    if (qty != null) deltaCount = Math.max(1, Integer.parseInt(qty.toString()));
                } catch (Exception ignored) {}

                // 标记为 pending 移除，确保后续刷新时不会重新渲染
                // 优先从事件源读取绑定的商品ID（使用 actionCommand 或 clientProperty）
                String thisPid = null;
                try {
                    Object src = e.getSource();
                    if (src instanceof AbstractButton) {
                        AbstractButton b = (AbstractButton) src;
                        String cmd = b.getActionCommand();
                        if (cmd != null && !cmd.isEmpty()) thisPid = cmd;
                        else {
                            Object cp = b.getClientProperty("productId");
                            if (cp != null) thisPid = cp.toString();
                        }
                    }
                } catch (Exception ignored) {}

                if (thisPid != null) {
                    synchronized (pendingRemovedIds) { pendingRemovedIds.add(thisPid); }
                }

                if (cachedCartCount != null) cachedCartCount = Math.max(0, cachedCartCount - deltaCount);
                updateCartCountDisplay(cachedCartCount == null ? 0 : cachedCartCount);

                // 尝试调整底部汇总金额（保守：若无法解析 price 则只调整数量）
                try {
                    Object priceObj = item.get("price");
                    java.math.BigDecimal p = java.math.BigDecimal.ZERO;
                    if (priceObj != null) {
                        try { p = new java.math.BigDecimal(priceObj.toString()); } catch (Exception ex) { p = java.math.BigDecimal.ZERO; }
                    }
                    java.math.BigDecimal amountDelta = p.multiply(new java.math.BigDecimal(deltaCount));
                    // 解析现有汇总文本并减去金额，使用 BigDecimal 避免精度问题
                    String cur = cartSummaryLabel.getText();
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("合计:\\s*(\\d+)\\s*件\\s*¥([0-9\\.]+)").matcher(cur);
                    if (m.find()) {
                        int curCnt = Integer.parseInt(m.group(1));
                        java.math.BigDecimal curAmt = new java.math.BigDecimal(m.group(2));
                        curCnt = Math.max(0, curCnt - deltaCount);
                        java.math.BigDecimal newAmt = curAmt.subtract(amountDelta);
                        if (newAmt.compareTo(java.math.BigDecimal.ZERO) < 0) newAmt = java.math.BigDecimal.ZERO;
                        cartSummaryLabel.setText(String.format("合计: %d 件  ¥%s", curCnt, newAmt.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()));
                    }
                } catch (Exception ignored) { System.out.println("[DEBUG] removeBtn local adjust failed: " + ignored); }

                // 从列表中移除该条目视图
                Component toRemove = SwingUtilities.getAncestorOfClass(JPanel.class, removeBtn);
                if (toRemove != null && cartListPanel != null) {
                    cartListPanel.remove(toRemove);
                    cartListPanel.revalidate();
                    cartListPanel.repaint();
                }
            });

            // 后台请求实际移除
            new Thread(() -> {
                try {
                    String boundPid = ShopUtils.findProductId(item);
                    if (boundPid == null || boundPid.isEmpty()) {
                        throw new IllegalArgumentException("商品 ID 缺失，无法移除");
                    }

                    Request req = new Request(getUserUri())
                        .addParam("action", "REMOVE_FROM_CART")
                        .addParam("productIds", boundPid);
                    req.setSession(nettyClient.getCurrentSession());

                    Response resp = nettyClient.sendRequest(req).get(10, java.util.concurrent.TimeUnit.SECONDS);

                    if (resp != null && "SUCCESS".equals(resp.getStatus())) {
                        synchronized (pendingRemovedIds) { pendingRemovedIds.remove(boundPid); }
                        loadCartAsync(true);
                    } else {
                        synchronized (pendingRemovedIds) { pendingRemovedIds.remove(boundPid); }
                        String message = "移除失败";
                        if (resp != null && resp.getMessage() != null) message = resp.getMessage();
                        final String messageFinal = message;
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ShopPanel.this, messageFinal, "错误", JOptionPane.ERROR_MESSAGE));
                        loadCartAsync(true);
                    }
                } catch (Exception ex) {
                    log.error("移除购物车商品失败", ex);
                    SwingUtilities.invokeLater(() -> {
                        String pid = ShopUtils.findProductId(item);
                        if (pid != null) synchronized (pendingRemovedIds) { pendingRemovedIds.remove(pid); }
                        JOptionPane.showMessageDialog(ShopPanel.this, "移除失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        loadCartAsync(true);
                    });
                } finally {
                    SwingUtilities.invokeLater(() -> {
                        removeBtn.setEnabled(true);
                        removeBtn.setText(oldText);
                    });
                }
            }).start();
        });

        buttonPanel.add(removeBtn);

        itemPanel.add(infoPanel, BorderLayout.CENTER);
        itemPanel.add(buttonPanel, BorderLayout.EAST);

        return itemPanel;
    }

    // 对外公开，供 ProductDetailDialog / CartDialog 等调用
    public String getUserUri() {
        try {
            var session = nettyClient.getCurrentSession();
            if (session != null && session.getRoleSet() != null) {
                java.util.Set<String> roles = session.getRoleSet();
                for (String r : roles) { if (r != null && r.equalsIgnoreCase("admin")) return "shop/adminManager"; }
                for (String r : roles) { if (r != null && r.equalsIgnoreCase("manager")) return "shop/manager"; }
                for (String r : roles) { if (r != null && (r.equalsIgnoreCase("staff") || r.equalsIgnoreCase("teacher"))) return "shop/staff"; }
            }
        } catch (Exception e) { log.debug("无法从会话确定用户角色，回退 student", e); }
        return "shop/student";
    }

    // 触发重新加载商品（对外调用）
    public void reloadProducts() { loadProducts(); }

    // 简单判断用户是否为 manager（从 userData 中尝试多种字段）
    private boolean isUserManager(Map<String, Object> data) {
        if (data == null) return false;
        try {
            Object v = data.get("isManager");
            if (Boolean.TRUE.equals(v)) return true;
            if (v instanceof String && "true".equalsIgnoreCase((String) v)) return true;
            Object roles = data.get("roles");
            if (roles instanceof java.util.Collection) {
                for (Object r : (java.util.Collection<?>) roles) {
                    if (r != null && "manager".equalsIgnoreCase(r.toString())) return true;
                }
            }
            Object role = data.get("role"); if (role != null && "manager".equalsIgnoreCase(role.toString())) return true;
            Object roleName = data.get("roleName"); if (roleName != null && "manager".equalsIgnoreCase(roleName.toString())) return true;
        } catch (Exception ignored) {}
        return false;
    }

    // 判断是否为严格 admin（优先检查显式字段，然后回退到 roles）
    private boolean isAdminStrict(Map<String, Object> data) {
        if (data == null) return false;
        try {
            Object strict = data.get("isAdminStrict");
            if (Boolean.TRUE.equals(strict)) return true;
            Object isAdmin = data.get("isAdmin");
            if (Boolean.TRUE.equals(isAdmin)) {
                Object roleType = data.get("roleType");
                if (roleType == null) return true;
                String rt = roleType.toString().toLowerCase();
                if (!rt.contains("staff") && !rt.contains("teacher")) return true;
            }
            Object roles = data.get("roles");
            if (roles instanceof java.util.Collection) {
                for (Object r : (java.util.Collection<?>) roles) {
                    if (r != null && "admin".equalsIgnoreCase(r.toString())) return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    // 注册必要的事件监听（轻量实现）
    private void hookListeners() {
        try {
            if (toolbar != null) {
                JToggleButton vt = toolbar.getViewToggleButton();
                if (vt != null) vt.addActionListener(e -> {
                    if (vt.isSelected()) centerLayout.show(centerCards, "grid"); else centerLayout.show(centerCards, "table");
                    SwingUtilities.invokeLater(this::updateGridOverlayWidth);
                });
            }
            if (pagination != null) {
                pagination.addPageChangeListener(page -> {
                    currentPage = page;
                    if (productGridPanel != null) productGridPanel.setProductsForPage(fullProductList, currentPage, pageSize);
                    if (productTablePanel != null) productTablePanel.setProductsForPage(fullProductList, currentPage, pageSize);
                });
                // 当每页数量改变时，更新本地 pageSize 并刷新视图
                pagination.addPageSizeListenerInternal(e -> {
                     try {
                         int newSize = pagination.getPageSize();
                         if (newSize <= 0) return;
                         pageSize = newSize;
                         // 重置为第1页并刷新展示
                         currentPage = 1;
                         if (pagination != null) pagination.setTotal(fullProductList.size());
                         if (productGridPanel != null) productGridPanel.setProductsForPage(fullProductList, currentPage, pageSize);
                         if (productTablePanel != null) productTablePanel.setProductsForPage(fullProductList, currentPage, pageSize);
                         SwingUtilities.invokeLater(this::updateGridOverlayWidth);
                     } catch (Exception ignored) {}
                });
            }
        } catch (Exception ignored) {}
    }

    // 基础的商品加载实现（保守占位实现，保证编译与基本功能）
    private void loadProducts() {
        new Thread(() -> {
            try {
                // 尝试异步调用服务器获取商品列表；如果失败则保守处理为空
                Request req = new Request(getUserUri()).addParam("action", "PRODUCT_LIST");
                try { req.setSession(nettyClient.getCurrentSession()); } catch (Exception ignored) {}
                Response resp = null;
                try { resp = nettyClient.sendRequest(req).get(10, java.util.concurrent.TimeUnit.SECONDS); } catch (Exception ignored) {}
                if (resp != null && "SUCCESS".equals(resp.getStatus()) && resp.getData() instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<Map<String,Object>> list = (java.util.List<Map<String,Object>>) resp.getData();
                    fullProductList = list == null ? new ArrayList<>() : list;
                } else {
                    // 回退为空列表
                    fullProductList = new ArrayList<>();
                }
                SwingUtilities.invokeLater(() -> {
                    currentPage = 1;
                    if (pagination != null) pagination.setTotal(fullProductList.size());
                    if (productGridPanel != null) productGridPanel.setProductsForPage(fullProductList, currentPage, pageSize);
                    if (productTablePanel != null) productTablePanel.setProductsForPage(fullProductList, currentPage, pageSize);
                });
            } catch (Exception e) { log.debug("loadProducts failed", e); }
        }).start();
    }

    // 分隔布局切换与侧栏显示/隐藏
    private void ensureSplitLayout(boolean showSidebar) {
        if (mainSplit == null) return;
        SwingUtilities.invokeLater(() -> {
            try {
                if (showSidebar && transactionPanel != null) {
                    transactionPanel.setVisible(true);
                    mainSplit.setRightComponent(transactionPanel);
                    int w = Math.min(lastSidebarWidth, Math.max(180, getWidth() / 4));
                    mainSplit.setDividerLocation(Math.max(0, getWidth() - w));
                    try {
                        int rightWidth = Math.max(0, getWidth() - mainSplit.getDividerLocation());
                        if (productGridPanel != null) productGridPanel.applySidebarMode(true, rightWidth);
                    } catch (Exception ignored) {}
                } else {
                    if (transactionPanel != null) transactionPanel.setVisible(false);
                    mainSplit.setRightComponent(new JPanel());
                    mainSplit.setDividerLocation(getWidth());
                    try { if (productGridPanel != null) productGridPanel.applySidebarMode(false, 0); } catch (Exception ignored) {}
                }
                updateGridOverlayWidth();
            } catch (Exception ignored) {}
        });
    }

    // 更新网格遮罩宽度
    private void updateGridOverlayWidth() {
        if (productGridPanel == null || mainSplit == null) return;
        SwingUtilities.invokeLater(() -> {
            try {
                Component right = mainSplit.getRightComponent();
                int rightWidth = 0;
                if (right != null) rightWidth = right.getWidth();
                if (rightWidth <= 0) {
                    try { int divider = mainSplit.getDividerLocation(); int total = getWidth(); rightWidth = Math.max(0, total - divider); } catch (Exception ignored) { rightWidth = 0; }
                }
                try { productGridPanel.updateOverlayWidth(rightWidth); } catch (Exception ignored) {}
            } catch (Exception ignored) {}
        });
    }

    // 切换侧栏购物车
    private void toggleFloatingCart() {
        if (transactionPanel == null) return;
        if (transactionPanel.isVisible() && "cart".equals(sidebarMode)) {
            hideSidebar();
        } else {
            sidebarMode = "cart";
            ensureSplitLayout(true);
            loadCartAsync(false);
        }
    }

    private void hideSidebar() {
        sidebarMode = null;
        ensureSplitLayout(false);
    }

    // 加载订单（兼容实现）
    private void loadMyOrders() {
        sidebarMode = "orders";
        ensureSplitLayout(true);
        // 立即替换侧栏内容为订单占位结构，防止之前的购物车视图残留
        if (transactionPanel != null) {
            transactionPanel.removeAll();
            JPanel transTop = new JPanel(new BorderLayout());
            transTop.setOpaque(false);
            JLabel transTitle = new JLabel("我的订单");
            transTitle.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
            transTop.add(transTitle, BorderLayout.WEST);
            JButton closeBtn = new JButton("×");
            closeBtn.setFont(getScaledFont(16, Font.BOLD));
            closeBtn.setForeground(Color.GRAY);
            closeBtn.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
            closeBtn.setContentAreaFilled(false);
            closeBtn.addActionListener(e -> hideSidebar());
            transTop.add(closeBtn, BorderLayout.EAST);
            transactionPanel.add(transTop, BorderLayout.NORTH);

            JScrollPane transScroll = new JScrollPane(transactionListPanel);
            transScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            transScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            transScroll.setOpaque(false);
            transScroll.getViewport().setOpaque(false);
            transScroll.setBorder(null);
            transactionPanel.add(transScroll, BorderLayout.CENTER);

            transactionPanel.revalidate();
            transactionPanel.repaint();
        }

        transactionListPanel.removeAll();
        JLabel loading = new JLabel("正在加载订单..."); loading.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        transactionListPanel.add(loading);
        transactionListPanel.revalidate(); transactionListPanel.repaint();
        new Thread(() -> {
            try {
                Request req = new Request(getUserUri()).addParam("action", "MY_ORDERS");
                try { req.setSession(nettyClient.getCurrentSession()); } catch (Exception ignored) {}
                Response resp = nettyClient.sendRequest(req).get(12, java.util.concurrent.TimeUnit.SECONDS);
                if (resp != null && "SUCCESS".equals(resp.getStatus()) && resp.getData() instanceof java.util.List) {
                    @SuppressWarnings("unchecked") java.util.List<Map<String,Object>> orders = (java.util.List<Map<String,Object>>) resp.getData();
                    SwingUtilities.invokeLater(() -> {
                        // 使用 OrdersDialog 来显示订单（已实现直接显示金额/内容并带展开时间的按钮），
                        // 以确保与用户要求一致且避免侧栏旧实现导致的显示不一致。
                        try {
                            if (orders == null || orders.isEmpty()) {
                                transactionListPanel.removeAll();
                                JLabel none = new JLabel("您还没有订单"); none.setBorder(BorderFactory.createEmptyBorder(12,12,12,12)); transactionListPanel.add(none);
                                transactionListPanel.revalidate(); transactionListPanel.repaint();
                            } else {
                                // 在侧栏内直接渲染订单卡片（避免弹窗）
                                renderOrdersInSidebar(orders);
                                ensureSplitLayout(true);
                            }
                        } catch (Exception ex) {
                            transactionListPanel.removeAll(); JLabel err = new JLabel("显示订单失败"); err.setBorder(BorderFactory.createEmptyBorder(12,12,12,12)); transactionListPanel.add(err); transactionListPanel.revalidate(); transactionListPanel.repaint();
                        }
                     });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        transactionListPanel.removeAll(); JLabel err = new JLabel("加载订单失败"); err.setBorder(BorderFactory.createEmptyBorder(12,12,12,12)); transactionListPanel.add(err); transactionListPanel.revalidate(); transactionListPanel.repaint();
                    });
                }
            } catch (Exception e) {
                log.error("加载我的订单失败", e);
                SwingUtilities.invokeLater(() -> { transactionListPanel.removeAll(); JLabel err = new JLabel("加载订单失败: " + e.getMessage()); err.setBorder(BorderFactory.createEmptyBorder(12,12,12,12)); transactionListPanel.add(err); transactionListPanel.revalidate(); transactionListPanel.repaint(); });
            }
        }).start();
    }

    // 在侧栏内渲染订单列表（与 OrdersDialog 相同的卡片视图）
    private void renderOrdersInSidebar(java.util.List<Map<String,Object>> orders) {
        if (transactionListPanel == null) return;
        transactionListPanel.removeAll();

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        for (Map<String,Object> o : orders) {
            // 兼容字段映射
            String orderId = String.valueOf(o.getOrDefault("transId", o.getOrDefault("orderId", "")));
            String status = String.valueOf(o.getOrDefault("status", ""));
            Object amountObj = o.getOrDefault("amount", o.getOrDefault("totalAmount", o.get("totalAmount")));
            String amount = formatAmount(amountObj);
            Object created = o.getOrDefault("transTime", o.getOrDefault("createdAt", o.getOrDefault("created_at", o.get("time"))));
            String dateStr = formatTime(created);
            String productName = String.valueOf(o.getOrDefault("productName", ""));
            String qty = String.valueOf(o.getOrDefault("qty", o.getOrDefault("quantity", "1")));

            if (productName == null || productName.isEmpty()) {
                Object pidObj = o.getOrDefault("productId", o.getOrDefault("product_id", o.get("productId")));
                String pid = pidObj == null ? "" : pidObj.toString();
                if (!pid.isEmpty()) productName = "商品#" + pid; else productName = "未知商品";
            }

            JPanel panel = new JPanel(new BorderLayout());
            panel.setOpaque(false);
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
            ));

            JLabel idLabel = new JLabel("订单号: " + orderId);
            idLabel.setFont(getScaledFont(12, Font.BOLD));

            JLabel productLabel = new JLabel("商品: " + productName + " × " + qty);
            productLabel.setFont(getScaledFont(12));

            JLabel statusLabel = new JLabel("状态: " + mapStatusText(status));
            statusLabel.setFont(getScaledFont(12));

            JLabel amountLabel = new JLabel("金额: ¥" + amount);
            amountLabel.setFont(getScaledFont(12));
            amountLabel.setForeground(new Color(34, 197, 94));

            JLabel dateLabel = new JLabel("日期: " + dateStr);
            dateLabel.setFont(getScaledFont(11));
            dateLabel.setForeground(new Color(120,125,140));

            // 显示购买人姓名与校园卡号（若后端返回），兼容多种字段命名与嵌套结构
            Object buyerObj = firstPresent(o, "buyer", "buyerName", "userName", "username", "realName", "name", "user_name");
            Object cardObj = firstPresent(o, "cardId", "cardNum", "cardnum", "card_id", "card_no", "cardNo", "cardNumber", "card_number");
            // 兼容：如果直接字段未命中，尝试从嵌套的 user/buyer 对象中查找
            if (cardObj == null) {
                Object nested = firstPresent(o, "user", "buyerInfo", "buyerObj");
                if (nested instanceof Map) cardObj = firstPresent((Map<String,Object>)nested, "cardId", "cardNum", "cardno", "cardnum", "card_no", "cardNo", "cardNumber", "card_number");
            }
            String buyerNameStr = buyerObj == null ? "" : buyerObj.toString();
            String cardIdStr = cardObj == null ? "" : cardObj.toString();
            String buyerDisplay;
            // 修改：即使是普通用户，也优先显示卡号（如果后端返回），避免默认显示“我”。仅在没有卡号与姓名时显示“我”。
            if (!cardIdStr.isEmpty() && !buyerNameStr.isEmpty()) {
                buyerDisplay = buyerNameStr + " (卡号: " + cardIdStr + ")";
            } else if (!cardIdStr.isEmpty()) {
                buyerDisplay = "卡号: " + cardIdStr;
            } else if (!buyerNameStr.isEmpty()) {
                buyerDisplay = buyerNameStr;
            } else {
                buyerDisplay = "我";
            }
            JLabel buyerLabel = new JLabel("购买人: " + buyerDisplay);
            buyerLabel.setFont(getScaledFont(11));
            buyerLabel.setForeground(new Color(120,125,140));

            JPanel textPanel = new JPanel(new GridLayout(3,1));
            textPanel.setOpaque(false);
            textPanel.add(productLabel);
            textPanel.add(statusLabel);
            textPanel.add(amountLabel);

            JPanel detailsPanel = new JPanel(new GridLayout(2,1));
            detailsPanel.setOpaque(false);
            detailsPanel.add(dateLabel);
            detailsPanel.add(buyerLabel);
            detailsPanel.setVisible(false);

            JButton toggleBtn = new JButton("▾");
            toggleBtn.setMargin(new Insets(2,6,2,6));
            toggleBtn.setFocusable(false);
            toggleBtn.addActionListener(ev -> {
                boolean vis = !detailsPanel.isVisible();
                detailsPanel.setVisible(vis);
                toggleBtn.setText(vis ? "▴" : "▾");
                SwingUtilities.invokeLater(() -> { panel.revalidate(); panel.repaint(); });
            });
            JPanel rightWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); rightWrap.setOpaque(false); rightWrap.add(toggleBtn);

            panel.add(textPanel, BorderLayout.CENTER);
            panel.add(detailsPanel, BorderLayout.SOUTH);
            panel.add(rightWrap, BorderLayout.EAST);

            list.add(panel);
            list.add(Box.createRigidArea(new Dimension(0,8)));
        }

        transactionListPanel.add(list);
        transactionListPanel.revalidate();
        transactionListPanel.repaint();
    }

    // 简单映射状态为中文显示，保持与管理端一致
    private String mapStatusText(String status) {
        if (status == null) return "未知";
        switch (status.toUpperCase()) {
            case "ON_SHELF": return "在售";
            case "OFF_SHELF": return "下架";
            case "SOLD_OUT": return "售罄";
            default: return status;
        }
    }

    // 格式化金额（支持多种输入格式，输出统一为带符号的两位小数）
    private String formatAmount(Object amountObj) {
        if (amountObj == null) return "0.00";
        try {
            if (amountObj instanceof Number) {
                return String.format("%.2f", ((Number) amountObj).doubleValue());
            }
            String amtStr = amountObj.toString().trim();
            if (amtStr.isEmpty()) return "0.00";
            // 支持以元为单位的整数或小数
            try {
                double v = Double.parseDouble(amtStr);
                return String.format("%.2f", v);
            } catch (Exception ignored) {}
            // 支持以分为单位的整数（如 1234 表示 12.34 元）
            try {
                long cents = Long.parseLong(amtStr);
                return String.format("%.2f", cents / 100.0);
            } catch (Exception ignored) {}
            // 其他格式尝试直接解析为 BigDecimal
            try {
                java.math.BigDecimal bd = new java.math.BigDecimal(amtStr);
                return String.format("%.2f", bd.doubleValue());
            } catch (Exception ignored) {}
        } catch (Exception e) { log.warn("金额格式化失败: " + amountObj, e); }
        return "0.00";
    }

    // 格式化时间（支持多种输入格式，输出统一为 yyyy-MM-dd HH:mm:ss）
    private String formatTime(Object timeObj) {
        if (timeObj == null) return "";
        try {
            if (timeObj instanceof Number) {
                long v = ((Number) timeObj).longValue();
                java.time.Instant instant = v > 1_000_000_000_000L ? java.time.Instant.ofEpochMilli(v) : java.time.Instant.ofEpochSecond(v);
                return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(instant);
            }
            String s = timeObj.toString().trim(); if (s.isEmpty()) return "";
            try { java.time.Instant inst = java.time.Instant.parse(s); return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()).format(inst); } catch (Exception ignored) {}
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{4}[-/]\\d{2}[-/]\\d{2})").matcher(s);
            if (m.find()) return m.group(1).replace('/', '-');
            return s; // 无法解析的格式直接返回原始字符串
        } catch (Exception e) { return ""; }
    }

    // 结算购物车（与 CartDialog 保持一致的行为）
    private void checkoutCart() {
        new Thread(() -> {
            try {
                Request req = new Request(getUserUri()).addParam("action", "CHECKOUT_CART");
                req.setSession(nettyClient.getCurrentSession());
                Response resp = nettyClient.sendRequest(req).get(15, java.util.concurrent.TimeUnit.SECONDS);
                System.out.println("[DEBUG] checkoutCart resp=" + resp);
                SwingUtilities.invokeLater(() -> {
                    if (resp != null && "SUCCESS".equals(resp.getStatus())) {
                        JOptionPane.showMessageDialog(this, "结算成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                        hideSidebar(); loadProducts(); loadCartAsync(true);
                    } else {
                        String message = resp != null && resp.getMessage() != null ? resp.getMessage() : "结算失败";
                        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception e) {
                log.error("结算失败", e);
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "结算失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    // 清空购物车（与 CartDialog 保持一致的行为）
    private void clearCart() {
        int confirm = JOptionPane.showConfirmDialog(this, "确定要清空购物车吗？", "确认", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        new Thread(() -> {
            try {
                Request req = new Request(getUserUri()).addParam("action", "CLEAR_CART");
                req.setSession(nettyClient.getCurrentSession());
                Response resp = nettyClient.sendRequest(req).get(10, java.util.concurrent.TimeUnit.SECONDS);
                System.out.println("[DEBUG] clearCart resp=" + resp);
                SwingUtilities.invokeLater(() -> {
                    if (resp != null && "SUCCESS".equals(resp.getStatus())) loadCartAsync(true);
                    else JOptionPane.showMessageDialog(this, resp != null && resp.getMessage() != null ? resp.getMessage() : "清空失败", "错误", JOptionPane.ERROR_MESSAGE);
                });
            } catch (Exception e) {
                log.error("清空购物车失败", e);
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "清空购物车失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }


    // 缺失的方法：切换到管理面板（占位实现，避免编译错误）
    private void upgradeToManagerPanel() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "切换到管理后台功能暂未实现。", "提示", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    // 新增：从map中按候选键顺序取第一个存在且非空的值（与管理员面板相似）
    private Object firstPresent(Map<String, Object> m, String... keys) {
        if (m == null || keys == null) return null;
        for (String k : keys) {
            if (k == null) continue;
            if (m.containsKey(k)) {
                Object v = m.get(k);
                if (v != null) return v;
            }
        }
        return null;
    }
}

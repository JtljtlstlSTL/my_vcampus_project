package com.vcampus.client.core.ui.shop;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import com.vcampus.client.core.ui.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.*;

// Excel文件处理相关导入
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import java.io.FileInputStream;

@Slf4j
public class ShopManagerPanel extends JPanel {
    private final NettyClient nettyClient;
    private final Map<String, Object> userData;

    // 新增：表格视图组件与状态
    private ProductTablePanel productTablePanel;
    private JComponent listScrollPane; // 左侧主列表的容器（用于在卡片和表格之间切换）
    private boolean isTableView = false;
    private java.util.List<Map<String,Object>> currentProductList = new java.util.ArrayList<>();
    // 使用与普通用户相同的网格面板以统一卡片样式
    private ProductGridPanel productGridPanel;

    private JPanel productListPanel; // 商品列表面板
    private Map<String, Object> selectedProduct; // 当前选中的商品（保持向后兼容）
    private java.util.List<Map<String, Object>> selectedProducts = new java.util.ArrayList<>(); // 多选商品列表
    // 交易侧栏相关
    private JPanel transactionPanel; // 右侧交易面板容器
    private JPanel transactionListPanel; // 交易卡片列表
    private JSplitPane mainSplit;

    private JTextField txtName;
    private JTextField txtCategory;
    private JButton btnSearchName;
    private JButton btnSearchCategory;
    private JButton btnRefresh;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnChangeStock;
    private JButton btnChangeStatus;
    private JButton btnCategories;
    private JButton btnTrans;
    private JButton btnImport;      // 新增：导入
    private JButton btnOffShelf;    // 新增：一键下架
    // === 新增：分类浮动面板相关字段 ===
    private JLayeredPane layeredCenter;
    private JPanel categoryFloatingPanel;
    private JTable categoryTable;
    private DefaultTableModel categoryModel;
    private boolean categoryPanelVisible = false;

        // 字体大小常量 - 添加响应式字体支持
        private static final int BASE_FONT_SIZE = 14;
        private static final int TITLE_FONT_SIZE = 18;
        private static final int BUTTON_FONT_SIZE = 12;
        private static final int SMALL_FONT_SIZE = 11;
        

    /**
     * 根据界面大小自动调整字体
     */
    private java.awt.Font getScaledFont(int baseSize, int style) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double scaleFactor = Math.min(screenSize.width / 1920.0, screenSize.height / 1080.0);
        scaleFactor = Math.max(0.8, Math.min(1.2, scaleFactor)); // 限制缩放比例在0.8-1.2之间
        int scaledSize = (int) (baseSize * scaleFactor);
        return new java.awt.Font("微软雅黑", style, scaledSize);
    }

    private java.awt.Font getScaledFont(int baseSize) {
        return getScaledFont(baseSize, java.awt.Font.PLAIN);
    }
    
    // 状态颜色方法
    private java.awt.Color getStatusColor(String status) {
        if ("ON_SHELF".equals(status)) {
            return new java.awt.Color(34, 197, 94); // 绿色
        } else if ("OFF_SHELF".equals(status)) {
            return new java.awt.Color(239, 68, 68); // 红色
        } else {
            return new java.awt.Color(107, 114, 128); // 灰色
        }
    }

    /**
     * 创建现代化按钮样式
     */
    private JButton createModernButton(String text, java.awt.Color bgColor) {
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // 文字
                g2.setColor(java.awt.Color.WHITE);
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
        button.setFont(getScaledFont(BUTTON_FONT_SIZE, java.awt.Font.BOLD));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * 创建现代化文本框样式
     */
    private JTextField createModernTextField(String placeholder, int columns) {
        JTextField field = new JTextField(columns) {
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (hasFocus()) {
                    g2.setColor(new java.awt.Color(59, 130, 246));
                    g2.setStroke(new BasicStroke(2));
                } else {
                    g2.setColor(new java.awt.Color(209, 213, 219));
                    g2.setStroke(new BasicStroke(1));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
            }
        };
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setFont(getScaledFont(BASE_FONT_SIZE));
        field.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return field;
    }

    public ShopManagerPanel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        initUI();
        loadAllProducts();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建带渐变背景的主面板
        JPanel backgroundPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 渐变背景 - 使用管理界面主题色（紫色系）
                GradientPaint gradient = new GradientPaint(
                    0, 0, new java.awt.Color(248, 250, 252),
                    getWidth(), getHeight(), new java.awt.Color(243, 244, 246)
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // 创建标题面板
        JPanel titleContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 标题区域背景
                GradientPaint titleGradient = new GradientPaint(
                    0, 0, new java.awt.Color(139, 92, 246, 40),
                    getWidth(), getHeight(), new java.awt.Color(124, 58, 237, 60)
                );
                g2.setPaint(titleGradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // 装饰元素
                g2.setColor(new java.awt.Color(255, 255, 255, 30));
                g2.fillOval(getWidth() - 80, -20, 100, 100);
            }
        };
        titleContainer.setOpaque(false);
        titleContainer.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("商城管理");
        title.setFont(getScaledFont(TITLE_FONT_SIZE, java.awt.Font.BOLD));
        title.setForeground(new java.awt.Color(75, 85, 99));

        // 顶部标题行：在右侧放置订单查询等快捷按钮
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(title, BorderLayout.WEST);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightControls.setOpaque(false);
        // JButton btnOrderHistory = createModernButton("订单查询", new java.awt.Color(99, 102, 241));
        // btnOrderHistory.setPreferredSize(new Dimension(100, 32));
        // btnOrderHistory.setToolTipText("查看订单历史");
        // btnOrderHistory.addActionListener(e -> {
        //     // 切换侧栏显示
        //     if (transactionPanel != null && transactionPanel.isVisible()) hideTransactions(); else showTransactions();
        // });
        // rightControls.add(btnOrderHistory);
        // 已移除顶部右侧的“订单查询”快捷按钮（按需求）

        // 将“全选/取消全选/查看记录”放到顶部右侧，避免底部按钮过多
        JButton btnSelectAll = createModernButton("全选", new java.awt.Color(59, 130, 246));
        JButton btnClearSelection = createModernButton("取消全选", new java.awt.Color(107, 114, 128));
        btnSelectAll.setPreferredSize(new Dimension(80, 32));
        btnClearSelection.setPreferredSize(new Dimension(100, 32));
        // btnTrans 为类字段，在下方也会被复用，这里直接创建并放到顶部
        btnTrans = createModernButton("查看记录", new java.awt.Color(99, 102, 241));
        btnTrans.setPreferredSize(new Dimension(100, 32));
        // 视图切换按钮
        JButton btnToggleView = createModernButton("表格视图", new java.awt.Color(99, 102, 241));
        btnToggleView.setPreferredSize(new Dimension(100,32));
        rightControls.add(btnSelectAll);
        rightControls.add(btnClearSelection);
        rightControls.add(btnToggleView);
        rightControls.add(btnTrans);

        titleRow.add(rightControls, BorderLayout.EAST);
        titleContainer.add(titleRow, BorderLayout.CENTER);

        // 搜索区域（现代化设计）
        JPanel searchContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 搜索区域背景
                g2.setColor(new java.awt.Color(255, 255, 255, 180));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 12, 12);

                // 边框
                g2.setColor(new java.awt.Color(226, 232, 240));
                g2.drawRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 12, 12);
            }
        };
        searchContainer.setOpaque(false);
        searchContainer.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        searchRow.setOpaque(false);

        JLabel nameLabel = new JLabel("商品名称:");
        nameLabel.setFont(getScaledFont(BASE_FONT_SIZE, java.awt.Font.BOLD));
        nameLabel.setForeground(new java.awt.Color(71, 85, 105));

        txtName = createModernTextField("请输入商品名称", 16);
        btnSearchName = createModernButton("搜索", new java.awt.Color(34, 197, 94));
        btnSearchName.setPreferredSize(new Dimension(70, 32));

        JLabel categoryLabel = new JLabel("分类:");
        categoryLabel.setFont(getScaledFont(BASE_FONT_SIZE, java.awt.Font.BOLD));
        categoryLabel.setForeground(new java.awt.Color(71, 85, 105));

        txtCategory = createModernTextField("分类代码", 10);
        btnSearchCategory = createModernButton("按分类", new java.awt.Color(59, 130, 246));
        btnSearchCategory.setPreferredSize(new Dimension(80, 32));

        searchRow.add(nameLabel);
        searchRow.add(txtName);
        searchRow.add(btnSearchName);
        searchRow.add(Box.createHorizontalStrut(20));
        searchRow.add(categoryLabel);
        searchRow.add(txtCategory);
        searchRow.add(btnSearchCategory);

        searchContainer.add(searchRow, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(titleContainer, BorderLayout.NORTH);
        topPanel.add(searchContainer, BorderLayout.SOUTH);
        backgroundPanel.add(topPanel, BorderLayout.NORTH);

        // 底部工具栏：现代化按钮样式
        JPanel toolbarContainer = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 工具栏背景
                g2.setColor(new java.awt.Color(255, 255, 255, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // 顶部分割线
                g2.setColor(new java.awt.Color(226, 232, 240));
                g2.drawLine(10, 0, getWidth() - 10, 0);
            }
        };
        toolbarContainer.setOpaque(false);
        toolbarContainer.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setOpaque(false);

        btnRefresh = createModernButton("刷新", new java.awt.Color(107, 114, 128));
        btnAdd = createModernButton("新增", new java.awt.Color(34, 197, 94));
        btnEdit = createModernButton("编辑", new java.awt.Color(59, 130, 246));
        btnDelete = createModernButton("删除", new java.awt.Color(239, 68, 68));
        btnImport = createModernButton("导入", new java.awt.Color(168, 85, 247));
        btnChangeStock = createModernButton("改库存", new java.awt.Color(245, 158, 11));
        btnChangeStatus = createModernButton("改状态", new java.awt.Color(16, 185, 129));
        btnOffShelf = createModernButton("下架", new java.awt.Color(249, 115, 22));
        btnCategories = createModernButton("分类管理", new java.awt.Color(139, 92, 246));
        // btnTrans 在顶部右侧已创建，这里不再重复

        // 设置按钮大小
        Dimension btnSize = new Dimension(90, 35);
        btnRefresh.setPreferredSize(btnSize);
        btnAdd.setPreferredSize(btnSize);
        btnEdit.setPreferredSize(btnSize);
        btnDelete.setPreferredSize(btnSize);
        btnImport.setPreferredSize(btnSize);
        btnChangeStock.setPreferredSize(new Dimension(100, 35));
        btnChangeStatus.setPreferredSize(new Dimension(100, 35));
        btnOffShelf.setPreferredSize(btnSize);
        btnCategories.setPreferredSize(new Dimension(110, 35));
        // btnTrans 已在顶部设置

        // 按功能分组添加按钮
        toolbar.add(btnRefresh);
        toolbar.add(createSeparator());
        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(createSeparator());
        toolbar.add(btnCategories); // 将分类管理按钮提前到更显眼位置
        toolbar.add(btnImport);
        toolbar.add(createSeparator());
        toolbar.add(btnChangeStock);
        toolbar.add(btnChangeStatus);
        toolbar.add(btnOffShelf);
        // 顶部已包含全选/取消全选/查看记录，底部不再重复添加
        // toolbar.add(createSeparator());
        // toolbar.add(btnSelectAll);
        // toolbar.add(btnClearSelection);
        // toolbar.add(createSeparator());
        // toolbar.add(btnTrans);

        toolbarContainer.add(toolbar, BorderLayout.CENTER);
        backgroundPanel.add(toolbarContainer, BorderLayout.SOUTH);

        // 创建商品列表面板 - 使用自定义的管理员友好布局
        productListPanel = new JPanel();
        productListPanel.setLayout(new BoxLayout(productListPanel, BoxLayout.Y_AXIS));
        productListPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(productListPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setBlockIncrement(80);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        // 初始化与普通用户一致的网格视图，作为管理员端的卡片视图
        productGridPanel = new ProductGridPanel();
        productGridPanel.setAdminMode(true);
        productGridPanel.setOpaque(false);

        // 监听 card 视图的点击与管理动作，映射到当前的 currentProductList 和管理操作
        productGridPanel.addProductClickListener(e -> {
            try {
                String cmd = e.getActionCommand();
                if (cmd == null) return;
                if (cmd.startsWith("detail:")) {
                    int idx = Integer.parseInt(cmd.substring("detail:".length()));
                    if (idx >= 0 && idx < currentProductList.size()) {
                        Map<String,Object> p = currentProductList.get(idx);
                        try { ProductDetailDialog dlg = new ProductDetailDialog(SwingUtilities.getWindowAncestor(ShopManagerPanel.this), p, nettyClient, false); dlg.setVisible(true); } catch (Exception ignored) {}
                    }
                } else if (cmd.startsWith("select:")) {
                    int idx = Integer.parseInt(cmd.substring("select:".length()));
                    if (idx >= 0 && idx < currentProductList.size()) {
                        Map<String,Object> p = currentProductList.get(idx);
                        // 单选行为：清空旧选择并选中当前
                        selectedProducts.clear();
                        selectedProducts.add(p);
                        selectedProduct = p;
                        updateSelectionStatus();
                    }
                }
            } catch (Exception ignored) {}
        });

        // 管理员卡片上的编辑/删除动作映射
        productGridPanel.addAdminActionListener(e -> {
            try {
                String cmd = e.getActionCommand();
                if (cmd == null) return;
                if (cmd.startsWith("edit:")) {
                    int idx = Integer.parseInt(cmd.substring("edit:".length()));
                    if (idx >= 0 && idx < currentProductList.size()) {
                        Map<String,Object> p = currentProductList.get(idx);
                        openProductDialog(p);
                    }
                } else if (cmd.startsWith("delete:")) {
                    int idx = Integer.parseInt(cmd.substring("delete:".length()));
                    if (idx >= 0 && idx < currentProductList.size()) {
                        Map<String,Object> p = currentProductList.get(idx);
                        selectedProducts.clear(); selectedProducts.add(p); selectedProduct = p;
                        deleteSelected();
                    }
                }
            } catch (Exception ignored) {}
        });

        // 新增：表格视图组件与左侧容器（CardLayout）用于切换视图
        String[] tableCols = new String[]{"ID","编码","名称","价格","库存","状态","分类","描述","更新时间"};
        productTablePanel = new ProductTablePanel(tableCols);
        // 绑定表格右键动作：编辑/删除/切换上架
        productTablePanel.addTableActionListener(ev -> {
            try {
                String cmd = ev.getActionCommand(); // 格式 action:id
                if (cmd == null || cmd.isEmpty()) return;
                String[] parts = cmd.split(":", 2);
                if (parts.length < 2) return;
                String action = parts[0]; String id = parts[1];
                // 在 currentProductList 中根据 productId 查找对应产品对象
                Map<String,Object> target = null;
                for (Map<String,Object> p : currentProductList) {
                    String pid = getProductId(p);
                    if (pid != null && pid.equals(id)) { target = p; break; }
                }
                if (target == null) return;
                if ("edit".equalsIgnoreCase(action)) {
                    openProductDialog(target);
                } else if ("delete".equalsIgnoreCase(action)) {
                    if (JOptionPane.showConfirmDialog(ShopManagerPanel.this, "确定删除该商品吗？", "确认", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        java.util.List<Map<String,Object>> single = new java.util.ArrayList<>(); single.add(target);
                        deleteMultipleProducts(single);
                    }
                } else if ("toggle".equalsIgnoreCase(action)) {
                    String cur = String.valueOf(target.getOrDefault("status", "ON_SHELF"));
                    String next = "ON_SHELF".equalsIgnoreCase(cur) ? "OFF_SHELF" : "ON_SHELF";
                    java.util.List<Map<String,Object>> single = new java.util.ArrayList<>(); single.add(target);
                    changeStatusMultipleProducts(single, next);
                }
            } catch (Exception ignored) {}
        });
        // 将产品列表数据通过 productTablePanel 也能展示（稍后在 updateProducts 填充）
        JPanel leftContainer = new JPanel(new CardLayout());
        // 使用 productGridPanel 作为卡片视图，保留旧的线性列表 scrollPane 作为回退
        leftContainer.add(productGridPanel, "card");
        leftContainer.add(productTablePanel, "table");
        listScrollPane = leftContainer;

        // 使用水平分割面板容纳主列表与交易侧栏（左侧使用容器）
        // 初始化交易侧栏（管理员界面之前未创建，导致查看记录按钮无效）
        transactionPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint sidebarGradient = new GradientPaint(
                    0, 0, new java.awt.Color(248, 250, 252),
                    getWidth(), getHeight(), new java.awt.Color(241, 245, 249)
                );
                g2.setPaint(sidebarGradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new java.awt.Color(203, 213, 225));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
        };
        transactionPanel.setPreferredSize(new Dimension(360, 0));
        transactionPanel.setVisible(false);
        transactionPanel.setOpaque(false);

        JPanel transTop = new JPanel(new BorderLayout()); transTop.setOpaque(false);
        JLabel transTitle = new JLabel("交易记录"); transTitle.setFont(getScaledFont(16, Font.BOLD)); transTitle.setForeground(new Color(71,85,105)); transTitle.setBorder(BorderFactory.createEmptyBorder(12,15,12,15)); transTop.add(transTitle, BorderLayout.WEST);
        transactionPanel.add(transTop, BorderLayout.NORTH);

        transactionListPanel = new JPanel();
        transactionListPanel.setLayout(new BoxLayout(transactionListPanel, BoxLayout.Y_AXIS));
        transactionListPanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        transactionListPanel.setOpaque(false);

        JScrollPane transScroll = new JScrollPane(transactionListPanel);
        transScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        transScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        transScroll.getVerticalScrollBar().setUnitIncrement(20);
        transScroll.setOpaque(false); transScroll.getViewport().setOpaque(false); transScroll.setBorder(null);
        transactionPanel.add(transScroll, BorderLayout.CENTER);

        mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, transactionPanel);
         mainSplit.setResizeWeight(1.0);
         mainSplit.setDividerSize(8);
         mainSplit.setContinuousLayout(true);
         mainSplit.setOneTouchExpandable(false);
         mainSplit.setOpaque(false);

        // 使用分层面板以支持右下角悬浮分类管理面板
        layeredCenter = new JLayeredPane();
        layeredCenter.setLayout(null);
        layeredCenter.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutLayeredChildren();
            }
        });
        layeredCenter.add(mainSplit, JLayeredPane.DEFAULT_LAYER);
        // 确保首次构建时也能进行一次布局设置，避免初始未触发 componentResized 导致子组件未显示
        SwingUtilities.invokeLater(this::layoutLayeredChildren);
        // 绑定视图切换按钮逻辑
        btnToggleView.addActionListener(e -> {
            isTableView = !isTableView;
            CardLayout cl = (CardLayout) leftContainer.getLayout();
            if (isTableView) {
                cl.show(leftContainer, "table");
                btnToggleView.setText("卡片视图");
                // 将当前列表数据填充到表格
                try { productTablePanel.setProductsForPage(currentProductList, 1, Math.max(1, currentProductList.size())); } catch (Exception ignored) {}
                // 将表格点击事件映射为选中集合
                productTablePanel.addTableMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseClicked(java.awt.event.MouseEvent ev) {
                        syncSelectionFromTable();
                    }
                });
            } else {
                cl.show(leftContainer, "card");
                btnToggleView.setText("表格视图");
                // 切回卡片视图时同步表格选择到卡片选择（若需要）
                syncSelectionFromTable();
            }
        });

        backgroundPanel.add(layeredCenter, BorderLayout.CENTER);

        add(backgroundPanel, BorderLayout.CENTER);

        initCategoryFloatingPanel();

        // Events
        btnRefresh.addActionListener(e -> loadAllProducts());
        btnSearchName.addActionListener(e -> searchByName());
        btnSearchCategory.addActionListener(e -> searchByCategory());
        btnAdd.addActionListener(e -> openProductDialog(null));
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        btnChangeStock.addActionListener(e -> changeStockSelected());
        btnChangeStatus.addActionListener(e -> changeStatusSelected());
        btnCategories.addActionListener(e -> openCategoryManager());
        btnTrans.addActionListener(e -> {
            if (transactionPanel != null && transactionPanel.isVisible())
                hideTransactions();
            else
                showTransactions();
        });
        btnImport.addActionListener(e -> importProducts());
        btnOffShelf.addActionListener(e -> offShelfSelected());
        btnSelectAll.addActionListener(e -> selectAllProducts());
        btnClearSelection.addActionListener(e -> clearAllSelections());
    }

    /**
     * 创建分隔符
     */
    private Component createSeparator() {
        JPanel separator = new JPanel();
        separator.setPreferredSize(new Dimension(1, 25));
        separator.setOpaque(false);
        return separator;
    }

    // === 新增：分层子组件布局 ===
    private void layoutLayeredChildren() {
        if (layeredCenter == null) return;
        int w = layeredCenter.getWidth();
        int h = layeredCenter.getHeight();
        if (w <= 0 || h <= 0) return;

        if (mainSplit != null) mainSplit.setBounds(0, 0, w, h);
        if (categoryFloatingPanel != null && categoryFloatingPanel.isVisible()) {
            int cw = Math.min(520, Math.max(360, w / 2));
            int ch = Math.min(400, Math.max(260, h / 2));
            int margin = 14;
            categoryFloatingPanel.setBounds(w - cw - margin, h - ch - margin, cw, ch);
        }
    }
    // === 新增：初始化分类浮动面板 ===
    private void initCategoryFloatingPanel() {
        categoryFloatingPanel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new java.awt.Color(255,255,255,245));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(new java.awt.Color(200,200,200));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                g2.dispose();
            }
        }; categoryFloatingPanel.setOpaque(false); categoryFloatingPanel.setVisible(false); categoryFloatingPanel.setBorder(new EmptyBorder(6,8,8,8));
        JPanel head = new JPanel(new BorderLayout()); head.setOpaque(false);
        JLabel lbl = new JLabel("分类管理"); lbl.setFont(new java.awt.Font("微软雅黑", java.awt.Font.BOLD, 14)); head.add(lbl, BorderLayout.WEST);
        JPanel headBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0)); headBtns.setOpaque(false);
        JButton btnClose = new JButton("×"); btnClose.setMargin(new Insets(2,8,2,8)); btnClose.addActionListener(e -> toggleCategoryPanel(false));
        headBtns.add(btnClose); head.add(headBtns, BorderLayout.EAST); categoryFloatingPanel.add(head, BorderLayout.NORTH);
        categoryModel = new DefaultTableModel(new Object[]{"ID","代码","名称"},0){ @Override public boolean isCellEditable(int r,int c){ return false; } };
        categoryTable = new JTable(categoryModel); categoryTable.setRowHeight(24);
        try { var col0 = categoryTable.getColumnModel().getColumn(0); col0.setMinWidth(0); col0.setMaxWidth(0); col0.setPreferredWidth(0); col0.setResizable(false);} catch (Exception ignored) {}
        JScrollPane sp = new JScrollPane(categoryTable); sp.getVerticalScrollBar().setUnitIncrement(16); categoryFloatingPanel.add(sp, BorderLayout.CENTER);
        JPanel ops = new JPanel(new FlowLayout(FlowLayout.LEFT,8,4)); ops.setOpaque(false);
        JButton bRefresh = new JButton("刷新"), bAdd = new JButton("新增"), bEdit = new JButton("编辑"), bDel = new JButton("删除");
        ops.add(bRefresh); ops.add(bAdd); ops.add(bEdit); ops.add(bDel); categoryFloatingPanel.add(ops, BorderLayout.SOUTH);
        Runnable refresh = this::refreshCategoryList; bRefresh.addActionListener(e -> refresh.run()); bAdd.addActionListener(e -> addCategoryDialog()); bEdit.addActionListener(e -> editCategoryDialog()); bDel.addActionListener(e -> deleteCategoryDialog());
        layeredCenter.add(categoryFloatingPanel, JLayeredPane.POPUP_LAYER);
    }
    // === 新增：分类 CRUD 辅助 ===
    private void refreshCategoryList() {
        runBg(() -> new Request(managerUri()).addParam("action","CATEGORY_LIST"), resp -> {
            categoryModel.setRowCount(0);
            @SuppressWarnings("unchecked") List<Map<String,Object>> list = (List<Map<String,Object>>) resp.getData();
            if (list != null) {
                list.sort((a,b)->{ Object ai=a.getOrDefault("categoryId",a.get("category_id")); Object bi=b.getOrDefault("categoryId",b.get("category_id")); String as=ai==null?"":ai.toString(); String bs=bi==null?"":bi.toString(); try { long an=Long.parseLong(as); long bn=Long.parseLong(bs); return Long.compare(an,bn);} catch(Exception ex){ return as.compareTo(bs);} });
                for (Map<String,Object> r : list) categoryModel.addRow(new Object[]{ r.getOrDefault("categoryId", r.get("category_id")), r.getOrDefault("categoryCode", r.get("category_code")), r.getOrDefault("categoryName", r.get("category_name")) });
            }
        }, this::showError);
    }
    private void addCategoryDialog() {
        JTextField fCode=new JTextField(); JTextField fName=new JTextField(); JPanel f=new JPanel(new GridLayout(0,2,8,8)); f.add(new JLabel("代码")); f.add(fCode); f.add(new JLabel("名称")); f.add(fName);
        if (JOptionPane.showConfirmDialog(this,f,"新增分类",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) return;
        Map<String,Object> cat=new HashMap<>(); cat.put("categoryCode",fCode.getText().trim()); cat.put("categoryName",fName.getText().trim());
        runBg(() -> new Request(managerUri()).addParam("action","CATEGORY_CREATE").addParam("category", JsonUtils.toJson(cat)), r->{ info("创建成功"); refreshCategoryList(); }, this::showError);
    }
    private void editCategoryDialog() {
        int r=categoryTable.getSelectedRow(); if (r<0){ warn("请选择一行"); return; }
        String id=asPlainString(categoryModel.getValueAt(r,0)); String code=asPlainString(categoryModel.getValueAt(r,1)); String name=asPlainString(categoryModel.getValueAt(r,2));
        JTextField fCode=new JTextField(code); JTextField fName=new JTextField(name); JPanel f=new JPanel(new GridLayout(0,2,8,8)); f.add(new JLabel("代码")); f.add(fCode); f.add(new JLabel("名称")); f.add(fName);
        if (JOptionPane.showConfirmDialog(this,f,"编辑分类",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION) return;
        Map<String,Object> cat=new HashMap<>(); cat.put("categoryId",id); cat.put("categoryCode",fCode.getText().trim()); cat.put("categoryName",fName.getText().trim());
        runBg(() -> new Request(managerUri()).addParam("action","CATEGORY_UPDATE").addParam("category", JsonUtils.toJson(cat)), r2->{ info("更新成功"); refreshCategoryList(); }, this::showError);
    }
    private void deleteCategoryDialog() {
        int r=categoryTable.getSelectedRow(); if (r<0){ warn("请选择一行"); return; }
        if (JOptionPane.showConfirmDialog(this,"确定删除该分类吗？","确认",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        String id=asPlainString(categoryModel.getValueAt(r,0));
        runBg(() -> new Request(managerUri()).addParam("action","CATEGORY_DELETE").addParam("categoryId",id), r3->{ info("删除成功"); refreshCategoryList(); }, this::showError);
    }
    private void toggleCategoryPanel(boolean show) {
        if (categoryFloatingPanel==null) return; categoryPanelVisible=show; categoryFloatingPanel.setVisible(show);
        if (show) { refreshCategoryList(); layoutLayeredChildren(); }
    }
    // 替换原对话框实现
    private void openCategoryManager() { toggleCategoryPanel(!categoryPanelVisible); }

    // === 补回缺失的基础方法 ===
    private String managerUri() {
        Object pr = userData.get("primaryRole");
        if (pr == null) pr = userData.get("role");
        String role = pr != null ? pr.toString().toLowerCase() : "";
        if (role.contains("admin")) return "shop/adminManager"; // admin 别名路由
        return "shop/manager";
    }

    private void loadAllProducts() {
        runBg(() -> new Request(managerUri()).addParam("action","PRODUCT_LIST"), this::updateProducts, this::showError);
    }

    private void searchByName() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) { JOptionPane.showMessageDialog(this, "请输入名称", "提示", JOptionPane.WARNING_MESSAGE); return; }
        runBg(() -> new Request(managerUri()).addParam("action","PRODUCT_SEARCH_BY_NAME").addParam("name", name), this::updateProducts, this::showError);
    }

    private void searchByCategory() {
        String cat = txtCategory.getText().trim();
        if (cat.isEmpty()) { JOptionPane.showMessageDialog(this, "请输入分类代码", "提示", JOptionPane.WARNING_MESSAGE); return; }
        runBg(() -> new Request(managerUri()).addParam("action","PRODUCT_SEARCH_BY_CATEGORY").addParam("category", cat), this::updateProducts, this::showError);
    }

    private void openProductDialog(Map<String, Object> initial) {
        JTextField fCode = new JTextField(initial != null ? s(initial.get("productCode"), initial.get("Product_code")) : "");
        JTextField fName = new JTextField(initial != null ? s(initial.get("productName"), initial.get("Productname")) : "");
        JTextField fPrice = new JTextField(initial != null ? s(initial.get("price"), initial.get("Price")) : "");
        JTextField fStock = new JTextField(initial != null ? s(initial.get("stock"), initial.get("Stock")) : "");
        JTextField fCategory = new JTextField(initial != null ? s(initial.get("category"), initial.get("Product_category")) : "");
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"ON_SHELF","OFF_SHELF","SOLD_OUT"});
        if (initial != null) {
            Object st = initial.getOrDefault("status", initial.get("Product_status"));
            if (st != null) cbStatus.setSelectedItem(st.toString());
        }
        JTextArea fDesc = new JTextArea(initial != null ? s(initial.get("description"), initial.get("Product_description")) : "");
        fDesc.setRows(3);

        JPanel form = new JPanel(new GridLayout(0,2,8,8));
        form.add(new JLabel("编码")); form.add(fCode);
        form.add(new JLabel("名称")); form.add(fName);
        form.add(new JLabel("价格")); form.add(fPrice);
        form.add(new JLabel("库存")); form.add(fStock);
        form.add(new JLabel("分类")); form.add(fCategory);
        form.add(new JLabel("状态")); form.add(cbStatus);
        form.add(new JLabel("描述")); form.add(new JScrollPane(fDesc));

        int opt = JOptionPane.showConfirmDialog(this, form, initial==null?"新增商品":"编辑商品", JOptionPane.OK_CANCEL_OPTION);
        if (opt != JOptionPane.OK_OPTION) return;

        // 验证数值输入
        String priceStr = fPrice.getText().trim();
        java.math.BigDecimal priceVal = null;
        if (!priceStr.isEmpty()) {
            try {
                priceVal = new java.math.BigDecimal(priceStr);
                priceVal = priceVal.setScale(2, java.math.RoundingMode.HALF_UP);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "价格格式不正确，请输入数字，例如 12.50", "输入错误", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else {
            priceVal = java.math.BigDecimal.ZERO;
        }

        String stockStr = fStock.getText().trim();
        int stockVal = 0;
        if (!stockStr.isEmpty()) {
            try { stockVal = parseInt(stockStr); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "库存格式不正确，请输入整数，例如 10", "输入错误", JOptionPane.WARNING_MESSAGE); return; }
        }

        Map<String,Object> product = new HashMap<>();
        if (initial != null) {
            Object id = initial.getOrDefault("productId", initial.get("product_Id"));
            if (id != null) product.put("productId", asPlainString(id));
        }
        product.put("productCode", fCode.getText().trim());
        product.put("productName", fName.getText().trim());
        product.put("price", priceVal);
        product.put("stock", stockVal);
        product.put("category", fCategory.getText().trim());
        product.put("status", cbStatus.getSelectedItem().toString());
        product.put("description", fDesc.getText().trim());

        String json = JsonUtils.toJson(product);
        if (initial == null) {
            runBg(() -> new Request(managerUri()).addParam("action","PRODUCT_CREATE").addParam("product", json), r -> { info("创建成功"); loadAllProducts(); }, this::showError);
        } else {
            runBg(() -> new Request(managerUri()).addParam("action","PRODUCT_UPDATE").addParam("product", json), r -> { info("更新成功"); loadAllProducts(); }, this::showError);
        }
    }

    private void editSelected() {
        if (selectedProduct == null) {
            warn("请先点击选择一个商品");
            return;
        }
        openProductDialog(selectedProduct);
    }

    private void deleteSelected() {
        if (selectedProducts.isEmpty()) {
            warn("请先点击选择一个或多个商品（按住Ctrl键可多选）");
            return;
        }
        
        String message;
        if (selectedProducts.size() == 1) {
            String productName = getProductName(selectedProducts.get(0));
            message = "确定删除商品 \"" + productName + "\" 吗？";
        } else {
            message = "确定删除选中的 " + selectedProducts.size() + " 个商品吗？";
        }
        
        if (JOptionPane.showConfirmDialog(this, message, "确认删除", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        
        // 批量删除
        deleteMultipleProducts(selectedProducts);
    }
    
    private void deleteMultipleProducts(java.util.List<Map<String, Object>> productsToDelete) {
        int successCount = 0;
        int failCount = 0;
        
        for (Map<String, Object> product : productsToDelete) {
            String id = getProductId(product);
            if (id == null || id.isEmpty()) {
                failCount++;
                continue;
            }
            
            try {
                Request request = new Request(managerUri())
                    .addParam("action", "PRODUCT_DELETE")
                    .addParam("productId", id);
                Response response = nettyClient.sendRequest(request).get(20, java.util.concurrent.TimeUnit.SECONDS);
                
                if (response != null && "SUCCESS".equals(response.getStatus())) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("删除商品失败: {}", e.getMessage());
                failCount++;
            }
        }
        
        // 显示结果
        if (failCount == 0) {
            info("成功删除 " + successCount + " 个商品");
        } else if (successCount == 0) {
            showError("删除失败，请重试");
        } else {
            warn("成功删除 " + successCount + " 个商品，" + failCount + " 个删除失败");
        }
        
        // 清除选择并刷新列表
        selectedProducts.clear();
        selectedProduct = null;
        loadAllProducts();
    }
    
    private String getProductId(Map<String, Object> product) {
        if (product == null) return "";
        Object id = null;
        String[] keys = new String[]{"productId", "product_Id", "product_id", "productid", "id"};
        for (String k : keys) {
            if (product.containsKey(k)) {
                id = product.get(k);
                if (id != null) break;
            }
        }
        return asPlainString(id);
    }
    
    private String getProductName(Map<String, Object> product) {
        if (product == null) return "未知商品";
        Object name = product.getOrDefault("productName", product.get("Productname"));
        return name != null ? name.toString() : "未知商品";
    }

    // 新增：从map中按候选键顺序取第一个存在且非空的值
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

    // 判断当前用户是否为管理员
    private boolean isAdminUser() {
        try {
            Object pr = userData.get("primaryRole");
            if (pr == null) pr = userData.get("role");
            if (pr == null) return false;
            String role = pr.toString().toLowerCase();
            return role.contains("admin");
        } catch (Exception ignored) {
            return false;
        }
    }

     private void changeStockSelected() {
        if (selectedProducts.isEmpty()) {
            warn("请先点击选择一个或多个商品（按住Ctrl键可多选）");
            return;
        }
        
        String deltaStr = JOptionPane.showInputDialog(this, 
            "库存变更(正数增加/负数减少)\n将应用到选中的 " + selectedProducts.size() + " 个商品", "0");
        if (deltaStr == null) return;
        
        int delta;
        try { 
            delta = parseInt(deltaStr); 
        } catch (Exception e) { 
            warn("请输入整数"); 
            return; 
        }
        
        // 批量更新库存
        changeStockMultipleProducts(selectedProducts, delta);
    }
    
    private void changeStockMultipleProducts(java.util.List<Map<String, Object>> products, int delta) {
        int successCount = 0;
        int failCount = 0;
        
        for (Map<String, Object> product : products) {
            String id = getProductId(product);
            if (id == null || id.isEmpty()) {
                failCount++;
                continue;
            }
            
            try {
                Request request = new Request(managerUri())
                    .addParam("action", "PRODUCT_CHANGE_STOCK")
                    .addParam("productId", id)
                    .addParam("delta", String.valueOf(delta));
                Response response = nettyClient.sendRequest(request).get(20, java.util.concurrent.TimeUnit.SECONDS);
                
                if (response != null && "SUCCESS".equals(response.getStatus())) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("更新库存失败: {}", e.getMessage());
                failCount++;
            }
        }
        
        // 显示结果
        if (failCount == 0) {
            info("成功更新 " + successCount + " 个商品的库存");
        } else if (successCount == 0) {
            showError("库存更新失败，请重试");
        } else {
            warn("成功更新 " + successCount + " 个商品的库存，" + failCount + " 个更新失败");
        }
        
        // 刷新列表
        loadAllProducts();
    }

    private void changeStatusSelected() {
        if (selectedProducts.isEmpty()) {
            warn("请先点击选择一个或多个商品（按住Ctrl键可多选）");
            return;
        }
        
        String status = (String) JOptionPane.showInputDialog(this, 
            "选择状态\n将应用到选中的 " + selectedProducts.size() + " 个商品", 
            "修改状态", JOptionPane.PLAIN_MESSAGE, null, 
            new String[]{"ON_SHELF","OFF_SHELF","SOLD_OUT"}, "ON_SHELF");
        if (status == null) return;
        
        // 批量更新状态
        changeStatusMultipleProducts(selectedProducts, status);
    }
    
    private void changeStatusMultipleProducts(java.util.List<Map<String, Object>> products, String status) {
        int successCount = 0;
        int failCount = 0;
        
        for (Map<String, Object> product : products) {
            String id = getProductId(product);
            if (id == null || id.isEmpty()) {
                failCount++;
                continue;
            }
            
            try {
                Request request = new Request(managerUri())
                    .addParam("action", "PRODUCT_CHANGE_STATUS")
                    .addParam("productId", id)
                    .addParam("status", status);
                Response response = nettyClient.sendRequest(request).get(20, java.util.concurrent.TimeUnit.SECONDS);
                
                if (response != null && "SUCCESS".equals(response.getStatus())) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("更新状态失败: {}", e.getMessage());
                failCount++;
            }
        }
        
        // 显示结果
        if (failCount == 0) {
            info("成功更新 " + successCount + " 个商品的状态");
        } else if (successCount == 0) {
            showError("状态更新失败，请重试");
        } else {
            warn("成功更新 " + successCount + " 个商品的状态，" + failCount + " 个更新失败");
        }
        
        // 刷新列表
        loadAllProducts();
    }

    private void offShelfSelected() {
        if (selectedProducts.isEmpty()) {
            warn("请先点击选择一个或多个商品（按住Ctrl键可多选）");
            return;
        }
        
        String message;
        if (selectedProducts.size() == 1) {
            String productName = getProductName(selectedProducts.get(0));
            message = "确定下架商品 \"" + productName + "\" 吗？";
        } else {
            message = "确定下架选中的 " + selectedProducts.size() + " 个商品吗？";
        }
        
        if (JOptionPane.showConfirmDialog(this, message, "确认下架", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        
        // 批量下架
        changeStatusMultipleProducts(selectedProducts, "OFF_SHELF");
    }

    // === 恢复：交易侧栏显示/隐藏与布局 ===
    private void showTransactions() {
        if (transactionPanel == null) return;
        transactionPanel.setVisible(true);
        ensureManagerSplitLayout(true);
        loadTransList(transactionListPanel, SwingUtilities.getWindowAncestor(this));
    }
    private void hideTransactions() {
        if (transactionPanel == null) return;
        transactionPanel.setVisible(false);
        SwingUtilities.invokeLater(() -> {
            try { transactionListPanel.removeAll(); transactionListPanel.revalidate(); transactionListPanel.repaint(); } catch (Exception ignored) {}
            ensureManagerSplitLayout(false);
        });
    }
    private void ensureManagerSplitLayout(boolean sidebarVisible){
        if (mainSplit == null) return;
        SwingUtilities.invokeLater(() -> {
            int total = getWidth(); if (total <= 0) return;
            if (sidebarVisible && transactionPanel != null && transactionPanel.isVisible()) {
                int desired = transactionPanel.getPreferredSize()!=null ? transactionPanel.getPreferredSize().width : 360;
                int loc = Math.max(100, total - desired);
                mainSplit.setDividerLocation(loc);
            } else {
                mainSplit.setDividerLocation(1.0);
            }
        });
    }

    // === 修改交易记录加载逻辑，使用统一格式 ===
    private void loadTransList(JPanel list, Window parent) {
        runBg(() -> new Request(managerUri()).addParam("action","TRANS_LIST_ALL"), resp -> {
            @SuppressWarnings("unchecked") List<Map<String,Object>> listResp = (List<Map<String,Object>>) resp.getData();
            final List<Map<String,Object>> safeList = listResp == null ? Collections.emptyList() : listResp;
            SwingUtilities.invokeLater(() -> {
                list.removeAll();
                for (Map<String,Object> r : safeList) {
                    JPanel orderPanel = createOrderPanel(r);
                    list.add(orderPanel);
                    list.add(Box.createVerticalStrut(8));
                }
                list.revalidate(); list.repaint();
            });
        }, this::showError);
    }

    // === 创建与普通用户相同格式的订单面板 ===
    private JPanel createOrderPanel(Map<String, Object> order) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new java.awt.Color(229, 231, 235)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // 修复字段映射以匹配服务器返回的数据结构
        String orderId = String.valueOf(order.getOrDefault("transId", order.getOrDefault("orderId", "")));
        String status = String.valueOf(order.getOrDefault("status", ""));
        String amount = String.valueOf(order.getOrDefault("amount", order.getOrDefault("totalAmount", "0.00")));
        String date = String.valueOf(order.getOrDefault("transTime", order.getOrDefault("createdAt", "")));
        String productName = String.valueOf(order.getOrDefault("productName", ""));
        String qty = String.valueOf(order.getOrDefault("qty", "1"));

        // 如果商品名为空，尝试从productId获取
        if (productName.isEmpty()) {
            Object pidObj = order.getOrDefault("productId", order.getOrDefault("product_Id", order.getOrDefault("product_id", order.get("productId"))));
            String pid = asPlainString(pidObj);
            if (!pid.isEmpty()) {
                productName = "商品#" + pid;
                // 异步获取商品名称
                runBg(() -> new Request(managerUri()).addParam("action", "PRODUCT_GET_BY_ID").addParam("productId", pid), resp -> {
                    try {
                        @SuppressWarnings("unchecked") Map<String,Object> prod = (Map<String,Object>) resp.getData();
                        if (prod != null) {
                            Object fn = prod.getOrDefault("productName", prod.get("Productname"));
                            if (fn != null && !fn.toString().isEmpty()) {
                                String finalName = fn.toString();
                                SwingUtilities.invokeLater(() -> {
                                    // 找到商品标签并更新
                                    Component[] components = panel.getComponents();
                                    for (Component comp : components) {
                                        if (comp instanceof JPanel) {
                                            JPanel textPanel = (JPanel) comp;
                                            Component[] textComponents = textPanel.getComponents();
                                            if (textComponents.length > 1 && textComponents[1] instanceof JLabel) {
                                                JLabel productLabel = (JLabel) textComponents[1];
                                                productLabel.setText("商品: " + finalName + " × " + qty);
                                                break;
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    } catch (Exception ex) {
                        log.warn("查询商品名失败: {}", ex.getMessage());
                    }
                }, err -> log.warn("查询商品名失败: {}", err));
            } else {
                productName = "未知商品";
            }
        }

        JLabel idLabel = new JLabel("订单号: " + orderId);
        idLabel.setFont(getScaledFont(SMALL_FONT_SIZE, java.awt.Font.BOLD));

        JLabel productLabel = new JLabel("商品: " + productName + " × " + qty);
        productLabel.setFont(getScaledFont(SMALL_FONT_SIZE));

        JLabel statusLabel = new JLabel("状态: " + getStatusText(status));
        statusLabel.setFont(getScaledFont(SMALL_FONT_SIZE));

        JLabel amountLabel = new JLabel("金额: ¥" + formatAmount(amount));
        amountLabel.setFont(getScaledFont(SMALL_FONT_SIZE));

        JLabel dateLabel = new JLabel("日期: " + date);
        dateLabel.setFont(getScaledFont(SMALL_FONT_SIZE));
        dateLabel.setForeground(java.awt.Color.GRAY);

        // 将日期与购买人信息放入可折叠的 detailsPanel（默认隐藏），并添加右侧折叠按钮
        // 提取购买者姓名与校园卡号，支持多种字段命名
        Object cardObj = firstPresent(order, "cardId", "cardNum", "cardnum", "card_id", "card_no", "cardNo", "cardNumber", "card_number");
        Object buyerObj = firstPresent(order, "buyer", "buyerName", "userName", "username", "realName", "name", "buyerName", "user_name");
        String cardIdStr = cardObj == null ? "" : cardObj.toString();
        String buyerNameStr = buyerObj == null ? "" : buyerObj.toString();
        String buyerDisplay;
        boolean isAdmin = isAdminUser();
        if (isAdmin) {
            // 管理员可以看到姓名与卡号
            if (!buyerNameStr.isEmpty() && !cardIdStr.isEmpty()) {
                buyerDisplay = buyerNameStr + " (卡号: " + cardIdStr + ")";
            } else if (!cardIdStr.isEmpty()) {
                buyerDisplay = "卡号: " + cardIdStr;
            } else if (!buyerNameStr.isEmpty()) {
                buyerDisplay = buyerNameStr;
            } else {
                buyerDisplay = "未知";
            }
        } else {
            // 非管理员（用户自己查看仅限自己的记录）不显示校园卡号，仅显示姓名或我
            // 修改：即使是非管理员，也优先显示购买者的校园卡号（如果后端返回），
            // 以避免在不知道购买者信息时都显示“我”。只有在没有卡号和姓名时才显示“我”。
            if (!cardIdStr.isEmpty()) {
                buyerDisplay = "卡号: " + cardIdStr;
            } else if (!buyerNameStr.isEmpty()) {
                buyerDisplay = buyerNameStr;
            } else {
                buyerDisplay = "我";
            }
        }
        JLabel buyerLabel = new JLabel("购买人: " + buyerDisplay);
        buyerLabel.setFont(getScaledFont(SMALL_FONT_SIZE));
        buyerLabel.setForeground(java.awt.Color.GRAY);

        // 主文本区不包含可折叠项（保持原有顺序和样式）
        JPanel textPanel = new JPanel(new GridLayout(4, 1));
        textPanel.setOpaque(false);
        textPanel.add(idLabel);
        textPanel.add(productLabel);
        textPanel.add(statusLabel);
        textPanel.add(amountLabel);

        // detailsPanel：包含日期和购买人，两行，默认隐藏
        JPanel detailsPanel = new JPanel(new GridLayout(2,1));
        detailsPanel.setOpaque(false);
        detailsPanel.add(dateLabel);
        detailsPanel.add(buyerLabel);
        detailsPanel.setVisible(false);

        // 折叠按钮，放在 panel 的东侧
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

        // 组装：文本区在中，折叠区域在下，按钮在东侧，保持其他格式不变
        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(detailsPanel, BorderLayout.SOUTH);
        panel.add(rightWrap, BorderLayout.EAST);

        return panel;
    }

    // === 恢复：商品列表刷新渲染 ===
    private void updateProducts(Response resp) {
        SwingUtilities.invokeLater(() -> {
            productListPanel.removeAll();
            // 清除之前的选择，避免在刷新后选中集合引用过期的对象
            selectedProducts.clear();
            selectedProduct = null;
            @SuppressWarnings("unchecked") List<Map<String,Object>> list = (List<Map<String,Object>>) resp.getData();
            if (list == null) return;
            // 保存当前产品列表供表格视图使用
            currentProductList = new ArrayList<>();
            currentProductList.addAll(list);
            // 如果当前是表格视图，先填充表格数据
            if (isTableView && productTablePanel != null) {
                try { productTablePanel.setProductsForPage(currentProductList, 1, Math.max(1, currentProductList.size())); } catch (Exception ignored) {}
            }
            // 如果当前为卡片视图，使用 productGridPanel 渲染统一样式（与普通用户一致）
            if (!isTableView && productGridPanel != null) {
                try { productGridPanel.setProductsForPage(currentProductList, 1, Math.max(1, currentProductList.size())); } catch (Exception ignored) {}
            }
            for (Map<String,Object> row : list) {
                try {
                    Object stockObj = row.getOrDefault("stock", row.get("Stock"));
                    int stockVal = 0;
                    if (stockObj instanceof Number) stockVal = ((Number)stockObj).intValue(); else if (stockObj != null) stockVal = Integer.parseInt(stockObj.toString());
                    String st = s(row.getOrDefault("status", row.get("Product_status")), "UNKNOWN");
                    if (stockVal <= 0 && "ON_SHELF".equalsIgnoreCase(st)) {
                        row.put("status", "SOLD_OUT"); row.put("stock", 0);
                        final String pid = asPlainString(row.getOrDefault("productId", row.get("product_Id")));
                        if (pid != null && !pid.isEmpty()) {
                            runBg(() -> new Request(managerUri()).addParam("action","PRODUCT_CHANGE_STATUS").addParam("productId", pid).addParam("status", "SOLD_OUT"), r -> {}, err -> log.warn("自动售罄失败: {}", err));
                        }
                    }
                } catch (Exception ignored) {}
                // 管理端改为使用统一的网格视图渲染，故这里保留旧渲染逻辑作为回退，不再向 productListPanel 添加条目
            }
            // 若仍在使用旧的线性列表（回退），刷新其 UI；否则网格面板已自行刷新
            try { productListPanel.revalidate(); productListPanel.repaint(); } catch (Exception ignored) {}
         });
     }

    // 将表格的选中行同步到 selectedProducts 并在卡片视图中高亮
    private void syncSelectionFromTable() {
        if (productTablePanel == null) return;
        selectedProducts.clear();
        int[] viewRows = productTablePanel.getSelectedViewRows();
        if (viewRows == null || viewRows.length == 0) {
            // 清除卡片高亮
            highlightSelectedInCardView();
            selectedProduct = null;
            updateSelectionStatus();
            // 同步到网格视图：取消选中
            try { if (productGridPanel != null) productGridPanel.clearSelection(); } catch (Exception ignored) {}
            return;
        }
        java.util.List<Integer> gridIdxs = new java.util.ArrayList<>();
        for (int vr : viewRows) {
            try {
                int modelRow = productTablePanel.convertRowIndexToModel(vr);
                Object idVal = productTablePanel.getModelValueAt(modelRow, 0);
                String id = asPlainString(idVal);
                // 在 currentProductList 中查找对应对象
                for (int i = 0; i < currentProductList.size(); i++) {
                    Map<String,Object> p = currentProductList.get(i);
                    String pid = getProductId(p);
                    if (pid.equals(id)) { selectedProducts.add(p); gridIdxs.add(i); break; }
                }
            } catch (Exception ignored) {}
        }
        if (!selectedProducts.isEmpty()) selectedProduct = selectedProducts.get(selectedProducts.size()-1); else selectedProduct = null;
        highlightSelectedInCardView();
        updateSelectionStatus();
        // 同步到网格视图：高亮所有选中的卡片并滚动到第一个
        try {
            if (productGridPanel != null) {
                if (gridIdxs.isEmpty()) productGridPanel.clearSelection(); else productGridPanel.setSelectedIndices(gridIdxs);
                if (!gridIdxs.isEmpty() && !isTableView) productGridPanel.selectCardIndex(gridIdxs.get(0));
            }
        } catch (Exception ignored) {}
    }

    // 在卡片视图中根据 selectedProducts 高亮对应项
    private void highlightSelectedInCardView() {
        // 优先同步到统一的 productGridPanel（管理员/卡片视图）
        try {
            if (productGridPanel != null) {
                java.util.List<Integer> idxs = new java.util.ArrayList<>();
                for (Map<String,Object> p : selectedProducts) {
                    for (int i = 0; i < currentProductList.size(); i++) {
                        if (getProductId(currentProductList.get(i)).equals(getProductId(p))) { idxs.add(i); break; }
                    }
                }
                if (idxs.isEmpty()) productGridPanel.clearSelection(); else productGridPanel.setSelectedIndices(idxs);
                // 如果存在选中项，确保至少第一个可见
                if (!idxs.isEmpty()) productGridPanel.selectCardIndex(idxs.get(0));
                return;
            }
        } catch (Exception ignored) {}

        // 回退：旧的线性列表方式（如果 productGridPanel 不可用）
        Set<String> ids = new HashSet<>();
        for (Map<String,Object> p : selectedProducts) ids.add(getProductId(p));
        for (Component comp : productListPanel.getComponents()) {
            if (comp instanceof JPanel) {
                Object prod = ((JPanel) comp).getClientProperty("productData");
                String pid = prod instanceof Map ? getProductId((Map<String,Object>)prod) : "";
                if (ids.contains(pid)) comp.setBackground(new Color(230, 244, 255)); else comp.setBackground(Color.WHITE);
            }
        }
    }

    private void selectProduct(JPanel itemPanel, Map<String, Object> product) {
        selectProduct(itemPanel, product, false);
    }
    
    private void selectProduct(JPanel itemPanel, Map<String, Object> product, boolean multiSelect) {
        if (!multiSelect) {
            // 单选模式：清除所有选择
            clearAllSelections();
            selectedProducts.clear();
        }
        
        // 检查是否已经选中
        boolean alreadySelected = selectedProducts.contains(product);
        
        if (alreadySelected) {
            // 如果已经选中，则取消选择
            selectedProducts.remove(product);
            itemPanel.setBackground(java.awt.Color.WHITE);
        } else {
            // 如果未选中，则添加到选择列表
            selectedProducts.add(product);
            itemPanel.setBackground(new java.awt.Color(230, 244, 255));
        }
        
        // 更新selectedProduct以保持向后兼容（选择最后一个）
        if (!selectedProducts.isEmpty()) {
            selectedProduct = selectedProducts.get(selectedProducts.size() - 1);
        } else {
            selectedProduct = null;
        }
        
        productListPanel.repaint();
        updateSelectionStatus();
        // 同步到网格视图：更新网格的多选高亮
        try {
            if (productGridPanel != null) {
                java.util.List<Integer> idxs = new java.util.ArrayList<>();
                for (Map<String,Object> p : selectedProducts) {
                    for (int i = 0; i < currentProductList.size(); i++) {
                        if (getProductId(currentProductList.get(i)).equals(getProductId(p))) { idxs.add(i); break; }
                    }
                }
                if (idxs.isEmpty()) productGridPanel.clearSelection(); else productGridPanel.setSelectedIndices(idxs);
            }
        } catch (Exception ignored) {}
    }
    
    private void clearAllSelections() {
        // 卡片视图清除高亮
        for (Component comp : productListPanel.getComponents()) {
            if (comp instanceof JPanel) comp.setBackground(java.awt.Color.WHITE);
        }
        // 表格视图清除选择
        if (isTableView && productTablePanel != null) productTablePanel.clearSelectionRows();
        selectedProducts.clear();
        selectedProduct = null;
        updateSelectionStatus();
        // 同步到网格视图
        try { if (productGridPanel != null) productGridPanel.clearSelection(); } catch (Exception ignored) {}
    }

    private void selectAllProducts() {
        if (isTableView && productTablePanel != null) {
            productTablePanel.selectAllRows();
            syncSelectionFromTable();
            return;
        }
        // 卡片视图全选
        selectedProducts.clear();
        java.util.List<Integer> allIdx = new java.util.ArrayList<>();
        for (int i = 0; i < currentProductList.size(); i++) {
            Map<String,Object> pmap = currentProductList.get(i);
            selectedProducts.add(pmap);
            allIdx.add(i);
        }
        // 卡片列表高亮（线性回退）
        for (Component comp : productListPanel.getComponents()) {
            if (comp instanceof JPanel) {
                comp.setBackground(new java.awt.Color(230, 244, 255));
            }
        }
        if (!selectedProducts.isEmpty()) selectedProduct = selectedProducts.get(selectedProducts.size() - 1);
        updateSelectionStatus();
        // 网格高亮所有项（使用 productGridPanel 的 selectAll 接口）
        try {
            if (productGridPanel != null) {
                productGridPanel.selectAll();
            }
        } catch (Exception ignored) {}
    }
    
    private void updateSelectionStatus() {
        // 更新状态栏或提示信息
        if (selectedProducts.size() > 0) {
            log.debug("已选中 {} 个商品", selectedProducts.size());
            // 可以在这里添加状态栏显示选中数量
            // 例如：statusLabel.setText("已选中 " + selectedProducts.size() + " 个商品");
        }
    }
    private String getSelectedProductId() {
        if (selectedProduct == null) return ""; Object id = null; String[] keys = new String[]{"productId","product_Id","product_id","productid","id"};
        for (String k : keys) if (selectedProduct.containsKey(k)) { id = selectedProduct.get(k); if (id!=null) break; }
        return asPlainString(id);
    }
    private Integer getSelectedProductIdInt() {
        if (selectedProduct == null) return null; Object id = null; String[] keys = new String[]{"productId","product_Id","product_id","productid","id"};
        for (String k : keys) if (selectedProduct.containsKey(k)) { id = selectedProduct.get(k); if (id!=null) break; }
        if (id == null) return null; if (id instanceof Number) return ((Number)id).intValue(); try { return Integer.valueOf(id.toString()); } catch (Exception e) { return null; }
    }
    private String getCategoryIcon(String category) {
        if (category == null) return UIUtils.getEmojiOrFallback("📦", "[商品]");
        switch (category.toUpperCase()) {
            case "ELEC": return UIUtils.getEmojiOrFallback("🔌", "[电子]");
            case "BOOK": return UIUtils.getEmojiOrFallback("📚", "[书籍]");
            case "FOOD": return UIUtils.getEmojiOrFallback("🍎", "[食品]");
            case "LIFE": return UIUtils.getEmojiOrFallback("🏠", "[生活]");
            case "SPORT": return UIUtils.getEmojiOrFallback("⚽", "[运动]");
            case "STAT": return UIUtils.getEmojiOrFallback("✏️", "[文具]");
            case "DAILY": return UIUtils.getEmojiOrFallback("🛍️", "[日用]");
            default: return UIUtils.getEmojiOrFallback("📦", "[商品]");
        }
    }
    private String getStatusText(String status) {
        if (status == null) return "未知";
        switch (status.toUpperCase()) { case "ON_SHELF": return "在售"; case "OFF_SHELF": return "下架"; case "SOLD_OUT": return "售罄"; default: return status; }
    }
    // ==== 原有通用与导入/解析辅助方法（恢复） ====
    private void runBg(SupplierWithEx<Request> reqSupplier, java.util.function.Consumer<Response> onSuccess, java.util.function.Consumer<String> onError) {
        SwingUtilities.invokeLater(() -> new Thread(() -> {
            try {
                Request req = reqSupplier.get();
                Response resp = nettyClient.sendRequest(req).get(20, java.util.concurrent.TimeUnit.SECONDS);
                if (resp != null && "SUCCESS".equals(resp.getStatus())) onSuccess.accept(resp); else onError.accept(resp != null ? resp.getMessage() : "服务器无响应");
            } catch (Exception ex) { onError.accept(ex.getMessage()); }
        }).start());
    }
    @FunctionalInterface private interface SupplierWithEx<T> { T get() throws Exception; }
    private String s(Object a, Object b) { Object v = a != null ? a : b; return v == null ? "" : v.toString(); }
    private String asPlainString(Object obj) {
        if (obj == null) return ""; if (obj instanceof Number) { try { java.math.BigDecimal bd = new java.math.BigDecimal(obj.toString()).stripTrailingZeros(); return bd.toPlainString(); } catch (Exception e) { return obj.toString(); } } return obj.toString(); }
    private int parseInt(String s) { if (s==null) return 0; String t=s.trim(); if (t.isEmpty()) return 0; try { return Integer.parseInt(t);} catch(Exception e){ try { java.math.BigDecimal bd=new java.math.BigDecimal(t); bd=bd.stripTrailingZeros(); if (bd.scale()<=0) return bd.intValueExact(); } catch(Exception ignored) {} return 0; } }
    private String formatAmount(Object obj) { if (obj==null) return "0.00"; try { java.math.BigDecimal bd=obj instanceof java.math.BigDecimal?(java.math.BigDecimal)obj:new java.math.BigDecimal(obj.toString()); bd=bd.setScale(2, java.math.RoundingMode.HALF_UP); return bd.toPlainString(); } catch(Exception e){ return obj.toString(); } }
    private Number parseNumber(String s) { try { return new java.math.BigDecimal(s); } catch (Exception e) { return 0; } }
    private void info(String msg){ JOptionPane.showMessageDialog(this,msg,"提示",JOptionPane.INFORMATION_MESSAGE);} private void warn(String msg){ JOptionPane.showMessageDialog(this,msg,"提示",JOptionPane.WARNING_MESSAGE);} private void showError(String msg){ JOptionPane.showMessageDialog(this,msg,"错误",JOptionPane.ERROR_MESSAGE);}

    // === 导入实现（恢复原逻辑） ===
    private void importProducts() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择商品数据文件（Excel/CSV/JSON）");
        
        // 设置文件过滤器支持Excel文件
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".xlsx") || name.endsWith(".xls") || 
                       name.endsWith(".csv") || name.endsWith(".json");
            }
            
            @Override
            public String getDescription() {
                return "支持的文件格式 (*.xlsx, *.xls, *.csv, *.json)";
            }
        });
        
        int ret = chooser.showOpenDialog(this);
        if (ret != JFileChooser.APPROVE_OPTION) return;
        java.io.File file = chooser.getSelectedFile();
        if (file == null || !file.exists()) { showError("文件不存在"); return; }

        SwingUtilities.invokeLater(() -> new Thread(() -> {
            int ok = 0, fail = 0;
            java.util.List<java.util.Map<String,Object>> items = new java.util.ArrayList<>();
            try {
                String nameLower = file.getName().toLowerCase();
                if (nameLower.endsWith(".xlsx") || nameLower.endsWith(".xls")) {
                    items.addAll(parseExcelProducts(file));
                } else if (nameLower.endsWith(".csv")) {
                    items.addAll(parseCsvProducts(file));
                } else if (nameLower.endsWith(".json")) {
                    items.addAll(parseJsonProducts(file));
                } else {
                    items.addAll(parseCsvProducts(file)); // 默认按 CSV 解析
                }
            } catch (Exception ex) {
                log.error("解析导入文件失败", ex);
                showError("解析失败：" + ex.getMessage());
                return;
            }

            // 预取已存在的分类代码集合
            java.util.Set<String> categories = fetchCategoryCodes();
            if (categories == null) categories = new java.util.HashSet<>();

            for (java.util.Map<String,Object> p : items) {
                try {
                    // 确保分类存在
                    Object cat = p.get("category");
                    if (cat != null) {
                        String code = cat.toString().trim();
                        if (!code.isEmpty() && !categories.contains(code)) {
                            if (createCategory(code, code, 0)) {
                                categories.add(code);
                            } else {
                                log.warn("创建分类失败，跳过商品：{}", code);
                                fail++;
                                continue;
                            }
                        }
                    }
                    // 兜底：默认上架
                    Object st = p.get("status");
                    if (st == null || st.toString().isBlank()) p.put("status", "ON_SHELF");
                    String json = JsonUtils.toJson(p);
                    Request req = new Request(managerUri()).addParam("action","PRODUCT_CREATE").addParam("product", json);
                    Response resp = nettyClient.sendRequest(req).get(20, java.util.concurrent.TimeUnit.SECONDS);
                    if (resp != null && "SUCCESS".equals(resp.getStatus())) ok++; else fail++;
                } catch (Exception e) { fail++; log.warn("单条导入失败: {}", e.getMessage()); }
            }

            int fOk = ok, fFail = fail, total = items.size();
            SwingUtilities.invokeLater(() -> {
                info(String.format("导入完成：成功 %d 条，失败 %d 条（共 %d 条）", fOk, fFail, total));
                loadAllProducts();
            });
        }).start());
    }

    // 拉取现有分类代码
    private java.util.Set<String> fetchCategoryCodes() {
        try {
            Request req = new Request(managerUri()).addParam("action", "CATEGORY_LIST");
            Response resp = nettyClient.sendRequest(req).get(20, java.util.concurrent.TimeUnit.SECONDS);
            java.util.Set<String> set = new java.util.HashSet<>();
            if (resp != null && "SUCCESS".equals(resp.getStatus())) {
                @SuppressWarnings("unchecked") java.util.List<java.util.Map<String,Object>> list = (java.util.List<java.util.Map<String,Object>>) resp.getData();
                if (list != null) for (java.util.Map<String,Object> r : list) {
                    Object code = r.getOrDefault("categoryCode", r.get("category_code"));
                    if (code != null) set.add(code.toString());
                }
            }
            return set;
        } catch (Exception e) {
            log.warn("获取分类列表失败: {}", e.getMessage());
            return null;
        }
    }

    // 创建分类
    private boolean createCategory(String code, String name, int sort) {
        try {
            java.util.Map<String,Object> cat = new java.util.HashMap<>();
            cat.put("categoryCode", code);
            cat.put("categoryName", name);
            cat.put("sortOrder", sort);
            Response resp = nettyClient.sendRequest(new Request(managerUri()).addParam("action","CATEGORY_CREATE").addParam("category", JsonUtils.toJson(cat))).get(20, java.util.concurrent.TimeUnit.SECONDS);
            return resp != null && "SUCCESS".equals(resp.getStatus());
        } catch (Exception e) {
            log.warn("创建分类异常: {}", e.getMessage());
            return false;
        }
    }

    // 解析CSV
    private java.util.List<java.util.Map<String,Object>> parseCsvProducts(java.io.File file) throws Exception {
        java.util.List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
        java.util.List<java.util.Map<String,Object>> list = new java.util.ArrayList<>();
        if (lines.isEmpty()) return list;
        String[] headers;
        int idx = 0;
        while (idx < lines.size() && lines.get(idx).trim().isEmpty()) idx++;
        if (idx >= lines.size()) return list;
        String first = lines.get(idx).trim();
        headers = splitCsv(first);
        boolean hasHeader = containsAny(headers, "productCode","productName","price","stock","category","status","description");
        if (!hasHeader) {
            headers = new String[]{"productCode","productName","price","stock","category","status","description"};
            idx--; // 第一行就是数据
        }
        for (idx = idx + 1; idx < lines.size(); idx++) {
            String line = lines.get(idx).trim();
            if (line.isEmpty()) continue;
            String[] vals = splitCsv(line);
            java.util.Map<String,Object> m = new java.util.HashMap<>();
            for (int i = 0; i < Math.min(headers.length, vals.length); i++) {
                String k = headers[i].trim();
                String v = unquote(vals[i].trim());
                switch (k) {
                    case "productCode": m.put("productCode", v); break;
                    case "productName": m.put("productName", v); break;
                    case "price": m.put("price", parseNumber(v)); break;
                    case "stock": m.put("stock", parseInt(v)); break;
                    case "category": m.put("category", v); break;
                    case "status": m.put("status", v); break;
                    case "description": m.put("description", v); break;
                    default: // ignore
                }
            }
            if (m.containsKey("productCode") && m.containsKey("productName")) list.add(m);
        }
        return list;
    }

    // 解析JSON
    private java.util.List<java.util.Map<String,Object>> parseJsonProducts(java.io.File file) throws Exception {
        String json = java.nio.file.Files.readString(file.toPath());
        java.util.List<java.util.Map<String,Object>> list = new java.util.ArrayList<>();
        Object obj = JsonUtils.fromJson(json, Object.class);
        if (obj instanceof java.util.List) {
            for (Object it : (java.util.List<?>) obj) if (it instanceof java.util.Map) list.add(canonProductMap((java.util.Map<?,?>) it));
        } else if (obj instanceof java.util.Map) {
            list.add(canonProductMap((java.util.Map<?,?>) obj));
        } else {
            for (String line : json.split("\r?\n")) {
                String t = line.trim(); if (t.isEmpty()) continue;
                Object one = JsonUtils.fromJson(t, Object.class);
                if (one instanceof java.util.Map) list.add(canonProductMap((java.util.Map<?,?>) one));
            }
        }
        return list;
    }

    private java.util.Map<String,Object> canonProductMap(java.util.Map<?,?> src) {
        java.util.Map<String,Object> m = new java.util.HashMap<>();
        putIfPresent(m, src, "productCode"); putIfPresent(m, src, "Product_code", "productCode");
        putIfPresent(m, src, "productName"); putIfPresent(m, src, "Productname", "productName");
        putIfPresent(m, src, "price");      putIfPresent(m, src, "Price", "price");
        putIfPresent(m, src, "stock");      putIfPresent(m, src, "Stock", "stock");
        putIfPresent(m, src, "category");   putIfPresent(m, src, "Product_category", "category");
        putIfPresent(m, src, "status");     putIfPresent(m, src, "Product_status", "status");
        putIfPresent(m, src, "description"); putIfPresent(m, src, "Product_description", "description");
        return m;
    }
    private void putIfPresent(java.util.Map<String,Object> dst, java.util.Map<?,?> src, String key) { if (src.containsKey(key)) dst.put(key, src.get(key)); }
    private void putIfPresent(java.util.Map<String,Object> dst, java.util.Map<?,?> src, String from, String to) { if (src.containsKey(from)) dst.put(to, src.get(from)); }

    // CSV 简易分割（支持双引号包裹）
    private String[] splitCsv(String line) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') { inQuote = !inQuote; continue; }
            if (c == ',' && !inQuote) { parts.add(sb.toString()); sb.setLength(0); }
            else sb.append(c);
        }
        parts.add(sb.toString());
        return parts.toArray(new String[0]);
    }
    private String unquote(String s) { if (s == null) return null; if (s.startsWith("\"") && s.endsWith("\"")) return s.substring(1, s.length()-1); return s; }
    private boolean containsAny(String[] arr, String... keys) { java.util.Set<String> set = new java.util.HashSet<>(); for (String a: arr) set.add(a.trim()); for (String k: keys) if (set.contains(k)) return true; return false; }

    // === Excel文件解析方法 ===
    private java.util.List<java.util.Map<String,Object>> parseExcelProducts(java.io.File file) throws Exception {
        java.util.List<java.util.Map<String,Object>> list = new java.util.ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook;
            String fileName = file.getName().toLowerCase();
            
            // 根据文件扩展名选择合适的工作簿类型
            if (fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (fileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook(fis);
            } else {
                throw new IllegalArgumentException("不支持的Excel文件格式");
            }
            
            try {
                Sheet sheet = workbook.getSheetAt(0); // 读取第一个工作表
                if (sheet == null) {
                    log.warn("Excel文件中没有找到工作表");
                    return list;
                }
                
                // 获取表头行
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    log.warn("Excel文件第一行为空");
                    return list;
                }
                
                // 解析表头，建立列索引映射
                java.util.Map<String, Integer> columnMap = new java.util.HashMap<>();
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell cell = headerRow.getCell(i);
                    if (cell != null) {
                        String headerName = getCellValueAsString(cell).trim();
                        if (!headerName.isEmpty()) {
                            // 支持中英文列名映射
                            String mappedKey = mapColumnName(headerName);
                            if (mappedKey != null) {
                                columnMap.put(mappedKey, i);
                            }
                        }
                    }
                }
                
                log.info("Excel列映射: {}", columnMap);
                
                // 检查必要的列是否存在
                if (!columnMap.containsKey("productCode") && !columnMap.containsKey("productName")) {
                    throw new IllegalArgumentException("Excel文件必须包含'编号'或'名称'列");
                }
                
                // 解析数据行
                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;
                    
                    java.util.Map<String,Object> product = new java.util.HashMap<>();
                    boolean hasData = false;
                    
                    // 解析每一列的数据
                    for (java.util.Map.Entry<String, Integer> entry : columnMap.entrySet()) {
                        String key = entry.getKey();
                        int colIndex = entry.getValue();
                        Cell cell = row.getCell(colIndex);
                        
                        if (cell != null) {
                            String value = getCellValueAsString(cell).trim();
                            if (!value.isEmpty()) {
                                hasData = true;
                                
                                // 根据列类型进行数据转换
                                switch (key) {
                                    case "productCode":
                                        product.put("productCode", value);
                                        break;
                                    case "productName":
                                        product.put("productName", value);
                                        break;
                                    case "price":
                                        try {
                                            double price = Double.parseDouble(value);
                                            product.put("price", price);
                                        } catch (NumberFormatException e) {
                                            log.warn("价格格式错误，行{}: {}", rowIndex + 1, value);
                                        }
                                        break;
                                    case "stock":
                                        try {
                                            int stock = Integer.parseInt(value);
                                            product.put("stock", stock);
                                        } catch (NumberFormatException e) {
                                            log.warn("库存格式错误，行{}: {}", rowIndex + 1, value);
                                        }
                                        break;
                                    case "category":
                                        product.put("category", value);
                                        break;
                                    case "status":
                                        product.put("status", value);
                                        break;
                                    case "description":
                                        product.put("description", value);
                                        break;
                                    case "listingTime":
                                        product.put("listingTime", value);
                                        break;
                                }
                            }
                        }
                    }
                    
                    // 如果行有数据且包含必要字段，则添加到列表
                    if (hasData && (product.containsKey("productCode") || product.containsKey("productName"))) {
                        // 设置默认值
                        if (!product.containsKey("status")) {
                            product.put("status", "ON_SHELF");
                        }
                        if (!product.containsKey("stock")) {
                            product.put("stock", 0);
                        }
                        if (!product.containsKey("price")) {
                            product.put("price", 0.0);
                        }
                        
                        list.add(product);
                    }
                }
                
            } finally {
                workbook.close();
            }
        }
        
        log.info("Excel文件解析完成，共解析到{}条商品数据", list.size());
        return list;
    }
    
    /**
     * 获取单元格值作为字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 处理日期格式
                    java.util.Date date = cell.getDateCellValue();
                    return new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm").format(date);
                } else {
                    // 处理数字格式
                    double numericValue = cell.getNumericCellValue();
                    // 如果是整数，不显示小数点
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return getCellValueAsString(cell.getCachedFormulaResultType(), cell);
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            default:
                return "";
        }
    }
    
    /**
     * 处理公式单元格的值
     */
    private String getCellValueAsString(CellType cellType, Cell cell) {
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    return String.valueOf((long) numericValue);
                } else {
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
    
    /**
     * 映射Excel列名到系统字段名
     */
    private String mapColumnName(String headerName) {
        if (headerName == null) return null;
        
        String name = headerName.trim().toLowerCase();
        
        // 编号列映射
        if (name.equals("编号") || name.equals("id") || name.equals("productcode") || name.equals("product_code")) {
            return "productCode";
        }
        // SKU列映射
        if (name.equals("sku")) {
            return "productCode"; // SKU作为产品代码
        }
        // 名称列映射
        if (name.equals("名称") || name.equals("name") || name.equals("productname") || name.equals("product_name")) {
            return "productName";
        }
        // 价格列映射
        if (name.equals("价格") || name.equals("price")) {
            return "price";
        }
        // 库存列映射
        if (name.equals("库存") || name.equals("inventory") || name.equals("stock")) {
            return "stock";
        }
        // 状态列映射
        if (name.equals("状态") || name.equals("status")) {
            return "status";
        }
        // 描述列映射
        if (name.equals("描述") || name.equals("description") || name.equals("desc")) {
            return "description";
        }
        // 分类列映射
        if (name.equals("分类") || name.equals("category")) {
            return "category";
        }
        // 上架时间列映射
        if (name.equals("上架时间") || name.equals("listing time") || name.equals("listingtime") || name.equals("listing_time")) {
            return "listingTime";
        }
        
        return null; // 不认识的列名
    }
}

package com.vcampus.client.core.ui.shop;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.text.DecimalFormat;
import com.vcampus.client.core.ui.UIUtils;

/**
 * 网格卡片式商品展示面板，显示商品名称、价格和 Emoji，支持选择与点击事件。
 */
public class ProductGridPanel extends JPanel {
    private final JPanel grid;
    // 缓存按分类/emoji 渲染的图标，避免每次重建时重复渲染造成卡顿
    private final java.util.concurrent.ConcurrentHashMap<String, javax.swing.ImageIcon> iconCache = new java.util.concurrent.ConcurrentHashMap<>();
    private List<Map<String, Object>> products = new ArrayList<>();
    private final List<ActionListener> listeners = new ArrayList<>();
    private final List<ActionListener> cartListeners = new ArrayList<>();
    private final List<ActionListener> adminListeners = new ArrayList<>();
    private int selectedIndex = -1;
    // 支持多选索引集合，用于管理员端的全选/多选高亮
    private final java.util.Set<Integer> selectedIndices = new java.util.HashSet<>();
    private boolean adminMode = false;
    private int forcedColumns = 0; // 0 表示自适应，多于 0 表示强制列数
    // 右侧可能有浮动面板（购物车/订单）遮挡，记录其宽度以从可用宽度中扣除
    private int rightOverlayWidth = 0;
    // 新增：记录进入侧栏前的列设置
    private int originalForcedColumns = 0;
    private boolean sidebarSingleColumnActive = false;

    // 按钮颜色常量
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);       // 绿色 - 加购物车
    private static final Color WARNING_COLOR = new Color(251, 146, 60);      // 橙色 - 补货提醒
    private static final Color SECONDARY_COLOR = new Color(107, 114, 128);   // 灰色 - 更多操作
    private static final Font BUTTON_FONT = new Font("微软雅黑", Font.BOLD, 11);
    private static final Color SELECTED_BG = new Color(230, 244, 255);

    /**
     * 创建现代化商品卡片按钮
     */
    private JButton createModernCardButton(String text, Color bgColor, boolean hasIcon) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isVisible()) return;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color currentColor;
                if (getModel().isPressed()) {
                    currentColor = bgColor.darker();
                } else if (getModel().isRollover()) {
                    currentColor = new Color(
                        Math.min(255, bgColor.getRed() + 20),
                        Math.min(255, bgColor.getGreen() + 20),
                        Math.min(255, bgColor.getBlue() + 20)
                    );
                } else {
                    currentColor = bgColor;
                }

                g2.setColor(currentColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                // 如果有图标，不绘制文字（图标已设置）
                if (!hasIcon && getText() != null && !getText().isEmpty()) {
                    g2.setColor(Color.WHITE);
                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(getText())) / 2;
                    int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(getText(), x, y);
                }
                g2.dispose();
            }
        };
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(BUTTON_FONT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(hasIcon ? 28 : 60, 24));
        return button;
    }

    private JScrollPane scrollPane; // 保存构造时创建的 JScrollPane 引用，确保读取 viewport 宽度准确

    public ProductGridPanel() {
        setLayout(new BorderLayout());
        grid = new JPanel();
        // 启用双缓冲，减少重绘闪烁
        grid.setDoubleBuffered(true);
        grid.setLayout(new WrapLayout(FlowLayout.LEFT, 12, 12));
        grid.setBorder(new EmptyBorder(8,8,8,8));
        JScrollPane scroll = new JScrollPane(grid);
        this.scrollPane = scroll; // 保存引用
        scroll.setBorder(BorderFactory.createEmptyBorder());
        // 只允许垂直滚动，禁止水平滚动（用户需求：侧栏出现后压缩，向下滚动查看，不出现横向滚动）
        // 改为按需显示水平滚动条，允许底部出现滚动条以便左右滚动查看（用户要求）
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        // 提升滚动性能：使用 BLIT_SCROLL_MODE（减小重绘区域），并扩大滚动步进
        try {
            scroll.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        } catch (Exception ignored) {}
        try {
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            scroll.getHorizontalScrollBar().setUnitIncrement(16);
        } catch (Exception ignored) {}
        add(scroll, BorderLayout.CENTER);

        // 监听 viewport 尺寸变化，自适应重新计算卡片宽度与列数
        scroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(ProductGridPanel.this::adjustCardSizes);
            }
        });
        // 自身尺寸变化（例如所在 JSplitPane 左侧宽度变化）时也触发
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(ProductGridPanel.this::adjustCardSizes);
            }
        });

        // 监听祖先尺寸变化（例如 JSplitPane 拖动或窗口调整），确保在更深层次的尺寸变化时也重新布局
        addHierarchyBoundsListener(new HierarchyBoundsListener() {
            @Override
            public void ancestorMoved(HierarchyEvent e) { /* ignore */ }
            @Override
            public void ancestorResized(HierarchyEvent e) {
                SwingUtilities.invokeLater(ProductGridPanel.this::adjustCardSizes);
            }
        });
    }

    public void setProducts(List<Map<String, Object>> products) {
        this.products = products != null ? products : new ArrayList<>();
        rebuildGrid();
    }

    // 新增：强制列数（0=自适应）
    public void setForcedColumns(int cols) {
        if (cols < 1) cols = 0;
        if (this.forcedColumns != cols) {
            this.forcedColumns = cols;
            adjustCardSizes();
        }
    }

    // 供外部（ShopPanel）调用：根据侧栏模式切换列数（之前强制2列导致行数不变，这里改为纯自适应）。
    public void applySidebarMode(boolean sidebarVisible) {
        // 立即触发重新布局，不使用延迟，确保侧栏状态改变时立即调整
        SwingUtilities.invokeLater(() -> {
            // 强制刷新父容器尺寸信息
            Container parent = getParent();
            while (parent != null) {
                if (parent instanceof JComponent) {
                    ((JComponent) parent).revalidate();
                }
                parent = parent.getParent();
            }

            // 强制重新计算viewport尺寸
            JScrollPane scrollPane = findScrollPane();
            if (scrollPane != null) {
                scrollPane.getViewport().revalidate();
            }

            // 立即调整卡片尺寸
            adjustCardSizes();

            // 强制重新布局和重绘
            revalidate();
            repaint();

            // 再次延迟调整以确保JSplitPane完成布局
            Timer timer = new Timer(50, e -> {
                adjustCardSizes();
                revalidate();
                repaint();
            });
            timer.setRepeats(false);
            timer.start();
        });
    }

    // 新增：带遮挡宽度参数的侧栏模式应用方法（保持兼容老方法）
    public void applySidebarMode(boolean sidebarVisible, int overlayWidth) {
        if (!sidebarVisible) overlayWidth = 0;
        // 仅设置遮挡宽度，取消在侧栏显示时强制单列的行为，保持自适应布局
        setRightOverlayWidth(overlayWidth);
        applySidebarMode(sidebarVisible);
    }

    // 新增：设置右侧遮挡宽度（例如购物车/订单侧栏），并触发重新布局
    public void setRightOverlayWidth(int width) {
        if (width < 0) width = 0;
        if (this.rightOverlayWidth != width) {
            this.rightOverlayWidth = width;
            SwingUtilities.invokeLater(this::adjustCardSizes);
        }
    }

    // 新增：根据页码设置当前显示的商品（兼容 ShopPanel.setProductsForPage 调用）
    @SuppressWarnings("unchecked")
    public void setProductsForPage(java.util.List<?> allProducts, int page, int pageSize) {
        try {
            java.util.List<Map<String,Object>> normalized = new java.util.ArrayList<>();
            if (allProducts != null) {
                for (Object o : allProducts) {
                    if (o instanceof Map) {
                        try { normalized.add((Map<String,Object>) o); } catch (Exception ignored) {}
                    }
                }
            }
            if (pageSize <= 0) pageSize = 10;
            int total = normalized.size();
            int start = Math.max(0, (page - 1) * pageSize);
            int end = Math.min(total, start + pageSize);
            java.util.List<Map<String, Object>> sub = new java.util.ArrayList<>();
            if (start < end) sub = new java.util.ArrayList<>(normalized.subList(start, end));
            setProducts(sub);
        } catch (Exception ignored) { setProducts(new java.util.ArrayList<>()); }
    }


    // 更宽松的兼容重载：接受任意对象作为第一个参数，避免编译器/调用处类型推断问题
    @SuppressWarnings("unchecked")
    public void setProductsForPage(Object allProducts, int page, int pageSize) {
        try {
            if (allProducts instanceof java.util.List) {
                setProductsForPage((java.util.List<Map<String,Object>>) allProducts, page, pageSize);
            } else {
                // 无法识别，清空列表
                setProductsForPage(new java.util.ArrayList<Map<String,Object>>(), page, pageSize);
            }
        } catch (Exception ignored) { setProductsForPage(new java.util.ArrayList<Map<String,Object>>(), page, pageSize); }
    }

    // 新增：用于 ShopPanel.updateGridOverlayWidth 的兼容方法
    public void updateOverlayWidth(int width) {
        try { setRightOverlayWidth(width); }
        catch (Exception ignored) {}
    }

    // 兼容：Integer 参数重载
    public void updateOverlayWidth(Integer width) { if (width == null) updateOverlayWidth(0); else updateOverlayWidth(width.intValue()); }

    // 辅助方法：查找包含此面板的JScrollPane
    private JScrollPane findScrollPane() {
        Container parent = getParent();
        while (parent != null) {
            if (parent instanceof JScrollPane) {
                return (JScrollPane) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    public void addProductClickListener(ActionListener l) { listeners.add(l); }
    public void addAddToCartListener(ActionListener l) { cartListeners.add(l); }
    public void addAdminActionListener(ActionListener l) { adminListeners.add(l); }
    public void setAdminMode(boolean admin) { this.adminMode = admin; }

    public int getSelectedIndex() { return selectedIndex; }
    public Map<String, Object> getSelectedProduct() { if (selectedIndex >=0 && selectedIndex < products.size()) return products.get(selectedIndex); return null; }

    // 返回当前多选的索引集合（不可变视图）
    public java.util.Set<Integer> getSelectedIndices() { return java.util.Collections.unmodifiableSet(selectedIndices); }

    // 外部设置多选索引（用于全选/多选同步）
    public void setSelectedIndices(java.util.Collection<Integer> idxs) {
        selectedIndices.clear();
        if (idxs != null) {
            for (Integer i : idxs) {
                if (i != null && i >= 0 && i < products.size()) selectedIndices.add(i);
            }
        }
        // 如果只有一个元素，也同步 selectedIndex
        if (selectedIndices.size() == 1) {
            selectedIndex = selectedIndices.iterator().next();
        } else if (selectedIndices.isEmpty()) {
            selectedIndex = -1;
        }
        highlightSelected();
    }

    // 兼容数组参数
    public void setSelectedIndices(int[] idxs) {
        java.util.List<Integer> list = new java.util.ArrayList<>();
        if (idxs != null) for (int i : idxs) list.add(i);
        setSelectedIndices(list);
    }


    private void rebuildGrid() {
        grid.removeAll();
        selectedIndex = -1;
        selectedIndices.clear();
        for (int i = 0; i < products.size(); i++) {
            Map<String,Object> p = products.get(i);
            JPanel card = createCard(p, i);
            grid.add(card);
        }
        revalidate();
        repaint();
        // 重建后再调整一次尺寸以适应当前容器宽度
        SwingUtilities.invokeLater(this::adjustCardSizes);
    }

    // 计算卡片宽度（仅在 forcedColumns>0 时使用固定列数逻辑）
    private int computeCardWidth() {
        if (forcedColumns <= 0) return 220; // 自适应模式里不直接用这个值
        int vpWidth = 0;
        Container parent = grid.getParent();
        if (parent instanceof JViewport) {
            vpWidth = parent.getWidth();
        } else {
            vpWidth = grid.getWidth();
        }
        if (vpWidth <= 0) return 220; // 尚未布局时返回默认
        Insets insets = grid.getInsets();
        int hgap = 0;
        if (grid.getLayout() instanceof FlowLayout) {
            hgap = ((FlowLayout)grid.getLayout()).getHgap();
        }
        int available = vpWidth - insets.left - insets.right - (forcedColumns - 1) * hgap - hgap * 2 - this.rightOverlayWidth;
        if (forcedColumns == 1) {
            // 单列模式：占满可用宽度（保留最小与合理最大）
            return Math.max(200, Math.min(available, 1000));
        }
        int w = available / forcedColumns;
        w = Math.max(200, w);
        w = Math.min(480, w);
        return w;
    }

    // 自适应卡片尺寸（forcedColumns == 0）
    // 调试开关：临时默认开启，确认问题后可改回 false
    private boolean debugAdaptive = true; // 调试时打开以打印布局计算信息
    private int lastComputedCols = -1; // 保存最近一次计算的列数
    private int lastComputedCardWidth = -1; // 保存最近一次计算的卡片宽度

    public void setDebugAdaptive(boolean debug) { this.debugAdaptive = debug; }
    public boolean isDebugAdaptive() { return this.debugAdaptive; }
    public int getLastComputedCols() { return this.lastComputedCols; }
    public int getLastComputedCardWidth() { return this.lastComputedCardWidth; }

    private void adjustAdaptive() {
        // 优先使用保存的 scrollPane 的 viewport 宽度，回退到 grid.getWidth()
        int vpWidth = 0;
        if (this.scrollPane != null && this.scrollPane.getViewport() != null) {
            vpWidth = this.scrollPane.getViewport().getWidth();
        }
        if (vpWidth <= 0) {
            Container parent = grid.getParent();
            vpWidth = (parent instanceof JViewport) ? parent.getWidth() : grid.getWidth();
        }
        if (vpWidth <= 0) return;

        Insets insets = grid.getInsets();
        int hgap = (grid.getLayout() instanceof FlowLayout) ? ((FlowLayout)grid.getLayout()).getHgap() : 0;

        int minW = 220; // 提高最小宽度以使卡片更大
        int maxW = 700; // 单列允许更宽
        int targetW = 320; // 目标卡片宽度，用于估算列数
        int sidePadding = hgap * 2;
        // 扣除右侧遮挡宽度，避免卡片布局到被遮挡区域
        int available = vpWidth - insets.left - insets.right - sidePadding - this.rightOverlayWidth;
        if (available < minW) available = minW;

        int maxCols = Math.min(8, Math.max(1, available / Math.max(1, minW)));

        // 估算列数：根据目标宽度决定，优先选能接近 targetW 的列数
        int cols = Math.max(1, Math.min(maxCols, (available + hgap) / (targetW + hgap)));
        int cardW = (available - (cols - 1) * hgap) / cols;

        // 如果计算出的 cardW 过窄，则减少列数；如果过宽且还能增加列数，则增加列数
        while (cardW < minW && cols > 1) {
            cols--;
            cardW = (available - (cols - 1) * hgap) / cols;
        }
        while (cardW > targetW && cols < maxCols) {
            int next = cols + 1;
            int nextW = (available - (next - 1) * hgap) / next;
            if (Math.abs(nextW - targetW) < Math.abs(cardW - targetW)) {
                cols = next;
                cardW = nextW;
            } else break;
        }

        // 限制在合理范围
        cardW = Math.max(minW, Math.min(cardW, maxW));

        // 保存计算结果供外部查询
        this.lastComputedCols = cols;
        this.lastComputedCardWidth = cardW;

        if (debugAdaptive) {
            System.out.println("[ProductGridPanel] vpWidth=" + vpWidth + " available=" + available + " hgap=" + hgap + " cols=" + cols + " cardW=" + cardW + " maxCols=" + maxCols);
        }

        for (Component c : grid.getComponents()) {
            if (c instanceof JPanel) {
                Dimension pref = c.getPreferredSize();
                // 如果未设置首选高度，使用更大的默认高度以匹配更大图像（便于展示）
                int height = (pref != null && pref.height > 0) ? pref.height : 300;
                c.setPreferredSize(new Dimension(cardW, height));
            }
        }

        // 计算并设置 grid 的首选尺寸：当内容总宽度大于 viewport 时，水平滚动条会出现
        try {
            int finalCols = (this.lastComputedCols <= 0) ? Math.max(1, (available + hgap) / (targetW + hgap)) : this.lastComputedCols;
            int maxCardH = 0;
            int vgap = (grid.getLayout() instanceof FlowLayout) ? ((FlowLayout)grid.getLayout()).getVgap() : 0;
            for (Component c : grid.getComponents()) {
                Dimension p = c.getPreferredSize();
                if (p != null) maxCardH = Math.max(maxCardH, p.height);
            }
            if (maxCardH <= 0) maxCardH = 300;
            int rows = (int) Math.ceil(Math.max(1, grid.getComponentCount()) / (double) finalCols);
            int totalW = finalCols * cardW + Math.max(0, (finalCols - 1) * hgap) + grid.getInsets().left + grid.getInsets().right + sidePadding;
            int totalH = rows * maxCardH + Math.max(0, (rows - 1) * vgap) + grid.getInsets().top + grid.getInsets().bottom + sidePadding;
            // 如果内容宽度小于 viewport，则至少保证与 viewport 同宽，避免错位
            int prefW = Math.max(available + grid.getInsets().left + grid.getInsets().right, totalW);
            grid.setPreferredSize(new Dimension(prefW, totalH));
        } catch (Exception ignored) {}
    }

    // 调整当前已有卡片尺寸
    private void adjustCardSizes() {
        if (grid.getComponentCount() == 0) return;
        if (forcedColumns <= 0) {
            // 自适应逻辑：根据当前 viewport 宽度自动决定列数
            adjustAdaptive();
        } else {
            int cardWidth = computeCardWidth();
            for (Component c : grid.getComponents()) {
                if (c instanceof JPanel) {
                    Dimension pref = c.getPreferredSize();
                    if (pref == null) pref = new Dimension(cardWidth, 300);
                    c.setPreferredSize(new Dimension(cardWidth, pref.height));
                }
            }
            // 当强制列数时也需要调整 grid 的首选尺寸以支持水平滚动（如果需要）
            try {
                int cols = Math.max(1, forcedColumns);
                int vgap = (grid.getLayout() instanceof FlowLayout) ? ((FlowLayout)grid.getLayout()).getVgap() : 0;
                int maxCardH = 0;
                for (Component c : grid.getComponents()) {
                    Dimension p = c.getPreferredSize();
                    if (p != null) maxCardH = Math.max(maxCardH, p.height);
                }
                if (maxCardH <= 0) maxCardH = 300;
                int rows = (int) Math.ceil(Math.max(1, grid.getComponentCount()) / (double) cols);
                Insets insets = grid.getInsets();
                int hgap = (grid.getLayout() instanceof FlowLayout) ? ((FlowLayout)grid.getLayout()).getHgap() : 0;
                int totalW = cols * cardWidth + Math.max(0, (cols - 1) * hgap) + insets.left + insets.right + hgap * 2;
                int totalH = rows * maxCardH + Math.max(0, (rows - 1) * vgap) + insets.top + insets.bottom + hgap * 2;
                int vpW = this.scrollPane != null && this.scrollPane.getViewport() != null ? this.scrollPane.getViewport().getWidth() : grid.getWidth();
                int prefW = Math.max(vpW, totalW);
                grid.setPreferredSize(new Dimension(prefW, totalH));
            } catch (Exception ignored) {}
        }
        grid.revalidate();
        grid.repaint();
    }

    private JPanel createCard(Map<String, Object> p, int index) {
        JPanel card = new JPanel(new BorderLayout());
        // 确保卡片为不透明以便背景色可见
        card.setOpaque(true);
        // 设置更大的首选尺寸，宽度会被自适应覆盖，但高度保留用于大图展示
        card.setPreferredSize(new Dimension(260, 300));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220,220,220)), new EmptyBorder(8,8,8,8)));

        // emoji/icon on left
        JLabel lblEmoji = new JLabel();
        // 优先使用 product_code 对应的图片资源：/products/{productCode}.png
        String productCode = String.valueOf(p.getOrDefault("productCode", p.getOrDefault("Product_code", "")));
        String category = String.valueOf(p.getOrDefault("category", p.getOrDefault("Product_category", "其他")));
        javax.swing.ImageIcon imgIcon = null;
        if (productCode != null && !productCode.trim().isEmpty()) {
            // 使用更大的缩略图以便在放大卡片中清晰展示
            String key = "productImg:" + productCode + ":160";
            imgIcon = iconCache.get(key);
            if (imgIcon == null) {
                try {
                    imgIcon = UIUtils.loadIcon("/products/" + productCode + ".png", 160, 160);
                    if (imgIcon != null) iconCache.put(key, imgIcon);
                } catch (Exception ignored) {}
            }
        }
        if (imgIcon != null) {
            lblEmoji.setIcon(imgIcon);
        } else {
            // 回退到按分类查找小图标，再回退到原先的 emoji 文本或渲染
            javax.swing.ImageIcon catIcon = UIUtils.getIconForCategory(category, 160);
            if (catIcon != null) {
                lblEmoji.setIcon(catIcon);
            } else {
                String emoji = emojiForProduct(p);
                String cacheKey = "emoji:" + emoji + ":96";
                javax.swing.ImageIcon emojiIcon = iconCache.get(cacheKey);
                if (emojiIcon == null) {
                    emojiIcon = UIUtils.renderEmojiAsIcon(emoji, 96);
                    if (emojiIcon != null) iconCache.put(cacheKey, emojiIcon);
                }
                if (emojiIcon != null) {
                    lblEmoji.setIcon(emojiIcon);
                } else {
                    lblEmoji.setText(emoji);
                    lblEmoji.setFont(UIUtils.getEmojiCapableFont(Font.PLAIN, 72));
                }
            }
        }
         lblEmoji.setHorizontalAlignment(SwingConstants.CENTER);
         lblEmoji.setPreferredSize(new Dimension(160,160));

         // title + price
         String name = String.valueOf(p.getOrDefault("productName", p.getOrDefault("Productname", "未知")));
         Object priceObj = p.getOrDefault("price", p.get("Price"));
         String price = formatPrice(priceObj);
         // 使用 HTML 指定 font-family 回退，优先使用支持 emoji 的字体，再用中文字体，最后使用 sans-serif
         JLabel lblName = new JLabel();
         String safeName = escapeHtml(name);
         String html = "<html><div style=\"text-align:center; font-family: 'Microsoft YaHei', sans-serif; font-weight: bold;\">" + safeName + "</div></html>";
         lblName.setText(html);
         lblName.setHorizontalAlignment(SwingConstants.CENTER);

         // 价格使用绿色展示并略大
         JLabel lblPrice = new JLabel("¥" + price);
         lblPrice.setForeground(SUCCESS_COLOR);
         lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 15));
         lblPrice.setHorizontalAlignment(SwingConstants.CENTER);

         // 垂直布局：图片 -> 名称 -> 价格
         JPanel center = new JPanel();
         center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
         center.setOpaque(false);
         center.setBorder(new EmptyBorder(8,8,8,8));

         // 保证组件居中
         lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
         lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);
         lblEmoji.setAlignmentX(Component.CENTER_ALIGNMENT);

         center.add(lblEmoji);
         center.add(Box.createRigidArea(new Dimension(0,8)));
         center.add(lblName);
         center.add(Box.createRigidArea(new Dimension(0,6)));
         center.add(lblPrice);

         card.add(center, BorderLayout.CENTER);

        // tooltip with short description
        String desc = String.valueOf(p.getOrDefault("description", p.get("Product_description")));
        if (desc == null) desc = "";
        String tip = desc.length() > 120 ? desc.substring(0, 120) + "..." : desc;
        card.setToolTipText(" " + tip);

        // action panel (hidden by default, shown on hover)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
        actionPanel.setOpaque(false);

        // 使用现代化样式创建加购按钮
        JButton btnAdd = createModernCardButton("", SUCCESS_COLOR, true);
        // 尝试使用 SVG 购物车图标资源（仅使用 SVG）
        javax.swing.ImageIcon cartIcon = UIUtils.loadIcon("/figures/icons/cart.svg", 16, 16);
        if (cartIcon != null) {
            btnAdd.setIcon(cartIcon);
        } else {
            btnAdd.setText(UIUtils.getEmojiOrFallback("🛒", "加购"));
        }
        btnAdd.setToolTipText("加入购物车");

        // 根据库存与状态决定是否可购买：即使库存为0也要展示，但禁止购买并显示售罄
        int stockVal = 0;
        try {
            Object stockObj = p.getOrDefault("stock", p.get("Stock"));
            if (stockObj instanceof Number) stockVal = ((Number)stockObj).intValue();
            else if (stockObj != null) stockVal = Integer.parseInt(stockObj.toString());
        } catch (Exception ignored) {}
        String statusVal = String.valueOf(p.getOrDefault("status", p.get("Product_status")));
        boolean purchasable = (statusVal == null || "null".equals(statusVal)) ? (stockVal > 0) : ("ON_SHELF".equalsIgnoreCase(statusVal) && stockVal > 0);

        JButton btnRestock = null;
        if (!purchasable) {
            // 将加入购物车按钮替换为"提醒补货"按钮（可点击，提示成功）
            btnRestock = createModernCardButton("补货", WARNING_COLOR, false);
            btnRestock.setToolTipText("通知管理员补货");
            btnRestock.addActionListener(ae -> {
                JOptionPane.showMessageDialog(ProductGridPanel.this, "已发送补货提醒，客服会尽快处理。", "提示", JOptionPane.INFORMATION_MESSAGE);
            });
            // 隐藏原有 btnAdd（仍保留但不可见）
            btnAdd.setVisible(false);
        }

        // 更多按钮（保留原有行为）
        JButton btnMore = createModernCardButton("", SECONDARY_COLOR, true);
        javax.swing.ImageIcon moreIcon = UIUtils.loadIcon("/figures/icons/more.svg", 12, 12);
        if (moreIcon != null) {
            btnMore.setIcon(moreIcon);
        } else {
            btnMore.setText(UIUtils.getEmojiOrFallback("⋯", "更多"));
        }
        btnMore.setToolTipText("更多操作");

        actionPanel.add(btnRestock != null ? btnRestock : btnAdd);
        actionPanel.add(btnMore);
        actionPanel.setVisible(false);

        // add to EAST overlay container
        JPanel eastWrapper = new JPanel(new BorderLayout());
        eastWrapper.setOpaque(false);
        eastWrapper.add(actionPanel, BorderLayout.NORTH);
        card.add(eastWrapper, BorderLayout.EAST);

        // popup menu for more
        JPopupMenu popup = new JPopupMenu();
        JMenuItem miDetail = new JMenuItem("查看详情");
        JMenuItem miAdd = new JMenuItem("加入购物车");
        JMenuItem miRestock = new JMenuItem("提醒补货");
        popup.add(miDetail);
        if (purchasable) popup.add(miAdd); else popup.add(miRestock);

        // admin actions
        JMenuItem miEdit = null;
        JMenuItem miDelete = null;
        if (adminMode) {
            miEdit = new JMenuItem("编辑");
            miDelete = new JMenuItem("删除");
            popup.addSeparator();
            popup.add(miEdit);
            popup.add(miDelete);
        }

        // 如果为售罄，提醒补货菜单项显示并提示成功
        if (!purchasable) {
            miRestock.addActionListener(ae -> JOptionPane.showMessageDialog(ProductGridPanel.this, "已发送补货提醒，客服会尽快处理。", "提示", JOptionPane.INFORMATION_MESSAGE));
        }

        // 如果不可购买，增加视觉标识：右上角售罄徽章，置灰标题和价格
        if (!purchasable) {
            JPanel badgePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            badgePanel.setOpaque(false);
            JLabel soldBadge = new JLabel("售罄");
            soldBadge.setOpaque(true);
            soldBadge.setBackground(new Color(255,140,0));
            soldBadge.setForeground(Color.WHITE);
            soldBadge.setFont(soldBadge.getFont().deriveFont(Font.BOLD, 12f));
            soldBadge.setBorder(new EmptyBorder(2, 6, 2, 6));
            badgePanel.add(soldBadge);
            card.add(badgePanel, BorderLayout.NORTH);

            // 置灰标题与价格，强调售罄
            lblName.setForeground(new Color(120,120,120));
            lblPrice.setForeground(new Color(140,140,140));
        }

        // hover effect and pointer
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            Color original = card.getBackground();
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                // 如果已选中则保持选中背景
                if (selectedIndices.contains(index) || index == selectedIndex) return;
                card.setBackground(new Color(245,250,255));
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                // 如果已选中则保持选中背景，否则恢复原始背景
                if (selectedIndices.contains(index) || index == selectedIndex) {
                    try { card.setBackground(SELECTED_BG); } catch (Exception ignored) {}
                } else {
                    try { card.setBackground(original); } catch (Exception ignored) {}
                }
            }
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                // 支持单次点击选中，双击查看详情
                if (e.getClickCount() == 2) {
                    selectedIndex = index;
                    highlightSelected();
                    ActionEvent ev = new ActionEvent(ProductGridPanel.this, ActionEvent.ACTION_PERFORMED, "detail:" + index);
                    for (ActionListener l : listeners) l.actionPerformed(ev);
                } else if (e.getClickCount() == 1) {
                    selectedIndex = index;
                    highlightSelected();
                    ActionEvent ev = new ActionEvent(ProductGridPanel.this, ActionEvent.ACTION_PERFORMED, "select:" + index);
                    for (ActionListener l : listeners) l.actionPerformed(ev);
                }
            }
        });

        // button actions
        btnAdd.addActionListener(ae -> {
            // fire cart listeners with command "add:<index>"
            ActionEvent ev = new ActionEvent(ProductGridPanel.this, ActionEvent.ACTION_PERFORMED, "add:" + index);
            for (ActionListener l : cartListeners) l.actionPerformed(ev);
            // give quick feedback (使用图标优先，否则 emoji/文本)
            // 这里也尝试复用 small icons 缓存，UIUtils.loadIcon 内部可能已有缓存，但我们优先使用 iconCache 减少 I/O
            String okKey = "icon:/figures/icons/ok.svg:14:14";
            javax.swing.ImageIcon okIcon = iconCache.get(okKey);
            if (okIcon == null) {
                okIcon = UIUtils.loadIcon("/figures/icons/ok.svg", 14, 14);
                if (okIcon != null) iconCache.put(okKey, okIcon);
            }
             if (okIcon != null) { btnAdd.setIcon(okIcon); btnAdd.setText(""); }
             else btnAdd.setText(UIUtils.getEmojiOrFallback("✅", "已加"));
             Timer t = new Timer(900, e -> {
                String cartKey = "icon:/figures/icons/cart.svg:16:16";
                javax.swing.ImageIcon ci = iconCache.get(cartKey);
                if (ci == null) {
                    ci = UIUtils.loadIcon("/figures/icons/cart.svg", 16, 16);
                    if (ci != null) iconCache.put(cartKey, ci);
                }
                if (ci != null) { btnAdd.setIcon(ci); btnAdd.setText(""); }
                else btnAdd.setText(UIUtils.getEmojiOrFallback("🛒", "加购"));
             });
             t.setRepeats(false);
             t.start();
         });

        miAdd.addActionListener(ae -> {
            ActionEvent ev = new ActionEvent(ProductGridPanel.this, ActionEvent.ACTION_PERFORMED, "add:" + index);
            for (ActionListener l : cartListeners) l.actionPerformed(ev);
        });
        miDetail.addActionListener(ae -> {
            ActionEvent ev = new ActionEvent(ProductGridPanel.this, ActionEvent.ACTION_PERFORMED, "detail:" + index);
            for (ActionListener l : listeners) l.actionPerformed(ev);
        });
        if (adminMode) {
            miEdit.addActionListener(ae -> {
                ActionEvent ev = new ActionEvent(ProductGridPanel.this, ActionEvent.ACTION_PERFORMED, "edit:" + index);
                for (ActionListener l : adminListeners) l.actionPerformed(ev);
            });
            miDelete.addActionListener(ae -> {
                ActionEvent ev = new ActionEvent(ProductGridPanel.this, ActionEvent.ACTION_PERFORMED, "delete:" + index);
                for (ActionListener l : adminListeners) l.actionPerformed(ev);
            });
        }

        btnMore.addActionListener(ae -> {
            popup.show(btnMore, 0, btnMore.getHeight());
        });

        return card;
    }

    // 辅助：根据商品信息返回一个代表性的 emoji 文本（实例方法）
    private String emojiForProduct(Map<String, Object> p) {
        if (p == null) return "📦";
        Object cat = p.getOrDefault("category", p.get("Product_category"));
        String category = cat == null ? "" : String.valueOf(cat);
        String e = emojiForCategory(category);
        if (e == null || e.isEmpty()) {
            // 尝试从名字中猜测
            String name = String.valueOf(p.getOrDefault("productName", p.getOrDefault("Productname", ""))).toLowerCase();
            if (name.contains("book") || name.contains("书")) return "📚";
            if (name.contains("pen") || name.contains("笔")) return "✏️";
            if (name.contains("card") || name.contains("卡")) return "💳";
            return "📦";
        }
        return e;
    }

    // 公共静态辅助：根据分类返回 emoji，供外部（ProductEditorDialog 等）静态调用
    public static String emojiForCategory(String category) {
        if (category == null) return "📦";
        String c = category.trim().toLowerCase();
        switch (c) {
            case "书籍": case "book": case "books": return "📚";
            case "文具": case "stationery": return "✏️";
            case "食品": case "food": return "🍎";
            case "电子": case "electronics": return "💻";
            case "服饰": case "clothing": return "👕";
            case "其他": case "other": default: return "📦";
        }
    }

    // 格式化价格为保留两位小数的字符串
    private String formatPrice(Object priceObj) {
        try {
            double v;
            if (priceObj == null) v = 0.0;
            else if (priceObj instanceof Number) v = ((Number) priceObj).doubleValue();
            else v = Double.parseDouble(priceObj.toString());
            DecimalFormat df = new DecimalFormat("0.00");
            return df.format(v);
        } catch (Exception ignored) {
            return "0.00";
        }
    }

    // 简单的 HTML 转义，避免标签注入
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private void highlightSelected() {
        Component[] comps = grid.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof JComponent) {
                JComponent c = (JComponent) comps[i];
                boolean sel = selectedIndices.contains(i) || i == selectedIndex;
                if (sel) {
                    c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(100,150,240), 2), new EmptyBorder(6,6,6,6)));
                    try { c.setBackground(SELECTED_BG); c.setOpaque(true); } catch (Exception ignored) {}
                } else {
                    c.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220,220,220)), new EmptyBorder(8,8,8,8)));
                    try { c.setBackground(Color.WHITE); c.setOpaque(true); } catch (Exception ignored) {}
                }
            }
        }
        repaint();
    }

    // 对外 API：在网格中选中指定索引并确保可见（-1 表示取消选中）
    public void selectCardIndex(int idx) {
        if (idx < -1) idx = -1;
        if (idx >= products.size()) idx = -1;
        this.selectedIndex = idx;
        // 将单选也映射到多选集合中（兼容全选/多选）
        selectedIndices.clear();
        if (idx >= 0) selectedIndices.add(idx);
        // 高亮选中项
        highlightSelected();
        // 滚动到可见
        if (idx >= 0) {
            try {
                Component[] comps = grid.getComponents();
                if (idx >= 0 && idx < comps.length) {
                    Component c = comps[idx];
                    if (c != null) {
                        JScrollPane sp = findScrollPane();
                        if (sp != null) {
                            Rectangle r = c.getBounds();
                            JViewport vp = sp.getViewport();
                            if (vp != null) vp.scrollRectToVisible(r);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    // 取消所有选择
    public void clearSelection() {
        selectedIndex = -1;
        selectedIndices.clear();
        highlightSelected();
    }

    // 对外API：全选当前页面/列表中的所有商品并高亮
    public void selectAll() {
        try {
            selectedIndices.clear();
            for (int i = 0; i < products.size(); i++) selectedIndices.add(i);
            // 不修改 selectedIndex（避免 selectCardIndex 清空 selectedIndices），仅使用 selectedIndices 来控制多选高亮
            if (selectedIndices.isEmpty()) selectedIndex = -1;
            // 更新样式
            highlightSelected();
            // 确保第一个选中项可见，但不要改变 selectedIndices
            if (!selectedIndices.isEmpty()) {
                try {
                    int first = selectedIndices.iterator().next();
                    Component[] comps = grid.getComponents();
                    if (first >= 0 && first < comps.length) {
                        Component c = comps[first];
                        JScrollPane sp = findScrollPane();
                        if (sp != null && c != null) {
                            Rectangle r = c.getBounds();
                            JViewport vp = sp.getViewport();
                            if (vp != null) vp.scrollRectToVisible(r);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            } catch (Exception ignored) {}
        }

        // 可选：返回当前页面选中的数量
        public int getSelectedCount() { return selectedIndices.size(); }

        // 可选：从当前产品列表中移除指定索引（用于删除操作），并刷新网格
        public void removeProductAt(int index) {
            if (index < 0 || index >= products.size()) return;
            products.remove(index);
            rebuildGrid();
        }
    }

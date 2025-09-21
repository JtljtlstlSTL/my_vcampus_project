package com.vcampus.client.core.ui.shop;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.function.BiConsumer; // 仍保留以兼容旧构造器签名
import java.util.concurrent.TimeUnit;
import com.vcampus.client.core.ui.UIUtils; // 添加 UIUtils 导入

@Slf4j
public class ProductDetailDialog extends JDialog {

    private final Map<String, Object> product;
    private final NettyClient client;
    private final boolean showBuyControls;
    // private final BiConsumer<Map<String,Object>, Integer> addToCartCallback; // 已废弃

    // 按钮颜色和字体常量
    private static final Font BUTTON_FONT = new Font("微软雅黑", Font.BOLD, 13);
    private static final Font SMALL_BUTTON_FONT = new Font("微软雅黑", Font.BOLD, 11);
    // 属性/信息区域字体
    private static final Font ATTR_FONT = new Font("微软雅黑", Font.PLAIN, 14);
    private static final Font ATTR_FONT_BOLD = new Font("微软雅黑", Font.BOLD, 14);
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);      // 蓝色 - 主要操作
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);       // 绿色 - 成功/购买
    private static final Color WARNING_COLOR = new Color(251, 146, 60);      // 橙色 - 警告/补货
    private static final Color SECONDARY_COLOR = new Color(107, 114, 128);   // 灰色 - 次要操作

    /**
     * 创建现代化按钮
     */
    private JButton createModernButton(String text, Color bgColor, boolean isLarge) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color currentColor;
                if (!isEnabled()) {
                    currentColor = new Color(156, 163, 175);
                } else if (getModel().isPressed()) {
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

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
        button.setFont(isLarge ? BUTTON_FONT : SMALL_BUTTON_FONT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (isLarge) {
            button.setPreferredSize(new Dimension(Math.max(100, text.length() * 14 + 20), 36));
        } else {
            button.setPreferredSize(new Dimension(30, 28));
        }
        return button;
    }

    /**
     * 创建现代化数量输入框
     */
    private JTextField createModernTextField(String initialValue, int columns) {
        JTextField textField = new JTextField(initialValue, columns);
        textField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        textField.setBackground(Color.WHITE);
        textField.setHorizontalAlignment(JTextField.CENTER);
        textField.setPreferredSize(new Dimension(textField.getPreferredSize().width, 28));
        return textField;
    }

    public ProductDetailDialog(Window owner, Map<String, Object> product, NettyClient client) {
        this(owner, product, client, true, null);
    }

    public ProductDetailDialog(Window owner, Map<String, Object> product, NettyClient client, boolean showBuyControls) {
        this(owner, product, client, showBuyControls, null);
    }

    // 兼容旧签名: addToCartCallback 不再使用
    public ProductDetailDialog(Window owner, Map<String, Object> product, NettyClient client, boolean showBuyControls, BiConsumer<Map<String,Object>, Integer> unused) {
        super(owner, "商品详情", ModalityType.APPLICATION_MODAL);
        this.product = product;
        this.client = client;
        this.showBuyControls = showBuyControls;
        initUI();
        // 增大对话框尺寸以便更好展示大图和详情
        setSize(860, 560);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(16,16));
        main.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        // 左侧图片容器：简洁样式（移除阴影与额外边框），直接展示图片
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setOpaque(true);
        imagePanel.setBackground(new Color(247,249,250));
        // 放大图片区域：左侧占比调整为 60%
        imagePanel.setPreferredSize(new Dimension(520, 520));

        // 使用自定义的 RoundedImageLabel 类来绘制带圆角的图片（主图）
        RoundedImageLabel imgLabel = new RoundedImageLabel();
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imgLabel.setVerticalAlignment(SwingConstants.CENTER);
        // 减小内边距并设置较大的首选尺寸，确保图片能够尽可能放大显示
        imgLabel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        imgLabel.setPreferredSize(new Dimension(488, 488));
        // 鼠标悬停提示可放大
        imgLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        imgLabel.setToolTipText("点击查看大图");
        // 点击放大预览
        imgLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                Object o = imgLabel.getClientProperty("origImage");
                if (o instanceof Image) showImagePreview((Image) o);
            }
        });

        // 异步加载图片到 imgLabel（优先从 classpath:/shop/ 等目录查找）
        loadImageAsync(imgLabel);

        // 直接将 imgLabel 添加到 imagePanel，移除底部缩略/额外标签
        imagePanel.add(imgLabel, BorderLayout.CENTER);

        // 右侧信息区域：标题 + 详细属性 + 描述
        JPanel center = new JPanel(new BorderLayout(12,12));

        // 计算商品名称并显示在顶部：支持多种字段名
        String productName = String.valueOf(product.getOrDefault("productName", product.getOrDefault("product_name", "未知商品")));
        // 顶部：商品名、描述卡片、价格（按用户要求的顺序）
        JPanel topInfo = new JPanel();
        topInfo.setLayout(new BoxLayout(topInfo, BoxLayout.Y_AXIS));
        topInfo.setOpaque(false);
        // 将商品名与描述合并为同一行显示，字号一致
        String fullDescription = String.valueOf(product.getOrDefault("description", "无"));
        if (fullDescription == null || "null".equals(fullDescription)) fullDescription = "无";
        String escName = productName.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
        String descEsc = fullDescription.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;").replace("\n", "<br/>");
        String nameFontCss = "font-family:微软雅黑; font-size:18px; color:#0f172a;";
        JLabel nameDesc = new JLabel("<html><div style='" + nameFontCss + " margin:0; padding-top:4px; text-align:center;'>" + escName + "&nbsp;&nbsp;" + descEsc + "</div></html>");
        nameDesc.setHorizontalAlignment(SwingConstants.CENTER);
        nameDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        topInfo.add(nameDesc);
        // 在名称+描述与价格之间增加垂直间距（稍微缩小）
        topInfo.add(Box.createRigidArea(new Dimension(0,8)));

        // 价格：放在描述之下，绿色显示
        String priceStr = formatPrice(product.get("price"));
        JLabel priceLabel = new JLabel("¥" + priceStr);
        priceLabel.setFont(new Font("微软雅黑", Font.BOLD, 24)); // 更显眼（价格保持加粗）
        priceLabel.setForeground(new Color(16,185,129)); // 保持主题绿
        priceLabel.setBorder(BorderFactory.createEmptyBorder(8,0,0,0));
        // 居中显示
        priceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topInfo.add(priceLabel);
        // 在价格与属性区域之间增加垂直间距（稍微缩小）
        topInfo.add(Box.createRigidArea(new Dimension(0,12)));

        // 详情网格（进一步增大行间距并整体居中显示以提高可读性）
        JPanel info = new JPanel(new GridBagLayout());
        info.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        // 增大上下间距并适当增加内填充（略小于先前设置）
        gbc.insets = new Insets(12,8,12,8);
        gbc.ipady = 4; // 每行增加些高度
        gbc.anchor = GridBagConstraints.CENTER; // 改为居中
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;

        // 编码 / 分类 / 状态 / 库存 / 更新时间（使用更大字号以提升可读性），并设置标签居中
        JLabel codeLabel = new JLabel("编码: " + safeGet("productCode")); codeLabel.setForeground(new Color(75,85,99)); codeLabel.setFont(ATTR_FONT); codeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; info.add(codeLabel, gbc);
        JLabel catLabel = new JLabel("分类: " + safeGet("category")); catLabel.setForeground(new Color(75,85,99)); catLabel.setFont(ATTR_FONT); catLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1; gbc.gridwidth = 2; info.add(catLabel, gbc);
        JLabel statusLabel = new JLabel("状态: " + safeGet("status")); statusLabel.setForeground(new Color(75,85,99)); statusLabel.setFont(ATTR_FONT); statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 2; info.add(statusLabel, gbc);
        JLabel stockLabel = new JLabel("库存: " + safeGet("stock")); stockLabel.setForeground(new Color(75,85,99)); stockLabel.setFont(ATTR_FONT); stockLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 3; info.add(stockLabel, gbc);
        JLabel updatedLabel = new JLabel("更新时间: " + safeGet("updatedAt")); updatedLabel.setForeground(new Color(120,125,140)); updatedLabel.setFont(ATTR_FONT); updatedLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 4; info.add(updatedLabel, gbc);

        // 将 info 放入居中的包装面板，确保在较宽的 center 区域也保持水平居中
        JPanel infoWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        infoWrap.setOpaque(false);
        infoWrap.add(info);
        // 在 infoWrap 上方再添加一点外边距以增强分隔感（稍微缩小）
        infoWrap.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        center.add(topInfo, BorderLayout.NORTH);
        center.add(infoWrap, BorderLayout.CENTER);

        // 使用 JSplitPane 将左右两块放入中间区域，确保右侧信息可见并可调整宽度
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imagePanel, center);
        // 左侧占比 60%，右侧 40%
        split.setResizeWeight(0.6);
        split.setDividerLocation(0.6);
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(true);
        main.add(split, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        if (this.showBuyControls) {
            int stockVal = 0;
            try {
                Object so = product.getOrDefault("stock", product.get("Stock"));
                if (so instanceof Number) stockVal = ((Number) so).intValue();
                else if (so != null) stockVal = Integer.parseInt(so.toString());
            } catch (Exception ignored) {}
            String statusVal = String.valueOf(product.getOrDefault("status", product.get("Product_status")));
            boolean purchasable = (statusVal == null || "null".equals(statusVal)) ? (stockVal > 0) : ("ON_SHELF".equalsIgnoreCase(statusVal) && stockVal > 0);
            if (purchasable) {
                JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                JButton minus = createModernButton("-", SECONDARY_COLOR, false);
                JTextField qtyField = createModernTextField("1", 5);
                JButton plus = createModernButton("+", SECONDARY_COLOR, false);
                minus.addActionListener(e -> { try { int v = parseQty(qtyField.getText()); if (v > 1) qtyField.setText(String.valueOf(v - 1)); } catch (Exception ex) { qtyField.setText("1"); } });
                plus.addActionListener(e -> { try { int v = parseQty(qtyField.getText()); qtyField.setText(String.valueOf(v + 1)); } catch (Exception ex) { qtyField.setText("1"); } });
                qtyPanel.add(new JLabel("数量:")); qtyPanel.add(minus); qtyPanel.add(qtyField); qtyPanel.add(plus);

                // 新增加入购物车按钮
                JButton addCartBtn = createModernButton("加入购物车", SUCCESS_COLOR, true);
                addCartBtn.addActionListener(ae -> {
                    try {
                        int qty = parseQty(qtyField.getText());
                        if (qty <= 0) { JOptionPane.showMessageDialog(ProductDetailDialog.this, "请输入有效数量", "错误", JOptionPane.ERROR_MESSAGE); return; }
                        addCartBtn.setEnabled(false);
                        // 在后台线程先检查卡状态再发起加入购物车请求
                        new Thread(() -> {
                            try {
                                if (!ensureCardActive()) {
                                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ProductDetailDialog.this, "当前校园卡处于挂失状态，无法进行购物操作。如需解除挂失请前往校园卡服务。", "卡片不可用", JOptionPane.WARNING_MESSAGE));
                                    return;
                                }
                                Request req = new Request(getUserUri()).addParam("action","ADD_TO_CART");
                                try { req.setSession(client.getCurrentSession()); } catch (Exception ignore) {}
                                req.addParam("productId", asPlainString(product.getOrDefault("productId", product.get("product_Id"))));
                                req.addParam("qty", String.valueOf(qty));
                                Response resp = client.sendRequest(req).get(12, TimeUnit.SECONDS);
                                SwingUtilities.invokeLater(() -> {
                                    if (resp != null && "SUCCESS".equals(resp.getStatus())) {
                                        JOptionPane.showMessageDialog(ProductDetailDialog.this, "已加入购物车", "成功", JOptionPane.INFORMATION_MESSAGE);
                                        // 更新计数
                                        Object data = resp.getData();
                                        if (data instanceof Map) {
                                            Object cc = ((Map<?,?>)data).get("cartCount");
                                            if (cc != null) {
                                                try {
                                                    int c = new java.math.BigDecimal(cc.toString()).intValue();
                                                    ShopPanel sp = findShopPanelInContainer(SwingUtilities.getWindowAncestor(ProductDetailDialog.this));
                                                    if (sp != null) sp.updateCartCount(c);
                                                } catch (Exception ignored) {}
                                            }
                                        }
                                    } else {
                                        JOptionPane.showMessageDialog(ProductDetailDialog.this, resp == null ? "服务器无响应" : resp.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                                    }
                                });
                            } catch (Exception ex) {
                                log.error("加入购物车失败", ex);
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ProductDetailDialog.this, "加入购物车失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE));
                            } finally { SwingUtilities.invokeLater(() -> addCartBtn.setEnabled(true)); }
                        }).start();
                    } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(ProductDetailDialog.this, "数量必须为整数", "错误", JOptionPane.ERROR_MESSAGE); }
                });

                JButton buyBtn = createModernButton("立即购买", PRIMARY_COLOR, true);
                buyBtn.addActionListener(ae -> {
                    try {
                        int qty = parseQty(qtyField.getText());
                        if (qty <= 0) { JOptionPane.showMessageDialog(ProductDetailDialog.this, "请输入有效数量", "错误", JOptionPane.ERROR_MESSAGE); return; }
                        buyBtn.setEnabled(false);
                        // 后台线程先检查卡状态
                        new Thread(() -> {
                            try {
                                if (!ensureCardActive()) {
                                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ProductDetailDialog.this, "当前校园卡处于挂失状态，无法进行购物操作。如需解除挂失请前往校园卡服务。", "卡片不可用", JOptionPane.WARNING_MESSAGE));
                                    return;
                                }
                                Request req = new Request(getUserUri()).addParam("action","BUY");
                                try { req.setSession(client.getCurrentSession()); } catch (Exception ignore) {}
                                req.addParam("productId", asPlainString(product.getOrDefault("productId", product.get("product_Id"))));
                                req.addParam("qty", String.valueOf(qty));
                                Response resp = client.sendRequest(req).get(15, TimeUnit.SECONDS);
                                SwingUtilities.invokeLater(() -> {
                                    if (resp != null && "SUCCESS".equals(resp.getStatus())) {
                                        JOptionPane.showMessageDialog(ProductDetailDialog.this, "下单成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                                        Window w = SwingUtilities.getWindowAncestor(ProductDetailDialog.this);
                                        ShopPanel sp = findShopPanelInContainer(w);
                                        if (sp != null) { sp.reloadProducts(); }
                                        ProductDetailDialog.this.dispose();
                                    } else {
                                        JOptionPane.showMessageDialog(ProductDetailDialog.this, resp == null ? "服务器无响应" : resp.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                                    }
                                });
                            } catch (Exception ex) {
                                log.error("购买请求失败", ex);
                                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ProductDetailDialog.this, "购买失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE));
                            } finally { SwingUtilities.invokeLater(() -> buyBtn.setEnabled(true)); }
                        }).start();
                    } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(ProductDetailDialog.this, "数量必须为整数", "错误", JOptionPane.ERROR_MESSAGE); }
                });
                bottom.add(qtyPanel);
                bottom.add(addCartBtn);
                bottom.add(buyBtn);
            } else {
                JButton restockBtn = createModernButton("提醒补货", WARNING_COLOR, true);
                restockBtn.addActionListener(ae -> JOptionPane.showMessageDialog(ProductDetailDialog.this, "已发送补货提醒。", "提示", JOptionPane.INFORMATION_MESSAGE));
                bottom.add(restockBtn);
            }
        }
        JButton closeBtn = createModernButton("关闭", SECONDARY_COLOR, true);
        closeBtn.addActionListener(e -> dispose());
        bottom.add(closeBtn);
        main.add(bottom, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private void showImagePreview(Image img) {
        if (img == null) return;
        // 计算合适尺寸，限制最大为 900x900
        int iw = img.getWidth(null), ih = img.getHeight(null);
        if (iw <= 0 || ih <= 0) {
            return;
        }
        int max = 900;
        double scale = Math.min(1.0, Math.min((double) max / iw, (double) max / ih));
        int nw = Math.max(1, (int) Math.round(iw * scale));
        int nh = Math.max(1, (int) Math.round(ih * scale));
        Image scaled = img.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "图片预览", Dialog.ModalityType.APPLICATION_MODAL);
        JLabel lbl = new JLabel(new ImageIcon(scaled));
        lbl.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        dlg.getContentPane().add(new JScrollPane(lbl));
        dlg.setSize(Math.min(nw+40, max+40), Math.min(nh+60, max+60));
        dlg.setLocationRelativeTo(this);
        // 单击关闭
        lbl.addMouseListener(new java.awt.event.MouseAdapter() { @Override public void mouseClicked(java.awt.event.MouseEvent e) { dlg.dispose(); } });
        dlg.setVisible(true);
    }

    private String getUserUri() {
        try {
            var session = client.getCurrentSession();
            if (session != null && session.getRoleSet() != null) {
                java.util.Set<String> roles = session.getRoleSet();
                for (String r : roles) { if (r != null && r.equalsIgnoreCase("admin")) return "shop/adminManager"; }
                for (String r : roles) { if (r != null && r.equalsIgnoreCase("manager")) return "shop/manager"; }
                for (String r : roles) { if (r != null && (r.equalsIgnoreCase("staff") || r.equalsIgnoreCase("teacher"))) return "shop/staff"; }
            }
        } catch (Exception e) { log.error("无法从会话确定用户角色，回退 student", e); }
        return "shop/student";
    }

    // 检查当前用户卡片状态所用的 URI
    private String getCardUri() {
        try {
            var session = client.getCurrentSession();
            if (session != null && session.getRoleSet() != null) {
                java.util.Set<String> roles = session.getRoleSet();
                for (String r : roles) { if (r != null && (r.equalsIgnoreCase("staff") || r.equalsIgnoreCase("teacher"))) return "card/staff"; }
            }
        } catch (Exception ignored) {}
        return "card/student";
    }

    // 同步检查卡片是否处于可用状态（非挂失）。在调用线程中执行（网络调用，建议在后台线程调用）
    private boolean ensureCardActive() {
        try {
            Request req = new Request(getCardUri()).addParam("action", "GET_BALANCE");
            try { req.setSession(client.getCurrentSession()); } catch (Exception ignore) {}
            Response resp = client.sendRequest(req).get(6, TimeUnit.SECONDS);
            if (resp == null || !"SUCCESS".equals(resp.getStatus()) || !(resp.getData() instanceof Map)) {
                return true; // 无法确定时不阻止（以容错为主），服务器会在下游再校验
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) resp.getData();
            Object st = data.getOrDefault("status", data.getOrDefault("card_status", data.get("state")));
            if (st == null) return true;
            String s = st.toString().trim().toLowerCase();
            if ("挂失".equalsIgnoreCase(s) || "lost".equalsIgnoreCase(s) || "lost".equalsIgnoreCase(s)) return false;
            if (s.contains("lost") || s.contains("挂失")) return false;
            return true;
        } catch (Exception e) {
            log.debug("检查卡状态失败，允许继续操作：{}", e.toString());
            return true; // 出现异常时不阻止购买，服务器应有最终校验
        }
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row, String label, Object value) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        JLabel lbl = new JLabel(label);
        lbl.setFont(ATTR_FONT);
        lbl.setForeground(new Color(75,85,99));
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel v = new JLabel(value == null ? "" : value.toString());
        v.setFont(ATTR_FONT_BOLD);
        panel.add(v, gbc);
    }

    private Object safeGet(String key) { Object v = product.get(key); return v == null ? "" : v; }
    private String formatPrice(Object price) {
        if (price == null) return "";
        try { java.math.BigDecimal bd = new java.math.BigDecimal(price.toString()); return bd.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(); } catch (Exception e) { return price.toString(); }
    }

    private void loadImageAsync(JLabel label) {
        new Thread(() -> {
            try {
                Object urlObj = product.get("imageUrl");
                Image img = null;

                // 首先尝试通过 imageUrl 加载
                if (urlObj != null) {
                    String s = urlObj.toString().trim();
                    try {
                        // 支持以 '/' 开头的项目内资源路径（如 /figures/xxx 或 /images/xxx）
                        if (s.startsWith("/")) {
                            try {
                                // 请求更高分辨率以获得更大图片
                                javax.swing.ImageIcon ic = UIUtils.loadIcon(s, 520, 520);
                                if (ic != null) img = ic.getImage();
                            } catch (Exception e) { log.debug("从资源加载图片失败: {}", s, e); }
                        }

                        // 如果 imageUrl 是简单文件名（例如 product_code.png 或 product_code），尝试从 /products/ 目录加载
                        if (img == null) {
                            // 提取文件名（可能包含扩展名）
                            String filename = s.replaceAll("^.+/([^/]+)$", "$1");
                            boolean hasExt = filename.contains(".");
                            String nameOnly = hasExt ? filename.substring(0, filename.lastIndexOf('.')) : filename;
                            String[] exts = hasExt ? new String[]{filename.substring(filename.lastIndexOf('.') + 1)} : new String[]{"png","jpg","jpeg"};
                            java.util.List<String> candidates = new java.util.ArrayList<>();
                            // 优先尝试项目 classpath 下的 /products/<file>
                            if (hasExt) {
                                candidates.add("/products/" + filename);
                                candidates.add("/shop/" + filename);
                                candidates.add("/resources/products/" + filename);
                                candidates.add("/images/products/" + filename);
                            } else {
                                for (String ex : exts) {
                                    candidates.add("/products/" + nameOnly + "." + ex);
                                    candidates.add("/shop/" + nameOnly + "." + ex);
                                    candidates.add("/resources/products/" + nameOnly + "." + ex);
                                    candidates.add("/images/products/" + nameOnly + "." + ex);
                                }
                            }
                            // 也尝试小写/大写变体
                            for (int i = 0; i < candidates.size(); i++) {
                                String c = candidates.get(i);
                                candidates.add(c.toLowerCase());
                                candidates.add(c.toUpperCase());
                            }
                            for (String c : candidates) {
                                try {
                                    javax.swing.ImageIcon ic = UIUtils.loadIcon(c, 520, 520);
                                    if (ic != null) { img = ic.getImage(); log.debug("ProductDetailDialog: loaded product image from {}", c); break; }
                                } catch (Exception e) { log.debug("加载图片失败: {}", c, e); }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("处理 imageUrl 时发生异常: {}", e.getMessage());
                    }
                }

                // 若通过 imageUrl 未找到图片，尝试根据商品编码从 classpath:/products/ 加载（文件名通常为 <productCode>.png）
                if (img == null) {
                    try {
                        String code = null;
                        Object c1 = product.get("productCode"); if (c1 != null) code = c1.toString();
                        if ((code == null || code.isEmpty()) && product.get("code") != null) code = product.get("code").toString();
                        if ((code == null || code.isEmpty()) && product.get("product_id") != null) code = product.get("product_id").toString();
                        if ((code == null || code.isEmpty()) && product.get("productId") != null) code = product.get("productId").toString();
                        if (code != null) {
                            code = code.trim();
                            String[] exts = new String[]{"png","jpg","jpeg"};
                            for (String ex : exts) {
                                String p = "/products/" + code + "." + ex;
                                javax.swing.ImageIcon ic = UIUtils.loadIcon(p, 520, 520);
                                if (ic != null) { img = ic.getImage(); log.debug("ProductDetailDialog: loaded product image from {}", p); break; }
                                // 尝试小写/大写
                                ic = UIUtils.loadIcon(('/' + "products/" + code.toLowerCase() + '.' + ex).replaceAll("\\\\/\\\\/","/"), 520, 520);
                                if (ic != null) { img = ic.getImage(); log.debug("ProductDetailDialog: loaded product image from {}", "/products/" + code.toLowerCase() + "." + ex); break; }
                            }
                        }
                    } catch (Exception ignored) { log.debug("根据商品编码加载图片时出错", ignored); }
                }

                // 如果通过 imageUrl 仍未找到图片，则使用默认占位图
                if (img == null) {
                    img = UIUtils.getDefaultProductImage();
                }

                // 缩放并缓存原始图像以备放大预览
                ImageIcon icon = new ImageIcon(img);
                label.setIcon(icon);
                label.putClientProperty("origImage", img);
            } catch (Exception e) {
                log.error("加载商品图片失败", e);
            }
        }).start();
    }

    private int parseQty(String qtyText) {
        if (qtyText == null || qtyText.trim().isEmpty()) return 1;
        try {
            int qty = Integer.parseInt(qtyText.trim());
            return Math.max(1, qty);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    // 辅助方法：查找父容器中的 ShopPanel 实例
    private ShopPanel findShopPanelInContainer(Container container) {
        if (container == null) return null;
        for (Component comp : container.getComponents()) {
            if (comp instanceof ShopPanel) {
                return (ShopPanel) comp;
            } else if (comp instanceof Container) {
                ShopPanel found = findShopPanelInContainer((Container) comp);
                if (found != null) return found;
            }
        }
        return null;
    }

    private String asPlainString(Object obj) {
        if (obj == null) return "";
        if (obj instanceof Number) {
            try { java.math.BigDecimal bd = new java.math.BigDecimal(obj.toString()); bd = bd.stripTrailingZeros(); return bd.toPlainString(); }
            catch (Exception ex) { return obj.toString(); }
        }
        return obj.toString();
    }

    // 自定义 JLabel：绘制圆角、居中缩放的图片，保留 origImage 客户端属性供预览使用
    private static class RoundedImageLabel extends JLabel {
        RoundedImageLabel() { super(); setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int arc = Math.min(24, Math.min(w, h) / 6);
                Shape clip = new RoundRectangle2D.Float(0, 0, w, h, arc, arc);
                g2.setClip(clip);
                Object o = getClientProperty("origImage");
                Image img = null;
                if (o instanceof Image) img = (Image) o;
                else if (getIcon() instanceof ImageIcon) img = ((ImageIcon) getIcon()).getImage();

                if (img != null) {
                    int iw = img.getWidth(null), ih = img.getHeight(null);
                    if (iw > 0 && ih > 0) {
                        double scale = Math.min((double) w / iw, (double) h / ih);
                        int nw = Math.max(1, (int) Math.round(iw * scale));
                        int nh = Math.max(1, (int) Math.round(ih * scale));
                        int x = (w - nw) / 2; int y = (h - nh) / 2;
                        g2.drawImage(img, x, y, nw, nh, null);
                        return;
                    }
                }

                // 无图片时绘制占位
                g2.setColor(new Color(245, 247, 250));
                g2.fillRect(0, 0, w, h);
                g2.setColor(new Color(200, 208, 216));
                String t = "无图片";
                FontMetrics fm = g2.getFontMetrics(new Font("微软雅黑", Font.PLAIN, 12));
                int tx = (w - fm.stringWidth(t)) / 2;
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(t, tx, ty);
            } finally {
                g2.dispose();
            }
        }
    }
}

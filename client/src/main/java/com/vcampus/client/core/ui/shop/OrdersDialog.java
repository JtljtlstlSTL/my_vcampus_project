package com.vcampus.client.core.ui.shop;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * 简单的订单对话框，用于展示从服务端获取的订单列表。
 */
public class OrdersDialog extends JDialog {
    public OrdersDialog(Window owner, List<Map<String, Object>> orders) {
        super(owner, "我的订单", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(700, 400);
        setLocationRelativeTo(owner);

        if (orders == null || orders.isEmpty()) {
            add(new JLabel("无订单"), BorderLayout.CENTER);
            return;
        }

        // 原来的表格展示被替换为卡片视图
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        for (Map<String, Object> o : orders) {
            Object rawId = o.getOrDefault("transId", o.getOrDefault("orderId", o.get("id")));
            String id = asPlainString(rawId);

            Object prodName = o.getOrDefault("productName", o.getOrDefault("name", o.get("product_name")));
            Object prodId = o.getOrDefault("productId", o.getOrDefault("product_id", o.get("productId")));
            String productDisp = "";
            if (prodName != null) productDisp = prodName.toString();
            else if (prodId != null) productDisp = asPlainString(prodId);

            // 兼容 qty / Qty / quantity /qty
            Object qty = o.getOrDefault("qty", o.getOrDefault("Qty", o.getOrDefault("quantity", o.get("qty"))));
            String qtyStr = qty == null ? "1" : qty.toString();

            Object amount = o.getOrDefault("amount",
                    o.getOrDefault("Amount",
                            o.getOrDefault("price", o.getOrDefault("totalPrice", o.get("totalAmount")))));
            String amountStr = formatAmount(amount);

            Object created = o.getOrDefault("transTime", o.getOrDefault("trans_time", o.getOrDefault("createdAt", o.getOrDefault("created_at", o.get("time")))));
            String createdStr = formatTime(created);

            Object cardNum = o.getOrDefault("cardNum", o.getOrDefault("card_num", o.get("cardnum")));
            String cardNumStr = cardNum == null ? "" : cardNum.toString();

            // 状态显示（可选）
            Object statusObj = o.getOrDefault("status", o.get("state"));
            String statusStr = statusObj == null ? "" : statusObj.toString();

             // 卡片
             JPanel card = new JPanel(new BorderLayout());
             card.setBorder(BorderFactory.createCompoundBorder(
                     BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                     BorderFactory.createEmptyBorder(8,8,8,8)));

            // 取消左侧装饰，改为简洁卡片样式；保留一行用于展开更多信息
            // （注意：不再使用左侧条纹，以满足设计要求）

             // 中间信息（商品名、数量、金额）
             JPanel center = new JPanel();
             center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
             String nameShown = productDisp == null ? "" : productDisp;
             if (nameShown.length() > 40) nameShown = nameShown.substring(0, 37) + "...";
             JLabel nameLabel = new JLabel(nameShown + "  ");
             nameLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
             nameLabel.setForeground(new Color(34, 40, 49));
             center.add(nameLabel);

             JPanel detailRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
             detailRow.setOpaque(false);
             JLabel qtyLabel = new JLabel("数量: " + qtyStr);
             qtyLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
             qtyLabel.setForeground(new Color(102, 112, 133));
             JLabel amountLabel = new JLabel("金额: ￥" + amountStr);
             amountLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
             amountLabel.setForeground(new Color(34, 197, 94));
             detailRow.add(qtyLabel);
             detailRow.add(amountLabel);
             // 如果有状态则显示
             if (!statusStr.isEmpty()) {
                 JLabel statusLabel = new JLabel(statusStr);
                 statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
                 statusLabel.setForeground(new Color(59, 130, 246));
                 detailRow.add(Box.createHorizontalStrut(8));
                 detailRow.add(statusLabel);
             }
             center.add(detailRow);

             // 直接显示订单内容（更适合单商品交易），若需扩展为多商品请从数据端传递 items 列表
             JLabel contentLabel = new JLabel("订单内容: " + nameShown + " × " + qtyStr);
             contentLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
             contentLabel.setForeground(new Color(99, 102, 120));
             center.add(Box.createVerticalStrut(6));
             center.add(contentLabel);

             // 预留一个可展开行，用于显示扩展信息（购买时间与卡号），默认隐藏
             JPanel detailsPanel = new JPanel(new GridLayout(2,1));
             detailsPanel.setOpaque(false);
             detailsPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
             JLabel dtLabel = new JLabel("购买时间: " + createdStr);
             dtLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10)); dtLabel.setForeground(new Color(120,125,140));
             JLabel cardLabel = new JLabel("购买卡号: " + cardNumStr);
             cardLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10)); cardLabel.setForeground(new Color(120,125,140));
             detailsPanel.add(dtLabel); detailsPanel.add(cardLabel);
             detailsPanel.setVisible(false);
             center.add(Box.createVerticalStrut(6));

             // 右侧折叠按钮：控制 detailsPanel 显示/隐藏（与管理员界面一致）
             JButton toggleBtn = new JButton("▾");
             toggleBtn.setMargin(new Insets(2,6,2,6)); toggleBtn.setFocusable(false);
             toggleBtn.addActionListener(ev -> {
                 boolean vis = !detailsPanel.isVisible();
                 detailsPanel.setVisible(vis);
                 toggleBtn.setText(vis ? "▴" : "▾");
                 SwingUtilities.invokeLater(() -> { card.revalidate(); card.repaint(); });
             });
             JPanel rightBtnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); rightBtnWrap.setOpaque(false); rightBtnWrap.add(toggleBtn);

             card.add(center, BorderLayout.CENTER);
             card.add(detailsPanel, BorderLayout.SOUTH);
             card.add(rightBtnWrap, BorderLayout.EAST);

             list.add(card);
             list.add(Box.createRigidArea(new Dimension(0,8)));
         }

         JScrollPane scroll = new JScrollPane(list);
         add(scroll, BorderLayout.CENTER);
     }

    // 格式化时间为可读字符串
    private String formatTime(Object timeObj) {
        if (timeObj == null) return "";
        try {
            if (timeObj instanceof Number) {
                long v = ((Number) timeObj).longValue();
                java.time.Instant instant = v > 1_000_000_000_000L ? java.time.Instant.ofEpochMilli(v) : java.time.Instant.ofEpochSecond(v);
                return java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(java.time.ZoneId.systemDefault()).format(instant);
            }
            String s = timeObj.toString().trim(); if (s.isEmpty()) return "";
            try { java.time.Instant inst = java.time.Instant.parse(s); return java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(java.time.ZoneId.systemDefault()).format(inst); } catch (Exception ignored) {}
            // 尝试 ISO 本地时间
            try { java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(s); return ldt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); } catch (Exception ignored) {}
            // 简单提取日期子串
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{4}[-/]\\d{2}[-/]\\d{2}([ T]\\d{2}:\\d{2}:\\d{2})?)").matcher(s);
            if (m.find()) return m.group(1).replace('T', ' ');
            return s;
        } catch (Exception e) { return timeObj.toString(); }
    }

    // 将可能为数字的 ID 格式化为不带冗余小数的字符串（例如 1.0 -> "1")
    private String asPlainString(Object obj) {
        if (obj == null) return "";
        // 数字类型直接规范化
        if (obj instanceof Number) {
            try {
                java.math.BigDecimal bd = new java.math.BigDecimal(obj.toString());
                bd = bd.stripTrailingZeros();
                return bd.toPlainString();
            } catch (Exception ex) {
                return obj.toString();
            }
        }
        // 字符串形式也尝试解析为数值以去掉 .0
        try {
            java.math.BigDecimal bd = new java.math.BigDecimal(obj.toString());
            bd = bd.stripTrailingZeros();
            return bd.toPlainString();
        } catch (Exception ignored) {
            return obj.toString();
        }
    }

    // 格式化金额，保留两位小数
    private String formatAmount(Object obj) {
        if (obj == null) return "";
        try {
            java.math.BigDecimal bd;
            if (obj instanceof java.math.BigDecimal) bd = (java.math.BigDecimal)obj;
            else bd = new java.math.BigDecimal(obj.toString());
            bd = bd.setScale(2, java.math.RoundingMode.HALF_UP);
            return bd.toPlainString();
        } catch (Exception e) {
            return obj.toString();
        }
    }

    // 移除了订单视图中的 emoji，保持简洁专业的展示风格
 }

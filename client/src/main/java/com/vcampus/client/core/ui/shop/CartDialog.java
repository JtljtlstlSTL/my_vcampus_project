package com.vcampus.client.core.ui.shop;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/** 精简版购物车对话框 */
public class CartDialog extends JDialog {
    private final NettyClient nettyClient;
    private final ShopPanel shopPanel;
    private final CartPanel cartPanel = new CartPanel();
    private final JButton btnRefresh = new JButton("刷新");
    private final JButton btnSetQty = new JButton("改数量");
    private final JButton btnRemove = new JButton("移除");
    private final JButton btnClear = new JButton("清空");
    private final JButton btnCheckout = new JButton("结算");
    private final JLabel summary = new JLabel("合计: 0 件  ¥0.00");

    public CartDialog(Window owner, NettyClient nettyClient, ShopPanel shopPanel) {
        super(owner, "购物车", ModalityType.MODELESS);
        this.nettyClient = nettyClient;
        this.shopPanel = shopPanel;
        setSize(640, 420);
        setLayout(new BorderLayout(8,8));
        add(new JScrollPane(cartPanel), BorderLayout.CENTER);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,6,4));
        top.add(summary);
        add(top, BorderLayout.NORTH);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,4));
        bottom.add(btnRefresh); bottom.add(btnSetQty); bottom.add(btnRemove); bottom.add(btnClear); bottom.add(btnCheckout);
        JButton btnClose = new JButton("关闭"); bottom.add(btnClose);
        add(bottom, BorderLayout.SOUTH);
        bind();
        // 订阅本地模型变更，以便立即更新汇总与顶部计数（当进行乐观更新或本地修改时）
        cartPanel.setOnChange(() -> {
            SwingUtilities.invokeLater(() -> {
                updateSummary(cartPanel.totalCount(), cartPanel.totalAmount());
                if (shopPanel != null) shopPanel.updateCartCount(cartPanel.totalCount());
            });
        });
        setLocationRelativeTo(owner);
    }

    private void bind() {
        btnRefresh.addActionListener(e -> loadCartAsync());
        btnSetQty.addActionListener(e -> modifyQty());
        btnRemove.addActionListener(e -> removeSelected());
        btnClear.addActionListener(e -> clearCart());
        btnCheckout.addActionListener(e -> checkout());
        // 设置默认按钮为结算
        getRootPane().setDefaultButton(btnCheckout);
        // 关闭按钮监听
        for (Component c : ((JPanel)getContentPane().getComponent(2)).getComponents()) {
            if (c instanceof JButton b && "关闭".equals(b.getText())) b.addActionListener(ev -> setVisible(false));
        }
    }

    private String userUri() {
        // 使用ShopPanel的正确路由逻辑
        if (shopPanel != null) {
            return shopPanel.getUserUri();
        }
        return "shop/student"; // 默认路由
    }

    public void loadCartAsync() {
        new Thread(() -> {
            try {
                Request req = new Request(userUri()).addParam("action","GET_CART");
                req.setSession(nettyClient.getCurrentSession());
                Response resp = nettyClient.sendRequest(req).get(10, java.util.concurrent.TimeUnit.SECONDS);
                System.out.println("[DEBUG] CartDialog.loadCartAsync resp=" + resp);
                if (resp!=null && "SUCCESS".equals(resp.getStatus()) && resp.getData() instanceof Map) {
                    @SuppressWarnings("unchecked") Map<String,Object> data = (Map<String,Object>) resp.getData();
                    Object itemsObj = data.get("items");
                    @SuppressWarnings("unchecked") List<Map<String,Object>> items = (List<Map<String,Object>>) (itemsObj instanceof List? itemsObj : java.util.Collections.emptyList());
                    // 兼容多种命名：count / cartCount
                    int count = asInt(data.getOrDefault("count", data.get("cartCount")));
                    // 兼容 totalAmount / total_amount，以及可能嵌套在 remainingCart 中
                    java.math.BigDecimal total = bd(data.get("totalAmount"));
                    if (total.compareTo(java.math.BigDecimal.ZERO) == 0 && data.get("total_amount") != null) total = bd(data.get("total_amount"));
                    if (total.compareTo(java.math.BigDecimal.ZERO) == 0 && data.get("remainingCart") instanceof Map) {
                        @SuppressWarnings("unchecked") Map<String,Object> rem = (Map<String,Object>) data.get("remainingCart");
                        total = bd(rem.getOrDefault("totalAmount", rem.get("total_amount")));
                        if (count == 0) count = asInt(rem.getOrDefault("count", rem.get("cartCount")));
                    }
                    // 为了使 lambda 捕获的值为 effectively-final，创建 final 副本供 Swing 线程使用
                    final int finalCount = count;
                    final java.math.BigDecimal finalTotal = total;
                    SwingUtilities.invokeLater(() -> {
                        cartPanel.setItems(items);
                        updateSummary(finalCount, finalTotal);
                        if (shopPanel != null) shopPanel.updateCartCount(finalCount);
                    });
                } else {
                    System.out.println("[DEBUG] CartDialog.loadCartAsync non-success resp=" + resp);
                    showError(resp);
                }
            } catch (Exception ex) { showError(ex.getMessage()); }
        }).start();
    }

    private void modifyQty() {
        int viewRow = cartPanel.getSelectedRow();
        if (viewRow < 0) { info("请选择一行"); return; }
        Integer pid = cartPanel.getProductIdAt(viewRow); if (pid == null) { info("ID无效"); return; }
        String input = JOptionPane.showInputDialog(this,"新数量","1"); if (input==null) return;
        int qty; try { qty = new java.math.BigDecimal(input.trim()).intValueExact(); if (qty<=0) throw new RuntimeException(); } catch (Exception e){ info("数量必须为正整数"); return; }
        // 进行乐观本地更新以提升交互感受，然后在后台同步服务器
        cartPanel.updateQuantityLocal(pid, qty);
        new Thread(() -> {
            try {
                Request req = new Request(userUri()).addParam("action","SET_CART_ITEM")
                        .addParam("productId", pid.toString()).addParam("qty", String.valueOf(qty));
                req.setSession(nettyClient.getCurrentSession());
                Response resp = nettyClient.sendRequest(req).get(10, java.util.concurrent.TimeUnit.SECONDS);
                System.out.println("[DEBUG] CartDialog.modifyQty resp=" + resp);
                if (resp!=null && "SUCCESS".equals(resp.getStatus())) {
                    SwingUtilities.invokeLater(() -> { loadCartAsync(); });
                } else {
                    SwingUtilities.invokeLater(() -> { loadCartAsync(); showError(resp); });
                }
            } catch (Exception ex) { SwingUtilities.invokeLater(() -> { loadCartAsync(); showError(ex.getMessage()); }); }
        }).start();
    }

    private void removeSelected() {
        List<Integer> ids = cartPanel.getSelectedProductIds();
        if (ids.isEmpty()) { info("请选择要移除的行"); return; }
        if (JOptionPane.showConfirmDialog(this,"确认移除选中?","确认",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        String joined = ids.stream().map(Object::toString).reduce((a,b)->a+","+b).orElse("");
        new Thread(() -> {
            try {
                Request req = new Request(userUri()).addParam("action","REMOVE_FROM_CART").addParam("productIds", joined);
                req.setSession(nettyClient.getCurrentSession());
                Response resp = nettyClient.sendRequest(req).get(10, java.util.concurrent.TimeUnit.SECONDS);
                System.out.println("[DEBUG] CartDialog.removeSelected resp=" + resp);
                if (resp!=null && "SUCCESS".equals(resp.getStatus())) SwingUtilities.invokeLater(this::loadCartAsync); else SwingUtilities.invokeLater(() -> { showError(resp); loadCartAsync(); });
            } catch (Exception ex){ SwingUtilities.invokeLater(() -> { showError(ex.getMessage()); loadCartAsync(); }); }
        }).start();
    }

    private void clearCart() {
        if (JOptionPane.showConfirmDialog(this,"确认清空购物车?","确认",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        // 乐观更新：立即清空本地面板与汇总，随后同步服务器
        SwingUtilities.invokeLater(() -> {
            cartPanel.setItems(java.util.Collections.emptyList());
            updateSummary(0, java.math.BigDecimal.ZERO);
            if (shopPanel != null) shopPanel.updateCartCount(0);
        });
        new Thread(() -> {
            try {
                Request req = new Request(userUri()).addParam("action","CLEAR_CART");
                req.setSession(nettyClient.getCurrentSession());
                Response resp = nettyClient.sendRequest(req).get(10, java.util.concurrent.TimeUnit.SECONDS);
                System.out.println("[DEBUG] CartDialog.clearCart resp=" + resp);
                if (resp!=null && "SUCCESS".equals(resp.getStatus())) SwingUtilities.invokeLater(this::loadCartAsync); else SwingUtilities.invokeLater(() -> { showError(resp); loadCartAsync(); });
            } catch (Exception ex){ SwingUtilities.invokeLater(() -> { showError(ex.getMessage()); loadCartAsync(); }); }
        }).start();
    }

    private void checkout() {
        if (cartPanel.totalCount()==0) { info("购物车为空"); return; }
        if (JOptionPane.showConfirmDialog(this,"确认结算?","结算",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION) return;
        btnCheckout.setEnabled(false);
        // 乐观：禁用按钮并在完成后处理
        new Thread(() -> {
            try {
                Request req = new Request(userUri()).addParam("action","CHECKOUT_CART");
                req.setSession(nettyClient.getCurrentSession());
                Response resp = nettyClient.sendRequest(req).get(40, java.util.concurrent.TimeUnit.SECONDS);
                System.out.println("[DEBUG] CartDialog.checkout resp=" + resp);
                SwingUtilities.invokeLater(() -> {
                    btnCheckout.setEnabled(true);
                    if (resp!=null && ("SUCCESS".equals(resp.getStatus()) || "ERROR".equals(resp.getStatus()))) {
                        String msg = resp.getMessage();
                        JOptionPane.showMessageDialog(this, msg, "结算结果", "SUCCESS".equals(resp.getStatus())?JOptionPane.INFORMATION_MESSAGE:JOptionPane.WARNING_MESSAGE);
                        // 尝试使用服务器返回的数据立即更新界面
                        if (resp.getData() instanceof Map) {
                            @SuppressWarnings("unchecked") Map<String,Object> d = (Map<String,Object>) resp.getData();
                            Object cartCount = d.getOrDefault("cartCount", d.get("count"));
                            // 优先使用返回的 remainingCart（如果有），否则使用 top-level totalAmount
                            Object totalAmt = d.get("totalAmount");
                            if (totalAmt == null && d.get("remainingCart") instanceof Map) {
                                @SuppressWarnings("unchecked") Map<String,Object> rem = (Map<String,Object>) d.get("remainingCart");
                                totalAmt = rem.getOrDefault("totalAmount", rem.get("total_amount"));
                                cartCount = rem.getOrDefault("count", rem.get("cartCount"));
                            }
                            if (cartCount != null) {
                                int c = new java.math.BigDecimal(cartCount.toString()).intValue();
                                if (shopPanel != null) shopPanel.updateCartCount(c);
                            }
                            if (totalAmt != null) {
                                java.math.BigDecimal ta = bd(totalAmt);
                                updateSummary(cartPanel.totalCount(), ta);
                            }
                        }
                        loadCartAsync();
                        shopPanel.reloadProducts();
                    } else showError(resp);
                });
            } catch (Exception ex){ SwingUtilities.invokeLater(() -> { btnCheckout.setEnabled(true); showError(ex.getMessage()); }); }
        }).start();
    }

    private void updateSummary(int count, java.math.BigDecimal amount){ summary.setText("合计: "+count+" 件  ¥"+amount.setScale(2, java.math.RoundingMode.HALF_UP)); }

    private int asInt(Object o){ if(o==null) return 0; try { return new java.math.BigDecimal(o.toString()).intValue(); } catch (Exception e){ return 0;} }

    private java.math.BigDecimal bd(Object o){ if(o==null) return java.math.BigDecimal.ZERO; try { return new java.math.BigDecimal(o.toString()); } catch (Exception e){ return java.math.BigDecimal.ZERO;} }

    private void showError(Response resp){ showError(resp != null ? resp.getMessage() : "操作失败"); }

    private void showError(String msg){ SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, msg, "错误", JOptionPane.ERROR_MESSAGE)); }

    private void info(String msg){ JOptionPane.showMessageDialog(this, msg, "提示", JOptionPane.INFORMATION_MESSAGE); }
}

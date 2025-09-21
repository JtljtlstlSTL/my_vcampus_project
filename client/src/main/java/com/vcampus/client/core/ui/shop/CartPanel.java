package com.vcampus.client.core.ui.shop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 精简购物车面板：仅负责表格展示及本地行操作，网络交互放在 CartDialog 中。
 */
public class CartPanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;
    // 保留原始项数据以便展开显示更多字段（例如购买日期与购买卡号）
    private List<Map<String,Object>> rawItems = new ArrayList<>();
    private final JPanel detailsPanel;
    private final JLabel lblPurchaseDate;
    private final JLabel lblCardNum;
    // 当前已展开的模型行索引（-1 表示未展开）
    private int expandedModelRow = -1;

    // 当模型变化时的回调（例如数量变化后通知外部更新汇总）
    private Runnable onChange = null;

    public CartPanel() {
        setLayout(new BorderLayout());
        model = new DefaultTableModel(new String[]{"商品ID","名称","单价","数量","小计"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        table = new JTable(model);
        table.setRowHeight(26);

        // 初始化可展开的详情面板及标签（之前被意外移除，导致编译失败）
        detailsPanel = new JPanel();
        detailsPanel.setLayout(new GridLayout(2,1,4,4));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(8,12,12,12));
        lblPurchaseDate = new JLabel("");
        lblCardNum = new JLabel("");
        lblPurchaseDate.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        lblCardNum.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        detailsPanel.add(lblPurchaseDate);
        detailsPanel.add(lblCardNum);
        detailsPanel.setVisible(false);

        // 为价格与小计列设置渲染器，保持模型中存放数值类型但以格式化文本显示
        javax.swing.table.DefaultTableCellRenderer moneyRenderer = new javax.swing.table.DefaultTableCellRenderer(){
            @Override public void setValue(Object value){
                if(value instanceof BigDecimal){ setText(fmt((BigDecimal)value)); }
                else if(value==null) setText(""); else setText(value.toString());
            }
        };
        // 注意列索引在表格创建后可用
        // 构建中部区域：表格 + 可展开的详情面板
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        centerPanel.add(detailsPanel, BorderLayout.SOUTH);

        // 如果列已创建则应用渲染器（安全检查）
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                if (table.getColumnCount() > 2) table.getColumnModel().getColumn(2).setCellRenderer(moneyRenderer);
                if (table.getColumnCount() > 4) table.getColumnModel().getColumn(4).setCellRenderer(moneyRenderer);
            } catch (Exception ignored) {}
        });

        add(centerPanel, BorderLayout.CENTER);

        // 鼠标监听：双击或回车时切换展开；单击只选择
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = table.getSelectedRow();
                    toggleDetailsForViewRow(viewRow);
                }
            }
        });
        // 支持键盘回车切换
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ENTER"), "toggleDetails");
        table.getActionMap().put("toggleDetails", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                toggleDetailsForViewRow(table.getSelectedRow());
            }
        });
    }

    public void setOnChange(Runnable onChange) { this.onChange = onChange; }

    private void notifyChange(){ if(onChange!=null) SwingUtilities.invokeLater(onChange); }

    public void setItems(List<Map<String,Object>> items){
        model.setRowCount(0);
        rawItems.clear();
        if(items==null) { notifyChange(); return; }
        // 保留原始 items 顺序以便展开显示更多字段
        rawItems.addAll(items);
        for(Map<String,Object> it: items){
            // 使用兼容逻辑查找商品ID，优先 productId -> id -> product_id -> sku 等
            String pid = ShopUtils.findProductId(it);
            Object name = it.getOrDefault("productName", it.get("name"));
            BigDecimal price = parseBD(it.get("price"));
            int qty = parseInt(it.get("qty"));
            BigDecimal subtotal = parseBD(it.get("subtotal"));
            if(subtotal.compareTo(BigDecimal.ZERO)==0) subtotal = price.multiply(BigDecimal.valueOf(qty));
            // 在模型中保留数值类型，渲染器负责格式化显示
            model.addRow(new Object[]{pid, name, price, qty, subtotal});
        }
        notifyChange();
    }

    // 切换 viewRow 的展开状态（内部使用）
    private void toggleDetailsForViewRow(int viewRow) {
        if (viewRow < 0) return;
        int modelRow = table.convertRowIndexToModel(viewRow);
        if (modelRow == expandedModelRow) {
            // 关闭
            detailsPanel.setVisible(false);
            expandedModelRow = -1;
        } else {
            showDetailsForModelRow(modelRow);
        }
        revalidate(); repaint();
    }

    private void showDetailsForModelRow(int modelRow) {
        if (modelRow < 0 || modelRow >= rawItems.size()) {
            detailsPanel.setVisible(false); expandedModelRow = -1; return;
        }
        Map<String,Object> it = rawItems.get(modelRow);
        // 尝试多种字段名以兼容后端
        Object created = it.getOrDefault("transTime", it.getOrDefault("trans_time", it.getOrDefault("createdAt", it.getOrDefault("created_at", it.get("time")))));
        Object cardNum = it.getOrDefault("cardNum", it.getOrDefault("card_num", it.get("cardnum")));
        String createdStr = created == null ? "" : created.toString();
        String cardStr = cardNum == null ? "" : cardNum.toString();
        lblPurchaseDate.setText("购买日期: " + createdStr);
        lblCardNum.setText("购买卡号: " + cardStr);
        detailsPanel.setVisible(true);
        expandedModelRow = modelRow;
    }

    public List<Map<String,Object>> getCurrentRows(){
        List<Map<String,Object>> list = new ArrayList<>();
        for(int i=0;i<model.getRowCount();i++){
            list.add(Map.of(
                    "productId", model.getValueAt(i,0),
                    "productName", model.getValueAt(i,1),
                    "price", model.getValueAt(i,2),
                    "qty", model.getValueAt(i,3),
                    "subtotal", model.getValueAt(i,4)
            ));
        }
        return list;
    }

    public int getSelectedRow(){ return table.getSelectedRow(); }
    public List<Integer> getSelectedProductIds(){
        int[] rows = table.getSelectedRows();
        List<Integer> ids = new ArrayList<>();
        for(int r: rows){
            // table.getSelectedRows 返回的是 view 索引，需要转换为 model 索引
            int modelRow = table.convertRowIndexToModel(r);
            Object v = model.getValueAt(modelRow,0);
            try { ids.add(Integer.parseInt(v.toString())); } catch (Exception ignored) {}
        }
        return ids;
    }

    public Integer getProductIdAt(int viewRow){
        if(viewRow<0 || viewRow>=table.getRowCount()) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object v = model.getValueAt(modelRow,0);
        try { return Integer.parseInt(v.toString()); } catch (Exception e){ return null; }
    }

    public void updateQuantityLocal(Integer productId,int newQty){
        for(int i=0;i<model.getRowCount();i++){
            Object pid = model.getValueAt(i,0);
            if(pid!=null && pid.toString().equals(productId.toString())){
                model.setValueAt(newQty, i,3);
                BigDecimal price = parseBD(model.getValueAt(i,2));
                model.setValueAt(price.multiply(BigDecimal.valueOf(newQty)), i,4);
                notifyChange();
                break;
            }
        }
    }

    public int totalCount(){
        int sum=0; for(int i=0;i<model.getRowCount();i++){ sum += parseInt(model.getValueAt(i,3)); }
        return sum;
    }
    public BigDecimal totalAmount(){
        BigDecimal t = BigDecimal.ZERO;
        for(int i=0;i<model.getRowCount();i++) t = t.add(parseBD(model.getValueAt(i,4)));
        return t;
    }

    private int parseInt(Object o){
        if(o==null) return 0;
        if(o instanceof Number) return ((Number)o).intValue();
        try { return new java.math.BigDecimal(o.toString()).intValue(); } catch (Exception e){ return 0; }
    }
    private BigDecimal parseBD(Object o){
        if(o==null) return BigDecimal.ZERO;
        if(o instanceof BigDecimal) return (BigDecimal)o;
        if(o instanceof Number) return new BigDecimal(o.toString());
        try { return new BigDecimal(o.toString()); } catch (Exception e){ return BigDecimal.ZERO; }
    }
    private String fmt(BigDecimal bd){ return bd.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(); }
}

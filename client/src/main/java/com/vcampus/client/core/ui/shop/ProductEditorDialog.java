package com.vcampus.client.core.ui.shop;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单的商品编辑/添加对话框，返回表单数据的 Map。
 */
public class ProductEditorDialog extends JDialog {
    private final JTextField txtCode = new JTextField(20);
    private final JTextField txtName = new JTextField(30);
    private final JTextField txtPrice = new JTextField(10);
    private final JTextField txtStock = new JTextField(6);
    private final JTextField txtStatus = new JTextField(10);
    private final JTextField txtCategory = new JTextField(12);
    private final JTextArea txtDescription = new JTextArea(4, 40);
    private final JLabel lblEmoji = new JLabel("🛍️");

    private Map<String, Object> result = null;

    public ProductEditorDialog(Window owner, Map<String, Object> product) {
        super(owner, (product == null ? "添加商品" : "编辑商品"), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.anchor = GridBagConstraints.WEST;

        int row = 0;
        c.gridx = 0; c.gridy = row; form.add(new JLabel("编码:"), c);
        c.gridx = 1; form.add(txtCode, c);
        row++;
        c.gridx = 0; c.gridy = row; form.add(new JLabel("名称:"), c);
        c.gridx = 1; form.add(txtName, c);
        row++;
        c.gridx = 0; c.gridy = row; form.add(new JLabel("价格:"), c);
        c.gridx = 1; form.add(txtPrice, c);
        row++;
        c.gridx = 0; c.gridy = row; form.add(new JLabel("库存:"), c);
        c.gridx = 1; form.add(txtStock, c);
        row++;
        c.gridx = 0; c.gridy = row; form.add(new JLabel("状态:"), c);
        c.gridx = 1; form.add(txtStatus, c);
        row++;
        c.gridx = 0; c.gridy = row; form.add(new JLabel("分类:"), c);
        c.gridx = 1; {
            JPanel catRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            txtCategory.setPreferredSize(new Dimension(160, txtCategory.getPreferredSize().height));
            lblEmoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            catRow.add(txtCategory);
            catRow.add(lblEmoji);
            form.add(catRow, c);
        }
        row++;
        c.gridx = 0; c.gridy = row; c.anchor = GridBagConstraints.NORTHWEST; form.add(new JLabel("描述:"), c);
        c.gridx = 1; JScrollPane sp = new JScrollPane(txtDescription); sp.setPreferredSize(new Dimension(400, 80)); form.add(sp, c);

        if (product != null) {
            txtCode.setText(String.valueOf(product.getOrDefault("productCode", product.get("Product_code"))));
            txtName.setText(String.valueOf(product.getOrDefault("productName", product.get("Productname"))));
            Object price = product.getOrDefault("price", product.get("Price")); txtPrice.setText(price==null?"":String.valueOf(price));
            Object stock = product.getOrDefault("stock", product.get("Stock")); txtStock.setText(stock==null?"":String.valueOf(stock));
            txtStatus.setText(String.valueOf(product.getOrDefault("status", product.get("Product_status"))));
            txtCategory.setText(String.valueOf(product.getOrDefault("category", product.get("Product_category"))));
            // 如果产品有 emoji 字段优先显示
            Object emo = product.get("emoji");
            if (emo != null && !String.valueOf(emo).trim().isEmpty()) lblEmoji.setText(String.valueOf(emo));
            txtDescription.setText(String.valueOf(product.getOrDefault("description", product.get("Product_description"))));
        }

        // 根据分类文本实时更新 emoji 预览
        txtCategory.getDocument().addDocumentListener(new DocumentListener() {
            void update() {
                String cat = txtCategory.getText().trim();
                String e = ProductGridPanel.emojiForCategory(cat);
                lblEmoji.setText(e);
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("确定");
        JButton cancel = new JButton("取消");
        buttons.add(ok); buttons.add(cancel);

        ok.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                // 校验
                String code = txtCode.getText().trim();
                String name = txtName.getText().trim();
                String priceStr = txtPrice.getText().trim();
                String stockStr = txtStock.getText().trim();
                if (name.isEmpty()) { JOptionPane.showMessageDialog(ProductEditorDialog.this, "名称不能为空", "错误", JOptionPane.ERROR_MESSAGE); return; }
                if (priceStr.isEmpty()) { JOptionPane.showMessageDialog(ProductEditorDialog.this, "价格不能为空", "错误", JOptionPane.ERROR_MESSAGE); return; }
                try { new java.math.BigDecimal(priceStr); } catch (Exception ex) { JOptionPane.showMessageDialog(ProductEditorDialog.this, "价格格式错误", "错误", JOptionPane.ERROR_MESSAGE); return; }
                try { parseQty(stockStr); } catch (Exception ex) { JOptionPane.showMessageDialog(ProductEditorDialog.this, "库存必须为整数", "错误", JOptionPane.ERROR_MESSAGE); return; }

                result = new HashMap<>();
                if (!code.isEmpty()) result.put("productCode", code);
                result.put("productName", name);
                result.put("price", priceStr);
                result.put("stock", stockStr);
                result.put("status", txtStatus.getText().trim());
                result.put("category", txtCategory.getText().trim());
                // include emoji computed from category (or visible emoji if edited)
                result.put("emoji", lblEmoji.getText());
                result.put("description", txtDescription.getText().trim());
                dispose();
            }
        });

        cancel.addActionListener(e -> { result = null; dispose(); });

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);
    }

    public Map<String, Object> getResult() { return result; }

    // 解析数量输入，兼容 "12" 或 "12.0"，要求为整数
    private int parseQty(String s) throws NumberFormatException {
        if (s == null) throw new NumberFormatException("空数量");
        String t = s.trim();
        if (t.isEmpty()) throw new NumberFormatException("空数量");
        try { return Integer.parseInt(t); }
        catch (NumberFormatException e) {
            try {
                java.math.BigDecimal bd = new java.math.BigDecimal(t);
                java.math.BigDecimal stripped = bd.stripTrailingZeros();
                if (stripped.scale() <= 0) return stripped.intValueExact();
            } catch (Exception ignored) {}
            throw new NumberFormatException("数量必须为整数");
        }
    }
}

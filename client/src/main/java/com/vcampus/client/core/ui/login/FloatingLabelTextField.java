package com.vcampus.client.core.ui.login;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * 浮动标签输入框，支持圆角、标签上浮动画、加粗字体、浅色风格
 */
public class FloatingLabelTextField extends JPanel {
    private final JTextField textField;
    private final JLabel label;
    private boolean isFloating = false;
    public final String labelText;
    private final Color baseColor = new Color(240, 248, 255); // 浅色背景
    private final Color borderColor = new Color(180, 220, 250);
    private final Color labelColor = new Color(120, 160, 220);
    private final Color labelFloatColor = new Color(80, 120, 200);

    public FloatingLabelTextField(String labelText, int columns, boolean isPassword) {
        setLayout(null);
        setOpaque(false);
        this.labelText = labelText;
        label = new JLabel(labelText);
        label.setFont(new Font("微软雅黑", Font.BOLD, 15));
        label.setForeground(labelColor);
        textField = isPassword ? new JPasswordField() : new JTextField();
        textField.setColumns(columns);
    textField.setFont(new Font("微软雅黑", Font.BOLD, 13));
        textField.setBackground(baseColor);
        textField.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        textField.setForeground(Color.BLACK);
    setPreferredSize(new Dimension(180, 56));
    setMaximumSize(new Dimension(180, 56));
    setMinimumSize(new Dimension(180, 56));
    add(label);
    add(textField);
    textField.setBounds(0, 24, 180, 32);
        label.setBounds(12, 24, 200, 24);
        // 监听焦点和内容变化
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                isFloating = true;
                repaint();
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    isFloating = false;
                }
                repaint();
            }
        });
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateFloating(); }
            public void removeUpdate(DocumentEvent e) { updateFloating(); }
            public void changedUpdate(DocumentEvent e) { updateFloating(); }
            private void updateFloating() {
                if (!textField.getText().isEmpty()) {
                    isFloating = true;
                } else if (!textField.isFocusOwner()) {
                    isFloating = false;
                }
                repaint();
            }
        });
    }
    public JTextField getTextField() { return textField; }
    public String getText() { return textField.getText(); }
    public void setText(String text) { textField.setText(text); }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 绘制最大圆角背景
        int arc = 32; // 圆角半径等于高度，最圆角
        g2.setColor(baseColor);
        g2.fillRoundRect(0, 24, 180, 32, arc, arc);
        // 绘制边框
        g2.setColor(borderColor);
        g2.drawRoundRect(0, 24, 180, 32, arc, arc);
        // 标签浮动动画
        if (isFloating) {
            g2.setFont(new Font("微软雅黑", Font.BOLD, 13));
            g2.setColor(labelFloatColor);
            g2.drawString(labelText, 12, 18);
            label.setVisible(false);
        } else {
            label.setVisible(true);
        }
        g2.dispose();
    }
}

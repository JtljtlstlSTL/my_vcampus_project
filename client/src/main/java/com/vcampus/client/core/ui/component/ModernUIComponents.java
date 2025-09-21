package com.vcampus.client.core.ui.component;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 现代化UI组件工具类
 * 提供统一的现代化样式组件
 */
public class ModernUIComponents {

    // 现代化的颜色主题
    public static final Color PRIMARY_COLOR = new Color(74, 144, 226);       // 主色调 - 现代蓝
    public static final Color PRIMARY_DARK = new Color(56, 108, 176);        // 深色主色调
    public static final Color SECONDARY_COLOR = new Color(248, 250, 252);    // 次要颜色 - 极浅灰
    public static final Color ACCENT_COLOR = new Color(34, 197, 94);         // 强调色 - 现代绿
    public static final Color ACCENT_DARK = new Color(22, 163, 74);          // 深色强调色
    public static final Color WARNING_COLOR = new Color(251, 146, 60);       // 警告色 - 现代橙
    public static final Color WARNING_DARK = new Color(234, 88, 12);         // 深色警告色
    public static final Color DANGER_COLOR = new Color(239, 68, 68);         // 危险色 - 现代红
    public static final Color DANGER_DARK = new Color(220, 38, 38);          // 深色危险色
    public static final Color BACKGROUND_COLOR = new Color(249, 250, 251);   // 背景色 - 温和灰白
    public static final Color CARD_COLOR = Color.WHITE;                      // 卡片背景色
    public static final Color TEXT_PRIMARY = new Color(17, 24, 39);          // 主要文字色 - 深灰
    public static final Color TEXT_SECONDARY = new Color(107, 114, 128);     // 次要文字色 - 中灰
    public static final Color BORDER_COLOR = new Color(229, 231, 235);       // 边框色 - 浅灰
    public static final Color HOVER_COLOR = new Color(243, 244, 246);        // 悬停色 - 很浅灰
    public static final Color SELECTED_COLOR = new Color(239, 246, 255);     // 选中色 - 浅蓝白

    /**
     * 创建现代化样式按钮
     */
    public static JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text) {
            private boolean isHovered = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 背景色
                Color currentBg = isHovered ? hoverColor : bgColor;
                g2d.setColor(currentBg);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // 文字
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                // 不绘制边框
            }
        };

        button.setFont(new Font("微软雅黑", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(120, 36));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 添加悬停效果
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.putClientProperty("isHovered", true);
                button.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.putClientProperty("isHovered", false);
                button.repaint();
            }
        });

        return button;
    }

    /**
     * 创建现代化样式文本框
     */
    public static JTextField createStyledTextField(String placeholder, int columns) {
        JTextField textField = new JTextField(columns) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 背景
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

                // 边框
                g2d.setColor(isFocusOwner() ? PRIMARY_COLOR : BORDER_COLOR);
                g2d.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1f));
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 6, 6);

                g2d.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // 不绘制默认边框
            }
        };

        textField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        textField.setBackground(CARD_COLOR);
        textField.setForeground(TEXT_PRIMARY);
        textField.setBorder(new EmptyBorder(8, 12, 8, 12));
        textField.setToolTipText(placeholder);

        // 添加占位符效果
        if (placeholder != null && !placeholder.isEmpty()) {
            textField.putClientProperty("JTextField.placeholderText", placeholder);
        }

        return textField;
    }

    /**
     * 创建现代化样式下拉框
     */
    public static JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        comboBox.setBackground(CARD_COLOR);
        comboBox.setForeground(TEXT_PRIMARY);
        comboBox.setBorder(createRoundedBorder(BORDER_COLOR, 6));
        comboBox.setPreferredSize(new Dimension(120, 32));

        return comboBox;
    }

    /**
     * 创建现代化样式表格
     */
    public static JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);

        // 基本样式
        table.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        table.setRowHeight(45);
        table.setBackground(CARD_COLOR);
        table.setForeground(TEXT_PRIMARY);
        table.setSelectionBackground(SELECTED_COLOR);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER_COLOR);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        // 表头样式
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("微软雅黑", Font.BOLD, 14));
        header.setBackground(SECONDARY_COLOR);
        header.setForeground(TEXT_PRIMARY);
        header.setBorder(createRoundedBorder(BORDER_COLOR, 0));
        header.setPreferredSize(new Dimension(0, 50));

        // 自定义渲染器
        table.setDefaultRenderer(Object.class, new ModernTableCellRenderer());

        return table;
    }

    /**
     * 创建现代化样式滚动面板
     */
    public static JScrollPane createStyledScrollPane(JTable table, String title) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(CARD_COLOR);
        scrollPane.getViewport().setBackground(CARD_COLOR);
        scrollPane.setBorder(createTitledBorder(title));

        // 自定义滚动条
        customizeScrollBar(scrollPane);

        return scrollPane;
    }

    /**
     * 自定义滚动条样式
     */
    public static void customizeScrollBar(JScrollPane scrollPane) {
        // 垂直滚动条
        JScrollBar vScrollBar = scrollPane.getVerticalScrollBar();
        vScrollBar.setBackground(SECONDARY_COLOR);
        vScrollBar.setPreferredSize(new Dimension(12, 0));

        // 水平滚动条
        JScrollBar hScrollBar = scrollPane.getHorizontalScrollBar();
        hScrollBar.setBackground(SECONDARY_COLOR);
        hScrollBar.setPreferredSize(new Dimension(0, 12));
    }

    /**
     * 现代化表格单元格渲染器
     */
    public static class ModernTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (isSelected) {
                c.setBackground(SELECTED_COLOR);
                c.setForeground(TEXT_PRIMARY);
            } else {
                c.setBackground(row % 2 == 0 ? CARD_COLOR : HOVER_COLOR);
                c.setForeground(TEXT_PRIMARY);
            }

            setBorder(new EmptyBorder(8, 12, 8, 12));
            setFont(new Font("微软雅黑", Font.PLAIN, 13));

            return c;
        }
    }

    /**
     * 创建圆角边框
     */
    public static CompoundBorder createRoundedBorder(Color color, int radius) {
        return new CompoundBorder(
            new LineBorder(color, 1, true),
            new EmptyBorder(5, 8, 5, 8)
        );
    }

    /**
     * 创建阴影边框
     */
    public static EmptyBorder createShadowBorder() {
        return new EmptyBorder(2, 2, 8, 8);
    }

    /**
     * 创建标题边框
     */
    public static CompoundBorder createTitledBorder(String title) {
        return new CompoundBorder(
            BorderFactory.createTitledBorder(
                createRoundedBorder(BORDER_COLOR, 8),
                title,
                0,
                0,
                new Font("微软雅黑", Font.BOLD, 14),
                TEXT_PRIMARY
            ),
            new EmptyBorder(10, 15, 15, 15)
        );
    }

    /**
     * 显示现代化样式消息框
     */
    public static void showStyledMessage(Component parent, String message, String title, int messageType) {
        UIManager.put("OptionPane.background", CARD_COLOR);
        UIManager.put("Panel.background", CARD_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);

        JOptionPane.showMessageDialog(parent, message, title, messageType);
    }

    /**
     * 创建工具栏卡片容器
     */
    public static JPanel createToolbarCard(JPanel toolbar) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(new CompoundBorder(
            createRoundedBorder(BORDER_COLOR, 12),
            new EmptyBorder(15, 20, 15, 20)
        ));
        card.add(toolbar, BorderLayout.CENTER);

        // 添加阴影效果
        card.setBorder(new CompoundBorder(
            createShadowBorder(),
            card.getBorder()
        ));

        return card;
    }

    /**
     * 创建表格卡片容器
     */
    public static JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(new CompoundBorder(
            createShadowBorder(),
            new EmptyBorder(20, 20, 20, 20)
        ));
        return card;
    }
}

package com.vcampus.client.core.ui.shop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * 简单的搜索工具栏，提供对外事件与数据访问接口
 */
public class SearchToolbar extends JPanel {
    private final JTextField txtName;
    private final JTextField txtCategory;
    private final JButton btnSearchByName;
    private final JButton btnSearchByCategory;
    private final JButton btnBuy;

    // admin buttons
    private final JButton btnAdd;
    private final JButton btnEdit;
    private final JButton btnDelete;
    private final JSeparator adminSep;

    // view toggle
    private final JToggleButton btnViewToggle;

    // 字体和颜色常量
    private static final Font BUTTON_FONT = new Font("微软雅黑", Font.BOLD, 13);
    private static final Font LABEL_FONT = new Font("微软雅黑", Font.PLAIN, 12);

    // 按钮颜色主题
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);      // 蓝色 - 主要操作
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);       // 绿色 - 成功/购买
    private static final Color WARNING_COLOR = new Color(251, 146, 60);      // 橙色 - 警告/编辑
    private static final Color DANGER_COLOR = new Color(239, 68, 68);        // 红色 - 危险/删除
    private static final Color SECONDARY_COLOR = new Color(107, 114, 128);   // 灰色 - 次要操作
    private static final Color INFO_COLOR = new Color(14, 165, 233);         // 青色 - 信息/搜索

    /**
     * 创建现代化按钮
     */
    private JButton createModernButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 背景颜色
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(
                        Math.min(255, bgColor.getRed() + 20),
                        Math.min(255, bgColor.getGreen() + 20),
                        Math.min(255, bgColor.getBlue() + 20)
                    ));
                } else {
                    g2.setColor(bgColor);
                }
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
        button.setFont(BUTTON_FONT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(Math.max(80, text.length() * 12 + 20), 32));
        return button;
    }

    /**
     * 创建现代化切换按钮
     */
    private JToggleButton createModernToggleButton(String text, Color bgColor) {
        JToggleButton button = new JToggleButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color currentColor;
                if (isSelected()) {
                    currentColor = bgColor;
                } else {
                    currentColor = new Color(229, 231, 235); // 未选中时的灰色
                }

                if (getModel().isPressed()) {
                    currentColor = currentColor.darker();
                } else if (getModel().isRollover()) {
                    currentColor = new Color(
                        Math.min(255, currentColor.getRed() + 15),
                        Math.min(255, currentColor.getGreen() + 15),
                        Math.min(255, currentColor.getBlue() + 15)
                    );
                }

                g2.setColor(currentColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // 文字颜色根据选中状态调整
                g2.setColor(isSelected() ? Color.WHITE : new Color(75, 85, 99));
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
        button.setFont(BUTTON_FONT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(Math.max(80, text.length() * 12 + 20), 32));
        return button;
    }

    /**
     * 创建现代化文本框
     */
    private JTextField createModernTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        textField.setBackground(Color.WHITE);
        textField.setPreferredSize(new Dimension(textField.getPreferredSize().width, 32));
        return textField;
    }

    /**
     * 创建现代化标签
     */
    private JLabel createModernLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(new Color(75, 85, 99));
        return label;
    }

    public SearchToolbar() { this(false); }

    public SearchToolbar(boolean isAdmin) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 6));
        setBackground(new Color(248, 250, 252));

        // 使用现代化样式创建组件
        txtName = createModernTextField(12);
        btnSearchByName = createModernButton("按名称搜索", INFO_COLOR);
        txtCategory = createModernTextField(8);
        btnSearchByCategory = createModernButton("按分类搜索", INFO_COLOR);
        btnBuy = createModernButton("购买", SUCCESS_COLOR);

        btnAdd = createModernButton("添加", SUCCESS_COLOR);
        btnEdit = createModernButton("编辑", WARNING_COLOR);
        btnDelete = createModernButton("删除", DANGER_COLOR);
        adminSep = new JSeparator(SwingConstants.VERTICAL);
        adminSep.setPreferredSize(new Dimension(8, 24));

        btnViewToggle = createModernToggleButton("网格视图", PRIMARY_COLOR);
        btnViewToggle.setSelected(true);
        // 更新按钮文本以反映当前状态
        btnViewToggle.addActionListener(e -> {
            btnViewToggle.setText(btnViewToggle.isSelected() ? "网格视图" : "表格视图");
            btnViewToggle.repaint();
        });
        // 仅管理员可见视图切换：普通学生/教职工界面不显示切换按钮，并默认卡片（网格）视图
        btnViewToggle.setVisible(isAdmin);

        add(createModernLabel("名称:"));
        add(txtName);
        add(btnSearchByName);
        add(createModernLabel("分类:"));
        add(txtCategory);
        add(btnSearchByCategory);
        // 仅在管理员模式下显示"购买"按钮；普通学生界面移除该按钮
        if (isAdmin) add(btnBuy);

        // 视图切换按钮不再默认放在此处，允许父容器将其移动到更合适的位置（例如右下角）

        // 始终添加 admin 组件，但通过 setAdminMode 控制可见性
        add(adminSep);
        add(btnAdd);
        add(btnEdit);
        add(btnDelete);
        setAdminMode(isAdmin);
    }

    // 供外部布局使用：返回视图切换按钮，以便父容器可放置到右下角等位置
    public JToggleButton getViewToggleButton() { return btnViewToggle.isVisible() ? btnViewToggle : null; }

    public void addSearchByNameListener(ActionListener l) { btnSearchByName.addActionListener(l); }
    public void addSearchByCategoryListener(ActionListener l) { btnSearchByCategory.addActionListener(l); }
    public void addBuyListener(ActionListener l) { btnBuy.addActionListener(l); }

    public void addAddListener(ActionListener l) { btnAdd.addActionListener(l); }
    public void addEditListener(ActionListener l) { btnEdit.addActionListener(l); }
    public void addDeleteListener(ActionListener l) { btnDelete.addActionListener(l); }

    public void addViewToggleListener(ActionListener l) { btnViewToggle.addActionListener(l); }
    // 保留旧接口以兼容现有代码调用；工具栏中已移除可见的“我的订单”按钮，因此此方法为 no-op
    public void addMyOrdersListener(ActionListener l) { /* no visible button - kept for compatibility */ }
    // 非管理员始终返回网格视图
    public boolean isGridSelected() { return btnViewToggle.isVisible() ? btnViewToggle.isSelected() : true; }
    public void setViewSelected(boolean grid) { if (btnViewToggle.isVisible()) { btnViewToggle.setSelected(grid); btnViewToggle.setText(grid ? "网格视图" : "表格视图"); } }

    // 运行时开启/关闭管理员控件
    public void setAdminMode(boolean admin) {
        adminSep.setVisible(admin);
        btnAdd.setVisible(admin);
        btnEdit.setVisible(admin);
        btnDelete.setVisible(admin);
        revalidate();
        repaint();
    }

    public String getSearchName() { return txtName.getText().trim(); }
    public String getCategory() { return txtCategory.getText().trim(); }
}

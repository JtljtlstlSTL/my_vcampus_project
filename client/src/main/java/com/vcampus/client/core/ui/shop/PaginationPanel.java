package com.vcampus.client.core.ui.shop;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class PaginationPanel extends JPanel {
    private final JButton btnPrev;
    private final JButton btnNext;
    private final JLabel lblInfo;
    private final JComboBox<Integer> cmbPageSize;

    // 按钮颜色和字体常量
    private static final Font BUTTON_FONT = new Font("微软雅黑", Font.BOLD, 12);
    private static final Font LABEL_FONT = new Font("微软雅黑", Font.PLAIN, 12);
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);      // 蓝色
    private static final Color DISABLED_COLOR = new Color(156, 163, 175);    // 禁用状态灰色

    /**
     * 创建现代化分页按钮
     */
    private JButton createPaginationButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color bgColor;
                if (!isEnabled()) {
                    bgColor = DISABLED_COLOR;
                } else if (getModel().isPressed()) {
                    bgColor = PRIMARY_COLOR.darker();
                } else if (getModel().isRollover()) {
                    bgColor = new Color(
                        Math.min(255, PRIMARY_COLOR.getRed() + 20),
                        Math.min(255, PRIMARY_COLOR.getGreen() + 20),
                        Math.min(255, PRIMARY_COLOR.getBlue() + 20)
                    );
                } else {
                    bgColor = PRIMARY_COLOR;
                }

                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);

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
        // 使用更紧凑的方形尺寸以配合上下箭头样式
        button.setPreferredSize(new Dimension(36, 28));
        return button;
    }

    /**
     * 创建现代化下拉框
     */
    private JComboBox<Integer> createModernComboBox(Integer[] items, int selected) {
        JComboBox<Integer> comboBox = new JComboBox<>(items);
        comboBox.setSelectedItem(selected);
        comboBox.setFont(LABEL_FONT);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        comboBox.setBackground(Color.WHITE);
        comboBox.setPreferredSize(new Dimension(100, 28)); // 继续增加宽度从80到100
        return comboBox;
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

    public PaginationPanel(int initialPageSize) {
        setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        setOpaque(false);

        cmbPageSize = createModernComboBox(new Integer[]{5,10,15,20,50}, initialPageSize);
        // 使用上/下箭头符号替代文字，逻辑保持不变
        btnPrev = createPaginationButton("▲");
        btnNext = createPaginationButton("▼");
        lblInfo = createModernLabel("第 0 / 0 页");

        add(createModernLabel("每页"));
        add(cmbPageSize);
        add(lblInfo);
        add(btnPrev);
        add(btnNext);

        // 默认状态
        currentPage = 1;
        totalItems = 0;
        totalPages = 0;

        // 绑定按钮行为
        // 保留原有行为，仅添加友好提示
        btnPrev.setToolTipText("上一页");
        btnNext.setToolTipText("下一页");
        btnPrev.addActionListener(e -> setCurrentPage(Math.max(1, currentPage - 1)));
        btnNext.addActionListener(e -> setCurrentPage(Math.min(totalPages == 0 ? 1 : totalPages, currentPage + 1)));
        cmbPageSize.addActionListener(e -> {
            // 首先通知外部注册的 pageSize listeners，允许外部先更新本地 pageSize
            for (java.awt.event.ActionListener l : pageSizeListeners) {
                try { l.actionPerformed(e); } catch (Exception ignored) {}
            }
            // 然后重新计算页数并回到第1页
            recomputePagesAndNotify();
        });
    }

    // 新增：分页状态
    private int currentPage;
    private int totalItems;
    private int totalPages;
    private java.util.List<java.util.function.IntConsumer> pageChangeListeners = new java.util.ArrayList<>();
    // page size change listeners (ActionListener) invoked when user changes page size
    private java.util.List<java.awt.event.ActionListener> pageSizeListeners = new java.util.ArrayList<>();

    private void recomputePagesAndNotify() {
        int pageSize = getPageSize();
        totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) pageSize));
        setCurrentPage(1);
    }

    private void setCurrentPage(int page) {
        int pageSize = getPageSize();
        totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) pageSize));
        int p = Math.max(1, Math.min(totalPages, page));
        if (p != currentPage) currentPage = p;
        setPageInfo(currentPage, totalPages);
        // notify
        for (java.util.function.IntConsumer c : pageChangeListeners) {
            try { c.accept(currentPage); } catch (Exception ignored) {}
        }
    }

    // 供外部设置总条目数，自动计算总页数
    public void setTotal(int totalItems) {
        this.totalItems = Math.max(0, totalItems);
        int pageSize = getPageSize();
        this.totalPages = Math.max(1, (int) Math.ceil(this.totalItems / (double) pageSize));
        // 保持当前页在合法范围
        if (currentPage < 1) currentPage = 1;
        if (currentPage > totalPages) currentPage = totalPages;
        setPageInfo(currentPage, totalPages);
    }

    public void addPageChangeListener(java.util.function.IntConsumer listener) {
        if (listener == null) return;
        pageChangeListeners.add(listener);
    }

    public void addPrevListener(ActionListener l) { btnPrev.addActionListener(l); }
    public void addNextListener(ActionListener l) { btnNext.addActionListener(l); }
    public void addPageSizeListener(ActionListener l) { cmbPageSize.addActionListener(l); }

    // replace previous behavior: register to internal list so PaginationPanel can control invocation order
    public void addPageSizeListenerInternal(ActionListener l) { if (l != null) pageSizeListeners.add(l); }

    public int getPageSize() { return (Integer)cmbPageSize.getSelectedItem(); }
    public void setPageInfo(int current, int totalPages) {
        int cur = Math.max(0, current);
        int tot = Math.max(0, totalPages);
        lblInfo.setText(String.format("第 %d / %d 页", cur, tot));
        btnPrev.setEnabled(cur > 1);
        btnNext.setEnabled(cur < tot);
    }
}

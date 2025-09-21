package com.vcampus.client.core.ui.AiChatAssistant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * AI助手悬浮球
 */
public class AiAssistantFloatBall extends JButton {
    private JFrame parentFrame;
    private JDialog dialog;

    public AiAssistantFloatBall(JFrame parentFrame) {
        super("AI");
        this.parentFrame = parentFrame;
        setFont(new Font("微软雅黑", Font.BOLD, 16));
            Color normalColor = new Color(33, 150, 243);
            Color hoverColor = new Color(30, 180, 255);
            setBackground(normalColor);
        setForeground(Color.WHITE);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(true);
        setPreferredSize(new Dimension(48, 48));
        setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder());

            // 鼠标悬停变色
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(hoverColor);
                    repaint();
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(normalColor);
                    repaint();
                }
            });
        // 圆形外观
        setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, c.getWidth(), c.getHeight());
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                int x = (c.getWidth() - fm.stringWidth(text)) / 2;
                int y = (c.getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(text, x, y);
                g2.dispose();
            }
        });

        // 拖动功能，区分拖动和点击
        final boolean[] isDragging = {false};
        addMouseListener(new MouseAdapter() {
            Point offset;
            @Override
            public void mousePressed(MouseEvent e) {
                offset = e.getPoint();
                isDragging[0] = false;
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                // 拖动结束不弹窗
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point p = getLocation();
                setLocation(p.x + e.getX() - 24, p.y + e.getY() - 24);
                isDragging[0] = true;
            }
        });

        // 只有未拖动时才弹出AI助手对话框
        addActionListener(e -> {
            if (isDragging[0]) return;
            dialog = new JDialog(parentFrame, "AI助手对话", false);
            // 移除对话框装饰（标题栏及右上角的关闭按钮）
            dialog.setUndecorated(true);
            dialog.setSize(400, 500);
            dialog.setLocationRelativeTo(parentFrame);
            dialog.setLayout(new BorderLayout());
            dialog.add(new AiChatAssistantPanel(), BorderLayout.CENTER);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    setVisible(true);
                }
            });
            setVisible(false);
            dialog.setVisible(true);
        });
    }
}

package com.vcampus.client.core.ui.login;

import com.vcampus.client.core.util.FontManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;

public class createLeftPanel {

    // 自定义圆点按钮
    static class DotButton extends JButton {
        private boolean selected;

        public DotButton() {
            setPreferredSize(new Dimension(14, 14));
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
        }

        public void setSelectedDot(boolean selected) {
            this.selected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(selected ? Color.ORANGE : Color.LIGHT_GRAY);
            g2.fillOval(2, 2, 10, 10);
            g2.dispose();
        }
    }

    static JPanel createLeftPanel(LoginFrame loginFrame) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // 图片资源路径
        String[] imagePaths = {
            "/figures/login_spring.jpg",
            "/figures/login_summer.jpg",
            "/figures/login_autumn.jpg",
            "/figures/login_winter.jpg"
        };
        int imageCount = imagePaths.length;

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);

        DotButton[] dotButtons = new DotButton[imageCount];

        // 当前图片索引
        final int[] currentIndex = {0};

        // -------------------- 半透明背景条 --------------------
        JPanel dotsBackground = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8)) {
            private float alpha = 0.3f; // 初始透明度
            private float targetAlpha = 0.3f;

            {
                setOpaque(false);

                // 鼠标悬停事件：改变目标透明度
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        targetAlpha = 0.7f; // 悬停变更明显
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        targetAlpha = 0.3f; // 离开恢复
                    }
                });

                // 动画定时器：每40ms逐渐逼近 targetAlpha
                new Timer(40, e -> {
                    if (Math.abs(alpha - targetAlpha) > 0.02f) {
                        alpha += (targetAlpha - alpha) * 0.2f;
                        repaint();
                    }
                }).start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, (int)(alpha * 255)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        dotsBackground.setPreferredSize(new Dimension(400, 40));

        for (int i = 0; i < imageCount; i++) {
            DotButton btn = new DotButton();
            dotButtons[i] = btn;
            dotsBackground.add(btn);
        }

        // 设置图片和高亮点
        Runnable updateImageAndDots = () -> {
            try {
                imageLabel.setIcon(new ImageIcon(loginFrame.getClass().getResource(imagePaths[currentIndex[0]])));
                imageLabel.setText("");
            } catch (Exception e) {
                imageLabel.setIcon(null);
                imageLabel.setText("VCampus");
                imageLabel.setFont(new Font("微软雅黑", Font.BOLD, 48));
                imageLabel.setForeground(new Color(255, 127, 80));
            }
            for (int i = 0; i < imageCount; i++) {
                dotButtons[i].setSelectedDot(i == currentIndex[0]);
            }
        };

        // 点的事件
        for (int i = 0; i < imageCount; i++) {
            final int idx = i;
            dotButtons[i].addActionListener(e -> {
                currentIndex[0] = idx;
                updateImageAndDots.run();
            });
        }

        // 定时器轮播
        Timer timer = new Timer(2500, e -> {
            currentIndex[0] = (currentIndex[0] + 1) % imageCount;
            updateImageAndDots.run();
        });
        timer.start();

        for (DotButton btn : dotButtons) {
            btn.addActionListener(e -> timer.restart());
        }

        // -------------------- 绝对定位叠放 --------------------
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);

        imageLabel.setBounds(0, 0, 460, 400);
        dotsBackground.setBounds(0, 360, 460, 40); 

        layeredPane.add(imageLabel, JLayeredPane.DEFAULT_LAYER);
        layeredPane.add(dotsBackground, JLayeredPane.PALETTE_LAYER);

        panel.add(layeredPane, BorderLayout.CENTER);

        updateImageAndDots.run();

        return panel;
    }
}

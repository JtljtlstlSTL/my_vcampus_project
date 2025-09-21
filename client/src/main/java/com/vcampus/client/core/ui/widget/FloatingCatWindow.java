package com.vcampus.client.core.ui.widget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.vcampus.client.core.ui.component.SvgButton;

/**
 * 悬浮猫窗口：独立于主窗口之外的 JWindow，点击后尝试触发目标窗口的 toggleAIPanel() 方法或 btnAI 按钮点击。
 */
public class FloatingCatWindow extends JWindow {

    private final Window target;
    private final int size = 80;
    private Point dragOffset = null;

    public FloatingCatWindow(Window target) {
        super();
        this.target = target;
        initUI();
        bindToTarget();
    }

    private void initUI() {
        setSize(size, size);
        setAlwaysOnTop(true);
        // 不抢占焦点，避免遮挡主窗口的交互
        setFocusableWindowState(false);
        setBackground(new Color(0,0,0,0)); // 透明背景

        // 使用 SvgButton 渲染 SVG 图标，兼容项目内 SvgButton 实现
        SvgButton catBtn = new SvgButton("/figures/cat.svg");
        catBtn.setPreferredSize(new Dimension(size, size));
        catBtn.setContentAreaFilled(false);
        catBtn.setBorderPainted(false);
        catBtn.setFocusPainted(false);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(catBtn, BorderLayout.CENTER);

        // 点击触发目标窗口的 AI 行为
        catBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    triggerTargetAI();
                }
                // 移除右键隐藏功能，AI头像无法通过右击消失
            }

            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragOffset = null;
            }
        });

        // 拖动移动悬浮窗
        catBtn.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragOffset != null) {
                    Point p = e.getLocationOnScreen();
                    setLocation(p.x - dragOffset.x, p.y - dragOffset.y);
                }
            }
        });
    }

    private void bindToTarget() {
        if (target == null) return;
        // 初始放置在目标窗口的右下角
        updatePositionRelativeToTarget();

        // 监听目标窗口移动/调整大小，同步位置
        target.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                updatePositionRelativeToTarget();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                updatePositionRelativeToTarget();
            }
        });

        // 目标关闭时自动销毁悬浮窗
        if (target instanceof Window) {
            ((Window) target).addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        setVisible(false);
                        dispose();
                    });
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    SwingUtilities.invokeLater(() -> {
                        setVisible(false);
                        dispose();
                    });
                }

                @Override
                public void windowIconified(WindowEvent e) {
                    // 主窗口最小化时隐藏悬浮窗
                    SwingUtilities.invokeLater(() -> {
                        setVisible(false);
                    });
                }

                @Override
                public void windowDeiconified(WindowEvent e) {
                    // 主窗口恢复时显示悬浮窗
                    SwingUtilities.invokeLater(() -> {
                        setVisible(true);
                        toFront();
                    });
                }
            });
        }
    }

    private void updatePositionRelativeToTarget() {
        try {
            int margin = 20;
            if (target != null && target.isVisible()) {
                Point p = target.getLocationOnScreen();
                int tx = p.x;
                int ty = p.y;
                int tw = target.getWidth();
                int th = target.getHeight();
                int x = tx + Math.max(0, tw - size - margin);
                int y = ty + Math.max(0, th - size - margin);
                setLocation(x, y);
                return;
            }
            // 回退：将悬浮窗固定在屏幕右下角（不依赖主窗口），便于调试与确认可见
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            int x = Math.max(0, screen.width - size - margin);
            int y = Math.max(0, screen.height - size - margin - getTaskbarHeight());
            setLocation(x, y);
        } catch (IllegalComponentStateException ex) {
            // 目标或屏幕信息不可用，忽略
        }
    }

    // 大多数平台任务栏高度不可直接获取；这里尝试通过可用屏幕尺寸计算
    private int getTaskbarHeight() {
        Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        return insets == null ? 0 : insets.bottom;
    }

    public void showFloating() {
        updatePositionRelativeToTarget();
        // 显示并尝试置顶，但不抢焦点
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            toFront();
        });
    }

    private void triggerTargetAI() {
        if (target == null) return;
        try {
            // 尝试调用 toggleAIPanel 方法
            Method m = null;
            Class<?> cls = target.getClass();
            while (cls != null) {
                try {
                    m = cls.getDeclaredMethod("toggleAIPanel");
                    break;
                } catch (NoSuchMethodException ignored) {
                    cls = cls.getSuperclass();
                }
            }
            if (m != null) {
                m.setAccessible(true);
                m.invoke(target);
                return;
            }

            // 如果没有该方法，尝试查找 btnAI 字段并模拟点击
            cls = target.getClass();
            Field f = null;
            while (cls != null) {
                try {
                    f = cls.getDeclaredField("btnAI");
                    break;
                } catch (NoSuchFieldException ignored) {
                    cls = cls.getSuperclass();
                }
            }
            if (f != null) {
                f.setAccessible(true);
                Object btn = f.get(target);
                if (btn instanceof Component) {
                    if (btn instanceof JButton) {
                        ((JButton) btn).doClick();
                    } else {
                        // 尝试触发鼠标事件
                        ((Component) btn).dispatchEvent(
                                new MouseEvent((Component) btn, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 10, 10, 1, false));
                    }
                }
                return;
            }

            // 最后回退：将目标窗口置为前台并闪烁
            target.toFront();
            target.requestFocus();
        } catch (Exception e) {
            // 忽略错误，避免影响主界面
            e.printStackTrace();
        }
    }
}

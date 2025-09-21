package com.vcampus.client.core.ui.admin;

import com.vcampus.client.core.net.NettyClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 简单的功能选择面板，嵌入到主界面中。
 */
public class FunctionSelectionPanel extends JPanel {
    private final NettyClient nettyClient;
    private final Map<String, Object> userData;
    private final Object parentFrame; // AdminFrame

    public FunctionSelectionPanel(NettyClient nettyClient, Map<String, Object> userData, Object parentFrame) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        this.parentFrame = parentFrame;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        // 使用 2 行 3 列布局以容纳更多功能按钮
        JPanel grid = new JPanel(new GridLayout(2, 3, 20, 20));
        grid.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        grid.add(createFeatureButton("个人信息", e -> invokeShowModule("profile")));
        grid.add(createFeatureButton("用户管理", e -> invokeShowModule("user")));
        grid.add(createFeatureButton("学籍管理", e -> invokeShowModule("system")));
        grid.add(createFeatureButton("数据管理", e -> invokeShowModule("data")));
        grid.add(createFeatureButton("商城管理", e -> invokeShowModule("shop")));
        // 仅当父容器不是 AdminFrame（管理员界面）时显示校园卡入口
        try {
            if (!(parentFrame instanceof AdminFrame)) {
                grid.add(createFeatureButton("校园卡", e -> invokeShowModule("card")));
            }
        } catch (Exception ignored) {
            // 兼容性保护：若判断失败则不显示校园卡按钮
        }

        add(new JLabel("功能选择", SwingConstants.CENTER), BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
    }

    private JButton createFeatureButton(String text, java.util.function.Consumer<ActionEvent> onClick) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btn.addActionListener(e -> onClick.accept(e));
        return btn;
    }

    private void invokeShowModule(String module) {
        if (parentFrame == null) return;
        try {
            Method m = parentFrame.getClass().getMethod("showModule", String.class);
            m.invoke(parentFrame, module);
        } catch (NoSuchMethodException nsme) {
            // 尝试寻找 switchToModule
            try {
                Method m2 = parentFrame.getClass().getMethod("switchToModule", String.class);
                m2.invoke(parentFrame, module);
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            // ignore - 不阻塞UI
        }
    }
}

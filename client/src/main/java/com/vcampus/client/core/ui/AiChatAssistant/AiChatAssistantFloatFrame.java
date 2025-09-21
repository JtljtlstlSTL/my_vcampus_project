package com.vcampus.client.core.ui.AiChatAssistant;

import javax.swing.*;
import java.awt.*;

/**
 * 右下角常驻AI助手窗口
 */
public class AiChatAssistantFloatFrame extends JFrame {
    public AiChatAssistantFloatFrame() {
        // 移除窗口装饰（标题栏及右上角的关闭按钮）
        setUndecorated(true);

        setTitle("AI助手");
        setSize(350, 420);
        setAlwaysOnTop(true);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        add(new AiChatAssistantPanel(), BorderLayout.CENTER);
        // 定位到屏幕右下角
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width - getWidth() - 20;
        int y = screenSize.height - getHeight() - 60;
        setLocation(x, y);
    }
}

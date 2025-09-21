package com.vcampus.client.core.ui.login;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class animateMinimize {
    public static void animateMinimize(final LoginFrame loginFrame) {
        final int steps = 10;
        final int delay = 10;
        final Dimension originalSize = loginFrame.getSize();
        final Point originalLocation = loginFrame.getLocation();
        final int originalWidth = originalSize.width;
        final int originalHeight = originalSize.height;
        final int originalCenterX = originalLocation.x + originalWidth / 2;
        final int originalCenterY = originalLocation.y + originalHeight / 2;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int targetCenterY = screenSize.height - 20;

        Timer timer = new Timer(delay, null);
        timer.addActionListener(new ActionListener() {
            int step = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                step++;
                double progress = (double) step / steps;
                int newWidth = (int) (originalWidth * (1 - progress));
                int newHeight = (int) (originalHeight * (1 - progress));
                int currentCenterY = (int) (originalCenterY + (targetCenterY - originalCenterY) * progress);
                int newX = originalCenterX - newWidth / 2;
                int newY = currentCenterY - newHeight / 2;

                if (newWidth <= 0 || newHeight <= 0 || progress >= 1.0) {
                    timer.stop();
                    // 重要：先恢复窗口大小和位置，再最小化
                    loginFrame.setSize(originalSize);
                    loginFrame.setLocation(originalLocation);
                    // 确保窗口在任务栏中可见和可恢复
                    loginFrame.setExtendedState(JFrame.ICONIFIED);
                } else {
                    loginFrame.setSize(newWidth, newHeight);
                    loginFrame.setLocation(newX, newY);
                }
            }
        });
        timer.start();
    }
}

package com.vcampus.client.core.ui.login;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class animateClose {
    public static void animateClose(final LoginFrame loginFrame) {
        final int steps = 20;
        final int delay = 15;

        Timer timer = new Timer(delay, null);
        timer.addActionListener(new ActionListener() {
            int step = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                step++;
                float opacity = 1.0f - ((float) step / steps);
                if (opacity <= 0.0f) {
                    timer.stop();
                    System.exit(0);
                } else {
                    loginFrame.setOpacity(opacity);
                }
            }
        });
        timer.start();
    }
}

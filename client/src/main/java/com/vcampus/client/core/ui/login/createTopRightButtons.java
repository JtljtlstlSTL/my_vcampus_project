package com.vcampus.client.core.ui.login;

import com.vcampus.client.core.ui.component.SvgButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.vcampus.client.core.ui.component.SvgButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class createTopRightButtons {
    // Java
    static void createTopRightButtons(final LoginFrame loginFrame, JLayeredPane layeredPane) {
        SvgButton btnClose = loginFrame.getBtnClose();
        btnClose = new SvgButton("/figures/close.svg");
        btnClose.setBounds(690, 0, 20, 20);
        btnClose.addActionListener(e -> animateClose.animateClose(loginFrame));
        SvgButton finalBtnClose = btnClose;
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                finalBtnClose.setContentAreaFilled(true);
                finalBtnClose.setBackground(new Color(220, 53, 69));
            }
            public void mouseExited(MouseEvent e) {
                finalBtnClose.setContentAreaFilled(false);
            }
        });
        SvgButton btnMinimize = loginFrame.getBtnMinimize();
        btnMinimize = new SvgButton("/figures/minimize.svg");
        btnMinimize.setBounds(665, 0, 20, 20);
        btnMinimize.addActionListener(e -> animateMinimize.animateMinimize(loginFrame));
        SvgButton finalBtnMinimize = btnMinimize;
        btnMinimize.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                finalBtnMinimize.setContentAreaFilled(true);
                finalBtnMinimize.setBackground(new Color(230, 230, 230));
            }
            public void mouseExited(MouseEvent e) {
                finalBtnMinimize.setContentAreaFilled(false);
            }
        });

        layeredPane.add(btnClose, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(btnMinimize, JLayeredPane.PALETTE_LAYER);
    }
}

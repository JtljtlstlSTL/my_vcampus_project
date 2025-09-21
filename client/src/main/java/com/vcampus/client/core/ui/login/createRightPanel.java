package com.vcampus.client.core.ui.login;

import com.vcampus.client.core.util.FontManager;
import com.vcampus.client.core.ui.component.SvgButton;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class createRightPanel {
    static JPanel createRightPanel(final LoginFrame loginFrame) {
        // 渐变背景+卡片式圆角阴影+动画+玻璃拟态效果
        class AnimatedPanel extends JPanel {
            private float hue = 0f;
            public AnimatedPanel() {
                setLayout(new BorderLayout());
                new javax.swing.Timer(30, e -> {
                    hue += 0.002f;
                    if (hue > 1f) hue = 0f;
                    repaint();
                }).start();
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // 玻璃拟态渐变背景
                Color topColor = Color.getHSBColor(hue, 0.22f, 1f);
                Color bottomColor = Color.getHSBColor((hue+0.18f)%1f, 0.22f, 0.98f);
                GradientPaint gradient = new GradientPaint(
                        0, 0, topColor,
                        0, getHeight(), bottomColor
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(10, 10, getWidth()-20, getHeight()-20, 38, 38);
                // 玻璃拟态阴影
                g2d.setColor(new Color(180, 220, 240, 60));
                g2d.fillRoundRect(18, 18, getWidth()-36, getHeight()-36, 38, 38);
                // 轻微高光
                g2d.setColor(new Color(255,255,255,40));
                g2d.fillRoundRect(10, 10, getWidth()-20, 32, 38, 38);
                g2d.dispose();
            }
        }
        JPanel rightPanel = new AnimatedPanel();
        rightPanel.setOpaque(false);
        // 顶部内容区
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // 顶部Logo和标题
        JPanel portalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        portalPanel.setOpaque(false);
        JLabel portalIcon = new JLabel();
        ImageIcon icon = new ImageIcon(loginFrame.getClass().getResource("/figures/SEU.png"));
        Image img = icon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
        portalIcon.setIcon(new ImageIcon(img));
        portalIcon.setPreferredSize(new Dimension(28, 28));
        JLabel lblCnName = new JLabel("统一信息门户");
    lblCnName.setFont(new Font("微软雅黑", Font.BOLD, 26));
    lblCnName.setForeground(new Color(80, 180, 180));
        portalPanel.add(portalIcon);
        portalPanel.add(lblCnName);
        JLabel lblEgName = new JLabel("VCampus");
    lblEgName.setFont(new Font("微软雅黑", Font.BOLD, 13));
    lblEgName.setForeground(new Color(80, 180, 180, 120));
        // 使用LoginFrame中已有的字段
        JTextField usernameField = loginFrame.getTxtUsername();
        JPasswordField passwordField = loginFrame.getTxtPassword();
        
        // 设置字段样式
        usernameField.setColumns(15);
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        usernameField.setBackground(new Color(240, 248, 255));
    usernameField.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        usernameField.setForeground(Color.BLACK);
        
        passwordField.setColumns(15);
        passwordField.setEchoChar('*');
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        passwordField.setBackground(new Color(240, 248, 255));
    passwordField.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        passwordField.setForeground(Color.BLACK);
        
        // 添加占位符功能
        addPlaceholder(usernameField, "用户名");
        addPlaceholder(passwordField, "密码");
        
        // 眼睛按钮逻辑
        SvgButton btnTogglePwd = loginFrame.getBtnTogglePwd();
        btnTogglePwd.setBorderPainted(false);
        btnTogglePwd.setFocusPainted(false);
        btnTogglePwd.setContentAreaFilled(false);
        btnTogglePwd.setPreferredSize(new Dimension(20, 20));
        btnTogglePwd.setSvgIcon("/figures/eye_close.svg");
        
        btnTogglePwd.addActionListener(e -> {
            boolean isVisible = passwordField.getEchoChar() == 0;
            if (isVisible) {
                passwordField.setEchoChar('*');
                btnTogglePwd.setSvgIcon("/figures/eye_close.svg");
            } else {
                passwordField.setEchoChar((char) 0);
                btnTogglePwd.setSvgIcon("/figures/eye_open.svg");
            }
        });
        // 错误提示
        JLabel lblUsernameError = loginFrame.getLblUsernameError();
        lblUsernameError.setText("用户名不能为空！");
        lblUsernameError.setForeground(new Color(255, 0, 0));
        lblUsernameError.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        lblUsernameError.setVisible(false);
        lblUsernameError.setPreferredSize(new Dimension(150, 18));
        JLabel lblPasswordError = loginFrame.getLblPasswordError();
        lblPasswordError.setText("密码不能为空！");
        lblPasswordError.setForeground(new Color(255, 0, 0));
        lblPasswordError.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        lblPasswordError.setVisible(false);
        lblPasswordError.setPreferredSize(new Dimension(150, 18));
        // 高级风格按钮
        JButton btnLogin = loginFrame.getBtnLogin();
    btnLogin.setText("密码登录");
        btnLogin.setPreferredSize(new Dimension(180, 38));
        btnLogin.setFont(new Font("微软雅黑", Font.BOLD, 15));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setOpaque(false);
        // 圆角按钮美化
        btnLogin.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        btnLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnLogin.repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnLogin.repaint();
            }
        });
        btnLogin.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = b.getWidth(), h = b.getHeight();
                Color bg = b.getModel().isRollover() ? new Color(80, 180, 180) : new Color(120, 160, 220);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w, h, 22, 22);
                g2.setColor(new Color(120, 160, 220, 120));
                g2.drawRoundRect(0, 0, w-1, h-1, 22, 22);
                g2.dispose();
                super.paint(g, c);
            }
        });
        // 移除实时错误提示逻辑，只在登录时显示错误
        // 用户输入时隐藏错误提示
        usernameField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { 
                lblUsernameError.setVisible(false); 
            }
            public void removeUpdate(DocumentEvent e) {
                lblUsernameError.setVisible(false);
            }
            public void changedUpdate(DocumentEvent e) {}
        });
        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { 
                lblPasswordError.setVisible(false); 
            }
            public void removeUpdate(DocumentEvent e) {
                lblPasswordError.setVisible(false);
            }
            public void changedUpdate(DocumentEvent e) {}
        });
        // 顶部内容布局
        gbc.insets = new Insets(20, 5, 8, 5);
        loginFrame.addComponent(topPanel, portalPanel, 0, 0, 3, gbc);
        gbc.insets = new Insets(0, 5, 35, 5);
        loginFrame.addComponent(topPanel, lblEgName, 0, 1, 2, gbc);
        gbc.insets = new Insets(8, 5, 8, 2);
        loginFrame.addComponent(topPanel, usernameField, 0, 2, 3, gbc);
        gbc.insets = new Insets(0, 5, 0, 5);
        loginFrame.addComponent(topPanel, lblUsernameError, 0, 3, 3, gbc);
        
        gbc.insets = new Insets(8, 5, 8, 2);
        JPanel pwdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pwdPanel.setOpaque(false);
        pwdPanel.add(passwordField);
        pwdPanel.add(btnTogglePwd);
        loginFrame.addComponent(topPanel, pwdPanel, 0, 4, 3, gbc);
        
        gbc.insets = new Insets(0, 5, 10, 5);
        loginFrame.addComponent(topPanel, lblPasswordError, 0, 5, 3, gbc);
        rightPanel.add(topPanel, BorderLayout.CENTER);
        // 人脸识别登录按钮，风格与登录按钮一致
        JButton btnFaceLogin = loginFrame.getBtnFaceLogin();
        btnFaceLogin.setText("人脸识别登录");
        btnFaceLogin.setPreferredSize(new Dimension(180, 38));
        btnFaceLogin.setFont(new Font("微软雅黑", Font.BOLD, 15));
        btnFaceLogin.setForeground(Color.WHITE);
        btnFaceLogin.setFocusPainted(false);
        btnFaceLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFaceLogin.setContentAreaFilled(false);
        btnFaceLogin.setBorderPainted(false);
        btnFaceLogin.setOpaque(false);
        btnFaceLogin.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        btnFaceLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btnFaceLogin.repaint(); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btnFaceLogin.repaint(); }
        });
        // 浅绿色按钮美化
        btnFaceLogin.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                AbstractButton b = (AbstractButton) c;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = b.getWidth(), h = b.getHeight();
                // 深绿色，悬停更深
                Color bg = b.getModel().isRollover() ? new Color(60, 180, 140) : new Color(90, 200, 150);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w, h, 22, 22);
                g2.setColor(new Color(60, 180, 140, 120));
                g2.drawRoundRect(0, 0, w-1, h-1, 22, 22);
                g2.dispose();
                super.paint(g, c);
            }
        });
    // 按钮绝对定位，登录按钮在上，人脸识别在下
    JPanel bottomPanel = new JPanel(null); // 绝对布局
    bottomPanel.setOpaque(false);
    int btnWidth = 180, btnHeight = 38, gap = 16;
    int panelHeight = btnHeight * 2 + gap;
    bottomPanel.setPreferredSize(new Dimension(btnWidth + 0, panelHeight + 20));
    btnLogin.setBounds(50, 0, btnWidth, btnHeight);
    // 两个按钮整体向左移动20px
    btnLogin.setBounds(40, 0, btnWidth, btnHeight);
    btnFaceLogin.setBounds(-20, 10 + btnHeight + gap - 18, btnWidth, btnHeight);
    bottomPanel.add(btnLogin);
    bottomPanel.add(btnFaceLogin);
    rightPanel.add(bottomPanel, BorderLayout.SOUTH);
        return rightPanel;
    }
    
    // 添加占位符功能
    private static void addPlaceholder(JTextField textField, String placeholder) {
        // 对于密码字段，需要特殊处理
        if (textField instanceof JPasswordField) {
            JPasswordField passwordField = (JPasswordField) textField;
            passwordField.setText(placeholder);
            passwordField.setForeground(new Color(150, 150, 150));
            passwordField.setEchoChar((char) 0); // 暂时不显示星号，显示占位符
            
            passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    if (new String(passwordField.getPassword()).equals(placeholder)) {
                        passwordField.setText("");
                        passwordField.setEchoChar('*'); // 恢复星号显示
                        passwordField.setForeground(Color.BLACK);
                    }
                }
                
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    if (new String(passwordField.getPassword()).trim().isEmpty()) {
                        passwordField.setText(placeholder);
                        passwordField.setEchoChar((char) 0); // 不显示星号，显示占位符
                        passwordField.setForeground(new Color(150, 150, 150));
                    }
                }
            });
            
            // 监听文本变化
            passwordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    if (!new String(passwordField.getPassword()).equals(placeholder)) {
                        passwordField.setEchoChar('*'); // 确保输入时显示星号
                        passwordField.setForeground(Color.BLACK);
                    }
                }
                
                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    // 只在失去焦点时处理占位符，避免焦点冲突
                    // 这里不处理，让focusLost处理
                }
                
                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {}
            });
        } else {
            // 普通文本字段的处理
            textField.setText(placeholder);
            textField.setForeground(new Color(150, 150, 150));
            
            textField.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    if (textField.getText().equals(placeholder)) {
                        textField.setText("");
                        textField.setForeground(Color.BLACK);
                    }
                }
                
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    if (textField.getText().trim().isEmpty()) {
                        textField.setText(placeholder);
                        textField.setForeground(new Color(150, 150, 150));
                    }
                }
            });
            
            // 监听文本变化
            textField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    if (!textField.getText().equals(placeholder)) {
                        textField.setForeground(Color.BLACK);
                    }
                }
                
                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    // 只在失去焦点时处理占位符，避免焦点冲突
                    // 这里不处理，让focusLost处理
                }
                
                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {}
            });
        }
    }
}

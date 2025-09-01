package com.vcampus.client.ui;

import lombok.extern.slf4j.Slf4j;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;
import com.vcampus.client.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.client.ui.component.SvgButton;

@Slf4j
public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin, btnTogglePwd, btnClose, btnMinimize;
    private JLabel lblUsernameError, lblPasswordError;
    private boolean isPasswordVisible = false;
    private MainFrame mainFrame;
    private NettyClient nettyClient;
    private String currentUserCardNum;
    private String currentUserType, currentUserName;

    public LoginFrame() {
        setOpacity(1.0f);
        initializeUI();
        setupEventHandlers();
        nettyClient = new NettyClient("localhost", 8080);
        log.info("🔐 登录界面初始化完成");
    }

    private void initializeUI() {
        setTitle("VCampus - 统一登录门户");
        setSize(710, 400);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            setIconImage(new ImageIcon(getClass().getResource("/figures/logo.png")).getImage());
        } catch (Exception e) {
            log.warn("图标加载失败");
        }

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(710, 400));

        JSplitPane splitPane = new JSplitPane();
        splitPane.setBounds(0, 0, 710, 400);
        splitPane.setDividerLocation(450);
        splitPane.setEnabled(false);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);
        splitPane.setLeftComponent(createLeftPanel());
        splitPane.setRightComponent(createRightPanel());

        layeredPane.add(splitPane, JLayeredPane.DEFAULT_LAYER);
        createTopRightButtons(layeredPane);
        createPasswordToggleButton(layeredPane);
        setContentPane(layeredPane);
        setupWindowEffects();
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        JLabel imageLabel = new JLabel();
        try {
            imageLabel.setIcon(new ImageIcon(getClass().getResource("/figures/login_backgroud.jpg")));
        } catch (Exception e) {
            imageLabel.setText("VCampus");
            imageLabel.setFont(new Font("微软雅黑", Font.BOLD, 48));
            imageLabel.setForeground(new Color(255, 127, 80));
        }
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        panel.add(imageLabel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(248, 249, 250));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblCnName = new JLabel("统一登录门户");
        lblCnName.setFont(new Font("微软雅黑", Font.BOLD, 20));
        lblCnName.setForeground(new Color(255, 127, 80));

        JLabel lblEgName = new JLabel("VCampus");
        lblEgName.setFont(new Font("微软雅黑", Font.PLAIN, 8));
        lblEgName.setForeground(Color.GRAY);

        Color grayColor = new Color(80, 80, 80);

        JLabel lblUsername = new JLabel("用户名:");
        lblUsername.setFont(new Font("微软雅黑", Font.BOLD, 13));
        lblUsername.setForeground(grayColor);
        txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        JLabel lblPassword = new JLabel("密码:");
        lblPassword.setFont(new Font("微软雅黑", Font.BOLD, 13));
        lblPassword.setForeground(grayColor);
        txtPassword = new JPasswordField(15);
        txtPassword.setEchoChar('*');
        txtPassword.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        lblUsernameError = new JLabel("用户名不能为空！");
        lblUsernameError.setForeground(new Color(248, 249, 250)); // 初始与背景同色（看不见）
        lblUsernameError.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        lblUsernameError.setVisible(true); // 初始时显示，但颜色与背景一致
        // 设置固定宽度，确保错误信息能够完整显示
        lblUsernameError.setPreferredSize(new Dimension(150, lblUsernameError.getPreferredSize().height));

        lblPasswordError = new JLabel("密码不能为空！");
        lblPasswordError.setForeground(new Color(248, 249, 250)); // 初始与背景同色（看不见）
        lblPasswordError.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        lblPasswordError.setVisible(true); // 初始时显示，但颜色与背景一致
        // 固定宽度，基于最长文本"用户名或密码错误！"计算
        lblPasswordError.setPreferredSize(new Dimension(150, lblPasswordError.getPreferredSize().height));

        btnLogin = new JButton("登录");
        btnLogin.setPreferredSize(new Dimension(300, 40));
        btnLogin.setBackground(new Color(255, 127, 80));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);

        txtUsername.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // 移除自动显示错误信息的逻辑，只在登录时验证
            }
        });
        txtUsername.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                // 当用户输入字符时，将错误标签颜色变回背景色
                if (!txtUsername.getText().trim().isEmpty()) {
                    lblUsernameError.setForeground(new Color(248, 249, 250));
                }
            }
            public void removeUpdate(DocumentEvent e) {
                // 当用户删除字符时，如果文本框为空，显示错误提示
                if (txtUsername.getText().trim().isEmpty()) {
                    lblUsernameError.setText("用户名不能为空！");
                    lblUsernameError.setForeground(Color.RED);
                } else {
                    lblUsernameError.setForeground(new Color(248, 249, 250));
                }
            }
            public void changedUpdate(DocumentEvent e) {}
        });

        txtPassword.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // 移除自动显示错误信息的逻辑，只在登录时验证
            }
        });
        txtPassword.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                // 当用户输入字符时，将错误标签颜色变回背景色
                if (!new String(txtPassword.getPassword()).trim().isEmpty()) {
                    lblPasswordError.setForeground(new Color(248, 249, 250));
                }
            }
            public void removeUpdate(DocumentEvent e) {
                // 当用户删除字符时，如果密码框为空，显示错误提示
                if (new String(txtPassword.getPassword()).trim().isEmpty()) {
                    lblPasswordError.setText("密码不能为空！");
                    lblPasswordError.setForeground(Color.RED);
                } else {
                    lblPasswordError.setForeground(new Color(248, 249, 250));
                }
            }
            public void changedUpdate(DocumentEvent e) {}
        });

        gbc.insets = new Insets(20, 5, 8, 5);
        addComponent(rightPanel, lblCnName, 0, 0, 3, gbc);
        gbc.insets = new Insets(0, 5, 35, 5);
        addComponent(rightPanel, lblEgName, 0, 1, 2, gbc);

        gbc.insets = new Insets(8, 5, 8, 2);
        addComponent(rightPanel, lblUsername, 0, 2, 1, gbc);
        gbc.insets = new Insets(8, 0, 8, 5);
        addComponent(rightPanel, txtUsername, 1, 2, 2, gbc);

        gbc.insets = new Insets(0, 5, 0, 5);
        addComponent(rightPanel, lblUsernameError, 1, 3, 2, gbc);

        gbc.insets = new Insets(0, 5, 8, 2);
        addComponent(rightPanel, lblPassword, 0, 4, 1, gbc);
        gbc.insets = new Insets(0, 0, 8, 5);
        addComponent(rightPanel, txtPassword, 1, 4, 2, gbc);

        gbc.insets = new Insets(0, 5, 10, 5);
        addComponent(rightPanel, lblPasswordError, 1, 5, 2, gbc);

        gbc.insets = new Insets(25, 5, 15, 5);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        rightPanel.add(btnLogin, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        txtUsername.setBackground(new Color(220, 220, 220));
        txtUsername.setForeground(Color.BLACK);

        txtPassword.setBackground(new Color(220, 220, 220));
        txtPassword.setForeground(Color.BLACK);
        return rightPanel;
    }

    // Java
    private void createTopRightButtons(JLayeredPane layeredPane) {
        btnClose = new SvgButton("/figures/close.svg");
        btnClose.setBounds(690, 0, 20, 20);
        btnClose.addActionListener(e -> animateClose());
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnClose.setContentAreaFilled(true);
                btnClose.setBackground(new Color(220, 53, 69));
            }
            public void mouseExited(MouseEvent e) {
                btnClose.setContentAreaFilled(false);
            }
        });

        btnMinimize = new SvgButton("/figures/minimize.svg");
        btnMinimize.setBounds(665, 0, 20, 20);
        btnMinimize.addActionListener(e -> animateMinimize());
        btnMinimize.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnMinimize.setContentAreaFilled(true);
                btnMinimize.setBackground(new Color(230, 230, 230));
            }
            public void mouseExited(MouseEvent e) {
                btnMinimize.setContentAreaFilled(false);
            }
        });

        layeredPane.add(btnClose, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(btnMinimize, JLayeredPane.PALETTE_LAYER);
    }

    private void createPasswordToggleButton(JLayeredPane layeredPane) {
        btnTogglePwd = new SvgButton("/figures/eye_close.svg");
        btnTogglePwd.setBorderPainted(false);
        btnTogglePwd.setFocusPainted(false);
        btnTogglePwd.setContentAreaFilled(false);
        btnTogglePwd.setBounds(655, 220, 20, 20);

        btnTogglePwd.addActionListener(e -> {
            if (isPasswordVisible) {
                txtPassword.setEchoChar('*');
                ((SvgButton) btnTogglePwd).setSvgIcon("/figures/eye_close.svg");
                isPasswordVisible = false;
            } else {
                txtPassword.setEchoChar((char) 0);
                ((SvgButton) btnTogglePwd).setSvgIcon("/figures/eye_open.svg");
                isPasswordVisible = true;
            }
        });

        layeredPane.add(btnTogglePwd, JLayeredPane.PALETTE_LAYER);
    }

    private void setupWindowEffects() {
        SwingUtilities.invokeLater(() -> {
            try {
                setWindowRounded();
                addSubtleShadow();
            } catch (Exception e) {
                log.warn("设置窗口效果失败: {}", e.getMessage());
            }
        });
    }

    private void setWindowRounded() {
        setBackground(new Color(0, 0, 0, 0));
        setShape(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
    }

    private void addSubtleShadow() {
        JLayeredPane layeredPane = (JLayeredPane) getContentPane();
        JPanel shadowPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 4; i > 0; i--) {
                    int alpha = Math.max(5, 20 - i * 3);
                    g2d.setColor(new Color(0, 0, 0, alpha));
                    g2d.fillRoundRect(i, i, getWidth() - i * 2, getHeight() - i * 2, 15, 15);
                }
                g2d.dispose();
            }
        };
        shadowPanel.setOpaque(false);
        shadowPanel.setBounds(0, 0, getWidth(), getHeight());
        layeredPane.add(shadowPanel, JLayeredPane.FRAME_CONTENT_LAYER);
        layeredPane.moveToBack(shadowPanel);
    }

    private void setupEventHandlers() {
        btnLogin.addActionListener(e -> handleLogin());
        txtPassword.addActionListener(e -> handleLogin());
        addMouseListeners();

        addWindowStateListener(new WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                if (e.getNewState() == JFrame.NORMAL && e.getOldState() == JFrame.ICONIFIED) {
                    SwingUtilities.invokeLater(() -> {
                        setVisible(true);
                        toFront();
                        requestFocus();
                        repaint();
                    });
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeiconified(WindowEvent e) {
                SwingUtilities.invokeLater(() -> {
                    setVisible(true);
                    toFront();
                    requestFocus();
                    setState(JFrame.NORMAL);
                });
            }
        });
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        boolean usernameEmpty = username.isEmpty();
        boolean passwordEmpty = password.isEmpty();

        // 根据验证结果显示或隐藏错误标签，并设置颜色
        if (usernameEmpty) {
            lblUsernameError.setText("用户名不能为空！");
            lblUsernameError.setForeground(Color.RED);
            lblUsernameError.setVisible(true);
        } else {
            lblUsernameError.setForeground(new Color(248, 249, 250)); // 与背景同色
        }

        if (passwordEmpty) {
            lblPasswordError.setText("密码不能为空！");
            lblPasswordError.setForeground(Color.RED);
            lblPasswordError.setVisible(true);
        } else {
            lblPasswordError.setForeground(new Color(248, 249, 250)); // 与背景同色
        }

        if (usernameEmpty || passwordEmpty) {
            return;
        }

        String cardNum = username;

        log.info("🔐 用户尝试登录: cardNum={}", cardNum);

        try {
            Response response = performLoginWithNetty(cardNum, password);

            if (response != null && "SUCCESS".equals(response.getStatus())) {
                Map<String, Object> userData = (Map<String, Object>) response.getData();

                // 添加调试日志，显示服务器返回的完整数据
                log.info("🔍 服务器返回的用户数据: {}", userData);

                // 修复：服务器返回的是 primaryRole，不是 userType
                String userType = (String) userData.get("primaryRole");
                String userName = (String) userData.get("userName");

                log.info("🔍 提取的用户类型: '{}', 用户名: '{}'", userType, userName);

                storeUserInfo(cardNum, userType, userName);
                log.info("登录成功！用户: {} ({})", userName, userType);

                // 根据用户类型跳转到不同的界面
                openFrameBasedOnUserType(userData);
            } else {
                String errorMessage = response != null ? response.getMessage() : "无服务器响应";
                if (errorMessage.toLowerCase().contains("user not found") || errorMessage.contains("用户不存在")) {
                    lblUsernameError.setText("用户名不存在！");
                    lblUsernameError.setForeground(Color.RED);
                    lblPasswordError.setForeground(new Color(248, 249, 250)); // 与背景同色
                } else {
                    lblPasswordError.setText("用户名或密码错误");
                    lblPasswordError.setForeground(Color.RED);
                    lblUsernameError.setForeground(new Color(248, 249, 250)); // 与背景同色
                }
                log.warn("登录失败: {}", errorMessage);
            }
        } catch (Exception e) {
            log.error("登录过程中发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "登录失败: " + e.getMessage(),
                    "系统错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // 新增方法：根据用户类型打开对应的界面
    private void openFrameBasedOnUserType(Map<String, Object> userData) {
        try {
            // 修复：使用 primaryRole 字段
            String userType = (String) userData.get("primaryRole");
            String userName = (String) userData.get("userName");

            log.info("🔍 在 openFrameBasedOnUserType 中，用户类型: '{}', 用户名: '{}'", userType, userName);

            if ("student".equalsIgnoreCase(userType)) {
                // 打开学生界面
                StudentFrame studentFrame = new StudentFrame(nettyClient, userData);
                studentFrame.setVisible(true);
                log.info("✅ 学生界面打开成功，用户: {}", userName);
            } else if ("staff".equalsIgnoreCase(userType) || "teacher".equalsIgnoreCase(userType)) {
                // 打开教职工界面
                StaffFrame staffFrame = new StaffFrame(nettyClient, userData);
                staffFrame.setVisible(true);
                log.info("✅ 教职工界面打开成功，用户: {}", userName);
            } else if ("admin".equalsIgnoreCase(userType) || "manager".equalsIgnoreCase(userType)) {
                // 打开管理员界面
                AdminFrame adminFrame = new AdminFrame(nettyClient, userData);
                adminFrame.setVisible(true);
                log.info("✅ 管理员界面打开成功，用户: {}", userName);
            } else {
                // 默认打开原来的主界面
                log.warn("⚠️ 未知的用户类型: '{}'，打开默认主界面", userType);
                mainFrame = new MainFrame(currentUserCardNum, currentUserType, currentUserName);
                mainFrame.setVisible(true);
                log.info("✅ 主界面打开成功，用户: {} ({})", currentUserName, currentUserType);
            }

            // 隐藏登录窗口
            this.setVisible(false);
        } catch (Exception e) {
            log.error("💥 界面打开失败", e);
            JOptionPane.showMessageDialog(this,
                    "界面打开失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Response performLoginWithNetty(String cardNum, String password) {
        try {
            log.info("使用 Netty 客户端进行登录: cardNum={}", cardNum);

            if (!nettyClient.isConnected()) {
                log.info("正在连接到服务器...");
                nettyClient.connect().get(5, java.util.concurrent.TimeUnit.SECONDS);
            }

            Request loginRequest = new Request("auth/login")
                    .addParam("username", cardNum)
                    .addParam("password", password);

            Response response = nettyClient.sendRequest(loginRequest).get(10, java.util.concurrent.TimeUnit.SECONDS);

            if (response != null && "SUCCESS".equals(response.getStatus())) {
                Map<String, Object> userData = (Map<String, Object>) response.getData();
                String userType = (String) userData.get("userType");
                String userName = (String) userData.get("name");

                storeUserInfo(cardNum, userType, userName);
                log.info("Netty 登录成功: user={}, type={}", userName, userType);
            }
            return response;
        } catch (Exception e) {
            log.error("Netty 登录错误", e);
            return null;
        }
    }

    private void storeUserInfo(String cardNum, String userType, String userName) {
        this.currentUserCardNum = cardNum;
        this.currentUserType = userType;
        this.currentUserName = userName;
    }

    private void openMainFrame() {
        try {
            mainFrame = new MainFrame(currentUserCardNum, currentUserType, currentUserName);
            mainFrame.setVisible(true);
            this.setVisible(false);
            log.info("✅ 主界面打开成功，用户: {} ({})", currentUserName, currentUserType);
        } catch (Exception e) {
            log.error("💥 主界面打开失败", e);
            JOptionPane.showMessageDialog(this,
                    "主界面打开失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addMouseListeners() {
        // 可选：窗口拖动功能
    }

    private void animateMinimize() {
        final int steps = 10;
        final int delay = 10;
        final Dimension originalSize = getSize();
        final Point originalLocation = getLocation();
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
                    setSize(originalSize);
                    setLocation(originalLocation);
                    // 确保窗口在任务栏中可见和可恢复
                    setExtendedState(JFrame.ICONIFIED);
                } else {
                    setSize(newWidth, newHeight);
                    setLocation(newX, newY);
                }
            }
        });
        timer.start();
    }

    private void animateClose() {
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
                    setOpacity(opacity);
                }
            }
        });
        timer.start();
    }

    private void addComponent(JPanel panel, JComponent comp, int x, int y, int w, GridBagConstraints gbc) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        panel.add(comp, gbc);
    }
}
package com.vcampus.client.core.ui.login;

import com.vcampus.client.core.ui.admin.AdminFrame;
import com.vcampus.client.core.ui.admin.StaffFrame;
import com.vcampus.client.core.ui.student.StudentFrame;
// 暂时注释掉MainFrame的导入，因为还没找到这个类
// import com.vcampus.client.core.ui.MainFrame;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;
import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.client.core.ui.component.SvgButton;

@Slf4j
public class LoginFrame extends JFrame {

    @Getter
    private JTextField txtUsername = new JTextField(15);
    @Getter
    private JPasswordField txtPassword = new JPasswordField(15);
    @Getter
    private JButton btnLogin = new JButton("登录");
    @Getter
    private SvgButton btnTogglePwd = new SvgButton("/figures/eye_close.svg");
    @Getter
    private SvgButton btnClose = new SvgButton("/figures/close.svg");
    @Getter
    private SvgButton btnMinimize = new SvgButton("/figures/minimize.svg");
    @Getter
    private JLabel lblUsernameError = new JLabel("用户名不能为空！");
    @Getter
    private JLabel lblPasswordError = new JLabel("密码不能为空！");
    @Getter
    private boolean isPasswordVisible = false;
    // 暂时注释掉MainFrame相关代码，直到找到或创建该类
    // private MainFrame mainFrame = null;
    @Getter
    private NettyClient nettyClient = new NettyClient("localhost", 8080);
    @Getter
    private String currentUserCardNum = "";
    @Getter
    private String currentUserType = "";
    @Getter
    private String currentUserName = "";

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
        splitPane.setLeftComponent(createLeftPanel.createLeftPanel(this));
        splitPane.setRightComponent(createRightPanel.createRightPanel(this));

        layeredPane.add(splitPane, JLayeredPane.DEFAULT_LAYER);
        createTopRightButtons.createTopRightButtons(this, layeredPane);
        createPasswordToggleButton(layeredPane);
        setContentPane(layeredPane);
        setupWindowEffects();
    }


    private void createPasswordToggleButton(JLayeredPane layeredPane) {
        // 移除重复创建，直接使用已定义的 btnTogglePwd
        btnTogglePwd.setBorderPainted(false);
        btnTogglePwd.setFocusPainted(false);
        btnTogglePwd.setContentAreaFilled(false);
        btnTogglePwd.setBounds(655, 220, 20, 20);

        btnTogglePwd.addActionListener(e -> {
            if (isPasswordVisible) {
                txtPassword.setEchoChar('*');
                btnTogglePwd.setSvgIcon("/figures/eye_close.svg");
                isPasswordVisible = false;
            } else {
                txtPassword.setEchoChar((char) 0);
                btnTogglePwd.setSvgIcon("/figures/eye_open.svg");
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
                // mainFrame = new MainFrame(currentUserCardNum, currentUserType, currentUserName);
                // mainFrame.setVisible(true);
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
            // mainFrame = new MainFrame(currentUserCardNum, currentUserType, currentUserName);
            // mainFrame.setVisible(true);
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
    }
    public void addComponent(JPanel panel, JComponent comp, int x, int y, int w, GridBagConstraints gbc) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        panel.add(comp, gbc);
    }
}
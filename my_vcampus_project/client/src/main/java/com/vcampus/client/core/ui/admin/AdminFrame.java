package com.vcampus.client.core.ui.admin;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.client.core.ui.login.LoginFrame;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;

/**
 * 管理员用户主界面
 *
 * @author VCampus Team
 * @version 1.0
 */
public class AdminFrame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(AdminFrame.class);

    private JPanel mainPanel;
    private JLabel lblAdminName;
    private JLabel lblAdminId;
    private JButton btnLogout;
    private JButton btnChangePassword;
    private JButton btnFunctionSelect; // 功能选择按钮

    private NettyClient nettyClient;
    private Map<String, Object> userData;
    private Timer statusTimer;

    public AdminFrame(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;

        initUI();
        setupEventHandlers();

        logger.info("管理员主界面初始化完成: {}", userData.get("userName"));
    }

    private void initUI() {
        setTitle("VCampus - 管理员端");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 禁用默认关闭操作
        setResizable(false); // 固定窗口大小，禁用调整

        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/figures/logo.png"));
            setIconImage(logoIcon.getImage());
        } catch (Exception e) {
            logger.warn("自定义图标加载失败，使用默认图标");
        }

        // 创建主面板
        mainPanel = new JPanel(new BorderLayout());

        // 创建顶部面板
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 创建个人信息面板
        JPanel profilePanel = createProfilePanel();
        mainPanel.add(profilePanel, BorderLayout.CENTER);

        // 创建底部状态栏
        JPanel statusBar = createStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // 左侧显示欢迎信息和管理员信息
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userInfoPanel.setOpaque(false);

        JLabel lblWelcome = new JLabel("欢迎，");
        lblWelcome.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        lblAdminName = new JLabel(userData.get("userName").toString());
        lblAdminName.setFont(new Font("微软雅黑", Font.BOLD, 14));
        lblAdminName.setForeground(new Color(25, 133, 57)); // 使用绿色，突出管理员身份

        // 修复ClassCastException：添加类型检查来正确处理cardNum
        String cardNumText = "";
        Object cardNumObj = userData.get("cardNum");
        if (cardNumObj instanceof Number) {
            cardNumText = String.format("%.0f", ((Number)cardNumObj).doubleValue());
        } else if (cardNumObj instanceof String) {
            try {
                // 尝试将字符串解析为数字
                double cardNum = Double.parseDouble((String)cardNumObj);
                cardNumText = String.format("%.0f", cardNum);
            } catch (NumberFormatException e) {
                // 如果解析失败，直接使用原始字符串
                cardNumText = (String)cardNumObj;
            }
        } else if (cardNumObj != null) {
            // 其他类型，转换为字符串
            cardNumText = cardNumObj.toString();
        }
        
        lblAdminId = new JLabel("(" + cardNumText + ")");
        lblAdminId.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        lblAdminId.setForeground(Color.GRAY);

        userInfoPanel.add(lblWelcome);
        userInfoPanel.add(lblAdminName);
        userInfoPanel.add(lblAdminId);

        // 右侧显示功能选择、修改密码和退出登录按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        btnFunctionSelect = new JButton("功能选择");
        btnChangePassword = new JButton("修改密码");
        btnLogout = new JButton("退出登录");

        // 设置按钮大小一致
        Dimension buttonSize = new Dimension(100, 30);
        btnFunctionSelect.setPreferredSize(buttonSize);
        btnChangePassword.setPreferredSize(buttonSize);
        btnLogout.setPreferredSize(buttonSize);

        btnFunctionSelect.setFocusPainted(false);
        btnChangePassword.setFocusPainted(false);
        btnLogout.setFocusPainted(false);

        // 设置功能选择按钮的样式
        btnFunctionSelect.setBackground(new Color(25, 133, 57));
        btnFunctionSelect.setForeground(Color.WHITE);

        buttonPanel.add(btnFunctionSelect);
        buttonPanel.add(btnChangePassword);
        buttonPanel.add(btnLogout);

        panel.add(userInfoPanel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("个人信息");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 个人信息面板
        JPanel infoPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        infoPanel.add(new JLabel("管理员ID:"));
        // 添加类型检查以避免ClassCastException
        Object cardNumObj = userData.get("cardNum");
        String cardNumStr;
        if (cardNumObj instanceof Number) {
            cardNumStr = String.format("%.0f", ((Number) cardNumObj).doubleValue());
        } else if (cardNumObj instanceof String) {
            try {
                // 尝试将字符串解析为数字
                double cardNum = Double.parseDouble((String) cardNumObj);
                cardNumStr = String.format("%.0f", cardNum);
            } catch (NumberFormatException e) {
                // 如果解析失败，直接使用原始字符串
                cardNumStr = (String) cardNumObj;
            }
        } else {
            // 其他类型直接转换为字符串
            cardNumStr = cardNumObj != null ? cardNumObj.toString() : "";
        }
        infoPanel.add(new JLabel(cardNumStr));

        infoPanel.add(new JLabel("姓名:"));
        infoPanel.add(new JLabel(userData.get("userName").toString()));

        infoPanel.add(new JLabel("性别:"));
        infoPanel.add(new JLabel(userData.get("gender").toString()));

        infoPanel.add(new JLabel("电话:"));
        infoPanel.add(new JLabel(userData.get("phone") != null ? userData.get("phone").toString() : "未设置"));

        infoPanel.add(new JLabel("角色:"));
        infoPanel.add(new JLabel(translateRole(userData.get("primaryRole").toString())));

        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusBar.setBackground(new Color(240, 240, 240));

        JLabel statusLabel = new JLabel("就绪");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusBar.add(statusLabel, BorderLayout.WEST);

        JLabel timeLabel = new JLabel(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusBar.add(timeLabel, BorderLayout.EAST);

        // 创建定时器更新时间，并保存引用以便清理
        statusTimer = new Timer(1000, e -> {
            timeLabel.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        });
        statusTimer.start();

        return statusBar;
    }

    private void setupEventHandlers() {
        btnLogout.addActionListener(e -> handleLogout());
        btnChangePassword.addActionListener(e -> handleChangePassword());
        btnFunctionSelect.addActionListener(e -> handleFunctionSelect()); // 功能选择按钮事件

        // 窗口关闭时的确认
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(AdminFrame.this,
                        "确定要退出登录吗？", "确认", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    // 直接进行界面切换，避免复杂的处理流程
                    try {
                        // 立即停止定时器
                        if (statusTimer != null && statusTimer.isRunning()) {
                            statusTimer.stop();
                        }

                        // 立即关闭当前界面
                        AdminFrame.this.dispose();

                        // 立即打开新的登录界面
                        new LoginFrame().setVisible(true);

                        logger.info("通过窗口关闭事件退出登录完成");
                    } catch (Exception ex) {
                        logger.error("窗口关闭处理过程中发生错误", ex);
                        System.exit(0);
                    }
                }
            }
        });
    }

    private void handleLogout() {
        try {
            logger.info("开始退出登录流程: {}", userData.get("userName"));

            // 立即停止定时器
            if (statusTimer != null && statusTimer.isRunning()) {
                statusTimer.stop();
                logger.debug("状态栏定时器已停止");
            }

            // 立即清理资源
            cleanupResources();

            // 立即关闭当前界面
            this.dispose();

            // 立即打开新的登录界面
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);

            logger.info("退出登录完成，已返回登录界面");

            // 在后台线程中发送退出登录请求（不阻塞UI）
            new Thread(() -> {
                try {
                    Request logoutRequest = new Request("auth/logout");
                    Response response = nettyClient.sendRequest(logoutRequest).get(2, java.util.concurrent.TimeUnit.SECONDS);

                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        logger.info("用户退出登录请求成功: {}", userData.get("userName"));
                    } else {
                        logger.warn("用户退出登录请求失败: {}", response != null ? response.getMessage() : "未知错误");
                    }
                } catch (Exception e) {
                    logger.warn("退出登录请求处理异常: {}", e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            logger.error("退出登录过程中发生错误", e);
            // 即使出现错误也要确保界面能够切换
            try {
                if (statusTimer != null && statusTimer.isRunning()) {
                    statusTimer.stop();
                }
                this.dispose();
                new LoginFrame().setVisible(true);
            } catch (Exception ex) {
                logger.error("错误恢复过程中再次发生错误", ex);
                System.exit(0);
            }
        }
    }

    /**
     * 清理资源
     */
    private void cleanupResources() {
        try {
            // 停止定时器
            if (statusTimer != null && statusTimer.isRunning()) {
                statusTimer.stop();
                logger.debug("状态栏定时器已停止");
            }

            // 清理用户数据引用
            if (userData != null) {
                userData.clear();
                logger.debug("用户数据引用已清理");
            }

        } catch (Exception e) {
            logger.warn("清理资源时发生异常", e);
        }
    }

    private void handleChangePassword() {
        // 创建修改密码对话框
        JDialog dialog = new JDialog(this, "修改密码", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // 原密码输入
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("原密码:"), gbc);

        JPanel oldPasswordPanel = new JPanel(new BorderLayout());
        JPasswordField txtOldPassword = new JPasswordField(20);
        JButton btnToggleOldPassword = createPasswordToggleButton(txtOldPassword);
        oldPasswordPanel.add(txtOldPassword, BorderLayout.CENTER);
        oldPasswordPanel.add(btnToggleOldPassword, BorderLayout.EAST);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(oldPasswordPanel, gbc);

        // 新密码输入
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("新密码:"), gbc);

        JPanel newPasswordPanel = new JPanel(new BorderLayout());
        JPasswordField txtNewPassword = new JPasswordField(20);
        JButton btnToggleNewPassword = createPasswordToggleButton(txtNewPassword);
        newPasswordPanel.add(txtNewPassword, BorderLayout.CENTER);
        newPasswordPanel.add(btnToggleNewPassword, BorderLayout.EAST);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(newPasswordPanel, gbc);

        // 确认新密码输入
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("确认新密码:"), gbc);

        JPanel confirmPasswordPanel = new JPanel(new BorderLayout());
        JPasswordField txtConfirmPassword = new JPasswordField(20);
        JButton btnToggleConfirmPassword = createPasswordToggleButton(txtConfirmPassword);
        confirmPasswordPanel.add(txtConfirmPassword, BorderLayout.CENTER);
        confirmPasswordPanel.add(btnToggleConfirmPassword, BorderLayout.EAST);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(confirmPasswordPanel, gbc);

        // 密码强度指示器
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("密码强度:"), gbc);

        JLabel lblPasswordStrength = new JLabel("未输入");
        lblPasswordStrength.setFont(new Font("微软雅黑", Font.BOLD, 12));
        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(lblPasswordStrength, gbc);

        // 密码要求说明
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;

        JTextArea passwordRequirements = new JTextArea(
            "密码要求：\n" +
            "• 长度至少8个字符\n" +
            "• 必须包含以下至少两种类型的字符：\n" +
            "  - 数字 (0-9)\n" +
            "  - 大写字母 (A-Z)\n" +
            "  - 小写字母 (a-z)\n" +
            "  - 特殊符号 (!@#$%^&*等)"
        );
        passwordRequirements.setEditable(false);
        passwordRequirements.setOpaque(false);
        passwordRequirements.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        passwordRequirements.setForeground(Color.GRAY);

        formPanel.add(passwordRequirements, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnConfirm = new JButton("确认修改");
        JButton btnCancel = new JButton("取消");

        btnConfirm.setPreferredSize(new Dimension(100, 30));
        btnCancel.setPreferredSize(new Dimension(100, 30));

        buttonPanel.add(btnConfirm);
        buttonPanel.add(btnCancel);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);

        // 添加密码强度检测
        txtNewPassword.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updatePasswordStrength();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updatePasswordStrength();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updatePasswordStrength();
            }

            private void updatePasswordStrength() {
                String password = new String(txtNewPassword.getPassword());
                if (password.isEmpty()) {
                    lblPasswordStrength.setText("未输入");
                    lblPasswordStrength.setForeground(Color.GRAY);
                    return;
                }

                int score = calculatePasswordStrength(password);
                switch (score) {
                    case 0:
                    case 1:
                        lblPasswordStrength.setText("弱");
                        lblPasswordStrength.setForeground(Color.RED);
                        break;
                    case 2:
                        lblPasswordStrength.setText("中等");
                        lblPasswordStrength.setForeground(Color.ORANGE);
                        break;
                    case 3:
                        lblPasswordStrength.setText("强");
                        lblPasswordStrength.setForeground(new Color(0, 150, 0));
                        break;
                    case 4:
                        lblPasswordStrength.setText("很强");
                        lblPasswordStrength.setForeground(new Color(0, 100, 0));
                        break;
                }
            }
        });

        // 添加事件处理
        btnConfirm.addActionListener(e -> {
            String oldPassword = new String(txtOldPassword.getPassword());
            String newPassword = new String(txtNewPassword.getPassword());
            String confirmPassword = new String(txtConfirmPassword.getPassword());

            // 验证输入
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写所有密码字段！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(dialog, "新密码与确认密码不一致！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isPasswordValid(newPassword)) {
                JOptionPane.showMessageDialog(dialog, "新密码不符合强度要求！\n请确保密码长度至少8位，且包含至少两种不同类型的字符。", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 发送修改密码请求
            changePassword(oldPassword, newPassword, dialog);
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        // 显示对话框
        dialog.setVisible(true);
    }

    /**
     * 创建密码显示/隐藏切换按钮
     */
    private JButton createPasswordToggleButton(JPasswordField passwordField) {
        JButton toggleButton = new JButton();
        toggleButton.setPreferredSize(new Dimension(30, 30));
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setFocusPainted(false);

        // 尝试加载图标
        ImageIcon eyeOpenIcon = null;
        ImageIcon eyeCloseIcon = null;

        try {
            eyeOpenIcon = new ImageIcon(getClass().getResource("/figures/eye_open.png"));
            eyeCloseIcon = new ImageIcon(getClass().getResource("/figures/eye_close.png"));

            // 缩放图标到合适大小
            eyeOpenIcon = new ImageIcon(eyeOpenIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
            eyeCloseIcon = new ImageIcon(eyeCloseIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            logger.warn("密码显示图标加载失败，使用文本按钮");
        }

        // 设置初始状态（密码隐藏）
        final ImageIcon finalEyeOpenIcon = eyeOpenIcon;
        final ImageIcon finalEyeCloseIcon = eyeCloseIcon;

        if (finalEyeCloseIcon != null) {
            toggleButton.setIcon(finalEyeCloseIcon);
        } else {
            toggleButton.setText("显示");
            toggleButton.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        }

        toggleButton.addActionListener(e -> {
            if (passwordField.getEchoChar() == 0) {
                // 当前显示密码，切换为隐藏
                passwordField.setEchoChar('•');
                if (finalEyeCloseIcon != null) {
                    toggleButton.setIcon(finalEyeCloseIcon);
                } else {
                    toggleButton.setText("显示");
                }
            } else {
                // 当前隐藏密码，切换为显示
                passwordField.setEchoChar((char) 0);
                if (finalEyeOpenIcon != null) {
                    toggleButton.setIcon(finalEyeOpenIcon);
                } else {
                    toggleButton.setText("隐藏");
                }
            }
        });

        return toggleButton;
    }

    /**
     * 计算密码强度分数
     */
    private int calculatePasswordStrength(String password) {
        if (password.length() < 8) {
            return 0;
        }

        boolean hasDigit = false;
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }

        // 计算满足的条件数量
        int conditionsMet = 0;
        if (hasDigit) conditionsMet++;
        if (hasUpperCase) conditionsMet++;
        if (hasLowerCase) conditionsMet++;
        if (hasSpecialChar) conditionsMet++;

        return conditionsMet;
    }

    /**
     * 验证密码强度
     */
    private boolean isPasswordValid(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean hasDigit = false;
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }

        // 计算满足的条件数量
        int conditionsMet = 0;
        if (hasDigit) conditionsMet++;
        if (hasUpperCase) conditionsMet++;
        if (hasLowerCase) conditionsMet++;
        if (hasSpecialChar) conditionsMet++;

        return conditionsMet >= 2;
    }

    /**
     * 发送修改密码请求
     */
    private void changePassword(String oldPassword, String newPassword, JDialog dialog) {
        try {
            // 修复卡号格式问题 - 确保正确处理科学计数法
            Object cardNumObj = userData.get("cardNum");
            String cardNumStr;

            if (cardNumObj instanceof Number) {
                // 使用 BigDecimal 来避免科学计数法格式问题
                java.math.BigDecimal bd = new java.math.BigDecimal(cardNumObj.toString());
                cardNumStr = bd.toPlainString();
            } else {
                cardNumStr = cardNumObj.toString();
            }

            logger.info("发送密码修改请求，用户卡号: {}", cardNumStr);

            Request request = new Request("auth/changepassword")
                .addParam("cardNum", cardNumStr)
                .addParam("oldPassword", oldPassword)
                .addParam("newPassword", newPassword)
                .addParam("userType", "admin"); // 添加用户类型参数

            // 发送请求，增加超时时间
            Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);

            if (response != null && "SUCCESS".equals(response.getStatus())) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(dialog, "密码修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                });
                logger.info("用户 {} 密码修改成功", userData.get("userName"));
            } else {
                String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(dialog, "密码修改失败：" + errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                });
                logger.warn("用户 {} 密码修改失败: {}", userData.get("userName"), errorMsg);
            }

        } catch (java.util.concurrent.TimeoutException e) {
            logger.error("密码修改请求超时", e);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(dialog, "请求超时，请检查网络连接后重试", "超时错误", JOptionPane.ERROR_MESSAGE);
            });
        } catch (Exception e) {
            logger.error("修改密码过程中发生错误", e);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(dialog, "修改密码时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    /**
     * 将英文角色名转换为中文显示
     */
    private String translateRole(String englishRole) {
        if (englishRole == null) {
            return "未知";
        }
        switch (englishRole.toLowerCase()) {
            case "student":
                return "学生";
            case "staff":
                return "教职工";
            case "teacher":
                return "教师";
            case "admin":
                return "管理员";
            case "manager":
                return "管理员";
            default:
                return englishRole; // 如果没有匹配的，返回原文
        }
    }

    /**
     * 处理功能选择按钮点击事件
     */
    private void handleFunctionSelect() {
        try {
            logger.info("管理员 {} 点击功能选择按钮", userData.get("userName"));

            // 创建并显示SelectFrame界面
            SelectFrame selectFrame = new SelectFrame(nettyClient, userData, this);
            selectFrame.setVisible(true);

            logger.info("功能选择界面已打开");
        } catch (Exception e) {
            logger.error("打开功能选择界面时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "无法打开功能选择界面：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

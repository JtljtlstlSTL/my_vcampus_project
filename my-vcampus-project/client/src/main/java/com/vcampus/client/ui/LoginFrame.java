package com.vcampus.client.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 登录界面
 * 基于VirtualCampusSystem的LoginUI设计
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JCheckBox cboxRememberPwd;
    private JButton btnLogin;
    private JButton btnRegister;
    private JButton btnTogglePwd;
    private JButton btnClose;
    private JButton btnMinimize;
    
    private boolean isPasswordVisible = false;
    private MainFrame mainFrame;

    public LoginFrame() {
        initializeUI();
        setupEventHandlers();
        log.info("🔐 登录界面初始化完成");
    }

    /**
     * 初始化UI
     */
    private void initializeUI() {
        setTitle("VCampus - 统一登录门户");
        setSize(710, 400);
        setLocationRelativeTo(null); // 居中
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 设置自定义窗口图标
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/figures/logo.png"));
            setIconImage(logoIcon.getImage());
        } catch (Exception e) {
            log.warn("自定义图标加载失败，使用默认图标");
        }

        // 使用分层面板（JLayeredPane）来实现右上角按钮
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(710, 400));

        // 使用 JSplitPane 分割左右面板
        JSplitPane splitPane = new JSplitPane();
        splitPane.setBounds(0, 0, 710, 400);
        splitPane.setDividerLocation(450);   // 左右分割线位置
        splitPane.setEnabled(false);         // 禁止拖动分隔线
        splitPane.setDividerSize(1);         // 分割线宽度为1像素
        splitPane.setBorder(null);           // 去掉分割线边框

        // 左边面板：放图片
        JPanel leftPanel = createLeftPanel();

        // 右边面板：放表单
        JPanel rightPanel = createRightPanel();

        // 放入分隔面板
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);

        // 加到分层面板
        layeredPane.add(splitPane, JLayeredPane.DEFAULT_LAYER);

        // 创建右上角按钮
        createTopRightButtons(layeredPane);

        // 创建密码可见性切换按钮
        createPasswordToggleButton(layeredPane);

        setContentPane(layeredPane);
    }

    /**
     * 创建左侧图片面板
     */
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);

        JLabel imageLabel = new JLabel();
        try {
            // 加载背景图片
            ImageIcon icon = new ImageIcon(getClass().getResource("/figures/1.jpg"));
            imageLabel.setIcon(icon);
        } catch (Exception e) {
            log.warn("背景图片加载失败，使用文字替代");
            // 临时使用文字替代图片
            imageLabel.setText("VCampus");
            imageLabel.setFont(new Font("微软雅黑", Font.BOLD, 48));
            imageLabel.setForeground(new Color(255, 127, 80));
        }
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);

        leftPanel.add(imageLabel, BorderLayout.CENTER);
        return leftPanel;
    }

    /**
     * 创建右侧表单面板
     */
    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(248, 249, 250)); // 浅灰色背景

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        JLabel lblCnName = new JLabel("统一登录门户");
        lblCnName.setFont(new Font("微软雅黑", Font.BOLD, 20));
        lblCnName.setForeground(new Color(255, 127, 80));

        JLabel lblEgName = new JLabel("VCampus");
        lblEgName.setFont(new Font("微软雅黑", Font.PLAIN, 8));
        lblEgName.setForeground(Color.GRAY);

        // 用户名
        JLabel lblUsername = new JLabel("用户名:");
        txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 密码
        JLabel lblPassword = new JLabel("密码:");
        txtPassword = new JPasswordField(15);
        txtPassword.setEchoChar('*');
        txtPassword.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 记住密码
        cboxRememberPwd = new JCheckBox("记住密码");
        cboxRememberPwd.setSelected(true);

        // 按钮
        btnLogin = new JButton("登录");
        btnRegister = new JButton("注册");
        Dimension buttonSize = new Dimension(80, 30);
        btnRegister.setPreferredSize(buttonSize);
        btnLogin.setPreferredSize(buttonSize);

        // 设置按钮样式
        btnLogin.setBackground(new Color(255, 127, 80));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        
        btnRegister.setBackground(new Color(108, 117, 125));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setBorderPainted(false);
        btnRegister.setFocusPainted(false);

        // 添加控件到 rightPanel
        gbc.insets = new Insets(2, 5, 2, 5);
        addComponent(rightPanel, lblCnName, 0, 0, 3, gbc);
        addComponent(rightPanel, lblEgName, 0, 1, 2, gbc);
        addComponent(rightPanel, lblUsername, 0, 2, 1, gbc);
        addComponent(rightPanel, txtUsername, 1, 2, 2, gbc);

        gbc.insets = new Insets(5, 5, 5, 5);
        addComponent(rightPanel, lblPassword, 0, 3, 1, gbc);
        addComponent(rightPanel, txtPassword, 1, 3, 2, gbc);

        addComponent(rightPanel, cboxRememberPwd, 0, 4, 2, gbc);

        gbc.insets = new Insets(2, 5, 2, 5);
        addComponent(rightPanel, btnRegister, 0, 5, 1, gbc);
        addComponent(rightPanel, btnLogin, 2, 5, 1, gbc);

        return rightPanel;
    }

    /**
     * 创建右上角按钮
     */
    private void createTopRightButtons(JLayeredPane layeredPane) {
        // 创建关闭按钮
        try {
            ImageIcon closeIcon = new ImageIcon(getClass().getResource("/figures/close1.png"));
            btnClose = new JButton(closeIcon);
        } catch (Exception e) {
            log.warn("关闭按钮图标加载失败，使用文字替代");
            btnClose = new JButton("✕");
            btnClose.setFont(new Font("Arial", Font.BOLD, 16));
            btnClose.setForeground(Color.WHITE);
        }
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setBounds(690, 5, 20, 20);
        btnClose.addActionListener(e -> System.exit(0));

        // 创建最小化按钮
        try {
            ImageIcon minimizeIcon = new ImageIcon(getClass().getResource("/figures/minimize.png"));
            btnMinimize = new JButton(minimizeIcon);
        } catch (Exception e) {
            log.warn("最小化按钮图标加载失败，使用文字替代");
            btnMinimize = new JButton("−");
            btnMinimize.setFont(new Font("Arial", Font.BOLD, 16));
            btnMinimize.setForeground(Color.WHITE);
        }
        btnMinimize.setBorderPainted(false);
        btnMinimize.setFocusPainted(false);
        btnMinimize.setContentAreaFilled(false);
        btnMinimize.setBounds(665, 5, 20, 20);
        btnMinimize.addActionListener(e -> setState(JFrame.ICONIFIED));

        // 添加按钮到分层面板
        layeredPane.add(btnClose, JLayeredPane.PALETTE_LAYER);
        layeredPane.add(btnMinimize, JLayeredPane.PALETTE_LAYER);
    }

    /**
     * 创建密码可见性切换按钮
     */
    private void createPasswordToggleButton(JLayeredPane layeredPane) {
        try {
            ImageIcon eyeCloseIcon = new ImageIcon(getClass().getResource("/figures/eye_close.png"));
            btnTogglePwd = new JButton(eyeCloseIcon);
        } catch (Exception e) {
            log.warn("密码可见性按钮图标加载失败，使用文字替代");
            btnTogglePwd = new JButton("👁");
            btnTogglePwd.setFont(new Font("Arial", Font.PLAIN, 14));
        }
        btnTogglePwd.setBorderPainted(false);
        btnTogglePwd.setFocusPainted(false);
        btnTogglePwd.setContentAreaFilled(false);
        btnTogglePwd.setBounds(670, 195, 20, 20);

        // 点击事件：切换密码可见性
        btnTogglePwd.addActionListener(e -> {
            if (isPasswordVisible) {
                txtPassword.setEchoChar('*');
                try {
                    ImageIcon eyeCloseIcon = new ImageIcon(getClass().getResource("/figures/eye_close.png"));
                    btnTogglePwd.setIcon(eyeCloseIcon);
                } catch (Exception ex) {
                    btnTogglePwd.setText("👁");
                }
                isPasswordVisible = false;
            } else {
                txtPassword.setEchoChar((char) 0);
                try {
                    ImageIcon eyeOpenIcon = new ImageIcon(getClass().getResource("/figures/eye_open.png"));
                    btnTogglePwd.setIcon(eyeOpenIcon);
                } catch (Exception ex) {
                    btnTogglePwd.setText("🙈");
                }
                isPasswordVisible = true;
            }
        });

        // 添加到分层面板
        layeredPane.add(btnTogglePwd, JLayeredPane.PALETTE_LAYER);
    }

    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 登录按钮事件
        btnLogin.addActionListener(e -> handleLogin());

        // 注册按钮事件
        btnRegister.addActionListener(e -> handleRegister());

        // 回车键登录
        txtPassword.addActionListener(e -> handleLogin());

        // 窗口拖动事件
        addMouseListeners();
    }

    /**
     * 处理登录
     */
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "用户名和密码不能为空", 
                "登录错误", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 这里可以添加实际的登录验证逻辑
        log.info("🔐 用户尝试登录: {}", username);
        
        // 模拟登录成功
        JOptionPane.showMessageDialog(this, 
            "登录成功！欢迎 " + username, 
            "登录成功", 
            JOptionPane.INFORMATION_MESSAGE);
        
        // 打开主界面
        openMainFrame();
    }

    /**
     * 处理注册
     */
    private void handleRegister() {
        JOptionPane.showMessageDialog(this, 
            "注册功能正在开发中...", 
            "提示", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * 打开主界面
     */
    private void openMainFrame() {
        try {
            mainFrame = new MainFrame();
            mainFrame.setVisible(true);
            this.setVisible(false); // 隐藏登录界面
            log.info("✅ 主界面打开成功");
        } catch (Exception e) {
            log.error("💥 主界面打开失败", e);
            JOptionPane.showMessageDialog(this, 
                "主界面打开失败: " + e.getMessage(), 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 添加窗口拖动事件
     */
    private void addMouseListeners() {
        // 这里可以添加窗口拖动功能
        // 为了简化，暂时不实现
    }

    /**
     * 添加组件到面板的辅助方法
     */
    private void addComponent(JPanel panel, JComponent comp, int x, int y, int w, GridBagConstraints gbc) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        panel.add(comp, gbc);
    }
}

package com.vcampus.client.ui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.vcampus.client.service.ClientService;
import com.vcampus.common.message.Response;
import com.vcampus.common.util.JsonUtils;
// import net.miglayout.swing.MigLayout; // 暂时注释掉，使用标准布局

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * 主界面
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class MainFrame extends JFrame {
    
    private final ClientService clientService;
    
    // 用户信息
    private String currentUserCardNum;
    private String currentUserType;
    private String currentUserName;
    
    // UI组件
    private JLabel statusLabel;
    private JLabel userInfoLabel;
    private JTextField serverField;
    private JTextField uriField;
    private JTextArea paramsArea;
    private JTextArea responseArea;
    private JButton connectButton;
    private JButton sendButton;
    private JButton heartbeatButton;
    
    // 菜单按钮
    private JButton profileButton;
    private JButton logoutButton;
    
    public MainFrame() {
        this(null, null, null);
    }
    
    public MainFrame(String cardNum, String userType, String userName) {
        this.clientService = new ClientService("localhost", 8080);
        this.currentUserCardNum = cardNum;
        this.currentUserType = userType;
        this.currentUserName = userName;
        initializeUI();
        setupEventHandlers();
    }
    
    /**
     * 初始化UI
     */
    private void initializeUI() {
        setTitle("VCampus客户端 v1.0.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        // 连接面板
        JPanel connectionPanel = createConnectionPanel();
        add(connectionPanel, BorderLayout.NORTH);
        
        // 请求面板
        JPanel requestPanel = createRequestPanel();
        add(requestPanel, BorderLayout.CENTER);
        
        // 响应面板
        JPanel responsePanel = createResponsePanel();
        add(responsePanel, BorderLayout.SOUTH);
        
        // 用户信息面板
        JPanel userPanel = createUserPanel();
        add(userPanel, BorderLayout.WEST);
        
        // 状态栏
        statusLabel = new JLabel("🔴 未连接");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        add(statusLabel, BorderLayout.PAGE_END);
        
        // 设置窗口
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // 更新UI状态
        updateUIState();
    }
    
    /**
     * 创建连接面板
     */
    private JPanel createConnectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("服务器连接"));
        
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("服务器:"));
        serverField = new JTextField("localhost:8080", 15);
        inputPanel.add(serverField);
        
        panel.add(inputPanel, BorderLayout.CENTER);
        
        connectButton = new JButton("连接");
        panel.add(connectButton, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * 创建请求面板
     */
    private JPanel createRequestPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("发送请求"));
        
        // 顶部面板 - URI输入
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("URI:"));
        uriField = new JTextField("system/heartbeat", 20);
        topPanel.add(uriField);
        
        // 中间面板 - 参数输入
        JPanel middlePanel = new JPanel(new BorderLayout(5, 5));
        middlePanel.add(new JLabel("参数(JSON):"), BorderLayout.NORTH);
        paramsArea = new JTextArea("{}", 3, 20);
        paramsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane paramsScroll = new JScrollPane(paramsArea);
        middlePanel.add(paramsScroll, BorderLayout.CENTER);
        
        // 底部面板 - 按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sendButton = new JButton("发送请求");
        heartbeatButton = new JButton("心跳检测");
        JButton infoButton = new JButton("服务器信息");
        JButton timeButton = new JButton("服务器时间");
        
        buttonPanel.add(sendButton);
        buttonPanel.add(heartbeatButton);
        buttonPanel.add(infoButton);
        buttonPanel.add(timeButton);
        
        // 组装面板
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(middlePanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 创建响应面板
     */
    private JPanel createResponsePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("服务器响应"));
        
        responseArea = new JTextArea();
        responseArea.setEditable(false);
        responseArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        responseArea.setText("等待响应...");
        
        JScrollPane scrollPane = new JScrollPane(responseArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建用户信息面板
     */
    private JPanel createUserPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("用户信息"));
        panel.setPreferredSize(new Dimension(200, 400));
        
        // 用户基本信息
        if (currentUserName != null) {
            userInfoLabel = new JLabel("欢迎, " + currentUserName);
            userInfoLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
            userInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(userInfoLabel);
            
            JLabel typeLabel = new JLabel("用户类型: " + (currentUserType != null ? currentUserType : "未知"));
            typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(typeLabel);
            
            JLabel cardLabel = new JLabel("卡号: " + (currentUserCardNum != null ? currentUserCardNum : "未知"));
            cardLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(cardLabel);
        } else {
            userInfoLabel = new JLabel("未登录");
            userInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(userInfoLabel);
        }
        
        panel.add(Box.createVerticalStrut(20));
        
        // 功能按钮
        profileButton = new JButton("个人信息");
        profileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        profileButton.setMaximumSize(new Dimension(150, 30));
        profileButton.addActionListener(e -> showProfile());
        panel.add(profileButton);
        
        panel.add(Box.createVerticalStrut(10));
        
        logoutButton = new JButton("退出登录");
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.setMaximumSize(new Dimension(150, 30));
        logoutButton.addActionListener(e -> logout());
        panel.add(logoutButton);
        
        // 根据用户类型显示不同功能
        if (currentUserType != null) {
            panel.add(Box.createVerticalStrut(20));
            addUserTypeSpecificButtons(panel);
        }
        
        return panel;
    }
    
    /**
     * 根据用户类型添加特定功能按钮
     */
    private void addUserTypeSpecificButtons(JPanel panel) {
        if ("admin".equals(currentUserType)) {
            // 管理员功能
            JButton userManageButton = new JButton("用户管理");
            userManageButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            userManageButton.setMaximumSize(new Dimension(150, 30));
            userManageButton.addActionListener(e -> showUserManagement());
            panel.add(userManageButton);
            
            panel.add(Box.createVerticalStrut(10));
            
            JButton systemButton = new JButton("系统设置");
            systemButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            systemButton.setMaximumSize(new Dimension(150, 30));
            systemButton.addActionListener(e -> showSystemSettings());
            panel.add(systemButton);
            
        } else if ("teacher".equals(currentUserType)) {
            // 教师功能
            JButton courseButton = new JButton("课程管理");
            courseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            courseButton.setMaximumSize(new Dimension(150, 30));
            courseButton.addActionListener(e -> showCourseManagement());
            panel.add(courseButton);
            
            panel.add(Box.createVerticalStrut(10));
            
            JButton gradeButton = new JButton("成绩管理");
            gradeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            gradeButton.setMaximumSize(new Dimension(150, 30));
            gradeButton.addActionListener(e -> showGradeManagement());
            panel.add(gradeButton);
            
        } else if ("student".equals(currentUserType)) {
            // 学生功能
            JButton courseButton = new JButton("我的课程");
            courseButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            courseButton.setMaximumSize(new Dimension(150, 30));
            courseButton.addActionListener(e -> showMyCourses());
            panel.add(courseButton);
            
            panel.add(Box.createVerticalStrut(10));
            
            JButton gradeButton = new JButton("我的成绩");
            gradeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            gradeButton.setMaximumSize(new Dimension(150, 30));
            gradeButton.addActionListener(e -> showMyGrades());
            panel.add(gradeButton);
        }
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 连接按钮
        connectButton.addActionListener(e -> {
            if (clientService.isConnected()) {
                disconnect();
            } else {
                connect();
            }
        });
        
        // 发送请求按钮
        sendButton.addActionListener(e -> sendRequest());
        
        // 心跳检测按钮
        heartbeatButton.addActionListener(e -> {
            uriField.setText("system/heartbeat");
            paramsArea.setText("{}");
            sendRequest();
        });
        
        // 服务器信息按钮
        addQuickButton("服务器信息", "system/info");
        
        // 服务器时间按钮
        addQuickButton("服务器时间", "system/time");
        
        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
                System.exit(0);
            }
        });
        
        // 回车发送请求
        uriField.addActionListener(e -> sendRequest());
    }
    
    /**
     * 添加快捷按钮
     */
    private void addQuickButton(String buttonText, String uri) {
        // 这里可以扩展更多快捷按钮
    }
    
    /**
     * 连接服务器
     */
    private void connect() {
        String serverAddress = serverField.getText().trim();
        String[] parts = serverAddress.split(":");
        
        if (parts.length != 2) {
            showError("服务器地址格式错误，应为: host:port");
            return;
        }
        
        try {
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            
            // 更新客户端服务配置
            // 注意：这里简化处理，实际可能需要重新创建客户端
            
            connectButton.setText("连接中...");
            connectButton.setEnabled(false);
            
            // 在后台线程中连接
            SwingUtilities.invokeLater(() -> {
                boolean success = clientService.connect();
                
                if (success) {
                    showMessage("✅ 连接成功！");
                    responseArea.setText("连接到服务器成功：" + serverAddress);
                } else {
                    showError("❌ 连接失败！");
                }
                
                updateUIState();
            });
            
        } catch (NumberFormatException e) {
            showError("端口号格式错误");
            connectButton.setText("连接");
            connectButton.setEnabled(true);
        }
    }
    
    /**
     * 断开连接
     */
    private void disconnect() {
        clientService.disconnect();
        showMessage("🔌 已断开连接");
        responseArea.setText("已断开与服务器的连接");
        updateUIState();
    }
    
    /**
     * 发送请求
     */
    private void sendRequest() {
        if (!clientService.isConnected()) {
            showError("请先连接服务器");
            return;
        }
        
        String uri = uriField.getText().trim();
        if (uri.isEmpty()) {
            showError("请输入URI");
            return;
        }
        
        String paramsJson = paramsArea.getText().trim();
        Map<String, String> params = new HashMap<>();
        
        // 解析参数
        if (!paramsJson.isEmpty() && !paramsJson.equals("{}")) {
            try {
                // 简单解析JSON参数（这里可以改进）
                // 实际项目中建议使用更完善的JSON解析
                params = JsonUtils.fromJson(paramsJson, Map.class);
                if (params == null) {
                    params = new HashMap<>();
                }
            } catch (Exception e) {
                showError("参数JSON格式错误: " + e.getMessage());
                return;
            }
        }
        
        sendButton.setText("发送中...");
        sendButton.setEnabled(false);
        
        // 在后台线程中发送请求
        final Map<String, String> finalParams = params;
        SwingUtilities.invokeLater(() -> {
            try {
                Response response = clientService.sendRequest(uri, finalParams);
                displayResponse(response);
            } catch (Exception e) {
                displayError("请求异常: " + e.getMessage());
            } finally {
                sendButton.setText("发送请求");
                sendButton.setEnabled(true);
            }
        });
    }
    
    /**
     * 显示响应
     */
    private void displayResponse(Response response) {
        StringBuilder sb = new StringBuilder();
        sb.append("📨 收到响应\\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\\n");
        sb.append("状态: ").append(response.getStatus()).append("\\n");
        sb.append("消息: ").append(response.getMessage()).append("\\n");
        sb.append("时间: ").append(new java.util.Date(response.getTimestamp())).append("\\n");
        
        if (response.getData() != null) {
            sb.append("数据:\\n");
            sb.append(JsonUtils.toJson(response.getData()));
        }
        
        responseArea.setText(sb.toString());
        responseArea.setCaretPosition(0);
    }
    
    /**
     * 显示错误
     */
    private void displayError(String error) {
        responseArea.setText("❌ " + error);
    }
    
    /**
     * 更新UI状态
     */
    private void updateUIState() {
        boolean connected = clientService.isConnected();
        
        // 更新状态标签
        if (connected) {
            statusLabel.setText("🟢 已连接到 " + clientService.getServerAddress());
            connectButton.setText("断开");
        } else {
            statusLabel.setText("🔴 未连接");
            connectButton.setText("连接");
        }
        
        // 更新按钮状态
        connectButton.setEnabled(true);
        sendButton.setEnabled(connected);
        heartbeatButton.setEnabled(connected);
        uriField.setEnabled(connected);
        paramsArea.setEnabled(connected);
    }
    
    /**
     * 显示消息
     */
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 显示错误
     */
    private void showError(String error) {
        JOptionPane.showMessageDialog(this, error, "错误", JOptionPane.ERROR_MESSAGE);
    }
    
    // =============== 用户功能方法 ===============
    
    /**
     * 显示个人信息
     */
    private void showProfile() {
        JOptionPane.showMessageDialog(this, 
            "个人信息功能正在开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 退出登录
     */
    private void logout() {
        int choice = JOptionPane.showConfirmDialog(this, 
            "确定要退出登录吗？", "确认退出", JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            this.dispose(); // 关闭主界面
            // 这里可以重新显示登录界面
            System.exit(0); // 暂时直接退出
        }
    }
    
    /**
     * 管理员功能
     */
    private void showUserManagement() {
        JOptionPane.showMessageDialog(this, 
            "用户管理功能正在开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showSystemSettings() {
        JOptionPane.showMessageDialog(this, 
            "系统设置功能正在开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 教师功能
     */
    private void showCourseManagement() {
        JOptionPane.showMessageDialog(this, 
            "课程管理功能正在开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showGradeManagement() {
        JOptionPane.showMessageDialog(this, 
            "成绩管理功能正在开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 学生功能
     */
    private void showMyCourses() {
        JOptionPane.showMessageDialog(this, 
            "我的课程功能正在开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showMyGrades() {
        JOptionPane.showMessageDialog(this, 
            "我的成绩功能正在开发中...", "提示", JOptionPane.INFORMATION_MESSAGE);
    }
}

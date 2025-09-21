package com.vcampus.client.core.ui.student.view;

import com.vcampus.client.core.ui.student.model.StudentModel;
import com.vcampus.client.core.ui.student.AvatarManager;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 学生界面视图
 * 负责UI组件的创建和显示
 *
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class StudentView extends JFrame {
    
    // UI组件
    private JPanel mainPanel;
    private JLabel lblStudentName;
    private JLabel lblStudentId;
    private JButton btnLogout;
    private JButton btnChangePassword;
    private JButton btnAI;
    private JLabel lblProfile;
    private JLabel lblCourse;
    private JLabel lblLibrary;
    private JLabel lblShop;
    private JLabel lblCard;
    private JPanel contentPanel;
    
    // AI助手相关
    private JPanel aiPanel;
    private JSplitPane mainSplitPane;
    private int originalWidth;
    
    // 头像管理
    private AvatarManager avatarManager;
    
    // 数据模型
    private StudentModel model;
    
    public StudentView(StudentModel model) {
        this.model = model;
        this.avatarManager = new AvatarManager(model.getUserData());
        initUI();
    }
    
    /**
     * 初始化UI
     */
    private void initUI() {
        setTitle("VCampus - 学生端");
        setupWindowProperties();
        createMainLayout();
        log.info("学生界面UI初始化完成");
    }
    
    /**
     * 设置窗口属性
     */
    private void setupWindowProperties() {
        // 统一与管理员界面缩放策略：目标 1080x690，必要时自适应屏幕
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        double scale = 0.89 * 1.05; // = 0.9345
        int targetW = (int) Math.round(1080 * scale);
        int targetH = (int) Math.round(690 * scale);
        if (screen.width < targetW + 40) targetW = Math.max(960, screen.width - 40);
        if (screen.height < targetH + 60) targetH = Math.max(640, screen.height - 60);
        setSize(targetW, targetH);
        originalWidth = targetW;
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(true);
        
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/figures/logo.png"));
            setIconImage(logoIcon.getImage());
        } catch (Exception e) {
            log.warn("自定义图标加载失败，使用默认图标");
        }
    }
    
    /**
     * 创建主布局
     */
    private void createMainLayout() {
        mainPanel = new JPanel(new BorderLayout());
        
        // 创建侧边栏面板
        JPanel sidebarPanel = createSidebarPanel();
        
        // 创建主内容面板
        contentPanel = createContentPanel();
        
        // 创建AI助手面板
        aiPanel = createAIPanel();
        
        // 创建分割面板
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, contentPanel, aiPanel);
        mainSplitPane.setDividerSize(0);
        mainSplitPane.setDividerLocation(originalWidth);
        mainSplitPane.setResizeWeight(1.0);
        
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(mainSplitPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    /**
     * 创建侧边栏面板
     */
    private JPanel createSidebarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(200, 0));
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 顶部用户信息区域
        JPanel userInfoPanel = createUserInfoPanel();
        
        // 中间模块选项区域
        JPanel modulePanel = createModulePanel();
        
        // 底部按钮区域
        JPanel buttonPanel = createButtonPanel();
        
        panel.add(userInfoPanel, BorderLayout.NORTH);
        panel.add(modulePanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 创建用户信息面板
     */
    private JPanel createUserInfoPanel() {
        JPanel userInfoPanel = new JPanel(new BorderLayout());
        userInfoPanel.setOpaque(false);
        userInfoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // 用户头像
        JLabel avatarLabel = new JLabel();
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setPreferredSize(new Dimension(80, 80));
        avatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 加载用户头像
        avatarManager.loadUserAvatar(avatarLabel, 80, 80);
        
        // 用户信息
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        
        lblStudentName = new JLabel(model.getStudentName());
        lblStudentName.setFont(new Font("微软雅黑", Font.BOLD, 16));
        lblStudentName.setForeground(new Color(25, 25, 112));
        
        lblStudentId = new JLabel("学号: " + model.getStudentId());
        lblStudentId.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        lblStudentId.setForeground(new Color(100, 100, 100));
        
        gbc.gridx = 0; gbc.gridy = 0;
        infoPanel.add(lblStudentName, gbc);
        gbc.gridy = 1;
        infoPanel.add(lblStudentId, gbc);
        
        userInfoPanel.add(avatarLabel, BorderLayout.NORTH);
        userInfoPanel.add(infoPanel, BorderLayout.CENTER);
        
        return userInfoPanel;
    }
    
    /**
     * 创建模块选择面板
     */
    private JPanel createModulePanel() {
        JPanel modulePanel = new JPanel(new GridBagLayout());
        modulePanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 创建模块标签
        lblProfile = createModuleLabel("个人信息", "/figures/profile.png");
        lblCourse = createModuleLabel("课程管理", "/figures/course.png");
        lblLibrary = createModuleLabel("图书馆", "/figures/library.png");
        lblShop = createModuleLabel("校园商城", "/figures/shop.png");
        lblCard = createModuleLabel("校园卡", "/figures/card.png");
        
        gbc.gridx = 0; gbc.gridy = 0;
        modulePanel.add(lblProfile, gbc);
        gbc.gridy = 1;
        modulePanel.add(lblCourse, gbc);
        gbc.gridy = 2;
        modulePanel.add(lblLibrary, gbc);
        gbc.gridy = 3;
        modulePanel.add(lblShop, gbc);
        gbc.gridy = 4;
        modulePanel.add(lblCard, gbc);
        
        return modulePanel;
    }
    
    /**
     * 创建模块标签
     */
    private JLabel createModuleLabel(String text, String iconPath) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        label.setPreferredSize(new Dimension(160, 40));
        label.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        label.setBackground(new Color(255, 255, 255));
        label.setOpaque(true);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 设置图标
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            label.setIcon(icon);
        } catch (Exception e) {
            log.warn("图标加载失败: {}", iconPath);
        }
        
        return label;
    }
    
    /**
     * 创建按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        buttonPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        btnLogout = new JButton("退出登录");
        btnChangePassword = new JButton("修改密码");
        btnAI = new JButton("AI助手");
        
        // 设置按钮样式
        Font buttonFont = new Font("微软雅黑", Font.PLAIN, 12);
        btnLogout.setFont(buttonFont);
        btnChangePassword.setFont(buttonFont);
        btnAI.setFont(buttonFont);
        
        gbc.gridx = 0; gbc.gridy = 0;
        buttonPanel.add(btnLogout, gbc);
        gbc.gridy = 1;
        buttonPanel.add(btnChangePassword, gbc);
        gbc.gridy = 2;
        buttonPanel.add(btnAI, gbc);
        
        return buttonPanel;
    }
    
    /**
     * 创建主内容面板
     */
    private JPanel createContentPanel() {
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        
        // 显示欢迎信息
        JLabel welcomeLabel = new JLabel("欢迎使用VCampus学生端系统", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(25, 25, 112));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
        
        contentPanel.add(welcomeLabel, BorderLayout.CENTER);
        
        return contentPanel;
    }
    
    /**
     * 创建AI助手面板
     */
    private JPanel createAIPanel() {
        aiPanel = new JPanel(new BorderLayout());
        aiPanel.setPreferredSize(new Dimension(300, 0));
        aiPanel.setBackground(new Color(248, 248, 255));
        aiPanel.setBorder(BorderFactory.createTitledBorder("AI助手"));
        
        JLabel aiLabel = new JLabel("AI助手功能开发中...", SwingConstants.CENTER);
        aiLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        aiLabel.setForeground(new Color(100, 100, 100));
        
        aiPanel.add(aiLabel, BorderLayout.CENTER);
        
        return aiPanel;
    }
    
    /**
     * 切换AI面板显示
     */
    public void toggleAIPanel(boolean visible) {
        if (visible) {
            mainSplitPane.setDividerLocation(originalWidth - 300);
            mainSplitPane.setDividerSize(8);
        } else {
            mainSplitPane.setDividerLocation(originalWidth);
            mainSplitPane.setDividerSize(0);
        }
    }
    
    /**
     * 显示个人信息面板
     */
    public void showProfilePanel() {
        contentPanel.removeAll();
        contentPanel.add(new ProfilePanel(model, this), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /**
     * 显示课程管理面板
     */
    public void showCoursePanel() {
        contentPanel.removeAll();
        contentPanel.add(new CoursePanel(model, this), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /**
     * 显示图书馆面板
     */
    public void showLibraryPanel() {
        contentPanel.removeAll();
        contentPanel.add(new LibraryPanel(model, this), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /**
     * 显示商城面板
     */
    public void showShopPanel() {
        contentPanel.removeAll();
        contentPanel.add(new ShopPanel(model, this), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    /**
     * 显示校园卡面板
     */
    public void showCardPanel() {
        contentPanel.removeAll();
        contentPanel.add(new CardPanel(model, this), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    // Getter方法
    public JButton getBtnLogout() { return btnLogout; }
    public JButton getBtnChangePassword() { return btnChangePassword; }
    public JButton getBtnAI() { return btnAI; }
    public JLabel getLblProfile() { return lblProfile; }
    public JLabel getLblCourse() { return lblCourse; }
    public JLabel getLblLibrary() { return lblLibrary; }
    public JLabel getLblShop() { return lblShop; }
    public JLabel getLblCard() { return lblCard; }
}

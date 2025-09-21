package com.vcampus.client.core.ui.admin;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.client.core.ui.login.LoginFrame;
import com.vcampus.client.core.ui.shop.ShopManagerPanel;
import com.vcampus.client.core.ui.userAdmin.StatusPanel;
import com.vcampus.client.core.ui.eduAdmin.eduStatus;
import com.vcampus.client.core.util.IdUtils; // 新增：导入ID工具类
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;


import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

/**
 * 教职工用户主界面
 *
 * @author VCampus Team
 * @version 2.0
 */
@Slf4j
public class StaffFrame extends JFrame {
    // 现代化配色方案
    private static final Color PRIMARY_COLOR = new Color(64, 128, 255);      // 主色调 - 现代蓝
    private static final Color SECONDARY_COLOR = new Color(99, 102, 241);    // 次要色 - 紫蓝
    private static final Color ACCENT_COLOR = new Color(16, 185, 129);       // 强调色 - 翠绿
    private static final Color WARNING_COLOR = new Color(245, 158, 11);      // 警告色 - 橙黄
    private static final Color DANGER_COLOR = new Color(239, 68, 68);        // 危险色 - 红色
    private static final Color SURFACE_COLOR = new Color(248, 250, 252);     // 表面色 - 浅灰
    private static final Color CARD_COLOR = Color.WHITE;                     // 卡片色 - 白色
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);         // 主文本色
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);    // 次要文本色
    private static final Color BORDER_COLOR = new Color(226, 232, 240);      // 边框色

    private JPanel mainPanel;
    private NettyClient nettyClient;
    private Map<String, Object> userData;

    // 现代化侧边栏
    private ModernSidebar sidebar;
    private JPanel contentPanel;

    // 编辑状态管理
    private boolean isEditing = false;
    private Map<String, JComponent> editingComponents = new HashMap<>();
    private Map<String, Object> currentStaffData;

    // 头像管理
    private AvatarManager avatarManager;

    // 添加缺失的字段声明
    private JButton btnChangePassword;
    private JLabel lblStaffName;
    private boolean shopResizeListenerAdded = false;

    // AI 分屏相关字段
    private JPanel aiPanel;
    private JSplitPane mainSplitPane;
    private boolean aiVisible = false;
    private int originalWidth;
    private JButton btnAI;


    public StaffFrame(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        this.avatarManager = new AvatarManager(userData, nettyClient);


        initModernUI();
        setupEventHandlers();
        log.info("现代化教职工主界面初始化完成: {}", userData.get("userName"));
    }

    /**
     * 初始化现代化UI
     */
    private void initModernUI() {
        setTitle("VCampus - 教职工端");

        // 设置现代化窗口样式
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int targetW = Math.min(1200, screen.width - 100);
        int targetH = Math.min(800, screen.height - 100);
        setSize(targetW, targetH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(true);

        // 设置应用图标
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/figures/logo.png"));
            setIconImage(logoIcon.getImage());
        } catch (Exception e) {
            log.warn("自定义图标加载失败，使用默认图标");
        }

        // 创建主面板
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(SURFACE_COLOR);

        // 创建现代化侧边栏
        sidebar = new ModernSidebar();

        // 创建主内容面板
        contentPanel = createModernContentPanel();

        // 创建 AI 面板（初始化但不显示）
        aiPanel = createAIPanel();

        // 创建分割面板
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setLeftComponent(createMainContentContainer());
        mainSplitPane.setRightComponent(null);
        mainSplitPane.setDividerSize(3);
        mainSplitPane.setResizeWeight(1.0);
        mainSplitPane.setBorder(null);

        // 保存原始宽度
        originalWidth = getWidth();

        mainPanel.add(mainSplitPane, BorderLayout.CENTER);

        // 创建现代化状态栏
        JPanel statusBar = createModernStatusBar();
        mainPanel.add(statusBar, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    /**
     * 创建主内容容器
     */
    private JPanel createMainContentContainer() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(SURFACE_COLOR);
        container.add(sidebar, BorderLayout.WEST);
        container.add(contentPanel, BorderLayout.CENTER);
        return container;
    }

    /**
     * 创建现代化内容面板
     */
    private JPanel createModernContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(SURFACE_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 默认显示个人信息面板
        JPanel profilePanel = createProfilePanel();
        panel.add(profilePanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 创建现代化状态栏
     */
    private JPanel createModernStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(CARD_COLOR);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        JLabel statusLabel = new JLabel("就绪");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setIcon(createColoredIcon("●", ACCENT_COLOR));
        statusBar.add(statusLabel, BorderLayout.WEST);

        // 中间添加退出登录按钮
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setBackground(CARD_COLOR);

        JButton logoutButton = new JButton("退出登录");
        logoutButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(DANGER_COLOR);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setPreferredSize(new Dimension(120, 28));
        logoutButton.addActionListener(e -> handleLogout());

        // 添加悬停效果
        logoutButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logoutButton.setBackground(DANGER_COLOR.brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logoutButton.setBackground(DANGER_COLOR);
            }
        });

        centerPanel.add(logoutButton);
        statusBar.add(centerPanel, BorderLayout.CENTER);

        JLabel timeLabel = new JLabel(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        timeLabel.setForeground(TEXT_SECONDARY);
        statusBar.add(timeLabel, BorderLayout.EAST);

        // 创建定时器更新时间
        new Timer(1000, e -> {
            timeLabel.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        }).start();

        return statusBar;
    }

    /**
     * 现代化侧边栏内部类
     */
    private class ModernSidebar extends JPanel {
        private static final int EXPANDED_WIDTH = 250;
        private static final int COLLAPSED_WIDTH = 80;

        private boolean isExpanded = true;
        private boolean isLocked = false; // 新增：锁定状态，防止自动收缩
        private JPanel topPanel;     // 固定的头像区域
        private JPanel menuPanel;    // 可变的菜单区域
        private Timer animationTimer;

        // 菜单项
        private ModernMenuItem profileItem;
        private ModernMenuItem courseItem;
        private ModernMenuItem libraryItem;
        private ModernMenuItem shopItem;
        private ModernMenuItem cardItem;

        public ModernSidebar() {
            initSidebar();
            setupAnimation();
        }

        private void initSidebar() {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(EXPANDED_WIDTH, 0));
            setBackground(CARD_COLOR);
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

            // 创建固定的头像区域
            topPanel = createTopPanel();
            add(topPanel, BorderLayout.NORTH);

            // 创建可变的菜单区域
            menuPanel = createMenuPanel();
            add(menuPanel, BorderLayout.CENTER);

            // 修改鼠标监听器，支持点击锁定
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isExpanded && !isLocked) {
                        expand();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    Point mousePos = e.getPoint();
                    // 只有在未锁定状态下才自动收缩
                    if (!contains(mousePos) && isExpanded && !isLocked) {
                        collapse();
                    }
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    // 点击切换锁定状态
                    isLocked = !isLocked;
                    if (isLocked) {
                        // 锁定时确保展开
                        if (!isExpanded) {
                            expand();
                        }
                    }
                    // 可选：添加视觉反馈，显示锁定状态
                    updateLockIndicator();
                }
            });
        }

        /**
         * 创建固定的头像区域 - 此区域在展开/收起时保持结构不变
         */
        private JPanel createTopPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(CARD_COLOR);
            panel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

            // 设置固定的首选高度，防止收缩时整个面板高度变化
            panel.setPreferredSize(new Dimension(EXPANDED_WIDTH, 150)); // 固定高度150px
            panel.setMinimumSize(new Dimension(COLLAPSED_WIDTH, 120));
            panel.setMaximumSize(new Dimension(EXPANDED_WIDTH, 120));

            // 创建可隐藏的内容容器
            JPanel contentContainer = new JPanel(new BorderLayout());
            contentContainer.setBackground(CARD_COLOR);
            contentContainer.setName("avatarContent"); // 设置名称用于识别

            // 头像 - 放入内容容器
            JLabel avatarLabel = new JLabel();
            avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
            avatarLabel.setPreferredSize(new Dimension(60, 60));
            avatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // 加载用户头像
            avatarManager.loadUserAvatar(avatarLabel, 60, 60);

            // 添加点击事件
            avatarLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showAvatarSelectionDialog(avatarLabel, null);
                }
            });

            // 用户信息面板 - 使用固定高度的容器
            JPanel infoContainer = new JPanel(new BorderLayout());
            infoContainer.setBackground(CARD_COLOR);
            infoContainer.setPreferredSize(new Dimension(EXPANDED_WIDTH - 30, 40)); // 固定高度40px
            infoContainer.setMinimumSize(new Dimension(COLLAPSED_WIDTH - 30, 40));
            infoContainer.setMaximumSize(new Dimension(EXPANDED_WIDTH - 30, 40));

            // 实际的用户信息面板 - 可以隐藏/显示
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBackground(CARD_COLOR);
            infoPanel.setName("userInfo"); // 设置名称用于识别

            lblStaffName = new JLabel(userData.get("userName").toString());
            lblStaffName.setFont(new Font("微软雅黑", Font.BOLD, 14));
            lblStaffName.setForeground(TEXT_PRIMARY);
            lblStaffName.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel idLabel = new JLabel("(" + userData.get("cardNum").toString() + ")");
            idLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            idLabel.setForeground(TEXT_SECONDARY);
            idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            infoPanel.add(Box.createVerticalStrut(5));
            infoPanel.add(lblStaffName);
            infoPanel.add(Box.createVerticalStrut(2));
            infoPanel.add(idLabel);

            // 将信息面板放入固定高度的容器中
            infoContainer.add(infoPanel, BorderLayout.CENTER);

            // 将头像和信息都放入内容容器
            contentContainer.add(avatarLabel, BorderLayout.NORTH);
            contentContainer.add(infoContainer, BorderLayout.CENTER);

            // 将内容容器添加到主面板
            panel.add(contentContainer, BorderLayout.CENTER);

            return panel;
        }

        private JPanel createMenuPanel() {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(CARD_COLOR);
            panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 20, 5));

            // 创建菜单项
            profileItem = new ModernMenuItem("👤", "个人信息", () -> switchToModule("profile"));
            courseItem = new ModernMenuItem("📚", "课程管理", () -> switchToModule("course"));
            libraryItem = new ModernMenuItem("📖", "图书管理", () -> switchToModule("library"));
            shopItem = new ModernMenuItem("🛒", "校园商城", () -> switchToModule("shop"));
            cardItem = new ModernMenuItem("💳", "校园卡", () -> switchToModule("card"));

            // 设置默认选中
            profileItem.setSelected(true);

            panel.add(profileItem);
            panel.add(Box.createVerticalStrut(2));
            panel.add(courseItem);
            panel.add(Box.createVerticalStrut(2));
            panel.add(libraryItem);
            panel.add(Box.createVerticalStrut(2));
            panel.add(shopItem);
            panel.add(Box.createVerticalStrut(2));
            panel.add(cardItem);

            return panel;
        }

        private void setupAnimation() {
            animationTimer = new Timer(10, null);
        }

        public void expand() {
            if (isExpanded) return;

            isExpanded = true;
            animateWidth(COLLAPSED_WIDTH, EXPANDED_WIDTH);
        }

        public void collapse() {
            if (!isExpanded) return;

            isExpanded = false;
            animateWidth(EXPANDED_WIDTH, COLLAPSED_WIDTH);
        }

        private void animateWidth(int fromWidth, int toWidth) {
            if (animationTimer.isRunning()) {
                animationTimer.stop();
            }

            final int[] currentWidth = {fromWidth};
            final int totalSteps = 15; // 增加动画步数，让动画更平滑
            final double stepSize = (double)(toWidth - fromWidth) / totalSteps;
            final int[] stepCount = {0};

            // 在动画开始前就设置目标状态，避免突然跳跃
            if (toWidth > fromWidth) {
                // 展开动画：立即显示文本但设置透明度为0
                updateMenuItemsDisplayMode(true, 0.0f);
            } else {
                // 收起动画：保持当前显示状态直到动画结束
                updateMenuItemsDisplayMode(true, 1.0f);
            }

            animationTimer = new Timer(16, e -> { // 60fps 动画
                stepCount[0]++;
                currentWidth[0] = (int)(fromWidth + stepSize * stepCount[0]);

                // 计算动画进度 (0.0 到 1.0)
                float progress = (float)stepCount[0] / totalSteps;

                if (stepCount[0] >= totalSteps) {
                    currentWidth[0] = toWidth;
                    animationTimer.stop();

                    // 动画完成后设置最终状态
                    updateUIForCurrentState();
                } else {
                    // 动画过程中平滑更新透明度
                    if (toWidth > fromWidth) {
                        // 展开：文本透明度从0到1
                        updateMenuItemsDisplayMode(true, progress);
                    } else {
                        // 收起：文本透明度从1到0
                        updateMenuItemsDisplayMode(true, 1.0f - progress);
                    }
                }

                setPreferredSize(new Dimension(currentWidth[0], getHeight()));
                revalidate();
                repaint();
            });

            animationTimer.start();
        }

        /**
         * 更新菜单项显示模式（带透明度控制）
         * @param isExpanded 是否展开状态
         * @param textAlpha 文本透明度 (0.0-1.0)
         */
        private void updateMenuItemsDisplayMode(boolean isExpanded, float textAlpha) {
            profileItem.updateDisplayModeWithAlpha(isExpanded, textAlpha);
            courseItem.updateDisplayModeWithAlpha(isExpanded, textAlpha);
            libraryItem.updateDisplayModeWithAlpha(isExpanded, textAlpha);
            shopItem.updateDisplayModeWithAlpha(isExpanded, textAlpha);
            cardItem.updateDisplayModeWithAlpha(isExpanded, textAlpha);
        }

        /**
         * 根据当前展开/收起状态更新UI显示 - 重构版本
         * 只更新用户信息文本的可见性和菜单项的显示模式，不重新创建组件
         */
        private void updateUIForCurrentState() {
            // 更新头像区域的用户信息文本可见性
            updateTopPanelDisplay();

            // 更新菜单项显示状态（完全透明度）
            updateMenuItemsDisplayMode(isExpanded, 1.0f);

            revalidate();
            repaint();
        }

        /**
         * 更新头像区域的显示状态 - 使用平滑的高度动画
         */
        private void updateTopPanelDisplay() {
            // 查找内容容器（包含头像和用户信息的容器）
            Component[] components = topPanel.getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel && "avatarContent".equals(comp.getName())) {
                    // 只设置内容容器的可见性，topPanel本身保持可见和占位
                    comp.setVisible(isExpanded);
                    topPanel.revalidate();
                    topPanel.repaint();
                    return;
                }
            }
        }

        /**
         * 更新锁定状态指示器
         */
        private void updateLockIndicator() {
            // 通过改变边框颜色来显示锁定状态
            if (isLocked) {
                // 锁定状态：使用蓝色边框
                setBorder(BorderFactory.createMatteBorder(0, 0, 0, 3, new Color(162, 194, 229)));
            } else {
                // 未锁定状态：使用默认边框
                setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
            }
        }

        /**
         * 获取锁定状态
         */
        public boolean isLocked() {
            return isLocked;
        }

        /**
         * 设置锁定状态
         */
        public void setLocked(boolean locked) {
            this.isLocked = locked;
            updateLockIndicator();
        }

        /**
         * 设置选中的菜单项
         */
        public void setSelectedMenuItem(String module) {
            // 先取消所有选中状态
            profileItem.setSelected(false);
            courseItem.setSelected(false);
            libraryItem.setSelected(false);
            shopItem.setSelected(false);
            cardItem.setSelected(false);

            // 设置对应模块为选中状态
            switch (module) {
                case "profile":
                    profileItem.setSelected(true);
                    break;
                case "course":
                    courseItem.setSelected(true);
                    break;
                case "library":
                    libraryItem.setSelected(true);
                    break;
                case "shop":
                    shopItem.setSelected(true);
                    break;
                case "card":
                    cardItem.setSelected(true);
                    break;
            }
        }
    }

    /**
     * 现代化菜单项内部类
     */
    private class ModernMenuItem extends JPanel {
        private boolean isSelected = false;
        private boolean isHovered = false;
        private final String icon;
        private final String text;
        private final Runnable action;

        // 保存组件引用，避免重复创建
        private JLabel iconLabel;
        private JLabel textLabel;

        public ModernMenuItem(String icon, String text, Runnable action) {
            this.icon = icon;
            this.text = text;
            this.action = action;

            initMenuItem();
        }

        private void initMenuItem() {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(240, 45));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            setBackground(CARD_COLOR);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            // 创建图标标签 - 保存引用
            iconLabel = new JLabel(icon, SwingConstants.CENTER);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            iconLabel.setPreferredSize(new Dimension(50, 45));

            // 创建文本标签 - 保存引用
            textLabel = new JLabel(text);
            textLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            textLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

            add(iconLabel, BorderLayout.WEST);
            add(textLabel, BorderLayout.CENTER);

            // 添加事件监听器
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    action.run();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    updateAppearance();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    updateAppearance();
                }
            });

            updateAppearance();
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            updateAppearance();
        }

        /**
         * 更新显示模式 - 重构版本，不再使用 removeAll()
         * @param isExpanded true 表示展开状态，false 表示收起状态
         */
        public void updateDisplayMode(boolean isExpanded) {
            if (isExpanded) {
                // 展开状态：显示文本，调整尺寸
                setPreferredSize(new Dimension(240, 45));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
                setMinimumSize(new Dimension(240, 45));

                // 显示文本标签
                textLabel.setVisible(true);

                // 确保图标位置正确
                iconLabel.setPreferredSize(new Dimension(50, 45));
            } else {
                // 收起状态：隐藏文本，调整尺寸
                setPreferredSize(new Dimension(60, 45));
                setMaximumSize(new Dimension(60, 45));
                setMinimumSize(new Dimension(60, 45));

                // 隐藏文本标签
                textLabel.setVisible(false);

                // 调整图标位置以居中显示
                iconLabel.setPreferredSize(new Dimension(60, 45));
            }

            // 更新外观
            updateAppearance();

            // 轻量级重绘，不强制重新布局整个容器
            revalidate();
            repaint();
        }

        /**
         * 更新显示模式 - 带透明度控制的重构版本
         * @param isExpanded true 表示展开状态，false 表示收起状态
         * @param alpha 透明度 (0.0 - 1.0)
         */
        public void updateDisplayModeWithAlpha(boolean isExpanded, float alpha) {
            // 动画插值器，实现先快后慢的效果
            float interpolatedAlpha = (float) (1.0 - Math.pow(1.0 - alpha, 2));

            if (isExpanded) {
                // 展开状态
                // 确保布局是展开时的布局
                if (getComponentCount() == 1) { // 如果只有icon
                    add(textLabel, BorderLayout.CENTER);
                    add(iconLabel, BorderLayout.WEST);
                }
                iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
                iconLabel.setPreferredSize(new Dimension(50, 45));

                setPreferredSize(new Dimension(240, 45));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
                setMinimumSize(new Dimension(240, 45));

                textLabel.setVisible(true);

                // 文本淡入
                setAlpha(interpolatedAlpha);

            } else {
                // 收起状态
                int currentWidth = (int) (60 + (240 - 60) * (1 - interpolatedAlpha));
                setPreferredSize(new Dimension(currentWidth, 45));
                setMaximumSize(new Dimension(currentWidth, 45));
                setMinimumSize(new Dimension(currentWidth, 45));

                // 文本淡出
                setAlpha(1.0f - interpolatedAlpha);
                if (alpha > 0.9f) { // 动画快结束时再隐藏文本
                    textLabel.setVisible(false);
                } else {
                    textLabel.setVisible(true);
                }

                // 在收起动画开始时，将图标居中
                if (alpha < 0.1f) {
                    if (getComponentCount() > 1) { // 如果有文本和图标
                        remove(textLabel);
                        remove(iconLabel);
                        add(iconLabel, BorderLayout.CENTER);
                    }
                }
                iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            }

            // 更新外观
            updateAppearance();

            revalidate();
            repaint();
        }

        private void updateAppearance() {
            if (isSelected) {
                setBackground(PRIMARY_COLOR);
                setForeground(Color.WHITE);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 3, 0, 0, ACCENT_COLOR),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            } else if (isHovered) {
                setBackground(new Color(248, 250, 252));
                setForeground(TEXT_PRIMARY);
                setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            } else {
                setBackground(CARD_COLOR);
                setForeground(TEXT_SECONDARY);
                setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
            }

            // 更新子组件颜色
            if (iconLabel != null) {
                iconLabel.setForeground(getForeground());
            }
            if (textLabel != null) {
                textLabel.setForeground(getForeground());
            }

            repaint();
        }

        /**
         * 设置透明度
         */
        private void setAlpha(float alpha) {
            // 限制透明度范围
            alpha = Math.max(0.0f, Math.min(1.0f, alpha));

            // 仅对文本标签设置透明度
            if (textLabel != null) {
                Color currentColor = textLabel.getForeground();
                if (currentColor != null) {
                    Color newColor = new Color(
                            currentColor.getRed(),
                            currentColor.getGreen(),
                            currentColor.getBlue(),
                            (int)(alpha * 255)
                    );
                    textLabel.setForeground(newColor);
                }
            }
        }
    }

    /**
     * 现代化按钮内部类
     */
    private class ModernButton extends JButton {
        private final Color buttonColor;
        private final String icon;
        private String text;
        private boolean isHovered = false;

        public ModernButton(String icon, String text, Color color) {
            super();
            this.icon = icon;
            this.text = text;
            this.buttonColor = color;
            initButton();
        }

        private void initButton() {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(220, 45));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setMargin(new Insets(0, 0, 0, 0));

            JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            iconLabel.setPreferredSize(new Dimension(50, 45));

            JLabel textLabel = new JLabel(text);
            textLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            textLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

            add(iconLabel, BorderLayout.WEST);
            add(textLabel, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        // 背景绘制
        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(isHovered ? buttonColor.brighter() : buttonColor);
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
        }


        // 重写setText方法来更新文本标签
        @Override
        public void setText(String text) {
            this.text = text;
            // 查找并更新文本标签
            Component[] components = getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel && !((JLabel) comp).getText().equals(icon)) {
                    ((JLabel) comp).setText(text);
                    break;
                }
            }
            // 不再调用 super.setText(text)，避免按钮重复显示文字
        }

        @Override
        public String getText() {
            return this.text;
        }

        private void updateAppearance() {
            if (isHovered) {
                setBackground(buttonColor.darker());
            } else {
                setBackground(buttonColor);
            }
            setOpaque(true);

            // 更新子组件颜色
            Component[] components = getComponents();
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    comp.setForeground(Color.WHITE);
                }
            }

            // 设置边框样式，类似 ModernMenuItem
            setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

            repaint();
        }


    }

    /**
     * 图标工具类内部类
     */
    private static class IconUtils {
        public static ImageIcon createIcon(String text, Color color, int size) {
            java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.setColor(color);
            g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size - 4));

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getAscent();
            int x = (size - textWidth) / 2;
            int y = (size + textHeight) / 2 - 2;

            g2d.drawString(text, x, y);
            g2d.dispose();

            return new ImageIcon(image);
        }
    }

    /**
     * 创建彩色图标
     */
    private ImageIcon createColoredIcon(String text, Color color) {
        return IconUtils.createIcon(text, color, 16);
    }


    /**
     * 切换模块
     */
    private void switchToModule(String module) {
        // 更新侧边栏选中状态
        sidebar.setSelectedMenuItem(module);

        // 移除当前内容
        contentPanel.removeAll();

        // 根据模块切换内容
        JPanel panel = null;
        switch (module) {
            case "profile":
                panel = createProfilePanel();
                break;
            case "course":
                panel = createCourseManagePanel();
                break;
            case "library":
                panel = new com.vcampus.client.core.ui.library.LibraryPanel(nettyClient, userData);
                break;
            case "shop":
                panel = createShopPanel();
                break;
            case "card":
                panel = new com.vcampus.client.core.ui.card.CardPanel(nettyClient, userData);
                break;
        }

        if (panel != null) {
            contentPanel.add(panel, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(StaffFrame.this,
                        "确定要退出登录吗？", "确认", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    handleLogout();
                }
            }
        });
    }

    /**
     * 处理退出登录
     */
    private void handleLogout() {
        try {
            Request logoutRequest = new Request("auth/logout");
            Response response = nettyClient.sendRequest(logoutRequest).get(5, java.util.concurrent.TimeUnit.SECONDS);

            if (response != null && "SUCCESS".equals(response.getStatus())) {
                log.info("用户退出登录成功: {}", userData.get("userName"));
            } else {
                log.warn("用户退出登录请求失败: {}", response != null ? response.getMessage() : "未知错误");
            }

            this.dispose();
            new LoginFrame().setVisible(true);

        } catch (Exception e) {
            log.error("退出登录过程中发生错误", e);
            this.dispose();
            new LoginFrame().setVisible(true);
        }
    }

    /**
     * 创建个人信息面板
     */
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("个人信息");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 创建加载提示
        JLabel loadingLabel = new JLabel("正在加载个人信息...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        loadingLabel.setForeground(Color.GRAY);
        panel.add(loadingLabel, BorderLayout.CENTER);

        // 在后台线程中加载完整的教职工信息
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    // 获取用户卡号
                    Object cardNumObj = userData.get("cardNum");
                    String cardNumStr;

                    if (cardNumObj instanceof Number) {
                        java.math.BigDecimal bd = new java.math.BigDecimal(cardNumObj.toString());
                        cardNumStr = bd.toPlainString();
                    } else {
                        cardNumStr = cardNumObj.toString();
                    }

                    log.info("开始加载教职工信息，卡号: {}", cardNumStr);

                    // 发送请求获取教职工信息
                    Request request = new Request("academic/staff")
                            .addParam("action", "GET_STAFF_BY_CARD_NUM")
                            .addParam("cardNum", cardNumStr);

                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);

                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> staffData = (Map<String, Object>) response.getData();
                        SwingUtilities.invokeLater(() -> displayStaffInfo(panel, staffData));
                        log.info("教职工信息加载成功");
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> showError(panel, "加载个人信息失败：" + errorMsg));
                        log.warn("教职工信息加载失败: {}", errorMsg);
                    }

                } catch (Exception e) {
                    log.error("加载教职工信息时发生错误", e);
                    SwingUtilities.invokeLater(() -> showError(panel, "加载个人信息时发生错误：" + e.getMessage()));
                }
            }).start();
        });

        return panel;
    }

    /**
     * 显示教职工信息
     */
    private void displayStaffInfo(JPanel panel, Map<String, Object> staffData) {
        // 保存当前教师数据
        this.currentStaffData = staffData;

        // 移除加载提示
        panel.removeAll();

        // 创建主内容面板
        JPanel mainContentPanel = new JPanel(new BorderLayout());

        // 创建头像和信息面板
        JPanel avatarInfoPanel = new JPanel(new BorderLayout());
        avatarInfoPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 头像区域
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel avatarLabel = new JLabel();
        avatarLabel.setHorizontalAlignment(SwingConstants.CENTER);
        avatarLabel.setPreferredSize(new Dimension(160, 160));
        avatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 加载用户头像
        avatarManager.loadUserAvatar(avatarLabel, 160, 160);

        // 添加点击事件
        avatarLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showAvatarSelectionDialog(null, avatarLabel);
            }
        });

        avatarPanel.add(avatarLabel);
        
        // 创建信息显示面板
        JPanel infoPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制圆角背景
                g2d.setColor(new Color(255, 255, 255, 250));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // 绘制边框
                g2d.setColor(new Color(200, 200, 200));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 15, 15);
                
                g2d.dispose();
            }
        };
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(),
                "教职工详细信息",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("微软雅黑", Font.BOLD, 16),
                new Color(70, 130, 180)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12); // 优化间距，更紧凑
        gbc.anchor = GridBagConstraints.WEST;

        // 基本信息 - 不可编辑的字段
        addInfoRow(infoPanel, "卡号:", staffData.get("cardNum"), gbc, 0, false);
        addInfoRow(infoPanel, "工号:", staffData.get("staffId"), gbc, 1, false);
        addInfoRow(infoPanel, "职称/职位:", staffData.get("title"), gbc, 2, false);
        addInfoRow(infoPanel, "学院:", staffData.get("department"), gbc, 3, false);

        // 可编辑的字段
        addInfoRow(infoPanel, "姓名:", staffData.get("name"), gbc, 4, true);
        addInfoRow(infoPanel, "性别:", staffData.get("gender"), gbc, 5, true);
        addInfoRow(infoPanel, "出生年月:", staffData.get("birthDate"), gbc, 6, true);
        addInfoRow(infoPanel, "电话:", staffData.get("phone"), gbc, 7, true);
        addInfoRow(infoPanel, "民族:", staffData.get("ethnicity"), gbc, 8, true);
        addInfoRow(infoPanel, "身份证号:", staffData.get("idCard"), gbc, 9, true);
        addInfoRow(infoPanel, "籍贯:", staffData.get("hometown"), gbc, 10, true);
        addInfoRow(infoPanel, "参工年份:", staffData.get("workYear"), gbc, 11, true);

        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(infoPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        // 创建左右布局：左侧头像，右侧信息
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);
        contentPanel.add(avatarPanel, BorderLayout.WEST);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        avatarInfoPanel.add(contentPanel, BorderLayout.CENTER);
        mainContentPanel.add(avatarInfoPanel, BorderLayout.CENTER);

        // 添加按钮区域
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        btnChangePassword = new JButton("修改密码");
        btnChangePassword.setPreferredSize(new Dimension(120, 35));
        btnChangePassword.setFocusPainted(false);
        btnChangePassword.setBackground(new Color(52, 152, 219)); // 蓝色
        btnChangePassword.setForeground(Color.WHITE);
        btnChangePassword.setFont(new Font("微软雅黑", Font.BOLD, 12));
        btnChangePassword.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnChangePassword.addActionListener(e -> handleChangePassword());

        JButton btnEditInfo = new JButton("编辑");
        btnEditInfo.setPreferredSize(new Dimension(120, 35));
        btnEditInfo.setFocusPainted(false);
        btnEditInfo.setBackground(new Color(46, 204, 113)); // 绿色
        btnEditInfo.setForeground(Color.WHITE);
        btnEditInfo.setFont(new Font("微软雅黑", Font.BOLD, 12));
        btnEditInfo.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btnEditInfo.addActionListener(e -> toggleEditMode(panel, staffData, btnEditInfo));

        buttonPanel.add(btnChangePassword);
        buttonPanel.add(Box.createHorizontalStrut(10)); // 添加间距
        buttonPanel.add(btnEditInfo);

        mainContentPanel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(mainContentPanel, BorderLayout.CENTER);

        // 刷新界面
        panel.revalidate();
        panel.repaint();
    }

    private void addInfoRow(JPanel infoPanel, String label, Object value, GridBagConstraints gbc, int row, boolean editable) {
        // 计算列位置：偶数行在左列，奇数行在右列
        int column = (row % 2 == 0) ? 0 : 2;
        int actualRow = row / 2;
        
        // 创建一行容器
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setOpaque(false);
        rowPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        
        // 左侧标签
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("微软雅黑", Font.BOLD, 16)); // 增大字号
        labelComponent.setForeground(editable ? new Color(60, 60, 60) : new Color(100, 100, 100));
        labelComponent.setPreferredSize(new Dimension(120, 35)); // 增大高度
        labelComponent.setHorizontalAlignment(SwingConstants.LEFT);
        
        // 右侧值
        String displayValue;
        if (value == null) {
            displayValue = "未设置";
        } else {
            displayValue = value.toString();
        }
        JLabel valueComponent = new JLabel(displayValue);
        valueComponent.setFont(new Font("微软雅黑", Font.PLAIN, 16)); // 增大字号
        valueComponent.setForeground(editable ? new Color(40, 40, 40) : new Color(80, 80, 80));
        valueComponent.setName(label); // 设置名称用于识别
        valueComponent.setHorizontalAlignment(SwingConstants.LEFT);
        
        // 添加分隔线
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(new Color(220, 220, 220));
        
        // 组装行
        rowPanel.add(labelComponent, BorderLayout.WEST);
        rowPanel.add(valueComponent, BorderLayout.CENTER);
        rowPanel.add(separator, BorderLayout.SOUTH);
        
        // 添加到主面板
        gbc.gridx = column;
        gbc.gridy = actualRow;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        infoPanel.add(rowPanel, gbc);
        
        // 如果是可编辑的，存储组件引用
        if (editable) {
            editingComponents.put(label, valueComponent);
        }
    }

    // 重载方法以保持兼容性
    private void addInfoRow(JPanel infoPanel, String label, Object value, GridBagConstraints gbc, int row) {
        addInfoRow(infoPanel, label, value, gbc, row, false);
    }

    /**
     * 切换编辑模式
     */
    private void toggleEditMode(JPanel parentPanel, Map<String, Object> staffData, JButton editButton) {
        if (!isEditing) {
            // 进入编辑模式
            isEditing = true;
            editButton.setText("取消");
            editButton.setBackground(new Color(231, 76, 60)); // 红色
            editButton.setForeground(Color.WHITE);

            // 创建确认按钮
            JButton confirmButton = new JButton("确认");
            confirmButton.setPreferredSize(new Dimension(120, 35));
            confirmButton.setFocusPainted(false);
            confirmButton.setBackground(new Color(46, 204, 113)); // 绿色
            confirmButton.setForeground(Color.WHITE);
            confirmButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
            confirmButton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            confirmButton.addActionListener(e -> saveChanges(parentPanel, staffData, editButton));

            // 添加确认按钮到按钮面板
            JPanel buttonPanel = findButtonPanel(parentPanel);
            if (buttonPanel != null) {
                buttonPanel.add(confirmButton, 1); // 在"编辑"按钮前添加
                buttonPanel.revalidate();
                buttonPanel.repaint();
            }

            // 将可编辑的标签替换为输入框
            convertLabelsToInputs();

        } else {
            // 退出编辑模式
            exitEditMode(parentPanel, staffData, editButton);
        }
    }

    /**
     * 退出编辑模式
     */
    private void exitEditMode(JPanel parentPanel, Map<String, Object> staffData, JButton editButton) {
        isEditing = false;
        editButton.setText("编辑");
        editButton.setBackground(new Color(46, 204, 113)); // 恢复绿色
        editButton.setForeground(Color.WHITE);

        // 移除确认按钮
        JPanel buttonPanel = findButtonPanel(parentPanel);
        if (buttonPanel != null && buttonPanel.getComponentCount() > 2) {
            buttonPanel.remove(1); // 移除确认按钮
            buttonPanel.revalidate();
            buttonPanel.repaint();
        }

        // 重新显示信息
        displayStaffInfo(parentPanel, staffData);
    }

    /**
     * 查找按钮面板
     */
    private JPanel findButtonPanel(Container parent) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getLayout() instanceof BorderLayout) {
                    Component south = ((BorderLayout) panel.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
                    if (south instanceof JPanel && ((JPanel) south).getLayout() instanceof FlowLayout) {
                        return (JPanel) south;
                    }
                }
                // 递归查找
                JPanel result = findButtonPanel(panel);
                if (result != null) return result;
            }
        }
        return null;
    }

    /**
     * 将标签转换为输入框
     */
    private void convertLabelsToInputs() {
        for (Map.Entry<String, JComponent> entry : editingComponents.entrySet()) {
            String fieldName = entry.getKey();
            JComponent component = entry.getValue();

            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                String currentValue = label.getText();

                JComponent inputComponent;

                // 根据字段类型创建不同的输入组件
                if ("性别:".equals(fieldName)) {
                    JComboBox<String> comboBox = new JComboBox<>(new String[]{"男", "女"});
                    comboBox.setSelectedItem(currentValue);
                    comboBox.setFont(new Font("微软雅黑", Font.PLAIN, 16)); // 增大字号
                    inputComponent = comboBox;
                } else {
                    JTextField textField = new JTextField(currentValue);
                    textField.setFont(new Font("微软雅黑", Font.PLAIN, 16)); // 增大字号
                    inputComponent = textField;
                }

                // 替换组件 - 适应新的BorderLayout结构
                Container parent = component.getParent();
                if (parent != null) {
                    if (parent.getLayout() instanceof BorderLayout) {
                        // 新的布局结构：在BorderLayout的CENTER位置替换
                        parent.remove(component);
                        parent.add(inputComponent, BorderLayout.CENTER);
                        
                        // 更新引用
                        editingComponents.put(fieldName, inputComponent);
                    } else if (parent.getLayout() instanceof GridBagLayout) {
                        // 旧的布局结构：在GridBagLayout中替换
                        GridBagLayout layout = (GridBagLayout) parent.getLayout();
                        GridBagConstraints gbc = layout.getConstraints(component);

                        parent.remove(component);
                        parent.add(inputComponent, gbc);

                        // 更新引用
                        editingComponents.put(fieldName, inputComponent);
                    }
                }
            }
        }

        // 刷新界面
        SwingUtilities.invokeLater(() -> {
            Container topLevel = this;
            topLevel.revalidate();
            topLevel.repaint();
        });
    }

    /**
     * 保存更改
     */
    private void saveChanges(JPanel parentPanel, Map<String, Object> staffData, JButton editButton) {
        try {
            // 收集所有编辑的值
            Map<String, String> updatedValues = new HashMap<>();

            for (Map.Entry<String, JComponent> entry : editingComponents.entrySet()) {
                String fieldName = entry.getKey();
                JComponent component = entry.getValue();
                String value = "";

                if (component instanceof JTextField) {
                    value = ((JTextField) component).getText().trim();
                } else if (component instanceof JComboBox) {
                    value = ((JComboBox<?>) component).getSelectedItem().toString();
                }

                updatedValues.put(fieldName, value);
            }

            // 验证必填字段
            if (updatedValues.get("姓名:") == null || updatedValues.get("姓名:").isEmpty()) {
                JOptionPane.showMessageDialog(this, "姓名不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (updatedValues.get("电话:") == null || updatedValues.get("电话:").isEmpty()) {
                JOptionPane.showMessageDialog(this, "电话不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 提交更新
            submitStaffInfoUpdate(staffData, updatedValues, parentPanel, editButton);

        } catch (Exception e) {
            log.error("保存更改时发生错误", e);
            JOptionPane.showMessageDialog(this, "保存更改时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 提交教师信息更新 - 重构版本
     */
    private void submitStaffInfoUpdate(Map<String, Object> currentData, Map<String, String> updatedValues,
                                     JPanel parentPanel, JButton editButton) {
        try {
            Object cardNumObj = currentData.get("cardNum");
            String cardNumStr;

            if (cardNumObj instanceof Number) {
                java.math.BigDecimal bd = new java.math.BigDecimal(cardNumObj.toString());
                cardNumStr = bd.toPlainString();
            } else {
                cardNumStr = cardNumObj.toString();
            }

            log.info("提交教师信息修改请求，卡号: {}", cardNumStr);

            Request request = new Request("academic/staff")
                    .addParam("action", "UPDATE_STAFF_INFO")
                    .addParam("cardNum", cardNumStr)
                    .addParam("name", updatedValues.get("姓名:"))
                    .addParam("gender", updatedValues.get("性别:"))
                    .addParam("birthDate", updatedValues.get("出生年月:"))
                    .addParam("phone", updatedValues.get("电话:"))
                    .addParam("ethnicity", updatedValues.get("民族:"))
                    .addParam("idCard", updatedValues.get("身份证号:"))
                    .addParam("hometown", updatedValues.get("籍贯:"))
                    .addParam("workYear", updatedValues.get("参工年份:"));

            Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);

            if (response != null && "SUCCESS".equals(response.getStatus())) {
                SwingUtilities.invokeLater(() -> {
                    // 更新userData中的姓名
                    userData.put("userName", updatedValues.get("姓名:"));
                    // 更新侧边栏显示的姓名
                    lblStaffName.setText(updatedValues.get("姓名:"));

                    JOptionPane.showMessageDialog(this, "个人信息修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);

                    // 更新当前数据
                    currentData.put("name", updatedValues.get("姓名:"));
                    currentData.put("gender", updatedValues.get("性别:"));
                    currentData.put("birthDate", updatedValues.get("出生年月:"));
                    currentData.put("phone", updatedValues.get("电话:"));
                    currentData.put("ethnicity", updatedValues.get("民族:"));
                    currentData.put("idCard", updatedValues.get("身份证号:"));
                    currentData.put("hometown", updatedValues.get("籍贯:"));
                    currentData.put("workYear", updatedValues.get("参工年份:"));

                    // 退出编辑模式
                    exitEditMode(parentPanel, currentData, editButton);
                });
                log.info("教师 {} 个人信息修改成功", updatedValues.get("姓名:"));
            } else {
                String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "个人信息修改失败：" + errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                });
                log.warn("教师 {} 个人信息修改失败: {}", updatedValues.get("姓名:"), errorMsg);
            }

        } catch (Exception e) {
            log.error("修改教师个人信息时发生错误", e);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "修改个人信息时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    private void showError(JPanel panel, String message) {
        // 移除加载提示
        panel.removeAll();

        // 重新添加标题
        JLabel titleLabel = new JLabel("个人信息");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 显示错误信息
        JLabel errorLabel = new JLabel("<html><div style='text-align: center; color: red;'>" +
                message + "</div></html>", SwingConstants.CENTER);
        errorLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(errorLabel, BorderLayout.CENTER);

        panel.revalidate();
        panel.repaint();
    }

    // 新增：商城面板
    private JPanel createShopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 不再强制将 staff 标记为管理员，直接使用真实的 userData
        com.vcampus.client.core.ui.shop.ShopPanel shopPanel = new com.vcampus.client.core.ui.shop.ShopPanel(nettyClient, userData);
        // 将商城面板相比默认缩小约 3%：通过设置首选大小为 StaffFrame 的 97%
        int w = (int) Math.round(getWidth() > 0 ? getWidth() * 0.97 : 1080 * 0.89 * 97 / 100.0);
        int h = (int) Math.round(getHeight() > 0 ? getHeight() * 0.97 : 690 * 0.89 * 97 / 100.0);
        shopPanel.setPreferredSize(new Dimension(w, h));
        // 当窗口大小变化时，动态更新商城面板的首选尺寸（只添加一次监听器）
        if (!shopResizeListenerAdded) {
            shopResizeListenerAdded = true;
            this.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    try {
                        int nw = (int) Math.round(getWidth() * 0.97);
                        int nh = (int) Math.round(getHeight() * 0.97);
                        shopPanel.setPreferredSize(new Dimension(Math.max(400, nw), Math.max(300, nh)));
                        shopPanel.revalidate();
                    } catch (Exception ignored) {}
                }
            });
        }
        panel.add(shopPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCourseManagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("课程管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        panel.add(titleLabel, BorderLayout.NORTH);

        // 使用专门的教师课程管理面板
        try {
            com.vcampus.client.core.ui.teacher.TeacherCoursePanel teacherCoursePanel =
                new com.vcampus.client.core.ui.teacher.TeacherCoursePanel(nettyClient, userData);
            panel.add(teacherCoursePanel, BorderLayout.CENTER);
        } catch (Exception e) {
            log.error("创建教师课程面板时发生错误", e);
            JLabel errorLabel = new JLabel("课程管理功能加载失败：" + e.getMessage(), SwingConstants.CENTER);
            errorLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            errorLabel.setForeground(Color.RED);
            panel.add(errorLabel, BorderLayout.CENTER);
        }

        return panel;
    }

    /**
     * 创建 AI 助手面板
     */
    private JPanel createAIPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(420, 0));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
                "AI智能助手",
                javax.swing.border.TitledBorder.CENTER,
                javax.swing.border.TitledBorder.TOP,
                new Font("微软雅黑", Font.BOLD, 14),
                new Color(100, 149, 237)
        ));

        // 添加AI聊天面板
        com.vcampus.client.core.ui.AiChatAssistant.AiChatAssistantPanel chatPanel =
            new com.vcampus.client.core.ui.AiChatAssistant.AiChatAssistantPanel();
        panel.add(chatPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * 切换AI助手面板的显示/隐藏
     */
    private void toggleAIPanel() {
        // 确保分割面板连续布局，减少重绘闪烁
        mainSplitPane.setContinuousLayout(true);

        if (!aiVisible) {
            // 显示 AI 面板
            mainSplitPane.setRightComponent(aiPanel);
            int aiWidth = (aiPanel != null && aiPanel.getPreferredSize().width > 0) ? aiPanel.getPreferredSize().width : 420;
            int margin = 20;
            int divider = Math.max(100, getWidth() - aiWidth - margin);
            Component left = mainSplitPane.getLeftComponent();
            if (left != null) left.setMinimumSize(new Dimension(200, 100));
            if (aiPanel != null) aiPanel.setMinimumSize(new Dimension(200, 100));
            mainSplitPane.setDividerLocation(divider);
            mainSplitPane.setResizeWeight(1.0);
            aiVisible = true;
            if (btnAI != null) {
                btnAI.setText("隐藏AI");
                btnAI.setBackground(new Color(255, 193, 7));
            }
        } else {
            // 隐藏 AI 面板
            mainSplitPane.setRightComponent(null);
            mainSplitPane.setResizeWeight(1.0);
            aiVisible = false;
            if (btnAI != null) {
                btnAI.setText("AI助手");
                btnAI.setBackground(null);
            }
        }
        mainSplitPane.revalidate();
        mainSplitPane.repaint();

    }


    /**
     * 处理修改密码
     */
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

        // 设置确认修改按钮样式
        btnConfirm.setFocusPainted(false);
        btnConfirm.setBackground(new Color(46, 204, 113)); // 绿色
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("微软雅黑", Font.BOLD, 12));
        btnConfirm.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        // 设置取消按钮样式
        btnCancel.setFocusPainted(false);
        btnCancel.setBackground(new Color(231, 76, 60)); // 红色
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        btnCancel.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        buttonPanel.add(btnConfirm);
        buttonPanel.add(btnCancel);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);

        // 添加密码强度检测
        txtNewPassword.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePasswordStrength();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePasswordStrength();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
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

            // 发起修改密码请求
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
        // 使用SvgButton组件
        com.vcampus.client.core.ui.component.SvgButton toggleButton =
                new com.vcampus.client.core.ui.component.SvgButton("/figures/eye_close.svg");
        toggleButton.setPreferredSize(new Dimension(30, 30));
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setFocusPainted(false);

        // 设置初始状态（密码隐藏）
        toggleButton.setSvgIcon("/figures/eye_close.svg");

        toggleButton.addActionListener(e -> {
            if (passwordField.getEchoChar() == 0) {
                // 当前显示密码，切换为隐藏
                passwordField.setEchoChar('•');
                toggleButton.setSvgIcon("/figures/eye_close.svg");
            } else {
                // 当前隐藏密码，切换为显示
                passwordField.setEchoChar((char) 0);
                toggleButton.setSvgIcon("/figures/eye_open.svg");
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

            log.info("发送密码修改请求，用户卡号: {}", cardNumStr);

            Request request = new Request("auth/changepassword")
                    .addParam("cardNum", cardNumStr)
                    .addParam("oldPassword", oldPassword)
                    .addParam("newPassword", newPassword)
                    .addParam("userType", "staff"); // 添加用户类型参数

            // 发送请求，增加超时时间
            Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);

            if (response != null && "SUCCESS".equals(response.getStatus())) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(dialog, "密码修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                });
                log.info("用户 {} 密码修改成功", userData.get("userName"));
            } else {
                String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(dialog, "密码修改失败：" + errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                });
                log.warn("用户 {} 密码修改失败: {}", userData.get("userName"), errorMsg);
            }

        } catch (java.util.concurrent.TimeoutException e) {
            log.error("密码修改请求超时", e);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(dialog, "请求超时，请检查网络连接后重试", "超时错误", JOptionPane.ERROR_MESSAGE);
            });
        } catch (Exception e) {
            log.error("修改密码过程中发生错误", e);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(dialog, "修改密码时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    /**
     * 显示头像选择对话框
     */
    private void showAvatarSelectionDialog(JLabel sidebarAvatar, JLabel profileAvatar) {
        // 使用AvatarManager的头像选择对话框
        avatarManager.showAvatarSelectionDialog(sidebarAvatar, profileAvatar);
    }

    /**
     * 头像管理内部类
     */
    private class AvatarManager {
        private Map<String, Object> userData;
        private NettyClient nettyClient;
        private JLabel sidebarAvatarLabel;
        private JLabel profileAvatarLabel;

        public AvatarManager(Map<String, Object> userData, NettyClient nettyClient) {
            this.userData = userData;
            this.nettyClient = nettyClient;
        }

        /**
         * 加载用户头像
         */
        public void loadUserAvatar(JLabel avatarLabel, int width, int height) {
            try {
                String avatarPath = userData.get("avatar") != null ? userData.get("avatar").toString() : null;

                if (avatarPath != null && !avatarPath.isEmpty()) {
                    File avatarFile = new File(avatarPath);
                    if (avatarFile.exists() && avatarFile.canRead()) {
                        BufferedImage avatarImage = ImageIO.read(avatarFile);
                        Image scaledImage = avatarImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                        avatarLabel.setIcon(new ImageIcon(scaledImage));
                        return;
                    }
                }

                String gender = userData.get("gender") != null ? userData.get("gender").toString() : "男";
                ImageIcon defaultAvatar = createDefaultAvatar(width, height, gender);
                avatarLabel.setIcon(defaultAvatar);

            } catch (Exception e) {
                log.error("加载用户头像时发生错误", e);
                String gender = userData.get("gender") != null ? userData.get("gender").toString() : "男";
                ImageIcon defaultAvatar = createDefaultAvatar(width, height, gender);
                avatarLabel.setIcon(defaultAvatar);
            }
        }

        /**
         * 显示头像选择对话框
         */
        public void showAvatarSelectionDialog(JLabel sidebarAvatar, JLabel profileAvatar) {
            this.sidebarAvatarLabel = sidebarAvatar;
            this.profileAvatarLabel = profileAvatar;

            JDialog dialog = new JDialog(StaffFrame.this, "更换头像", true);
            dialog.setSize(500, 450);
            dialog.setLocationRelativeTo(StaffFrame.this);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel previewPanel = new JPanel(new BorderLayout());
            previewPanel.setBorder(BorderFactory.createTitledBorder("头像预览"));

            JLabel lblPreview = new JLabel();
            lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
            lblPreview.setPreferredSize(new Dimension(120, 120));
            previewPanel.add(lblPreview, BorderLayout.CENTER);
            mainPanel.add(previewPanel, BorderLayout.CENTER);

            loadUserAvatar(lblPreview, 120, 120);

            JPanel infoPanel = new JPanel();
            JLabel infoLabel = new JLabel("<html><div style='text-align: center; color: gray;'>" +
                    "支持JPG、PNG格式图片<br>文件大小不超过2MB</div></html>");
            infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            infoPanel.add(infoLabel);
            mainPanel.add(infoPanel, BorderLayout.NORTH);

            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton btnUpload = new JButton("选择新头像");
            JButton btnReset = new JButton("恢复默认");
            JButton btnCancel = new JButton("取消");

            buttonPanel.add(btnUpload);
            buttonPanel.add(btnReset);
            buttonPanel.add(btnCancel);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            btnUpload.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("选择头像文件");
                fileChooser.setAcceptAllFileFilterUsed(false);
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("图片文件 (*.jpg, *.jpeg, *.png)", "jpg", "jpeg", "png"));

                int result = fileChooser.showOpenDialog(dialog);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    if (isValidImageFile(selectedFile)) {
                        try {
                            BufferedImage selectedImage = ImageIO.read(selectedFile);
                            Image scaledImage = selectedImage.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                            lblPreview.setIcon(new ImageIcon(scaledImage));

                            int confirm = JOptionPane.showConfirmDialog(dialog, "确认要更换头像吗？", "确认", JOptionPane.YES_NO_OPTION);
                            if (confirm == JOptionPane.YES_OPTION) {
                                updateAvatarWithFile(selectedFile, dialog);
                            }
                        } catch (IOException ex) {
                            log.error("读取图片文件时发生错误", ex);
                        }
                    }
                }
            });

            btnReset.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(dialog, "确认要恢复默认头像吗？", "确认", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    resetToDefaultAvatar(dialog, lblPreview);
                }
            });

            btnCancel.addActionListener(e -> dialog.dispose());

            dialog.add(mainPanel);
            dialog.setVisible(true);
        }

        private boolean isValidImageFile(File file) {
            if (!file.exists() || !file.isFile()) return false;
            if (file.length() > 2 * 1024 * 1024) return false;
            String fileName = file.getName().toLowerCase();
            return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png");
        }

        private void updateAvatarWithFile(File selectedFile, JDialog dialog) {
            try {
                String userDir = System.getProperty("user.home");
                Path avatarDir = Paths.get(userDir, ".vcampus", "avatars");
                Files.createDirectories(avatarDir);

                String cardNum = userData.get("cardNum").toString();
                String extension = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.'));
                String newFileName = "avatar_" + cardNum + "_" + System.currentTimeMillis() + extension;
                Path newAvatarPath = avatarDir.resolve(newFileName);

                Files.copy(selectedFile.toPath(), newAvatarPath, StandardCopyOption.REPLACE_EXISTING);
                updateAvatarOnServer(newAvatarPath.toString(), dialog);
            } catch (Exception e) {
                log.error("复制头像文件时发生错误", e);
            }
        }

        private void resetToDefaultAvatar(JDialog dialog, JLabel previewLabel) {
            String gender = userData.get("gender") != null ? userData.get("gender").toString() : "男";
            ImageIcon defaultAvatar = createDefaultAvatar(120, 120, gender);
            previewLabel.setIcon(defaultAvatar);
            updateAvatarOnServer("", dialog); // 空路径表示默认
        }

        private void updateAvatarOnServer(String avatarPath, JDialog dialog) {
            new Thread(() -> {
                try {
                    Request request = new Request("auth/updateAvatar")
                            .addParam("cardNum", userData.get("cardNum").toString())
                            .addParam("avatar", avatarPath);

                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);

                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        SwingUtilities.invokeLater(() -> {
                            userData.put("avatar", avatarPath);
                            updateAvatarDisplay(avatarPath);
                            dialog.dispose();
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(dialog, "头像更换失败", "错误", JOptionPane.ERROR_MESSAGE));
                    }
                } catch (Exception e) {
                    log.error("更换头像时发生错误", e);
                }
            }).start();
        }

        private void updateAvatarDisplay(String newAvatarPath) {
            if (profileAvatarLabel != null) {
                loadUserAvatar(profileAvatarLabel, 120, 120);
            }
            // 更新侧边栏主头像
            Component[] topComponents = sidebar.topPanel.getComponents();
            for(Component tc : topComponents) {
                if(tc instanceof JPanel && "avatarContent".equals(tc.getName())) {
                    Component[] avatarComponents = ((JPanel) tc).getComponents();
                    for(Component ac : avatarComponents) {
                        if(ac instanceof JLabel) {
                            loadUserAvatar((JLabel)ac, 60, 60);
                            break;
                        }
                    }
                    break;
                }
            }
        }

        private ImageIcon createDefaultAvatar(int width, int height, String gender) {
            try {
                String role = "staff"; // 教职工界面默认为staff
                String avatarResource = getDefaultAvatarResource(role, gender);
                InputStream imageStream = getClass().getResourceAsStream(avatarResource);
                if (imageStream != null) {
                    BufferedImage originalImage = ImageIO.read(imageStream);
                    Image scaledImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaledImage);
                }
            } catch (Exception e) {
                log.warn("加载默认头像资源失败: " + e.getMessage());
            }
            return new ImageIcon(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
        }

        private String getDefaultAvatarResource(String role, String gender) {
            String rolePrefix = "staff";
            String genderSuffix = "女".equals(gender) ? "_female" : "_male";
            return String.format("/figures/%s%s.png", rolePrefix, genderSuffix);
        }
    }
}

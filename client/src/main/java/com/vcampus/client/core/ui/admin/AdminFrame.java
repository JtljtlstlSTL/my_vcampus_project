package com.vcampus.client.core.ui.admin;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.client.core.ui.login.LoginFrame;
import com.vcampus.client.core.ui.shop.ShopManagerPanel;
import com.vcampus.client.core.ui.userAdmin.StatusPanel;
import com.vcampus.client.core.ui.eduAdmin.eduStatus;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.RowFilter;
import javax.swing.table.TableRowSorter;

/**
 * 管理员用户主界面
 *
 * @author VCampus Team
 * @version 2.0
 */
@Slf4j
public class AdminFrame extends JFrame {
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
    private Map<String, Object> currentAdminData;

    // 头像管理
    private AvatarManager avatarManager;

    // UI组件
    private JButton btnChangePassword;
    private JLabel lblAdminName;
    private boolean shopResizeListenerAdded = false;

    // AI 分屏相关字段
    private JPanel aiPanel;
    private JSplitPane mainSplitPane;
    private boolean aiVisible = false;

    private Timer statusTimer;

    public AdminFrame(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        this.avatarManager = new AvatarManager(userData, nettyClient);

        initModernUI();
        setupEventHandlers();
        log.info("现代化管理员主界面初始化完成: {}", userData.get("userName"));
    }

    /**
     * 初始化现代化UI
     */
    private void initModernUI() {
        setTitle("VCampus - 管理员端");

        // 设置现代化窗口样式
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int targetW = Math.min(1280, screen.width - 100);
        int targetH = Math.min(820, screen.height - 100);
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

        // 中间添加功能按钮
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setOpaque(false);

        JButton logoutButton = new JButton("退出登录");
        styleStatusBarButton(logoutButton, DANGER_COLOR);
        logoutButton.addActionListener(e -> handleLogout());

        centerPanel.add(logoutButton);
        statusBar.add(centerPanel, BorderLayout.CENTER);

        JLabel timeLabel = new JLabel(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        timeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        timeLabel.setForeground(TEXT_SECONDARY);
        statusBar.add(timeLabel, BorderLayout.EAST);

        // 创建定时器更新时间
        statusTimer = new Timer(1000, e -> {
            timeLabel.setText(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date()));
        });
        statusTimer.start();

        return statusBar;
    }

    private void styleStatusBarButton(JButton button, Color color) {
        button.setFont(new Font("微软雅黑", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 28));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
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
        private ModernMenuItem profileItem, userManageItem, systemManageItem, courseManageItem, eduManageItem, shopManageItem, libraryManageItem;

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
            JScrollPane scrollPane = new JScrollPane(menuPanel);
            scrollPane.setBorder(null);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.getViewport().setBackground(CARD_COLOR);
            add(scrollPane, BorderLayout.CENTER);


            // 修改鼠标监听器，支持点击锁定
            MouseAdapter sidebarMouseAdapter = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isExpanded && !isLocked) {
                        expand();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    // 当鼠标离开侧边栏区域时，检查是否需要收起
                    // 使用 getComponent() 获取事件源，然后获取其在屏幕上的位置和尺寸
                    Component component = (Component) e.getSource();
                    Point locationOnScreen = component.getLocationOnScreen();
                    Point mouseOnScreen = e.getLocationOnScreen();

                    // 创建一个代表组件边界的矩形
                    Rectangle bounds = new Rectangle(locationOnScreen, component.getSize());

                    // 如果鼠标指针仍在组件边界内，则不执行任何操作
                    if (bounds.contains(mouseOnScreen)) {
                        return;
                    }

                    // 只有在未锁定状态下才自动收缩
                    if (isExpanded && !isLocked) {
                        // 检查鼠标是否真的离开了整个侧边栏
                        Point sidebarMousePos = SwingUtilities.convertPoint(component, e.getPoint(), ModernSidebar.this);
                        if (!ModernSidebar.this.contains(sidebarMousePos)) {
                            collapse();
                        }
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
            };

            // 将监听器添加到侧边栏本身、滚动面板和菜单面板
            addMouseListener(sidebarMouseAdapter);
            scrollPane.addMouseListener(sidebarMouseAdapter);
            menuPanel.addMouseListener(sidebarMouseAdapter);
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
                    avatarManager.showAvatarSelectionDialog(avatarLabel, null);
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

            lblAdminName = new JLabel(userData.get("userName").toString());
            lblAdminName.setFont(new Font("微软雅黑", Font.BOLD, 14));
            lblAdminName.setForeground(TEXT_PRIMARY);
            lblAdminName.setAlignmentX(Component.CENTER_ALIGNMENT);

            Object cardNumObj = userData.get("cardNum");
            String cardNumText = "";
            if (cardNumObj instanceof Number) {
                cardNumText = String.format("%.0f", ((Number)cardNumObj).doubleValue());
            } else if (cardNumObj != null) {
                cardNumText = cardNumObj.toString();
            }
            JLabel idLabel = new JLabel("(" + cardNumText + ")");
            idLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            idLabel.setForeground(TEXT_SECONDARY);
            idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            infoPanel.add(Box.createVerticalStrut(5));
            infoPanel.add(lblAdminName);
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

            // 获取卡号首位
            String cardNumFirstDigit = getCardNumFirstDigit();
            log.info("管理员卡号首位: {}", cardNumFirstDigit);

            profileItem = new ModernMenuItem("👤", "个人信息", () -> switchToModule("profile"));
            userManageItem = new ModernMenuItem("👥", "用户管理", () -> switchToModule("user"));
            systemManageItem = new ModernMenuItem("🎓", "学籍管理", () -> switchToModule("system"));
            courseManageItem = new ModernMenuItem("📚", "课程管理", () -> switchToModule("course"));
            eduManageItem = new ModernMenuItem("📊", "教务管理", () -> switchToModule("edu"));
            shopManageItem = new ModernMenuItem("🛒", "商城管理", () -> switchToModule("shop"));
            libraryManageItem = new ModernMenuItem("📖", "图书管理", () -> switchToModule("library"));

            // 根据卡号首位条件性添加菜单项
            if ("2".equals(cardNumFirstDigit)) {
                // 卡号首位为2，显示个人信息和图书管理
                profileItem.setSelected(true);
                panel.add(profileItem);
                panel.add(Box.createVerticalStrut(2));
                panel.add(libraryManageItem);
                log.info("管理员权限：显示个人信息和图书管理");
            } else if ("3".equals(cardNumFirstDigit)) {
                // 卡号首位为3，显示个人信息和商店管理
                profileItem.setSelected(true);
                panel.add(profileItem);
                panel.add(Box.createVerticalStrut(2));
                panel.add(shopManageItem);
                log.info("管理员权限：显示个人信息和商店管理");
            } else {
                // 其他情况（包括首位为1），显示所有基础菜单项
                profileItem.setSelected(true);
                panel.add(profileItem);
                panel.add(Box.createVerticalStrut(2));
                panel.add(userManageItem);
                panel.add(Box.createVerticalStrut(2));
                panel.add(systemManageItem);
                panel.add(Box.createVerticalStrut(2));
                panel.add(courseManageItem);
                panel.add(Box.createVerticalStrut(2));
                panel.add(eduManageItem);
                
                if (!"1".equals(cardNumFirstDigit)) {
                    // 卡号首位不是1，还显示商店管理和图书管理
                    panel.add(Box.createVerticalStrut(2));
                    panel.add(shopManageItem);
                    panel.add(Box.createVerticalStrut(2));
                    panel.add(libraryManageItem);
                    log.info("管理员权限：显示所有管理功能");
                } else {
                    // 卡号首位为1，不显示图书管理和商店管理
                    log.info("管理员权限：不显示图书管理和商店管理");
                }
            }

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
            userManageItem.updateDisplayModeWithAlpha(isExpanded, textAlpha);
            systemManageItem.updateDisplayModeWithAlpha(isExpanded, textAlpha);
            courseManageItem.updateDisplayModeWithAlpha(isExpanded, textAlpha);
            eduManageItem.updateDisplayModeWithAlpha(isExpanded, textAlpha);
            shopManageItem.updateDisplayModeWithAlpha(isExpanded, textAlpha);
            libraryManageItem.updateDisplayModeWithAlpha(isExpanded, textAlpha);
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
         * 获取卡号首位数字
         */
        private String getCardNumFirstDigit() {
            try {
                Object cardNumObj = userData.get("cardNum");
                if (cardNumObj == null) {
                    log.warn("用户数据中未找到卡号信息");
                    return "";
                }
                
                String cardNumStr;
                if (cardNumObj instanceof Number) {
                    cardNumStr = new java.math.BigDecimal(cardNumObj.toString()).toPlainString();
                } else {
                    cardNumStr = cardNumObj.toString();
                }
                
                if (cardNumStr.isEmpty()) {
                    log.warn("卡号为空");
                    return "";
                }
                
                return cardNumStr.substring(0, 1);
            } catch (Exception e) {
                log.error("获取卡号首位数字时发生错误", e);
                return "";
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


        public void setSelectedMenuItem(String module) {
            // 获取卡号首位
            String cardNumFirstDigit = getCardNumFirstDigit();
            
            if ("2".equals(cardNumFirstDigit)) {
                // 卡号首位为2，设置个人信息和图书管理
                profileItem.setSelected("profile".equals(module));
                libraryManageItem.setSelected("library".equals(module));
            } else if ("3".equals(cardNumFirstDigit)) {
                // 卡号首位为3，设置个人信息和商店管理
                profileItem.setSelected("profile".equals(module));
                shopManageItem.setSelected("shop".equals(module));
            } else {
                // 其他情况，设置所有基础菜单项
                profileItem.setSelected("profile".equals(module));
                userManageItem.setSelected("user".equals(module));
                systemManageItem.setSelected("system".equals(module));
                courseManageItem.setSelected("course".equals(module));
                eduManageItem.setSelected("edu".equals(module));
                
                if (!"1".equals(cardNumFirstDigit)) {
                    // 卡号首位不是1，还设置商店管理和图书管理
                    shopManageItem.setSelected("shop".equals(module));
                    libraryManageItem.setSelected("library".equals(module));
                }
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
     * 图标工具类
     */
    private static class IconUtils {
        public static ImageIcon createIcon(String text, Color color, int size) {
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size - 4));
            FontMetrics fm = g2d.getFontMetrics();
            int x = (size - fm.stringWidth(text)) / 2;
            int y = (size - fm.getHeight()) / 2 + fm.getAscent();
            g2d.drawString(text, x, y);
            g2d.dispose();
            return new ImageIcon(image);
        }
    }

    private ImageIcon createColoredIcon(String text, Color color) {
        return IconUtils.createIcon(text, color, 16);
    }

    /**
     * 切换模块
     */
    private void switchToModule(String module) {
        sidebar.setSelectedMenuItem(module);
        contentPanel.removeAll();
        JPanel panel = null;
        switch (module) {
            case "profile":
                panel = createProfilePanel();
                break;
            case "user":
                panel = new StatusPanel(nettyClient, userData, AdminFrame.this).getMainPanel();
                break;
            case "system":
                panel = new eduStatus(nettyClient);
                break;
            case "edu":
                panel = createEduManagePanel();
                break;
            case "shop":
                panel = new ShopManagerPanel(nettyClient, userData);
                break;
            case "course":
                panel = new com.vcampus.client.core.ui.courseAdmin.CourseManagePanel(nettyClient, userData);
                break;
            case "library":
                Object currentRole = userData.get("primaryRole");
                if (currentRole == null || "manager".equals(currentRole.toString())) {
                    userData.put("primaryRole", "admin");
                }
                if (nettyClient != null && nettyClient.getCurrentSession() != null) {
                    nettyClient.getCurrentSession().addRole("admin");
                }
                panel = new com.vcampus.client.core.ui.library.LibraryPanel(nettyClient, userData);
                break;
        }
        if (panel != null) {
            contentPanel.add(panel, BorderLayout.CENTER);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(AdminFrame.this,
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
            if (statusTimer != null && statusTimer.isRunning()) {
                statusTimer.stop();
            }
            new Thread(() -> {
                try {
                    Request logoutRequest = new Request("auth/logout");
                    nettyClient.sendRequest(logoutRequest).get(2, java.util.concurrent.TimeUnit.SECONDS);
                } catch (Exception ex) {
                    log.warn("后台退出登录请求失败", ex);
                }
            }).start();
            this.dispose();
            new LoginFrame().setVisible(true);
            log.info("退出登录完成，已返回登录界面");
        } catch (Exception e) {
            log.error("退出登录过程中发生错误", e);
            System.exit(0);
        }
    }

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

        // 在后台线程中加载完整的管理员信息
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

                    log.info("开始加载管理员信息，卡号: {}", cardNumStr);

                    // 发送请求获取管理员信息
                    Request request = new Request("auth/getAdminByCardNum")
                            .addParam("cardNum", cardNumStr);

                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);

                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> adminData = (Map<String, Object>) response.getData();
                        SwingUtilities.invokeLater(() -> displayAdminInfo(panel, adminData));
                        log.info("管理员信息加载成功");
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                        SwingUtilities.invokeLater(() -> showError(panel, "加载个人信息失败：" + errorMsg));
                        log.warn("管理员信息加载失败: {}", errorMsg);
                    }

                } catch (Exception e) {
                    log.error("加载管理员信息时发生错误", e);
                    SwingUtilities.invokeLater(() -> showError(panel, "加载个人信息时发生错误：" + e.getMessage()));
                }
            }).start();
        });

        return panel;
    }

    /**
     * 显示管理员信息
     */
    private void displayAdminInfo(JPanel panel, Map<String, Object> adminData) {
        // 保存当前管理员数据
        this.currentAdminData = adminData;

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
                avatarManager.showAvatarSelectionDialog(null, avatarLabel);
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
                "管理员详细信息",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("微软雅黑", Font.BOLD, 16),
                new Color(70, 130, 180)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12); // 优化间距，更紧凑
        gbc.anchor = GridBagConstraints.WEST;

        // 基本信息 - 不可编辑的字段
        addInfoRow(infoPanel, "卡号:", adminData.get("cardNum"), gbc, 0, false);
        addInfoRow(infoPanel, "角色:", translateRole(adminData.get("primaryRole")), gbc, 1, false);

        // 可编辑的字段
        addInfoRow(infoPanel, "姓名:", adminData.get("name"), gbc, 2, true);
        addInfoRow(infoPanel, "性别:", adminData.get("gender"), gbc, 3, true);
        addInfoRow(infoPanel, "电话:", adminData.get("phone"), gbc, 4, true);

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
        btnEditInfo.addActionListener(e -> toggleEditMode(panel, adminData, btnEditInfo));

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

    /**
     * 切换编辑模式
     */
    private void toggleEditMode(JPanel parentPanel, Map<String, Object> adminData, JButton editButton) {
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
            confirmButton.addActionListener(e -> saveChanges(parentPanel, adminData, editButton));

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
            exitEditMode(parentPanel, adminData, editButton);
        }
    }

    /**
     * 退出编辑模式
     */
    private void exitEditMode(JPanel parentPanel, Map<String, Object> adminData, JButton editButton) {
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
        displayAdminInfo(parentPanel, adminData);
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
    private void saveChanges(JPanel parentPanel, Map<String, Object> adminData, JButton editButton) {
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
            submitAdminInfoUpdate(adminData, updatedValues, parentPanel, editButton);

        } catch (Exception e) {
            log.error("保存更改时发生错误", e);
            JOptionPane.showMessageDialog(this, "保存更改时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 提交管理员信息更新
     */
    private void submitAdminInfoUpdate(Map<String, Object> currentData, Map<String, String> updatedValues,
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

            log.info("提交管理员信息修改请求，卡号: {}", cardNumStr);

            Request request = new Request("auth/updateAdminInfo")
                    .addParam("cardNum", cardNumStr)
                    .addParam("name", updatedValues.get("姓名:"))
                    .addParam("gender", updatedValues.get("性别:"))
                    .addParam("phone", updatedValues.get("电话:"));

            Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);

            if (response != null && "SUCCESS".equals(response.getStatus())) {
                SwingUtilities.invokeLater(() -> {
                    // 更新userData中的姓名
                    userData.put("userName", updatedValues.get("姓名:"));
                    // 更新侧边栏显示的姓名
                    lblAdminName.setText(updatedValues.get("姓名:"));

                    JOptionPane.showMessageDialog(this, "个人信息修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);

                    // 更新当前数据
                    currentData.put("name", updatedValues.get("姓名:"));
                    currentData.put("gender", updatedValues.get("性别:"));
                    currentData.put("phone", updatedValues.get("电话:"));

                    // 退出编辑模式
                    exitEditMode(parentPanel, currentData, editButton);
                });
                log.info("管理员 {} 个人信息修改成功", updatedValues.get("姓名:"));
            } else {
                String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "个人信息修改失败：" + errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                });
                log.warn("管理员 {} 个人信息修改失败: {}", updatedValues.get("姓名:"), errorMsg);
            }

        } catch (Exception e) {
            log.error("修改管理员个人信息时发生错误", e);
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


    private void styleModernButton(JButton button, Color color) {
        button.setFont(new Font("微软雅黑", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(color.brighter()); }
            public void mouseExited(MouseEvent e) { button.setBackground(color); }
        });
    }

    private JPanel createEduManagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(SURFACE_COLOR);

        JLabel titleLabel = new JLabel("教务管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        panel.add(titleLabel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        tabbedPane.addTab("学生成绩", createStudentGradesTablePanel());
        tabbedPane.addTab("教师评教", createTeacherEvaluationTablePanel());
        panel.add(tabbedPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            loadStudentGrades();
            loadTeacherEvaluations();
        });
        return panel;
    }

    private JPanel createStudentGradesTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        panel.add(createStudentGradesToolbar(), BorderLayout.NORTH);
        String[] columns = {"卡号", "学号", "姓名", "出生年月", "性别", "入学年份", "专业", "学院", "民族", "身份证号", "籍贯", "电话", "均分", "平均GPA"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);
        setupModernTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.putClientProperty("table", table);
        return panel;
    }

    private JPanel createTeacherEvaluationTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        panel.add(createTeacherEvaluationToolbar(), BorderLayout.NORTH);
        String[] columns = {"教学班ID", "课程名称", "学分", "开课学院", "学期", "教师工号", "教师姓名", "教室", "容量", "上课时间", "评教分数"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);
        setupModernTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.putClientProperty("table", table);
        return panel;
    }

    private JPanel createStudentGradesToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setOpaque(false);
        JButton refreshBtn = new JButton("刷新");
        styleModernButton(refreshBtn, SECONDARY_COLOR);
        refreshBtn.addActionListener(e -> loadStudentGrades());
        JTextField searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "按姓名、学号等搜索...");
        toolbar.add(refreshBtn);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(new JLabel("搜索:"));
        toolbar.add(searchField);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { searchStudentGrades(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { searchStudentGrades(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { searchStudentGrades(searchField.getText()); }
        });
        return toolbar;
    }

    private JPanel createTeacherEvaluationToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setOpaque(false);
        JButton refreshBtn = new JButton("刷新");
        styleModernButton(refreshBtn, SECONDARY_COLOR);
        refreshBtn.addActionListener(e -> loadTeacherEvaluations());
        JTextField searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "按课程、教师等搜索...");
        toolbar.add(refreshBtn);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(new JLabel("搜索:"));
        toolbar.add(searchField);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { searchTeacherEvaluations(searchField.getText()); }
            public void removeUpdate(DocumentEvent e) { searchTeacherEvaluations(searchField.getText()); }
            public void changedUpdate(DocumentEvent e) { searchTeacherEvaluations(searchField.getText()); }
        });
        return toolbar;
    }

    private void loadStudentGrades() {
        log.info("加载学生数据");
        Request studentRequest = new Request("ACADEMIC").addParam("action", "GET_ALL_STUDENTS_WITH_USER_INFO");
        nettyClient.sendRequest(studentRequest).thenAccept(res -> SwingUtilities.invokeLater(() -> {
            if (res != null && res.isSuccess()) {
                displayStudentDataInTable(res);
            } else {
                showError("加载学生数据失败: " + (res != null ? res.getMessage() : "未知错误"));
            }
        }));
    }

    private void loadTeacherEvaluations() {
        log.info("加载教学班数据");
        Request request = new Request("academic/course").addParam("action", "GET_ALL_SECTIONS");
        nettyClient.sendRequest(request).thenAccept(res -> SwingUtilities.invokeLater(() -> {
            if (res != null && res.isSuccess()) {
                displaySectionDataInTable(res);
            } else {
                showError("加载教学班数据失败: " + (res != null ? res.getMessage() : "未知错误"));
            }
        }));
    }

    private void displayStudentDataInTable(Response response) {
        JTable table = findTableInPanel("学生成绩");
        if (table == null) return;
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        java.util.List<Map<String, Object>> students = (java.util.List<Map<String, Object>>) response.getData();
        if (students == null) return;

        for (Map<String, Object> student : students) {
            String studentId = String.valueOf(student.get("studentId"));
            model.addRow(new Object[]{
                student.get("cardNum"), studentId, student.get("name"), student.get("birthDate"),
                student.get("gender"), student.get("enrollmentYear"), student.get("major"),
                student.get("department"), student.get("ethnicity"), student.get("idCard"),
                student.get("hometown"), student.get("phone"), "", ""
            });
            Request gradeRequest = new Request("course/manager")
                .addParam("action", "GET_STUDENT_AVERAGE_GRADES").addParam("studentId", studentId);
            nettyClient.sendRequest(gradeRequest).thenAccept(gradeRes -> SwingUtilities.invokeLater(() -> {
                if (gradeRes != null && "SUCCESS".equals(gradeRes.getStatus())) {
                    Map<String, Object> gradeData = (Map<String, Object>) gradeRes.getData();
                    if (gradeData == null) return;
                    String avgScore = (gradeData.get("avgScore") instanceof Number && ((Number)gradeData.get("avgScore")).doubleValue() > 0) ? String.format("%.4f", gradeData.get("avgScore")) : "";
                    String avgGPA = (gradeData.get("avgGPA") instanceof Number && ((Number)gradeData.get("avgGPA")).doubleValue() > 0) ? String.format("%.4f", gradeData.get("avgGPA")) : "";
                    updateStudentGradeInTable(studentId, avgScore, avgGPA);
                }
            }));
        }
    }

    private void updateStudentGradeInTable(String studentId, String avgScore, String avgGPA) {
        JTable table = findTableInPanel("学生成绩");
        if (table == null) return;
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (studentId.equals(String.valueOf(model.getValueAt(i, 1)))) {
                model.setValueAt(avgScore, i, 12);
                model.setValueAt(avgGPA, i, 13);
                break;
            }
        }
    }

    private void displaySectionDataInTable(Response response) {
        JTable table = findTableInPanel("教师评教");
        if (table == null) return;
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        java.util.List<Map<String, Object>> sections = (java.util.List<Map<String, Object>>) response.getData();
        if (sections == null) return;

        for (Map<String, Object> section : sections) {
            Object avgEvalScoreObj = section.get("avgEvalScore");
            String evalScore = "";
            if (avgEvalScoreObj instanceof Number && ((Number) avgEvalScoreObj).doubleValue() > 0) {
                evalScore = String.format("%.2f", ((Number) avgEvalScoreObj).doubleValue());
            }
            model.addRow(new Object[]{
                section.get("section_Id"), section.get("courseName"), section.get("Credit"),
                section.get("Department"), section.get("Term"), section.get("Teacher_id"),
                section.get("teacher"), section.get("Room"), section.get("Capacity"),
                section.get("Schedule"), evalScore
            });
        }
    }

    private JTable findTableInPanel(String tabTitle) {
        return findTableInPanelRecursive(contentPanel, tabTitle);
    }

    private JTable findTableInPanelRecursive(Container parent, String tabTitle) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) comp;
                int tabIndex = tabbedPane.indexOfTab(tabTitle);
                if (tabIndex != -1) {
                    Component tabComponent = tabbedPane.getComponentAt(tabIndex);
                    if (tabComponent instanceof JPanel) {
                        Object tableObj = ((JPanel) tabComponent).getClientProperty("table");
                        if (tableObj instanceof JTable) return (JTable) tableObj;
                      }
                    }
                  } else if (comp instanceof Container) {
                    JTable result = findTableInPanelRecursive((Container) comp, tabTitle);
                    if (result != null) return result;
                  }
                }
                return null;
              }

    private void searchStudentGrades(String keyword) {
        JTable table = findTableInPanel("学生成绩");
        if (table == null) return;
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) table.getModel());
        table.setRowSorter(sorter);
        if (keyword.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
        }
    }

    private void searchTeacherEvaluations(String keyword) {
        JTable table = findTableInPanel("教师评教");
        if (table == null) return;
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>((DefaultTableModel) table.getModel());
        table.setRowSorter(sorter);
        if (keyword.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + keyword));
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }

    private void handleChangePassword() {
        JDialog dialog = new JDialog(this, "修改密码", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JPasswordField txtOldPassword = new JPasswordField(20);
        JPasswordField txtNewPassword = new JPasswordField(20);
        JPasswordField txtConfirmPassword = new JPasswordField(20);

        formPanel.add(new JLabel("原密码:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(createPasswordPanel(txtOldPassword), gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("新密码:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(createPasswordPanel(txtNewPassword), gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("确认新密码:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(createPasswordPanel(txtConfirmPassword), gbc);

        JLabel lblPasswordStrength = new JLabel("未输入");
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("密码强度:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(lblPasswordStrength, gbc);

        JTextArea passwordRequirements = new JTextArea("密码要求：\n• 长度至少8个字符\n• 必须包含数字、大小写字母、特殊符号中至少两种");
        passwordRequirements.setEditable(false);
        passwordRequirements.setOpaque(false);
        passwordRequirements.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        passwordRequirements.setForeground(Color.GRAY);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(passwordRequirements, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnConfirm = new JButton("确认修改");
        styleModernButton(btnConfirm, ACCENT_COLOR);
        JButton btnCancel = new JButton("取消");
        styleModernButton(btnCancel, DANGER_COLOR);
        buttonPanel.add(btnConfirm);
        buttonPanel.add(btnCancel);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);

        txtNewPassword.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateStrength(); }
            public void removeUpdate(DocumentEvent e) { updateStrength(); }
            public void changedUpdate(DocumentEvent e) { updateStrength(); }
            void updateStrength() {
                String password = new String(txtNewPassword.getPassword());
                int score = calculatePasswordStrength(password);
                if (password.isEmpty()) {
                    lblPasswordStrength.setText("未输入"); lblPasswordStrength.setForeground(Color.GRAY);
                } else if (score < 2) {
                    lblPasswordStrength.setText("弱"); lblPasswordStrength.setForeground(Color.RED);
                } else if (score == 2) {
                    lblPasswordStrength.setText("中等"); lblPasswordStrength.setForeground(Color.ORANGE);
                } else {
                    lblPasswordStrength.setText("强"); lblPasswordStrength.setForeground(new Color(0, 150, 0));
                }
            }
        });

        btnConfirm.addActionListener(e -> {
            String oldPassword = new String(txtOldPassword.getPassword());
            String newPassword = new String(txtNewPassword.getPassword());
            String confirmPassword = new String(txtConfirmPassword.getPassword());
            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showError("请填写所有密码字段！"); return;
            }
            if (!newPassword.equals(confirmPassword)) {
                showError("新密码与确认密码不一致！"); return;
            }
            if (!isPasswordValid(newPassword)) {
                showError("新密码不符合强度要求！"); return;
            }
            changePassword(oldPassword, newPassword, dialog);
        });
        btnCancel.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private JPanel createPasswordPanel(JPasswordField passwordField) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(passwordField, BorderLayout.CENTER);
        com.vcampus.client.core.ui.component.SvgButton toggleButton = new com.vcampus.client.core.ui.component.SvgButton("/figures/eye_close.svg");
        toggleButton.setPreferredSize(new Dimension(30, 30));
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setFocusPainted(false);
        toggleButton.addActionListener(e -> {
            if (passwordField.getEchoChar() == 0) {
                passwordField.setEchoChar('•');
                toggleButton.setSvgIcon("/figures/eye_close.svg");
            } else {
                passwordField.setEchoChar((char) 0);
                toggleButton.setSvgIcon("/figures/eye_open.svg");
            }
        });
        panel.add(toggleButton, BorderLayout.EAST);
        return panel;
    }

    private int calculatePasswordStrength(String password) {
        if (password.length() < 8) return 0;
        int score = 0;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[^a-zA-Z0-9].*")) score++;
        return score;
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 8 && calculatePasswordStrength(password) >= 2;
    }

    private void changePassword(String oldPassword, String newPassword, JDialog dialog) {
        Object cardNumObj = userData.get("cardNum");
        String cardNumStr = (cardNumObj instanceof Number) ? new java.math.BigDecimal(cardNumObj.toString()).toPlainString() : cardNumObj.toString();
        Request request = new Request("auth/changepassword")
            .addParam("cardNum", cardNumStr)
            .addParam("oldPassword", oldPassword)
            .addParam("newPassword", newPassword)
            .addParam("userType", "admin");
        nettyClient.sendRequest(request).thenAccept(res -> SwingUtilities.invokeLater(() -> {
            if (res != null && "SUCCESS".equals(res.getStatus())) {
                JOptionPane.showMessageDialog(dialog, "密码修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                showError("密码修改失败：" + (res != null ? res.getMessage() : "服务器无响应"));
            }
        }));
    }

    private String translateRole(Object role) {
        if (role == null) return "未知";
        switch (role.toString().toLowerCase()) {
            case "student": return "学生";
            case "staff": return "教职工";
            case "teacher": return "教师";
            case "admin": case "manager": return "管理员";
            default: return role.toString();
        }
    }

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
        panel.add(new com.vcampus.client.core.ui.AiChatAssistant.AiChatAssistantPanel(), BorderLayout.CENTER);
        return panel;
    }

    private void toggleAIPanel() {
        mainSplitPane.setContinuousLayout(true);
        if (!aiVisible) {
            mainSplitPane.setRightComponent(aiPanel);
            int divider = Math.max(100, getWidth() - 440);
            mainSplitPane.setDividerLocation(divider);
            mainSplitPane.setResizeWeight(1.0);
            aiVisible = true;
        } else {
            mainSplitPane.setRightComponent(null);
            aiVisible = false;
        }
        mainSplitPane.revalidate();
        mainSplitPane.repaint();
    }

    private void setupModernTable(JTable table) {
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(40);
        table.setGridColor(new Color(226, 232, 240));
        table.setSelectionBackground(new Color(64, 128, 255, 50));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setBackground(SECONDARY_COLOR);
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
    }

    private class AlternatingRowRenderer extends javax.swing.table.DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : SURFACE_COLOR);
            }
            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            return c;
        }
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

        public void loadUserAvatar(JLabel avatarLabel, int width, int height) {
            try {
                String avatarPath = userData.get("avatar") != null ? userData.get("avatar").toString() : null;
                if (avatarPath != null && !avatarPath.isEmpty()) {
                    File avatarFile = new File(avatarPath);
                    if (avatarFile.exists() && avatarFile.canRead()) {
                        BufferedImage img = ImageIO.read(avatarFile);
                        avatarLabel.setIcon(new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
                        return;
                    }
                }
                String gender = userData.get("gender") != null ? userData.get("gender").toString() : "男";
                avatarLabel.setIcon(createDefaultAvatar(width, height, gender));
            } catch (Exception e) {
                log.error("加载用户头像时发生错误", e);
                String gender = userData.get("gender") != null ? userData.get("gender").toString() : "男";
                avatarLabel.setIcon(createDefaultAvatar(width, height, gender));
            }
        }

        public void showAvatarSelectionDialog(JLabel sidebarAvatar, JLabel profileAvatar) {
            this.sidebarAvatarLabel = sidebarAvatar;
            this.profileAvatarLabel = profileAvatar;

            JDialog dialog = new JDialog(AdminFrame.this, "更换头像", true);
            dialog.setSize(500, 450);
            dialog.setLocationRelativeTo(AdminFrame.this);
            JPanel mainPanel = new JPanel(new BorderLayout(10,10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JLabel lblPreview = new JLabel();
            lblPreview.setHorizontalAlignment(SwingConstants.CENTER);
            lblPreview.setPreferredSize(new Dimension(120, 120));
            loadUserAvatar(lblPreview, 120, 120);
            mainPanel.add(lblPreview, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton btnUpload = new JButton("选择新头像");
            JButton btnReset = new JButton("恢复默认");
            JButton btnCancel = new JButton("取消");
            buttonPanel.add(btnUpload);
            buttonPanel.add(btnReset);
            buttonPanel.add(btnCancel);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            btnUpload.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("图片文件", "jpg", "jpeg", "png"));
                if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    if (file.length() > 2 * 1024 * 1024) {
                        showError("文件大小不能超过2MB"); return;
                    }
                    try {
                        BufferedImage img = ImageIO.read(file);
                        lblPreview.setIcon(new ImageIcon(img.getScaledInstance(120, 120, Image.SCALE_SMOOTH)));
                        if (JOptionPane.showConfirmDialog(dialog, "确认更换头像吗？", "确认", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            updateAvatarWithFile(file, dialog);
                        }
                    } catch (IOException ex) { log.error("读取图片文件失败", ex); }
                }
            });
            btnReset.addActionListener(e -> {
                if (JOptionPane.showConfirmDialog(dialog, "确认恢复默认头像吗？", "确认", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    resetToDefaultAvatar(dialog, lblPreview);
                }
            });
            btnCancel.addActionListener(e -> dialog.dispose());
            dialog.add(mainPanel);
            dialog.setVisible(true);
        }

        private void updateAvatarWithFile(File file, JDialog dialog) {
            try {
                Path avatarDir = Paths.get(System.getProperty("user.home"), ".vcampus", "avatars");
                Files.createDirectories(avatarDir);
                String cardNum = userData.get("cardNum").toString();
                String ext = file.getName().substring(file.getName().lastIndexOf('.'));
                Path newPath = avatarDir.resolve("avatar_" + cardNum + "_" + System.currentTimeMillis() + ext);
                Files.copy(file.toPath(), newPath, StandardCopyOption.REPLACE_EXISTING);
                updateAvatarOnServer(newPath.toString(), dialog);
            } catch (IOException e) {
                log.error("复制头像文件失败", e);
            }
        }

        private void resetToDefaultAvatar(JDialog dialog, JLabel previewLabel) {
            String gender = userData.get("gender") != null ? userData.get("gender").toString() : "男";
            previewLabel.setIcon(createDefaultAvatar(120, 120, gender));
            updateAvatarOnServer("", dialog);
        }

        private void updateAvatarOnServer(String avatarPath, JDialog dialog) {
            Request request = new Request("auth/updateAvatar")
                .addParam("cardNum", userData.get("cardNum").toString())
                .addParam("avatar", avatarPath);
            nettyClient.sendRequest(request).thenAccept(res -> SwingUtilities.invokeLater(() -> {
                if (res != null && "SUCCESS".equals(res.getStatus())) {
                    userData.put("avatar", avatarPath);
                    updateAvatarDisplay(avatarPath);
                    dialog.dispose();
                } else {
                    showError("头像更换失败");
                }
            }));
        }

        private void updateAvatarDisplay(String newAvatarPath) {
            if (profileAvatarLabel != null) loadUserAvatar(profileAvatarLabel, 120, 120);
            if (sidebarAvatarLabel != null) loadUserAvatar(sidebarAvatarLabel, 60, 60);
            // Also update the main sidebar avatar
            Component[] topComponents = sidebar.topPanel.getComponents();
            for(Component tc : topComponents) {
                if(tc instanceof JLabel) {
                    loadUserAvatar((JLabel)tc, 60, 60);
                    break;
                }
            }
        }

        private ImageIcon createDefaultAvatar(int width, int height, String gender) {
            try {
                String avatarResource = getDefaultAvatarResource("admin", gender);
                InputStream stream = getClass().getResourceAsStream(avatarResource);
                if (stream != null) {
                    BufferedImage img = ImageIO.read(stream);
                    return new ImageIcon(img.getScaledInstance(width, height, Image.SCALE_SMOOTH));
                }
            } catch (Exception e) {
                log.warn("加载默认头像资源失败", e);
            }
            // Fallback to generated avatar
            BufferedImage avatar = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = avatar.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(0xA5, 0xFF, 0xE2));
            g2d.fillOval(2, 2, width - 4, height - 4);
            g2d.setColor(new Color(0x1F, 0x6F, 0x85));
            g2d.setStroke(new BasicStroke(2));
            int headSize = width / 3;
            g2d.drawOval((width - headSize) / 2, height / 4, headSize, headSize);
            g2d.drawOval((width - width / 2) / 2, height / 4 + headSize - 5, width / 2, height / 3);
            g2d.dispose();
            return new ImageIcon(avatar);
        }

        private String getDefaultAvatarResource(String role, String gender) {
            // 管理员不区分性别
            return "/figures/admin.png";
        }
    }
}

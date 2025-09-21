package com.vcampus.client.core.ui.student;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.client.core.service.CourseClientService;
import com.vcampus.client.core.ui.login.LoginFrame;
import com.vcampus.client.core.ui.student.view.FaceCollectionPanel;
import com.vcampus.client.ui.VideoPlayerPanel;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.HashMap;

import com.vcampus.client.ui.VideoPlayerPanel; // 新增：视频面板导入
import com.vcampus.client.ui.VideoGalleryPanel; // 新增：视频画廊导入

/**
 * 学生用户主界面 - 现代化设计
 *
 * @author VCampus Team
 * @version 2.0
 */
@Slf4j
public class StudentFrame extends JFrame {

    // 现代化配色方案
    // 用于切换内容区的面板
    private JPanel newContent;
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

    private static final String TIMETABLE_VIEW = "TIMETABLE_VIEW";
    private static final String LIST_VIEW = "LIST_VIEW";

    private JPanel mainPanel;
    private NettyClient nettyClient;
    private Map<String, Object> userData;

    // 现代化侧边栏
    private ModernSidebar sidebar;
    private JPanel contentPanel;

    // 编辑状态管理
    private boolean isEditing = false;
    private Map<String, JComponent> editingComponents = new HashMap<>();
    private Map<String, Object> currentStudentData;

    // 头像管理
    private AvatarManager avatarManager;

    // 添加缺失的字段声明
    private JButton btnChangePassword;
    private JLabel lblStudentName;
    private boolean shopResizeListenerAdded = false;
    
    // AI 分屏相关字段
    private JPanel aiPanel;
    private JSplitPane mainSplitPane;
    private boolean aiVisible = false;
    private int originalWidth;
    private JButton btnAI;
    
    // 视频播放器面板引用（当显示时保留，以便释放）
    private VideoPlayerPanel videoPlayerPanel;

    public StudentFrame(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        this.avatarManager = new AvatarManager(userData, nettyClient);

        initModernUI();
        setupEventHandlers();
        log.info("现代化学生主界面初始化完成: {}", userData.get("userName"));
    }

    /**
     * 初始化现代化UI
     */
    private void initModernUI() {
        setTitle("VCampus - 学生端");

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
        private ModernMenuItem commentItem;
        private ModernMenuItem videoItem;
        private ModernMenuItem faceItem;

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

            JLabel nameLabel = new JLabel(userData.get("userName").toString());
            nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
            nameLabel.setForeground(TEXT_PRIMARY);
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel idLabel = new JLabel("(" + userData.get("cardNum").toString() + ")");
            idLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            idLabel.setForeground(TEXT_SECONDARY);
            idLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            infoPanel.add(Box.createVerticalStrut(5));
            infoPanel.add(nameLabel);
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
            libraryItem = new ModernMenuItem("📖", "图书借阅", () -> switchToModule("library"));
            shopItem = new ModernMenuItem("🛒", "校园商城", () -> switchToModule("shop"));
            cardItem = new ModernMenuItem("💳", "校园卡", () -> switchToModule("card"));
            commentItem = new ModernMenuItem("🗣", "简易校园交流", () -> switchToModule("comment"));
            videoItem = new ModernMenuItem("▶", "云课堂", () -> switchToModule("video"));
            faceItem = new ModernMenuItem("👦", "人脸采集", () -> switchToModule("face"));

            // 设置默认选中s
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
            panel.add(Box.createVerticalStrut(2));
            panel.add(commentItem);
            panel.add(Box.createVerticalStrut(2));
            panel.add(videoItem);
            panel.add(Box.createVerticalStrut(2));
            panel.add(faceItem);

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
            commentItem.setSelected(false);
            videoItem.setSelected(false);
            if (faceItem != null) faceItem.setSelected(false);

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
                case "comment":
                    commentItem.setSelected(true);
                    break;
                case "video":
                    videoItem.setSelected(true);
                    break;
                case "face":
                    if (faceItem != null) faceItem.setSelected(true);
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
            iconLabel.setVerticalAlignment(SwingConstants.CENTER);
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

            // 创建文本标签 - 保存引用
            textLabel = new JLabel(text);
            textLabel.setFont(new Font("微软雅黑", Font.PLAIN, 13));
            textLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
            textLabel.setVerticalAlignment(SwingConstants.CENTER);
            textLabel.setHorizontalAlignment(SwingConstants.LEFT);

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

        // 如果当前存在视频面板且要切换到非视频模块，先释放视频资源
        if (!"video".equals(module) && videoPlayerPanel != null) {
            try {
                videoPlayerPanel.dispose();
            } catch (Exception ignore) {
            }
            videoPlayerPanel = null;
        }

        // 移除当前内容
        contentPanel.removeAll();

        // 根据模块切换内容
        JPanel panel = null;
        switch (module) {
            case "profile":
                panel = createProfilePanel();
                break;
            case "course":
                panel = createCoursePanel();
                break;
            case "library":
                panel = createLibraryPanel();
                break;
            case "shop":
                panel = createShopPanel();
                break;
            case "card":
                panel = new com.vcampus.client.core.ui.card.CardPanel(nettyClient, userData);
                break;
            case "comment":
                panel = new com.vcampus.client.core.ui.student.CommentPanel(nettyClient, userData);
                break;
            case "video":
                // 只显示画廊面板；点击画廊中的卡片会打开独立播放器对话框（类似视频网站：先列表，点击进入）
                VideoGalleryPanel galleryOnly = new VideoGalleryPanel();
                panel = new JPanel(new BorderLayout());
                panel.add(galleryOnly, BorderLayout.CENTER);
                break;
            case "face":
                 panel = new FaceCollectionPanel(userData);
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
                int result = JOptionPane.showConfirmDialog(StudentFrame.this,
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

    // 处理课程点击：显示详情、评教或退选
    private void handleCourseClick(JPanel parent, Map<String, Object> courseData, Runnable refreshCallback) {
        String courseName = courseData.get("courseName") != null ? String.valueOf(courseData.get("courseName")) : "";
        String teacher = courseData.get("teacher") != null ? String.valueOf(courseData.get("teacher")) : "";
        String schedule = courseData.get("Schedule") != null ? String.valueOf(courseData.get("Schedule")) : "";
        String room = courseData.get("Room") != null ? String.valueOf(courseData.get("Room")) : "";
        Object sectionIdObj = courseData.get("section_Id");
        String sectionId;
        if (sectionIdObj instanceof Double) {
            sectionId = String.valueOf(((Double) sectionIdObj).intValue());
        } else if (sectionIdObj instanceof Number) {
            sectionId = String.valueOf(((Number) sectionIdObj).intValue());
        } else {
            sectionId = String.valueOf(sectionIdObj);
        }

        StringBuilder info = new StringBuilder();
        info.append("课程名称: ").append(courseName).append("\n");
        if (!teacher.isEmpty()) info.append("任课教师: ").append(teacher).append("\n");
        if (!schedule.isEmpty()) info.append("上课时间: ").append(schedule).append("\n");
        if (!room.isEmpty()) info.append("上课地点: ").append(room).append("\n");
        info.append("教学班号: ").append(sectionId);

        int option = JOptionPane.showOptionDialog(
                parent,
                info.toString(),
                "课程详情",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"评教", "退选", "关闭"},
                "关闭"
        );

        if (option == 0) {
            showEvaluationDialog(sectionId, courseName, teacher);
        } else if (option == 1) {
            dropCourse(sectionId, refreshCallback);
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
        JLabel loadingLabel = new JLabel("正在加载学籍信息...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        loadingLabel.setForeground(Color.GRAY);
        panel.add(loadingLabel, BorderLayout.CENTER);

        // 在后台线程中加载完整的学籍信息
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    // 取用户卡号
                    Object cardNumObj = userData.get("cardNum");
                    String cardNumStr;

                    if (cardNumObj instanceof Number) {
                        java.math.BigDecimal bd = new java.math.BigDecimal(cardNumObj.toString());
                        cardNumStr = bd.toPlainString();
                    } else {
                        cardNumStr = cardNumObj.toString();
                    }

                    log.info("开始加载学生学籍信息，卡号: {}", cardNumStr);

                    // 发送请求获取学生信息
                    Request request = new Request("academic/student")
                            .addParam("action", "GET_STUDENT_BY_CARD_NUM")
                            .addParam("cardNum", cardNumStr);

                    Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);

                    if (response != null && "SUCCESS".equals(response.getStatus())) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> studentData = (Map<String, Object>) response.getData();
                        SwingUtilities.invokeLater(() -> displayStudentInfo(panel, studentData));
                        log.info("学生学籍信息加载成功");
                    } else {
                        String errorMsg = response != null ? response.getMessage() : "服���器无响应";
                        SwingUtilities.invokeLater(() -> showError(panel, "加载学籍信息失败：" + errorMsg));
                        log.warn("学生学籍信息加载失��: {}", errorMsg);
                    }

                } catch (Exception e) {
                    log.error("加载学生学籍信息时发生错误", e);
                    SwingUtilities.invokeLater(() -> showError(panel, "加载学籍信息时发生错误：" + e.getMessage()));
                }
            }).start();
        });

        return panel;
    }

    /**
     * 显示学生信息
     */
    private void displayStudentInfo(JPanel panel, Map<String, Object> studentData) {
        // 保存当前学生数据
        this.currentStudentData = studentData;

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
                "学籍详细信息",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("微软雅黑", Font.BOLD, 16),
                new Color(255, 127, 80)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12); // 优化间距，更紧凑
        gbc.anchor = GridBagConstraints.WEST;

        // 基本信息 - 不可编辑的字段
        addInfoRow(infoPanel, "卡号:", studentData.get("cardNum"), gbc, 0, false);
        addInfoRow(infoPanel, "学号:", studentData.get("studentId"), gbc, 1, false);
        addInfoRow(infoPanel, "专业:", studentData.get("major"), gbc, 2, false);
        addInfoRow(infoPanel, "学院:", studentData.get("department"), gbc, 3, false);

        // 可编辑的字段
        addInfoRow(infoPanel, "姓名:", studentData.get("name"), gbc, 4, true);
        addInfoRow(infoPanel, "性别:", studentData.get("gender"), gbc, 5, true);
        addInfoRow(infoPanel, "出生年月:", studentData.get("birthDate"), gbc, 6, true);
        addInfoRow(infoPanel, "电话:", studentData.get("phone"), gbc, 7, true);
        addInfoRow(infoPanel, "民族:", studentData.get("ethnicity"), gbc, 8, true);
        addInfoRow(infoPanel, "身份证号:", studentData.get("idCard"), gbc, 9, true);
        addInfoRow(infoPanel, "籍贯:", studentData.get("hometown"), gbc, 10, true);
        addInfoRow(infoPanel, "入学年份:", studentData.get("enrollmentYear"), gbc, 11, true);

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
        btnEditInfo.addActionListener(e -> toggleEditMode(panel, studentData, btnEditInfo));

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
    private void toggleEditMode(JPanel parentPanel, Map<String, Object> studentData, JButton editButton) {
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
            confirmButton.addActionListener(e -> {
                log.info("个人信息修改成功");
                JOptionPane.showMessageDialog(this, "个人信息修改成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
                saveChanges(parentPanel, studentData, editButton);
            });

            // 添加确认按钮到按钮面板
            JPanel buttonPanel = findButtonPanel(parentPanel);
            if (buttonPanel != null) {
                buttonPanel.add(confirmButton, 1); // 在"编辑"按钮前添加
                buttonPanel.revalidate();
                buttonPanel.repaint();
            }

            // 将可编辑的标签替换为输入框
            log.info("开始转换标签为输入框，编辑组件数量: {}", editingComponents.size());
            convertLabelsToInputs();
            log.info("转换完成，编辑组件数量: {}", editingComponents.size());

        } else {
            // 退出编�����式
            exitEditMode(parentPanel, studentData, editButton);
        }
    }

    /**
     * 退出编辑模式
     */
    private void exitEditMode(JPanel parentPanel, Map<String, Object> studentData, JButton editButton) {
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
        displayStudentInfo(parentPanel, studentData);
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
        log.info("convertLabelsToInputs开始，编辑组件数量: {}", editingComponents.size());
        for (Map.Entry<String, JComponent> entry : editingComponents.entrySet()) {
            String fieldName = entry.getKey();
            JComponent component = entry.getValue();
            log.info("处理字段: {}, 组件类型: {}", fieldName, component.getClass().getSimpleName());

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
    private void saveChanges(JPanel parentPanel, Map<String, Object> studentData, JButton editButton) {
        log.info("saveChanges方法被调用，编辑组件数量: {}", editingComponents.size());
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
            submitStudentInfoUpdate(studentData, updatedValues, parentPanel, editButton);

        } catch (Exception e) {
            log.error("保存更改时发生错误", e);
            JOptionPane.showMessageDialog(this, "保存更改时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 提交学生信息更新 - 重构版本
     */
    private void submitStudentInfoUpdate(Map<String, Object> currentData, Map<String, String> updatedValues,
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

            log.info("提交学生信息修改请求，卡号: {}", cardNumStr);

            Request request = new Request("academic/student")
                    .addParam("action", "UPDATE_STUDENT_INFO")
                    .addParam("cardNum", cardNumStr)
                    .addParam("name", updatedValues.get("姓名:"))
                    .addParam("gender", updatedValues.get("性别:"))
                    .addParam("birthDate", updatedValues.get("出生年月:"))
                    .addParam("phone", updatedValues.get("电话:"))
                    .addParam("ethnicity", updatedValues.get("民族:"))
                    .addParam("idCard", updatedValues.get("身份证号:"))
                    .addParam("hometown", updatedValues.get("籍贯:"));

            Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);

            if (response != null && "SUCCESS".equals(response.getStatus())) {
                SwingUtilities.invokeLater(() -> {
                    // ���新userData中的姓名
                    userData.put("userName", updatedValues.get("姓名:"));
                    // 更新侧边栏显示的姓名
                    lblStudentName.setText(updatedValues.get("姓名:"));

                    JOptionPane.showMessageDialog(this, "个人信息修改成功！", "成功", JOptionPane.INFORMATION_MESSAGE);

                    // 更新当前数据
                    currentData.put("name", updatedValues.get("姓名:"));
                    currentData.put("gender", updatedValues.get("性别:"));
                    currentData.put("birthDate", updatedValues.get("出生年月:"));
                    currentData.put("phone", updatedValues.get("电话:"));
                    currentData.put("ethnicity", updatedValues.get("民族:"));
                    currentData.put("idCard", updatedValues.get("身份证号:"));
                    currentData.put("hometown", updatedValues.get("籍贯:"));
                    currentData.put("enrollmentYear", updatedValues.get("入学年份:"));

                    // 退出编辑模式
                    exitEditMode(parentPanel, currentData, editButton);
                });
                log.info("学生 {} 个人信息修改成功", updatedValues.get("姓名:"));
            } else {
                String errorMsg = response != null ? response.getMessage() : "服务器无响应";
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "个人信息修改失败：" + errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                });
                log.warn("学生 {} 个人信息修改失败: {}", updatedValues.get("姓名:"), errorMsg);
            }

        } catch (Exception e) {
            log.error("修改学生个人信息时发生错误", e);
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

    private JPanel createCoursePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15)); // Add vertical gap
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(SURFACE_COLOR);

        // Top panel with title
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel titleLabel = new JLabel("课程管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        topPanel.add(titleLabel, BorderLayout.NORTH);
        panel.add(topPanel, BorderLayout.NORTH);

        // Main content panel with CardLayout for switching views
        JPanel contentSwitchPanel = new JPanel(new CardLayout());
        contentSwitchPanel.setOpaque(false);

        // View 1: Timetable (CourseScheduleTable)
        com.vcampus.client.core.ui.component.CourseScheduleTable scheduleTable =
                new com.vcampus.client.core.ui.component.CourseScheduleTable();
        contentSwitchPanel.add(scheduleTable, TIMETABLE_VIEW);

        // View 2: List (JTable) + 搜索
        JTable courseListTable = new JTable();
        String[] columnNames = {"课程名称", "教师", "上课时间", "上课地点", "教学班号"};
        DefaultTableModel listModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        courseListTable.setModel(listModel);
        courseListTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        courseListTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        courseListTable.setRowHeight(28);
        courseListTable.getTableHeader().setReorderingAllowed(false);

        // 搜索条
        JTextField txtSearch = new JTextField(18);
        JButton btnClear = new JButton("清空");
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.add(new JLabel("搜索："));
        txtSearch.setToolTipText("按课程/教师/时间/地点/教学班号筛选");
        searchBar.add(txtSearch);
        btnClear.setFocusable(false);
        searchBar.add(btnClear);

        JScrollPane listScrollPane = new JScrollPane(courseListTable);
        listScrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        JPanel listContainer = new JPanel(new BorderLayout());
        listContainer.add(searchBar, BorderLayout.NORTH);
        listContainer.add(listScrollPane, BorderLayout.CENTER);
        contentSwitchPanel.add(listContainer, LIST_VIEW);

        panel.add(contentSwitchPanel, BorderLayout.CENTER);

        // Bottom button panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        // View switcher buttons
        JPanel viewSwitchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        viewSwitchPanel.setOpaque(false);

        JButton btnTimetableView = new JButton("课表视图");
        JButton btnListView = new JButton("列表视图");

        // Style the buttons
        Font buttonFont = new Font("微软雅黑", Font.BOLD, 14);
        btnTimetableView.setFont(buttonFont);
        btnListView.setFont(buttonFont);
        btnTimetableView.setFocusPainted(false);
        btnListView.setFocusPainted(false);
        
        // 设置课表视图按钮样式
        btnTimetableView.setBackground(new Color(52, 152, 219)); // 蓝色
        btnTimetableView.setForeground(Color.WHITE);
        btnTimetableView.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        // 设置列表视图按钮样式
        btnListView.setBackground(new Color(155, 89, 182)); // 紫色
        btnListView.setForeground(Color.WHITE);
        btnListView.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        viewSwitchPanel.add(btnTimetableView);
        viewSwitchPanel.add(btnListView);
        bottomPanel.add(viewSwitchPanel, BorderLayout.WEST);

        // Action buttons
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionButtonPanel.setOpaque(false);
        JButton btnRefresh = new JButton("刷新");
        JButton btnSelectCourse = new JButton("选课");
        JButton btnViewScore = new JButton("查看成绩");

        btnRefresh.setFont(buttonFont);
        btnSelectCourse.setFont(buttonFont);
        btnViewScore.setFont(buttonFont);
        
        // 设置刷新按钮样式
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBackground(new Color(46, 204, 113)); // 绿色
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        // 设置选课按钮样式
        btnSelectCourse.setFocusPainted(false);
        btnSelectCourse.setBackground(new Color(230, 126, 34)); // 橙色
        btnSelectCourse.setForeground(Color.WHITE);
        btnSelectCourse.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        // 设置查看成绩按钮样式
        btnViewScore.setFocusPainted(false);
        btnViewScore.setBackground(new Color(155, 89, 182)); // 紫色
        btnViewScore.setForeground(Color.WHITE);
        btnViewScore.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        actionButtonPanel.add(btnRefresh);
        actionButtonPanel.add(btnSelectCourse);
        actionButtonPanel.add(btnViewScore);
        bottomPanel.add(actionButtonPanel, BorderLayout.EAST);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        // 搜索与过滤
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(listModel);
        courseListTable.setRowSorter(sorter);
        Runnable applyFilter = () -> {
            String text = txtSearch.getText();
            if (text == null || text.trim().isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                final String needle = text.trim().toLowerCase();
                sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                    @Override
                    public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                        int count = entry.getValueCount();
                        for (int i = 0; i < count; i++) {
                            Object v = entry.getValue(i);
                            if (v != null && v.toString().toLowerCase().contains(needle)) return true;
                        }
                        return false;
                    }
                });
            }
        };
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter.run(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter.run(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter.run(); }
        });
        btnClear.addActionListener(e -> txtSearch.setText(""));

        // --- Event Handlers ---

        // View switcher logic
        CardLayout cardLayout = (CardLayout) contentSwitchPanel.getLayout();
        btnTimetableView.addActionListener(e -> cardLayout.show(contentSwitchPanel, TIMETABLE_VIEW));
        btnListView.addActionListener(e -> cardLayout.show(contentSwitchPanel, LIST_VIEW));

        // Data loading logic
        Runnable loadCoursesRunnable = () -> {
            loadCourses(scheduleTable);
            loadCoursesIntoList(listModel);
            SwingUtilities.invokeLater(applyFilter);
        };

        // Initial data load
        loadCoursesRunnable.run();

        // Refresh button
        btnRefresh.addActionListener(e -> loadCoursesRunnable.run());

        // Course selection button
        btnSelectCourse.addActionListener(e -> showSelectCourseDialog(loadCoursesRunnable));

        // Score view button
        btnViewScore.addActionListener(e -> showScoreDialog());

        // Timetable click listener
        scheduleTable.setCourseSelectionListener(courseData -> handleCourseClick(panel, courseData, loadCoursesRunnable));

        // List view click listener（双击）
        courseListTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int viewRow = courseListTable.getSelectedRow();
                    if (viewRow != -1) {
                        int modelRow = courseListTable.convertRowIndexToModel(viewRow);
                        Map<String, Object> courseData = new HashMap<>();
                        courseData.put("courseName", listModel.getValueAt(modelRow, 0));
                        courseData.put("teacher", listModel.getValueAt(modelRow, 1));
                        courseData.put("Schedule", listModel.getValueAt(modelRow, 2));
                        courseData.put("Room", listModel.getValueAt(modelRow, 3));
                        courseData.put("section_Id", listModel.getValueAt(modelRow, 4));
                        handleCourseClick(panel, courseData, loadCoursesRunnable);
                    }
                }
            }
        });

        return panel;
    }

    // 加载课程数据到列表视图（使用共享模型，避免替换模型导致过滤丢失）
    private void loadCoursesIntoList(DefaultTableModel model) {
        new Thread(() -> {
            try {
                String cardNum = userData.get("cardNum").toString();
                java.util.List<Map<String, Object>> courses = CourseClientService.getSelectedCourses(nettyClient, cardNum);
                SwingUtilities.invokeLater(() -> {
                    model.setRowCount(0);
                    if (courses != null && !courses.isEmpty()) {
                        for (Map<String, Object> course : courses) {
                            model.addRow(new Object[]{
                                    course.get("courseName"),
                                    course.get("teacher"),
                                    course.get("Schedule"),
                                    course.get("Room"),
                                    course.get("section_Id")
                            });
                        }
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "加载课程列表失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();
    }


    /**
     * 加载课程数据到课程表
     */
    private void loadCourses(com.vcampus.client.core.ui.component.CourseScheduleTable scheduleTable) {
        new Thread(() -> {
            try {
                String cardNum = userData.get("cardNum").toString();
                java.util.List<Map<String, Object>> courses = CourseClientService.getSelectedCourses(nettyClient, cardNum);
                SwingUtilities.invokeLater(() -> scheduleTable.setCourseData(courses));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "加载课程失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();
    }

    /**
     * 显示评教对话框
     */
    private void showEvaluationDialog(String sectionId, String courseName, String teacher) {
        JDialog dialog = new JDialog(this, "课程评教", true);
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // 标题面板
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("课程评教 - " + courseName);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titlePanel.add(titleLabel);

        // 教师信息
        JLabel teacherLabel = new JLabel("任课教师：" + teacher);
        teacherLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        teacherLabel.setForeground(Color.GRAY);
        titlePanel.add(teacherLabel);

        dialog.add(titlePanel, BorderLayout.NORTH);

        // 评教问题面板
        JPanel questionsPanel = new JPanel();
        questionsPanel.setLayout(new BoxLayout(questionsPanel, BoxLayout.Y_AXIS));
        questionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // 11个评教问题
        String[] questions = {
                "我对这门课程和老师的总体评价",
                "老师课堂无不良言论,价值导向积极,体现了\"立德树人\"的师者风范",
                "老师能够让我了解课程的价值,激发我的学习热情",
                "老师能让我明白每次课程的具体学习目标和要求",
                "老师教学能够理论联系实际,教学内容具有适当的挑战性",
                "我认为老师讲授思路清楚,重点突出,层次分明",
                "我��为老师备课充分,为我们提供了丰富的学习资料",
                "老师能根据大��数同学的学习情况,合理调整教学安排���进度",
                "老师���我们的问题(包括作业和考核)能够给予及时、有帮助的反馈",
                "我认为课程的考核评价方式能够反映我的学习成效",
                "我认为我能够达到本课程的教学目标,在学习中有所收获"
        };

        JSlider[] sliders = new JSlider[11];
        JLabel[] scoreLabels = new JLabel[11];

        for (int i = 0; i < 11; i++) {
            JPanel questionPanel = new JPanel(new BorderLayout());
            questionPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

            // 问题标签
            JLabel questionLabel = new JLabel("<html><div style='width: 500px;'>" + (i + 1) + ". " + questions[i] + "</div></html>");
            questionLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            questionPanel.add(questionLabel, BorderLayout.NORTH);

            // 评分面板
            JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            // 滑块
            sliders[i] = new JSlider(1, 10, 8); // 默认8分
            sliders[i].setMajorTickSpacing(1);
            sliders[i].setPaintTicks(true);
            sliders[i].setPaintLabels(true);
            sliders[i].setPreferredSize(new Dimension(300, 50));

            // 分数标签
            scoreLabels[i] = new JLabel("8分");
            scoreLabels[i].setFont(new Font("微软雅黑", Font.BOLD, 12));
            scoreLabels[i].setForeground(Color.BLUE);
            scoreLabels[i].setPreferredSize(new Dimension(40, 20));

            // 添加滑块监听器
            final int index = i;
            sliders[i].addChangeListener(e -> {
                int value = sliders[index].getValue();
                scoreLabels[index].setText(value + "分");
                scoreLabels[index].setForeground(value >= 8 ? Color.BLUE : (value >= 6 ? Color.ORANGE : Color.RED));
            });

            scorePanel.add(sliders[i]);
            scorePanel.add(scoreLabels[i]);
            questionPanel.add(scorePanel, BorderLayout.CENTER);

            questionsPanel.add(questionPanel);
        }

        JScrollPane scrollPane = new JScrollPane(questionsPanel);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton submitButton = new JButton("提交评教");
        JButton cancelButton = new JButton("取消");

        submitButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        submitButton.addActionListener(e -> {
            // 计算平均分
            double total = 0;
            for (JSlider slider : sliders) {
                total += slider.getValue();
            }
            double avgScore = total / 11.0;

            // 提交评教
            submitEvaluation(sectionId, avgScore, () -> {
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "评教提交成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
            });
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /**
     * 提交评教数据
     */
    private void submitEvaluation(String sectionId, double score, Runnable callback) {
        new Thread(() -> {
            try {
                String studentId = userData.get("student_Id").toString();

                Request request = new Request("academic/course")
                        .addParam("action", "submitEvaluation")
                        .addParam("section_Id", sectionId)
                        .addParam("student_Id", studentId)
                        .addParam("score", String.valueOf(score));

                Response response = nettyClient.sendRequest(request).get();
                if (response.isSuccess()) {
                    SwingUtilities.invokeLater(callback);
                } else {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(this, "评教提交失败：" + response.getMessage(), "错误", JOptionPane.ERROR_MESSAGE)
                    );
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "评教提交失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();
    }

    /**
     * 显示选课对话框
     */
    private void showSelectCourseDialogOld(Runnable refreshCallback) {
        JDialog dialog = new JDialog(this, "可选课程", true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);

        // 创建可选课程的传统表格视图
        String[] columnNames = {"教学班号", "课程名称", "教师", "学分", "上课时间", "教室", "操作"};
        DefaultTableModel selectModel = new DefaultTableModel(columnNames, 0);
        JTable selectTable = new JTable(selectModel);

        // 设置表格样式
        selectTable.setFont(new Font("微软雅��", Font.PLAIN, 12));
        selectTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        selectTable.setRowHeight(25);

        JScrollPane selectScroll = new JScrollPane(selectTable);
        dialog.add(selectScroll, BorderLayout.CENTER);

        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnChoose = new JButton("选择课程");
        JButton btnClose = new JButton("关闭");

        btnChoose.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnClose.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        buttonPanel.add(btnChoose);
        buttonPanel.add(btnClose);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // 加载可选课程
        new Thread(() -> {
            try {
                String cardNum = userData.get("cardNum").toString();
                java.util.List<Map<String, Object>> courses = CourseClientService.getAvailableCourses(nettyClient, cardNum);
                if (courses != null && !courses.isEmpty()) {
                    for (Map<String, Object> c : courses) {
                        Object[] row = {
                                c.get("section_Id"),
                                c.get("courseName"),
                                c.get("teacher") != null ? c.get("teacher") : "",
                                c.get("Credit"),
                                c.get("Schedule") != null ? c.get("Schedule") : "",
                                c.get("Room") != null ? c.get("Room") : "",
                                "可选"
                        };
                        SwingUtilities.invokeLater(() -> selectModel.addRow(row));
                    }
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(dialog, "加载可选课程失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();

        // ��择课程按钮事件
        btnChoose.addActionListener(ev -> {
            int selectedRow = selectTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dialog, "请选择一门课程", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Object sectionIdObj = selectModel.getValueAt(selectedRow, 0);
            if (sectionIdObj == null) {
                JOptionPane.showMessageDialog(dialog, "选课数据异常，请刷新后重试", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 确保sectionId是整数格式
            final String sectionId;
            if (sectionIdObj instanceof Double) {
                sectionId = String.valueOf(((Double) sectionIdObj).intValue());
            } else {
                sectionId = sectionIdObj.toString();
            }
            String courseName = (String) selectModel.getValueAt(selectedRow, 1);

            int confirm = JOptionPane.showConfirmDialog(
                    dialog,
                    "确定要选择课程：" + courseName + " 吗？",
                    "确认选课",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                selectCourse(sectionId, () -> {
                    dialog.dispose();
                    refreshCallback.run();
                });
            }
        });

        btnClose.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    /**
     * 显示选课对话框 - 卡片模式
     */
    private void showSelectCourseDialog(Runnable refreshCallback) {
        JDialog dialog = new JDialog(this, "可选课程", true);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(this);

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchField.setToolTipText("搜索课程名称、教师、教室等");
        
        JButton searchButton = new JButton("搜索");
        searchButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchButton.setBackground(new Color(52, 152, 219));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFocusPainted(false);
        searchButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        
        JButton clearButton = new JButton("清空");
        clearButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        clearButton.setBackground(new Color(149, 165, 166));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        
        searchPanel.add(new JLabel("搜索: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // 创建卡片容器 - 使用网格布局，每行4列，行数动态调整
        JPanel cardsPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 使用一个包装器面板来确保滚动条正常工作
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.add(cardsPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapperPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton btnClose = new JButton("关闭");
        
        btnClose.setFont(new Font("微软雅黑", Font.BOLD, 14));
        btnClose.setBackground(new Color(149, 165, 166));
        btnClose.setForeground(Color.WHITE);
        btnClose.setFocusPainted(false);
        btnClose.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        buttonPanel.add(btnClose);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);

        // 存储课程数据用于搜索
        java.util.List<Map<String, Object>> allCourses = new java.util.ArrayList<>();
        
        // 创建课程卡片的方法
        Runnable createCourseCards = () -> {
            cardsPanel.removeAll();
            
            String searchText = searchField.getText().toLowerCase().trim();

            for (Map<String, Object> course : allCourses) {
                String courseName = course.get("courseName") != null ? course.get("courseName").toString() : "";
                String teacher = course.get("teacher") != null ? course.get("teacher").toString() : "";
                String room = course.get("Room") != null ? course.get("Room").toString() : "";
                
                // 搜索过滤
                if (!searchText.isEmpty()) {
                    if (!courseName.toLowerCase().contains(searchText) && 
                        !teacher.toLowerCase().contains(searchText) && 
                        !room.toLowerCase().contains(searchText)) {
                        continue;
                    }
                }
                
                // 创建课程卡片
                JPanel cardPanel = createCourseCard(course, dialog, refreshCallback);
                cardsPanel.add(cardPanel);
            }
            
            cardsPanel.revalidate();
            cardsPanel.repaint();
        };

        // 加载可选课程
        new Thread(() -> {
            try {
                String cardNum = userData.get("cardNum").toString();
                java.util.List<Map<String, Object>> courses = CourseClientService.getAvailableCourses(nettyClient, cardNum);
                if (courses != null && !courses.isEmpty()) {
                    allCourses.addAll(courses);
                    SwingUtilities.invokeLater(createCourseCards);
                }
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(dialog, "加载可选课程失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();

        // 搜索功能
        searchButton.addActionListener(e -> createCourseCards.run());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            createCourseCards.run();
        });
        
        // 实时搜索
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                createCourseCards.run();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                createCourseCards.run();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                createCourseCards.run();
            }
        });

        btnClose.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }
    
    /**
     * 创建课程卡片
     */
    private JPanel createCourseCard(Map<String, Object> course, JDialog parent, Runnable refreshCallback) {
        // 统一使用浅黄色背景
        Color cardColor = new Color(255, 251, 235); // 浅黄色
        
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制阴影
                g2d.setColor(new Color(0, 0, 0, 25));
                g2d.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 12, 12);
                
                // 绘制卡片背景
                g2d.setColor(cardColor);
                g2d.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 12, 12);
                
                // 绘制顶部装饰条
                g2d.setColor(new Color(52, 152, 219, 100));
                g2d.fillRoundRect(0, 0, getWidth() - 3, 3, 12, 12);
                
                // 绘制边框
                g2d.setColor(new Color(220, 220, 220, 150));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 12, 12);
                
                g2d.dispose();
            }
        };
        
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        card.setMaximumSize(new Dimension(200, 120)); // 增加高度
        card.setPreferredSize(new Dimension(200, 120)); // 增加高度

        // 添加悬停效果
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                card.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                card.repaint();
            }
        });
        
        // 左侧信息区域 - 使用更紧凑的布局
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(1, 0, 1, 6); // 增加垂直间距

        // 课程名称 - 更紧凑的字体，添加颜色
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel nameLabel = new JLabel(course.get("courseName") != null ? course.get("courseName").toString() : "未知课程");
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        nameLabel.setForeground(new Color(52, 73, 94)); // 深蓝灰色
        infoPanel.add(nameLabel, gbc);
        
        // 教师和学分 - 同一行显示
        gbc.gridy = 1;
        gbc.gridwidth = 1; // 恢复默认
        JLabel teacherLabel = new JLabel("教师: " + (course.get("teacher") != null ? course.get("teacher").toString() : "未设置"));
        teacherLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10)); // 稍微增大字体
        teacherLabel.setForeground(new Color(127, 140, 141)); // 中灰色
        infoPanel.add(teacherLabel, gbc);

        gbc.gridx = 1;
        JLabel creditLabel = new JLabel("学分: " + (course.get("Credit") != null ? course.get("Credit").toString() : "0"));
        creditLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10)); // 稍微增大字体
        creditLabel.setForeground(new Color(46, 204, 113)); // 绿色突出学分
        infoPanel.add(creditLabel, gbc);

        // 时间和教室 - 同一行显示
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel scheduleLabel = new JLabel("时间: " + (course.get("Schedule") != null ? course.get("Schedule").toString() : "未设置"));
        scheduleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10)); // 稍微增大字体
        scheduleLabel.setForeground(new Color(52, 152, 219)); // 蓝色突出时间
        infoPanel.add(scheduleLabel, gbc);

        gbc.gridx = 1;
        JLabel roomLabel = new JLabel("教室: " + (course.get("Room") != null ? course.get("Room").toString() : "未设置"));
        roomLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10)); // 稍微增大字体
        roomLabel.setForeground(new Color(155, 89, 182)); // 紫色突出教室
        infoPanel.add(roomLabel, gbc);
        
        // 创建主内容区域
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setOpaque(false);
        mainContentPanel.add(infoPanel, BorderLayout.CENTER);
        
        // 按钮区域 - 置于信息下方
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0)); // 增加与上方信息的间距

        JButton selectButton = new JButton("选择课程") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制按钮背景
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // 绘制按钮文字
                g2d.setColor(getForeground());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(getText(), x, y);
                
                g2d.dispose();
            }
        };
        selectButton.setFont(new Font("微软雅黑", Font.BOLD, 12)); // 增大按钮字体
        selectButton.setBackground(new Color(40, 167, 69));
        selectButton.setForeground(Color.WHITE);
        selectButton.setFocusPainted(false);
        selectButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // 增加按钮内边距
        selectButton.setPreferredSize(new Dimension(90, 28)); // 增大按钮尺寸
        selectButton.setContentAreaFilled(false);
        selectButton.setOpaque(false);
        
        // 添加悬停效果
        selectButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                selectButton.setBackground(new Color(34, 139, 58));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                selectButton.setBackground(new Color(40, 167, 69));
            }
        });
        
        selectButton.addActionListener(e -> {
            Object sectionIdObj = course.get("section_Id");
            if (sectionIdObj == null) {
                JOptionPane.showMessageDialog(parent, "选课数据异常，请刷新后重试", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String sectionId;
            if (sectionIdObj instanceof Double) {
                sectionId = String.valueOf(((Double) sectionIdObj).intValue());
            } else {
                sectionId = sectionIdObj.toString();
            }
            
            String selectedCourseName = course.get("courseName") != null ? course.get("courseName").toString() : "未知课程";
            
            int confirm = JOptionPane.showConfirmDialog(
                parent,
                "确定要选择课程：" + selectedCourseName + " 吗？",
                "确认选课",
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                selectCourse(sectionId, () -> {
                    parent.dispose();
                    refreshCallback.run();
                });
            }
        });
        
        buttonPanel.add(selectButton);
        
        // 将按钮添加到主内容区域下方
        mainContentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 将主内容区域添加到卡片
        card.add(mainContentPanel, BorderLayout.CENTER);
        
        return card;
    }

    /**
     * 选课操作
     */
    private void selectCourse(String sectionId, Runnable successCallback) {
        String cardNum = userData.get("cardNum").toString();
        new Thread(() -> {
            try {
                CourseClientService.selectCourse(nettyClient, cardNum, sectionId);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "选课成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    if (successCallback != null) {
                        successCallback.run();
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "选课失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();
    }

    /**
     * 退课操作
     */
    private void dropCourse(String sectionId, Runnable successCallback) {
        String cardNum = userData.get("cardNum").toString();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确定要退选该课程吗？",
                "确认退课",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        new Thread(() -> {
            try {
                CourseClientService.dropCourse(nettyClient, cardNum, sectionId);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "退课成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                    if (successCallback != null) {
                        successCallback.run();
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "退课失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();
    }

    private JPanel createLibraryPanel() {
        // 新增：使用新版LibraryPanel
        JPanel panel = new com.vcampus.client.core.ui.library.LibraryPanel(nettyClient, userData);
        return panel;
    }

    private JPanel createShopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 删除校园商城标题，与student界面保持一致
        // JLabel titleLabel = new JLabel("校园商城");
        // titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        // panel.add(titleLabel, BorderLayout.NORTH);

        // 嵌入可用的商城面板
        com.vcampus.client.core.ui.shop.ShopPanel shopPanel = new com.vcampus.client.core.ui.shop.ShopPanel(nettyClient, userData);
        // 将��城面板相比默认缩小约 3%：通过设置首选大小为 StudentFrame 的 97%
        int w = (int) Math.round(getWidth() > 0 ? getWidth() * 0.97 : 1080 * 0.89 * 97 / 100.0);
        int h = (int) Math.round(getHeight() > 0 ? getHeight() * 0.97 : 690 * 0.89 * 97 / 100.0);
        shopPanel.setPreferredSize(new Dimension(w, h));
        // 当窗口大小变化时，动态更新商城面板的首选尺寸（只添加一次监听器）
        if (!shopResizeListenerAdded) {
            shopResizeListenerAdded = true;
            this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
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
                // 当前显示密码，切��为隐藏
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
                    .addParam("userType", "student"); // 添加用户类型参数

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
     * 显示成绩表对话框
     */
    private void showScoreDialog() {
        JDialog dialog = new JDialog(this, "成绩查询", true);
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建统计信息面板
        JPanel statsPanel = createStatsPanel();
        mainPanel.add(statsPanel, BorderLayout.NORTH);

        String[] columnNames = {"课程名称", "学期", "成绩", "绩点"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        table.setRowHeight(24);


        // 禁止表格编辑
        table.setEnabled(false); // 禁用整个表格的交互
        // 禁止列拖动和调整大小
        table.getTableHeader().setReorderingAllowed(false); // 禁止列重排序
        table.getTableHeader().setResizingAllowed(false);   // 禁止调整列宽


        // 设置表格为只读模式
        table.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 加载成绩数据
        new Thread(() -> {
            try {
                String cardNum = userData.get("cardNum").toString();
                java.util.List<Map<String, Object>> scores = CourseClientService.getScoreList(nettyClient, cardNum);
                Map<String, Object> statistics = CourseClientService.getStudentGradeStatistics(nettyClient, cardNum);

                SwingUtilities.invokeLater(() -> {
                    // 更新统计信息
                    updateStatsPanel(statsPanel, statistics);

                    // 添加成绩数据
                    for (Map<String, Object> s : scores) {
                        Object[] row = {
                                s.getOrDefault("courseName", ""),
                                s.getOrDefault("Term", ""),
                                s.getOrDefault("Score", ""),
                                s.getOrDefault("GPA", "")
                        };
                        model.addRow(row);
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(dialog, "加载成绩失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE)
                );
            }
        }).start();

        JButton btnClose = new JButton("关闭");
        btnClose.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnClose);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);

        dialog.setVisible(true);
    }

    /**
     * 创建统计信息面板
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("成绩统计"));
        panel.setBackground(new Color(240, 248, 255));

        // 创建标签
        JLabel lblTotalCount = new JLabel("已修科目数: 0");
        JLabel lblTotalCredits = new JLabel("总学分: 0");
        JLabel lblAvgScore = new JLabel("加权平均分: 0.00");
        JLabel lblAvgGPA = new JLabel("加权平均GPA: 0.00");

        // 设置字体和颜色
        Font statsFont = new Font("微软雅黑", Font.BOLD, 14);
        lblTotalCount.setFont(statsFont);
        lblTotalCredits.setFont(statsFont);
        lblAvgScore.setFont(statsFont);
        lblAvgGPA.setFont(statsFont);

        lblTotalCount.setForeground(new Color(25, 133, 57));
        lblTotalCredits.setForeground(new Color(128, 0, 128));
        lblAvgScore.setForeground(new Color(70, 130, 180));
        lblAvgGPA.setForeground(new Color(255, 140, 0));

        // 添加标签���面板
        panel.add(lblTotalCount);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(lblTotalCredits);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(lblAvgScore);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(lblAvgGPA);

        // 设置标签名称以便后续更新
        lblTotalCount.setName("totalCount");
        lblTotalCredits.setName("totalCredits");
        lblAvgScore.setName("avgScore");
        lblAvgGPA.setName("avgGPA");

        return panel;
    }

    /**
     * 更新统计信息面板
     */
    private void updateStatsPanel(JPanel statsPanel, Map<String, Object> statistics) {
        if (statistics == null || statistics.isEmpty()) {
            return;
        }

        // 获取统计值
        Object totalCountObj = statistics.get("totalCount");
        Object totalCreditsObj = statistics.get("totalCredits");
        Object avgScoreObj = statistics.get("avgScore");
        Object avgGPAObj = statistics.get("avgGPA");

        int totalCount = totalCountObj != null ? ((Number) totalCountObj).intValue() : 0;
        int totalCredits = totalCreditsObj != null ? ((Number) totalCreditsObj).intValue() : 0;
        double avgScore = avgScoreObj != null ? ((Number) avgScoreObj).doubleValue() : 0.0;
        double avgGPA = avgGPAObj != null ? ((Number) avgGPAObj).doubleValue() : 0.0;

        // 更新标签
        for (Component comp : statsPanel.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                String name = label.getName();
                if ("totalCount".equals(name)) {
                    label.setText("已修科目数: " + totalCount);
                } else if ("totalCredits".equals(name)) {
                    label.setText("总学分: " + totalCredits);
                } else if ("avgScore".equals(name)) {
                    label.setText(String.format("加权平均分: %.4f", avgScore));
                } else if ("avgGPA".equals(name)) {
                    label.setText(String.format("加权平均GPA: %.4f", avgGPA));
                }
            }
        }
    }

    /**
     * 显示头像选择对话框
     */
    private void showAvatarSelectionDialog(JLabel sidebarAvatar, JLabel profileAvatar) {
        // 使用AvatarManager的头像选择对话框
        avatarManager.showAvatarSelectionDialog(sidebarAvatar, profileAvatar);
    }

}

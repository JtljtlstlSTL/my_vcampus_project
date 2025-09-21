package com.vcampus.client.core.ui.library;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vcampus.client.core.net.NettyClient;

/**
 * 图书馆管理主面板
 * 根据用户角色显示不同的功能界面
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class LibraryPanel extends JPanel {
    
    private static final Logger log = LoggerFactory.getLogger(LibraryPanel.class);
    
    private final NettyClient nettyClient;
    private final Map<String, Object> userData;
    private final String userRole;
    
    // 主界面组件
    private JTabbedPane tabbedPane;
    private JPanel currentBorrowsPanel;
    private JPanel borrowHistoryPanel;
    private JPanel bookSearchPanel;
    private JPanel bookManagementPanel; // 管理员专用
    private JPanel myBookshelfPanel; // 我的书架
    private JPanel bookRecommendationPanel; // 荐购功能面板
    private JPanel adminRecommendationPanel; // 管理员荐购管理面板

    public LibraryPanel(NettyClient nettyClient, Map<String, Object> userData) {
    this.nettyClient = nettyClient;
    this.userData = userData;
    this.userRole = determineUserRole();

    initUI();
    // 优化：直接在后台线程加载初始数据，避免阻塞UI
    new Thread(this::loadInitialData).start();
    }
    
    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 创建标题
        JLabel titleLabel = new JLabel("图书馆管理系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // 创建选项卡面板
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        // 添加选项卡切换监听器
        tabbedPane.addChangeListener(e -> {
            Component selectedComponent = tabbedPane.getSelectedComponent();
            if (selectedComponent instanceof RefreshablePanel) {
                // 延迟刷新，确保界面完全切换后再刷新数据
                SwingUtilities.invokeLater(() -> {
                    ((RefreshablePanel) selectedComponent).refresh();
                });
            }
        });
        
        // 根据用户角色添加不同的选项卡
        log.info("初始化图书馆界面，用户角色: {}", userRole);
        if ("admin".equals(userRole)) {
            // 管理员界面
            log.info("创建管理员界面标签页");
            createAdminTabs();
        } else if ("teacher".equals(userRole) || "staff".equals(userRole)) {
            // 教师/教职工界面
            log.info("创建教师界面标签页");
            createTeacherTabs();
        } else {
            // 学生界面
            log.info("创建学生界面标签页");
            createStudentTabs();
        }
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private void createStudentTabs() {
        // 图书搜索
        bookSearchPanel = new BookSearchPanel(nettyClient, userData);
        tabbedPane.addTab("图书搜索", bookSearchPanel);

        // 我的书架
        myBookshelfPanel = new MyBookshelfPanel(nettyClient, userData);
        tabbedPane.addTab("我的书架", myBookshelfPanel);

        // 当前借阅
        currentBorrowsPanel = new CurrentBorrowsPanel(nettyClient, userData);
        tabbedPane.addTab("当前借阅", currentBorrowsPanel);

        // 借阅历史
        borrowHistoryPanel = new BorrowHistoryPanel(nettyClient, userData);
        tabbedPane.addTab("借阅历史", borrowHistoryPanel);
        
        // 图书荐购
        bookRecommendationPanel = new BookRecommendationPanel(nettyClient, userData);
        tabbedPane.addTab("图书荐购", bookRecommendationPanel);
    }
    
    private void createTeacherTabs() {
        // 图书搜索
        bookSearchPanel = new BookSearchPanel(nettyClient, userData);
        tabbedPane.addTab("图书搜索", bookSearchPanel);

        // 我的书架
        myBookshelfPanel = new MyBookshelfPanel(nettyClient, userData);
        tabbedPane.addTab("我的书架", myBookshelfPanel);

        // 当前借阅
        currentBorrowsPanel = new CurrentBorrowsPanel(nettyClient, userData);
        tabbedPane.addTab("当前借阅", currentBorrowsPanel);

        // 借阅历史
        borrowHistoryPanel = new BorrowHistoryPanel(nettyClient, userData);
        tabbedPane.addTab("借阅历史", borrowHistoryPanel);
        
        // 图书荐购
        bookRecommendationPanel = new BookRecommendationPanel(nettyClient, userData);
        tabbedPane.addTab("图书荐购", bookRecommendationPanel);

        // 图书借阅参考（教师可以查看图书借阅记录）
        bookManagementPanel = new TeacherBookReferencePanel(nettyClient, userData);
        tabbedPane.addTab("图书借阅参考", bookManagementPanel);
    }
    
    private void createAdminTabs() {
        // 图书管理（管理员完整权限）
        bookManagementPanel = new BookManagementPanel(nettyClient, userData, userRole);
        tabbedPane.addTab("图书管理", bookManagementPanel);

        // 借阅管理（管理员专用）
        JPanel borrowManagementPanel = new BorrowManagementPanel(nettyClient, userData);
        tabbedPane.addTab("借阅管理", borrowManagementPanel);

        // 统计报表（管理员专用）
        StatisticsReportMainPanel statisticsReportPanel = new StatisticsReportMainPanel(nettyClient, userData);
        statisticsReportPanel.setParentTabbedPane(tabbedPane);
        tabbedPane.addTab("统计报表", statisticsReportPanel);
        
        // 荐购管理（管理员专用）
        adminRecommendationPanel = new AdminRecommendationPanel(nettyClient, userData);
        tabbedPane.addTab("荐购管理", adminRecommendationPanel);

        // 注意：管理员不显示"图书搜索"、"我的书架"、"当前借阅"、"借阅历史"这些标签页
        // 这些功能只对学生和教师开放
    }

    private String determineUserRole() {
        // 从用户数据中确定角色
        Object primaryRole = userData.get("primaryRole");
        if (primaryRole != null) {
            String role = primaryRole.toString().toLowerCase();
            log.info("从primaryRole获取用户角色: {}", role);
            // 将manager映射为admin
            if ("manager".equals(role)) {
                log.info("将manager角色映射为admin");
                return "admin";
            }
            return role;
        }

        // 如果没有primaryRole，检查roles列表
        Object roles = userData.get("roles");
        if (roles instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> roleList = (List<String>) roles;
            if (roleList.contains("admin") || roleList.contains("manager")) {
                log.info("从roles列表获取用户角色: admin");
                return "admin";
            } else if (roleList.contains("teacher") || roleList.contains("staff")) {
                log.info("从roles列表获取用户角色: teacher");
                return "teacher";
            }
        }

        // 检查用户类型字段
        Object userType = userData.get("userType");
        if (userType != null) {
            String type = userType.toString().toLowerCase();
            log.info("从userType获取用户角色: {}", type);
            if (type.contains("admin") || type.contains("管理员") || type.contains("manager")) {
                return "admin";
            } else if (type.contains("teacher") || type.contains("教师") || type.contains("staff") || type.contains("教职工")) {
                return "teacher";
            }
        }

        // 默认为学生
        log.info("使用默认用户角色: student");
        return "student";
    }
    
    private void loadInitialData() {
        // 在后台线程中加载初始数据
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                try {
                    System.out.println("开始加载图书馆初始数据，用户角色: " + userRole);

                    // 这里可以预加载一些数据
                    // 比如热门图书、用户借阅统计等

                    System.out.println("图书馆初始数据加载完成");
                } catch (Exception e) {
                    System.err.println("加载图书馆初始数据时发生错误: " + e.getMessage());
                }
            }).start();
        });
    }

    /**
     * 刷新当前选中的面板数据
     */
    public void refreshCurrentPanel() {
        Component selectedComponent = tabbedPane.getSelectedComponent();
        if (selectedComponent instanceof RefreshablePanel) {
            ((RefreshablePanel) selectedComponent).refresh();
        }
    }
    
    /**
     * 可刷新面板接口
     */
    public interface RefreshablePanel {
        void refresh();
    }
}

package com.vcampus.client.core.ui.courseAdmin;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.client.core.util.IdUtils; // 新增：导入ID工具类
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

// Excel文件处理相关导入
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import java.io.FileInputStream;
import java.util.Vector;

// 明确导入AWT类以避免与POI冲突
import java.awt.Color;
import java.awt.Font;

/**
 * 课程管理面板 - 管理员使用
 * 支持对 tblCourse 和 tblSection 的增删改查操作
 * 支持界面缩放适应和院系筛选功能
 */
@Slf4j
public class CourseManagePanel extends JPanel {

    private NettyClient nettyClient;
    private Map<String, Object> userData;

    private JTabbedPane tabbedPane;
    private JTable courseTable;
    private JTable sectionTable;
    private DefaultTableModel courseTableModel;
    private DefaultTableModel sectionTableModel;
    private TableRowSorter<DefaultTableModel> courseTableSorter;

    // 搜索和筛选组件
    private JTextField txtSearch;
    private JComboBox<String> cmbDepartment;

    // 缩放相关
    private static final double DEFAULT_SCALE = 1.0;
    private double currentScale = DEFAULT_SCALE;

    // UI美化颜色常量
    private static final Color PRIMARY_COLOR = new Color(52, 144, 220);
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color DANGER_COLOR = new Color(220, 53, 69);
    private static final Color INFO_COLOR = new Color(23, 162, 184);
    private static final Color LIGHT_BG = new Color(248, 249, 250);
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color BORDER_COLOR = new Color(220, 220, 220);
    private static final Color TEXT_PRIMARY = new Color(52, 58, 64);
    private static final Color HEADER_GRADIENT_START = new Color(76, 151, 234);
    private static final Color HEADER_GRADIENT_END = new Color(52, 144, 220);

    // 课程表格列名
    private static final String[] COURSE_COLUMNS = {
        "课程ID", "课程名称", "学分", "开课学院"
    };

    // 教学班表格列名
    private static final String[] SECTION_COLUMNS = {
        "教学班ID", "课程名称", "学期", "任课教师", "教室", "容量", "上课时间"
    };

    // 预定义的学院列表
    private static final String[] DEPARTMENTS = {
        "全部", "计算机学院", "电子信息学院", "机械工程学院", "材料科学学院",
        "化学化工学院", "生命科学学院", "数学学院", "物理学院", "经济管理学院",
        "人文学院", "外国语学院", "艺术学院"
    };

    public CourseManagePanel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;

        setBackground(LIGHT_BG);
        initUI();
        setupScaling();
        loadData();

        log.info("课程管理面板初始化完成");
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);

        // 创建选项卡面板
        createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * 设置界面缩放适应功能
     */
    private void setupScaling() {
        // 监听面板大小变化
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustScaling();
            }
        });

        // 初始缩放调整
        SwingUtilities.invokeLater(this::adjustScaling);
    }

    /**
     * 根据窗口大小自动调整缩放
     */
    private void adjustScaling() {
        Dimension size = getSize();
        if (size.width > 0 && size.height > 0) {
            // 基于宽度计算缩放比例（参考宽度：1200px）
            double widthScale = Math.max(0.8, Math.min(1.5, size.width / 1200.0));
            // 基于高度计算缩放比例（参考高度：800px）
            double heightScale = Math.max(0.8, Math.min(1.5, size.height / 800.0));

            // 取较小值，确保内容完全可见
            double newScale = Math.min(widthScale, heightScale);

            if (Math.abs(newScale - currentScale) > 0.1) {
                currentScale = newScale;
                applyScaling();
            }
        }
    }

    /**
     * 应用缩放到所有组件
     */
    private void applyScaling() {
        // 计算字体大小
        int baseFontSize = 12;
        int scaledFontSize = Math.max(10, (int) (baseFontSize * currentScale));
        // 移除titleFontSize的动态计算，保持固定大小
        // int titleFontSize = Math.max(16, (int) (24 * currentScale));

        java.awt.Font baseFont = new java.awt.Font("微软雅黑", java.awt.Font.PLAIN, scaledFontSize);
        java.awt.Font boldFont = new java.awt.Font("微软雅黑", java.awt.Font.BOLD, scaledFontSize);
        // 移除titleFont的使用，标题字体将保持固定

        // 应用字体到各个组件（但排除标题）
        applyFontRecursively(this, baseFont);

        // 调整表格行高
        if (courseTable != null) {
            courseTable.setRowHeight((int) (30 * currentScale));
            courseTable.getTableHeader().setFont(boldFont);
        }
        if (sectionTable != null) {
            sectionTable.setRowHeight((int) (30 * currentScale));
            sectionTable.getTableHeader().setFont(boldFont);
        }

        // 重新验证和重绘
        revalidate();
        repaint();
    }

    /**
     * 递归应用字体到所有子组件（排除标题组件）
     */
    private void applyFontRecursively(Container container, java.awt.Font font) {
        for (Component component : container.getComponents()) {
            // 排除标题组件，避免被缩放影响
            if (component instanceof JLabel) {
                JLabel label = (JLabel) component;
                String text = label.getText();
                // 跳过标题和副标题，保持它们的固定字体大小
                if (!"课程管理系统".equals(text) && !"课程信息和教学班管理".equals(text)) {
                    label.setFont(font);
                }
            } else if (component instanceof JButton ||
                component instanceof JTextField || component instanceof JComboBox) {
                component.setFont(font);
            }
            if (component instanceof Container) {
                applyFontRecursively((Container) component, font);
            }
        }
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制渐变背景
                GradientPaint gradient = new GradientPaint(
                    0, 0, HEADER_GRADIENT_START,
                    getWidth(), getHeight(), HEADER_GRADIENT_END
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                g2d.dispose();
            }
        };
        
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setPreferredSize(new Dimension(0, 100));

        // 左侧标题区域
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("课程管理系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("课程信息管理 · 教学班管理 · 数据统计");
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(8));
        leftPanel.add(subtitleLabel);

        // 右侧管理员信息
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        JLabel adminLabel = new JLabel("管理员");
        adminLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        adminLabel.setForeground(Color.WHITE);
        adminLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel idLabel = new JLabel("ID: " + userData.get("cardNum"));
        idLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        idLabel.setForeground(new Color(255, 255, 255, 180));
        idLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        rightPanel.add(adminLabel);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(idLabel);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private void createTabbedPane() {
        tabbedPane = new JTabbedPane() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制卡片背景
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // 绘制边框
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        
        tabbedPane.setFont(new Font("微软雅黑", Font.BOLD, 14));
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
        
        // 自定义选项卡外观
        UIManager.put("TabbedPane.tabInsets", new Insets(12, 20, 12, 20));
        UIManager.put("TabbedPane.selectedTabPadInsets", new Insets(2, 2, 2, 2));

        // 课程管理选项卡
        JPanel coursePanel = createCoursePanel();
        tabbedPane.addTab("课程管理", coursePanel);

        // 教学班管理选项卡
        JPanel sectionPanel = createSectionPanel();
        tabbedPane.addTab("教学班管理", sectionPanel);

        // 设置选项卡颜色
        tabbedPane.setForegroundAt(0, TEXT_PRIMARY);
        tabbedPane.setForegroundAt(1, TEXT_PRIMARY);
    }

    private JPanel createCoursePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建工具栏卡片
        JPanel toolbarCard = createCardPanel();
        toolbarCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));

        JButton btnAdd = createModernButton("添加课程", SUCCESS_COLOR, Color.WHITE);
        btnAdd.addActionListener(e -> showAddCourseDialog());

        JButton btnEdit = createModernButton("编辑课程", PRIMARY_COLOR, Color.WHITE);
        btnEdit.addActionListener(e -> {
            int selectedRow = courseTable.getSelectedRow();
            if (selectedRow != -1) {
                editCourse(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "请选择要编辑的课程", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnDelete = createModernButton("删除课程", DANGER_COLOR, Color.WHITE);
        btnDelete.addActionListener(e -> {
            int[] selectedRows = courseTable.getSelectedRows();
            if (selectedRows.length > 0) {
                deleteCourses(selectedRows);
            } else {
                JOptionPane.showMessageDialog(this, "请选择要删除的课程", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnImport = createModernButton("批量导入", INFO_COLOR, Color.WHITE);
        btnImport.addActionListener(e -> importCoursesFromExcel());

        JButton btnRefresh = createModernButton("刷新数据", WARNING_COLOR, Color.WHITE);
        btnRefresh.addActionListener(e -> loadCourseData());

        toolbarCard.add(btnAdd);
        toolbarCard.add(btnEdit);
        toolbarCard.add(btnDelete);
        toolbarCard.add(btnImport);
        toolbarCard.add(btnRefresh);

        panel.add(toolbarCard, BorderLayout.NORTH);

        // 创建搜索面板
        JPanel searchCard = createCardPanel();
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10)); // 减少垂直间距
        searchCard.setPreferredSize(new Dimension(0, 60)); // 设置固定高度

        JLabel lblSearch = new JLabel("搜索：");
        lblSearch.setFont(new Font("微软雅黑", Font.BOLD, 14));
        lblSearch.setForeground(TEXT_PRIMARY);

        txtSearch = new JTextField(15);
        txtSearch.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        txtSearch.setToolTipText("输入课程名称或院系进行搜索");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JButton btnSearch = createModernButton("搜索", PRIMARY_COLOR, Color.WHITE);
        btnSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            String department = (String) cmbDepartment.getSelectedItem();
            searchCourses(keyword, department);
        });

        JButton btnClearSearch = createModernButton("清空", WARNING_COLOR, Color.WHITE);
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            cmbDepartment.setSelectedIndex(0);
            loadCourseData();
        });

        JLabel lblDept = new JLabel("院系：");
        lblDept.setFont(new Font("微软雅黑", Font.BOLD, 14));
        lblDept.setForeground(TEXT_PRIMARY);

        cmbDepartment = new JComboBox<>(DEPARTMENTS);
        cmbDepartment.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cmbDepartment.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        cmbDepartment.addActionListener(e -> {
            String selectedDepartment = (String) cmbDepartment.getSelectedItem();
            if (selectedDepartment != null) {
                filterCoursesByDepartment(selectedDepartment);
            }
        });

        searchCard.add(lblSearch);
        searchCard.add(txtSearch);
        searchCard.add(btnSearch);
        searchCard.add(btnClearSearch);
        searchCard.add(lblDept);
        searchCard.add(cmbDepartment);

        // 创建课程表格
        createCourseTable();

        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            "课程信息列表",
            0, 0,
            new Font("微软雅黑", Font.BOLD, 14),
            TEXT_PRIMARY
        ));

        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableCard.add(scrollPane, BorderLayout.CENTER);

        // 使用垂直布局将搜索面板和表格放在一起
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(searchCard, BorderLayout.NORTH);
        contentPanel.add(tableCard, BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建工具栏卡片
        JPanel toolbarCard = createCardPanel();
        toolbarCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));

        JButton btnAdd = createModernButton("添加教学班", SUCCESS_COLOR, Color.WHITE);
        btnAdd.addActionListener(e -> showAddSectionDialog());

        JButton btnEdit = createModernButton("编辑教学班", PRIMARY_COLOR, Color.WHITE);
        btnEdit.addActionListener(e -> {
            int selectedRow = sectionTable.getSelectedRow();
            if (selectedRow != -1) {
                editSection(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "请选择要编辑的教学班", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnDelete = createModernButton("删除教学班", DANGER_COLOR, Color.WHITE);
        btnDelete.addActionListener(e -> {
            int selectedRow = sectionTable.getSelectedRow();
            if (selectedRow != -1) {
                deleteSection(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "请选择要删除的教学班", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnRefresh = createModernButton("刷新数据", WARNING_COLOR, Color.WHITE);
        btnRefresh.addActionListener(e -> loadSectionData());

        toolbarCard.add(btnAdd);
        toolbarCard.add(btnEdit);
        toolbarCard.add(btnDelete);
        toolbarCard.add(btnRefresh);

        panel.add(toolbarCard, BorderLayout.NORTH);

        // 创建搜索面板
        JPanel searchCard = createCardPanel();
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        searchCard.setPreferredSize(new Dimension(0, 60));

        JLabel lblSearch = new JLabel("搜索：");
        lblSearch.setFont(new Font("微软雅黑", Font.BOLD, 14));
        lblSearch.setForeground(TEXT_PRIMARY);

        JTextField txtSectionSearch = new JTextField(15);
        txtSectionSearch.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        txtSectionSearch.setToolTipText("输入课程名称、教师、教室或学期进行搜索");
        txtSectionSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JButton btnSearch = createModernButton("搜索", PRIMARY_COLOR, Color.WHITE);
        btnSearch.addActionListener(e -> {
            String keyword = txtSectionSearch.getText().trim();
            searchSections(keyword);
        });

        JButton btnClearSearch = createModernButton("清空", WARNING_COLOR, Color.WHITE);
        btnClearSearch.addActionListener(e -> {
            txtSectionSearch.setText("");
            loadSectionData();
        });

        searchCard.add(lblSearch);
        searchCard.add(txtSectionSearch);
        searchCard.add(btnSearch);
        searchCard.add(btnClearSearch);

        // 创建教学班表格
        createSectionTable();

        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout());
        tableCard.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            "教学班信息列表",
            0, 0,
            new Font("微软雅黑", Font.BOLD, 14),
            TEXT_PRIMARY
        ));

        JScrollPane scrollPane = new JScrollPane(sectionTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);

        tableCard.add(scrollPane, BorderLayout.CENTER);

        // 创建内容面板，包含搜索和表格
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(searchCard, BorderLayout.NORTH);
        contentPanel.add(tableCard, BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }



    private void loadData() {
        loadCourseData();
        loadSectionData();
    }

    private void loadCourseData() {
        try {
            Request request = new Request("academic/course")
                    .addParam("action", "GET_ALL_COURSES");

            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> courseList = (List<Map<String, Object>>) response.getData();

                        // 清空现有数据
                        courseTableModel.setRowCount(0);

                        // 添加课程数据到表格
                        for (Map<String, Object> course : courseList) {
                            Vector<Object> row = new Vector<>();
                            // 修复：使用IdUtils确保course_Id为整数格式
                            Object courseIdObj = course.get("course_Id");
                            int courseId = IdUtils.parseId(courseIdObj);
                            row.add(courseId);
                            row.add(course.get("courseName"));
                            row.add(course.get("credit"));
                            row.add(course.get("department"));
                            courseTableModel.addRow(row);
                        }

                        log.info("课程数据加载完成，共 {} 条记录", courseList.size());
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "加载课程数据失败：" + response.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.error("加载课程数据时发生错误", throwable);
                    JOptionPane.showMessageDialog(this,
                            "加载课程数据时发生错误：" + throwable.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
        } catch (Exception e) {
            log.error("发送课程数据请求时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "发送课程数据请求时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSectionData() {
        try {
            Request request = new Request("academic/course")
                    .addParam("action", "GET_ALL_SECTIONS");

            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> sectionList = (List<Map<String, Object>>) response.getData();

                        // 清空现有数据
                        sectionTableModel.setRowCount(0);

                        // 添加教学班数据到表格
                        for (Map<String, Object> section : sectionList) {
                            Vector<Object> row = new Vector<>();
                            // 修复：使用IdUtils确保section_Id为整数格式
                            Object sectionIdObj = section.get("section_Id");
                            int sectionId = IdUtils.parseId(sectionIdObj);
                            row.add(sectionId);
                            row.add(section.get("courseName"));
                            row.add(section.get("Term"));
                            row.add(section.get("Teacher_id"));
                            row.add(section.get("Room"));
                            row.add(section.get("Capacity"));
                            row.add(section.get("Schedule"));
                            sectionTableModel.addRow(row);
                        }

                        log.info("教学班数据加载完成，共 {} 条记录", sectionList.size());
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "加载教学班数据失败：" + response.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.error("加载教学班数据时发生错误", throwable);
                    JOptionPane.showMessageDialog(this,
                            "加载教学班数据时发生错误：" + throwable.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
        } catch (Exception e) {
            log.error("发送教学班数据请求时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "发送教学班数据请求时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddCourseDialog() {
        CourseEditDialog dialog = new CourseEditDialog(
                SwingUtilities.getWindowAncestor(this),
                "添加课程",
                null
        );
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Map<String, Object> courseData = dialog.getCourseData();
            addCourse(courseData);
        }
    }

    private void editCourse(int row) {
        if (row >= 0 && row < courseTableModel.getRowCount()) {
            Map<String, Object> courseData = new java.util.HashMap<>();
            courseData.put("course_Id", courseTableModel.getValueAt(row, 0));
            courseData.put("courseName", courseTableModel.getValueAt(row, 1));
            courseData.put("credit", courseTableModel.getValueAt(row, 2));
            courseData.put("department", courseTableModel.getValueAt(row, 3));

            CourseEditDialog dialog = new CourseEditDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "编辑课程",
                    courseData
            );
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                Map<String, Object> updatedData = dialog.getCourseData();
                updateCourse(updatedData);
            }
        }
    }

    private void deleteCourse(int row) {
        if (row >= 0 && row < courseTableModel.getRowCount()) {
            Object courseIdObj = courseTableModel.getValueAt(row, 0);
            String courseId = courseIdObj.toString().trim(); // 去除前后空格
            String courseName = courseTableModel.getValueAt(row, 1).toString();

            // 客户端验证courseId格式
            if (!courseId.matches("\\d+")) {
                JOptionPane.showMessageDialog(this,
                        "课程ID格式错误: [" + courseId + "]，请刷新数据后重试",
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int result = JOptionPane.showConfirmDialog(this,
                    "确定要删除课程 \"" + courseName + "\" 吗？\n注意：删除课程将会同时删除相关的教学班信息！",
                    "确认删除",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                try {
                    Request request = new Request("academic/course")
                            .addParam("action", "DELETE_COURSE")
                            .addParam("courseId", courseId);

                    nettyClient.sendRequest(request).thenAccept(response -> {
                        SwingUtilities.invokeLater(() -> {
                            if (response.isSuccess()) {
                                JOptionPane.showMessageDialog(this,
                                        "课程删除成功！",
                                        "成功",
                                        JOptionPane.INFORMATION_MESSAGE);
                                loadData(); // 刷新数据
                            } else {
                                JOptionPane.showMessageDialog(this,
                                        "删除课程失败：" + response.getMessage(),
                                        "错误",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    });
                } catch (Exception e) {
                    log.error("删除课程时发生错误", e);
                    JOptionPane.showMessageDialog(this,
                            "删除课程时发生错误：" + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void addCourse(Map<String, Object> courseData) {
        try {
            Request request = new Request("academic/course")
                    .addParam("action", "INSERT_COURSE")
                    .addParam("courseName", courseData.get("courseName").toString())
                    .addParam("credit", courseData.get("credit").toString())
                    .addParam("department", courseData.get("department").toString());

            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(this,
                                "课程添加成功！",
                                "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadCourseData(); // 刷新数据
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "添加课程失败：" + response.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            });
        } catch (Exception e) {
            log.error("添加课程时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "添加课程时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCourse(Map<String, Object> courseData) {
        try {
            // 修复：确保courseId为整数格式
            Object courseIdObj = courseData.get("course_Id");
            String courseIdStr;
            if (courseIdObj instanceof Number) {
                // 如果是数字类型，转换为整数字符串
                courseIdStr = String.valueOf(((Number) courseIdObj).intValue());
            } else {
                // 如果是字符串，尝试解析并转换为整数
                String rawStr = courseIdObj.toString().trim();
                try {
                    // 处理可能的浮点数格式（如"1.0"）
                    double courseIdDouble = Double.parseDouble(rawStr);
                    courseIdStr = String.valueOf((int) courseIdDouble);
                } catch (NumberFormatException e) {
                    log.warn("课程ID格式无法解析: {}", rawStr);
                    courseIdStr = rawStr; // 保持原值
                }
            }

            Request request = new Request("academic/course")
                    .addParam("action", "UPDATE_COURSE")
                    .addParam("courseId", courseIdStr)
                    .addParam("courseName", courseData.get("courseName").toString())
                    .addParam("credit", courseData.get("credit").toString())
                    .addParam("department", courseData.get("department").toString());

            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(this,
                                "课程更新成功！",
                                "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadCourseData(); // 刷新数据
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "更新课程失败：" + response.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            });
        } catch (Exception e) {
            log.error("更新课程时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "更新课程时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddSectionDialog() {
        SectionEditDialog dialog = new SectionEditDialog(
                SwingUtilities.getWindowAncestor(this),
                "添加教学班",
                null,
                nettyClient
        );
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Map<String, Object> sectionData = dialog.getSectionData();
            addSection(sectionData);
        }
    }

    private void editSection(int row) {
        if (row >= 0 && row < sectionTableModel.getRowCount()) {
            Map<String, Object> sectionData = new java.util.HashMap<>();
            sectionData.put("section_Id", sectionTableModel.getValueAt(row, 0));
            sectionData.put("courseName", sectionTableModel.getValueAt(row, 1));
            sectionData.put("Term", sectionTableModel.getValueAt(row, 2));
            sectionData.put("Teacher_id", sectionTableModel.getValueAt(row, 3));
            sectionData.put("Room", sectionTableModel.getValueAt(row, 4));
            sectionData.put("Capacity", sectionTableModel.getValueAt(row, 5));
            sectionData.put("Schedule", sectionTableModel.getValueAt(row, 6));

            // 通过课程名称查找对应的course_Id，确保编辑时能正确预选课程
            String courseName = sectionTableModel.getValueAt(row, 1).toString();
            findCourseIdByName(courseName, courseId -> {
                if (courseId != null) {
                    sectionData.put("course_Id", courseId);
                }

                // 创建并显示编辑对话框
                SectionEditDialog dialog = new SectionEditDialog(
                        SwingUtilities.getWindowAncestor(this),
                        "编辑教学班",
                        sectionData,
                        nettyClient
                );
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    Map<String, Object> updatedData = dialog.getSectionData();
                    updateSection(updatedData);
                }
            });
        }
    }

    /**
     * 通过课程名称查找课程ID，用于编辑教学班时正确预选课程
     */
    private void findCourseIdByName(String courseName, java.util.function.Consumer<String> callback) {
        try {
            Request request = new Request("academic/course")
                    .addParam("action", "GET_ALL_COURSES");

            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    String foundCourseId = null;
                    if (response != null && response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> courseList = (List<Map<String, Object>>) response.getData();

                        for (Map<String, Object> course : courseList) {
                            if (courseName.equals(course.get("courseName"))) {
                                Object courseIdObj = course.get("course_Id");
                                if (courseIdObj instanceof Number) {
                                    foundCourseId = String.valueOf(((Number) courseIdObj).intValue());
                                } else {
                                    foundCourseId = courseIdObj.toString();
                                }
                                break;
                            }
                        }
                    }
                    callback.accept(foundCourseId);
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.warn("查找课程ID时发生错误: {}", throwable.getMessage());
                    callback.accept(null);
                });
                return null;
            });
        } catch (Exception e) {
            log.error("查找课程ID请求失败", e);
            callback.accept(null);
        }
    }

    private void deleteSection(int row) {
        if (row >= 0 && row < sectionTableModel.getRowCount()) {
            String sectionId = sectionTableModel.getValueAt(row, 0).toString();
            String courseName = sectionTableModel.getValueAt(row, 1).toString();

            int result = JOptionPane.showConfirmDialog(this,
                    "确定要删除教学班 \"" + courseName + "\" (ID: " + sectionId + ") 吗？",
                    "确认删除",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                try {
                    Request request = new Request("academic/course")
                            .addParam("action", "DELETE_SECTION")
                            .addParam("sectionId", sectionId);

                    nettyClient.sendRequest(request).thenAccept(response -> {
                        SwingUtilities.invokeLater(() -> {
                            if (response.isSuccess()) {
                                JOptionPane.showMessageDialog(this,
                                        "教学班删除成功！",
                                        "成功",
                                        JOptionPane.INFORMATION_MESSAGE);
                                loadSectionData(); // 刷新数据
                            } else {
                                JOptionPane.showMessageDialog(this,
                                        "删除教学班失败：" + response.getMessage(),
                                        "错误",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    });
                } catch (Exception e) {
                    log.error("删除教学班时发生错误", e);
                    JOptionPane.showMessageDialog(this,
                            "删除教学班时发生错误：" + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void addSection(Map<String, Object> sectionData) {
        try {
            Request request = new Request("academic/course")
                    .addParam("action", "INSERT_SECTION")
                    .addParam("courseId", sectionData.get("course_Id").toString())
                    .addParam("term", sectionData.get("Term").toString())
                    .addParam("teacherId", sectionData.get("Teacher_id").toString())
                    .addParam("room", sectionData.get("Room").toString())
                    .addParam("capacity", sectionData.get("Capacity").toString())
                    .addParam("schedule", sectionData.get("Schedule").toString());

            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(this,
                                "教学班添加成功！",
                                "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadSectionData(); // 刷新数据
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "添加教学班失败：" + response.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            });
        } catch (Exception e) {
            log.error("添加教学班时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "添加教学班时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSection(Map<String, Object> sectionData) {
        try {
            Request request = new Request("academic/course")
                    .addParam("action", "UPDATE_SECTION")
                    .addParam("sectionId", sectionData.get("section_Id").toString())
                    .addParam("courseId", sectionData.get("course_Id").toString())
                    .addParam("term", sectionData.get("Term").toString())
                    .addParam("teacherId", sectionData.get("Teacher_id").toString())
                    .addParam("room", sectionData.get("Room").toString())
                    .addParam("capacity", sectionData.get("Capacity").toString())
                    .addParam("schedule", sectionData.get("Schedule").toString());

            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(this,
                                "教学班更新成功！",
                                "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadSectionData(); // 刷新数据
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "更新教学班失败：" + response.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            });
        } catch (Exception e) {
            log.error("更新教学班时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "更新教学班时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchCourses(String keyword, String department) {
        try {
            Request request = new Request("academic/course")
                    .addParam("action", "SEARCH_COURSES")
                    .addParam("keyword", keyword)
                    .addParam("department", department);

            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> courseList = (List<Map<String, Object>>) response.getData();

                        // 清空现有数据
                        courseTableModel.setRowCount(0);

                        // 添加搜索结果到表格
                        for (Map<String, Object> course : courseList) {
                            Vector<Object> row = new Vector<>();
                            // 修复：确保course_Id为整数格式（与loadCourseData保持一致）
                            Object courseIdObj = course.get("course_Id");
                            if (courseIdObj instanceof Number) {
                                // 如果是数字类型，转换为整数
                                int courseIdInt = ((Number) courseIdObj).intValue();
                                row.add(courseIdInt);
                            } else {
                                // 如果是字符串，尝试解析
                                String courseIdStr = courseIdObj.toString();
                                try {
                                    // 处理可能的浮点数格式（如"1.0"）
                                    double courseIdDouble = Double.parseDouble(courseIdStr);
                                    int courseIdInt = (int) courseIdDouble;
                                    row.add(courseIdInt);
                                } catch (NumberFormatException e) {
                                    row.add(courseIdObj); // 保持原值
                                }
                            }
                            row.add(course.get("courseName"));
                            row.add(course.get("credit"));
                            row.add(course.get("department"));
                            courseTableModel.addRow(row);
                        }

                        log.info("课程搜索完成，共 {} 条记录", courseList.size());
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "搜索课程失败：" + response.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.error("搜索课程时发生错误", throwable);
                    JOptionPane.showMessageDialog(this,
                            "搜索课程时发生错误：" + throwable.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
        } catch (Exception e) {
            log.error("发送课程搜索请求时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "发送课程搜索请求时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 根据院系筛选课程表格
     */
    private void filterCoursesByDepartment(String department) {
        if (courseTableSorter == null) return;

        if ("全部".equals(department) || department == null || department.trim().isEmpty()) {
            // 显示所有课程
            courseTableSorter.setRowFilter(null);
        } else {
            // 根据院系筛选
            RowFilter<DefaultTableModel, Object> departmentFilter = RowFilter.regexFilter(
                "(?i)" + department, 3); // 第3列是院系列，(?i)表示忽略大小写
            courseTableSorter.setRowFilter(departmentFilter);
        }

        // 更新状态显示
        updateFilterStatus();
    }

    /**
     * 更新筛选状态显示
     */
    private void updateFilterStatus() {
        if (courseTable != null && courseTableSorter != null) {
            int totalRows = courseTableModel.getRowCount();
            int visibleRows = courseTable.getRowCount();

            String status = String.format("显示 %d / %d 条记录", visibleRows, totalRows);
            if (visibleRows < totalRows) {
                status += " (已筛选)";
            }

            // 可以在状态栏显示，这里先用日志记录
            log.debug("课程表格状态: {}", status);
        }
    }

    /**
     * 手动设置缩放比例
     */
    public void setScale(double scale) {
        if (scale >= 0.5 && scale <= 2.0) {
            currentScale = scale;
            applyScaling();
        }
    }

    /**
     * 获取当前缩放比例
     */
    public double getCurrentScale() {
        return currentScale;
    }

    /**
     * 重置缩放到默认值
     */
    public void resetScale() {
        currentScale = DEFAULT_SCALE;
        applyScaling();
    }
    
    // === Excel导入和批量删除功能 ===
    
    /**
     * 从Excel文件导入课程数据
     */
    private void importCoursesFromExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择课程数据文件（Excel格式）");
        
        // 设置文件过滤器支持Excel文件
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                if (f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".xlsx") || name.endsWith(".xls");
            }
            
            @Override
            public String getDescription() {
                return "Excel文件 (*.xlsx, *.xls)";
            }
        });
        
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;
        
        java.io.File file = chooser.getSelectedFile();
        if (file == null || !file.exists()) {
            JOptionPane.showMessageDialog(this, "文件不存在", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 在后台线程中处理导入
        SwingUtilities.invokeLater(() -> new Thread(() -> {
            try {
                List<Map<String, Object>> courses = parseExcelCourses(file);
                if (courses.isEmpty()) {
                    SwingUtilities.invokeLater(() -> 
                        JOptionPane.showMessageDialog(this, "Excel文件中没有找到有效的课程数据", "提示", JOptionPane.WARNING_MESSAGE));
                    return;
                }
                
                // 批量导入课程
                importCoursesBatch(courses);
                
            } catch (Exception e) {
                log.error("导入课程失败", e);
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(this, "导入失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE));
            }
        }).start());
    }
    
    /**
     * 解析Excel文件中的课程数据
     */
    private List<Map<String, Object>> parseExcelCourses(java.io.File file) throws Exception {
        List<Map<String, Object>> courses = new java.util.ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook;
            String fileName = file.getName().toLowerCase();
            
            // 根据文件扩展名选择合适的工作簿类型
            if (fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (fileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook(fis);
            } else {
                throw new IllegalArgumentException("不支持的Excel文件格式");
            }
            
            try {
                Sheet sheet = workbook.getSheetAt(0); // 读取第一个工作表
                if (sheet == null) {
                    log.warn("Excel文件中没有找到工作表");
                    return courses;
                }
                
                // 获取表头行
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    log.warn("Excel文件第一行为空");
                    return courses;
                }
                
                // 解析表头，建立列索引映射
                Map<String, Integer> columnMap = new java.util.HashMap<>();
                for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                    Cell cell = headerRow.getCell(i);
                    if (cell != null) {
                        String headerName = getCellValueAsString(cell).trim();
                        if (!headerName.isEmpty()) {
                            String mappedKey = mapCourseColumnName(headerName);
                            if (mappedKey != null) {
                                columnMap.put(mappedKey, i);
                            }
                        }
                    }
                }
                
                log.info("Excel列映射: {}", columnMap);
                
                // 检查必要的列是否存在
                if (!columnMap.containsKey("courseName")) {
                    throw new IllegalArgumentException("Excel文件必须包含'课程名称'列");
                }
                
                // 解析数据行
                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row == null) continue;
                    
                    Map<String, Object> course = new java.util.HashMap<>();
                    boolean hasData = false;
                    
                    // 解析每一列的数据
                    for (Map.Entry<String, Integer> entry : columnMap.entrySet()) {
                        String key = entry.getKey();
                        int colIndex = entry.getValue();
                        Cell cell = row.getCell(colIndex);
                        
                        if (cell != null) {
                            String value = getCellValueAsString(cell).trim();
                            if (!value.isEmpty()) {
                                hasData = true;
                                
                                // 根据列类型进行数据转换
                                switch (key) {
                                    case "courseId":
                                        try {
                                            int courseId = Integer.parseInt(value);
                                            course.put("courseId", courseId);
                                        } catch (NumberFormatException e) {
                                            log.warn("课程ID格式错误，行{}: {}", rowIndex + 1, value);
                                        }
                                        break;
                                    case "courseName":
                                        course.put("courseName", value);
                                        break;
                                    case "credit":
                                        try {
                                            int credit = Integer.parseInt(value);
                                            course.put("credit", credit);
                                        } catch (NumberFormatException e) {
                                            log.warn("学分格式错误，行{}: {}", rowIndex + 1, value);
                                        }
                                        break;
                                    case "department":
                                        course.put("department", value);
                                        break;
                                }
                            }
                        }
                    }
                    
                    // 如果行有数据且包含必要字段，则添加到列表
                    if (hasData && course.containsKey("courseName")) {
                        // 设置默认值
                        if (!course.containsKey("credit")) {
                            course.put("credit", 3); // 默认3学分
                        }
                        if (!course.containsKey("department")) {
                            course.put("department", "未指定学院");
                        }
                        
                        courses.add(course);
                    }
                }
                
            } finally {
                workbook.close();
            }
        }
        
        log.info("Excel文件解析完成，共解析到{}条课程数据", courses.size());
        return courses;
    }
    
    /**
     * 获取单元格值作为字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 处理日期格式
                    java.util.Date date = cell.getDateCellValue();
                    return new java.text.SimpleDateFormat("yyyy/MM/dd").format(date);
                } else {
                    // 处理数字格式
                    double numericValue = cell.getNumericCellValue();
                    // 如果是整数，不显示小数点
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return getCellValueAsString(cell.getCachedFormulaResultType(), cell);
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            default:
                return "";
        }
    }
    
    /**
     * 处理公式单元格的值
     */
    private String getCellValueAsString(CellType cellType, Cell cell) {
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    return String.valueOf((long) numericValue);
                } else {
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
    
    /**
     * 映射Excel列名到系统字段名
     */
    private String mapCourseColumnName(String headerName) {
        if (headerName == null) return null;
        
        String name = headerName.trim().toLowerCase();
        
        // 课程ID列映射
        if (name.equals("课程id") || name.equals("courseid") || name.equals("course_id")) {
            return "courseId";
        }
        // 课程名称列映射
        if (name.equals("课程名称") || name.equals("coursename") || name.equals("course_name")) {
            return "courseName";
        }
        // 学分列映射
        if (name.equals("学分") || name.equals("credit")) {
            return "credit";
        }
        // 开课学院列映射
        if (name.equals("开课学院") || name.equals("department")) {
            return "department";
        }
        
        return null; // 不认识的列名
    }
    
    /**
     * 批量导入课程
     */
    private void importCoursesBatch(List<Map<String, Object>> courses) {
        final int[] successCount = {0};
        final int[] failCount = {0};
        
        for (Map<String, Object> course : courses) {
            try {
                Request request = new Request("academic/course")
                    .addParam("action", "INSERT_COURSE")
                    .addParam("courseName", String.valueOf(course.get("courseName")))
                    .addParam("credit", String.valueOf(course.get("credit")))
                    .addParam("department", String.valueOf(course.get("department")));
                
                Response response = nettyClient.sendRequest(request).get(20, java.util.concurrent.TimeUnit.SECONDS);
                
                if (response != null && response.isSuccess()) {
                    successCount[0]++;
                } else {
                    failCount[0]++;
                    log.warn("导入课程失败: {}", response != null ? response.getMessage() : "未知错误");
                }
            } catch (Exception e) {
                failCount[0]++;
                log.error("导入课程异常: {}", e.getMessage());
            }
        }
        
        // 显示结果
        SwingUtilities.invokeLater(() -> {
            if (failCount[0] == 0) {
                JOptionPane.showMessageDialog(this, "成功导入 " + successCount[0] + " 个课程", "导入完成", JOptionPane.INFORMATION_MESSAGE);
            } else if (successCount[0] == 0) {
                JOptionPane.showMessageDialog(this, "导入失败，请检查Excel文件格式", "导入失败", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "成功导入 " + successCount[0] + " 个课程，" + failCount[0] + " 个导入失败", "导入完成", JOptionPane.WARNING_MESSAGE);
            }
            
            // 刷新课程列表
            loadCourseData();
        });
    }
    
    /**
     * 批量删除课程
     */
    private void deleteCourses(int[] selectedRows) {
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的课程", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 获取选中的课程信息
        List<String> courseNames = new java.util.ArrayList<>();
        List<String> courseIds = new java.util.ArrayList<>();
        
        for (int row : selectedRows) {
            if (row >= 0 && row < courseTableModel.getRowCount()) {
                Object courseIdObj = courseTableModel.getValueAt(row, 0);
                String courseId = courseIdObj.toString().trim();
                String courseName = courseTableModel.getValueAt(row, 1).toString();
                
                // 验证courseId格式
                if (courseId.matches("\\d+")) {
                    courseIds.add(courseId);
                    courseNames.add(courseName);
                }
            }
        }
        
        if (courseIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有找到有效的课程ID，请刷新数据后重试", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 确认删除
        String message;
        if (courseIds.size() == 1) {
            message = "确定要删除课程 \"" + courseNames.get(0) + "\" 吗？\n注意：删除课程将会同时删除相关的教学班信息！";
        } else {
            message = "确定要删除选中的 " + courseIds.size() + " 个课程吗？\n注意：删除课程将会同时删除相关的教学班信息！";
        }
        
        int result = JOptionPane.showConfirmDialog(this, message, "确认删除", 
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            // 批量删除
            deleteCoursesBatch(courseIds, courseNames);
        }
    }
    
    /**
     * 执行批量删除
     */
    private void deleteCoursesBatch(List<String> courseIds, List<String> courseNames) {
        int successCount = 0;
        int failCount = 0;
        
        for (int i = 0; i < courseIds.size(); i++) {
            String courseId = courseIds.get(i);
            String courseName = courseNames.get(i);
            
            try {
                Request request = new Request("academic/course")
                    .addParam("action", "DELETE_COURSE")
                    .addParam("courseId", courseId);
                
                Response response = nettyClient.sendRequest(request).get(20, java.util.concurrent.TimeUnit.SECONDS);
                
                if (response != null && response.isSuccess()) {
                    successCount++;
                    log.info("成功删除课程: {} (ID: {})", courseName, courseId);
                } else {
                    failCount++;
                    log.warn("删除课程失败: {} (ID: {}), 错误: {}", courseName, courseId, 
                        response != null ? response.getMessage() : "未知错误");
                }
            } catch (Exception e) {
                failCount++;
                log.error("删除课程异常: {} (ID: {}), 错误: {}", courseName, courseId, e.getMessage());
            }
        }
        
        // 显示结果
        if (failCount == 0) {
            JOptionPane.showMessageDialog(this, "成功删除 " + successCount + " 个课程", "删除完成", JOptionPane.INFORMATION_MESSAGE);
        } else if (successCount == 0) {
            JOptionPane.showMessageDialog(this, "删除失败，请重试", "删除失败", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "成功删除 " + successCount + " 个课程，" + failCount + " 个删除失败", "删除完成", JOptionPane.WARNING_MESSAGE);
        }
        
        // 刷新课程列表
        loadCourseData();
    }

    /**
     * 创建卡片样式面板
     */
    private JPanel createCardPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制阴影
                g2d.setColor(new Color(0, 0, 0, 20));
                g2d.fillRoundRect(3, 3, getWidth() - 3, getHeight() - 3, 12, 12);
                
                // 绘制卡片背景
                g2d.setColor(CARD_BG);
                g2d.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 12, 12);
                
                // 绘制边框
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(1f));
                g2d.drawRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 12, 12);
                
                g2d.dispose();
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return panel;
    }

    /**
     * 创建现代化按钮
     */
    private JButton createModernButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 绘制按钮背景
                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // 绘制文字
                g2d.setColor(fgColor);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), textX, textY);
                
                g2d.dispose();
            }
        };
        
        button.setFont(new Font("微软雅黑", Font.BOLD, 12));
        button.setForeground(fgColor);
        button.setBackground(bgColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(120, 35));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加鼠标悬停效果
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });
        
        return button;
    }

    /**
     * 创建教学班表格
     */
    private void createSectionTable() {
        sectionTableModel = new DefaultTableModel(SECTION_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 不可直接编辑，通过对话框编辑
            }
        };

        sectionTable = new JTable(sectionTableModel);
        setupModernTable(sectionTable);

        // 添加双击编辑功能
        sectionTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = sectionTable.getSelectedRow();
                    if (selectedRow != -1) {
                        editSection(selectedRow);
                    }
                }
            }
        });
    }

    /**
     * 创建课程表格
     */
    private void createCourseTable() {
        courseTableModel = new DefaultTableModel(COURSE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 不可直接编辑，通过对话框编辑
            }
        };

        courseTable = new JTable(courseTableModel);
        setupModernTable(courseTable);
        
        // 启用多选功能
        courseTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // 设置表格排序器
        courseTableSorter = new TableRowSorter<>(courseTableModel);
        courseTable.setRowSorter(courseTableSorter);

        // 添加双击编辑功能
        courseTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = courseTable.getSelectedRow();
                    if (selectedRow != -1) {
                        editCourse(selectedRow);
                    }
                }
            }
        });
    }

    /**
     * 设置现代化表格样式
     */
    private void setupModernTable(JTable table) {
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(40);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(52, 144, 220, 30));
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setBackground(Color.WHITE);

        // 设置表头样式
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("微软雅黑", Font.BOLD, 13));
        header.setBackground(new Color(250, 250, 250));
        header.setForeground(TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(0, 45));

        // 禁用表格拖动功能
        table.setDragEnabled(false);
        header.setReorderingAllowed(false);

        // 设置交替行颜色
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
    }

    /**
     * 交替行渲染器
     */
    private class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(new Color(248, 249, 250));
                }
            }

            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            return c;
        }
    }

    /**
     * 搜索教学班
     */
    private void searchSections(String keyword) {
        try {
            Request request = new Request("academic/course")
                    .addParam("action", "SEARCH_SECTIONS")
                    .addParam("keyword", keyword);

            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> sectionList = (List<Map<String, Object>>) response.getData();

                        // 清空现有数据
                        sectionTableModel.setRowCount(0);

                        // 添加搜索结果到表格
                        for (Map<String, Object> section : sectionList) {
                            Vector<Object> row = new Vector<>();
                            Object sectionIdObj = section.get("section_Id");
                            int sectionId = IdUtils.parseId(sectionIdObj);
                            row.add(sectionId);
                            row.add(section.get("courseName"));
                            row.add(section.get("Term"));
                            row.add(section.get("Teacher_id"));
                            row.add(section.get("Room"));
                            row.add(section.get("Capacity"));
                            row.add(section.get("Schedule"));
                            sectionTableModel.addRow(row);
                        }

                        log.info("教学班搜索完成，共找到 {} 条记录", sectionList.size());
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "搜索教学班失败：" + response.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.error("搜索教学班时发生错误", throwable);
                    JOptionPane.showMessageDialog(this,
                            "搜索教学班时发生错误：" + throwable.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
        } catch (Exception e) {
            log.error("发送教学班搜索请求时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "发送教学班搜索请求时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

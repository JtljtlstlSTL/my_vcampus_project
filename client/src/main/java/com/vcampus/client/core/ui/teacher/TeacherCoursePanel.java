package com.vcampus.client.core.ui.teacher;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.client.core.util.IdUtils;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.util.GPACalculator;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 教师端课程管理面板
 * 包含教学班管理和学生成绩打分功能
 */
@Slf4j
public class TeacherCoursePanel extends JPanel {

    private NettyClient nettyClient;
    private Map<String, Object> userData;
    private String teacherId;

    private JTabbedPane tabbedPane;
    private JTable sectionsTable;
    private JTable studentsTable;
    private DefaultTableModel sectionsTableModel;
    private DefaultTableModel studentsTableModel;

    // 教学班表格列名
    private static final String[] SECTIONS_COLUMNS = {
        "教学班ID", "课程名称", "学期", "任课教师", "教室", "容量", "已报名", "上课时间", "操作"
    };

    // 学生成绩表格列名
    private static final String[] STUDENTS_COLUMNS = {
        "学号", "姓名", "性别", "专业", "年级", "学分", "分数", "GPA", "操作"
    };

    private int currentSectionId = -1;
    private String currentCourseName = "";

    // 添加用于成绩管理的组件引用
    private JComboBox<SectionItem> sectionComboBox;
    private JLabel lblCurrentSection;
    private List<Map<String, Object>> teacherSections = new ArrayList<>();

    public TeacherCoursePanel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        // 修复：使用教师工号而不是卡号来查询教学班
        // 优先使用 staffId，如果不存在则使用 cardNum
        Object staffIdObj = userData.get("staffId");
        if (staffIdObj != null) {
            this.teacherId = staffIdObj.toString();
        } else {
            // 如果没有 staffId，使用 cardNum（可能需要后续转换）
            this.teacherId = userData.get("cardNum").toString();
        }

        initUI();
        loadTeacherSections();

        log.info("教师课程管理面板初始化完成，教师ID: {}", this.teacherId);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        // 美化：添加渐变背景和更精致的边框
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        setBackground(new Color(248, 250, 252));

        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);

        // 创建选项卡面板
        createTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createTitlePanel() {
        // 美化：创建带渐变背景的标题面板
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制渐变背景
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(245, 247, 250),
                    0, getHeight(), new Color(255, 255, 255)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2d.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 240), 1),
            BorderFactory.createEmptyBorder(20, 0, 25, 0)
        ));
        panel.setBackground(new Color(248, 250, 252));
        panel.setOpaque(false);

        // 创建标题容器
        JPanel titleContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        titleContainer.setOpaque(false);

        // 添加教学图标
        try {
            ImageIcon teachingIcon = new ImageIcon(getClass().getResource("/figures/teaching.png"));
            if (teachingIcon.getIconWidth() > 0) {
                Image scaledIcon = teachingIcon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                JLabel iconLabel = new JLabel(new ImageIcon(scaledIcon));
                titleContainer.add(iconLabel);
            }
        } catch (Exception e) {
            // 如果图标不存在，使用Unicode符号
            JLabel iconLabel = new JLabel("📚");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
            titleContainer.add(iconLabel);
        }

        JLabel titleLabel = new JLabel("教师课程管理系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 26));
        titleLabel.setForeground(new Color(51, 102, 153));
        titleContainer.add(titleLabel);
        titleContainer.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 美化：教师信息卡片
        JPanel teacherInfoCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制圆角背景
                g2d.setColor(new Color(255, 255, 255, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // 绘制边框
                g2d.setColor(new Color(200, 220, 240));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);

                g2d.dispose();
            }
        };
        teacherInfoCard.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 8));
        teacherInfoCard.setOpaque(false);
        teacherInfoCard.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        // 教师头像或图标
        JLabel avatarLabel = new JLabel("👨‍🏫");
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        teacherInfoCard.add(avatarLabel);

        JLabel teacherLabel = new JLabel("教师：" + userData.get("userName") + " (" + teacherId + ")");
        teacherLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        teacherLabel.setForeground(new Color(85, 85, 85));
        teacherInfoCard.add(teacherLabel);

        // 添加在线状态指示器
        JLabel statusLabel = new JLabel("● 在线");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(34, 139, 34));
        teacherInfoCard.add(statusLabel);

        panel.add(titleContainer);
        panel.add(Box.createVerticalStrut(15));
        panel.add(teacherInfoCard);

        return panel;
    }

    private void createTabbedPane() {
        tabbedPane = new JTabbedPane();
        // 美化：选项卡样式
        tabbedPane.setFont(new Font("微软雅黑", Font.BOLD, 14));
        tabbedPane.setBackground(new Color(248, 250, 252));
        tabbedPane.setForeground(new Color(51, 102, 153));

        // 美化：设置选项卡边框和间距
        tabbedPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 240), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // 我的教学班选项卡
        JPanel sectionsPanel = createSectionsPanel();
        tabbedPane.addTab("我的教学班", sectionsPanel);

        // 学生成绩管理选项卡
        JPanel gradesPanel = createGradesPanel();
        tabbedPane.addTab("成绩管理", gradesPanel);

        // 美化：设置选项卡颜色
        tabbedPane.setTabPlacement(JTabbedPane.TOP);
    }

    private JPanel createSectionsPanel() {
        // 美化：创建带渐变背景的面板
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制淡淡的渐变背景
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(252, 254, 255),
                    0, getHeight(), new Color(248, 250, 252)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.dispose();
            }
        };
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 240, 250), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // 创建工具栏
        JPanel toolbar = createSectionsToolbar();
        panel.add(toolbar, BorderLayout.NORTH);

        // 创建教学班表格
        sectionsTableModel = new DefaultTableModel(SECTIONS_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == SECTIONS_COLUMNS.length - 1; // 只有操作列可编辑
            }
        };

        sectionsTable = new JTable(sectionsTableModel);
        // 美化：表格样式
        sectionsTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        sectionsTable.setRowHeight(40);
        sectionsTable.setSelectionBackground(new Color(230, 244, 255));
        sectionsTable.setSelectionForeground(new Color(51, 102, 153));
        sectionsTable.setGridColor(new Color(220, 230, 240));
        sectionsTable.setShowGrid(true);
        sectionsTable.setIntercellSpacing(new Dimension(1, 1));

        // 美化：表头样式
        sectionsTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        sectionsTable.getTableHeader().setBackground(new Color(51, 102, 153));
        sectionsTable.getTableHeader().setForeground(Color.WHITE);
        sectionsTable.getTableHeader().setBorder(BorderFactory.createRaisedBevelBorder());

        // 禁用表格拖动功能
        sectionsTable.setDragEnabled(false);
        sectionsTable.getTableHeader().setReorderingAllowed(false);

        // 设置操作列的自定义渲染器和编辑器
        sectionsTable.getColumn("操作").setCellRenderer(new ButtonRenderer());
        sectionsTable.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox(), this));

        // 美化：滚动面板
        JScrollPane scrollPane = new JScrollPane(sectionsTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(51, 102, 153), 2),
                "我的教学班列表",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("微软雅黑", Font.BOLD, 14),
                new Color(51, 102, 153)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createGradesPanel() {
        // 美化：创建带渐变背景的面板
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制淡淡的渐变背景
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(252, 254, 255),
                    0, getHeight(), new Color(248, 250, 252)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.dispose();
            }
        };
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 240, 250), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // 创建工具栏
        JPanel toolbar = createGradesToolbar();
        panel.add(toolbar, BorderLayout.NORTH);

        // 创建学生成绩表格
        studentsTableModel = new DefaultTableModel(STUDENTS_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 修复：分数（第6列）、GPA（第7列）和操作列（第8列）可编辑
                // 列索引：0=学号, 1=姓名, 2=性别, 3=专业, 4=年级, 5=学分, 6=分数, 7=GPA, 8=操作
                return column == 6 || column == 7 || column == STUDENTS_COLUMNS.length - 1;
            }
            
            @Override
            public void setValueAt(Object value, int row, int column) {
                super.setValueAt(value, row, column);
                
                // 当分数列（第6列）发生变化时，自动计算GPA
                if (column == 6 && value != null) {
                    try {
                        double score = Double.parseDouble(value.toString());
                        if (score >= 0 && score <= 100) {
                            double gpa = GPACalculator.calculateGPA(score);
                            super.setValueAt(String.format("%.2f", gpa), row, 7); // 更新GPA列
                        }
                    } catch (NumberFormatException e) {
                        // 忽略无效输入
                    }
                }
            }
        };

        studentsTable = new JTable(studentsTableModel);
        // 美化：表格样式
        studentsTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        studentsTable.setRowHeight(40);
        studentsTable.setSelectionBackground(new Color(230, 244, 255));
        studentsTable.setSelectionForeground(new Color(51, 102, 153));
        studentsTable.setGridColor(new Color(220, 230, 240));
        studentsTable.setShowGrid(true);
        studentsTable.setIntercellSpacing(new Dimension(1, 1));

        // 美化：表头样式
        studentsTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 13));
        studentsTable.getTableHeader().setBackground(new Color(51, 102, 153));
        studentsTable.getTableHeader().setForeground(Color.WHITE);
        studentsTable.getTableHeader().setBorder(BorderFactory.createRaisedBevelBorder());

        // 禁用表格拖动功能
        studentsTable.setDragEnabled(false);
        studentsTable.getTableHeader().setReorderingAllowed(false);

        // 设置操作列的自定义渲染器和编辑器
        studentsTable.getColumn("操作").setCellRenderer(new ButtonRenderer());
        studentsTable.getColumn("操作").setCellEditor(new ButtonEditor(new JCheckBox(), this));

        // 美化：滚动面板
        JScrollPane scrollPane = new JScrollPane(studentsTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(51, 102, 153), 2),
                "学生成绩列表",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("微软雅黑", Font.BOLD, 14),
                new Color(51, 102, 153)
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSectionsToolbar() {
        // 美化：创建带渐变背景的工具栏
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制工具栏渐变背景
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(245, 248, 252),
                    0, getHeight(), new Color(235, 240, 248)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // 绘制顶部高光
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight()/2, 8, 8);

                g2d.dispose();
            }
        };
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 215, 235), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        toolbar.setOpaque(false);

        // 美化：刷新数据按钮
        JButton btnRefresh = createStyledButton("刷新数据", new Color(108, 117, 125), Color.WHITE);
        btnRefresh.addActionListener(e -> loadTeacherSections());

        // 美化：查看评教按钮
        JButton btnViewEvaluation = createStyledButton("查看评教", new Color(40, 167, 69), Color.WHITE);
        btnViewEvaluation.addActionListener(e -> showEvaluationDialog());

        // 美化：视频上传按钮
        JButton btnVideoUpload = createStyledButton("视频上传", new Color(59, 130, 246), Color.WHITE);
        btnVideoUpload.addActionListener(e -> {
            try {
                VideoUpload.showUploadDialog((JFrame) SwingUtilities.getWindowAncestor(this));
            } catch (Exception ex) {
                log.error("打开视频上传对话框失败", ex);
                JOptionPane.showMessageDialog(this, "打开视频上传功能失败: " + ex.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        toolbar.add(btnRefresh);
        toolbar.add(btnViewEvaluation);
        toolbar.add(btnVideoUpload);

        return toolbar;
    }

    private JPanel createGradesToolbar() {
        // 美化：创建带渐变背景的工具栏
        JPanel toolbar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制工具栏渐变背景
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(245, 248, 252),
                    0, getHeight(), new Color(235, 240, 248)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                // 绘制顶部高光
                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight()/2, 8, 8);

                g2d.dispose();
            }
        };
        // 修复：使用BoxLayout防止组件堆叠
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.X_AXIS));
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 215, 235), 1),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        toolbar.setOpaque(false);

        // 美化：教学班选择标签
        JLabel lblSection = new JLabel("📚 选择教学班：");
        lblSection.setFont(new Font("微软雅黑", Font.BOLD, 13));
        lblSection.setForeground(new Color(51, 102, 153));

        // 美化：教学班下拉框
        sectionComboBox = new JComboBox<>();
        sectionComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        sectionComboBox.setPreferredSize(new Dimension(250, 32));
        sectionComboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 190, 200), 1),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)
        ));
        sectionComboBox.setBackground(Color.WHITE);
        sectionComboBox.addActionListener(e -> onSectionSelected());

        // 美化：当前选择标签
        lblCurrentSection = new JLabel("请选择一个教学班");
        lblCurrentSection.setFont(new Font("微软雅黑", Font.BOLD, 12));
        lblCurrentSection.setForeground(new Color(25, 133, 57));

        // 美化按钮样式
        JButton btnRefreshGrades = createStyledButton("刷新成绩", new Color(108, 117, 125), Color.WHITE);
        btnRefreshGrades.addActionListener(e -> {
            if (currentSectionId != -1) {
                loadSectionStudents(currentSectionId);
            } else {
                JOptionPane.showMessageDialog(this, "请先选择一个教学班", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnBatchGrade = createStyledButton("批量评分", new Color(25, 133, 57), Color.WHITE);
        btnBatchGrade.addActionListener(e -> showBatchGradeDialog());

        JButton btnExportGrades = createStyledButton("导出成绩", new Color(70, 130, 180), Color.WHITE);
        btnExportGrades.addActionListener(e -> exportGrades());

        JButton btnViewStats = createStyledButton("成绩统计", new Color(255, 165, 0), Color.WHITE);
        btnViewStats.addActionListener(e -> showGradeStatistics());

        JButton btnAutoCalculateGPA = createStyledButton("自动计算GPA", new Color(40, 167, 69), Color.WHITE);
        btnAutoCalculateGPA.addActionListener(e -> autoCalculateGPA());

        // 添加组件到工具栏
        toolbar.add(lblSection);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(sectionComboBox);
        toolbar.add(Box.createHorizontalStrut(15));
        toolbar.add(lblCurrentSection);
        toolbar.add(Box.createHorizontalGlue()); // 使用Glue占据多余空间
        toolbar.add(btnRefreshGrades);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(btnBatchGrade);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(btnExportGrades);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(btnViewStats);
        toolbar.add(Box.createHorizontalStrut(5));
        toolbar.add(btnAutoCalculateGPA);

        return toolbar;
    }

    /**
     * 创建美化的按钮
     */
    private JButton createStyledButton(String text, Color backgroundColor, Color textColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制纯色按钮背景
                if (getModel().isPressed()) {
                    // 按下时的效果
                    g2d.setColor(backgroundColor.darker());
                } else if (getModel().isRollover()) {
                    // 悬停时的效果
                    g2d.setColor(backgroundColor.brighter());
                } else {
                    // 正常状态
                    g2d.setColor(backgroundColor);
                }

                g2d.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);

                // 绘制边框
                g2d.setColor(backgroundColor.darker());
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);

                g2d.dispose();

                // 绘制文本
                super.paintComponent(g);
            }
        };

        button.setFont(new Font("微软雅黑", Font.BOLD, 12));
        button.setForeground(textColor);
        button.setPreferredSize(new Dimension(120, 32));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 添加鼠标事件
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.repaint();
            }
        });

        return button;
    }

    /**
     * 显示评教对话框
     */
    private void showEvaluationDialog() {
        int selectedRow = sectionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要查看评教的教学班", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 确保sectionId是整数格式
        Object sectionIdObj = sectionsTableModel.getValueAt(selectedRow, 0);
        int sectionId;
        if (sectionIdObj instanceof Double) {
            sectionId = ((Double) sectionIdObj).intValue();
        } else {
            sectionId = (Integer) sectionIdObj;
        }
        String courseName = (String) sectionsTableModel.getValueAt(selectedRow, 1);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "教学评教查看", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // 标题面板
        JPanel titlePanel = new JPanel();
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("教学评教查看 - " + courseName);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titlePanel.add(titleLabel);

        dialog.add(titlePanel, BorderLayout.NORTH);

        // 评教信息面板
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 显示评教统计信息
        JLabel statsLabel = new JLabel("正在加载评教数据...");
        statsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        statsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoPanel.add(statsLabel);

        dialog.add(infoPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("刷新");
        JButton closeButton = new JButton("关闭");

        refreshButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        closeButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        refreshButton.addActionListener(e -> loadEvaluationData(sectionId, statsLabel));
        closeButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // 初始加载数据
        loadEvaluationData(sectionId, statsLabel);

        dialog.setVisible(true);
    }

    /**
     * 加载评教数据
     */
    private void loadEvaluationData(int sectionId, JLabel statsLabel) {
        new Thread(() -> {
            try {
                Request request = new Request("academic/course")
                    .addParam("action", "getEvaluationStats")
                    .addParam("section_Id", String.valueOf(sectionId));

                Response response = nettyClient.sendRequest(request).get();
                
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        Map<String, Object> data = (Map<String, Object>) response.getData();
                        double avgScore = ((Number) data.get("avgScore")).doubleValue();
                        int totalCount = ((Number) data.get("totalCount")).intValue();
                        int evaluatedCount = ((Number) data.get("evaluatedCount")).intValue();

                        StringBuilder statsText = new StringBuilder();
                        statsText.append("<html><div style='text-align: center;'>");
                        statsText.append("<h3>评教统计信息</h3>");
                        statsText.append("<p><b>教学班ID：</b>").append(sectionId).append("</p>");
                        statsText.append("<p><b>总学生数：</b>").append(totalCount).append("人</p>");
                        statsText.append("<p><b>已评教数：</b>").append(evaluatedCount).append("人</p>");
                        statsText.append("<p><b>评教率：</b>").append(String.format("%.1f", (double)evaluatedCount/totalCount*100)).append("%</p>");
                        statsText.append("<p><b>平均评分：</b><span style='color: ").append(avgScore >= 8 ? "green" : avgScore >= 6 ? "orange" : "red").append("; font-size: 18px;'>").append(String.format("%.2f", avgScore)).append("分</span></p>");
                        statsText.append("</div></html>");

                        statsLabel.setText(statsText.toString());
                    } else {
                        statsLabel.setText("加载评教数据失败：" + response.getMessage());
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> 
                    statsLabel.setText("加载评教数据失败：" + e.getMessage())
                );
            }
        }).start();
    }

    // 加载教师的教学班数据
    private void loadTeacherSections() {
        try {
            Request request = new Request("academic/course")
                    .addParam("action", "GET_TEACHER_SECTIONS")
                    .addParam("teacherId", teacherId);

            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> sections = (List<Map<String, Object>>) response.getData();

                        // 清空现有数据
                        sectionsTableModel.setRowCount(0);
                        teacherSections = sections; // 保存教师的所有教学班数据

                        // 更新下拉框数据
                        updateSectionComboBox(sections);

                        // 添加教学班数据到表格
                        for (Map<String, Object> section : sections) {
                            Vector<Object> row = new Vector<>();
                            row.add(section.get("section_Id"));
                            row.add(section.get("courseName"));
                            row.add(section.get("Term"));
                            // 显示教师ID（当前登录的教师）
                            row.add(teacherId);
                            row.add(section.get("Room"));
                            row.add(section.get("Capacity"));
                            // 获取已报名人数
                            Object enrolledCount = section.get("enrolledCount");
                            row.add(enrolledCount != null ? enrolledCount : "0");
                            // 显示上课时间（使用Schedule字段）
                            Object schedule = section.get("Schedule");
                            row.add(schedule != null && !schedule.toString().trim().isEmpty() ?
                                    schedule.toString() : "未安排");
                            row.add("查看学生");
                            sectionsTableModel.addRow(row);
                        }

                        log.info("教师教学班数据加载完成，共 {} 条记录", sections.size());
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "加载教学班数据失败：" + response.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.error("加载教师教学班数据时发生错误", throwable);
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

    // 更新下拉框数据
    private void updateSectionComboBox(List<Map<String, Object>> sections) {
        if (sectionComboBox != null) {
            sectionComboBox.removeAllItems();
            sectionComboBox.addItem(new SectionItem(-1, "请选择教学班"));

            for (Map<String, Object> section : sections) {
                Object sectionIdObj = section.get("section_Id");
                Object courseNameObj = section.get("courseName");

                if (sectionIdObj != null && courseNameObj != null) {
                    try {
                        int sectionId = parseIntSafely(sectionIdObj.toString());
                        String courseName = courseNameObj.toString();
                        sectionComboBox.addItem(new SectionItem(sectionId, courseName));
                    } catch (NumberFormatException e) {
                        log.warn("解析教学班ID失败: {}", sectionIdObj);
                    }
                }
            }
        }
    }

    // 安全解析整数，处理可能包含小数点的字符串
    private int parseIntSafely(String str) {
        str = str.trim();
        if (str.contains(".")) {
            return (int) Double.parseDouble(str);
        } else {
            return Integer.parseInt(str);
        }
    }

    // 下拉框选择事件处理
    private void onSectionSelected() {
        SectionItem selectedItem = (SectionItem) sectionComboBox.getSelectedItem();
        if (selectedItem != null && selectedItem.getSectionId() != -1) {
            currentSectionId = selectedItem.getSectionId();
            currentCourseName = selectedItem.getCourseName();

            // 自动加载学生数据
            loadSectionStudents(currentSectionId);
        } else {
            // 重置状态
            currentSectionId = -1;
            currentCourseName = "";
            studentsTableModel.setRowCount(0);
            lblCurrentSection.setText("请选择一个教学班");
            lblCurrentSection.setForeground(new Color(108, 117, 125));
        }
    }

    // 加载指定教学班的学生数据
    private void loadSectionStudents(int sectionId) {
        try {
            Request request = new Request("academic/course")
                    .addParam("action", "GET_SECTION_STUDENTS")
                    .addParam("sectionId", String.valueOf(sectionId));

            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> students = (List<Map<String, Object>>) response.getData();

                        // 清空现有数据
                        studentsTableModel.setRowCount(0);

                        // 添加学生数据到表格
                        for (Map<String, Object> student : students) {
                            Vector<Object> row = new Vector<>();
                            row.add(student.get("cardNum"));
                            row.add(student.get("studentName"));
                            row.add(student.get("gender"));
                            row.add(student.get("major"));
                            // 获取年级信息
                            Object grade = student.get("grade");
                            row.add(grade != null ? grade : "未知");
                            // 获取学分信息
                            Object credit = student.get("Credit");
                            row.add(credit != null ? credit : "0");
                            row.add(student.get("Score") != null ? student.get("Score") : "");
                            row.add(student.get("GPA") != null ? student.get("GPA") : "");
                            row.add("保存成绩");
                            studentsTableModel.addRow(row);
                        }

                        // 更新成绩管理选项卡的标题，显示当前选择的教学班
                        updateGradesTabTitle();

                        // 切换到成绩管理选项卡
                        tabbedPane.setSelectedIndex(1);

                        log.info("教学班学生数据加载完成，共 {} 条记录", students.size());
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "加载学生数据失败：" + response.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.error("加载学生数据时发生错误", throwable);
                    JOptionPane.showMessageDialog(this,
                            "加载学生数据时发生错误：" + throwable.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
        } catch (Exception e) {
            log.error("发送学生数据请求时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "发送学生数据请求时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // 更新成绩管理选项卡的标题信息
    private void updateGradesTabTitle() {
        lblCurrentSection.setText(currentCourseName + " (教学班ID: " + currentSectionId + ")");
        lblCurrentSection.setForeground(new Color(25, 133, 57));
    }

    // 更新单个学生成绩
    private void updateStudentScore(String cardNum, double score, double gpa) {
        try {
            Request request = new Request("academic/course")
                    .addParam("action", "UPDATE_STUDENT_SCORE")
                    .addParam("cardNum", cardNum)
                    .addParam("sectionId", String.valueOf(currentSectionId))
                    .addParam("score", String.valueOf(score))
                    .addParam("gpa", String.valueOf(gpa));

            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(this,
                                "成绩更新成功！",
                                "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadSectionStudents(currentSectionId); // 刷新数据
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "成绩更新失败：" + response.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.error("更新学生成绩时发生错误", throwable);
                    JOptionPane.showMessageDialog(this,
                            "更新学生成绩时发生错误：" + throwable.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
        } catch (Exception e) {
            log.error("发送成绩更新请求时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "发送成绩更新请求时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // 显示批量评分对话框
    private void showBatchGradeDialog() {
        if (currentSectionId == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一个教学班", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BatchGradeDialog dialog = new BatchGradeDialog(
                SwingUtilities.getWindowAncestor(this),
                currentCourseName,
                studentsTableModel,
                nettyClient,
                currentSectionId
        );
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            // 刷新数据以显示最新的成绩
            loadSectionStudents(currentSectionId);
        }
    }

    // 新增：导出成绩功能
    private void exportGrades() {
        if (currentSectionId == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一个教学班", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (studentsTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "当前教学班没有学生数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            // 创建文件选择器
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("导出成绩数据");
            fileChooser.setSelectedFile(new java.io.File(currentCourseName + "_成绩表.txt"));

            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();

                // 导出为文本格式
                StringBuilder content = new StringBuilder();
                content.append("课程：").append(currentCourseName).append("\n");
                content.append("教学班ID：").append(currentSectionId).append("\n");
                content.append("导出时间：").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())).append("\n");
                content.append("教师：").append(userData.get("userName")).append("\n\n");

                // 表头
                content.append("学号\t姓名\t性别\t专业\t年级\t分数\tGPA\n");
                content.append("------------------------------------------------\n");

                // 数据行
                for (int i = 0; i < studentsTableModel.getRowCount(); i++) {
                    for (int j = 0; j < studentsTableModel.getColumnCount() - 1; j++) { // 排除操作列
                        Object value = studentsTableModel.getValueAt(i, j);
                        content.append(value != null ? value.toString() : "").append("\t");
                    }
                    content.append("\n");
                }

                // 写入文件
                try (java.io.FileWriter writer = new java.io.FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
                    writer.write(content.toString());
                }

                JOptionPane.showMessageDialog(this,
                    "成绩数据已成功导出到：\n" + file.getAbsolutePath(),
                    "导出成功",
                    JOptionPane.INFORMATION_MESSAGE);

                log.info("成绩数据导出成功：{}", file.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("导出成绩数据时发生错误", e);
            JOptionPane.showMessageDialog(this,
                "导出失败：" + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // 新增：显示成绩统计
    private void showGradeStatistics() {
        if (currentSectionId == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一个教学班", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (studentsTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "当前教学班没有学生数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 计算统计数据
        int totalStudents = studentsTableModel.getRowCount();
        int gradedStudents = 0;
        int excellentCount = 0; // 90分以上
        int goodCount = 0;      // 80-89分
        int passCount = 0;      // 60-79分
        int failCount = 0;      // 60分以下
        double maxScore = 0;
        double minScore = 100;

        // 使用GPA计算器进行统计（加权平均）
        GPACalculator.GradeStatistics statistics = new GPACalculator.GradeStatistics();

        for (int i = 0; i < totalStudents; i++) {
            Object scoreObj = studentsTableModel.getValueAt(i, 6); // 分数列
            Object creditObj = studentsTableModel.getValueAt(i, 5); // 学分列

            if (scoreObj != null && !scoreObj.toString().trim().isEmpty()) {
                try {
                    double score = Double.parseDouble(scoreObj.toString());
                    int credit = creditObj != null ? Integer.parseInt(creditObj.toString()) : 1;
                    
                    if (score >= 0 && score <= 100 && credit > 0) {
                        gradedStudents++;
                        statistics.addScoreWithCredit(score, credit);

                        maxScore = Math.max(maxScore, score);
                        minScore = Math.min(minScore, score);

                        if (score >= 90) excellentCount++;
                        else if (score >= 80) goodCount++;
                        else if (score >= 60) passCount++;
                        else failCount++;
                    }
                } catch (NumberFormatException e) {
                    // 忽略无效数据
                }
            }
        }

        // 创建统计信息对话框 - 修复构造函数参数类型问题
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog statsDialog = new JDialog(parentWindow, "成绩统计 - " + currentCourseName, Dialog.ModalityType.APPLICATION_MODAL);

        statsDialog.setSize(500, 400);
        statsDialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 统计信息文本
        StringBuilder stats = new StringBuilder();
        stats.append("课程：").append(currentCourseName).append("\n");
        stats.append("教学班ID：").append(currentSectionId).append("\n\n");

        stats.append("=== 基本统计 ===\n");
        stats.append("总学生数：").append(totalStudents).append(" 人\n");
        stats.append("已评分数：").append(gradedStudents).append(" 人\n");
        stats.append("未评分数：").append(totalStudents - gradedStudents).append(" 人\n\n");

        if (gradedStudents > 0) {
            // 使用GPA计算器的统计结果（加权平均）
            double avgScore = statistics.getAvgScore();
            double avgGPA = statistics.getAvgGPA();
            int totalCredits = statistics.getTotalCredits();

            stats.append("=== 成绩分析 ===\n");
            stats.append(String.format("总学分：%d 学分\n", totalCredits));
            stats.append(String.format("加权平均分：%.4f 分\n", avgScore));
            stats.append(String.format("加权平均GPA：%.4f\n", avgGPA));
            stats.append(String.format("最高分：%.1f 分\n", maxScore));
            stats.append(String.format("最低分：%.1f 分\n", gradedStudents > 0 ? minScore : 0));
            stats.append(String.format("分数范围：%.1f 分\n\n", maxScore - (gradedStudents > 0 ? minScore : 0)));

            stats.append("=== 等级分布 ===\n");
            stats.append(String.format("优秀(90-100分)：%d 人 (%.1f%%)\n", excellentCount, (double)excellentCount / gradedStudents * 100));
            stats.append(String.format("良好(80-89分)：%d 人 (%.1f%%)\n", goodCount, (double)goodCount / gradedStudents * 100));
            stats.append(String.format("及格(60-79分)：%d 人 (%.1f%%)\n", passCount, (double)passCount / gradedStudents * 100));
            stats.append(String.format("不及格(<60分)：%d 人 (%.1f%%)\n", failCount, (double)failCount / gradedStudents * 100));

            double passRate = (double)(excellentCount + goodCount + passCount) / gradedStudents * 100;
            stats.append(String.format("\n及格率：%.1f%%\n", passRate));
        } else {
            stats.append("暂无成绩数据可供分析\n");
        }

        JTextArea textArea = new JTextArea(stats.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        textArea.setBackground(getBackground());

        JScrollPane scrollPane = new JScrollPane(textArea);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> statsDialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(closeButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        statsDialog.add(contentPanel);
        statsDialog.setVisible(true);
    }

    // 新增：自动计算GPA方法
    private void autoCalculateGPA() {
        if (currentSectionId == -1) {
            JOptionPane.showMessageDialog(this, "请先选择一个教学班", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (studentsTableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "当前教学班没有学生数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int updatedCount = 0;
        for (int i = 0; i < studentsTableModel.getRowCount(); i++) {
            Object scoreObj = studentsTableModel.getValueAt(i, 6); // 分数列
            if (scoreObj != null && !scoreObj.toString().trim().isEmpty()) {
                try {
                    double score = Double.parseDouble(scoreObj.toString());
                    if (score >= 0 && score <= 100) {
                        double gpa = GPACalculator.calculateGPA(score);
                        studentsTableModel.setValueAt(String.format("%.2f", gpa), i, 7); // 更新GPA列
                        updatedCount++;
                    }
                } catch (NumberFormatException e) {
                    // 忽略无效数据
                }
            }
        }

        JOptionPane.showMessageDialog(this, 
            String.format("已为 %d 名学生自动计算GPA", updatedCount), 
            "自动计算完成", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    // 显示学生信息对话框
    private void showStudentInfoDialog(int sectionId, String courseName) {
        try {
            // 修复：使用IdUtils确保sectionId为整数格式
            String sectionIdStr = String.valueOf(IdUtils.parseId(sectionId));

            Request request = new Request("academic/course")
                    .addParam("action", "GET_SECTION_STUDENTS")
                    .addParam("sectionId", sectionIdStr);

            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> students = (List<Map<String, Object>>) response.getData();

                        // 创建学生信息对话框
                        createStudentInfoDialog(students, courseName, sectionId);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "加载学生数据失败：" + response.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.error("加载学生数据时发生错误", throwable);
                    JOptionPane.showMessageDialog(this,
                            "加载学生数据时发生错误：" + throwable.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
        } catch (Exception e) {
            log.error("发送学生数据请求时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "发送学生数据请求时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // 创建学生信息对话框
    private void createStudentInfoDialog(List<Map<String, Object>> students, String courseName, int sectionId) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "学生信息 - " + courseName, Dialog.ModalityType.APPLICATION_MODAL);

        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 创建标题面板
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("课程：" + courseName + "  教学班ID：" + sectionId);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        titleLabel.setForeground(new Color(25, 133, 57));
        titlePanel.add(titleLabel);

        JLabel countLabel = new JLabel("共 " + students.size() + " 名学生");
        countLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        countLabel.setForeground(new Color(70, 130, 180));
        titlePanel.add(Box.createHorizontalStrut(20));
        titlePanel.add(countLabel);

        contentPanel.add(titlePanel, BorderLayout.NORTH);

        // 创建学生信息表格
        String[] columnNames = {"学号", "姓名", "性别", "专业", "年级", "联系方式", "邮箱"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 只读表格
            }
        };

        // 填充学生数据
        for (Map<String, Object> student : students) {
            Vector<Object> row = new Vector<>();
            row.add(student.get("cardNum"));
            row.add(student.get("studentName"));
            row.add(student.get("gender"));
            row.add(student.get("major"));
            row.add(student.get("grade") != null ? student.get("grade") : "未知");
            row.add(student.get("phoneNumber") != null ? student.get("phoneNumber") : "未填写");
            row.add(student.get("email") != null ? student.get("email") : "未填写");
            tableModel.addRow(row);
        }

        JTable table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(240, 240, 240));

        // 禁用表格拖动功能
        table.setDragEnabled(false);
        table.getTableHeader().setReorderingAllowed(false);

        // 设置列宽
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // 学号
        table.getColumnModel().getColumn(1).setPreferredWidth(80);  // 姓名
        table.getColumnModel().getColumn(2).setPreferredWidth(50);  // 性别
        table.getColumnModel().getColumn(3).setPreferredWidth(120); // 专业
        table.getColumnModel().getColumn(4).setPreferredWidth(60);  // 年级
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // 联系方式
        table.getColumnModel().getColumn(6).setPreferredWidth(150); // 邮箱

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("学生列表"));
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // 创建底部按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton exportButton = new JButton("导出学生名单");
        exportButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        exportButton.setBackground(new Color(70, 130, 180));
        exportButton.setForeground(Color.WHITE);
        exportButton.addActionListener(e -> exportStudentList(students, courseName, sectionId));

        JButton manageGradesButton = new JButton("进入成绩管理");
        manageGradesButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        manageGradesButton.setBackground(new Color(25, 133, 57));
        manageGradesButton.setForeground(Color.WHITE);
        manageGradesButton.addActionListener(e -> {
            dialog.dispose();
            // 切换到成绩管理并加载学生数据
            currentSectionId = sectionId;
            currentCourseName = courseName;
            loadSectionStudents(sectionId);
        });

        JButton closeButton = new JButton("关闭");
        closeButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        closeButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(exportButton);
        buttonPanel.add(manageGradesButton);
        buttonPanel.add(closeButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(contentPanel);
        dialog.setVisible(true);
    }

    // 导出学生名单
    private void exportStudentList(List<Map<String, Object>> students, String courseName, int sectionId) {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("导出学生名单");
            fileChooser.setSelectedFile(new java.io.File(courseName + "_学生名单.txt"));

            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();

                StringBuilder content = new StringBuilder();
                content.append("课程：").append(courseName).append("\n");
                content.append("教学班ID：").append(sectionId).append("\n");
                content.append("导出时间：").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())).append("\n");
                content.append("教师：").append(userData.get("userName")).append("\n");
                content.append("学生总数：").append(students.size()).append(" 人\n\n");

                content.append("学号\t姓名\t性别\t专业\t年级\t联系方式\t邮箱\n");
                content.append("------------------------------------------------------------------------\n");

                for (Map<String, Object> student : students) {
                    content.append(student.get("cardNum")).append("\t");
                    content.append(student.get("studentName")).append("\t");
                    content.append(student.get("gender")).append("\t");
                    content.append(student.get("major")).append("\t");
                    content.append(student.get("grade") != null ? student.get("grade") : "未知").append("\t");
                    content.append(student.get("phoneNumber") != null ? student.get("phoneNumber") : "未填写").append("\t");
                    content.append(student.get("email") != null ? student.get("email") : "未填写").append("\n");
                }

                try (java.io.FileWriter writer = new java.io.FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
                    writer.write(content.toString());
                }

                JOptionPane.showMessageDialog(this,
                    "学生名单已成功导出到：\n" + file.getAbsolutePath(),
                    "导出成功",
                    JOptionPane.INFORMATION_MESSAGE);

                log.info("学生名单导出成功：{}", file.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("导出学生名单时发生错误", e);
            JOptionPane.showMessageDialog(this,
                "导出失败：" + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // 教学班下拉框项目类
    private static class SectionItem {
        private final int sectionId;
        private final String courseName;
        private final String displayText;

        public SectionItem(int sectionId, String courseName) {
            this.sectionId = sectionId;
            this.courseName = courseName;
            this.displayText = courseName + " (ID: " + sectionId + ")";
        }

        public int getSectionId() {
            return sectionId;
        }

        public String getCourseName() {
            return courseName;
        }

        @Override
        public String toString() {
            return displayText;
        }
    }

    // 自定义按钮渲染器
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            setBackground(new Color(70, 130, 180));
            setForeground(Color.WHITE);
            setFont(new Font("微软雅黑", Font.PLAIN, 11));
            return this;
        }
    }

    // 自定义按钮编辑器
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;
        private final TeacherCoursePanel parent;

        public ButtonEditor(JCheckBox checkBox, TeacherCoursePanel parent) {
            super(checkBox);
            this.parent = parent;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentRow = row;
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            isPushed = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (isPushed) {
                if (parent.sectionsTable != null && parent.sectionsTable.isEditing()) {
                    Object sectionIdObj = parent.sectionsTableModel.getValueAt(currentRow, 0);
                    Object courseNameObj = parent.sectionsTableModel.getValueAt(currentRow, 1);

                    if (sectionIdObj != null && courseNameObj != null) {
                        try {
                            // 安全地解析教学班ID，处理可能包含小数点的情况
                            String sectionIdStr = sectionIdObj.toString().trim();
                            int sectionId;
                            if (sectionIdStr.contains(".")) {
                                sectionId = (int) Double.parseDouble(sectionIdStr);
                            } else {
                                sectionId = Integer.parseInt(sectionIdStr);
                            }
                            String courseName = courseNameObj.toString();

                            // 显示学生信息对话框，而不是直接跳转到成绩管理
                            parent.showStudentInfoDialog(sectionId, courseName);
                        } catch (NumberFormatException e) {
                            log.error("解析教学班ID失败: {}", sectionIdObj, e);
                            JOptionPane.showMessageDialog(button.getParent(),
                                "教学班ID格式错误: " + sectionIdObj,
                                "数据错误",
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else if (parent.studentsTable != null && parent.studentsTable.isEditing()) {
                    try {
                        // 修复列索引：6=分数列，7=GPA列，0=学号列
                        Object scoreObj = parent.studentsTableModel.getValueAt(currentRow, 6);
                        Object gpaObj = parent.studentsTableModel.getValueAt(currentRow, 7);
                        Object cardNumObj = parent.studentsTableModel.getValueAt(currentRow, 0);

                        if (scoreObj != null && gpaObj != null && cardNumObj != null &&
                            !scoreObj.toString().trim().isEmpty() &&
                            !gpaObj.toString().trim().isEmpty()) {

                            double score = Double.parseDouble(scoreObj.toString());
                            double gpa = Double.parseDouble(gpaObj.toString());
                            String cardNum = cardNumObj.toString();

                            if (score < 0 || score > 100) {
                                JOptionPane.showMessageDialog(button.getParent(),
                                    "分数必须在0-100之间", "输入错误", JOptionPane.ERROR_MESSAGE);
                                return label;
                            }
                            if (gpa < 0 || gpa > 4.8) {
                                JOptionPane.showMessageDialog(button.getParent(),
                                    "GPA必须在0-4.8之间", "输入错误", JOptionPane.ERROR_MESSAGE);
                                return label;
                            }

                            parent.updateStudentScore(cardNum, score, gpa);
                        } else {
                            JOptionPane.showMessageDialog(button.getParent(),
                                "请先输入分数和GPA", "提示", JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(button.getParent(),
                            "分数和GPA必须是数字", "输入错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            isPushed = false;
            return label;
        }

        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}

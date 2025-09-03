package com.vcampus.client.core.ui.academic;

import com.vcampus.client.core.net.NettyClient;
import java.util.HashMap;
import java.util.Map;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 教务管理界面 - 学生和教师信息管理
 * 支持增删改查功能
 */
@Slf4j
public class StatusPanel extends JFrame {

    private NettyClient nettyClient;
    private Map<String, Object> userData;

    private JTabbedPane tabbedPane;
    private JTable studentTable;
    private JTable staffTable;
    private DefaultTableModel studentTableModel;
    private DefaultTableModel staffTableModel;

    // 学生信息表格列名
    private static final String[] STUDENT_COLUMNS = {
        "卡号", "学号", "姓名", "年龄", "性别", "年级", "专业", "学院", "电话", "操作"
    };

    // 教师信息表格列名
    private static final String[] STAFF_COLUMNS = {
        "卡号", "工号", "姓名", "年龄", "性别", "职称", "学院", "电话", "操作"
    };

    public StatusPanel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;

        initUI();
        loadData();

        log.info("教务管理界面初始化完成");
    }

    private void initUI() {
        setTitle("VCampus - 教务管理系统");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/figures/logo.png"));
            setIconImage(logoIcon.getImage());
        } catch (Exception e) {
            log.warn("图标加载失败");
        }

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 创建选项卡面板
        createTabbedPane();
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        JLabel titleLabel = new JLabel("教务管理系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 133, 57));

        JLabel subtitleLabel = new JLabel("学生和教师信息管理");
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitleLabel);

        return panel;
    }

    private void createTabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        // 学生管理选项卡
        JPanel studentPanel = createStudentPanel();
        tabbedPane.addTab("学生管理", studentPanel);

        // 教师管理选项卡
        JPanel staffPanel = createStaffPanel();
        tabbedPane.addTab("教师管理", staffPanel);
    }

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建工具栏
        JPanel toolbar = createStudentToolbar();
        panel.add(toolbar, BorderLayout.NORTH);

        // 创建学生表格
        studentTableModel = new DefaultTableModel(STUDENT_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == STUDENT_COLUMNS.length - 1; // 只有操作列可编辑
            }
        };

        studentTable = new JTable(studentTableModel);
        studentTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        studentTable.setRowHeight(30);
        studentTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        studentTable.getTableHeader().setBackground(new Color(240, 240, 240));

        // 设置操作列的渲染器和编辑器
        setupStudentTableButtons();

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("学生信息列表"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建工具栏
        JPanel toolbar = createStaffToolbar();
        panel.add(toolbar, BorderLayout.NORTH);

        // 创建教师表格
        staffTableModel = new DefaultTableModel(STAFF_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == STAFF_COLUMNS.length - 1; // 只有操作列可编辑
            }
        };

        staffTable = new JTable(staffTableModel);
        staffTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        staffTable.setRowHeight(30);
        staffTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        staffTable.getTableHeader().setBackground(new Color(240, 240, 240));

        // 设置操作列的渲染器和编辑器
        setupStaffTableButtons();

        JScrollPane scrollPane = new JScrollPane(staffTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("教师信息列表"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStudentToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JButton btnAdd = new JButton("添加学生");
        JButton btnRefresh = new JButton("刷新数据");

        btnAdd.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        btnAdd.setBackground(new Color(25, 133, 57));
        btnAdd.setForeground(Color.WHITE);
        btnRefresh.setBackground(new Color(108, 117, 125));
        btnRefresh.setForeground(Color.WHITE);

        btnAdd.addActionListener(e -> showAddStudentDialog());
        btnRefresh.addActionListener(e -> loadStudentData());

        // 添加查询功能
        JLabel searchLabel = new JLabel("查询卡号:");
        searchLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        JTextField studentSearchField = new JTextField(15);
        studentSearchField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        studentSearchField.setToolTipText("输入卡号进行查询");

        JButton btnSearch = new JButton("查询");
        btnSearch.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnSearch.setBackground(new Color(255, 193, 7));
        btnSearch.setForeground(Color.BLACK);

        btnSearch.addActionListener(e -> searchStudentByCardNum(studentSearchField.getText()));

        // 回车键查询
        studentSearchField.addActionListener(e -> searchStudentByCardNum(studentSearchField.getText()));

        toolbar.add(btnAdd);
        toolbar.add(btnRefresh);

        // 添加分隔符
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(searchLabel);
        toolbar.add(studentSearchField);
        toolbar.add(btnSearch);

        return toolbar;
    }

    private JPanel createStaffToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JButton btnAdd = new JButton("添加教师");
        JButton btnRefresh = new JButton("刷新数据");

        btnAdd.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        btnAdd.setBackground(new Color(25, 133, 57));
        btnAdd.setForeground(Color.WHITE);
        btnRefresh.setBackground(new Color(108, 117, 125));
        btnRefresh.setForeground(Color.WHITE);

        btnAdd.addActionListener(e -> showAddStaffDialog());
        btnRefresh.addActionListener(e -> loadStaffData());

        // 添加查询功能
        JLabel searchLabel = new JLabel("查询卡号:");
        searchLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        JTextField staffSearchField = new JTextField(15);
        staffSearchField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        staffSearchField.setToolTipText("输入卡号进行查询");

        JButton btnSearch = new JButton("查询");
        btnSearch.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnSearch.setBackground(new Color(255, 193, 7));
        btnSearch.setForeground(Color.BLACK);

        btnSearch.addActionListener(e -> searchStaffByCardNum(staffSearchField.getText()));

        // 回车键查询
        staffSearchField.addActionListener(e -> searchStaffByCardNum(staffSearchField.getText()));

        toolbar.add(btnAdd);
        toolbar.add(btnRefresh);

        // 添加分隔符
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(searchLabel);
        toolbar.add(staffSearchField);
        toolbar.add(btnSearch);

        return toolbar;
    }

    private void setupStudentTableButtons() {
        // 设置操作列的宽度
        studentTable.getColumn("操作").setPreferredWidth(120);
        studentTable.getColumn("操作").setMinWidth(120);
        studentTable.getColumn("操作").setMaxWidth(150);

        // 操作列按钮渲染器
        studentTable.getColumn("操作").setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));

                JButton editBtn = new JButton("编辑");
                JButton deleteBtn = new JButton("删除");

                editBtn.setFont(new Font("微软雅黑", Font.PLAIN, 9));
                deleteBtn.setFont(new Font("微软雅黑", Font.PLAIN, 9));

                editBtn.setPreferredSize(new Dimension(50, 25));
                deleteBtn.setPreferredSize(new Dimension(50, 25));

                editBtn.setBackground(new Color(0, 123, 255));
                editBtn.setForeground(Color.WHITE);
                editBtn.setBorderPainted(false);
                deleteBtn.setBackground(new Color(220, 53, 69));
                deleteBtn.setForeground(Color.WHITE);
                deleteBtn.setBorderPainted(false);

                panel.add(editBtn);
                panel.add(deleteBtn);

                return panel;
            }
        });

        // 操作列按钮编辑器
        studentTable.getColumn("操作").setCellEditor(new TableCellEditor() {
            private JPanel panel;
            private JButton editBtn;
            private JButton deleteBtn;
            private int currentRow;

            {
                panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
                editBtn = new JButton("编辑");
                deleteBtn = new JButton("删除");

                editBtn.setFont(new Font("微软雅黑", Font.PLAIN, 9));
                deleteBtn.setFont(new Font("微软雅黑", Font.PLAIN, 9));

                editBtn.setPreferredSize(new Dimension(50, 25));
                deleteBtn.setPreferredSize(new Dimension(50, 25));

                editBtn.setBackground(new Color(0, 123, 255));
                editBtn.setForeground(Color.WHITE);
                editBtn.setBorderPainted(false);
                deleteBtn.setBackground(new Color(220, 53, 69));
                deleteBtn.setForeground(Color.WHITE);
                deleteBtn.setBorderPainted(false);

                editBtn.addActionListener(e -> {
                    editStudent(currentRow);
                    fireEditingStopped();
                });

                deleteBtn.addActionListener(e -> {
                    deleteStudent(currentRow);
                    fireEditingStopped();
                });

                panel.add(editBtn);
                panel.add(deleteBtn);
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                    boolean isSelected, int row, int column) {
                currentRow = row;
                return panel;
            }

            @Override
            public Object getCellEditorValue() { return null; }
            @Override
            public boolean isCellEditable(java.util.EventObject anEvent) { return true; }
            @Override
            public boolean shouldSelectCell(java.util.EventObject anEvent) { return true; }
            @Override
            public boolean stopCellEditing() { return true; }
            @Override
            public void cancelCellEditing() {}
            @Override
            public void addCellEditorListener(javax.swing.event.CellEditorListener l) {}
            @Override
            public void removeCellEditorListener(javax.swing.event.CellEditorListener l) {}

            private void fireEditingStopped() {
                studentTable.editingStopped(new javax.swing.event.ChangeEvent(this));
            }
        });
    }

    private void setupStaffTableButtons() {
        // 设置操作列的宽度
        staffTable.getColumn("操作").setPreferredWidth(120);
        staffTable.getColumn("操作").setMinWidth(120);
        staffTable.getColumn("操作").setMaxWidth(150);

        // 操作列按钮渲染器
        staffTable.getColumn("操作").setCellRenderer(new TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));

                JButton editBtn = new JButton("编辑");
                JButton deleteBtn = new JButton("删除");

                editBtn.setFont(new Font("微软雅黑", Font.PLAIN, 9));
                deleteBtn.setFont(new Font("微软雅黑", Font.PLAIN, 9));

                editBtn.setPreferredSize(new Dimension(50, 25));
                deleteBtn.setPreferredSize(new Dimension(50, 25));

                editBtn.setBackground(new Color(0, 123, 255));
                editBtn.setForeground(Color.WHITE);
                editBtn.setBorderPainted(false);
                deleteBtn.setBackground(new Color(220, 53, 69));
                deleteBtn.setForeground(Color.WHITE);
                deleteBtn.setBorderPainted(false);

                panel.add(editBtn);
                panel.add(deleteBtn);

                return panel;
            }
        });

        // 操作列按钮编辑器
        staffTable.getColumn("操作").setCellEditor(new TableCellEditor() {
            private JPanel panel;
            private JButton editBtn;
            private JButton deleteBtn;
            private int currentRow;

            {
                panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
                editBtn = new JButton("编辑");
                deleteBtn = new JButton("删除");

                editBtn.setFont(new Font("微软雅黑", Font.PLAIN, 9));
                deleteBtn.setFont(new Font("微软雅黑", Font.PLAIN, 9));

                editBtn.setPreferredSize(new Dimension(50, 25));
                deleteBtn.setPreferredSize(new Dimension(50, 25));

                editBtn.setBackground(new Color(0, 123, 255));
                editBtn.setForeground(Color.WHITE);
                editBtn.setBorderPainted(false);
                deleteBtn.setBackground(new Color(220, 53, 69));
                deleteBtn.setForeground(Color.WHITE);
                deleteBtn.setBorderPainted(false);

                editBtn.addActionListener(e -> {
                    editStaff(currentRow);
                    fireEditingStopped();
                });

                deleteBtn.addActionListener(e -> {
                    deleteStaff(currentRow);
                    fireEditingStopped();
                });

                panel.add(editBtn);
                panel.add(deleteBtn);
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                    boolean isSelected, int row, int column) {
                currentRow = row;
                return panel;
            }

            @Override
            public Object getCellEditorValue() { return null; }
            @Override
            public boolean isCellEditable(java.util.EventObject anEvent) { return true; }
            @Override
            public boolean shouldSelectCell(java.util.EventObject anEvent) { return true; }
            @Override
            public boolean stopCellEditing() { return true; }
            @Override
            public void cancelCellEditing() {}
            @Override
            public void addCellEditorListener(javax.swing.event.CellEditorListener l) {}
            @Override
            public void removeCellEditorListener(javax.swing.event.CellEditorListener l) {}

            private void fireEditingStopped() {
                staffTable.editingStopped(new javax.swing.event.ChangeEvent(this));
            }
        });
    }

    private void loadData() {
        loadStudentData();
        loadStaffData();
    }

    private void loadStudentData() {
        try {
            // 发送请求获取学生数据，包含关联用户信息
            Request request = new Request("ACADEMIC");
            request.addParam("action", "GET_ALL_STUDENTS_WITH_USER_INFO");
            // 添加Session信息以通过权限验证
            request.setSession(nettyClient.getCurrentSession());
            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> studentDataList = (List<Map<String, Object>>) response.getData();

                        // 清空现有数据
                        studentTableModel.setRowCount(0);

                        // 添加学生数据到表格
                        for (Map<String, Object> studentData : studentDataList) {
                            Vector<Object> row = new Vector<>();

                            // 从tbstudent表获取的数据
                            row.add(studentData.get("cardNum"));  // 卡号
                            row.add(studentData.get("studentId")); // 学号

                            // 从tbluser表获取的数据（通过卡号关联）
                            row.add(studentData.get("name") != null ? studentData.get("name") : ""); // 姓名

                            // 处理年龄，确保显示为整数
                            Object ageObj = studentData.get("age");
                            if (ageObj != null) {
                                if (ageObj instanceof Number) {
                                    row.add(((Number) ageObj).intValue()); // 转换为整数
                                } else {
                                    row.add(ageObj.toString());
                                }
                            } else {
                                row.add("");
                            }

                            row.add(studentData.get("gender") != null ? studentData.get("gender") : ""); // 性别

                            // 处理年级，确保显示为整数
                            Object gradeObj = studentData.get("grade");
                            if (gradeObj != null) {
                                if (gradeObj instanceof Number) {
                                    row.add(((Number) gradeObj).intValue()); // 转换为整数
                                } else {
                                    row.add(gradeObj.toString());
                                }
                            } else {
                                row.add("");
                            }

                            row.add(studentData.get("major") != null ? studentData.get("major") : ""); // 专业
                            row.add(studentData.get("department") != null ? studentData.get("department") : ""); // 学院

                            // 从tbluser表获取的联系方式
                            row.add(studentData.get("phone") != null ? studentData.get("phone") : ""); // 电话

                            row.add("操作"); // 操作列

                            studentTableModel.addRow(row);
                        }

                        log.info("学生数据加载完成，共 {} 条记录", studentDataList.size());
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

    private void loadStaffData() {
        try {
            // 发送请求获取教师数据，包含关联用户信息
            Request request = new Request("ACADEMIC");
            request.addParam("action", "GET_ALL_STAFF_WITH_USER_INFO");
            // 添加Session信息以通过权限验证
            request.setSession(nettyClient.getCurrentSession());
            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> staffDataList = (List<Map<String, Object>>) response.getData();

                        // 清空现有数据
                        staffTableModel.setRowCount(0);

                        // 添加教师数据到表格
                        for (Map<String, Object> staffData : staffDataList) {
                            Vector<Object> row = new Vector<>();

                            // 从tblstaff表获取的数据
                            row.add(staffData.get("cardNum"));  // 卡号
                            row.add(staffData.get("staffId")); // 工号

                            // 从tbluser表获取的数据（通过卡号关联）
                            row.add(staffData.get("name") != null ? staffData.get("name") : ""); // 姓名

                            // 处理年龄，确保显示为整数
                            Object ageObj = staffData.get("age");
                            if (ageObj != null) {
                                if (ageObj instanceof Number) {
                                    row.add(((Number) ageObj).intValue()); // 转换为整数
                                } else {
                                    row.add(ageObj.toString());
                                }
                            } else {
                                row.add("");
                            }

                            row.add(staffData.get("gender") != null ? staffData.get("gender") : ""); // 性别

                            // 从tblstaff表获取的职位信息
                            row.add(staffData.get("title") != null ? staffData.get("title") : ""); // 职称
                            row.add(staffData.get("department") != null ? staffData.get("department") : ""); // 学院

                            // 从tbluser表获取的联系方式
                            row.add(staffData.get("phone") != null ? staffData.get("phone") : ""); // 电话

                            row.add("操作"); // 操作列

                            staffTableModel.addRow(row);
                        }

                        log.info("教师数据加载完成，共 {} 条记录", staffDataList.size());
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "加载教师数据失败：" + response.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.error("加载教师数据时发生错误", throwable);
                    JOptionPane.showMessageDialog(this,
                            "加载教师数据时发生错误：" + throwable.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
        } catch (Exception e) {
            log.error("发送教师数据请求时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "发送教师数据请求时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // 学生相关操作方法
    private void showAddStudentDialog() {
        StudentEditDialog dialog = new StudentEditDialog(this, "添加学生", null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Map<String, Object> studentData = dialog.getStudentData();
            addStudent(studentData);
        }
    }

    private void editStudent(int row) {
        if (row >= 0 && row < studentTableModel.getRowCount()) {
            try {
                // 从表格获取学生信息，处理类型转换
                Map<String, Object> studentData = new HashMap<>();
                studentData.put("cardNum", studentTableModel.getValueAt(row, 0));
                studentData.put("studentId", studentTableModel.getValueAt(row, 1));
                studentData.put("name", studentTableModel.getValueAt(row, 2));

                // 处理年龄的类型转换
                Object ageObj = studentTableModel.getValueAt(row, 3);
                if (ageObj != null && !ageObj.toString().isEmpty()) {
                    try {
                        studentData.put("age", ageObj instanceof Integer ? (Integer) ageObj : Integer.parseInt(ageObj.toString()));
                    } catch (NumberFormatException e) {
                        studentData.put("age", 20); // 默认值
                    }
                } else {
                    studentData.put("age", 20); // 默认值
                }

                studentData.put("gender", studentTableModel.getValueAt(row, 4));

                // 处理年级的类型转换
                Object gradeObj = studentTableModel.getValueAt(row, 5);
                if (gradeObj != null && !gradeObj.toString().isEmpty()) {
                    try {
                        studentData.put("grade", gradeObj instanceof Integer ? (Integer) gradeObj : Integer.parseInt(gradeObj.toString()));
                    } catch (NumberFormatException e) {
                        studentData.put("grade", 1); // 默认值
                    }
                } else {
                    studentData.put("grade", 1); // 默认值
                }

                studentData.put("major", studentTableModel.getValueAt(row, 6));
                studentData.put("department", studentTableModel.getValueAt(row, 7));
                studentData.put("phone", studentTableModel.getValueAt(row, 8));

                StudentEditDialog dialog = new StudentEditDialog(this, "编辑学生", studentData);
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    Map<String, Object> updatedStudentData = dialog.getStudentData();
                    updateStudent(updatedStudentData);
                }
            } catch (Exception e) {
                log.error("编辑学生信息时发生错误", e);
                JOptionPane.showMessageDialog(this,
                        "编辑学生信息时发生错误：" + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteStudent(int row) {
        if (row >= 0 && row < studentTableModel.getRowCount()) {
            String cardNum = (String) studentTableModel.getValueAt(row, 0);
            String name = (String) studentTableModel.getValueAt(row, 2);

            int result = JOptionPane.showConfirmDialog(this,
                    "确定要删除学生 " + name + "（卡号：" + cardNum + "）的信息吗？",
                    "确认删除",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                try {
                    Request request = new Request("ACADEMIC");
                    request.addParam("action", "DELETE_STUDENT");
                    request.addParam("cardNum", cardNum);
                    // 添加Session信息以通过权限验证
                    request.setSession(nettyClient.getCurrentSession());
                    nettyClient.sendRequest(request).thenAccept(response -> {
                        SwingUtilities.invokeLater(() -> {
                            if (response.isSuccess()) {
                                JOptionPane.showMessageDialog(this,
                                        "学生信息删除成功！",
                                        "成功",
                                        JOptionPane.INFORMATION_MESSAGE);
                                loadStudentData(); // 刷新数据
                            } else {
                                JOptionPane.showMessageDialog(this,
                                        "删除学生信息失败：" + response.getMessage(),
                                        "错误",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }).exceptionally(throwable -> {
                        SwingUtilities.invokeLater(() -> {
                            log.error("删除学生信息时发生错误", throwable);
                            JOptionPane.showMessageDialog(this,
                                    "删除学生信息时发生错误：" + throwable.getMessage(),
                                    "错误",
                                    JOptionPane.ERROR_MESSAGE);
                        });
                        return null;
                    });
                } catch (Exception e) {
                    log.error("发送删除学生请求时发生错误", e);
                    JOptionPane.showMessageDialog(this,
                            "发送删除学生请求时发生错误：" + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void addStudent(Map<String, Object> studentData) {
        try {
            Request request = new Request("ACADEMIC");
            request.addParam("action", "ADD_STUDENT");
            request.addParam("student", JsonUtils.toJson(studentData));
            // 添加Session信息以通过权限验证
            request.setSession(nettyClient.getCurrentSession());
            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(this,
                                "学生信息添加成功！",
                                "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadStudentData(); // 刷新数据
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "添加学生信息失败：" + response.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.error("添加学生信息时发生错误", throwable);
                    JOptionPane.showMessageDialog(this,
                            "添加学生信息时发生错误：" + throwable.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
        } catch (Exception e) {
            log.error("发送添加学生请求时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "发送添加学生请求时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStudent(Map<String, Object> studentData) {
        try {
            Request request = new Request("ACADEMIC");
            request.addParam("action", "UPDATE_STUDENT");
            request.addParam("student", JsonUtils.toJson(studentData));
            // 添加Session信息以通过权限验证
            request.setSession(nettyClient.getCurrentSession());
            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(this,
                                "学生信息更新成功！",
                                "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadStudentData(); // 刷新数据
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "更新学生信息失败：" + response.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.error("更新学生信息时发生错误", throwable);
                    JOptionPane.showMessageDialog(this,
                            "更新学生信息时发生错误：" + throwable.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
        } catch (Exception e) {
            log.error("发送更新学生请求时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "发送更新学生请求时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // 教师相关操作方法
    private void showAddStaffDialog() {
        StaffEditDialog dialog = new StaffEditDialog(this, "添加教师", null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Map<String, Object> staffData = dialog.getStaffData();
            addStaff(staffData);
        }
    }

    private void editStaff(int row) {
        if (row >= 0 && row < staffTableModel.getRowCount()) {
            try {
                // 从表格获取教师信息，处理类型转换
                Map<String, Object> staffData = new HashMap<>();
                staffData.put("cardNum", staffTableModel.getValueAt(row, 0));
                staffData.put("staffId", staffTableModel.getValueAt(row, 1));
                staffData.put("name", staffTableModel.getValueAt(row, 2));

                // 处理年龄的类型转换
                Object ageObj = staffTableModel.getValueAt(row, 3);
                if (ageObj != null && !ageObj.toString().isEmpty()) {
                    try {
                        staffData.put("age", ageObj instanceof Integer ? (Integer) ageObj : Integer.parseInt(ageObj.toString()));
                    } catch (NumberFormatException e) {
                        staffData.put("age", 30); // 默认值
                    }
                } else {
                    staffData.put("age", 30); // 默认值
                }

                staffData.put("gender", staffTableModel.getValueAt(row, 4));
                staffData.put("title", staffTableModel.getValueAt(row, 5));
                staffData.put("department", staffTableModel.getValueAt(row, 6));
                staffData.put("phone", staffTableModel.getValueAt(row, 7));

                StaffEditDialog dialog = new StaffEditDialog(this, "编辑教师", staffData);
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    Map<String, Object> updatedStaffData = dialog.getStaffData();
                    updateStaff(updatedStaffData);
                }
            } catch (Exception e) {
                log.error("编辑教师信息时发生错误", e);
                JOptionPane.showMessageDialog(this,
                        "编辑教师信息时发生错误：" + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteStaff(int row) {
        if (row >= 0 && row < staffTableModel.getRowCount()) {
            String cardNum = (String) staffTableModel.getValueAt(row, 0);
            String name = (String) staffTableModel.getValueAt(row, 2);

            int result = JOptionPane.showConfirmDialog(this,
                    "确定要删除教师 " + name + "（卡号：" + cardNum + "）的信息吗？",
                    "确认删除",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                try {
                    Request request = new Request("ACADEMIC");
                    request.addParam("action", "DELETE_STAFF");
                    request.addParam("cardNum", cardNum);
                    // 添加Session信息以通过权限验证
                    request.setSession(nettyClient.getCurrentSession());
                    nettyClient.sendRequest(request).thenAccept(response -> {
                        SwingUtilities.invokeLater(() -> {
                            if (response.isSuccess()) {
                                JOptionPane.showMessageDialog(this,
                                        "教师信息删除成功！",
                                        "成功",
                                        JOptionPane.INFORMATION_MESSAGE);
                                loadStaffData(); // 刷新数据
                            } else {
                                JOptionPane.showMessageDialog(this,
                                        "删除教师信息失败：" + response.getMessage(),
                                        "错误",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }).exceptionally(throwable -> {
                        SwingUtilities.invokeLater(() -> {
                            log.error("删除教师信息时发生错误", throwable);
                            JOptionPane.showMessageDialog(this,
                                    "删除教师信息时发生错误：" + throwable.getMessage(),
                                    "错误",
                                    JOptionPane.ERROR_MESSAGE);
                        });
                        return null;
                    });
                } catch (Exception e) {
                    log.error("发送删除教师请求时发生错误", e);
                    JOptionPane.showMessageDialog(this,
                            "发送删除教师请求时发生错误：" + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void addStaff(Map<String, Object> staffData) {
        try {
            Request request = new Request("ACADEMIC");
            request.addParam("action", "ADD_STAFF");
            request.addParam("staff", JsonUtils.toJson(staffData));
            // 添加Session信息以通过权限验证
            request.setSession(nettyClient.getCurrentSession());
            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(this,
                                "教师信息添加成功！",
                                "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadStaffData(); // 刷新数据
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "添加教师信息失败：" + response.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.error("添加教师信息时发生错误", throwable);
                    JOptionPane.showMessageDialog(this,
                            "添加教师信息时发生错误：" + throwable.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
        } catch (Exception e) {
            log.error("发送添加教师请求时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "发送添加教师请求时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStaff(Map<String, Object> staffData) {
        try {
            Request request = new Request("ACADEMIC");
            request.addParam("action", "UPDATE_STAFF");
            request.addParam("staff", JsonUtils.toJson(staffData));
            // 添加Session信息以通过权限验证
            request.setSession(nettyClient.getCurrentSession());
            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(this,
                                "教师信息更新成功！",
                                "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadStaffData(); // 刷新数据
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "更新教师信息失败：" + response.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.error("更新教师信息时发生错误", throwable);
                    JOptionPane.showMessageDialog(this,
                            "更新教师信息时发生错误：" + throwable.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
        } catch (Exception e) {
            log.error("发送更新教师请求时发生错误", e);
            JOptionPane.showMessageDialog(this,
                    "发送更新教师请求时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchStudentByCardNum(String cardNum) {
        if (cardNum == null || cardNum.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入要查询的卡号", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 清除之前的选择
        studentTable.clearSelection();

        // 在表格中查找匹配的卡号并高亮显示
        boolean found = false;
        for (int i = 0; i < studentTableModel.getRowCount(); i++) {
            Object cellValue = studentTableModel.getValueAt(i, 0); // 卡号在第0列
            if (cellValue != null && cellValue.toString().equals(cardNum.trim())) {
                // 选中并高亮该行
                studentTable.setRowSelectionInterval(i, i);
                // 滚动到该行
                studentTable.scrollRectToVisible(studentTable.getCellRect(i, 0, true));
                found = true;
                break;
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(this,
                "未找到卡号为 " + cardNum + " 的学生",
                "查询结果",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            log.info("在学生表格中找到卡号: {}", cardNum);
        }
    }

    // 教师查询方法
    private void searchStaffByCardNum(String cardNum) {
        if (cardNum == null || cardNum.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入要查询的卡号", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 清除之前的选择
        staffTable.clearSelection();

        // 在表格中查找匹配的卡号并高亮显示
        boolean found = false;
        for (int i = 0; i < staffTableModel.getRowCount(); i++) {
            Object cellValue = staffTableModel.getValueAt(i, 0); // 卡号在第0列
            if (cellValue != null && cellValue.toString().equals(cardNum.trim())) {
                // 选中并高亮该行
                staffTable.setRowSelectionInterval(i, i);
                // 滚动到该行
                staffTable.scrollRectToVisible(staffTable.getCellRect(i, 0, true));
                found = true;
                break;
            }
        }

        if (!found) {
            JOptionPane.showMessageDialog(this,
                "未找到卡号为 " + cardNum + " 的教师",
                "查询结果",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            log.info("在教师表格中找到卡号: {}", cardNum);
        }
    }
}

package com.vcampus.client.core.ui.userAdmin;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.client.core.ui.admin.AdminFrame;
import java.util.HashMap;
import java.util.Map;
import com.vcampus.common.message.Request;
import com.vcampus.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;

/**
 * 教务管理界面 - 学生和教师信息管理
 * 支持增删改查功能
 */
@Slf4j
public class StatusPanel extends JFrame {

    private NettyClient nettyClient;
    private Map<String, Object> userData;
    private AdminFrame parentFrame; // 添加父窗口引用

    private JTabbedPane tabbedPane;
    private JTable studentTable;
    private JTable staffTable;
    private DefaultTableModel studentTableModel;
    private DefaultTableModel staffTableModel;
    // 将主面板提升为类字段，以便作为嵌入面板返回
    private JPanel mainPanel;

    // 学生信息表格列名
    private static final String[] STUDENT_COLUMNS = {
        "卡号", "学号", "姓名", "出生年月", "性别", "入学年份", "专业", "学院", "民族", "身份证号", "籍贯", "电话"
    };

    // 教师信息表格列名
    private static final String[] STAFF_COLUMNS = {
        "卡号", "工号", "姓名", "出生年月", "性别", "职称", "学院", "参工年份", "民族", "身份证号", "籍贯", "电话"
    };

    public StatusPanel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;

        initUI();
        loadData();

        log.info("教务管理界面初始化完成");
    }

    // 新增带AdminFrame参数的构造函数
    public StatusPanel(NettyClient nettyClient, Map<String, Object> userData, AdminFrame parentFrame) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        this.parentFrame = parentFrame;

        initUI();
        loadData();

        log.info("教务管理界面初始化完成");
    }

    private void initUI() {
        setTitle("VCampus - 教务管理系统");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // 修改为不直接关闭

        // 添加窗口关闭监听器
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleWindowClosing();
            }
        });

        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/figures/logo.png"));
            setIconImage(logoIcon.getImage());
        } catch (Exception e) {
            log.warn("图标加载失败");
        }

        // 创建主面板
        this.mainPanel = new JPanel(new BorderLayout());
        this.mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        this.mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 创建选��卡面板
        createTabbedPane();
        this.mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // 保留作为独立窗口时的 setContentPane
        setContentPane(this.mainPanel);
    }

    /**
     * 返回可嵌入到其它容器的主面板（用于在 AdminFrame 等右侧内容区域显示）
     */
    public JPanel getMainPanel() {
        return this.mainPanel;
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        JLabel titleLabel = new JLabel("用户管理系统");
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
                return false; // 用户管理界面不可编辑
            }
        };

        studentTable = new JTable(studentTableModel);
        setupModernTable(studentTable);

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("学信息列表"));
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
                return false; // 用户管理界面不可编辑
            }
        };

        staffTable = new JTable(staffTableModel);
        setupModernTable(staffTable);

        JScrollPane scrollPane = new JScrollPane(staffTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("教师信息列表"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStudentToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JButton btnAdd = new JButton("添加学生");
        JButton btnBatchImport = new JButton("批量导入");
        JButton btnBatchDelete = new JButton("批量删除");
        JButton btnRefresh = new JButton("刷新数据");

        btnAdd.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnBatchImport.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnBatchDelete.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        btnAdd.setBackground(new Color(25, 133, 57));
        btnAdd.setForeground(Color.WHITE);
        btnBatchImport.setBackground(new Color(13, 110, 253));
        btnBatchImport.setForeground(Color.WHITE);
        btnBatchDelete.setBackground(new Color(220, 53, 69));
        btnBatchDelete.setForeground(Color.WHITE);
        btnRefresh.setBackground(new Color(108, 117, 125));
        btnRefresh.setForeground(Color.WHITE);

        btnAdd.addActionListener(e -> showAddStudentBasicDialog());
        btnBatchImport.addActionListener(e -> {
            BatchImport batchImport = new BatchImport(nettyClient, this);
            batchImport.importStudents();
            loadStudentData(); // 刷新数据显示
        });
        btnBatchDelete.addActionListener(e -> {
            BatchDelete batchDelete = new BatchDelete(nettyClient, this);
            int[] selectedRows = studentTable.getSelectedRows();
            batchDelete.batchDeleteStudents(selectedRows, studentTable);
        });
        btnRefresh.addActionListener(e -> loadStudentData());

        // 添加查询功能
        JLabel searchLabel = new JLabel("查询:");
        searchLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        JTextField studentSearchField = new JTextField(15);
        studentSearchField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        studentSearchField.setToolTipText("输入卡号、姓名或性别进行模糊查询");

        JButton btnSearch = new JButton("查询");
        btnSearch.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnSearch.setBackground(new Color(255, 193, 7));
        btnSearch.setForeground(Color.BLACK);

        btnSearch.addActionListener(e -> searchStudent(studentSearchField.getText()));

        // 回车键查询
        studentSearchField.addActionListener(e -> searchStudent(studentSearchField.getText()));

        toolbar.add(btnAdd);
        toolbar.add(btnBatchImport);
        toolbar.add(btnBatchDelete);
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
        JButton btnBatchImport = new JButton("批量导入");
        JButton btnBatchDelete = new JButton("批量删除");
        JButton btnRefresh = new JButton("刷新数据");

        btnAdd.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnBatchImport.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnBatchDelete.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnRefresh.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        btnAdd.setBackground(new Color(25, 133, 57));
        btnAdd.setForeground(Color.WHITE);
        btnBatchImport.setBackground(new Color(13, 110, 253));
        btnBatchImport.setForeground(Color.WHITE);
        btnBatchDelete.setBackground(new Color(220, 53, 69));
        btnBatchDelete.setForeground(Color.WHITE);
        btnRefresh.setBackground(new Color(108, 117, 125));
        btnRefresh.setForeground(Color.WHITE);

        btnAdd.addActionListener(e -> showAddStaffBasicDialog());
        btnBatchImport.addActionListener(e -> {
            BatchImport batchImport = new BatchImport(nettyClient, this);
            batchImport.importStaff();
            loadStaffData(); // 刷新数据显示
        });
        btnBatchDelete.addActionListener(e -> {
            BatchDelete batchDelete = new BatchDelete(nettyClient, this);
            int[] selectedRows = staffTable.getSelectedRows();
            batchDelete.batchDeleteStaff(selectedRows, staffTable);
        });
        btnRefresh.addActionListener(e -> loadStaffData());

        // 添加查询功能
        JLabel searchLabel = new JLabel("查询:");
        searchLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        JTextField staffSearchField = new JTextField(15);
        staffSearchField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        staffSearchField.setToolTipText("输入卡号、姓名或性别进行模糊查询");

        JButton btnSearch = new JButton("查询");
        btnSearch.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnSearch.setBackground(new Color(255, 193, 7));
        btnSearch.setForeground(Color.BLACK);

        btnSearch.addActionListener(e -> searchStaff(staffSearchField.getText()));

        // 回车键查询
        staffSearchField.addActionListener(e -> searchStaff(staffSearchField.getText()));

        toolbar.add(btnAdd);
        toolbar.add(btnBatchImport);
        toolbar.add(btnBatchDelete);
        toolbar.add(btnRefresh);

        // 添加分隔符
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(searchLabel);
        toolbar.add(staffSearchField);
        toolbar.add(btnSearch);

        return toolbar;
    }

    private void loadData() {
        loadStudentData();
        loadStaffData();
    }

    // 公共方法供BatchDelete类调用
    public void loadStudentData() {
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
                            row.add(studentData.get("cardNum"));
                            row.add(studentData.get("studentId"));
                            row.add(studentData.get("name") != null ? studentData.get("name") : "");
                            row.add(studentData.get("birthDate") != null ? studentData.get("birthDate") : "");
                            row.add(studentData.get("gender") != null ? studentData.get("gender") : "");
                            row.add(studentData.get("enrollmentYear") != null ? studentData.get("enrollmentYear") : "");
                            row.add(studentData.get("major") != null ? studentData.get("major") : "");
                            row.add(studentData.get("department") != null ? studentData.get("department") : "");
                            row.add(studentData.get("ethnicity") != null ? studentData.get("ethnicity") : "");
                            row.add(studentData.get("idCard") != null ? studentData.get("idCard") : "");
                            row.add(studentData.get("hometown") != null ? studentData.get("hometown") : "");
                            row.add(studentData.get("phone") != null ? studentData.get("phone") : "");
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

    public void loadStaffData() {
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
                            row.add(staffData.get("cardNum"));
                            row.add(staffData.get("staffId"));
                            row.add(staffData.get("name") != null ? staffData.get("name") : "");
                            row.add(staffData.get("birthDate") != null ? staffData.get("birthDate") : "");
                            row.add(staffData.get("gender") != null ? staffData.get("gender") : "");
                            row.add(staffData.get("title") != null ? staffData.get("title") : "");
                            row.add(staffData.get("department") != null ? staffData.get("department") : "");
                            row.add(staffData.get("workYear") != null ? staffData.get("workYear") : "");
                            row.add(staffData.get("ethnicity") != null ? staffData.get("ethnicity") : "");
                            row.add(staffData.get("idCard") != null ? staffData.get("idCard") : "");
                            row.add(staffData.get("hometown") != null ? staffData.get("hometown") : "");
                            row.add(staffData.get("phone") != null ? staffData.get("phone") : "");
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
    private void showAddStudentBasicDialog() {
        BasicAddDialog dialog = new BasicAddDialog(this, "添加学生", true);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Map<String, Object> studentData = buildDefaultStudentData(dialog.getCardNum(), dialog.getEnteredName(), dialog.getGender());
            addStudent(studentData);
        }
    }

    private void deleteStudent(int row) {
        if (row >= 0 && row < studentTableModel.getRowCount()) {
            String cardNum = (String) studentTableModel.getValueAt(row, 0);
            String name = (String) studentTableModel.getValueAt(row, 2);

            int result = JOptionPane.showConfirmDialog(this,
                    "确定要删除学生 " + name + "（卡号���" + cardNum + "）的信息吗？",
                    "确认删除",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                try {
                    Request request = new Request("ACADEMIC");
                    request.addParam("action", "DELETE_STUDENT");
                    request.addParam("cardNum", cardNum);
                    // 添加Session信息以通过权��验证
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

    // 教师相关操作方法
    private void showAddStaffBasicDialog() {
        BasicAddDialog dialog = new BasicAddDialog(this, "添加教师", false);
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            Map<String, Object> staffData = buildDefaultStaffData(dialog.getCardNum(), dialog.getEnteredName(), dialog.getGender());
            addStaff(staffData);
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
                                    "删��教师信息时发生错误：" + throwable.getMessage(),
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

    private void searchStudent(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入要查询的内容", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 清除之前的选择
        studentTable.clearSelection();

        // 在表格中查找匹配的卡号或姓名（支持模糊查询）
        String search = searchText.trim().toLowerCase();
        java.util.List<Integer> matchedRows = new java.util.ArrayList<>();

        for (int i = 0; i < studentTableModel.getRowCount(); i++) {
            // 查询卡号（第0列）、姓名（第2列）和性别（第4列）
            Object cardNum = studentTableModel.getValueAt(i, 0); // 卡号
            Object name = studentTableModel.getValueAt(i, 2);    // 姓名
            Object gender = studentTableModel.getValueAt(i, 4);  // 性别
            
            boolean matched = false;
            if (cardNum != null && cardNum.toString().toLowerCase().contains(search)) {
                matched = true;
            }
            if (name != null && name.toString().toLowerCase().contains(search)) {
                matched = true;
            }
            if (gender != null && gender.toString().toLowerCase().contains(search)) {
                matched = true;
            }
            
            if (matched) {
                matchedRows.add(i);
            }
        }

        if (matchedRows.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "未找到包含 \"" + searchText + "\" 的学生记录",
                "查询结果",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            // 选中所有匹配的行
            int[] rows = matchedRows.stream().mapToInt(Integer::intValue).toArray();
            for (int i = 0; i < rows.length; i++) {
                if (i == 0) {
                    studentTable.setRowSelectionInterval(rows[i], rows[i]);
                } else {
                    studentTable.addRowSelectionInterval(rows[i], rows[i]);
                }
            }

            // 滚动到第一个匹配结果
            if (rows.length > 0) {
                studentTable.scrollRectToVisible(studentTable.getCellRect(rows[0], 0, true));
            }

            String message = String.format("找到 %d 个匹配的学生记录（包含 \"%s\"）",
                                          matchedRows.size(), searchText);
            JOptionPane.showMessageDialog(this, message, "查询结果", JOptionPane.INFORMATION_MESSAGE);

            log.info("在学生表格中找到 {} 个匹配记录: {}", matchedRows.size(), searchText);
        }
    }

    // 教师查询方法
    private void searchStaff(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入要查询的内容", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 清除之前的选择
        staffTable.clearSelection();

        // 在表格中查找匹配的卡号或姓名（支持模糊查询）
        String search = searchText.trim().toLowerCase();
        java.util.List<Integer> matchedRows = new java.util.ArrayList<>();

        for (int i = 0; i < staffTableModel.getRowCount(); i++) {
            // 查询卡号（第0列）、姓名（第2列）和性别（第4列）
            Object cardNum = staffTableModel.getValueAt(i, 0); // 卡号
            Object name = staffTableModel.getValueAt(i, 2);    // 姓名
            Object gender = staffTableModel.getValueAt(i, 4);  // 性别
            
            boolean matched = false;
            if (cardNum != null && cardNum.toString().toLowerCase().contains(search)) {
                matched = true;
            }
            if (name != null && name.toString().toLowerCase().contains(search)) {
                matched = true;
            }
            if (gender != null && gender.toString().toLowerCase().contains(search)) {
                matched = true;
            }
            
            if (matched) {
                matchedRows.add(i);
            }
        }

        if (matchedRows.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "未找到包含 \"" + searchText + "\" 的教师记录",
                "查询结果",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            // 选中所有匹配的行
            int[] rows = matchedRows.stream().mapToInt(Integer::intValue).toArray();
            for (int i = 0; i < rows.length; i++) {
                if (i == 0) {
                    staffTable.setRowSelectionInterval(rows[i], rows[i]);
                } else {
                    staffTable.addRowSelectionInterval(rows[i], rows[i]);
                }
            }

            // 滚动到第一个匹配结果
            if (rows.length > 0) {
                staffTable.scrollRectToVisible(staffTable.getCellRect(rows[0], 0, true));
            }

            String message = String.format("找到 %d 个匹配的教师记录（包含 \"%s\"）",
                                          matchedRows.size(), searchText);
            JOptionPane.showMessageDialog(this, message, "查询结果", JOptionPane.INFORMATION_MESSAGE);

            log.info("在教师表格中找到 {} 个匹配记录: {}", matchedRows.size(), searchText);
        }
    }

    // 处理窗口关闭事件
    private void handleWindowClosing() {
        int result = JOptionPane.showConfirmDialog(this,
                "确定要退出教务管理系统吗？",
                "确认退出",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            // 关闭当前窗口
            dispose();

            // 如果有父窗口引用，显示父窗口并恢复到个人信息页面
            if (parentFrame != null) {
                parentFrame.setVisible(true);
                parentFrame.toFront(); // 将AdminFrame窗口置于前台

                // 恢复AdminFrame到个人信息页面
                try {
                    // 使用反射调用switchToModule方法来切换到个人信息页面
                    java.lang.reflect.Method switchMethod = parentFrame.getClass().getDeclaredMethod("switchToModule", String.class);
                    switchMethod.setAccessible(true);
                    switchMethod.invoke(parentFrame, "profile");
                    log.info("已返回到管理员界面并恢复个人信息页面");
                } catch (Exception e) {
                    log.warn("恢复个人信息页面失败，仅显示AdminFrame: {}", e.getMessage());
                }

                log.info("已返回到管理员界面");
            } else {
                // 如果没有父窗口引用，创建新的AdminFrame（向后兼容）
                try {
                    AdminFrame adminFrame = new AdminFrame(nettyClient, userData);
                    adminFrame.setVisible(true);
                    log.info("已创建新的管理员界面");
                } catch (Exception e) {
                    log.error("创建管理员界面失败", e);
                    JOptionPane.showMessageDialog(null,
                            "返回管理员界面失败：" + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private Map<String, Object> buildDefaultStudentData(String cardNum, String name, String gender) {
        Map<String, Object> m = new HashMap<>();
        m.put("cardNum", cardNum);
        // 自动生成学号：若卡号长度>=8 取后8位，否则左侧补0
        String studentId = cardNum.length() >= 8 ? cardNum.substring(cardNum.length() - 8) : String.format("%8s", cardNum).replace(' ', '0');
        m.put("studentId", studentId);
        m.put("name", name);
        m.put("birthDate", "YYYY-MM");
        m.put("gender", gender);
        m.put("enrollmentYear", String.valueOf(java.time.Year.now().getValue()));
        m.put("major", "暂无");
        m.put("department", "暂无");
        m.put("phone", "暂无");
        m.put("ethnicity", "暂无");
        m.put("idCard", "111111111111111111");
        m.put("hometown", "暂无");
        return m;
    }

    private Map<String, Object> buildDefaultStaffData(String cardNum, String name, String gender) {
        Map<String, Object> m = new HashMap<>();
        m.put("cardNum", cardNum);
        String staffId = cardNum.length() >= 8 ? cardNum.substring(cardNum.length() - 8) : String.format("%8s", cardNum).replace(' ', '0');
        m.put("staffId", staffId);
        m.put("name", name);
        m.put("birthDate", "YYYY-MM");
        m.put("gender", gender);
        m.put("title", "暂无");
        m.put("department", "暂无");
        m.put("workYear", String.valueOf(java.time.Year.now().getValue()));
        m.put("phone", "暂无");
        m.put("ethnicity", "暂无");
        m.put("idCard", "111111111111111111");
        m.put("hometown", "暂无");
        return m;
    }

    // 基础添加对话框内部类
    private static class BasicAddDialog extends JDialog {
        private JTextField txtCardNum; private JTextField txtName; private JComboBox<String> cmbGender; private boolean confirmed=false; private final boolean isStudent;
        BasicAddDialog(Frame parent,String title,boolean isStudent){
            super(parent,title,true); this.isStudent=isStudent; init();
        }
        private void init(){
            setSize(300,200); setLayout(new BorderLayout()); setLocationRelativeTo(getParent());
            JPanel form=new JPanel(new GridLayout(3,2,5,5));
            form.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            form.add(new JLabel("卡号:")); txtCardNum=new JTextField(); form.add(txtCardNum);
            form.add(new JLabel("姓名:")); txtName=new JTextField(); form.add(txtName);
            form.add(new JLabel("性别:")); cmbGender=new JComboBox<>(new String[]{"男","女"}); form.add(cmbGender);
            add(form,BorderLayout.CENTER);
            JPanel btn=new JPanel(); JButton ok=new JButton("确定"), cancel=new JButton("取消");
            ok.addActionListener(e->{ if(validateInput()){confirmed=true; dispose();}});
            cancel.addActionListener(e->{dispose();});
            btn.add(ok); btn.add(cancel); add(btn,BorderLayout.SOUTH);
        }
        private boolean validateInput(){
            if(txtCardNum.getText().trim().isEmpty()) return false; if(txtName.getText().trim().isEmpty()) return false; return true;
        }
        boolean isConfirmed(){return confirmed;} String getCardNum(){return txtCardNum.getText().trim();} String getEnteredName(){return txtName.getText().trim();} String getGender(){return (String)cmbGender.getSelectedItem();}
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
        table.setSelectionForeground(new Color(52, 58, 64));
        table.setBackground(Color.WHITE);

        // 设置表头样式
        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setFont(new Font("微软雅黑", Font.BOLD, 13));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 45));

        // 设置自定义表头渲染器
        header.setDefaultRenderer(new HeaderRenderer());

        // 禁用表格拖动功能
        table.setDragEnabled(false);
        header.setReorderingAllowed(false);

        // 设置交替行颜色
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
    }

    /**
     * 表头渲染器 - 蓝色渐变背景
     */
    private class HeaderRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("微软雅黑", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setOpaque(false); // 让自定义绘制生效
            
            return this;
        }
        
        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 绘制蓝色渐变背景
            java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                0, 0, new Color(76, 151, 234),
                getWidth(), getHeight(), new Color(52, 144, 220)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            g2d.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * 交替行渲染器
     */
    private class AlternatingRowRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

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
}

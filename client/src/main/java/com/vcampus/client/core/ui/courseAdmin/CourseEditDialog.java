package com.vcampus.client.core.ui.courseAdmin;

import com.vcampus.client.core.util.IdUtils; // 新增：导入ID工具类
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * 课程编辑对话框
 * 用于添加和编辑课程信息
 */
public class CourseEditDialog extends JDialog {

    private JTextField txtCourseName;
    private JSpinner spnCredit;
    private JComboBox<String> cmbDepartment;
    private JTextArea txtDescription;

    private boolean confirmed = false;
    private final Map<String, Object> courseData;

    // 预定义的学院列表
    private static final String[] DEPARTMENTS = {
        "计算机学院", "电子信息学院", "机械工程学院", "材料科学学院",
        "化学化工学院", "生命科学学院", "数学学院", "物理学院",
        "经济管理学院", "人文学院", "外国语学院", "艺术学院"
    };

    public CourseEditDialog(Window parent, String title, Map<String, Object> existingData) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.courseData = existingData;

        initUI();
        if (existingData != null) {
            populateFields(existingData);
        }

        // 设置默认焦点
        SwingUtilities.invokeLater(() -> txtCourseName.requestFocus());
    }

    private void initUI() {
        setSize(450, 400);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建标题
        JLabel titleLabel = new JLabel(getTitle(), JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // 创建表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // 课程名称
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblCourseName = new JLabel("课程名称:");
        lblCourseName.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        formPanel.add(lblCourseName, gbc);

        txtCourseName = new JTextField(20);
        txtCourseName.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        txtCourseName.setToolTipText("请输入课程名称，如：高等数学");
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtCourseName, gbc);

        // 学分
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblCredit = new JLabel("学分:");
        lblCredit.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        formPanel.add(lblCredit, gbc);

        spnCredit = new JSpinner(new SpinnerNumberModel(1, 1, 8, 1));
        spnCredit.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        spnCredit.setToolTipText("选择课程学分，范围1-8分");
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(spnCredit, gbc);

        // 开课学院
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblDepartment = new JLabel("开课学院:");
        lblDepartment.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        formPanel.add(lblDepartment, gbc);

        cmbDepartment = new JComboBox<>(DEPARTMENTS);
        cmbDepartment.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cmbDepartment.setEditable(true); // 允许输入自定义学院
        cmbDepartment.setToolTipText("选择或输入开课学院");
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(cmbDepartment, gbc);

        // 课程描述（可选）
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblDescription = new JLabel("课程描述:");
        lblDescription.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        formPanel.add(lblDescription, gbc);

        txtDescription = new JTextArea(4, 20);
        txtDescription.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setToolTipText("可选：输入课程描述信息");
        JScrollPane scrollDesc = new JScrollPane(txtDescription);
        scrollDesc.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        formPanel.add(scrollDesc, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton btnOk = new JButton(courseData != null ? "更新" : "添加");
        JButton btnCancel = new JButton("取消");

        btnOk.setPreferredSize(new Dimension(80, 35));
        btnCancel.setPreferredSize(new Dimension(80, 35));

        btnOk.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnCancel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        btnOk.setBackground(new Color(25, 133, 57));
        btnOk.setForeground(Color.WHITE);
        btnOk.setFocusPainted(false);

        btnCancel.setBackground(new Color(108, 117, 125));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);

        btnOk.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });

        btnCancel.addActionListener(e -> dispose());

        // 添加快捷键支持
        getRootPane().setDefaultButton(btnOk);

        // ESC键取消
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke("ESCAPE");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(escapeKeyStroke, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonPanel.add(btnOk);
        buttonPanel.add(btnCancel);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void populateFields(Map<String, Object> data) {
        if (data.get("courseName") != null) {
            txtCourseName.setText(data.get("courseName").toString());
        }

        if (data.get("credit") != null) {
            try {
                int credit = Integer.parseInt(data.get("credit").toString());
                spnCredit.setValue(credit);
            } catch (NumberFormatException e) {
                spnCredit.setValue(1); // 默认值
            }
        }

        if (data.get("department") != null) {
            String department = data.get("department").toString();
            cmbDepartment.setSelectedItem(department);
        }

        if (data.get("description") != null) {
            txtDescription.setText(data.get("description").toString());
        }
    }

    private boolean validateInput() {
        // 验证课程名称
        String courseName = txtCourseName.getText().trim();
        if (courseName.isEmpty()) {
            showError("请输入课程名称");
            txtCourseName.requestFocus();
            return false;
        }

        if (courseName.length() > 50) {
            showError("课程名称不能超过50个字符");
            txtCourseName.requestFocus();
            return false;
        }

        // 验证学分
        int credit = (Integer) spnCredit.getValue();
        if (credit < 1 || credit > 8) {
            showError("学分必须在1-8之间");
            spnCredit.requestFocus();
            return false;
        }

        // 验证开课学院（安全处理可能的null值）
        Object selectedItem = cmbDepartment.getSelectedItem();
        if (selectedItem == null) {
            showError("请选择或输入开课学院");
            cmbDepartment.requestFocus();
            return false;
        }

        String department = selectedItem.toString().trim();
        if (department.isEmpty()) {
            showError("请选择或输入开课学院");
            cmbDepartment.requestFocus();
            return false;
        }

        if (department.length() > 30) {
            showError("开课学院名称不能超过30个字符");
            cmbDepartment.requestFocus();
            return false;
        }

        // 验证描述（可选）
        String description = txtDescription.getText().trim();
        if (description.length() > 500) {
            showError("课程描述不能超过500个字符");
            txtDescription.requestFocus();
            return false;
        }

        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "输入验证", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Map<String, Object> getCourseData() {
        Map<String, Object> data = new HashMap<>();

        // 如果是编辑模式，保留原有的课程ID
        if (courseData != null && courseData.containsKey("course_Id")) {
            data.put("course_Id", courseData.get("course_Id"));
        }

        data.put("courseName", txtCourseName.getText().trim());
        data.put("credit", spnCredit.getValue());

        // 安全处理开课学院
        Object selectedItem = cmbDepartment.getSelectedItem();
        if (selectedItem != null) {
            data.put("department", selectedItem.toString().trim());
        }

        // 课程描述（如果有的话）
        String description = txtDescription.getText().trim();
        if (!description.isEmpty()) {
            data.put("description", description);
        }

        return data;
    }
}

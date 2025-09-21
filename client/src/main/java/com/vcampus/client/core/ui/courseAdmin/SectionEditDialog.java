package com.vcampus.client.core.ui.courseAdmin;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 教学班编辑对话框
 * 用于添加和编辑教学班信息
 */
public class SectionEditDialog extends JDialog {

    private JComboBox<CourseItem> cbxCourse;
    private JTextField txtTerm;
    private JTextField txtTeacherId;
    private JTextField txtRoom;
    private JSpinner spnCapacity;
    private JTextField txtSchedule;

    private boolean confirmed = false;
    private Map<String, Object> sectionData;
    private NettyClient nettyClient;

    public SectionEditDialog(Window parent, String title, Map<String, Object> existingData, NettyClient nettyClient) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.sectionData = existingData;
        this.nettyClient = nettyClient;

        initUI();
        // 先加载课程列表，待加载完成后再填充编辑数据，避免异步竞态
        loadCourseList();
    }

    private void initUI() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // 课程选择
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("课程:"), gbc);

        cbxCourse = new JComboBox<>();
        cbxCourse.setPreferredSize(new Dimension(250, 25));
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(cbxCourse, gbc);

        // 学期
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("学期:"), gbc);

        txtTerm = new JTextField(20);
        txtTerm.setToolTipText("例如：2024春、2024秋");
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtTerm, gbc);

        // 任课教师
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("任课教师工号:"), gbc);

        txtTeacherId = new JTextField(20);
        txtTeacherId.setToolTipText("输入教师的工号");
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtTeacherId, gbc);

        // 教室
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("教室:"), gbc);

        txtRoom = new JTextField(20);
        txtRoom.setToolTipText("例如：A101、B202");
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtRoom, gbc);

        // 容量
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("容量:"), gbc);

        spnCapacity = new JSpinner(new SpinnerNumberModel(30, 1, 200, 1));
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(spnCapacity, gbc);

        // 上课时间
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("上课时间:"), gbc);

        txtSchedule = new JTextField(20);
        txtSchedule.setToolTipText("例如：周一1-2节、周三3-4节");
        gbc.gridx = 1; gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(txtSchedule, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnOk = new JButton("确定");
        JButton btnCancel = new JButton("取消");

        btnOk.setPreferredSize(new Dimension(80, 30));
        btnCancel.setPreferredSize(new Dimension(80, 30));

        btnOk.addActionListener(e -> {
            if (validateInput()) {
                confirmed = true;
                dispose();
            }
        });

        btnCancel.addActionListener(e -> dispose());

        buttonPanel.add(btnOk);
        buttonPanel.add(btnCancel);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadCourseList() {
        try {
            Request request = new Request("academic/course")
                    .addParam("action", "GET_ALL_COURSES");

            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response != null && response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> courseList = (List<Map<String, Object>>) response.getData();

                        cbxCourse.removeAllItems();
                        for (Map<String, Object> course : courseList) {
                            CourseItem item = new CourseItem(
                                safeToPlainString(course.get("course_Id")),
                                course.get("courseName") != null ? course.get("courseName").toString() : ""
                            );
                            cbxCourse.addItem(item);
                        }

                        // 课程列表加载后再填充已有数据，确保预选生效
                        if (sectionData != null) {
                            populateFields(sectionData);
                        }
                    }
                });
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "加载课程列表失败：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateFields(Map<String, Object> data) {
        // 优先根据 course_Id 预选；若缺失则回退按课程名称匹配
        String targetCourseId = data.get("course_Id") != null ? safeToPlainString(data.get("course_Id")) : null;
        String courseName = data.get("courseName") != null ? data.get("courseName").toString() : null;
        boolean selected = false;
        if (targetCourseId != null) {
            for (int i = 0; i < cbxCourse.getItemCount(); i++) {
                CourseItem item = cbxCourse.getItemAt(i);
                if (item.getId().equals(targetCourseId)) {
                    cbxCourse.setSelectedIndex(i);
                    selected = true;
                    break;
                }
            }
        }
        if (!selected && courseName != null) {
            for (int i = 0; i < cbxCourse.getItemCount(); i++) {
                CourseItem item = cbxCourse.getItemAt(i);
                if (item.getName().equals(courseName)) {
                    cbxCourse.setSelectedIndex(i);
                    selected = true;
                    break;
                }
            }
        }

        txtTerm.setText(data.get("Term") != null ? data.get("Term").toString() : "");
        txtTeacherId.setText(data.get("Teacher_id") != null ? data.get("Teacher_id").toString() : "");
        txtRoom.setText(data.get("Room") != null ? data.get("Room").toString() : "");
        spnCapacity.setValue(parseIntSafe(data.get("Capacity"), 30));
        txtSchedule.setText(data.get("Schedule") != null ? data.get("Schedule").toString() : "");
    }

    private boolean validateInput() {
        if (cbxCourse.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "请选择课程", "验证错误", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (txtTerm.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入学期", "验证错误", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        if (txtRoom.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入教室", "验证错误", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 容量必须为正数
        try {
            int cap = Integer.parseInt(spnCapacity.getValue().toString());
            if (cap <= 0) {
                JOptionPane.showMessageDialog(this, "容量必须大于0", "验证错误", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "容量格式错误", "验证错误", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Map<String, Object> getSectionData() {
        Map<String, Object> data = new HashMap<>();

        // 如果是编辑模式，保留原有的教学班ID
        if (sectionData != null && sectionData.containsKey("section_Id")) {
            data.put("section_Id", sectionData.get("section_Id"));
        }

        CourseItem selectedCourse = (CourseItem) cbxCourse.getSelectedItem();
        data.put("course_Id", selectedCourse != null ? selectedCourse.getId() : "");
        data.put("Term", txtTerm.getText().trim());
        data.put("Teacher_id", txtTeacherId.getText().trim());
        data.put("Room", txtRoom.getText().trim());
        // 转为整数，避免出现 30.0 之类
        data.put("Capacity", Integer.parseInt(spnCapacity.getValue().toString()));
        data.put("Schedule", txtSchedule.getText().trim());

        return data;
    }

    // 内部类：课程项
    private static class CourseItem {
        private String id;
        private String name;

        public CourseItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name + " (ID: " + id + ")";
        }
    }

    // 工具：安全转为整数字符串（兼容 1、1.0、数字类型）
    private static String safeToPlainString(Object v) {
        if (v == null) return "";
        if (v instanceof Number) {
            double d = ((Number) v).doubleValue();
            long l = (long) d;
            if (Math.abs(d - l) < 1e-9) return String.valueOf(l);
            return String.valueOf(d);
        }
        String s = v.toString().trim();
        try {
            if (s.contains(".")) {
                double d = Double.parseDouble(s);
                long l = (long) d;
                if (Math.abs(d - l) < 1e-9) return String.valueOf(l);
            }
        } catch (Exception ignored) {}
        return s;
    }

    // 工具：安全解析整数
    private static int parseIntSafe(Object v, int defaultVal) {
        if (v == null) return defaultVal;
        try {
            if (v instanceof Number) return (int) Math.round(((Number) v).doubleValue());
            String s = v.toString().trim();
            if (s.isEmpty()) return defaultVal;
            if (s.contains(".")) return (int) Math.round(Double.parseDouble(s));
            return Integer.parseInt(s);
        } catch (Exception e) {
            return defaultVal;
        }
    }
}

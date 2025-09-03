package com.vcampus.client.core.ui.academic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lombok.extern.slf4j.Slf4j;

/**
 * 学生信息编辑对话框
 */
@Slf4j
public class StudentEditDialog extends JDialog {

    private JTextField txtCardNum;
    private JTextField txtStudentId;
    private JTextField txtName;
    private JTextField txtAge;
    private JComboBox<String> cmbGender;
    private JComboBox<Integer> cmbGrade;
    private JTextField txtMajor;
    private JTextField txtDepartment;
    private JTextField txtPhone;

    private JButton btnOk;
    private JButton btnCancel;

    private boolean confirmed = false;
    private Map<String, Object> studentData;

    public StudentEditDialog(Frame parent, String title, Map<String, Object> studentData) {
        super(parent, title, true);
        this.studentData = studentData != null ? new HashMap<>(studentData) : new HashMap<>();

        initUI();
        if (this.studentData != null && !this.studentData.isEmpty()) {
            fillData(this.studentData);
        }

        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setSize(450, 550);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建表单面板
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // 卡号
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("卡号:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtCardNum = new JTextField(20);
        panel.add(txtCardNum, gbc);

        // 学号
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("学号:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtStudentId = new JTextField(20);
        panel.add(txtStudentId, gbc);

        // 姓名
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("姓名:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtName = new JTextField(20);
        panel.add(txtName, gbc);

        // 年龄
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("年龄:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtAge = new JTextField(20);
        panel.add(txtAge, gbc);

        // 性别
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("性别:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        cmbGender = new JComboBox<>(new String[]{"男", "女"});
        panel.add(cmbGender, gbc);

        // 年级
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("年级:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        cmbGrade = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        panel.add(cmbGrade, gbc);

        // 专业
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("专业:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtMajor = new JTextField(20);
        panel.add(txtMajor, gbc);

        // 学院
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("学院:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtDepartment = new JTextField(20);
        panel.add(txtDepartment, gbc);

        // 电话
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("电话:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtPhone = new JTextField(20);
        panel.add(txtPhone, gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        btnOk = new JButton("确定");
        btnCancel = new JButton("取消");

        btnOk.setPreferredSize(new Dimension(100, 35));
        btnCancel.setPreferredSize(new Dimension(100, 35));

        btnOk.setBackground(new Color(25, 133, 57));
        btnOk.setForeground(Color.WHITE);
        btnCancel.setBackground(new Color(108, 117, 125));
        btnCancel.setForeground(Color.WHITE);

        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateInput()) {
                    confirmed = true;
                    dispose();
                }
            }
        });

        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmed = false;
                dispose();
            }
        });

        panel.add(btnOk);
        panel.add(btnCancel);

        return panel;
    }

    private void fillData(Map<String, Object> data) {
        txtCardNum.setText(getStringValue(data, "cardNum"));
        txtStudentId.setText(getStringValue(data, "studentId"));
        txtName.setText(getStringValue(data, "name"));
        
        Integer age = getIntegerValue(data, "age");
        if (age != null) {
            txtAge.setText(age.toString());
        }
        
        String gender = getStringValue(data, "gender");
        if (gender != null) {
            cmbGender.setSelectedItem(gender);
        }
        
        Integer grade = getIntegerValue(data, "grade");
        if (grade != null) {
            cmbGrade.setSelectedItem(grade);
        }
        
        txtMajor.setText(getStringValue(data, "major"));
        txtDepartment.setText(getStringValue(data, "department"));
        txtPhone.setText(getStringValue(data, "phone"));
    }
    
    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "";
    }
    
    private Integer getIntegerValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private boolean validateInput() {
        if (txtCardNum.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入卡号！", "验证失败", JOptionPane.WARNING_MESSAGE);
            txtCardNum.requestFocus();
            return false;
        }

        if (txtStudentId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入学号！", "验证失败", JOptionPane.WARNING_MESSAGE);
            txtStudentId.requestFocus();
            return false;
        }

        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入姓名！", "验证失败", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return false;
        }

        try {
            Integer.parseInt(txtAge.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的年龄！", "验证失败", JOptionPane.WARNING_MESSAGE);
            txtAge.requestFocus();
            return false;
        }

        if (txtPhone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入电话号码！", "验证失败", JOptionPane.WARNING_MESSAGE);
            txtPhone.requestFocus();
            return false;
        }

        return true;
    }

    public Map<String, Object> getStudentData() {
        if (!confirmed) {
            return null;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("cardNum", txtCardNum.getText().trim());
        data.put("studentId", txtStudentId.getText().trim());
        data.put("name", txtName.getText().trim());
        
        try {
            data.put("age", Integer.parseInt(txtAge.getText().trim()));
        } catch (NumberFormatException e) {
            data.put("age", null);
        }
        
        data.put("gender", cmbGender.getSelectedItem());
        
        // 确保年级是Integer类型
        Object gradeObj = cmbGrade.getSelectedItem();
        if (gradeObj instanceof Integer) {
            data.put("grade", gradeObj);
        } else if (gradeObj instanceof String) {
            try {
                data.put("grade", Integer.parseInt((String) gradeObj));
            } catch (NumberFormatException e) {
                data.put("grade", null);
            }
        } else {
            data.put("grade", null);
        }
        data.put("major", txtMajor.getText().trim());
        data.put("department", txtDepartment.getText().trim());
        data.put("phone", txtPhone.getText().trim());

        return data;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}

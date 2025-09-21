package com.vcampus.client.core.ui.userAdmin;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * 教师信息编辑对话框
 */
@Slf4j
public class StaffEditDialog extends JDialog {

    private JTextField txtCardNum;
    private JTextField txtStaffId;
    private JTextField txtName;
    private JTextField txtBirthDate;
    private JComboBox<String> cmbGender;
    private JTextField txtTitle;
    private JTextField txtDepartment;
    private JTextField txtPhone;
    private JTextField txtWorkYear;
    private JTextField txtEthnicity;
    private JTextField txtIdCard;
    private JTextField txtHometown;

    private JButton btnOk;
    private JButton btnCancel;

    private boolean confirmed = false;
    private Map<String, Object> staffData;

    public StaffEditDialog(Frame parent, String title, Map<String, Object> staffData) {
        super(parent, title, true);
        this.staffData = staffData != null ? new HashMap<>(staffData) : new HashMap<>();

        initUI();
        if (this.staffData != null && !this.staffData.isEmpty()) {
            fillData(this.staffData);
        }

        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setSize(450, 700);
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

        // 工号
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("工号:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtStaffId = new JTextField(20);
        panel.add(txtStaffId, gbc);

        // 姓名
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("姓名:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtName = new JTextField(20);
        panel.add(txtName, gbc);

        // 出生年月
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("出生年月:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtBirthDate = new JTextField(20);
        panel.add(txtBirthDate, gbc);

        // 性别
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("性别:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        cmbGender = new JComboBox<>(new String[]{"男", "女"});
        panel.add(cmbGender, gbc);

        // 职称
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("职称:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtTitle = new JTextField(20);
        panel.add(txtTitle, gbc);

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

        // 参工年份
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("参工年份:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtWorkYear = new JTextField(20);
        panel.add(txtWorkYear, gbc);

        // 民族
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("民族:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtEthnicity = new JTextField(20);
        panel.add(txtEthnicity, gbc);

        // 身份证号
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("身份证号:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtIdCard = new JTextField(20);
        panel.add(txtIdCard, gbc);

        // 籍贯
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("籍贯:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtHometown = new JTextField(20);
        panel.add(txtHometown, gbc);

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
        txtStaffId.setText(getStringValue(data, "staffId"));
        txtName.setText(getStringValue(data, "name"));
        txtBirthDate.setText(getStringValue(data, "birthDate"));
        
        String gender = getStringValue(data, "gender");
        if (gender != null) {
            cmbGender.setSelectedItem(gender);
        }
        
        txtTitle.setText(getStringValue(data, "title"));
        txtDepartment.setText(getStringValue(data, "department"));
        txtPhone.setText(getStringValue(data, "phone"));
        txtWorkYear.setText(getStringValue(data, "workYear"));
        txtEthnicity.setText(getStringValue(data, "ethnicity"));
        txtIdCard.setText(getStringValue(data, "idCard"));
        txtHometown.setText(getStringValue(data, "hometown"));
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

        if (txtStaffId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入工号！", "验证失败", JOptionPane.WARNING_MESSAGE);
            txtStaffId.requestFocus();
            return false;
        }

        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入姓名！", "验证失败", JOptionPane.WARNING_MESSAGE);
            txtName.requestFocus();
            return false;
        }

        // 出生年月验证
        if (txtBirthDate.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入出生年月！", "验证失败", JOptionPane.WARNING_MESSAGE);
            txtBirthDate.requestFocus();
            return false;
        }

        if (txtPhone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入电话号码！", "验证失败", JOptionPane.WARNING_MESSAGE);
            txtPhone.requestFocus();
            return false;
        }

        return true;
    }

    public Map<String, Object> getStaffData() {
        if (!confirmed) {
            return null;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("cardNum", txtCardNum.getText().trim());
        data.put("staffId", txtStaffId.getText().trim());
        data.put("name", txtName.getText().trim());
        data.put("birthDate", txtBirthDate.getText().trim());
        data.put("gender", cmbGender.getSelectedItem());
        data.put("title", txtTitle.getText().trim());
        data.put("department", txtDepartment.getText().trim());
        data.put("phone", txtPhone.getText().trim());
        data.put("workYear", txtWorkYear.getText().trim());
        data.put("ethnicity", txtEthnicity.getText().trim());
        data.put("idCard", txtIdCard.getText().trim());
        data.put("hometown", txtHometown.getText().trim());

        return data;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}

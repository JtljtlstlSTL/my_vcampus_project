package com.vcampus.client.core.ui.eduAdmin;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * 师籍信息编辑对话框
 * 只包含工号、职称、学院字段
 */
@Slf4j
public class StaffInfoEditDialog extends JDialog {

    private JTextField txtStaffId;
    private JTextField txtTitle;
    private JTextField txtDepartment;

    private JButton btnOk;
    private JButton btnCancel;

    private boolean confirmed = false;
    private Map<String, Object> originalData;

    public StaffInfoEditDialog(Frame parent, Map<String, Object> staffData) {
        super(parent, "编辑师籍信息", true);
        this.originalData = staffData != null ? new HashMap<>(staffData) : new HashMap<>();

        initUI();
        if (this.originalData != null && !this.originalData.isEmpty()) {
            fillData(this.originalData);
        }

        setLocationRelativeTo(parent);
    }

    private void initUI() {
        setSize(550, 350);
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
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // 标题
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel titleLabel = new JLabel("编辑师籍信息");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, gbc);

        row++;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        // 工号
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("工号:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtStaffId = new JTextField(30);
        txtStaffId.setPreferredSize(new Dimension(300, 30));
        panel.add(txtStaffId, gbc);

        // 职称
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("职称:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtTitle = new JTextField(30);
        txtTitle.setPreferredSize(new Dimension(300, 30));
        panel.add(txtTitle, gbc);

        // 学院
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("学院:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        txtDepartment = new JTextField(30);
        txtDepartment.setPreferredSize(new Dimension(300, 30));
        panel.add(txtDepartment, gbc);

        // 添加说明文字
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel lblNote = new JLabel("<html><small>注：留空的字段将保持原值不变</small></html>");
        lblNote.setForeground(Color.GRAY);
        lblNote.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblNote, gbc);

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
                confirmed = true;
                dispose();
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
        txtStaffId.setText(getStringValue(data, "staffId"));
        txtTitle.setText(getStringValue(data, "title"));
        txtDepartment.setText(getStringValue(data, "department"));
    }

    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "";
    }

    public Map<String, Object> getUpdatedData() {
        if (!confirmed) {
            return null;
        }

        Map<String, Object> updatedData = new HashMap<>(originalData);
        
        // 只更新非空字段
        String staffId = txtStaffId.getText().trim();
        if (!staffId.isEmpty()) {
            updatedData.put("staffId", staffId);
        }
        
        String title = txtTitle.getText().trim();
        if (!title.isEmpty()) {
            updatedData.put("title", title);
        }
        
        String department = txtDepartment.getText().trim();
        if (!department.isEmpty()) {
            updatedData.put("department", department);
        }

        return updatedData;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}

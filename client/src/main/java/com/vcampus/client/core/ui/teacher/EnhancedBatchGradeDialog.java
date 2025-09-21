package com.vcampus.client.core.ui.teacher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 增强的批量成绩录入对话框
 * 提供更便捷的批量成绩管理功能
 */
public class EnhancedBatchGradeDialog extends JDialog {

    private boolean confirmed = false;
    private DefaultTableModel originalTableModel;
    private DefaultTableModel batchTableModel;
    private JTable batchTable;
    private String courseName;
    private List<Map<String, Object>> scoreChanges;

    // 成绩等级预设 - 根据新的GPA计算标准更新
    private static final Map<String, Double[]> GRADE_PRESETS = new HashMap<>();
    static {
        GRADE_PRESETS.put("优秀 (96-100)", new Double[]{97.0, 4.8});
        GRADE_PRESETS.put("良好 (90-95)", new Double[]{93.0, 4.5});
        GRADE_PRESETS.put("中等 (80-89)", new Double[]{85.0, 3.3});
        GRADE_PRESETS.put("及格 (60-79)", new Double[]{70.0, 2.0});
        GRADE_PRESETS.put("不及格 (0-59)", new Double[]{50.0, 0.0});
    }

    public EnhancedBatchGradeDialog(Window parent, String courseName, DefaultTableModel originalModel) {
        super(parent, "批量成绩管理 - " + courseName, ModalityType.APPLICATION_MODAL);
        this.courseName = courseName;
        this.originalTableModel = originalModel;
        this.scoreChanges = new ArrayList<>();

        initComponents();
        setupData();
        setupEventHandlers();

        setSize(900, 650);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 创建标题面板
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);

        // 创建主内容面板
        JPanel contentPanel = createContentPanel();
        add(contentPanel, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));
        panel.setBackground(new Color(248, 249, 250));

        JLabel titleLabel = new JLabel("📊 批量成绩管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 37, 41));

        JLabel subtitleLabel = new JLabel("课程：" + courseName);
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(108, 117, 125));

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(subtitleLabel, BorderLayout.CENTER);

        panel.add(textPanel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(0, 20, 0, 20));

        // 创建工具栏
        JPanel toolbarPanel = createToolbarPanel();
        panel.add(toolbarPanel, BorderLayout.NORTH);

        // 创建表格
        createBatchTable();
        JScrollPane scrollPane = new JScrollPane(batchTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(222, 226, 230)),
            "学生成绩列表",
            0, 0, new Font("微软雅黑", Font.BOLD, 12)
        ));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createToolbarPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));

        // 快速评分按钮组
        JLabel quickLabel = new JLabel("快速评分：");
        quickLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        panel.add(quickLabel);

        for (String gradeName : GRADE_PRESETS.keySet()) {
            JButton gradeBtn = new JButton(gradeName.split(" ")[0]);
            gradeBtn.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            gradeBtn.setPreferredSize(new Dimension(60, 28));
            gradeBtn.addActionListener(e -> applyGradeToSelected(gradeName));
            styleGradeButton(gradeBtn, gradeName);
            panel.add(gradeBtn);
        }

        panel.add(Box.createHorizontalStrut(20));

        // 批量操作按钮
        JButton selectAllBtn = new JButton("全选");
        selectAllBtn.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        selectAllBtn.addActionListener(e -> selectAllStudents());
        panel.add(selectAllBtn);

        JButton clearAllBtn = new JButton("清空成绩");
        clearAllBtn.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        clearAllBtn.addActionListener(e -> clearAllScores());
        panel.add(clearAllBtn);

        return panel;
    }

    private void styleGradeButton(JButton button, String gradeName) {
        if (gradeName.contains("优秀")) {
            button.setBackground(new Color(40, 167, 69));
            button.setForeground(Color.WHITE);
        } else if (gradeName.contains("良好")) {
            button.setBackground(new Color(23, 162, 184));
            button.setForeground(Color.WHITE);
        } else if (gradeName.contains("中等")) {
            button.setBackground(new Color(255, 193, 7));
            button.setForeground(Color.BLACK);
        } else if (gradeName.contains("及格")) {
            button.setBackground(new Color(255, 152, 0));
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(220, 53, 69));
            button.setForeground(Color.WHITE);
        }
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }

    private void createBatchTable() {
        String[] columns = {"选择", "学号", "姓名", "专业", "当前分数", "新分数", "当前GPA", "新GPA", "状态"};

        batchTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 5 || column == 7; // 选择、新分数、新GPA可编辑
            }
        };

        batchTable = new JTable(batchTableModel);
        batchTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        batchTable.setRowHeight(30);
        batchTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        batchTable.getTableHeader().setBackground(new Color(248, 249, 250));

        // 设置列宽
        batchTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // 选择
        batchTable.getColumnModel().getColumn(1).setPreferredWidth(100); // 学号
        batchTable.getColumnModel().getColumn(2).setPreferredWidth(100); // 姓名
        batchTable.getColumnModel().getColumn(3).setPreferredWidth(120); // 专业
        batchTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 当前分数
        batchTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // 新分数
        batchTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // 当前GPA
        batchTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // 新GPA
        batchTable.getColumnModel().getColumn(8).setPreferredWidth(80);  // 状态

        // 设置状态列的渲染器
        batchTable.getColumnModel().getColumn(8).setCellRenderer(new StatusCellRenderer());
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JButton previewBtn = new JButton("预览更改");
        previewBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        previewBtn.addActionListener(e -> previewChanges());

        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        cancelBtn.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        JButton confirmBtn = new JButton("确认提交");
        confirmBtn.setFont(new Font("微软雅黑", Font.BOLD, 12));
        confirmBtn.setBackground(new Color(0, 123, 255));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBorderPainted(false);
        confirmBtn.setFocusPainted(false);
        confirmBtn.addActionListener(e -> confirmChanges());

        panel.add(previewBtn);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(cancelBtn);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(confirmBtn);

        return panel;
    }

    private void setupData() {
        // 从原表格复制数据到批量编辑表格
        for (int i = 0; i < originalTableModel.getRowCount(); i++) {
            Object[] row = new Object[9];
            row[0] = false; // 未选择
            row[1] = originalTableModel.getValueAt(i, 0); // 学号
            row[2] = originalTableModel.getValueAt(i, 1); // 姓名
            row[3] = originalTableModel.getValueAt(i, 3); // 专业
            row[4] = originalTableModel.getValueAt(i, 5); // 当前分数
            row[5] = ""; // 新分数
            row[6] = originalTableModel.getValueAt(i, 6); // 当前GPA
            row[7] = ""; // 新GPA
            row[8] = "未修改"; // 状态

            batchTableModel.addRow(row);
        }
    }

    private void setupEventHandlers() {
        // 监听分数变化，自动更新状态
        batchTableModel.addTableModelListener(e -> {
            if (e.getColumn() == 5 || e.getColumn() == 7) { // 新分数或新GPA列
                int row = e.getFirstRow();
                updateRowStatus(row);
            }
        });
    }

    private void updateRowStatus(int row) {
        String newScore = String.valueOf(batchTableModel.getValueAt(row, 5));
        String newGPA = String.valueOf(batchTableModel.getValueAt(row, 7));

        if (!newScore.trim().isEmpty() || !newGPA.trim().isEmpty()) {
            batchTableModel.setValueAt("待提交", row, 8);
        } else {
            batchTableModel.setValueAt("未修改", row, 8);
        }
    }

    private void applyGradeToSelected(String gradeName) {
        Double[] gradeValues = GRADE_PRESETS.get(gradeName);
        if (gradeValues == null) return;

        int selectedCount = 0;
        for (int i = 0; i < batchTableModel.getRowCount(); i++) {
            Boolean selected = (Boolean) batchTableModel.getValueAt(i, 0);
            if (selected != null && selected) {
                batchTableModel.setValueAt(gradeValues[0].toString(), i, 5); // 分数
                batchTableModel.setValueAt(gradeValues[1].toString(), i, 7); // GPA
                selectedCount++;
            }
        }

        if (selectedCount > 0) {
            JOptionPane.showMessageDialog(this,
                String.format("已为 %d 名学生应用 %s 等级", selectedCount, gradeName.split(" ")[0]),
                "操作完成", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "请先选择要评分的学生", "提示", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void selectAllStudents() {
        for (int i = 0; i < batchTableModel.getRowCount(); i++) {
            batchTableModel.setValueAt(true, i, 0);
        }
    }

    private void clearAllScores() {
        int result = JOptionPane.showConfirmDialog(this,
            "确定要清空所有新录入的成绩吗？", "确认清空",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            for (int i = 0; i < batchTableModel.getRowCount(); i++) {
                batchTableModel.setValueAt("", i, 5); // 清空新分数
                batchTableModel.setValueAt("", i, 7); // 清空新GPA
                batchTableModel.setValueAt("未修改", i, 8); // 重置状态
            }
        }
    }

    private void previewChanges() {
        StringBuilder preview = new StringBuilder();
        preview.append("将要更新的成绩：\n\n");

        int changeCount = 0;
        for (int i = 0; i < batchTableModel.getRowCount(); i++) {
            String newScore = String.valueOf(batchTableModel.getValueAt(i, 5));
            String newGPA = String.valueOf(batchTableModel.getValueAt(i, 7));

            if (!newScore.trim().isEmpty() || !newGPA.trim().isEmpty()) {
                String studentId = String.valueOf(batchTableModel.getValueAt(i, 1));
                String studentName = String.valueOf(batchTableModel.getValueAt(i, 2));
                preview.append(String.format("%s (%s): 分数=%s, GPA=%s\n",
                    studentName, studentId, newScore, newGPA));
                changeCount++;
            }
        }

        if (changeCount == 0) {
            preview.append("暂无成绩更改");
        } else {
            preview.append(String.format("\n共 %d 名学生的成绩将被更新", changeCount));
        }

        JTextArea textArea = new JTextArea(preview.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "预览更改", JOptionPane.INFORMATION_MESSAGE);
    }

    private void confirmChanges() {
        // 验证和收集更改
        scoreChanges.clear();
        int changeCount = 0;

        for (int i = 0; i < batchTableModel.getRowCount(); i++) {
            String newScore = String.valueOf(batchTableModel.getValueAt(i, 5));
            String newGPA = String.valueOf(batchTableModel.getValueAt(i, 7));

            if (!newScore.trim().isEmpty() || !newGPA.trim().isEmpty()) {
                try {
                    double score = Double.parseDouble(newScore);
                    double gpa = Double.parseDouble(newGPA);

                    if (score < 0 || score > 100) {
                        JOptionPane.showMessageDialog(this,
                            String.format("第 %d 行分数超出范围 (0-100)", i + 1),
                            "输入错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (gpa < 0 || gpa > 4.8) {
                        JOptionPane.showMessageDialog(this,
                            String.format("第 %d 行GPA超出范围 (0-4.8)", i + 1),
                            "输入错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Map<String, Object> change = new HashMap<>();
                    change.put("studentId", batchTableModel.getValueAt(i, 1));
                    change.put("studentName", batchTableModel.getValueAt(i, 2));
                    change.put("score", score);
                    change.put("gpa", gpa);
                    scoreChanges.add(change);
                    changeCount++;

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this,
                        String.format("第 %d 行分数或GPA格式错误", i + 1),
                        "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        if (changeCount == 0) {
            JOptionPane.showMessageDialog(this, "没有检测到任何成绩更改", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
            String.format("确定要提交 %d 名学生的成绩更改吗？", changeCount),
            "确认提交", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            confirmed = true;
            dispose();
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public List<Map<String, Object>> getScoreChanges() {
        return scoreChanges;
    }

    // 状态列渲染器
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String status = value.toString();
            if ("待提交".equals(status)) {
                setForeground(new Color(255, 152, 0));
                setFont(getFont().deriveFont(Font.BOLD));
            } else if ("已提交".equals(status)) {
                setForeground(new Color(40, 167, 69));
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setForeground(Color.GRAY);
                setFont(getFont().deriveFont(Font.PLAIN));
            }

            return this;
        }
    }
}

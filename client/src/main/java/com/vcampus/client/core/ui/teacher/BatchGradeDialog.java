package com.vcampus.client.core.ui.teacher;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * 批量评分对话框
 * 允许教师为整个教学班的学生批量录入成绩
 */
@Slf4j
public class BatchGradeDialog extends JDialog {

    private final String courseName;
    private final DefaultTableModel studentsTableModel;
    private final NettyClient nettyClient;
    private final int currentSectionId;
    private boolean confirmed = false;

    private JTable gradeTable;
    private DefaultTableModel gradeTableModel;
    private JButton btnConfirm;
    private JButton btnCancel;
    private JButton btnImportTemplate;
    private JButton btnCalculateGPA;

    public BatchGradeDialog(Window parent, String courseName, DefaultTableModel studentsTableModel,
                           NettyClient nettyClient, int currentSectionId) {
        super(parent, "批量评分 - " + courseName, Dialog.ModalityType.APPLICATION_MODAL);

        this.courseName = courseName;
        this.studentsTableModel = studentsTableModel;
        this.nettyClient = nettyClient;
        this.currentSectionId = currentSectionId;

        initUI();
        loadStudentData();
        setupEventHandlers();

        setSize(800, 600);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setSize(800, 600);

        // 创建标题面板
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("批量评分 - " + courseName);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(25, 133, 57));
        titlePanel.add(titleLabel);

        add(titlePanel, BorderLayout.NORTH);

        // 创建中心面板 - 成绩表格
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // 创建批量评分表格
        String[] columns = {"学号", "姓名", "性别", "专业", "年级", "分数", "GPA"};
        gradeTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 只有分数和GPA列可编辑（第5列和第6列）
                return column == 5 || column == 6;
            }
        };

        gradeTable = new JTable(gradeTableModel);
        gradeTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        gradeTable.setRowHeight(30);
        gradeTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        gradeTable.getTableHeader().setBackground(new Color(240, 240, 240));

        // 设置列宽
        gradeTable.getColumnModel().getColumn(0).setPreferredWidth(100); // 学号
        gradeTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // 姓名
        gradeTable.getColumnModel().getColumn(2).setPreferredWidth(50);  // 性别
        gradeTable.getColumnModel().getColumn(3).setPreferredWidth(120); // 专业
        gradeTable.getColumnModel().getColumn(4).setPreferredWidth(60);  // 年级
        gradeTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // 分数
        gradeTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // GPA

        JScrollPane scrollPane = new JScrollPane(gradeTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("学生成绩批量录入"));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // 创建工具栏
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbarPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        btnCalculateGPA = new JButton("自动计算GPA");
        btnCalculateGPA.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnCalculateGPA.setBackground(new Color(255, 193, 7));
        btnCalculateGPA.setForeground(Color.BLACK);

        JButton btnClearAll = new JButton("清空所有");
        btnClearAll.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnClearAll.setBackground(new Color(220, 53, 69));
        btnClearAll.setForeground(Color.WHITE);
        btnClearAll.addActionListener(e -> clearAllGrades());

        JButton btnFillSample = new JButton("填充示例数据");
        btnFillSample.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        btnFillSample.setBackground(new Color(108, 117, 125));
        btnFillSample.setForeground(Color.WHITE);
        btnFillSample.addActionListener(e -> fillSampleData());

        toolbarPanel.add(btnCalculateGPA);
        toolbarPanel.add(btnClearAll);
        toolbarPanel.add(btnFillSample);

        centerPanel.add(toolbarPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        // 创建底部按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));

        btnConfirm = new JButton("确认保存");
        btnConfirm.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnConfirm.setBackground(new Color(25, 133, 57));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setPreferredSize(new Dimension(120, 35));

        btnCancel = new JButton("取消");
        btnCancel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        btnCancel.setBackground(new Color(108, 117, 125));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setPreferredSize(new Dimension(120, 35));

        buttonPanel.add(btnConfirm);
        buttonPanel.add(btnCancel);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadStudentData() {
        // 从原始表格模型加载学生数据
        gradeTableModel.setRowCount(0);

        for (int i = 0; i < studentsTableModel.getRowCount(); i++) {
            Object[] row = new Object[7];
            // 复制学生基本信息
            row[0] = studentsTableModel.getValueAt(i, 0); // 学号
            row[1] = studentsTableModel.getValueAt(i, 1); // 姓名
            row[2] = studentsTableModel.getValueAt(i, 2); // 性别
            row[3] = studentsTableModel.getValueAt(i, 3); // 专业
            row[4] = studentsTableModel.getValueAt(i, 4); // 年级
            // 注意：原始表格列顺序是 学号,姓名,性别,专业,年级,学分,分数,GPA,操作
            // 批量评分表格列顺序是 学号,姓名,性别,专业,年级,分数,GPA
            row[5] = studentsTableModel.getValueAt(i, 6); // 分数（原始表格第6列）
            row[6] = studentsTableModel.getValueAt(i, 7); // GPA（原始表格第7列）

            gradeTableModel.addRow(row);
        }

        log.info("加载了 {} 名学生的数据到批量评分表格", gradeTableModel.getRowCount());
    }

    private void setupEventHandlers() {
        btnConfirm.addActionListener(e -> {
            if (validateAndSaveGrades()) {
                confirmed = true;
                dispose();
            }
        });

        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        btnCalculateGPA.addActionListener(e -> calculateAllGPA());
    }

    private boolean validateAndSaveGrades() {
        try {
            List<Map<String, Object>> gradesToUpdate = new ArrayList<>();
            int updatedCount = 0;

            // 验证并收集需要更新的成绩数据
            for (int i = 0; i < gradeTableModel.getRowCount(); i++) {
                Object scoreObj = gradeTableModel.getValueAt(i, 5);
                Object gpaObj = gradeTableModel.getValueAt(i, 6);
                Object cardNumObj = gradeTableModel.getValueAt(i, 0);

                // 如果分数或GPA有值，进行验证
                if (scoreObj != null && !scoreObj.toString().trim().isEmpty()) {
                    try {
                        double score = Double.parseDouble(scoreObj.toString());
                        if (score < 0 || score > 100) {
                            JOptionPane.showMessageDialog(this,
                                "第 " + (i + 1) + " 行：分数必须在0-100之间",
                                "输入错误",
                                JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this,
                            "第 " + (i + 1) + " 行：分数必须是数字",
                            "输入错误",
                            JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }

                if (gpaObj != null && !gpaObj.toString().trim().isEmpty()) {
                    try {
                        double gpa = Double.parseDouble(gpaObj.toString());
                        if (gpa < 0 || gpa > 4.8) {
                            JOptionPane.showMessageDialog(this,
                                "第 " + (i + 1) + " 行：GPA必须在0-4.8之间",
                                "输入错误",
                                JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this,
                            "第 " + (i + 1) + " 行：GPA必须是数字",
                            "输入错误",
                            JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }

                // 如果这一行有更新，添加到更新列表
                if ((scoreObj != null && !scoreObj.toString().trim().isEmpty()) ||
                    (gpaObj != null && !gpaObj.toString().trim().isEmpty())) {

                    Map<String, Object> gradeUpdate = new HashMap<>();
                    gradeUpdate.put("cardNum", cardNumObj.toString());
                    gradeUpdate.put("score", scoreObj != null ? scoreObj.toString() : "");
                    gradeUpdate.put("gpa", gpaObj != null ? gpaObj.toString() : "");
                    gradesToUpdate.add(gradeUpdate);
                    updatedCount++;

                    // 更新原始表格模型 - 注意原始表格的列索引
                    // 原始表格：学号,姓名,性别,专业,年级,学分,分数,GPA,操作
                    studentsTableModel.setValueAt(scoreObj, i, 6); // 分数列（原始表格第6列）
                    studentsTableModel.setValueAt(gpaObj, i, 7);   // GPA列（原始表格第7列）
                }
            }

            if (updatedCount == 0) {
                int result = JOptionPane.showConfirmDialog(this,
                    "没有发现任何成绩数据，确定要保存吗？",
                    "确认",
                    JOptionPane.YES_NO_OPTION);
                return result == JOptionPane.YES_OPTION;
            }

            // 实际保存到数据库
            return saveGradesToDatabase(gradesToUpdate, updatedCount);

        } catch (Exception e) {
            log.error("批量保存成绩时发生错误", e);
            JOptionPane.showMessageDialog(this,
                "保存成绩时发生错误：" + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * 实际保存成绩到数据库
     */
    private boolean saveGradesToDatabase(List<Map<String, Object>> gradesToUpdate, int updatedCount) {
        try {
            // 显示进度对话框
            JDialog progressDialog = new JDialog(this, "正在保存成绩...", true);
            JProgressBar progressBar = new JProgressBar(0, updatedCount);
            progressBar.setStringPainted(true);
            progressBar.setString("正在保存成绩... 0/" + updatedCount);

            JPanel progressPanel = new JPanel(new BorderLayout());
            progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            progressPanel.add(new JLabel("正在批量保存成绩，请稍候..."), BorderLayout.NORTH);
            progressPanel.add(progressBar, BorderLayout.CENTER);

            progressDialog.add(progressPanel);
            progressDialog.setSize(400, 150);
            progressDialog.setLocationRelativeTo(this);

            // 在后台线程中执行保存操作
            SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    int successCount = 0;
                    int failCount = 0;

                    for (int i = 0; i < gradesToUpdate.size(); i++) {
                        Map<String, Object> gradeData = gradesToUpdate.get(i);

                        try {
                            String cardNum = gradeData.get("cardNum").toString();
                            String scoreStr = gradeData.get("score").toString();
                            String gpaStr = gradeData.get("gpa").toString();

                            if (!scoreStr.isEmpty() && !gpaStr.isEmpty()) {
                                double score = Double.parseDouble(scoreStr);
                                double gpa = Double.parseDouble(gpaStr);

                                // 发送更新请求
                                Request request = new Request("academic/course")
                                        .addParam("action", "UPDATE_STUDENT_SCORE")
                                        .addParam("cardNum", cardNum)
                                        .addParam("sectionId", String.valueOf(currentSectionId))
                                        .addParam("score", String.valueOf(score))
                                        .addParam("gpa", String.valueOf(gpa));

                                Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);

                                if (response != null && response.isSuccess()) {
                                    successCount++;
                                    log.debug("学生 {} 成绩更新成功", cardNum);
                                } else {
                                    failCount++;
                                    log.warn("学生 {} 成绩更新失败: {}", cardNum,
                                           response != null ? response.getMessage() : "未知错误");
                                }
                            }
                        } catch (Exception e) {
                            failCount++;
                            log.error("更新学生成绩时发生错误", e);
                        }

                        // 更新进度
                        publish(i + 1);

                        // 添加小延迟避免过快请求
                        Thread.sleep(100);
                    }

                    // 记录最终结果
                    log.info("批量评分完成: 成功 {} 条，失败 {} 条", successCount, failCount);

                    return successCount > 0; // 只要有成功的就返回true
                }

                @Override
                protected void process(List<Integer> chunks) {
                    for (Integer progress : chunks) {
                        progressBar.setValue(progress);
                        progressBar.setString("正在保存成绩... " + progress + "/" + updatedCount);
                    }
                }

                @Override
                protected void done() {
                    progressDialog.dispose();

                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(BatchGradeDialog.this,
                                "批量评分完成！\n共处理了 " + updatedCount + " 名学生的成绩。\n请查看日志了解详细结果。",
                                "保存完成",
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(BatchGradeDialog.this,
                                "批量评分过程中出现错误，部分成绩可能未保存成功。\n请检查网络连接并重试。",
                                "保存失败",
                                JOptionPane.WARNING_MESSAGE);
                        }
                    } catch (Exception e) {
                        log.error("获取保存结果时发生错误", e);
                        JOptionPane.showMessageDialog(BatchGradeDialog.this,
                            "保存过程中发生错误：" + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            };

            worker.execute();
            progressDialog.setVisible(true);

            return true; // 异步操作，这里先返回true

        } catch (Exception e) {
            log.error("启动批量保存操作时发生错误", e);
            JOptionPane.showMessageDialog(this,
                "启动保存操作时发生错误：" + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void calculateAllGPA() {
        int calculatedCount = 0;

        for (int i = 0; i < gradeTableModel.getRowCount(); i++) {
            Object scoreObj = gradeTableModel.getValueAt(i, 5);

            if (scoreObj != null && !scoreObj.toString().trim().isEmpty()) {
                try {
                    double score = Double.parseDouble(scoreObj.toString());
                    double gpa = calculateGPAFromScore(score);
                    gradeTableModel.setValueAt(String.format("%.2f", gpa), i, 6);
                    calculatedCount++;
                } catch (NumberFormatException e) {
                    // 忽略无效的分数
                }
            }
        }

        if (calculatedCount > 0) {
            JOptionPane.showMessageDialog(this,
                "已为 " + calculatedCount + " 名学生自动计算GPA",
                "计算完成",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "没有找到有效的分数数据，无法计算GPA",
                "提示",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private double calculateGPAFromScore(double score) {
        // 根据新的GPA计算标准：96=4.8, 93=4.5, 90=4.0, 80=3.0
        if (score >= 96) return 4.8;
        else if (score >= 93) return 4.5;
        else if (score >= 90) return 4.0;
        else if (score >= 86) return 3.8;
        else if (score >= 83) return 3.5;
        else if (score >= 80) return 3.0;
        else if (score >= 76) return 2.8;
        else if (score >= 73) return 2.5;
        else if (score >= 70) return 2.0;
        else if (score >= 66) return 1.8;
        else if (score >= 63) return 1.5;
        else if (score >= 60) return 1.0;
        else return 0.0;
    }

    private void clearAllGrades() {
        int result = JOptionPane.showConfirmDialog(this,
            "确定要清空所有成绩数据吗？此操作不可撤销。",
            "确认清空",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            for (int i = 0; i < gradeTableModel.getRowCount(); i++) {
                gradeTableModel.setValueAt("", i, 5); // 分数列
                gradeTableModel.setValueAt("", i, 6); // GPA列
            }

            JOptionPane.showMessageDialog(this,
                "所有成绩数据已清空",
                "清空完成",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void fillSampleData() {
        int result = JOptionPane.showConfirmDialog(this,
            "确定要填充示例数据吗？这将覆盖现有数据。",
            "确认填充",
            JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            // 为前几个学生填充示例数据
            int count = Math.min(gradeTableModel.getRowCount(), 5);
            double[] sampleScores = {88.5, 92.0, 76.5, 85.0, 79.5};

            for (int i = 0; i < count; i++) {
                double score = sampleScores[i];
                double gpa = calculateGPAFromScore(score);

                gradeTableModel.setValueAt(String.format("%.1f", score), i, 5);
                gradeTableModel.setValueAt(String.format("%.2f", gpa), i, 6);
            }

            JOptionPane.showMessageDialog(this,
                "已为前 " + count + " 名学生填充示例数据",
                "填充完成",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}

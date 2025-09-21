package com.vcampus.client.core.ui.teacher;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Map;

/**
 * 课程详情展示面板
 * 显示课程的详细信息，包括时间安排、学生列表等
 */
public class CourseDetailPanel extends JPanel {

    private Map<String, Object> courseData;

    public CourseDetailPanel(Map<String, Object> courseData) {
        this.courseData = courseData;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 创建课程基本信息面板
        JPanel basicInfoPanel = createBasicInfoPanel();
        add(basicInfoPanel, BorderLayout.NORTH);

        // 创建课程时间安排面板
        JPanel schedulePanel = createSchedulePanel();
        add(schedulePanel, BorderLayout.CENTER);

        // 创建统计信息面板
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.SOUTH);
    }

    private JPanel createBasicInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "课程基本信息",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // 课程名称
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(createInfoLabel("课程名称："), gbc);
        gbc.gridx = 1;
        panel.add(createValueLabel(getString("courseName")), gbc);

        // 学期
        gbc.gridx = 2; gbc.gridy = 0;
        panel.add(createInfoLabel("学期："), gbc);
        gbc.gridx = 3;
        panel.add(createValueLabel(getString("term")), gbc);

        // 教室
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(createInfoLabel("教室："), gbc);
        gbc.gridx = 1;
        panel.add(createValueLabel(getString("room")), gbc);

        // 容量
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(createInfoLabel("容量："), gbc);
        gbc.gridx = 3;
        panel.add(createValueLabel(getString("capacity") + " 人"), gbc);

        return panel;
    }

    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "课程时间安排",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 14)
        ));

        // 创建时间显示组件
        String schedule = getString("schedule");
        JTextArea scheduleArea = new JTextArea(3, 40);
        scheduleArea.setText(formatSchedule(schedule));
        scheduleArea.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        scheduleArea.setBackground(new Color(248, 249, 250));
        scheduleArea.setEditable(false);
        scheduleArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(scheduleArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "统计信息",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 14)
        ));

        // 已报名人数
        int enrolled = getInt("enrolledCount");
        int capacity = getInt("capacity");
        double percentage = capacity > 0 ? (double) enrolled / capacity * 100 : 0;

        JLabel enrolledLabel = new JLabel(String.format("已报名：%d 人", enrolled));
        enrolledLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        JLabel capacityLabel = new JLabel(String.format("总容量：%d 人", capacity));
        capacityLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        JLabel percentageLabel = new JLabel(String.format("报名率：%.1f%%", percentage));
        percentageLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        percentageLabel.setForeground(percentage > 80 ? new Color(220, 53, 69) :
                                     percentage > 60 ? new Color(255, 193, 7) :
                                     new Color(40, 167, 69));

        panel.add(enrolledLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(capacityLabel);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(percentageLabel);

        return panel;
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.BOLD, 12));
        label.setForeground(new Color(73, 80, 87));
        return label;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        label.setForeground(new Color(33, 37, 41));
        return label;
    }

    private String formatSchedule(String schedule) {
        if (schedule == null || schedule.trim().isEmpty()) {
            return "暂无时间安排";
        }

        // 格式化时间安排显示
        StringBuilder formatted = new StringBuilder();
        formatted.append("📅 上课时间：").append(schedule).append("\n\n");

        // 解析时间安排（这里可以根据实际的时间格式进行解析）
        if (schedule.contains("周")) {
            formatted.append("💡 提示：请确保学生了解具体的上课时间和地点");
        }

        return formatted.toString();
    }

    private String getString(String key) {
        Object value = courseData.get(key);
        return value != null ? value.toString() : "";
    }

    private int getInt(String key) {
        Object value = courseData.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 更新课程数据
     */
    public void updateCourseData(Map<String, Object> newData) {
        this.courseData = newData;
        removeAll();
        initComponents();
        revalidate();
        repaint();
    }
}

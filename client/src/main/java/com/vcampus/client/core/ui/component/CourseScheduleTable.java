package com.vcampus.client.core.ui.component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 课程表组件 - 传统网格样式的课程表
 * 支持拖拽、点击选择课程等功能
 *
 * @author VCampus Team
 * @version 1.0
 */
public class CourseScheduleTable extends JPanel {

    // 常量定义
    private static final String[] WEEKDAYS = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    private static final String[] TIME_SLOTS = {
        "第1节\n08:00-08:45",
        "第2节\n08:55-09:40",
        "第3节\n10:00-10:45",
        "第4节\n10:55-11:40",
        "第5节\n14:00-14:45",
        "第6节\n14:55-15:40",
        "第7节\n16:00-16:45",
        "第8节\n16:55-17:40",
        "第9节\n19:00-19:45",
        "第10节\n19:55-20:40",
        "第11节\n20:50-21:35",
        "第12节\n21:45-22:30",
        "第13节\n22:40-23:25"
    };

    // 界面组件
    private JPanel headerPanel;
    private JPanel timePanel;
    private JPanel courseGridPanel;
    private JScrollPane scrollPane;

     // 数据存储
     private Map<String, CourseBlock> courseBlocks = new HashMap<>();
     
     // 课程颜色分配映射（课程名称 -> 颜色索引）
     private Map<String, Integer> courseColorMap = new HashMap<>();

    // 事件监听器
    private CourseSelectionListener courseSelectionListener;

    // 样式常量
    private static final Color HEADER_COLOR = new Color(245, 245, 245);
    private static final Color TIME_COLOR = new Color(250, 250, 250);
    private static final Color GRID_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color HEADER_TIME_BLUE = new Color(220, 235, 250); // 统一的浅蓝色

    // 星期标题的颜色 - 统一为浅蓝色
    private static final Color[] WEEKDAY_COLORS = {
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE
    };

    // 时间节次的颜色 - 统一为浅蓝色
    private static final Color[] TIME_SLOT_COLORS = {
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE,
            HEADER_TIME_BLUE
    };

    // 四种不同的课程颜色，用于区分相邻块
    private static final Color[] COURSE_COLORS = {
        new Color(173, 216, 230),  // 浅蓝色
        new Color(255, 182, 193),  // 浅粉色
        new Color(144, 238, 144),  // 浅绿色
        new Color(255, 218, 185)   // 浅橙色
    };
    
    // 选中状态的颜色（比原色更深一些）
    private static final Color[] SELECTED_COURSE_COLORS = {
        new Color(135, 206, 250),  // 深蓝色
        new Color(255, 105, 180),  // 深粉色
        new Color(50, 205, 50),    // 深绿色
        new Color(255, 165, 0)     // 深橙色
    };

    public CourseScheduleTable() {
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 创建头部面板（星期）
        headerPanel = createHeaderPanel();

        // 创建时间面板（节次和时间）
        timePanel = createTimePanel();

        // 创建课程网格面板
        courseGridPanel = createCourseGridPanel();

        // 创建主内容面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 组合时间面板和课程网格
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(timePanel, BorderLayout.WEST);
        contentPanel.add(courseGridPanel, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // 添加滚动面板
        scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerContainer = new JPanel(new BorderLayout());
        headerContainer.setBackground(HEADER_COLOR);
        headerContainer.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        headerContainer.setPreferredSize(new Dimension(0, 50));

        // 左侧空白区域，与时间面板宽度一致
        JPanel timeHeaderSpace = new JPanel();
        timeHeaderSpace.setBackground(HEADER_COLOR);
        timeHeaderSpace.setPreferredSize(new Dimension(120, 50));
        timeHeaderSpace.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        headerContainer.add(timeHeaderSpace, BorderLayout.WEST);

        // 右侧星期标题区域
        JPanel weekdayPanel = new JPanel(new GridLayout(1, 7));
        weekdayPanel.setBackground(HEADER_COLOR);

        for (int i = 0; i < WEEKDAYS.length; i++) {
            String weekday = WEEKDAYS[i];
            Color gradientColor = WEEKDAY_COLORS[i];

            JLabel label = new JLabel(weekday, SwingConstants.CENTER);
            label.setFont(new Font("微软雅黑", Font.BOLD, 14));
            label.setForeground(new Color(50, 50, 50)); // 设置深色文字以确保可读性
            label.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
            label.setOpaque(true);
            label.setBackground(gradientColor);
            weekdayPanel.add(label);
        }

        headerContainer.add(weekdayPanel, BorderLayout.CENTER);
        return headerContainer;
    }

    private JPanel createTimePanel() {
        JPanel panel = new JPanel(new GridLayout(13, 1));
        panel.setBackground(TIME_COLOR);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        panel.setPreferredSize(new Dimension(120, 0));

        for (int i = 0; i < TIME_SLOTS.length; i++) {
            String timeSlot = TIME_SLOTS[i];
            Color gradientColor = TIME_SLOT_COLORS[i];

            JLabel label = new JLabel("<html><div style='text-align: center;'>" +
                timeSlot.replace("\n", "<br>") + "</div></html>", SwingConstants.CENTER);
            label.setFont(new Font("微软雅黑", Font.PLAIN, 11));
            label.setForeground(new Color(50, 50, 50)); // 设置深色文字以确保可读性
            label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
            label.setOpaque(true);
            label.setBackground(gradientColor);
            label.setPreferredSize(new Dimension(120, 60));
            panel.add(label);
        }

        return panel;
    }

    private JPanel createCourseGridPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(13, 7));
        panel.setBackground(GRID_COLOR);

        // 创建13行7列的网格
        for (int period = 1; period <= 13; period++) {
            for (int day = 1; day <= 7; day++) {
                CourseCell cell = new CourseCell(day, period);
                panel.add(cell);
            }
        }

        return panel;
    }

    private void setupLayout() {
        setPreferredSize(new Dimension(900, 600));
    }

    private void setupEventHandlers() {
        // 可以在这里添加全局事件处理器
    }

    /**
     * 更新课程块的悬停状态
     * @param block 要更新的课程块
     * @param isHovered 是否悬停
     */
    private void updateCourseBlockHover(CourseBlock block, boolean isHovered) {
        if (block == null) return;

        Component[] components = courseGridPanel.getComponents();
        for (int period = block.timeSlot.startPeriod; period <= block.timeSlot.endPeriod; period++) {
            int index = (period - 1) * 7 + (block.timeSlot.dayOfWeek - 1);
            if (index >= 0 && index < components.length) {
                CourseCell cell = (CourseCell) components[index];
                cell.setHoverState(isHovered);
            }
        }
    }

     /**
      * 设置课程数据
      */
     public void setCourseData(List<Map<String, Object>> courses) {
         clearAllCourses();

        if (courses != null) {
            // 首先收集所有课程的时间段信息
            assignOptimalColors(courses);
            
            // 然后显示课程
            for (Map<String, Object> course : courses) {
                addCourseToGrid(course);
            }
        }

        repaint();
    }
    
    /**
     * 优化的颜色分配算法，基于图着色理论
     */
    private void assignOptimalColors(List<Map<String, Object>> courses) {
        // 清空之前的颜色分配
        courseColorMap.clear();
        
        // 按课程名称对课程进行分组
        Map<String, List<TimeSlot>> courseTimeSlots = new HashMap<>();
        
        for (Map<String, Object> course : courses) {
            String courseName = (String) course.get("courseName");
            String schedule = (String) course.get("Schedule");
            if (schedule != null && !schedule.trim().isEmpty()) {
                List<TimeSlot> timeSlots = parseSchedule(schedule);
                courseTimeSlots.put(courseName, timeSlots);
            }
        }
        
        int courseCount = courseTimeSlots.size();
        
        if (courseCount <= 4) {
            // 不超过四个课程时，每个课程使用不同颜色
            assignUniqueColors(courseTimeSlots);
        } else {
            // 超过四个课程时，确保四种颜色都使用且相邻不同色
            assignColorsWithAllUsed(courseTimeSlots);
        }
    }
    
    /**
     * 为不超过四个课程分配不同颜色
     */
    private void assignUniqueColors(Map<String, List<TimeSlot>> courseTimeSlots) {
        int colorIndex = 0;
        for (String courseName : courseTimeSlots.keySet()) {
            courseColorMap.put(courseName, colorIndex);
            colorIndex++;
        }
    }
    
    /**
     * 为超过四个课程分配颜色，确保四种颜色都使用且相邻不同色
     */
    private void assignColorsWithAllUsed(Map<String, List<TimeSlot>> courseTimeSlots) {
        List<String> courseNames = new ArrayList<>(courseTimeSlots.keySet());
        
        // 按课程的时间冲突程度排序，冲突越多的越先分配
        courseNames.sort((c1, c2) -> {
            int conflicts1 = calculateConflicts(c1, courseTimeSlots);
            int conflicts2 = calculateConflicts(c2, courseTimeSlots);
            return Integer.compare(conflicts2, conflicts1); // 降序
        });
        
        // 首先保证四种颜色都被使用
        for (int i = 0; i < Math.min(4, courseNames.size()); i++) {
            courseColorMap.put(courseNames.get(i), i);
        }
        
        // 为剩余课程分配颜色，确保相邻不同色
        for (int i = 4; i < courseNames.size(); i++) {
            String courseName = courseNames.get(i);
            int bestColor = findBestColorForCourse(courseName, courseTimeSlots);
            courseColorMap.put(courseName, bestColor);
        }
    }
    
    /**
     * 计算课程与其他课程的冲突数量
     */
    private int calculateConflicts(String courseName, Map<String, List<TimeSlot>> courseTimeSlots) {
        List<TimeSlot> currentSlots = courseTimeSlots.get(courseName);
        int conflicts = 0;
        
        for (Map.Entry<String, List<TimeSlot>> entry : courseTimeSlots.entrySet()) {
            if (!entry.getKey().equals(courseName)) {
                if (hasAdjacentOrOverlapSlots(currentSlots, entry.getValue())) {
                    conflicts++;
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * 为指定课程找到最优颜色
     */
    private int findBestColorForCourse(String courseName, Map<String, List<TimeSlot>> courseTimeSlots) {
        List<TimeSlot> currentSlots = courseTimeSlots.get(courseName);
        boolean[] usedColors = new boolean[COURSE_COLORS.length];
        
        // 检查与已分配课程的冲突
        for (Map.Entry<String, Integer> entry : courseColorMap.entrySet()) {
            String otherCourseName = entry.getKey();
            int otherColor = entry.getValue();
            List<TimeSlot> otherSlots = courseTimeSlots.get(otherCourseName);
            
            // 检查是否有相邻或重叠的时间段
            if (hasAdjacentOrOverlapSlots(currentSlots, otherSlots)) {
                usedColors[otherColor] = true;
            }
        }
        
        // 返回第一个可用的颜色
        for (int i = 0; i < COURSE_COLORS.length; i++) {
            if (!usedColors[i]) {
                return i;
            }
        }
        
        // 如果所有颜色都被使用，返回使用最少的颜色
        return findLeastUsedColor();
    }
    
    /**
     * 检查两组时间段是否有相邻或重叠
     */
    private boolean hasAdjacentOrOverlapSlots(List<TimeSlot> slots1, List<TimeSlot> slots2) {
        for (TimeSlot slot1 : slots1) {
            for (TimeSlot slot2 : slots2) {
                if (isAdjacentOrOverlap(slot1, slot2)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 检查两个时间段是否相邻或重叠
     */
    private boolean isAdjacentOrOverlap(TimeSlot slot1, TimeSlot slot2) {
        // 同一天的相邻节次
        if (slot1.dayOfWeek == slot2.dayOfWeek) {
            // 检查重叠
            if (!(slot1.endPeriod < slot2.startPeriod || slot2.endPeriod < slot1.startPeriod)) {
                return true;
            }
            // 检查相邻（紧挨着的节次）
            if (slot1.endPeriod + 1 == slot2.startPeriod || slot2.endPeriod + 1 == slot1.startPeriod) {
                return true;
            }
        }
        
        // 相邻天的同一节次
        if (Math.abs(slot1.dayOfWeek - slot2.dayOfWeek) == 1) {
            // 检查是否有重叠的节次
            if (!(slot1.endPeriod < slot2.startPeriod || slot2.endPeriod < slot1.startPeriod)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 找到使用次数最少的颜色
     */
    private int findLeastUsedColor() {
        int[] colorCount = new int[COURSE_COLORS.length];
        for (int color : courseColorMap.values()) {
            colorCount[color]++;
        }
        
        int minCount = Integer.MAX_VALUE;
        int leastUsedColor = 0;
        for (int i = 0; i < COURSE_COLORS.length; i++) {
            if (colorCount[i] < minCount) {
                minCount = colorCount[i];
                leastUsedColor = i;
            }
        }
        
        return leastUsedColor;
    }

    /**
     * 清除所有课程显示
     */
    private void clearAllCourses() {
        courseBlocks.clear();
        courseColorMap.clear();
        Component[] components = courseGridPanel.getComponents();
        for (Component comp : components) {
            if (comp instanceof CourseCell) {
                ((CourseCell) comp).clearCourse();
            }
        }
    }

    /**
     * 将课程添加到网格中
     */
    private void addCourseToGrid(Map<String, Object> course) {
        String schedule = (String) course.get("Schedule");
        if (schedule == null || schedule.trim().isEmpty()) {
            return;
        }

        String courseName = (String) course.get("courseName");
        int colorIndex = courseColorMap.getOrDefault(courseName, 0);

        // 解析时间安排
        List<TimeSlot> timeSlots = parseSchedule(schedule);

        for (TimeSlot slot : timeSlots) {
            CourseBlock block = new CourseBlock(course, slot, colorIndex);
            String key = slot.dayOfWeek + "-" + slot.startPeriod + "-" + slot.endPeriod;
            courseBlocks.put(key, block);

            // 在网格中显示课程
            displayCourseBlock(block);
        }
    }
    
    

    /**
     * 在网格中显示课程块
     */
    private void displayCourseBlock(CourseBlock block) {
        Component[] components = courseGridPanel.getComponents();

        for (int period = block.timeSlot.startPeriod; period <= block.timeSlot.endPeriod; period++) {
            int index = (period - 1) * 7 + (block.timeSlot.dayOfWeek - 1);
            if (index >= 0 && index < components.length) {
                CourseCell cell = (CourseCell) components[index];
                boolean isMainCell = (period == block.timeSlot.startPeriod);
                boolean isFirst = (period == block.timeSlot.startPeriod);
                boolean isLast = (period == block.timeSlot.endPeriod);
                cell.setCourse(block, isMainCell, isFirst, isLast);
            }
        }
    }

    /**
     * 解析时间安排字符串
     */
    private List<TimeSlot> parseSchedule(String schedule) {
        java.util.List<TimeSlot> slots = new java.util.ArrayList<>();

        // 支持格式：周一 1-2节, 周三 5-6节
        Pattern pattern = Pattern.compile("(周[一二三四五六日])\\s*(?:第?)?(\\d+)-(\\d+)节?");
        Matcher matcher = pattern.matcher(schedule);

        while (matcher.find()) {
            String dayStr = matcher.group(1);
            int startPeriod = Integer.parseInt(matcher.group(2));
            int endPeriod = Integer.parseInt(matcher.group(3));

            int dayOfWeek = getDayOfWeek(dayStr);
            if (dayOfWeek != -1) {
                slots.add(new TimeSlot(dayOfWeek, startPeriod, endPeriod));
            }
        }

        return slots;
    }

    /**
     * 获取星期几的数字表示
     */
    private int getDayOfWeek(String dayStr) {
        switch (dayStr) {
            case "周一": return 1;
            case "周二": return 2;
            case "周三": return 3;
            case "周四": return 4;
            case "周五": return 5;
            case "周六": return 6;
            case "周日": return 7;
            default: return -1;
        }
    }

    /**
     * 设置课程选择监听器
     */
    public void setCourseSelectionListener(CourseSelectionListener listener) {
        this.courseSelectionListener = listener;
    }

     /**
      * 圆角面板类
      */
    private class RoundedPanel extends JPanel {
        private int cornerRadius;
        private boolean roundTopLeft, roundTopRight, roundBottomLeft, roundBottomRight;

        public RoundedPanel(int radius) {
            this(radius, true, true, true, true);
        }

        public RoundedPanel(int radius, boolean roundTop, boolean roundBottom) {
            this(radius, roundTop, roundTop, roundBottom, roundBottom);
        }

        public RoundedPanel(int radius, boolean roundTopLeft, boolean roundTopRight, boolean roundBottomLeft, boolean roundBottomRight) {
            super();
            this.cornerRadius = radius;
            this.roundTopLeft = roundTopLeft;
            this.roundTopRight = roundTopRight;
            this.roundBottomLeft = roundBottomLeft;
            this.roundBottomRight = roundBottomRight;
            setOpaque(false); // 对自定义绘制很重要
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int width = getWidth();
            int height = getHeight();
            Graphics2D graphics = (Graphics2D) g;
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 使用指定的颜色绘制
            graphics.setColor(getBackground());

            // 创建一个可以自定义各个角的圆角矩形
            int r = cornerRadius;
            java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();

            // 从左上角开始
            path.moveTo(r, 0);

            // 顶部
            path.lineTo(width - r, 0);
            // 右上角
            if (roundTopRight) {
                path.quadTo(width, 0, width, r);
            } else {
                path.lineTo(width, 0);
                path.lineTo(width, r);
            }

            // 右侧
            path.lineTo(width, height - r);
            // 右下角
            if (roundBottomRight) {
                path.quadTo(width, height, width - r, height);
            } else {
                path.lineTo(width, height);
                path.lineTo(width - r, height);
            }

            // 底部
            path.lineTo(r, height);
            // 左下角
            if (roundBottomLeft) {
                path.quadTo(0, height, 0, height - r);
            } else {
                path.lineTo(0, height);
                path.lineTo(0, height - r);
            }

            // 左侧
            path.lineTo(0, r);
            // 左上角
            if (roundTopLeft) {
                path.quadTo(0, 0, r, 0);
            } else {
                path.lineTo(0, 0);
                path.lineTo(r, 0);
            }

            path.closePath();

            graphics.fill(path);
        }
    }


     /**
      * 课程单元格类
      */
     private class CourseCell extends JPanel {
         private CourseBlock courseBlock;

        public CourseCell(int dayOfWeek, int period) {

            setLayout(new BorderLayout());
            setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
            setBackground(GRID_COLOR);
            setPreferredSize(new Dimension(100, 60));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (courseBlock != null && courseSelectionListener != null) {
                        courseSelectionListener.onCourseSelected(courseBlock.courseData);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (courseBlock != null) {
                        updateCourseBlockHover(courseBlock, true);
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (courseBlock != null) {
                        updateCourseBlockHover(courseBlock, false);
                    }
                }
            });
        }

        public void setHoverState(boolean isHovered) {
            if (courseBlock == null) return;

            Component[] components = getComponents();
            if (components.length > 0 && components[0] instanceof JPanel) {
                JPanel contentPanel = (JPanel) components[0];
                if (isHovered) {
                    contentPanel.setBackground(SELECTED_COURSE_COLORS[courseBlock.colorIndex]);
                } else {
                    contentPanel.setBackground(COURSE_COLORS[courseBlock.colorIndex]);
                }
                repaint();
            }
        }

        public void setCourse(CourseBlock block, boolean isMainCell, boolean isFirst, boolean isLast) {
            this.courseBlock = block;

            setBackground(GRID_COLOR); // 设置背景为网格颜色，作为间隙
            removeAll();

            // 根据是否为第一节或最后一节，决定是否渲染圆角
            boolean isSingleCell = isFirst && isLast;
            RoundedPanel courseContentPanel;
            if (isSingleCell) {
                // 单节课，所有角都是圆角
                courseContentPanel = new RoundedPanel(15);
            } else if (isFirst) {
                // 多节课的第一节，只有顶部是圆角
                courseContentPanel = new RoundedPanel(15, true, false);
            } else if (isLast) {
                // 多节课的最后一节，只有底部是圆角
                courseContentPanel = new RoundedPanel(15, false, true);
            } else {
                // 中间课程，没有圆角
                courseContentPanel = new RoundedPanel(0);
            }

            courseContentPanel.setLayout(new BorderLayout());
            courseContentPanel.setBackground(COURSE_COLORS[block.colorIndex]);
            
            // 设置间隙：上下左右各留出2像素的空隙
            int margin = 2;
            int top = isFirst ? margin : 0;    // 只有第一节才有上边距
            int bottom = isLast ? margin : 0;   // 只有最后一节才有下边距
            int left = margin;
            int right = margin;
            
            setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
            add(courseContentPanel, BorderLayout.CENTER);

            if (isMainCell) {
                // 主单元格显示课程信息
                String courseName = (String) block.courseData.get("courseName");
                String teacher = (String) block.courseData.get("teacher");
                String room = (String) block.courseData.get("Room");

                JLabel nameLabel = new JLabel(courseName, SwingConstants.CENTER);
                nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));

                JLabel detailLabel = new JLabel("<html><div style='text-align: center;'>" +
                    (teacher != null && !teacher.isEmpty() ? teacher : "") +
                    (room != null && !room.isEmpty() ? "<br>" + room : "") +
                    "</div></html>", SwingConstants.CENTER);
                detailLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));

                courseContentPanel.add(nameLabel, BorderLayout.CENTER);
                courseContentPanel.add(detailLabel, BorderLayout.SOUTH);
            }

            revalidate();
            repaint();
        }

        public void clearCourse() {
            this.courseBlock = null;
            setBackground(GRID_COLOR);
            removeAll();
            setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
            revalidate();
            repaint();
        }
    }

    /**
     * 时间段类
     */
    private static class TimeSlot {
        final int dayOfWeek;
        final int startPeriod;
        final int endPeriod;

        public TimeSlot(int dayOfWeek, int startPeriod, int endPeriod) {
            this.dayOfWeek = dayOfWeek;
            this.startPeriod = startPeriod;
            this.endPeriod = endPeriod;
        }
    }

    /**
     * 课程块类
     */
    private static class CourseBlock {
        final Map<String, Object> courseData;
        final TimeSlot timeSlot;
        final int colorIndex;

        public CourseBlock(Map<String, Object> courseData, TimeSlot timeSlot, int colorIndex) {
            this.courseData = courseData;
            this.timeSlot = timeSlot;
            this.colorIndex = colorIndex;
        }
    }

    /**
     * 课程选择监听器接口
     */
    public interface CourseSelectionListener {
        void onCourseSelected(Map<String, Object> courseData);
    }
}

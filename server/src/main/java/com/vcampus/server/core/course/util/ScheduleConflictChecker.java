package com.vcampus.server.core.course.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 课程时间冲突检测工具类
 * 支持多种时间格式：
 * - "周一 1-2节"
 * - "周二 3-4节, 周四 5-6节"
 * - "Monday 08:00-09:50"
 * - "周三 第1-2节"
 */
public class ScheduleConflictChecker {

    // 中文星期映射
    private static final String[] CHINESE_WEEKDAYS = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
    private static final String[] ENGLISH_WEEKDAYS = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

    /**
     * 检查两个时间安排是否冲突
     */
    public static boolean hasConflict(String schedule1, String schedule2) {
        if (schedule1 == null || schedule2 == null ||
            schedule1.trim().isEmpty() || schedule2.trim().isEmpty()) {
            return false;
        }

        List<TimeSlot> slots1 = parseSchedule(schedule1);
        List<TimeSlot> slots2 = parseSchedule(schedule2);

        // 检查任意两个时间段是否冲突
        for (TimeSlot slot1 : slots1) {
            for (TimeSlot slot2 : slots2) {
                if (slot1.conflictsWith(slot2)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 解析时间安排字符串，返回时间段列表
     */
    private static List<TimeSlot> parseSchedule(String schedule) {
        List<TimeSlot> timeSlots = new ArrayList<>();

        // 按逗号分割多个时间段
        String[] parts = schedule.split("[,，]");

        for (String part : parts) {
            part = part.trim();
            TimeSlot slot = parseTimeSlot(part);
            if (slot != null) {
                timeSlots.add(slot);
            }
        }

        return timeSlots;
    }

    /**
     * 解析单个时间段
     */
    private static TimeSlot parseTimeSlot(String timeSlotStr) {
        timeSlotStr = timeSlotStr.trim();

        // 格式1: "周一 1-2节" 或 "周一 第1-2节"
        Pattern pattern1 = Pattern.compile("(周[一二三四五六日])\\s*(?:第?)?(\\d+)-(\\d+)节?");
        Matcher matcher1 = pattern1.matcher(timeSlotStr);
        if (matcher1.find()) {
            String weekday = matcher1.group(1);
            int startPeriod = Integer.parseInt(matcher1.group(2));
            int endPeriod = Integer.parseInt(matcher1.group(3));

            int dayOfWeek = getDayOfWeek(weekday);
            if (dayOfWeek != -1) {
                return new TimeSlot(dayOfWeek, startPeriod, endPeriod, true);
            }
        }

        // 格式2: "Monday 08:00-09:50"
        Pattern pattern2 = Pattern.compile("(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)\\s+(\\d{2}):(\\d{2})-(\\d{2}):(\\d{2})");
        Matcher matcher2 = pattern2.matcher(timeSlotStr);
        if (matcher2.find()) {
            String weekday = matcher2.group(1);
            int startHour = Integer.parseInt(matcher2.group(2));
            int startMin = Integer.parseInt(matcher2.group(3));
            int endHour = Integer.parseInt(matcher2.group(4));
            int endMin = Integer.parseInt(matcher2.group(5));

            int dayOfWeek = getDayOfWeek(weekday);
            if (dayOfWeek != -1) {
                int startTime = startHour * 60 + startMin;
                int endTime = endHour * 60 + endMin;
                return new TimeSlot(dayOfWeek, startTime, endTime, false);
            }
        }

        // 格式3: 简单的"周一"（假设全天）
        Pattern pattern3 = Pattern.compile("(周[一二三四五六日])");
        Matcher matcher3 = pattern3.matcher(timeSlotStr);
        if (matcher3.find()) {
            String weekday = matcher3.group(1);
            int dayOfWeek = getDayOfWeek(weekday);
            if (dayOfWeek != -1) {
                return new TimeSlot(dayOfWeek, 1, 12, true); // 假设1-12节
            }
        }

        return null;
    }

    /**
     * 获取星期几的数字表示 (1=周一, 2=周二, ..., 7=周日)
     */
    private static int getDayOfWeek(String weekday) {
        for (int i = 0; i < CHINESE_WEEKDAYS.length; i++) {
            if (CHINESE_WEEKDAYS[i].equals(weekday)) {
                return i == 0 ? 7 : i; // 周日=7
            }
        }

        for (int i = 0; i < ENGLISH_WEEKDAYS.length; i++) {
            if (ENGLISH_WEEKDAYS[i].equalsIgnoreCase(weekday)) {
                return i == 0 ? 7 : i; // Sunday=7
            }
        }

        return -1;
    }

    /**
     * 时间段类
     */
    private static class TimeSlot {
        private final int dayOfWeek; // 1-7 (1=周一, 7=周日)
        private final int startTime;
        private final int endTime;
        private final boolean isPeriodBased; // true=节次(1-12节), false=时间(分钟)

        public TimeSlot(int dayOfWeek, int startTime, int endTime, boolean isPeriodBased) {
            this.dayOfWeek = dayOfWeek;
            this.startTime = startTime;
            this.endTime = endTime;
            this.isPeriodBased = isPeriodBased;
        }

        /**
         * 检查与另一个时间段是否冲突
         */
        public boolean conflictsWith(TimeSlot other) {
            // 不是同一天，不冲突
            if (this.dayOfWeek != other.dayOfWeek) {
                return false;
            }

            // 不同的时间表示方法，需要转换
            if (this.isPeriodBased != other.isPeriodBased) {
                // 简化处理：如果一个是节次一个是时间，认为可能冲突
                return true;
            }

            // 检查时间重叠
            return !(this.endTime <= other.startTime || this.startTime >= other.endTime);
        }

        @Override
        public String toString() {
            return String.format("Day:%d, %d-%d (%s)",
                dayOfWeek, startTime, endTime,
                isPeriodBased ? "periods" : "minutes");
        }
    }
}

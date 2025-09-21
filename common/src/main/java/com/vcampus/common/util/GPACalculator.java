package com.vcampus.common.util;

/**
 * GPA计算工具类
 * 提供分数到GPA的转换和成绩统计功能
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class GPACalculator {
    
    /**
     * 根据分数计算GPA
     * 
     * @param score 分数 (0-100)
     * @return GPA值 (0.0-4.8)
     */
    public static double calculateGPA(int score) {
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
    
    /**
     * 根据分数计算GPA（重载方法，支持double类型）
     * 
     * @param score 分数 (0.0-100.0)
     * @return GPA值 (0.0-4.8)
     */
    public static double calculateGPA(double score) {
        return calculateGPA((int) Math.round(score));
    }
    
    /**
     * 计算平均分
     * 
     * @param scores 分数数组
     * @return 平均分
     */
    public static double calculateAverageScore(double[] scores) {
        if (scores == null || scores.length == 0) return 0.0;
        
        double sum = 0.0;
        int count = 0;
        for (double score : scores) {
            if (score >= 0 && score <= 100) {
                sum += score;
                count++;
            }
        }
        return count > 0 ? sum / count : 0.0;
    }
    
    /**
     * 计算平均GPA
     * 
     * @param scores 分数数组
     * @return 平均GPA
     */
    public static double calculateAverageGPA(double[] scores) {
        if (scores == null || scores.length == 0) return 0.0;
        
        double sum = 0.0;
        int count = 0;
        for (double score : scores) {
            if (score >= 0 && score <= 100) {
                sum += calculateGPA(score);
                count++;
            }
        }
        return count > 0 ? sum / count : 0.0;
    }
    
    /**
     * 成绩统计信息类（支持加权平均）
     */
    public static class GradeStatistics {
        private double totalScore;      // 总加权分数
        private double totalGPA;        // 总加权GPA
        private double avgScore;        // 加权平均分
        private double avgGPA;          // 加权平均GPA
        private int totalCredits;       // 总学分
        private int count;              // 科目数
        
        public GradeStatistics() {
            this.totalScore = 0.0;
            this.totalGPA = 0.0;
            this.avgScore = 0.0;
            this.avgGPA = 0.0;
            this.totalCredits = 0;
            this.count = 0;
        }
        
        /**
         * 添加一个成绩（简单平均）
         * 
         * @param score 分数
         */
        public void addScore(double score) {
            if (score >= 0 && score <= 100) {
                totalScore += score;
                totalGPA += calculateGPA(score);
                count++;
                avgScore = totalScore / count;
                avgGPA = totalGPA / count;
            }
        }
        
        /**
         * 添加一个成绩（重载方法，支持int类型）
         * 
         * @param score 分数
         */
        public void addScore(int score) {
            addScore((double) score);
        }
        
        /**
         * 添加一个带学分的成绩（加权平均）
         * 
         * @param score 分数
         * @param credit 学分
         */
        public void addScoreWithCredit(double score, int credit) {
            if (score >= 0 && score <= 100 && credit > 0) {
                totalScore += credit * score;
                totalGPA += credit * calculateGPA(score);
                totalCredits += credit;
                count++;
                avgScore = totalCredits > 0 ? totalScore / totalCredits : 0.0;
                avgGPA = totalCredits > 0 ? totalGPA / totalCredits : 0.0;
            }
        }
        
        /**
         * 添加一个带学分的成绩（重载方法，支持int类型分数）
         * 
         * @param score 分数
         * @param credit 学分
         */
        public void addScoreWithCredit(int score, int credit) {
            addScoreWithCredit((double) score, credit);
        }
        
        // Getters
        public double getTotalScore() { return totalScore; }
        public double getTotalGPA() { return totalGPA; }
        public double getAvgScore() { return avgScore; }
        public double getAvgGPA() { return avgGPA; }
        public int getTotalCredits() { return totalCredits; }
        public int getCount() { return count; }
        
        @Override
        public String toString() {
            return String.format("成绩统计: 总加权分数=%.2f, 总加权GPA=%.2f, 加权平均分=%.2f, 加权平均GPA=%.2f, 总学分=%d, 科目数=%d", 
                    totalScore, totalGPA, avgScore, avgGPA, totalCredits, count);
        }
    }
}

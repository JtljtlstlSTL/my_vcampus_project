package com.vcampus.client.core.ui.library.chart;

import javax.swing.*;
import java.awt.*;

/**
 * 统计卡片组件
 * 用于显示关键指标数据，如总图书数、总用户数等
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class StatisticsCard extends JPanel {
    
    private JLabel titleLabel;
    private JLabel valueLabel;
    private JLabel unitLabel;
    private JLabel trendLabel;
    private Color cardColor;
    private Icon cardIcon;
    
    /**
     * 构造函数
     * @param title 卡片标题
     * @param value 数值
     * @param unit 单位
     * @param color 卡片颜色
     */
    public StatisticsCard(String title, String value, String unit, Color color) {
        this(title, value, unit, color, null);
    }
    
    /**
     * 构造函数
     * @param title 卡片标题
     * @param value 数值
     * @param unit 单位
     * @param color 卡片颜色
     * @param icon 图标
     */
    public StatisticsCard(String title, String value, String unit, Color color, Icon icon) {
        this.cardColor = color;
        this.cardIcon = icon;
        initComponents(title, value, unit);
        setupLayout();
        setupStyle();
    }
    
    /**
     * 初始化组件
     */
    private void initComponents(String title, String value, String unit) {
        // 标题标签
        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(100, 100, 100));
        
        // 数值标签
        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        valueLabel.setForeground(Color.WHITE);
        
        // 单位标签
        unitLabel = new JLabel(unit);
        unitLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        unitLabel.setForeground(new Color(200, 200, 200));
        
        // 趋势标签（可选）
        trendLabel = new JLabel();
        trendLabel.setFont(new Font("微软雅黑", Font.PLAIN, 10));
        trendLabel.setForeground(new Color(150, 150, 150));
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // 顶部区域：图标和标题
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        if (cardIcon != null) {
            JLabel iconLabel = new JLabel(cardIcon);
            topPanel.add(iconLabel, BorderLayout.WEST);
        }
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.setOpaque(false);
        
        // 中间区域：数值和单位
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        centerPanel.add(valueLabel);
        centerPanel.add(unitLabel);
        centerPanel.setOpaque(false);
        
        // 底部区域：趋势信息
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottomPanel.add(trendLabel);
        bottomPanel.setOpaque(false);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 设置样式
     */
    private void setupStyle() {
        setBackground(cardColor);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        // 添加阴影效果
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
    }
    
    /**
     * 更新数值
     * @param value 新数值
     */
    public void updateValue(String value) {
        valueLabel.setText(value);
    }
    
    /**
     * 获取当前数值
     * @return 当前数值
     */
    public String getValue() {
        return valueLabel.getText();
    }
    
    /**
     * 更新趋势信息
     * @param trend 趋势文本
     * @param trendColor 趋势颜色
     */
    public void updateTrend(String trend, Color trendColor) {
        trendLabel.setText(trend);
        trendLabel.setForeground(trendColor);
    }
    
    /**
     * 设置卡片颜色
     * @param color 新颜色
     */
    public void setCardColor(Color color) {
        this.cardColor = color;
        setBackground(color);
    }
    
    /**
     * 创建预设样式的卡片
     */
    public static class Preset {
        
        // 预设颜色
        public static final Color BLUE = new Color(52, 152, 219);
        public static final Color GREEN = new Color(46, 204, 113);
        public static final Color PURPLE = new Color(155, 89, 182);
        public static final Color ORANGE = new Color(230, 126, 34);
        public static final Color RED = new Color(231, 76, 60);
        public static final Color TEAL = new Color(26, 188, 156);
        
        /**
         * 创建图书统计卡片
         */
        public static StatisticsCard createBookCard(String value) {
            return new StatisticsCard("总图书数", value, "本", BLUE);
        }
        
        /**
         * 创建用户统计卡片
         */
        public static StatisticsCard createUserCard(String value) {
            return new StatisticsCard("总用户数", value, "人", GREEN);
        }
        
        /**
         * 创建借阅统计卡片
         */
        public static StatisticsCard createBorrowCard(String value) {
            return new StatisticsCard("当前借阅", value, "本", PURPLE);
        }
        
        /**
         * 创建逾期统计卡片
         */
        public static StatisticsCard createOverdueCard(String value) {
            return new StatisticsCard("逾期图书", value, "本", RED);
        }
        
        /**
         * 创建库存统计卡片
         */
        public static StatisticsCard createInventoryCard(String value) {
            return new StatisticsCard("在馆图书", value, "本", TEAL);
        }
        
        /**
         * 创建借阅率卡片
         */
        public static StatisticsCard createBorrowRateCard(String value) {
            return new StatisticsCard("借阅率", value, "%", ORANGE);
        }
    }
}

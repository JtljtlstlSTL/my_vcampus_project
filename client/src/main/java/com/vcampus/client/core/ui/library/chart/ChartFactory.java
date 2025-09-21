package com.vcampus.client.core.ui.library.chart;

import org.jfree.chart.*;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * 图表工厂类
 * 提供各种图表的创建方法，包括柱状图、折线图、饼图等
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class ChartFactory {
    
    // 默认颜色方案
    private static final Color[] CHART_COLORS = {
        new Color(52, 152, 219),   // 蓝色
        new Color(46, 204, 113),   // 绿色
        new Color(155, 89, 182),   // 紫色
        new Color(241, 196, 15),   // 黄色
        new Color(231, 76, 60),    // 红色
        new Color(26, 188, 156),   // 青色
        new Color(230, 126, 34),   // 橙色
        new Color(149, 165, 166)   // 灰色
    };
    
    /**
     * 创建柱状图
     * @param title 图表标题
     * @param xAxisLabel X轴标签
     * @param yAxisLabel Y轴标签
     * @param data 数据列表
     * @param categoryKey 分类字段名
     * @param valueKey 数值字段名
     * @return JFreeChart对象
     */
    public static JFreeChart createBarChart(String title, String xAxisLabel, String yAxisLabel,
                                           List<Map<String, Object>> data, String categoryKey, String valueKey) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map<String, Object> item : data) {
            String category = String.valueOf(item.get(categoryKey));
            Number value = (Number) item.get(valueKey);
            dataset.addValue(value, "数据", category);
        }
        
        JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart(
            title, xAxisLabel, yAxisLabel, dataset,
            PlotOrientation.VERTICAL, true, true, false
        );
        
        // 设置样式
        customizeBarChart(chart);
        return chart;
    }
    
    /**
     * 创建折线图
     * @param title 图表标题
     * @param xAxisLabel X轴标签
     * @param yAxisLabel Y轴标签
     * @param data 数据列表
     * @param categoryKey 分类字段名
     * @param valueKey 数值字段名
     * @return JFreeChart对象
     */
    public static JFreeChart createLineChart(String title, String xAxisLabel, String yAxisLabel,
                                           List<Map<String, Object>> data, String categoryKey, String valueKey) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        for (Map<String, Object> item : data) {
            String category = String.valueOf(item.get(categoryKey));
            Number value = (Number) item.get(valueKey);
            dataset.addValue(value, "数据", category);
        }
        
        JFreeChart chart = org.jfree.chart.ChartFactory.createLineChart(
            title, xAxisLabel, yAxisLabel, dataset,
            PlotOrientation.VERTICAL, true, true, false
        );
        
        // 设置样式
        customizeLineChart(chart);
        return chart;
    }
    
    /**
     * 创建饼图
     * @param title 图表标题
     * @param data 数据列表
     * @param categoryKey 分类字段名
     * @param valueKey 数值字段名
     * @return JFreeChart对象
     */
    public static JFreeChart createPieChart(String title, List<Map<String, Object>> data,
                                          String categoryKey, String valueKey) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        for (Map<String, Object> item : data) {
            String category = String.valueOf(item.get(categoryKey));
            Number value = (Number) item.get(valueKey);
            dataset.setValue(category, value);
        }
        
        JFreeChart chart = org.jfree.chart.ChartFactory.createPieChart(
            title, dataset, true, true, false
        );
        
        // 设置样式
        customizePieChart(chart);
        return chart;
    }
    
    /**
     * 创建XY折线图（用于趋势分析）
     * @param title 图表标题
     * @param xAxisLabel X轴标签
     * @param yAxisLabel Y轴标签
     * @param data 数据列表
     * @param xKey X轴字段名
     * @param yKey Y轴字段名
     * @return JFreeChart对象
     */
    public static JFreeChart createXYLineChart(String title, String xAxisLabel, String yAxisLabel,
                                             List<Map<String, Object>> data, String xKey, String yKey) {
        XYSeries series = new XYSeries("数据");
        
        for (Map<String, Object> item : data) {
            Number x = (Number) item.get(xKey);
            Number y = (Number) item.get(yKey);
            series.add(x.doubleValue(), y.doubleValue());
        }
        
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        
        JFreeChart chart = org.jfree.chart.ChartFactory.createXYLineChart(
            title, xAxisLabel, yAxisLabel, dataset,
            PlotOrientation.VERTICAL, true, true, false
        );
        
        // 设置样式
        customizeXYLineChart(chart);
        return chart;
    }
    
    /**
     * 创建图表面板
     * @param chart JFreeChart对象
     * @param width 面板宽度
     * @param height 面板高度
     * @return ChartPanel对象
     */
    public static ChartPanel createChartPanel(JFreeChart chart, int width, int height) {
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(width, height));
        chartPanel.setMouseWheelEnabled(true);
        // chartPanel.setDisplayTooltips(true); // 此方法在JFreeChart 1.5.3中不存在
        return chartPanel;
    }
    
    // ==================== 样式定制方法 ====================
    
    /**
     * 定制柱状图样式
     */
    private static void customizeBarChart(JFreeChart chart) {
        // 设置标题字体
        chart.getTitle().setFont(new Font("微软雅黑", Font.BOLD, 16));
        
        // 设置图例字体
        chart.getLegend().setItemFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        CategoryPlot plot = chart.getCategoryPlot();
        
        // 设置坐标轴字体
        plot.getDomainAxis().setLabelFont(new Font("微软雅黑", Font.PLAIN, 12));
        plot.getRangeAxis().setLabelFont(new Font("微软雅黑", Font.PLAIN, 12));
        plot.getDomainAxis().setTickLabelFont(new Font("微软雅黑", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("微软雅黑", Font.PLAIN, 10));
        
        // 设置背景色
        plot.setBackgroundPaint(Color.WHITE);
        chart.setBackgroundPaint(Color.WHITE);
        
        // 设置网格线
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        // 设置柱状图颜色
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        for (int i = 0; i < plot.getDataset().getRowCount(); i++) {
            renderer.setSeriesPaint(i, CHART_COLORS[i % CHART_COLORS.length]);
        }
        
        // 显示数值标签
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("微软雅黑", Font.PLAIN, 10));
    }
    
    /**
     * 定制折线图样式
     */
    private static void customizeLineChart(JFreeChart chart) {
        // 设置标题字体
        chart.getTitle().setFont(new Font("微软雅黑", Font.BOLD, 16));
        
        // 设置图例字体
        chart.getLegend().setItemFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        CategoryPlot plot = chart.getCategoryPlot();
        
        // 设置坐标轴字体
        plot.getDomainAxis().setLabelFont(new Font("微软雅黑", Font.PLAIN, 12));
        plot.getRangeAxis().setLabelFont(new Font("微软雅黑", Font.PLAIN, 12));
        plot.getDomainAxis().setTickLabelFont(new Font("微软雅黑", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("微软雅黑", Font.PLAIN, 10));
        
        // 设置背景色
        plot.setBackgroundPaint(Color.WHITE);
        chart.setBackgroundPaint(Color.WHITE);
        
        // 设置网格线
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        // 设置折线图样式
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, CHART_COLORS[0]);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesFilled(0, true);
        
        // 显示数值标签
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("微软雅黑", Font.PLAIN, 10));
    }
    
    /**
     * 定制饼图样式
     */
    private static void customizePieChart(JFreeChart chart) {
        // 设置标题字体
        chart.getTitle().setFont(new Font("微软雅黑", Font.BOLD, 16));
        
        // 设置图例字体
        chart.getLegend().setItemFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        PiePlot plot = (PiePlot) chart.getPlot();
        
        // 设置背景色
        plot.setBackgroundPaint(Color.WHITE);
        chart.setBackgroundPaint(Color.WHITE);
        
        // 设置标签字体
        plot.setLabelFont(new Font("微软雅黑", Font.PLAIN, 10));
        
        // 设置颜色
        for (int i = 0; i < plot.getDataset().getItemCount(); i++) {
            plot.setSectionPaint(i, CHART_COLORS[i % CHART_COLORS.length]);
        }
        
        // 设置标签格式
        plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator(
            "{0}: {1} ({2})", new java.text.DecimalFormat("0"), new java.text.DecimalFormat("0.0%")
        ));
    }
    
    /**
     * 定制XY折线图样式
     */
    private static void customizeXYLineChart(JFreeChart chart) {
        // 设置标题字体
        chart.getTitle().setFont(new Font("微软雅黑", Font.BOLD, 16));
        
        // 设置图例字体
        chart.getLegend().setItemFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        XYPlot plot = chart.getXYPlot();
        
        // 设置坐标轴字体
        plot.getDomainAxis().setLabelFont(new Font("微软雅黑", Font.PLAIN, 12));
        plot.getRangeAxis().setLabelFont(new Font("微软雅黑", Font.PLAIN, 12));
        plot.getDomainAxis().setTickLabelFont(new Font("微软雅黑", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("微软雅黑", Font.PLAIN, 10));
        
        // 设置背景色
        plot.setBackgroundPaint(Color.WHITE);
        chart.setBackgroundPaint(Color.WHITE);
        
        // 设置网格线
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        
        // 设置折线样式
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, CHART_COLORS[0]);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShapesFilled(0, true);
    }
}

package com.vcampus.client.core.ui.library;

import com.vcampus.client.core.net.NettyClient;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.CategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 图书分类详情面板
 * 显示所有22个中图法分类的详细柱状图信息
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class CategoryDetailPanel extends JPanel {
    
    private final NettyClient nettyClient;
    private ChartPanel chartPanelComponent;
    
    public CategoryDetailPanel(NettyClient nettyClient, JPanel mainPanel, CardLayout cardLayout) {
        this.nettyClient = nettyClient;
        
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // 创建图表面板
        JPanel chartPanel = createChartPanel();
        add(chartPanel, BorderLayout.CENTER);
    }
    
    
    /**
     * 创建图表面板
     */
    private JPanel createChartPanel() {
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 创建柱状图
        JFreeChart chart = createCategoryDetailChart();
        chartPanelComponent = new ChartPanel(chart);
        chartPanelComponent.setPreferredSize(new Dimension(1000, 600));
        // 删除重复的标题边框
        chartPanelComponent.setBorder(BorderFactory.createEmptyBorder());
        
        chartPanel.add(chartPanelComponent, BorderLayout.CENTER);
        
        return chartPanel;
    }
    
    /**
     * 创建分类详情柱状图
     */
    private JFreeChart createCategoryDetailChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // 获取所有分类数据
        Map<String, Integer> allCategories = getAllCategoryData();
        
        // 中图法22大类完整分类（根据更新后的数据库）
        String[] categoryCodes = {
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", 
            "K", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "X", "Z"
        };
        String[] categoryNames = {
            "马克思主义、列宁主义、毛泽东思想、邓小平理论",
            "哲学、宗教",
            "社会科学总论",
            "政治、法律",
            "军事",
            "经济",
            "文化、科学、教育、体育",
            "语言、文字",
            "文学",
            "艺术",
            "历史、地理",
            "自然科学总论",
            "数理科学和化学",
            "天文学、地球科学",
            "生物科学",
            "医药、卫生",
            "农业科学",
            "工业技术",
            "交通运输",
            "航空、航天",
            "环境科学、安全科学",
            "综合性图书"
        };
        
        // 直接使用服务端返回的数据，简化逻辑
        for (String categoryCode : categoryCodes) {
            Object countObj = allCategories.getOrDefault(categoryCode, 0);
            int count = countObj instanceof Double ? ((Double) countObj).intValue() : (Integer) countObj;
            dataset.addValue(count, "图书数量", categoryCode);
        }
        
        JFreeChart chart = ChartFactory.createBarChart(
            "图书分类详情统计",
            "图书分类",
            "图书数量",
            dataset
        );
        
        // 设置样式
        CategoryPlot plot = chart.getCategoryPlot();
        
        // 创建自定义渲染器来设置每个柱子的不同颜色
        BarRenderer renderer = new BarRenderer() {
            @Override
            public java.awt.Paint getItemPaint(int row, int column) {
                // 22种不同颜色
                Color[] colors = {
                    new Color(52, 152, 219),   // 深蓝色
                    new Color(46, 204, 113),   // 绿色
                    new Color(231, 76, 60),    // 红色
                    new Color(241, 196, 15),   // 黄色
                    new Color(155, 89, 182),   // 紫色
                    new Color(230, 126, 34),   // 橙色
                    new Color(26, 188, 156),   // 青色
                    new Color(142, 68, 173),   // 深紫色
                    new Color(39, 174, 96),    // 深绿色
                    new Color(192, 57, 43),    // 深红色
                    new Color(41, 128, 185),   // 蓝色
                    new Color(149, 165, 166),  // 灰色
                    new Color(243, 156, 18),   // 橙色
                    new Color(127, 140, 141),  // 深灰色
                    new Color(52, 73, 94),     // 深蓝灰色
                    new Color(44, 62, 80),     // 深色
                    new Color(231, 76, 60),    // 红色
                    new Color(46, 204, 113),   // 绿色
                    new Color(155, 89, 182),   // 紫色
                    new Color(230, 126, 34),   // 橙色
                    new Color(26, 188, 156),   // 青色
                    new Color(142, 68, 173)    // 深紫色
                };
                return colors[column % colors.length];
            }
        };
        
        // 设置鼠标悬停提示
        renderer.setDefaultToolTipGenerator(new CategoryToolTipGenerator() {
            @Override
            public String generateToolTip(CategoryDataset dataset, int row, int column) {
                String categoryCode = (String) dataset.getColumnKey(column);
                String categoryName = getCategoryName(categoryCode);
                Number value = dataset.getValue(row, column);
                return String.format("%s (%s): %d 本书", categoryName, categoryCode, value.intValue());
            }
        });
        
        plot.setRenderer(renderer);
        renderer.setGradientPaintTransformer(null); // 禁用渐变，使用纯色
        renderer.setDrawBarOutline(false); // 不绘制柱子边框
        
        // 设置字体
        Font chineseFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
        
        // 删除主标题和图例
        chart.setTitle((org.jfree.chart.title.TextTitle) null);
        chart.removeLegend();
        plot.getDomainAxis().setLabelFont(chineseFont);
        plot.getRangeAxis().setLabelFont(chineseFont);
        plot.getDomainAxis().setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
        plot.getRangeAxis().setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
        
        // 设置Y轴为整数刻度
        plot.getRangeAxis().setStandardTickUnits(org.jfree.chart.axis.NumberAxis.createIntegerTickUnits());
        
        // 计算最大Y值并设置范围
        int maxValue = 0;
        for (Object value : allCategories.values()) {
            int intValue = value instanceof Double ? ((Double) value).intValue() : (Integer) value;
            maxValue = Math.max(maxValue, intValue);
        }
        if (maxValue > 0) {
            double upperBound = Math.max(maxValue * 1.1, 1.0);
            plot.getRangeAxis().setRange(0, upperBound);
        }
        
        // 设置图表背景为白色
        chart.setBackgroundPaint(Color.WHITE);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        
        return chart;
    }
    
    /**
     * 根据类别代码获取中文名称
     */
    private String getCategoryName(String categoryCode) {
        String[] categoryCodes = {
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", 
            "K", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "X", "Z"
        };
        
        String[] categoryNames = {
            "马克思主义、列宁主义、毛泽东思想、邓小平理论",
            "哲学、宗教",
            "社会科学总论",
            "政治、法律",
            "军事",
            "经济",
            "文化、科学、教育、体育",
            "语言、文字",
            "文学",
            "艺术",
            "历史、地理",
            "自然科学总论",
            "数理科学和化学",
            "天文学、地球科学",
            "生物科学",
            "医药、卫生",
            "农业科学",
            "工业技术",
            "交通运输",
            "航空、航天",
            "环境科学、安全科学",
            "综合性图书"
        };
        
        for (int i = 0; i < categoryCodes.length; i++) {
            if (categoryCodes[i].equals(categoryCode)) {
                return categoryNames[i];
            }
        }
        return categoryCode; // 如果找不到，返回原代码
    }
    
    /**
     * 获取所有分类数据
     */
    private Map<String, Integer> getAllCategoryData() {
        try {
            // 通过NettyClient调用服务器API获取真实数据
            if (nettyClient != null) {
                try {
                    com.vcampus.common.message.Request request = new com.vcampus.common.message.Request();
                    request.setUri("library/admin/statistics/book-categories");
                    
                    com.vcampus.common.message.Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    if (response != null && response.isSuccess()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> categoryData = (Map<String, Object>) response.getData();
                        if (categoryData != null && categoryData.containsKey("categories")) {
                            @SuppressWarnings("unchecked")
                            Map<String, Integer> categories = (Map<String, Integer>) categoryData.get("categories");
                            System.out.println("获取到的分类数据: " + categories);
                            return categories;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("网络请求失败: " + e.getMessage());
                }
            }
            
            // 如果网络请求失败，返回默认数据
            Map<String, Integer> defaultCategories = new HashMap<>();
            String[] categoryCodes = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", 
                                    "K", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "X", "Z"};
            for (String code : categoryCodes) {
                defaultCategories.put(code, 2); // 每个分类默认2本书
            }
            return defaultCategories;
        } catch (Exception e) {
            System.err.println("获取图书分类数据失败: " + e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * 刷新图表数据
     */
    public void refreshChart() {
        if (chartPanelComponent != null) {
            JFreeChart newChart = createCategoryDetailChart();
            chartPanelComponent.setChart(newChart);
            chartPanelComponent.repaint();
        }
    }
}

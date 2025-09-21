package com.vcampus.client.core.ui.library;




import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.vcampus.client.core.net.NettyClient;

/**
 * 统计分析主界面
 * 提供统计分析和数据看板的统一入口
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class StatisticsReportMainPanel extends JPanel {
    
    private static final Logger log = LoggerFactory.getLogger(StatisticsReportMainPanel.class);
    
    private final NettyClient nettyClient;
    private final Map<String, Object> userData;
    private JTabbedPane parentTabbedPane; // 父级选项卡面板
    
    // 主要功能按钮
    private JButton statisticsButton;
    private JButton dashboardButton;
    
    // 内容面板
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    // 子面板



    private AdvancedStatisticsPanel statisticsPanel;
    private DashboardPanel dashboardPanel;
    private JPanel mainPanel;
    private CategoryDetailPanel detailPanel;
    
    public StatisticsReportMainPanel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        
        initializeComponents();
        setupLayout();
        setupEventListeners();
    }
    
    /**
     * 设置父级选项卡面板
     */
    public void setParentTabbedPane(JTabbedPane parentTabbedPane) {
        this.parentTabbedPane = parentTabbedPane;
        
        // 添加标签页切换监听器，当统计报表标签页被选中时自动刷新图表
        parentTabbedPane.addChangeListener(e -> {
            if (parentTabbedPane.getSelectedComponent() == this) {
                System.out.println("统计报表标签页被选中，开始刷新图表...");
                // 延迟调用refreshCharts，确保方法已定义
                SwingUtilities.invokeLater(this::refreshCharts);
            }
        });
    }
    
    /**
     * 初始化组件
     */
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 创建导航按钮
        statisticsButton = createNavButton("数据分析", new Color(52, 152, 219));
        dashboardButton = createNavButton("统计分析", new Color(155, 89, 182));
        
        // 创建内容面板
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        
        // 创建子面板
        statisticsPanel = new AdvancedStatisticsPanel(nettyClient, userData);
        dashboardPanel = new DashboardPanel();
        
        // 设置mainPanel为contentPanel
        this.mainPanel = contentPanel;
        this.cardLayout = cardLayout;
        
        // 创建详情面板
        detailPanel = new CategoryDetailPanel(nettyClient, contentPanel, cardLayout);
        
        // 添加子面板到内容面板
        contentPanel.add(statisticsPanel, "statistics");
        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(detailPanel, "detail");
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        // 创建顶部导航栏
        JPanel navPanel = createNavPanel();
        
        // 创建主内容区域
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(Color.WHITE);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainContentPanel.add(contentPanel, BorderLayout.CENTER);
        
        // 添加到主面板
        add(navPanel, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);
        
        // 默认显示数据看板
        showDashboard();
    }
    
    /**
     * 创建导航面板
     */
    private JPanel createNavPanel() {
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        navPanel.setBackground(new Color(248, 249, 250));
        navPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        // 添加标题
        JLabel titleLabel = new JLabel("统计分析");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 73, 94));
        navPanel.add(titleLabel);
        
        // 添加分隔符
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setPreferredSize(new Dimension(1, 30));
        navPanel.add(separator);
        
        // 添加导航按钮
        navPanel.add(dashboardButton);
        navPanel.add(statisticsButton);
        
        return navPanel;
    }
    
    /**
     * 创建导航按钮
     */
    private JButton createNavButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        button.setPreferredSize(new Dimension(120, 40));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {



            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }



            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        dashboardButton.addActionListener(e -> showDashboard());
        statisticsButton.addActionListener(e -> showStatistics());
    }
    
    /**
     * 显示数据看板
     */
    private void showDashboard() {
        cardLayout.show(contentPanel, "dashboard");
        updateNavButtonState(dashboardButton);
    }
    
    /**
     * 显示统计分析
     */
    private void showStatistics() {
        cardLayout.show(contentPanel, "statistics");
        updateNavButtonState(statisticsButton);
    }
    
    
    /**
     * 更新导航按钮状态
     */
    private void updateNavButtonState(JButton activeButton) {
        // 重置所有按钮状态
        dashboardButton.setBackground(new Color(155, 89, 182));
        statisticsButton.setBackground(new Color(52, 152, 219));
        
        // 设置活动按钮状态
        activeButton.setBackground(activeButton.getBackground().darker());
    }
    
    /**
     * 数据看板面板
     */
    private class DashboardPanel extends JPanel {
        private int currentChartIndex = 0; // 当前显示的图表索引
        private JPanel chartContainer; // 图表容器
        private JButton prevButton, nextButton; // 切换按钮
        
        public DashboardPanel() {
            initializeDashboard();
        }
        
        private JPanel chartsPanel; // 将chartsPanel设为实例变量
        
        private void initializeDashboard() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            
            // 创建图表面板
            chartsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
            chartsPanel.setBackground(Color.WHITE);
            chartsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // 添加各种图表
            chartsPanel.add(createBookCategoryPieChart());
            chartsPanel.add(createBorrowRateBarChart());
            chartsPanel.add(createUserActivityChart()); // 用户活跃度统计图（移到左边）
            chartsPanel.add(createSwitchableTrendChart()); // 可切换的趋势图（移到右边）
            
            add(chartsPanel, BorderLayout.CENTER);
        }
        
        /**
         * 刷新所有图表
         */
        public void refreshCharts() {
            System.out.println("刷新数据看板图表...");
            
            // 移除所有现有图表
            chartsPanel.removeAll();
            
            // 重新创建所有图表
            chartsPanel.add(createBookCategoryPieChart());
            chartsPanel.add(createBorrowRateBarChart());
            chartsPanel.add(createUserActivityChart());
            chartsPanel.add(createSwitchableTrendChart());
            
            // 刷新显示
            chartsPanel.revalidate();
            chartsPanel.repaint();
            
            System.out.println("数据看板图表刷新完成");
        }
        
        /**
         * 创建可切换的借阅趋势图
         */
        private JPanel createSwitchableTrendChart() {
            JPanel container = new JPanel(new BorderLayout());
            container.setBackground(Color.WHITE);
            
            // 创建图表容器
            chartContainer = new JPanel(new BorderLayout());
            chartContainer.setBackground(Color.WHITE);
            
            // 初始化显示第一个图表
            updateTrendChart();
            
            // 创建切换按钮面板 - 增加上边距让按钮往下移动
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0)); // 增加上边距
            
            // 尝试加载图标，如果失败则使用文字按钮
            try {
                ImageIcon leftIcon = new ImageIcon(getClass().getResource("/com/vcampus/client/core/ui/library/resources/向左.png"));
                ImageIcon rightIcon = new ImageIcon(getClass().getResource("/com/vcampus/client/core/ui/library/resources/向右.png"));
                
                if (leftIcon.getImage() != null && rightIcon.getImage() != null) {
                    // 缩放图标到合适大小
                    Image leftImage = leftIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                    Image rightImage = rightIcon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                    
                    prevButton = new JButton(new ImageIcon(leftImage));
                    nextButton = new JButton(new ImageIcon(rightImage));
                } else {
                    throw new Exception("图标加载失败");
                }
            } catch (Exception e) {
                // 图标加载失败，使用文字按钮
                System.err.println("图标加载失败，使用文字按钮: " + e.getMessage());
                prevButton = new JButton("←");
                nextButton = new JButton("→");
                prevButton.setFont(new Font("Arial", Font.BOLD, 14));
                nextButton.setFont(new Font("Arial", Font.BOLD, 14));
            }
            
            // 设置按钮样式
            prevButton.setPreferredSize(new Dimension(32, 32));
            nextButton.setPreferredSize(new Dimension(32, 32));
            prevButton.setFocusPainted(false);
            nextButton.setFocusPainted(false);
            prevButton.setBorderPainted(false);
            nextButton.setBorderPainted(false);
            prevButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            nextButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            
            // 添加按钮事件
            prevButton.addActionListener(e -> {
                currentChartIndex = (currentChartIndex - 1 + 3) % 3;
                updateTrendChart();
            });
            
            nextButton.addActionListener(e -> {
                currentChartIndex = (currentChartIndex + 1) % 3;
                updateTrendChart();
            });
            
            buttonPanel.add(prevButton);
            buttonPanel.add(nextButton);
            
            container.add(chartContainer, BorderLayout.CENTER);
            container.add(buttonPanel, BorderLayout.SOUTH);
            
            return container;
        }
        
        /**
         * 更新趋势图表
         */
        private void updateTrendChart() {
            chartContainer.removeAll();
            
            String[] periods = {"4days", "3months", "1year"};
            String[] periodNames = {"近4天", "近3个月", "近1年"};
            
            String currentPeriod = periods[currentChartIndex];
            String currentPeriodName = periodNames[currentChartIndex];
            
            // 创建图表
            JFreeChart chart = createInteractiveBorrowChart(currentPeriod);
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(400, 300));
            chartPanel.setMaximumSize(new Dimension(400, 300)); // 确保高度一致
            chartPanel.setBorder(BorderFactory.createTitledBorder(currentPeriodName + "借阅趋势"));
            
            chartContainer.add(chartPanel, BorderLayout.CENTER);
            chartContainer.revalidate();
            chartContainer.repaint();
        }
        
        /**
         * 创建交互式借阅图表
         */
        private JFreeChart createInteractiveBorrowChart(String period) {
            // 获取真实数据
            Map<String, Object> data = getBorrowStatisticsData(period);
            
            XYSeriesCollection dataset = new XYSeriesCollection();
            XYSeries borrowSeries = new XYSeries("借阅数量");
            XYSeries returnSeries = new XYSeries("归还数量");
            
            // 根据时间周期生成数据
            @SuppressWarnings("unchecked")
            List<String> dates = (List<String>) data.get("dates");
            @SuppressWarnings("unchecked")
            List<Integer> borrowCounts = (List<Integer>) data.get("borrowCounts");
            @SuppressWarnings("unchecked")
            List<Integer> returnCounts = (List<Integer>) data.get("returnCounts");
            
            for (int i = 0; i < dates.size(); i++) {
                borrowSeries.add(i, borrowCounts.get(i));
                returnSeries.add(i, returnCounts.get(i));
            }
            
            dataset.addSeries(borrowSeries);
            dataset.addSeries(returnSeries);
            
            JFreeChart chart = ChartFactory.createXYLineChart(
                "",
                "日期",
                "数量",
                dataset
            );
            
            // 设置样式
            XYPlot plot = (XYPlot) chart.getPlot();
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
            
            // 设置线条样式 - 让两条线更容易区分
            renderer.setSeriesPaint(0, new Color(52, 152, 219));  // 借阅线：蓝色
            renderer.setSeriesPaint(1, new Color(231, 76, 60));   // 归还线：红色
            renderer.setSeriesShapesVisible(0, true);
            renderer.setSeriesShapesVisible(1, true);
            renderer.setSeriesShapesFilled(0, true);
            renderer.setSeriesShapesFilled(1, true);
            // 设置不同的线条粗细和样式
            renderer.setSeriesStroke(0, new BasicStroke(3.0f));  // 借阅线：粗实线
            renderer.setSeriesStroke(1, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8, 4}, 0)); // 归还线：虚线
            // 设置不同的点形状
            renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-3, -3, 6, 6)); // 借阅线：圆点
            renderer.setSeriesShape(1, new java.awt.geom.Rectangle2D.Double(-3, -3, 6, 6)); // 归还线：方点
            
            // 设置字体
            Font chineseFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
            Font titleFont = new Font("Microsoft YaHei", Font.BOLD, 16);
            
            chart.getTitle().setFont(titleFont);
            chart.getLegend().setItemFont(chineseFont);
            plot.getDomainAxis().setLabelFont(chineseFont);
            plot.getRangeAxis().setLabelFont(chineseFont);
            plot.getDomainAxis().setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            plot.getRangeAxis().setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            
            // 设置X轴和Y轴格式
            NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
            domainAxis.setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            if (dates.size() > 1) {
                domainAxis.setRange(0, dates.size() - 1);
            } else if (dates.size() == 1) {
                domainAxis.setRange(0, 1);
            } else {
                domainAxis.setRange(0, 1);
            }
            
            // 创建自定义标签生成器显示日期
            if (dates.size() > 0) {
                org.jfree.chart.axis.SymbolAxis symbolAxis = new org.jfree.chart.axis.SymbolAxis("日期", dates.toArray(new String[0]));
                symbolAxis.setRange(0, dates.size() - 1);
                symbolAxis.setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
                symbolAxis.setVerticalTickLabels(false);
                plot.setDomainAxis(symbolAxis);
            }
            
            // 设置Y轴为整数刻度，根据实际数据自适应范围
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            
            // 计算实际数据的最大值
            int maxBorrow = borrowCounts.isEmpty() ? 0 : Collections.max(borrowCounts);
            int maxReturn = returnCounts.isEmpty() ? 0 : Collections.max(returnCounts);
            int maxValue = Math.max(maxBorrow, maxReturn);
            
            // 设置Y轴范围，如果最大值为0则设为0-10，否则设为0到最大值的1.1倍
            if (maxValue == 0) {
                rangeAxis.setRange(0, 10);
            } else {
                rangeAxis.setRange(0, maxValue * 1.1);
            }
            
            // 设置图表背景为白色
            chart.setBackgroundPaint(Color.WHITE);
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);
            
            return chart;
        }
        
        /**
         * 获取借阅统计数据
         */
        private Map<String, Object> getBorrowStatisticsData(String period) {
            Map<String, Object> result = new HashMap<>();
            
            try {
                // 通过NettyClient调用服务器API获取真实数据
                if (nettyClient != null) {
                    try {
                        // 发送请求到服务器获取借阅趋势数据
                        com.vcampus.common.message.Request request = new com.vcampus.common.message.Request("library/admin/statistics/borrow-trend", Map.of("period", period));
                        
                        com.vcampus.common.message.Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                        if (response != null && ("200".equals(response.getStatus()) || "SUCCESS".equals(response.getStatus()))) {
                            Object data = response.getData();
                            List<Map<String, Object>> trendData = new ArrayList<>();
                            
                            if (data instanceof List) {
                                @SuppressWarnings("unchecked")
                                List<Object> dataList = (List<Object>) data;
                                for (Object item : dataList) {
                                    if (item instanceof Map) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> itemMap = (Map<String, Object>) item;
                                        trendData.add(itemMap);
                                    }
                                }
                            }
                            
                            List<String> dates = new ArrayList<>();
                            List<Integer> borrowCounts = new ArrayList<>();
                            List<Integer> returnCounts = new ArrayList<>();
                            
                            for (Map<String, Object> dayData : trendData) {
                                dates.add((String) dayData.get("date"));
                                
                                // 安全地转换数值类型
                                Object borrowObj = dayData.get("borrowCount");
                                Object returnObj = dayData.get("returnCount");
                                
                                int borrowCount = 0;
                                int returnCount = 0;
                                
                                if (borrowObj instanceof Number) {
                                    borrowCount = ((Number) borrowObj).intValue();
                                } else if (borrowObj instanceof String) {
                                    try {
                                        borrowCount = Integer.parseInt((String) borrowObj);
                                    } catch (NumberFormatException e) {
                                        borrowCount = 0;
                                    }
                                }
                                
                                if (returnObj instanceof Number) {
                                    returnCount = ((Number) returnObj).intValue();
                                } else if (returnObj instanceof String) {
                                    try {
                                        returnCount = Integer.parseInt((String) returnObj);
                                    } catch (NumberFormatException e) {
                                        returnCount = 0;
                                    }
                                }
                                
                                // 调试日志
                                System.out.println("接收数据 - 日期: " + dayData.get("date") + 
                                    ", 借阅数量: " + borrowCount + 
                                    ", 归还数量: " + returnCount);
                                
                                borrowCounts.add(borrowCount);
                                returnCounts.add(returnCount);
                            }
                            
                            // 如果服务器返回空数据，使用模拟数据
                            if (dates.isEmpty()) {
                                java.time.LocalDate today = java.time.LocalDate.now();
                                
                                switch (period) {
                                    case "4days":
                                        for (int i = 3; i >= 0; i--) {
                                            java.time.LocalDate date = today.minusDays(i);
                                            dates.add(date.format(java.time.format.DateTimeFormatter.ofPattern("M-d")));
                                            borrowCounts.add(0); // 没有数据时设为0
                                            returnCounts.add(0);
                                        }
                                        break;
                                    case "3months":
                                        for (int i = 2; i >= 0; i--) {
                                            java.time.LocalDate month = today.minusMonths(i);
                                            dates.add(month.format(java.time.format.DateTimeFormatter.ofPattern("M月")));
                                            borrowCounts.add(0);
                                            returnCounts.add(0);
                                        }
                                        break;
                                    case "1year":
                                        for (int month = 1; month <= today.getMonthValue(); month++) {
                                            dates.add(month + "月");
                                            borrowCounts.add(0);
                                            returnCounts.add(0);
                                        }
                                        break;
                                    default:
                                        for (int i = 3; i >= 0; i--) {
                                            java.time.LocalDate date = today.minusDays(i);
                                            dates.add(date.format(java.time.format.DateTimeFormatter.ofPattern("M-d")));
                                            borrowCounts.add(0);
                                            returnCounts.add(0);
                                        }
                                        break;
                                }
                            }
                            
                            result.put("dates", dates);
                            result.put("borrowCounts", borrowCounts);
                            result.put("returnCounts", returnCounts);
                            
                            return result;
                        }
                    } catch (Exception e) {
                        System.err.println("网络请求失败: " + e.getMessage());
                    }
                }
                
                // 网络请求失败时返回空数据（显示0）
                List<String> dates = new ArrayList<>();
                List<Integer> borrowCounts = new ArrayList<>();
                List<Integer> returnCounts = new ArrayList<>();
                
                java.time.LocalDate today = java.time.LocalDate.now();
                
                switch (period) {
                    case "4days":
                        // 近4天：今天和前3天，索引3是今天
                        for (int i = 3; i >= 0; i--) {
                            java.time.LocalDate date = today.minusDays(i);
                            dates.add(date.format(java.time.format.DateTimeFormatter.ofPattern("M-d")));
                            borrowCounts.add(0); // 没有数据时设为0
                            returnCounts.add(0);
                        }
                        break;
                    case "3months":
                        // 近3个月：本月、上月、上上月
                        for (int i = 2; i >= 0; i--) {
                            java.time.LocalDate month = today.minusMonths(i);
                            dates.add(month.format(java.time.format.DateTimeFormatter.ofPattern("M月")));
                            borrowCounts.add(0); // 没有数据时设为0
                            returnCounts.add(0);
                        }
                        break;
                    case "1year":
                        // 近1年：今年到目前为止的所有月份
                        for (int month = 1; month <= today.getMonthValue(); month++) {
                            dates.add(month + "月");
                            borrowCounts.add(0); // 没有数据时设为0
                            returnCounts.add(0);
                        }
                        break;
                    default:
                        // 默认返回近4天数据
                        for (int i = 3; i >= 0; i--) {
                            java.time.LocalDate date = today.minusDays(i);
                            dates.add(date.format(java.time.format.DateTimeFormatter.ofPattern("M-d")));
                            borrowCounts.add(0); // 没有数据时设为0
                            returnCounts.add(0);
                        }
                        break;
                }
                
                result.put("dates", dates);
                result.put("borrowCounts", borrowCounts);
                result.put("returnCounts", returnCounts);
                
            } catch (Exception e) {
                System.err.println("获取借阅统计数据失败: " + e.getMessage());
            }
            
            return result;
        }
        
        /**
         * 创建图书分类扇形图
         */
        private JPanel createBookCategoryPieChart() {
            DefaultPieDataset dataset = new DefaultPieDataset();
            
            // 从服务器获取真实数据
            try {
                System.out.println("开始获取图书分类数据...");
                Map<String, Object> categoryData = getBookCategoryData();
                System.out.println("获取到的图书分类数据: " + categoryData);
                if (categoryData != null && categoryData.containsKey("categories")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Integer> categories = (Map<String, Integer>) categoryData.get("categories");
                    System.out.println("分类详情: " + categories);
                    for (Map.Entry<String, Integer> entry : categories.entrySet()) {
                        // 直接使用分类代码作为图例标签
                        String categoryCode = entry.getKey();
                        System.out.println("添加分类: " + categoryCode + " = " + entry.getValue());
                        dataset.setValue(categoryCode, entry.getValue());
                    }
                } else {
                    // 如果获取失败，显示空图表
                    System.out.println("未获取到图书分类数据，显示空图表");
                }
            } catch (Exception e) {
                // 异常时显示空图表
                System.err.println("获取图书分类数据异常: " + e.getMessage());
            }
            
            JFreeChart chart = ChartFactory.createPieChart(
                "图书分类分布",
                dataset,
                true, // 显示图例
                true, // 显示工具提示
                false // 不生成URL
            );
            
            // 设置标题字体
            chart.getTitle().setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
            
            // 设置颜色和样式
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setSectionPaint("文学", new Color(52, 152, 219));
            plot.setSectionPaint("科技", new Color(46, 204, 113));
            plot.setSectionPaint("历史", new Color(155, 89, 182));
            plot.setSectionPaint("艺术", new Color(241, 196, 15));
            plot.setSectionPaint("其他", new Color(231, 76, 60));
            
            // 设置标签样式 - 不显示具体占比数据
            plot.setLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            plot.setLabelBackgroundPaint(Color.WHITE);
            plot.setLabelOutlinePaint(Color.BLACK);
            plot.setLabelShadowPaint(Color.LIGHT_GRAY);
            // 不显示标签，避免显示不开的问题
            plot.setLabelGenerator(null);
            
            // 设置图表背景为白色
            chart.setBackgroundPaint(Color.WHITE);
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);
            
            // 设置图例字体
            chart.getLegend().setItemFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
            
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(400, 300));
            chartPanel.setMaximumSize(new Dimension(400, 300)); // 确保高度一致
            chartPanel.setBorder(BorderFactory.createTitledBorder("图书分类统计"));
            
            // 创建包含图表和详情按钮的面板
            JPanel chartContainer = new JPanel(new BorderLayout());
            chartContainer.add(chartPanel, BorderLayout.CENTER);
            
            // 创建详情按钮
            JButton detailButton = new JButton("查看详情");
            detailButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
            detailButton.setPreferredSize(new Dimension(80, 30));
            detailButton.setMargin(new Insets(2, 8, 2, 8));
            detailButton.addActionListener(e -> {
                cardLayout.show(contentPanel, "detail");
            });
            
            // 将按钮放在右下角
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setOpaque(false);
            buttonPanel.add(detailButton);
            chartContainer.add(buttonPanel, BorderLayout.SOUTH);
            
            return chartContainer;
        }
        
        /**
         * 创建借阅率柱状图
         */
        private JPanel createBorrowRateBarChart() {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            // 从服务器获取真实数据（前10名）
            try {
                System.out.println("开始获取借阅率数据...");
                Map<String, Object> borrowRateData = getBorrowRateData();
                System.out.println("获取到的借阅率数据: " + borrowRateData);
                if (borrowRateData != null && borrowRateData.containsKey("rates")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Double> rates = (Map<String, Double>) borrowRateData.get("rates");
                    System.out.println("借阅率详情: " + rates);
                    for (Map.Entry<String, Double> entry : rates.entrySet()) {
                        // 直接使用分类代码
                        String categoryCode = entry.getKey();
                        System.out.println("添加借阅率: " + categoryCode + " = " + entry.getValue() + "%");
                        dataset.addValue(entry.getValue(), "借阅率", categoryCode);
                    }
                } else {
                    // 如果获取失败，显示空图表
                    System.out.println("未获取到借阅率数据，显示空图表");
                }
            } catch (Exception e) {
                // 异常时显示空图表
                System.err.println("获取借阅率数据异常: " + e.getMessage());
            }
            
            JFreeChart chart = ChartFactory.createBarChart(
                "各类图书借阅率",
                "图书分类",
                "借阅率 (%)",
                dataset
            );
            
            // 设置样式
            CategoryPlot plot = chart.getCategoryPlot();
            
            // 创建自定义渲染器来设置每个柱子的不同颜色
            BarRenderer renderer = new BarRenderer() {
                @Override
                public java.awt.Paint getItemPaint(int row, int column) {
                    // 根据列索引返回不同颜色（支持前10名）
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
                        new Color(192, 57, 43)     // 深红色
                    };
                    return colors[column % colors.length];
                }
            };
            
            plot.setRenderer(renderer);
            renderer.setGradientPaintTransformer(null); // 禁用渐变，使用纯色
            renderer.setDrawBarOutline(false); // 不绘制柱子边框
            
            // 设置字体
            Font chineseFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
            Font titleFont = new Font("Microsoft YaHei", Font.BOLD, 16);
            
            chart.getTitle().setFont(titleFont);
            chart.getLegend().setItemFont(chineseFont);
            plot.getDomainAxis().setLabelFont(chineseFont);
            plot.getRangeAxis().setLabelFont(chineseFont);
            plot.getDomainAxis().setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            plot.getRangeAxis().setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            
            // 设置Y轴范围，确保借阅率不超过100%
            plot.getRangeAxis().setRange(0, 100);
            
            // 设置图表背景为白色
            chart.setBackgroundPaint(Color.WHITE);
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);
            
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(400, 300));
            chartPanel.setMaximumSize(new Dimension(400, 300)); // 确保高度一致
            chartPanel.setBorder(BorderFactory.createTitledBorder("借阅率统计"));
            
            return chartPanel;
        }
        
        /**
         * 创建借阅趋势折线图
         */
        private JPanel createBorrowTrendLineChart() {
            XYSeriesCollection dataset = new XYSeriesCollection();
            XYSeries series = new XYSeries("借阅数量");
            
            // 生成最近7天的借阅数据
            String[] days = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
            double[] values = {85, 92, 78, 95, 88, 65, 72};
            
            for (int i = 0; i < days.length; i++) {
                series.add(i, values[i]);
            }
            
            dataset.addSeries(series);
            
            JFreeChart chart = ChartFactory.createXYLineChart(
                "借阅趋势",
                "日期",
                "借阅数量",
                dataset
            );
            
            // 设置样式
            XYPlot plot = (XYPlot) chart.getPlot();
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, new Color(46, 204, 113));
            renderer.setSeriesShapesVisible(0, true);
            renderer.setSeriesShapesFilled(0, true);
            renderer.setSeriesStroke(0, new BasicStroke(2.0f));
            
            // 设置字体
            Font chineseFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
            Font titleFont = new Font("Microsoft YaHei", Font.BOLD, 16);
            
            chart.getTitle().setFont(titleFont);
            chart.getLegend().setItemFont(chineseFont);
            plot.getDomainAxis().setLabelFont(chineseFont);
            plot.getRangeAxis().setLabelFont(chineseFont);
            plot.getDomainAxis().setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            plot.getRangeAxis().setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            
            // 设置X轴标签
            NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
            domainAxis.setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            
            // 设置图表背景为白色
            chart.setBackgroundPaint(Color.WHITE);
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);
            
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(400, 300));
            chartPanel.setMaximumSize(new Dimension(400, 300)); // 确保高度一致
            chartPanel.setBorder(BorderFactory.createTitledBorder("借阅趋势分析"));
            
            return chartPanel;
        }
        
        /**
         * 创建用户活跃度图表 - 折线图显示近三天数据
         */
        private JPanel createUserActivityChart() {
            // 获取真实数据
            Map<String, Object> data = getUserActivityData();
            
            XYSeriesCollection dataset = new XYSeriesCollection();
            XYSeries activeUsersSeries = new XYSeries("活跃用户");
            
            // 根据时间周期生成数据
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> activityData = (List<Map<String, Object>>) data.get("activity");
            
            for (int i = 0; i < activityData.size(); i++) {
                Map<String, Object> dayData = activityData.get(i);
                String date = (String) dayData.get("date");
                Object activeUsersObj = dayData.get("activeUsers");
                
                int activeUsers = 0;
                if (activeUsersObj instanceof Number) {
                    activeUsers = ((Number) activeUsersObj).intValue();
                } else if (activeUsersObj instanceof String) {
                    try {
                        activeUsers = Integer.parseInt((String) activeUsersObj);
                    } catch (NumberFormatException e) {
                        activeUsers = 0;
                    }
                }
                
                System.out.println("用户活跃数据 - 日期: " + date + ", 活跃用户数: " + activeUsers);
                activeUsersSeries.add(i, activeUsers);
            }
            
            dataset.addSeries(activeUsersSeries);
            
            JFreeChart chart = ChartFactory.createXYLineChart(
                "用户活跃度统计（近三天）",
                "日期",
                "活跃用户数",
                dataset
            );
            
            // 设置样式
            XYPlot plot = (XYPlot) chart.getPlot();
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
            
            // 设置线条样式
            renderer.setSeriesPaint(0, new Color(52, 152, 219));  // 蓝色
            renderer.setSeriesShapesVisible(0, true);
            renderer.setSeriesShapesFilled(0, true);
            renderer.setSeriesStroke(0, new BasicStroke(3.0f));
            renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-4, -4, 8, 8));
            
            // 设置字体
            Font chineseFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
            Font titleFont = new Font("Microsoft YaHei", Font.BOLD, 16);
            
            chart.getTitle().setFont(titleFont);
            chart.getLegend().setItemFont(chineseFont);
            plot.getDomainAxis().setLabelFont(chineseFont);
            plot.getRangeAxis().setLabelFont(chineseFont);
            plot.getDomainAxis().setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            plot.getRangeAxis().setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            
            // 设置X轴为整数刻度
            NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
            domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            if (activityData.size() > 1) {
                domainAxis.setRange(0, activityData.size() - 1);
            } else if (activityData.size() == 1) {
                domainAxis.setRange(0, 1);
            } else {
                domainAxis.setRange(0, 1);
            }
            
            // 创建自定义标签生成器显示日期
            if (activityData.size() > 0) {
                String[] dates = new String[activityData.size()];
                for (int i = 0; i < activityData.size(); i++) {
                    dates[i] = (String) activityData.get(i).get("date");
                }
                org.jfree.chart.axis.SymbolAxis symbolAxis = new org.jfree.chart.axis.SymbolAxis("日期", dates);
                symbolAxis.setRange(0, activityData.size() - 1);
                symbolAxis.setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
                symbolAxis.setVerticalTickLabels(false);
                plot.setDomainAxis(symbolAxis);
            }
            
            // 设置Y轴为整数刻度，根据实际数据自适应范围
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            
            // 计算Y轴范围
            int maxActiveUsers = 0;
            for (Map<String, Object> dayData : activityData) {
                Object activeUsersObj = dayData.get("activeUsers");
                int activeUsers = 0;
                if (activeUsersObj instanceof Number) {
                    activeUsers = ((Number) activeUsersObj).intValue();
                }
                maxActiveUsers = Math.max(maxActiveUsers, activeUsers);
            }
            
            if (maxActiveUsers == 0) {
                rangeAxis.setRange(0, 10);
            } else {
                rangeAxis.setRange(0, maxActiveUsers * 1.1);
            }
            
            // 设置网格线为不可见，统一白色背景
            plot.setDomainGridlinesVisible(false);
            plot.setRangeGridlinesVisible(false);
            
            // 设置图表背景为白色
            chart.setBackgroundPaint(Color.WHITE);
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);
            
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(400, 300));
            chartPanel.setMaximumSize(new Dimension(400, 300)); // 确保高度一致
            chartPanel.setBorder(BorderFactory.createTitledBorder("用户活跃度"));
            
            return chartPanel;
        }
        
        /**
         * 获取用户活跃度数据
         */
        private Map<String, Object> getUserActivityData() {
            try {
                // 调用服务器API获取用户活跃度数据
                com.vcampus.common.message.Request request = new com.vcampus.common.message.Request("library/admin/statistics/user-activity", Map.of());
                
                com.vcampus.common.message.Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                
                if (response != null && ("200".equals(response.getStatus()) || "SUCCESS".equals(response.getStatus()))) {
                    Object data = response.getData();
                    if (data instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> dataMap = (Map<String, Object>) data;
                        System.out.println("获取用户活跃度数据成功: " + dataMap);
                        return dataMap;
                    }
                } else {
                    System.err.println("获取用户活跃度数据失败: " + (response != null ? response.getMessage() : "无响应"));
                }
            } catch (Exception e) {
                System.err.println("获取用户活跃度数据异常: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 返回默认数据
            Map<String, Object> defaultData = new HashMap<>();
            List<Map<String, Object>> defaultActivity = new ArrayList<>();
            for (int i = 2; i >= 0; i--) {
                Map<String, Object> dayData = new HashMap<>();
                LocalDate date = LocalDate.now().minusDays(i);
                dayData.put("date", date.format(DateTimeFormatter.ofPattern("M-d")));
                dayData.put("activeUsers", 0);
                defaultActivity.add(dayData);
            }
            defaultData.put("activity", defaultActivity);
            return defaultData;
        }
    }
    
    /**
     * 高级统计分析面板
     * 支持真实数据获取和交互式图表
     */
    private class AdvancedStatisticsPanel extends JPanel {
        private final NettyClient nettyClient;
        private final Map<String, Object> userData;
        private JTabbedPane timePeriodTabs;
        private ChartPanel currentChartPanel;
        
        // 添加动态布局相关字段
        private JPanel mainContainerPanel;
        private JPanel cardsPanel;
        private JPanel rightPanel;
        private boolean isRankingVisible = false;
        
        public AdvancedStatisticsPanel(NettyClient nettyClient, Map<String, Object> userData) {
            this.nettyClient = nettyClient;
            this.userData = userData;
            initializePanel();
        }
        
        private void initializePanel() {
            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            
            // 创建主容器面板
            mainContainerPanel = new JPanel(new BorderLayout());
            mainContainerPanel.setBackground(Color.WHITE);
            
            // 创建数据卡片面板
            cardsPanel = createStatisticsCardsPanel();
            mainContainerPanel.add(cardsPanel, BorderLayout.CENTER);
            
            add(mainContainerPanel, BorderLayout.CENTER);
        }
        
        /**
         * 刷新详细分析图表
         */
        public void refreshCharts() {
            System.out.println("刷新详细分析图表...");
            
            // 刷新统计卡片
            if (cardsPanel != null) {
                cardsPanel.removeAll();
                cardsPanel.add(createStatisticsCardsPanel());
                cardsPanel.revalidate();
                cardsPanel.repaint();
            }
            
            System.out.println("详细分析图表刷新完成");
        }
        
        /**
         * 创建统计卡片面板
         */
        private JPanel createStatisticsCardsPanel() {
            JPanel panel = new JPanel(new GridLayout(2, 3, 20, 20));
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            // 获取真实统计数据
            Map<String, Object> statistics = getRealTimeStatistics();
            
            // 添加各种统计卡片
            panel.add(createStatCard("今日借阅", String.valueOf(statistics.get("todayBorrows")), "本", 
                new Color(52, 152, 219), "", "BORROW_TODAY"));
            panel.add(createStatCard("今日归还", String.valueOf(statistics.get("todayReturns")), "本", 
                new Color(46, 204, 113), "", "RETURN_TODAY"));
            panel.add(createStatCard("活跃用户", String.valueOf(statistics.get("activeUsers")), "人", 
                new Color(155, 89, 182), "", "ACTIVE_USERS"));
            panel.add(createStatCard("在馆图书", String.valueOf(statistics.get("availableBooks")), "本", 
                new Color(241, 196, 15), "", "BOOKS_IN_LIBRARY"));
            panel.add(createStatCard("借出图书", String.valueOf(statistics.get("borrowedBooks")), "本", 
                new Color(231, 76, 60), "", "BORROWED_BOOKS"));
            panel.add(createStatCard("逾期图书", String.valueOf(statistics.get("overdueBooks")), "本", 
                new Color(230, 126, 34), "", "OVERDUE_BOOKS"));
            
            return panel;
        }
        
        /**
         * 显示用户排名图表
         */
        public void showUserRanking() {
            if (isRankingVisible) return;
            
            try {
                // 创建右侧排名面板
                rightPanel = createUserRankingPanel();
                
                // 重新布局主容器
                mainContainerPanel.removeAll();
                mainContainerPanel.setLayout(new BorderLayout(10, 0));
                
                // 添加左侧卡片面板（压缩宽度）
                mainContainerPanel.add(cardsPanel, BorderLayout.WEST);
                
                // 添加右侧排名面板
                mainContainerPanel.add(rightPanel, BorderLayout.CENTER);
                
                isRankingVisible = true;
                mainContainerPanel.revalidate();
                mainContainerPanel.repaint();
                
            } catch (Exception e) {
                log.error("显示用户排名失败", e);
                JOptionPane.showMessageDialog(this, "显示用户排名失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        /**
         * 隐藏用户排名图表
         */
        public void hideUserRanking() {
            if (!isRankingVisible) return;
            
            // 重新布局主容器
            mainContainerPanel.removeAll();
            mainContainerPanel.setLayout(new BorderLayout());
            
            // 只显示卡片面板
            mainContainerPanel.add(cardsPanel, BorderLayout.CENTER);
            
            isRankingVisible = false;
            mainContainerPanel.revalidate();
            mainContainerPanel.repaint();
        }
        
        /**
         * 创建用户排名面板
         */
        private JPanel createUserRankingPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
            ));
            
            // 创建顶部面板，包含标题和关闭按钮
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setBackground(Color.WHITE);
            
            // 创建标题
            JLabel titleLabel = new JLabel("用户借阅排名 TOP 10", JLabel.CENTER);
            titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
            titleLabel.setForeground(new Color(52, 73, 94));
            
            // 创建关闭按钮
            JButton closeButton = new JButton("×");
            closeButton.setFont(new Font("Arial", Font.BOLD, 16));
            closeButton.setForeground(Color.GRAY);
            closeButton.setBackground(Color.WHITE);
            closeButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            closeButton.setFocusPainted(false);
            closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            closeButton.addActionListener(e -> hideUserRanking());
            
            topPanel.add(titleLabel, BorderLayout.CENTER);
            topPanel.add(closeButton, BorderLayout.EAST);
            
            // 创建图表面板
            JPanel chartPanel = createUserRankingChartPanel();
            
            panel.add(topPanel, BorderLayout.NORTH);
            panel.add(chartPanel, BorderLayout.CENTER);
            
            return panel;
        }
        
        /**
         * 创建用户排名图表面板
         */
        private JPanel createUserRankingChartPanel() {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(Color.WHITE);
            
            try {
                // 获取用户借阅排名数据
                List<Map<String, Object>> userRankingData = getUserBorrowRanking();
                
                if (userRankingData.isEmpty()) {
                    // 如果没有数据，显示提示信息
                    JLabel noDataLabel = new JLabel("暂无用户借阅数据", JLabel.CENTER);
                    noDataLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                    noDataLabel.setForeground(Color.GRAY);
                    panel.add(noDataLabel, BorderLayout.CENTER);
                    return panel;
                }
                
                // 创建横柱状图
                JPanel chartPanel = createHorizontalBarChart(userRankingData);
                panel.add(chartPanel, BorderLayout.CENTER);
                
            } catch (Exception e) {
                log.error("创建用户排名图表失败", e);
                JLabel errorLabel = new JLabel("加载数据失败", JLabel.CENTER);
                errorLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                errorLabel.setForeground(Color.RED);
                panel.add(errorLabel, BorderLayout.CENTER);
            }
            
            return panel;
        }
        
        /**
         * 创建统计卡片
         */
        private JPanel createStatCard(String title, String value, String unit, Color color, String icon, String actionType) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(Color.WHITE);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            ));
            
            // 创建顶部标题
            JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topPanel.setBackground(Color.WHITE);
            
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
            titleLabel.setForeground(new Color(100, 100, 100));
            
            topPanel.add(titleLabel);
            
            // 创建数值显示
            JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            valuePanel.setBackground(Color.WHITE);
            
            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 32));
            valueLabel.setForeground(color);
            
            JLabel unitLabel = new JLabel(unit);
            unitLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
            unitLabel.setForeground(new Color(150, 150, 150));
            
            valuePanel.add(valueLabel);
            valuePanel.add(unitLabel);
            
            // 添加到底部
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBackground(Color.WHITE);
            bottomPanel.add(valuePanel, BorderLayout.CENTER);
            
            // 添加渐变背景效果
            card.setBackground(new Color(250, 250, 250));
            
            card.add(topPanel, BorderLayout.NORTH);
            card.add(bottomPanel, BorderLayout.CENTER);
            
            // 添加双击事件监听器和鼠标悬停效果
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            final Color originalColor = card.getBackground();
            final Color hoverColor = new Color(220, 240, 255); // 浅蓝色
            
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        handleCardDoubleClick(actionType);
                    }
                }
                
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    card.setBackground(hoverColor);
                    card.repaint();
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    card.setBackground(originalColor);
                    card.repaint();
                }
            });
            
            return card;
        }
        
        /**
         * 获取实时统计数据
         */
        private Map<String, Object> getRealTimeStatistics() {
            Map<String, Object> statistics = new HashMap<>();
            
            try {
                // 获取今日借阅和归还数据
                String today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                log.info("获取统计数据，今日日期: {}", today);
                
                int todayBorrows = getTodayBorrowCount(today);
                int todayReturns = getTodayReturnCount(today);
                log.info("今日借阅: {}, 今日归还: {}", todayBorrows, todayReturns);
                
                // 获取图书统计数据
                long totalBooks = getTotalBooksCount();
                long availableBooks = getAvailableBooksCount();
                long borrowedBooks = getBorrowedBooksCount();
                long overdueBooks = getOverdueBooksCount();
                log.info("图书统计 - 总数: {}, 可借: {}, 借出: {}, 逾期: {}", totalBooks, availableBooks, borrowedBooks, overdueBooks);
                
                // 获取活跃用户数（所有借阅过图书的用户）
                int activeUsers = getAllActiveUsersCount();
                log.info("活跃用户数: {}", activeUsers);
                
                statistics.put("todayBorrows", todayBorrows);
                statistics.put("todayReturns", todayReturns);
                statistics.put("activeUsers", activeUsers);
                statistics.put("availableBooks", availableBooks);
                statistics.put("borrowedBooks", borrowedBooks);
                statistics.put("overdueBooks", overdueBooks);
                
            } catch (Exception e) {
                log.error("获取统计数据失败", e);
                // 如果获取失败，使用默认值
                statistics.put("todayBorrows", 0);
                statistics.put("todayReturns", 0);
                statistics.put("activeUsers", 0);
                statistics.put("availableBooks", 0);
                statistics.put("borrowedBooks", 0);
                statistics.put("overdueBooks", 0);
            }
            
            return statistics;
        }
        
        /**
         * 处理卡片双击事件
         */
        private void handleCardDoubleClick(String actionType) {
            try {
                switch (actionType) {
                    case "BOOKS_IN_LIBRARY":
                        // 跳转到图书管理主界面
                        openBookManagement();
                        break;
                    case "BORROW_TODAY":
                        // 跳转到借阅管理界面，按今天日期搜索，类型为全部
                        openBorrowManagement("ALL", java.time.LocalDate.now().toString());
                        break;
                    case "RETURN_TODAY":
                        // 跳转到借阅管理界面，按今天归还日期搜索
                        openBorrowManagementByReturnDate(java.time.LocalDate.now().toString());
                        break;
                    case "ACTIVE_USERS":
                        // 在主界面右侧显示活跃用户借阅排名
                        showActiveUsersRankingInMain();
                        break;
                    case "OVERDUE_BOOKS":
                        // 跳转到借阅管理界面，类型为逾期，不选择日期
                        openBorrowManagement("OVERDUE", null);
                        break;
                    default:
                        // 其他卡片暂时不处理
                        break;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "跳转失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        /**
         * 打开图书管理界面
         */
        private void openBookManagement() {
            try {
                if (parentTabbedPane != null) {
                    // 切换到图书管理选项卡
                    parentTabbedPane.setSelectedIndex(0); // 图书管理是第一个选项卡
                } else {
                    JOptionPane.showMessageDialog(this, "无法切换到图书管理界面", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "无法切换到图书管理界面: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        /**
         * 打开借阅管理界面
         */
        private void openBorrowManagement(String type, String date) {
            try {
                if (parentTabbedPane != null) {
                    // 切换到借阅管理选项卡（第二个选项卡）
                    parentTabbedPane.setSelectedIndex(1);
                    
                    // 设置搜索条件
                    if (type != null || date != null) {
                        SwingUtilities.invokeLater(() -> {
                            setBorrowSearchConditionsFromTab(type, date);
                        });
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "无法切换到借阅管理界面", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "无法切换到借阅管理界面: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        /**
         * 打开借阅管理界面并按归还日期搜索
         */
        private void openBorrowManagementByReturnDate(String date) {
            try {
                if (parentTabbedPane != null) {
                    // 切换到借阅管理选项卡（第二个选项卡）
                    parentTabbedPane.setSelectedIndex(1);
                    
                    // 设置搜索条件
                    if (date != null) {
                        SwingUtilities.invokeLater(() -> {
                            setBorrowSearchConditionsByReturnDate(date);
                        });
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "无法切换到借阅管理界面", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "无法切换到借阅管理界面: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        /**
         * 从选项卡中设置借阅管理界面的搜索条件
         */
        private void setBorrowSearchConditionsFromTab(String type, String date) {
            try {
                if (parentTabbedPane != null) {
                    // 获取借阅管理选项卡中的BorrowManagementPanel
                    Component borrowTab = parentTabbedPane.getComponentAt(1);
                    if (borrowTab instanceof BorrowManagementPanel) {
                        BorrowManagementPanel borrowPanel = (BorrowManagementPanel) borrowTab;
                        setBorrowSearchConditions(borrowPanel, type, date);
                    }
                }
            } catch (Exception e) {
                log.warn("设置借阅管理搜索条件失败: {}", e.getMessage());
            }
        }
        
        /**
         * 设置借阅管理界面的归还日期搜索条件
         */
        private void setBorrowSearchConditionsByReturnDate(String date) {
            try {
                if (parentTabbedPane != null) {
                    // 获取借阅管理选项卡中的BorrowManagementPanel
                    Component borrowTab = parentTabbedPane.getComponentAt(1);
                    if (borrowTab instanceof BorrowManagementPanel) {
                        BorrowManagementPanel borrowPanel = (BorrowManagementPanel) borrowTab;
                        setBorrowReturnDateSearchConditions(borrowPanel, date);
                    }
                }
            } catch (Exception e) {
                log.warn("设置借阅管理归还日期搜索条件失败: {}", e.getMessage());
            }
        }
        
        /**
         * 设置借阅管理界面的搜索条件
         */
        private void setBorrowSearchConditions(BorrowManagementPanel borrowPanel, String type, String date) {
            try {
                // 使用反射来访问BorrowManagementPanel的私有字段
                java.lang.reflect.Field statusComboField = BorrowManagementPanel.class.getDeclaredField("statusCombo");
                statusComboField.setAccessible(true);
                JComboBox<String> statusCombo = (JComboBox<String>) statusComboField.get(borrowPanel);
                
                // 设置状态类型
                if ("OVERDUE".equals(type)) {
                    statusCombo.setSelectedItem("逾期");
                } else if ("ALL".equals(type)) {
                    statusCombo.setSelectedItem("全部");
                }
                
                // 设置日期
                if (date != null) {
                    java.lang.reflect.Field yearComboField = BorrowManagementPanel.class.getDeclaredField("yearCombo");
                    yearComboField.setAccessible(true);
                    JComboBox<String> yearCombo = (JComboBox<String>) yearComboField.get(borrowPanel);
                    
                    java.lang.reflect.Field monthComboField = BorrowManagementPanel.class.getDeclaredField("monthCombo");
                    monthComboField.setAccessible(true);
                    JComboBox<String> monthCombo = (JComboBox<String>) monthComboField.get(borrowPanel);
                    
                    java.lang.reflect.Field dayComboField = BorrowManagementPanel.class.getDeclaredField("dayCombo");
                    dayComboField.setAccessible(true);
                    JComboBox<String> dayCombo = (JComboBox<String>) dayComboField.get(borrowPanel);
                    
                    // 解析日期
                    String[] dateParts = date.split("-");
                    if (dateParts.length >= 3) {
                        yearCombo.setSelectedItem(dateParts[0]);
                        monthCombo.setSelectedItem(dateParts[1]);
                        dayCombo.setSelectedItem(dateParts[2]);
                    } else if (dateParts.length == 2) {
                        yearCombo.setSelectedItem(dateParts[0]);
                        monthCombo.setSelectedItem(dateParts[1]);
                        dayCombo.setSelectedItem("全部");
                    } else if (dateParts.length == 1) {
                        yearCombo.setSelectedItem(dateParts[0]);
                        monthCombo.setSelectedItem("全部");
                        dayCombo.setSelectedItem("全部");
                    }
                }
                
                // 触发搜索
                java.lang.reflect.Field searchButtonField = BorrowManagementPanel.class.getDeclaredField("searchButton");
                searchButtonField.setAccessible(true);
                JButton searchButton = (JButton) searchButtonField.get(borrowPanel);
                searchButton.doClick();
                
            } catch (Exception e) {
                // 如果反射失败，至少界面已经打开了
                log.warn("设置借阅管理搜索条件失败: {}", e.getMessage());
            }
        }
        
        /**
         * 设置借阅管理界面的归还日期搜索条件
         */
        private void setBorrowReturnDateSearchConditions(BorrowManagementPanel borrowPanel, String date) {
            try {
                // 使用反射来访问BorrowManagementPanel的私有字段
                
                // 设置状态为全部
                java.lang.reflect.Field statusComboField = BorrowManagementPanel.class.getDeclaredField("statusCombo");
                statusComboField.setAccessible(true);
                JComboBox<String> statusCombo = (JComboBox<String>) statusComboField.get(borrowPanel);
                statusCombo.setSelectedItem("全部");
                
                // 设置日期类型为归还日期
                java.lang.reflect.Field dateTypeComboField = BorrowManagementPanel.class.getDeclaredField("dateTypeCombo");
                dateTypeComboField.setAccessible(true);
                JComboBox<String> dateTypeCombo = (JComboBox<String>) dateTypeComboField.get(borrowPanel);
                dateTypeCombo.setSelectedItem("归还日期");
                
                // 设置日期
                if (date != null) {
                    java.lang.reflect.Field yearComboField = BorrowManagementPanel.class.getDeclaredField("yearCombo");
                    yearComboField.setAccessible(true);
                    JComboBox<String> yearCombo = (JComboBox<String>) yearComboField.get(borrowPanel);
                    
                    java.lang.reflect.Field monthComboField = BorrowManagementPanel.class.getDeclaredField("monthCombo");
                    monthComboField.setAccessible(true);
                    JComboBox<String> monthCombo = (JComboBox<String>) monthComboField.get(borrowPanel);
                    
                    java.lang.reflect.Field dayComboField = BorrowManagementPanel.class.getDeclaredField("dayCombo");
                    dayComboField.setAccessible(true);
                    JComboBox<String> dayCombo = (JComboBox<String>) dayComboField.get(borrowPanel);
                    
                    // 解析日期
                    String[] dateParts = date.split("-");
                    if (dateParts.length >= 3) {
                        yearCombo.setSelectedItem(dateParts[0]);
                        monthCombo.setSelectedItem(dateParts[1]);
                        dayCombo.setSelectedItem(dateParts[2]);
                    } else if (dateParts.length == 2) {
                        yearCombo.setSelectedItem(dateParts[0]);
                        monthCombo.setSelectedItem(dateParts[1]);
                        dayCombo.setSelectedItem("全部");
                    } else if (dateParts.length == 1) {
                        yearCombo.setSelectedItem(dateParts[0]);
                        monthCombo.setSelectedItem("全部");
                        dayCombo.setSelectedItem("全部");
                    }
                }
                
                // 触发搜索
                java.lang.reflect.Field searchButtonField = BorrowManagementPanel.class.getDeclaredField("searchButton");
                searchButtonField.setAccessible(true);
                JButton searchButton = (JButton) searchButtonField.get(borrowPanel);
                searchButton.doClick();
                
            } catch (Exception e) {
                // 如果反射失败，至少界面已经打开了
                log.warn("设置借阅管理归还日期搜索条件失败: {}", e.getMessage());
            }
        }
        
        /**
         * 获取今日借阅数量
         */
        private int getTodayBorrowCount(String date) {
            try {
                log.info("请求今日借阅数量，日期: {}", date);
                com.vcampus.common.message.Request request = new com.vcampus.common.message.Request("library/admin/statistics/today-borrows", Map.of("date", date));
                com.vcampus.common.message.Response response = nettyClient.sendRequest(request).get(5, java.util.concurrent.TimeUnit.SECONDS);
                
                log.info("今日借阅响应状态: {}, 数据: {}", response.getStatus(), response.getData());
                if ("200".equals(response.getStatus()) || "SUCCESS".equals(response.getStatus())) {
                    Map<String, Object> data = (Map<String, Object>) response.getData();
                    Object countObj = data.getOrDefault("count", 0);
                    int count = 0;
                    if (countObj instanceof Number) {
                        count = ((Number) countObj).intValue();
                    }
                    log.info("今日借阅数量: {}", count);
                    return count;
                }
            } catch (Exception e) {
                log.error("获取今日借阅数量失败", e);
            }
            return 0;
        }
        
        /**
         * 获取今日归还数量
         */
        private int getTodayReturnCount(String date) {
            try {
                log.info("请求今日归还数量，日期: {}", date);
                com.vcampus.common.message.Request request = new com.vcampus.common.message.Request("library/admin/statistics/today-returns", Map.of("date", date));
                com.vcampus.common.message.Response response = nettyClient.sendRequest(request).get(5, java.util.concurrent.TimeUnit.SECONDS);
                log.info("今日归还响应状态: {}, 数据: {}", response.getStatus(), response.getData());
                if ("200".equals(response.getStatus()) || "SUCCESS".equals(response.getStatus())) {
                    Map<String, Object> data = (Map<String, Object>) response.getData();
                    Object countObj = data.getOrDefault("count", 0);
                    int count = 0;
                    if (countObj instanceof Number) {
                        count = ((Number) countObj).intValue();
                    }
                    log.info("今日归还数量: {}", count);
                    return count;
                } else if ("NOT_FOUND".equals(response.getStatus())) {
                    log.warn("今日归还接口未找到，返回0");
                    return 0;
                }
            } catch (java.util.concurrent.TimeoutException e) {
                log.warn("获取今日归还数量超时，返回0");
            } catch (Exception e) {
                log.error("获取今日归还数量失败", e);
            }
            return 0;
        }
        
        /**
         * 获取图书总数
         */
        private long getTotalBooksCount() {
            try {
                com.vcampus.common.message.Request request = new com.vcampus.common.message.Request("library/admin/statistics/total-books", Map.of());
                com.vcampus.common.message.Response response = nettyClient.sendRequest(request).get(5, java.util.concurrent.TimeUnit.SECONDS);
                if ("200".equals(response.getStatus()) || "SUCCESS".equals(response.getStatus())) {
                    Map<String, Object> data = (Map<String, Object>) response.getData();
                    return ((Number) data.getOrDefault("count", 0)).longValue();
                } else if ("NOT_FOUND".equals(response.getStatus())) {
                    log.warn("图书总数接口未找到，返回0");
                    return 0;
                }
            } catch (java.util.concurrent.TimeoutException e) {
                log.warn("获取图书总数超时，返回0");
            } catch (Exception e) {
                log.error("获取图书总数失败", e);
            }
            return 0;
        }
        
        /**
         * 获取可借阅图书数量
         */
        private long getAvailableBooksCount() {
            try {
                com.vcampus.common.message.Request request = new com.vcampus.common.message.Request("library/admin/statistics/available-books", Map.of());
                com.vcampus.common.message.Response response = nettyClient.sendRequest(request).get(5, java.util.concurrent.TimeUnit.SECONDS);
                if ("200".equals(response.getStatus()) || "SUCCESS".equals(response.getStatus())) {
                    Map<String, Object> data = (Map<String, Object>) response.getData();
                    return ((Number) data.getOrDefault("count", 0)).longValue();
                } else if ("NOT_FOUND".equals(response.getStatus())) {
                    log.warn("可借阅图书接口未找到，返回0");
                    return 0;
                }
            } catch (java.util.concurrent.TimeoutException e) {
                log.warn("获取可借阅图书数量超时，返回0");
            } catch (Exception e) {
                log.error("获取可借阅图书数量失败", e);
            }
            return 0;
        }
        
        /**
         * 获取借出图书数量
         */
        private long getBorrowedBooksCount() {
            try {
                com.vcampus.common.message.Request request = new com.vcampus.common.message.Request("library/admin/statistics/borrowed-books", Map.of());
                com.vcampus.common.message.Response response = nettyClient.sendRequest(request).get(5, java.util.concurrent.TimeUnit.SECONDS);
                if ("200".equals(response.getStatus()) || "SUCCESS".equals(response.getStatus())) {
                    Map<String, Object> data = (Map<String, Object>) response.getData();
                    return ((Number) data.getOrDefault("count", 0)).longValue();
                } else if ("NOT_FOUND".equals(response.getStatus())) {
                    log.warn("借出图书接口未找到，返回0");
                    return 0;
                }
            } catch (java.util.concurrent.TimeoutException e) {
                log.warn("获取借出图书数量超时，返回0");
            } catch (Exception e) {
                log.error("获取借出图书数量失败", e);
            }
            return 0;
        }
        
        /**
         * 获取逾期图书数量
         */
        private long getOverdueBooksCount() {
            try {
                com.vcampus.common.message.Request request = new com.vcampus.common.message.Request("library/admin/statistics/overdue-books", Map.of());
                com.vcampus.common.message.Response response = nettyClient.sendRequest(request).get(5, java.util.concurrent.TimeUnit.SECONDS);
                if ("200".equals(response.getStatus()) || "SUCCESS".equals(response.getStatus())) {
                    Map<String, Object> data = (Map<String, Object>) response.getData();
                    return ((Number) data.getOrDefault("count", 0)).longValue();
                } else if ("NOT_FOUND".equals(response.getStatus())) {
                    log.warn("逾期图书接口未找到，返回0");
                    return 0;
                }
            } catch (java.util.concurrent.TimeoutException e) {
                log.warn("获取逾期图书数量超时，返回0");
            } catch (Exception e) {
                log.error("获取逾期图书数量失败", e);
            }
            return 0;
        }
        
        /**
         * 获取所有活跃用户数量（所有借阅过图书的用户）
         */
        private int getAllActiveUsersCount() {
            try {
                com.vcampus.common.message.Request request = new com.vcampus.common.message.Request("library/admin/statistics/all-active-users", Map.of());
                com.vcampus.common.message.Response response = nettyClient.sendRequest(request).get(5, java.util.concurrent.TimeUnit.SECONDS);
                
                if ("200".equals(response.getStatus()) || "SUCCESS".equals(response.getStatus())) {
                    Map<String, Object> data = (Map<String, Object>) response.getData();
                    Object countObj = data.getOrDefault("count", 0);
                    if (countObj instanceof Number) {
                        return ((Number) countObj).intValue();
                    }
                }
            } catch (Exception e) {
                // 如果获取失败，返回0
            }
            return 0;
        }
        
        private JPanel createTimePeriodPanel(String period) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBackground(Color.WHITE);
            
            // 创建图表
            JFreeChart chart = createInteractiveBorrowChart(period);
            currentChartPanel = new InteractiveChartPanel(chart);
            currentChartPanel.setPreferredSize(new Dimension(800, 500));
            currentChartPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)), 
                getPeriodTitle(period), 
                javax.swing.border.TitledBorder.LEFT, 
                javax.swing.border.TitledBorder.TOP,
                new Font("Microsoft YaHei", Font.BOLD, 16),
                new Color(52, 73, 94)
            ));
            
            panel.add(currentChartPanel, BorderLayout.CENTER);
            
            // 添加统计信息面板
            panel.add(createStatisticsInfoPanel(period), BorderLayout.SOUTH);
            
            return panel;
        }
        
        private String getPeriodTitle(String period) {
            switch (period) {
                case "4days": return "近4天借阅统计";
                case "3months": return "近3个月借阅统计";
                case "1year": return "近1年借阅统计";
                default: return "借阅统计";
            }
        }
        
        private JFreeChart createInteractiveBorrowChart(String period) {
            // 获取真实数据
            Map<String, Object> data = getBorrowStatisticsData(period);
            
            XYSeriesCollection dataset = new XYSeriesCollection();
            XYSeries borrowSeries = new XYSeries("借阅数量");
            XYSeries returnSeries = new XYSeries("归还数量");
            
            // 根据时间周期生成数据
            List<String> dates = (List<String>) data.get("dates");
            List<Integer> borrowCounts = (List<Integer>) data.get("borrowCounts");
            List<Integer> returnCounts = (List<Integer>) data.get("returnCounts");
            
            for (int i = 0; i < dates.size(); i++) {
                borrowSeries.add(i, borrowCounts.get(i));
                returnSeries.add(i, returnCounts.get(i));
            }
            
            dataset.addSeries(borrowSeries);
            dataset.addSeries(returnSeries);
            
            JFreeChart chart = ChartFactory.createXYLineChart(
                "",
                "日期",
                "数量",
                dataset
            );
            
            // 设置样式
            XYPlot plot = (XYPlot) chart.getPlot();
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
            
            // 设置线条样式 - 让两条线更容易区分
            renderer.setSeriesPaint(0, new Color(52, 152, 219));  // 借阅线：蓝色
            renderer.setSeriesPaint(1, new Color(231, 76, 60));   // 归还线：红色
            renderer.setSeriesShapesVisible(0, true);
            renderer.setSeriesShapesVisible(1, true);
            renderer.setSeriesShapesFilled(0, true);
            renderer.setSeriesShapesFilled(1, true);
            // 设置不同的线条粗细和样式
            renderer.setSeriesStroke(0, new BasicStroke(3.0f));  // 借阅线：粗实线
            renderer.setSeriesStroke(1, new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{8, 4}, 0)); // 归还线：虚线
            // 设置不同的点形状
            renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-3, -3, 6, 6)); // 借阅线：圆点
            renderer.setSeriesShape(1, new java.awt.geom.Rectangle2D.Double(-3, -3, 6, 6)); // 归还线：方点
            
            // 设置字体
            chart.getTitle().setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
            plot.getDomainAxis().setLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
            plot.getRangeAxis().setLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
            plot.getDomainAxis().setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
            plot.getRangeAxis().setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
            
            // 设置网格线为不可见，统一白色背景
            plot.setDomainGridlinesVisible(false);
            plot.setRangeGridlinesVisible(false);
            
            // 设置X轴为整数刻度
            NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
            domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            if (dates.size() > 1) {
                domainAxis.setRange(0, dates.size() - 1);
            } else if (dates.size() == 1) {
                domainAxis.setRange(0, 1);
            } else {
                domainAxis.setRange(0, 1);
            }
            
            // 设置X轴标签显示日期
            domainAxis.setNumberFormatOverride(new java.text.DecimalFormat("0"));
            domainAxis.setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
            
            // 创建自定义标签生成器
            if (dates.size() > 0) {
                org.jfree.chart.axis.SymbolAxis symbolAxis = new org.jfree.chart.axis.SymbolAxis("日期", dates.toArray(new String[0]));
                symbolAxis.setRange(0, dates.size() - 1);
                symbolAxis.setTickLabelFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
                symbolAxis.setVerticalTickLabels(false);
                plot.setDomainAxis(symbolAxis);
            }
            
            // 设置Y轴为整数刻度，根据实际数据自适应范围
            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            
            // 计算实际数据的最大值
            int maxBorrow = borrowCounts.isEmpty() ? 0 : Collections.max(borrowCounts);
            int maxReturn = returnCounts.isEmpty() ? 0 : Collections.max(returnCounts);
            int maxValue = Math.max(maxBorrow, maxReturn);
            
            // 设置Y轴范围，如果最大值为0则设为0-10，否则设为0到最大值的1.1倍
            if (maxValue == 0) {
                rangeAxis.setRange(0, 10);
            } else {
                rangeAxis.setRange(0, maxValue * 1.1);
            }
            
            // 设置背景
            plot.setBackgroundPaint(Color.WHITE);
            plot.setOutlineVisible(false);
            
            return chart;
        }
        
        private Map<String, Object> getBorrowStatisticsData(String period) {
            Map<String, Object> data = new HashMap<>();
            List<String> dates = new ArrayList<>();
            List<Integer> borrowCounts = new ArrayList<>();
            List<Integer> returnCounts = new ArrayList<>();
            
            java.time.LocalDate today = java.time.LocalDate.now();
            
            switch (period) {
                case "4days":
                    // 近4天：今天往前推3天
                    for (int i = 3; i >= 0; i--) {
                        java.time.LocalDate date = today.minusDays(i);
                        dates.add(date.format(java.time.format.DateTimeFormatter.ofPattern("M-d")));
                        borrowCounts.add(0); // 没有数据时设为0
                        returnCounts.add(0);
                    }
                    break;
                    
                case "3months":
                    // 近3个月：当前月份往前推2个月
                    for (int i = 2; i >= 0; i--) {
                        java.time.LocalDate month = today.minusMonths(i);
                        dates.add(month.format(java.time.format.DateTimeFormatter.ofPattern("M月")));
                        borrowCounts.add(0); // 没有数据时设为0
                        returnCounts.add(0);
                    }
                    break;
                    
                case "1year":
                    // 近1年：从1月到当前月份
                    for (int month = 1; month <= today.getMonthValue(); month++) {
                        dates.add(month + "月");
                        borrowCounts.add(0); // 没有数据时设为0
                        returnCounts.add(0);
                    }
                    break;
                    
                default:
                    // 默认近4天
                    for (int i = 3; i >= 0; i--) {
                        java.time.LocalDate date = today.minusDays(i);
                        dates.add(date.format(java.time.format.DateTimeFormatter.ofPattern("M-d")));
                        borrowCounts.add(0); // 没有数据时设为0
                        returnCounts.add(0);
                    }
                    break;
            }
            
            data.put("dates", dates);
            data.put("borrowCounts", borrowCounts);
            data.put("returnCounts", returnCounts);
            
            return data;
        }
        
        
        private JPanel createStatisticsInfoPanel(String period) {
            JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
            infoPanel.setBackground(new Color(248, 249, 250));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            
            // 获取统计数据
            Map<String, Object> stats = getStatisticsSummary(period);
            
            // 总借阅量
            JLabel totalBorrowLabel = createStatLabel("总借阅量", stats.get("totalBorrows").toString(), new Color(52, 152, 219));
            infoPanel.add(totalBorrowLabel);
            
            // 总归还量
            JLabel totalReturnLabel = createStatLabel("总归还量", stats.get("totalReturns").toString(), new Color(46, 204, 113));
            infoPanel.add(totalReturnLabel);
            
            // 平均日借阅量
            JLabel avgBorrowLabel = createStatLabel("平均日借阅量", String.format("%.1f", stats.get("avgBorrows")), new Color(155, 89, 182));
            infoPanel.add(avgBorrowLabel);
            
            // 借阅增长率
            JLabel growthRateLabel = createStatLabel("借阅增长率", String.format("%.1f%%", stats.get("growthRate")), new Color(241, 196, 15));
            infoPanel.add(growthRateLabel);
            
            return infoPanel;
        }
        
        private JLabel createStatLabel(String title, String value, Color color) {
            JPanel statPanel = new JPanel(new BorderLayout());
            statPanel.setBackground(Color.WHITE);
            statPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));
            
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
            titleLabel.setForeground(new Color(100, 100, 100));
            
            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
            valueLabel.setForeground(color);
            valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            statPanel.add(titleLabel, BorderLayout.NORTH);
            statPanel.add(valueLabel, BorderLayout.CENTER);
            
            JLabel container = new JLabel();
            container.setLayout(new BorderLayout());
            container.add(statPanel, BorderLayout.CENTER);
            
            return container;
        }
        
        private Map<String, Object> getStatisticsSummary(String period) {
            Map<String, Object> summary = new HashMap<>();
            
            // 模拟统计数据（实际应该从数据库计算）
            int dataPoints = getDataPointsForPeriod(period);
            double totalBorrows = dataPoints * (50 + Math.random() * 100);
            double totalReturns = dataPoints * (45 + Math.random() * 90);
            double avgBorrows = totalBorrows / dataPoints;
            double growthRate = -5 + Math.random() * 20; // -5% 到 15% 的增长率
            
            summary.put("totalBorrows", (int) totalBorrows);
            summary.put("totalReturns", (int) totalReturns);
            summary.put("avgBorrows", avgBorrows);
            summary.put("growthRate", growthRate);
            
            return summary;
        }
        
        private int getDataPointsForPeriod(String period) {
            switch (period) {
                case "4days": return 4;
                case "3months": return 3;
                case "1year": return java.time.LocalDate.now().getMonthValue();
                default: return 4;
            }
        }
    }
    
    /**
     * 交互式图表面板
     * 支持鼠标悬浮显示数据点信息
     */
    private static class InteractiveChartPanel extends ChartPanel {
        private JLabel tooltipLabel;
        
        public InteractiveChartPanel(JFreeChart chart) {
            super(chart);
            setMouseWheelEnabled(true);
            setDomainZoomable(true);
            setRangeZoomable(true);
            setPopupMenu(null);
            
            // 创建工具提示标签
            tooltipLabel = new JLabel();
            tooltipLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
            tooltipLabel.setBackground(new Color(0, 0, 0, 200));
            tooltipLabel.setForeground(Color.WHITE);
            tooltipLabel.setOpaque(true);
            tooltipLabel.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
            tooltipLabel.setVisible(false);
            
            add(tooltipLabel);
            
            // 添加鼠标监听器
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    showTooltip(e);
                }
                
                @Override
                public void mouseMoved(java.awt.event.MouseEvent e) {
                    showTooltip(e);
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hideTooltip();
                }
            });
        }
        
        private void showTooltip(java.awt.event.MouseEvent e) {
            // 获取鼠标位置对应的数据点
            Point2D point = translateScreenToJava2D(e.getPoint());
            if (point != null) {
                XYPlot plot = (XYPlot) getChart().getPlot();
                XYDataset dataset = plot.getDataset();
                
                if (dataset != null && dataset.getSeriesCount() > 0) {
                    // 找到最近的数据点
                    double x = plot.getDomainAxis().java2DToValue(point.getX(), getScreenDataArea(), plot.getDomainAxisEdge());
                    double y = plot.getRangeAxis().java2DToValue(point.getY(), getScreenDataArea(), plot.getRangeAxisEdge());
                    
                    // 显示工具提示
                    tooltipLabel.setText(String.format("日期: %.0f, 数量: %.1f", x, y));
                    tooltipLabel.setLocation(e.getX() + 10, e.getY() - 30);
                    tooltipLabel.setVisible(true);
                    repaint();
                }
            }
        }
        
        private void hideTooltip() {
            tooltipLabel.setVisible(false);
            repaint();
        }
    }
    
    /**
     * 刷新所有图表数据
     */
    public void refreshCharts() {
        System.out.println("开始刷新图表数据...");
        
        // 刷新数据看板
        if (dashboardPanel != null) {
            dashboardPanel.refreshCharts();
        }
        
        // 刷新详细分析
        if (statisticsPanel != null) {
            statisticsPanel.refreshCharts();
        }
        
        // 刷新详情界面的图书数量统计柱状图
        if (detailPanel != null) {
            detailPanel.refreshChart();
        }
        
        System.out.println("图表数据刷新完成");
    }
    
    /**
     * 获取图书分类统计数据
     */
    private Map<String, Object> getBookCategoryData() {
        try {
            // 通过NettyClient调用服务器API获取真实数据
            if (nettyClient != null) {
                try {
                    // 发送请求到服务器获取图书分类统计
                    com.vcampus.common.message.Request request = new com.vcampus.common.message.Request("library/admin/statistics/book-categories");
                    
                    com.vcampus.common.message.Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    if (response != null && response.isSuccess()) {
                        return (Map<String, Object>) response.getData();
                    }
                } catch (Exception e) {
                    System.err.println("网络请求失败: " + e.getMessage());
                }
            }
            
            // 网络请求失败时返回空数据
            return new HashMap<>();
        } catch (Exception e) {
            System.err.println("获取图书分类数据失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 将中文分类名称转换为中图法代码
     */
    private String getCategoryEnglishName(String chineseName) {
        switch (chineseName) {
            case "马克思主义、列宁主义、毛泽东思想、邓小平理论": return "A";
            case "哲学、宗教": return "B";
            case "社会科学总论": return "C";
            case "政治、法律": return "D";
            case "军事": return "E";
            case "经济": return "F";
            case "文化、科学、教育、体育": return "G";
            case "语言、文字": return "H";
            case "文学": return "I";
            case "艺术": return "J";
            case "历史、地理": return "K";
            case "自然科学总论": return "N";
            case "数理科学和化学": return "O";
            case "天文学、地球科学": return "P";
            case "生物科学": return "Q";
            case "医药、卫生": return "R";
            case "农业科学": return "S";
            case "工业技术": return "T";
            case "交通运输": return "U";
            case "航空、航天": return "V";
            case "环境科学、安全科学": return "X";
            case "综合性图书": return "Z";
            default: return "其他";
        }
    }
    
    /**
     * 获取借阅率统计数据（前10名）
     */
    private Map<String, Object> getBorrowRateData() {
        try {
            // 通过NettyClient调用服务器API获取真实数据
            if (nettyClient != null) {
                try {
                    // 发送请求到服务器获取借阅率统计
                    com.vcampus.common.message.Request request = new com.vcampus.common.message.Request("library/admin/statistics/borrow-rates");
                    
                    com.vcampus.common.message.Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
                    if (response != null && response.isSuccess()) {
                        return (Map<String, Object>) response.getData();
                    }
                } catch (Exception e) {
                    System.err.println("网络请求失败: " + e.getMessage());
                }
            }
            
            // 网络请求失败时返回空数据
            return new HashMap<>();
        } catch (Exception e) {
            System.err.println("获取借阅率数据失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 在主界面显示活跃用户借阅排名
     */
    private void showActiveUsersRankingInMain() {
        try {
            // 调用AdvancedStatisticsPanel的showUserRanking方法
            if (statisticsPanel instanceof AdvancedStatisticsPanel) {
                ((AdvancedStatisticsPanel) statisticsPanel).showUserRanking();
            }
            log.info("在主界面显示活跃用户排名");
        } catch (Exception e) {
            log.error("显示活跃用户排名失败", e);
            JOptionPane.showMessageDialog(this, "显示活跃用户排名失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 显示活跃用户借阅排名柱状图（弹出窗口方式，已弃用）
     */
    private void showActiveUsersRanking() {
        try {
            // 创建弹窗
            JDialog dialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), "活跃用户借阅排名", true);
            dialog.setSize(800, 600);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout());
            
            // 创建标题
            JLabel titleLabel = new JLabel("借阅数量排名 TOP 10", JLabel.CENTER);
            titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            titleLabel.setForeground(new Color(52, 73, 94));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
            dialog.add(titleLabel, BorderLayout.NORTH);
            
            // 创建图表面板
            JPanel chartPanel = createActiveUsersRankingChart();
            dialog.add(chartPanel, BorderLayout.CENTER);
            
            // 创建关闭按钮
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton closeButton = new JButton("关闭");
            closeButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            closeButton.setPreferredSize(new Dimension(80, 32));
            closeButton.setBackground(new Color(52, 152, 219));
            closeButton.setForeground(Color.WHITE);
            closeButton.setBorderPainted(false);
            closeButton.setFocusPainted(false);
            closeButton.addActionListener(e -> dialog.dispose());
            buttonPanel.add(closeButton);
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.setVisible(true);
            
        } catch (Exception e) {
            log.error("显示活跃用户排名失败", e);
            JOptionPane.showMessageDialog(this, "显示活跃用户排名失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * 创建活跃用户借阅排名柱状图
     */
    private JPanel createActiveUsersRankingChart() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        try {
            // 获取用户借阅排名数据
            List<Map<String, Object>> userRankingData = getUserBorrowRanking();
            
            if (userRankingData.isEmpty()) {
                // 如果没有数据，显示提示信息
                JLabel noDataLabel = new JLabel("暂无用户借阅数据", JLabel.CENTER);
                noDataLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                noDataLabel.setForeground(Color.GRAY);
                panel.add(noDataLabel, BorderLayout.CENTER);
                return panel;
            }
            
            // 创建横柱状图
            JPanel chartPanel = createHorizontalBarChart(userRankingData);
            panel.add(chartPanel, BorderLayout.CENTER);
            
        } catch (Exception e) {
            log.error("创建活跃用户排名图表失败", e);
            JLabel errorLabel = new JLabel("加载数据失败", JLabel.CENTER);
            errorLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            errorLabel.setForeground(Color.RED);
            panel.add(errorLabel, BorderLayout.CENTER);
        }
        
        return panel;
    }
    
    /**
     * 获取用户借阅排名数据
     */
    private List<Map<String, Object>> getUserBorrowRanking() {
        List<Map<String, Object>> rankingData = new ArrayList<>();
        
        try {
            // 通过NettyClient调用服务器API获取用户借阅排名
            if (nettyClient != null) {
                com.vcampus.common.message.Request request = new com.vcampus.common.message.Request("library/admin/statistics/user-borrow-ranking");
                // 减少超时时间到5秒，提高响应速度
                com.vcampus.common.message.Response response = nettyClient.sendRequest(request).get(5, java.util.concurrent.TimeUnit.SECONDS);
                
                if (response != null && response.isSuccess()) {
                    Map<String, Object> data = (Map<String, Object>) response.getData();
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> users = (List<Map<String, Object>>) data.get("users");
                    if (users != null) {
                        rankingData.addAll(users);
                    }
                } else {
                    log.warn("获取用户借阅排名失败: response={}", response);
                }
            }
        } catch (java.util.concurrent.TimeoutException e) {
            log.error("获取用户借阅排名超时", e);
            // 超时时显示空数据而不是模拟数据
        } catch (Exception e) {
            log.error("获取用户借阅排名数据失败", e);
        }
        
        return rankingData;
    }
    
    
    /**
     * 创建横柱状图
     */
    private JPanel createHorizontalBarChart(List<Map<String, Object>> data) {
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 设置字体
                g2d.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                
                int width = getWidth();
                int height = getHeight();
                int margin = 80; // 增加左边距以容纳用户名
                int chartWidth = width - margin - 20; // 右边距
                int chartHeight = height - 2 * margin;
                
                // 找到最大借阅数量
                int maxBorrowCount = 0;
                for (Map<String, Object> user : data) {
                    int count = ((Number) user.get("borrowCount")).intValue();
                    maxBorrowCount = Math.max(maxBorrowCount, count);
                }
                
                if (maxBorrowCount == 0) return;
                
                // 计算柱状图参数
                int barCount = Math.min(data.size(), 10);
                int barHeight = chartHeight / barCount - 8; // 每个柱子之间的间距
                int barSpacing = 8;
                
                // 智能计算Y轴刻度值
                int[] yAxisValues = calculateYAxisValues(maxBorrowCount);
                
                // 绘制坐标轴
                g2d.setColor(Color.BLACK);
                g2d.drawLine(margin, margin, margin, margin + chartHeight); // Y轴
                g2d.drawLine(margin, margin + chartHeight, margin + chartWidth, margin + chartHeight); // X轴
                
                // 绘制X轴刻度
                g2d.setColor(Color.GRAY);
                for (int value : yAxisValues) {
                    int x = margin + (int) ((double) value / maxBorrowCount * chartWidth);
                    g2d.drawString(String.valueOf(value), x - 10, margin + chartHeight + 15);
                    g2d.drawLine(x, margin + chartHeight, x, margin + chartHeight + 5);
                }
                
                // 绘制柱状图
                for (int i = 0; i < barCount; i++) {
                    Map<String, Object> user = data.get(i);
                    String userName = (String) user.get("userName");
                    int borrowCount = ((Number) user.get("borrowCount")).intValue();
                    
                    // 计算柱子位置和长度
                    int barLength = (int) ((double) borrowCount / maxBorrowCount * chartWidth);
                    int x = margin;
                    int y = margin + i * (barHeight + barSpacing);
                    
                    // 绘制柱子
                    Color barColor = new Color(52, 152, 219, 180); // 半透明蓝色
                    g2d.setColor(barColor);
                    g2d.fillRect(x, y, barLength, barHeight);
                    
                    // 绘制柱子边框
                    g2d.setColor(new Color(52, 152, 219));
                    g2d.drawRect(x, y, barLength, barHeight);
                    
                    // 绘制数值标签（在柱子末端）
                    g2d.setColor(Color.BLACK);
                    String countText = String.valueOf(borrowCount);
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString(countText, x + barLength + 5, y + barHeight / 2 + 4);
                    
                    // 绘制用户姓名标签（在左侧）
                    g2d.setColor(Color.BLACK);
                    int nameWidth = fm.stringWidth(userName);
                    // 如果用户名太长，截断显示
                    String displayName = userName;
                    if (nameWidth > margin - 10) {
                        displayName = userName.substring(0, Math.min(userName.length(), 8)) + "...";
                        nameWidth = fm.stringWidth(displayName);
                    }
                    g2d.drawString(displayName, margin - nameWidth - 5, y + barHeight / 2 + 4);
                }
                
                g2d.dispose();
            }
        };
        
        chartPanel.setPreferredSize(new Dimension(400, 300)); // 调整为适合主界面的尺寸
        chartPanel.setBackground(Color.WHITE);
        
        return chartPanel;
    }
    
    /**
     * 智能计算Y轴刻度值
     */
    private int[] calculateYAxisValues(int maxValue) {
        if (maxValue <= 0) return new int[]{0};
        
        // 计算合适的刻度间隔
        int interval = 1;
        if (maxValue <= 5) {
            interval = 1;
        } else if (maxValue <= 10) {
            interval = 2;
        } else if (maxValue <= 20) {
            interval = 5;
        } else if (maxValue <= 50) {
            interval = 10;
        } else if (maxValue <= 100) {
            interval = 20;
        } else {
            interval = (int) Math.ceil(maxValue / 5.0);
        }
        
        // 生成刻度值
        java.util.List<Integer> values = new ArrayList<>();
        for (int i = 0; i <= maxValue; i += interval) {
            values.add(i);
        }
        
        // 确保包含最大值
        if (values.get(values.size() - 1) < maxValue) {
            values.add(maxValue);
        }
        
        return values.stream().mapToInt(Integer::intValue).toArray();
    }
}



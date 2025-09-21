package com.vcampus.client.core.ui.library;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.client.core.ui.library.chart.ChartFactory;
import com.vcampus.client.core.ui.library.chart.StatisticsCard;
import com.vcampus.client.core.ui.library.chart.StatisticsCard.Preset;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
// import org.jfree.chart.ChartPanel;
// import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 统计分析面板
 * 提供图书管理系统的统计分析和报表功能界面
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class StatisticsPanel extends JPanel {
    
    private static final Logger log = LoggerFactory.getLogger(StatisticsPanel.class);
    
    private final NettyClient nettyClient;
    private final Map<String, Object> userData;
    
    // 统计卡片
    private StatisticsCard totalBooksCard;
    private StatisticsCard totalUsersCard;
    private StatisticsCard currentBorrowsCard;
    private StatisticsCard overdueCard;
    private StatisticsCard availableBooksCard;
    private StatisticsCard borrowRateCard;
    
    // 图表面板（暂时注释掉）
    // private ChartPanel categoryChartPanel;
    // private ChartPanel borrowTrendChartPanel;
    // private ChartPanel statusDistributionChartPanel;
    // private ChartPanel popularBooksChartPanel;
    
    // 数据表格
    private JTable popularBooksTable;
    private JTable overdueBooksTable;
    private JTable inventoryAlertsTable;
    
    // 刷新按钮
    private JButton refreshButton;
    
    /**
     * 构造函数
     */
    public StatisticsPanel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        initComponents();
        setupLayout();
        setupEventHandlers();
        loadStatistics();
    }
    
    /**
     * 初始化组件
     */
    private void initComponents() {
        // 创建统计卡片
        totalBooksCard = Preset.createBookCard("0");
        totalUsersCard = Preset.createUserCard("0");
        currentBorrowsCard = Preset.createBorrowCard("0");
        overdueCard = Preset.createOverdueCard("0");
        availableBooksCard = Preset.createInventoryCard("0");
        borrowRateCard = Preset.createBorrowRateCard("0");
        
        // 创建图表面板（暂时注释掉）
        // categoryChartPanel = new ChartPanel(null);
        // borrowTrendChartPanel = new ChartPanel(null);
        // statusDistributionChartPanel = new ChartPanel(null);
        // popularBooksChartPanel = new ChartPanel(null);
        
        // 创建数据表格
        createTables();
        
        // 创建刷新按钮
        refreshButton = createStyledButton("刷新数据", new Color(52, 152, 219));
    }
    
    /**
     * 创建数据表格
     */
    private void createTables() {
        // 热门图书表格
        String[] popularColumns = {"排名", "书名", "作者", "借阅次数", "分类"};
        popularBooksTable = createStyledTable(popularColumns);
        
        // 逾期图书表格
        String[] overdueColumns = {"书名", "借阅人", "借阅日期", "应还日期", "逾期天数"};
        overdueBooksTable = createStyledTable(overdueColumns);
        
        // 库存预警表格
        String[] inventoryColumns = {"书名", "作者", "当前库存", "最低库存", "状态"};
        inventoryAlertsTable = createStyledTable(inventoryColumns);
    }
    
    /**
     * 创建样式化表格
     */
    private JTable createStyledTable(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(52, 152, 219));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
        
        return table;
    }
    
    /**
     * 设置布局
     */
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 顶部：统计卡片区域
        JPanel cardsPanel = createCardsPanel();
        
        // 中间：图表区域
        JPanel chartsPanel = createChartsPanel();
        
        // 底部：数据表格区域
        JPanel tablesPanel = createTablesPanel();
        
        // 右侧：控制面板
        JPanel controlPanel = createControlPanel();
        
        // 主布局
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.add(cardsPanel, BorderLayout.NORTH);
        mainPanel.add(chartsPanel, BorderLayout.CENTER);
        mainPanel.add(tablesPanel, BorderLayout.SOUTH);
        
        JPanel contentPanel = new JPanel(new BorderLayout(10, 0));
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        contentPanel.add(controlPanel, BorderLayout.EAST);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    /**
     * 创建统计卡片面板
     */
    private JPanel createCardsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 3, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("关键指标"));
        
        panel.add(totalBooksCard);
        panel.add(totalUsersCard);
        panel.add(currentBorrowsCard);
        panel.add(overdueCard);
        panel.add(availableBooksCard);
        panel.add(borrowRateCard);
        
        return panel;
    }
    
    /**
     * 创建图表面板
     */
    private JPanel createChartsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("数据图表"));
        
        // 暂时显示占位符，等待图表功能完善
        JLabel placeholder1 = createPlaceholderLabel("图书分类统计图表");
        JLabel placeholder2 = createPlaceholderLabel("借阅趋势图表");
        JLabel placeholder3 = createPlaceholderLabel("借阅状态分布图表");
        JLabel placeholder4 = createPlaceholderLabel("热门图书图表");
        
        panel.add(placeholder1);
        panel.add(placeholder2);
        panel.add(placeholder3);
        panel.add(placeholder4);
        
        return panel;
    }
    
    /**
     * 创建占位符标签
     */
    private JLabel createPlaceholderLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        label.setForeground(new Color(150, 150, 150));
        label.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        label.setPreferredSize(new Dimension(200, 150));
        return label;
    }
    
    /**
     * 创建数据表格面板
     */
    private JPanel createTablesPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setBorder(BorderFactory.createTitledBorder("详细数据"));
        
        // 热门图书表格
        JPanel popularPanel = new JPanel(new BorderLayout());
        popularPanel.setBorder(BorderFactory.createTitledBorder("热门图书排行"));
        popularPanel.add(new JScrollPane(popularBooksTable), BorderLayout.CENTER);
        panel.add(popularPanel);
        
        // 逾期图书表格
        JPanel overduePanel = new JPanel(new BorderLayout());
        overduePanel.setBorder(BorderFactory.createTitledBorder("逾期图书"));
        overduePanel.add(new JScrollPane(overdueBooksTable), BorderLayout.CENTER);
        panel.add(overduePanel);
        
        // 库存预警表格
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        inventoryPanel.setBorder(BorderFactory.createTitledBorder("库存预警"));
        inventoryPanel.add(new JScrollPane(inventoryAlertsTable), BorderLayout.CENTER);
        panel.add(inventoryPanel);
        
        return panel;
    }
    
    /**
     * 创建控制面板
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(200, 0));
        panel.setBorder(BorderFactory.createTitledBorder("操作控制"));
        
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        buttonPanel.add(refreshButton);
        
        // 添加其他控制按钮
        JButton exportButton = createStyledButton("导出报表", new Color(46, 204, 113));
        JButton generateReportButton = createStyledButton("生成报表", new Color(155, 89, 182));
        
        buttonPanel.add(exportButton);
        buttonPanel.add(generateReportButton);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        refreshButton.addActionListener(e -> loadStatistics());
    }
    
    /**
     * 加载统计数据
     */
    private void loadStatistics() {
        // 显示加载状态
        showLoadingState();
        
        // 异步加载数据
        CompletableFuture.runAsync(() -> {
            try {
                loadRealData();
            } catch (Exception e) {
                log.error("加载统计数据失败", e);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "加载统计数据失败: " + e.getMessage(), 
                                                "错误", JOptionPane.ERROR_MESSAGE);
                    loadMockData(); // 失败时使用模拟数据
                });
            }
        });
    }
    
    /**
     * 显示加载状态
     */
    private void showLoadingState() {
        SwingUtilities.invokeLater(() -> {
            totalBooksCard.updateValue("加载中...");
            totalUsersCard.updateValue("加载中...");
            currentBorrowsCard.updateValue("加载中...");
            overdueCard.updateValue("加载中...");
            availableBooksCard.updateValue("加载中...");
            borrowRateCard.updateValue("加载中...");
        });
    }
    
    /**
     * 加载真实数据
     */
    private void loadRealData() {
        try {
            // 获取图书统计
            loadBookStatistics();
            
            // 获取借阅统计
            loadBorrowStatistics();
            
            // 获取用户统计
            loadUserStatistics();
            
            // 创建图表
            createRealCharts();
            
            // 更新表格
            updateTableData();
            
        } catch (Exception e) {
            log.error("加载真实数据失败", e);
            throw e;
        }
    }
    
    /**
     * 加载图书统计
     */
    private void loadBookStatistics() {
        try {
            // 获取图书总数
            Request request = new Request("admin/getBookStatistics");
            Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
            
            if (response != null && "SUCCESS".equals(response.getStatus())) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.getData();
                
                SwingUtilities.invokeLater(() -> {
                    totalBooksCard.updateValue(String.valueOf(data.getOrDefault("totalBooks", "0")));
                    availableBooksCard.updateValue(String.valueOf(data.getOrDefault("availableBooks", "0")));
                });
            }
        } catch (Exception e) {
            log.error("加载图书统计失败", e);
        }
    }
    
    /**
     * 加载借阅统计
     */
    private void loadBorrowStatistics() {
        try {
            // 获取借阅统计
            Request request = new Request("admin/getBorrowStatistics");
            Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
            
            if (response != null && "SUCCESS".equals(response.getStatus())) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.getData();
                
                SwingUtilities.invokeLater(() -> {
                    currentBorrowsCard.updateValue(String.valueOf(data.getOrDefault("currentBorrows", "0")));
                    overdueCard.updateValue(String.valueOf(data.getOrDefault("overdueBooks", "0")));
                    
                    // 计算借阅率
                    int totalBooks = Integer.parseInt(totalBooksCard.getValue());
                    int currentBorrows = Integer.parseInt(currentBorrowsCard.getValue());
                    double borrowRate = totalBooks > 0 ? (double) currentBorrows / totalBooks * 100 : 0;
                    borrowRateCard.updateValue(String.format("%.1f", borrowRate));
                });
            }
        } catch (Exception e) {
            log.error("加载借阅统计失败", e);
        }
    }
    
    /**
     * 加载用户统计
     */
    private void loadUserStatistics() {
        try {
            // 获取用户统计
            Request request = new Request("admin/getUserStatistics");
            Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
            
            if (response != null && "SUCCESS".equals(response.getStatus())) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.getData();
                
                SwingUtilities.invokeLater(() -> {
                    totalUsersCard.updateValue(String.valueOf(data.getOrDefault("totalUsers", "0")));
                });
            }
        } catch (Exception e) {
            log.error("加载用户统计失败", e);
        }
    }
    
    /**
     * 创建真实图表（暂时注释掉）
     */
    private void createRealCharts() {
        // 图表功能暂时注释掉，等待JFreeChart依赖问题解决
        log.info("图表功能暂时不可用，等待依赖问题解决");
    }
    
    /**
     * 加载模拟数据（用于测试）
     */
    private void loadMockData() {
        // 更新统计卡片
        totalBooksCard.updateValue("1,234");
        totalUsersCard.updateValue("567");
        currentBorrowsCard.updateValue("89");
        overdueCard.updateValue("12");
        availableBooksCard.updateValue("1,145");
        borrowRateCard.updateValue("7.2");
        
        // 更新趋势信息
        totalBooksCard.updateTrend("↑ 较上月 +5.2%", new Color(46, 204, 113));
        totalUsersCard.updateTrend("↑ 较上月 +2.1%", new Color(46, 204, 113));
        currentBorrowsCard.updateTrend("↓ 较上月 -1.3%", new Color(231, 76, 60));
        overdueCard.updateTrend("↓ 较上月 -0.8%", new Color(46, 204, 113));
        availableBooksCard.updateTrend("↑ 较上月 +3.1%", new Color(46, 204, 113));
        borrowRateCard.updateTrend("↓ 较上月 -0.5%", new Color(231, 76, 60));
        
        // 创建示例图表
        createSampleCharts();
        
        // 更新表格数据
        updateTableData();
    }
    
    /**
     * 创建示例图表（暂时注释掉）
     */
    private void createSampleCharts() {
        // 图表功能暂时注释掉，等待JFreeChart依赖问题解决
        log.info("示例图表功能暂时不可用，等待依赖问题解决");
    }
    
    /**
     * 更新表格数据
     */
    private void updateTableData() {
        // 异步加载表格数据
        CompletableFuture.runAsync(() -> {
            try {
                loadPopularBooksData();
                loadOverdueBooksData();
                loadInventoryAlertsData();
            } catch (Exception e) {
                log.error("加载表格数据失败", e);
                // 失败时使用模拟数据
                loadMockTableData();
            }
        });
    }
    
    /**
     * 加载热门图书数据
     */
    private void loadPopularBooksData() {
        try {
            Request request = new Request("admin/getPopularBooks");
            Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
            
            if (response != null && "SUCCESS".equals(response.getStatus())) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> books = (List<Map<String, Object>>) response.getData();
                
                SwingUtilities.invokeLater(() -> {
                    DefaultTableModel model = (DefaultTableModel) popularBooksTable.getModel();
                    model.setRowCount(0);
                    
                    int rank = 1;
                    for (Map<String, Object> book : books) {
                        model.addRow(new Object[]{
                            String.valueOf(rank++),
                            book.get("title"),
                            book.get("author"),
                            book.get("borrowCount"),
                            book.get("category")
                        });
                    }
                });
            }
        } catch (Exception e) {
            log.error("加载热门图书数据失败", e);
        }
    }
    
    /**
     * 加载逾期图书数据
     */
    private void loadOverdueBooksData() {
        try {
            Request request = new Request("admin/getOverdueBooks");
            Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
            
            if (response != null && "SUCCESS".equals(response.getStatus())) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> books = (List<Map<String, Object>>) response.getData();
                
                SwingUtilities.invokeLater(() -> {
                    DefaultTableModel model = (DefaultTableModel) overdueBooksTable.getModel();
                    model.setRowCount(0);
                    
                    for (Map<String, Object> book : books) {
                        model.addRow(new Object[]{
                            book.get("title"),
                            book.get("borrowerName"),
                            book.get("borrowDate"),
                            book.get("dueDate"),
                            book.get("overdueDays")
                        });
                    }
                });
            }
        } catch (Exception e) {
            log.error("加载逾期图书数据失败", e);
        }
    }
    
    /**
     * 加载库存预警数据
     */
    private void loadInventoryAlertsData() {
        try {
            Request request = new Request("admin/getInventoryAlerts");
            Response response = nettyClient.sendRequest(request).get(10, java.util.concurrent.TimeUnit.SECONDS);
            
            if (response != null && "SUCCESS".equals(response.getStatus())) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> books = (List<Map<String, Object>>) response.getData();
                
                SwingUtilities.invokeLater(() -> {
                    DefaultTableModel model = (DefaultTableModel) inventoryAlertsTable.getModel();
                    model.setRowCount(0);
                    
                    for (Map<String, Object> book : books) {
                        model.addRow(new Object[]{
                            book.get("title"),
                            book.get("author"),
                            book.get("currentStock"),
                            book.get("minStock"),
                            book.get("status")
                        });
                    }
                });
            }
        } catch (Exception e) {
            log.error("加载库存预警数据失败", e);
        }
    }
    
    /**
     * 加载模拟表格数据
     */
    private void loadMockTableData() {
        SwingUtilities.invokeLater(() -> {
            // 更新热门图书表格
            DefaultTableModel popularModel = (DefaultTableModel) popularBooksTable.getModel();
            popularModel.setRowCount(0);
            popularModel.addRow(new Object[]{"1", "《Java编程思想》", "Bruce Eckel", "156", "计算机"});
            popularModel.addRow(new Object[]{"2", "《算法导论》", "Thomas H. Cormen", "134", "计算机"});
            popularModel.addRow(new Object[]{"3", "《红楼梦》", "曹雪芹", "98", "文学"});
            
            // 更新逾期图书表格
            DefaultTableModel overdueModel = (DefaultTableModel) overdueBooksTable.getModel();
            overdueModel.setRowCount(0);
            overdueModel.addRow(new Object[]{"《数据结构》", "张三", "2024-01-15", "2024-02-15", "5"});
            overdueModel.addRow(new Object[]{"《操作系统》", "李四", "2024-01-20", "2024-02-20", "3"});
            
            // 更新库存预警表格
            DefaultTableModel inventoryModel = (DefaultTableModel) inventoryAlertsTable.getModel();
            inventoryModel.setRowCount(0);
            inventoryModel.addRow(new Object[]{"《数据库原理》", "王五", "2", "5", "库存不足"});
            inventoryModel.addRow(new Object[]{"《计算机网络》", "赵六", "1", "3", "严重不足"});
        });
    }
    
    /**
     * 获取示例分类数据
     */
    private java.util.List<Map<String, Object>> getSampleCategoryData() {
        java.util.List<Map<String, Object>> data = new java.util.ArrayList<>();
        data.add(createDataItem("计算机", 156));
        data.add(createDataItem("文学", 98));
        data.add(createDataItem("历史", 67));
        data.add(createDataItem("科学", 45));
        data.add(createDataItem("艺术", 32));
        return data;
    }
    
    /**
     * 获取示例趋势数据
     */
    private java.util.List<Map<String, Object>> getSampleTrendData() {
        java.util.List<Map<String, Object>> data = new java.util.ArrayList<>();
        data.add(createDataItem("01-01", 15));
        data.add(createDataItem("01-02", 23));
        data.add(createDataItem("01-03", 18));
        data.add(createDataItem("01-04", 31));
        data.add(createDataItem("01-05", 27));
        data.add(createDataItem("01-06", 35));
        data.add(createDataItem("01-07", 42));
        return data;
    }
    
    /**
     * 获取示例状态数据
     */
    private java.util.List<Map<String, Object>> getSampleStatusData() {
        java.util.List<Map<String, Object>> data = new java.util.ArrayList<>();
        data.add(createDataItem("已归还", 156));
        data.add(createDataItem("已借出", 89));
        data.add(createDataItem("逾期", 12));
        data.add(createDataItem("续借", 23));
        return data;
    }
    
    /**
     * 获取示例热门数据
     */
    private java.util.List<Map<String, Object>> getSamplePopularData() {
        java.util.List<Map<String, Object>> data = new java.util.ArrayList<>();
        data.add(createDataItem("Java编程思想", 156));
        data.add(createDataItem("算法导论", 134));
        data.add(createDataItem("红楼梦", 98));
        data.add(createDataItem("数据结构", 87));
        data.add(createDataItem("操作系统", 76));
        return data;
    }
    
    /**
     * 创建数据项
     */
    private Map<String, Object> createDataItem(String key, int value) {
        Map<String, Object> item = new java.util.HashMap<>();
        item.put("category", key);
        item.put("book", key);
        item.put("date", key);
        item.put("status", key);
        item.put("count", value);
        return item;
    }
    
    /**
     * 创建样式化按钮
     */
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 35));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("微软雅黑", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}

package com.vcampus.client.core.ui.eduAdmin;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Vector;

/**
 * 学籍/师籍管理（支持编辑学号/专业/学院 或 工号/职称/学院，支持模糊查询）
 * 无添加/删除/批量操作。
 */
@Slf4j
public class eduStatus extends JPanel {
    private final NettyClient nettyClient;
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private JTable stuTable; private JTable staffTable;
    private DefaultTableModel stuModel; private DefaultTableModel staffModel;
    private JTextField stuSearchField; private JTextField staffSearchField;
    private List<Map<String,Object>> originalStuData = new ArrayList<>();
    private List<Map<String,Object>> originalStaffData = new ArrayList<>();
    private String currentStuSearchText = ""; // 当前学生搜索关键词
    private String currentStaffSearchText = ""; // 当前教师搜索关键词

    // 去掉“操作”列
    private static final String[] STU_COLS = {"卡号","学号","姓名","出生年月","性别","入学年份","专业","学院","民族","身份证号","籍贯","电话"};
    private static final String[] STAFF_COLS={"卡号","工号","姓名","出生年月","性别","职称","学院","参工年份","民族","身份证号","籍贯","电话"};

    public eduStatus(NettyClient client){
        this.nettyClient=client; setLayout(new BorderLayout());
        add(buildTitle(),BorderLayout.NORTH);
        buildTabs(); add(tabbedPane,BorderLayout.CENTER);
        loadStudents(); loadStaff();
    }

    private JComponent buildTitle(){
        JPanel p=new JPanel(new BorderLayout());
        JLabel title=new JLabel("学籍/师籍管理",SwingConstants.CENTER); title.setFont(new Font("微软雅黑",Font.BOLD,22));
        JLabel sub=new JLabel("可编辑学号/工号、专业/职称、学院，支持模糊查询",SwingConstants.CENTER); sub.setFont(new Font("微软雅黑",Font.PLAIN,12)); sub.setForeground(Color.GRAY);
        p.add(title,BorderLayout.CENTER); p.add(sub,BorderLayout.SOUTH); return p;
    }

    private void buildTabs(){
        tabbedPane.setFont(new Font("微软雅黑",Font.PLAIN,14));
        tabbedPane.addTab("学生学籍", buildStudentPanel());
        tabbedPane.addTab("教师师籍", buildStaffPanel());
    }

    private JPanel buildStudentPanel(){
        JPanel panel=new JPanel(new BorderLayout());
        
        // 顶部操作栏
        JPanel bar=new JPanel(new BorderLayout());
        
        // 左侧：刷新按钮
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh=new JButton("刷新数据"); 
        refresh.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        refresh.setBackground(new Color(108, 117, 125));
        refresh.setForeground(Color.WHITE);
        refresh.addActionListener(e->{loadStudents(); clearStudentHighlight();}); 
        leftPanel.add(refresh);
        
        // 右侧：搜索功能
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel searchLabel = new JLabel("查询:");
        searchLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        rightPanel.add(searchLabel);
        
        stuSearchField = new JTextField(15);
        stuSearchField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        stuSearchField.setToolTipText("输入任意信息进行模糊查询");
        // 回车键查询
        stuSearchField.addActionListener(e -> highlightStudentTable());
        rightPanel.add(stuSearchField);
        
        JButton searchBtn = new JButton("查询");
        searchBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchBtn.setBackground(new Color(255, 193, 7));
        searchBtn.setForeground(Color.BLACK);
        searchBtn.addActionListener(e -> highlightStudentTable());
        rightPanel.add(searchBtn);
        
        bar.add(leftPanel, BorderLayout.WEST);
        bar.add(rightPanel, BorderLayout.EAST);
        
        panel.add(bar,BorderLayout.NORTH);
        
        // 设置表格编辑规则：只允许编辑专业、学院、学号列
        stuModel=new DefaultTableModel(STU_COLS,0){
            public boolean isCellEditable(int r,int c){
                // 只允许编辑"学号"（索引1）、"专业"（索引6）、"学院"（索引7）列
                return c == 1 || c == 6 || c == 7;
            }
        };
        stuTable=new JTable(stuModel); 
        setupModernTable(stuTable);
        stuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 单行选择模式
        
        // 添加表格编辑监听器
        stuTable.getModel().addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (row >= 0 && col >= 0 && (col == 1 || col == 6 || col == 7)) {
                    updateStudentField(row, col);
                }
            }
        });
        
        // 设置自定义表格渲染器用于高亮显示
        stuTable.setDefaultRenderer(Object.class, new HighlightTableCellRenderer(true));
        
        panel.add(new JScrollPane(stuTable),BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStaffPanel(){
        JPanel panel=new JPanel(new BorderLayout());
        
        // 顶部操作栏
        JPanel bar=new JPanel(new BorderLayout());
        
        // 左侧：刷新按钮
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh=new JButton("刷新数据"); 
        refresh.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        refresh.setBackground(new Color(108, 117, 125));
        refresh.setForeground(Color.WHITE);
        refresh.addActionListener(e->{loadStaff(); clearStaffHighlight();}); 
        leftPanel.add(refresh);
        
        // 右侧：搜索功能
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel searchLabel = new JLabel("查询:");
        searchLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        rightPanel.add(searchLabel);
        
        staffSearchField = new JTextField(15);
        staffSearchField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        staffSearchField.setToolTipText("输入任意信息进行模糊查询");
        // 回车键查询
        staffSearchField.addActionListener(e -> highlightStaffTable());
        rightPanel.add(staffSearchField);
        
        JButton searchBtn = new JButton("查询");
        searchBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchBtn.setBackground(new Color(255, 193, 7));
        searchBtn.setForeground(Color.BLACK);
        searchBtn.addActionListener(e -> highlightStaffTable());
        rightPanel.add(searchBtn);
        
        bar.add(leftPanel, BorderLayout.WEST);
        bar.add(rightPanel, BorderLayout.EAST);
        
        panel.add(bar,BorderLayout.NORTH);
        
        // 设置表格编辑规则：只允许编辑工号、职称、学院列
        staffModel=new DefaultTableModel(STAFF_COLS,0){
            public boolean isCellEditable(int r,int c){
                // 只允许编辑"工号"（索引1）、"职称"（索引5）、"学院"（索引6）列
                return c == 1 || c == 5 || c == 6;
            }
        };
        staffTable=new JTable(staffModel); 
        setupModernTable(staffTable);
        staffTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 单行选择模式
        
        // 添加表格编辑监听器
        staffTable.getModel().addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (row >= 0 && col >= 0 && (col == 1 || col == 5 || col == 6)) {
                    updateStaffField(row, col);
                }
            }
        });
        
        // 设置自定义表格渲染器用于高亮显示
        staffTable.setDefaultRenderer(Object.class, new HighlightTableCellRenderer(false));
        
        panel.add(new JScrollPane(staffTable),BorderLayout.CENTER);
        
        return panel;
    }

    private void loadStudents(){
        try{Request req=new Request("ACADEMIC"); req.addParam("action","GET_ALL_STUDENTS_WITH_USER_INFO"); req.setSession(nettyClient.getCurrentSession());
            nettyClient.sendRequest(req).thenAccept(resp->SwingUtilities.invokeLater(()->{
                if(resp.isSuccess()){
                    stuModel.setRowCount(0);
                    @SuppressWarnings("unchecked")
                    List<Map<String,Object>> list=(List<Map<String,Object>>)resp.getData();
                    originalStuData.clear();
                    originalStuData.addAll(list); // 保存原始数据
                    for(Map<String,Object> m:list){Vector<Object> row=new Vector<>();
                        row.add(m.get("cardNum")); row.add(m.get("studentId")); row.add(nv(m,"name")); row.add(nv(m,"birthDate")); row.add(nv(m,"gender"));
                        row.add(nv(m,"enrollmentYear")); row.add(nv(m,"major")); row.add(nv(m,"department")); row.add(nv(m,"ethnicity"));
                        row.add(nv(m,"idCard")); row.add(nv(m,"hometown")); row.add(nv(m,"phone")); stuModel.addRow(row);} }
                else JOptionPane.showMessageDialog(this,"加载学生数据失败: "+resp.getMessage());
            })).exceptionally(th->{SwingUtilities.invokeLater(()->JOptionPane.showMessageDialog(this,th.getMessage())); return null;});
        }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage());}
    }

    private void loadStaff(){
        try{Request req=new Request("ACADEMIC"); req.addParam("action","GET_ALL_STAFF_WITH_USER_INFO"); req.setSession(nettyClient.getCurrentSession());
            nettyClient.sendRequest(req).thenAccept(resp->SwingUtilities.invokeLater(()->{
                if(resp.isSuccess()){
                    staffModel.setRowCount(0);
                    @SuppressWarnings("unchecked")
                    List<Map<String,Object>> list=(List<Map<String,Object>>)resp.getData();
                    originalStaffData.clear();
                    originalStaffData.addAll(list); // 保存原始数据
                    for(Map<String,Object> m:list){Vector<Object> row=new Vector<>();
                        row.add(m.get("cardNum")); row.add(m.get("staffId")); row.add(nv(m,"name")); row.add(nv(m,"birthDate")); row.add(nv(m,"gender"));
                        row.add(nv(m,"title")); row.add(nv(m,"department")); row.add(nv(m,"workYear")); row.add(nv(m,"ethnicity"));
                        row.add(nv(m,"idCard")); row.add(nv(m,"hometown")); row.add(nv(m,"phone")); staffModel.addRow(row);} }
                else JOptionPane.showMessageDialog(this,"加载教师数据失败: "+resp.getMessage());
            })).exceptionally(th->{SwingUtilities.invokeLater(()->JOptionPane.showMessageDialog(this,th.getMessage())); return null;});
        }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage());}
    }

    private String nv(Map<String,Object> m,String k){Object v=m.get(k); return v==null?"":v.toString();}
    
    /**
     * 更新学生字段
     */
    private void updateStudentField(int row, int col) {
        try {
            Object cardNumObj = stuModel.getValueAt(row, 0); // 卡号在第0列
            String cardNum = cardNumObj.toString();
            Object newValue = stuModel.getValueAt(row, col);
            
            // 获取列名
            String columnName = getStudentColumnName(col);
            if (columnName == null) return;
            
            // 从原始数据中找到对应的学生记录
            Map<String, Object> studentData = null;
            for (Map<String, Object> data : originalStuData) {
                if (cardNum.equals(data.get("cardNum").toString())) {
                    studentData = data;
                    break;
                }
            }
            
            if (studentData == null) {
                JOptionPane.showMessageDialog(this, "找不到学生记录: " + cardNum, "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 更新数据
            studentData.put(columnName, newValue);
            
            // 发送更新请求
            saveStudentUpdate(studentData, false); // 不显示成功消息
            
        } catch (Exception e) {
            log.error("更新学生字段失败", e);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "更新失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                // 恢复原值
                loadStudents();
            });
        }
    }
    
    /**
     * 更新教师字段
     */
    private void updateStaffField(int row, int col) {
        try {
            Object cardNumObj = staffModel.getValueAt(row, 0); // 卡号在第0列
            String cardNum = cardNumObj.toString();
            Object newValue = staffModel.getValueAt(row, col);
            
            // 获取列名
            String columnName = getStaffColumnName(col);
            if (columnName == null) return;
            
            // 从原始数据中找到对应的教师记录
            Map<String, Object> staffData = null;
            for (Map<String, Object> data : originalStaffData) {
                if (cardNum.equals(data.get("cardNum").toString())) {
                    staffData = data;
                    break;
                }
            }
            
            if (staffData == null) {
                JOptionPane.showMessageDialog(this, "找不到教师记录: " + cardNum, "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 更新数据
            staffData.put(columnName, newValue);
            
            // 发送更新请求
            saveStaffUpdate(staffData, false); // 不显示成功消息
            
        } catch (Exception e) {
            log.error("更新教师字段失败", e);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "更新失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                // 恢复原值
                loadStaff();
            });
        }
    }
    
    /**
     * 获取学生表格列对应的数据库字段名
     */
    private String getStudentColumnName(int col) {
        switch (col) {
            case 1: return "studentId";    // 学号
            case 6: return "major";        // 专业
            case 7: return "department";   // 学院
            default: return null;
        }
    }
    
    /**
     * 获取教师表格列对应的数据库字段名
     */
    private String getStaffColumnName(int col) {
        switch (col) {
            case 1: return "staffId";      // 工号
            case 5: return "title";        // 职称
            case 6: return "department";   // 学院
            default: return null;
        }
    }
    
    /**
     * 保存学生更新
     */
    private void saveStudentUpdate(Map<String, Object> studentData, boolean showSuccessMessage) {
        try {
            // 发送更新请求
            String studentJson = JsonUtils.toJson(studentData);
            Request request = new Request("ACADEMIC")
                .addParam("action", "UPDATE_STUDENT")
                .addParam("student", studentJson);
            request.setSession(nettyClient.getCurrentSession());
            
            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        // 更新原始数据
                        String cardNum = studentData.get("cardNum").toString();
                        for (Map<String, Object> data : originalStuData) {
                            if (cardNum.equals(data.get("cardNum").toString())) {
                                data.putAll(studentData);
                                break;
                            }
                        }
                        if (showSuccessMessage) {
                            JOptionPane.showMessageDialog(this, "学生信息更新成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "更新失败: " + response.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        loadStudents(); // 失败时重新加载数据
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "更新失败: " + throwable.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    loadStudents(); // 失败时重新加载数据
                });
                return null;
            });
            
        } catch (Exception e) {
            log.error("保存学生更新失败", e);
            JOptionPane.showMessageDialog(this, "保存失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            loadStudents(); // 失败时重新加载数据
        }
    }
    
    /**
     * 保存教师更新
     */
    private void saveStaffUpdate(Map<String, Object> staffData, boolean showSuccessMessage) {
        try {
            // 发送更新请求
            String staffJson = JsonUtils.toJson(staffData);
            Request request = new Request("ACADEMIC")
                .addParam("action", "UPDATE_STAFF")
                .addParam("staff", staffJson);
            request.setSession(nettyClient.getCurrentSession());
            
            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        // 更新原始数据
                        String cardNum = staffData.get("cardNum").toString();
                        for (Map<String, Object> data : originalStaffData) {
                            if (cardNum.equals(data.get("cardNum").toString())) {
                                data.putAll(staffData);
                                break;
                            }
                        }
                        if (showSuccessMessage) {
                            JOptionPane.showMessageDialog(this, "教师信息更新成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "更新失败: " + response.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                        loadStaff(); // 失败时重新加载数据
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "更新失败: " + throwable.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    loadStaff(); // 失败时重新加载数据
                });
                return null;
            });
            
        } catch (Exception e) {
            log.error("保存教师更新失败", e);
            JOptionPane.showMessageDialog(this, "保存失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            loadStaff(); // 失败时重新加载数据
        }
    }
    
    // 高亮学生表格
    private void highlightStudentTable() {
        currentStuSearchText = stuSearchField.getText().trim().toLowerCase();
        stuTable.repaint(); // 重新绘制表格以应用高亮效果
    }
    
    // 高亮教师表格  
    private void highlightStaffTable() {
        currentStaffSearchText = staffSearchField.getText().trim().toLowerCase();
        staffTable.repaint(); // 重新绘制表格以应用高亮效果
    }
    
    // 清除学生表格高亮
    private void clearStudentHighlight() {
        currentStuSearchText = "";
        stuSearchField.setText("");
        stuTable.repaint();
    }
    
    // 清除教师表格高亮
    private void clearStaffHighlight() {
        currentStaffSearchText = "";
        staffSearchField.setText("");
        staffTable.repaint();
    }
    
    // 自定义表格单元格渲染器，用于高亮显示匹配的行
    private class HighlightTableCellRenderer extends DefaultTableCellRenderer {
        private final boolean isStudentTable; // 标识是学生表格还是教师表格
        
        public HighlightTableCellRenderer(boolean isStudentTable) {
            this.isStudentTable = isStudentTable;
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, 
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // 获取当前搜索关键词
            String searchText = isStudentTable ? currentStuSearchText : currentStaffSearchText;
            
            if (searchText.isEmpty()) {
                // 没有搜索关键词时，使用默认样式
                if (!isSelected) {
                    setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                }
            } else {
                // 有搜索关键词时，检查当前行是否匹配
                boolean isMatched = false;
                
                // 遍历当前行的所有列，检查是否包含搜索关键词
                for (int col = 0; col < table.getColumnCount(); col++) {
                    Object cellValue = table.getValueAt(row, col);
                    if (cellValue != null && cellValue.toString().toLowerCase().contains(searchText)) {
                        isMatched = true;
                        break;
                    }
                }
                
                if (!isSelected) {
                    if (isMatched) {
                        // 匹配的行使用黄色背景，深色文字
                        setBackground(new Color(255, 255, 200)); // 浅黄色背景
                        setForeground(new Color(51, 51, 51)); // 深灰色文字，加深显示
                        setFont(getFont().deriveFont(Font.BOLD)); // 粗体字
                    } else {
                        // 不匹配的行使用默认样式
                        setBackground(Color.WHITE);
                        setForeground(Color.BLACK);
                        setFont(getFont().deriveFont(Font.PLAIN)); // 普通字体
                    }
                }
            }
            
            return component;
        }
    }

    /**
     * 设置现代化表格样式
     */
    private void setupModernTable(JTable table) {
        table.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        table.setRowHeight(40);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(52, 144, 220, 30));
        table.setSelectionForeground(new Color(52, 58, 64));
        table.setBackground(Color.WHITE);

        // 设置表头样式
        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setFont(new Font("微软雅黑", Font.BOLD, 13));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 45));

        // 设置自定义表头渲染器
        header.setDefaultRenderer(new HeaderRenderer());

        // 禁用表格拖动功能
        table.setDragEnabled(false);
        header.setReorderingAllowed(false);

        // 设置交替行颜色
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
    }

    /**
     * 表头渲染器 - 蓝色渐变背景
     */
    private class HeaderRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("微软雅黑", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setOpaque(false); // 让自定义绘制生效
            
            return this;
        }
        
        @Override
        protected void paintComponent(java.awt.Graphics g) {
            java.awt.Graphics2D g2d = (java.awt.Graphics2D) g.create();
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 绘制蓝色渐变背景
            java.awt.GradientPaint gradient = new java.awt.GradientPaint(
                0, 0, new Color(76, 151, 234),
                getWidth(), getHeight(), new Color(52, 144, 220)
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            g2d.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * 交替行渲染器
     */
    private class AlternatingRowRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected) {
                if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(new Color(248, 249, 250));
                }
            }

            setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            return c;
        }
    }
}

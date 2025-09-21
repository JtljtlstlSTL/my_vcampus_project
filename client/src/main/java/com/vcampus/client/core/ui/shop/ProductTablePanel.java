package com.vcampus.client.core.ui.shop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.util.List;

/**
 * 包装商品表格为独立组件，提供必要的访问与事件接口
 */
public class ProductTablePanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    // 当前鼠标悬停的视图行索引（-1 表示无悬停）
    private int hoverRow = -1;
    // 表格动作监听（右键菜单触发）
    private final java.util.List<ActionListener> actionListeners = new java.util.ArrayList<>();

    public ProductTablePanel(String[] columnNames) {
        setLayout(new BorderLayout());
        model = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        // 选择系统中可用的中文/通用字体作为表格字体（不使用 emoji），直接在本组件内处理
        java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        java.util.Set<String> fam = new java.util.HashSet<>();
        for (String f : ge.getAvailableFontFamilyNames()) fam.add(f);
        String[] candidates = new String[]{"Microsoft YaHei","微软雅黑","Noto Sans CJK SC","SimSun","Arial Unicode MS","Segoe UI","Dialog"};
        String pick = Font.DIALOG;
        for (String c : candidates) if (fam.contains(c)) { pick = c; break; }
        Font chosenFont = new Font(pick, Font.PLAIN, 13);
        table.setFont(chosenFont);
        // 增大行高以提高可读性
        table.setRowHeight(40);
        // 增加行内间距
        table.setRowMargin(6);
        // 强制表格为不透明并设置背景，确保行背景渲染可见
        table.setOpaque(true);
        table.setBackground(Color.WHITE);
        // 显示仅水平网格线并设置网格颜色以增强列边界感知
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230,230,230));
        // 选中样式更明显
        table.setSelectionBackground(new Color(38, 132, 255));
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        // 增大单元格水平与垂直间距，增加列之间可视间隔
        table.setIntercellSpacing(new Dimension(12, 8));
        // 支持多选以便管理界面批量操作
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // 改为自动缩放列以填充可用宽度，避免左右滚动条并让表头更宽
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // 表头美化：使用选中的系统字体，保证中文表头可见
        JTableHeader header = table.getTableHeader();
        final Font headerFont = new Font(pick, Font.BOLD, 13);
        header.setFont(headerFont);
        // 蓝色背景、白色文字
        final Color headerBg = new Color(38, 132, 255);
        header.setBackground(headerBg);
        header.setBorder(BorderFactory.createMatteBorder(0,0,1,0,new Color(200,200,200)));
        // 增加表头高度，便于阅读
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                lbl.setFont(headerFont);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBorder(BorderFactory.createEmptyBorder(8,20,8,20));
                // 确保不透明以显示自定义背景
                lbl.setOpaque(true);
                lbl.setBackground(headerBg);
                lbl.setForeground(Color.WHITE);
                return lbl;
            }
        });

        // 交替行与对齐渲染器
        DefaultTableCellRendererExt renderer = new DefaultTableCellRendererExt();
        table.setDefaultRenderer(Object.class, renderer);

        // 鼠标移动悬停高亮行（提升交互感知）
        table.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override public void mouseMoved(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoverRow) {
                    hoverRow = row;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                hoverRow = -1; table.repaint();
            }
        });

        // 右键菜单：编辑 / 删除 / 切换上架状态
        JPopupMenu popup = new JPopupMenu();
        JMenuItem miEdit = new JMenuItem("编辑");
        JMenuItem miDelete = new JMenuItem("删除");
        JMenuItem miToggle = new JMenuItem("切换上架");
        popup.add(miEdit); popup.add(miDelete); popup.addSeparator(); popup.add(miToggle);

        // 触发动作到外部监听器，命令格式为 action:id
        miEdit.addActionListener(ev -> fireTableAction("edit"));
        miDelete.addActionListener(ev -> fireTableAction("delete"));
        miToggle.addActionListener(ev -> fireTableAction("toggle"));

        // 显示弹出菜单并选中行
        table.addMouseListener(new MouseAdapter() {
            private void showIfPopup(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        table.setRowSelectionInterval(row, row);
                      }
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
            @Override public void mousePressed(java.awt.event.MouseEvent e) { showIfPopup(e); }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) { showIfPopup(e); }
        });

        // 设置合理的列宽（初始偏大，以便在可用宽度下更宽显示）
        int[] widths = new int[]{80, 100, 300, 140, 90, 100, 150, 360, 200};
        for (int i = 0; i < Math.min(widths.length, table.getColumnCount()); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(table);
        // 确保 viewport 背景与表格背景一致，使行背景色可以正常显示
        try { scroll.getViewport().setBackground(table.getBackground()); } catch (Exception ignored) {}
        // 提升滚动性能与体验：使用位图滚动模式并增加步进
        try {
            scroll.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        } catch (Exception ignored) {}
        try {
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            scroll.getHorizontalScrollBar().setUnitIncrement(16);
        } catch (Exception ignored) {}
        // 平滑鼠标滚轮：转为像素滚动，减少卡顿感
        table.addMouseWheelListener(e -> {
            JScrollBar bar = scroll.getVerticalScrollBar();
            int delta = e.getWheelRotation() * bar.getUnitIncrement(1);
            bar.setValue(bar.getValue() + delta);
            e.consume();
        });
        scroll.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220,220,220)), "商品列表"));
        add(scroll, BorderLayout.CENTER);
    }

    public void addTableMouseListener(MouseListener l) { table.addMouseListener(l); }
    public int getSelectedRowView() { return table.getSelectedRow(); }
    public int convertRowIndexToModel(int viewRow) { return table.convertRowIndexToModel(viewRow); }

    // 添加动作监听器，接收右键菜单触发的命令，ActionEvent.getActionCommand() 返回格式 "<action>:<id>"
    public void addTableActionListener(ActionListener l) { if (l != null) actionListeners.add(l); }

    private void fireTableAction(String action) {
        try {
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) return;
            int modelRow = table.convertRowIndexToModel(viewRow);
            Object idObj = model.getValueAt(modelRow, 0);
            String id = idObj == null ? "" : idObj.toString();
            ActionEvent ev = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, action + ":" + id);
            for (ActionListener l : actionListeners) l.actionPerformed(ev);
        } catch (Exception ignored) {}
    }

    public void setRows(List<Object[]> rows) {
        model.setRowCount(0);
        if (rows == null || rows.isEmpty()) return;
        for (Object[] r : rows) model.addRow(r);
    }

    // 新增：根据页码和页大小从完整商品列表中提取当前页并填充表格
    public void setProductsForPage(java.util.List<java.util.Map<String, Object>> allProducts, int page, int pageSize) {
        try {
            if (allProducts == null) allProducts = new java.util.ArrayList<>();
            if (pageSize <= 0) pageSize = 10;
            int total = allProducts.size();
            int start = Math.max(0, (page - 1) * pageSize);
            int end = Math.min(total, start + pageSize);
            java.util.List<Object[]> rows = new java.util.ArrayList<>();
            for (int i = start; i < end; i++) {
                java.util.Map<String,Object> p = allProducts.get(i);
                Object id = p.getOrDefault("productId", p.getOrDefault("id", ""));
                Object code = p.getOrDefault("productCode", p.getOrDefault("code", ""));
                Object name = p.getOrDefault("productName", p.getOrDefault("name", ""));
                Object price = p.get("price");
                Object stock = p.getOrDefault("stock", p.getOrDefault("Stock", ""));
                Object status = p.getOrDefault("status", p.getOrDefault("Product_status", ""));
                Object category = p.getOrDefault("category", p.getOrDefault("Product_category", ""));
                Object desc = p.getOrDefault("description", p.getOrDefault("Product_description", ""));
                Object updated = p.getOrDefault("updatedAt", p.getOrDefault("updated_at", p.getOrDefault("updated", "")));
                // 价格格式化为两位小数字符串
                String priceStr = "";
                if (price != null) {
                    try { java.math.BigDecimal bd = new java.math.BigDecimal(price.toString()); priceStr = bd.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(); } catch (Exception ignored) { priceStr = price.toString(); }
                }
                // 对描述做截断，避免表格过宽，同时保留完整 tooltip
                String descStr = desc == null ? "" : desc.toString();
                String shortDesc = descStr.length() > 80 ? descStr.substring(0, 77) + "..." : descStr;
                Object[] row = new Object[]{ id, code, name, priceStr, stock, status, category, shortDesc, updated };
                rows.add(row);
            }
            setRows(rows);
            // 为描述列和价格列设置 tooltips 与自定义渲染
            int descCol = 7; int priceCol = 3;
            if (table.getColumnCount() > descCol) table.getColumnModel().getColumn(descCol).setCellRenderer(new DefaultTableCellRendererExt() {
                @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    String full = "";
                    try {
                        int modelRow = table.convertRowIndexToModel(row);
                        Object orig = (modelRow >= 0 && modelRow < table.getRowCount()) ? table.getModel().getValueAt(modelRow, column) : value;
                        full = orig == null ? "" : orig.toString();
                    } catch (Exception ignored) {}
                    if (c instanceof JComponent) ((JComponent)c).setToolTipText(full);
                    return c;
                }
            });
            if (table.getColumnCount() > priceCol) table.getColumnModel().getColumn(priceCol).setCellRenderer(new DefaultTableCellRendererExt() {
                @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    try { setHorizontalAlignment(SwingConstants.RIGHT); setForeground(new Color(34, 197, 94)); } catch (Exception ignored) {}
                    return c;
                }
            });
        } catch (Exception ignored) {}
    }

    // 返回当前视图中被选中的视图行索引（用于将视图索引转换为 model 索引）
    public int[] getSelectedViewRows() { return table.getSelectedRows(); }

    // 方便外部进行全选/清空选择
    public void selectAllRows() { table.selectAll(); }
    public void clearSelectionRows() { table.clearSelection(); }

    // 允许外部读取 model 层的单元格值（通过 model 索引)
    public Object getModelValueAt(int modelRow, int col) { return model.getValueAt(modelRow, col); }

    // 自定义渲染器：交替背景色并对价格/数量列靠右显示
    private class DefaultTableCellRendererExt extends javax.swing.table.DefaultTableCellRenderer {
        private final Color evenColor = new Color(250,250,250);
        private final Color oddColor = Color.WHITE;
        private final Color hoverColor = new Color(240, 248, 255);

        @Override
        public Component getTableCellRendererComponent(JTable tableRef, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(tableRef, value, isSelected, hasFocus, row, column);
            // 保证单元格字体使用表格的字体（已在上方选择），避免字体回退导致方块
            try { c.setFont(tableRef.getFont()); } catch (Exception ignored) {}

            // 默认对齐
            if (column == 3 || column == 4) setHorizontalAlignment(SwingConstants.RIGHT); else setHorizontalAlignment(SwingConstants.LEFT);

            // 若该行对应的商品为售罄，则整行使用灰色背景以示区分（仅在未选中时）
            if (isSelected) {
                c.setBackground(tableRef.getSelectionBackground());
                setForeground(tableRef.getSelectionForeground());
            } else {
                boolean soldOut = false;
                try {
                    int modelRow = tableRef.convertRowIndexToModel(row);
                    if (tableRef.getColumnCount() > 5) {
                        Object st = tableRef.getModel().getValueAt(modelRow, 5);
                        if (st != null) {
                            String ss = st.toString().trim();
                            if ("SOLD_OUT".equalsIgnoreCase(ss) || "售罄".equals(ss)) soldOut = true;
                        }
                    }
                } catch (Exception ignored) {}
                if (soldOut) c.setBackground(new Color(245,245,245));
                else if (row == hoverRow) c.setBackground(hoverColor);
                else c.setBackground((row % 2) == 0 ? evenColor : oddColor);
                setForeground(Color.BLACK);
            }

            // 增加单元格内边距，提升可读性
            try { setBorder(BorderFactory.createEmptyBorder(6,12,6,12)); } catch (Exception ignored) {}

            // 状态列（索引 5）单独高亮售罄文本
            if (column == 5) {
                if (value != null) {
                    String vs = value.toString();
                    if ("SOLD_OUT".equalsIgnoreCase(vs) || "售罄".equals(vs)) {
                        setForeground(new Color(255,140,0));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else {
                        setForeground(Color.BLACK);
                        setFont(getFont().deriveFont(Font.PLAIN));
                    }
                } else {
                    setForeground(Color.BLACK);
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
            }

            // 对描述列显示 tooltip（如果被截断）
            if (column == 7 && c instanceof JComponent) {
                String text = value == null ? "" : value.toString();
                ((JComponent)c).setToolTipText(text);
            }

            return c;
        }
    }
}

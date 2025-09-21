package com.vcampus.client.core.ui.userAdmin;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * 批量导入功能类
 * 支持从Excel文件导入学生和教师数据
 */
@Slf4j
public class BatchImport {

    private NettyClient nettyClient;
    private Component parent;

    // 学生数据的列名映射
    private static final Map<String, String> STUDENT_COLUMN_MAP = new HashMap<>();
    // 教师数据的列名映射
    private static final Map<String, String> STAFF_COLUMN_MAP = new HashMap<>();

    static {
        // 学生列名映射
        STUDENT_COLUMN_MAP.put("卡号", "cardNum");
        STUDENT_COLUMN_MAP.put("学号", "studentId");
        STUDENT_COLUMN_MAP.put("姓名", "name");
        STUDENT_COLUMN_MAP.put("出生年月", "birthDate");
        STUDENT_COLUMN_MAP.put("性别", "gender");
        STUDENT_COLUMN_MAP.put("入学年份", "enrollmentYear");
        STUDENT_COLUMN_MAP.put("专业", "major");
        STUDENT_COLUMN_MAP.put("学院", "department");
        STUDENT_COLUMN_MAP.put("民族", "ethnicity");
        STUDENT_COLUMN_MAP.put("身份证号", "idCard");
        STUDENT_COLUMN_MAP.put("籍贯", "hometown");
        STUDENT_COLUMN_MAP.put("电话", "phone");

        // 教师列名映射（工号对应staffId，职称对应title）
        STAFF_COLUMN_MAP.put("卡号", "cardNum");
        STAFF_COLUMN_MAP.put("工号", "staffId");
        STAFF_COLUMN_MAP.put("姓名", "name");
        STAFF_COLUMN_MAP.put("出生年月", "birthDate");
        STAFF_COLUMN_MAP.put("性别", "gender");
        STAFF_COLUMN_MAP.put("职称", "title");
        STAFF_COLUMN_MAP.put("学院", "department");
        STAFF_COLUMN_MAP.put("参工年份", "workYear");
        STAFF_COLUMN_MAP.put("民族", "ethnicity");
        STAFF_COLUMN_MAP.put("身份证号", "idCard");
        STAFF_COLUMN_MAP.put("籍贯", "hometown");
        STAFF_COLUMN_MAP.put("电话", "phone");
    }

    public BatchImport(NettyClient nettyClient, Component parent) {
        this.nettyClient = nettyClient;
        this.parent = parent;
    }

    /**
     * 导入学生数据
     */
    public void importStudents() {
        try {
            File selectedFile = selectExcelFile();
            if (selectedFile == null) {
                return; // 用户取消选择
            }

            List<Map<String, Object>> studentDataList = parseExcelFile(selectedFile, STUDENT_COLUMN_MAP, "学生");
            if (studentDataList.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "未找到有效的学生数据！", "导入失败", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 显示预览对话框
            if (showPreviewDialog(studentDataList, "学生")) {
                // 用户确认导入
                int successCount = 0;
                int failCount = 0;
                StringBuilder errorMessages = new StringBuilder();

                for (Map<String, Object> studentData : studentDataList) {
                    try {
                        // 确保必填字段不为空
                        ensureRequiredFields(studentData, "学生");

                        Request request = new Request("ACADEMIC");
                        request.addParam("action", "ADD_STUDENT");
                        // 修复：将Map对象转换为JSON字符串
                        request.addParam("student", JsonUtils.toJson(studentData));
                        request.setSession(nettyClient.getCurrentSession());

                        // 修复：使用异步处理提高性能，但仍然等待结果以便统计
                        boolean isSuccess = nettyClient.sendRequest(request)
                            .thenApply(response -> {
                                if (response.isSuccess()) {
                                    log.info("成功导入学生数据: {}", studentData.get("cardNum"));
                                    return true;
                                } else {
                                    log.error("导入学生数据失败: {} - {}", studentData.get("cardNum"), response.getMessage());
                                    throw new RuntimeException(response.getMessage());
                                }
                            })
                            .exceptionally(throwable -> {
                                log.error("导入学生数据异常: {}", studentData.get("cardNum"), throwable);
                                throw new RuntimeException(throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage());
                            })
                            .get(); // 等待完成并获取结果

                        if (isSuccess) {
                            successCount++;
                        }

                        // 添加小延迟避免服务器压力过大
                        Thread.sleep(50);

                    } catch (Exception e) {
                        failCount++;
                        String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                        errorMessages.append("卡号 ").append(studentData.get("cardNum")).append(": ").append(errorMsg).append("\n");
                        log.error("导入学生数据失败: {}", studentData.get("cardNum"), e);
                    }
                }

                // 显示导入结果
                showImportResult(successCount, failCount, errorMessages.toString(), "���生");
            }

        } catch (Exception e) {
            log.error("批量导入学生数据时发生错误", e);
            JOptionPane.showMessageDialog(parent, "批量导入失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 导入教师数据
     */
    public void importStaff() {
        try {
            File selectedFile = selectExcelFile();
            if (selectedFile == null) {
                return; // 用户取消选择
            }

            List<Map<String, Object>> staffDataList = parseExcelFile(selectedFile, STAFF_COLUMN_MAP, "教师");
            if (staffDataList.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "未找到有效的教师数据！", "导入失败", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 显示预览对话框
            if (showPreviewDialog(staffDataList, "教师")) {
                // 用户确认导入
                int successCount = 0;
                int failCount = 0;
                StringBuilder errorMessages = new StringBuilder();

                for (Map<String, Object> staffData : staffDataList) {
                    try {
                        // 确保必填字段不为空
                        ensureRequiredFields(staffData, "教师");

                        Request request = new Request("ACADEMIC");
                        request.addParam("action", "ADD_STAFF");
                        // 修复：将Map对象转换为JSON字符串
                        request.addParam("staff", JsonUtils.toJson(staffData));
                        request.setSession(nettyClient.getCurrentSession());

                        // 修复：使用异步处理提高性能，但仍然等待结果以便统计
                        boolean isSuccess = nettyClient.sendRequest(request)
                            .thenApply(response -> {
                                if (response.isSuccess()) {
                                    log.info("成功导入教师数据: {}", staffData.get("cardNum"));
                                    return true;
                                } else {
                                    log.error("导入教师数据失败: {} - {}", staffData.get("cardNum"), response.getMessage());
                                    throw new RuntimeException(response.getMessage());
                                }
                            })
                            .exceptionally(throwable -> {
                                log.error("导入教师数据异常: {}", staffData.get("cardNum"), throwable);
                                throw new RuntimeException(throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage());
                            })
                            .get(); // 等待完成并获取结果

                        if (isSuccess) {
                            successCount++;
                        }

                        // 添加小延迟避免服务器压力过大
                        Thread.sleep(50);

                    } catch (Exception e) {
                        failCount++;
                        String errorMsg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                        errorMessages.append("卡号 ").append(staffData.get("cardNum")).append(": ").append(errorMsg).append("\n");
                        log.error("导入教师数据失败: {}", staffData.get("cardNum"), e);
                    }
                }

                // 显示导入结果
                showImportResult(successCount, failCount, errorMessages.toString(), "教师");
            }

        } catch (Exception e) {
            log.error("批量导入教师数据时发生错误", e);
            JOptionPane.showMessageDialog(parent, "批量导入失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 选择Excel文件
     */
    private File selectExcelFile() {
        JFileChooser fileChooser = new JFileChooser();

        // 设置初始目录为C盘根目录
        fileChooser.setCurrentDirectory(new File("C:\\"));

        // 设置文件过滤器，只显示Excel文件
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Excel文件 (*.xls, *.xlsx)", "xls", "xlsx");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        // 设置对话框标题
        fileChooser.setDialogTitle("选择要导入的Excel文件");

        // 显示文件选择对话框
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    /**
     * 解析Excel文件
     */
    private List<Map<String, Object>> parseExcelFile(File file, Map<String, String> columnMap, String dataType) throws IOException {
        List<Map<String, Object>> dataList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook;

            // 根据文件扩展名创建相应的工作簿
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (fileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook(fis);
            } else {
                throw new IOException("不支持的文件格式，请选择.xls或.xlsx文件");
            }

            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 2) {
                workbook.close();
                throw new IOException("Excel文件中没有数据行");
            }

            // 解析表头
            Row headerRow = sheet.getRow(0);
            Map<Integer, String> columnIndexMap = new HashMap<>();

            for (Cell cell : headerRow) {
                String headerValue = getCellValueAsString(cell).trim();
                if (columnMap.containsKey(headerValue)) {
                    columnIndexMap.put(cell.getColumnIndex(), columnMap.get(headerValue));
                }
            }

            if (columnIndexMap.isEmpty()) {
                workbook.close();
                throw new IOException("未找到有效的列标题，请确保Excel文件包含正确的表头");
            }

            // 解析数据行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, Object> rowData = new HashMap<>();
                boolean hasData = false;

                for (Map.Entry<Integer, String> entry : columnIndexMap.entrySet()) {
                    int columnIndex = entry.getKey();
                    String fieldName = entry.getValue();

                    Cell cell = row.getCell(columnIndex);
                    Object value = getCellValue(cell, fieldName);

                    if (value != null) {
                        rowData.put(fieldName, value);
                        hasData = true;
                    }
                }

                if (hasData) {
                    // 确保必填字段不为空
                    ensureRequiredFields(rowData, dataType);
                    dataList.add(rowData);
                }
            }

            workbook.close();
        }

        return dataList;
    }

    /**
     * 获取单元格值
     */
    private Object getCellValue(Cell cell, String fieldName) {
        if (cell == null) return null;

        try {
            // 数字类型字段
            if ("age".equals(fieldName) || "grade".equals(fieldName)) {
                switch (cell.getCellType()) {
                    case NUMERIC:
                        return (int) cell.getNumericCellValue();
                    case STRING:
                        String strValue = cell.getStringCellValue().trim();
                        if (strValue.isEmpty()) return null;
                        return Integer.parseInt(strValue);
                    default:
                        return null;
                }
            }
            // 文本类型字段
            else {
                switch (cell.getCellType()) {
                    case STRING:
                        return cell.getStringCellValue().trim();
                    case NUMERIC:
                        // 将数字转换为字符串（如卡号、学号等）
                        return String.valueOf((long) cell.getNumericCellValue());
                    default:
                        return null;
                }
            }
        } catch (Exception e) {
            log.warn("解析单元格值失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取单元格值作为字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    /**
     * 显示数据预览对话框
     */
    private boolean showPreviewDialog(List<Map<String, Object>> dataList, String dataType) {
        // 创建预览对话��
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parent), "数据预览 - " + dataType, true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(parent);

        // 创建表格显示数据
        String[] columnNames = dataType.equals("学生") ?
            new String[]{"卡号", "学号", "姓名", "年龄", "性别", "年级", "专业", "学院", "电话"} :
            new String[]{"卡号", "工号", "姓名", "年龄", "性别", "职称", "学院", "电话"};

        Object[][] tableData = new Object[dataList.size()][columnNames.length];

        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> data = dataList.get(i);
            if (dataType.equals("学生")) {
                tableData[i] = new Object[]{
                    data.get("cardNum"), data.get("studentId"), data.get("name"),
                    data.get("age"), data.get("gender"), data.get("grade"),
                    data.get("major"), data.get("department"), data.get("phone")
                };
            } else {
                tableData[i] = new Object[]{
                    data.get("cardNum"), data.get("staffId"), data.get("name"),
                    data.get("age"), data.get("gender"), data.get("title"),
                    data.get("department"), data.get("phone")
                };
            }
        }

        JTable table = new JTable(tableData, columnNames);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollPane = new JScrollPane(table);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton confirmButton = new JButton("确认导入");
        JButton cancelButton = new JButton("取消");

        final boolean[] confirmed = {false};

        confirmButton.addActionListener(e -> {
            confirmed[0] = true;
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);

        // 添加信息标签
        JLabel infoLabel = new JLabel("共找到 " + dataList.size() + " 条" + dataType + "数据，请确认是否导入：");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        dialog.setLayout(new BorderLayout());
        dialog.add(infoLabel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
        return confirmed[0];
    }

    /**
     * 显示导入结果
     */
    private void showImportResult(int successCount, int failCount, String errorMessages, String dataType) {
        StringBuilder message = new StringBuilder();
        message.append(dataType).append("数据导入完成！\n\n");
        message.append("成功导入：").append(successCount).append(" 条\n");
        message.append("导入失败：").append(failCount).append(" 条\n");

        if (failCount > 0 && !errorMessages.isEmpty()) {
            message.append("\n失败详情：\n").append(errorMessages);
        }

        int messageType = failCount == 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE;

        // 如果错误信息太长，使用滚动面板显示
        if (message.length() > 500) {
            JTextArea textArea = new JTextArea(message.toString());
            textArea.setEditable(false);
            textArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 300));
            JOptionPane.showMessageDialog(parent, scrollPane, "导入结果", messageType);
        } else {
            JOptionPane.showMessageDialog(parent, message.toString(), "导入结果", messageType);
        }
    }

    /**
     * 确保必填字段不为空
     */
    private void ensureRequiredFields(Map<String, Object> rowData, String dataType) {
        // 为空的电话号码字段提供默认值
        if (rowData.get("phone") == null || rowData.get("phone").toString().trim().isEmpty()) {
            rowData.put("phone", "00000000000"); // 默认电话号码
        }

        // 为空的姓名字段提供默认值
        if (rowData.get("name") == null || rowData.get("name").toString().trim().isEmpty()) {
            rowData.put("name", "未知");
        }

        // 为空的性别字段提供默认值
        if (rowData.get("gender") == null || rowData.get("gender").toString().trim().isEmpty()) {
            rowData.put("gender", "男");
        }

        // 为空的出生年月字段提供默认值
        if (rowData.get("birthDate") == null || rowData.get("birthDate").toString().trim().isEmpty()) {
            rowData.put("birthDate", "199001"); // 默认1990年1月
        }

        // 修复：为新的通用字段提供符合数据库长度限制的��认值
        if (rowData.get("ethnicity") == null || rowData.get("ethnicity").toString().trim().isEmpty()) {
            rowData.put("ethnicity", "汉"); // 修复：简化为单个字符，避免超过VARCHAR(10)限制
        }
        if (rowData.get("idCard") == null || rowData.get("idCard").toString().trim().isEmpty()) {
            rowData.put("idCard", "110101199001011234"); // 默认身份证号
        }
        if (rowData.get("hometown") == null || rowData.get("hometown").toString().trim().isEmpty()) {
            rowData.put("hometown", "北京市"); // 修复：确保不超过VARCHAR(40)限制
        }

        if (dataType.equals("学生")) {
            // 学生专有字段的默认值
            if (rowData.get("studentId") == null || rowData.get("studentId").toString().trim().isEmpty()) {
                // 如果学号为空，使用卡号作为学号
                String cardNum = (String) rowData.get("cardNum");
                rowData.put("studentId", cardNum != null ? cardNum : "00000000");
            }

            if (rowData.get("enrollmentYear") == null || rowData.get("enrollmentYear").toString().trim().isEmpty()) {
                rowData.put("enrollmentYear", "2021"); // 默认2021年入学
            }

            if (rowData.get("major") == null || rowData.get("major").toString().trim().isEmpty()) {
                rowData.put("major", "计算机科学"); // 修复：确保不超过VARCHAR(20)限制
            }

            if (rowData.get("department") == null || rowData.get("department").toString().trim().isEmpty()) {
                rowData.put("department", "计算机学院"); // 修复：确保不超过VARCHAR(20)限制
            }
        } else {
            // 教师专有字段的默认值
            if (rowData.get("staffId") == null || rowData.get("staffId").toString().trim().isEmpty()) {
                // 如果工号为空，使用卡号作为工号
                String cardNum = (String) rowData.get("cardNum");
                rowData.put("staffId", cardNum != null ? cardNum : "00000000");
            }

            if (rowData.get("workYear") == null || rowData.get("workYear").toString().trim().isEmpty()) {
                rowData.put("workYear", "2020"); // 默认2020年参工
            }

            if (rowData.get("title") == null || rowData.get("title").toString().trim().isEmpty()) {
                rowData.put("title", "讲师"); // 修复：确保不超过VARCHAR(10)限制
            }

            if (rowData.get("department") == null || rowData.get("department").toString().trim().isEmpty()) {
                rowData.put("department", "计算机学院"); // 修复：确保不超过VARCHAR(20)限制
            }
        }

        // 修复：确保所有字符串字段都不会超过数据库限制
        validateFieldLengths(rowData, dataType);
    }

    /**
     * 验证字段长度，确保不超过数据库限制
     */
    private void validateFieldLengths(Map<String, Object> rowData, String dataType) {
        // 截取字段以符合数据库约束
        truncateField(rowData, "name", 10);
        truncateField(rowData, "ethnicity", 10);
        truncateField(rowData, "hometown", 40);
        truncateField(rowData, "phone", 11);
        truncateField(rowData, "idCard", 18);

        if (dataType.equals("学生")) {
            truncateField(rowData, "major", 20);
            truncateField(rowData, "department", 20);
            truncateField(rowData, "studentId", 8);
            truncateField(rowData, "enrollmentYear", 4);
        } else {
            truncateField(rowData, "title", 10);
            truncateField(rowData, "department", 20);
            truncateField(rowData, "staffId", 8);
            truncateField(rowData, "workYear", 4);
        }
        truncateField(rowData, "birthDate", 7);
    }

    /**
     * 截取字段长度
     */
    private void truncateField(Map<String, Object> rowData, String fieldName, int maxLength) {
        Object value = rowData.get(fieldName);
        if (value != null && value instanceof String) {
            String strValue = (String) value;
            if (strValue.length() > maxLength) {
                rowData.put(fieldName, strValue.substring(0, maxLength));
                log.warn("字段 {} 的值被截取到 {} 个字符: {} -> {}",
                    fieldName, maxLength, strValue, rowData.get(fieldName));
            }
        }
    }
}

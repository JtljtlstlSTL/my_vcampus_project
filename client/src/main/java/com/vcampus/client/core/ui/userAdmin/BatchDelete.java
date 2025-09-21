package com.vcampus.client.core.ui.userAdmin;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 批量删除功能类
 * 支持批量删除学生和教师信息
 */
@Slf4j
public class BatchDelete {

    private NettyClient nettyClient;
    private StatusPanel statusPanel;

    public BatchDelete(NettyClient nettyClient, StatusPanel statusPanel) {
        this.nettyClient = nettyClient;
        this.statusPanel = statusPanel;
    }

    /**
     * 批量删除学生
     * @param selectedRows 选中的行索引数组
     * @param studentTable 学生表格
     */
    public void batchDeleteStudents(int[] selectedRows, JTable studentTable) {
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(statusPanel,
                    "请先选择要删除的学生记录！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 确认删除对话框
        int result = JOptionPane.showConfirmDialog(statusPanel,
                "您确定要删除选中的 " + selectedRows.length + " 条学生记录吗？\n删除后无法恢复！",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        // 获取选中行的卡号
        List<String> cardNumbers = new ArrayList<>();
        for (int row : selectedRows) {
            String cardNum = (String) studentTable.getValueAt(row, 0); // 卡号在第0列
            cardNumbers.add(cardNum);
        }

        // 执行批量删除
        try {
            Request request = new Request("ACADEMIC");
            request.addParam("action", "BATCH_DELETE_STUDENTS");
            request.addParam("cardNumbers", JsonUtils.toJson(cardNumbers));
            request.setSession(nettyClient.getCurrentSession());

            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(statusPanel,
                                "成功删除 " + selectedRows.length + " 条学生记录！",
                                "删除成功",
                                JOptionPane.INFORMATION_MESSAGE);

                        // 刷新学生数据
                        statusPanel.loadStudentData();
                        log.info("批量删除学生成功，删除数量: {}", selectedRows.length);
                    } else {
                        JOptionPane.showMessageDialog(statusPanel,
                                "批量删除学生失败：" + response.getMessage(),
                                "删除失败",
                                JOptionPane.ERROR_MESSAGE);
                        log.error("批量删除学生失败: {}", response.getMessage());
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.error("批量删除学生时发生错误", throwable);
                    JOptionPane.showMessageDialog(statusPanel,
                            "批量删除学生时发生错误：" + throwable.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });

        } catch (Exception e) {
            log.error("发送批量删除学生请求时发生错误", e);
            JOptionPane.showMessageDialog(statusPanel,
                    "发送批量删除学生请求时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 批量删除教师
     * @param selectedRows 选中的行索引数组
     * @param staffTable 教师表格
     */
    public void batchDeleteStaff(int[] selectedRows, JTable staffTable) {
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(statusPanel,
                    "请先选择要删除的教师记录！",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 确认删除对话框
        int result = JOptionPane.showConfirmDialog(statusPanel,
                "您确定要删除选中的 " + selectedRows.length + " 条教师记录吗？\n删除后无法恢复！",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        // 获取选中行的卡号
        List<String> cardNumbers = new ArrayList<>();
        for (int row : selectedRows) {
            String cardNum = (String) staffTable.getValueAt(row, 0); // 卡号在第0列
            cardNumbers.add(cardNum);
        }

        // 执行批量删除
        try {
            Request request = new Request("ACADEMIC");
            request.addParam("action", "BATCH_DELETE_STAFF");
            request.addParam("cardNumbers", JsonUtils.toJson(cardNumbers));
            request.setSession(nettyClient.getCurrentSession());

            nettyClient.sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if (response.isSuccess()) {
                        JOptionPane.showMessageDialog(statusPanel,
                                "成功删除 " + selectedRows.length + " 条教师记录！",
                                "删除成功",
                                JOptionPane.INFORMATION_MESSAGE);

                        // 刷新教师数据
                        statusPanel.loadStaffData();
                        log.info("批量删除教师成功，删除数量: {}", selectedRows.length);
                    } else {
                        JOptionPane.showMessageDialog(statusPanel,
                                "批量删除教师失败：" + response.getMessage(),
                                "删除失败",
                                JOptionPane.ERROR_MESSAGE);
                        log.error("批量删除教师失败: {}", response.getMessage());
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.error("批量删除教师时发生错误", throwable);
                    JOptionPane.showMessageDialog(statusPanel,
                            "批量删除教师时发生错误：" + throwable.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });

        } catch (Exception e) {
            log.error("发送批量删除教师请求时发生错误", e);
            JOptionPane.showMessageDialog(statusPanel,
                    "发送批量删除教师请求时发生错误：" + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

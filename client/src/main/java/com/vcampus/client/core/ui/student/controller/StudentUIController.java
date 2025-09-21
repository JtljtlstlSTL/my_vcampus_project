package com.vcampus.client.core.ui.student.controller;

import com.vcampus.client.core.ui.student.model.StudentModel;
import com.vcampus.client.core.ui.student.view.StudentView;
import com.vcampus.client.core.ui.student.dialog.*;
import com.vcampus.common.message.Request;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

/**
 * 学生界面控制器
 * 负责处理用户交互和业务逻辑
 *
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class StudentUIController {
    
    private StudentModel model;
    private StudentView view;
    
    public StudentUIController(StudentModel model, StudentView view) {
        this.model = model;
        this.view = view;
        setupEventHandlers();
    }
    
    /**
     * 设置事件处理器
     */
    private void setupEventHandlers() {
        // 登出按钮
        view.getBtnLogout().addActionListener(e -> handleLogout());
        
        // 修改密码按钮
        view.getBtnChangePassword().addActionListener(e -> handleChangePassword());
        
        // AI助手按钮
        view.getBtnAI().addActionListener(e -> handleToggleAI());
        
        // 功能模块点击事件
        view.getLblProfile().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleProfileClick();
            }
        });
        
        view.getLblCourse().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleCourseClick();
            }
        });
        
        view.getLblLibrary().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleLibraryClick();
            }
        });
        
        view.getLblShop().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleShopClick();
            }
        });
        
        view.getLblCard().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleCardClick();
            }
        });
        
        // 窗口关闭事件
        view.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleLogout();
            }
        });
    }
    
    /**
     * 处理登出
     */
    private void handleLogout() {
        int result = JOptionPane.showConfirmDialog(
            view, 
            "确定要退出登录吗？", 
            "确认退出", 
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
        try {
            // 发送登出请求
            Map<String, String> params = new java.util.HashMap<>();
            params.put("cardNumber", model.getStudentId());
            Request request = new Request("logout", params);
            
            model.getNettyClient().sendRequest(request).thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    if ("success".equals(response.getStatus())) {
                        log.info("用户登出成功");
                        view.dispose();
                        // 显示登录界面
                        new com.vcampus.client.core.ui.login.LoginFrame().setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(view, "登出失败：" + response.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }).exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    log.error("登出时发生错误", throwable);
                    JOptionPane.showMessageDialog(view, "登出时发生错误：" + throwable.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                });
                return null;
            });
        } catch (Exception e) {
            log.error("登出时发生错误", e);
            JOptionPane.showMessageDialog(view, "登出时发生错误：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
        }
    }
    
    /**
     * 处理修改密码
     */
    private void handleChangePassword() {
        new ChangePasswordDialog(view, model.getNettyClient(), model.getUserData()).setVisible(true);
    }
    
    /**
     * 处理AI助手切换
     */
    private void handleToggleAI() {
        model.setAiVisible(!model.isAiVisible());
        view.toggleAIPanel(model.isAiVisible());
    }
    
    /**
     * 处理个人信息点击
     */
    private void handleProfileClick() {
        view.showProfilePanel();
    }
    
    /**
     * 处理课程管理点击
     */
    private void handleCourseClick() {
        view.showCoursePanel();
    }
    
    /**
     * 处理图书馆点击
     */
    private void handleLibraryClick() {
        view.showLibraryPanel();
    }
    
    /**
     * 处理商城点击
     */
    private void handleShopClick() {
        view.showShopPanel();
    }
    
    /**
     * 处理校园卡点击
     */
    private void handleCardClick() {
        view.showCardPanel();
    }
    
    /**
     * 显示成绩查询对话框
     */
    public void showScoreDialog() {
        new StudentScoreDialog(view, model.getNettyClient(), model.getStudentId()).setVisible(true);
    }
    
    /**
     * 显示课程评价对话框
     */
    public void showEvaluationDialog() {
        new CourseEvaluationDialog(view, model.getNettyClient(), model.getStudentId()).setVisible(true);
    }
    
    /**
     * 显示头像选择对话框
     */
    public void showAvatarSelectionDialog(JLabel sidebarAvatar, JLabel profileAvatar) {
        new AvatarSelectionDialog(view, model.getUserData(), sidebarAvatar, profileAvatar).setVisible(true);
    }
}

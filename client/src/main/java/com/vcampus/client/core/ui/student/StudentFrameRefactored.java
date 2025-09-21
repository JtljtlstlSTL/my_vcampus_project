package com.vcampus.client.core.ui.student;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.client.core.ui.student.model.StudentModel;
import com.vcampus.client.core.ui.student.view.StudentView;
import com.vcampus.client.core.ui.student.controller.StudentUIController;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 学生用户主界面 - 重构版本
 * 采用MVC架构模式，将原来的大文件拆分为多个职责明确的类
 *
 * @author VCampus Team
 * @version 2.0
 */
@Slf4j
public class StudentFrameRefactored {
    
    private StudentModel model;
    private StudentView view;
    private StudentUIController controller;
    
    public StudentFrameRefactored(NettyClient nettyClient, Map<String, Object> userData) {
        // 初始化MVC组件
        this.model = new StudentModel(nettyClient, userData);
        this.view = new StudentView(model);
        this.controller = new StudentUIController(model, view);
        
        // 显示界面
        view.setVisible(true);
        
        log.info("学生主界面初始化完成: {}", userData.get("userName"));
    }
    
    /**
     * 获取视图对象（用于外部访问）
     */
    public StudentView getView() {
        return view;
    }
    
    /**
     * 获取模型对象（用于外部访问）
     */
    public StudentModel getModel() {
        return model;
    }
    
    /**
     * 获取控制器对象（用于外部访问）
     */
    public StudentUIController getController() {
        return controller;
    }
}

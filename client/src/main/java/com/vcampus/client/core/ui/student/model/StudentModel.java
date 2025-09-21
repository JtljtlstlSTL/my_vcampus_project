package com.vcampus.client.core.ui.student.model;

import com.vcampus.client.core.net.NettyClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.HashMap;

/**
 * 学生数据模型
 * 负责管理学生相关的数据和状态
 *
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
@Data
public class StudentModel {
    
    private NettyClient nettyClient;
    private Map<String, Object> userData;
    private Map<String, Object> currentStudentData;
    
    // 编辑状态管理
    private boolean isEditing = false;
    private Map<String, Object> editingComponents = new HashMap<>();
    
    // AI助手状态
    private boolean aiVisible = false;
    private int originalWidth;
    
    public StudentModel(NettyClient nettyClient, Map<String, Object> userData) {
        this.nettyClient = nettyClient;
        this.userData = userData;
        this.currentStudentData = new HashMap<>(userData);
    }
    
    /**
     * 更新学生数据
     */
    public void updateStudentData(Map<String, Object> newData) {
        this.currentStudentData = new HashMap<>(newData);
        log.info("学生数据已更新: {}", newData.get("userName"));
    }
    
    /**
     * 获取学生ID
     */
    public String getStudentId() {
        return userData.get("cardNumber") != null ? userData.get("cardNumber").toString() : "";
    }
    
    /**
     * 获取学生姓名
     */
    public String getStudentName() {
        return userData.get("userName") != null ? userData.get("userName").toString() : "";
    }
    
    /**
     * 获取学生性别
     */
    public String getStudentGender() {
        return userData.get("gender") != null ? userData.get("gender").toString() : "男";
    }
    
    /**
     * 设置编辑状态
     */
    public void setEditing(boolean editing) {
        this.isEditing = editing;
        if (!editing) {
            editingComponents.clear();
        }
    }
    
    /**
     * 设置AI助手可见性
     */
    public void setAiVisible(boolean visible) {
        this.aiVisible = visible;
    }
    
    /**
     * 获取AI助手可见性
     */
    public boolean isAiVisible() {
        return this.aiVisible;
    }
}

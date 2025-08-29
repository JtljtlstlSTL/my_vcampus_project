package com.vcampus.common.entity;

import com.vcampus.common.enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户实体类 
 * 
 * @author VCampus Team
 * @version 3.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements IEntity {
    
    private Integer cardNum;    // 用户卡号 - 全校唯一标识（主键）
    private String password;    // 用户密码 - 哈希存储
    private String name;        // 用户姓名
    private Gender gender;      // 性别
    private String phone;       // 手机号码
    private String userType;    // 用户类型/角色字符串
    
    /**
     * 获取用户角色数组
     */
    public String[] getRoles() {
        if (userType == null || userType.trim().isEmpty()) {
            return new String[0];
        }
        return userType.split(",");
    }
    
    /**
     * 设置用户角色
     */
    public void setRoles(String[] roles) {
        if (roles == null || roles.length == 0) {
            this.userType = "";
        } else {
            this.userType = String.join(",", roles);
        }
    }
    
    /**
     * 获取主要角色（第一个角色）
     */
    public String getPrimaryRole() {
        if (userType == null || userType.trim().isEmpty()) {
            return "";
        }
        String[] roles = userType.split(",");
        return roles.length > 0 ? roles[0].trim() : "";
    }
}
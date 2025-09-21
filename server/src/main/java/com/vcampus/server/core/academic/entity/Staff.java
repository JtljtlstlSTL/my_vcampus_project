package com.vcampus.server.core.academic.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 教师实体类 - 对应数据库tblStaff表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Staff {

    private String cardNum;     // 登录ID，外键关联tblUser
    private String staffId;     // 工号
    private String title;       // 职称/职位
    private String department;  // 学院

    // 来自tblUser表的基本信息
    private String name;        // 姓名
    private Integer age;        // 年龄
    private String gender;      // 性别
    private String phone;       // 电话号码
}

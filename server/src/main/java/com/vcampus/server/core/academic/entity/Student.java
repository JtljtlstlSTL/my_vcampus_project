package com.vcampus.server.core.academic.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 学生实体类 - 对应数据库tblStudent表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    private String cardNum;     // 登录ID，外键关联tblUser
    private String studentId;   // 学号
    private Integer grade;      // 年级 (1-4)
    private String major;       // 专业
    private String department;  // 学院

    // 来自tblUser表的基本信息
    private String name;        // 姓名
    private Integer age;        // 年龄
    private String gender;      // 性别
    private String phone;       // 电话号码
}

package com.vcampus.server.entity;

import com.vcampus.common.entity.User;
import com.vcampus.server.enums.Gender;
import com.vcampus.server.enums.PoliticalStatus;
import com.vcampus.server.enums.StudentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

/**
 * 学生实体类
 * 参考SEU项目设计，扩展用户信息为学生详细信息
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student implements IEntity {
    
    /**
     * 学生卡号 - 与User表的cardNum对应
     */
    private Integer cardNumber;
    
    /**
     * 学号 - 学生唯一标识
     */
    private String studentNumber;
    
    /**
     * 姓（家族名）
     */
    private String familyName;
    
    /**
     * 名（给定名）
     */
    private String givenName;
    
    /**
     * 性别
     */
    private Gender gender;
    
    /**
     * 出生日期
     */
    private Date birthDate;
    
    /**
     * 专业
     */
    private String major;
    
    /**
     * 学院
     */
    private String school;
    
    /**
     * 学生状态
     */
    private StudentStatus status;
    
    /**
     * 出生地
     */
    private String birthPlace;
    
    /**
     * 政治面貌
     */
    private PoliticalStatus politicalStatus;
    
    /**
     * 入学年份
     */
    private Integer enrollmentYear;
    
    /**
     * 年级
     */
    private String grade;
    
    /**
     * 班级
     */
    private String className;
    
    /**
     * 宿舍号
     */
    private String dormitoryNumber;
    
    /**
     * 紧急联系人姓名
     */
    private String emergencyContactName;
    
    /**
     * 紧急联系人电话
     */
    private String emergencyContactPhone;
    
    /**
     * 家庭地址
     */
    private String homeAddress;
    
    /**
     * 身份证号码
     */
    private String idCardNumber;
    
    /**
     * 便捷构造方法 - 基本信息
     * 
     * @param cardNumber 卡号
     * @param studentNumber 学号
     * @param familyName 姓
     * @param givenName 名
     * @param gender 性别
     * @param major 专业
     * @param school 学院
     */
    public Student(Integer cardNumber, String studentNumber, String familyName, 
                  String givenName, Gender gender, String major, String school) {
        this.cardNumber = cardNumber;
        this.studentNumber = studentNumber;
        this.familyName = familyName;
        this.givenName = givenName;
        this.gender = gender;
        this.major = major;
        this.school = school;
        this.status = StudentStatus.IN_SCHOOL;
        this.politicalStatus = PoliticalStatus.MASSES;
    }
    
    /**
     * 获取完整姓名
     * 
     * @return 完整姓名
     */
    public String getFullName() {
        if (familyName == null && givenName == null) {
            return "";
        }
        if (familyName == null) {
            return givenName;
        }
        if (givenName == null) {
            return familyName;
        }
        return familyName + givenName;
    }
    
    /**
     * 设置完整姓名（自动分离姓和名）
     * 
     * @param fullName 完整姓名
     */
    public void setFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            this.familyName = "";
            this.givenName = "";
            return;
        }
        
        fullName = fullName.trim();
        if (fullName.length() == 1) {
            this.familyName = "";
            this.givenName = fullName;
        } else if (fullName.length() == 2) {
            this.familyName = fullName.substring(0, 1);
            this.givenName = fullName.substring(1);
        } else {
            // 对于3个字符以上的姓名，假设最后两个字符是名
            this.familyName = fullName.substring(0, fullName.length() - 2);
            this.givenName = fullName.substring(fullName.length() - 2);
        }
    }
    
    /**
     * 计算年龄
     * 
     * @return 年龄
     */
    public Integer getAge() {
        if (birthDate == null) {
            return null;
        }
        
        Date now = new Date();
        long diff = now.getTime() - birthDate.getTime();
        return (int) (diff / (365.25 * 24 * 60 * 60 * 1000));
    }
    
    /**
     * 检查是否毕业
     * 
     * @return 是否毕业
     */
    public boolean isGraduated() {
        return status == StudentStatus.GRADUATED;
    }
    
    /**
     * 检查是否在校
     * 
     * @return 是否在校
     */
    public boolean isInSchool() {
        return status == StudentStatus.IN_SCHOOL;
    }
    
    /**
     * 从User对象创建Student对象
     * 
     * @param user 用户对象
     * @return 学生对象
     */
    /*
    public static Student createFromUser(User user) {
        Student student = new Student();
        student.setCardNumber(user.getCardNum());
        student.setStudentNumber(""); // 需要单独设置
        student.setFamilyName("");
        student.setGivenName(user.getName());
        student.setGender(user.getGender());
        student.setBirthDate(null);
        student.setMajor("");
        student.setSchool("");
        student.setStatus(StudentStatus.IN_SCHOOL);
        student.setBirthPlace("");
        student.setPoliticalStatus(PoliticalStatus.MASSES);
        return student;
    }
    */
    
    /**
     * 获取学生基本信息摘要
     * 
     * @return 基本信息摘要
     */
    public String getSummary() {
        return String.format("学生[%s] %s - %s %s (%s)", 
                studentNumber, getFullName(), school, major, status.getLabel());
    }
    
    /**
     * 脱敏输出 - 隐藏敏感信息
     * 
     * @return 脱敏后的Student对象
     */
    public Student sanitized() {
        Student sanitized = new Student();
        sanitized.cardNumber = this.cardNumber;
        sanitized.studentNumber = this.studentNumber;
        sanitized.familyName = this.familyName;
        sanitized.givenName = this.givenName;
        sanitized.gender = this.gender;
        sanitized.birthDate = this.birthDate;
        sanitized.major = this.major;
        sanitized.school = this.school;
        sanitized.status = this.status;
        sanitized.birthPlace = this.birthPlace;
        sanitized.politicalStatus = this.politicalStatus;
        sanitized.enrollmentYear = this.enrollmentYear;
        sanitized.grade = this.grade;
        sanitized.className = this.className;
        sanitized.dormitoryNumber = this.dormitoryNumber;
        
        // 脱敏敏感信息
        sanitized.emergencyContactPhone = maskPhone(this.emergencyContactPhone);
        sanitized.homeAddress = maskAddress(this.homeAddress);
        sanitized.idCardNumber = maskIdCard(this.idCardNumber);
        sanitized.emergencyContactName = this.emergencyContactName;
        
        return sanitized;
    }
    
    /**
     * 手机号脱敏
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
    
    /**
     * 地址脱敏
     */
    private String maskAddress(String address) {
        if (address == null || address.length() < 6) {
            return address;
        }
        return address.substring(0, 6) + "****";
    }
    
    /**
     * 身份证号脱敏
     */
    private String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }
    
    @Override
    public String toString() {
        return String.format("Student{studentNumber='%s', name='%s', major='%s', school='%s', status=%s}", 
                studentNumber, getFullName(), major, school, status);
    }
}

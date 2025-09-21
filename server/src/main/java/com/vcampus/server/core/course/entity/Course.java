package com.vcampus.server.core.course.entity;

/**
 * 课程实体类，对应tblCourse表
 */
public class Course {
    private int course_Id;         // 课程号
    private String courseName;     // 课程名
    private int Credit;            // 学分
    private String Department;     // 开课学院

    public Course() {}

    public Course(int course_Id, String courseName, int Credit, String Department) {
        this.course_Id = course_Id;
        this.courseName = courseName;
        this.Credit = Credit;
        this.Department = Department;
    }

    public int getCourse_Id() {
        return course_Id;
    }

    public void setCourse_Id(int course_Id) {
        this.course_Id = course_Id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getCredit() {
        return Credit;
    }

    public void setCredit(int Credit) {
        this.Credit = Credit;
    }

    public String getDepartment() {
        return Department;
    }

    public void setDepartment(String Department) {
        this.Department = Department;
    }
}

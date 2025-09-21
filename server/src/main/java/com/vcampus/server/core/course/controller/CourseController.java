package com.vcampus.server.core.course.controller;

import com.vcampus.server.core.course.entity.Course;
import com.vcampus.server.core.course.service.CourseService;
import com.vcampus.server.core.common.annotation.RouteMapping;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;

import java.util.List;
import java.util.Map;

public class CourseController {
    private final CourseService courseService = new CourseService();

    @RouteMapping(uri = "course/handleRequest", role = "all", description = "处理课程相关请求（学生、教师、管理员均可访问）")
    public Response handleRequest(Request request) {
        return handleCourseRequest(request);
    }

    @RouteMapping(uri = "course/manager", role = "manager", description = "课程管理相关请求（管理员专用）")
    public Response handleManagerRequest(Request request) {
        return handleCourseRequest(request);
    }

    private Response handleCourseRequest(Request request) {
        String action = request.getParam("action");
        String cardNum = request.getParam("cardNum");

        try {
            switch (action) {
                case "GET_SELECTED_COURSES":
                    List<Map<String, Object>> selected = courseService.getSelectedCourses(cardNum);
                    return Response.Builder.success(selected);

                case "GET_AVAILABLE_COURSES":
                    List<Map<String, Object>> available = courseService.getAvailableCourses(cardNum);
                    return Response.Builder.success(available);

                case "SELECT_COURSE":
                    String selectSectionId = request.getParam("sectionId");
                    try {
                        boolean ok = courseService.selectCourse(cardNum, selectSectionId);
                        return ok ? Response.Builder.success() : Response.Builder.error("选课失败");
                    } catch (RuntimeException e) {
                        return Response.Builder.error("选课失败：" + e.getMessage());
                    }

                case "DROP_COURSE":
                    String dropSectionId = request.getParam("sectionId");
                    try {
                        boolean dropOk = courseService.dropCourse(cardNum, dropSectionId);
                        return dropOk ? Response.Builder.success() : Response.Builder.error("退课失败");
                    } catch (RuntimeException e) {
                        return Response.Builder.error("退课失败：" + e.getMessage());
                    }

                case "INSERT_COURSE": {
                    String courseName = request.getParam("courseName");
                    String creditStr = request.getParam("credit");
                    String department = request.getParam("department");

                    if (courseName == null || creditStr == null || department == null) {
                        return Response.Builder.error("参数不完整：课程名、学分和开课学院不能为空");
                    }

                    try {
                        int credit = Integer.parseInt(creditStr);
                        if (credit < 1 || credit > 5) {
                            return Response.Builder.error("学分必须在1-5之间");
                        }
                        Course course = new Course(0, courseName, credit, department);
                        boolean insertOk = courseService.insertCourse(course);
                        return insertOk ? Response.Builder.success() : Response.Builder.error("新增课程失败");
                    } catch (NumberFormatException e) {
                        return Response.Builder.error("学分格式错误，必须为数字");
                    } catch (Exception e) {
                        return Response.Builder.error("新增课程失败：" + e.getMessage());
                    }
                }

                case "UPDATE_COURSE": {
                    String courseIdStr = request.getParam("courseId");
                    String courseName = request.getParam("courseName");
                    String creditStr = request.getParam("credit");
                    String department = request.getParam("department");

                    if (courseIdStr == null || courseName == null || creditStr == null || department == null) {
                        return Response.Builder.error("参数不完整：所有字段不能为空");
                    }

                    try {
                        int updateCourseId = Integer.parseInt(courseIdStr);
                        int credit = Integer.parseInt(creditStr);
                        if (credit < 1 || credit > 5) {
                            return Response.Builder.error("学分必须在1-5之间");
                        }
                        Course course = new Course(updateCourseId, courseName, credit, department);
                        boolean updateOk = courseService.updateCourse(course);
                        return updateOk ? Response.Builder.success() : Response.Builder.error("修改课程失败");
                    } catch (NumberFormatException e) {
                        return Response.Builder.error("课程ID或学分格式错误，必须为数字");
                    } catch (Exception e) {
                        return Response.Builder.error("修改课程失败：" + e.getMessage());
                    }
                }

                case "DELETE_COURSE": {
                    String deleteCourseId = request.getParam("courseId");
                    if (deleteCourseId == null) {
                        return Response.Builder.error("课程ID不能为空");
                    }

                    // 添加调试信息
                    System.out.println("收到删除课程请求 - 原始courseId: [" + deleteCourseId + "], 长度: " + deleteCourseId.length());

                    try {
                        // 去除前后空格
                        deleteCourseId = deleteCourseId.trim();
                        System.out.println("去除空格后的courseId: [" + deleteCourseId + "], 长度: " + deleteCourseId.length());

                        // 优化：支持浮点数格式（如"1.0"、"2.0"等）
                        int courseId;
                        if (deleteCourseId.contains(".")) {
                            // 处理浮点数格式
                            try {
                                double courseIdDouble = Double.parseDouble(deleteCourseId);
                                courseId = (int) courseIdDouble;
                                System.out.println("浮点数转换：" + deleteCourseId + " -> " + courseId);
                            } catch (NumberFormatException e) {
                                return Response.Builder.error("课程ID格式错误，无法解析浮点数: [" + deleteCourseId + "]");
                            }
                        } else {
                            // 处理整数格式
                            if (!deleteCourseId.matches("\\d+")) {
                                return Response.Builder.error("课程ID格式错误，包含非数字字符: [" + deleteCourseId + "]");
                            }
                            courseId = Integer.parseInt(deleteCourseId);
                        }

                        System.out.println("最终解析的courseId: " + courseId);

                        boolean deleteOk = courseService.deleteCourse(courseId);
                        return deleteOk ? Response.Builder.success() : Response.Builder.error("删除课程失败");
                    } catch (NumberFormatException e) {
                        return Response.Builder.error("课程ID格式错误，必须为数字，实际值: [" + deleteCourseId + "], 错误: " + e.getMessage());
                    } catch (Exception e) {
                        return Response.Builder.error("删除课程失败：" + e.getMessage());
                    }
                }

                case "GET_ALL_COURSES":
                    List<Map<String, Object>> allCourses = courseService.getAllCoursesAsMap();
                    return Response.Builder.success(allCourses);

                case "GET_ALL_SECTIONS":
                    List<Map<String, Object>> allSections = courseService.getAllSections();
                    return Response.Builder.success(allSections);

                // 教师端功能
                case "GET_TEACHER_SECTIONS": {
                    String teacherId = request.getParam("teacherId");
                    if (teacherId == null) {
                        return Response.Builder.error("教师ID不能为空");
                    }

                    try {
                        List<Map<String, Object>> sections = courseService.getTeacherSections(teacherId);
                        return Response.Builder.success(sections);
                    } catch (Exception e) {
                        return Response.Builder.error("获取教师教学班失败：" + e.getMessage());
                    }
                }

                case "GET_SECTION_STUDENTS": {
                    String sectionIdStr = request.getParam("sectionId");
                    if (sectionIdStr == null) {
                        return Response.Builder.error("教学班ID不能为空");
                    }

                    try {
                        int sectionId = Integer.parseInt(sectionIdStr);
                        List<Map<String, Object>> students = courseService.getSectionStudents(sectionId);
                        return Response.Builder.success(students);
                    } catch (NumberFormatException e) {
                        return Response.Builder.error("教学班ID格式错误");
                    } catch (Exception e) {
                        return Response.Builder.error("获取教学班学生失败：" + e.getMessage());
                    }
                }

                case "UPDATE_STUDENT_SCORE": {
                    String studentCardNum = request.getParam("cardNum");
                    String sectionIdStr = request.getParam("sectionId");
                    String scoreStr = request.getParam("score");
                    String gpaStr = request.getParam("gpa");

                    if (studentCardNum == null || sectionIdStr == null || scoreStr == null || gpaStr == null) {
                        return Response.Builder.error("参数不完整：学号、教学班ID、分数、GPA不能为空");
                    }

                    try {
                        int sectionId = Integer.parseInt(sectionIdStr);
                        double score = Double.parseDouble(scoreStr);
                        double gpa = Double.parseDouble(gpaStr);

                        if (score < 0 || score > 100) {
                            return Response.Builder.error("分数必须在0-100之间");
                        }
                        if (gpa < 0 || gpa > 4.8) {
                            return Response.Builder.error("GPA必须在0-4.8之间");
                        }

                        boolean updateOk = courseService.updateStudentScore(studentCardNum, sectionId, score, gpa);
                        return updateOk ? Response.Builder.success() : Response.Builder.error("更新成绩失败");
                    } catch (NumberFormatException e) {
                        return Response.Builder.error("数字格式错误");
                    } catch (Exception e) {
                        return Response.Builder.error("更新成绩失败：" + e.getMessage());
                    }
                }

                case "BATCH_UPDATE_SCORES": {
                    String sectionIdStr = request.getParam("sectionId");
                    String scoresData = request.getParam("scoresData"); // JSON格式的成绩数据

                    if (sectionIdStr == null || scoresData == null) {
                        return Response.Builder.error("参数不完整：教学班ID和成绩数据不能为空");
                    }

                    try {
                        int sectionId = Integer.parseInt(sectionIdStr);
                        boolean updateOk = courseService.batchUpdateScores(sectionId, scoresData);
                        return updateOk ? Response.Builder.success() : Response.Builder.error("批量更新成绩失败");
                    } catch (NumberFormatException e) {
                        return Response.Builder.error("教学班ID格式错误");
                    } catch (Exception e) {
                        return Response.Builder.error("批量更新成绩失败：" + e.getMessage());
                    }
                }

                case "GET_SECTION_DETAILS": {
                    String sectionIdStr = request.getParam("sectionId");
                    if (sectionIdStr == null) {
                        return Response.Builder.error("教学班ID不能为空");
                    }

                    try {
                        int sectionId = Integer.parseInt(sectionIdStr);
                        Map<String, Object> sectionDetails = courseService.getSectionDetails(sectionId);
                        return Response.Builder.success(sectionDetails);
                    } catch (NumberFormatException e) {
                        return Response.Builder.error("教学班ID格式错误");
                    } catch (Exception e) {
                        return Response.Builder.error("获取教学班详情失败：" + e.getMessage());
                    }
                }

                case "GET_STUDENT_GRADE_STATISTICS": {
                    if (cardNum == null) {
                        return Response.Builder.error("学生卡号不能为空");
                    }

                    try {
                        Map<String, Object> statistics = courseService.getStudentGradeStatistics(cardNum);
                        return Response.Builder.success(statistics);
                    } catch (Exception e) {
                        return Response.Builder.error("获取学生成绩统计失败：" + e.getMessage());
                    }
                }

                case "GET_COURSE_BY_TEACHER": {
                    String teacherId = request.getParam("teacherId");
                    if (teacherId == null) {
                        return Response.Builder.error("教师ID不能为空");
                    }
                    try {
                        List<Map<String, Object>> courses = courseService.getCoursesByTeacher(teacherId);
                        return Response.Builder.success(courses);
                    } catch (Exception e) {
                        return Response.Builder.error("获取教师课程失败：" + e.getMessage());
                    }
                }

                case "GET_SCORE_LIST": {
                    // 处理根据学生卡号查询成绩的请求
                    if (cardNum != null && !cardNum.trim().isEmpty()) {
                        try {
                            List<Map<String, Object>> scoreList = courseService.getScoreList(cardNum);
                            return Response.Builder.success(scoreList);
                        } catch (Exception e) {
                            return Response.Builder.error("获取学生成绩失败：" + e.getMessage());
                        }
                    }

                    // 原有的根据教学班ID查询成绩的逻辑（用于教师端）
                    String sectionIdStr = request.getParam("sectionId");
                    if (sectionIdStr == null) {
                        return Response.Builder.error("教学班ID不能为空");
                    }
                    try {
                        int sectionId = Integer.parseInt(sectionIdStr);
                        List<Map<String, Object>> scoreList = courseService.getScoreList(sectionId);
                        return Response.Builder.success(scoreList);
                    } catch (NumberFormatException e) {
                        return Response.Builder.error("教学班ID格式错误");
                    } catch (Exception e) {
                        return Response.Builder.error("获取成绩列表失败：" + e.getMessage());
                    }
                }

                case "SEARCH_COURSES": {
                    String keyword = request.getParam("keyword");
                    try {
                        List<Course> courses = courseService.searchCourses(keyword);
                        // 转换为Map格式，保证字段映射一致
                        List<Map<String, Object>> result = new java.util.ArrayList<>();
                        for (Course course : courses) {
                            Map<String, Object> courseMap = new java.util.HashMap<>();
                            courseMap.put("course_Id", course.getCourse_Id());
                            courseMap.put("courseName", course.getCourseName());
                            courseMap.put("credit", course.getCredit());
                            courseMap.put("department", course.getDepartment());
                            result.add(courseMap);
                        }
                        return Response.Builder.success(result);
                    } catch (Exception e) {
                        return Response.Builder.error("搜索课程失败：" + e.getMessage());
                    }
                }
                case "SEARCH_SECTIONS": {
                    String keyword = request.getParam("keyword");
                    try {
                        List<Map<String, Object>> sections = courseService.searchSections(keyword);
                        return Response.Builder.success(sections);
                    } catch (Exception e) {
                        return Response.Builder.error("搜索教学班失败：" + e.getMessage());
                    }
                }


                case "DELETE_SECTION": {
                    String sectionIdStr = request.getParam("sectionId");
                    if (sectionIdStr == null || sectionIdStr.trim().isEmpty()) {
                        return Response.Builder.error("教学班ID不能为空");
                    }

                    try {
                        // 去除前后空格
                        sectionIdStr = sectionIdStr.trim();

                        // 支持浮点数格式（如"1.0"、"2.0"等）
                        int sectionId;
                        if (sectionIdStr.contains(".")) {
                            // 处理浮点数格式
                            try {
                                double sectionIdDouble = Double.parseDouble(sectionIdStr);
                                sectionId = (int) sectionIdDouble;
                            } catch (NumberFormatException e) {
                                return Response.Builder.error("教学班ID格式错误，无法解析浮点数: [" + sectionIdStr + "]");
                            }
                        } else {
                            // 处理整数格式
                            if (!sectionIdStr.matches("\\d+")) {
                                return Response.Builder.error("教学班ID格式错误，包含非数字字符: [" + sectionIdStr + "]");
                            }
                            sectionId = Integer.parseInt(sectionIdStr);
                        }

                        boolean deleteOk = courseService.deleteSection(sectionId);
                        return deleteOk ? Response.Builder.success() : Response.Builder.error("删除教学班失败");
                    } catch (NumberFormatException e) {
                        return Response.Builder.error("教学班ID格式错误，必须为数字，实际值: [" + sectionIdStr + "], 错误: " + e.getMessage());
                    } catch (Exception e) {
                        return Response.Builder.error("删除教学班失败：" + e.getMessage());
                    }
                }

                case "INSERT_SECTION": {
                    String courseIdStr = request.getParam("courseId");
                    String term = request.getParam("term");
                    String teacherId = request.getParam("teacherId");
                    String room = request.getParam("room");
                    String capacityStr = request.getParam("capacity");
                    String schedule = request.getParam("schedule");

                    if (courseIdStr == null || term == null || teacherId == null ||
                        room == null || capacityStr == null || schedule == null) {
                        return Response.Builder.error("参数不完整：所有字段不能为空");
                    }

                    try {
                        int courseId = parseIntRobust(courseIdStr, "课程ID");
                        int capacity = parseIntRobust(capacityStr, "容量");
                        if (capacity <= 0) {
                            return Response.Builder.error("容量必须大于0");
                        }
                        boolean insertOk = courseService.insertSection(courseId, term, teacherId, room, capacity, schedule);
                        return insertOk ? Response.Builder.success() : Response.Builder.error("新增教学班失败");
                    } catch (NumberFormatException e) {
                        return Response.Builder.error("课程ID或容量格式错误，必须为数字");
                    } catch (Exception e) {
                        return Response.Builder.error("新增教学班失败：" + e.getMessage());
                    }
                }

                case "UPDATE_SECTION": {
                    String sectionIdStr = request.getParam("sectionId");
                    String courseIdStr = request.getParam("courseId");
                    String term = request.getParam("term");
                    String teacherId = request.getParam("teacherId");
                    String room = request.getParam("room");
                    String capacityStr = request.getParam("capacity");
                    String schedule = request.getParam("schedule");

                    if (sectionIdStr == null || courseIdStr == null || term == null ||
                        teacherId == null || room == null || capacityStr == null || schedule == null) {
                        return Response.Builder.error("参数不完整：所有字段不能为空");
                    }

                    try {
                        int sectionId = Integer.parseInt(sectionIdStr);
                        int courseId = Integer.parseInt(courseIdStr);
                        int capacity = Integer.parseInt(capacityStr);

                        if (capacity <= 0) {
                            return Response.Builder.error("容量必须大于0");
                        }

                        boolean updateOk = courseService.updateSection(sectionId, courseId, term, teacherId, room, capacity, schedule);
                        return updateOk ? Response.Builder.success() : Response.Builder.error("修改教学班失败");
                    } catch (NumberFormatException e) {
                        return Response.Builder.error("教学班ID、课程ID或容量格式错误，必须为数字");
                    } catch (Exception e) {
                        return Response.Builder.error("修改教学班失败：" + e.getMessage());
                    }
                }

                // 评教相关功能
                case "submitEvaluation": {
                    String studentId = request.getParam("student_Id");
                    String sectionIdStr = request.getParam("section_Id");
                    String scoreStr = request.getParam("score");

                    if (studentId == null || sectionIdStr == null || scoreStr == null) {
                        return Response.Builder.error("参数不完整：学生ID、教学班ID、评分不能为空");
                    }

                    try {
                        int sectionId = Integer.parseInt(sectionIdStr);
                        double score = Double.parseDouble(scoreStr);

                        if (score < 0 || score > 10) {
                            return Response.Builder.error("评分必须在0-10之间");
                        }

                        boolean success = courseService.submitEvaluation(studentId, sectionId, score);
                        return success ? Response.Builder.success() : Response.Builder.error("提交评教失败");
                    } catch (NumberFormatException e) {
                        return Response.Builder.error("参数格式错误");
                    } catch (Exception e) {
                        return Response.Builder.error("提交评教失败：" + e.getMessage());
                    }
                }

                case "getEvaluationStats": {
                    String sectionIdStr = request.getParam("section_Id");
                    System.out.println("获取评教统计请求 - sectionId: " + sectionIdStr);
                    
                    if (sectionIdStr == null) {
                        return Response.Builder.error("教学班ID不能为空");
                    }

                    try {
                        int sectionId = Integer.parseInt(sectionIdStr);
                        System.out.println("解析sectionId成功: " + sectionId);
                        Map<String, Object> stats = courseService.getEvaluationStats(sectionId);
                        System.out.println("获取评教统计数据: " + stats);
                        return Response.Builder.success(stats);
                    } catch (NumberFormatException e) {
                        System.out.println("sectionId格式错误: " + sectionIdStr);
                        return Response.Builder.error("教学班ID格式错误");
                    } catch (Exception e) {
                        System.out.println("获取评教统计失败: " + e.getMessage());
                        e.printStackTrace();
                        return Response.Builder.error("获取评教统计失败：" + e.getMessage());
                    }
                }

                case "GET_ALL_STUDENT_GRADES": {
                    try {
                        List<Map<String, Object>> studentGrades = courseService.getAllStudentGrades();
                        return Response.Builder.success(studentGrades);
                    } catch (Exception e) {
                        return Response.Builder.error("获取所有学生成绩失败：" + e.getMessage());
                    }
                }

                case "GET_ALL_TEACHER_EVALUATIONS": {
                    try {
                        List<Map<String, Object>> evaluations = courseService.getAllTeacherEvaluations();
                        return Response.Builder.success(evaluations);
                    } catch (Exception e) {
                        return Response.Builder.error("获取所有教师评教失败：" + e.getMessage());
                    }
                }

                case "GET_STUDENT_AVERAGE_GRADES": {
                    String studentId = request.getParam("studentId");
                    if (studentId == null || studentId.trim().isEmpty()) {
                        return Response.Builder.error("学生ID不能为空");
                    }
                    try {
                        Map<String, Object> studentGrades = courseService.getStudentAverageGrades(studentId);
                        return Response.Builder.success(studentGrades);
                    } catch (Exception e) {
                        return Response.Builder.error("获取学生平均成绩失败：" + e.getMessage());
                    }
                }

                default:
                    return Response.Builder.error("不支持的操作：" + action);
            }
        } catch (Exception e) {
            return Response.Builder.error("处理请求时发生错误：" + e.getMessage());
        }
    }

    private int parseIntRobust(String value, String fieldName) throws NumberFormatException {
        if (value == null || value.trim().isEmpty()) {
            throw new NumberFormatException(fieldName + "不能为空");
        }
        value = value.trim();
        if (value.contains(".")) {
            double d = Double.parseDouble(value);
            return (int) d;
        } else {
            return Integer.parseInt(value);
        }
    }
}

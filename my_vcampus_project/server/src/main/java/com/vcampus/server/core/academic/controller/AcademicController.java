package com.vcampus.server.core.academic.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.util.JsonUtils;
import com.vcampus.server.core.academic.service.AcademicService;
import com.vcampus.server.core.common.annotation.RouteMapping;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 学术管理控制器 - MyBatis版本
 * 处理学生和教师信息的增删改查操作
 */
@Slf4j
public class AcademicController {

    private final AcademicService academicService;

    public AcademicController() {
        this.academicService = new AcademicService();
    }

    /**
     * 处理学术管理相关请求
     */
    @RouteMapping(uri = "ACADEMIC", role = "manager", description = "学术管理操作")
    public Response handleRequest(Request request) {
        try {
            String action = request.getParam("action");
            if (action == null) {
                return Response.Builder.badRequest("缺少action参数");
            }

            switch (action) {
                // 学生相关操作
                case "GET_ALL_STUDENTS_WITH_USER_INFO":
                    return getAllStudentsWithUserInfo();

                case "GET_STUDENT_BY_CARD_NUM":
                    return getStudentByCardNum(request);

                case "GET_STUDENT_BY_STUDENT_ID":
                    return getStudentByStudentId(request);

                case "ADD_STUDENT":
                    return addStudent(request);

                case "UPDATE_STUDENT":
                    return updateStudent(request);

                case "DELETE_STUDENT":
                    return deleteStudent(request);

                // 教师相关操作
                case "GET_ALL_STAFF_WITH_USER_INFO":
                    return getAllStaffWithUserInfo();

                case "GET_STAFF_BY_CARD_NUM":
                    return getStaffByCardNum(request);

                case "GET_STAFF_BY_STAFF_ID":
                    return getStaffByStaffId(request);

                case "ADD_STAFF":
                    return addStaff(request);

                case "UPDATE_STAFF":
                    return updateStaff(request);

                case "DELETE_STAFF":
                    return deleteStaff(request);

                default:
                    log.warn("未知的学术管理操作: {}", action);
                    return Response.Builder.badRequest("不支持的操作类型: " + action);
            }
        } catch (Exception e) {
            log.error("处理学术管理请求时发生错误", e);
            return Response.Builder.internalError("处理请求时发生错误: " + e.getMessage());
        }
    }

    // 学生相关方法
    private Response getAllStudentsWithUserInfo() {
        try {
            List<Map<String, Object>> studentDataList = academicService.getAllStudentsWithUserInfo();
            log.info("获取学生及用户信息列表成功，共 {} 条记录", studentDataList.size());
            return Response.Builder.success("获取学生及用户信息列表成功", studentDataList);
        } catch (Exception e) {
            log.error("获取学生及用户信息列表失败", e);
            return Response.Builder.error("获取学生及用户信息列表失败: " + e.getMessage());
        }
    }

    private Response getStudentByCardNum(Request request) {
        try {
            String cardNum = request.getParam("cardNum");
            if (cardNum == null || cardNum.trim().isEmpty()) {
                return Response.Builder.badRequest("卡号不能为空");
            }

            Map<String, Object> studentData = academicService.getStudentByCardNum(cardNum);
            if (studentData != null) {
                log.info("根据卡号获取学生信息成功: {}", cardNum);
                return Response.Builder.success("获取学生信息成功", studentData);
            } else {
                return Response.Builder.error("学生不存在: " + cardNum);
            }
        } catch (Exception e) {
            log.error("根据卡号获取学生信息失败", e);
            return Response.Builder.error("获取学生信息失败: " + e.getMessage());
        }
    }

    private Response getStudentByStudentId(Request request) {
        try {
            String studentId = request.getParam("studentId");
            if (studentId == null || studentId.trim().isEmpty()) {
                return Response.Builder.badRequest("学号不能为空");
            }

            Map<String, Object> studentData = academicService.getStudentByStudentId(studentId);
            if (studentData != null) {
                log.info("根据学号获取学生信息成功: {}", studentId);
                return Response.Builder.success("获取学生信息成功", studentData);
            } else {
                return Response.Builder.error("学生不存在: " + studentId);
            }
        } catch (Exception e) {
            log.error("根据学号获取学生信息失败", e);
            return Response.Builder.error("获取学生信息失败: " + e.getMessage());
        }
    }

    private Response addStudent(Request request) {
        try {
            String studentJson = request.getParam("student");
            if (studentJson == null || studentJson.trim().isEmpty()) {
                return Response.Builder.badRequest("学生信息不能为空");
            }

            Map<String, Object> studentData = JsonUtils.fromJson(studentJson, Map.class);
            if (studentData == null) {
                return Response.Builder.badRequest("学生信息格式错误");
            }

            boolean success = academicService.addStudent(studentData);
            if (success) {
                String name = (String) studentData.get("name");
                log.info("添加学生成功: {}", name);
                return Response.Builder.success("添加学生成功");
            } else {
                return Response.Builder.error("添加学生失败");
            }
        } catch (Exception e) {
            log.error("添加学生失败", e);
            return Response.Builder.error("添加学生失败: " + e.getMessage());
        }
    }

    private Response updateStudent(Request request) {
        try {
            String studentJson = request.getParam("student");
            if (studentJson == null || studentJson.trim().isEmpty()) {
                return Response.Builder.badRequest("学生信息不能为空");
            }

            Map<String, Object> studentData = JsonUtils.fromJson(studentJson, Map.class);
            if (studentData == null) {
                return Response.Builder.badRequest("学生信息格式错误");
            }

            boolean success = academicService.updateStudent(studentData);
            if (success) {
                String name = (String) studentData.get("name");
                log.info("更新学生信息成功: {}", name);
                return Response.Builder.success("更新学生信息成功");
            } else {
                return Response.Builder.error("更新学生信息失败");
            }
        } catch (Exception e) {
            log.error("更新学生信息失败", e);
            return Response.Builder.error("更新学生信息失败: " + e.getMessage());
        }
    }

    private Response deleteStudent(Request request) {
        try {
            String cardNum = request.getParam("cardNum");
            if (cardNum == null || cardNum.trim().isEmpty()) {
                return Response.Builder.badRequest("卡号不能为空");
            }

            boolean success = academicService.deleteStudent(cardNum);
            if (success) {
                log.info("删除学生成功: {}", cardNum);
                return Response.Builder.success("删除学生成功");
            } else {
                return Response.Builder.error("删除学生失败");
            }
        } catch (Exception e) {
            log.error("删除学生失败", e);
            return Response.Builder.error("删除学生失败: " + e.getMessage());
        }
    }

    // 教师相关方法
    private Response getAllStaffWithUserInfo() {
        try {
            List<Map<String, Object>> staffDataList = academicService.getAllStaffWithUserInfo();
            log.info("获取教师及用户信息列表成功，共 {} 条记录", staffDataList.size());
            return Response.Builder.success("获取教师及用户信息列表成功", staffDataList);
        } catch (Exception e) {
            log.error("获取教师及用户信息列表失败", e);
            return Response.Builder.error("获取教师及用户信息列表失败: " + e.getMessage());
        }
    }

    private Response getStaffByCardNum(Request request) {
        try {
            String cardNum = request.getParam("cardNum");
            if (cardNum == null || cardNum.trim().isEmpty()) {
                return Response.Builder.badRequest("卡号不能为空");
            }

            Map<String, Object> staffData = academicService.getStaffByCardNum(cardNum);
            if (staffData != null) {
                log.info("根据卡号获取教师信息成功: {}", cardNum);
                return Response.Builder.success("获取教师信息成功", staffData);
            } else {
                return Response.Builder.error("教师不存在: " + cardNum);
            }
        } catch (Exception e) {
            log.error("根据卡号获取教师信息失败", e);
            return Response.Builder.error("获取教师信息失败: " + e.getMessage());
        }
    }

    private Response getStaffByStaffId(Request request) {
        try {
            String staffId = request.getParam("staffId");
            if (staffId == null || staffId.trim().isEmpty()) {
                return Response.Builder.badRequest("工号不能为空");
            }

            Map<String, Object> staffData = academicService.getStaffByStaffId(staffId);
            if (staffData != null) {
                log.info("根据工号获取教师信息成功: {}", staffId);
                return Response.Builder.success("获取教师信息成功", staffData);
            } else {
                return Response.Builder.error("教师不存在: " + staffId);
            }
        } catch (Exception e) {
            log.error("根据工号获取教师信息失败", e);
            return Response.Builder.error("获取教师信息失败: " + e.getMessage());
        }
    }

    private Response addStaff(Request request) {
        try {
            String staffJson = request.getParam("staff");
            if (staffJson == null || staffJson.trim().isEmpty()) {
                return Response.Builder.badRequest("教师信息不能为空");
            }

            Map<String, Object> staffData = JsonUtils.fromJson(staffJson, Map.class);
            if (staffData == null) {
                return Response.Builder.badRequest("教师信息格式错误");
            }

            boolean success = academicService.addStaff(staffData);
            if (success) {
                String name = (String) staffData.get("name");
                log.info("添加教师成功: {}", name);
                return Response.Builder.success("添加教师成功");
            } else {
                return Response.Builder.error("添加教师失败");
            }
        } catch (Exception e) {
            log.error("添加教师失败", e);
            return Response.Builder.error("添加教师失败: " + e.getMessage());
        }
    }

    private Response updateStaff(Request request) {
        try {
            String staffJson = request.getParam("staff");
            if (staffJson == null || staffJson.trim().isEmpty()) {
                return Response.Builder.badRequest("教师信息不能为空");
            }

            Map<String, Object> staffData = JsonUtils.fromJson(staffJson, Map.class);
            if (staffData == null) {
                return Response.Builder.badRequest("教师信息格式错误");
            }

            boolean success = academicService.updateStaff(staffData);
            if (success) {
                String name = (String) staffData.get("name");
                log.info("更新教师信息成功: {}", name);
                return Response.Builder.success("更新教师信息成功");
            } else {
                return Response.Builder.error("更新教师信息失败");
            }
        } catch (Exception e) {
            log.error("更新教师信息失败", e);
            return Response.Builder.error("更新教师信息失败: " + e.getMessage());
        }
    }

    private Response deleteStaff(Request request) {
        try {
            String cardNum = request.getParam("cardNum");
            if (cardNum == null || cardNum.trim().isEmpty()) {
                return Response.Builder.badRequest("卡号不能为空");
            }

            boolean success = academicService.deleteStaff(cardNum);
            if (success) {
                log.info("删除教师成功: {}", cardNum);
                return Response.Builder.success("删除教师成功");
            } else {
                return Response.Builder.error("删除教师失败");
            }
        } catch (Exception e) {
            log.error("删除教师失败", e);
            return Response.Builder.error("删除教师失败: " + e.getMessage());
        }
    }
}
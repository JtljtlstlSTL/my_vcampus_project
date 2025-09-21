package com.vcampus.client.core.service;

import com.vcampus.client.core.net.NettyClient;
import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CourseClientService {
    /**
     * 获取已选课程列表
     */
    public static List<Map<String, Object>> getSelectedCourses(NettyClient nettyClient, String cardNum) throws Exception {
        Request req = new Request("academic/course")
                .addParam("action", "GET_SELECTED_COURSES")
                .addParam("cardNum", cardNum);
        Response resp = nettyClient.sendRequest(req).get(10, TimeUnit.SECONDS);
        if (resp != null && "SUCCESS".equals(resp.getStatus())) {
            return (List<Map<String, Object>>) resp.getData();
        } else {
            throw new Exception(resp != null ? resp.getMessage() : "服务器无响应");
        }
    }

    /**
     * 获取可选课程列表
     */
    public static List<Map<String, Object>> getAvailableCourses(NettyClient nettyClient, String cardNum) throws Exception {
        Request req = new Request("academic/course")
                .addParam("action", "GET_AVAILABLE_COURSES")
                .addParam("cardNum", cardNum);
        Response resp = nettyClient.sendRequest(req).get(10, TimeUnit.SECONDS);
        if (resp != null && "SUCCESS".equals(resp.getStatus())) {
            return (List<Map<String, Object>>) resp.getData();
        } else {
            throw new Exception(resp != null ? resp.getMessage() : "服务器无响应");
        }
    }

    /**
     * 选课
     */
    public static boolean selectCourse(NettyClient nettyClient, String cardNum, String sectionId) throws Exception {
        Request req = new Request("academic/course")
                .addParam("action", "SELECT_COURSE")
                .addParam("cardNum", cardNum)
                .addParam("sectionId", sectionId);
        Response resp = nettyClient.sendRequest(req).get(10, TimeUnit.SECONDS);
        if (resp == null) {
            throw new Exception("服务器无响应");
        }
        if ("SUCCESS".equals(resp.getStatus())) {
            return true;
        } else {
            throw new Exception(resp.getMessage() != null ? resp.getMessage() : "选课失败");
        }
    }

    /**
     * 退课
     */
    public static boolean dropCourse(NettyClient nettyClient, String cardNum, String sectionId) throws Exception {
        Request req = new Request("academic/course")
                .addParam("action", "DROP_COURSE")
                .addParam("cardNum", cardNum)
                .addParam("sectionId", sectionId);
        Response resp = nettyClient.sendRequest(req).get(10, TimeUnit.SECONDS);
        if (resp == null) {
            throw new Exception("服务器无响应");
        }
        if ("SUCCESS".equals(resp.getStatus())) {
            return true;
        } else {
            throw new Exception(resp.getMessage() != null ? resp.getMessage() : "退课失败");
        }
    }

    /**
     * 获取成绩列表
     */
    public static List<Map<String, Object>> getScoreList(NettyClient nettyClient, String cardNum) throws Exception {
        Request req = new Request("academic/course")
                .addParam("action", "GET_SCORE_LIST")
                .addParam("cardNum", cardNum);
        Response resp = nettyClient.sendRequest(req).get(10, TimeUnit.SECONDS);
        if (resp != null && "SUCCESS".equals(resp.getStatus())) {
            return (List<Map<String, Object>>) resp.getData();
        } else {
            throw new Exception(resp != null ? resp.getMessage() : "服务器无响应");
        }
    }

    /**
     * 获取学生成绩统计
     */
    public static Map<String, Object> getStudentGradeStatistics(NettyClient nettyClient, String cardNum) throws Exception {
        Request req = new Request("academic/course")
                .addParam("action", "GET_STUDENT_GRADE_STATISTICS")
                .addParam("cardNum", cardNum);
        Response resp = nettyClient.sendRequest(req).get(10, TimeUnit.SECONDS);
        if (resp != null && "SUCCESS".equals(resp.getStatus())) {
            return (Map<String, Object>) resp.getData();
        } else {
            throw new Exception(resp != null ? resp.getMessage() : "服务器无响应");
        }
    }

    /**
     * 获取学生成绩 - 别名方法，兼容StudentFrame调用
     */
    public static List<Map<String, Object>> getStudentScores(NettyClient nettyClient, String cardNum) throws Exception {
        return getScoreList(nettyClient, cardNum);
    }
}

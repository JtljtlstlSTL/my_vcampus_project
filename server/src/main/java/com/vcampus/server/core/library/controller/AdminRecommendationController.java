package com.vcampus.server.core.library.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.message.Session;
import com.vcampus.server.core.common.annotation.RouteMapping;
import com.vcampus.server.core.library.entity.core.BookRecommendation;
import com.vcampus.server.core.library.entity.result.RecommendResult;
import com.vcampus.server.core.library.entity.view.RecommendationHistory;
import com.vcampus.server.core.library.enums.RecommendStatus;
import com.vcampus.server.core.library.service.BookRecommendationService;
import com.vcampus.server.core.library.service.impl.BookRecommendationServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 管理员荐购控制器
 * 提供管理员审核和管理荐购申请的功能
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class AdminRecommendationController {
    
    private final BookRecommendationService recommendationService;
    
    public AdminRecommendationController() {
        this.recommendationService = new BookRecommendationServiceImpl();
    }
    
    // ==================== 荐购管理功能 ====================
    
    /**
     * 获取所有荐购申请
     */
    @RouteMapping(uri = "/library/admin/recommend/all", role = "admin", description = "获取所有荐购申请")
    public Response getAllRecommendations(Request request) {
        log.info("处理获取所有荐购申请请求: {}", request);
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || !session.hasPermission("admin")) {
                return Response.Builder.forbidden("权限不足，无法查看荐购申请");
            }
            
            // 2. 调用服务层
            List<RecommendationHistory> allRecommendations = recommendationService.getAllRecommendations();
            
            log.info("获取所有荐购申请成功: count={}", allRecommendations.size());
            return Response.Builder.success("查询成功", allRecommendations);
            
        } catch (Exception e) {
            log.error("处理获取所有荐购申请请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取待审核的荐购申请
     */
    @RouteMapping(uri = "/library/admin/recommend/pending", role = "admin", description = "获取待审核的荐购申请")
    public Response getPendingRecommendations(Request request) {
        log.info("处理获取待审核荐购申请请求: {}", request);
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || !session.hasPermission("admin")) {
                return Response.Builder.forbidden("权限不足，无法查看待审核荐购申请");
            }
            
            // 2. 调用服务层 - 获取所有荐购申请，然后筛选待审核的
            List<RecommendationHistory> allRecommendations = recommendationService.getAllRecommendations();
            List<RecommendationHistory> pendingRecommendations = allRecommendations.stream()
                    .filter(rec -> "PENDING".equals(rec.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
            
            log.info("获取待审核荐购申请成功: count={}", pendingRecommendations.size());
            return Response.Builder.success("查询成功", pendingRecommendations);
            
        } catch (Exception e) {
            log.error("处理获取待审核荐购申请请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 审核荐购申请
     */
    @RouteMapping(uri = "/library/admin/recommend/review", role = "admin", description = "审核荐购申请")
    public Response reviewRecommendation(Request request) {
        log.info("处理审核荐购申请请求: {}", request);
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || !session.hasPermission("admin")) {
                return Response.Builder.forbidden("权限不足，无法审核荐购申请");
            }
            
            String adminCardNum = session.getUserId();
            if (adminCardNum == null || adminCardNum.trim().isEmpty()) {
                return Response.Builder.badRequest("管理员信息异常，请重新登录");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            log.info("接收到的参数: {}", params);
            String recIdStr = params.get("recId");
            String action = params.get("action");
            String feedback = params.get("adminFeedback");
            log.info("解析参数: recIdStr={}, action={}, feedback={}", recIdStr, action, feedback);
            
            if (recIdStr == null || recIdStr.trim().isEmpty()) {
                return Response.Builder.badRequest("荐购ID不能为空");
            }
            if (action == null || action.trim().isEmpty()) {
                return Response.Builder.badRequest("审核操作不能为空");
            }
            
            // 3. 解析荐购ID
            Integer recId;
            try {
                recId = Integer.valueOf(recIdStr.trim());
            } catch (NumberFormatException e) {
                return Response.Builder.badRequest("无效的荐购ID: " + recIdStr);
            }
            
            // 4. 状态转换
            RecommendStatus status;
            try {
                if ("approve".equals(action)) {
                    status = RecommendStatus.APPROVED;
                } else if ("reject".equals(action)) {
                    status = RecommendStatus.REJECTED;
                } else {
                    return Response.Builder.badRequest("无效的审核操作: " + action);
                }
            } catch (IllegalArgumentException e) {
                return Response.Builder.badRequest("无效的审核操作: " + action);
            }
            
            // 4. 调用服务层
            RecommendResult result = recommendationService.reviewRecommendation(
                    recId, status, feedback, adminCardNum);
            
            if (result.isSuccess()) {
                log.info("审核荐购申请成功: recId={}, status={}, adminCardNum={}", 
                        recId, status, adminCardNum);
                return Response.Builder.success(result.getMessage(), result.getRecId());
            } else {
                log.warn("审核荐购申请失败: recId={}, status={}, adminCardNum={}, reason={}", 
                        recId, status, adminCardNum, result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理审核荐购申请请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 批量审核荐购申请
     */
    @RouteMapping(uri = "/library/admin/recommend/batch-review", role = "admin", description = "批量审核荐购申请")
    public Response batchReviewRecommendations(Request request) {
        log.info("处理批量审核荐购申请请求: {}", request);
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || !session.hasPermission("admin")) {
                return Response.Builder.forbidden("权限不足，无法批量审核荐购申请");
            }
            
            String adminCardNum = session.getUserId();
            if (adminCardNum == null || adminCardNum.trim().isEmpty()) {
                return Response.Builder.badRequest("管理员信息异常，请重新登录");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String recIdsStr = params.get("recIds");
            String statusStr = params.get("status");
            String feedback = params.get("feedback");
            
            if (recIdsStr == null || recIdsStr.trim().isEmpty()) {
                return Response.Builder.badRequest("荐购ID列表不能为空");
            }
            if (statusStr == null || statusStr.trim().isEmpty()) {
                return Response.Builder.badRequest("审核状态不能为空");
            }
            
            // 3. 解析荐购ID列表
            List<Integer> recIds = java.util.Arrays.stream(recIdsStr.split(","))
                    .map(String::trim)
                    .map(Integer::valueOf)
                    .collect(java.util.stream.Collectors.toList());
            
            // 4. 状态转换
            RecommendStatus status;
            try {
                status = RecommendStatus.fromCode(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.Builder.badRequest("无效的审核状态: " + statusStr);
            }
            
            // 5. 调用服务层
            RecommendResult result = recommendationService.batchReviewRecommendations(
                    recIds, status, feedback, adminCardNum);
            
            if (result.isSuccess()) {
                log.info("批量审核荐购申请成功: recIds={}, status={}, adminCardNum={}", 
                        recIds, status, adminCardNum);
                return Response.Builder.success(result.getMessage());
            } else {
                log.warn("批量审核荐购申请失败: recIds={}, status={}, adminCardNum={}, reason={}", 
                        recIds, status, adminCardNum, result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理批量审核荐购申请请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 查询统计功能 ====================
    
    /**
     * 根据状态查询荐购申请
     */
    @RouteMapping(uri = "/library/admin/recommend/by-status", role = "admin", description = "根据状态查询荐购申请")
    public Response getRecommendationsByStatus(Request request) {
        log.info("处理根据状态查询荐购申请请求: {}", request);
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || !session.hasPermission("admin")) {
                return Response.Builder.forbidden("权限不足，无法查看荐购申请");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String statusStr = params.get("status");
            
            if (statusStr == null || statusStr.trim().isEmpty()) {
                return Response.Builder.badRequest("状态不能为空");
            }
            
            // 3. 状态转换
            RecommendStatus status;
            try {
                status = RecommendStatus.fromCode(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return Response.Builder.badRequest("无效的状态: " + statusStr);
            }
            
            // 4. 调用服务层
            List<RecommendationHistory> recommendations = recommendationService.getRecommendationHistoryByStatus(status);
            
            log.info("根据状态查询荐购申请成功: status={}, count={}", status, recommendations.size());
            return Response.Builder.success("查询成功", recommendations);
            
        } catch (Exception e) {
            log.error("处理根据状态查询荐购申请请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 获取荐购统计信息
     */
    @RouteMapping(uri = "/library/admin/recommend/statistics", role = "admin", description = "获取荐购统计信息")
    public Response getRecommendationStatistics(Request request) {
        log.info("处理获取荐购统计信息请求: {}", request);
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || !session.hasPermission("admin")) {
                return Response.Builder.forbidden("权限不足，无法查看荐购统计信息");
            }
            
            // 2. 调用服务层
            Map<RecommendStatus, Long> statistics = recommendationService.getRecommendationStatistics();
            
            log.info("获取荐购统计信息成功: {}", statistics);
            return Response.Builder.success("查询成功", statistics);
            
        } catch (Exception e) {
            log.error("处理获取荐购统计信息请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 根据ISBN查询荐购申请
     */
    @RouteMapping(uri = "/library/admin/recommend/search/isbn", role = "admin", description = "根据ISBN查询荐购申请")
    public Response searchRecommendationsByIsbn(Request request) {
        log.info("处理根据ISBN查询荐购申请请求: {}", request);
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || !session.hasPermission("admin")) {
                return Response.Builder.forbidden("权限不足，无法搜索荐购申请");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String isbn = params.get("isbn");
            
            if (isbn == null || isbn.trim().isEmpty()) {
                return Response.Builder.badRequest("ISBN不能为空");
            }
            
            // 3. 调用服务层
            List<BookRecommendation> recommendations = recommendationService.getRecommendationsByIsbn(isbn);
            
            log.info("根据ISBN查询荐购申请成功: isbn={}, count={}", isbn, recommendations.size());
            return Response.Builder.success("查询成功", recommendations);
            
        } catch (Exception e) {
            log.error("处理根据ISBN查询荐购申请请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 根据书名搜索荐购申请
     */
    @RouteMapping(uri = "/library/admin/recommend/search/title", role = "admin", description = "根据书名搜索荐购申请")
    public Response searchRecommendationsByTitle(Request request) {
        log.info("处理根据书名搜索荐购申请请求: {}", request);
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || !session.hasPermission("admin")) {
                return Response.Builder.forbidden("权限不足，无法搜索荐购申请");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String title = params.get("title");
            
            if (title == null || title.trim().isEmpty()) {
                return Response.Builder.badRequest("书名关键词不能为空");
            }
            
            // 3. 调用服务层
            List<BookRecommendation> recommendations = recommendationService.searchRecommendationsByTitle(title);
            
            log.info("根据书名搜索荐购申请成功: title={}, count={}", title, recommendations.size());
            return Response.Builder.success("搜索成功", recommendations);
            
        } catch (Exception e) {
            log.error("处理根据书名搜索荐购申请请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 更新管理员反馈
     */
    @RouteMapping(uri = "/library/admin/recommend/feedback", role = "admin", description = "更新管理员反馈")
    public Response updateAdminFeedback(Request request) {
        log.info("处理更新管理员反馈请求: {}", request);
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || !session.hasPermission("admin")) {
                return Response.Builder.forbidden("权限不足，无法更新管理员反馈");
            }
            
            // 2. 参数解析
            Map<String, String> params = request.getParams();
            Integer recId = Integer.parseInt(params.get("recId"));
            String adminFeedback = params.get("adminFeedback");
            String adminCardNum = session.getUserId();
            
            if (recId == null) {
                return Response.Builder.badRequest("荐购ID不能为空");
            }
            
            // 3. 调用服务层
            RecommendResult result = recommendationService.updateAdminFeedback(recId, adminFeedback, adminCardNum);
            
            if (result.isSuccess()) {
                log.info("更新管理员反馈成功: recId={}, adminCardNum={}", recId, adminCardNum);
                return Response.Builder.success(result.getMessage());
            } else {
                log.warn("更新管理员反馈失败: recId={}, reason={}", recId, result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理更新管理员反馈请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
}
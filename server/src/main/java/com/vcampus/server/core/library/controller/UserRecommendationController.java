package com.vcampus.server.core.library.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.message.Session;
import com.vcampus.server.core.common.annotation.RouteMapping;
import com.vcampus.server.core.library.entity.core.BookRecommendation;
import com.vcampus.server.core.library.entity.result.RecommendResult;
import com.vcampus.server.core.library.entity.view.RecommendationHistory;
import com.vcampus.server.core.library.service.BookRecommendationService;
import com.vcampus.server.core.library.service.impl.BookRecommendationServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 用户荐购控制器
 * 提供学生和教师的荐购功能
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Slf4j
public class UserRecommendationController {
    
    private final BookRecommendationService recommendationService;
    
    public UserRecommendationController() {
        this.recommendationService = new BookRecommendationServiceImpl();
    }
    
    // ==================== 荐购申请功能 ====================
    
    /**
     * 提交图书荐购申请
     */
    @RouteMapping(uri = "/library/recommend/submit", role = "student,teacher", description = "提交图书荐购申请")
    public Response submitRecommendation(Request request) {
        log.info("处理荐购申请请求: {}", request);
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("权限不足，无法提交荐购申请");
            }
            
            String cardNum = session.getUserId();
            if (cardNum == null || cardNum.trim().isEmpty()) {
                return Response.Builder.badRequest("用户信息异常，请重新登录");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            String bookTitle = params.get("bookTitle");
            String bookAuthor = params.get("bookAuthor");
            String bookPublisher = params.get("bookPublisher");
            String bookIsbn = params.get("bookIsbn");
            String bookCategory = params.get("bookCategory");
            Integer recommendQty = Integer.valueOf(params.get("recommendQty"));
            String recommendReason = params.get("recommendReason");
            
            // 3. 调用服务层
            RecommendResult result = recommendationService.submitRecommendation(
                    cardNum, bookTitle, bookAuthor, bookPublisher, bookIsbn, 
                    bookCategory, recommendQty, recommendReason);
            
            if (result.isSuccess()) {
                log.info("荐购申请提交成功: cardNum={}, recId={}", cardNum, result.getRecId());
                return Response.Builder.success(result.getMessage(), result.getRecommendation());
            } else {
                log.warn("荐购申请提交失败: cardNum={}, reason={}", cardNum, result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理荐购申请请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 查看用户荐购历史
     */
    @RouteMapping(uri = "/library/recommend/history", role = "student,teacher", description = "查看用户荐购历史")
    public Response getUserRecommendationHistory(Request request) {
        log.info("处理查询荐购历史请求: {}", request);
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("权限不足，无法查看荐购历史");
            }
            
            String cardNum = session.getUserId();
            if (cardNum == null || cardNum.trim().isEmpty()) {
                return Response.Builder.badRequest("用户信息异常，请重新登录");
            }
            
            // 2. 调用服务层
            List<RecommendationHistory> history = recommendationService.getUserRecommendationHistory(cardNum);
            
            log.info("查询荐购历史成功: cardNum={}, count={}", cardNum, history.size());
            return Response.Builder.success("查询成功", history);
            
        } catch (Exception e) {
            log.error("处理查询荐购历史请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 查看荐购详情
     */
    @RouteMapping(uri = "/library/recommend/detail", role = "student,teacher", description = "查看荐购详情")
    public Response getRecommendationDetail(Request request) {
        log.info("处理查询荐购详情请求: {}", request);
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("权限不足，无法查看荐购详情");
            }
            
            String cardNum = session.getUserId();
            if (cardNum == null || cardNum.trim().isEmpty()) {
                return Response.Builder.badRequest("用户信息异常，请重新登录");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            Integer recId = Integer.valueOf(params.get("recId"));
            
            if (recId == null) {
                return Response.Builder.badRequest("荐购ID不能为空");
            }
            
            // 3. 调用服务层
            BookRecommendation recommendation = recommendationService.getRecommendationDetail(recId, cardNum);
            
            if (recommendation != null) {
                log.info("查询荐购详情成功: recId={}, cardNum={}", recId, cardNum);
                return Response.Builder.success("查询成功", recommendation);
            } else {
                log.warn("荐购记录不存在或无权限访问: recId={}, cardNum={}", recId, cardNum);
                return Response.Builder.notFound("荐购记录不存在或无权限访问");
            }
            
        } catch (Exception e) {
            log.error("处理查询荐购详情请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 取消荐购申请
     */
    @RouteMapping(uri = "/library/recommend/cancel", role = "student,teacher", description = "取消荐购申请")
    public Response cancelRecommendation(Request request) {
        log.info("处理取消荐购申请请求: {}", request);
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("权限不足，无法取消荐购申请");
            }
            
            String cardNum = session.getUserId();
            if (cardNum == null || cardNum.trim().isEmpty()) {
                return Response.Builder.badRequest("用户信息异常，请重新登录");
            }
            
            // 2. 参数提取
            Map<String, String> params = request.getParams();
            Integer recId = Integer.valueOf(params.get("recId"));
            
            if (recId == null) {
                return Response.Builder.badRequest("荐购ID不能为空");
            }
            
            // 3. 调用服务层
            RecommendResult result = recommendationService.cancelRecommendation(recId, cardNum);
            
            if (result.isSuccess()) {
                log.info("取消荐购申请成功: recId={}, cardNum={}", recId, cardNum);
                return Response.Builder.success(result.getMessage());
            } else {
                log.warn("取消荐购申请失败: recId={}, cardNum={}, reason={}", 
                        recId, cardNum, result.getMessage());
                return Response.Builder.badRequest(result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("处理取消荐购申请请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    // ==================== 统计查询功能 ====================
    
    /**
     * 统计用户荐购数量
     */
    @RouteMapping(uri = "/library/recommend/count", role = "student,teacher", description = "统计用户荐购数量")
    public Response countUserRecommendations(Request request) {
        log.info("处理统计用户荐购数量请求: {}", request);
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
                return Response.Builder.forbidden("权限不足，无法查看荐购统计");
            }
            
            String cardNum = session.getUserId();
            if (cardNum == null || cardNum.trim().isEmpty()) {
                return Response.Builder.badRequest("用户信息异常，请重新登录");
            }
            
            // 2. 调用服务层
            long count = recommendationService.countUserRecommendations(cardNum);
            
            log.info("统计用户荐购数量成功: cardNum={}, count={}", cardNum, count);
            return Response.Builder.success("统计成功", count);
            
        } catch (Exception e) {
            log.error("处理统计用户荐购数量请求异常", e);
            return Response.Builder.internalError("系统错误: " + e.getMessage());
        }
    }
    
    /**
     * 根据ISBN查询荐购申请
     */
    @RouteMapping(uri = "/library/recommend/search/isbn", role = "student,teacher", description = "根据ISBN查询荐购申请")
    public Response searchRecommendationsByIsbn(Request request) {
        log.info("处理根据ISBN查询荐购申请请求: {}", request);
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
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
    @RouteMapping(uri = "/library/recommend/search/title", role = "student,teacher", description = "根据书名搜索荐购申请")
    public Response searchRecommendationsByTitle(Request request) {
        log.info("处理根据书名搜索荐购申请请求: {}", request);
        
        try {
            // 1. 权限验证
            Session session = request.getSession();
            if (session == null || (!session.hasPermission("student") && !session.hasPermission("teacher"))) {
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
}
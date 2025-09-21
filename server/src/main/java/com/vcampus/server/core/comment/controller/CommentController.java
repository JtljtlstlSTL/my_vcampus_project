package com.vcampus.server.core.comment.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.message.Session;
import com.vcampus.server.core.comment.entity.Comment;
import com.vcampus.server.core.comment.service.CommentService;
import com.vcampus.server.core.common.annotation.RouteMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 校园集市评论控制器
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class CommentController {
    
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
    private final CommentService commentService = new CommentService();

    @RouteMapping(uri = "comment/handleRequest", role = "student", description = "处理校园集市评论相关请求")
    public Response handleRequest(Request request) {
        String action = request.getParam("action");
        Session session = request.getSession();
        
        if (session == null) {
            return Response.Builder.forbidden("需要登录");
        }
        
        String cardNum = session.getUserId();
        if (cardNum == null || cardNum.trim().isEmpty()) {
            return Response.Builder.badRequest("无法获取用户卡号");
        }
        
        logger.info("处理校园集市评论请求: action={}, cardNum={}", action, cardNum);
        
        try {
            switch (action) {
                case "ADD_COMMENT":
                    return addComment(request, cardNum);
                case "GET_ALL_COMMENTS":
                    return getAllComments();
                case "GET_MY_COMMENTS":
                    return getMyComments(cardNum);
                case "DELETE_COMMENT":
                    return deleteComment(request, cardNum);
                case "TOGGLE_LIKE":
                    return toggleLike(request, cardNum);
                case "CHECK_LIKE_STATUS":
                    return checkLikeStatus(request, cardNum);
                default:
                    return Response.Builder.badRequest("不支持的操作: " + action);
            }
        } catch (Exception e) {
            logger.error("处理校园集市评论请求时发生错误", e);
            return Response.Builder.error("服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 添加评论
     */
    private Response addComment(Request request, String cardNum) {
        String content = request.getParam("content");
        
        if (content == null || content.trim().isEmpty()) {
            return Response.Builder.badRequest("评论内容不能为空");
        }
        
        if (content.length() > 60) {
            return Response.Builder.badRequest("评论内容不能超过60字符");
        }
        
        boolean success = commentService.addComment(cardNum, content);
        
        if (success) {
            return Response.Builder.success("评论发表成功");
        } else {
            return Response.Builder.error("评论发表失败");
        }
    }

    /**
     * 获取所有评论
     */
    private Response getAllComments() {
        List<Map<String, Object>> comments = commentService.getAllComments().stream()
                .map(comment -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("commentId", comment.getCommentId());
                    map.put("cardNum", comment.getCardNum());
                    map.put("postTime", comment.getPostTime().toString());
                    map.put("content", comment.getContent());
                    map.put("likeCount", comment.getLikeCount());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
        
        return Response.Builder.success("获取评论列表成功", comments);
    }

    /**
     * 获取我的评论
     */
    private Response getMyComments(String cardNum) {
        List<Map<String, Object>> comments = commentService.getCommentsByCardNum(cardNum).stream()
                .map(comment -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("commentId", comment.getCommentId());
                    map.put("cardNum", comment.getCardNum());
                    map.put("postTime", comment.getPostTime().toString());
                    map.put("content", comment.getContent());
                    map.put("likeCount", comment.getLikeCount());
                    return map;
                })
                .collect(java.util.stream.Collectors.toList());
        
        return Response.Builder.success("获取我的评论成功", comments);
    }

    /**
     * 删除评论
     */
    private Response deleteComment(Request request, String cardNum) {
        String commentIdStr = request.getParam("commentId");
        
        if (commentIdStr == null || commentIdStr.trim().isEmpty()) {
            return Response.Builder.badRequest("评论ID不能为空");
        }
        
        try {
            Integer commentId = Integer.parseInt(commentIdStr);
            boolean success = commentService.deleteComment(commentId, cardNum);
            
            if (success) {
                return Response.Builder.success("评论删除成功");
            } else {
                return Response.Builder.error("评论删除失败，可能是权限不足或评论不存在");
            }
        } catch (NumberFormatException e) {
            return Response.Builder.badRequest("评论ID格式错误");
        }
    }
    
    /**
     * 切换点赞状态
     */
    private Response toggleLike(Request request, String cardNum) {
        String commentIdStr = request.getParam("commentId");
        
        if (commentIdStr == null || commentIdStr.trim().isEmpty()) {
            return Response.Builder.badRequest("评论ID不能为空");
        }
        
        try {
            Integer commentId = Integer.parseInt(commentIdStr);
            boolean newLikeStatus = commentService.toggleLike(commentId, cardNum);
            
            // 获取更新后的评论信息
            Comment comment = commentService.getCommentById(commentId);
            if (comment != null) {
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("commentId", commentId);
                result.put("likeCount", comment.getLikeCount());
                result.put("hasLiked", newLikeStatus);
                
                return Response.Builder.success("点赞状态切换成功", result);
            } else {
                return Response.Builder.error("评论不存在");
            }
        } catch (NumberFormatException e) {
            return Response.Builder.badRequest("评论ID格式错误");
        }
    }
    
    /**
     * 检查点赞状态
     */
    private Response checkLikeStatus(Request request, String cardNum) {
        String commentIdStr = request.getParam("commentId");
        
        if (commentIdStr == null || commentIdStr.trim().isEmpty()) {
            return Response.Builder.badRequest("评论ID不能为空");
        }
        
        try {
            Integer commentId = Integer.parseInt(commentIdStr);
            boolean hasLiked = commentService.hasLiked(commentId, cardNum);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("commentId", commentId);
            result.put("hasLiked", hasLiked);
            
            return Response.Builder.success("获取点赞状态成功", result);
        } catch (NumberFormatException e) {
            return Response.Builder.badRequest("评论ID格式错误");
        }
    }
}

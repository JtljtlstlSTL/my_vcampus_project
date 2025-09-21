package com.vcampus.server.core.comment.service;

import com.vcampus.server.core.comment.dao.CommentDao;
import com.vcampus.server.core.comment.dao.CommentDaoImpl;
import com.vcampus.server.core.comment.entity.Comment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 校园集市评论服务类
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class CommentService {
    
    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);
    private final CommentDao commentDao = new CommentDaoImpl();

    /**
     * 发表评论
     * @param cardNum 卡号
     * @param content 内容
     * @return 是否成功
     */
    public boolean addComment(String cardNum, String content) {
        // 验证输入
        if (cardNum == null || cardNum.trim().isEmpty()) {
            logger.warn("卡号不能为空");
            return false;
        }
        
        if (content == null || content.trim().isEmpty()) {
            logger.warn("评论内容不能为空");
            return false;
        }
        
        if (content.length() > 60) {
            logger.warn("评论内容超过60字符限制: {}", content.length());
            return false;
        }
        
        // 创建评论对象
        Comment comment = new Comment(cardNum.trim(), content.trim());
        comment.setPostTime(LocalDateTime.now());
        
        return commentDao.addComment(comment);
    }

    /**
     * 获取所有评论
     * @return 评论列表
     */
    public List<Comment> getAllComments() {
        return commentDao.getAllComments();
    }

    /**
     * 根据卡号获取评论
     * @param cardNum 卡号
     * @return 评论列表
     */
    public List<Comment> getCommentsByCardNum(String cardNum) {
        if (cardNum == null || cardNum.trim().isEmpty()) {
            logger.warn("卡号不能为空");
            return List.of();
        }
        
        return commentDao.getCommentsByCardNum(cardNum.trim());
    }

    /**
     * 删除评论
     * @param commentId 评论ID
     * @param cardNum 卡号（用于权限验证）
     * @return 是否成功
     */
    public boolean deleteComment(Integer commentId, String cardNum) {
        if (commentId == null) {
            logger.warn("评论ID不能为空");
            return false;
        }
        
        if (cardNum == null || cardNum.trim().isEmpty()) {
            logger.warn("卡号不能为空");
            return false;
        }
        
        return commentDao.deleteComment(commentId, cardNum.trim());
    }

    /**
     * 根据ID获取评论
     * @param commentId 评论ID
     * @return 评论对象
     */
    public Comment getCommentById(Integer commentId) {
        if (commentId == null) {
            logger.warn("评论ID不能为空");
            return null;
        }
        
        return commentDao.getCommentById(commentId);
    }
    
    /**
     * 点赞评论
     * @param commentId 评论ID
     * @param cardNum 用户卡号
     * @return 是否成功
     */
    public boolean likeComment(Integer commentId, String cardNum) {
        if (commentId == null) {
            logger.warn("评论ID不能为空");
            return false;
        }
        
        if (cardNum == null || cardNum.trim().isEmpty()) {
            logger.warn("用户卡号不能为空");
            return false;
        }
        
        // 检查是否已经点赞
        if (commentDao.hasLiked(commentId, cardNum)) {
            logger.warn("用户 {} 已经点赞过评论 {}", cardNum, commentId);
            return false;
        }
        
        return commentDao.likeComment(commentId, cardNum);
    }
    
    /**
     * 取消点赞
     * @param commentId 评论ID
     * @param cardNum 用户卡号
     * @return 是否成功
     */
    public boolean unlikeComment(Integer commentId, String cardNum) {
        if (commentId == null) {
            logger.warn("评论ID不能为空");
            return false;
        }
        
        if (cardNum == null || cardNum.trim().isEmpty()) {
            logger.warn("用户卡号不能为空");
            return false;
        }
        
        return commentDao.unlikeComment(commentId, cardNum);
    }
    
    /**
     * 切换点赞状态（点赞/取消点赞）
     * @param commentId 评论ID
     * @param cardNum 用户卡号
     * @return 新的点赞状态（true=已点赞，false=未点赞）
     */
    public boolean toggleLike(Integer commentId, String cardNum) {
        if (commentId == null) {
            logger.warn("评论ID不能为空");
            return false;
        }
        
        if (cardNum == null || cardNum.trim().isEmpty()) {
            logger.warn("用户卡号不能为空");
            return false;
        }
        
        if (commentDao.hasLiked(commentId, cardNum)) {
            // 已点赞，取消点赞
            boolean success = commentDao.unlikeComment(commentId, cardNum);
            return success ? false : true; // 成功取消点赞返回false，失败返回true（保持原状态）
        } else {
            // 未点赞，点赞
            boolean success = commentDao.likeComment(commentId, cardNum);
            return success ? true : false; // 成功点赞返回true，失败返回false（保持原状态）
        }
    }
    
    /**
     * 检查用户是否已点赞
     * @param commentId 评论ID
     * @param cardNum 用户卡号
     * @return 是否已点赞
     */
    public boolean hasLiked(Integer commentId, String cardNum) {
        if (commentId == null || cardNum == null || cardNum.trim().isEmpty()) {
            return false;
        }
        
        return commentDao.hasLiked(commentId, cardNum);
    }
}

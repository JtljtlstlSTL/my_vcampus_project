package com.vcampus.server.core.comment.dao;

import com.vcampus.server.core.comment.entity.Comment;
import java.util.List;

/**
 * 校园集市评论数据访问接口
 * 
 * @author VCampus Team
 * @version 1.0
 */
public interface CommentDao {
    
    /**
     * 添加评论
     * @param comment 评论对象
     * @return 是否成功
     */
    boolean addComment(Comment comment);
    
    /**
     * 获取所有评论（按时间倒序）
     * @return 评论列表
     */
    List<Comment> getAllComments();
    
    /**
     * 根据卡号获取评论
     * @param cardNum 卡号
     * @return 评论列表
     */
    List<Comment> getCommentsByCardNum(String cardNum);
    
    /**
     * 删除评论
     * @param commentId 评论ID
     * @param cardNum 卡号（用于权限验证）
     * @return 是否成功
     */
    boolean deleteComment(Integer commentId, String cardNum);
    
    /**
     * 根据评论ID获取评论
     * @param commentId 评论ID
     * @return 评论对象
     */
    Comment getCommentById(Integer commentId);
    
    /**
     * 点赞评论
     * @param commentId 评论ID
     * @param cardNum 用户卡号
     * @return 是否成功
     */
    boolean likeComment(Integer commentId, String cardNum);
    
    /**
     * 取消点赞
     * @param commentId 评论ID
     * @param cardNum 用户卡号
     * @return 是否成功
     */
    boolean unlikeComment(Integer commentId, String cardNum);
    
    /**
     * 检查用户是否已点赞
     * @param commentId 评论ID
     * @param cardNum 用户卡号
     * @return 是否已点赞
     */
    boolean hasLiked(Integer commentId, String cardNum);
    
    /**
     * 更新评论赞数
     * @param commentId 评论ID
     * @param likeCount 新的赞数
     * @return 是否成功
     */
    boolean updateLikeCount(Integer commentId, Integer likeCount);
}

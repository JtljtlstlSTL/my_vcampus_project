package com.vcampus.server.core.comment.dao;

import com.vcampus.server.core.comment.entity.Comment;
import com.vcampus.common.db.DbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 校园集市评论数据访问实现类
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class CommentDaoImpl implements CommentDao {
    
    private static final Logger logger = LoggerFactory.getLogger(CommentDaoImpl.class);

    @Override
    public boolean addComment(Comment comment) {
        String sql = "INSERT INTO tblComment (cardNum, content) VALUES (?, ?)";
        
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, comment.getCardNum());
            stmt.setString(2, comment.getContent());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        comment.setCommentId(generatedKeys.getInt(1));
                    }
                }
                logger.info("成功添加评论: {}", comment);
                return true;
            }
        } catch (SQLException e) {
            logger.error("添加评论时发生错误", e);
        }
        return false;
    }

    @Override
    public List<Comment> getAllComments() {
        String sql = "SELECT commentId, cardNum, postTime, content, likeCount FROM tblComment ORDER BY postTime DESC";
        List<Comment> comments = new ArrayList<>();
        
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setCommentId(rs.getInt("commentId"));
                comment.setCardNum(rs.getString("cardNum"));
                comment.setPostTime(rs.getTimestamp("postTime").toLocalDateTime());
                comment.setContent(rs.getString("content"));
                comment.setLikeCount(rs.getInt("likeCount"));
                comments.add(comment);
            }
            
            logger.info("成功获取所有评论，共 {} 条", comments.size());
        } catch (SQLException e) {
            logger.error("获取所有评论时发生错误", e);
        }
        
        return comments;
    }

    @Override
    public List<Comment> getCommentsByCardNum(String cardNum) {
        String sql = "SELECT commentId, cardNum, postTime, content, likeCount FROM tblComment WHERE cardNum = ? ORDER BY postTime DESC";
        List<Comment> comments = new ArrayList<>();
        
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cardNum);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Comment comment = new Comment();
                    comment.setCommentId(rs.getInt("commentId"));
                    comment.setCardNum(rs.getString("cardNum"));
                    comment.setPostTime(rs.getTimestamp("postTime").toLocalDateTime());
                    comment.setContent(rs.getString("content"));
                    comment.setLikeCount(rs.getInt("likeCount"));
                    comments.add(comment);
                }
            }
            
            logger.info("成功获取卡号 {} 的评论，共 {} 条", cardNum, comments.size());
        } catch (SQLException e) {
            logger.error("获取卡号 {} 的评论时发生错误", cardNum, e);
        }
        
        return comments;
    }

    @Override
    public boolean deleteComment(Integer commentId, String cardNum) {
        String sql = "DELETE FROM tblComment WHERE commentId = ? AND cardNum = ?";
        
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            stmt.setString(2, cardNum);
            
            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;
            
            if (success) {
                logger.info("成功删除评论 ID: {}, 卡号: {}", commentId, cardNum);
            } else {
                logger.warn("删除评论失败，可能是权限不足或评论不存在。ID: {}, 卡号: {}", commentId, cardNum);
            }
            
            return success;
        } catch (SQLException e) {
            logger.error("删除评论时发生错误", e);
        }
        return false;
    }

    @Override
    public Comment getCommentById(Integer commentId) {
        String sql = "SELECT commentId, cardNum, postTime, content, likeCount FROM tblComment WHERE commentId = ?";
        
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Comment comment = new Comment();
                    comment.setCommentId(rs.getInt("commentId"));
                    comment.setCardNum(rs.getString("cardNum"));
                    comment.setPostTime(rs.getTimestamp("postTime").toLocalDateTime());
                    comment.setContent(rs.getString("content"));
                    comment.setLikeCount(rs.getInt("likeCount"));
                    return comment;
                }
            }
        } catch (SQLException e) {
            logger.error("根据ID获取评论时发生错误", e);
        }
        
        return null;
    }

    @Override
    public boolean likeComment(Integer commentId, String cardNum) {
        String sql = "INSERT INTO tblCommentLike (commentId, cardNum) VALUES (?, ?)";
        
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            stmt.setString(2, cardNum);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                // 更新评论的赞数
                updateLikeCount(commentId, getCurrentLikeCount(commentId) + 1);
                logger.info("用户 {} 成功点赞评论 {}", cardNum, commentId);
                return true;
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) { // 重复键错误，表示已经点赞过
                logger.warn("用户 {} 已经点赞过评论 {}", cardNum, commentId);
                return false;
            }
            logger.error("点赞评论时发生错误", e);
        }
        return false;
    }

    @Override
    public boolean unlikeComment(Integer commentId, String cardNum) {
        String sql = "DELETE FROM tblCommentLike WHERE commentId = ? AND cardNum = ?";
        
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            stmt.setString(2, cardNum);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                // 更新评论的赞数
                updateLikeCount(commentId, getCurrentLikeCount(commentId) - 1);
                logger.info("用户 {} 成功取消点赞评论 {}", cardNum, commentId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("取消点赞时发生错误", e);
        }
        return false;
    }

    @Override
    public boolean hasLiked(Integer commentId, String cardNum) {
        String sql = "SELECT COUNT(*) FROM tblCommentLike WHERE commentId = ? AND cardNum = ?";
        
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            stmt.setString(2, cardNum);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("检查点赞状态时发生错误", e);
        }
        return false;
    }

    @Override
    public boolean updateLikeCount(Integer commentId, Integer likeCount) {
        String sql = "UPDATE tblComment SET likeCount = ? WHERE commentId = ?";
        
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, likeCount);
            stmt.setInt(2, commentId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("更新赞数时发生错误", e);
        }
        return false;
    }
    
    /**
     * 获取当前赞数
     */
    private int getCurrentLikeCount(Integer commentId) {
        String sql = "SELECT likeCount FROM tblComment WHERE commentId = ?";
        
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, commentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("likeCount");
                }
            }
        } catch (SQLException e) {
            logger.error("获取当前赞数时发生错误", e);
        }
        return 0;
    }
}

package com.vcampus.server.core.comment.entity;

import java.time.LocalDateTime;

/**
 * 评论点赞记录实体类
 * 对应tblCommentLike表
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class CommentLike {
    private Integer likeId;
    private Integer commentId;
    private String cardNum;
    private LocalDateTime likeTime;

    public CommentLike() {}

    public CommentLike(Integer commentId, String cardNum) {
        this.commentId = commentId;
        this.cardNum = cardNum;
        this.likeTime = LocalDateTime.now();
    }

    public CommentLike(Integer likeId, Integer commentId, String cardNum, LocalDateTime likeTime) {
        this.likeId = likeId;
        this.commentId = commentId;
        this.cardNum = cardNum;
        this.likeTime = likeTime;
    }

    public Integer getLikeId() {
        return likeId;
    }

    public void setLikeId(Integer likeId) {
        this.likeId = likeId;
    }

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public LocalDateTime getLikeTime() {
        return likeTime;
    }

    public void setLikeTime(LocalDateTime likeTime) {
        this.likeTime = likeTime;
    }

    @Override
    public String toString() {
        return "CommentLike{" +
                "likeId=" + likeId +
                ", commentId=" + commentId +
                ", cardNum='" + cardNum + '\'' +
                ", likeTime=" + likeTime +
                '}';
    }
}


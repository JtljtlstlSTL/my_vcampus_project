package com.vcampus.server.core.comment.entity;

import java.time.LocalDateTime;

/**
 * 校园集市评论实体类
 * 对应tblComment表
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class Comment {
    private Integer commentId;
    private String cardNum;
    private LocalDateTime postTime;
    private String content;
    private Integer likeCount;

    public Comment() {}

    public Comment(String cardNum, String content) {
        this.cardNum = cardNum;
        this.content = content;
        this.likeCount = 0;
    }

    public Comment(Integer commentId, String cardNum, LocalDateTime postTime, String content) {
        this.commentId = commentId;
        this.cardNum = cardNum;
        this.postTime = postTime;
        this.content = content;
        this.likeCount = 0;
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

    public LocalDateTime getPostTime() {
        return postTime;
    }

    public void setPostTime(LocalDateTime postTime) {
        this.postTime = postTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "commentId=" + commentId +
                ", cardNum='" + cardNum + '\'' +
                ", postTime=" + postTime +
                ", content='" + content + '\'' +
                '}';
    }
}

package com.vcampus.server.core.library.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 荐购状态枚举
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Getter
@AllArgsConstructor
public enum RecommendStatus {
    
    PENDING("PENDING", "待审核"),
    APPROVED("APPROVED", "已通过"),
    REJECTED("REJECTED", "已拒绝"),
    PURCHASED("PURCHASED", "已采购");
    
    private final String code;
    private final String description;
    
    /**
     * 根据代码获取枚举
     */
    public static RecommendStatus fromCode(String code) {
        for (RecommendStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的荐购状态代码: " + code);
    }
    
    /**
     * 根据描述获取枚举
     */
    public static RecommendStatus fromDescription(String description) {
        for (RecommendStatus status : values()) {
            if (status.description.equals(description)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的荐购状态描述: " + description);
    }
}

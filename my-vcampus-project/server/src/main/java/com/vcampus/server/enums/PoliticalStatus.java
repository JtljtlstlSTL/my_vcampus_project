package com.vcampus.server.enums;

/**
 * 政治面貌枚举
 * 
 * @author VCampus Team
 * @version 1.0
 */
public enum PoliticalStatus implements LabelledEnum {
    
    COMMUNIST_PARTY("中国共产党党员"),
    PROBATIONARY_PARTY_MEMBER("中国共产党预备党员"),
    YOUTH_LEAGUE_MEMBER("中国共产主义青年团团员"),
    MASSES("群众"),
    DEMOCRATIC_PARTY("民主党派");
    
    private final String label;
    
    PoliticalStatus(String label) {
        this.label = label;
    }
    
    @Override
    public String getLabel() {
        return label;
    }
    
    /**
     * 从标签获取枚举值
     * 
     * @param label 中文标签
     * @return 政治面貌枚举
     */
    public static PoliticalStatus fromLabel(String label) {
        for (PoliticalStatus status : values()) {
            if (status.getLabel().equals(label)) {
                return status;
            }
        }
        return MASSES; // 默认为群众
    }
}

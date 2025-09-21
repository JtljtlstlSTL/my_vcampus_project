package com.vcampus.server.core.library.entity.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Excel导入结果类
 * 
 * @author VCampus Team
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelImportResult {
    
    /**
     * 导入是否成功
     */
    private boolean success;
    
    /**
     * 结果消息
     */
    private String message;
    
    /**
     * 总记录数
     */
    private Integer totalCount;
    
    /**
     * 成功导入数量
     */
    private Integer successCount;
    
    /**
     * 失败数量
     */
    private Integer failCount;
    
    /**
     * 跳过数量（重复数据等）
     */
    private Integer skipCount;
    
    /**
     * 错误详情列表
     */
    private List<String> errors;
    
    /**
     * 警告信息列表
     */
    private List<String> warnings;
    
    /**
     * 导入的文件名
     */
    private String fileName;
    
    /**
     * 导入时间
     */
    private String importTime;
    
    /**
     * 创建成功结果
     * 
     * @param message 成功消息
     * @param totalCount 总记录数
     * @param successCount 成功数量
     * @param failCount 失败数量
     * @param skipCount 跳过数量
     * @return 成功结果
     */
    public static ExcelImportResult success(String message, Integer totalCount, 
                                         Integer successCount, Integer failCount, Integer skipCount) {
        return ExcelImportResult.builder()
                .success(true)
                .message(message)
                .totalCount(totalCount)
                .successCount(successCount)
                .failCount(failCount)
                .skipCount(skipCount)
                .build();
    }
    
    /**
     * 创建成功结果（带警告）
     * 
     * @param message 成功消息
     * @param totalCount 总记录数
     * @param successCount 成功数量
     * @param failCount 失败数量
     * @param skipCount 跳过数量
     * @param warnings 警告信息
     * @return 成功结果
     */
    public static ExcelImportResult success(String message, Integer totalCount, 
                                         Integer successCount, Integer failCount, Integer skipCount,
                                         List<String> warnings) {
        return ExcelImportResult.builder()
                .success(true)
                .message(message)
                .totalCount(totalCount)
                .successCount(successCount)
                .failCount(failCount)
                .skipCount(skipCount)
                .warnings(warnings)
                .build();
    }
    
    /**
     * 创建失败结果
     * 
     * @param message 失败消息
     * @return 失败结果
     */
    public static ExcelImportResult failure(String message) {
        return ExcelImportResult.builder()
                .success(false)
                .message(message)
                .build();
    }
    
    /**
     * 创建失败结果（带错误详情）
     * 
     * @param message 失败消息
     * @param errors 错误详情列表
     * @return 失败结果
     */
    public static ExcelImportResult failure(String message, List<String> errors) {
        return ExcelImportResult.builder()
                .success(false)
                .message(message)
                .errors(errors)
                .build();
    }
    
    /**
     * 创建部分成功结果
     * 
     * @param message 结果消息
     * @param totalCount 总记录数
     * @param successCount 成功数量
     * @param failCount 失败数量
     * @param skipCount 跳过数量
     * @param errors 错误详情列表
     * @param warnings 警告信息列表
     * @return 部分成功结果
     */
    public static ExcelImportResult partialSuccess(String message, Integer totalCount, 
                                                Integer successCount, Integer failCount, Integer skipCount,
                                                List<String> errors, List<String> warnings) {
        return ExcelImportResult.builder()
                .success(successCount > 0)
                .message(message)
                .totalCount(totalCount)
                .successCount(successCount)
                .failCount(failCount)
                .skipCount(skipCount)
                .errors(errors)
                .warnings(warnings)
                .build();
    }
    
    /**
     * 获取成功率
     * 
     * @return 成功率（百分比）
     */
    public double getSuccessRate() {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }
        return (double) (successCount != null ? successCount : 0) / totalCount * 100;
    }
    
    /**
     * 获取失败率
     * 
     * @return 失败率（百分比）
     */
    public double getFailRate() {
        if (totalCount == null || totalCount == 0) {
            return 0.0;
        }
        return (double) (failCount != null ? failCount : 0) / totalCount * 100;
    }
}

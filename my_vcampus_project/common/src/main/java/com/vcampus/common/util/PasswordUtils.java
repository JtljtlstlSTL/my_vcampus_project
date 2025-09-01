package com.vcampus.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码工具类 - 提供密码加密、验证、生成功能
 * 
 * 使用SHA-256 + 盐值的方式进行密码加密
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class PasswordUtils {
    
    private static final String ALGORITHM = "SHA-256";
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * 生成随机盐值
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * 使用盐值加密密码
     * 
     * @param password 原始密码
     * @param salt 盐值
     * @return 加密后的密码
     */
    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null) {
            return null;
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            String saltedPassword = password + salt;
            byte[] hashedBytes = md.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    /**
     * 加密密码 (自动生成盐值)
     * 
     * @param password 原始密码
     * @return 格式: salt:hashedPassword
     */
    public static String hashPassword(String password) {
        String salt = generateSalt();
        String hashedPassword = hashPassword(password, salt);
        return salt + ":" + hashedPassword;
    }
    
    /**
     * 验证密码
     * 
     * @param password 用户输入的密码
     * @param storedPassword 存储的密码 (格式: salt:hashedPassword)
     * @return 是否匹配
     */
    public static boolean verifyPassword(String password, String storedPassword) {
        if (password == null || storedPassword == null) {
            return false;
        }
        
        try {
            // 分离盐值和哈希密码
            String[] parts = storedPassword.split(":", 2);
            System.out.println("🔐 PasswordUtils: 存储密码分割结果，长度: " + parts.length);
            System.out.println("🔐 PasswordUtils: 存储密码: '" + storedPassword + "'");
            System.out.println("🔐 PasswordUtils: 输入密码: '" + password + "'");
            
            if (parts.length != 2) {
                // 兼容旧版本简单密码
                boolean result = password.equals(storedPassword);
                System.out.println("🔐 PasswordUtils: 使用直接比较，结果: " + result);
                return result;
            }
            
            String salt = parts[0];
            String hashedPassword = parts[1];
            
            // 验证密码
            String inputHashed = hashPassword(password, salt);
            boolean result = hashedPassword.equals(inputHashed);
            System.out.println("🔐 PasswordUtils: 使用哈希比较，结果: " + result);
            return result;
            
        } catch (Exception e) {
            System.out.println("🔐 PasswordUtils: 验证过程中发生异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 生成随机密码
     * 
     * @param length 密码长度
     * @return 随机密码
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return password.toString();
    }
    
    /**
     * 生成默认长度(8位)随机密码
     */
    public static String generateRandomPassword() {
        return generateRandomPassword(8);
    }
    
    /**
     * 检查密码强度
     * 
     * @param password 密码
     * @return 强度等级 1-5 (5最强)
     */
    public static int checkPasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return 1; // 太短
        }
        
        int score = 0;
        
        // 长度
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // 包含小写字母
        if (password.matches(".*[a-z].*")) score++;
        
        // 包含大写字母
        if (password.matches(".*[A-Z].*")) score++;
        
        // 包含数字
        if (password.matches(".*[0-9].*")) score++;
        
        // 包含特殊字符
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score++;
        
        return Math.min(5, Math.max(1, score));
    }
    
    /**
     * 检查密码是否符合基本要求
     * 
     * @param password 密码
     * @return 是否合规
     */
    public static boolean isValidPassword(String password) {
        return password != null && 
               password.length() >= 6 && 
               password.length() <= 20 &&
               checkPasswordStrength(password) >= 2;
    }
    
    /**
     * 检查密码是否为明文格式（未加密）
     * 明文密码不包含冒号(:)分隔符
     * 
     * @param password 存储的密码
     * @return 是否为明文密码
     */
    public static boolean isPlaintextPassword(String password) {
        // 明文密码不包含冒号分隔符
        return password != null && !password.contains(":");
    }
    
    /**
     * 迁移明文密码为加密格式
     * 
     * @param plaintextPassword 明文密码
     * @return 加密后的密码 (格式: salt:hashedPassword)
     */
    public static String migratePlaintextPassword(String plaintextPassword) {
        if (plaintextPassword == null) {
            return null;
        }
        
        // 直接使用hashPassword方法加密明文密码
        return hashPassword(plaintextPassword);
    }
}

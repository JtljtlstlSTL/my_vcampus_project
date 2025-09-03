package com.vcampus.common.util;

import com.vcampus.common.util.security.PasswordUtils;
import org.junit.Test;
import static org.junit.Assert.*;

public class PasswordUtilsTest {
    
    @Test
    public void testHashPassword() {
        String password = "test123";
        String hashedPassword = PasswordUtils.hashPassword(password);
        
        // 验证返回的密码不为空
        assertNotNull(hashedPassword);
        
        // 验证密码格式包含盐值和哈希值
        assertTrue(hashedPassword.contains(":"));
        
        // 验证密码长度符合预期
        String[] parts = hashedPassword.split(":");
        assertEquals(2, parts.length);
        assertEquals(32, parts[0].length()); // 盐值长度
        assertEquals(64, parts[1].length()); // SHA-256哈希值长度
    }
    
    @Test
    public void testVerifyPassword() {
        String password = "test123";
        String wrongPassword = "wrongpassword";
        String hashedPassword = PasswordUtils.hashPassword(password);
        
        // 验证正确密码能通过验证
        assertTrue(PasswordUtils.verifyPassword(password, hashedPassword));
        
        // 验证错误密码不能通过验证
        assertFalse(PasswordUtils.verifyPassword(wrongPassword, hashedPassword));
    }
    
    @Test
    public void testIsPlaintextPassword() {
        String plaintextPassword = "test123";
        String encryptedPassword = PasswordUtils.hashPassword(plaintextPassword);
        
        // 验证明文密码识别
        assertTrue(PasswordUtils.isPlaintextPassword(plaintextPassword));
        
        // 验证加密密码识别
        assertFalse(PasswordUtils.isPlaintextPassword(encryptedPassword));
    }
    
    @Test
    public void testMigratePlaintextPassword() {
        String plaintextPassword = "test123";
        String migratedPassword = PasswordUtils.migratePlaintextPassword(plaintextPassword);
        
        // 验证迁移后的密码不是明文
        assertFalse(PasswordUtils.isPlaintextPassword(migratedPassword));
        
        // 验证迁移后的密码能通过验证
        assertTrue(PasswordUtils.verifyPassword(plaintextPassword, migratedPassword));
        
        // 验证迁移后的密码格式正确
        assertTrue(migratedPassword.contains(":"));
        String[] parts = migratedPassword.split(":");
        assertEquals(2, parts.length);
        assertEquals(32, parts[0].length()); // 盐值长度
        assertEquals(64, parts[1].length()); // SHA-256哈希值长度
    }
    
    @Test
    public void testDifferentSalts() {
        String password = "test123";
        String hashed1 = PasswordUtils.hashPassword(password);
        String hashed2 = PasswordUtils.hashPassword(password);
        
        // 验证相同密码使用不同盐值生成不同哈希值
        assertNotEquals(hashed1, hashed2);
        
        // 但都能验证通过
        assertTrue(PasswordUtils.verifyPassword(password, hashed1));
        assertTrue(PasswordUtils.verifyPassword(password, hashed2));
    }
}
package com.vcampus.server.dao;

import com.vcampus.common.entity.User;
import com.vcampus.common.util.PasswordUtils;
import org.junit.Before;
import org.junit.Test;
import java.util.Optional;
import static org.junit.Assert.*;

public class UserDaoTest {
    
    private UserDao userDao;
    
    @Before
    public void setUp() {
        userDao = UserDao.getInstance();
    }
    
    @Test
    public void testFindByCardNumAndPasswordWithEncryptedPassword() {
        // 创建测试用户
        User user = new User();
        user.setCardNum("999999"); // 使用特殊卡号避免冲突
        String plainPassword = "test123";
        String encryptedPassword = PasswordUtils.hashPassword(plainPassword);
        user.setPassword(encryptedPassword);
        user.setName("Test User");
        user.setAge(25);
        user.setUserType("STUDENT");
        user.setPhone("13800138000");
        
        // 保存用户
        User savedUser = userDao.save(user);
        
        // 验证使用正确密码能查找到用户
        Optional<User> foundUser = userDao.findByCardNumAndPassword("999999", plainPassword);
        assertTrue(foundUser.isPresent());
        assertEquals("999999", foundUser.get().getCardNum());
        
        // 验证使用错误密码不能查找到用户
        Optional<User> notFoundUser = userDao.findByCardNumAndPassword("999999", "wrongpassword");
        assertFalse(notFoundUser.isPresent());
        
        // 清理测试数据
        userDao.deleteById("999999");
    }
    
    @Test
    public void testFindByNameAndPasswordWithEncryptedPassword() {
        // 创建测试用户
        User user = new User();
        user.setCardNum("999998"); // 使用特殊卡号避免冲突
        String plainPassword = "test123";
        String encryptedPassword = PasswordUtils.hashPassword(plainPassword);
        user.setPassword(encryptedPassword);
        user.setName("Test User For Name Search");
        user.setAge(25);
        user.setUserType("STUDENT");
        user.setPhone("13800138001");
        
        // 保存用户
        User savedUser = userDao.save(user);
        
        // 验证使用正确密码能查找到用户
        Optional<User> foundUser = userDao.findByNameAndPassword("Test User For Name Search", plainPassword);
        assertTrue(foundUser.isPresent());
        assertEquals("Test User For Name Search", foundUser.get().getName());
        
        // 验证使用错误密码不能查找到用户
        Optional<User> notFoundUser = userDao.findByNameAndPassword("Test User For Name Search", "wrongpassword");
        assertFalse(notFoundUser.isPresent());
        
        // 清理测试数据
        userDao.deleteById("999998");
    }
}
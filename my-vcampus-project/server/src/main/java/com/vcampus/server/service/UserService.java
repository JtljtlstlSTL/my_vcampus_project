package com.vcampus.server.service;

import com.vcampus.common.entity.User;
import com.vcampus.common.util.UserUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 用户服务类 - 重新设计的用户管理服务
 * 
 * 设计原则：
 * 1. 管理员账户预置在数据库中
 * 2. 其他用户通过管理界面添加
 * 3. 不在代码中硬编码用户数据
 * 
 * @author VCampus Team
 * @version 3.0
 */
@Slf4j
public class UserService {
    
    // =============== 数据存储（模拟数据库） ===============
    
    private static final Map<Integer, User> userDatabase = new ConcurrentHashMap<>();
    private static final Map<String, Integer> usernameToCardNum = new ConcurrentHashMap<>();
    
    // =============== 系统初始化 ===============
    
    static {
        initializeSystemAdmin();
    }
    
    /**
     * 初始化系统管理员账户
     * 实际项目中，这应该是数据库中的预置数据
     */
    private static void initializeSystemAdmin() {
        log.info("初始化系统管理员账户...");
        
        // 系统预置管理员账户（模拟数据库中的数据）
        User admin = new User(10001, "admin123", "系统管理员", null, 
                             "13800000001", "admin,staff");
        
        userDatabase.put(admin.getCardNum(), admin);
        usernameToCardNum.put("admin", admin.getCardNum());
        
        log.info("系统管理员账户初始化完成：{}", admin.getCardNum());
    }
    
    // =============== 用户认证 ===============
    
    /**
     * 用户登录
     */
    public static User login(String username, String password) {
        if (username == null || password == null) {
            return null;
        }
        
        User user = getUserByUsername(username);
        if (user == null) {
            log.warn("登录失败：用户不存在 - {}", username);
            return null;
        }
        
        // 检查密码（实际应用中应该验证哈希）
        if (!password.equals(user.getPassword())) {
            log.warn("登录失败：密码错误 - {}", username);
            return null;
        }
        
        log.info("用户登录成功 - {}: {}", user.getCardNum(), user.getName());
        return user;
    }
    
    /**
     * 检查用户名是否可用
     */
    public static boolean isUsernameAvailable(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return !usernameToCardNum.containsKey(username.trim());
    }
    
    /**
     * 根据用户名获取用户
     */
    private static User getUserByUsername(String username) {
        // 先尝试按用户名查找
        Integer cardNum = usernameToCardNum.get(username);
        if (cardNum != null) {
            return userDatabase.get(cardNum);
        }
        
        // 再尝试按卡号查找
        try {
            Integer cardNumInt = Integer.parseInt(username);
            return userDatabase.get(cardNumInt);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    // =============== 用户管理（管理员功能） ===============
    
    /**
     * 添加新用户（管理员操作）
     */
    public static boolean addUser(User newUser, String username) {
        if (newUser == null || newUser.getCardNum() == null) {
            return false;
        }
        
        // 检查卡号是否已存在
        if (userDatabase.containsKey(newUser.getCardNum())) {
            log.warn("添加用户失败：卡号已存在 - {}", newUser.getCardNum());
            return false;
        }
        
        // 检查用户名是否已存在
        if (username != null && !username.trim().isEmpty()) {
            if (usernameToCardNum.containsKey(username.trim())) {
                log.warn("添加用户失败：用户名已存在 - {}", username);
                return false;
            }
        }
        
        // 生成默认密码
        if (newUser.getPassword() == null || newUser.getPassword().isEmpty()) {
            newUser.setPassword("123456"); // 默认密码
        }
        
        // 保存用户
        userDatabase.put(newUser.getCardNum(), newUser);
        
        // 如果提供了用户名，建立映射
        if (username != null && !username.trim().isEmpty()) {
            usernameToCardNum.put(username.trim(), newUser.getCardNum());
        }
        
        log.info("新用户添加成功 - {}: {}", newUser.getCardNum(), newUser.getName());
        return true;
    }
    
    /**
     * 批量导入用户（Excel导入等）
     */
    public static int batchAddUsers(List<User> users) {
        int successCount = 0;
        
        for (User user : users) {
            if (addUser(user, null)) {
                successCount++;
            }
        }
        
        log.info("批量导入完成，成功添加 {} 个用户", successCount);
        return successCount;
    }
    
    /**
     * 生成新的卡号
     */
    public static Integer generateCardNum(String userType) {
        if (userType.contains("student")) {
            return generateCardNumByPrefix(20000); // 学生：20001, 20002...
        } else if (userType.contains("teacher")) {
            return generateCardNumByPrefix(30000); // 教师：30001, 30002...
        } else if (userType.contains("staff")) {
            return generateCardNumByPrefix(40000); // 员工：40001, 40002...
        } else {
            return generateCardNumByPrefix(10000); // 管理员：10001, 10002...
        }
    }
    
    /**
     * 根据前缀生成卡号
     */
    private static Integer generateCardNumByPrefix(int prefix) {
        int maxCardNum = prefix;
        
        for (Integer cardNum : userDatabase.keySet()) {
            if (cardNum >= prefix && cardNum < prefix + 10000) {
                maxCardNum = Math.max(maxCardNum, cardNum);
            }
        }
        
        return maxCardNum + 1;
    }
    
    // =============== 用户查询 ===============
    
    /**
     * 根据卡号获取用户
     */
    public static User getUserByCardNum(Integer cardNum) {
        return userDatabase.get(cardNum);
    }
    
    /**
     * 获取所有用户列表（分页）
     */
    public static List<User> getAllUsers(int page, int size) {
        List<User> allUsers = new ArrayList<>(userDatabase.values());
        
        // 按卡号排序
        allUsers.sort(Comparator.comparing(User::getCardNum));
        
        // 分页
        int start = page * size;
        int end = Math.min(start + size, allUsers.size());
        
        if (start >= allUsers.size()) {
            return new ArrayList<>();
        }
        
        return allUsers.subList(start, end)
                      .stream()
                      .map(UserUtils::sanitized)
                      .collect(Collectors.toList());
    }
    
    /**
     * 根据角色获取用户列表
     */
    public static List<User> getUsersByRole(String role) {
        return userDatabase.values()
                          .stream()
                          .filter(user -> UserUtils.hasRole(user, role))
                          .map(UserUtils::sanitized)
                          .sorted(Comparator.comparing(User::getCardNum))
                          .collect(Collectors.toList());
    }
    
    /**
     * 搜索用户（按姓名或卡号）
     */
    public static List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowercaseKeyword = keyword.toLowerCase();
        
        return userDatabase.values()
                          .stream()
                          .filter(user -> 
                              user.getName().toLowerCase().contains(lowercaseKeyword) ||
                              user.getCardNum().toString().contains(keyword)
                          )
                          .map(UserUtils::sanitized)
                          .sorted(Comparator.comparing(User::getCardNum))
                          .collect(Collectors.toList());
    }
    
    // =============== 用户管理操作 ===============
    
    /**
     * 修改密码
     */
    public static boolean changePassword(Integer cardNum, String oldPassword, String newPassword) {
        User user = userDatabase.get(cardNum);
        if (user == null) {
            return false;
        }
        
        // 验证旧密码
        if (!oldPassword.equals(user.getPassword())) {
            return false;
        }
        
        // 更新密码
        user.setPassword(newPassword);
        
        log.info("用户 {} 修改密码成功", cardNum);
        return true;
    }
    
    /**
     * 更新用户基础信息
     */
    public static boolean updateUserInfo(Integer cardNum, String name, String phone) {
        User user = userDatabase.get(cardNum);
        if (user == null) {
            return false;
        }
        
        if (name != null && !name.trim().isEmpty()) {
            user.setName(name.trim());
        }
        if (phone != null && !phone.trim().isEmpty()) {
            user.setPhone(phone.trim());
        }
        
        log.info("用户 {} 信息更新成功", cardNum);
        return true;
    }
    
    /**
     * 删除用户（管理员操作）
     */
    public static boolean deleteUser(Integer cardNum) {
        if (cardNum.equals(10001)) {
            log.warn("不能删除系统管理员账户");
            return false;
        }
        
        User removed = userDatabase.remove(cardNum);
        if (removed != null) {
            // 移除用户名映射
            usernameToCardNum.entrySet().removeIf(entry -> entry.getValue().equals(cardNum));
            log.info("用户 {} 删除成功", cardNum);
            return true;
        }
        
        return false;
    }
    
    // =============== 统计信息 ===============
    
    /**
     * 获取用户统计信息
     */
    public static Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalUsers = userDatabase.size();
        long studentCount = getUsersByRole("student").size();
        long teacherCount = getUsersByRole("teacher").size();
        long adminCount = getUsersByRole("admin").size();
        
        stats.put("totalUsers", totalUsers);
        stats.put("studentCount", studentCount);
        stats.put("teacherCount", teacherCount);
        stats.put("adminCount", adminCount);
        
        return stats;
    }

    /**
     * 获取脱敏后的用户信息（server模块专用）
     */
    public static User sanitized(User user) {
        if (user == null) return null;
        return UserUtils.sanitized(user);
    }
}
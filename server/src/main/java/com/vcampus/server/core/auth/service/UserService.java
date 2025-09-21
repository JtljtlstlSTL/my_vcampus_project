package com.vcampus.server.core.auth.service;

import com.vcampus.common.entity.base.User;
import com.vcampus.common.enums.Gender;
import com.vcampus.common.util.UserUtils;
import com.vcampus.common.util.security.PasswordUtils;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * 用户服务类 - 直接使用数据库连接，适配真实数据库表结构
 * @author VCampus Team
 * @version 3.0
 */
@Slf4j
public class UserService {

    private static final DataSource dataSource = com.vcampus.common.db.impl.MysqlDataSource.getInstance();

    // =============== 用户认证 ===============

    /**
     * 用户登录
     */
    public static User login(String username, String password) {
        if (username == null || password == null) {
            log.warn("Login failed: Username or password is null");
            return null;
        }

        try {
            // 先检查用户是否存在
            User user = null;
            try {
                // 尝试按卡号查找
                Integer cardNumInt = Integer.parseInt(username);
                user = findUserByCardNum(cardNumInt.toString());
            } catch (NumberFormatException e) {
                // 如果不是数字，尝试按姓名查找
                user = findUserByName(username);
            }

            if (user == null) {
                log.warn("Login failed: User not found - {}", username);
                throw new RuntimeException("用户不存在");
            }

            // 自动迁移明文密码
            String storedPassword = user.getPassword();
            if (PasswordUtils.isPlaintextPassword(storedPassword)) {
                log.info("Auto-migrating plaintext password for user: {}", user.getCardNum());
                String encryptedPassword = PasswordUtils.migratePlaintextPassword(storedPassword);
                updatePasswordInDatabase(user.getCardNum(), encryptedPassword);
                user.setPassword(encryptedPassword);
            }

            // 验证密码
            if (!PasswordUtils.verifyPassword(password, user.getPassword())) {
                log.warn("Login failed: Incorrect password for user - {}", username);
                throw new RuntimeException("密码错误");
            }

            log.info("User logged in successfully - {}: {}", user.getCardNum(), user.getName());
            return user;

        } catch (SQLException e) {
            log.error("Database error during authentication: {}", username, e);
            throw new RuntimeException("数据库错误，请稍后重试");
        }
    }



    private static User findUserByName(String name) throws SQLException {
        String sql = "SELECT cardNum, cardNumPassword, Name, BirthDate, Gender, userType, Phone, avatar_url FROM tblUser WHERE Name = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        }
    }

    /**
     * 更新数据库中的密码
     */
    private static void updatePasswordInDatabase(String cardNum, String encryptedPassword) {
        String sql = "UPDATE tblUser SET cardNumPassword = ? WHERE cardNum = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, encryptedPassword);
            stmt.setString(2, cardNum);
            stmt.executeUpdate();

        } catch (SQLException e) {
            log.error("Failed to update password in database for user: {}", cardNum, e);
        }
    }

    /**
     * 按卡号查找用户
     */
    private static User findUserByCardNum(String cardNum) {
        String sql = "SELECT cardNum, cardNumPassword, Name, BirthDate, Gender, userType, Phone, avatar_url FROM tblUser WHERE cardNum = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cardNum);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            } else {
                log.warn("User not found - {}", cardNum);
                return null;
            }

        } catch (SQLException e) {
            log.error("Database error while finding user: {}", cardNum, e);
            return null;
        }
    }

    /**
     * 按姓名验证用户
     */
    private static User authenticateByName(String name, String password) {
        String sql = "SELECT cardNum, cardNumPassword, Name, BirthDate, Gender, userType, Phone, avatar_url FROM tblUser WHERE Name = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = mapResultSetToUser(rs);
                if (PasswordUtils.verifyPassword(password, user.getPassword())) {
                    log.info("User logged in successfully - {}: {}", user.getCardNum(), user.getName());
                    return user;
                } else {
                    log.warn("Login failed: Incorrect password - {}", name);
                    return null;
                }
            } else {
                log.warn("Login failed: User not found - {}", name);
                return null;
            }

        } catch (SQLException e) {
            log.error("Database error during authentication: {}", name, e);
            return null;
        }
    }

    /**
     * 根据卡号获取用户
     */
    public static User getUserByCardNum(String cardNum) {
        String sql = "SELECT cardNum, cardNumPassword, Name, BirthDate, Gender, userType, Phone, avatar_url FROM tblUser WHERE cardNum = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cardNum);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            log.error("Failed to get user by card number: {}", cardNum, e);
        }

        return null;
    }

    /**
     * 获取所有用户
     */
    public static List<User> getAllUsers() {
        String sql = "SELECT cardNum, cardNumPassword, Name, BirthDate, Gender, userType, Phone, avatar_url FROM tblUser ORDER BY cardNum";
        List<User> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            log.error("Failed to get all users", e);
        }

        return users;
    }

    /**
     * 分页获取用户列表
     */
    public static List<User> getAllUsers(int page, int size) {
        String sql = "SELECT cardNum, cardNumPassword, Name, BirthDate, Gender, userType, Phone, avatar_url FROM tblUser ORDER BY cardNum LIMIT ? OFFSET ?";
        List<User> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, size);
            stmt.setInt(2, page * size);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            log.error("Failed to get users with pagination: page={}, size={}", page, size, e);
        }

        return users;
    }

    /**
     * 根据角色获取用户
     */
    public static List<User> getUsersByRole(String role) {
        String sql = "SELECT cardNum, cardNumPassword, Name, BirthDate, Gender, userType, Phone, avatar_url FROM tblUser WHERE userType = ? ORDER BY cardNum";
        List<User> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            log.error("Failed to get users by role: {}", role, e);
        }

        return users;
    }

    /**
     * 修改密码
     */
    public static boolean changePassword(String cardNum, String oldPassword, String newPassword) {
        try {
            log.info("开始修改密码，用户卡号: {}", cardNum);

            // 先验证旧密码
            User user = getUserByCardNum(cardNum);
            if (user == null) {
                log.warn("修改密码失败：用户不存在，卡号: {}", cardNum);
                return false;
            }

            log.info("找到用户: {}, 存储的密码: '{}'", user.getName(), user.getPassword());
            log.info("用户输入的旧密码: '{}'", oldPassword);

            // 使用PasswordUtils验证旧密码
            if (!PasswordUtils.verifyPassword(oldPassword, user.getPassword())) {
                log.warn("旧密码不正确，用户: {}", cardNum);
                return false;
            }

            log.info("旧密码验证通过，开始更新新密码");

            // 加密新密码
            String encryptedNewPassword = PasswordUtils.hashPassword(newPassword);
            String sql = "UPDATE tblUser SET cardNumPassword = ? WHERE cardNum = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, encryptedNewPassword);
                stmt.setString(2, cardNum);

                log.info("执行SQL: {} 参数: [加密密码, 卡号={}]", sql, cardNum);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    log.info("用户 {} 密码修改成功，影响行数: {}", cardNum, rows);
                    return true;
                } else {
                    log.warn("数据库更新失败，没有影响任何行，用户: {}", cardNum);
                    return false;
                }

            } catch (SQLException e) {
                log.error("数据库更新密码时发生SQL错误，用户: {}", cardNum, e);
                return false;
            }

        } catch (Exception e) {
            log.error("修改密码过程中发生异常，用户: {}", cardNum, e);
            return false;
        }
    }

    /**
     * 将ResultSet映射为User对象
     */
    private static User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        // 将数据库中的字段正确映射
        user.setCardNum(rs.getString("cardNum"));
        user.setPassword(rs.getString("cardNumPassword"));
        user.setName(rs.getString("Name"));

        // 从BirthDate字段获取出生年月，并计算年龄
        String birthDate = rs.getString("BirthDate");
        if (birthDate != null && !birthDate.isEmpty()) {
            try {
                // BirthDate格式为"YYYY-MM"，计算年龄
                int birthYear = Integer.parseInt(birthDate.substring(0, 4));
                int currentYear = java.time.Year.now().getValue();
                user.setAge(currentYear - birthYear);
            } catch (Exception e) {
                user.setAge(20); // 默认年龄
            }
        } else {
            user.setAge(20); // 默认年龄
        }

        String genderStr = rs.getString("Gender");
        if (genderStr != null) {
            try {
                // 转换中文性别为英文枚举
                if ("男".equals(genderStr)) {
                    user.setGender(Gender.MALE);
                } else if ("女".equals(genderStr)) {
                    user.setGender(Gender.FEMALE);
                } else {
                    user.setGender(Gender.valueOf(genderStr));
                }
            } catch (IllegalArgumentException e) {
                user.setGender(Gender.MALE);
            }
        }

        user.setPhone(rs.getString("Phone"));
        user.setUserType(rs.getString("userType"));
        user.setAvatar(rs.getString("avatar_url")); // 添加头像字段映射

        return user;
    }

    /**
     * 获取脱敏后的用户信息
     */
    public static User sanitized(User user) {
        if (user == null) return null;
        return UserUtils.sanitized(user);
    }

    // =============== 新增的静态方法 ===============

    /**
     * 用户认证方法 - 适配LoginController的调用
     */
    public static User authenticateUser(String cardNum, String password) {
        if (cardNum == null || password == null) {
            return null;
        }
        return findUserByCardNum(cardNum);
    }

    /**
     * 检查用户名是否可用
     */
    public static boolean isUsernameAvailable(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        try {
            Integer cardNumInt = Integer.parseInt(username);
            return !isCardNumExists(cardNumInt.toString());
        } catch (NumberFormatException e) {
            // 如果不是数字，检查姓名是否可用
            return !isNameExists(username);
        }
    }

    /**
     * 生成卡号
     */
    public static String generateCardNum(String userType) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT MAX(cardNum) FROM tblUser WHERE userType = ?")) {

            stmt.setString(1, userType);
            ResultSet rs = stmt.executeQuery();

            int maxCardNum = 10000; // 默认起始值
            if (rs.next() && rs.getObject(1) != null) {
                maxCardNum = Math.max(maxCardNum, rs.getInt(1));
            }

            return String.valueOf(maxCardNum + 1);

        } catch (SQLException e) {
            log.error("Failed to generate card number for user type: {}", userType, e);
            return "10001"; // 返回默认值
        }
    }

    /**
     * 添加用户
     */
    public static boolean addUser(User user, String username) {
        if (user == null || username == null) {
            return false;
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO tblUser (cardNum, cardNumPassword, Name, Age, Gender, userType, Phone) VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

            // 加密密码后存储
            String encryptedPassword = PasswordUtils.hashPassword(user.getPassword());

            stmt.setString(1, user.getCardNum());
            stmt.setString(2, encryptedPassword);
            stmt.setString(3, username);
            stmt.setInt(4, user.getAge());
            stmt.setString(5, user.getGender().toDatabase());
            stmt.setString(6, user.getUserType());
            stmt.setString(7, user.getPhone());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                log.info("User added successfully: {} ({})", username, user.getCardNum());

                // 新增逻辑：为该用户创建一卡通记录（若不存在）
                try {
                    String cardNum = user.getCardNum();
                    // 检查是否已存在一卡通记录
                    try (PreparedStatement checkPs = conn.prepareStatement("SELECT COUNT(*) FROM tblCard WHERE cardNum = ?")) {
                        checkPs.setString(1, cardNum);
                        try (ResultSet crs = checkPs.executeQuery()) {
                            boolean needCreate = true;
                            if (crs.next()) {
                                int cnt = crs.getInt(1);
                                needCreate = (cnt == 0);
                            }

                            if (needCreate) {
                                try (PreparedStatement insPs = conn.prepareStatement("INSERT INTO tblCard (cardNum, balance, status) VALUES (?, 0.00, '正常')")) {
                                    insPs.setString(1, cardNum);
                                    insPs.executeUpdate();
                                    log.info("Created campus card for user {} (cardNum={}) with balance=0", username, cardNum);
                                }
                            } else {
                                log.info("Campus card already exists for user {} (cardNum={})", username, cardNum);
                            }
                        }
                    }
                } catch (SQLException e) {
                    // 不应阻止用户创建成功，但记录警告以便排查
                    log.warn("Failed to create campus card for user {}: {}", username, e.getMessage());
                }

                return true;
            } else {
                log.warn("Failed to add user: {}", username);
                return false;
            }

        } catch (SQLException e) {
            log.error("Failed to add user: {}", username, e);
            return false;
        }
    }

    /**
     * 检查卡号是否已存在
     */
    private static boolean isCardNumExists(String cardNum) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM tblUser WHERE cardNum = ?")) {

            stmt.setString(1, cardNum);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            log.error("Failed to check card number existence: {}", cardNum, e);
        }

        return false;
    }

    /**
     * 检查姓名是否已存在
     */
    private static boolean isNameExists(String name) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM tblUser WHERE Name = ?")) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            log.error("Failed to check name existence: {}", name, e);
        }

        return false;
    }

    // =============== 新增的缺失方法 ===============

    /**
     * 搜索用户
     */
    public static List<User> searchUsers(String keyword) {
        String sql = "SELECT cardNum, cardNumPassword, Name, BirthDate, Gender, userType, Phone, avatar_url FROM tblUser WHERE Name LIKE ? OR cardNum LIKE ? ORDER BY cardNum";
        List<User> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            log.error("Failed to search users with keyword: {}", keyword, e);
        }

        return users;
    }

    /**
     * 删除用户
     */
    public static boolean deleteUser(String cardNum) {
        String sql = "DELETE FROM tblUser WHERE cardNum = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cardNum);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                log.info("User deleted successfully: {}", cardNum);
                return true;
            } else {
                log.warn("Failed to delete user: user not found - {}", cardNum);
                return false;
            }

        } catch (SQLException e) {
            log.error("Failed to delete user: {}", cardNum, e);
            return false;
        }
    }

    /**
     * 获取用户统计信息
     */
    public static Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            // 总用户数
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM tblUser")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.put("totalUsers", rs.getInt(1));
                }
            }

            // 按角色统计
            try (PreparedStatement stmt = conn.prepareStatement("SELECT userType, COUNT(*) FROM tblUser GROUP BY userType")) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    stats.put("roleStats", rs.getInt(2));
                }
            }

            // 按性别统计
            try (PreparedStatement stmt = conn.prepareStatement("SELECT Gender, COUNT(*) FROM tblUser GROUP BY Gender")) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    stats.put("genderStats", rs.getInt(2));
                }
            }

        } catch (SQLException e) {
            log.error("Failed to get user statistics", e);
        }

        return stats;
    }

    /**
     * 更新用户头像
     */
    public static boolean updateUserAvatar(String cardNum, String avatarUrl) {
        String sql = "UPDATE tblUser SET avatar_url = ? WHERE cardNum = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, avatarUrl);
            stmt.setString(2, cardNum);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                log.info("User avatar updated successfully - cardNum: {}, avatarUrl: {}", cardNum, avatarUrl);
                return true;
            } else {
                log.warn("No user found with cardNum: {}", cardNum);
                return false;
            }

        } catch (SQLException e) {
            log.error("Failed to update user avatar - cardNum: {}, avatarUrl: {}", cardNum, avatarUrl, e);
            return false;
        }
    }

    /**
     * 更新用户基本信息
     */
    public static boolean updateUserInfo(String cardNum, String name, String gender, String phone) {
        String sql = "UPDATE tblUser SET name = ?, gender = ?, phone = ? WHERE cardNum = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, gender);
            stmt.setString(3, phone);
            stmt.setString(4, cardNum);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            log.error("Failed to update user info - cardNum: {}, name: {}, gender: {}, phone: {}", 
                    cardNum, name, gender, phone, e);
            return false;
        }
    }
}

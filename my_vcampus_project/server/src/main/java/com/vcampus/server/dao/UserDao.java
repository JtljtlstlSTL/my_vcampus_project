package com.vcampus.server.dao;

import com.vcampus.common.dao.AbstractDao;
import com.vcampus.common.dao.EntityMapper;
import com.vcampus.common.dao.QueryCondition;
import com.vcampus.common.entity.User;
import com.vcampus.common.enums.Gender;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * User entity DAO implementation - 适配真实数据库表结构
 */
public class UserDao extends AbstractDao<User, String> {
    
    private static UserDao instance;
    
    private UserDao() {
        super("tblUser", "cardNum", new UserEntityMapper());
    }
    
    public static synchronized UserDao getInstance() {
        if (instance == null) {
            instance = new UserDao();
        }
        return instance;
    }
    
    @Override
    protected String getInsertSql() {
        return "INSERT INTO tblUser (cardNum, cardNumPassword, Name, Age, Gender, userType, Phone) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }
    
    @Override
    protected String getUpdateSql() {
        return "UPDATE tblUser SET cardNumPassword = ?, Name = ?, Age = ?, Gender = ?, userType = ?, Phone = ? WHERE cardNum = ?";
    }
    
    @Override
    protected void setInsertParams(PreparedStatement stmt, User user) throws SQLException {
        stmt.setString(1, user.getCardNum());
        stmt.setString(2, user.getPassword());
        stmt.setString(3, user.getName());
        stmt.setInt(4, user.getAge());
        stmt.setString(5, user.getGender() != null ? user.getGender().name() : "MALE");
        stmt.setString(6, user.getUserType());
        stmt.setString(7, user.getPhone());
    }
    
    @Override
    protected void setUpdateParams(PreparedStatement stmt, User user) throws SQLException {
        stmt.setString(1, user.getPassword());
        stmt.setString(2, user.getName());
        stmt.setInt(3, user.getAge());
        stmt.setString(4, user.getGender() != null ? user.getGender().name() : "MALE");
        stmt.setString(5, user.getUserType());
        stmt.setString(6, user.getPhone());
        stmt.setString(7, user.getCardNum());
    }
    
    @Override
    protected String getIdValue(User user) {
        return user.getCardNum();
    }
    
    @Override
    protected void setIdValue(User user, Object id) {
        user.setCardNum((String) id);
    }
    
    /**
     * Find user by phone number
     */
    public Optional<User> findByPhone(String phone) {
        QueryCondition condition = QueryCondition.create().eq("Phone", phone);
        List<User> users = findByCondition(condition);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }
    
    /**
     * Find users by user type
     */
    public List<User> findByUserType(String userType) {
        QueryCondition condition = QueryCondition.create().eq("userType", userType);
        return findByCondition(condition);
    }
    
    /**
     * Find users by name (fuzzy search)
     */
    public List<User> findByNameLike(String name) {
        QueryCondition condition = QueryCondition.create().like("Name", name);
        return findByCondition(condition);
    }
    
    /**
     * Check if username is available
     */
    public boolean isUsernameAvailable(String cardNum) {
        return !existsById(cardNum);
    }
    
    /**
     * Find user by card number and password (for authentication)
     */
    public Optional<User> findByCardNumAndPassword(String cardNum, String password) {
        // 先按卡号查找用户
        Optional<User> userOpt = findById(cardNum);
        if (!userOpt.isPresent()) {
            return Optional.empty();
        }
        
        // 验证密码
        User user = userOpt.get();
        if (com.vcampus.common.util.PasswordUtils.verifyPassword(password, user.getPassword())) {
            return Optional.of(user);
        }
        
        return Optional.empty();
    }
    
    /**
     * Find user by name and password (for authentication)
     */
    public Optional<User> findByNameAndPassword(String name, String password) {
        // 先按姓名查找用户
        QueryCondition condition = QueryCondition.create().eq("Name", name);
        List<User> users = findByCondition(condition);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        
        // 验证密码
        User user = users.get(0);
        if (com.vcampus.common.util.PasswordUtils.verifyPassword(password, user.getPassword())) {
            return Optional.of(user);
        }
        
        return Optional.empty();
    }
    
    /**
     * User entity mapper
     */
    private static class UserEntityMapper implements EntityMapper<User> {
        @Override
        public User map(ResultSet rs) throws SQLException {
            User user = new User();
            user.setCardNum(rs.getString("cardNum"));
            user.setPassword(rs.getString("cardNumPassword"));
            user.setName(rs.getString("Name"));
            user.setAge(rs.getInt("Age"));
            user.setGender(Gender.valueOf(rs.getString("Gender")));
            user.setUserType(rs.getString("userType"));
            user.setPhone(rs.getString("Phone"));
            return user;
        }
    }
}

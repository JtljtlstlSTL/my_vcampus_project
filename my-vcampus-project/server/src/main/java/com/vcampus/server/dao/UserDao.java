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
 * User实体的DAO实现
 */
public class UserDao extends AbstractDao<User, Integer> {
    
    private static UserDao instance;
    
    private UserDao() {
        super("users", "cardNum", new UserEntityMapper());
    }
    
    public static synchronized UserDao getInstance() {
        if (instance == null) {
            instance = new UserDao();
        }
        return instance;
    }
    
    @Override
    protected String getInsertSql() {
        return "INSERT INTO users (password, name, gender, phone, userType) VALUES (?, ?, ?, ?, ?)";
    }
    
    @Override
    protected String getUpdateSql() {
        return "UPDATE users SET password = ?, name = ?, gender = ?, phone = ?, userType = ? WHERE cardNum = ?";
    }
    
    @Override
    protected void setInsertParams(PreparedStatement stmt, User user) throws SQLException {
        stmt.setString(1, user.getPassword());
        stmt.setString(2, user.getName());
        stmt.setString(3, user.getGender().name());
        stmt.setString(4, user.getPhone());
        stmt.setString(5, user.getUserType());
    }
    
    @Override
    protected void setUpdateParams(PreparedStatement stmt, User user) throws SQLException {
        stmt.setString(1, user.getPassword());
        stmt.setString(2, user.getName());
        stmt.setString(3, user.getGender().name());
        stmt.setString(4, user.getPhone());
        stmt.setString(5, user.getUserType());
        stmt.setInt(6, user.getCardNum());
    }
    
    @Override
    protected Integer getIdValue(User user) {
        return user.getCardNum();
    }
    
    @Override
    protected void setIdValue(User user, Object id) {
        user.setCardNum((Integer) id);
    }
    
    /**
     * 根据手机号查找用户
     */
    public Optional<User> findByPhone(String phone) {
        QueryCondition condition = QueryCondition.create().eq("phone", phone);
        List<User> users = findByCondition(condition);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }
    
    /**
     * 根据用户类型查找用户
     */
    public List<User> findByUserType(String userType) {
        QueryCondition condition = QueryCondition.create().eq("userType", userType);
        return findByCondition(condition);
    }
    
    /**
     * 根据姓名模糊查询用户
     */
    public List<User> findByNameLike(String name) {
        QueryCondition condition = QueryCondition.create().like("name", name);
        return findByCondition(condition);
    }
    
    /**
     * 检查用户名是否可用
     */
    public boolean isUsernameAvailable(Integer cardNum) {
        return !existsById(cardNum);
    }
    
    /**
     * 用户登录验证
     */
    public Optional<User> authenticate(Integer cardNum, String password) {
        Optional<User> userOpt = findById(cardNum);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                return userOpt;
            }
        }
        return Optional.empty();
    }
    
    /**
     * 用户实体映射器
     */
    private static class UserEntityMapper implements EntityMapper<User> {
        @Override
        public User map(ResultSet rs) throws SQLException {
            User user = new User();
            user.setCardNum(rs.getInt("cardNum"));
            user.setPassword(rs.getString("password"));
            user.setName(rs.getString("name"));
            user.setGender(Gender.valueOf(rs.getString("gender")));
            user.setPhone(rs.getString("phone"));
            user.setUserType(rs.getString("userType"));
            return user;
        }
    }
}

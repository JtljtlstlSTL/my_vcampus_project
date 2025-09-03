package com.vcampus.server.core.auth.dao;

import com.vcampus.common.entity.base.User;
import com.vcampus.common.util.security.PasswordUtils;
import com.vcampus.server.core.auth.mapper.UserMapper;
import com.vcampus.server.core.db.DatabaseManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;
import java.util.Optional;

/**
 * User entity DAO implementation - MyBatis版本
 */
@Slf4j
public class UserDao {
    
    private final SqlSessionFactory sqlSessionFactory;
    private static UserDao instance;
    
    private UserDao() {
        this.sqlSessionFactory = DatabaseManager.getSqlSessionFactory();
    }
    
    public static synchronized UserDao getInstance() {
        if (instance == null) {
            instance = new UserDao();
        }
        return instance;
    }
    
    /**
     * 根据卡号查找用户
     */
    public Optional<User> findById(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            User user = mapper.findByCardNum(cardNum);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            log.error("根据卡号查找用户失败: {}", cardNum, e);
            throw new RuntimeException("查找用户失败", e);
        }
    }
    
    /**
     * 获取所有用户
     */
    public List<User> findAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            return mapper.getAllUsers();
        } catch (Exception e) {
            log.error("获取所有用户失败", e);
            throw new RuntimeException("获取用户列表失败", e);
        }
    }
    
    /**
     * 插入用户
     */
    public boolean insert(User user) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            int result = mapper.insertUser(
                user.getCardNum(),
                user.getPassword(),
                user.getName(),
                user.getAge(),
                user.getGender() != null ? user.getGender().name() : "MALE",
                user.getPhone(),
                user.getUserType()
            );
            session.commit();
            return result > 0;
        } catch (Exception e) {
            log.error("插入用户失败", e);
            throw new RuntimeException("插入用户失败", e);
        }
    }
    
    /**
     * 更新用户
     */
    public boolean update(User user) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            int result = mapper.updateUser(
                user.getCardNum(),
                user.getName(),
                user.getAge(),
                user.getGender() != null ? user.getGender().name() : "MALE",
                user.getPhone(),
                user.getUserType()
            );
            session.commit();
            return result > 0;
        } catch (Exception e) {
            log.error("更新用户失败", e);
            throw new RuntimeException("更新用户失败", e);
        }
    }
    
    /**
     * 删除用户
     */
    public boolean deleteById(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            int result = mapper.deleteByCardNum(cardNum);
            session.commit();
            return result > 0;
        } catch (Exception e) {
            log.error("删除用户失败: {}", cardNum, e);
            throw new RuntimeException("删除用户失败", e);
        }
    }
    
    /**
     * 检查用户是否存在
     */
    public boolean existsById(String cardNum) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            return mapper.isCardNumExists(cardNum) > 0;
        } catch (Exception e) {
            log.error("检查用户是否存在失败: {}", cardNum, e);
            throw new RuntimeException("检查用户失败", e);
        }
    }
    
    /**
     * 根据手机号查找用户
     */
    public Optional<User> findByPhone(String phone) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            List<User> users = mapper.findByNameLike(phone); // 这里需要根据实际需求调整
            return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
        } catch (Exception e) {
            log.error("根据手机号查找用户失败: {}", phone, e);
            throw new RuntimeException("查找用户失败", e);
        }
    }
    
    /**
     * 根据用户类型查找用户
     */
    public List<User> findByUserType(String userType) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            return mapper.findByUserType(userType);
        } catch (Exception e) {
            log.error("根据用户类型查找用户失败: {}", userType, e);
            throw new RuntimeException("查找用户失败", e);
        }
    }
    
    /**
     * 根据姓名模糊查找用户
     */
    public List<User> findByNameLike(String name) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            return mapper.findByNameLike(name);
        } catch (Exception e) {
            log.error("根据姓名模糊查找用户失败: {}", name, e);
            throw new RuntimeException("查找用户失败", e);
        }
    }
    
    /**
     * 检查用户名是否可用
     */
    public boolean isUsernameAvailable(String cardNum) {
        return !existsById(cardNum);
    }
    
    /**
     * 根据卡号和密码查找用户（用于认证）
     */
    public Optional<User> findByCardNumAndPassword(String cardNum, String password) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            User user = mapper.findByCardNum(cardNum);
            if (user == null) {
                return Optional.empty();
            }
            
            // 验证密码
            if (PasswordUtils.verifyPassword(password, user.getPassword())) {
                return Optional.of(user);
            }
            
            return Optional.empty();
        } catch (Exception e) {
            log.error("根据卡号和密码查找用户失败: {}", cardNum, e);
            throw new RuntimeException("用户认证失败", e);
        }
    }
    
    /**
     * 根据姓名和密码查找用户（用于认证）
     */
    public Optional<User> findByNameAndPassword(String name, String password) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            List<User> users = mapper.findByNameLike(name);
            if (users.isEmpty()) {
                return Optional.empty();
            }
            
            // 验证密码
            User user = users.get(0);
            if (PasswordUtils.verifyPassword(password, user.getPassword())) {
                return Optional.of(user);
            }
            
            return Optional.empty();
        } catch (Exception e) {
            log.error("根据姓名和密码查找用户失败: {}", name, e);
            throw new RuntimeException("用户认证失败", e);
        }
    }
    
    /**
     * 更新用户密码
     */
    public boolean updatePassword(String cardNum, String newPassword) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            int result = mapper.updatePassword(cardNum, newPassword);
            session.commit();
            return result > 0;
        } catch (Exception e) {
            log.error("更新用户密码失败: {}", cardNum, e);
            throw new RuntimeException("更新密码失败", e);
        }
    }
}
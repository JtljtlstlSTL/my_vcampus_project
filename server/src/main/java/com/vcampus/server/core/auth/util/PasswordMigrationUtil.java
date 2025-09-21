package com.vcampus.server.util;

import com.vcampus.common.util.security.PasswordUtils;
import com.vcampus.server.core.auth.dao.UserDao;
import com.vcampus.common.entity.base.User;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * 密码迁移工具类
 * 用于将数据库中存储的明文密码迁移到加密格式
 */
public class PasswordMigrationUtil {
    private static final Logger log = Logger.getLogger(PasswordMigrationUtil.class.getName());
    
    /**
     * 迁移所有用户的明文密码
     * 该方法会检查每个用户的密码是否为明文格式，如果是则将其加密
     */
    public static void migrateAllPlaintextPasswords() {
        log.info("开始执行密码迁移任务");
        
        try {
            // 获取所有用户
            List<User> users = UserDao.getInstance().findAll();
            int migratedCount = 0;
            
            for (User user : users) {
                String storedPassword = user.getPassword();
                
                // 检查密码是否为明文格式
                if (PasswordUtils.isPlaintextPassword(storedPassword)) {
                    log.info("发现明文密码用户: " + user.getCardNum() + ", 开始迁移");
                    
                    // 迁移密码
                    String encryptedPassword = PasswordUtils.migratePlaintextPassword(storedPassword);
                    
                    // 更新用户密码
                    user.setPassword(encryptedPassword);
                    UserDao.getInstance().update(user);
                    
                    migratedCount++;
                    log.info("用户 " + user.getCardNum() + " 密码迁移完成");
                }
            }
            
            log.info("密码迁移任务完成，共迁移 " + migratedCount + " 个用户");
        } catch (Exception e) {
            log.severe("密码迁移过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 迁移指定用户的明文密码
     * 
     * @param cardNum 用户卡号
     */
    public static boolean migrateUserPassword(String cardNum) {
        try {
            // 查找用户
            Optional<User> userOpt = UserDao.getInstance().findById(cardNum);
            
            if (!userOpt.isPresent()) {
                log.warning("未找到用户: " + cardNum);
                return false;
            }
            
            User user = userOpt.get();
            String storedPassword = user.getPassword();
            
            // 检查密码是否为明文格式
            if (PasswordUtils.isPlaintextPassword(storedPassword)) {
                log.info("开始迁移用户 " + cardNum + " 的密码");
                
                // 迁移密码
                String encryptedPassword = PasswordUtils.migratePlaintextPassword(storedPassword);
                
                // 更新用户密码
                user.setPassword(encryptedPassword);
                UserDao.getInstance().update(user);
                
                log.info("用户 " + cardNum + " 密码迁移完成");
                return true;
            } else {
                log.info("用户 " + cardNum + " 的密码已经是加密格式，无需迁移");
                return false;
            }
        } catch (Exception e) {
            log.severe("迁移用户 " + cardNum + " 密码时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 主方法，用于独立运行密码迁移任务
     */
    public static void main(String[] args) {
        log.info("密码迁移工具启动");
        migrateAllPlaintextPasswords();
        log.info("密码迁移工具执行完毕");
    }
}
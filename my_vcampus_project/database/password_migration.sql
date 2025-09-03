-- =============================================================
-- 密码迁移脚本：将现有明文密码加密存储
-- 注意：这个脚本需要在应用层执行，因为SQL无法直接调用Java加密方法
-- =============================================================

-- 1. 首先备份原有密码数据
CREATE TABLE IF NOT EXISTS tblUser_password_backup AS 
SELECT cardNum, cardNumPassword, CURRENT_TIMESTAMP as backup_time 
FROM tblUser 
WHERE cardNumPassword NOT LIKE '%:%';

-- 2. 查看需要迁移的用户数量
SELECT 
    COUNT(*) as total_users,
    COUNT(CASE WHEN cardNumPassword LIKE '%:%' THEN 1 END) as encrypted_users,
    COUNT(CASE WHEN cardNumPassword NOT LIKE '%:%' THEN 1 END) as plaintext_users
FROM tblUser;

-- 3. 显示需要迁移的用户（仅显示前10个）
SELECT cardNum, Name, '***' as masked_password, 'PLAINTEXT' as status
FROM tblUser 
WHERE cardNumPassword NOT LIKE '%:%'
LIMIT 10;

-- 注意：实际的密码加密迁移需要在Java应用层进行
-- 可以通过以下方式执行：
-- 1. 启动应用
-- 2. 用户首次登录时自动迁移
-- 3. 或者运行专门的迁移工具

-- 验证迁移状态的查询
SELECT 
    cardNum, 
    Name,
    CASE 
        WHEN cardNumPassword LIKE '%:%' THEN 'ENCRYPTED' 
        ELSE 'PLAINTEXT' 
    END as password_status
FROM tblUser
ORDER BY cardNum;

# 密码加密功能说明

## 概述
本项目实现了基于SHA-256和盐值的密码加密功能，以提高用户密码的安全性。所有密码在存储到数据库之前都会被加密，确保即使数据库被泄露，攻击者也无法直接获取用户密码。

## 加密方案
- 使用SHA-256哈希算法
- 为每个密码生成随机盐值
- 存储格式：`盐值:哈希值`
- 密码验证时会使用相同的盐值重新计算哈希值进行比较

## 实现细节

### 1. PasswordUtils类
位于`common/src/main/java/com/vcampus/common/util/PasswordUtils.java`，包含以下核心方法：

- `hashPassword(String password)`: 生成加密密码
- `verifyPassword(String password, String hashedPassword)`: 验证密码
- `isPlaintextPassword(String password)`: 检查密码是否为明文格式
- `migratePlaintextPassword(String plaintextPassword)`: 迁移明文密码

### 2. 数据库表结构
- 密码字段(`cardNumPassword`)长度已从`VARCHAR(10)`扩展到`VARCHAR(128)`，以容纳加密后的密码
- 加密密码格式：`盐值(32字符):哈希值(64字符)`，总长度约97字符

### 3. 自动迁移机制
在用户登录时，系统会自动检测密码是否为明文格式，如果是则会自动迁移为加密格式：

```java
// 自动迁移明文密码
String storedPassword = user.getPassword();
if (PasswordUtils.isPlaintextPassword(storedPassword)) {
    String encryptedPassword = PasswordUtils.migratePlaintextPassword(storedPassword);
    updatePasswordInDatabase(user.getCardNum(), encryptedPassword);
    user.setPassword(encryptedPassword);
}
```

### 4. 批量迁移工具
提供了`PasswordMigrationUtil`工具类，可以批量迁移所有用户的明文密码：

- 位于`server/src/main/java/com/vcampus/server/util/PasswordMigrationUtil.java`
- 包含`migrateAllPlaintextPasswords()`和`migrateUserPassword(int cardNum)`方法
- 可以通过运行`main`方法独立执行密码迁移任务

## 使用说明

### 新用户注册
新用户的密码会在注册时自动加密存储，无需额外处理。

### 现有用户登录
现有用户的明文密码会在首次登录时自动迁移为加密格式。

### 批量迁移
如果需要批量迁移所有用户的密码，可以运行PasswordMigrationUtil类的main方法：

```bash
java com.vcampus.server.util.PasswordMigrationUtil
```

### 验证迁移状态
可以使用以下SQL查询验证密码迁移状态：

```sql
SELECT 
    cardNum, 
    Name,
    CASE 
        WHEN cardNumPassword LIKE '%:%' THEN 'ENCRYPTED' 
        ELSE 'PLAINTEXT' 
    END as password_status
FROM tblUser
ORDER BY cardNum;
```

## 安全建议
1. 定期检查密码迁移状态，确保所有密码都已加密
2. 不要在日志中记录用户密码
3. 使用强密码策略，要求用户设置复杂密码
4. 定期更新加密算法以应对新的安全威胁
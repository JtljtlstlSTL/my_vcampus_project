-- =============================================================
-- 移除姓名字段唯一约束的迁移脚本
-- =============================================================

USE virtual_campus;

-- 移除Name字段的UNIQUE约束
ALTER TABLE tblUser DROP INDEX Name;

-- 验证约束是否已移除
SHOW INDEX FROM tblUser;

SELECT '姓名字段唯一约束已成功移除，现在可以添加重名用户了！' as message;

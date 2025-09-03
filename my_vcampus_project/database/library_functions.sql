-- =============================================================
-- 图书管理模块 - 完整业务逻辑
-- =============================================================
-- 说明：此文件包含图书管理模块的所有业务逻辑
-- 包括：视图、存储过程、触发器、索引
-- 使用前请确保已运行 init_new.sql 创建表结构
-- =============================================================

USE virtual_campus;
SET NAMES utf8mb4;

-- ==================== 视图定义 ====================

-- 图书借阅状态视图
CREATE OR REPLACE VIEW v_book_borrow_status AS
SELECT 
    b.book_Id,
    b.isbn,
    b.Title,
    b.Author,
    b.Category,
    bc.category_name as CategoryName,
    b.Location,
    b.Total_qty,
    b.Avail_qty,
    b.Status as book_status,
    CASE 
        WHEN b.Avail_qty > 0 THEN '可借阅'
        ELSE '已借完'
    END as borrow_status
FROM tblBook b
LEFT JOIN tblBookCategory bc ON b.Category = bc.category_code;

-- 用户借阅记录视图
CREATE OR REPLACE VIEW v_user_borrow_history AS
SELECT 
    bt.trans_Id,
    bt.cardNum,
    u.Name as user_name,
    u.userType,
    b.Title as book_title,
    b.Author as book_author,
    b.Category as book_category,
    bc.category_name as book_category_name,
    bt.Borrow_time,
    bt.Due_time,
    bt.Return_time,
    bt.Status as borrow_status,
    bt.Renew_count,
    CASE 
        WHEN bt.Status = 'BORROWED' AND bt.Due_time < NOW() THEN '已逾期'
        WHEN bt.Status = 'BORROWED' AND bt.Due_time >= NOW() THEN '借阅中'
        ELSE bt.Status
    END as display_status
FROM tblBook_trans bt
JOIN tblBook b ON bt.book_Id = b.book_Id
LEFT JOIN tblBookCategory bc ON b.Category = bc.category_code
JOIN tblUser u ON bt.cardNum = u.cardNum;

-- 图书借阅统计视图
CREATE OR REPLACE VIEW v_book_borrow_statistics AS
SELECT 
    b.book_Id,
    b.Title,
    b.Author,
    b.Category,
    bc.category_name,
    b.Total_qty,
    b.Avail_qty,
    COUNT(bt.trans_Id) as total_borrows,
    COUNT(CASE WHEN bt.Status = 'BORROWED' THEN 1 END) as current_borrows,
    COUNT(CASE WHEN bt.Status = 'OVERDUE' THEN 1 END) as overdue_count,
    ROUND(COUNT(bt.trans_Id) / b.Total_qty, 2) as borrow_rate
FROM tblBook b
LEFT JOIN tblBook_trans bt ON b.book_Id = bt.book_Id
LEFT JOIN tblBookCategory bc ON b.Category = bc.category_code
GROUP BY b.book_Id, b.Title, b.Author, b.Category, bc.category_name, b.Total_qty, b.Avail_qty;

-- 用户借阅统计视图
CREATE OR REPLACE VIEW v_user_borrow_statistics AS
SELECT 
    u.cardNum,
    u.Name,
    u.userType,
    COUNT(bt.trans_Id) as total_borrows,
    COUNT(CASE WHEN bt.Status = 'BORROWED' THEN 1 END) as current_borrows,
    COUNT(CASE WHEN bt.Status = 'OVERDUE' THEN 1 END) as overdue_count,
    COUNT(CASE WHEN bt.Status = 'RETURNED' THEN 1 END) as returned_count
FROM tblUser u
LEFT JOIN tblBook_trans bt ON u.cardNum = bt.cardNum
GROUP BY u.cardNum, u.Name, u.userType;

-- 分类统计视图
CREATE OR REPLACE VIEW v_category_statistics AS
SELECT 
    bc.category_code,
    bc.category_name,
    COUNT(b.book_Id) as total_books,
    SUM(b.Total_qty) as total_copies,
    SUM(b.Avail_qty) as available_copies,
    COUNT(bt.trans_Id) as total_borrows
FROM tblBookCategory bc
LEFT JOIN tblBook b ON bc.category_code = b.Category
LEFT JOIN tblBook_trans bt ON b.book_Id = bt.book_Id
GROUP BY bc.category_code, bc.category_name;

-- 逾期统计视图
CREATE OR REPLACE VIEW v_overdue_statistics AS
SELECT 
    u.userType,
    COUNT(*) as overdue_count,
    AVG(DATEDIFF(NOW(), bt.Due_time)) as avg_overdue_days,
    MAX(DATEDIFF(NOW(), bt.Due_time)) as max_overdue_days
FROM tblBook_trans bt
JOIN tblUser u ON bt.cardNum = u.cardNum
WHERE bt.Status = 'OVERDUE'
GROUP BY u.userType;

-- 逾期详情视图
CREATE OR REPLACE VIEW v_overdue_details AS
SELECT 
    bt.trans_Id,
    bt.cardNum,
    u.Name as user_name,
    u.userType,
    b.Title as book_title,
    b.Author as book_author,
    bt.Borrow_time,
    bt.Due_time,
    DATEDIFF(NOW(), bt.Due_time) as overdue_days,
    bt.Renew_count
FROM tblBook_trans bt
JOIN tblBook b ON bt.book_Id = b.book_Id
JOIN tblUser u ON bt.cardNum = u.cardNum
WHERE bt.Status = 'OVERDUE'
ORDER BY overdue_days DESC;

-- 热门图书视图
CREATE OR REPLACE VIEW v_popular_books AS
SELECT 
    b.book_Id,
    b.Title,
    b.Author,
    b.Category,
    bc.category_name,
    b.Total_qty,
    b.Avail_qty,
    COUNT(bt.trans_Id) as borrow_count,
    RANK() OVER (ORDER BY COUNT(bt.trans_Id) DESC) as popularity_rank
FROM tblBook b
LEFT JOIN tblBook_trans bt ON b.book_Id = bt.book_Id
LEFT JOIN tblBookCategory bc ON b.Category = bc.category_code
GROUP BY b.book_Id, b.Title, b.Author, b.Category, bc.category_name, b.Total_qty, b.Avail_qty
ORDER BY borrow_count DESC;

-- 最近借阅视图
CREATE OR REPLACE VIEW v_recent_borrows AS
SELECT 
    bt.trans_Id,
    bt.cardNum,
    u.Name as user_name,
    b.Title as book_title,
    b.Author as book_author,
    bt.Borrow_time,
    bt.Due_time,
    bt.Status,
    DATEDIFF(NOW(), bt.Borrow_time) as days_since_borrow
FROM tblBook_trans bt
JOIN tblBook b ON bt.book_Id = b.book_Id
JOIN tblUser u ON bt.cardNum = u.cardNum
WHERE bt.Borrow_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)
ORDER BY bt.Borrow_time DESC;

-- ==================== 存储过程定义 ====================

DELIMITER //

-- 借阅图书存储过程
CREATE OR REPLACE PROCEDURE sp_borrow_book(
    IN p_book_id INT,
    IN p_card_num VARCHAR(10),
    IN p_borrow_days INT
)
BEGIN
    DECLARE v_avail_qty INT DEFAULT 0;
    DECLARE v_max_borrow_count INT DEFAULT 5;
    DECLARE v_current_borrow_count INT DEFAULT 0;
    DECLARE v_user_type VARCHAR(20);
    DECLARE v_max_borrow_days INT DEFAULT 30;
    
    -- 检查图书是否可借
    SELECT Avail_qty INTO v_avail_qty FROM tblBook WHERE book_Id = p_book_id;
    IF v_avail_qty <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '图书库存不足';
    END IF;
    
    -- 获取用户类型
    SELECT userType INTO v_user_type FROM tblUser WHERE cardNum = p_card_num;
    IF v_user_type IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '用户不存在';
    END IF;
    
    -- 获取用户借阅规则
    SELECT max_borrow_count, max_borrow_days 
    INTO v_max_borrow_count, v_max_borrow_days 
    FROM tblBorrowRule WHERE user_type = v_user_type;
    
    -- 检查借阅天数是否超限
    IF p_borrow_days > v_max_borrow_days THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = CONCAT('借阅天数不能超过', v_max_borrow_days, '天');
    END IF;
    
    -- 检查用户当前借阅数量
    SELECT COUNT(*) INTO v_current_borrow_count 
    FROM tblBook_trans 
    WHERE cardNum = p_card_num AND Status = 'BORROWED';
    
    IF v_current_borrow_count >= v_max_borrow_count THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '借阅数量已达上限';
    END IF;
    
    -- 开始事务
    START TRANSACTION;
    
    -- 插入借阅记录
    INSERT INTO tblBook_trans (book_Id, cardNum, Due_time, Status) 
    VALUES (p_book_id, p_card_num, DATE_ADD(NOW(), INTERVAL p_borrow_days DAY), 'BORROWED');
    
    -- 更新图书库存
    UPDATE tblBook SET Avail_qty = Avail_qty - 1 WHERE book_Id = p_book_id;
    
    COMMIT;
    
    SELECT '借阅成功' as result, LAST_INSERT_ID() as trans_id;
END //

-- 归还图书存储过程
CREATE OR REPLACE PROCEDURE sp_return_book(IN p_trans_id INT)
BEGIN
    DECLARE v_book_id INT;
    DECLARE v_status VARCHAR(20);
    DECLARE v_due_time DATETIME;
    DECLARE v_overdue_days INT DEFAULT 0;
    
    -- 获取借阅记录信息
    SELECT book_Id, Status, Due_time INTO v_book_id, v_status, v_due_time
    FROM tblBook_trans WHERE trans_Id = p_trans_id;
    
    IF v_book_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '借阅记录不存在';
    END IF;
    
    IF v_status = 'RETURNED' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '图书已归还，无需重复操作';
    END IF;
    
    -- 计算逾期天数
    IF v_due_time < NOW() THEN
        SET v_overdue_days = DATEDIFF(NOW(), v_due_time);
    END IF;
    
    -- 开始事务
    START TRANSACTION;
    
    -- 更新借阅记录
    UPDATE tblBook_trans 
    SET Return_time = NOW(), 
        Status = CASE WHEN v_overdue_days > 0 THEN 'OVERDUE' ELSE 'RETURNED' END
    WHERE trans_Id = p_trans_id;
    
    -- 更新图书库存
    UPDATE tblBook SET Avail_qty = Avail_qty + 1 WHERE book_Id = v_book_id;
    
    COMMIT;
    
    SELECT '归还成功' as result, v_overdue_days as overdue_days;
END //

-- 续借图书存储过程
CREATE OR REPLACE PROCEDURE sp_renew_book(
    IN p_trans_id INT,
    IN p_extend_days INT
)
BEGIN
    DECLARE v_status VARCHAR(20);
    DECLARE v_renew_count INT DEFAULT 0;
    DECLARE v_max_renew_count INT DEFAULT 2;
    DECLARE v_due_time DATETIME;
    DECLARE v_card_num VARCHAR(10);
    DECLARE v_user_type VARCHAR(20);
    
    -- 获取借阅记录信息
    SELECT Status, Renew_count, Due_time, cardNum 
    INTO v_status, v_renew_count, v_due_time, v_card_num
    FROM tblBook_trans WHERE trans_Id = p_trans_id;
    
    IF v_status IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '借阅记录不存在';
    END IF;
    
    IF v_status != 'BORROWED' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '只能续借已借出的图书';
    END IF;
    
    IF v_due_time < NOW() THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '逾期图书不能续借，请先归还';
    END IF;
    
    -- 获取用户类型和续借规则
    SELECT userType INTO v_user_type FROM tblUser WHERE cardNum = v_card_num;
    SELECT max_renew_count INTO v_max_renew_count FROM tblBorrowRule WHERE user_type = v_user_type;
    
    IF v_renew_count >= v_max_renew_count THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '续借次数已达上限';
    END IF;
    
    -- 更新借阅记录
    UPDATE tblBook_trans 
    SET Due_time = DATE_ADD(Due_time, INTERVAL p_extend_days DAY), 
        Renew_count = Renew_count + 1
    WHERE trans_Id = p_trans_id;
    
    SELECT '续借成功' as result, DATE_ADD(v_due_time, INTERVAL p_extend_days DAY) as new_due_time;
END //

-- 添加图书存储过程
CREATE OR REPLACE PROCEDURE sp_add_book(
    IN p_isbn VARCHAR(20),
    IN p_title VARCHAR(100),
    IN p_author VARCHAR(50),
    IN p_publisher VARCHAR(100),
    IN p_publish_date DATE,
    IN p_category VARCHAR(2),
    IN p_location VARCHAR(50),
    IN p_total_qty INT
)
BEGIN
    DECLARE v_book_id INT;
    
    -- 检查ISBN是否已存在
    IF EXISTS(SELECT 1 FROM tblBook WHERE isbn = p_isbn) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'ISBN已存在';
    END IF;
    
    -- 检查分类是否存在
    IF NOT EXISTS(SELECT 1 FROM tblBookCategory WHERE category_code = p_category) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '图书分类不存在';
    END IF;
    
    -- 插入图书
    INSERT INTO tblBook (isbn, Title, Author, Publisher, Publish_date, Category, Location, Total_qty, Avail_qty, Status)
    VALUES (p_isbn, p_title, p_author, p_publisher, p_publish_date, p_category, p_location, p_total_qty, p_total_qty, 'IN_LIBRARY');
    
    SET v_book_id = LAST_INSERT_ID();
    
    SELECT '图书添加成功' as result, v_book_id as book_id;
END //

-- 更新图书信息存储过程
CREATE OR REPLACE PROCEDURE sp_update_book(
    IN p_book_id INT,
    IN p_title VARCHAR(100),
    IN p_author VARCHAR(50),
    IN p_publisher VARCHAR(100),
    IN p_publish_date DATE,
    IN p_category VARCHAR(2),
    IN p_location VARCHAR(50),
    IN p_total_qty INT
)
BEGIN
    DECLARE v_current_avail_qty INT;
    DECLARE v_current_total_qty INT;
    DECLARE v_new_avail_qty INT;
    
    -- 获取当前库存信息
    SELECT Total_qty, Avail_qty INTO v_current_total_qty, v_current_avail_qty
    FROM tblBook WHERE book_Id = p_book_id;
    
    IF v_current_total_qty IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '图书不存在';
    END IF;
    
    -- 计算新的可借数量
    SET v_new_avail_qty = v_current_avail_qty + (p_total_qty - v_current_total_qty);
    
    -- 更新图书信息
    UPDATE tblBook 
    SET Title = p_title,
        Author = p_author,
        Publisher = p_publisher,
        Publish_date = p_publish_date,
        Category = p_category,
        Location = p_location,
        Total_qty = p_total_qty,
        Avail_qty = v_new_avail_qty
    WHERE book_Id = p_book_id;
    
    SELECT '图书信息更新成功' as result;
END //

-- 逾期检查存储过程
CREATE OR REPLACE PROCEDURE sp_check_overdue()
BEGIN
    DECLARE v_updated_count INT DEFAULT 0;
    
    -- 更新逾期状态
    UPDATE tblBook_trans 
    SET Status = 'OVERDUE' 
    WHERE Status = 'BORROWED' AND Due_time < NOW();
    
    SET v_updated_count = ROW_COUNT();
    
    SELECT '逾期检查完成' as result, v_updated_count as updated_count;
END //

-- 图书借阅统计存储过程
CREATE OR REPLACE PROCEDURE sp_get_book_statistics(IN p_book_id INT)
BEGIN
    SELECT 
        b.book_Id,
        b.Title,
        b.Author,
        b.Category,
        bc.category_name,
        b.Total_qty,
        b.Avail_qty,
        COUNT(bt.trans_Id) as total_borrows,
        COUNT(CASE WHEN bt.Status = 'BORROWED' THEN 1 END) as current_borrows,
        COUNT(CASE WHEN bt.Status = 'OVERDUE' THEN 1 END) as overdue_count,
        COUNT(CASE WHEN bt.Status = 'RETURNED' THEN 1 END) as returned_count
    FROM tblBook b
    LEFT JOIN tblBook_trans bt ON b.book_Id = bt.book_Id
    LEFT JOIN tblBookCategory bc ON b.Category = bc.category_code
    WHERE b.book_Id = p_book_id
    GROUP BY b.book_Id, b.Title, b.Author, b.Category, bc.category_name, b.Total_qty, b.Avail_qty;
END //

-- 用户借阅统计存储过程
CREATE OR REPLACE PROCEDURE sp_get_user_statistics(IN p_card_num VARCHAR(10))
BEGIN
    SELECT 
        u.cardNum,
        u.Name,
        u.userType,
        COUNT(bt.trans_Id) as total_borrows,
        COUNT(CASE WHEN bt.Status = 'BORROWED' THEN 1 END) as current_borrows,
        COUNT(CASE WHEN bt.Status = 'OVERDUE' THEN 1 END) as overdue_count,
        COUNT(CASE WHEN bt.Status = 'RETURNED' THEN 1 END) as returned_count,
        AVG(DATEDIFF(bt.Return_time, bt.Borrow_time)) as avg_borrow_days
    FROM tblUser u
    LEFT JOIN tblBook_trans bt ON u.cardNum = bt.cardNum
    WHERE u.cardNum = p_card_num
    GROUP BY u.cardNum, u.Name, u.userType;
END //

-- 图书搜索存储过程
CREATE OR REPLACE PROCEDURE sp_search_books(
    IN p_keyword VARCHAR(100),
    IN p_category VARCHAR(2),
    IN p_author VARCHAR(50),
    IN p_publisher VARCHAR(100),
    IN p_location VARCHAR(50),
    IN p_available_only BOOLEAN,
    IN p_limit INT
)
BEGIN
    DECLARE v_sql TEXT DEFAULT '';
    DECLARE v_where_conditions TEXT DEFAULT '';
    
    -- 构建WHERE条件
    IF p_keyword IS NOT NULL AND p_keyword != '' THEN
        SET v_where_conditions = CONCAT(v_where_conditions, 
            ' AND (b.Title LIKE CONCAT(''%'', ?, ''%'') OR b.Author LIKE CONCAT(''%'', ?, ''%'') OR b.Publisher LIKE CONCAT(''%'', ?, ''%''))');
    END IF;
    
    IF p_category IS NOT NULL AND p_category != '' THEN
        SET v_where_conditions = CONCAT(v_where_conditions, ' AND b.Category = ?');
    END IF;
    
    IF p_author IS NOT NULL AND p_author != '' THEN
        SET v_where_conditions = CONCAT(v_where_conditions, ' AND b.Author LIKE CONCAT(''%'', ?, ''%'')');
    END IF;
    
    IF p_publisher IS NOT NULL AND p_publisher != '' THEN
        SET v_where_conditions = CONCAT(v_where_conditions, ' AND b.Publisher LIKE CONCAT(''%'', ?, ''%'')');
    END IF;
    
    IF p_location IS NOT NULL AND p_location != '' THEN
        SET v_where_conditions = CONCAT(v_where_conditions, ' AND b.Location LIKE CONCAT(''%'', ?, ''%'')');
    END IF;
    
    IF p_available_only = TRUE THEN
        SET v_where_conditions = CONCAT(v_where_conditions, ' AND b.Avail_qty > 0');
    END IF;
    
    -- 构建完整SQL
    SET v_sql = CONCAT('
        SELECT 
            b.book_Id,
            b.isbn,
            b.Title,
            b.Author,
            b.Publisher,
            b.Publish_date,
            b.Category,
            bc.category_name,
            b.Location,
            b.Total_qty,
            b.Avail_qty,
            b.Status,
            CASE WHEN b.Avail_qty > 0 THEN ''可借阅'' ELSE ''已借完'' END as borrow_status,
            COUNT(bt.trans_Id) as borrow_count
        FROM tblBook b
        LEFT JOIN tblBookCategory bc ON b.Category = bc.category_code
        LEFT JOIN tblBook_trans bt ON b.book_Id = bt.book_Id
        WHERE 1=1', v_where_conditions, '
        GROUP BY b.book_Id, b.isbn, b.Title, b.Author, b.Publisher, b.Publish_date, b.Category, bc.category_name, b.Location, b.Total_qty, b.Avail_qty, b.Status
        ORDER BY b.Title
        LIMIT ?');
    
    -- 执行动态SQL
    SET @sql = v_sql;
    PREPARE stmt FROM @sql;
    
    -- 设置参数
    IF p_keyword IS NOT NULL AND p_keyword != '' THEN
        SET @keyword = p_keyword;
        EXECUTE stmt USING @keyword, @keyword, @keyword;
    ELSE
        EXECUTE stmt USING p_category, p_author, p_publisher, p_location, p_limit;
    END IF;
    
    DEALLOCATE PREPARE stmt;
END //

-- 热门图书搜索存储过程
CREATE OR REPLACE PROCEDURE sp_search_popular_books(
    IN p_category VARCHAR(2),
    IN p_limit INT
)
BEGIN
    SELECT 
        b.book_Id,
        b.Title,
        b.Author,
        b.Category,
        bc.category_name,
        b.Total_qty,
        b.Avail_qty,
        COUNT(bt.trans_Id) as borrow_count,
        RANK() OVER (ORDER BY COUNT(bt.trans_Id) DESC) as popularity_rank
    FROM tblBook b
    LEFT JOIN tblBook_trans bt ON b.book_Id = bt.book_Id
    LEFT JOIN tblBookCategory bc ON b.Category = bc.category_code
    WHERE (p_category IS NULL OR b.Category = p_category)
    GROUP BY b.book_Id, b.Title, b.Author, b.Category, bc.category_name, b.Total_qty, b.Avail_qty
    ORDER BY borrow_count DESC
    LIMIT p_limit;
END //

-- 分类图书搜索存储过程
CREATE OR REPLACE PROCEDURE sp_search_books_by_category(
    IN p_category VARCHAR(2),
    IN p_available_only BOOLEAN,
    IN p_limit INT
)
BEGIN
    SELECT 
        b.book_Id,
        b.Title,
        b.Author,
        b.Publisher,
        b.Category,
        bc.category_name,
        b.Location,
        b.Total_qty,
        b.Avail_qty,
        CASE WHEN b.Avail_qty > 0 THEN '可借阅' ELSE '已借完' END as borrow_status
    FROM tblBook b
    LEFT JOIN tblBookCategory bc ON b.Category = bc.category_code
    WHERE b.Category = p_category
      AND (p_available_only = FALSE OR b.Avail_qty > 0)
    ORDER BY b.Title
    LIMIT p_limit;
END //

DELIMITER ;

-- ==================== 触发器定义 ====================

DELIMITER //

-- 借阅记录插入后，自动更新图书状态
CREATE OR REPLACE TRIGGER tr_book_trans_after_insert
AFTER INSERT ON tblBook_trans
FOR EACH ROW
BEGIN
    IF NEW.Status = 'BORROWED' THEN
        UPDATE tblBook SET Status = 'BORROWED' WHERE book_Id = NEW.book_Id;
    END IF;
END //

-- 借阅记录更新后，自动更新图书状态
CREATE OR REPLACE TRIGGER tr_book_trans_after_update
AFTER UPDATE ON tblBook_trans
FOR EACH ROW
BEGIN
    IF NEW.Status = 'RETURNED' AND OLD.Status != 'RETURNED' THEN
        UPDATE tblBook SET Status = 'IN_LIBRARY' WHERE book_Id = NEW.book_Id;
    END IF;
    
    IF NEW.Status = 'OVERDUE' AND OLD.Status = 'BORROWED' THEN
        UPDATE tblBook SET Status = 'BORROWED' WHERE book_Id = NEW.book_Id;
    END IF;
END //

-- 图书插入前，检查数据完整性
CREATE OR REPLACE TRIGGER tr_book_before_insert
BEFORE INSERT ON tblBook
FOR EACH ROW
BEGIN
    IF NEW.Avail_qty > NEW.Total_qty THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '可借数量不能超过总数量';
    END IF;
    
    IF NEW.Total_qty <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '总数量必须大于0';
    END IF;
    
    IF NEW.Avail_qty < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '可借数量不能为负数';
    END IF;
    
    IF NEW.Status IS NULL THEN
        SET NEW.Status = 'IN_LIBRARY';
    END IF;
END //

-- 图书更新前，检查数据完整性
CREATE OR REPLACE TRIGGER tr_book_before_update
BEFORE UPDATE ON tblBook
FOR EACH ROW
BEGIN
    IF NEW.Avail_qty > NEW.Total_qty THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '可借数量不能超过总数量';
    END IF;
    
    IF NEW.Total_qty <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '总数量必须大于0';
    END IF;
    
    IF NEW.Avail_qty < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '可借数量不能为负数';
    END IF;
END //

DELIMITER ;

-- ==================== 索引定义 ====================

-- 图书表索引
CREATE INDEX IF NOT EXISTS idx_book_search ON tblBook(Title, Author, Category);
CREATE INDEX IF NOT EXISTS idx_book_status_location ON tblBook(Status, Location);
CREATE INDEX IF NOT EXISTS idx_book_category ON tblBook(Category);
CREATE INDEX IF NOT EXISTS idx_book_isbn ON tblBook(isbn);
CREATE INDEX IF NOT EXISTS idx_book_publisher ON tblBook(Publisher);
CREATE INDEX IF NOT EXISTS idx_book_publish_date ON tblBook(Publish_date);
CREATE INDEX IF NOT EXISTS idx_book_avail_qty ON tblBook(Avail_qty);

-- 借阅记录表索引
CREATE INDEX IF NOT EXISTS idx_borrow_user_status ON tblBook_trans(cardNum, Status);
CREATE INDEX IF NOT EXISTS idx_borrow_book_status ON tblBook_trans(book_Id, Status);
CREATE INDEX IF NOT EXISTS idx_borrow_due_time ON tblBook_trans(Due_time, Status);
CREATE INDEX IF NOT EXISTS idx_borrow_borrow_time ON tblBook_trans(Borrow_time);
CREATE INDEX IF NOT EXISTS idx_borrow_return_time ON tblBook_trans(Return_time);
CREATE INDEX IF NOT EXISTS idx_borrow_renew_count ON tblBook_trans(Renew_count);
CREATE INDEX IF NOT EXISTS idx_borrow_user_time ON tblBook_trans(cardNum, Borrow_time);
CREATE INDEX IF NOT EXISTS idx_borrow_book_time ON tblBook_trans(book_Id, Borrow_time);
CREATE INDEX IF NOT EXISTS idx_borrow_due_status ON tblBook_trans(Due_time, Status);

-- 图书分类表索引
CREATE INDEX IF NOT EXISTS idx_category_code ON tblBookCategory(category_code);
CREATE INDEX IF NOT EXISTS idx_category_name ON tblBookCategory(category_name);
CREATE INDEX IF NOT EXISTS idx_category_sort ON tblBookCategory(sort_order);

-- 借阅规则表索引
CREATE INDEX IF NOT EXISTS idx_borrow_rule_user_type ON tblBorrowRule(user_type);
CREATE INDEX IF NOT EXISTS idx_borrow_rule_active ON tblBorrowRule(is_active);
CREATE INDEX IF NOT EXISTS idx_borrow_rule_type_active ON tblBorrowRule(user_type, is_active);

-- 用户表索引（图书管理相关）
CREATE INDEX IF NOT EXISTS idx_user_type ON tblUser(userType);
CREATE INDEX IF NOT EXISTS idx_user_name ON tblUser(Name);
CREATE INDEX IF NOT EXISTS idx_user_type_name ON tblUser(userType, Name);

-- 学生表索引（图书管理相关）
CREATE INDEX IF NOT EXISTS idx_student_id ON tblStudent(student_Id);
CREATE INDEX IF NOT EXISTS idx_student_major ON tblStudent(Major);
CREATE INDEX IF NOT EXISTS idx_student_department ON tblStudent(Department);
CREATE INDEX IF NOT EXISTS idx_student_grade ON tblStudent(Grade);
CREATE INDEX IF NOT EXISTS idx_student_major_grade ON tblStudent(Major, Grade);
CREATE INDEX IF NOT EXISTS idx_student_department_major ON tblStudent(Department, Major);

-- 教职工表索引（图书管理相关）
CREATE INDEX IF NOT EXISTS idx_staff_id ON tblStaff(staff_Id);
CREATE INDEX IF NOT EXISTS idx_staff_title ON tblStaff(Title);
CREATE INDEX IF NOT EXISTS idx_staff_department ON tblStaff(Department);
CREATE INDEX IF NOT EXISTS idx_staff_department_title ON tblStaff(Department, Title);

-- ==================== 完成提示 ====================

SELECT '图书管理模块业务逻辑创建完成！' as message;
SELECT '已创建：' as summary;
SELECT '- 9个视图：数据展示和统计' as view_summary;
SELECT '- 11个存储过程：业务逻辑处理（包含搜索功能）' as proc_summary;
SELECT '- 4个触发器：数据完整性保证' as trigger_summary;
SELECT '- 20+个索引：查询性能优化' as index_summary;
SELECT '所有功能已就绪，包括完整的图书搜索功能！' as completion;

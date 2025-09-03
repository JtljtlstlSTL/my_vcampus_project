-- =============================================================
-- 虚拟校园系统完整建库脚本（cardNum 统一 VARCHAR(10)）
-- =============================================================
DROP DATABASE IF EXISTS virtual_campus;
CREATE DATABASE virtual_campus
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
USE virtual_campus;
SET NAMES utf8mb4;
-- 1️ 统一用户表
CREATE TABLE tblUser (
    cardNum   VARCHAR(10) PRIMARY KEY COMMENT '登录 ID',
    cardNumPassword  VARCHAR(128) NOT NULL COMMENT '登录密码',
    Name  VARCHAR(10) COMMENT '真实姓名',
    Age       SMALLINT NOT NULL CHECK (Age BETWEEN 1 AND 150) COMMENT '年龄',
    Gender    ENUM('男','女') NOT NULL DEFAULT '男' COMMENT '性别',
    userType      ENUM('student','staff','manager') NOT NULL COMMENT '身份标志',
    Phone     VARCHAR(11)  NOT NULL COMMENT '电话号码'
) COMMENT='用户总表';

-- 2️ 学生扩展表
CREATE TABLE tblStudent (
    cardNum    VARCHAR(10) PRIMARY KEY COMMENT '登录 ID，外键',
    student_Id CHAR(8) UNIQUE NOT NULL COMMENT '学号',
    Grade      TINYINT NOT NULL CHECK (Grade BETWEEN 1 AND 4) COMMENT '年级',
    Major      VARCHAR(20) COMMENT '专业',
    Department VARCHAR(20) COMMENT '学院',
    CONSTRAINT fk_stu_user
        FOREIGN KEY (cardNum) REFERENCES tblUser(cardNum)
        ON UPDATE CASCADE ON DELETE CASCADE
) COMMENT='学生扩展信息';

-- 3️ 教职工扩展表
CREATE TABLE tblStaff (
    cardNum    VARCHAR(10) PRIMARY KEY COMMENT '登录 ID，外键',
    staff_Id   CHAR(8) UNIQUE NOT NULL COMMENT '工号',
    Title      VARCHAR(10) COMMENT '职称/职位',
    Department VARCHAR(20) COMMENT '学院',
    CONSTRAINT fk_staff_user
        FOREIGN KEY (cardNum) REFERENCES tblUser(cardNum)
        ON UPDATE CASCADE ON DELETE CASCADE
) COMMENT='教职工扩展信息';

-- 4️ 一卡通表
CREATE TABLE tblCard (
    card_Id  INT AUTO_INCREMENT PRIMARY KEY COMMENT '卡号',
    cardNum  VARCHAR(10) NOT NULL COMMENT '所属用户',
    balance  DECIMAL(10,2) DEFAULT 0.00 COMMENT '余额',
    status   ENUM('正常','挂失','注销') DEFAULT '正常' COMMENT '状态',
    CONSTRAINT fk_card_user
        FOREIGN KEY (cardNum) REFERENCES tblUser(cardNum)
        ON UPDATE CASCADE ON DELETE CASCADE
) COMMENT='一卡通表';

-- 5️ 一卡通交易记录表
CREATE TABLE tblCard_trans (
    cardNum    VARCHAR(10) NOT NULL COMMENT '一卡通号',
    Trans_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
    Trans_type ENUM('RECHARGE','CONSUME','REFUND') NOT NULL COMMENT '交易类型',
    Amount     DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '交易金额',
    PRIMARY KEY (cardNum, Trans_time),
    CONSTRAINT fk_ct_user
        FOREIGN KEY (cardNum) REFERENCES tblUser(cardNum)
        ON UPDATE CASCADE ON DELETE CASCADE
) COMMENT='一卡通交易记录';

-- 6️ 课程表
CREATE TABLE tblCourse (
    course_Id  INT AUTO_INCREMENT PRIMARY KEY COMMENT '课程号',
    courseName VARCHAR(20) UNIQUE NOT NULL COMMENT '课程名',
    Credit     TINYINT NOT NULL CHECK (Credit BETWEEN 1 AND 5) COMMENT '学分',
    Department VARCHAR(20) COMMENT '开课学院'
) COMMENT='课程表';

-- 7️ 教学班表
CREATE TABLE tblSection (
    section_Id INT AUTO_INCREMENT PRIMARY KEY COMMENT '教学班号',
    course_Id  INT NOT NULL COMMENT '课程号',
    Term       VARCHAR(20) COMMENT '学期',
    Teacher_id VARCHAR(20) COMMENT '任课老师工号',
    Room       VARCHAR(20) COMMENT '教室',
    Capacity   SMALLINT NOT NULL COMMENT '课程容量',
    Schedule   VARCHAR(50) COMMENT '上课时间',
    CONSTRAINT fk_section_course
        FOREIGN KEY (course_Id) REFERENCES tblCourse(course_Id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_section_teacher
        FOREIGN KEY (Teacher_id) REFERENCES tblStaff(staff_Id)
        ON UPDATE CASCADE ON DELETE SET NULL
) COMMENT='教学班表';

-- 8️ 选课/成绩表
CREATE TABLE tblEnrollment (
    student_Id  CHAR(8),
    section_Id  INT,
    Select_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    Score       SMALLINT DEFAULT 0 CHECK (Score BETWEEN 0 AND 100) COMMENT '成绩',
    GPA         DECIMAL(5,4) DEFAULT 0.0000 CHECK (GPA BETWEEN 0 AND 4.8000) COMMENT '绩点',
    PRIMARY KEY (student_Id, section_Id),
    CONSTRAINT fk_enr_student
        FOREIGN KEY (student_Id) REFERENCES tblStudent(student_Id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_enr_section
        FOREIGN KEY (section_Id) REFERENCES tblSection(section_Id)
        ON UPDATE CASCADE ON DELETE CASCADE
) COMMENT='选课及成绩表';

-- 9 教学评价表
CREATE TABLE tblEdu_evaluate (
    eval_Id    INT AUTO_INCREMENT PRIMARY KEY COMMENT '评教记录ID',
    section_Id INT NOT NULL COMMENT '被评价教学班',
    student_Id CHAR(8) NOT NULL COMMENT '评价学生学号',
    Score      DECIMAL(5,2) DEFAULT 0.00 COMMENT '评教分数',
    Eval_time  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评教时间',
    CONSTRAINT fk_eval_section
        FOREIGN KEY (section_Id) REFERENCES tblSection(section_Id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_eval_student
        FOREIGN KEY (student_Id) REFERENCES tblStudent(student_Id)
        ON UPDATE CASCADE ON DELETE CASCADE
) COMMENT='教学评价表';

-- 10 图书表
CREATE TABLE tblBook (
    book_Id      INT AUTO_INCREMENT PRIMARY KEY COMMENT '图书ID',
    isbn         VARCHAR(20) UNIQUE NOT NULL COMMENT 'ISBN号（唯一约束）',
    Title        VARCHAR(100) NOT NULL COMMENT '书名',
    Author       VARCHAR(50) NOT NULL COMMENT '作者',
    Publisher    VARCHAR(100) COMMENT '出版社',
    Publish_date DATE COMMENT '出版日期',
    Category     VARCHAR(2) COMMENT '图书分类（中图法代码A-Z）',
    Location     VARCHAR(50) COMMENT '馆藏位置',
    Total_qty    INT DEFAULT 1 COMMENT '馆藏总数量',
    Avail_qty    INT DEFAULT 1 COMMENT '当前可借数量',
    Status       ENUM('IN_LIBRARY', 'BORROWED') DEFAULT 'IN_LIBRARY' COMMENT '图书状态：在馆/借出',
    CONSTRAINT fk_book_category 
        FOREIGN KEY (Category) REFERENCES tblBookCategory(category_code)
        ON UPDATE CASCADE ON DELETE SET NULL
) COMMENT='图书表';

-- 11️ 图书借还记录表
CREATE TABLE tblBook_trans (
    trans_Id    INT AUTO_INCREMENT PRIMARY KEY COMMENT '借还记录ID',
    book_Id     INT NOT NULL COMMENT '图书ID',
    cardNum     VARCHAR(10) NOT NULL COMMENT '一卡通号',
    Borrow_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '借出时间',
    Return_time DATETIME NULL COMMENT '归还时间',
    Due_time    DATETIME NOT NULL COMMENT '应还时间',
    Status      ENUM('BORROWED', 'RETURNED', 'OVERDUE') DEFAULT 'BORROWED' COMMENT '状态',
    Renew_count INT DEFAULT 0 COMMENT '续借次数',
    Remarks     TEXT COMMENT '备注信息',
    CONSTRAINT fk_bktrans_book
        FOREIGN KEY (book_Id) REFERENCES tblBook(book_Id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_bktrans_user
        FOREIGN KEY (cardNum) REFERENCES tblUser(cardNum)
        ON UPDATE CASCADE ON DELETE CASCADE
) COMMENT='图书借还记录表';

-- 12 商品表
CREATE TABLE tblProduct (
    product_Id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '商品号',
    Product_code VARCHAR(20) COMMENT '商品编码',
    Productname  VARCHAR(50) COMMENT '商品名',
    Price        DECIMAL(8,2) NOT NULL COMMENT '单价',
    Stock        INT DEFAULT 0 COMMENT '库存数量'
) COMMENT='商品表';

-- 13️ 商品交易记录表
CREATE TABLE tblProduct_trans (
    trans_Id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '商品交易ID',
    product_Id INT NOT NULL COMMENT '商品号',
    cardNum    VARCHAR(10) NOT NULL COMMENT '购买人ID',
    Qty        INT NOT NULL COMMENT '购买数量',
    Amount     DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    Trans_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
    CONSTRAINT fk_pdtrans_product
        FOREIGN KEY (product_Id) REFERENCES tblProduct(product_Id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_pdtrans_user
        FOREIGN KEY (cardNum) REFERENCES tblUser(cardNum)
        ON UPDATE CASCADE ON DELETE CASCADE
) COMMENT='商品交易记录表';

-- 14. 图书分类表（中图法标准）
CREATE TABLE tblBookCategory (
    category_id   INT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    category_code VARCHAR(2) UNIQUE NOT NULL COMMENT '分类代码（A-Z）',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description  TEXT COMMENT '分类描述',
    sort_order   INT DEFAULT 0 COMMENT '排序顺序'
) COMMENT='图书分类表（中图法标准）';

-- 15. 借阅规则配置表（新增）
CREATE TABLE tblBorrowRule (
    rule_id           INT AUTO_INCREMENT PRIMARY KEY COMMENT '规则ID',
    user_type         ENUM('student', 'staff', 'manager') NOT NULL COMMENT '用户类型',
    max_borrow_count  INT DEFAULT 5 COMMENT '最大借阅数量',
    max_borrow_days   INT DEFAULT 30 COMMENT '最大借阅天数',
    max_renew_count   INT DEFAULT 2 COMMENT '最大续借次数',
    renew_extend_days INT DEFAULT 15 COMMENT '续借延长天数',
    overdue_fine      DECIMAL(5,2) DEFAULT 0.50 COMMENT '逾期罚金（每天）',
    is_active         BOOLEAN DEFAULT TRUE COMMENT '是否启用'
) COMMENT='借阅规则配置表';





-- 数据库表结构创建完成
SELECT '虚拟校园系统数据库表结构创建完成！' as message;
SELECT '请运行 library_data.sql 文件来插入初始数据' as instruction;

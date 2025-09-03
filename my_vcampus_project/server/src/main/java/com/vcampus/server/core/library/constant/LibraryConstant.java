package com.vcampus.server.core.library.constant;

/**
 * 图书管理模块常量类
 * 包含所有业务相关的常量定义
 * 
 * @author VCampus Team
 * @version 1.0
 */
public class LibraryConstant {
    
    // ==================== 数据库表名常量 ====================
    public static final String TABLE_BOOK = "tblBook"; // 图书表名
    public static final String TABLE_BOOK_TRANS = "tblBook_trans"; // 图书借阅记录表名
    public static final String TABLE_BOOK_CATEGORY = "tblBookCategory"; // 图书分类表名
    public static final String TABLE_BORROW_RULE = "tblBorrowRule"; // 借阅规则表名
    public static final String TABLE_USER = "tblUser"; // 用户表名
    
    // ==================== 数据库字段名常量 ====================
    public static class BookFields { // 图书表字段
        public static final String BOOK_ID = "book_Id";
        public static final String ISBN = "isbn";
        public static final String TITLE = "Title";
        public static final String AUTHOR = "Author";
        public static final String PUBLISHER = "Publisher";
        public static final String PUBLISH_DATE = "Publish_date";
        public static final String CATEGORY = "Category";
        public static final String LOCATION = "Location";
        public static final String TOTAL_QTY = "Total_qty";
        public static final String AVAIL_QTY = "Avail_qty";
        public static final String STATUS = "Status";
    }
    
    public static class BookTransFields { // 借阅记录表字段
        public static final String TRANS_ID = "trans_Id";
        public static final String BOOK_ID = "book_Id";
        public static final String CARD_NUM = "cardNum";
        public static final String BORROW_TIME = "Borrow_time";
        public static final String RETURN_TIME = "Return_time";
        public static final String DUE_TIME = "Due_time";
        public static final String STATUS = "Status";
        public static final String RENEW_COUNT = "Renew_count";
        public static final String REMARKS = "Remarks";
    }
    
    // ==================== 业务常量 ====================
    public static class DefaultBorrowRules { // 默认借阅规则
        public static final int DEFAULT_MAX_BORROW_COUNT = 5;
        public static final int DEFAULT_MAX_BORROW_DAYS = 30;
        public static final int DEFAULT_MAX_RENEW_COUNT = 2;
        public static final int DEFAULT_RENEW_EXTEND_DAYS = 15;
        public static final double DEFAULT_OVERDUE_FINE = 0.50;
    }
    
    public static class Pagination { // 分页常量
        public static final int DEFAULT_PAGE_SIZE = 20;
        public static final int MAX_PAGE_SIZE = 100;
        public static final int MIN_PAGE_SIZE = 1;
    }
    
    public static class Search { // 搜索常量
        public static final int DEFAULT_SEARCH_LIMIT = 50;
        public static final int MAX_SEARCH_LIMIT = 200;
        public static final String WILDCARD = "%";
    }
    
    // ==================== 中图法分类常量 ====================
    public static class BookCategories { // 中图法基本大类
        public static final String A = "A"; // 马克思主义、列宁主义、毛泽东思想、邓小平理论
        public static final String B = "B"; // 哲学、宗教
        public static final String C = "C"; // 社会科学总论
        public static final String D = "D"; // 政治、法律
        public static final String E = "E"; // 军事
        public static final String F = "F"; // 经济
        public static final String G = "G"; // 文化、科学、教育、体育
        public static final String H = "H"; // 语言、文字
        public static final String I = "I"; // 文学
        public static final String J = "J"; // 艺术
        public static final String K = "K"; // 历史、地理
        public static final String N = "N"; // 自然科学总论
        public static final String O = "O"; // 数理科学和化学
        public static final String P = "P"; // 天文学、地球科学
        public static final String Q = "Q"; // 生物科学
        public static final String R = "R"; // 医药、卫生
        public static final String S = "S"; // 农业科学
        public static final String T = "T"; // 工业技术
        public static final String U = "U"; // 交通运输
        public static final String V = "V"; // 航空、航天
        public static final String X = "X"; // 环境科学、安全科学
        public static final String Z = "Z"; // 综合性图书
    }
    
    // ==================== 消息常量 ====================
    public static class SuccessMessage { // 成功消息
        public static final String BOOK_ADDED = "图书添加成功";
        public static final String BOOK_UPDATED = "图书信息更新成功";
        public static final String BOOK_DELETED = "图书删除成功";
        public static final String BORROW_SUCCESS = "借阅成功";
        public static final String RETURN_SUCCESS = "归还成功";
        public static final String RENEW_SUCCESS = "续借成功";
        public static final String RULE_UPDATED = "借阅规则更新成功";
    }
    
    public static class ErrorMessage { // 错误消息
        public static final String BOOK_NOT_FOUND = "图书不存在";
        public static final String BOOK_OUT_OF_STOCK = "图书库存不足";
        public static final String BOOK_ALREADY_EXISTS = "图书已存在";
        public static final String INVALID_ISBN = "ISBN格式不正确";
        public static final String INVALID_CATEGORY = "图书分类不存在";
        public static final String BORROW_LIMIT_EXCEEDED = "借阅数量已达上限";
        public static final String BORROW_DAYS_EXCEEDED = "借阅天数超过限制";
        public static final String RENEW_LIMIT_EXCEEDED = "续借次数已达上限";
        public static final String OVERDUE_CANNOT_RENEW = "逾期图书不能续借";
        public static final String ALREADY_RETURNED = "图书已归还";
        public static final String USER_NOT_FOUND = "用户不存在";
        public static final String INVALID_USER_TYPE = "用户类型无效";
        public static final String RULE_NOT_FOUND = "借阅规则不存在";
        public static final String INVALID_PARAMETERS = "参数无效";
        public static final String DATABASE_ERROR = "数据库操作失败";
        public static final String SYSTEM_ERROR = "系统错误";
    }
    // ==================== 配置常量 ====================
    public static class SystemConfig { // 系统配置
        public static final String DEFAULT_ENCODING = "UTF-8";
        public static final String DATE_FORMAT = "yyyy-MM-dd";
        public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        public static final int SESSION_TIMEOUT = 30; // 分钟
    }
    
    // ==================== 验证常量 ====================
    public static class FieldLength { // 字段长度限制
        public static final int ISBN_MAX_LENGTH = 20;
        public static final int TITLE_MAX_LENGTH = 100;
        public static final int AUTHOR_MAX_LENGTH = 50;
        public static final int PUBLISHER_MAX_LENGTH = 100;
        public static final int LOCATION_MAX_LENGTH = 50;
        public static final int CATEGORY_CODE_LENGTH = 2;
        public static final int CATEGORY_NAME_MAX_LENGTH = 50;
        public static final int CARD_NUM_LENGTH = 10;
        public static final int REMARKS_MAX_LENGTH = 500;
    }
    
    public static class NumericRange { // 数值范围限制
        public static final int MIN_BOOK_QUANTITY = 0;
        public static final int MAX_BOOK_QUANTITY = 9999;
        public static final int MIN_BORROW_DAYS = 1;
        public static final int MAX_BORROW_DAYS = 365;
        public static final int MIN_RENEW_COUNT = 0;
        public static final int MAX_RENEW_COUNT = 10;
        public static final double MIN_OVERDUE_FINE = 0.0;
        public static final double MAX_OVERDUE_FINE = 100.0;
    }
    
    // ==================== 私有构造函数 ====================
    private LibraryConstant() { // 私有构造函数，防止实例化
        throw new UnsupportedOperationException("常量类不能被实例化");
    }
}
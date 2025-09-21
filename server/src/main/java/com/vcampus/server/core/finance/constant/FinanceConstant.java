package com.vcampus.server.core.finance.constant;

/**
 * 财务管理模块常量定义
 * 
 * @author VCampus Team
 */
public class FinanceConstant {
    
    // 支付状态常量
    public static final String PAYMENT_STATUS_PENDING = "PENDING";
    public static final String PAYMENT_STATUS_COMPLETED = "COMPLETED";
    public static final String PAYMENT_STATUS_FAILED = "FAILED";
    public static final String PAYMENT_STATUS_CANCELLED = "CANCELLED";
    public static final String PAYMENT_STATUS_REFUNDED = "REFUNDED";
    
    // 支付类型常量
    public static final String PAYMENT_TYPE_TUITION = "TUITION";
    public static final String PAYMENT_TYPE_ACCOMMODATION = "ACCOMMODATION";
    public static final String PAYMENT_TYPE_MEAL = "MEAL";
    public static final String PAYMENT_TYPE_LIBRARY_FINE = "LIBRARY_FINE";
    public static final String PAYMENT_TYPE_OTHER = "OTHER";
    
    // 账单状态常量
    public static final String BILL_STATUS_UNPAID = "UNPAID";
    public static final String BILL_STATUS_PAID = "PAID";
    public static final String BILL_STATUS_OVERDUE = "OVERDUE";
    public static final String BILL_STATUS_CANCELLED = "CANCELLED";
    
    // 财务相关常量
    public static final String CURRENCY_CNY = "CNY";
    public static final double LIBRARY_FINE_PER_DAY = 0.5; // 每天罚金0.5元
    public static final int PAYMENT_TIMEOUT_MINUTES = 30;
    
    // 账户类型常量
    public static final String ACCOUNT_TYPE_STUDENT = "STUDENT";
    public static final String ACCOUNT_TYPE_STAFF = "STAFF";
    public static final String ACCOUNT_TYPE_SYSTEM = "SYSTEM";
    
    private FinanceConstant() {
        // 工具类，禁止实例化
    }
}

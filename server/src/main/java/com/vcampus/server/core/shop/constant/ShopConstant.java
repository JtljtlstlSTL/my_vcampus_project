package com.vcampus.server.core.shop.constant;

/**
 * 商城模块常量类
 * 定义与商品与交易相关的表名、字段名和业务常量
 */
public final class ShopConstant {

    // ==================== 数据库表名 ====================
    public static final String TABLE_PRODUCT = "tblProduct";              // 商品表
    public static final String TABLE_PRODUCT_TRANS = "tblProduct_trans";  // 商品购买记录表
    public static final String TABLE_PRODUCT_CATEGORY = "tblProductCategory"; // 商品分类表

    // ==================== 字段名 ====================
    public static final class ProductFields {
        public static final String PRODUCT_ID = "product_Id";
        public static final String PRODUCT_CODE = "Product_code";
        public static final String PRODUCT_NAME = "Productname";
        public static final String PRICE = "Price";
        public static final String STOCK = "Stock";
        public static final String STATUS = "Product_status";
        public static final String DESCRIPTION = "Product_description";
        public static final String CATEGORY = "Product_category";
        public static final String UPDATED_AT = "updated_at";
    }

    public static final class ProductTransFields {
        public static final String TRANS_ID = "trans_Id";
        public static final String PRODUCT_ID = "product_Id";
        public static final String CARD_NUM = "cardNum";
        public static final String QTY = "Qty";
        public static final String AMOUNT = "Amount";
        public static final String TRANS_TIME = "Trans_time";
        public static final String STATUS = "status";
    }

    public static final class ProductCategoryFields {
        public static final String CATEGORY_ID = "category_id";
        public static final String CATEGORY_CODE = "category_code";
        public static final String CATEGORY_NAME = "category_name";
        public static final String SORT_ORDER = "sort_order";
    }

    // ==================== 业务常量 ====================
    public static final class Status {
        public static final String ON_SHELF = "ON_SHELF";      // 在售
        public static final String OFF_SHELF = "OFF_SHELF";    // 下架
        public static final String SOLD_OUT = "SOLD_OUT";      // 售罄
    }

    public static final class OrderStatus {
        public static final String CREATED = "CREATED";
        public static final String PAID = "PAID";
        public static final String CANCELLED = "CANCELLED";
        public static final String REFUNDED = "REFUNDED";
    }

    public static final class Pagination {
        public static final int DEFAULT_PAGE_SIZE = 20;
        public static final int MAX_PAGE_SIZE = 100;
    }

    private ShopConstant() {
        throw new UnsupportedOperationException("常量类不能被实例化");
    }
}

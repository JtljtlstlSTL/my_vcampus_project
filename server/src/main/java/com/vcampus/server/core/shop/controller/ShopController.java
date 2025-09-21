package com.vcampus.server.core.shop.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.message.Session;
import com.vcampus.common.util.JsonUtils;
import com.vcampus.server.core.common.annotation.RouteMapping;
import com.vcampus.server.core.shop.entity.Product;
import com.vcampus.server.core.shop.entity.ProductCategory;
import com.vcampus.server.core.shop.entity.ProductTrans;
import com.vcampus.server.core.shop.enums.OrderStatus;
import com.vcampus.server.core.shop.enums.ProductStatus;
import com.vcampus.server.core.shop.service.ProductCategoryService;
import com.vcampus.server.core.shop.service.ProductService;
import com.vcampus.server.core.shop.service.ProductTransService;
import com.vcampus.server.core.shop.service.impl.ProductCategoryServiceImpl;
import com.vcampus.server.core.shop.service.impl.ProductServiceImpl;
import com.vcampus.server.core.shop.service.impl.ProductTransServiceImpl;
import com.vcampus.server.core.shop.store.CartStore;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;

@Slf4j
public class ShopController {

    private final ProductService productService;
    private final ProductCategoryService categoryService;
    private final ProductTransService transService;

    public ShopController() {
        this.productService = ProductServiceImpl.getInstance();
        this.categoryService = ProductCategoryServiceImpl.getInstance();
        this.transService = ProductTransServiceImpl.getInstance();
    }

    // 管理端入口（admin 角色别名，复用 manager 逻辑）
    @RouteMapping(uri = "shop/adminManager", role = "admin", description = "商城管理入口-管理员别名")
    public Response handleAdminManager(Request request) {
        // 直接复用 manager 分发逻辑
        return handleManager(request);
    }

    // 管理端入口：商品、分类、交易管理
    @RouteMapping(uri = "shop/manager", role = "manager", description = "商城管理入口")
    public Response handleManager(Request request) {
        String action = request.getParam("action");
        if (action == null) return Response.Builder.badRequest("缺少action参数");
        try {
            switch (action) {
                // 商品管理
                case "PRODUCT_CREATE":
                    return productCreate(request);
                case "PRODUCT_UPDATE":
                    return productUpdate(request);
                case "PRODUCT_DELETE":
                    return productDelete(request);
                case "PRODUCT_LIST":
                    return Response.Builder.success(productService.listAll());
                case "PRODUCT_AVAILABLE_LIST":
                    return Response.Builder.success(productService.findAvailableProducts());
                case "PRODUCT_GET_BY_ID":
                    return productGetById(request);
                case "PRODUCT_CHANGE_STOCK":
                    return productChangeStock(request);
                case "PRODUCT_CHANGE_STATUS":
                    return productChangeStatus(request);
                case "PRODUCT_SEARCH_BY_NAME":
                    return productSearchByName(request);
                case "PRODUCT_SEARCH_BY_CATEGORY":
                    return productSearchByCategory(request);

                // 分类管理
                case "CATEGORY_CREATE":
                    return categoryCreate(request);
                case "CATEGORY_UPDATE":
                    return categoryUpdate(request);
                case "CATEGORY_DELETE":
                    return categoryDelete(request);
                case "CATEGORY_LIST":
                    return Response.Builder.success(categoryService.listAll());

                // 交易管理
                case "TRANS_LIST_ALL":
                    return Response.Builder.success(transService.listAll());
                case "TRANS_GET_BY_ID":
                    return transGetById(request);
                case "TRANS_CHANGE_STATUS":
                    return transChangeStatus(request);

                default:
                    return Response.Builder.badRequest("不支持的操作: " + action);
            }
        } catch (Exception e) {
            log.error("shop/manager 处理失败, action={}", action, e);
            return Response.Builder.error("处理失败: " + e.getMessage());
        }
    }

    // 学生入口：商品浏览、下单、我的订单
    @RouteMapping(uri = "shop/student", role = "student", description = "商城-学生入口")
    public Response handleStudent(Request request) {
        return handleUserCommon(request);
    }

    // 教职工入口：商品浏览、下单、我的订单
    @RouteMapping(uri = "shop/staff", role = "staff", description = "商城-教职工入口")
    public Response handleStaff(Request request) {
        return handleUserCommon(request);
    }

    // 普通用户（学生/教职工）共用逻辑
    private Response handleUserCommon(Request request) {
        String action = request.getParam("action");
        try { log.info("shop user action={} params={}", action, request.getParams()); } catch (Exception ignore) {}
        if (action == null) return Response.Builder.badRequest("缺少action参数");
        try {
            switch (action) {
                case "PRODUCT_LIST":
                    return Response.Builder.success(productService.listAll());
                case "PRODUCT_AVAILABLE_LIST":
                    return Response.Builder.success(productService.findAvailableProducts());
                case "PRODUCT_GET_BY_ID":
                    return productGetById(request);
                case "BUY":
                case "BUY_NOW":
                    return buyProduct(request);
                case "MY_ORDERS":
                    return myOrders(request);
                case "PRODUCT_SEARCH_BY_NAME":
                    return productSearchByName(request);
                case "PRODUCT_SEARCH_BY_CATEGORY":
                    return productSearchByCategory(request);
                // ===== 购物车动作 =====
                case "ADD_TO_CART":
                    return cartAdd(request);
                case "SET_CART_ITEM":
                    return cartSetItem(request);
                case "REMOVE_FROM_CART":
                    return cartRemove(request);
                case "CLEAR_CART":
                    return cartClear(request);
                case "GET_CART":
                    return cartGet(request);
                case "CHECKOUT_CART":
                    return cartCheckout(request);
                default:
                    return Response.Builder.badRequest("不支持的操作: " + action);
            }
        } catch (Exception e) {
            log.error("shop 用户入口处理失败, action={}", action, e);
            return Response.Builder.error("处理失败: " + e.getMessage());
        }
    }

    // ================== 商品管理实现 ==================
    private Response productCreate(Request request) {
        String json = request.getParam("product");
        Product product = JsonUtils.fromJson(json, Product.class);
        if (product == null) return Response.Builder.badRequest("商品参数错误");
        Product saved = productService.createProduct(product);
        return Response.Builder.success("创建成功", saved);
    }

    private Response productUpdate(Request request) {
        String json = request.getParam("product");
        Product product = JsonUtils.fromJson(json, Product.class);
        if (product == null || product.getProductId() == null) return Response.Builder.badRequest("商品参数错误");
        Product saved = productService.updateProduct(product);
        return Response.Builder.success("更新成功", saved);
    }

    private Response productDelete(Request request) {
        String idStr = request.getParam("productId");
        if (idStr == null) return Response.Builder.badRequest("缺少productId");
        Integer id = Integer.valueOf(idStr);
        productService.deleteById(id);
        return Response.Builder.success("删除成功", null);
    }

    private Response productGetById(Request request) {
        String idStr = request.getParam("productId");
        if (idStr == null) return Response.Builder.badRequest("缺少productId");
        Integer id = Integer.valueOf(idStr);
        Optional<Product> p = productService.getById(id);
        return p.map(value -> Response.Builder.success("查询成功", value))
                .orElseGet(() -> Response.Builder.notFound("商品不存在: " + id));
    }

    private Response productChangeStock(Request request) {
        String idStr = request.getParam("productId");
        String deltaStr = request.getParam("delta");
        if (idStr == null || deltaStr == null) return Response.Builder.badRequest("缺少productId或delta");
        Integer id = Integer.valueOf(idStr);
        int delta = Integer.parseInt(deltaStr);
        productService.changeStock(id, delta);
        return Response.Builder.success("库存变更成功", Map.of("productId", id, "delta", delta));
    }

    private Response productChangeStatus(Request request) {
        String idStr = request.getParam("productId");
        String statusStr = request.getParam("status");
        if (idStr == null || statusStr == null) return Response.Builder.badRequest("缺少productId或status");
        Integer id = Integer.valueOf(idStr);
        ProductStatus status = ProductStatus.valueOf(statusStr);
        productService.changeStatus(id, status);
        return Response.Builder.success("状态更新成功", Map.of("productId", id, "status", status.name()));
    }

    private Response productSearchByName(Request request) {
        String name = request.getParam("name");
        if (name == null || name.isEmpty()) return Response.Builder.badRequest("缺少name");
        List<Product> list = productService.findByNameLike(name);
        return Response.Builder.success(list);
    }

    private Response productSearchByCategory(Request request) {
        String category = request.getParam("category");
        if (category == null || category.isEmpty()) return Response.Builder.badRequest("缺少category");
        List<Product> list = productService.findByCategory(category);
        return Response.Builder.success(list);
    }

    // ================== 分类管理实现 ==================
    private Response categoryCreate(Request request) {
        String json = request.getParam("category");
        ProductCategory c = JsonUtils.fromJson(json, ProductCategory.class);
        if (c == null) return Response.Builder.badRequest("分类参数错误");
        ProductCategory saved = categoryService.createCategory(c);
        return Response.Builder.success("创建成功", saved);
    }

    private Response categoryUpdate(Request request) {
        String json = request.getParam("category");
        ProductCategory c = JsonUtils.fromJson(json, ProductCategory.class);
        if (c == null || c.getCategoryId() == null) return Response.Builder.badRequest("分类参数错误");
        ProductCategory saved = categoryService.updateCategory(c);
        return Response.Builder.success("更新成功", saved);
    }

    private Response categoryDelete(Request request) {
        String idStr = request.getParam("categoryId");
        if (idStr == null) return Response.Builder.badRequest("缺少categoryId");
        Integer id = Integer.valueOf(idStr);
        categoryService.deleteById(id);
        return Response.Builder.success("删除成功", null);
    }

    // ================== 交易相关实现 ==================
    private Response transGetById(Request request) {
        String idStr = request.getParam("transId");
        if (idStr == null) return Response.Builder.badRequest("缺少transId");
        Integer id = Integer.valueOf(idStr);
        Optional<ProductTrans> t = transService.getById(id);
        return t.map(value -> Response.Builder.success("查询成功", value))
                .orElseGet(() -> Response.Builder.notFound("交易不存在: " + id));
    }

    private Response transChangeStatus(Request request) {
        String idStr = request.getParam("transId");
        String statusStr = request.getParam("status");
        if (idStr == null || statusStr == null) return Response.Builder.badRequest("缺少transId或status");
        Integer id = Integer.valueOf(idStr);
        OrderStatus status = OrderStatus.valueOf(statusStr);
        transService.changeStatus(id, status);
        return Response.Builder.success("状态更新成功", Map.of("transId", id, "status", status.name()));
    }

    private Response buyProduct(Request request) {
        String productIdStr = request.getParam("productId");
        String qtyStr = request.getParam("qty");
        if (productIdStr == null || qtyStr == null) return Response.Builder.badRequest("缺少productId或qty");
        Integer productId = parsePositiveIntFromMaybeDecimal(productIdStr);
        if (productId == null) return Response.Builder.badRequest("productId格式非法: " + productIdStr);
        Integer qty = parsePositiveIntFromMaybeDecimal(qtyStr);
        if (qty == null) return Response.Builder.badRequest("qty格式非法(必须为正整数), 收到: " + qtyStr);
        String cardNum = request.getParam("cardNum");
        if (cardNum == null || cardNum.isEmpty()) {
            Session session = request.getSession();
            if (session == null || session.getUserId() == null) return Response.Builder.forbidden("未登录或会话失效");
            cardNum = session.getUserId();
        }
        Product product = productService.getById(productId).orElse(null);
        if (product == null) return Response.Builder.notFound("商品不存在: " + productId);
        BigDecimal price = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
        BigDecimal total = price.multiply(new BigDecimal(qty));
        if (total.compareTo(BigDecimal.ZERO) <= 0) return Response.Builder.badRequest("金额非法: " + total);
        ProductTrans trans;
        try {
            trans = transService.createTransactionAtomic(productId, cardNum, qty);
        } catch (Exception e) {
            log.error("原子下单失败: productId={} cardNum={} qty={}", productId, cardNum, qty, e);
            return Response.Builder.error("下单失败：" + e.getMessage());
        }
        return Response.Builder.success("下单成功", trans);
    }

    /**
     * 解析一个代表正整数数量的字符串，允许形式:
     *  - 纯整数: "4"
     *  - 带小数点且小数部分全为0: "4.0", "4.000" (视为4)
     * 返回 null 表示非法。
     */
    private Integer parsePositiveIntFromMaybeDecimal(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;
        try {
            if (s.contains(".")) {
                BigDecimal bd = new BigDecimal(s);
                // 去掉尾随0后，如仍有小数位则非法
                BigDecimal stripped = bd.stripTrailingZeros();
                if (stripped.scale() > 0) return null; // 仍有小数部分
                int val = stripped.intValueExact();
                return val > 0 ? val : null;
            } else {
                int val = Integer.parseInt(s);
                return val > 0 ? val : null;
            }
        } catch (Exception e) {
            return null;
        }
    }


    // 查询当前用户或指定 cardNum 的订单列表
    private Response myOrders(Request request) {
        String cardNum = request.getParam("cardNum");
        if (cardNum == null || cardNum.isBlank()) {
            Session s = request.getSession();
            if (s == null || s.getUserId() == null) {
                return Response.Builder.forbidden("未登录或会话失效");
            }
            cardNum = s.getUserId();
        }
        try {
            List<ProductTrans> list = transService.findByCardNum(cardNum);
            // 将 ProductTrans 转换为兼容客户端显示的 Map，附带 productName
            List<Map<String,Object>> out = new java.util.ArrayList<>();
            for (ProductTrans t : list) {
                Map<String,Object> m = new java.util.LinkedHashMap<>();
                m.put("transId", t.getTransId());
                m.put("productId", t.getProductId());
                // 尝试查询商��名称，若不存在则为空字符串
                String pname = "";
                try {
                    if (t.getProductId() != null) {
                        var pOpt = productService.getById(t.getProductId());
                        if (pOpt.isPresent()) pname = pOpt.get().getProductName();
                    }
                } catch (Exception ignored) {}
                m.put("productName", pname);
                m.put("qty", t.getQty());
                m.put("amount", t.getAmount());
                m.put("transTime", t.getTransTime());
                m.put("status", t.getStatus() == null ? null : t.getStatus().name());
                m.put("cardNum", t.getCardNum());
                out.add(m);
            }
            return Response.Builder.success(out);
        } catch (Exception e) {
            log.error("查询订单失败 cardNum={}", cardNum, e);
            return Response.Builder.error("查询订单失败: " + e.getMessage());
        }
    }

    // ================= 购物车实现 =================
    private String currentUser(Request request) {
        String uid = null;
        Session s = request.getSession();
        if (s != null) uid = s.getUserId();
        if ((uid == null || uid.isBlank())) uid = request.getParam("cardNum");
        return uid;
    }

    private Response cartAdd(Request request) {
        String uid = currentUser(request);
        if (uid == null || uid.isBlank()) return Response.Builder.forbidden("未登录");
        Integer pid = parsePositiveIntFromMaybeDecimal(request.getParam("productId"));
        Integer qty = parsePositiveIntFromMaybeDecimal(request.getParam("qty"));
        if (pid == null || qty == null) return Response.Builder.badRequest("参数错误(productId/qty)");
        var prodOpt = productService.getById(pid);
        if (prodOpt.isEmpty()) return Response.Builder.notFound("商品不存在:"+pid);
        Product p = prodOpt.get();
        if (p.getStock() != null && p.getStock() <= 0) return Response.Builder.badRequest("商品库存不足");
        if (p.getStatus() != null && p.getStatus() != ProductStatus.ON_SHELF) return Response.Builder.badRequest("商品未上架");
        // 限制加入后总数量不超过库存
        int current = CartStore.getInstance().snapshot(uid).getOrDefault(pid,0);
        int maxAllowed = p.getStock()==null? (current+qty): Math.min(current+qty, p.getStock());
        int delta = maxAllowed - current;
        if (delta <= 0) return Response.Builder.badRequest("已达到库存上限");
        log.info("[CART] ADD uid={} pid={} reqQty={} deltaAdded={} newSnapshot={}", uid, pid, qty, delta, CartStore.getInstance().snapshot(uid));
        CartStore.getInstance().add(uid, pid, delta);
        return Response.Builder.success("已加入", Map.of(
                "cartCount", CartStore.getInstance().totalCount(uid),
                "productId", pid,
                "qtyAdded", delta
        ));
    }

    private Response cartSetItem(Request request) {
        String uid = currentUser(request);
        if (uid == null) return Response.Builder.forbidden("未登录");
        Integer pid = parsePositiveIntFromMaybeDecimal(request.getParam("productId"));
        Integer qty = parsePositiveIntFromMaybeDecimal(request.getParam("qty"));
        log.info("[CART] SET_ITEM uid={} pid={} qty={}", uid, pid, qty);
        if (pid == null || qty == null) return Response.Builder.badRequest("参数错误");
        if (qty <= 0) {
            log.info("[CART] SET_ITEM removed uid={} pid={} newSnapshot={}", uid, pid, CartStore.getInstance().snapshot(uid));
            CartStore.getInstance().remove(uid, List.of(pid));
            return Response.Builder.success("已移除", Map.of("cartCount", CartStore.getInstance().totalCount(uid)));
        }
        var prodOpt = productService.getById(pid);
        if (prodOpt.isEmpty()) return Response.Builder.notFound("商品不存在:"+pid);
        Product p = prodOpt.get();
        if (p.getStock()!=null && p.getStock() < qty) qty = p.getStock();
        log.info("[CART] SET_ITEM updated uid={} pid={} qty={} newSnapshot={}", uid, pid, qty, CartStore.getInstance().snapshot(uid));
        CartStore.getInstance().setQty(uid, pid, qty);
        return Response.Builder.success("更新成功", Map.of(
                "cartCount", CartStore.getInstance().totalCount(uid),
                "productId", pid,
                "qty", qty
        ));
    }

    private Response cartRemove(Request request) {
        String uid = currentUser(request);
        if (uid == null) return Response.Builder.forbidden("未登录");
        String ids = request.getParam("productIds");
        if (ids == null || ids.isBlank()) return Response.Builder.badRequest("缺少productIds");
        List<Integer> list = new java.util.ArrayList<>();
        for (String s : ids.split(",")) {
            Integer v = parsePositiveIntFromMaybeDecimal(s.trim());
            if (v != null) list.add(v);
        }
        log.info("[CART] REMOVE uid={} productIds={}", uid, list);
        if (list.isEmpty()) return Response.Builder.badRequest("无有效productId");
        log.info("[CART] REMOVE after uid={} snapshot={}", uid, CartStore.getInstance().snapshot(uid));
        CartStore.getInstance().remove(uid, list);
        return Response.Builder.success("已移除", Map.of("cartCount", CartStore.getInstance().totalCount(uid)));
    }

    private Response cartClear(Request request) {
        String uid = currentUser(request);
        if (uid == null) return Response.Builder.forbidden("未登录");
        log.info("[CART] CLEAR uid={} beforeSnapshot={}", uid, CartStore.getInstance().snapshot(uid));
        CartStore.getInstance().clear(uid);
        log.info("[CART] CLEAR uid={} afterSnapshot={}", uid, CartStore.getInstance().snapshot(uid));
        return Response.Builder.success("已清空", Map.of("cartCount",0));
    }

    private Response cartGet(Request request) {
        String uid = currentUser(request);
        if (uid == null) return Response.Builder.forbidden("未登录");
        return Response.Builder.success(buildCartView(uid));
    }

    private Map<String,Object> buildCartView(String uid) {
        Map<Integer,Integer> snap = CartStore.getInstance().snapshot(uid);
        log.info("[CART] BUILD_VIEW uid={} snapshot={}", uid, snap);
        List<Map<String,Object>> items = new java.util.ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int count = 0;
        for (var e : snap.entrySet()) {
            Integer pid = e.getKey(); Integer qty = e.getValue();
            if (qty == null || qty <= 0) continue;
            Product p = productService.getById(pid).orElse(null);
            if (p == null) continue; // 跳过已删商品
            BigDecimal price = p.getPrice()==null? BigDecimal.ZERO : p.getPrice();
            BigDecimal sub = price.multiply(new BigDecimal(qty));
            totalAmount = totalAmount.add(sub);
            count += qty;
            // 使用可变 HashMap 以允许 null 值（Map.of 不允许 null）
            Map<String,Object> item = new java.util.LinkedHashMap<>();
            item.put("productId", pid);
            item.put("productName", p.getProductName());
            item.put("price", price);
            item.put("qty", qty);
            item.put("subtotal", sub);
            item.put("stock", p.getStock()); // 可能为 null
            item.put("status", p.getStatus()==null? null : p.getStatus().name()); // 可能为 null
            items.add(item);
        }
        log.info("[CART] BUILD_VIEW uid={} totalAmount={} count={}", uid, totalAmount, count);
        return Map.of(
                "items", items,
                "count", count,
                "totalAmount", totalAmount
        );
    }

    private Response cartCheckout(Request request) {
        String uid = currentUser(request);
        if (uid == null) return Response.Builder.forbidden("未登录");
        Map<Integer,Integer> snap = CartStore.getInstance().snapshot(uid);
        log.info("[CART] CHECKOUT uid={} snapshot={}", uid, snap);
        if (snap.isEmpty()) return Response.Builder.badRequest("购物车为空");
        // 使用服务层的一次性原子方法处理整个购物车，避免逐项处理导致的不一致
        try {
            List<ProductTrans> results = transService.createCartTransactionAtomic(snap, uid);
            // 计算返回总额与条目数
            BigDecimal totalAmount = BigDecimal.ZERO;
            int totalItems = 0;
            List<Map<String,Object>> success = new java.util.ArrayList<>();
            for (ProductTrans t : results) {
                BigDecimal amt = t.getAmount() == null ? BigDecimal.ZERO : t.getAmount();
                totalAmount = totalAmount.add(amt);
                int q = t.getQty() == null ? 0 : t.getQty();
                totalItems += q;
                success.add(Map.of(
                        "productId", t.getProductId(),
                        "qty", q,
                        "amount", amt,
                        "transId", t.getTransId()
                ));
            }

            // 结算成功后从内存购物车中移除已结算的商品
            try { CartStore.getInstance().remove(uid, results.stream().map(ProductTrans::getProductId).collect(java.util.stream.Collectors.toList())); } catch (Exception ignored) {}

            Map<String,Object> data = new java.util.HashMap<>();
            data.put("success", success);
            data.put("fail", new java.util.ArrayList<>());
            data.put("totalAmount", totalAmount);
            data.put("totalItems", totalItems);
            data.put("cartCount", CartStore.getInstance().totalCount(uid));
            data.put("remainingCart", buildCartView(uid));
            return Response.Builder.success("结算成功", data);
        } catch (RuntimeException re) {
            log.error("[CART] CHECKOUT failed (atomic): {}", re.getMessage(), re);
            // 若原子处理失败，返回错误并附带当前购物车视图以便客户端能恢复
            Map<String,Object> data = new java.util.HashMap<>();
            data.put("remainingCart", buildCartView(uid));
            return Response.Builder.error("结算失败: " + re.getMessage()).withSession(null);
        }
    }
    // ================= 购物车实现结束 =================

    // 恢复余额查询方法
    private BigDecimal getCardBalance(String cardNum) throws Exception {
        org.apache.ibatis.session.SqlSessionFactory factory = com.vcampus.server.core.db.DatabaseManager.getSqlSessionFactory();
        try (org.apache.ibatis.session.SqlSession sqlSession = factory.openSession(true)) {
            java.sql.Connection conn = sqlSession.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT balance FROM tblCard WHERE cardNum = ?")) {
                ps.setString(1, cardNum);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBigDecimal("balance");
                    }
                    return null;
                }
            }
        }
    }
}

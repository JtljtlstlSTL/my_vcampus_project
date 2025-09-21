package com.vcampus.server.core.shop.service.impl;

import com.vcampus.server.core.shop.dao.ProductDao;
import com.vcampus.server.core.shop.dao.ProductTransDao;
import com.vcampus.server.core.shop.entity.Product;
import com.vcampus.server.core.shop.entity.ProductTrans;
import com.vcampus.server.core.shop.enums.OrderStatus;
import com.vcampus.server.core.shop.service.ProductTransService;
import com.vcampus.server.core.db.DatabaseManager;
import com.vcampus.server.core.shop.mapper.ProductMapper;
import com.vcampus.server.core.shop.mapper.ProductTransMapper;
import com.vcampus.server.core.card.mapper.CardMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductTransServiceImpl implements ProductTransService {
    private static final Logger log = LoggerFactory.getLogger(ProductTransServiceImpl.class);
    private static ProductTransServiceImpl instance;
    private final ProductTransDao transDao;
    private final ProductDao productDao;
    private final SqlSessionFactory sqlSessionFactory;

    private ProductTransServiceImpl() {
        this.transDao = ProductTransDao.getInstance();
        this.productDao = ProductDao.getInstance();
        this.sqlSessionFactory = DatabaseManager.getSqlSessionFactory();
    }

    public static synchronized ProductTransServiceImpl getInstance() {
        if (instance == null) {
            instance = new ProductTransServiceImpl();
        }
        return instance;
    }

    @Override
    public ProductTrans createTransaction(Integer productId, String cardNum, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("数量必须大于0");
        Product product = productDao.findById(productId).orElseThrow(() -> new RuntimeException("商品不存在: " + productId));

        // 并发安全扣库存：若库存不足，则返回false
        boolean ok = productDao.decreaseStockIfEnough(productId, qty);
        if (!ok) {
            throw new RuntimeException("库存不足");
        }

        try {
            BigDecimal price = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
            BigDecimal amount = price.multiply(new BigDecimal(qty));
            ProductTrans trans = ProductTrans.builder()
                    .productId(productId)
                    .cardNum(cardNum)
                    .qty(qty)
                    .amount(amount)
                    .transTime(LocalDateTime.now())
                    .status(OrderStatus.PAID)
                    .build();

            transDao.save(trans);
            return trans;
        } catch (RuntimeException ex) {
            // 失败补偿：回滚已扣减库存
            productDao.increaseStock(productId, qty);
            throw ex;
        }
    }

    @Override
    public ProductTrans createTransactionAtomic(Integer productId, String cardNum, int qty) {
        if (qty <= 0) throw new IllegalArgumentException("数量必须大于0");

        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            ProductMapper productMapper = session.getMapper(ProductMapper.class);
            ProductTransMapper transMapper = session.getMapper(ProductTransMapper.class);
            CardMapper cardMapper = session.getMapper(CardMapper.class);

            // 验证商品存在
            Product product = productMapper.findById(productId);
            if (product == null) throw new RuntimeException("商品不存在: " + productId);

            BigDecimal price = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
            BigDecimal amount = price.multiply(new BigDecimal(qty));

            // 扣库存（并发安全）
            int stockAffected = productMapper.decreaseStockIfEnough(productId, qty);
            log.info("[DEBUG] createTransactionAtomic stockAffected={}", stockAffected);
            if (stockAffected <= 0) {
                session.rollback();
                throw new RuntimeException("库存不足");
            }

            // 扣一卡通余额（原子性：只有余额充足时才会成功）
            // 使用 JDBC 直接执行原子更新，确保参数名与类型一致
            java.sql.Connection conn = session.getConnection();
            String updSql = "UPDATE tblCard SET balance = balance - ? WHERE cardNum = ? AND balance >= ?";
            int balAffected = 0;
            try (java.sql.PreparedStatement ups = conn.prepareStatement(updSql)) {
                ups.setBigDecimal(1, amount);
                ups.setString(2, cardNum);
                ups.setBigDecimal(3, amount);
                balAffected = ups.executeUpdate();
                log.info("[DEBUG] createTransactionAtomic balance update affectedRows={}", balAffected);
            }
            if (balAffected <= 0) {
                session.rollback();
                throw new RuntimeException("余额不足或卡不存在");
            }

            // 读取更新后的余额（同一连接）以便调试
            try (java.sql.PreparedStatement q = conn.prepareStatement("SELECT balance FROM tblCard WHERE cardNum = ?")) {
                q.setString(1, cardNum);
                try (java.sql.ResultSet rsq = q.executeQuery()) {
                    if (rsq.next()) {
                        java.math.BigDecimal newBal = rsq.getBigDecimal("balance");
                        log.info("[DEBUG] createTransactionAtomic balance after update (same conn) = {}", newBal);
                    } else {
                        log.warn("[DEBUG] createTransactionAtomic could not find card after update: {}", cardNum);
                    }
                }
            } catch (Exception ex) {
                log.warn("[DEBUG] query after balance update failed", ex);
            }

            // 插入商品交易记录
            ProductTrans trans = ProductTrans.builder()
                    .productId(productId)
                    .cardNum(cardNum)
                    .qty(qty)
                    .amount(amount)
                    .status(OrderStatus.PAID)
                    .build();

            transMapper.insert(trans);

            // 在同一事务中插入一卡通交易记录（tblCard_trans）
             String sql = "INSERT INTO tblCard_trans (cardNum, Trans_time, Trans_type, Amount) VALUES (?, CURRENT_TIMESTAMP(6), ?, ?)";
             try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                 ps.setString(1, cardNum);
                 ps.setString(2, "CONSUME");
                 ps.setBigDecimal(3, amount);
                 int ins = ps.executeUpdate();
                 log.info("[DEBUG] createTransactionAtomic inserted tblCard_trans rows={}", ins);
             }

             session.commit();
             log.info("[DEBUG] createTransactionAtomic committed transaction for productId={}, cardNum={}, amount={}", productId, cardNum, amount);


            // 若MyBatis未回填transId，可尝试从数据库查询最新插入项，但通常会被回填
            return trans;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("原子下单失败", e);
        }
    }

    @Override
    public List<ProductTrans> createCartTransactionAtomic(Map<Integer, Integer> cartItems, String cardNum) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("购物车为空");
        }
        if (cardNum == null || cardNum.trim().isEmpty()) {
            throw new IllegalArgumentException("cardNum不能为空");
        }

        List<ProductTrans> results = new ArrayList<>();

        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            ProductMapper productMapper = session.getMapper(ProductMapper.class);
            ProductTransMapper transMapper = session.getMapper(ProductTransMapper.class);
            CardMapper cardMapper = session.getMapper(CardMapper.class);
            java.sql.Connection conn = session.getConnection();

            // 第一步：验证所有商品存在并计算总金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            Map<Integer, Product> productCache = new HashMap<>();
            Map<Integer, BigDecimal> itemAmounts = new HashMap<>();

            // 增加详细日志：记录原始 cartItems
            log.info("[CART][ATOMIC] 开始解析 cartItems={}, cardNum={}", cartItems, cardNum);

            for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
                Integer productId = entry.getKey();
                Integer qty = entry.getValue();

                if (qty <= 0) {
                    session.rollback();
                    throw new RuntimeException("商品数量必须大于0: productId=" + productId + ", qty=" + qty);
                }

                Product product = productMapper.findById(productId);
                if (product == null) {
                    session.rollback();
                    throw new RuntimeException("商品不存在: " + productId);
                }

                BigDecimal price = product.getPrice() == null ? BigDecimal.ZERO : product.getPrice();
                BigDecimal amount = price.multiply(new BigDecimal(qty));

                productCache.put(productId, product);
                itemAmounts.put(productId, amount);
                totalAmount = totalAmount.add(amount);
            }

            // 记录计算结果
            log.info("[CART][ATOMIC] 计算完成 totalAmount={} itemAmounts={} productCacheKeys={}", totalAmount, itemAmounts, productCache.keySet());

            // 第二步：检查校园卡余额是否足够
            String balanceCheckSql = "SELECT balance FROM tblCard WHERE cardNum = ?";
            BigDecimal currentBalance;
            try (java.sql.PreparedStatement ps = conn.prepareStatement(balanceCheckSql)) {
                ps.setString(1, cardNum);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        session.rollback();
                        throw new RuntimeException("校园卡不存在: " + cardNum);
                    }
                    currentBalance = rs.getBigDecimal("balance");
                    log.info("[CART][ATOMIC] Balance check for cardNum={}: currentBalance={}, totalAmount={}", cardNum, currentBalance, totalAmount);
                    if (currentBalance.compareTo(totalAmount) < 0) {
                        session.rollback();
                        throw new RuntimeException("余额不足，当前余额: " + currentBalance + ", 需要: " + totalAmount);
                    }
                }
            } catch (Exception ex) {
                log.error("[CART][ATOMIC] 检查余额时发生异常", ex);
                session.rollback();
                throw new RuntimeException("检查余额失败", ex);
            }

            // 第三步：扣减所有商品库存（原子性检查）
            for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
                Integer productId = entry.getKey();
                Integer qty = entry.getValue();

                int stockAffected = productMapper.decreaseStockIfEnough(productId, qty);
                log.info("[CART][ATOMIC] decreaseStock productId={} qty={} affected={}", productId, qty, stockAffected);
                if (stockAffected <= 0) {
                    session.rollback();
                    throw new RuntimeException("库存不足: productId=" + productId + ", 需要数量=" + qty);
                }
            }

            // 第四步：扣减校园卡总余额
            String updateBalanceSql = "UPDATE tblCard SET balance = balance - ? WHERE cardNum = ? AND balance >= ?";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(updateBalanceSql)) {
                ps.setBigDecimal(1, totalAmount);
                ps.setString(2, cardNum);
                ps.setBigDecimal(3, totalAmount);
                int balanceAffected = ps.executeUpdate();
                log.info("[CART][ATOMIC] updateBalance cardNum={} totalAmount={} affected={}", cardNum, totalAmount, balanceAffected);
                if (balanceAffected <= 0) {
                    session.rollback();
                    throw new RuntimeException("余额扣减失败，可能余额不足");
                }
            } catch (Exception ex) {
                if (ex instanceof java.sql.SQLException) {
                    java.sql.SQLException sqe = (java.sql.SQLException) ex;
                    log.error("[CART][ATOMIC] updateBalance SQL异常 sql={} cardNum={} totalAmount={} SQLState={} errorCode={}", updateBalanceSql, cardNum, totalAmount, sqe.getSQLState(), sqe.getErrorCode(), sqe);
                } else {
                    log.error("[CART][ATOMIC] updateBalance 异常", ex);
                }
                session.rollback();
                throw new RuntimeException("余额扣减失败", ex);
            }

            // 第五步：插入所有商品交易记录
            for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
                Integer productId = entry.getKey();
                Integer qty = entry.getValue();
                BigDecimal amount = itemAmounts.get(productId);

                ProductTrans trans = ProductTrans.builder()
                        .productId(productId)
                        .cardNum(cardNum)
                        .qty(qty)
                        .amount(amount)
                        .status(OrderStatus.PAID)
                        .build();

                try {
                    transMapper.insert(trans);
                    results.add(trans);
                    log.info("[CART][ATOMIC] inserted ProductTrans: {}", trans);
                } catch (Exception ex) {
                    if (ex instanceof java.sql.SQLException) {
                        java.sql.SQLException sqe = (java.sql.SQLException) ex;
                        log.error("[CART][ATOMIC] 插入 ProductTrans SQL 异常 productId={} qty={} SQLState={} errorCode={}", productId, qty, sqe.getSQLState(), sqe.getErrorCode(), sqe);
                    } else {
                        log.error("[CART][ATOMIC] 插入 ProductTrans 异常 productId={} qty={}", productId, qty, ex);
                    }
                    session.rollback();
                    throw new RuntimeException("插入商品交易记录失败", ex);
                }
            }

            // 第六步：插入校园卡交易记录
            String insertCardTransSql = "INSERT INTO tblCard_trans (cardNum, Trans_time, Trans_type, Amount) VALUES (?, CURRENT_TIMESTAMP(6), ?, ?)";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(insertCardTransSql)) {
                ps.setString(1, cardNum);
                ps.setString(2, "CONSUME");
                ps.setBigDecimal(3, totalAmount);
                int cardTransInserted = ps.executeUpdate();
                log.info("[CART][ATOMIC] inserted tblCard_trans rows={} totalAmount={}", cardTransInserted, totalAmount);
            } catch (Exception ex) {
                if (ex instanceof java.sql.SQLException) {
                    java.sql.SQLException sqe = (java.sql.SQLException) ex;
                    log.error("[CART][ATOMIC] 插入 tblCard_trans SQL 异常 SQLState={} errorCode={}", sqe.getSQLState(), sqe.getErrorCode(), sqe);
                } else {
                    log.error("[CART][ATOMIC] 插入 tblCard_trans 异常", ex);
                }
                session.rollback();
                throw new RuntimeException("插入校园卡交易记录失败", ex);
            }

            // 提交事务
            session.commit();
            log.info("[CART][ATOMIC] 批量处理成功 cardNum={} totalAmount={} itemCount={}", cardNum, totalAmount, results.size());

            return results;

        } catch (RuntimeException e) {
            // 已在各步骤中记录并回滚，直接抛出
            log.error("[CART][ATOMIC] 运行时异常导致回滚: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // 尝试提取 SQLException 详细信息
            Throwable cause = e;
            while (cause != null) {
                if (cause instanceof java.sql.SQLException) {
                    java.sql.SQLException sqe = (java.sql.SQLException) cause;
                    log.error("[CART][ATOMIC] 捕获到 SQLException SQLState={} errorCode={} message={}", sqe.getSQLState(), sqe.getErrorCode(), sqe.getMessage(), sqe);
                    break;
                }
                cause = cause.getCause();
            }
            log.error("[CART][ATOMIC] 未知异常，回滚并抛出", e);
            throw new RuntimeException("购物车批量处理失败", e);
        }
    }

    @Override
    public Optional<ProductTrans> getById(Integer transId) {
        return transDao.findById(transId);
    }

    @Override
    public List<ProductTrans> listAll() {
        return transDao.findAll();
    }

    @Override
    public List<ProductTrans> findByCardNum(String cardNum) {
        return transDao.findByCardNum(cardNum);
    }

    @Override
    public List<ProductTrans> findByProductId(Integer productId) {
        return transDao.findByProductId(productId);
    }

    @Override
    public void changeStatus(Integer transId, OrderStatus status) {
        transDao.updateStatus(transId, status);
    }

    @Override
    public void deleteById(Integer transId) {
        transDao.deleteById(transId);
    }

    @Override
    public long count() {
        return transDao.count();
    }

    @Override
    public long countByCardNum(String cardNum) {
        return transDao.countOrdersByCardNum(cardNum);
    }

    @Override
    public long countByProductId(Integer productId) {
        return transDao.countOrdersByProductId(productId);
    }
}

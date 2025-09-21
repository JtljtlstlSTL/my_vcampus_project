package com.vcampus.server.core.card.controller;

import com.vcampus.common.message.Request;
import com.vcampus.common.message.Response;
import com.vcampus.common.message.Session;
import com.vcampus.common.util.JsonUtils;
import com.vcampus.server.core.card.dao.CardDao;
import com.vcampus.server.core.card.entity.Card;
import com.vcampus.server.core.card.service.CardService;
import com.vcampus.server.core.card.service.impl.CardServiceImpl;
import com.vcampus.server.core.common.annotation.RouteMapping;
import com.vcampus.server.core.db.DatabaseManager;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
public class CardController {

    private final CardService cardService;
    private final CardDao cardDao;

    public CardController() {
        this.cardService = CardServiceImpl.getInstance();
        this.cardDao = CardDao.getInstance();
    }

    @RouteMapping(uri = "card/student", role = "student", description = "一卡通-学生入口")
    public Response handleStudent(Request request) {
        return handleUserCommon(request);
    }

    @RouteMapping(uri = "card/staff", role = "staff", description = "一卡通-教职工入口")
    public Response handleStaff(Request request) {
        return handleUserCommon(request);
    }

    @RouteMapping(uri = "card/manager", role = "manager", description = "一卡通管理入口")
    public Response handleManager(Request request) {
        String action = request.getParam("action");
        if (action == null) return Response.Builder.badRequest("缺少action参数");
        try {
            switch (action) {
                case "CARD_LIST":
                    return Response.Builder.success(listAllCards());
                case "CARD_GET_BY_NUM":
                    return cardGetByNum(request);
                case "CARD_CREATE":
                    return cardCreate(request);
                case "CARD_DELETE":
                    return cardDelete(request);
                default:
                    return Response.Builder.badRequest("不支持的操作: " + action);
            }
        } catch (Exception e) {
            log.error("card/manager 处理失败, action={}", action, e);
            return Response.Builder.error("处理失败: " + e.getMessage());
        }
    }

    private Response listAllCards() {
        // 简单实现：直接使用 JDBC 查询所有卡（避免额外 DAO）
        try (var session = DatabaseManager.getSqlSessionFactory().openSession()) {
            var conn = session.getConnection();
            // 关联用户表获取姓名
            String sql = "SELECT c.card_Id, c.cardNum, c.balance, c.status, u.Name AS userName FROM tblCard c LEFT JOIN tblUser u ON c.cardNum = u.cardNum ORDER BY c.card_Id";
            try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                List<Map<String,Object>> list = new ArrayList<>();
                while (rs.next()) {
                    Map<String,Object> m = new HashMap<>();
                    m.put("cardId", rs.getInt("card_Id"));
                    m.put("cardNum", rs.getString("cardNum"));
                    m.put("balance", rs.getBigDecimal("balance"));
                    m.put("status", rs.getString("status"));
                    m.put("name", rs.getString("userName")); // 增加姓名
                    list.add(m);
                }
                return Response.Builder.success(list);
            }
        } catch (Exception e) {
            throw new RuntimeException("查询一卡通列表失败", e);
        }
    }

    private Response cardGetByNum(Request request) {
        String cardNum = request.getParam("cardNum");
        if (cardNum == null || cardNum.isEmpty()) return Response.Builder.badRequest("缺少cardNum");
        Card c = cardService.findByCardNum(cardNum);
        if (c == null) return Response.Builder.notFound("卡不存在: " + cardNum);
        return Response.Builder.success(c);
    }

    private Response cardCreate(Request request) {
        String cardNum = request.getParam("cardNum");
        String balStr = request.getParam("balance");
        if (cardNum == null || cardNum.isEmpty()) return Response.Builder.badRequest("缺少cardNum");
        BigDecimal balance = BigDecimal.ZERO;
        if (balStr != null && !balStr.isEmpty()) {
            try { balance = new BigDecimal(balStr); } catch (Exception e) { return Response.Builder.badRequest("balance格式非法"); }
        }
        Card c = new Card();
        c.setCardNum(cardNum);
        c.setBalance(balance);
        c.setStatus("正常");
        Card saved = cardDao.save(c);
        return Response.Builder.success("创建成功", saved);
    }

    private Response cardDelete(Request request) {
        String cardNum = request.getParam("cardNum");
        if (cardNum == null || cardNum.isEmpty()) return Response.Builder.badRequest("缺少cardNum");
        cardDao.deleteByCardNum(cardNum);
        return Response.Builder.success("删除成功", null);
    }

    // 公共用户入口方法：查询余额、充值、查看交易记录
    private Response handleUserCommon(Request request) {
        String action = request.getParam("action");
        try {
            log.info("card user common received action={}, params={}", action, request.getParams());
        } catch (Exception ignore) {}
        if (action == null) return Response.Builder.badRequest("缺少action参数");
        try {
            switch (action) {
                case "GET_BALANCE":
                    return getBalance(request);
                case "SET_STATUS":
                    return setStatus(request);
                case "RECHARGE":
                    return recharge(request);
                case "CONSUME":
                    return consume(request);
                case "TRANS_LIST":
                    return transList(request);
                default:
                    return Response.Builder.badRequest("不支持的操作: " + action);
            }
        } catch (Exception e) {
            log.error("card 用户入口处理失败, action={}", action, e);
            return Response.Builder.error("处理失败: " + e.getMessage());
        }
    }

    private String resolveCardNumFromRequestOrSession(Request request) {
        // 优先使用会话中的 userId，避免客户端通过参数冒充或篡改
        Session session = request.getSession();
        if (session != null && session.getUserId() != null && !session.getUserId().isEmpty()) {
            return session.getUserId();
        }
        // 回退：若会话不可用，则使用请求参数（用于测试或管理场景）
        String cardNum = request.getParam("cardNum");
        if (cardNum == null || cardNum.isEmpty()) return null;
        return cardNum;
    }

    private Response getBalance(Request request) {
        String cardNum = resolveCardNumFromRequestOrSession(request);
        if (cardNum == null) return Response.Builder.forbidden("未登录或会话失效");
        try (var session = DatabaseManager.getSqlSessionFactory().openSession()) {
            var conn = session.getConnection();
            // 关联用户表取姓名
            String sql = "SELECT c.cardNum, c.balance, c.status, u.Name AS userName FROM tblCard c LEFT JOIN tblUser u ON c.cardNum = u.cardNum WHERE c.cardNum = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, cardNum);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        BigDecimal balance = rs.getBigDecimal("balance");
                        String status = rs.getString("status");
                        String userName = rs.getString("userName");
                        String expiryText = "未知";
                        String q = "SELECT MIN(Trans_time) AS first_time FROM tblCard_trans WHERE cardNum = ?";
                        try (PreparedStatement ps2 = conn.prepareStatement(q)) {
                            ps2.setString(1, cardNum);
                            try (ResultSet rs2 = ps2.executeQuery()) {
                                if (rs2.next()) {
                                    java.sql.Timestamp t = rs2.getTimestamp("first_time");
                                    if (t != null) {
                                        LocalDate created = t.toLocalDateTime().toLocalDate();
                                        LocalDate expiry = created.plusYears(4);
                                        expiryText = expiry.format(DateTimeFormatter.ISO_LOCAL_DATE);
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            log.warn("查询卡片最早交易时间失败, cardNum={}", cardNum, ex);
                        }
                        Map<String,Object> data = Map.of(
                                "cardNum", rs.getString("cardNum"),
                                "balance", balance,
                                "status", status,
                                "expiry", expiryText,
                                "name", userName // 增加姓名
                        );
                        return Response.Builder.success(data);
                    } else {
                        return Response.Builder.notFound("卡不存在: " + cardNum);
                    }
                }
            }
        } catch (Exception e) {
            log.error("查询一卡通余额失败，cardNum={}", cardNum, e);
            return Response.Builder.error("查询失败: " + e.getMessage());
        }
    }

    private Response recharge(Request request) {
        String amountStr = request.getParam("amount");
        if (amountStr == null || amountStr.isEmpty()) return Response.Builder.badRequest("缺少amount");
        double amount;
        try { amount = Double.parseDouble(amountStr); } catch (Exception e) { return Response.Builder.badRequest("amount格式非法"); }
        String cardNum = resolveCardNumFromRequestOrSession(request);
        if (cardNum == null) return Response.Builder.forbidden("未登录或会话失效");
        boolean ok = cardService.recharge(cardNum, amount);
        if (!ok) return Response.Builder.error("充值失败：卡不存在或更新失败");
        Card c = cardService.findByCardNum(cardNum);
        String userName = fetchUserName(cardNum);
        return Response.Builder.success("充值成功", Map.of(
                "cardNum", cardNum,
                "balance", c != null ? c.getBalance() : null,
                "name", userName
        ));
    }

    private Response consume(Request request) {
        String amountStr = request.getParam("amount");
        if (amountStr == null || amountStr.isEmpty()) return Response.Builder.badRequest("缺少amount");
        double amount;
        try { amount = Double.parseDouble(amountStr); } catch (Exception e) { return Response.Builder.badRequest("amount格式非法"); }
        String cardNum = resolveCardNumFromRequestOrSession(request);
        if (cardNum == null) return Response.Builder.forbidden("未登录或会话失效");
        boolean ok = cardService.consume(cardNum, amount);
        if (!ok) return Response.Builder.error("消费失败：余额不足或卡不存在");
        Card c = cardService.findByCardNum(cardNum);
        String userName = fetchUserName(cardNum);
        return Response.Builder.success("消费成功", Map.of(
                "cardNum", cardNum,
                "balance", c != null ? c.getBalance() : null,
                "name", userName
        ));
    }

    private Response transList(Request request) {
        String cardNum = resolveCardNumFromRequestOrSession(request);
        if (cardNum == null) return Response.Builder.forbidden("未登录或会话失效");
        try (var session = DatabaseManager.getSqlSessionFactory().openSession()) {
            var conn = session.getConnection();
            String sql = "SELECT cardNum, Trans_time, Trans_type, Amount FROM tblCard_trans WHERE cardNum = ? ORDER BY Trans_time DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, cardNum);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Map<String,Object>> list = new ArrayList<>();
                    while (rs.next()) {
                        Map<String,Object> m = new HashMap<>();
                        m.put("cardNum", rs.getString("cardNum"));
                        m.put("time", rs.getTimestamp("Trans_time"));
                        m.put("type", rs.getString("Trans_type"));
                        m.put("amount", rs.getBigDecimal("Amount"));
                        list.add(m);
                    }
                    return Response.Builder.success(list);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("查询交易记录失败", e);
        }
    }

    // 新增：根据 cardNum 查询用户姓名
    private String fetchUserName(String cardNum) {
        if (cardNum == null) return null;
        try (var session = DatabaseManager.getSqlSessionFactory().openSession()) {
            var conn = session.getConnection();
            try (PreparedStatement ps = conn.prepareStatement("SELECT Name FROM tblUser WHERE cardNum=? LIMIT 1")) {
                ps.setString(1, cardNum);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("Name");
                }
            }
        } catch (Exception e) {
            log.warn("查询用户名失败 cardNum={}", cardNum, e);
        }
        return null;
    }

    /**
     * 允许用户对自己的卡设置状态（例如：LOST / NORMAL）。
     * 优先使用会话中的 userId 作为 cardNum，避免客户端滥用。
     */
    private Response setStatus(Request request) {
        String cardNum = resolveCardNumFromRequestOrSession(request);
        if (cardNum == null) return Response.Builder.forbidden("未登录或会话失效");
        String status = request.getParam("status");
        if (status == null || status.isEmpty()) return Response.Builder.badRequest("缺少status参数");
        // 允许的状态值，可根据需要扩展或与数据库枚举对齐
        String input = status.trim();
        // 将常见的英文/中文状态映射为数据库中定义的枚举（中文）
        String dbStatus;
        if ("LOST".equalsIgnoreCase(input) || "挂失".equalsIgnoreCase(input) || input.toLowerCase().contains("lost")) {
            dbStatus = "挂失";
        } else if ("NORMAL".equalsIgnoreCase(input) || "正常".equalsIgnoreCase(input) || input.toLowerCase().contains("normal")) {
            dbStatus = "正常";
        } else if ("注销".equalsIgnoreCase(input) || "CANCEL".equalsIgnoreCase(input) || "DELETED".equalsIgnoreCase(input)) {
            dbStatus = "注销";
        } else {
            return Response.Builder.badRequest("不支持的status值: " + status);
        }

        try (var session = DatabaseManager.getSqlSessionFactory().openSession()) {
            var conn = session.getConnection();
            String sql = "UPDATE tblCard SET status = ? WHERE cardNum = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, dbStatus);
                ps.setString(2, cardNum);
                int updated = ps.executeUpdate();
                if (updated > 0) {
                    session.commit();
                    // 返回最新卡信息片段
                    Card c = cardService.findByCardNum(cardNum);
                    return Response.Builder.success("卡片状态更新成功", Map.of("cardNum", cardNum, "status", dbStatus));
                } else {
                    return Response.Builder.notFound("卡不存在: " + cardNum);
                }
            }
        } catch (Exception e) {
            log.error("设置卡状态失败, cardNum={}, status={}", cardNum, status, e);
            return Response.Builder.error("设置卡状态失败: " + e.getMessage());
        }
    }
}

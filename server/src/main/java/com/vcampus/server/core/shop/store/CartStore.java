package com.vcampus.server.core.shop.store;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

/**
 * 线程安全内存购物车：按 userId -> (productId -> qty)。
 * 仅存储数量，价格/名称在读取时动态查询，避免缓存脏数据。
 */
public class CartStore {
    private static final CartStore INSTANCE = new CartStore();
    private final ConcurrentHashMap<String, ConcurrentHashMap<Integer,Integer>> carts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> userLocks = new ConcurrentHashMap<>();
    private CartStore() {}
    public static CartStore getInstance() { return INSTANCE; }

    private ReentrantLock lockFor(String userId) {
        return userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
    }

    public void add(String userId, Integer productId, int delta) {
        if (userId == null || productId == null || delta <= 0) return;
        ReentrantLock lock = lockFor(userId); lock.lock();
        try {
            carts.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                    .merge(productId, delta, Integer::sum);
        } finally { lock.unlock(); }
    }

    public void setQty(String userId, Integer productId, int qty) {
        if (userId == null || productId == null) return;
        ReentrantLock lock = lockFor(userId); lock.lock();
        try {
            if (qty <= 0) {
                Optional.ofNullable(carts.get(userId)).ifPresent(m -> m.remove(productId));
            } else {
                carts.computeIfAbsent(userId, k -> new ConcurrentHashMap<>()).put(productId, qty);
            }
            Optional.ofNullable(carts.get(userId)).ifPresent(m -> { if (m.isEmpty()) carts.remove(userId); });
        } finally { lock.unlock(); }
    }

    public void remove(String userId, Collection<Integer> productIds) {
        if (userId == null || productIds == null || productIds.isEmpty()) return;
        ReentrantLock lock = lockFor(userId); lock.lock();
        try {
            var map = carts.get(userId);
            if (map == null) return;
            for (Integer pid : productIds) if (pid != null) map.remove(pid);
            if (map.isEmpty()) carts.remove(userId);
        } finally { lock.unlock(); }
    }

    public Map<Integer,Integer> snapshot(String userId) {
        var m = carts.get(userId);
        if (m == null) return Map.of();
        return new HashMap<>(m);
    }

    public int totalCount(String userId) {
        var m = carts.get(userId);
        if (m == null) return 0;
        return m.values().stream().mapToInt(i -> i == null?0:i).sum();
    }

    public void clear(String userId) {
        if (userId == null) return;
        ReentrantLock lock = lockFor(userId); lock.lock();
        try { carts.remove(userId); } finally { lock.unlock(); }
    }
}

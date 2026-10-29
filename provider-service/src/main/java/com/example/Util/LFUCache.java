package com.example.Util;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
public class LFUCache<K, V> {
    // key -> value
    private final Map<K, V> kvMap;
    // key -> 访问频次
    private final Map<K, Integer> keyFreqMap;
    // 频次 -> 同频次key集合，LinkedHashSet保证同频下先入先出（淘汰更早的）
    private final Map<Integer, LinkedHashSet<K>> freqKeysMap;

    private final int capacity;
    private int minFreq;

    public LFUCache(int capacity) {
        this.capacity = capacity;
        kvMap = new HashMap<>();
        keyFreqMap = new HashMap<>();
        freqKeysMap = new HashMap<>();
        minFreq = 0;
    }

    public V get(K key) {
        if (!kvMap.containsKey(key)) {
            return null;
        }
        increaseFreq(key);
        return kvMap.get(key);
    }

    public void put(K key, V value) {
        if (capacity <= 0) {
            return;
        }
        // key已存在，更新值，频次+1
        if (kvMap.containsKey(key)) {
            kvMap.put(key, value);
            increaseFreq(key);
            return;
        }
        // 容量满，淘汰最小频次
        if (kvMap.size() >= capacity) {
            removeMinFreqKey();
        }
        // 新增key，频次初始1
        kvMap.put(key, value);
        keyFreqMap.put(key, 1);
        freqKeysMap.computeIfAbsent(1, k -> new LinkedHashSet<>()).add(key);
        minFreq = 1;
    }

    private void increaseFreq(K key) {
        int oldFreq = keyFreqMap.get(key);
        LinkedHashSet<K> oldSet = freqKeysMap.get(oldFreq);
        oldSet.remove(key);

        // 如果旧频次集合空了，并且是最小频次，minFreq自增
        if (oldSet.isEmpty()) {
            freqKeysMap.remove(oldFreq);
            if (oldFreq == minFreq) {
                minFreq++;
            }
        }

        int newFreq = oldFreq + 1;
        keyFreqMap.put(key, newFreq);
        freqKeysMap.computeIfAbsent(newFreq, k -> new LinkedHashSet<>()).add(key);
    }

    private void removeMinFreqKey() {
        LinkedHashSet<K> minSet = freqKeysMap.get(minFreq);
        K deleteKey = minSet.iterator().next();
        minSet.remove(deleteKey);
        if (minSet.isEmpty()) {
            freqKeysMap.remove(minFreq);
        }
        kvMap.remove(deleteKey);
        keyFreqMap.remove(deleteKey);
    }

    // 测试
    public static void main(String[] args) {
        // 示例1：String key，String value
        LFUCache<String, String> lfuStr = new LFUCache<>(3);
        lfuStr.put("k1", "v1");
        lfuStr.put("k2", "v2");
        lfuStr.put("k2", "v2");
        lfuStr.put("k3", "v3");
        lfuStr.put("k4", "v4");
        System.out.println(lfuStr.get("k2"));

        // 示例2：自定义对象作为key（注意：作为HashMap的key，一定要重写equals和hashCode！）
        LFUCache<User, String> lfuObj = new LFUCache<>(2);
        User u1 = new User(1L, "zhangsan");
        lfuObj.put(u1, "用户数据1");
        System.out.println(lfuObj.get(u1));
    }

    static class User {
        private Long id;
        private String name;

        public User(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            User user = (User) o;
            return id.equals(user.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}

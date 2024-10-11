package com.dev.biz.service;

import com.dev.sdk.vector.VectorIdGenerator;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : gao
 * @date 2024年10月09日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
@Service
public class CacheServiceImpl implements CacheService {
    private ConcurrentHashMap<String, Map<String, String>> cache = new ConcurrentHashMap<>();

    @Override
    public String generateId(String question) throws NoSuchAlgorithmException {
        return VectorIdGenerator.deterministicUUID(question);
    }

    @Override
    public void set(String id, String key, String question) {
        if (cache.containsKey(id)) {
            cache.get(id).put(key, question);
        } else {
            Map<String, String> map = new HashMap<>();
            map.put(key, question);
            cache.put(id, map);
        }
    }

    @Override
    public String get(String id, String key) {
        return cache.get(id).get(key);
    }
}

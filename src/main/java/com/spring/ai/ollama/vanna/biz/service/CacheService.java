package com.spring.ai.ollama.vanna.biz.service;

import java.security.NoSuchAlgorithmException;

/**
 * @author : gao
 * @date 2024年10月09日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
public interface CacheService {

    String generateId(String question) throws NoSuchAlgorithmException;

    void set(String id, String key, String question);

    String get(String id, String key);
}

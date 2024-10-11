package com.dev.biz.service;

import com.dev.biz.response.VannaResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * @author : gao
 * @date 2024年10月09日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
public interface VnService {


    String generateSql(String question, boolean allowLlmToSeeData);

    ResponseEntity<?> runSql(String id, String sql);

    ResponseEntity<?> removeTrainingData(String id);

    String train(String question, String sql, String ddl, String documentation);
}

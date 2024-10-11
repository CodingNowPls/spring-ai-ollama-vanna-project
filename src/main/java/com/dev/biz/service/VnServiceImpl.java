package com.dev.biz.service;

import cn.hutool.json.JSONUtil;
import com.dev.biz.config.PromptConfig;
import com.dev.biz.response.VannaResponse;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.prompt.ChatOptionsBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : gao
 * @date 2024年10月09日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
@Service
@AllArgsConstructor
public class VnServiceImpl implements VnService {

    private final SqlProcessor sqlProcessor;
    private final PromptConfig promptConfig;

    @Override
    public String generateSql(String question, boolean allowLlmToSeeData) {
        return sqlProcessor.processQuestion(question, promptConfig.getArgsMap());
    }

    @Override
    public ResponseEntity<?> runSql(String id, String sql) {
        try {
            List<Map<String, Object>> df = sqlProcessor.runSql(sql);
            if (df == null) {
                return VannaResponse.of("type", "error", "error", "SQL执行失败").fail();
            }
            return VannaResponse.of("type", "df", "id", id, "df", JSONUtil.toJsonStr(df)).ok();
        } catch (Exception e) {
            e.printStackTrace();
            return VannaResponse.of("type", "error", "error", e.getMessage()).fail();
        }
    }

    /**
     * 移除训练数据
     *
     * @param id
     * @return
     */
    @Override
    public ResponseEntity<?> removeTrainingData(String id) {
        try {
            return VannaResponse.of("success", true).ok();
        } catch (Exception e) {
            e.printStackTrace();
            return VannaResponse.of("type", "error", "error", "不能移除训练数据").fail(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 添加训练数据
     *
     * @param question
     * @param sql
     * @param ddl
     * @param documentation
     * @return
     */
    @Override
    public String train(String question, String sql, String ddl, String documentation) {
        //添加到向量库，然后写入json文件
        return "";
    }


}

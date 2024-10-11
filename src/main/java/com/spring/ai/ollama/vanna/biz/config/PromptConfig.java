package com.spring.ai.ollama.vanna.biz.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : gao
 * @date 2024年10月10日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
@Configuration
public class PromptConfig {

    private Map<String, String> prompts = new HashMap<>();
    @Getter
    private final Map<String, Object> argsMap = new HashMap<>();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${ollama.modelName}")
    private String modelName;

    @PostConstruct
    public void init() {
        String dialect;
        try {
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName().toLowerCase();
            if (databaseProductName.contains("mysql")) {
                dialect = "mysql";
            } else if (databaseProductName.contains("oracle")) {
                dialect = "oracle";
            } else if (databaseProductName.contains("sqlserver")) {
                dialect = "sqlserver";
            } else if (databaseProductName.contains("postgresql")) {
                dialect = "postgresql";
            } else {
                dialect = "mysql";
            }
        } catch (SQLException e) {
            throw new RuntimeException("检索数据库方言时出错", e);
        }

        String INITIAL_PROMPT = """ 
                你是一个%s数据库专家，请用中文回答，你只能使用提供给你的sql语句,不可以乱写SQL列名与表名，假如需要加入查询条件，就拼接条件查询，如果列中出现了关键字，
                必须使用单引号把关键字括起来，把拼好的sql返回，列中的注释写了列值结果是kv分号分割的键值对，使用case返回注释中的枚举中文值，返回的列名用中文别名，
                如果查询数据量超过10条，就只返回前十条
                """.formatted(dialect);
        prompts.put("initial_prompt", INITIAL_PROMPT);


        argsMap.put("dialect", dialect);
        argsMap.put("model", modelName);
        argsMap.put("temperature", 0.7);
    }


    public String get(String key) {
        return prompts.get(key);
    }

}

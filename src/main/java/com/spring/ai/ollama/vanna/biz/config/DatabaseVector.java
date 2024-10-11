package com.spring.ai.ollama.vanna.biz.config;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DatabaseVector {

    public static Map<String, String[]> getDatabaseVector(String dbname) throws SQLException {
        // 获取表的元信息
        String tableQuery = "SELECT TABLE_NAME, TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='" + dbname + "'";
        List<Map<String, Object>> tableResults = runSQL(tableQuery);

        // 获取列的元信息
        String columnQuery = "SELECT TABLE_NAME, COLUMN_NAME, COLUMN_COMMENT, COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='" + dbname + "'";
        List<String> ddlStatements = new ArrayList<>();
        Map<String, String[]> questionsMapping = new HashMap<>();

        for (Map<String, Object> tableRow : tableResults) {
            String tableName = (String) tableRow.get("TABLE_NAME");
            String tableComment = (String) tableRow.get("TABLE_COMMENT");

            if (tableComment == null || tableComment.isEmpty()) {
                continue;
            }

            // 构建 CREATE TABLE 语句
            StringBuilder ddl = new StringBuilder("CREATE TABLE " + tableName + " ( ");

            List<Map<String, Object>> columnResults = runSQL(columnQuery + " AND TABLE_NAME = '" + tableName + "'");
            List<String> columnDefinitions = new ArrayList<>();
            List<String> selectStatements = new ArrayList<>();

            for (Map<String, Object> columnRow : columnResults) {
                String columnName = (String) columnRow.get("COLUMN_NAME");
                String columnComment = (String) columnRow.get("COLUMN_COMMENT");
                String columnType = (String) columnRow.get("COLUMN_TYPE");

                // 组装列定义
                StringBuilder columnDefinition = new StringBuilder("  `" + columnName + "` " + columnType);
                if (columnComment != null && !columnComment.isEmpty()) {
                    columnDefinition.append(" COMMENT '").append(columnComment).append("'");
                    selectStatements.add(columnName + " AS `" + columnComment + "`");
                } else {
                    selectStatements.add(columnName);
                }
                columnDefinitions.add(columnDefinition.toString());
            }

            // 拼接所有列定义
            ddl.append(String.join(", ", columnDefinitions));
            ddl.append(" ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3");

            // 如果有表注释，则添加
            if (tableComment != null && !tableComment.isEmpty()) {
                ddl.append(" COMMENT='").append(tableComment).append("'");
            }

            ddlStatements.add(ddl.toString());

            String question = "查询" + tableComment;
            String sqlQuery = "SELECT " + String.join(", ", selectStatements) + " FROM " + tableName + ";";
            questionsMapping.put(tableName, new String[]{ddl.toString(), question, sqlQuery});
        }

        return questionsMapping;
    }


    // 执行 SQL 查询
    private static List<Map<String, Object>> runSQL(String query) {
        JdbcTemplate jdbcTemplate = SpringUtil.getBean(JdbcTemplate.class);
        return jdbcTemplate.queryForList(query);
    }


    public static void appendToJsonFile(String filename, JSONObject jsonObject) {
        JSONArray dataArray;

        // 检查文件是否存在
        File file = new File(filename);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }

                dataArray = JSON.parseArray(content.toString());
                if (dataArray == null) {
                    dataArray = new JSONArray();
                }
            } catch (IOException e) {
                dataArray = new JSONArray();
            }
        } else {
            dataArray = new JSONArray();
        }


        dataArray.add(jsonObject);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(dataArray.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

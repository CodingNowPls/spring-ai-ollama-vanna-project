package com.spring.ai.ollama.vanna.biz.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.FileResource;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.spring.ai.ollama.vanna.sdk.vector.VectorIdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

import java.util.*;

/**
 * @author : gao
 * @date 2024年10月09日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
@Configuration
public class VectorDataBaseConfig {

    @Autowired
    private VectorStore vectorStore;

    @Value("${jsonFilePath}")
    private String jsonFilePath;


    @Value("${dbName}")
    private String dbName;


    private ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);

    @PostConstruct
    public void init() throws Exception {
        //读取json文件
        String jsonPath = System.getProperty("user.dir") + File.separator + jsonFilePath;
        FileResource resource = new FileResource(new File(jsonPath));
        String json = FileUtil.readString(resource.getFile(), "UTF-8");
        if (StrUtil.isEmpty(json)) {
            //读取数据库中的表数据，然后组成json，插入文件中，避免每次重启服务都重新插入数据
            Map<String, String[]> databaseVector = DatabaseVector.getDatabaseVector(dbName);
            JSONObject jsonObject;
            for (Map.Entry<String, String[]> entry : databaseVector.entrySet()) {
                String tableName = entry.getKey();
                String[] columns = entry.getValue();
                //String  ddl = columns[0];
                String question = columns[1];
                String querySql = columns[2];
                jsonObject = new JSONObject();
                jsonObject.put("question", question);
                jsonObject.put("sql", querySql);
                DatabaseVector.appendToJsonFile(jsonPath, jsonObject);
            }
            json = FileUtil.readString(resource.getFile(), "UTF-8");
        }
        if (StrUtil.isEmpty(json)) {
            return;
        }
        List<Map> list = JSONUtil.toList(json, Map.class);
        Map emptyMap = Collections.EMPTY_MAP;
        for (Map map : list) {
            String question = (String) map.get("question");
            String sql = (String) map.get("sql");
            // 生成唯一 ID
            String uuid = VectorIdGenerator.deterministicUUID(question);
            String jsonContent = objectMapper.writeValueAsString(map);
            // 创建 Document 对象
            Document document = new Document(uuid, jsonContent, emptyMap);
            vectorStore.add(List.of(document));
        }


    }

}

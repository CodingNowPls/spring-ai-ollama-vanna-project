package com.spring.ai.ollama.vanna.biz.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.spring.ai.ollama.vanna.biz.config.PromptConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptionsBuilder;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@AllArgsConstructor
public class SqlProcessor {

    private final PromptConfig promptConfig;

    private final VectorStore vectorStore;

    private final OllamaChatModel chatModel;

    private SqlPromptGenerator sqlPromptGenerator;

    private final JdbcTemplate jdbcTemplate;

    public String processQuestion(String question, Map<String, Object> kwargs) {
        String initialPrompt = null;
        if (this.promptConfig != null) {
            initialPrompt = this.promptConfig.get("initial_prompt");
        }
        log.info("question is:{}", question);
        List<String> questionSqlList = getSimilarQuestionSql(question, kwargs);
        if (CollectionUtils.isEmpty(questionSqlList)) {
            throw new RuntimeException("未找到关联的SQL");
        }
        String sqLPrompt = getSqlPrompt(
                initialPrompt, kwargs
        );

        //log.info("SQL Prompt:{}", sqLPrompt);
        // 构建消息日志
        List<Message> messageList = new ArrayList<>();
        messageList.add(crateSystemMessage(sqLPrompt));
        messageList.add(crateUserMessage(question));
        createAssistantMessage(questionSqlList, messageList);

        String llmResponse = submitPrompt(messageList, kwargs);
        log.info("LLM Response:{}", llmResponse);
        return extractSql(llmResponse);
    }

    private void createAssistantMessage(List<String> questionSqlList, List<Message> messageList) {
        if (CollectionUtils.isNotEmpty(questionSqlList)) {
            JSONObject obj = JSONUtil.parseObj(questionSqlList.get(0));
            String sql = (String) obj.get("sql");
            //Message message = new AssistantPromptTemplate("{sql}").createMessage(Map.of("sql", sql));
            messageList.add(new UserMessage(sql));
        }
    }

    private Message crateUserMessage(String question) {
        return new UserMessage(question);
    }

    private Message crateSystemMessage(String initialPrompt) {
        Message systemMessage = new SystemPromptTemplate(initialPrompt + "你的名字是{name}").createMessage(Map.of("name", "小摩"));

        return systemMessage;
    }

    // 假设这些方法在其他地方实现
    private List<String> getSimilarQuestionSql(String question, Map<String, Object> kwargs) {
        SearchRequest searchRequest = SearchRequest.query(question).withTopK(1).withSimilarityThreshold(0.5f);
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        log.info("getSimilarQuestionSql is:{}", documents);
        List<String> questionSqlList = documents.stream().map(Document::getContent).toList();
        return questionSqlList;
    }

    private List<String> getRelatedDdl(String question, Map<String, Object> kwargs) {
        return null;
    }

    private List<String> getRelatedDocumentation(String question, Map<String, Object> kwargs) {
        return null;
    }

    private String getSqlPrompt(String initialPrompt, Map<String, Object> kwargs) {
        return sqlPromptGenerator.getSqlPrompt(initialPrompt, kwargs);
    }


    private String submitPrompt(List<Message> messageList, Map<String, Object> kwargs) {
        String modelName = (String) kwargs.get("model");
        Double temperature = (Double) kwargs.get("temperature");
        var portableOptions = ChatOptionsBuilder.builder().withTemperature(temperature).withModel(modelName).build();
        Prompt prompt = new Prompt(messageList, portableOptions);
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getContent();
    }

    private String extractSql(String llmResponse) {
        // 使用正则表达式提取 SQL
        Pattern pattern = Pattern.compile("```sql(.+?)```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(llmResponse);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    /**
     * 执行 SQL 并返回结果
     *
     * @param sql
     * @return
     */
    public List<Map<String, Object>> runSql(String sql) {
        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String convertDataFrameToMarkdown(List<Map<String, Object>> df) {
        // 实现将 List<Map<String, Object>> 转换为 Markdown 表格
        // 可以遍历df并构建类似表格的字符串
        return null;
    }
}

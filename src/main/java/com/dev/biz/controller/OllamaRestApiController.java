package com.dev.biz.controller;

import cn.hutool.core.date.DateUtil;
import com.dev.sdk.api.ollama.OllamaApi;
import com.dev.sdk.api.ollama.OllamaApiClient;
import com.dev.sdk.api.ollama.chat.response.Message;
import com.dev.sdk.api.ollama.chat.response.ChatResponse;
import com.dev.sdk.api.ollama.chat.request.ChatRequest;
import com.dev.sdk.api.ollama.chat.request.GeneratorRequest;
import com.dev.sdk.api.ollama.chat.request.Messages;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptionsBuilder;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author : gao
 * @date 2024年09月29日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class OllamaRestApiController {

    private final OllamaApiClient apiClient;

    private final OllamaApi ollamaApi;

    private final OllamaChatModel chatModel;

    private final ObjectMapper objectMapper;


    @PostMapping(path = {"/chat"},
            consumes = MediaType.TEXT_EVENT_STREAM_VALUE,
            produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Mono<ChatResponse> chat(@RequestBody Flux<DataBuffer> chatRequestFlux) {

        return chatRequestFlux
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return Mono.just(new String(bytes, StandardCharsets.UTF_8));
                })

                .collectList()
                .map(segments -> {
                    // 合并所有的片段，生成完整的 JSON 字符串
                    String fullRequestBody = String.join("", segments);
                    ChatRequest request;
                    try {
                        request = parseChatRequest(fullRequestBody, ChatRequest.class);
                    } catch (JsonProcessingException e) {
                        // 捕获并记录解析异常
                        log.error("JSON 解析失败: " + e.getMessage());
                        throw new RuntimeException("解析请求失败，请确保输入的 JSON 格式正确");
                    }
                    String model = request.getModel();
                    List<org.springframework.ai.chat.messages.Message> messageList = new ArrayList<>();
                    org.springframework.ai.chat.messages.Message systemMessage = new SystemPromptTemplate("""
                            你是一个乐于助人的人工智能助手。你的名字是{name}。
                            你是一个帮助人们查找信息的人工智能助手。
                            你的名字是{name}你应该用你的名字以及回复用户的请求。用中文回答
                            """).createMessage(Map.of("name", "Vanna"));
                    messageList.add(systemMessage);

                    List<Messages> messages = request.getMessages();
                    for (Messages message : messages) {
                        UserMessage userMessage = new UserMessage(message.getContent());
                        messageList.add(userMessage);
                    }

                    var portableOptions = ChatOptionsBuilder.builder().withTemperature(0.7).withModel(model).build();
                    Prompt prompt = new Prompt(messageList, portableOptions);
                    org.springframework.ai.chat.model.ChatResponse response = chatModel.call(prompt);
                    String content = response.getResult().getOutput().getContent();
                    String now = DateUtil.now();
                    return ChatResponse.builder()
                            .done(false)
                            .createdAt(now)
                            .model(model)
                            .message(Message.builder().role("assistant").content(content).build())
                            .build();
                });
    }


    private <T> T parseChatRequest(String chatRequestString, Class<T> clazz) throws JsonProcessingException {
        // 解析 JSON 字符串到 ChatRequest 对象
        return objectMapper.readValue(chatRequestString, clazz);
    }


    @PostMapping(value = "/generate", consumes = MediaType.TEXT_EVENT_STREAM_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<OllamaApi.ChatResponse> generate(@RequestBody Flux<DataBuffer> chatRequestFlux) {
        return chatRequestFlux
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return Mono.just(new String(bytes, StandardCharsets.UTF_8));
                })

                .collectList()
                .map(segments -> {
                    // 合并所有的片段，生成完整的 JSON 字符串
                    String fullRequestBody = String.join("", segments);
                    GeneratorRequest request;
                    try {
                        request = parseChatRequest(fullRequestBody, GeneratorRequest.class);
                    } catch (JsonProcessingException e) {
                        // 捕获并记录解析异常
                        log.error("JSON 解析失败: " + e.getMessage());
                        throw new RuntimeException("解析请求失败，请确保输入的 JSON 格式正确");
                    }
                    String model = request.getModel();
                    OllamaApi.ChatRequest chatReq = OllamaApi.ChatRequest
                            .builder(model)
                            .withStream(false)
                            .withFormat("json")
                            .build();

                    OllamaApi.ChatResponse resp = ollamaApi.chat(chatReq);
                    return resp;
                });
    }


    @GetMapping("/tags")
    public OllamaApiClient.TagResponse tags() {
        return apiClient.getTags();
    }


    @GetMapping("/version")
    public OllamaApiClient.VersionResponse version() {
        return apiClient.getVersion();
    }


}


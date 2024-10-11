package com.dev;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * @author : gao
 * @date 2024年09月29日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
@SpringBootApplication
@Import(cn.hutool.extra.spring.SpringUtil.class)
public class OllamaRestApiApplication {
    @Value("${server.port}")
    private Long serverPort;

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(OllamaRestApiApplication.class, args);
        OllamaRestApiApplication application = context.getBean(OllamaRestApiApplication.class);
        // 打印端口
        System.out.println("http://localhost:" + application.serverPort);

    }

}

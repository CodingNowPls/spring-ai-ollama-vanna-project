package com.dev.biz.service;


import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SqlPromptGenerator {

    public String getSqlPrompt(
            String initialPrompt, Map<String, Object> kwargs) {
        if (initialPrompt == null) {
            Object dialect = kwargs.get("dialect");
            initialPrompt = """ 
                     你是一个%s专家,用中文回答.请帮助生成 SQL 查询来回答问题。您的回复应仅基于给定的上下文，
                    并遵循回复指南和格式说明.
                    """.formatted(dialect);
        }

        initialPrompt += """ 
                1. 如果提供的上下文足够，请生成有效的SQL查询，但不对问题进行任何解释
                2. 如果提供的上下文几乎足够，但需要了解特定列中的特定字符串，请生成中间SQL查询以查找该列中的不同字符串。在查询前面添加注释，说明 middle_sql
                3. 如果提供的上下文不足，请解释无法生成的原因。
                4. 请使用最相关的表。
                5. 如果之前已经问过并回答过该问题，请准确重复之前的答案。
                6. 确保输出SQL符合且可执行,并且没有语法错误。
                7. 只返回SQL语句，其他解释性的字符串都不需要，方便下文要执行SQL
                8. 不能自己创造SQL列，仅使用给你提供的SQL
                9. 生成的SQL 以 ```sql开头，以```结尾
                """;
        return initialPrompt;
    }
}

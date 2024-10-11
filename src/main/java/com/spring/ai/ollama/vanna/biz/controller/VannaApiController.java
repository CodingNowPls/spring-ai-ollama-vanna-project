package com.spring.ai.ollama.vanna.biz.controller;

import cn.hutool.core.io.resource.FileResource;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.spring.ai.ollama.vanna.biz.response.VannaResponse;
import com.spring.ai.ollama.vanna.biz.service.CacheService;
import com.spring.ai.ollama.vanna.biz.service.VnService;
import com.spring.ai.ollama.vanna.biz.util.VectorDataFileUtil;
import com.spring.ai.ollama.vanna.sdk.vector.simple.SimpleVectorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author : gao
 * @date 2024年10月09日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
@Slf4j
@RestController
@RequestMapping("/api/v0")
public class VannaApiController {

    private final CacheService cache;

    private final VnService vn;

    private SimpleVectorStore vectorStore;

    private final String jsonFilePath;

    public VannaApiController(CacheService cache, VnService vn, SimpleVectorStore vectorStore, @Value("${jsonFilePath}") String jsonFilePath) {
        this.cache = cache;
        this.vn = vn;
        this.vectorStore = vectorStore;
        this.jsonFilePath = jsonFilePath;
    }

    @PostMapping("/remove_training_data")
    public ResponseEntity<?> removeTrainingData(@RequestBody Map<String, String> request) {
        String id = request.get("id");
        // 检查 ID 是否为空
        if (id == null || id.isEmpty()) {
            return VannaResponse.of("type", "error", "error", "ID不能为空").fail();
        }
        // 从缓存中获取问题
        String question = cache.get(id, "question");
        // 从 JSON 文件中移除问题
        removeQuestionFromJson(question);
        // 移除训练数据
        return vn.removeTrainingData(id);

    }

    /**
     * @param question
     */
    private void removeQuestionFromJson(String question) {
        FileResource questionFilePath = VectorDataFileUtil.getQuestionFilePath(question);
    }

    @PostMapping("/train")
    public ResponseEntity<Map<String, Object>> addTrainingData(@RequestBody Map<String, String> request) {
        String sql = request.get("sql");
        String ddl = request.get("ddl");
        String documentation = request.get("documentation");

        try {
            // 初始化切割后的变量
            String firstPart, secondPart;
            String id = null;

            // 如果 sql 不为空，进行切割并设置 firstPart 为 question，secondPart 为 sql
            if (sql != null && !sql.isEmpty()) {
                //只分割一次
                String[] splitStrings = sql.split(",", 2);
                firstPart = splitStrings[0].trim();
                secondPart = splitStrings.length > 1 ? splitStrings[1].replace("\n", "").trim() : null;

                Map<String, String> sqlData = new HashMap<>();
                sqlData.put("question", firstPart);
                sqlData.put("sql", secondPart);
                appendToJsonFile(jsonFilePath, sqlData);
                // 调用 train 并传递 firstPart 作为 question, secondPart 作为 sql
                id = vn.train(firstPart, secondPart, null, null);
            }
            // 如果 ddl 不为空，进行切割并设置 firstPart 为 question，secondPart 为 ddl
            else if (ddl != null && !ddl.isEmpty()) {
                String[] splitStrings = ddl.split(",");
                firstPart = splitStrings[0].trim();
                secondPart = splitStrings.length > 1 ? splitStrings[1].trim() : null;

                // 写入建表ddl.json
                Map<String, String> ddlData = new HashMap<>();
                ddlData.put("question", firstPart);
                ddlData.put("ddl", secondPart);
                //appendToJsonFile(jsonFilePath + "/ddl.json", ddlData);
                id = vn.train(firstPart, null, secondPart, null);
            }
            // 如果 documentation 不为空，进行切割并设置 firstPart 为 question，secondPart 为 documentation
            else if (documentation != null && !documentation.isEmpty()) {
                String[] splitStrings = documentation.split(",");
                firstPart = splitStrings[0].trim();
                secondPart = splitStrings.length > 1 ? splitStrings[1].trim() : null;

                // 调用 train 并传递 firstPart 作为 question, secondPart 作为 documentation
                id = vn.train(firstPart, null, null, secondPart);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("TRAINING ERROR: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("type", "error");
            errorResponse.put("error", e.getMessage());
            return VannaResponse.ofMap(errorResponse).fail(HttpStatus.BAD_REQUEST);
        }
    }


    private void appendToJsonFile(String filename, Map<String, String> data) {

    }


    @GetMapping("/generate_followup_questions")
    public ResponseEntity<Object> generateFollowupQuestions(@RequestParam String id) {
        try {
            String[] questions = {
                    "按国家和地区统计销售总额",
                    "按产品类别和月份统计销售额",
                    "显示每个客户的购买频率",
                    "列出所有退货的订单详情",
                    "哪些产品的销量超过100件"
            };
            String header = "\u60a8\u53ef\u4ee5\u95ee\u4e00\u4e9b\u540e\u7eed\u95ee\u9898:";
            VannaResponse vannaResponse = VannaResponse.of("header", header, "id", id, "type", "question_list", "questions", questions);
            return vannaResponse.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return VannaResponse.of("type", "error", "error", e.getMessage()).fail();
        }
    }


    @GetMapping("/generate_plotly_figure")
    public ResponseEntity<Object> generatePlotlyFigure(@RequestParam String id) {
        try {
            String fig = "{\"data\":[{\"hovertemplate\":\"id=%{x}\\u003cbr\\u003eqty=%{y}\\u003cextra\\u003e\\u003c\\u002fextra\\u003e\",\"legendgroup\":\"\",\"marker\":{\"color\":\"#636efa\",\"symbol\":\"circle\"},\"mode\":\"markers\",\"name\":\"\",\"orientation\":\"v\",\"showlegend\":false,\"x\":[794486627475914752,794487060353253376,794487061980643328,794487225025822720,794487394140160000,794487395838853120,794487566186315776,794487662940520448,794487992994496512,794487994810630144],\"xaxis\":\"x\",\"y\":[500.0,300.0,200.0,500.0,100.0,400.0,400.0,3000.0,666.0,334.0],\"yaxis\":\"y\",\"type\":\"scatter\"}],\"layout\":{\"template\":{\"data\":{\"histogram2dcontour\":[{\"type\":\"histogram2dcontour\",\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"},\"colorscale\":[[0.0,\"#0d0887\"],[0.1111111111111111,\"#46039f\"],[0.2222222222222222,\"#7201a8\"],[0.3333333333333333,\"#9c179e\"],[0.4444444444444444,\"#bd3786\"],[0.5555555555555556,\"#d8576b\"],[0.6666666666666666,\"#ed7953\"],[0.7777777777777778,\"#fb9f3a\"],[0.8888888888888888,\"#fdca26\"],[1.0,\"#f0f921\"]]}],\"choropleth\":[{\"type\":\"choropleth\",\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"}}],\"histogram2d\":[{\"type\":\"histogram2d\",\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"},\"colorscale\":[[0.0,\"#0d0887\"],[0.1111111111111111,\"#46039f\"],[0.2222222222222222,\"#7201a8\"],[0.3333333333333333,\"#9c179e\"],[0.4444444444444444,\"#bd3786\"],[0.5555555555555556,\"#d8576b\"],[0.6666666666666666,\"#ed7953\"],[0.7777777777777778,\"#fb9f3a\"],[0.8888888888888888,\"#fdca26\"],[1.0,\"#f0f921\"]]}],\"heatmap\":[{\"type\":\"heatmap\",\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"},\"colorscale\":[[0.0,\"#0d0887\"],[0.1111111111111111,\"#46039f\"],[0.2222222222222222,\"#7201a8\"],[0.3333333333333333,\"#9c179e\"],[0.4444444444444444,\"#bd3786\"],[0.5555555555555556,\"#d8576b\"],[0.6666666666666666,\"#ed7953\"],[0.7777777777777778,\"#fb9f3a\"],[0.8888888888888888,\"#fdca26\"],[1.0,\"#f0f921\"]]}],\"heatmapgl\":[{\"type\":\"heatmapgl\",\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"},\"colorscale\":[[0.0,\"#0d0887\"],[0.1111111111111111,\"#46039f\"],[0.2222222222222222,\"#7201a8\"],[0.3333333333333333,\"#9c179e\"],[0.4444444444444444,\"#bd3786\"],[0.5555555555555556,\"#d8576b\"],[0.6666666666666666,\"#ed7953\"],[0.7777777777777778,\"#fb9f3a\"],[0.8888888888888888,\"#fdca26\"],[1.0,\"#f0f921\"]]}],\"contourcarpet\":[{\"type\":\"contourcarpet\",\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"}}],\"contour\":[{\"type\":\"contour\",\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"},\"colorscale\":[[0.0,\"#0d0887\"],[0.1111111111111111,\"#46039f\"],[0.2222222222222222,\"#7201a8\"],[0.3333333333333333,\"#9c179e\"],[0.4444444444444444,\"#bd3786\"],[0.5555555555555556,\"#d8576b\"],[0.6666666666666666,\"#ed7953\"],[0.7777777777777778,\"#fb9f3a\"],[0.8888888888888888,\"#fdca26\"],[1.0,\"#f0f921\"]]}],\"surface\":[{\"type\":\"surface\",\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"},\"colorscale\":[[0.0,\"#0d0887\"],[0.1111111111111111,\"#46039f\"],[0.2222222222222222,\"#7201a8\"],[0.3333333333333333,\"#9c179e\"],[0.4444444444444444,\"#bd3786\"],[0.5555555555555556,\"#d8576b\"],[0.6666666666666666,\"#ed7953\"],[0.7777777777777778,\"#fb9f3a\"],[0.8888888888888888,\"#fdca26\"],[1.0,\"#f0f921\"]]}],\"mesh3d\":[{\"type\":\"mesh3d\",\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"}}],\"scatter\":[{\"fillpattern\":{\"fillmode\":\"overlay\",\"size\":10,\"solidity\":0.2},\"type\":\"scatter\"}],\"parcoords\":[{\"type\":\"parcoords\",\"line\":{\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"}}}],\"scatterpolargl\":[{\"type\":\"scatterpolargl\",\"marker\":{\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"}}}],\"bar\":[{\"error_x\":{\"color\":\"#2a3f5f\"},\"error_y\":{\"color\":\"#2a3f5f\"},\"marker\":{\"line\":{\"color\":\"#E5ECF6\",\"width\":0.5},\"pattern\":{\"fillmode\":\"overlay\",\"size\":10,\"solidity\":0.2}},\"type\":\"bar\"}],\"scattergeo\":[{\"type\":\"scattergeo\",\"marker\":{\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"}}}],\"scatterpolar\":[{\"type\":\"scatterpolar\",\"marker\":{\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"}}}],\"histogram\":[{\"marker\":{\"pattern\":{\"fillmode\":\"overlay\",\"size\":10,\"solidity\":0.2}},\"type\":\"histogram\"}],\"scattergl\":[{\"type\":\"scattergl\",\"marker\":{\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"}}}],\"scatter3d\":[{\"type\":\"scatter3d\",\"line\":{\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"}},\"marker\":{\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"}}}],\"scattermapbox\":[{\"type\":\"scattermapbox\",\"marker\":{\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"}}}],\"scatterternary\":[{\"type\":\"scatterternary\",\"marker\":{\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"}}}],\"scattercarpet\":[{\"type\":\"scattercarpet\",\"marker\":{\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"}}}],\"carpet\":[{\"aaxis\":{\"endlinecolor\":\"#2a3f5f\",\"gridcolor\":\"white\",\"linecolor\":\"white\",\"minorgridcolor\":\"white\",\"startlinecolor\":\"#2a3f5f\"},\"baxis\":{\"endlinecolor\":\"#2a3f5f\",\"gridcolor\":\"white\",\"linecolor\":\"white\",\"minorgridcolor\":\"white\",\"startlinecolor\":\"#2a3f5f\"},\"type\":\"carpet\"}],\"table\":[{\"cells\":{\"fill\":{\"color\":\"#EBF0F8\"},\"line\":{\"color\":\"white\"}},\"header\":{\"fill\":{\"color\":\"#C8D4E3\"},\"line\":{\"color\":\"white\"}},\"type\":\"table\"}],\"barpolar\":[{\"marker\":{\"line\":{\"color\":\"#E5ECF6\",\"width\":0.5},\"pattern\":{\"fillmode\":\"overlay\",\"size\":10,\"solidity\":0.2}},\"type\":\"barpolar\"}],\"pie\":[{\"automargin\":true,\"type\":\"pie\"}]},\"layout\":{\"autotypenumbers\":\"strict\",\"colorway\":[\"#636efa\",\"#EF553B\",\"#00cc96\",\"#ab63fa\",\"#FFA15A\",\"#19d3f3\",\"#FF6692\",\"#B6E880\",\"#FF97FF\",\"#FECB52\"],\"font\":{\"color\":\"#2a3f5f\"},\"hovermode\":\"closest\",\"hoverlabel\":{\"align\":\"left\"},\"paper_bgcolor\":\"white\",\"plot_bgcolor\":\"#E5ECF6\",\"polar\":{\"bgcolor\":\"#E5ECF6\",\"angularaxis\":{\"gridcolor\":\"white\",\"linecolor\":\"white\",\"ticks\":\"\"},\"radialaxis\":{\"gridcolor\":\"white\",\"linecolor\":\"white\",\"ticks\":\"\"}},\"ternary\":{\"bgcolor\":\"#E5ECF6\",\"aaxis\":{\"gridcolor\":\"white\",\"linecolor\":\"white\",\"ticks\":\"\"},\"baxis\":{\"gridcolor\":\"white\",\"linecolor\":\"white\",\"ticks\":\"\"},\"caxis\":{\"gridcolor\":\"white\",\"linecolor\":\"white\",\"ticks\":\"\"}},\"coloraxis\":{\"colorbar\":{\"outlinewidth\":0,\"ticks\":\"\"}},\"colorscale\":{\"sequential\":[[0.0,\"#0d0887\"],[0.1111111111111111,\"#46039f\"],[0.2222222222222222,\"#7201a8\"],[0.3333333333333333,\"#9c179e\"],[0.4444444444444444,\"#bd3786\"],[0.5555555555555556,\"#d8576b\"],[0.6666666666666666,\"#ed7953\"],[0.7777777777777778,\"#fb9f3a\"],[0.8888888888888888,\"#fdca26\"],[1.0,\"#f0f921\"]],\"sequentialminus\":[[0.0,\"#0d0887\"],[0.1111111111111111,\"#46039f\"],[0.2222222222222222,\"#7201a8\"],[0.3333333333333333,\"#9c179e\"],[0.4444444444444444,\"#bd3786\"],[0.5555555555555556,\"#d8576b\"],[0.6666666666666666,\"#ed7953\"],[0.7777777777777778,\"#fb9f3a\"],[0.8888888888888888,\"#fdca26\"],[1.0,\"#f0f921\"]],\"diverging\":[[0,\"#8e0152\"],[0.1,\"#c51b7d\"],[0.2,\"#de77ae\"],[0.3,\"#f1b6da\"],[0.4,\"#fde0ef\"],[0.5,\"#f7f7f7\"],[0.6,\"#e6f5d0\"],[0.7,\"#b8e186\"],[0.8,\"#7fbc41\"],[0.9,\"#4d9221\"],[1,\"#276419\"]]},\"xaxis\":{\"gridcolor\":\"white\",\"linecolor\":\"white\",\"ticks\":\"\",\"title\":{\"standoff\":15},\"zerolinecolor\":\"white\",\"automargin\":true,\"zerolinewidth\":2},\"yaxis\":{\"gridcolor\":\"white\",\"linecolor\":\"white\",\"ticks\":\"\",\"title\":{\"standoff\":15},\"zerolinecolor\":\"white\",\"automargin\":true,\"zerolinewidth\":2},\"scene\":{\"xaxis\":{\"backgroundcolor\":\"#E5ECF6\",\"gridcolor\":\"white\",\"linecolor\":\"white\",\"showbackground\":true,\"ticks\":\"\",\"zerolinecolor\":\"white\",\"gridwidth\":2},\"yaxis\":{\"backgroundcolor\":\"#E5ECF6\",\"gridcolor\":\"white\",\"linecolor\":\"white\",\"showbackground\":true,\"ticks\":\"\",\"zerolinecolor\":\"white\",\"gridwidth\":2},\"zaxis\":{\"backgroundcolor\":\"#E5ECF6\",\"gridcolor\":\"white\",\"linecolor\":\"white\",\"showbackground\":true,\"ticks\":\"\",\"zerolinecolor\":\"white\",\"gridwidth\":2}},\"shapedefaults\":{\"line\":{\"color\":\"#2a3f5f\"}},\"annotationdefaults\":{\"arrowcolor\":\"#2a3f5f\",\"arrowhead\":0,\"arrowwidth\":1},\"geo\":{\"bgcolor\":\"white\",\"landcolor\":\"#E5ECF6\",\"subunitcolor\":\"white\",\"showland\":true,\"showlakes\":true,\"lakecolor\":\"white\"},\"title\":{\"x\":0.05},\"mapbox\":{\"style\":\"light\"}}},\"xaxis\":{\"anchor\":\"y\",\"domain\":[0.0,1.0],\"title\":{\"text\":\"id\"}},\"yaxis\":{\"anchor\":\"x\",\"domain\":[0.0,1.0],\"title\":{\"text\":\"qty\"}},\"legend\":{\"tracegroupgap\":0},\"margin\":{\"t\":60}}}";
            VannaResponse vannaResponse = VannaResponse.of("fig", fig, "id", id, "type", "plotly_figure");
            return vannaResponse.ok();
        } catch (Exception e) {
            // 打印堆栈跟踪
            e.printStackTrace();
            return VannaResponse.of("type", "error", "error", e.getMessage()).fail();
        }
    }


    @GetMapping("/load_question")
    public ResponseEntity<?> loadQuestion(@RequestParam String id,
                                          @RequestParam String question,
                                          @RequestParam String sql,
                                          @RequestParam String dfJson,
                                          @RequestParam String figJson,
                                          @RequestParam String followupQuestions) {
        try {

            VannaResponse response = VannaResponse.of(
                    "type", "question_cache"
                    , "id", id
                    , "question", question
                    , "sql", sql
                    , "df", dfJson
                    , "fig_json", figJson
                    , "followup_questions", followupQuestions);
            return response.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return VannaResponse.of("type", "error", "error", e.getMessage()).fail();
        }

    }

    @GetMapping("/download_csv/{id}")
    public ResponseEntity<byte[]> downloadCsv(@RequestParam String id) {
        String question = cache.get(id, "question");
        if (question == null || question.isEmpty()) {
            question = id;
        }
        String csvContent = null;

        // 使用 UTF-8 编码的文件名
        String filename = new String((question + ".csv").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);

        // 返回 CSV 文件
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.TEXT_PLAIN)
                .body(csvContent.getBytes(StandardCharsets.UTF_8));
    }

    // 将 DataFrame 转换为 CSV 字符串的示例方法
    private String convertDfToCsv(Object df) {
        // 这里需要根据具体的 DataFrame 类型实现转换逻辑
        // 示例返回一个简单的 CSV 字符串
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            // 生成 CSV 内容
            writer.write("Column1,Column2\n");
            writer.write("Value1,Value2\n");
            writer.flush();
            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error while converting DataFrame to CSV", e);
        }
    }


    @GetMapping("/get_question_history")
    public ResponseEntity<?> getQuestionHistory() {
        //return jsonify({"type": "question_history", "questions": cache.get_all(field_list=['question'])})
        VannaResponse response = VannaResponse.of("questions", Collections.EMPTY_LIST, "type", "question_history");
        return response.ok();
    }

    @GetMapping("/get_training_data")
    public ResponseEntity<?> getTrainingData() {
        try {
            Set<Map.Entry<String, Document>> entrySet = vectorStore.getAllData().entrySet();
            List<Map<String, Object>> list = Optional.of(entrySet)
                    .filter(set -> !set.isEmpty())
                    .map(set -> set.stream().map(entry -> {
                        String key = entry.getKey();
                        Document document = entry.getValue();
                        JSONObject jsonObject = JSON.parseObject(document.getContent());
                        String question = (String) jsonObject.get("question");
                        String sql = (String) jsonObject.get("sql");
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", key);
                        map.put("question", question);
                        map.put("content", sql);
                        map.put("training_data_type", "sql");
                        return map;
                    }).collect(Collectors.toList()))
                    .orElseGet(ArrayList::new);

            VannaResponse response = VannaResponse.of("id", "training_data", "type", "df", "df", JSONUtil.toJsonStr(list));
            return response.ok();
        } catch (Exception e) {
            return VannaResponse.of("type", "error", "error", e.getMessage()).fail();
        }
    }


    @GetMapping("/generate_sql")
    public ResponseEntity<?> generateSql(@RequestParam("question") String question) throws NoSuchAlgorithmException {
        if (question == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse("没有提供问题"));
        }
        try {
            String id = cache.generateId(question);
            String sql = vn.generateSql(question, true);
            if (StrUtil.isEmpty(sql)) {
                return VannaResponse.of("type", "error", "error", "无法生成SQL").fail();
            }
            cache.set(id, "question", question);
            cache.set(id, "sql", sql);

            VannaResponse response = VannaResponse.of("id", id, "text", sql, "type", "sql");
            return response.ok();
        } catch (Exception e) {
            return VannaResponse.of("type", "error", "error", e.getMessage()).fail();
        }

    }

    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("type", "error");
        errorResponse.put("error", errorMessage);
        return errorResponse;
    }

    @GetMapping("/run_sql")
    public ResponseEntity<?> runSql(@RequestParam("id") String id, @RequestParam(value = "sql", required = false) String sql) {
        try {
            sql = cache.get(id, "sql");
            return vn.runSql(id, sql);
        } catch (Exception e) {
            e.printStackTrace();
            return VannaResponse.of("type", "error", "error", e.getMessage()).fail();
        }
    }
}

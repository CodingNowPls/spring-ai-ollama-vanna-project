package com.spring.ai.ollama.vanna.biz.response;


import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : gao
 * @date 2024年10月09日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
public class VannaResponse extends HashMap<Object, Object> {

    public static VannaResponse of(Object... args) {
        VannaResponse builder = builder();
        for (int i = 0; i < args.length - 1; i += 2) {
            builder.put(args[i].toString(), args[i + 1]);
        }

        return builder;

    }

    public static VannaResponse ofMap(Map map) {
        VannaResponse builder = builder();
        builder.putAll(map);
        return builder;

    }

    public ResponseEntity ok() {
        return ResponseEntity.ok(this);
    }

    /**
     * 会携带of参数
     *
     * @param status
     * @return
     */
    public ResponseEntity fail(HttpStatusCode status) {
        return ResponseEntity.status(status)
                .body(this);
    }


    public ResponseEntity fail() {
        return ok();
    }

    public ResponseEntity failWithoutOf(HttpStatusCode status) {
        return ResponseEntity.status(status).build();
    }

    public ResponseEntity fail(HttpStatusCode status, Object... args) {
        Map<Object, Object> failParams = new HashMap<>();
        for (int i = 0; i < args.length - 1; i += 2) {
            failParams.put(args[i].toString(), args[i + 1]);
        }
        return ResponseEntity.status(status)
                .body(failParams);
    }

    public static VannaResponse builder() {
        return builder(new HashMap<>());
    }

    public static VannaResponse builder(Map<? extends String, ?> map) {
        if (map != null) {
            map = new HashMap<>();
        }
        return new VannaResponse(map);
    }

    public VannaResponse set(Object key, Object value) {
        this.put(key, value);
        return this;
    }

    @Override
    public VannaResponse put(Object key, Object value) {
        super.put(key, value);
        return this;
    }


    @Override
    public VannaResponse remove(Object key) {
        super.remove(key);
        return this;
    }


    private VannaResponse(Map<? extends String, ?> map) {
        super(map);
    }

}

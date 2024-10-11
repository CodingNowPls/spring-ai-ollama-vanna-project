package com.spring.ai.ollama.vanna.biz.util;

import cn.hutool.core.io.resource.FileResource;

import java.io.File;

/**
 * @author : gao
 * @date 2024年10月11日
 * -------------------------------------------<br>
 * <br>
 * <br>
 */
public class VectorDataFileUtil {


    public static FileResource getQuestionFilePath(String jsonFilePath) {
        String jsonPath = System.getProperty("user.dir") + File.separator + jsonFilePath;
        FileResource resource = new FileResource(new File(jsonPath));
        return resource;
    }
}

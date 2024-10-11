package com.dev.sdk.vector;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * @author gao
 */
public class VectorIdGenerator {

    public static String deterministicUUID(Object content) throws NoSuchAlgorithmException {
        // 创建 SHA-256 哈希对象
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] contentBytes;
        // 确定内容类型并转换为字节数组
        if (content instanceof String) {
            contentBytes = ((String) content).getBytes(StandardCharsets.UTF_8);
        } else if (content instanceof byte[]) {
            contentBytes = (byte[]) content;
        } else {
            throw new IllegalArgumentException("Content type " + content.getClass().getName() + " not supported!");
        }
        // 计算 SHA-256 哈希值
        byte[] hashBytes = digest.digest(contentBytes);
        // 将哈希值转换为十六进制字符串
        StringBuilder hashHex = new StringBuilder();
        for (byte b : hashBytes) {
            hashHex.append(String.format("%02x", b));
        }
        // 使用命名空间生成 UUID
        String uuidWithDashes = UUID.nameUUIDFromBytes(hashHex.toString().getBytes(StandardCharsets.UTF_8)).toString();
        return uuidWithDashes.replace("-", "");
    }

}

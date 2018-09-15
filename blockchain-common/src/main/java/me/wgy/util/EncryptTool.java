package me.wgy.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 用SHA256计算Hash值
 *
 * @author wgy
 * @date 2018/9/4
 */
public class EncryptTool {
    /**
     * 计算Hash值
     * 将传入参数转换成哈希值返回
     */
    public static String applySha256(String input) {
        try {
            //通过MessageDigest来使用SHA256加密算法
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            //对输入input使用 sha256 算法
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();

            for (byte aHash : hash) {
                String hex = Integer.toHexString(0xff & aHash);
                if (hex.length() == 1) {
                    hexString.append('0');
                } else {
                    hexString.append(hex);
                }
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

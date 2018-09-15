package me.wgy.util;

import com.google.gson.GsonBuilder;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

/**
 * String 工具类
 *
 * @author wgy
 * @date 2018/9/4
 */
public class StringTool {

  /**
   * 通过GSON库转换成JSON
   */
  public static String getJson(Object o) {
    return new GsonBuilder().setPrettyPrinting().create().toJson(o);
  }

  /**
   * 创建一个用 difficulty * "0" 组成的字符串
   *
   * @param difficulty 挖矿复杂度
   */
  public static String getDificultyString(int difficulty) {
    return new String(new char[difficulty]).replace('\0', '0');
  }

  /**
   * 生成ECDSA签名 接收发送方的私钥和字符串输入，对其进行签名并返回字节数组
   */
  public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
    Signature dsa;
    byte[] output;
    //output = new byte[0];
    try {
      // 数字签名算法ECDSA
      dsa = Signature.getInstance("ECDSA", "BC");
      dsa.initSign(privateKey);
      byte[] strByte = input.getBytes();
      dsa.update(strByte);
      output = dsa.sign();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return output;
  }

  /**
   * 验证签名 接受公钥、字符串数据、签名，如果签名是有效的，则返回true，否则false
   */
  public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
    try {
      Signature ecdsaVerify = Signature.getInstance("ECDSA", "BC");
      ecdsaVerify.initVerify(publicKey);
      ecdsaVerify.update(data.getBytes());
      return ecdsaVerify.verify(signature);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 把Key转换为String
   */
  public static String getStringFromKey(Key key) {
    return Base64.getEncoder().encodeToString(key.getEncoded());
  }

}

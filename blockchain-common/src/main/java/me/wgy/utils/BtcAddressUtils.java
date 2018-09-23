package me.wgy.utils;

import java.util.Arrays;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;

/**
 * 地址工具类
 *
 * @author wgy
 * @date 2018/9/23
 */
public class BtcAddressUtils {

  /**
   * 双重Hash
   */
  public static byte[] doubleHash(byte[] data) {
    return DigestUtils.sha256(DigestUtils.sha256(data));
  }

  /**
   * 计算公钥的 RIPEMD160 Hash值
   *
   * @param pubKey 公钥
   * @return ipeMD160Hash(sha256 ( pubkey))
   */
  public static byte[] ripeMD160Hash(byte[] pubKey) {
    //1. 先对公钥做 sha256 处理
    byte[] shaHashedKey = DigestUtils.sha256(pubKey);
    RIPEMD160Digest ripemd160 = new RIPEMD160Digest();
    ripemd160.update(shaHashedKey, 0, shaHashedKey.length);
    byte[] output = new byte[ripemd160.getDigestSize()];
    ripemd160.doFinal(output, 0);
    return output;
  }

  /**
   * 生成公钥的校验码
   */
  public static byte[] checksum(byte[] payload) {
    return Arrays.copyOfRange(doubleHash(payload), 0, 4);
  }
}

package me.wgy.transaction.utxo.model;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.wgy.utils.Base58Check;

/**
 * 交易输出
 *
 * @author wgy
 * @date 2018/9/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TXOutput {

  /**
   * 数值
   */
  private int value;
  /**
   * 公钥Hash
   */
  private byte[] pubKeyHash;


  /**
   * 创建交易输出
   */
  public static TXOutput createTXOutput(int value, String address) {
    // 反向转化为 byte 数组
    byte[] versionedPayload = Base58Check.base58ToBytes(address);
    byte[] pubKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);
    return new TXOutput(value, pubKeyHash);
  }

  /**
   * 检查交易输出是否能够使用指定的公钥
   */
  public boolean isLockedWithKey(byte[] pubKeyHash) {
    return Arrays.equals(this.getPubKeyHash(), pubKeyHash);
  }
}

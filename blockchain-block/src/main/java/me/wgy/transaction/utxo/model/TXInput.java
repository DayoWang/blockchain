package me.wgy.transaction.utxo.model;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.wgy.utils.BtcAddressUtils;

/**
 * 交易输入
 *
 * @author wgy
 * @date 2018/9/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TXInput {

  /**
   * 交易Id的hash值
   */
  private byte[] txId;
  /**
   * 交易输出索引
   */
  private int txOutputIndex;
  /**
   * 签名
   */
  private byte[] signature;
  /**
   * 公钥
   */
  private byte[] pubKey;


  /**
   * 检查公钥hash是否用于交易输入
   */
  public boolean usesKey(byte[] pubKeyHash) {
    byte[] lockingHash = BtcAddressUtils.ripeMD160Hash(this.getPubKey());
    return Arrays.equals(lockingHash, pubKeyHash);
  }
}

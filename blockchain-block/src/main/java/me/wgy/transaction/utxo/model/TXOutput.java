package me.wgy.transaction.utxo.model;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.wgy.transaction.script.model.Script;
import me.wgy.transaction.script.model.ScriptBuilder;
import me.wgy.utils.BtcAddressUtils;

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
   * p2pkh脚本
   */
  private Script p2pkhScript;

  public TXOutput(int value, byte[] pubKeyHash) {
    this.value = value;
    this.pubKeyHash = pubKeyHash;
    this.p2pkhScript = ScriptBuilder.createOutputScript(pubKeyHash);
  }

  /**
   * 创建交易输出
   */
  public static TXOutput createTXOutput(int value, String address) {
    byte[] pubKeyHash = BtcAddressUtils.getRipeMD160Hash(address);
    Script p2pkhScript = ScriptBuilder.createOutputScript(pubKeyHash);
    return new TXOutput(value, pubKeyHash, p2pkhScript);
  }

  /**
   * 检查交易输出是否能够使用指定的公钥
   */
  public boolean isLockedWithKey(byte[] pubKeyHash) {
    return Arrays.equals(this.getPubKeyHash(), pubKeyHash);
  }
}

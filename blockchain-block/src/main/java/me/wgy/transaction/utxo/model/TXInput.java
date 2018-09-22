package me.wgy.transaction.utxo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
   * 解锁脚本
   */
  private String scriptSig;


  /**
   * 判断解锁数据是否能够解锁交易输出
   */
  public boolean canUnlockOutputWith(String unlockingData) {
    return this.getScriptSig().endsWith(unlockingData);
  }
}

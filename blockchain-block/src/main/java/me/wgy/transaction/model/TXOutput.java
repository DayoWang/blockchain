package me.wgy.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
   * 锁定脚本
   */
  private String scriptPubKey;


  /**
   * 判断解锁数据是否能够解锁交易输出
   */
  public boolean canBeUnlockedWith(String unlockingData) {
    return this.getScriptPubKey().endsWith(unlockingData);
  }
}

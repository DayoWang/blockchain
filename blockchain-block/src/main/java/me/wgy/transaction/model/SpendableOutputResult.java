package me.wgy.transaction.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询结果
 *
 * @author wgy
 * @date 2018/9/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpendableOutputResult {

  /**
   * 交易时的支付金额
   */
  private int accumulated;
  /**
   * 未花费的交易
   */
  private Map<String, int[]> unspentOuts;
}

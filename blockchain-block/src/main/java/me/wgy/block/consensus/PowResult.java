package me.wgy.block.consensus;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 工作量计算结果
 *
 * @author wgy
 * @date 2018/9/15
 */
@AllArgsConstructor
@Data
public class PowResult {

  /**
   * 计数器
   */
  private long nonce;
  /**
   * hash值
   */
  private String hash;

}

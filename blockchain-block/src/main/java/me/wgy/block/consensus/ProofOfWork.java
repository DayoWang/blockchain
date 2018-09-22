package me.wgy.block.consensus;

import java.math.BigInteger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.wgy.block.model.Block;
import me.wgy.utils.ByteUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 工作量证明
 *
 * @author wgy
 * @date 2018/9/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProofOfWork {

  /**
   * 难度目标位
   */
  public static final int TARGET_BITS = 20;

  /**
   * 区块
   */
  private Block block;
  /**
   * 难度目标值
   */
  private BigInteger target;

  /**
   * 创建新的工作量证明，设定难度目标值
   * <p>
   * 对1进行移位运算，将1向左移动 (256 - TARGET_BITS) 位，得到我们的难度目标值
   */
  public static ProofOfWork createProofOfWork(Block block) {
    BigInteger targetValue = BigInteger.ONE.shiftLeft((256 - TARGET_BITS));
    return new ProofOfWork(block, targetValue);
  }

  /**
   * 运行工作量证明，开始挖矿，找到小于难度目标值的Hash
   */
  public PowResult run() {
    long nonce = 0;
    String shaHex = "";
    long startTime = System.currentTimeMillis();
    while (nonce < Long.MAX_VALUE) {
      byte[] data = this.prepareData(nonce);
      shaHex = DigestUtils.sha256Hex(data);
      if (new BigInteger(shaHex, 16).compareTo(this.target) == -1) {
        System.out.printf("Elapsed Time: %s seconds \n",
            (float) (System.currentTimeMillis() - startTime) / 1000);
        System.out.printf("correct hash Hex: %s \n\n", shaHex);
        break;
      } else {
        nonce++;
      }
    }
    return new PowResult(nonce, shaHex);
  }

  /**
   * 验证区块是否有效
   */
  public boolean validate() {
    byte[] data = this.prepareData(this.getBlock().getNonce());
    return new BigInteger(DigestUtils.sha256Hex(data), 16).compareTo(this.target) == -1;
  }

  /**
   * 准备数据
   * <p>
   * 注意：在准备区块数据时，一定要从原始数据类型转化为byte[]，不能直接从字符串进行转换
   */
  private byte[] prepareData(long nonce) {
    byte[] prevBlockHashBytes = {};
    if (StringUtils.isNoneBlank(this.getBlock().getPrevBlockHash())) {
      prevBlockHashBytes = new BigInteger(this.getBlock().getPrevBlockHash(), 16).toByteArray();
    }

    return ByteUtils.merge(
        prevBlockHashBytes,
        this.getBlock().hashTransaction(),
        ByteUtils.toBytes(this.getBlock().getTimeStamp()),
        ByteUtils.toBytes(TARGET_BITS),
        ByteUtils.toBytes(nonce)
    );

  }

}

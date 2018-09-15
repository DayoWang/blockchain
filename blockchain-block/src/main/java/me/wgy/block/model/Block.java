package me.wgy.block.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.wgy.block.consensus.PowResult;
import me.wgy.block.consensus.ProofOfWork;
import org.apache.commons.codec.binary.Hex;

/**
 * 区块
 *
 * @author wgy
 * @date 2018/9/14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Block {

  private static final String ZERO_HASH = Hex.encodeHexString(new byte[32]);

  /**
   * 区块hash值
   */
  private String hash;
  /**
   * 前一个区块的hash值
   */
  private String prevBlockHash;
  /**
   * 区块数据
   */
  private String data;
  /**
   * 区块创建时间(单位:秒)
   */
  private long timeStamp;
  /**
   * 工作量证明计数器
   */
  private long nonce;

  /**
   * <p> 创建创世区块 </p>
   */
  public static Block createGenesisBlock() {
    return Block.createBlock(ZERO_HASH, "Genesis Block");
  }

  /**
   * <p> 创建新区块 </p>
   */
  public static Block createBlock(String prevBlockHash, String data) {
    Block block = new Block("", prevBlockHash, data, Instant.now().getEpochSecond(), 0);
    ProofOfWork pow = ProofOfWork.createProofOfWork(block);
    PowResult powResult = pow.run();
    block.setHash(powResult.getHash());
    block.setNonce(powResult.getNonce());
    return block;

  }
}

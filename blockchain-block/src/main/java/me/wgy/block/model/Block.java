package me.wgy.block.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.wgy.block.consensus.PowResult;
import me.wgy.block.consensus.ProofOfWork;
import me.wgy.transaction.utxo.model.MerkleTree;
import me.wgy.transaction.utxo.model.Transaction;
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
@ToString
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
   * 交易信息
   */
  private Transaction[] transactions;
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
  public static Block createGenesisBlock(Transaction coinbase) {
    return Block.createBlock(ZERO_HASH, new Transaction[]{coinbase});
  }

  /**
   * <p> 创建新区块 </p>
   */
  public static Block createBlock(String previousHash, Transaction[] transactions) {
    Block block = new Block("", previousHash, transactions, Instant.now().getEpochSecond(), 0);
    ProofOfWork pow = ProofOfWork.createProofOfWork(block);
    PowResult powResult = pow.run();
    block.setHash(powResult.getHash());
    block.setNonce(powResult.getNonce());
    return block;
  }

  /**
   * 对区块中的交易信息进行Hash计算
   */
  public byte[] hashTransaction() {
    byte[][] txIdArrays = new byte[this.getTransactions().length][];
    for (int i = 0; i < this.getTransactions().length; i++) {
      txIdArrays[i] = this.getTransactions()[i].hash();
    }
    return new MerkleTree(txIdArrays).getRoot().getHash();
  }
}

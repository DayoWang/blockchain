package me.wgy.block.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.wgy.block.store.RocksDBStore;
import me.wgy.utils.ByteUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 区块链
 *
 * @author wgy
 * @date 2018/9/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Blockchain {

  private String lastBlockHash;

  /**
   * <p> 创建区块链 </p>
   */
  public static Blockchain createBlockchain() {
    String lastBlockHash = RocksDBStore.getInstance().getLastBlockHash();
    if (StringUtils.isBlank(lastBlockHash)) {
      Block genesisBlock = Block.createGenesisBlock();
      lastBlockHash = genesisBlock.getHash();
      RocksDBStore.getInstance().putBlock(genesisBlock);
      RocksDBStore.getInstance().putLastBlockHash(lastBlockHash);
    }
    return new Blockchain(lastBlockHash);
  }

  /**
   * <p> 添加区块  </p>
   */
  public void addBlock(String data) throws Exception {
    String lastBlockHash = RocksDBStore.getInstance().getLastBlockHash();
    if (StringUtils.isBlank(lastBlockHash)) {
      throw new Exception("Fail to add block into blockchain ! ");
    }
    this.addBlock(Block.createBlock(lastBlockHash, data));
  }

  /**
   * <p> 添加区块  </p>
   */
  public void addBlock(Block block) {
    RocksDBStore.getInstance().putLastBlockHash(block.getHash());
    RocksDBStore.getInstance().putBlock(block);
    this.lastBlockHash = block.getHash();
  }

  /**
   * 区块链迭代器
   */

  public class BlockchainIterator {

    private String currentBlockHash;

    public BlockchainIterator(String currentBlockHash) {
      this.currentBlockHash = currentBlockHash;
    }

    /**
     * 是否有下一个区块
     */
    public boolean hashNext() {
      if (ByteUtils.ZERO_HASH.equals(currentBlockHash)) {
        return false;
      }
      Block lastBlock = RocksDBStore.getInstance().getBlock(currentBlockHash);
      if (lastBlock == null) {
        return false;
      }
      // 创世区块直接放行
      if (ByteUtils.ZERO_HASH.equals(lastBlock.getPrevBlockHash())) {
        return true;
      }
      return RocksDBStore.getInstance().getBlock(lastBlock.getPrevBlockHash()) != null;
    }


    /**
     * 返回区块
     */
    public Block next() {
      Block currentBlock = RocksDBStore.getInstance().getBlock(currentBlockHash);
      if (currentBlock != null) {
        this.currentBlockHash = currentBlock.getPrevBlockHash();
        return currentBlock;
      }
      return null;
    }
  }

  public BlockchainIterator getBlockchainIterator() {
    return new BlockchainIterator(lastBlockHash);
  }
}

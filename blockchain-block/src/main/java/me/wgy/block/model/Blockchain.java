package me.wgy.block.model;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.wgy.block.store.RocksDBStore;
import me.wgy.transaction.utxo.model.SpendableOutputResult;
import me.wgy.transaction.utxo.model.TXInput;
import me.wgy.transaction.utxo.model.TXOutput;
import me.wgy.transaction.utxo.model.Transaction;
import me.wgy.utils.ByteUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
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
   * 从 DB 从恢复区块链数据
   */
  public static Blockchain initBlockchainFromDB() throws Exception {
    String lastBlockHash = RocksDBStore.getInstance().getLastBlockHash();
    if (lastBlockHash == null) {
      throw new Exception("ERROR: Fail to init blockchain from db. ");
    }
    return new Blockchain(lastBlockHash);
  }

  /**
   * <p> 创建区块链 </p>
   *
   * @param address 钱包地址
   */
  public static Blockchain createBlockchain(String address) {
    String lastBlockHash = RocksDBStore.getInstance().getLastBlockHash();
    if (StringUtils.isBlank(lastBlockHash)) {
      // 创建 coinBase 交易
      Transaction coinbaseTX = Transaction.createCoinbaseTX(address, "");
      Block genesisBlock = Block.createGenesisBlock(coinbaseTX);
      lastBlockHash = genesisBlock.getHash();
      RocksDBStore.getInstance().putBlock(genesisBlock);
      RocksDBStore.getInstance().putLastBlockHash(lastBlockHash);
    }
    return new Blockchain(lastBlockHash);
  }

  /**
   * 打包交易，进行挖矿
   */
  public void mineBlock(Transaction[] transactions) throws Exception {
    String lastBlockHash = RocksDBStore.getInstance().getLastBlockHash();
    if (lastBlockHash == null) {
      throw new Exception("ERROR: Fail to get last block hash ! ");
    }
    Block block = Block.createBlock(lastBlockHash, transactions);
    this.addBlock(block);
  }

  /**
   * <p> 添加区块  </p>
   */
  private void addBlock(Block block) {
    RocksDBStore.getInstance().putLastBlockHash(block.getHash());
    RocksDBStore.getInstance().putBlock(block);
    this.lastBlockHash = block.getHash();
  }

  /**
   * 区块链迭代器
   */
  public class BlockchainIterator {

    private String currentBlockHash;

    private BlockchainIterator(String currentBlockHash) {
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

  /**
   * 查找钱包地址对应的所有UTXO
   *
   * @param address 钱包地址
   */
  public TXOutput[] findUTXO(String address) throws Exception {
    Transaction[] unspentTxs = this.findUnspentTransactions(address);
    TXOutput[] utxos = {};
    if (unspentTxs == null || unspentTxs.length == 0) {
      return utxos;
    }
    for (Transaction tx : unspentTxs) {
      for (TXOutput txOutput : tx.getOutputs()) {
        if (txOutput.canBeUnlockedWith(address)) {
          utxos = ArrayUtils.add(utxos, txOutput);
        }
      }
    }
    return utxos;
  }

  /**
   * 查找钱包地址对应的所有未花费的交易
   *
   * @param address 钱包地址
   */
  private Transaction[] findUnspentTransactions(String address) throws Exception {
    Map<String, int[]> allSpentTXOs = this.getAllSpentTXOs(address);
    Transaction[] unspentTxs = {};

    // 再次遍历所有区块中的交易输出
    for (BlockchainIterator blockchainIterator = this.getBlockchainIterator();
        blockchainIterator.hashNext(); ) {
      Block block = blockchainIterator.next();
      for (Transaction transaction : block.getTransactions()) {

        String txId = Hex.encodeHexString(transaction.getTxId());

        int[] spentOutIndexArray = allSpentTXOs.get(txId);

        for (int outIndex = 0; outIndex < transaction.getOutputs().length; outIndex++) {
          if (spentOutIndexArray != null && ArrayUtils.contains(spentOutIndexArray, outIndex)) {
            continue;
          }

          // 保存不存在 allSpentTXOs 中的交易
          if (transaction.getOutputs()[outIndex].canBeUnlockedWith(address)) {
            unspentTxs = ArrayUtils.add(unspentTxs, transaction);
          }
        }
      }
    }
    return unspentTxs;
  }

  /**
   * 从交易输入中查询区块链中所有已被花费了的交易输出
   *
   * @param address 钱包地址
   * @return 交易ID以及对应的交易输出下标地址
   */
  private Map<String, int[]> getAllSpentTXOs(String address) {
    // 定义TxId ——> spentOutIndex[]，存储交易ID与已被花费的交易输出数组索引值
    Map<String, int[]> spentTXOs = new HashMap<>();
    for (BlockchainIterator blockchainIterator = this.getBlockchainIterator();
        blockchainIterator.hashNext(); ) {
      Block block = blockchainIterator.next();

      for (Transaction transaction : block.getTransactions()) {
        // 如果是 coinbase 交易，直接跳过，因为它不存在引用前一个区块的交易输出
        if (transaction.isCoinbase()) {
          continue;
        }
        for (TXInput txInput : transaction.getInputs()) {
          if (txInput.canUnlockOutputWith(address)) {
            String inTxId = Hex.encodeHexString(txInput.getTxId());
            int[] spentOutIndexArray = spentTXOs.get(inTxId);
            if (spentOutIndexArray == null) {
              spentTXOs.put(inTxId, new int[]{txInput.getTxOutputIndex()});
            } else {
              spentOutIndexArray = ArrayUtils.add(spentOutIndexArray, txInput.getTxOutputIndex());
              spentTXOs.put(inTxId, spentOutIndexArray);
            }
          }
        }
      }
    }
    return spentTXOs;
  }

  /**
   * 寻找能够花费的交易
   *
   * @param address 钱包地址
   * @param amount 花费金额
   */
  public SpendableOutputResult findSpendableOutputs(String address, int amount) throws Exception {
    Transaction[] unspentTXs = this.findUnspentTransactions(address);
    int accumulated = 0;
    Map<String, int[]> unspentOuts = new HashMap<>();
    for (Transaction tx : unspentTXs) {

      String txId = Hex.encodeHexString(tx.getTxId());

      for (int outId = 0; outId < tx.getOutputs().length; outId++) {

        TXOutput txOutput = tx.getOutputs()[outId];

        if (txOutput.canBeUnlockedWith(address) && accumulated < amount) {
          accumulated += txOutput.getValue();

          int[] outIds = unspentOuts.get(txId);
          if (outIds == null) {
            outIds = new int[]{outId};
          } else {
            outIds = ArrayUtils.add(outIds, outId);
          }
          unspentOuts.put(txId, outIds);
          if (accumulated >= amount) {
            break;
          }
        }
      }
    }
    return new SpendableOutputResult(accumulated, unspentOuts);
  }
}

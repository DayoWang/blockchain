package me.wgy.block.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.wgy.block.store.RocksDBStore;
import me.wgy.transaction.utxo.model.TXInput;
import me.wgy.transaction.utxo.model.TXOutput;
import me.wgy.transaction.utxo.model.Transaction;
import me.wgy.utils.ByteUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;

/**
 * 区块链
 *
 * @author wgy
 * @date 2018/9/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class Blockchain {

  private String lastBlockHash;


  /**
   * 从 DB 中恢复区块链数据
   */
  public static Blockchain initBlockchainFromDB() {
    String lastBlockHash = RocksDBStore.getInstance().getLastBlockHash();
    if (lastBlockHash == null) {
      throw new RuntimeException("ERROR: Fail to init blockchain from db. ");
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
      String genesisCoinBaseData = "The Times 03/Jan/2009 Chancellor on brink of second bailout for banks";
      Transaction coinBaseTX = Transaction.createCoinbaseTX(address, genesisCoinBaseData);
      Block genesisBlock = Block.createGenesisBlock(coinBaseTX);
      lastBlockHash = genesisBlock.getHash();
      RocksDBStore.getInstance().putBlock(genesisBlock);
      RocksDBStore.getInstance().putLastBlockHash(lastBlockHash);
    }
    return new Blockchain(lastBlockHash);
  }

  /**
   * 打包交易，进行挖矿
   */
  public Block mineBlock(Transaction[] transactions) {
    // 挖矿前，先验证交易记录
    for (Transaction tx : transactions) {
      if (!this.verifyTransactions(tx)) {
        log.error("ERROR: Fail to mine block ! Invalid transaction ! tx=" + tx.toString());
        throw new RuntimeException("ERROR: Fail to mine block ! Invalid transaction ! ");
      }
    }
    Block lastBlock = RocksDBStore.getInstance().getLastBlock();
    Block block = Block.createBlock(lastBlockHash, transactions, lastBlock.getHeight() + 1);
    this.addBlock(block);
    return block;
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
   * <p> 保存区块  </p>
   */
  public void saveBlock(Block block) {
    Block existBlock = RocksDBStore.getInstance().getBlock(block.getHash());
    if (existBlock != null) {
      return;
    }
    // 保存区块数据
    RocksDBStore.getInstance().putBlock(block);
    Block lastBlock = RocksDBStore.getInstance().getLastBlock();

    if (block.getHeight() > lastBlock.getHeight()) {
      RocksDBStore.getInstance().putLastBlockHash(block.getHash());
      this.lastBlockHash = block.getHash();
    }
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

  /**
   * 获取区块链迭代器
   */
  public BlockchainIterator getBlockchainIterator() {
    return new BlockchainIterator(lastBlockHash);
  }

  /**
   * 查找所有的 unspent transaction outputs
   */
  public Map<String, TXOutput[]> findAllUTXOs() {
    Map<String, int[]> allSpentTXOs = this.getAllSpentTXOs();
    Map<String, TXOutput[]> allUTXOs = Maps.newHashMap();
    // 再次遍历所有区块中的交易输出
    for (BlockchainIterator blockchainIterator = this.getBlockchainIterator();
        blockchainIterator.hashNext(); ) {
      Block block = blockchainIterator.next();
      for (Transaction transaction : block.getTransactions()) {

        String txId = Hex.encodeHexString(transaction.getTxId());

        int[] spentOutIndexArray = allSpentTXOs.get(txId);
        TXOutput[] txOutputs = transaction.getOutputs();
        for (int outIndex = 0; outIndex < txOutputs.length; outIndex++) {
          if (spentOutIndexArray != null && ArrayUtils.contains(spentOutIndexArray, outIndex)) {
            continue;
          }
          TXOutput[] UTXOArray = allUTXOs.get(txId);
          if (UTXOArray == null) {
            UTXOArray = new TXOutput[]{txOutputs[outIndex]};
          } else {
            UTXOArray = ArrayUtils.add(UTXOArray, txOutputs[outIndex]);
          }
          allUTXOs.put(txId, UTXOArray);
        }
      }
    }
    return allUTXOs;
  }

  /**
   * 从交易输入中查询区块链中所有已被花费了的交易输出
   *
   * @return 交易ID以及对应的交易输出下标地址
   */
  private Map<String, int[]> getAllSpentTXOs() {
    // 定义TxId ——> spentOutIndex[]，存储交易ID与已被花费的交易输出数组索引值
    Map<String, int[]> spentTXOs = Maps.newHashMap();
    for (BlockchainIterator blockchainIterator = this.getBlockchainIterator();
        blockchainIterator.hashNext(); ) {
      Block block = blockchainIterator.next();

      for (Transaction transaction : block.getTransactions()) {
        // 如果是 coinbase 交易，直接跳过，因为它不存在引用前一个区块的交易输出
        if (transaction.isCoinbase()) {
          continue;
        }
        for (TXInput txInput : transaction.getInputs()) {
          String inTxId = Hex.encodeHexString(txInput.getTxId());
          int[] spentOutIndexArray = spentTXOs.get(inTxId);
          if (spentOutIndexArray == null) {
            spentOutIndexArray = new int[]{txInput.getTxOutputIndex()};
          } else {
            spentOutIndexArray = ArrayUtils.add(spentOutIndexArray, txInput.getTxOutputIndex());
          }
          spentTXOs.put(inTxId, spentOutIndexArray);
        }
      }
    }
    return spentTXOs;
  }


  /**
   * 依据交易ID查询交易信息
   *
   * @param txId 交易ID
   */
  private Transaction findTransaction(byte[] txId) {
    for (BlockchainIterator iterator = this.getBlockchainIterator(); iterator.hashNext(); ) {
      Block block = iterator.next();
      for (Transaction tx : block.getTransactions()) {
        if (Arrays.equals(tx.getTxId(), txId)) {
          return tx;
        }
      }
    }
    throw new RuntimeException("ERROR: Can not found tx by txId ! ");
  }


  /**
   * 进行交易签名
   *
   * @param tx 交易数据
   * @param privateKey 私钥
   */
  public void signTransaction(Transaction tx, BCECPrivateKey privateKey) throws Exception {
    // 先来找到这笔新的交易中，交易输入所引用的前面的多笔交易的数据
    Map<String, Transaction> prevTxMap = Maps.newHashMap();
    for (TXInput txInput : tx.getInputs()) {
      Transaction prevTx = this.findTransaction(txInput.getTxId());
      prevTxMap.put(Hex.encodeHexString(txInput.getTxId()), prevTx);
    }
    tx.sign(privateKey, prevTxMap);
  }

  /**
   * 交易签名验证
   */
  public boolean verifyTransactions(Transaction tx) {
    if (tx.isCoinbase()) {
      return true;
    }
    Map<String, Transaction> prevTx = Maps.newHashMap();
    for (TXInput txInput : tx.getInputs()) {
      Transaction transaction = this.findTransaction(txInput.getTxId());
      prevTx.put(Hex.encodeHexString(txInput.getTxId()), transaction);
    }
    try {
      return tx.verify(prevTx);
    } catch (Exception e) {
      log.error("Fail to verify transaction ! transaction invalid ! ", e);
      throw new RuntimeException("Fail to verify transaction ! transaction invalid ! ", e);
    }
  }

  /**
   * 获取本地节点区块的最大高度
   */
  public long getBestHeight() {
    Block lastBlock = RocksDBStore.getInstance().getLastBlock();
    return lastBlock.getHeight();
  }

  /**
   * 获取区块链中所有区块的hash值
   */
  public List<String> getAllBlockHash() {
    List<String> blockHashes = Lists.newArrayList();
    for (BlockchainIterator blockchainIterator = this.getBlockchainIterator();
        blockchainIterator.hashNext(); ) {
      Block block = blockchainIterator.next();
      blockHashes.add(block.getHash());
    }
    return blockHashes;
  }

  /**
   * 根据hash查询区块
   */
  public Block getBlockByHash(String hash) {
    return RocksDBStore.getInstance().getBlock(hash);
  }
}

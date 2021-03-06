package me.wgy.block.store;

import com.google.common.collect.Maps;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.wgy.block.model.Block;
import me.wgy.transaction.utxo.model.TXOutput;
import me.wgy.utils.SerializeUtils;
import org.apache.commons.lang3.StringUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

/**
 * 存储工具类
 *
 * @author wgy
 * @date 2018/9/16
 */
@Slf4j
public class RocksDBStore {

  /**
   * 区块链数据文件
   */
  private static final String DB_FILE = "blockchain.db";
  /**
   * 区块桶Key
   */
  private static final String BLOCKS_BUCKET_KEY = "blocks";
  /**
   * 链状态桶Key
   */
  private static final String CHAINSTATE_BUCKET_KEY = "chainstate";

  /**
   * 最新一个区块
   */
  private static final String LAST_BLOCK_KEY = "l";

  private volatile static RocksDBStore instance;

  public static RocksDBStore getInstance() {
    if (instance == null) {
      synchronized (RocksDBStore.class) {
        if (instance == null) {
          instance = new RocksDBStore();
        }
      }
    }
    return instance;
  }

  private RocksDB db;

  /**
   * block buckets
   */
  private Map<String, byte[]> blocksBucket;
  /**
   * chainstate buckets
   */
  @Getter
  private Map<String, byte[]> chainstateBucket;

  private RocksDBStore() {
    openDB();
    initBlockBucket();
    initChainStateBucket();
  }

  /**
   * 打开数据库
   */
  private void openDB() {
    try {
      db = RocksDB.open(DB_FILE);
    } catch (RocksDBException e) {
      log.error("Fail to open db ! ", e);
      throw new RuntimeException("Fail to open db ! ", e);
    }
  }

  /**
   * 初始化 blocks 数据桶
   */
  private void initBlockBucket() {
    try {
      byte[] blockBucketKey = SerializeUtils.serialize(BLOCKS_BUCKET_KEY);
      byte[] blockBucketBytes = db.get(blockBucketKey);
      if (blockBucketBytes != null) {
        blocksBucket = (Map) SerializeUtils.deserialize(blockBucketBytes);
      } else {
        blocksBucket = Maps.newHashMap();
        db.put(blockBucketKey, SerializeUtils.serialize(blocksBucket));
      }
    } catch (RocksDBException e) {
      log.error("Fail to init block bucket ! ", e);
      throw new RuntimeException("Fail to init block bucket ! ", e);
    }
  }

  /**
   * 初始化 blocks 数据桶
   */
  private void initChainStateBucket() {
    try {
      byte[] chainstateBucketKey = SerializeUtils.serialize(CHAINSTATE_BUCKET_KEY);
      byte[] chainstateBucketBytes = db.get(chainstateBucketKey);
      if (chainstateBucketBytes != null) {
        chainstateBucket = (Map) SerializeUtils.deserialize(chainstateBucketBytes);
      } else {
        chainstateBucket = Maps.newHashMap();
        db.put(chainstateBucketKey, SerializeUtils.serialize(chainstateBucket));
      }
    } catch (RocksDBException e) {
      log.error("Fail to init chainstate bucket ! ", e);
      throw new RuntimeException("Fail to init chainstate bucket ! ", e);
    }
  }

  /**
   * 保存最新一个区块的Hash值
   */
  public void putLastBlockHash(String tipBlockHash) {
    try {
      blocksBucket.put(LAST_BLOCK_KEY, SerializeUtils.serialize(tipBlockHash));
      db.put(SerializeUtils.serialize(BLOCKS_BUCKET_KEY), SerializeUtils.serialize(blocksBucket));
    } catch (RocksDBException e) {
      log.error("Fail to put last block hash ! tipBlockHash=" + tipBlockHash, e);
      throw new RuntimeException("Fail to put last block hash ! tipBlockHash=" + tipBlockHash, e);
    }
  }

  /**
   * 查询最新一个区块的Hash值
   */
  public String getLastBlockHash() {
    byte[] lastBlockHashBytes = blocksBucket.get(LAST_BLOCK_KEY);
    if (lastBlockHashBytes != null) {
      return (String) SerializeUtils.deserialize(lastBlockHashBytes);
    }
    return "";
  }

  /**
   * 保存区块
   */
  public void putBlock(Block block) {
    try {
      blocksBucket.put(block.getHash(), SerializeUtils.serialize(block));
      db.put(SerializeUtils.serialize(BLOCKS_BUCKET_KEY), SerializeUtils.serialize(blocksBucket));
    } catch (RocksDBException e) {
      log.error("Fail to put block ! block=" + block.toString(), e);
      throw new RuntimeException("Fail to put block ! block=" + block.toString(), e);
    }
  }

  /**
   * 查询区块
   */
  public Block getBlock(String blockHash) {
    byte[] blockBytes = blocksBucket.get(blockHash);
    if (blockBytes != null) {
      return (Block) SerializeUtils.deserialize(blockBytes);
    }
    throw new RuntimeException("Fail to get block , don`t exist ! blockHash=" + blockHash);
  }

  /**
   * 获取最新一个区块
   */
  public Block getLastBlock() {
    String lastBlockHash = getLastBlockHash();
    if (StringUtils.isBlank(lastBlockHash)) {
      throw new RuntimeException("ERROR: Fail to get last block hash ! ");
    }
    Block lastBlock = getBlock(lastBlockHash);
    if (lastBlock == null) {
      throw new RuntimeException("ERROR: Fail to get last block ! ");
    }
    return lastBlock;
  }

  /**
   * 清空chainstate bucket
   */
  public void cleanChainStateBucket() {
    try {
      chainstateBucket.clear();
    } catch (Exception e) {
      log.error("Fail to clear chainstate bucket ! ", e);
      throw new RuntimeException("Fail to clear chainstate bucket ! ", e);
    }
  }

  /**
   * 一次性放入所有UTXO数据
   */
  public void initAllUTXOs(Map<String, byte[]> utxoDatas) {
    try {
      chainstateBucket.putAll(utxoDatas);
      db.put(SerializeUtils.serialize(CHAINSTATE_BUCKET_KEY),
          SerializeUtils.serialize(chainstateBucket));
    } catch (RocksDBException e) {
      log.error("Fail to init all UTXOs data into chainstate bucket ! ", e);
      throw new RuntimeException("Fail to init all UTXOs data into chainstate bucket ! ", e);
    }
  }

  /**
   * 保存UTXO数据
   *
   * @param key 交易ID
   * @param utxos UTXOs
   */
  public void putUTXOs(String key, TXOutput[] utxos) {
    try {
      chainstateBucket.put(key, SerializeUtils.serialize(utxos));
      db.put(SerializeUtils.serialize(CHAINSTATE_BUCKET_KEY),
          SerializeUtils.serialize(chainstateBucket));
    } catch (Exception e) {
      log.error("Fail to put UTXOs into chainstate bucket ! key=" + key, e);
      throw new RuntimeException("Fail to put UTXOs into chainstate bucket ! key=" + key, e);
    }
  }

  /**
   * 查询UTXO数据
   *
   * @param key 交易ID
   */
  public TXOutput[] getUTXOs(String key) {
    byte[] utxosByte = chainstateBucket.get(key);
    if (utxosByte != null) {
      return (TXOutput[]) SerializeUtils.deserialize(utxosByte);
    }
    return null;
  }


  /**
   * 删除 UTXO 数据
   *
   * @param key 交易ID
   */
  public void deleteUTXOs(String key) {
    try {
      chainstateBucket.remove(key);
      db.put(SerializeUtils.serialize(CHAINSTATE_BUCKET_KEY),
          SerializeUtils.serialize(chainstateBucket));
    } catch (Exception e) {
      log.error("Fail to delete UTXOs by key ! key=" + key, e);
      throw new RuntimeException("Fail to delete UTXOs by key ! key=" + key, e);
    }
  }

  /**
   * 关闭数据库
   */
  public void closeDB() {
    try {
      db.close();
    } catch (Exception e) {
      log.error("Fail to close db ! ", e);
      throw new RuntimeException("Fail to close db ! ", e);
    }
  }
}

package me.wgy.block.store;

import com.google.common.collect.Maps;
import java.util.Map;
import me.wgy.block.model.Block;
import me.wgy.utils.SerializeUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

/**
 * 存储工具类
 *
 * @author wgy
 * @date 2018/9/16
 */
public class RocksDBStore {

  /**
   * 区块链数据文件
   */
  private static final String DB_FILE = "blockchain.db";
  /**
   * 区块桶前缀
   */
  private static final String BLOCKS_BUCKET_KEY = "blocks";
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

  private RocksDBStore() {
    openDB();
    initBlockBucket();
  }

  /**
   * 打开数据库
   */
  private void openDB() {
    try {
      db = RocksDB.open(DB_FILE);
    } catch (RocksDBException e) {
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
      throw new RuntimeException("Fail to init block bucket ! ", e);
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
      throw new RuntimeException("Fail to put last block hash ! ", e);
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
      throw new RuntimeException("Fail to put block ! ", e);
    }
  }

  /**
   * 查询区块
   */
  public Block getBlock(String blockHash) {
    return (Block) SerializeUtils.deserialize(blocksBucket.get(blockHash));
  }

  /**
   * 关闭数据库
   */
  public void closeDB() {
    try {
      db.close();
    } catch (Exception e) {
      throw new RuntimeException("Fail to close db ! ", e);
    }
  }
}

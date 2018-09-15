package me.wgy.model;

import java.math.BigInteger;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.wgy.util.ByteUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

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
   * <p> 创建创世区块 </p>
   */
  public static Block createGenesisBlock() {
    return Block.createBlock(ZERO_HASH, "Genesis Block");
  }

  /**
   * <p> 创建新区块 </p>
   */
  public static Block createBlock(String prevBlockHash, String data) {
    Block block = new Block(ZERO_HASH, prevBlockHash, data, Instant.now().getEpochSecond());
    block.calculateHash();
    return block;
  }

  /**
   * 计算区块Hash 通过SHA256算法对区块头进行二次哈希计算而得到的数字签名
   * <p>
   * 注意：在准备区块数据时，一定要从原始数据类型转化为byte[]，不能直接从字符串进行转换
   */
  private void calculateHash() {
    byte[] prevBlockHashBytes = {};
    if (StringUtils.isNoneBlank(this.getPrevBlockHash())) {
      prevBlockHashBytes = new BigInteger(this.getPrevBlockHash(), 16).toByteArray();
    }

    byte[] headers = ByteUtils.merge(
        prevBlockHashBytes,
        this.getData().getBytes(),
        ByteUtils.toBytes(this.getTimeStamp()));

    this.setHash(DigestUtils.sha256Hex(headers));
  }
}

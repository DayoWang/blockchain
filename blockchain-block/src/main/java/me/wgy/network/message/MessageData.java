package me.wgy.network.message;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.wgy.block.model.Block;
import me.wgy.network.PeerInfo;
import me.wgy.transaction.utxo.model.Transaction;
import me.wgy.utils.SerializeUtils;
import org.apache.commons.codec.binary.Hex;

/**
 * 消息数据抽象父类
 *
 * @author wgy
 * @date 2018/9/27
 */
@Data
public class MessageData {

  /**
   * 本地地址信息
   */
  protected PeerInfo myPeerInfo;
  /**
   * 当前时间
   */
  protected long nTime;

  /**
   * 消息数据转化为字节数组
   */
  public byte[] toBytes() {
    return SerializeUtils.serialize(this);
  }

  /**
   * 文本消息
   */
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class StringMessageData extends MessageData {

    private String msg;
  }

  /**
   * 节点加入消息
   *
   * @author wangwei
   * @date 2018/08/26
   */
  @NoArgsConstructor
  public static class JoinMessageData extends MessageData {

  }

  /**
   * 版本消息
   */
  @Data
  @NoArgsConstructor
  public static class VersionMessageData extends MessageData {

    /**
     * 版本号
     */
    private int clientVersion = 0;
    /**
     * 当前节点区块链的高度
     */
    private long bestHeight;

  }

  /**
   * 获取区块消息
   *
   * @author wangwei
   * @date 2018/08/27
   */
  @Data
  @NoArgsConstructor
  public static class GetBlocksMessageData extends MessageData {

  }

  /**
   * 库存清单消息
   */
  @Data
  @NoArgsConstructor
  public static class InventoryMessageData extends MessageData {

    /**
     * 库存清单类型("tx","block")
     */
    private InvTypeEnum invType;
    /**
     * 区块Hash列表
     */
    private List<String> blockHashes = Lists.newArrayList();
    /**
     * 交易Hash列表
     */
    private List<String> txHashes = Lists.newArrayList();

    /**
     * 新增TxID
     */
    public void addTxIdHash(String txIdHash) {
      txHashes.add(txIdHash);
    }

    /**
     * 新增TxID
     */
    public void addTxIdHash(byte[] txIdBytes) {
      this.addTxIdHash(Hex.encodeHexString(txIdBytes));
    }

    /**
     * 添加区块Hash
     */
    public void addBlockHash(String blockHash) {
      blockHashes.add(blockHash);
    }

  }

  /**
   * 获取数据
   */
  @Data
  @NoArgsConstructor
  public static class GetDataMessageData extends MessageData {

    private InvTypeEnum type;

    private String id;
  }

  /**
   * 区块
   */
  @Data
  @NoArgsConstructor
  public static class BlockMessageData extends MessageData {

    private Block block;

  }

  /**
   * 交易消息
   */
  @Data
  @NoArgsConstructor
  public static class TxMessageData extends MessageData {

    private Transaction tx;

  }
}

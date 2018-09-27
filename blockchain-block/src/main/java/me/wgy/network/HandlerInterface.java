package me.wgy.network;

import me.wgy.block.model.Blockchain;
import me.wgy.network.message.MessageData;
import me.wgy.network.message.PeerMessage;

/**
 * 消息处理接口
 *
 * @author wgy
 * @date 2018/9/27
 */
public interface HandlerInterface<T extends MessageData> {

  /**
   * 消息处理
   */
  public void handleMessage(PeerConnection peerConn, PeerMessage peerMessage,
      Blockchain blockchain);

  /**
   * 获取消息数据对象
   */
  public T getMsgData(PeerMessage peerMessage);

}

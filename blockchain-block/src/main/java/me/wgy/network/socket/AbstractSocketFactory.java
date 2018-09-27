package me.wgy.network.socket;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * socket 抽象工厂
 *
 * @author wgy
 * @date 2018/9/27
 */
public abstract class AbstractSocketFactory {

  private static AbstractSocketFactory currentFactory = new NormalSocketFactory();

  public static AbstractSocketFactory getSocketFactory() {
    return currentFactory;
  }

  public static void setSocketFactory(AbstractSocketFactory sf) {
    if (sf == null) {
      throw new NullPointerException("Attempting to set null socket factory.");
    }
    currentFactory = sf;
  }

  /**
   * 创建socket
   */
  public abstract SocketInterface makeSocket(String host, int port)
      throws IOException, UnknownHostException;

  /**
   * 创建socket
   */
  public abstract SocketInterface makeSocket(Socket socket) throws IOException;
}

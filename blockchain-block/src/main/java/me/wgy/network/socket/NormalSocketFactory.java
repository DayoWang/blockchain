package me.wgy.network.socket;

import java.io.IOException;
import java.net.Socket;

/**
 * @author wgy
 * @date 2018/9/27
 */
public class NormalSocketFactory extends AbstractSocketFactory {

  @Override
  public SocketInterface makeSocket(String host, int port) throws IOException {
    return new NormalSocket(host, port);
  }

  @Override
  public SocketInterface makeSocket(Socket socket) throws IOException {
    return new NormalSocket(socket);
  }
}

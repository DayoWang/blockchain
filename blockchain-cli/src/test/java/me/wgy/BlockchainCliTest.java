package me.wgy;

import me.wgy.cli.CliService;

/**
 * Cli 测试
 *
 * @author wgy
 * @date 2018/9/16
 */
public class BlockchainCliTest {

  public static void main(String[] args) {
    try {
      //String argss[] = {"-addblock", "Send 2.0 BTC to Dayo Wang Cli"};
      String argss[] = {"-print"};
      CliService cli = new CliService(argss);
      cli.parse();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

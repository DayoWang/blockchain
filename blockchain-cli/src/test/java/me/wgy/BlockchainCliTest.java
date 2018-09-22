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
      String[] argss = {"send", "-from", "wgy", "-to", "wgy1", "-amount", "6"};
      CliService cli = new CliService(argss);
      cli.parse();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

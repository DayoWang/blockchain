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
 //     String[] argss = {"createwallet"};
 //        String[] argss = {"createblockchain", "-address", "1NiLxCMhXLD8MgPFAArbq19zuHLqZYtGnJ"};
  //          String[] argss = {"printaddresses"};
  //         String[] argss = {"getbalance", "-address", "1NiLxCMhXLD8MgPFAArbq19zuHLqZYtGnJ"};
//           String[] argss = {"send", "-from", "1NiLxCMhXLD8MgPFAArbq19zuHLqZYtGnJ", "-to", "1E34e5HFhLghuiNbNRaUjwTfyqywAqNmYs", "-amount", "5"};
      String[] argss = {"printchain"};
      CliService cli = new CliService(argss);
      cli.parse();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

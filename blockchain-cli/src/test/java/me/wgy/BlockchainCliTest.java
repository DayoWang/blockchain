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
      // String[] argss = {"createwallet"};
       String[] argss = {"createblockchain", "-address", "1AJJ9n2oHPkDyfZw6hddHmtDWPVG2D9nib"};
      // String[] argss = {"printaddresses"};
      // String[] argss = {"getbalance", "-address", "1AJJ9n2oHPkDyfZw6hddHmtDWPVG2D9nib"};
      // String[] argss = {"send", "-from", "1AJJ9n2oHPkDyfZw6hddHmtDWPVG2D9nib", "-to","1E34e5HFhLghuiNbNRaUjwTfyqywAqNmYs", "-amount", "5"};
      CliService cli = new CliService(argss);
      cli.parse();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

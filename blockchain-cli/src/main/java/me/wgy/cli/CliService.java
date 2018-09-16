package me.wgy.cli;

import me.wgy.block.consensus.ProofOfWork;
import me.wgy.block.model.Block;
import me.wgy.block.model.Blockchain;
import me.wgy.block.store.RocksDBStore;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

/**
 * 程序命令行工具入口
 *
 * @author wgy
 * @date 2018/9/16
 */
public class CliService {

  private String[] args;
  private Options options = new Options();

  public CliService(String[] args) {
    this.args = args;
    options.addOption("h", "help", false, "show help");
    options.addOption("add", "addblock", true, "add a block to the blockchain");
    options.addOption("print", "printchain", false, "print all the blocks of the blockchain");
  }

  /**
   * 命令行解析入口
   */
  public void parse() {
    this.validateArgs(args);
    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(options, args);

      if (cmd.hasOption("h")) {
        help();
      }
      if (cmd.hasOption("add")) {
        String data = cmd.getOptionValue("add");
        addBlock(data);
      }
      if (cmd.hasOption("print")) {
        printChain();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      RocksDBStore.getInstance().closeDB();
    }
  }


  /**
   * 验证入参
   */
  private void validateArgs(String[] args) {
    if (args == null || args.length < 1) {
      help();
    }
  }

  /**
   * 打印帮助信息
   */
  private void help() {
    HelpFormatter helpFormatter = new HelpFormatter();
    helpFormatter.printHelp("Main", options);
    System.exit(0);
  }

  /**
   * 添加区块
   */
  private void addBlock(String data) throws Exception {
    Blockchain blockchain = Blockchain.createBlockchain();
    blockchain.addBlock(data);
  }

  /**
   * 打印出区块链中的所有区块
   */
  private void printChain() {
    Blockchain blockchain = Blockchain.createBlockchain();
    for (Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator();
        iterator.hashNext(); ) {
      Block block = iterator.next();

      if (block != null) {
        boolean validate = ProofOfWork.createProofOfWork(block).validate();
        System.out.println(block.toString() + ", validate = " + validate);
      }
    }
  }
}

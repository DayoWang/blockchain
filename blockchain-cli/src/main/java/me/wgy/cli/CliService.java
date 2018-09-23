package me.wgy.cli;

import java.util.Arrays;
import java.util.Set;
import me.wgy.block.consensus.ProofOfWork;
import me.wgy.block.model.Block;
import me.wgy.block.model.Blockchain;
import me.wgy.block.store.RocksDBStore;
import me.wgy.model.Wallet;
import me.wgy.transaction.utxo.model.TXOutput;
import me.wgy.transaction.utxo.model.Transaction;
import me.wgy.utils.Base58Check;
import me.wgy.utils.WalletUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

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

    Option helpCmd = Option.builder("h").desc("show help").build();
    options.addOption(helpCmd);

    Option address = Option.builder("address").hasArg(true).desc("Source wallet address").build();
    Option sendFrom = Option.builder("from").hasArg(true).desc("Source wallet address").build();
    Option sendTo = Option.builder("to").hasArg(true).desc("Destination wallet address").build();
    Option sendAmount = Option.builder("amount").hasArg(true).desc("Amount to send").build();

    options.addOption(address);
    options.addOption(sendFrom);
    options.addOption(sendTo);
    options.addOption(sendAmount);
  }

  /**
   * 命令行解析入口
   */
  public void parse() {
    this.validateArgs(args);
    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(options, args);
      switch (args[0]) {
        case "createblockchain":
          String createblockchainAddress = cmd.getOptionValue("address");
          if (StringUtils.isBlank(createblockchainAddress)) {
            help();
          }
          this.createBlockchain(createblockchainAddress);
          break;
        case "getbalance":
          String getBalanceAddress = cmd.getOptionValue("address");
          if (StringUtils.isBlank(getBalanceAddress)) {
            help();
          }
          this.getBalance(getBalanceAddress);
          break;
        case "send":
          String sendFrom = cmd.getOptionValue("from");
          String sendTo = cmd.getOptionValue("to");
          String sendAmount = cmd.getOptionValue("amount");
          if (StringUtils.isBlank(sendFrom) ||
              StringUtils.isBlank(sendTo) ||
              !NumberUtils.isDigits(sendAmount)) {
            help();
          }
          this.send(sendFrom, sendTo, Integer.valueOf(sendAmount));
          break;
        case "createwallet":
          this.createWallet();
          break;
        case "printaddresses":
          this.printAddresses();
          break;
        case "printchain":
          this.printChain();
          break;
        case "h":
          this.help();
          break;
        default:
          this.help();
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
   * 创建区块链
   */
  private void createBlockchain(String address) throws Exception {
    Blockchain.createBlockchain(address);
    System.out.println("Done ! ");
  }

  /**
   * 创建钱包
   */
  private void createWallet() throws Exception {
    Wallet wallet = WalletUtils.getInstance().createWallet();
    System.out.println("wallet address : " + wallet.getAddress());
  }

  /**
   * 打印钱包地址
   */
  private void printAddresses() throws Exception {
    Set<String> addresses = WalletUtils.getInstance().getAddresses();
    if (addresses == null || addresses.isEmpty()) {
      System.out.println("There isn't address");
      return;
    }
    for (String address : addresses) {
      System.out.println("Wallet address: " + address);
    }
  }

  /**
   * 查询钱包余额
   *
   * @param address 钱包地址
   */
  private void getBalance(String address) throws Exception {
    // 检查钱包地址是否合法
    try {
      Base58Check.base58ToBytes(address);
    } catch (Exception e) {
      throw new Exception("ERROR: invalid wallet address");
    }
    Blockchain blockchain = Blockchain.createBlockchain(address);
    // 得到公钥Hash值
    byte[] versionedPayload = Base58Check.base58ToBytes(address);
    byte[] pubKeyHash = Arrays.copyOfRange(versionedPayload, 1, versionedPayload.length);

    TXOutput[] txOutputs = blockchain.findUTXO(pubKeyHash);
    int balance = 0;
    if (txOutputs != null && txOutputs.length > 0) {
      for (TXOutput txOutput : txOutputs) {
        balance += txOutput.getValue();
      }
    }
    System.out.printf("Balance of '%s': %d\n", address, balance);
  }

  /**
   * 转账
   */
  private void send(String from, String to, int amount) throws Exception {
    // 检查钱包地址是否合法
    try {
      Base58Check.base58ToBytes(from);
    } catch (Exception e) {
      throw new Exception("ERROR: sender address invalid ! address=" + from);
    }
    // 检查钱包地址是否合法
    try {
      Base58Check.base58ToBytes(to);
    } catch (Exception e) {
      throw new Exception("ERROR: receiver address invalid ! address=" + to);
    }
    if (amount < 1) {
      throw new Exception("ERROR: amount invalid ! ");
    }
    Blockchain blockchain = Blockchain.createBlockchain(from);
    Transaction transaction = Transaction.createUTXOTransaction(from, to, amount, blockchain);
    blockchain.mineBlock(new Transaction[]{transaction});
    RocksDBStore.getInstance().closeDB();
    System.out.println("Success!");
  }

  /**
   * 打印帮助信息
   */
  private void help() {
    System.out.println("Usage:");
    System.out
        .println("  createwallet - Generates a new key-pair and saves it into the wallet file");
    System.out.println("  printaddresses - print all wallet address");
    System.out.println("  getbalance -address ADDRESS - Get balance of ADDRESS");
    System.out.println(
        "  createblockchain -address ADDRESS - Create a blockchain and send genesis block reward to ADDRESS");
    System.out.println("  printchain - Print all the blocks of the blockchain");
    System.out.println(
        "  send -from FROM -to TO -amount AMOUNT - Send AMOUNT of coins from FROM address to TO");
    System.exit(0);
  }

  /**
   * 打印出区块链中的所有区块
   */
  private void printChain() throws Exception {
    Blockchain blockchain = Blockchain.initBlockchainFromDB();
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

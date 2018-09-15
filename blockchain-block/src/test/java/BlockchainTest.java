import me.wgy.model.Block;
import me.wgy.model.Blockchain;

/**
 * 区块链测试
 *
 * @author wgy
 * @date 2018/9/15
 */
public class BlockchainTest {

  public static void main(String[] args) {

    Blockchain blockchain = Blockchain.createBlockchain();
    blockchain.addBlock("Send 1 BTC to WGY");
    blockchain.addBlock("Send 2 BTC to Dayo Wang");

    for (Block block : blockchain.getBlockList()) {
      System.out.println("Prev. hash: " + block.getPrevBlockHash());
      System.out.println("Data: " + block.getData());
      System.out.println("Hash: " + block.getHash());
      System.out.println();
    }
  }
}

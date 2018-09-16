import me.wgy.block.consensus.ProofOfWork;
import me.wgy.block.model.Block;
import me.wgy.block.model.Blockchain;

/**
 * 区块链测试
 *
 * @author wgy
 * @date 2018/9/15
 */
public class BlockchainTest {

  public static void main(String[] args) {

    try {
      Blockchain blockchain = Blockchain.createBlockchain();

      blockchain.addBlock("Send 1.0 BTC to Dayo Wang");
      blockchain.addBlock("Send 2.0 more BTC to Dayo Wang");
      blockchain.addBlock("Send 3.0 more BTC to Dayo Wang");

      for (Blockchain.BlockchainIterator iterator = blockchain.getBlockchainIterator();
          iterator.hashNext(); ) {
        Block block = iterator.next();

        if (block != null) {
          boolean validate = ProofOfWork.createProofOfWork(block).validate();
          System.out.println(block.toString() + ", validate = " + validate);
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

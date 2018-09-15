package me.wgy.block.model;

import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 区块链
 *
 * @author wgy
 * @date 2018/9/15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Blockchain {

  private List<Block> blockList;

  /**
   * <p> 创建区块链 </p>
   */
  public static Blockchain createBlockchain() {
    List<Block> blocks = new LinkedList<>();
    blocks.add(Block.createGenesisBlock());
    return new Blockchain(blocks);
  }

  /**
   * <p> 添加区块  </p>
   */
  public void addBlock(String data) {
    Block previousBlock = blockList.get(blockList.size() - 1);
    this.addBlock(Block.createBlock(previousBlock.getHash(), data));
  }

  /**
   * <p> 添加区块  </p>
   */
  public void addBlock(Block block) {
    this.blockList.add(block);
  }
}

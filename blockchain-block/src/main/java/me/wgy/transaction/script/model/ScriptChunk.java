package me.wgy.transaction.script.model;

import static com.google.common.base.Preconditions.checkState;
import static me.wgy.transaction.script.model.ScriptOpCodes.OP_16;
import static me.wgy.transaction.script.model.ScriptOpCodes.OP_PUSHDATA4;
import static me.wgy.transaction.script.model.ScriptOpCodes.getOpCodeName;
import static me.wgy.transaction.script.model.ScriptOpCodes.getPushDataName;

import com.google.common.io.BaseEncoding;
import java.io.IOException;
import java.io.OutputStream;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 脚本元素基本组块
 *
 * @author wgy
 * @date 2018/9/26
 */
@Data
@AllArgsConstructor
public class ScriptChunk {

  /**
   * 操作码 {@link ScriptOpCodes}
   */
  private int opcode;
  private byte[] data;

  public boolean equalsOpCode(int opcode) {
    return opcode == this.opcode;
  }

  /**
   * 如果该组块是非推送数据内容的单个字节（可能是OP_RESERVED或某些无效的操作码）
   */
  public boolean isOpCode() {
    return opcode > OP_PUSHDATA4;
  }

  /**
   * 如果该组块是pushdata内容（包括单字节pushdatas），则返回true。
   */
  public boolean isPushData() {
    return opcode <= OP_16;
  }


  public void write(OutputStream stream) throws IOException {
    if (isOpCode()) {
      checkState(data == null);
      stream.write(opcode);
    }
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    if (isOpCode()) {
      buf.append(getOpCodeName(opcode));
    } else if (data != null) {
      // Data chunk
      buf.append(getPushDataName(opcode))
          .append("[")
          .append(BaseEncoding.base16().lowerCase().encode(data))
          .append("]");
    } else {
      // Small num
      buf.append(Script.decodeFromOpN(opcode));
    }
    return buf.toString();
  }
}

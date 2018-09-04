package entity.transcation;

import util.EncryptTool;
import util.StringTool;

import java.security.PublicKey;

/**
 * 交易输出类--出账
 * 资金发送方发送资金金额给资金接收方
 *
 * @author Dayo
 * @date 2018/9/4
 */
public class TransactionOutput {
    public String id;

    /**
     * 资金接收方的公钥（钱包地址）
     */
    public PublicKey reciepient;

    /**
     * 资金发送方拥有的资金金额
     */
    public float value;

    /**
     * 交易类Transaction的transactionId
     */
    public String parentTransactionId;

    /**
     * 构造方法
     */
    public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
        this.reciepient = reciepient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = EncryptTool.applySha256(StringTool.getStringFromKey(reciepient) + Float.toString(value) + parentTransactionId);
    }

    /**
     * 校验这个钱是否属于资金接收方
     */
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == reciepient);
    }
}

package co.emblock.sdk.crypto;

import java.math.BigInteger;

public class RawTransaction {

    private BigInteger nonce;
    private BigInteger gasPrice;
    private BigInteger gasLimit;
    private String to;
    private BigInteger value;
    private String data;

    protected RawTransaction(BigInteger nonce,
                             BigInteger gasPrice,
                             BigInteger gasLimit,
                             String to,
                             BigInteger value,
                             String data) {
        this.nonce = nonce;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.to = to;
        this.value = value;
        this.data = data != null ? Numeric.cleanHexPrefix(data) : null;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public String getTo() {
        return to;
    }

    public BigInteger getValue() {
        return value;
    }

    public String getData() {
        return data;
    }
}
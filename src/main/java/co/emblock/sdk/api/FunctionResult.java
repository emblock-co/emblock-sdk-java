package co.emblock.sdk.api;

import org.web3j.crypto.RawTransaction;

public class FunctionResult {
    private String callId;
    private String txHash;
    private RawTransaction txRaw;

    public FunctionResult(String callId, String txHash, RawTransaction txRaw) {
        this.callId = callId;
        this.txHash = txHash;
        this.txRaw = txRaw;
    }

    public String getCallId() {
        return callId;
    }

    public String getTxHash() {
        return txHash;
    }

    public RawTransaction getTxRaw() {
        return txRaw;
    }
}
package co.emblock.sdk.api;

public class FunctionResult {
    private String callId;
    private String txHash;

    public FunctionResult(String callId, String txHash) {
        this.callId = callId;
        this.txHash = txHash;
    }

    public String getCallId() {
        return callId;
    }

    public String getTxHash() {
        return txHash;
    }
}
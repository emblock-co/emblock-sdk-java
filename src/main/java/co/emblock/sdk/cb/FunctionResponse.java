package co.emblock.sdk.cb;

public class FunctionResponse {
    private final boolean success;
    private final String txHash;

    public FunctionResponse(boolean success, String txHash) {
        this.success = success;
        this.txHash = txHash;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getTxHash() {
        return txHash;
    }

}

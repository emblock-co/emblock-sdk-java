package co.emblock.sdk.api;

public class CallRawBody {
    private final String signature;

    public CallRawBody(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature;
    }
}
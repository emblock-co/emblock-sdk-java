package co.emblock.sdk.cb;

public interface FunctionCallback {
    void onResponse(boolean success, String txHash, Throwable e);
}

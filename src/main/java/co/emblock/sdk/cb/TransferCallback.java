package co.emblock.sdk.cb;

public interface TransferCallback {
    void onResponse(boolean success, Throwable e);
}

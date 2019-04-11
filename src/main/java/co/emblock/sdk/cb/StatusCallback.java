package co.emblock.sdk.cb;

public interface StatusCallback {
    void onResponse(boolean success, Throwable e);
}
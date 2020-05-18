package co.emblock.sdk.cb;

public interface FunctionCallSignatureCallback {
    void onResponse(boolean success, String signature, Throwable e);
}
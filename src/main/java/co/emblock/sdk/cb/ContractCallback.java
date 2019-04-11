package co.emblock.sdk.cb;

public interface ContractCallback {
    void onResponse(String contractId, Throwable e);
}
package co.emblock.sdk.cb;

import co.emblock.sdk.api.ConstantResult;

import java.util.List;

public interface ConstantCallback {
    void onResponse(List<ConstantResult> results, Throwable e);
}
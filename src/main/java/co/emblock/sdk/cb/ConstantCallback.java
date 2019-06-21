package co.emblock.sdk.cb;

import co.emblock.sdk.api.ParamResult;

import java.util.List;

public interface ConstantCallback {
    void onResponse(List<ParamResult> results, Throwable e);
}
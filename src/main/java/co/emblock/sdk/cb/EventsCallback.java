package co.emblock.sdk.cb;

import co.emblock.sdk.api.EventResult;

import java.util.List;

public interface EventsCallback {
    void onResponse(List<EventResult> events, Throwable e);
}

package co.emblock.sdk;

import co.emblock.sdk.api.Param;

import java.util.List;

public interface EventsListener {
    void onEvent(String eventName, List<Param> params, Throwable e);
}
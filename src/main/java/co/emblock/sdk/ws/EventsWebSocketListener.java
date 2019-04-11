package co.emblock.sdk.ws;

import co.emblock.sdk.api.Param;

import java.util.List;

public interface EventsWebSocketListener {
    void onEvent(String eventName, List<Param> params);
    void onError(Exception ex);
}
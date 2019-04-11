package co.emblock.sdk.ws;

import co.emblock.sdk.api.EventMessage;
import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class EventsWebSocketClient extends WebSocketClient {

    private final String contractId;
    private final EventsWebSocketListener listener;

    public EventsWebSocketClient(URI serverUri, String contractId, EventsWebSocketListener listener) {
        super(serverUri);
        this.contractId = contractId;
        this.listener = listener;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        String message = String.format("{\"type\":\"contract_events\", \"data\": {\"contractId\":\"%s\"}}", contractId);
        send(message);
    }

    @Override
    public void onMessage(String message) {
        Gson gson = new Gson();
        EventMessage eventMessage = gson.fromJson(message, EventMessage.class);
        listener.onEvent(eventMessage.getName(), eventMessage.getParams());
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println();
        listener.onError(new Exception("webSocket closed, code=" + code + " reason=" + reason + " remote=" + remote));
    }

    @Override
    public void onError(Exception ex) {
        listener.onError(ex);
    }
}


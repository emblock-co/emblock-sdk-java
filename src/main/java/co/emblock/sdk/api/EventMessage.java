package co.emblock.sdk.api;

import java.util.List;

public class EventMessage {

    private final String name;
    private final List<Param> params;

    public EventMessage(String name, List<Param> params) {
        this.name = name;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public List<Param> getParams() {
        return params;
    }
}



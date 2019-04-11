package co.emblock.sdk.api;

public class Param {
    private final String type;
    private final String value;

    public Param(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
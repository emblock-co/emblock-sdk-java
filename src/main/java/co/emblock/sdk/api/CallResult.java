package co.emblock.sdk.api;

public class CallResult {
    private String status;
    private String log;

    public CallResult(String status, String log) {
        this.status = status;
        this.log = log;
    }

    public String getStatus() {
        return status;
    }

    public String getLog() {
        return log;
    }
}
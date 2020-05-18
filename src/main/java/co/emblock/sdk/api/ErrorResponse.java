package co.emblock.sdk.api;

public class ErrorResponse {
    private final Integer code;
    private final String status;
    private final String message;

    public ErrorResponse(Integer code, String status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
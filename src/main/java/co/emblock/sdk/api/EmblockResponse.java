package co.emblock.sdk.api;

import java.util.List;

public class EmblockResponse {
    private List<ParamResult> results;

    EmblockResponse(List<ParamResult> results) {
        this.results = results;
    }
    public List<ParamResult> getResults() {
        return results;
    }
}
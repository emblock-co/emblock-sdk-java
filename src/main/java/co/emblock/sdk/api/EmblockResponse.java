package co.emblock.sdk.api;

import java.util.List;

public class EmblockResponse {
    private List<ConstantResult> results;

    EmblockResponse(List<ConstantResult> results) {
        this.results = results;
    }
    public List<ConstantResult> getResults() {
        return results;
    }
}
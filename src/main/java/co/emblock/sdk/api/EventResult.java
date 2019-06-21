package co.emblock.sdk.api;

import java.util.List;

public class EventResult {

    private final String name;
    private final String transactionHash;
    private final Long blockTimestamp;
    private final Long blockDate;
    private final List<ParamResult> params;

    public EventResult(String name, String transactionHash, Long blockTimestamp, Long blockDate, List<ParamResult> params) {
        this.name = name;
        this.transactionHash = transactionHash;
        this.blockTimestamp = blockTimestamp;
        this.blockDate = blockDate;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public Long getBlockTimestamp() {
        return blockTimestamp;
    }

    public Long getBlockDate() {
        return blockDate;
    }

    public List<ParamResult> getParams() {
        return params;
    }

}

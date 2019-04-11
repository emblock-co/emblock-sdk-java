package co.emblock.sdk.api;

public class ContractResult {
    private ContractDetails details;

    public ContractResult(ContractDetails details) {
        this.details = details;
    }

    public ContractDetails getDetails() {
        return details;
    }
}


package co.emblock.sdk.api;

public class TransferBody {
    private final String networkId;
    private final String amount;
    private final String to;

    public TransferBody(String networkId, String amount, String to) {
        this.networkId = networkId;
        this.amount = amount;
        this.to = to;
    }

    public String getNetworkId() {
        return networkId;
    }

    public String getAmount() {
        return amount;
    }

    public String getTo() {
        return to;
    }
}
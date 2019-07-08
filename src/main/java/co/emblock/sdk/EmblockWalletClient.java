package co.emblock.sdk;

import co.emblock.sdk.api.*;
import co.emblock.sdk.cb.*;
import co.emblock.sdk.ws.EventsWebSocketClient;
import co.emblock.sdk.ws.EventsWebSocketListener;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.utils.Numeric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static co.emblock.sdk.EmblockUtils.checkNotEmptyOrNull;

/**
 * This is a Java SDK to interact with a project/smart contract deployed on the Emblock platform.
 */
public class EmblockWalletClient {

    private final EmblockApi emblockApi;

    //private final String serverUrl = "https://api.emblock.co";
    private final String serverUrl = "http://localhost:9000";
    private final String wsUrl = "wss://api.emblock.co/notifs";

    public static String ETH_NETWORK_POA_EMBLOCK = "5132";
    public static String ETH_NETWORK_ROPSTEN = "3";

    public EmblockWalletClient(final String apiToken) {
        checkNotEmptyOrNull(apiToken, "apiToken cannot be null or empty");

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder().addHeader("Authorization", "Bearer " + apiToken).build();
                    return chain.proceed(request);
                })
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build();

        emblockApi = retrofit.create(EmblockApi.class);
    }

    public void transfer(
            String walletAddress,
            String networkId,
            String amount,
            String to,
            TransferCallback cb
    ) {
        TransferBody body = new TransferBody(networkId, amount, to);
        emblockApi
                .transfer(walletAddress, body)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        cb.onResponse(response.isSuccessful(), null);
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        cb.onResponse(false, t);
                    }
                });
    }

}
package co.emblock.sdk;

import co.emblock.sdk.api.*;
import co.emblock.sdk.cb.*;
import co.emblock.sdk.crypto.Credentials;
import co.emblock.sdk.crypto.Numeric;
import co.emblock.sdk.crypto.RawTransaction;
import co.emblock.sdk.crypto.TransactionEncoder;
import co.emblock.sdk.ws.EventsWebSocketClient;
import co.emblock.sdk.ws.EventsWebSocketListener;
import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static co.emblock.sdk.EmblockUtils.checkNotEmptyOrNull;

/**
 * This is a Java SDK to interact with a project/smart contract deployed on the Emblock platform.
 */
public class EmblockClient {

    public static final String SERVER_URL = "https://api.emblock.co";
    public static final String WS_URL = "wss://api.emblock.co/notifs";
    private final EmblockApi emblockApi;
    private final String projectId;
    private final String wsUrl;
    private EventsListener eventsListener;
    private EventsWebSocketClient wsClient;

    public EmblockClient(final String apiToken, final String projectId) {
        this(apiToken, projectId, false, SERVER_URL, WS_URL);
    }

    public EmblockClient(final String apiToken, final String projectId, final Boolean logging, final String serverUrl, final String wsUrl) {
        checkNotEmptyOrNull(apiToken, "apiToken cannot be null or empty");
        checkNotEmptyOrNull(projectId, "projectId cannot be null or empty");
        this.projectId = projectId;
        this.wsUrl = wsUrl;

        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder().addHeader("Authorization", "Bearer " + apiToken).build();
                    return chain.proceed(request);
                })
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS);

        if (logging) okHttpBuilder.addInterceptor(new HttpLoggingInterceptor());

        OkHttpClient okHttpClient = okHttpBuilder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        emblockApi = retrofit.create(EmblockApi.class);
    }

    /**
     * Call a constant function of the smart contract or get the value of a state
     *
     * @param functionName name of the constant function to call
     * @param parameters   function parameters if needed
     * @param cb           callback containing the result
     */
    public void callConstant(String functionName, Map<String, String> parameters, final ConstantCallback cb) {
        Call<List<ParamResult>> call = emblockApi.callConstant(projectId, functionName, parameters);
        call.enqueue(new Callback<List<ParamResult>>() {
            @Override
            public void onResponse(Call<List<ParamResult>> call, retrofit2.Response<List<ParamResult>> response) {
                if (response.isSuccessful()) {
                    List<ParamResult> body = response.body();
                    cb.onResponse(body, null);
                } else {
                    try {
                        EmblockClientException e = handleResponseError(response);
                        cb.onResponse(null, e);
                    } catch (IOException e) {
                        cb.onResponse(null, e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ParamResult>> call, Throwable t) {
                cb.onResponse(null, t);
            }
        });
    }

    public List<ParamResult> callConstant(String functionName, Map<String, String> parameters) throws IOException, EmblockClientException {
        Call<List<ParamResult>> call = emblockApi.callConstant(projectId, functionName, parameters);
        Response<List<ParamResult>> response = call.execute();
        if (response.isSuccessful()) {
            List<ParamResult> body = response.body();
            return body;
        }
        throw handleResponseError(response);
    }

    /**
     * Call a smart contract function.
     *
     * @param walletAddress address of the sender that calls the function
     * @param functionName  name of the function to call
     * @param parameters    map of the functions parameters. {"param1": "value1", "param2" :"value2"}
     * @param cb            callback to get the function call response containing a "functionCallId" that you need to use to get the function call status.
     */
    public void callFunction(String walletAddress, String functionName, Map<String, String> parameters, final FunctionCallback cb) {
        Call<FunctionResult> call = emblockApi.callFunction(walletAddress, projectId, functionName, parameters);
        call.enqueue(new Callback<FunctionResult>() {
            @Override
            public void onResponse(Call<FunctionResult> call, retrofit2.Response<FunctionResult> response) {
                if (response.isSuccessful()) {
                    FunctionResult body = response.body();
                    getFunctionStatus(body.getCallId(), (success, e) -> {
                        if (e != null) {
                            cb.onResponse(false, null, e);
                        } else {
                            String txHash = response.body().getTxHash();
                            cb.onResponse(success, txHash, null);
                        }
                    });
                } else {
                    try {
                        EmblockClientException e = handleResponseError(response);
                        cb.onResponse(false, null, e);
                    } catch (IOException e) {
                        cb.onResponse(false, null, e);
                    }
                }
            }

            @Override
            public void onFailure(Call<FunctionResult> call, Throwable t) {
                cb.onResponse(false, null, t);
            }
        });
    }

    private <T> EmblockClientException handleResponseError(Response<T> response) throws IOException {
        String content = response.errorBody().string();
        ErrorResponse error = new Gson().fromJson(content, ErrorResponse.class);
        String errorMessage = "Error code " + response.code();
        if (error != null) errorMessage += ": " + error.getMessage();
        return new EmblockClientException(errorMessage);
    }

    /**
     * Call a smart contract function but sign the transaction on client side.
     * We need the private key for signing but it's not send to the server.
     *
     * @param privateKey   privateKey needed for client side signature
     * @param publicKey    we send it to the server to encode the transaction
     * @param functionName name of the function to call
     * @param parameters   parameters passed to the function
     * @param cb           callback to get the function call response containing a "functionCallId" that you need to use to get the function call status.
     */
    public void callFunctionWithClientSideSignature(String privateKey, String publicKey, String functionName, Map<String, String> parameters, final FunctionCallback cb) {
        Call<FunctionResult> call = emblockApi.callFunction(publicKey, projectId, functionName, parameters);
        call.enqueue(new Callback<FunctionResult>() {
            @Override
            public void onResponse(Call<FunctionResult> call, retrofit2.Response<FunctionResult> response) {
                if (response.isSuccessful()) {
                    FunctionResult body = response.body();
                    RawTransaction rawTx = body.getTxRaw();
                    String callId = body.getCallId();

                    if (rawTx != null) {
                        Credentials credentials = Credentials.create(privateKey);
                        byte[] signatureData = TransactionEncoder.signMessage(rawTx, credentials);
                        String hexString = Numeric.toHexString(signatureData);

                        emblockApi.callRaw(callId, new CallRawBody(hexString)).enqueue(new Callback<FunctionResult>() {
                            @Override
                            public void onResponse(
                                    Call<FunctionResult> call,
                                    Response<FunctionResult> response
                            ) {
                                if (response.isSuccessful()) {
                                    String txHash = response.body().getTxHash();
                                    getFunctionStatus(body.getCallId(), (success, e) -> {
                                        if (e != null) {
                                            cb.onResponse(false, txHash, e);
                                        } else {
                                            cb.onResponse(success, txHash, null);
                                        }
                                    });
                                } else {
                                    try {
                                        EmblockClientException e = handleResponseError(response);
                                        cb.onResponse(false, null, e);
                                    } catch (IOException e) {
                                        cb.onResponse(false, null, e);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<FunctionResult> call, Throwable t) {
                                cb.onResponse(false, null, t);
                            }
                        });
                    } else {
                        // TxRaw can be null if anything happened before sending it on blockchain
                        cb.onResponse(false, null, new Exception("This should not happened, TxRaw is null. Please send an issue on our github."));
                    }
                } else {
                    try {
                        EmblockClientException e = handleResponseError(response);
                        cb.onResponse(false, null, e);
                    } catch (IOException e) {
                        cb.onResponse(false, null, e);
                    }
                }

            }

            @Override
            public void onFailure(Call<FunctionResult> call, Throwable t) {
                cb.onResponse(false, null, t);
            }
        });
    }

    public FunctionResponse callFunctionWithClientSideSignature(String privateKey, String publicKey, String functionName, Map<String, String> parameters) throws IOException, EmblockClientException {
        Call<FunctionResult> call = emblockApi.callFunction(publicKey, projectId, functionName, parameters);
        Response<FunctionResult> response = call.execute();
        if (response.isSuccessful()) {
            FunctionResult body = response.body();
            RawTransaction rawTx = body.getTxRaw();
            String callId = body.getCallId();

            if (rawTx != null) {
                Credentials credentials = Credentials.create(privateKey);
                byte[] signatureData = TransactionEncoder.signMessage(rawTx, credentials);
                String hexString = Numeric.toHexString(signatureData);

                Response<FunctionResult> rawResponse = emblockApi.callRaw(callId, new CallRawBody(hexString)).execute();
                if (rawResponse.isSuccessful()) {
                    String txHash = rawResponse.body().getTxHash();
                    boolean success = getFunctionStatus(body.getCallId());
                    return new FunctionResponse(success, txHash);
                } else {
                    throw handleResponseError(rawResponse);
                }
            } else {
                throw new IllegalStateException("This should not happened, TxRaw is null. Please send an issue on our github.");
            }
        } else {
            throw handleResponseError(response);
        }
    }

    public void getFunctionCallSignature(String privateKey, String publicKey, String functionName, Map<String, String> parameters, final FunctionCallSignatureCallback cb) {
        Call<FunctionResult> call = emblockApi.callFunction(publicKey, projectId, functionName, parameters);
        call.enqueue(new Callback<FunctionResult>() {
            @Override
            public void onResponse(Call<FunctionResult> call, retrofit2.Response<FunctionResult> response) {
                try {
                    String signature = handleGetFunctionCallSignature(privateKey, response);
                    cb.onResponse(true, signature, null);
                } catch (Exception e) {
                    cb.onResponse(false, null, e);
                }
            }

            @Override
            public void onFailure(Call<FunctionResult> call, Throwable t) {
                cb.onResponse(false, null, t);
            }
        });
    }

    public String getFunctionCallSignature(String privateKey, String publicKey, String functionName, Map<String, String> parameters) throws Exception {
        Call<FunctionResult> call = emblockApi.callFunction(publicKey, projectId, functionName, parameters);
        Response<FunctionResult> response = call.execute();
        return handleGetFunctionCallSignature(privateKey, response);
    }

    private String handleGetFunctionCallSignature(String privateKey, Response<FunctionResult> response) throws EmblockClientException, IOException {
        if (response.isSuccessful()) {
            FunctionResult body = response.body();
            RawTransaction txRaw = body.getTxRaw();

            if (txRaw != null) {
                Credentials credentials = Credentials.create(privateKey);
                byte[] signatureData = TransactionEncoder.signMessage(txRaw, credentials);
                return Numeric.toHexString(signatureData);
            } else {
                // txRaw can be null if transaction has been reverted
                throw new IllegalStateException("This should not happened, TxRaw is null. Please send an issue on our github.");
            }
        } else {
            throw handleResponseError(response);
        }
    }

    /**
     * Get a function status (Successful|Failed) from a callId
     *
     * @param callId id of a call returned by the 'callFunction'
     * @param cb     callback
     */
    public void getFunctionStatus(String callId, final StatusCallback cb) {
        emblockApi
                .getCallStatus(callId)
                .enqueue(new Callback<CallResult>() {
                    @Override
                    public void onResponse(Call<CallResult> call, retrofit2.Response<CallResult> response) {
                        CallResult body = response.body();
                        cb.onResponse("Successful".equals(body.getStatus()), null);
                    }

                    @Override
                    public void onFailure(Call<CallResult> call, Throwable t) {
                        cb.onResponse(false, t);
                    }
                });
    }

    public boolean getFunctionStatus(String callId) throws IOException {
        Response<CallResult> response = emblockApi.getCallStatus(callId).execute();
        CallResult body = response.body();
        return "Successful".equals(body.getStatus());
    }

    private void getCurrentContract(String projectId, final ContractCallback cb) {
        emblockApi
                .getCurrentContract(projectId)
                .enqueue(new Callback<ContractResult>() {
                    @Override
                    public void onResponse(Call<ContractResult> call, retrofit2.Response<ContractResult> response) {
                        ContractResult body = response.body();
                        String contractId = body.getDetails().getId();
                        cb.onResponse(contractId, null);
                    }

                    @Override
                    public void onFailure(Call<ContractResult> call, Throwable t) {
                        cb.onResponse(null, t);
                    }
                });
    }

    /**
     * Listen to events sent by your smart contract.
     *
     * @param eventsListener a listener
     */
    public void addEventsListener(EventsListener eventsListener) {
        this.eventsListener = eventsListener;
        getCurrentContract(projectId, (contractId, e) -> {
            System.out.println("contratID=" + contractId);
            if (e != null) {
                eventsListener.onEvent(null, null, e);
            } else {
                URI uri = URI.create(wsUrl);
                wsClient = new EventsWebSocketClient(uri, contractId, new EventsWebSocketListener() {
                    @Override
                    public void onEvent(String eventName, List<Param> params) {
                        eventsListener.onEvent(eventName, params, null);
                    }

                    @Override
                    public void onError(Exception ex) {
                        eventsListener.onEvent(null, null, ex);
                    }
                });
                wsClient.connect();
            }
        });
    }

    public void removeEventsListener() {
        this.eventsListener = null;
        if (wsClient != null) {
            wsClient.close();
            wsClient = null;
        }
    }

    public void getEvents(EventsCallback cb) {
        emblockApi
                .getEvents(projectId)
                .enqueue(new Callback<List<EventResult>>() {
                    @Override
                    public void onResponse(Call<List<EventResult>> call, Response<List<EventResult>> response) {
                        List<EventResult> events = response.body();
                        cb.onResponse(events, null);
                    }

                    @Override
                    public void onFailure(Call<List<EventResult>> call, Throwable t) {
                        cb.onResponse(null, t);
                    }
                });
    }

}
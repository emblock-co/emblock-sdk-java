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

import static co.emblock.sdk.EmblockUtils.checkNotEmptyOrNull;

/**
 * This is a Java SDK to interact with a project/smart contract deployed on the Emblock platform.
 */
public class EmblockClient {

    private final EmblockApi emblockApi;
    private final String projectId;

    private EventsListener eventsListener;
    private EventsWebSocketClient wsClient;

    private final String serverUrl = "https://api.emblock.co";
    private final String wsUrl = "wss://api.emblock.co/notifs";

    public EmblockClient(final String apiToken, final String projectId) {
        checkNotEmptyOrNull(apiToken, "apiToken cannot be null or empty");
        checkNotEmptyOrNull(projectId, "projectId cannot be null or empty");
        this.projectId = projectId;

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder().addHeader("Authorization", "Bearer " + apiToken).build();
                    return chain.proceed(request);
                })
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
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
                List<ParamResult> body = response.body();
                cb.onResponse(body, null);
            }

            @Override
            public void onFailure(Call<List<ParamResult>> call, Throwable t) {
                cb.onResponse(null, t);
            }
        });
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
                FunctionResult body = response.body();
                getFunctionStatus(body.getCallId(), (success, e) -> {
                    if (e != null) {
                        cb.onResponse(false, e);
                    } else {
                        cb.onResponse(success, null);
                    }
                });
            }

            @Override
            public void onFailure(Call<FunctionResult> call, Throwable t) {
                cb.onResponse(false, t);
            }
        });
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
                            getFunctionStatus(body.getCallId(), (success, e) -> {
                                if (e != null) {
                                    cb.onResponse(false, e);
                                } else {
                                    cb.onResponse(success, null);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<FunctionResult> call, Throwable t) {
                            cb.onResponse(false, t);
                        }
                    });
                } else {
                    //TODO rawTx is null
                    cb.onResponse(false, new Exception("This should not happened, TxRaw is null. Please send an issue on our github."));
                }

            }

            @Override
            public void onFailure(Call<FunctionResult> call, Throwable t) {
                cb.onResponse(false, t);
            }
        });
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
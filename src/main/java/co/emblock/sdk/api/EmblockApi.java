package co.emblock.sdk.api;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface EmblockApi {

    @POST("/projects/{projectId}/calls/current/{function}")
    Call<List<ParamResult>> callConstant(
            @Path("projectId") String projectId,
            @Path("function") String function,
            @Body Map<String, String> parameters
    );

    @POST("/projects/{projectId}/calls/current/{function}")
    Call<FunctionResult> callFunction(
            @Header("wallet") String walletAddress,
            @Path("projectId") String projectId,
            @Path("function") String function,
            @Body Map<String, String> parameters
    );

    @POST("/calls/{callId}/raw")
    Call<FunctionResult> callRaw(
            @Path("callId") String callId,
            @Body CallRawBody body
    );

    @GET("/calls/{callId}/status")
    Call<CallResult> getCallStatus(
            @Path("callId") String callId
    );

    @GET("/projects/{projectId}/contracts/current")
    Call<ContractResult> getCurrentContract(
            @Path("projectId") String projectId
    );

    @GET("/projects/{projectId}/events/current")
    Call<List<EventResult>> getEvents(
            @Path("projectId") String projectId
    );

    @POST("/wallets/{publicKey}/transfer")
    Call<Void> transfer(
            @Path("publicKey") String from,
            @Body TransferBody body
    );

}




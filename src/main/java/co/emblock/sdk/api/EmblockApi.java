package co.emblock.sdk.api;

import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface EmblockApi {

    @POST("/projects/{projectId}/calls/current/{function}")
    Call<List<ConstantResult>> callConstant(
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

    @GET("/calls/{callId}/status")
    Call<CallResult> getCallStatus(
            @Path("callId") String callId
    );

    @GET("/projects/{projectId}/contracts/current")
    Call<ContractResult> getCurrentContract(
            @Path("projectId") String projectId
    );

}
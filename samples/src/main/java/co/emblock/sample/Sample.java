package co.emblock.sample;

import co.emblock.sdk.EmblockClient;
import co.emblock.sdk.api.ConstantResult;
import co.emblock.sdk.api.Param;

import java.util.HashMap;
import java.util.Map;

public class Sample {


    public static void main(String... args) {
        String apiKey = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbGV4QGVtYmxvY2suY28iLCJpc3MiOiJlbWJsb2NrIiwiaWF0IjoxNTU1ODA5MzA1fQ.MQdyZxyagHQNaLjRFdPlYNERkhxMWONf_n4CkncmZJw";
        String projectId = "e49b5165-6cca-451f-bdbc-a27de79aba93";
        EmblockClient emblockClient = new EmblockClient(apiKey, projectId);

        // subscribe to all events
        emblockClient.addEventsListener((eventName, params, e) -> {
            if (e == null) {
                System.out.println("eventName=" + eventName);
                for (Param param : params) {
                    System.out.println("param type=" + param.getType() + " value=" + param.getValue());
                }
            } else e.printStackTrace();
        });

        getBalance(emblockClient);

        String sender = "0xF923c87B3C143a44053C44904724C03CC704342e";
        Map<String, String> params = new HashMap<>();
        params.put("to", "0x905C22656bB3a2BC457Ac8AA4131264b89D59e91");
        params.put("value", "100");
        emblockClient.callFunction(sender, "transfer", params, (success, e) -> {
            if (e == null) {
                if (success) {
                    getBalance(emblockClient);
                } else {
                    System.out.println("function call failed");
                }
            }
        });
    }

    private static void getBalance(EmblockClient emblockClient) {
        Map<String, String> cParams = new HashMap<>();
        emblockClient.callConstant("balanceOf", cParams, (results, e) -> {
            if (e == null) {
                for (ConstantResult result : results) {
                    System.out.println("result name=" + result.getName() + " value=" + result.getValue() + " type=" + result.getType());
                }
            } else e.printStackTrace();
        });
    }

}

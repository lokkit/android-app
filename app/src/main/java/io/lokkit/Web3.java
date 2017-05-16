package io.lokkit;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Nick on 15.05.2017.
 */

@Deprecated
public class Web3 {
    private Web3Bridge web3;
    private String host;

    public Web3(String host) {
        this.web3 = new Web3Bridge();
        this.host = host;
    }

    public JSONObject createRequest(String protocol, String method) {
        JSONObject j = new JSONObject();
        try {
            j.put("method", protocol + "_" + method);
            j.put("id", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return j;
    }

    private JSONObject send(JSONObject requestBody) {
        return web3.sendRequest(this.host, requestBody);
    }

    public String generateSymmetricKey() {
        JSONObject request = createRequest("shh", "generateSymmetricKey");
        JSONObject response = send(request);
        try {
            return response.getString("response");
        } catch (JSONException e) {
            String error = "";
            try {
                error = response.getJSONObject("error").getString("message");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    public enum ShhType {
        sym, asym
    }

    public JSONObject post(ShhType type, int ttl, String topic, float powTarget, int powTimeSeconds, String payload, String key) {
        return null;
    }
}

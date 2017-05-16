package io.lokkit;

import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Web3Bridge {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client;

    public Web3Bridge() {
        OkHttpClient.Builder b = new OkHttpClient.Builder();
        b.readTimeout(310, TimeUnit.SECONDS);
        client = b.build();
    }

    public JSONObject sendRequest(final String host, final JSONObject json) {
        try {
            return new JSONObject(sendRequest(host, json.toString()));
        } catch (JSONException e) {
            e.printStackTrace();
            return null; // :)
        }
    }

    public String sendRequest(final String host, final String json) {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(host)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string().trim();
        } catch (IOException e) {
            e.printStackTrace();
            return null; // haha return null. what a fail.
        }
    }
}
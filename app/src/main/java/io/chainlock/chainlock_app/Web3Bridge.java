package io.chainlock.chainlock_app;

import android.os.Build;

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
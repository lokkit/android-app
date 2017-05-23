package io.lokkit;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.Semaphore;
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
        try {
            final Semaphore s = new Semaphore(1);
            s.acquire();
            final String[] v = {""};
            sendRequestAsync(host, json, new Web3Callback() {
                @Override
                public void callback(String value) {
                    v[0] = value;
                    s.release();
                }
            });
            s.acquire();
            return v[0];
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void sendRequestAsync(final String host, final String json, final Web3Callback c) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestBody body = RequestBody.create(JSON, json);
                Request request = new Request.Builder()
                        .url(host)
                        .post(body)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    c.callback(response.body().string().trim());
                } catch (IOException e) {
                    e.printStackTrace();
                    c.callback(null);
                }
            }
        }).start();
    }
}
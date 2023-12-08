package com.aerotrack.utils.Api;


import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class HttpUtils {

    public static String HttpGetRequest(String url, Headers headers) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .headers(headers)
                .build();

        try (Response response = new OkHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new IOException("Unexpected response: " + response);
            return response.body().string();
        }
    }
}
package com.aerotrack.utils.clients.api;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;

import java.io.IOException;

@Slf4j
public class CurrencyConverterApiClient {

    private final CurrencyApiService currencyApiService;

    public static CurrencyConverterApiClient create() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://cdn.jsdelivr.net/gh/fawazahmed0/currency-api@1/latest/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        return new CurrencyConverterApiClient(retrofit.create(CurrencyApiService.class));
    }

    public CurrencyConverterApiClient(CurrencyApiService currencyApiService) {
        this.currencyApiService = currencyApiService;
    }

    public double getConversionFactor(String currencyFrom, String currencyTo) {
        if(currencyTo.equals(currencyFrom))
            return 1.0;
        try {
            String response = currencyApiService.getCurrencyRate(currencyFrom.toLowerCase(), currencyTo.toLowerCase()).execute().body();
            if (response != null) {
                JSONObject jsonResponse = new JSONObject(response);
                double conversionRate = jsonResponse.optDouble(currencyTo.toLowerCase(), -1);
                if (conversionRate != -1) {
                    return conversionRate;
                } else {
                    log.error("Conversion rate not found for currencies {} to {}", currencyFrom, currencyTo);
                    throw new RuntimeException("Conversion rate not found for currencies");
                }
            } else {
                log.error("Null response from currency API.");
                throw new RuntimeException("Null response from currency API");
            }
        } catch (IOException e) {
            log.error("IOException caught while calling currency API: " + e.getMessage());
            throw new RuntimeException("IOException caught while calling currency API", e);
        }
    }

    public interface CurrencyApiService {
        @GET("currencies/{from}/{to}.json")
        retrofit2.Call<String> getCurrencyRate(
                @retrofit2.http.Path("from") String from,
                @retrofit2.http.Path("to") String to
        );
    }
}

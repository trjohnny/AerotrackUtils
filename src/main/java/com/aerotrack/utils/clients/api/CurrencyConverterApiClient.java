package com.aerotrack.utils.clients.api;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;

import java.io.IOException;

@Slf4j
public class CurrencyConverterApiClient {

    private final CurrencyApiService currencyApiService;

    public static CurrencyConverterApiClient create() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        return new CurrencyConverterApiClient(retrofit.create(CurrencyApiService.class));
    }

    public CurrencyConverterApiClient(CurrencyApiService currencyApiService) {
        this.currencyApiService = currencyApiService;
    }

    public double getConversionFactor(String currencyFrom, String currencyTo) {
        if (currencyTo.equals(currencyFrom)) return 1.0;

        try {
            Response<String> response = currencyApiService.getCurrencyRate(currencyFrom.toLowerCase()).execute();

            // Log full request URL
            log.info("Requested URL: " + response.raw().request().url());

            if (response.isSuccessful() && response.body() != null) {

                JSONObject jsonResponse = new JSONObject(response.body());
                JSONObject conversions = jsonResponse.getJSONObject(currencyFrom);
                double conversionRate = conversions.optDouble(currencyTo.toLowerCase(), -1);

                if (conversionRate != -1) {
                    return conversionRate;
                } else {
                    log.error("Conversion rate not found for currencies {} to {}", currencyFrom, currencyTo);
                    throw new RuntimeException("Conversion rate not found");
                }
            } else {
                // Log error response
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                System.out.println("Error response body: " + errorBody);
                throw new RuntimeException("Unsuccessful response from currency API");
            }
        } catch (IOException e) {
            log.error("IOException caught while calling currency API: " + e.getMessage());
            throw new RuntimeException("IOException caught", e);
        }
    }

    public interface CurrencyApiService {
        @GET("v1/currencies/{from}.json")
        retrofit2.Call<String> getCurrencyRate(@retrofit2.http.Path("from") String from);
    }
}

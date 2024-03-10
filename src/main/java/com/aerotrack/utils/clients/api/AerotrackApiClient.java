package com.aerotrack.utils.clients.api;

import com.aerotrack.model.entities.AerotrackStage;
import com.aerotrack.model.entities.Airport;
import com.aerotrack.model.entities.AirportsJsonFile;
import com.aerotrack.model.entities.Trip;
import com.aerotrack.model.exceptions.AerotrackClientException;
import com.aerotrack.model.protocol.ScanQueryRequest;
import com.aerotrack.model.protocol.ScanQueryResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
public class AerotrackApiClient {

    private final AerotrackStage stage;

    private final ApiGatewayService apiGatewayService;

    public static AerotrackApiClient create(AerotrackStage stage) {
        // Configure OkHttpClient with custom timeouts
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Set connection timeout
                .readTimeout(30, TimeUnit.SECONDS)     // Set read timeout
                .writeTimeout(30, TimeUnit.SECONDS)    // Set write timeout
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(stage.getApiEndpoint().baseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .client(okHttpClient)
                .build();

        return new AerotrackApiClient(stage, retrofit.create(ApiGatewayService.class));
    }

    public List<Trip> getBestFlight(ScanQueryRequest scanQueryRequest) {
        try {
            System.out.println("Endpoint: " + stage.getApiEndpoint().baseUrl());
            ScanQueryResponse response = apiGatewayService.sendScanQueryRequest(stage.getApiEndpoint().apiKey(), scanQueryRequest)
                    .execute()
                    .body();

            if (response != null) {
                return response.getTrips();
            }
        } catch (IOException e) {
            log.error("Error in API request: " + e.getMessage());
            throw new AerotrackClientException("Error in API request: ", e);
        }
        throw new RuntimeException("Response is null");
    }

    public AirportsJsonFile getAirportsJson() {
        try {
            AirportsJsonFile ryanairAirports = apiGatewayService.sendRyanairAirportsJSONRequest(stage.getApiEndpoint().apiKey()).execute().body();
            AirportsJsonFile wizzairAirports = apiGatewayService.sendWizzairAirportsJSONRequest(stage.getApiEndpoint().apiKey()).execute().body();

            // Merge both airport lists, ensuring no duplicates
            Set<Airport> mergedAirports = new HashSet<>();
            if (ryanairAirports != null) {
                mergedAirports.addAll(ryanairAirports.getAirports());
            }
            if (wizzairAirports != null) {
                mergedAirports.addAll(wizzairAirports.getAirports());
            }

            return new AirportsJsonFile(new HashSet<>(mergedAirports));

        } catch (IOException e) {
            log.error("Error in API request: " + e.getMessage());
            throw new AerotrackClientException("Error in API request: ", e);
        } catch (Exception e) {
            log.error("Error when calling getAirportsJson: " + e.getMessage());
            throw new AerotrackClientException("Error when calling getAirportsJson: ", e);
        }
    }

    public interface ApiGatewayService {
        @GET("airports/ryanair")
        retrofit2.Call<AirportsJsonFile> sendRyanairAirportsJSONRequest(@Header("x-api-key") String apiKey);
        @GET("airports/wizzair")
        retrofit2.Call<AirportsJsonFile> sendWizzairAirportsJSONRequest(@Header("x-api-key") String apiKey);
        @POST("scan")
        retrofit2.Call<ScanQueryResponse> sendScanQueryRequest(@Header("x-api-key") String apiKey, @Body ScanQueryRequest request);
    }

}

package com.aerotrack.utils.clients.api;

import com.aerotrack.model.entities.AerotrackStage;
import com.aerotrack.model.entities.AirportsJsonFile;
import com.aerotrack.model.entities.Trip;
import com.aerotrack.model.exceptions.AerotrackClientException;
import com.aerotrack.model.protocol.ScanQueryRequest;
import com.aerotrack.model.protocol.ScanQueryResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

import java.io.IOException;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class AerotrackApiClient {

    private final AerotrackStage stage;

    private final ApiGatewayService apiGatewayService;

    public static AerotrackApiClient create(AerotrackStage stage) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(stage.getApiEndpoint().baseUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        return new AerotrackApiClient(stage, retrofit.create(ApiGatewayService.class));
    }

    public List<Trip> getBestFlight(ScanQueryRequest scanQueryRequest) {
        try {
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

    public AirportsJsonFile getAirportsJson(){
        try {
            AirportsJsonFile response = apiGatewayService.sendAirportsJSONRequest(stage.getApiEndpoint().apiKey())
                    .execute()
                    .body();

            if (response != null){
                return response;
            }
        } catch (IOException e) {
            log.error("Error in API request: " + e.getMessage());
            throw new AerotrackClientException("Error in API request: ", e);
        }
        throw new RuntimeException("Response is null");
    }

    public interface ApiGatewayService {
        @GET("airports")
        retrofit2.Call<AirportsJsonFile> sendAirportsJSONRequest(@Header("x-api-key") String apiKey);
        @POST("scan")
        retrofit2.Call<ScanQueryResponse> sendScanQueryRequest(@Header("x-api-key") String apiKey, @Body ScanQueryRequest request);
    }
}

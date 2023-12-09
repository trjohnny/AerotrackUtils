package com.aerotrack.utils.clients.apigateway;

import com.aerotrack.model.entities.FlightPair;
import com.aerotrack.model.exceptions.AerotrackClientException;
import com.aerotrack.model.protocol.ScanQueryRequest;
import com.aerotrack.model.protocol.ScanQueryResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

import java.io.IOException;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class AerotrackApiClient {

    private static final String BASE_URL = "https://f1muce19kh.execute-api.eu-west-1.amazonaws.com/prod/";
    private static final String API_KEY = "z9inDLaWtOamHqvOCl25w33KtSbqVpOf61oPHGhK";

    private final ApiGatewayService apiGatewayService;

    public static AerotrackApiClient create() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        return new AerotrackApiClient(retrofit.create(ApiGatewayService.class));
    }

    public List<FlightPair> getBestFlight(ScanQueryRequest scanQueryRequest) {
        try {
            ScanQueryResponse response = apiGatewayService.sendScanQueryRequest(API_KEY, scanQueryRequest).execute().body();
            if (response != null) {
                return response.getFlightPairs();
            }
        } catch (IOException e) {
            log.error("Error in API request: " + e.getMessage());
            throw new AerotrackClientException("Error in API request: ", e);
        }
        throw new RuntimeException("Response is null");
    }

    public interface ApiGatewayService {
        @POST("scan")
        retrofit2.Call<ScanQueryResponse> sendScanQueryRequest(@Header("x-api-key") String apiKey, @Body ScanQueryRequest request);
    }
}

package com.aerotrack.utils.clients.apigateway;

import com.aerotrack.model.exceptions.AerotrackClientException;
import com.aerotrack.model.exceptions.ApiRequestException;
import com.aerotrack.model.entities.FlightPair;
import com.aerotrack.model.protocol.ScanQueryRequest;
import com.aerotrack.model.protocol.ScanQueryResponse;
import com.aerotrack.utils.clients.ApiClientWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class ApiGatewayClient {
    private final static String personalEndpointUrl = "https://f1muce19kh.execute-api.eu-west-1.amazonaws.com/prod/scan";
    private final static String alphaEndpointUrl = "https://u4ck1qvmfe.execute-api.eu-west-1.amazonaws.com/prod/scan";
    private final static String prodEndpointUrl = "https://bfq0c9l0dl.execute-api.eu-west-1.amazonaws.com/prod/scan";
    private final static String apiKey = "z9inDLaWtOamHqvOCl25w33KtSbqVpOf61oPHGhK";

    private final ApiClientWrapper apiClientWrapper;

    public static ApiGatewayClient create() {
        return new ApiGatewayClient(new ApiClientWrapper());
    }

    public List<FlightPair> getBestFlight(String startDateString,String endDateString,String minDurationString,String maxDurationString,List<String> departureAirports,Boolean returnToSameAirport, List<String> destinationAirports){
        ScanQueryRequest scanQueryRequest = buildScanQueryRequest(startDateString,endDateString,minDurationString,maxDurationString,departureAirports,returnToSameAirport,destinationAirports);
        return sendScanQueryRequest(scanQueryRequest).getFlightPairs();
    }

    public ScanQueryRequest buildScanQueryRequest(String startDateString,String endDateString,String minDurationString,String maxDurationString,List<String> departureAirports,Boolean returnToSameAirport, List<String> destinationAirports) {
        ScanQueryRequest.ScanQueryRequestBuilder builder = ScanQueryRequest.builder();

        // Imposta i valori dal flightInfoFields
        builder.minDays(Integer.valueOf(minDurationString));
        builder.maxDays(Integer.valueOf(maxDurationString));
        builder.availabilityStart(startDateString);
        builder.availabilityEnd(endDateString);
        builder.departureAirports(departureAirports);
        builder.returnToSameAirport(returnToSameAirport);
        builder.destinationAirports(destinationAirports);
        return builder.build();
    }
    public ScanQueryResponse sendScanQueryRequest(ScanQueryRequest scanQueryRequest) {
        try {
            return apiClientWrapper.sendPostRequest(personalEndpointUrl, Optional.of(apiKey), scanQueryRequest, ScanQueryResponse.class);
        } catch (ApiRequestException e) {
            log.error("Error in API request: " + e.getMessage());
            throw new AerotrackClientException(e); // Rilancia l'eccezione per consentire la gestione a livelli superiori, se necessario
        }
    }
}




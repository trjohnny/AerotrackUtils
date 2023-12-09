package com.aerotrack.utils.clients;

import com.aerotrack.model.entities.FlightPair;
import com.aerotrack.model.exceptions.AerotrackClientException;
import com.aerotrack.model.exceptions.ApiRequestException;
import com.aerotrack.model.protocol.ScanQueryRequest;
import com.aerotrack.model.protocol.ScanQueryResponse;
import com.aerotrack.utils.clients.apigateway.ApiGatewayClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ApiGatewayClientTest {

    @Mock
    private ApiClientWrapper mockApiClientWrapper;

    private ApiGatewayClient apiGatewayClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        apiGatewayClient = new ApiGatewayClient(mockApiClientWrapper);
    }

    @Test
    void getBestFlight_Success() {
        // Mock the ScanQueryResponse
        ScanQueryResponse mockResponse = new ScanQueryResponse();
        mockResponse.setFlightPairs(Arrays.asList(new FlightPair(), new FlightPair())); // Add mock FlightPairs

        when(mockApiClientWrapper.sendPostRequest(anyString(), any(), any(ScanQueryRequest.class), eq(ScanQueryResponse.class)))
                .thenReturn(mockResponse);

        List<FlightPair> result = apiGatewayClient.getBestFlight("2023-01-01", "2023-01-10", "3", "7", Arrays.asList("LAX"), true, Arrays.asList("JFK"));

        assertNotNull(result);
        assertEquals(2, result.size()); // Assuming 2 FlightPairs were mocked
    }

    @Test
    void getBestFlight_ApiException() {
        when(mockApiClientWrapper.sendPostRequest(anyString(), any(), any(ScanQueryRequest.class), eq(ScanQueryResponse.class)))
                .thenThrow(new ApiRequestException("API request failed"));

        assertThrows(AerotrackClientException.class, () ->
                apiGatewayClient.getBestFlight("2023-01-01", "2023-01-10", "3", "7", List.of("LAX"), true, List.of("JFK"))
        );
    }
}

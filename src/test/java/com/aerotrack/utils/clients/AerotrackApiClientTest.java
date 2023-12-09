package com.aerotrack.utils.clients;

import com.aerotrack.model.entities.Flight;
import com.aerotrack.model.entities.FlightPair;
import com.aerotrack.model.protocol.ScanQueryRequest;
import com.aerotrack.model.protocol.ScanQueryResponse;
import com.aerotrack.utils.clients.apigateway.AerotrackApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AerotrackApiClientTest {

    @Mock
    private AerotrackApiClient.ApiGatewayService mockApiGatewayService;

    @Mock
    private Call<ScanQueryResponse> mockCall;

    private AerotrackApiClient aerotrackApiClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        aerotrackApiClient = new AerotrackApiClient(mockApiGatewayService);
    }

    @Test
    void getBestFlight_Success() throws IOException {
        Flight outboundFlight = new Flight("VCE", "DUB", "2023-01-01T08:00:00", "2023-01-01T10:00:00", "OB123", 150.0);
        Flight returnFlight = new Flight("DUB", "VCE", "2023-01-10T18:00:00", "2023-01-10T20:00:00", "RT456", 200.0);
        FlightPair flightPair = new FlightPair(outboundFlight, returnFlight, 350);

        ScanQueryResponse scanQueryResponse = new ScanQueryResponse(List.of(flightPair));

        when(mockApiGatewayService.sendScanQueryRequest(anyString(), any(ScanQueryRequest.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(Response.success(scanQueryResponse));

        List<FlightPair> result = aerotrackApiClient.getBestFlight(new ScanQueryRequest());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("OB123", result.get(0).getOutboundFlight().getFlightNumber());
        assertEquals("RT456", result.get(0).getReturnFlight().getFlightNumber());
        assertEquals(350, result.get(0).getTotalPrice());
    }

    @Test
    void getBestFlight_Failure() throws IOException {
        when(mockApiGatewayService.sendScanQueryRequest(anyString(), any(ScanQueryRequest.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new IOException("Failed to execute request"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            aerotrackApiClient.getBestFlight(new ScanQueryRequest());
        });

        String expectedMessage = "Error in API request:";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    // Additional test cases can be added to cover different scenarios
}

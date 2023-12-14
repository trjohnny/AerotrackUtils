package com.aerotrack.utils.clients;

import com.aerotrack.model.entities.AerotrackStage;
import com.aerotrack.model.entities.Flight;
import com.aerotrack.model.entities.Trip;
import com.aerotrack.model.protocol.ScanQueryRequest;
import com.aerotrack.model.protocol.ScanQueryResponse;
import com.aerotrack.utils.clients.api.AerotrackApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
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
        aerotrackApiClient = new AerotrackApiClient(AerotrackStage.ALPHA, mockApiGatewayService);
    }

    @Test
    void getBestFlight_Success() throws IOException {
        Flight outboundFlight = new Flight("VCE", "DUB", "2023-01-01T08:00:00.000Z", "2023-01-01T10:00:00.000Z", "OB123", 150.0);
        Flight returnFlight = new Flight("DUB", "VCE", "2023-01-10T18:00:00.000Z", "2023-01-10T20:00:00.000Z", "RT456", 200.0);
        Trip flightPair = new Trip(List.of(outboundFlight), List.of(returnFlight), 350);

        ScanQueryResponse scanQueryResponse = new ScanQueryResponse(List.of(flightPair));

        when(mockApiGatewayService.sendScanQueryRequest(anyString(), any(ScanQueryRequest.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(Response.success(scanQueryResponse));

        List<Trip> result = aerotrackApiClient.getBestFlight(new ScanQueryRequest());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("OB123", result.get(0).getOutboundFlights().get(0).getFlightNumber());
        assertEquals("RT456", result.get(0).getReturnFlights().get(0).getFlightNumber());
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

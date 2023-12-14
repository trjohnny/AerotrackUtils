package com.aerotrack.utils.clients;

import com.aerotrack.model.entities.Airport;
import com.aerotrack.model.entities.Flight;
import com.aerotrack.utils.clients.api.RyanairApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import retrofit2.Call;
import retrofit2.Response;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RyanairApiClientTest {

    @Mock
    private RyanairApiClient.RyanairApiService mockApiService;

    @Mock
    private Call<String> mockCall;

    private RyanairApiClient ryanairApiClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ryanairApiClient = new RyanairApiClient(mockApiService);
    }

    @Test
    void getFlights_Success() throws Exception {
        String jsonResponse =
                "{\"currency\":\"EUR\",\"trips\":[{\"dates\":[{\"flights\":[{\"timeUTC\":[\"2023-01-01T10:00:00.000Z\",\"2023-01-01T12:00:00.000Z\"], \"regularFare\":{\"fares\":[{\"amount\":100.0}]}, \"flightNumber\":\"FR123\"}]}]}]}";
        when(mockApiService.getFlights(any(), any(), any(), any(), any(), any(), any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(Response.success(jsonResponse));

        List<Flight> flights = ryanairApiClient.getFlights("OriginCode", "DestinationCode", LocalDate.of(2023, 1, 1)).getFlights();

        assertNotNull(flights);
        assertFalse(flights.isEmpty());

        Flight firstFlight = flights.get(0);
        assertEquals("FR123", firstFlight.getFlightNumber());
        assertEquals(100.0, firstFlight.getPrice());
    }

    @Test
    void getFlights_Failure() throws Exception {
        when(mockApiService.getFlights(any(), any(), any(), any(), any(), any(), any())).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new RuntimeException("API request failed"));

        Exception exception = assertThrows(RuntimeException.class, () ->
                ryanairApiClient.getFlights("OriginCode", "DestinationCode", LocalDate.of(2023, 1, 1))
        );
    }


    @Test
    void getAirportConnections_Success() throws Exception {
        String jsonResponse = "[{\"arrivalAirport\":{\"code\":\"TEST1\"}}, {\"arrivalAirport\":{\"code\":\"TEST2\"}}]";
        when(mockApiService.getAirportConnections(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(Response.success(jsonResponse));

        List<String> connections = ryanairApiClient.getAirportConnections("OriginCode");

        assertNotNull(connections);
        assertFalse(connections.isEmpty());
        assertEquals(2, connections.size());
        assertTrue(connections.contains("TEST1"));
        assertTrue(connections.contains("TEST2"));
    }

    @Test
    void getAirportConnections_Failure() throws Exception {
        when(mockApiService.getAirportConnections(any())).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new RuntimeException("API request failed"));

        assertThrows(RuntimeException.class, () ->
                ryanairApiClient.getAirportConnections("OriginCode")
        );
    }

    @Test
    void getAvailableAirports_Success() throws Exception {
        String jsonResponse = "[{\"code\":\"AAA\",\"name\":\"Airport A\",\"country\":{\"code\":\"C1\"}}, {\"code\":\"BBB\",\"name\":\"Airport B\",\"country\":{\"code\":\"C2\"}}]";
        when(mockApiService.getActiveAirports()).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(Response.success(jsonResponse));
        List<Airport> airports = ryanairApiClient.getAvailableAirports();

        assertNotNull(airports);
        assertFalse(airports.isEmpty());
        assertEquals(2, airports.size());
        assertEquals("Airport A", airports.get(0).getName());
        assertEquals("C1", airports.get(0).getCountryCode());
        assertEquals("Airport B", airports.get(1).getName());
        assertEquals("C2", airports.get(1).getCountryCode());
    }

    @Test
    void getAvailableAirports_Failure() throws Exception {
        when(mockApiService.getActiveAirports()).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new RuntimeException("API request failed"));

        assertThrows(RuntimeException.class, () ->
                ryanairApiClient.getAvailableAirports()
        );
    }
}

package com.aerotrack.utils.clients;

import com.aerotrack.model.entities.Flight;
import com.aerotrack.utils.clients.ryanair.RyanairClient;
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

class RyanairClientTest {

    @Mock
    private RyanairClient.RyanairApiService mockApiService;

    @Mock
    private Call<String> mockCall;

    private RyanairClient ryanairClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ryanairClient = new RyanairClient(mockApiService);
    }

    @Test
    void getFlights_Success() throws Exception {
        String jsonResponse = // Your JSON mock response
                "{\"trips\":[{\"dates\":[{\"flights\":[{\"timeUTC\":[\"2023-01-01T10:00:00\",\"2023-01-01T12:00:00\"], \"regularFare\":{\"fares\":[{\"amount\":100.0}]}, \"flightNumber\":\"FR123\"}]}]}]}";
        when(mockApiService.getFlights(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(Response.success(jsonResponse));

        List<Flight> flights = ryanairClient.getFlights("OriginCode", "DestinationCode", LocalDate.of(2023, 1, 1));

        assertNotNull(flights);
        assertFalse(flights.isEmpty());

        Flight firstFlight = flights.get(0);
        assertEquals("FR123", firstFlight.getFlightNumber());
        assertEquals(100.0, firstFlight.getPrice()); // Assuming the price is 100.0 as per the mock response
    }

    @Test
    void getFlights_Failure() throws Exception {
        when(mockApiService.getFlights(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(mockCall);
        when(mockCall.execute()).thenThrow(new RuntimeException("API request failed"));

        Exception exception = assertThrows(RuntimeException.class, () ->
                ryanairClient.getFlights("OriginCode", "DestinationCode", LocalDate.of(2023, 1, 1))
        );

        String expectedMessage = "Caught exception while calling Ryanair API.";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }
}

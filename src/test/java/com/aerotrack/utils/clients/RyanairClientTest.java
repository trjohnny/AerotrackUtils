package com.aerotrack.utils.clients;

import com.aerotrack.model.entities.Flight;
import com.aerotrack.utils.clients.ryanair.RyanairClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class RyanairClientTest {
    @Mock
    private ApiClientWrapper mockApiClientWrapper;
    private RyanairClient ryanairClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ryanairClient = new RyanairClient(mockApiClientWrapper);
    }

    @Test
    void getFlights_Success() {
        String mockResponse = "{ \"trips\": [{ \"dates\": [{ \"flights\": [" +
                "{\"timeUTC\": [\"2023-01-01T12:00:00\", \"2023-01-01T15:00:00\"], " +
                "\"regularFare\": {\"fares\": [{\"amount\": 100.0}]}, " +
                "\"flightNumber\": \"FR123\"}]}]}]}";
        when(mockApiClientWrapper.sendGetRequest(anyString(), eq(Optional.empty()), eq(String.class)))
                .thenReturn(mockResponse);

        List<Flight> result = ryanairClient.getFlights("LAX", "JFK", LocalDate.of(2023, 1, 1));

        assertNotNull(result);
        assertEquals(1, result.size()); // Assuming 1 mock Flight was returned
        Flight flight = result.get(0);
        assertEquals("FR123", flight.getFlightNumber());
        assertEquals(100.0, flight.getPrice());
    }

    @Test
    void getFlights_NoFlightsAvailable() {
        String mockResponse = "{ \"trips\": [] }"; // Simulate a response with no flights
        when(mockApiClientWrapper.sendGetRequest(anyString(), eq(Optional.empty()), eq(String.class)))
                .thenReturn(mockResponse);

        List<Flight> result = ryanairClient.getFlights("LAX", "JFK", LocalDate.of(2023, 1, 1));

        assertTrue(result.isEmpty());
    }
}

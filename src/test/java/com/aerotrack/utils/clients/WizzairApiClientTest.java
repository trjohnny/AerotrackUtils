package com.aerotrack.utils.clients;

import com.aerotrack.model.entities.Flight;
import com.aerotrack.model.entities.FlightList;
import com.aerotrack.utils.clients.api.WizzairApiClient;
import com.aerotrack.utils.clients.api.WizzairApiClient.WizzAirApiService;
import com.aerotrack.model.entities.TimetableRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WizzairApiClientTest {

    @Mock
    private WizzAirApiService mockApiService;

    @Mock
    private Call<String> mockCall;

    private WizzairApiClient client;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        // Set up the mock behavior for the API service
        when(mockApiService.getTimetable(any(TimetableRequest.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(Response.success("{\"outboundFlights\":[{\"departureStation\":\"VIE\",\"arrivalStation\":\"LON\",\"departureDate\":\"2024-03-02T00:00:00\",\"price\":{\"amount\":100.0,\"currencyCode\":\"EUR\"},\"departureDates\":[\"2024-03-02T06:00:00\"]}]}"));

        // Create the client instance with the mocked API service
        client = new WizzairApiClient(mockApiService);
    }

    @Test
    void getFlightsTest() {
        // Prepare test data
        String fromAirportCode = "VIE";
        String toAirportCode = "LON";
        LocalDate date = LocalDate.of(2024, 3, 2); // Specific date for testing

        // Execute the test method
        FlightList result = client.getFlights(fromAirportCode, toAirportCode, date);

        // Verify the interaction with the mock
        verify(mockApiService).getTimetable(any(TimetableRequest.class));

        // Assert the result
        assertNotNull(result, "Result should not be null");
        assertFalse(result.getFlights().isEmpty(), "Expected to find at least one flight");
        Flight flight = result.getFlights().get(0);
        assertEquals("VIE-LON", flight.getDirection(), "Flight direction should match");
        assertEquals(100.0, flight.getPrice(), "Flight price should match");
    }

    @Test
    @Disabled("This test makes real API calls and should not be run with the regular test suite.")
    void getFlightsTest_RealCall() {
        // Prepare test data
        String fromAirportCode = "TSF";
        String toAirportCode = "VCE";
        LocalDate date = LocalDate.of(2024, 3, 2); // Specific date for testing

        WizzairApiClient realClient = WizzairApiClient.create();

        // Execute the test method
        FlightList result = realClient.getFlights(fromAirportCode, toAirportCode, date);
        System.out.println(result.getFlights().toString());


        // Assert the result
        assertNotNull(result, "Result should not be null");
        assertFalse(result.getFlights().isEmpty(), "Expected to find at least one flight");
    }
}

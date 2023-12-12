package com.aerotrack.utils.clients;

import com.aerotrack.model.entities.Flight;
import com.aerotrack.utils.clients.dynamodb.AerotrackDynamoDbClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AerotrackDynamoDbClientTest {
    @Mock
    private DynamoDbTable<Flight> mockFlightTable;
    @Mock
    private PageIterable<Flight> mockPageIterable;
    @Mock
    private SdkIterable<Flight> mockSdkIterable;
    private AerotrackDynamoDbClient aerotrackDynamoDbClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        aerotrackDynamoDbClient = new AerotrackDynamoDbClient(mockFlightTable);
    }

    @Test
    void scanFlightsBetweenDates_Success() {
        Flight flight = new Flight("VCE", "DUB", "2021-01-01T00:00:00.000Z", "2021-01-02T00:00:00.000Z", "FR830", 50);

        when(mockFlightTable.query(any(QueryConditional.class))).thenReturn(mockPageIterable);
        when(mockPageIterable.items()).thenReturn(mockSdkIterable);
        when(mockSdkIterable.stream()).thenReturn(Stream.of(flight));

        List<Flight> result = aerotrackDynamoDbClient.scanFlightsBetweenDates("LAX", "JFK", "2023-01-01T00:00:00.000Z", "2023-01-10T00:00:00.000Z");

        assertEquals(1, result.size());
        assertEquals(result.get(0), flight);
    }
}

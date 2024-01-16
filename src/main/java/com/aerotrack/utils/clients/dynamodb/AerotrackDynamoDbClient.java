package com.aerotrack.utils.clients.dynamodb;

import com.aerotrack.model.entities.Flight;
import com.aerotrack.common.Constants;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;

@AllArgsConstructor
public class AerotrackDynamoDbClient {
    private static final TableSchema<Flight> FLIGHT_TABLE_SCHEMA = TableSchema.fromClass(Flight.class);
    private final DynamoDbTable<Flight> flightTable;
    public static AerotrackDynamoDbClient create() {
        return new AerotrackDynamoDbClient(DynamoDbEnhancedClient.create().table(
                System.getenv(Constants.FLIGHT_TABLE_ENV_VAR), FLIGHT_TABLE_SCHEMA));
    }

    public List<Flight> scanFlightsBetweenDates(String departure, String destination, String availabilityStart, String availabilityEnd) {
        String partitionKey = getDirection(departure, destination);
        Key startKey = Key.builder().partitionValue(partitionKey).sortValue(availabilityStart).build();
        Key endKey = Key.builder().partitionValue(partitionKey).sortValue(availabilityEnd).build();

        return flightTable.query(QueryConditional.sortBetween(startKey, endKey))
                .items()
                .stream()
                .toList();
    }

    private String getDirection(String departure, String destination) {
        return departure + "-" + destination;
    }
}

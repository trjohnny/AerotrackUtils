package com.aerotrack.utils.clients.dynamodb;

import com.aerotrack.model.Flight;
import com.aerotrack.model.TableObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;

import static com.aerotrack.utils.Constants.FLIGHT_TABLE_ENV_VAR;

@AllArgsConstructor
public class AerotrackDynamoDbClient {
    private static final TableSchema<Flight> FLIGHT_TABLE_SCHEMA = TableSchema.fromClass(Flight.class);
    private final DynamoDbTable<Flight> flightTable;
    public static AerotrackDynamoDbClient create() {
        return new AerotrackDynamoDbClient(DynamoDbEnhancedClient.create().table(
                System.getenv(FLIGHT_TABLE_ENV_VAR), FLIGHT_TABLE_SCHEMA));
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

    // TODO: move to public static utils class
    private String getDirection(String departure, String destination) {
        return departure + "-" + destination;
    }
}

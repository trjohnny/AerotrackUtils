package com.aerotrack.utils;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final String FLIGHT_TABLE_ENV_VAR = "FLIGHT_TABLE";
    public static final String AIRPORTS_OBJECT_NAME = "airports.json";
    public static final String AIRPORTS_BUCKET_ENV_VAR = "AIRPORTS_BUCKET";

    public static final String METRIC_REFRESH_FLIGHTS_NAMESPACE = "RefreshLambdaMetric";
    public static final String METRIC_REFRESH_FLIGHTS_API_CALLS = "SuccessfulAPICalls";
    public static final Map<String, String> METRIC_REFRESH_FLIGHTS_API_CALLS_DIMENSIONS = new HashMap<>() {
        {
            put("SuccessfulAPICalls", "Count");
        }
    };

}

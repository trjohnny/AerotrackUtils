package com.aerotrack.utils.clients.ryanair;

import com.aerotrack.model.entities.Flight;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class RyanairClient {

    private final RyanairApiService ryanairApiService;

    public static RyanairClient create() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.ryanair.com/api/booking/v4/it-it/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        return new RyanairClient(retrofit.create(RyanairApiService.class));
    }

    public List<Flight> getFlights(String fromAirportCode, String toAirportCode, LocalDate date) {
        try {
            String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String response = ryanairApiService.getFlights("1", fromAirportCode, toAirportCode, "false", "0", formattedDate, "false", "AGREED")
                    .execute()
                    .body();
            return parseFlights(response, fromAirportCode, toAirportCode); // Parse the JSON string
        } catch (Exception e) {
            log.error("Error in calling Ryanair API: " + e.getMessage());
            throw new RuntimeException("Caught exception while calling Ryanair API.", e);
        }
    }

    private List<Flight> parseFlights(String jsonResponse, String fromAirportCode, String toAirportCode) {
        List<Flight> flights = new ArrayList<>();
        try {
            JSONObject flightDetails = new JSONObject(jsonResponse);

            JSONArray trips = flightDetails.optJSONArray("trips");
            if (trips == null || trips.isEmpty()) {
                return flights;
            }

            JSONArray dates = trips.getJSONObject(0).optJSONArray("dates");
            if (dates == null || dates.isEmpty()) {
                return flights;
            }

            JSONArray flightsArray = dates.getJSONObject(0).optJSONArray("flights");
            if (flightsArray == null || flightsArray.isEmpty()) {
                return flights;
            }

            for (int i = 0; i < flightsArray.length(); i++) {
                JSONObject flight = flightsArray.getJSONObject(i);

                String timeFrom = flight.getJSONArray("timeUTC").getString(0);
                String timeTo = flight.getJSONArray("timeUTC").getString(1);
                double price = flight.getJSONObject("regularFare").getJSONArray("fares").getJSONObject(0).getDouble("amount");
                String flightNumber = flight.getString("flightNumber");

                Flight flightItem = new Flight(fromAirportCode, toAirportCode, timeFrom, timeTo, flightNumber, price);
                flights.add(flightItem);
            }
        } catch (Exception e) {
            // Handle exception (log or throw as appropriate)
            log.error(e.getMessage());
            throw new RuntimeException("Caught exception while calling Ryanair API.", e);
        }

        return flights;
    }

    public interface RyanairApiService {
        @GET("availability")
        retrofit2.Call<String> getFlights(
                @Query("ADT") String adultCount,
                @Query("Origin") String fromAirportCode,
                @Query("Destination") String toAirportCode,
                @Query("IncludeConnectingFlights") String includeConnectingFlights,
                @Query("Disc") String discount,
                @Query("DateOut") String dateOut,
                @Query("RoundTrip") String roundTrip,
                @Query("ToUs") String termsOfUse
        );
    }
}

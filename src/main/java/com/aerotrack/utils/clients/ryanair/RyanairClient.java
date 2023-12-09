package com.aerotrack.utils.clients.ryanair;

import com.aerotrack.model.entities.Flight;
import com.aerotrack.model.exceptions.ApiRequestException;
import com.aerotrack.utils.clients.ApiClientWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class RyanairClient {
    private final ApiClientWrapper apiClientWrapper;

    public static RyanairClient create() {
        return new RyanairClient(new ApiClientWrapper());
    }

    private static final String RYANAIR_AVAILABILITY_API = "https://www.ryanair.com/api/booking/v4/it-it/availability?ADT=1&Origin=%s" +
            "&Destination=%s&IncludeConnectingFlights=false&Disc=0&DateOut=%s&RoundTrip=false&ToUs=AGREED";

    public List<Flight> getFlights(String fromAirportCode, String toAirportCode, LocalDate date) {
        String ryanairURL = String.format(RYANAIR_AVAILABILITY_API, fromAirportCode, toAirportCode, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        List<Flight> flights = new ArrayList<>();
        try {
            JSONObject flightDetails = new JSONObject(apiClientWrapper.sendGetRequest(ryanairURL, Optional.empty(), String.class));

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
}

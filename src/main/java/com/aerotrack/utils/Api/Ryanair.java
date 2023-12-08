package com.aerotrack.utils.Api;

import com.aerotrack.model.Flight;
import okhttp3.Headers;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Ryanair {

    private static final String RYANAIR_AVAILABILITY_API = "https://www.ryanair.com/api/booking/v4/it-it/availability?ADT=1&Origin=%s" +
            "&Destination=%s&IncludeConnectingFlights=false&Disc=0&DateOut=%s&RoundTrip=false&ToUs=AGREED";


    public static List<Flight> getFlights(String fromAirportCode, String toAirportCode, LocalDate date) throws IOException {
        String ryanairURL = String.format(RYANAIR_AVAILABILITY_API, fromAirportCode, toAirportCode, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        String jsonResponse = HttpUtils.HttpGetRequest(ryanairURL, getDefaultHeaders());

        List<Flight> flights = new ArrayList<>();
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

        return flights;
    }

    private static Headers getDefaultHeaders() {
        return new Headers.Builder()
                .add("Cookie", "rid.sig=jyna6R42wntYgoTpqvxHMK7H+KyM6xLed+9I3KsvYZaVt7P36AL6zp9dGFPu5uVxaIiFpNXrszr+LfNCdY3IT3oCSYLeNv/ujtjsDqOzkY66AL3V6kH2vsK+au12X21HkZ4S8GaG8CoBmm/m0rLsOKYkxtw+U3+ejBaPc15jJjKXnc3owMBg82SNbqyKjVd6Z6qcsoE25p3RmlcaHuHC3GBf1yIGtlqeQun3Mj0vmSURVPQBjK65pge2zGBymoORV6PsmZ0Nabv73gS2VkhG+Eccz5iSIRPrZm5cloi/941TFo8oiXdyqvTMx/ozox2fkvaKB2vd/goSx543TxPdKoGKRLaDY3FoIepe6I46UFvXEZYszzugXHYRnp0lbIn/HPyvHH/iW/TXRrqsELQabKd1hH+Ut1ZgpfsKEtwDVyL7mVvi1qEOqHddSVKCN/439KxQqi9K03dQDm+knQaLRpzZL8EYqCeSaosMOeEhc2CAYWLW2D5jH5iTot0YiyaN3QJFE59H3MYDpGjoZfTfen83yVSpT8DBahOOr6Eibv62bKQXBxel5kIm75dZB26iUzmkBs1Iags291UJ8wpu/GtBD1rghlZoRJt9u3ASkAj3P85dBcV8MwGykVWJ4mCO; mkt=/gb/en/; .AspNetCore.Session=CfDJ8NJy2CjeBXdEiTlIkEo9jP1x6eP6igWNkoeL9uCto1qdz97HQLRlCAJWIhw97YY5uemEBnTrcLNnoB7lqKOnEJRzF%2F4yhGKN9A5COUteaQmZduO6o2whfYqdyD1Qd%2B%2BH6EXjFP4cd0DtvPDwguugs%2Bc684C2CSfw16lyvYFtSkvU; fr-correlation-id=b8d2a2fe-383b-44fa-8c4a-ba6cab8c994d; rid=034ed121-51ec-4cac-9196-97a50ee42d2c; RY_COOKIE_CONSENT=true; STORAGE_PREFERENCES={\"STRICTLY_NECESSARY\":true,\"PERFORMANCE\":false,\"FUNCTIONAL\":false,\"TARGETING\":false,\"SOCIAL_MEDIA\":false,\"PIXEL\":false,\"GANALYTICS\":true,\"__VERSION\":2}; myRyanairID=")
                .add("User-Agent", "Mozilla/5.0 (Linux; Android 10; Pixel 3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Mobile Safari/537.36")
                .add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .add("Accept-Language", "en-US,en;q=0.5")
                .build();
    }

}
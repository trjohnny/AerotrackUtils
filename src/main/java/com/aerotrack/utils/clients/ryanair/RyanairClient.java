package com.aerotrack.utils.clients.ryanair;

import com.aerotrack.model.entities.Flight;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class RyanairClient {

    private final RyanairApiService ryanairApiService;
    private final static String RYANAIR_COOKIE = "rid.sig=jyna6R42wntYgoTpqvxHMK7H+KyM6xLed+9I3KsvYZaVt7P36AL6zp9dGFPu5uVxaIiFpNXrszr+LfNCdY3IT3oCSYLeNv/ujtjsDqOzkY66AL3V6kH2vsK+au12X21HkZ4S8GaG8CoBmm/m0rLsOKYkxtw+U3+ejBaPc15jJjKXnc3owMBg82SNbqyKjVd6Z6qcsoE25p3RmlcaHuHC3GBf1yIGtlqeQun3Mj0vmSURVPQBjK65pge2zGBymoORV6PsmZ0Nabv73gS2VkhG+Eccz5iSIRPrZm5cloi/941TFo8oiXdyqvTMx/ozox2fkvaKB2vd/goSx543TxPdKoGKRLaDY3FoIepe6I46UFvXEZYszzugXHYRnp0lbIn/HPyvHH/iW/TXRrqsELQabKd1hH+Ut1ZgpfsKEtwDVyL7mVvi1qEOqHddSVKCN/439KxQqi9K03dQDm+knQaLRpzZL8EYqCeSaosMOeEhc2CAYWLW2D5jH5iTot0YiyaN3QJFE59H3MYDpGjoZfTfen83yVSpT8DBahOOr6Eibv62bKQXBxel5kIm75dZB26iUzmkBs1Iags291UJ8wpu/GtBD1rghlZoRJt9u3ASkAj3P85dBcV8MwGykVWJ4mCO; mkt=/gb/en/; .AspNetCore.Session=CfDJ8NJy2CjeBXdEiTlIkEo9jP1x6eP6igWNkoeL9uCto1qdz97HQLRlCAJWIhw97YY5uemEBnTrcLNnoB7lqKOnEJRzF%2F4yhGKN9A5COUteaQmZduO6o2whfYqdyD1Qd%2B%2BH6EXjFP4cd0DtvPDwguugs%2Bc684C2CSfw16lyvYFtSkvU; fr-correlation-id=b8d2a2fe-383b-44fa-8c4a-ba6cab8c994d; rid=034ed121-51ec-4cac-9196-97a50ee42d2c; RY_COOKIE_CONSENT=true; STORAGE_PREFERENCES={\"STRICTLY_NECESSARY\":true,\"PERFORMANCE\":false,\"FUNCTIONAL\":false,\"TARGETING\":false,\"SOCIAL_MEDIA\":false,\"PIXEL\":false,\"GANALYTICS\":true,\"__VERSION\":2}; myRyanairID=";

    public static RyanairClient create() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.ryanair.com/api/booking/v4/it-it/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        return new RyanairClient(retrofit.create(RyanairApiService.class));
    }

    public List<Flight> getFlights(String fromAirportCode, String toAirportCode, LocalDate date) {
        try {

            Response<String> response = ryanairApiService.getFlights(
                    "rid.sig=jyna6R42wntYgoTpqvxHMK7H+KyM6xLed+9I3KsvYZaVt7P36AL6zp9dGFPu5uVxaIiFpNXrszr+LfNCdY3IT3oCSYLeNv/ujtjsDqOzkY66AL3V6kH2vsK+au12X21HkZ4S8GaG8CoBmm/m0rLsOKYkxtw+U3+ejBaPc15jJjKXnc3owMBg82SNbqyKjVd6Z6qcsoE25p3RmlcaHuHC3GBf1yIGtlqeQun3Mj0vmSURVPQBjK65pge2zGBymoORV6PsmZ0Nabv73gS2VkhG+Eccz5iSIRPrZm5cloi/941TFo8oiXdyqvTMx/ozox2fkvaKB2vd/goSx543TxPdKoGKRLaDY3FoIepe6I46UFvXEZYszzugXHYRnp0lbIn/HPyvHH/iW/TXRrqsELQabKd1hH+Ut1ZgpfsKEtwDVyL7mVvi1qEOqHddSVKCN/439KxQqi9K03dQDm+knQaLRpzZL8EYqCeSaosMOeEhc2CAYWLW2D5jH5iTot0YiyaN3QJFE59H3MYDpGjoZfTfen83yVSpT8DBahOOr6Eibv62bKQXBxel5kIm75dZB26iUzmkBs1Iags291UJ8wpu/GtBD1rghlZoRJt9u3ASkAj3P85dBcV8MwGykVWJ4mCO; mkt=/gb/en/; .AspNetCore.Session=CfDJ8NJy2CjeBXdEiTlIkEo9jP1x6eP6igWNkoeL9uCto1qdz97HQLRlCAJWIhw97YY5uemEBnTrcLNnoB7lqKOnEJRzF%2F4yhGKN9A5COUteaQmZduO6o2whfYqdyD1Qd%2B%2BH6EXjFP4cd0DtvPDwguugs%2Bc684C2CSfw16lyvYFtSkvU; fr-correlation-id=b8d2a2fe-383b-44fa-8c4a-ba6cab8c994d; rid=034ed121-51ec-4cac-9196-97a50ee42d2c; RY_COOKIE_CONSENT=true; STORAGE_PREFERENCES={\"STRICTLY_NECESSARY\":true,\"PERFORMANCE\":false,\"FUNCTIONAL\":false,\"TARGETING\":false,\"SOCIAL_MEDIA\":false,\"PIXEL\":false,\"GANALYTICS\":true,\"__VERSION\":2}; myRyanairID=",
                    "Mozilla/5.0 (Linux; Android 10; Pixel 3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Mobile Safari/537.36",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
                    "en-US,en;q=0.5", // HEADERS

                    "1",
                    fromAirportCode, // INPUTS
                    toAirportCode,
                    "false",
                    date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    "FALSE",
                    "AGREED")
                    .execute();

            if (!response.isSuccessful()) {
                log.error("Unsuccessful response from Ryanair API: {}", response);
                throw new HttpException(response);
            }

            String responseBody = response.body();
            if (responseBody == null) {
                log.error("Null response from Ryanair API.");
                throw new HttpException(response);
            }

            return parseFlights(responseBody, fromAirportCode, toAirportCode); // Parse the JSON string
        } catch (IOException e) {
            log.error("IOException caught while calling Ryanair API: " + e.getMessage());
            throw new RuntimeException("IOException caught while calling Ryanair API.", e);
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
                @Header("Cookie") String cookie,
                @Header("User-Agent") String userAgent,
                @Header("Accept") String accept,
                @Header("Accept-Language") String acceptLanguage,

                @Query("ADT") String adultCount,
                @Query("Origin") String fromAirportCode,
                @Query("Destination") String toAirportCode,
                @Query("IncludeConnectingFlights") String includeConnectingFlights,
                @Query("DateOut") String dateOut,
                @Query("RoundTrip") String roundTrip,
                @Query("ToUs") String termsOfUse
        );
    }
}

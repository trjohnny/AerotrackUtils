package com.aerotrack.utils.clients.api;

import com.aerotrack.model.entities.Airport;
import com.aerotrack.model.entities.Flight;
import com.aerotrack.model.entities.FlightList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class RyanairApiClient {

    private final RyanairApiService ryanairApiService;
    public static RyanairApiClient create() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.ryanair.com/api/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        return new RyanairApiClient(retrofit.create(RyanairApiService.class));
    }

    public List<String> getAirportConnections(String airportCode) {
        try {
            Response<String> response = ryanairApiService.getAirportConnections(airportCode).execute();
            if(!response.isSuccessful())
            {
                log.error("Unsuccessful response from Ryanair API when trying to get airport connections: {}", response);
                throw new HttpException(response);
            }

            String responseBody = response.body();
            if(responseBody == null)
            {
                log.error("Null response from Ryanair API.");
                throw new HttpException(response);
            }

            return parseAirportConnections(responseBody);
        } catch (IOException  e) {
            log.error("Error in calling Ryanair API for airport connections: " + e.getMessage());
            throw new RuntimeException("Caught exception while getting airport connections.", e);
        }
    }
    private List<String> parseAirportConnections(String jsonResponse) {
        List<String> connections = new ArrayList<>();
        try {
            JSONArray connectionsArray = new JSONArray(jsonResponse);

            for (int i = 0; i < connectionsArray.length(); i++) {
                JSONObject connection = connectionsArray.getJSONObject(i);

                String arrivalAirportCode = connection.getJSONObject("arrivalAirport").getString("code");
                connections.add(arrivalAirportCode);
            }
        } catch (NullPointerException | JSONException e) {
            log.error("Error parsing airport connections: " + e.getMessage());
            throw new RuntimeException("Caught exception while parsing airport connections.", e);
        }

        return connections;
    }
    public List<Airport> getAvailableAirports() {
        try {
            Response<String> response = ryanairApiService.getActiveAirports().execute();
            if(!response.isSuccessful())
            {
                log.error("Unsuccessful response from Ryanair API when trying to get available airports: {}", response);
                throw new HttpException(response);
            }

            String responseBody = response.body();
            if(responseBody == null)
            {
                log.error("Null response from Ryanair API.");
                throw new HttpException(response);
            }

            return parseAvailableAirports(responseBody);
        } catch (IOException  e) {
            log.error("Error in calling Ryanair API for available airports: " + e.getMessage());
            throw new RuntimeException("Caught exception while getting available airports.", e);
        }
    }

    private List<Airport> parseAvailableAirports(String jsonResponse) {
        List<Airport> availableAirports = new ArrayList<>();
        try {
            JSONArray airportsArray = new JSONArray(jsonResponse);

            for (int i = 0; i < airportsArray.length(); i++) {
                JSONObject airportObj = airportsArray.getJSONObject(i);

                String airportCode = airportObj.getString("code");
                String airportName = airportObj.getString("name");
                String countryCode = airportObj.getJSONObject("country").getString("code");

                // Creating an Airport instance
                Airport airport = Airport.builder()
                        .airportCode(airportCode)
                        .name(airportName)
                        .countryCode(countryCode)
                        .build();

                availableAirports.add(airport);
            }
        } catch (Exception e) {
            log.error("Error parsing available airports: " + e.getMessage());
            throw new RuntimeException("Caught exception while parsing available airports.", e);
        }

        return availableAirports;
    }

    public FlightList getFlights(String fromAirportCode, String toAirportCode, LocalDate date) {
        try {

            Response<String> response = ryanairApiService.getFlights(
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


    private FlightList parseFlights(String jsonResponse, String fromAirportCode, String toAirportCode) {
        FlightList result = new FlightList(new ArrayList<Flight>(), "EUR");
        List<Flight> flights = new ArrayList<>();
        String currency = "";

        try {
            JSONObject flightDetails = new JSONObject(jsonResponse);

            JSONArray trips = flightDetails.optJSONArray("trips");
            currency = flightDetails.getString("currency");
            if (trips == null || trips.isEmpty() || currency == null) {
                return result;
            }

            JSONArray dates = trips.getJSONObject(0).optJSONArray("dates");
            if (dates == null || dates.isEmpty()) {
                return result;
            }

            JSONArray flightsArray = dates.getJSONObject(0).optJSONArray("flights");
            if (flightsArray == null || flightsArray.isEmpty()) {
                return result;
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
        } catch (NullPointerException | JSONException e) {
            // Handle exception (log or throw as appropriate)
            log.error(e.getMessage());
            throw new RuntimeException("Caught exception while calling Ryanair API.", e);
        }

        result.setFlights(flights);
        result.setCurrency(currency.toLowerCase());
        return result;
    }

    public interface RyanairApiService {

        @Headers({
                "Cookie: rid.sig=jyna6R42wntYgoTpqvxHMK7H+KyM6xLed+9I3KsvYZaVt7P36AL6zp9dGFPu5uVxaIiFpNXrszr+LfNCdY3IT3oCSYLeNv/ujtjsDqOzkY66AL3V6kH2vsK+au12X21HkZ4S8GaG8CoBmm/m0rLsOKYkxtw+U3+ejBaPc15jJjKXnc3owMBg82SNbqyKjVd6Z6qcsoE25p3RmlcaHuHC3GBf1yIGtlqeQun3Mj0vmSURVPQBjK65pge2zGBymoORV6PsmZ0Nabv73gS2VkhG+Eccz5iSIRPrZm5cloi/941TFo8oiXdyqvTMx/ozox2fkvaKB2vd/goSx543TxPdKoGKRLaDY3FoIepe6I46UFvXEZYszzugXHYRnp0lbIn/HPyvHH/iW/TXRrqsELQabKd1hH+Ut1ZgpfsKEtwDVyL7mVvi1qEOqHddSVKCN/439KxQqi9K03dQDm+knQaLRpzZL8EYqCeSaosMOeEhc2CAYWLW2D5jH5iTot0YiyaN3QJFE59H3MYDpGjoZfTfen83yVSpT8DBahOOr6Eibv62bKQXBxel5kIm75dZB26iUzmkBs1Iags291UJ8wpu/GtBD1rghlZoRJt9u3ASkAj3P85dBcV8MwGykVWJ4mCO; mkt=/gb/en/; .AspNetCore.Session=CfDJ8NJy2CjeBXdEiTlIkEo9jP1x6eP6igWNkoeL9uCto1qdz97HQLRlCAJWIhw97YY5uemEBnTrcLNnoB7lqKOnEJRzF%2F4yhGKN9A5COUteaQmZduO6o2whfYqdyD1Qd%2B%2BH6EXjFP4cd0DtvPDwguugs%2Bc684C2CSfw16lyvYFtSkvU; fr-correlation-id=b8d2a2fe-383b-44fa-8c4a-ba6cab8c994d; rid=034ed121-51ec-4cac-9196-97a50ee42d2c; RY_COOKIE_CONSENT=true; STORAGE_PREFERENCES={\"STRICTLY_NECESSARY\":true,\"PERFORMANCE\":false,\"FUNCTIONAL\":false,\"TARGETING\":false,\"SOCIAL_MEDIA\":false,\"PIXEL\":false,\"GANALYTICS\":true,\"__VERSION\":2}; myRyanairID=",
                "User-Agent: Mozilla/5.0 (Linux; Android 10; Pixel 3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Mobile Safari/537.36",
                "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
                "Accept-language: en-US,en;q=0.5"
        })
        @GET("booking/v4/en-en/availability")
        retrofit2.Call<String> getFlights(
                @Query("ADT") String adultCount,
                @Query("Origin") String fromAirportCode,
                @Query("Destination") String toAirportCode,
                @Query("IncludeConnectingFlights") String includeConnectingFlights,
                @Query("DateOut") String dateOut,
                @Query("RoundTrip") String roundTrip,
                @Query("ToUs") String termsOfUse
        );

        @GET("views/locate/5/airports/en/active")
        retrofit2.Call<String> getActiveAirports();

        @GET("views/locate/searchWidget/routes/en/airport/{Airport}")
        retrofit2.Call<String> getAirportConnections(
                @Path("Airport") String airport
        );
    }
}

package com.aerotrack.utils.clients.api;

import com.aerotrack.model.entities.TimetableRequest;
import com.aerotrack.model.entities.WizzAirFlight;
import com.aerotrack.model.entities.Flight;
import com.aerotrack.model.entities.FlightList;
import com.aerotrack.model.exceptions.DirectionNotAvailableException;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

@Slf4j
public class WizzairApiClient implements AirlineApiClient{
    private final WizzAirApiService wizzAirApiService;

    public WizzairApiClient(WizzAirApiService wizzAirApiService) {
        this.wizzAirApiService = wizzAirApiService;
    }

    public static WizzairApiClient create() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://be.wizzair.com/") // Adjust the base URL
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return new WizzairApiClient(retrofit.create(WizzAirApiService.class));
    }

    public FlightList getFlights(String fromAirportCode, String toAirportCode, LocalDate date) {

        String fromDateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String toDateString = date.plusDays(42).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        TimetableRequest timetableRequest = TimetableRequest.builder()
                .adultCount(1)
                .childCount(0)
                .infantCount(0)
                .priceType("regular")
                .flightList(List.of(
                        WizzAirFlight.builder()
                                .departureStation(fromAirportCode)
                                .arrivalStation(toAirportCode)
                                .from(fromDateString)
                                .to(toDateString)
                                .build(),
                        WizzAirFlight.builder()
                                .departureStation(toAirportCode)
                                .arrivalStation(fromAirportCode)
                                .from(fromDateString)
                                .to(toDateString)
                                .build()
                ))
                .build();

        try {
            Response<String> response = wizzAirApiService.getTimetable(timetableRequest).execute();

            if (!response.isSuccessful() && response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                JSONObject json = new JSONObject(errorBody);
                JSONArray validationCodes = json.getJSONArray("validationCodes");
                String reason = validationCodes.getString(0);

                if (! reason.equals("InvalidMarket")) {
                    log.error("Error body: " + errorBody);
                    throw new RuntimeException("Unsuccessful response from WizzAir API.");
                }

                log.warn("Departure/destination combination not found in wizzair back end.");
                throw new DirectionNotAvailableException("Departure/destination combination not found in wizzair back end.");
            } else if (!response.isSuccessful()) {
                log.error("Unsuccessful response from WizzAir API: {}", response);
                throw new RuntimeException("Unsuccessful response from WizzAir API.");
            }

            String responseBody = response.body();
            if (responseBody == null) {
                log.error("Null response from WizzAir API.");
                throw new RuntimeException("Null response from WizzAir API.");
            }

            return parseFlights(responseBody);
        } catch (IOException e) {
            log.error("IOException caught while calling WizzAir API: " + e.getMessage());
            throw new RuntimeException("IOException caught while calling WizzAir API.", e);
        }
    }

    private FlightList parseFlights(String jsonResponse) {
        JSONObject json = new JSONObject(jsonResponse);
        JSONArray outboundFlights = json.getJSONArray("outboundFlights");
        List<Flight> flights = new ArrayList<>();
        String currency = "";

        for (int i = 0; i < outboundFlights.length(); i++) {
            JSONObject flightJson = outboundFlights.getJSONObject(i);
            String departureDateTime = flightJson.getJSONArray("departureDates").getString(0);
            double price = flightJson.getJSONObject("price").getDouble("amount");
            currency = flightJson.getJSONObject("price").getString("currencyCode").toLowerCase();

            Flight flight = Flight.builder()
                    .direction(flightJson.getString("departureStation") + "-" + flightJson.getString("arrivalStation"))
                    .departureDateTime(departureDateTime)
                    .arrivalDateTime(departureDateTime)
                    .airline("Wizzair")
                    .price(price)
                    .build();

            flights.add(flight);
        }

        return new FlightList(flights, currency);
    }

    public interface WizzAirApiService {
        @Headers({
                "authority: be.wizzair.com",
                "accept: application/json, text/plain, */*",
                "origin: https://wizzair.com",
                "user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36",
                "content-type: application/json;charset=UTF-8",
                "sec-fetch-site: same-site",
                "sec-fetch-mode: cors",
                "referer: https://wizzair.com/en-gb/flights/timetable",
                "accept-language: en-GB,en;q=0.9,hu-HU;q=0.8,hu;q=0.7,en-US;q=0.6"
        })
        @POST("20.6.0/Api/search/timetable")
        Call<String> getTimetable(@Body TimetableRequest requestBody);
    }
}

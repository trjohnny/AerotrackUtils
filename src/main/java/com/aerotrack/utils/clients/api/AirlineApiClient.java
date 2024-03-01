package com.aerotrack.utils.clients.api;

import com.aerotrack.model.entities.FlightList;
import java.time.LocalDate;

public interface AirlineApiClient {
    FlightList getFlights(String fromAirportCode, String toAirportCode, LocalDate date);
}

package com.example.fn;

import com.fnproject.fn.api.FnConfiguration;
import com.fnproject.fn.api.RuntimeContext;
import com.goplacesairlines.api.GoPlacesAirlines;

import java.util.Date;

public class FlightFunction {

    private GoPlacesAirlines apiClient;

    public static class FlightBookingRequest {
        public Date departureTime;
        public String flightCode;
    }

    public static class FlightBookingResponse {
        public String confirmation;
    }

    @FnConfiguration
    public void configure(RuntimeContext ctx) {
        String airlineApiUrl = ctx.getConfigurationByKey("FLIGHT_API_URL")
                .orElseThrow(() -> new RuntimeException("No URL endpoint was provided."));

        String airlineApiSecret = ctx.getConfigurationByKey("FLIGHT_API_SECRET")
                .orElseThrow(() -> new RuntimeException("No secret was provided."));

        apiClient = new GoPlacesAirlines(airlineApiUrl, airlineApiSecret);

    }

    public FlightBookingResponse book(FlightBookingRequest input) {
        FlightBookingResponse res = new FlightBookingResponse();
        res.confirmation = apiClient.bookFlight(input.flightCode, input.departureTime).confirmation;
        return res;
    }
}
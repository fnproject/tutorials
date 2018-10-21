package com.example.fn;

import com.fnproject.fn.api.FnConfiguration;
import com.fnproject.fn.api.RuntimeContext;
import com.goplacescars.api.GoPlacesCars;


public class CarFunction {

    private GoPlacesCars apiClient;

    public static class carBookingRequest {
        public String model;
    }

    public static class carBookingResponse {
        public String confirmation;
    }

    @FnConfiguration
    public void configure(RuntimeContext ctx) {
        String carApiUrl = ctx.getConfigurationByKey("CAR_API_URL")
                .orElseThrow(() -> new RuntimeException("No URL endpoint was provided."));

        String carApiSecret = "Todo";
        //String carApiSecret = ctx.getConfigurationByKey("HOTEL_API_SECRET")
        //        .orElseThrow(() -> new RuntimeException("No secret was provided."));

        apiClient = new GoPlacesCars(carApiUrl, carApiSecret);

    }

    public carBookingResponse book(carBookingRequest input) {

        System.out.println("-----> carBookingResponse : " +input.model );
        carBookingResponse res = new carBookingResponse();
        res.confirmation = apiClient.bookCar(input.model).confirmation;
        return res;
    }
}